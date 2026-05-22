package com.ccommit.fashionserver.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * packageName    : com.ccommit.fashionserver.config
 * fileName       : TossPaymentConfig
 * author         : juoiy
 * date           : 2023-10-14
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-10-14        juoiy       최초 생성
 */
@Configuration
@Getter
public class TossPaymentConfig {
    @Value("${payment.toss.test_client_api_key}")
    private String clientApiKey;
    @Value("${payment.toss.test_secrete_api_key}")
    private String secretKey;
    @Value("${payment.toss.payment_url}")
    private String tossPaymentUrl;
    @Value("${payment.toss.payment_api_key}")
    private String tossPaymentApiKey;

}
