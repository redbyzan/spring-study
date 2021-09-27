package com.example.demo.controller;

import com.example.demo.controller.dto.MemberRequestDto;
import com.example.demo.controller.dto.MemberResponseDto;
import com.example.demo.controller.dto.TokenDto;
import com.example.demo.controller.dto.TokenRequestDto;
import com.example.demo.model.social.KakaoProfile;
import com.example.demo.service.AuthService;
import com.example.demo.service.KakaoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Optional;

@Api(tags = {"1. Sign"}) // swagger 제목
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final KakaoService kakaoService;

    // 들어오는 헤더에 관해서도 swagger 설명 추가 가능
   /*
    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 access_token", required = true, dataType = "String", paramType = "header")
    })
     */
    @ApiOperation(value = "로그인", notes = "이메일 회원 가입")
    @PostMapping("/signup")
    public ResponseEntity<MemberResponseDto> signup(@ApiParam(value = "회원입력정보", required = true)@RequestBody MemberRequestDto memberRequestDto) {
        return ResponseEntity.ok(authService.signup(memberRequestDto));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody MemberRequestDto memberRequestDto) {
        return ResponseEntity.ok(authService.login(memberRequestDto));
    }

    // 로그아웃은 소셜이나 그냥이나 무관 어쩌피 jwt 토큰으로 접근함??
    // todo 카카오 api에 로그아웃 탭이 있는데 확인해보자.
    @PostMapping("/logout")
    public ResponseEntity logout(@RequestBody TokenRequestDto tokenRequestDto) {
        authService.logout(tokenRequestDto);
        return ResponseEntity.ok().build();
    }

    // todo 재발급 자체를 필터에서 처리할 수 있을 것 같은데
    // https://velog.io/@ehdrms2034/Spring-Security-JWT-Redis%EB%A5%BC-%ED%86%B5%ED%95%9C-%ED%9A%8C%EC%9B%90%EC%9D%B8%EC%A6%9D%ED%97%88%EA%B0%80-%EA%B5%AC%ED%98%84
    // 참고해보자
    @PostMapping("/reissue")
    public ResponseEntity<TokenDto> reissue(@RequestBody TokenRequestDto tokenRequestDto) {
        return ResponseEntity.ok(authService.reissue(tokenRequestDto));
    }


    // 소셜 회원가입
    @PostMapping(value = "/signup/{provider}")
    public ResponseEntity<MemberResponseDto> signupProvider(@PathVariable String provider,
                                       @RequestParam String accessToken,
                                       @RequestParam String name) {

        // 토큰으로 사용자 profile 가져오기
        KakaoProfile profile = kakaoService.getKakaoProfile(accessToken);

        // 회원 가입
        MemberResponseDto responseDto = authService.signup(profile, provider);

        return ResponseEntity.ok(responseDto);
    }

    // 소셜 로그인
    @PostMapping(value = "/signin/{provider}")
    public ResponseEntity<TokenDto> loginByProvider(
            @PathVariable String provider,
            @RequestParam String accessToken) {

        KakaoProfile profile = kakaoService.getKakaoProfile(accessToken);

        return ResponseEntity.ok(authService.login(profile, provider));
    }
}
