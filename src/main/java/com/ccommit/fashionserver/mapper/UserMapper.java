package com.ccommit.fashionserver.mapper;

import com.ccommit.fashionserver.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    int signUp(UserDto userDto);

    int isIdCheck(String userId);

    int userWithdraw(String userId);

    int userInfoUpdate(UserDto userDto);

    UserDto readUserInfo(String userId);
}