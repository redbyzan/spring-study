package com.apistudy.restapi.config;

import com.apistudy.restapi.accounts.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    AccountService accountService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Bean // oauth 토큰을 저장하는 저장소 // 원래는 여기다 하면 안됨
    public TokenStore tokenStore(){
        return new InMemoryTokenStore();
    }

    // authenticationmanager를 빈으로 노출 시켜야한다.
    // 다른 authorization 서버나 resource 서버가 참조할 수 있도록해야 하기 때문이다
    @Bean // 재정의로 불러오고 그대로 빈으로 등록하면 된다.
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    // authenticationmanager을 어떻게 만들거냐 -> 재정의
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 내가 만든 accountservice와 password인코더를 사용해서 매니저를 만들도록
        auth.userDetailsService(accountService)
                .passwordEncoder(passwordEncoder);
    }


    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().mvcMatchers("/docs/**");//docs 보안 검사 무시
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations()); // 정적 리소스 보안 검사 무시
    }



}