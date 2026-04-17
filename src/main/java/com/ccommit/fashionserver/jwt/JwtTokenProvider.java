package com.ccommit.fashionserver.jwt;

import com.ccommit.fashionserver.dto.UserType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    /* 일반 회원용 토큰 생성 */
    public String generateToken(int userId, UserType userType) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("userType", userType)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    /* 신규 회원용 토큰 생성 메서드 추가 */
    public String generateTempToken(String email, String oauthId, String provider) {
        return Jwts.builder()
                .setSubject(email)
                .claim("oauthId", oauthId)
                .claim("provider", provider)
                .claim("isTemp", true)      // 임시 토큰 표시
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    public int getUserId(String token) {
        return Integer.parseInt(getClaims(token).getSubject());
    }

    public String getEmail(String token) {
        return getClaims(token).getSubject();
    }

    public String getOauthId(String token) {
        return getClaims(token).get("oauthId", String.class);
    }

    public String getProvider(String token) {
        return getClaims(token).get("provider", String.class);
    }

    /* 공통 토큰 파싱 - 중복 코드 제거 : secretKey로 토큰 위변조 여부 검증 */
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
