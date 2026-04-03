package com.ccommit.fashionserver.dto;

import com.ccommit.fashionserver.service.PhoneNumCheck;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.NotBlank;
import java.sql.Date;

@Getter
@Setter
@ToString
public class UserDto {
    private int id;                //번호
    @NotBlank
    private String address;        //주소
    @NotBlank
    private String password;       //비밀번호
    @NotBlank
    @PhoneNumCheck
    private String phoneNumber;    //휴대폰번호
    private Date createDate;       //생성날짜
    private Date updateDate;       //수정날짜
    private boolean isJoin;        //가입상태
    private boolean isWithdraw;    //탈퇴상태
    @NotBlank
    private String userId;         //아이디
    private UserType userType;      //관리자여부
}
