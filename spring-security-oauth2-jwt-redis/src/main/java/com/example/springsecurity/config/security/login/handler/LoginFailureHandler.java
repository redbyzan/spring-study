package com.example.springsecurity.config.security.login.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final String MESSAGE = "로그인에 실패했습니다.";

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        log.info("[LoginFailureHandler] Responding with unauthorized error - {}", e.getMessage());

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                MESSAGE);
    }
}
