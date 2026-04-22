package com.ccommit.fashionserver.service;

import com.ccommit.fashionserver.common.exception.ErrorCode;
import com.ccommit.fashionserver.common.exception.FashionServerException;
import com.ccommit.fashionserver.dto.UserDto;
import com.ccommit.fashionserver.dto.UserType;
import com.ccommit.fashionserver.jwt.JwtTokenProvider;
import com.ccommit.fashionserver.mapper.UserMapper;
import com.ccommit.fashionserver.utils.BcryptEncoder;
import jakarta.validation.constraints.NotBlank;
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
    public UserDto completeOAuthProfile(String token, UserDto userDto) {
        String rawToken = token.replace("Bearer ", "");

        // 임시 토큰에서 정보 추출
        String email = jwtTokenProvider.getEmail(rawToken);
        String oauthId = jwtTokenProvider.getOauthId(rawToken);
        String provider = jwtTokenProvider.getProvider(rawToken);

        userDto.setUserId(email);
        userDto.setOauthProvider(provider);
        userDto.setOauthId(oauthId);
        userDto.setIsProfileComplete(1);
        userDto.setUserType(UserType.USER);
        userDto.setJoin(true);
        userDto.setWithdraw(false);

        String encryptedPassword = encrypt.hashPassword(userDto.getPassword());
        userDto.setPassword(encryptedPassword);

        userMapper.signUpOAuth(userDto);
        log.info("[추가 정보 입력] 완료 email: {}", email);

        UserDto result = userMapper.findByUserInfo(userDto.getUserId());
        return result;
    }

    @Transactional
    public UserDto signUp(UserDto userDto) {
        UserDto result = new UserDto();
        String joinPossibleDate = "";
        if (isExistId(userDto.getUserId())) {
            throw new FashionServerException(ErrorCode.USER_INSERT_DUPLICATE_ERROR.getMessage(),
                    ErrorCode.USER_INSERT_DUPLICATE_ERROR.getStatus());
        } else {

            joinPossibleDate = userMapper.getJoinPossibleDate(userDto.getUserId());
            log.info("[재가입 가능 날짜] : {} ", joinPossibleDate);

            if (joinPossibleDate != null) {
                if (userMapper.isJoinPossible(userDto.getUserId(), joinPossibleDate) == 1) {
                    log.info("[탈퇴날짜 기준으로 30일 이내로 재가입 불가]");
                    throw new FashionServerException(ErrorCode.USER_ALREADY_WITHDRAWN_ERROR.getMessage(),
                            ErrorCode.USER_ALREADY_WITHDRAWN_ERROR.getStatus());
                }
            }

            // UserType 유효성 검사 + set
            UserType validUserType = Arrays.stream(UserType.values())
                    .filter(userType -> userDto.getUserType().equals(userType))
                            .findFirst()
                    .orElseThrow(() -> {
                        log.debug("존재하지 않는 회원 타입입니다.");
                        return new FashionServerException(
                                ErrorCode.USER_TYPE_NOT_FOUND_ERROR.getMessage(),
                                ErrorCode.USER_TYPE_NOT_FOUND_ERROR.getStatus()
                        );
                    });

            userDto.setUserType(validUserType);
            userDto.setPassword(encrypt.hashPassword(userDto.getPassword()));
            userDto.setJoin(true);
            userDto.setWithdraw(false);

            userMapper.signUp(userDto);
            result = userMapper.findByUserInfo(userDto.getUserId());
        }
        return result;
    }

    @Transactional
    public int userWithdraw(int id) {
        return userMapper.userWithdraw(id);
    }

    @Transactional
    public void userInfoUpdate(int id, UserDto userDto) {
        if (!isExistId(userDto.getUserId())) {
            throw new FashionServerException(ErrorCode.USER_NOT_FOUND_ERROR.getMessage(), 604);
        } else {
            if (!StringUtils.isBlank(userDto.getPassword())) {
                userDto.setPassword(encrypt.hashPassword(userDto.getPassword()));
            }
            if (!StringUtils.isBlank(userDto.getPhoneNumber())) {
                userDto.setPhoneNumber(userDto.getPhoneNumber());
            }
            if (!StringUtils.isBlank(userDto.getAddress())) {
                userDto.setAddress(userDto.getAddress());
            }
            userDto.setId(id);
            userMapper.userInfoUpdate(userDto);
        }
    }

    public UserDto passwordCheck(String userId, String password) {
        UserDto result = new UserDto();
        boolean isMachPassword = false;
        String hashedPassword = "";
        if (!isExistId(userId))
            throw new FashionServerException(ErrorCode.USER_NOT_FOUND_ERROR.getMessage(), 604);
        else
            hashedPassword = userMapper.findByUserInfo(userId).getPassword();

        if (StringUtils.isBlank(hashedPassword))
            throw new NullPointerException(ErrorCode.INPUT_NULL_ERROR.getMessage());
        else
            isMachPassword = encrypt.isMach(password, hashedPassword);

        if (isMachPassword)
            result = userMapper.findByUserInfo(userId);
        return result;
    }

    public UserDto login(@NotBlank String userId, @NotBlank String password) {
        UserDto userDto = passwordCheck(userId, password);

        if (userDto == null || userDto.getId() == 0) {
            throw new FashionServerException(ErrorCode.LOGIN_FAIL_ERROR.getMessage(), 607);
        }

        // 세션 대신 JWT 토큰 발급
        String token = jwtTokenProvider.generateToken(userDto.getId(), userDto.getUserType());
        log.info("[로그인] 완료 userId: {}", userId);

        // 토큰 UserDto에 담아서 응답
        userDto.setToken(token);
        return userDto;
    }
}
