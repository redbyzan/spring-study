package com.example.springsecurity.config.security;

import com.example.springsecurity.config.security.login.filter.LoginFilter;
import com.example.springsecurity.config.security.login.filter.TokenAuthenticationFilter;
import com.example.springsecurity.config.security.login.handler.LoginSuccessHandler;
import com.example.springsecurity.config.security.login.handler.RestAuthenticationEntryPoint;
import com.example.springsecurity.config.security.login.service.CustomOauth2Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true) // allow preAuthorize
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final ObjectMapper objectMapper;
    private final CustomOauth2Service customOauth2Service;

    private final LoginSuccessHandler loginSuccessHandler;

    private final TokenAuthenticationFilter tokenAuthenticationFilter;


//    @Override
//    public void configure(WebSecurity web) {
//        // 시큐리티 무시
//        web.ignoring()
//                .antMatchers("/h2-console/**", "/favicon.ico");
//    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .headers().frameOptions().sameOrigin() // for h2

                // session 사용안함
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                // form login 제거
                .and()
                .formLogin().disable()
                .httpBasic().disable()
                .exceptionHandling()
                .authenticationEntryPoint(new RestAuthenticationEntryPoint()) // 필터에서 터지는 예외 처리

                .and()
                .oauth2Login()
                .successHandler(loginSuccessHandler)
                .userInfoEndpoint()//oAuth2 로그인 성공 이후 사용자 정보를 가져올 때의 설정들을 담당
                .userService(customOauth2Service) // 소셜 로그인 성공 시 후속 조치를 진행할 service 구현체 등록

                .and()
                .authorizationEndpoint()
                // /oauth2/authorization/google 또는 naver가 기본값
                // oauth 요청 url -> /api/oauth2/authorization/naver 또는 google
                .baseUri("/api/oauth2/authorization")
        ;

        http.addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(loginFilter(), UsernamePasswordAuthenticationFilter.class);

    }


    @Bean
    public LoginFilter loginFilter() throws Exception {
        LoginFilter loginFilter = new LoginFilter(objectMapper);
        loginFilter.setFilterProcessesUrl("/api/login"); // login 진입점
        loginFilter.setAuthenticationManager(authenticationManagerBean());
        loginFilter.setAuthenticationSuccessHandler(loginSuccessHandler);
        return loginFilter;
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }


}
