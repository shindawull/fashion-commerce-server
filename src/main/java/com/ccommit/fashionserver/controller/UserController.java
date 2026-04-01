package com.ccommit.fashionserver.controller;

import com.ccommit.fashionserver.dto.UserDto;
import com.ccommit.fashionserver.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Controller + @ResponseBody = @RestController
 * @ResponseBody를 붙여서 JSON을 만들었지만,
 * @RestController로 쉽게 알아서 전송 가능한 문자열 만들어준다.
 */
@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * @RequestMapping(method = RequestMethod.POST, path="")
     * 아래의 @PostMapping("")와 동일. Post일 경우 간결하게 원하면 지금처럼 작성하면 된다.
     */
    @PostMapping("/sign-up")
    public int signUp(@Valid UserDto userDto) {
        int result = result = userService.signUp(userDto);
        return result;
    }

    @PostMapping("/withdraw")
    public int userWithdraw(@Valid String userId) {
        return userService.userWithdraw(userId);
    }

    @PatchMapping("")
    public int userInfoUpdate(UserDto userDto) {
        int result = 0;
        if (StringUtils.isBlank(userDto.getUserId()))
            log.info("log4j2 info - 회원정보 수정 실패: 아이디가 입력되지 않았습니다.");
        else
            result = userService.userInfoUpdate(userDto);
        return result;
    }

    @PostMapping("/login")
    public boolean login(String id, String password) {
        boolean result = false;
        if (StringUtils.isBlank(id) || StringUtils.isBlank(password))
            log.info("log4j2 info - 로그인 실패: 아이디 또는 비밀번호가 입력되지 않았습니다.");
        else
            result = userService.login(id, password);
        return result;
    }

    @GetMapping("/logout")
    public void clearSession(HttpSession session) {
        userService.clearSession(session);
        log.info("log4j2 info - 로그아웃");
    }
}
