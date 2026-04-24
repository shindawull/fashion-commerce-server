package com.ccommit.fashionserver.service;

import com.ccommit.fashionserver.common.exception.ErrorCode;
import com.ccommit.fashionserver.common.exception.FashionServerException;
import com.ccommit.fashionserver.dto.UserDto;
import com.ccommit.fashionserver.dto.UserType;
import com.ccommit.fashionserver.dto.request.user.OAuthProfileRequest;
import com.ccommit.fashionserver.dto.request.user.UserLoginRequest;
import com.ccommit.fashionserver.dto.request.user.UserSignUpRequest;
import com.ccommit.fashionserver.dto.request.user.UserUpdateRequset;
import com.ccommit.fashionserver.dto.response.user.UserResponse;
import com.ccommit.fashionserver.jwt.JwtTokenProvider;
import com.ccommit.fashionserver.mapper.UserMapper;
import com.ccommit.fashionserver.utils.BcryptEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final BcryptEncoder encrypt;
    private final JwtTokenProvider jwtTokenProvider;

    public boolean isExistId(String userId) {
        return userMapper.isExistId(userId) == 1;
    }

    public UserDto findByUserInfo(String userId) {
        return userMapper.findByUserInfo(userId);
    }

    @Transactional
    public UserResponse signUp(UserSignUpRequest request) {
        if (isExistId(request.getUserId())) {
            throw new FashionServerException(ErrorCode.USER_INSERT_DUPLICATE_ERROR.getMessage(),
                    ErrorCode.USER_INSERT_DUPLICATE_ERROR.getStatus());
        }

        String joinPossibleDate = userMapper.getJoinPossibleDate(request.getUserId());
        log.info("[재가입 가능 날짜] : {} ", joinPossibleDate);

        if (joinPossibleDate != null &&
                userMapper.isJoinPossible(request.getUserId(), joinPossibleDate) == 1) {
            log.info("[탈퇴날짜 기준으로 30일 이내로 재가입 불가]");
            throw new FashionServerException(ErrorCode.USER_ALREADY_WITHDRAWN_ERROR.getMessage(),
                    ErrorCode.USER_ALREADY_WITHDRAWN_ERROR.getStatus());
        }

        // UserType 유효성 검사 + set
        UserType validUserType = Arrays.stream(UserType.values())
                .filter(userType -> request.getUserType().equals(userType))
                .findFirst()
                .orElseThrow(() -> new FashionServerException(
                        ErrorCode.USER_TYPE_NOT_FOUND_ERROR.getMessage(),
                        ErrorCode.USER_TYPE_NOT_FOUND_ERROR.getStatus()
                ));

        UserDto userDto = UserDto.builder()
                .userId(request.getUserId())
                .password(encrypt.hashPassword(request.getPassword()))
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .userType(validUserType)
                .isJoin(true)
                .isWithdraw(false)
                .build();

        userMapper.signUp(userDto);
        UserDto result = userMapper.findByUserInfo(userDto.getUserId());
        return UserResponse.from(result);
    }

    public UserResponse login(UserLoginRequest request) {
        UserDto userDto = passwordCheck(request.getUserId(), request.getPassword());

        if (userDto == null || userDto.getId() == 0) {
            throw new FashionServerException(ErrorCode.LOGIN_FAIL_ERROR.getMessage(),
                    ErrorCode.LOGIN_FAIL_ERROR.getStatus());
        }

        String token = jwtTokenProvider.generateToken(userDto.getId(), userDto.getUserType());
        log.info("[로그인] 완료 userId: {}", request.getUserId());

        userDto.setToken(token);
        return UserResponse.from(userDto);
    }

    @Transactional
    public void userInfoUpdate(int id, UserUpdateRequset requset) {
        UserDto userDto = userMapper.findById(id);
        if (userDto == null) {
            throw new FashionServerException(ErrorCode.USER_NOT_FOUND_ERROR.getMessage(),
                    ErrorCode.USER_NOT_FOUND_ERROR.getStatus());
        }

        UserDto updateDto = UserDto.builder()
                .id(id)
                .password(StringUtils.isBlank(requset.getPassword()) ?
                        userDto.getPassword() :
                        encrypt.hashPassword(requset.getPassword()))
                .address(StringUtils.isBlank(requset.getAddress()) ?
                        userDto.getAddress() : requset.getAddress())
                .phoneNumber(StringUtils.isBlank(requset.getPhoneNumber()) ?
                        userDto.getPhoneNumber() : requset.getPhoneNumber())
                .build();

        userMapper.userInfoUpdate(updateDto);
    }

    @Transactional
    public int userWithdraw(int id) {
        return userMapper.userWithdraw(id);
    }

    @Transactional
    public UserResponse completeOAuthProfile(String token, OAuthProfileRequest request) {
        String rawToken = token.replace("Bearer ", "");

        // 임시 토큰에서 정보 추출
        String email = jwtTokenProvider.getEmail(rawToken);
        String oauthId = jwtTokenProvider.getOauthId(rawToken);
        String provider = jwtTokenProvider.getProvider(rawToken);

        UserDto userDto = UserDto.builder()
                .userId(email)
                .password(encrypt.hashPassword(request.getPassword()))
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .oauthProvider(provider)
                .oauthId(oauthId)
                .isProfileComplete(1)
                .userType(UserType.USER)
                .isJoin(true)
                .isWithdraw(false)
                .build();

        userMapper.signUpOAuth(userDto);
        log.info("[추가 정보 입력] 완료 email: {}", email);

        UserDto result = userMapper.findByUserInfo(email);
        return UserResponse.from(result);
    }

    public UserDto passwordCheck(String userId, String password) {
        if (!isExistId(userId))
            throw new FashionServerException(
                    ErrorCode.USER_NOT_FOUND_ERROR.getMessage(),
                    ErrorCode.USER_NOT_FOUND_ERROR.getStatus());

        UserDto userDto = userMapper.findByUserInfo(userId);
        if(StringUtils.isBlank(userDto.getPassword()))
            throw new FashionServerException(
                    ErrorCode.INPUT_NULL_ERROR.getMessage(),
                    ErrorCode.INPUT_NULL_ERROR.getStatus());

        if(!encrypt.isMach(password, userDto.getPassword()))
            throw new FashionServerException(
                    ErrorCode.LOGIN_FAIL_ERROR.getMessage(),
                    ErrorCode.LOGIN_FAIL_ERROR.getStatus());

        return userDto;
    }
}
