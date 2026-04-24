package com.ccommit.fashionserver.aop;

import com.ccommit.fashionserver.common.exception.ErrorCode;
import com.ccommit.fashionserver.common.exception.FashionServerException;
import com.ccommit.fashionserver.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class LoginCheckAspect {

    private final JwtTokenProvider jwtTokenProvider;

    @Around("@annotation(com.ccommit.fashionserver.aop.LoginCheck) && @annotation(loginCheck)")
    public Object LoginSessionCheck(ProceedingJoinPoint proceedingJoinPoint,
                                    LoginCheck loginCheck) throws Throwable {
        // 1. 요청 헤더에서 JWT 토큰 꺼내기
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest();

        String bearerToken = request.getHeader("Authorization");

        if (bearerToken == null || !bearerToken.startsWith("Bearer "))
            throw new Exception("로그인이 필요합니다.");

        String token = bearerToken.replace("Bearer ", "");

        // 2. 토큰에서 userId, userType 꺼내기
        int userId = 0;
        try {
            Claims claims = jwtTokenProvider.getClaims(token);
            String userType = claims.get("userType", String.class);
            userId = jwtTokenProvider.getUserId(token);

            // 3. userType 권한 체크
            boolean isLoginCheck = false;
            for (LoginCheck.UserType type : loginCheck.types()) {
                if (type.toString().equals(userType)) {
                    isLoginCheck = true;
                    break;
                }
            }

            if (!isLoginCheck) {
                throw new FashionServerException(ErrorCode.USER_NOT_AUTHORIZED_ERROR.getMessage()
                        , ErrorCode.USER_NOT_AUTHORIZED_ERROR.getStatus());

            }
        } catch (FashionServerException e) {
            throw e;
        } catch (Exception e) {
            throw new FashionServerException(ErrorCode.USER_NOT_AUTHORIZED_ERROR.getMessage()
                    , ErrorCode.USER_NOT_AUTHORIZED_ERROR.getStatus());
        }

        // 4. controller 첫 번째 파라미터에 userId 주입
        Object[] modifiedArgs = proceedingJoinPoint.getArgs();
        if (modifiedArgs != null)
            modifiedArgs[0] = userId;

        return proceedingJoinPoint.proceed(modifiedArgs);
    }
}
