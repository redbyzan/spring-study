package com.webservicestudy.webservicestudy.modules.account.validator;


import com.webservicestudy.webservicestudy.modules.account.AccountRepository;
import com.webservicestudy.webservicestudy.modules.account.form.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

// api할때는 직접 만들었는데 이 강의에서는 인터페이스 구현한다.
// api할 때도 이렇게 구현하는게 좋은듯?
// repository 주입받을 껀데 -> 빈 주입은 빈끼리만 주입 가능!!!!!!!!!!!
// 가입할 때 들어오는 SignUpForm에서 nickname과 email 중복 검사
@Component
@RequiredArgsConstructor
public class SignUpFormValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> aClass) {
        return SignUpForm.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object object, Errors errors) {
        SignUpForm signUpForm = (SignUpForm) object;
        if(accountRepository.existsByEmail(signUpForm.getEmail())){
            errors.rejectValue("email","invalid.email",new Object[]{signUpForm.getEmail()},"이미 사용중인 이메일입니다.");
        }
        if(accountRepository.existsByNickname(signUpForm.getNickname())){
            errors.rejectValue("nickname","invalid.nickname",new Object[]{signUpForm.getNickname()},"이미 사용중인 닉네임입니다.");
        }
    }
}
















