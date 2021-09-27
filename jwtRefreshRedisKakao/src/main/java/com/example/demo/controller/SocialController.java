package com.example.demo.controller;

import com.example.demo.service.KakaoService;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;


// todo rest api 서버에서는 프론트에서 로그인까지 완료하고 access_token을 서버에 전달해주도록 짜도록 하자
@RequiredArgsConstructor
@Controller
@RequestMapping("/auth/social/login")
public class SocialController {

    private final Environment env;
    private final RestTemplate restTemplate;
    private final Gson gson;
    private final KakaoService kakaoService;

    @Value("${spring.url.base}")
    private String baseUrl;

    @Value("${spring.social.kakao.client_id}")
    private String kakaoClientId;

    @Value("${spring.social.kakao.redirect}")
    private String kakaoRedirect;

    /**
     * 카카오 로그인 페이지
     */
    // 여기서 화면에서 클릭하면 redirect로 코드와 함께 반환함
    @GetMapping
    public ModelAndView socialLogin(ModelAndView mav) {

        StringBuilder loginUrl = new StringBuilder()
                .append(env.getProperty("spring.social.kakao.url.login"))
                .append("?client_id=").append(kakaoClientId)
                .append("&response_type=code")
                .append("&redirect_uri=").append(baseUrl).append(kakaoRedirect);

        mav.addObject("loginUrl", loginUrl);
        mav.setViewName("social/login");
        return mav;
    }

    /**
     * 카카오 인증 완료 후 리다이렉트 화면
     */
    // 위에 컨트롤러에서 리다이렉트되어 여기로 들어옴
    // getKakaoTokenInfo 함수를 보면 code로 토큰을 요청하는 과정
    // 토큰 받은 것을 화면으로 보내게 되고
    // 이제 프런트에서 실제로 로그인할 소셜 로그인 controller로 요청을 보낼때 토큰과 함께 요청을 보내게된다.
    @GetMapping(value = "/kakao")
    public ModelAndView redirectKakao(ModelAndView mav, @RequestParam String code) {
        mav.addObject("authInfo", kakaoService.getKakaoTokenInfo(code));
        mav.setViewName("social/redirectKakao");
        return mav;
    }
}
