package com.ccommit.fashionserver.utils;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BcryptEncoder implements EncryptHelper {
    // 평문비밀번호를 salt를 사용하여 암호화
    @Override
    public String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    // 평문비밀번호와 DB에 저장된 암호화비밀번호(hashed) 값과 비교
    @Override
    public boolean isMach(String plainTextPassword, String hashed) {
        return BCrypt.checkpw(plainTextPassword, hashed);
    }
}
