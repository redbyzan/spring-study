package com.apistudy.restapi.accounts;

import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AccountServiceTest {

    @Autowired
    AccountService accountService;

    @Autowired
    PasswordEncoder passwordEncoder;


    // 실패 예외를 예상해주는 코드 // public으로 제한해주고 비어있다고 표시

    @DisplayName("유저 찾기 실패")
    @Test
    public void findByUserFail(){
        assertThrows(UsernameNotFoundException.class, () -> accountService.loadUserByUsername("random@email.com"));
    }


    @DisplayName("유저 찾기 성공")
    @Test
    void findByUsername() throws Exception{
        // given
        String password = "backtony";
        String username = "backtony@gmail.com";
        Account account = Account.builder()
                .email(username)
                .password(password)
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();

        accountService.saveAccount(account);


        //when
        UserDetailsService userDetailsService = (UserDetailsService)accountService;
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // then
        // matches(날것,인코딩된것)
        Assertions.assertThat(passwordEncoder.matches(password,userDetails.getPassword()));

    }



}