package com.ccommit.fashionserver.oauth;

import com.ccommit.fashionserver.dto.UserDto;
import com.ccommit.fashionserver.jwt.JwtTokenProvider;
import com.ccommit.fashionserver.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 1. 구글에서 받아온 사용자 정보 꺼내기
        String email = oAuth2User.getAttribute("email");
        String oauthId = oAuth2User.getAttribute("sub");
        String provider = "google";

        log.info("[OAuth2 로그인] email : {}", email);

        // 2. DB에 이미 가입된 회원인지 확인
        UserDto existUser = userService.findByUserInfo(email);

        response.setContentType("application/json;charset=UTF-8");

        if (existUser != null) {
            // 3-1. 기존 회원이면 바로 JWT 토큰 발급
            String token = jwtTokenProvider.generateToken(
                    existUser.getId(), existUser.getUserType());
            // 로그인 완료
            log.info("[OAuth2] 기존 회원 로그인 email : {}", email);
            response.getWriter().write(
                    "{\"token\": \"" + token + "\"," +
                            "\"isProfileComplete\": true," +
                            "\"message\": \"로그인 성공\"}"
            );
        } else {
            // 3-2. 신규 회원이면 임시 토큰 발급: DB insert는 아직 안해요
            String tempToken = jwtTokenProvider.generateTempToken(email, oauthId, provider);
            // 추가 정보 입력 필요
            log.info("[OAuth2] 신규 회원 감지 email : {}", email);
            response.getWriter().write(
                    "{\"token\": \"" + tempToken + "\"," +
                            "\"isProfileComplete\": false," +
                            "\"message\": \"추가 정보 입력이 필요합니다.\"}"
            );
        }
    }
}
