package com.ccommit.fashionserver.mapper;

import com.ccommit.fashionserver.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    int signUp(UserDto userDto);

    int isExistId(String userId);

    int userWithdraw(int id);

    int userInfoUpdate(UserDto userDto);

    UserDto findByUserInfo(String userId);

    int isJoinPossible(String userId, String joinPossibleDate);

    String getJoinPossibleDate(String userId);

    int signUpOAuth(UserDto userDto);

    UserDto findById(int id);
}