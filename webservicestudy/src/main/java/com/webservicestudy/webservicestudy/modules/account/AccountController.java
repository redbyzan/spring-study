package com.webservicestudy.webservicestudy.modules.account;


import com.webservicestudy.webservicestudy.modules.account.form.SignUpForm;
import com.webservicestudy.webservicestudy.modules.account.validator.SignUpFormValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;

    @InitBinder("signUpForm") // 타입의 캐멀케이스 -> 파라미터 바인딩 할 때 검증
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(signUpFormValidator); // 가입시 이메일, 닉네임 중복 검사
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model){

        model.addAttribute(new SignUpForm()); // key값을 작성하지 않으면 해당 타입의 캐멀케이스로 키값이 들어간다
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors){
        if(errors.hasErrors()){
            return "account/sign-up";
        }

        Account account = accountService.processNewAccount(signUpForm);

        // 가입후 자동 로그인
        accountService.login(account);

        return "redirect:/";
    }

    @GetMapping("/check-email-token")
    // 메일에 토큰이랑 이메일 파라미터로 추가해서 해당 링크로 접속하도록 링크 만들어서 보냈음
    public String checkEmailToken(String token, String email,Model model){
        // 리파지토리를 컨트롤러에서 쓰냐 마느냐?
        // 여기서는 repository를 도메인으로 보고 account와 같은 레벨로 보도록 하자
        Account account = accountRepository.findByEmail(email);

        // 계정이 조회되지 않으면
        String view = "account/checked-email";
        if(account == null){
            model.addAttribute("error","wrong.email");
            return view;
        }

        // 토큰이 일치하지 않으면
        if (!account.isValidToken(token)){
            model.addAttribute("error","wrong.email");
            return view;
        }

        // 현재 account를 persistence 상태 -> 서비스에서 transactional로 변경하면 변경 될 것임
        accountService.completeSignup(account);


        model.addAttribute("numberOfUser",accountRepository.count()); // jpa에서 기본으로 count 제공
        model.addAttribute("nickname",account.getNickname());
        return view;
    }

    // 애초에 로그인 한 사용자만 화면에서 클릭 버튼이 보임
    @GetMapping("/check-email")
    public String checkEmail(@CurrentUser Account account, Model model){
        model.addAttribute("email",account.getEmail());

        return "account/check-email";
    }

    @GetMapping("/resend-confirm-email")
    public String resend(@CurrentUser Account account,Model model){
        if(!account.canSendConfirmEmail()){
            model.addAttribute("error","인증 이메일은 3분마다 전송할 수 있습니다.");
            model.addAttribute("email",account.getEmail());
            return "account/check-email";
        }

        accountService.sendSignUpConfirmEmail(account); // 이메일 재전송
        // get요청임에도 불구하고 이메일을 resend하는 로직이기 때문에 새로고침하면 다시 메일 전송된다.
        // 따라서 redirect로 루트 페이지로 넘김
        return "redirect:/";

    }

    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model, @CurrentUser Account account){

        Account accountToView = accountService.getAccount(nickname);

        model.addAttribute(accountToView); // 타입의 키값의 캐멀케이스로 키값 지정 -> account
        model.addAttribute("isOwner",accountToView.equals(account));

        return "account/profile";
    }

    @GetMapping("/email-login")
    public String emailLoginForm(){
        return "account/email-login";
    }

    @PostMapping("/email-login")
    public String sendEmailLoginLink(String email, Model model, RedirectAttributes attributes){
        Account account = accountRepository.findByEmail(email);
        if(account==null){
            model.addAttribute("error","유효한 이메일이 아닙니다.");
            return "account/email-login";
        }
        if (!account.canSendConfirmEmail()){
            model.addAttribute("error","이메일은 3분에 한번씩 보낼 수 있습니다.");
            return"account/email-login";
        }

        accountService.sendLoginLink(account);
        attributes.addFlashAttribute("message","이메일 인증 메일을 발송했습니다.");
        return "redirect:/email-login";
    }

    @GetMapping("/login-by-email")
    public String loginByEmail(String token, String email, Model model) {
        Account account = accountRepository.findByEmail(email);
        String view = "account/logged-in-by-email";
        if (account == null || !account.isValidToken(token)) {
            model.addAttribute("error", "로그인할 수 없습니다.");
            return view;
        }

        accountService.login(account);
        return view;
    }





}
