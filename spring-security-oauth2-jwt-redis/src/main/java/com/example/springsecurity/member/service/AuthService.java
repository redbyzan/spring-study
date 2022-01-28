package com.example.springsecurity.member.service;

import com.example.springsecurity.config.security.login.TokenProvider;
import com.example.springsecurity.config.security.login.dto.AuthResponse;
import com.example.springsecurity.config.security.redis.LogoutAccessToken;
import com.example.springsecurity.config.security.redis.LogoutAccessTokenRepository;
import com.example.springsecurity.config.security.redis.LogoutRefreshToken;
import com.example.springsecurity.config.security.redis.LogoutRefreshTokenRepository;
import com.example.springsecurity.exception.member.DuplicatedMemberException;
import com.example.springsecurity.member.dto.SignUpRequest;
import com.example.springsecurity.member.entity.Member;
import com.example.springsecurity.member.enums.AuthProvider;
import com.example.springsecurity.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final LogoutAccessTokenRepository logoutAccessTokenRepository;
    private final LogoutRefreshTokenRepository logoutRefreshTokenRepository;

    public void signUp(SignUpRequest signUpRequest) {
        if (memberRepository.existsByEmailAndAuthProvider(signUpRequest.getEmail(), AuthProvider.local)) {
            throw new DuplicatedMemberException();
        }

        String encodePassword = passwordEncoder.encode(signUpRequest.getPassword());
        signUpRequest.setPassword(encodePassword);
        memberRepository.save(Member.ofLocal(signUpRequest));
    }

    public void logout(String accessToken, String refreshToken) {
        String removedTypeAccessToken = getRemovedBearerType(accessToken);
        String removedTypeRefreshToken = getRemovedBearerType(refreshToken);

        LogoutAccessToken logoutAccessToken
                = LogoutAccessToken.of(removedTypeAccessToken,
                tokenProvider.getRemainingMilliSecondsFromToken(removedTypeAccessToken));
        logoutAccessTokenRepository.save(logoutAccessToken);

        LogoutRefreshToken logoutRefreshToken
                = LogoutRefreshToken.of(removedTypeRefreshToken,
                tokenProvider.getRemainingMilliSecondsFromToken(removedTypeRefreshToken));
        logoutRefreshTokenRepository.save(logoutRefreshToken);
    }

    private String getRemovedBearerType(String token){
        return token.substring(7);
    }

    public AuthResponse reissue() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String accessToken = tokenProvider.createAccessToken(authentication);
        String refreshToken = tokenProvider.createRefreshToken(authentication);
        return AuthResponse.of(accessToken, refreshToken);
    }


}
