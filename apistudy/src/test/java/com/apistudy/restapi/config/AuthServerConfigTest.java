package com.apistudy.restapi.config;

import com.apistudy.restapi.accounts.Account;
import com.apistudy.restapi.accounts.AccountRole;
import com.apistudy.restapi.accounts.AccountService;
import com.apistudy.restapi.common.AppProperties;
import com.apistudy.restapi.common.BaseControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class AuthServerConfigTest extends BaseControllerTest {

    @Autowired
    AccountService accountService;

    @Autowired
    AppProperties appProperties;

    //grant type을 password 방식으로 사용할 건데
    // 이 방식은 account의 정보를 내 서비스가 가지고 있어야만 사용하는 방식이다.
    // 즉 서비스 오너가 만든 클라이언트에서 사용하는 타입이다.
    // 토큰을 발급받기 위해서는
    // grant_type = password
    // 유저에 대한 정보 (유저네임 패스워드)
    // 클라이언트 id와 클라이언트 시크릿
    // 클라이언트 아이디와 시크릿은 basic authentication 형태로 헤더에 넣고
    // 나머지 3개의 값 grant type, 유저네임 페스워드는 request의 파라미터로 넘겨야 한다.
    // httpBasic 사용하려면 security test dependency 추가 필요

    // 클라이언트 ID와 시크릿은 사용자가 아니라 클라이언트 애플리케이션당 만들어집니다.
    // 페이스북이나 깃헙 로그인을 만들거나 연동하는 애플리케이션을 만들 때 발급받는
    // 애플리케이션 id와 시크릿과 동일하다고 생각하시면 됩니다. -> 사용자가 아니라 클라이언트당 하나

    // 현재 강좌에서 사용하는 spring security oauth 프로젝트가 deprecation 됬다.
    // auth server를 따로 커뮤니티 프로젝트로 분리됬고 그밖의 기능은 스프링 시큐리티 프로젝트 5에 포함되었다.

    @DisplayName("인증 토큰을 발급받는 테스트")
    @Test
    void getAuthToken() throws Exception{
        // 기본으로 인증서버가 등록되면 oauth 토큰을 처리할 수 있는 핸들러가 적용된다.

        // 아무런 매핑 없이 /oauth/token로 보내도 결과가 나오는 이유는
        // pom.xml에 oauth2 의존성을 추가했기 때문이다. 이쪽으로 담아서 보내면 토큰을 발급해준다
        mockMvc.perform(post("/oauth/token")
                // 인증 토큰을 발급받으려면 http basic 인증 헤더에 클라이언트 아이디와 클라이언트 시크릿을 줘야한다.
                .with(httpBasic(appProperties.getClientId(),appProperties.getClientSecret()))
                .param("username",appProperties.getUserUsername())
                .param("password",appProperties.getUserPassword())
                .param("grant_type","password"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists());

        //when

        //then
    }

}