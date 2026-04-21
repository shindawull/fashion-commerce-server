package com.ccommit.fashionserver.controller;

import com.ccommit.fashionserver.aop.CommonResponse;
import com.ccommit.fashionserver.aop.LoginCheck;
import com.ccommit.fashionserver.dto.UserDto;
import com.ccommit.fashionserver.exception.ErrorCode;
import com.ccommit.fashionserver.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    /* 브라우저에서 구글 로그인
    http://localhost:8080/oauth2/authorization/google */
    @PostMapping("/oauth/profile")
    public ResponseEntity<CommonResponse<UserDto>> completeOAuthProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody UserDto userDto) {
        log.info("[추가 정보 입력] 요청");
        UserDto userDtoResult = userService.completeOAuthProfile(token, userDto);
        CommonResponse<UserDto> response = new CommonResponse<>(HttpStatus.OK, "SUCCESS",
                userDtoResult.getUserId() + "님 정상적으로 가입되었습니다.", userDtoResult);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<CommonResponse<UserDto>> signUp(@Valid @RequestBody UserDto userDto) {
        log.info("[회원가입] 요청: {}", userDto);
        UserDto userDtoResult = userService.signUp(userDto);
        CommonResponse<UserDto> response = new CommonResponse<>(HttpStatus.OK, "SUCCESS",
                userDto.getUserId() + "님 정상적으로 가입되었습니다.", userDtoResult);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdraw")
    @LoginCheck(types = {LoginCheck.UserType.USER})
    public ResponseEntity<CommonResponse<String>> userWithdraw(Integer loginSession) {
        log.info("[회원탈퇴] 요청 loginSession: {}", loginSession);
        userService.userWithdraw(loginSession);
        CommonResponse<String> response = new CommonResponse<>(HttpStatus.OK, "SUCCESS",
                "정상적으로 탈퇴되었습니다.", null);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("")
    @LoginCheck(types = {LoginCheck.UserType.USER, LoginCheck.UserType.SELLER})
    public ResponseEntity<CommonResponse<String>> userInfoUpdate(Integer loginSession, @RequestBody UserDto userDto) {
        userService.userInfoUpdate(loginSession, userDto);
        log.info("[회원수정] 요청 userId: ", loginSession);
        CommonResponse<String> response = new CommonResponse<>(HttpStatus.OK, "SUCCESS",
                "정상적으로 회원정보가 수정되었습니다.", null);
        return ResponseEntity.ok(response);
    }

    // 사용자 인증 후 비밀번호 찾기 : SMTP 로 구현 도전 예정

    @PostMapping("/login")
    public ResponseEntity<CommonResponse<UserDto>> login(@RequestBody UserDto userDto) {
        if (StringUtils.isBlank(userDto.getUserId()) || StringUtils.isBlank(userDto.getPassword())) {
            throw new NullPointerException(ErrorCode.INPUT_NULL_ERROR.getMessage());
        }
        log.info("[로그인] 요청 userId: {}", userDto.getUserId());
        UserDto userInfo = userService.login(userDto.getUserId(), userDto.getPassword());
        CommonResponse<UserDto> response = new CommonResponse<>(HttpStatus.OK, "SUCCESS",
                userDto.getUserId() + " 회원 로그인", userInfo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/logout")
    @LoginCheck(types = {LoginCheck.UserType.USER, LoginCheck.UserType.SELLER
            , LoginCheck.UserType.ADMIN})
    public ResponseEntity<CommonResponse<String>> logout(Integer loginSession) {
        log.info("[로그아웃] userId: {}", loginSession);
        CommonResponse<String> response = new CommonResponse<>(HttpStatus.OK, "SUCCESS",
                "정상적으로 로그아웃 되었습니다.", null);
        return ResponseEntity.ok(response);
    }
}
