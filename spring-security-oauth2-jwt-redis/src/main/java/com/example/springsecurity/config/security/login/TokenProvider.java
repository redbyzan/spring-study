package com.example.springsecurity.config.security.login;

import com.example.springsecurity.config.security.login.dto.MemberPrincipal;
import com.example.springsecurity.member.enums.AuthProvider;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class TokenProvider {

    private static final String AUTH_PROVIDER = "authProvider";

    private final String secretKey;
    private final long accessTokenExpirationTimeInMilliSeconds;
    private final long refreshTokenExpirationTimeInMilliSeconds;

    public TokenProvider(
            @Value("${jwt.secret-key}") String secretKey,
            @Value("${jwt.access-expiration-time}") long accessTokenExpirationTimeInMilliSeconds,
            @Value("${jwt.refresh-expiration-time}") long refreshTokenExpirationTimeInMilliSeconds) {
        this.secretKey = secretKey;
        this.accessTokenExpirationTimeInMilliSeconds = accessTokenExpirationTimeInMilliSeconds;
        this.refreshTokenExpirationTimeInMilliSeconds = refreshTokenExpirationTimeInMilliSeconds;
    }

    public String createAccessToken(Authentication authentication) {
        return createToken(authentication, accessTokenExpirationTimeInMilliSeconds);
    }

    public String createRefreshToken(Authentication authentication){
        return createToken(authentication, refreshTokenExpirationTimeInMilliSeconds);
    }

    private String createToken(Authentication authentication, long accessTokenExpirationTimeInMilliSeconds) {
        MemberPrincipal memberPrincipal = (MemberPrincipal)authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationTimeInMilliSeconds);

        return Jwts.builder()
                .setSubject(memberPrincipal.getUsername())
                .claim(AUTH_PROVIDER,memberPrincipal.getMember().getAuthProvider())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    public String getUserEmailFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    public AuthProvider getAuthProviderFromToken(String token) {
        Claims claims = getClaims(token);
        String authProviderType = claims.get(AUTH_PROVIDER, String.class);
        return AuthProvider.valueOf(authProviderType);
    }

    public long getRemainingMilliSecondsFromToken(String token){
        Date expiration = getClaims(token).getExpiration();
        return expiration.getTime() - (new Date()).getTime();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }


    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }



}
