package com.ccommit.fashionserver.dto;

import lombok.*;

import java.sql.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserDto {
    private int id;                //번호
    private String address;        //주소
    private String password;       //비밀번호
    private String phoneNumber;    //휴대폰번호
    private Date createDate;       //생성날짜
    private Date updateDate;       //수정날짜
    private boolean isJoin;        //가입상태
    private boolean isWithdraw;    //탈퇴상태
    private String userId;         //아이디
    private UserType userType;      //관리자여부
    private String oauthProvider;   // 소셜 로그인 제공자(google, naver)
    private String oauthId;         // 소셜 로그인 고유ID
    private int isProfileComplete;  // 추가 정보 입력 완료 여부
    private String token;           // 응답용 토큰 필드
}
