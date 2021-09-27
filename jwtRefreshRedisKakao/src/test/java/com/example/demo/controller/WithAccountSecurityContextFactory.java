package com.example.demo.controller;


import com.example.demo.controller.dto.MemberRequestDto;
import com.example.demo.repository.MemberRepository;
import com.example.demo.service.AuthService;
import com.example.demo.service.CustomUserDetailsService;
import com.example.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

// 이 클래스는 WithAccountSecurityContextFactory의 구현체로 빈으로 자동 등록되므로 빈 주입 가능
// WithSecurityContextFactory의 제네릭으로 커스텀한 애노테이션 넣기
@RequiredArgsConstructor
public class WithAccountSecurityContextFactory implements WithSecurityContextFactory<WithAccount> {

    private final AuthService authService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public SecurityContext createSecurityContext(WithAccount withAccount) {

        // 애노테이션 파라미터로 받은 이름
        String username = withAccount.value();

        // 유저 만들고 저장
        MemberRequestDto dto = MemberRequestDto.builder()
                .username(username)
                .nickname(username)
                .password(username)
                .build();
        authService.signup(dto);


        // security context에 넣는 작업 시작 //

        // 뽑아오고
        UserDetails principle = userDetailsService.loadUserByUsername(username);
        // 뽑아온걸로 토큰 만들고
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principle,principle.getPassword(),principle.getAuthorities()
        );
        // 빈 컨텍스트 만들고
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        // 컨텍스트에 토큰 만든 토큰 넣기
        context.setAuthentication(authentication);

        // 해당 컨텍스트 반환
        return context;
    }
}
