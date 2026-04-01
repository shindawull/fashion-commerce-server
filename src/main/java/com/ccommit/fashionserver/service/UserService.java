package com.ccommit.fashionserver.service;

import com.ccommit.fashionserver.dto.UserDto;
import com.ccommit.fashionserver.dto.UserType;
import com.ccommit.fashionserver.mapper.UserMapper;
import com.ccommit.fashionserver.utils.BcryptEncoder;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Slf4j
@Service
public class UserService {
    @Autowired
    UserMapper userMapper;

    @Autowired
    BcryptEncoder encrypt;

    public boolean isIdCheck(String userId) {
        return userMapper.isIdCheck(userId) == 1;
    }

    //회원가입
    public int signUp(UserDto userDto) {
        int result = 0;
        String regexPhoneNum = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$";

        if (isIdCheck(userDto.getUserId())) {
            throw new RuntimeException("중복된 아이디 존재");
        } else {
            //boolean isRegexPhoneNum = Pattern.matches(regexPhoneNum, userDto.getPhoneNumber());
            //if (!isRegexPhoneNum) {
            //  System.out.println("휴대폰번호를 확인해주세요. (입력 예시:010-1234-1234) ");
            //    throw new IllegalArgumentException("휴대폰번호를 확인해주세요. (입력 예시:010-1234-1234) ");
            //} else {
            userDto.setPassword(encrypt.hashPassword(userDto.getPassword()));
            userDto.setPhoneNumber(userDto.getPhoneNumber());
            userDto.setJoin(true);
            userDto.setWithdraw(false);
            log.info("userDto.getUserType()  " + userDto.getUserType());
            if (userDto.getUserType().equals(UserType.USER))
                userDto.setUserType(UserType.USER);
            else if (userDto.getUserType().equals(UserType.SELLER))
                userDto.setUserType(UserType.SELLER);
            else if (userDto.getUserType().equals(UserType.ADMIN))
                userDto.setUserType(UserType.ADMIN);

            result = userMapper.signUp(userDto);
            //}
        }
        return result;
    }

    public int userWithdraw(String userId) {
        int result = 0;
        result = userMapper.userWithdraw(userId);
        return result;
    }

    public int userInfoUpdate(UserDto userDto) {
        int result = 0;
        String regexPhoneNum = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$";
        boolean isRegexPhoneNum = true;
        if (!isIdCheck(userDto.getUserId())) {
            throw new RuntimeException("존재하지 않는 회원입니다.");
        } else {
            if (!StringUtils.isBlank(userDto.getPassword())) {
                userDto.setPassword(encrypt.hashPassword(userDto.getPassword()));
            }
            if (!StringUtils.isBlank(userDto.getPhoneNumber())) {
                isRegexPhoneNum = Pattern.matches(regexPhoneNum, userDto.getPhoneNumber());
                if (!isRegexPhoneNum) {
                    throw new RuntimeException("휴대폰번호를 확인해주세요. (입력 예시:010-1234-1234) ");
                } else {
                    userDto.setPhoneNumber(userDto.getPhoneNumber());
                }
            }
            if (!StringUtils.isBlank(userDto.getAddress())) {
                userDto.setAddress(userDto.getAddress());
            }
            if (userDto.getUserType().equals("user"))
                userDto.setUserType(UserType.USER);
            else if (userDto.getUserType().equals("seller"))
                userDto.setUserType(UserType.SELLER);
            else if (userDto.getUserType().equals(UserType.ADMIN))
                userDto.setUserType(UserType.ADMIN);
            result = userMapper.userInfoUpdate(userDto);
        }
        return result;
    }

    public boolean login(String id, String password) {
        boolean result = false;
        String hashedPassword = "";
        if (!isIdCheck(id)) {
            throw new RuntimeException("존재하지 않는 회원입니다.");
        } else {
            hashedPassword = userMapper.readUserInfo(id).getPassword();
        }
        result = encrypt.isMach(password, hashedPassword);
        return result;
    }

    public void clearSession(HttpSession session) {
        session.invalidate();
    }
}
