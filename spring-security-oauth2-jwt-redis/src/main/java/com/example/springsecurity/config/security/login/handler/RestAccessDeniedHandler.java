package com.example.springsecurity.config.security.login.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final String MESSAGE = "권한이 없습니다.";

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
        log.info("[RestAccessDeniedHandler] Responding with accessDenied error. Message - {}", e.getMessage());


        response.sendError(HttpServletResponse.SC_FORBIDDEN,
                MESSAGE);
    }
}
