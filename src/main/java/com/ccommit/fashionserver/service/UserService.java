package com.ccommit.fashionserver.service;

import com.ccommit.fashionserver.controller.UserController;
import com.ccommit.fashionserver.dto.UserDto;
import com.ccommit.fashionserver.dto.UserType;
import com.ccommit.fashionserver.exception.ErrorCode;
import com.ccommit.fashionserver.exception.FashionServerException;
import com.ccommit.fashionserver.mapper.UserMapper;
import com.ccommit.fashionserver.utils.BcryptEncoder;
import com.ccommit.fashionserver.utils.SessionUtils;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class UserService {
    @Autowired
    private final UserMapper userMapper;

    @Autowired
    private final BcryptEncoder encrypt;

    public static final Logger logger = LogManager.getLogger(UserController.class);

    public UserService(UserMapper userMapper, BcryptEncoder encrypt) {
        this.userMapper = userMapper;
        this.encrypt = encrypt;
    }

    public boolean isExistId(String userId) {
        return userMapper.isExistId(userId) == 1;
    }

    //회원가입
    public UserDto signUp(UserDto userDto) {
        UserDto result = new UserDto();
        String joinPossibleDate = "";
        if (isExistId(userDto.getUserId())) {
            throw new FashionServerException(ErrorCode.valueOf("USER_INSERT_DUPLICATE_ERROR").getMessage(), 601);
        } else {

            joinPossibleDate = userMapper.getJoinPossibleDate(userDto.getUserId());
            logger.info("joinPossibleDate : " + joinPossibleDate);
            if (joinPossibleDate != null) {
                logger.debug("첫 가입");
                if (userMapper.isJoinPossible(userDto.getUserId(), joinPossibleDate) == 1) {
                    logger.debug("탈퇴날짜 기준으로 30일 이내로 재가입 불가");
                    throw new FashionServerException(ErrorCode.USER_NOT_AUTHORIZED_ERROR.getMessage(), 603);
                }
            }
            userDto.setPassword(encrypt.hashPassword(userDto.getPassword()));
            userDto.setPhoneNumber(userDto.getPhoneNumber());
            userDto.setJoin(true);
            userDto.setWithdraw(false);
            Arrays.stream(UserType.values())
                    .filter(userType -> userDto.getUserType().equals(userType.getName()))
                    .forEach(userType -> {
                        if (userDto.getUserType().equals(userType.getName())) {
                            userDto.setUserType(userType);
                        } else {
                            logger.debug("존재하지 않는 회원 타입입니다.");
                            throw new NullPointerException("존재하지 않는 회원 타입입니다.");
                        }
                    });
            userMapper.signUp(userDto);
            result = userMapper.readUserInfo(userDto.getUserId());
        }
        return result;
    }

    public int userWithdraw(int id) {
        return userMapper.userWithdraw(id);
    }

    public void userInfoUpdate(int id, UserDto userDto) {
        if (!isExistId(userDto.getUserId())) {
            throw new FashionServerException(ErrorCode.valueOf("USER_NOT_USING_ERROR").getMessage(), 604);
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
            throw new FashionServerException(ErrorCode.valueOf("USER_NOT_USING_ERROR").getMessage(), 604);
        else
            hashedPassword = userMapper.readUserInfo(userId).getPassword();

        if (StringUtils.isBlank(hashedPassword))
            throw new NullPointerException("패스워드를 확인해주세요.");
        else
            isMachPassword = encrypt.isMach(password, hashedPassword);

        if (isMachPassword)
            result = userMapper.readUserInfo(userId);
        return result;
    }

    public void insertSession(HttpSession session, UserDto userDto) {
        if (userDto.getUserType() == UserType.USER)
            SessionUtils.setUserLoginSession(session, userDto.getId());
        else if (userDto.getUserType() == UserType.SELLER)
            SessionUtils.setSellerLoginSession(session, userDto.getId());
        else if (userDto.getUserType() == UserType.ADMIN)
            SessionUtils.setAdminLoginSession(session, userDto.getId());
    }

    public void clearSession(HttpSession session) {
        SessionUtils.clearSession(session);
    }
}
