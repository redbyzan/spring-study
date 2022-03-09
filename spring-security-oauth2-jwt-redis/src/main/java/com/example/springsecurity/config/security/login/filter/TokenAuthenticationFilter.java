package com.example.springsecurity.config.security.login.filter;

import com.example.springsecurity.config.security.login.TokenProvider;
import com.example.springsecurity.config.security.login.service.CustomUserDetailsService;
import com.example.springsecurity.config.security.redis.LogoutAccessTokenRepository;
import com.example.springsecurity.config.security.redis.LogoutRefreshTokenRepository;
import com.example.springsecurity.exception.auth.TokenAuthenticationFilterException;
import com.example.springsecurity.member.enums.AuthProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final TokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final LogoutAccessTokenRepository logoutAccessTokenRepository;
    private final LogoutRefreshTokenRepository logoutRefreshTokenRepository;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (isValidToken(jwt)) {
                String email = tokenProvider.getUserEmailFromToken(jwt);
                AuthProvider authProvider = tokenProvider.getAuthProviderFromToken(jwt);
                UserDetails userDetails = customUserDetailsService.loadTokenUserByUsername(email,authProvider);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // (WebAuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
                // 저런식으로 꺼내쓰는거라는데 저기서는 ip 주소와 세션 ID를 얻을 수 있다.
                // 필요 없다면 세팅하지 않아도 되지 않을까 싶다.
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch(Exception e){
            throw new TokenAuthenticationFilterException();
        }

        filterChain.doFilter(request, response);

    }

    private boolean isValidToken(String jwt) {

        return StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)
                && !logoutAccessTokenRepository.existsById(jwt) && !logoutRefreshTokenRepository.existsById(jwt);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
