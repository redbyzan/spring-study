package com.apistudy.restapi.accounts;


import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class AccountAdapter extends User {
    // 컨트롤러에서 인증정보 account 엔티티로 받게 하기
    private Account account;

    public AccountAdapter(Account account) {
        super(account.getEmail(), account.getPassword(), authorities(account.getRoles()));
        this.account=account;
    }

    private static Collection<? extends GrantedAuthority> authorities(Set<AccountRole> roles) {
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_"+r.name())) // 한줄이라 람다식 리턴 생략 가능
                .collect(Collectors.toSet());
    }

}
