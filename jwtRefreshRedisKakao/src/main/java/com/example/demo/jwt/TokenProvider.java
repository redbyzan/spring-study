package com.example.demo.jwt;


import com.example.demo.controller.dto.TokenDto;
import com.example.demo.domain.Member;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TokenProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "bearer";
    public static final long ACCESS_TOKEN_EXPIRE_TIME = 1000L * 60 * 30;            // 30분
    public static final long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 7;  // 7일

    private final Key key;

    public TokenProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }


    /**
     * 토큰 생성 메서드
     */
    public TokenDto generateTokenDto(Authentication authentication){
        // 권한들 가져오기
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));


        // 현재 시간
        Date date = new Date();
        long now = date.getTime();

        // Access Token 생성
        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())       // payload "sub": "name"
                .setIssuedAt(date)
                .claim(AUTHORITIES_KEY, authorities)        // payload "auth": "ROLE_USER"
                .setExpiration(accessTokenExpiresIn)        // payload "exp": 1516239022 (예시)
                .signWith(key, SignatureAlgorithm.HS512)    // header "alg": "HS512"
                .compact();

        // Refresh Token 생성
        String refreshToken = Jwts.builder()
                .setExpiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
                .setIssuedAt(date)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();



        return TokenDto.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessTokenExpiresIn.getTime())
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 소셜 로그인용 토큰 만들기
     */
    public TokenDto generateTokenDto(Member member){
        // 권한들 가져오기
        String authorities = member.getAuthority().name();

        // 현재 시간
        Date date = new Date();
        long now = date.getTime();

        // Access Token 생성
        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        String accessToken = Jwts.builder()
                .setSubject(member.getUsername())       // payload "sub": "name"
                .setIssuedAt(date)
                .claim(AUTHORITIES_KEY, authorities)        // payload "auth": "ROLE_USER"
                .setExpiration(accessTokenExpiresIn)        // payload "exp": 1516239022 (예시)
                .signWith(key, SignatureAlgorithm.HS512)    // header "alg": "HS512"
                .compact();

        // Refresh Token 생성
        String refreshToken = Jwts.builder()
                .setExpiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
                .setIssuedAt(date)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();



        return TokenDto.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessTokenExpiresIn.getTime())
                .refreshToken(refreshToken)
                .build();
    }


    /**
     * token에 담겨있는 정보를 이용해 Authentication 객체를 리턴하는 메서드
     */
    public Authentication getAuthentication(String accessToken) {
        // 토큰을 key로 복호화하고 파싱해서 claims 추출
        Claims claims = getClaims(accessToken);

        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 클레임에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // UserDetails 객체를 만들어서 Authentication 리턴
        UserDetails principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }



    /**
     *  token에서 username 꺼내기
     */

    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * 토큰을 key로 복호화하고 파싱해서 claims 추출
     */
    public Claims getClaims(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims;
    }

    /**
     * 토큰의 남은 시간
     */

    public long getRemainingSeconds(String token){
        Date expiration = getClaims(token).getExpiration();
        return expiration.getTime() - (new Date()).getTime();
    }

    /**
     * 토큰을 파라미터로 받아서 토큰의 유효성 검증을 하는 validateToken 메서드
     */

    // 토큰을 파싱해보고 문제가 있으면 false, 없으면 true 반환
    public boolean validateToken(String token){
        try {
            // 토큰파싱해서 문제 없으면 true
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e){
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e){
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e){
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e){
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }


}
