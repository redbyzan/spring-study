package com.example.springsecurity.dummy;

import com.example.springsecurity.member.entity.Member;
import com.example.springsecurity.member.enums.AuthProvider;
import com.example.springsecurity.member.enums.Role;
import com.example.springsecurity.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Component
public class MemberRunner implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Member user = Member.builder()
                .email("user@gmail.com")
                .password(passwordEncoder.encode("user"))
                .name("user")
                .social(false)
                .authProvider(AuthProvider.local)
                .role(Role.USER)
                .build();


        Member admin = Member.builder()
                .email("admin@gmail.com")
                .password(passwordEncoder.encode("admin"))
                .name("admin")
                .social(false)
                .authProvider(AuthProvider.local)
                .role(Role.ADMIN)
                .build();
        memberRepository.save(user);
        memberRepository.save(admin);
    }
}
