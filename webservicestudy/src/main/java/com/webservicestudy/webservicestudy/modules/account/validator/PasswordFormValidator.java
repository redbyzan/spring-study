package com.webservicestudy.webservicestudy.modules.account.validator;


import com.webservicestudy.webservicestudy.modules.account.form.PasswordForm;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

// 다른 곳에서 사용할일이 없으므로 빈 등록 안하고 그냥 new로 쓰자.
public class PasswordFormValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return PasswordForm.class.isAssignableFrom(aClass);
    }

    // 검증으로 걸리면 에러에 오류 담기
    @Override
    public void validate(Object o, Errors errors) {
        PasswordForm passwordForm = (PasswordForm) o;
        if(!passwordForm.getNewPassword().equals(passwordForm.getNewPasswordConfirm())){
            errors.rejectValue("newPassword","wrong.value","입력한 새 패스워드가 일치하지 않습니다.");
        }
    }
}
