package com.apistudy.restapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;

// 리소스 서버는 앞서 설정해두었던 auth서버와 연동되어 사용된다.
// 리소스에 접근할 때 인증이 필요하다면 auth서버에서 제공하는 토큰 서비스쪽 auth쪽에서 요청 보내서 토큰을 유효한지
// 확인하는 일 작업
// 리소스 서버는 토큰기반으로 인증 정보가 있는지 없는지 확인하고 리소스 서버에 접근 제한
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {


    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId("event");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .anonymous()// 익명 사용자 허용
                    .and()
                .authorizeRequests()
                    .mvcMatchers(HttpMethod.GET,"/api/**")
                        .permitAll() // 전부 허용 anoymoous로 해버리면 익명만 사용 가능함 -ㅅ-
                    .anyRequest().authenticated()// 다른 요청은 인증 해야함
                    .and()
                .exceptionHandling() // 접근 권히 없는 경우 oauth2 핸들러 사용
                    .accessDeniedHandler(new OAuth2AccessDeniedHandler());
    }
}
