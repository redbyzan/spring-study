package com.apistudy.restapi.config;

import com.apistudy.restapi.accounts.Account;
import com.apistudy.restapi.accounts.AccountRole;
import com.apistudy.restapi.accounts.AccountService;
import com.apistudy.restapi.common.AppProperties;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class AppConfig {

    // modelmapper은 공용으로 사용할 수 있는 객체이므로 빈으로 등록해서 편하게 사용
    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // 그냥 스프링 뜰때 유저 하나 넣어준 거임
    @Bean
    public ApplicationRunner applicationRunner() {
        return new ApplicationRunner() {

            @Autowired
            AccountService accountService;

            @Autowired
            AppProperties appProperties;

            @Override
            public void run(ApplicationArguments args) throws Exception {
                Account admin = Account.builder()
                        .email(appProperties.getAdminUsername())
                        .password(appProperties.getAdminPassword())
                        .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                        .build();
                accountService.saveAccount(admin);

                Account user = Account.builder()
                        .email(appProperties.getUserUsername())
                        .password(appProperties.getUserPassword())
                        .roles(Set.of(AccountRole.USER))
                        .build();
                accountService.saveAccount(user);
            }
        };
    }
}
