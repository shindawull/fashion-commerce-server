package com.ccommit.fashionserver.dto.request.user;

import com.ccommit.fashionserver.validation.PhoneNumCheck;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateRequset {

    private String password;

    private String address;

    @PhoneNumCheck
    private String phoneNumber;
}
