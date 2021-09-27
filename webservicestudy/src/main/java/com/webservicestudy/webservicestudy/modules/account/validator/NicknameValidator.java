package com.webservicestudy.webservicestudy.modules.account.validator;

import com.webservicestudy.webservicestudy.modules.account.AccountRepository;
import com.webservicestudy.webservicestudy.modules.account.Account;
import com.webservicestudy.webservicestudy.modules.account.form.NicknameForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class NicknameValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> aClass) {
        return NicknameForm.class.isAssignableFrom(aClass);
    }

    @Override
    // 닉네임 중복이 있는지 확인
    public void validate(Object o, Errors errors) {
        NicknameForm nameForm = (NicknameForm) o;
        Account byNickname = accountRepository.findByNickname(nameForm.getNickname());
        if (byNickname != null){
            errors.rejectValue("nickname","wrong.value","입력하신 닉네임은 사용할 수 없습니다.");
        }

    }
}
