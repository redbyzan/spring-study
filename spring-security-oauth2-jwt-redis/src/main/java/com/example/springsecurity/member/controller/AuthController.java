package com.example.springsecurity.member.controller;


import com.example.springsecurity.config.security.login.dto.AuthResponse;
import com.example.springsecurity.member.dto.SignUpRequest;
import com.example.springsecurity.member.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
//@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PreAuthorize("isAnonymous()")
    @PostMapping("/sign-up")
    public ResponseEntity<Void> signUp(@Validated @RequestBody SignUpRequest signUpRequest){
        authService.signUp(signUpRequest);
        return ResponseEntity.ok().build();
    }

    // todo 프론트에서는 두가지 토큰 모두 Bearer XXX 형식으로 줘야만 한다.
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization") String accessToken,
                                       @RequestHeader(value = "refreshToken") String refreshToken
                                       ){
        authService.logout(accessToken,refreshToken);
        return ResponseEntity.ok().build();
    }

    // RefreshToken으로 요청 받는다.
    // 애초에 필터에서 refresh토큰에 대한 모든 검증이 끝나기 때문에 여기서는 그냥 발급만 해주면 된다.
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/token/reissue")
    public ResponseEntity<AuthResponse> reissueToken(){
        return ResponseEntity.ok(authService.reissue());
    }


}

