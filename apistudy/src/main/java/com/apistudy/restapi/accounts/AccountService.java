package com.apistudy.restapi.accounts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AccountService implements UserDetailsService {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;


    // 리파지토리로 save 넘기기 전에 password 인코딩
    public Account saveAccount(Account account) {
            account.setPassword(passwordEncoder.encode(account.getPassword()));
            return accountRepository.save(account);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 분명 없으면 null 나옴 -> repository에서 정의할 때 optional로 감싸준다.
        // 여기서 받을 때는 편하게 그냥 Account 해놓고 null 되면 오류 던지게 만들기
        Account account = accountRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return new AccountAdapter(account);

    }

    private Collection<? extends GrantedAuthority> authorities(Set<AccountRole> roles) {
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_"+r.name())) // 한줄이라 람다식 리턴 생략 가능
                .collect(Collectors.toSet());
    }
}















