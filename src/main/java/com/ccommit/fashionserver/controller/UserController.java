package com.ccommit.fashionserver.controller;

import com.ccommit.fashionserver.aop.LoginCheck;
import com.ccommit.fashionserver.common.CommonResponse;
import com.ccommit.fashionserver.dto.request.user.OAuthProfileRequest;
import com.ccommit.fashionserver.dto.request.user.UserLoginRequest;
import com.ccommit.fashionserver.dto.request.user.UserSignUpRequest;
import com.ccommit.fashionserver.dto.request.user.UserUpdateRequset;
import com.ccommit.fashionserver.dto.response.user.UserResponse;
import com.ccommit.fashionserver.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<CommonResponse<UserResponse>> signUp(@Valid @RequestBody UserSignUpRequest request) {
        log.info("[회원가입] 요청: {}", request.getUserId());
        UserResponse result = userService.signUp(request);
        return ResponseEntity.ok(
                new CommonResponse<>(HttpStatus.OK, "SUCCESS",
                        request.getUserId() + "님 정상적으로 가입되었습니다.", result));
    }

    @PostMapping("/login")
    public ResponseEntity<CommonResponse<UserResponse>> login(
            @Valid @RequestBody UserLoginRequest request) {
        log.info("[로그인] 요청 userId: {}", request.getUserId());
        UserResponse result = userService.login(request);
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, "SUCCESS",
                request.getUserId() + " 회원 로그인", result));
    }

    @PatchMapping("")
    @LoginCheck(types = {LoginCheck.UserType.USER, LoginCheck.UserType.SELLER})
    public ResponseEntity<CommonResponse<String>> userInfoUpdate(Integer loginSession,
                                                                 @RequestBody UserUpdateRequset requset) {
        userService.userInfoUpdate(loginSession, requset);
        log.info("[회원수정] 요청 userId: ", loginSession);
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, "SUCCESS",
                "정상적으로 회원정보가 수정되었습니다.", null));
    }

    @PostMapping("/withdraw")
    @LoginCheck(types = {LoginCheck.UserType.USER})
    public ResponseEntity<CommonResponse<String>> userWithdraw(Integer loginSession) {
        log.info("[회원탈퇴] 요청 loginSession: {}", loginSession);
        userService.userWithdraw(loginSession);
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, "SUCCESS",
                "정상적으로 탈퇴되었습니다.", null));
    }

    /* 사용자 인증 후 비밀번호 찾기 : SMTP 로 구현 도전 예정 */

    @GetMapping("/logout")
    @LoginCheck(types = {LoginCheck.UserType.USER, LoginCheck.UserType.SELLER
            , LoginCheck.UserType.ADMIN})
    public ResponseEntity<CommonResponse<String>> logout(Integer loginSession) {
        log.info("[로그아웃] userId: {}", loginSession);
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, "SUCCESS",
                "정상적으로 로그아웃 되었습니다.", null));
    }

    /* 브라우저에서 구글 로그인
    http://localhost:8080/oauth2/authorization/google */
    @PostMapping("/oauth/profile")
    public ResponseEntity<CommonResponse<UserResponse>> completeOAuthProfile(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody OAuthProfileRequest request) {
        log.info("[추가 정보 입력] 요청");
        UserResponse result = userService.completeOAuthProfile(token, request);
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, "SUCCESS",
                result.getUserId() + "님 정상적으로 가입되었습니다.", result));
    }
}
