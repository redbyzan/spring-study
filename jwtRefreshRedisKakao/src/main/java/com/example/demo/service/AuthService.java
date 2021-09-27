package com.example.demo.service;

import com.example.demo.controller.dto.MemberRequestDto;
import com.example.demo.controller.dto.MemberResponseDto;
import com.example.demo.controller.dto.TokenDto;
import com.example.demo.controller.dto.TokenRequestDto;
import com.example.demo.domain.Authority;
import com.example.demo.domain.Member;
import com.example.demo.jwt.TokenProvider;
import com.example.demo.model.social.KakaoProfile;
import com.example.demo.repository.MemberRepository;
import com.example.demo.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RedisUtil redisUtil;



    // todo 각각에 validation  검증 필요함

    // 일반 로그인
    public MemberResponseDto signup(MemberRequestDto memberRequestDto) {
        if (memberRepository.existsByUsername(memberRequestDto.getUsername())) {
            throw new RuntimeException("이미 가입되어 있는 유저입니다");
        }

        Member member = memberRequestDto.toMember(passwordEncoder);
        return new MemberResponseDto(memberRepository.save(member));
    }

    // 소셜로그인
    public MemberResponseDto signup(KakaoProfile profile, String provider) {
        String username = String.valueOf(profile.getId());
        Optional<Member> member = memberRepository.findByUsernameAndProvider(username, provider);
        if(member.isPresent()){
            throw new RuntimeException("이미 존재하는 유저");
        }
        Member newMember = Member.builder()
                .username(username)
                .authority(Authority.ROLE_USER)
                .provider(provider)
                .build();

        memberRepository.save(newMember);

        // todo 카카오로만 할꺼면 responsedto 수정 필요함
        MemberResponseDto responseDto = MemberResponseDto.builder()
                .username(username)
                .build();

        return responseDto;
    }


    public TokenDto login(MemberRequestDto memberRequestDto) {
        // 1. Login ID/PW 를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = memberRequestDto.toAuthentication();

        // 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        //    authenticate 메서드가 실행이 될 때 CustomUserDetailsService 에서 만들었던 loadUserByUsername 메서드가 실행됨
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        // DaoAuthenticationProvider의 retrieveUser메서드에서  loadUserByUsername 호출해서 결과 반환
        // DaoAuthenticationProvider 의 부모 클래스인 AbstractUserDetailsAuthenticationProvider에서 retrieveUser 호출
        // 받은 User로 additionalAuthenticationChecks 메서드 호출
        // 이 메서드는 추상 클래스였고 DaoAuthenticationProvider 가 오버라이드 해서 구현했음
        // DaoAuthenticationProvider에서 request로 받은 authentication 와 DB에서 꺼낸 값인 userDetails의 패스워드 비교
        // DB값은 암호화되어 있지만 passwordEncoder가 알아서 처리해줌
        // 결론은 시큐리티가 제공하는 클래스에서 패스워드 검증이 자동으로 이뤄진다.


        // 3. 인증 정보를 기반으로 JWT 토큰(refresh, access) 생성
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);


       // 4. redis에 refresh 토큰 저장
        redisUtil.setDataExpire(memberRequestDto.getUsername(),
                tokenDto.getRefreshToken(),
                tokenProvider.REFRESH_TOKEN_EXPIRE_TIME
                );


        // 5. 토큰 발급
        return tokenDto;
    }

    // 소셜 로그인
    public TokenDto login(KakaoProfile profile, String provider) {

        // 1. 사용자 정보 뽑기
        String username = String.valueOf(profile.getId());
        Optional<Member> member = memberRepository.findByUsernameAndProvider(username, provider);
        if(member.isEmpty()){
            throw new RuntimeException("존재하지 않는 유저");
        }

        // 2. 토큰 만들기
        Member currentMember = member.get();
        TokenDto tokenDto = tokenProvider.generateTokenDto(currentMember);


        // 3. redis에 refresh 토큰 저장
        redisUtil.setDataExpire(currentMember.getUsername(),
                tokenDto.getRefreshToken(),
                tokenProvider.REFRESH_TOKEN_EXPIRE_TIME
        );

        // 4. 토큰 발급
        return tokenDto;
    }


    public TokenDto reissue(TokenRequestDto tokenRequestDto) {

        // 1. Refresh Token 검증
        // 유효하지 않으면 재로그인 해야함
        String refreshTokenFromRequest = tokenRequestDto.getRefreshToken();
        if (!tokenProvider.validateToken(refreshTokenFromRequest)) {
            throw new RuntimeException("Refresh Token 이 유효하지 않습니다.");
        }

        // 아래부터는 refresh token이 유효함

        // 2. Access Token 복호화하여 authentication 뽑기 -> 이를 통해 username 뽑기
        Authentication authentication = tokenProvider.getAuthentication(tokenRequestDto.getAccessToken());
        String username = authentication.getName();

        // redis에서 refresh token 꺼내오기
        String refreshTokenFromRedis = redisUtil.getData(username);

        if (!refreshTokenFromRequest.equals(refreshTokenFromRedis)) {
            throw new RuntimeException("토큰의 유저 정보가 일치하지 않습니다.");
        }

        // redis와 request에서 받은 토큰이 일치하면 redis 토큰 삭제하고 access, refresh 재발급
        // 참고자료에서는 access토큰만 재발급하고 있음

        // 재발급
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);

        // redis 기존 refresh 삭제
        redisUtil.deleteData(username);

        // 새로운 refresh 추가
        redisUtil.setDataExpire(username,tokenDto.getRefreshToken(),tokenProvider.REFRESH_TOKEN_EXPIRE_TIME);

        return tokenDto;
    }

    public void logout(TokenRequestDto tokenRequestDto){

        // 1. 토큰 유효 여부 확인
        String accessToken = tokenRequestDto.getAccessToken();
        if(!tokenProvider.validateToken(accessToken)){
            throw new RuntimeException("Access Token 이 유효하지 않습니다.");
        }

        // 2. redis에서 토큰 삭제
        String username = tokenProvider.getUsernameFromToken(tokenRequestDto.getAccessToken());
        redisUtil.deleteData(username);

        // 3. 이후 해당 access token 접근하는 경우, 막기 위해 redis에 현재 accesstoken의 남은 시간동안 저장
        redisUtil.setDataExpire(accessToken,"true", tokenProvider.getRemainingSeconds(accessToken));


    }
}
