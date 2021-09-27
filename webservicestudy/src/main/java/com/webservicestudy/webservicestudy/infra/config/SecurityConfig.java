package com.webservicestudy.webservicestudy.infra.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;



@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    // module은 인프라를 참조해도
    // 인프라는 모듈을 참조하지 않도록 하는게 좋다
    // accountservice는 모듈에 속한다
    // 그런데 사실 security에서는 accountservice가 아니라
    // userdetailsService를 필요로 한다
    // 현재 accountService가 userdetailsservice의 구현체로 빈으로 등록되어 있으니
    // userdetailsservice로 하면 accounservice가 들어올 것이다

    private final UserDetailsService userDetailsService;
    private final DataSource dataSource; // jpa 사용하니까 빈으로 등록되어있음


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .mvcMatchers("/","/login","/sign-up","/check-email-token","/search/study",
                        "/email-login","/check-email-login","/login-link","/login-by-email").permitAll() // 뒤에 파람은 상관없음
                .mvcMatchers(HttpMethod.GET,"/profile/*").permitAll()
                .anyRequest().authenticated();

        http.formLogin()
                // 로그인 url 설정
                // 실제 컨트롤러와 뷰를 통한 로직도 다 만들어야하고
                // 이 loginpage는 로그인 로직이 실행되는 url을 어느 url에서 시행하는지를 정한다고 보면 된다.
                .loginPage("/login").permitAll();

        http.logout()
                .logoutSuccessUrl("/");

        // 가장 안전한 방법 사용
        // username, 토큰(랜덤,매번 바뀜), 시리즈(랜덤,고정) -> 이 3가지를 조합해서 db에 저장 -> 나중에 사용자가 rememberme 토큰을 보내면 일치하는지 확인
        // 탈취당하면 토큰은 바뀌게 되고, 피해자는 전 토큰으로 로그인 시도 -> 모든 토큰 자동 삭제
        http.rememberMe()
                .userDetailsService(userDetailsService) // tokenrepository 사용시에는 userdetailsservice도 같이 설정해야함
                .tokenRepository(tokenRepository()); // db에서 토큰 값을 읽어오거나 저장하는 인터페이스의 구현체를 주입
    }

    @Bean
    public PersistentTokenRepository tokenRepository(){
        // JdbcTokenRepositoryImpl는 jdbc 기반의 토큰 구현체
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource); // jdbc니까 당연히 datasource 필요 -> jpa를 사용하고 있으니까 datasource는 빈에 등록되어있음
        return jdbcTokenRepository;

        // JdbcTokenRepositoryImpl이 사용하는 table이 db에 반드시 있어야 한다.
        // 타고 들어가면 설명이 적혀있는데
        // "create table persistent_logins (username varchar(64) not null, series varchar(64) primary key, token varchar(64) not null, last_used timestamp not null)";
        // db에 위의 테이블이 있어야 한다는 뜻이다
        // 현재는 인메모리 db를 사용하고 있으니 위 테이블에 해당하는 엔티티를 만들면 테이블이 알아서 만들어지겠지!
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                // npm 으로 js 버전들 관리하면서 html에서 정보를 node_module에서 가져오게 했다.
                // 그럼 웹에서 해당 접근도 필터에서 제외시켜줘야 한다.
                .mvcMatchers("/node_modules/**")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()); // static에서 흔히 사용하는 위치만 제외

    }
}
