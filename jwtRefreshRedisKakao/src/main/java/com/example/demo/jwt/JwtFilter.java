package com.example.demo.jwt;

import com.example.demo.util.RedisUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    private final TokenProvider tokenProvider;
    private final RedisUtil redisUtil;



    // Request Header 에서 토큰 정보를 꺼내오기
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 서블릿 실행 전 작업

        // 1. Request Header 에서 토큰을 꺼냄
        String jwt = resolveToken(request);

        // 2. validateToken 으로 토큰 유효성 검사
        // 조건으로 jwt 있는지, 유효한지, redis에 없는지(로그아웃여부)
        // 정상 토큰이면 해당 토큰으로 Authentication 을 가져와서 SecurityContext 에 저장
        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)  && redisUtil.getData(jwt) == null) {
            Authentication authentication = tokenProvider.getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }


       // 앞서 요청에 대해 필터링(서블릿 실행 전 작업을) 했고 이제 다음 필터로 넘기는 작업
        filterChain.doFilter(request, response);

        // 이후의 코드는 서블릿 실행 후의 작업

        // 여기서 통과해서 컨트롤러까지 도착했다면 securityContext에는 username정보가 담긴다.
        // 하지만 직접 DB에서 조회한 것이 아니라 토큰에서 꺼낸 정보이므로 탈퇴로 인한 경우가
        // 있을 수 있으므로 예외 상황은 service 단에서 고려해야 한다.
    }
}
