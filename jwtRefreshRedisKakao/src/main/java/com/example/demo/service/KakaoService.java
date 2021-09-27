package com.example.demo.service;

import com.example.demo.model.social.KakaoProfile;
import com.example.demo.model.social.RetKakaoAuth;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


@RequiredArgsConstructor
@Service
public class KakaoService {

    private final RestTemplate restTemplate;
    private final Environment env;
    private final Gson gson;

    @Value("${spring.url.base}")
    private String baseUrl;

    @Value("${spring.social.kakao.client_id}")
    private String kakaoClientId;

    @Value("${spring.social.kakao.redirect}")
    private String kakaoRedirect;

    public KakaoProfile getKakaoProfile(String accessToken) {
        // Set header : Content-type: application/x-www-form-urlencoded
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer " + accessToken);

        // Set http entity
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, headers);
        try {
            // Request profile
            // url, post body에 들어갈 정보, responseEntity의 반환 타입
            ResponseEntity<String> response = restTemplate.postForEntity(env.getProperty("spring.social.kakao.url.profile"), request, String.class);
            if (response.getStatusCode() == HttpStatus.OK)
                return gson.fromJson(response.getBody(), KakaoProfile.class);
        } catch (Exception e) {
            throw new RuntimeException();  //throw new CCommunicationException();
        }
        throw new RuntimeException(); // throw new CCommunicationException();
    }

    public RetKakaoAuth getKakaoTokenInfo(String code) {
        // Set header : Content-type: application/x-www-form-urlencoded
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Set parameter
        // 바디부로 들어간다고 보면 된다.
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", baseUrl + kakaoRedirect);
        params.add("code", code);

        // Set http entity
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        // url, request(헤더랑 파라미터 정보), response 타입
        // 여기는 파라미터 정보와 헤더가 필요해서 넣었지만 헤더가 필요 없으면 그냥 post body에 들어갈 정보 하나면 넣어줘도 무관
        ResponseEntity<String> response = restTemplate.postForEntity(env.getProperty("spring.social.kakao.url.token"), request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return gson.fromJson(response.getBody(), RetKakaoAuth.class);
        }
        return null;
    }
}