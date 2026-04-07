package com.ccommit.fashionserver.dto;

import com.ccommit.fashionserver.service.PhoneNumCheck;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("phone_number")
    private String phoneNumber;    //휴대폰번호

    @JsonProperty("create_date")
    private Date createDate;       //생성날짜

    @JsonProperty("update_date")
    private Date updateDate;       //수정날짜

    @JsonProperty("is_join")
    private boolean isJoin;        //가입상태

    @JsonProperty("is_withdraw")
    private boolean isWithdraw;    //탈퇴상태

    @NotBlank
    @JsonProperty("user_id")
    private String userId;         //아이디

    @JsonProperty("user_type")
    private UserType userType;      //관리자여부
}
