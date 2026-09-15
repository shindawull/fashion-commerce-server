package com.ccommit.fashionserver.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
@Getter
public class TossPaymentConfig {
    @Value("${payment.toss.test_client_api_key}")
    private String clientApiKey;

    @Value("${payment.toss.test_secrete_api_key}")
    private String secretKey;

    @Value("${payment.toss.payment_url}")
    private String tossPaymentUrl;

    /*@Value("${payment.toss.payment_api_key}")
    private String tossPaymentApiKey;*/

    /* 토스페이먼츠 Basic 인증 헤더
     * 시크릿 키 뒤에 콜론을 붙여 Base64 인코딩한다.
     * 콜론은 "비밀번호 없음" 을 의미하며 생략하면 인증에 실패한다.
     * */
    public String getAuthorizationHeader() {
        String encoded = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

}
