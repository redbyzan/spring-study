package com.webservicestudy.webservicestudy.modules.account;

import com.webservicestudy.webservicestudy.infra.AbstractContainerBaseTest;
import com.webservicestudy.webservicestudy.infra.MockMvcTest;
import com.webservicestudy.webservicestudy.infra.mail.EmailMessage;
import com.webservicestudy.webservicestudy.infra.mail.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@MockMvcTest
class AccountControllerTest extends AbstractContainerBaseTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;


    @MockBean
    EmailService emailService;

    @DisplayName("회원 가입 화면 보이는지 테스트")
    @Test
    void signUpForm() throws Exception{
        mockMvc.perform(get("/sign-up"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up")) // view 이름 확인
                .andExpect(model().attributeExists("signUpForm")) // 해당 키의 모델이 들어있는지
                .andExpect(unauthenticated())
        ;
    }

    @DisplayName("회원 가입 처리 - 입력값 오류")
    @Test
    void signUpFormSubmit_wrong_input() throws Exception{
        mockMvc.perform(post("/sign-up")
                .param("nickname","backtony")
                .param("email","email..")
                .param("password","12345")
                .with(csrf())) // security 설정에 의해 formlogin에서 csrf 토큰이 자동으로 들어가 있음, 같이 안넘겨주면 403 에러
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up")) // 컨트롤러에서 return하는 view 일치하는지 확인
                .andExpect(unauthenticated())
        ;
    }

    @DisplayName("회원 가입 처리 - 입력값 정상")
    @Test
    void signUpFormSubmit_correct_input() throws Exception{
        mockMvc.perform(post("/sign-up")
                .param("nickname","backtony")
                .param("email","backtony@email.com")
                .param("password","12345678")
                .with(csrf())) // security 설정에 의해 formlogin에서 csrf 토큰이 자동으로 들어가 있음, 같이 안넘겨주면 403 에러
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated())
        ;

        Account account = accountRepository.findByEmail("backtony@email.com");
        assertNotEquals(account.getPassword(),"12345678"); // 인코딩 잘 됬는지 확인
        assertTrue(accountRepository.existsByEmail("backtony@email.com")); // 저장 잘 됬는지
        assertNotNull(account.getEmailCheckToken()); // 토큰값 생성 확인

        // mockbean으로 javamailsender 껍데기만 만들고 해당 인스턴스타입으로 send 메서드를 호출하는지만 확인
        // bddmockito의 then과 mockito any 사용
        then(emailService).should().sendEmail(any(EmailMessage.class));
    }

    @DisplayName("인증 메일 확인 - 입력값 오휴")
    @Test
    void checkEmailToken_with_wrong_input() throws Exception{
        mockMvc.perform(get("/check-email-token")
                .param("token","asdfasdfasdf")
                .param("email","email@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(unauthenticated()) // 인증 사용자가 아님
        ;
    }

    @DisplayName("인증 메일 확인 - 입력값 정상")
    @Test
    void checkEmailToken_with_correct_input() throws Exception{

        Account account = Account.builder()
                .email("test@email.com")
                .password("12345678")
                .nickname("test")
                .build();

        account.generateEmailCheckToken();
        Account newAccount = accountRepository.save(account);

        mockMvc.perform(get("/check-email-token")
                .param("token",newAccount.getEmailCheckToken())
                .param("email","test@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(authenticated()) // 인증 사용자임
        ;
    }

}