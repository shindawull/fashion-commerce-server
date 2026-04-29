package com.ccommit.fashionserver.config;

import com.ccommit.fashionserver.jwt.JwtAuthenticationFilter;
import com.ccommit.fashionserver.jwt.JwtTokenProvider;
import com.ccommit.fashionserver.oauth.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (REST API라서 불필요)
                .csrf(csrf ->
                        csrf.disable())
                // 세션 사용 안함 (JWT 방식이라서)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 경로별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 이 경로들은 로그인 없이 접근 가능
                        .requestMatchers(
                                "/users/sign-up",
                                "/users/login",
                                "/users/oauth/profile",
                                "/products/**"
                        ).permitAll()
                        // 나머지 인증 필요
                        .anyRequest().authenticated()
                )
                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2LoginSuccessHandler)
                )

                // JWT 필터를 Spring Security 필터 앞에 추가
                // 모든 요청에서 JWT 검증을 먼저 해요
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                );
        return http.build();
    }

}
