package com.webservicestudy.webservicestudy.modules.account;

import com.webservicestudy.webservicestudy.infra.config.AppProperties;
import com.webservicestudy.webservicestudy.modules.account.form.SignUpForm;
import com.webservicestudy.webservicestudy.modules.tag.Tag;
import com.webservicestudy.webservicestudy.modules.zone.Zone;
import com.webservicestudy.webservicestudy.infra.mail.EmailMessage;
import com.webservicestudy.webservicestudy.infra.mail.EmailService;
import com.webservicestudy.webservicestudy.modules.account.form.NicknameForm;
import com.webservicestudy.webservicestudy.modules.account.form.Notifications;
import com.webservicestudy.webservicestudy.modules.account.form.Profile;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    public Account processNewAccount(SignUpForm signUpForm){
        Account newAccount = saveNewAccount(signUpForm);
        sendSignUpConfirmEmail(newAccount);
        return newAccount;
    }


    private Account saveNewAccount(SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account account = modelMapper.map(signUpForm, Account.class);
        account.generateEmailCheckToken();
        return accountRepository.save(account);
    }

    public void sendSignUpConfirmEmail(Account newAccount) {
        Context context = new Context();
        context.setVariable("link", "/check-email-token?token=" + newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail());
        context.setVariable("nickname", newAccount.getNickname());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("message", "스터디올래 서비스를 사용하려면 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(newAccount.getEmail())
                .subject("스터디올래, 회원 가입 인증")
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

    // 정성적인 방법은 아님
    public void login(Account account) {
        // account 로 토큰 만들어서 컨텍스에 넣어주는 형식으로 정석은 아님
        // 이렇게 하는 이유는 인코딩한 패스워드 뿐이 접근하지 못하기 때문이다
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        SecurityContextHolder.getContext().setAuthentication(token);



    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
        // 로그인 가능 방법을 아이디로 nickname과 email 둘다 가능하도록 할 예정
        Account account = accountRepository.findByEmail(emailOrNickname);
        if(account == null){
            account = accountRepository.findByNickname(emailOrNickname);
        }

        // 두가지 다 조회해도 없으면 없다고 던지기
        if(account == null){
            throw new UsernameNotFoundException(emailOrNickname);
        }

        return new UserAccount(account);
    }

    public void completeSignup(Account account) {
        account.completeSignUp();
        login(account);

    }

    public void updateProfile(Account account, Profile profile) {
        modelMapper.map(profile,account);
        // 현재 들어온 account는 detached 상태로 transactional로 데이터를 변경해도 변경되지 않음
        // save는 account의 id값이 있으면 merge 시킨다
        // merge하기 싫다면 find로 db에서 꺼내와서 persistence 상태로 만들어야함
        accountRepository.save(account);
    }

    public void passwordUpdate(Account account, String newPasswordConfirm) {
        account.setPassword(passwordEncoder.encode(newPasswordConfirm));
        accountRepository.save(account);
    }

    public void updateNotifications(Account account, Notifications notifications) {
        // source -> destination
        // 여기서는 modelmapper가 이해를 못한다.
        // 문자를 어디까지 잘라서 해석해야할지 이해하지 못한다 -> notifications에는 email 값이 여러개인데 어느걸로 세팅할지를 못찾음
        // 이를 위해 빈등록시 설정필요
        modelMapper.map(notifications,account);
        accountRepository.save(account);
    }

    public void updateNickname(Account account, NicknameForm nickNameForm) {
        account.setNickname(nickNameForm.getNickname());
        accountRepository.save(account);
        login(account); // 로그인 해줘야 오른쪽 상단에 이름이 바뀜
    }

    public void sendLoginLink(Account account) {
        Context context = new Context();
        context.setVariable("link", "/login-by-email?token=" + account.getEmailCheckToken() +
                "&email=" + account.getEmail());
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", "스터디올래 로그인하기");
        context.setVariable("message", "로그인 하려면 아래 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("스터디올래, 로그인 링크")
                .message(message)
                .build();
        emailService.sendEmail(emailMessage);
    }

    public void addTag(Account account, Tag tag) {
        // account가 detached 상태임
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().add(tag)); // optional 장점, 만약 존재하면 어떤 로직 수행

        // account를 persistence 상태로 올리기 위해서는 findById 혹은 getOne을 사용할 수 있다.
        // getOne은 lazy 로딩이고 findById는 eager 이다.
    }

    public Set<Tag> getTags(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        // 없으면 에러 던지고 있으면 태그 반환
        return byId.orElseThrow().getTags();
    }

    public void removeTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().remove(tag));
    }

    public Set<Zone> getZones(Account account) {
        // account는 detached 상태고 zone는 manytomany로 lazy -> 프록시 -> persistence 상태로 만들어야함
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getZones();

    }

    public void addZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().add(zone));
    }

    public void removeZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().remove(zone));
    }

    public Account getAccount(String nickname) {
        Account account = accountRepository.findByNickname(nickname);
        // path로 받은 사용자가 없을 때
        if(account == null){
            throw new IllegalArgumentException(nickname+"에 해당하는 사용자가 없습니다.");
        }
        return account;
    }
}
