package com.ccommit.fashionserver.dto.request.user;

import com.ccommit.fashionserver.dto.UserType;
import com.ccommit.fashionserver.validation.PhoneNumCheck;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserSignUpRequest {

    @NotBlank(message = "아이디를 입력해주세요.")
    private String userId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

    @NotBlank(message = "주소를 입력해주세요.")
    private String address;

    @NotBlank(message = "휴대폰 번호를 입력해주세요.")
    @PhoneNumCheck
    private String phoneNumber;

    @NotNull(message = "회원 타입을 입력해주세요.")
    private UserType userType;
}
