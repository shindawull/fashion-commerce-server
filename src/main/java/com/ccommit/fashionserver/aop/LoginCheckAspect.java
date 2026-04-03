package com.ccommit.fashionserver.aop;

import com.ccommit.fashionserver.utils.SessionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;

@Aspect
@Component
public class LoginCheckAspect {
    @Around("@annotation(com.ccommit.fashionserver.aop.LoginCheck) && @annotation(loginCheck)")
    public Object LoginSessionCheck(ProceedingJoinPoint proceedingJoinPoint, LoginCheck loginCheck) throws Throwable {
        HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getSession();
        int id = 0;
        int index = 0;
        Boolean isLoginCheck = false;
        for (int i = 0; i < loginCheck.types().length; i++) {
            if (isLoginCheck == false) {
                switch (loginCheck.types()[i].toString()) {
                    case "USER":
                        id = SessionUtils.getUserLoginSession(session);
                        break;
                    case "SELLER":
                        id = SessionUtils.getSellerLoginSession(session);
                        break;
                    case "ADMIN":
                        id = SessionUtils.getAdminLoginSession(session);
                        break;
                }
                if (id != 0)
                    isLoginCheck = true;
            }
        }
        if (isLoginCheck == false) {
            throw new Exception("로그인이 필요합니다.");
        }
        Object[] modifiedArgs = proceedingJoinPoint.getArgs();
        if (proceedingJoinPoint.getArgs() != null)
            modifiedArgs[index] = id;
        return proceedingJoinPoint.proceed(modifiedArgs);
    }

}
