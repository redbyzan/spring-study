package com.example.springsecurity.config.security.login.dto;

import com.example.springsecurity.member.entity.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;


public class MemberPrincipal implements OAuth2User, UserDetails {
    private Member member;
    private Map<String, Object> attributes;

    private MemberPrincipal(Member member) {
        this.member = member;
    }

    public static MemberPrincipal from(Member member) {
        return new MemberPrincipal(member);
    }

    public static MemberPrincipal of(Member member, Map<String, Object> attributes) {
        MemberPrincipal memberPrincipal = MemberPrincipal.from(member);
        memberPrincipal.attributes = attributes;
        return memberPrincipal;
    }

    public Member getMember() {
        return member;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(member.getRoleKey()));
    }

    @Override
    public String getName() {
        return member.getName();
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
