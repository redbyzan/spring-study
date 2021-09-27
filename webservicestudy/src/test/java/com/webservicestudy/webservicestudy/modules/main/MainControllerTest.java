package com.webservicestudy.webservicestudy.modules.main;

import com.webservicestudy.webservicestudy.infra.AbstractContainerBaseTest;
import com.webservicestudy.webservicestudy.infra.MockMvcTest;
import com.webservicestudy.webservicestudy.modules.account.AccountService;
import com.webservicestudy.webservicestudy.modules.account.form.SignUpForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcTest
class MainControllerTest extends AbstractContainerBaseTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;


    @BeforeEach
    void beforEach(){
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("backtony");
        signUpForm.setEmail("backtony@email.com");
        signUpForm.setPassword("123456789");
        accountService.processNewAccount(signUpForm);
    }


    @DisplayName("이메일 로그인 성공")
    @Test
    void login_with_email() throws Exception{


        mockMvc.perform(post("/login")
                .param("username","backtony@email.com") // spring security에서 username과 password로 키가 정해져 있음 config에서 바꿀 수 있음
                .param("password","123456789")
                .with(csrf()))
                .andExpect(status().is3xxRedirection()) // 로그인은 결과로 redirection된다.
                .andExpect(redirectedUrl("/"))
                // 토큰에 username이 nickname인 이유는 loadUserByUsername에서 반환하는 UserAccount에서 생성자에서 username을 nickname으로 줬기 때문
                .andExpect(authenticated().withUsername("backtony"))
        ;

    }

    @DisplayName("닉네임 로그인 성공")
    @Test
    void login_with_nickname() throws Exception{

        mockMvc.perform(post("/login")
                .param("username","backtony")
                .param("password","123456789")
                .with(csrf()))
                .andExpect(status().is3xxRedirection()) // 로그인은 결과로 redirection된다.
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("backtony"))
        ;

    }

    @DisplayName("로그인 실패")
    @Test
    void login_fail() throws Exception{
        mockMvc.perform(post("/login")
                .param("username","123456")
                .param("password","123456789")
                .with(csrf()))
                .andExpect(status().is3xxRedirection()) // 실패해도 마찬가지로 리다이렉션이 일어남
                .andExpect(redirectedUrl("/login?error")) // 실패시에는 로그인 페이지 그대로 에러만 파라미터로 추가됨
                // 토큰에 username이 nickname인 이유는 loadUserByUsername에서 반환하는 UserAccount에서 생성자에서 username을 nickname으로 줬기 때문
                .andExpect(unauthenticated())
        ;

    }

    @DisplayName("로그아웃")
    @Test
    void login_fai1l() throws Exception{

        mockMvc.perform(post("/logout")
                .with(csrf())) // 로그아웃시에도 csrf 토큰이 필요함
                .andExpect(status().is3xxRedirection()) // 마찬가지로 리다이렉트
                .andExpect(redirectedUrl("/")) // successfulhandler로 루트로 지정해줫었음
                .andExpect(unauthenticated())
        ;

    }

}