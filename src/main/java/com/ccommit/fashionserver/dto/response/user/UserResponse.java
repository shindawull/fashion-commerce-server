package com.ccommit.fashionserver.dto.response.user;

import com.ccommit.fashionserver.dto.UserDto;
import com.ccommit.fashionserver.dto.UserType;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Getter
@Builder
public class UserResponse {
    private int id;
    private String userId;
    private String address;
    private String phoneNumber;
    private UserType userType;
    private Date createDate;
    private Date updateDate;
    private String token;

    public static UserResponse from(UserDto userDto) {
        return UserResponse.builder()
                .id(userDto.getId())
                .userId(userDto.getUserId())
                .address(userDto.getAddress())
                .phoneNumber(userDto.getPhoneNumber())
                .userType(userDto.getUserType())
                .createDate(userDto.getCreateDate())
                .updateDate(userDto.getUpdateDate())
                .token(userDto.getToken())
                .build();
    }
}