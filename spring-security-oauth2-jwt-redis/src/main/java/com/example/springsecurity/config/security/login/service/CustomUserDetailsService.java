package com.example.springsecurity.config.security.login.service;

import com.example.springsecurity.config.security.login.dto.MemberPrincipal;
import com.example.springsecurity.member.entity.Member;
import com.example.springsecurity.member.enums.AuthProvider;
import com.example.springsecurity.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmailAndSocial(username,false)
                .orElseThrow(() -> new UsernameNotFoundException("check email or social"));

        return MemberPrincipal.from(member);
    }

    public UserDetails loadTokenUserByUsername(String username, AuthProvider authProvider) throws UsernameNotFoundException{
        Member member = memberRepository.findByEmailAndAuthProvider(username,authProvider)
                .orElseThrow(() -> new UsernameNotFoundException("check email or social"));

        return MemberPrincipal.from(member);

    }
}
