package com.ccommit.fashionserver.service;

import com.ccommit.fashionserver.common.exception.ErrorCode;
import com.ccommit.fashionserver.common.exception.FashionServerException;
import com.ccommit.fashionserver.dto.UserDto;
import com.ccommit.fashionserver.dto.UserType;
import com.ccommit.fashionserver.dto.request.user.UserLoginRequest;
import com.ccommit.fashionserver.dto.request.user.UserSignUpRequest;
import com.ccommit.fashionserver.dto.request.user.UserUpdateRequset;
import com.ccommit.fashionserver.dto.response.user.UserResponse;
import com.ccommit.fashionserver.jwt.JwtTokenProvider;
import com.ccommit.fashionserver.mapper.UserMapper;
import com.ccommit.fashionserver.utils.BcryptEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    // 테스트 대상 (실제 구현체 주입)
    @InjectMocks
    private UserService userService;

    //의존성 Mock 처리
    @Mock
    private UserMapper userMapper;

    @Mock
    private BcryptEncoder encrypt;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    /* 정상적인 회원가입 요청 객체 생성 */
    private UserSignUpRequest buildSignUpRequest(String userId, UserType userType) {
        UserSignUpRequest request = new UserSignUpRequest();
        request.setUserId(userId);
        request.setPassword("password123");
        request.setAddress("서울시 강남구");
        request.setPhoneNumber("010-1234-1234");
        request.setUserType(userType);
        return request;
    }

    /* DB에서 조회되는 UserDto 생성 */
    private UserDto buildUserDto(int id, String userId, UserType userType) {
        return UserDto.builder()
                .id(id)
                .userId(userId)
                .password("$2a$10$hashedPassword")
                .address("서울시 강남구")
                .phoneNumber("010-1234-1234")
                .userType(userType)
                .isJoin(true)
                .isWithdraw(false)
                .build();
    }

    // ────────────────────────────────────────────────────────────────────────
    // 1. signUp 테스트
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("signUp() - 회원가입")
    class signUp {

        @Test
        @DisplayName("[성공] 정상 회원가입")
        void signUp_success() {
            //given
            UserSignUpRequest request = buildSignUpRequest("testUser", UserType.USER);

            given(userMapper.isExistId("testUser")).willReturn(0);              // 중복 없음
            given(userMapper.getJoinPossibleDate("testUser")).willReturn(null); // 탈퇴 이력 없음
            given(encrypt.hashPassword(anyString())).willReturn("$2a$10$hashedPassword");
            //signUp() insert 후 findByUserInfo로 결과 조회
            given(userMapper.findByUserInfo("testUser")).willReturn(buildUserDto(1, "testUser", UserType.USER));

            //when
            UserResponse result = userService.signUp(request);

            //then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo("testUser");

            // mapper.signUp()이 정확히 1번 호출됐는지 검증
            then(userMapper).should(times(1)).signUp(any(UserDto.class));
        }

        @Test
        @DisplayName("[실패] 이미 존재하는 userId -> USER_INSERT_DUPLICATE_ERROR")
        void signUp_fail_duplicateUserId() {
            // given
            UserSignUpRequest request = buildSignUpRequest("existUser", UserType.USER);

            given(userMapper.isExistId("existUser")).willReturn(1); //이미존재

            // when & then
            assertThatThrownBy(() -> userService.signUp(request))
                    .isInstanceOf(FashionServerException.class)
                    .hasMessage(ErrorCode.USER_INSERT_DUPLICATE_ERROR.getMessage());

            // 중복이면 signUp mapper는 호출되면 안됨
            then(userMapper).should(never()).signUp(any());

        }

        @Test
        @DisplayName("[실패] 탈퇴 후 30일 이내 재가입 시도 -> USER_ALREADY_WITHDRAWN_ERROR")
        void signUp_fail_withdrawnUserWithin30days() {
            //given
            UserSignUpRequest request = buildSignUpRequest("withdrawnUser", UserType.USER);

            given(userMapper.isExistId("withdrawnUser")).willReturn(0);
            given(userMapper.getJoinPossibleDate("withdrawnUser")).willReturn("2099-12-31"); // 아직 재가입 불가 날짜
            given(userMapper.isJoinPossible("withdrawnUser", "2099-12-31")).willReturn(1); // 재가입 불가

            //when & then
            assertThatThrownBy(() -> userService.signUp(request)).isInstanceOf(FashionServerException.class)
                    .hasMessage(ErrorCode.USER_ALREADY_WITHDRAWN_ERROR.getMessage());

        }

        @Test
        @DisplayName("[실패] 유효하지 않은 UserType -> USER_TYPE_NOT_FOUND_ERROR")
        void signUp_fail_invalidUserType() {
            //given
            // 실제 코드에서 Arrays.stream(UserType.value()) 통과하지 못하는 케이스
            UserSignUpRequest request = buildSignUpRequest("testUser", null);

            given(userMapper.isExistId("testUser")).willReturn(0);
            given(userMapper.getJoinPossibleDate("testUser")).willReturn(null);

            // when & then
            // UserType이 null이면 filter 조건에 안걸려 orElseThrow 호출
            assertThatThrownBy(() -> userService.signUp(request))
                    .isInstanceOf(FashionServerException.class)
                    .hasMessage(ErrorCode.USER_TYPE_NOT_FOUND_ERROR.getMessage());
        }
    } // end signUp

    // ────────────────────────────────────────────────────────────────────────
    // 2. Login 테스트
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("login() - 로그인")
    class login {

        @Test
        @DisplayName("[성공] 정상 로그인 -> JWT 토큰 발급")
        void login_success() {
            // given
            UserLoginRequest request = new UserLoginRequest();
            request.setUserId("testUser");
            request.setPassword("password123");

            UserDto userDto = buildUserDto(1, "testUser", UserType.USER);

            given(userMapper.isExistId("testUser")).willReturn(1);
            given(userMapper.findByUserInfo("testUser")).willReturn(userDto);
            given(encrypt.isMach("password123", userDto.getPassword())).willReturn(true);
            given(jwtTokenProvider.generateToken(1, UserType.USER)).willReturn("mocked.jwt.token");

            //when
            UserResponse result = userService.login(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getToken()).isEqualTo("mocked.jwt.token");
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 userId -> USER_NOT_FOUND_ERROR")
        void login_fail_userNotFound() {
            //given
            UserLoginRequest request = new UserLoginRequest();
            request.setUserId("noUser");
            request.setPassword("password123");

            given(userMapper.isExistId("noUser")).willReturn(0); // 없는 유저

            //when & then
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(FashionServerException.class)
                    .hasMessage(ErrorCode.USER_NOT_FOUND_ERROR.getMessage());
        }

        @Test
        @DisplayName("[실패] 비밀번호 불일치 -> LOGIN_FAIL_ERROR")
        void login_fail_wrongPassword() {
            //given
            UserLoginRequest request = new UserLoginRequest();
            request.setUserId("testUser");
            request.setPassword("wrongPassword");

            UserDto userDto = buildUserDto(1, "testUser", UserType.USER);

            given(userMapper.isExistId("testUser")).willReturn(1);
            given(userMapper.findByUserInfo("testUser")).willReturn(userDto);
            given(encrypt.isMach("wrongPassword", userDto.getPassword())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(FashionServerException.class)
                    .hasMessage(ErrorCode.LOGIN_FAIL_ERROR.getMessage());
        }

        @Test
        @DisplayName("[실패] 비밀번호가 null/blank -> INPUT_NULL_ERROR")
        void login_fail_blankPassword() {
            //given
            UserLoginRequest request = new UserLoginRequest();
            request.setUserId("testUser");
            request.setPassword("someInput");

            // DB에 저장된 비밀번호가 blank (OAuth 회원 등  예외 케이스)
            UserDto userDto = buildUserDto(1, "testUser", UserType.USER);
            userDto.setPassword("");    // 비밀번호 없는 상태

            given(userMapper.isExistId("testUser")).willReturn(1);
            given(userMapper.findByUserInfo("testUser")).willReturn(userDto);

            // when & then
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(FashionServerException.class)
                    .hasMessage(ErrorCode.INPUT_NULL_ERROR.getMessage());
        }
    } // end login

    // ────────────────────────────────────────────────────────────────────────
    // 3. userInfoUpdate 테스트
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("userInfoUpdate() - 회원정보 수정")
    class UserInfoUpdate {

        @Test
        @DisplayName("[성공] 정상 회원정보 수정")
        void userInfoUpdate_success() {
            // given
            int userId = 1;
            UserUpdateRequset request = new UserUpdateRequset();
            request.setAddress("서울시 서초구");
            request.setPassword("newPassword123!");
            request.setPhoneNumber("010-9999-8888");

            UserDto existingUser = buildUserDto(userId, "testUser", UserType.USER);

            given(userMapper.findById(userId)).willReturn(existingUser);
            given(encrypt.hashPassword("newPassword123!")).willReturn("$2a$10$newHashedPassword");

            // when
            userService.userInfoUpdate(userId, request);

            // then
            // updateDto 담아서 userInfoUpdate 호출됐는지 검증
            then(userMapper).should(times(1)).userInfoUpdate(any(UserDto.class));
        }

        @Test
        @DisplayName("[성공] 비밀번호 미입력 시 기존 비밀번호 유지")
        void userInfoUpdate_success_keepExistingPassword() {
            // given
            int userId = 1;
            UserUpdateRequset request = new UserUpdateRequset();
            request.setAddress("서울시 서초구");
            request.setPassword("");        // 비밀번호 변경 안 함
            request.setPhoneNumber("");     // 전화번호 변경 안 함

            UserDto existingUser = buildUserDto(userId, "testUser", UserType.USER);

            given(userMapper.findById(userId)).willReturn(existingUser);

            // when
            userService.userInfoUpdate(userId, request);

            // then
            // blank 비밀번호라 encrypt.hashPassword는 호출되면 안 됨
            then(encrypt).should(never()).hashPassword(any());
            then(userMapper).should(times(1)).userInfoUpdate(any(UserDto.class));
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 유저 수정 → USER_NOT_FOUND_ERROR")
        void userInfoUpdate_fail_userNotFound() {
            // given
            given(userMapper.findById(999)).willReturn(null);

            UserUpdateRequset request = new UserUpdateRequset();
            request.setAddress("서울시 서초구");

            // when & then
            assertThatThrownBy(() -> userService.userInfoUpdate(999, request))
                    .isInstanceOf(FashionServerException.class)
                    .hasMessage(ErrorCode.USER_NOT_FOUND_ERROR.getMessage());

            then(userMapper).should(never()).userInfoUpdate(any());
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // 4. userWithdraw 테스트
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("userWithdraw() - 회원탈퇴")
    class UserWithdraw {

        @Test
        @DisplayName("[성공] 정상 회원탈퇴")
        void userWithdraw_success() {
            // given
            int userId = 1;
            given(userMapper.userWithdraw(userId)).willReturn(1); // update 1건 성공

            // when
            int result = userService.userWithdraw(userId);

            // then
            assertThat(result).isEqualTo(1);
            then(userMapper).should(times(1)).userWithdraw(userId);
        }
    }
}
