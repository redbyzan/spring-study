package com.example.springsecurity.config.security.login.entrypoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final String MESSAGE = "로그인에 실패했습니다.";

    // todo trace 빼는 반환값 만들고
    // todo 필터 엑셉션 받는 핸들러 추가

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        log.info("Responding with unauthorized error. Message - {}", e.getMessage());

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                MESSAGE);
    }
}
