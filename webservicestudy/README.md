## 소개
인프런의 백기선님의 '스프링과 JPA 기반 웹 애플리케이션' 강의를 듣고 실습 내용을 코드와 함께 복습용으로 정리하였습니다.  
  

<a href="https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-JPA-%EC%9B%B9%EC%95%B1" target="_blank">강의 링크</a>

## 관련해 작성한 포스팅
<a href="https://backtony.github.io/spring/2021-04-28-spring-project-1/" target="_blank">Spring 웹 프로젝트 정리</a>

본 포스팅은 인프런 백기선님의 '스프링과 JPA 기반 웹 애플리케이션 개발' 강의를 듣고 필요한 부분만 정리했습니다.


## 1. 패키지 구조 정리
---
![그림1](https://backtony.github.io/assets/img/post/spring/project/1-1.PNG)

이번 강의에서 진행한 웹 프로젝트는 다음과 같은 구조를 가지고 있다. 이전까지는 그냥 패키지로만 관리했는데 이번에는 모듈로 묶어보았다. 개발시 모듈 적용이 필수는 아니라고 한다. 클래스들은 패키지로 구성, 패키지들은 모듈로 구성되므로 여러 패키지와 이미지 등의 자원을 모아 놓은 컨테이너를 모듈이라고 생각하면 된다. 아래와 같이 구조를 정리했다. 가급적이면 modules는 infra를 참조할 수 있지만 infra는 modules를 참조하지 않고 Spring이나 JPA같은 라이브러리를 참조하도록 하는 것이 좋다. 예를 들면, config에서 UserDetailsService를 상속받아 작성한 AccountService가 아니라 UserDetailsService를 사용하도록 하는 것이다.

![그림2](https://backtony.github.io/assets/img/post/spring/project/1-2.PNG)

처음부터 위와 같이 정리한 건 아니었다. 강의가 끝나갈 시점에 위와 같이 정리했었는데 첫 그림의 관계와 같이 엩티티는 단방향으로 이루어져있다. Study 패키지는 Event와 Study 패키지에 있는 클래스에서만 사용할 수 있고, Event 패키지는 Study와 Study, Account, Event에 있는 클래스들을 사용할 수 있다고 볼 수 있다. 이런 관계들은 코딩하다보면 실수할 수도 있고 모듈간의 순환 참조가 없는지도 확인해야하는데 이걸 일일이 보면서 확인할 수 없으므로 아키텍처 테스트 유틸리티를 사용하면 쉽게 해결할 수 있다.
```xml
<dependency>
<groupId>com.tngtech.archunit</groupId>
<artifactId>archunit-junit5</artifactId>
<version>0.13.1</version>
<scope>test</scope>
</dependency>
```
dependency 추가해주고 아래와 같은 테스트 코드를 작성할 수 있다.
```java
// XXXapplication.class를 넣으면 된다.
@AnalyzeClasses(packagesOf = WebServiceStudyApplication.class)
public class PackageDependencyTests {

    private static final String STUDY = "..modules.study..";
    private static final String EVENT = "..modules.event..";
    private static final String ACCOUNT = "..modules.account..";
    private static final String TAG = "..modules.tag..";
    private static final String ZONE = "..modules.zone..";

    @ArchTest
    // modules에 있는 것은 modules에 있는 건만 참조하도록
    ArchRule modulesPackageRule = classes().that().resideInAPackage("com.webservicestudy.webservicestudy.module(*)..")
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage("com.webservicestudy.webservicestudy.module(*)..");

    @ArchTest
    // study 패키지 안에 있는 클래스들은 스터디와 event에서만 접근이 가능해야한다는 의미로 작성
    ArchRule studyPackageRule = classes().that().resideInAPackage(STUDY)
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage(STUDY,EVENT);

    @ArchTest
    // event 패키지는 study event account를 참조한다
    ArchRule eventPackageRule = classes().that().resideInAPackage(EVENT)
            .should().accessClassesThat().resideInAnyPackage(STUDY,EVENT,ACCOUNT);

    @ArchTest
    // event 패키지는 study event account를 참조한다
    ArchRule accountPackageRule = classes().that().resideInAPackage(ACCOUNT)
            .should().accessClassesThat().resideInAnyPackage(ZONE,TAG,ACCOUNT);

    @ArchTest
    // 모듈에 있는 것들을 조각내서 그들 간의 순환참조가 있으면 안된다.
    ArchRule cycleCheck = slices().matching("com.webservicestudy.webservicestudy.module(*)..")
            .should().beFreeOfCycles();
}
```
<br>

## 2. 패스워드 인코더
---
```java
@Bean
public PasswordEncoder passwordEncoder(){
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
}

// 인코딩
passwordEncoder.encode(패스워드)
```
패스워드는 문자 그대로 저장하게 되면 보안상 문제가 발생하므로 Spring에서 제공하는 패스워드 인코더를 AppConfig 같은 곳에 빈으로 저장해두고 계정을 저장할 때 입력 받은 패스워드를 인코딩 한 뒤에 저장한다.
<br>

## 3. 이메일 발송
---
회원가입을 무작위로 하게 만들 수 없으니 Email 인증 방식을 사용하기로 해보자. 우선 mail의 패키지 구조는 다음과 같다.
![그림3](https://backtony.github.io/assets/img/post/spring/project/1-3.PNG)



```java
// 회원가입을 시도하면 account를 저장하기 전에 랜덤 토큰 값을 부여하고 저장한다.
public void generateEmailCheckToken() {
    // 랜덤값 부여
    this.emailCheckToken = UUID.randomUUID().toString();
    this.emailTokenGeneratedAt = LocalDateTime.now();
}

// 그리고 서비스계층에서 아래 메서드를 사용해서 메일을 보낸다.
public void sendSignUpConfirmEmail(Account newAccount) {
    Context context = new Context();
    context.setVariable("link", "/check-email-token?token=" + newAccount.getEmailCheckToken() +
            "&email=" + newAccount.getEmail());
    context.setVariable("nickname", newAccount.getNickname());
    context.setVariable("linkName", "이메일 인증하기");
    context.setVariable("message", "스터디올래 서비스를 사용하려면 링크를 클릭하세요.");
    context.setVariable("host", appProperties.getHost()); // 아래서 설명
    // templates에 있는 html 위치, th: 안에 들어갈 값들 넣어주기
    String message = templateEngine.process("mail/simple-link", context);

    EmailMessage emailMessage = EmailMessage.builder()
            .to(newAccount.getEmail())
            .subject("스터디올래, 회원 가입 인증")
            .message(message)
            .build();

    // emailService는 아래서 설명하는데 추상화해서 만들었다.
    // local,test와 dev에서 주입되는 것이 다르므로 위에서는 EmailService를 주입받으면 
    // 설정에 따라 알아서 맞는 것이 주입된다.
    emailService.sendEmail(emailMessage);
}

// 완성한 메시지 담을 객체
@Data
@Builder
public class EmailMessage {

    private String to;

    private String subject;

    private String message;

}
```
TemplateEngine은 주입받은 것인데 thymeleaf에서 제공하는 것이다. html로 메일의 메시지부분을 꾸며놓고 thymeleaf에서 제공하는 Context로 html에 thymeleaf 문법으로 들어갈 내용들을 담아서 process해주면 값들이 채워진 message가 만들어 진다. 이걸 임이의 클래스(여기서는 EmailMessage)에 담아서 객체를 만들고 JavaMailSender을 이용해서 메일을 보내면 된다.  
메일을 보낼때 링크로 타고 오게 하기 위해서는 사이트의 도메인 + 토큰값 등등 으로 URL을 보낼 것이다. 이때 매번 도메인주소를 하드코딩하기 번거로우니 설정으로 넣어두는 방법이 있다.
```java
// application.properties
app.host=http://localhost:8080 # 실제로는 도메인 주소

// config 패키지에 하나 만들어서 사용
@Data
@Component
@ConfigurationProperties("app")
public class AppProperties {
    private String host;
}
```
<Br>

```java
// 로컬에서는 굳이 메일을 보내지 않고 로그로 찍고 실제 dev에서는 메일을 보내도록 하기 위해서
// 서비스를 추상화했다.
public interface EmailService {
    void sendEmail(EmailMessage emailMessage);
}

// 로컬, 테스트에서는 보냈다는 로그만 찍도록
@Slf4j
@Profile({"local","test"})
@Component
public class ConsoleEmailService implements EmailService{

    @Override
    public void sendEmail(EmailMessage emailMessage) {
        log.info("sent email: {}", emailMessage.getMessage());
    }
}


// 실제 보내기
@Slf4j
@Profile("dev")
@Component
@RequiredArgsConstructor
public class HtmlEmailService implements EmailService {
    // 실제로 보내기 위해 JavaMailSender 주입받기
    private final JavaMailSender javaMailSender;

    @Override
    public void sendEmail(EmailMessage emailMessage) {
        // JavaMailSender를 이용해 Mime메시지를 만들고
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            // helper를 이용해 메시지, 멀티파일유무, 인코딩 설정
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            // 메일 채우기
            mimeMessageHelper.setTo(emailMessage.getTo());
            mimeMessageHelper.setSubject(emailMessage.getSubject());
            mimeMessageHelper.setText(emailMessage.getMessage(), true);
            // 메일 보내기
            javaMailSender.send(mimeMessage);
            log.info("sent email: {}", emailMessage.getMessage());
        } catch (MessagingException e) {
            log.error("failed to send email", e);
            throw new RuntimeException(e);
        }
    }
}
```
<br>

실제로 메일을 보내기 위해서는 SMTP 설정이 필요하다. 이게 아마 하루 메일 횟수 제한이 있기에 실제로 서비스할 때는 SendGrid같으 서비스를 사용해야한다고 한다.
```java
//application-dev.properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587

spring.mail.username=본인 gmail 계정

spring.mail.password=발급받은 App 패스워드
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.starttls.enable=true
```
<br>

이렇게 이메일을 보내고 링크를 타고 들어오면 파라미터로 받는 토큰, 이메일을 활용해 accountRepository에서 해당 account를 찾아와서 토큰과 비교해보고 맞다면 최종적으로 가입 처리를 하면 된다.  
현재 내 수준에서는 사용자의 개인정보를 받는 것이 부담스럽다. 따라서 이 이메일을 활용하면 비밀번호 찾기를 대신할 수 있다. 이메일 로그인을 통해 이메일로 인증메일을 보내고 그 인증메일을 통해 접속하면 로그인처리해도록 해주면 된다.
<br>

## 4. 자동 로그인
---
```java
// 정성적인 방법은 아님
public void login(Account account) {
    // 정석적으로라면 사용자가 입력한 아이디와 실제 패스워드를 통해서
    // 토큰을 만들고 AuthenticationManager 를 통해서 인증하고
    // 인증을 거친 토큰을 context에 넣어줘야한다.

    // 하지만 지금은 실제 패스워드를 저장하지 않고 인코딩하기 때문에
    // 위 방법을 사용할 수 없고 직접 토큰을 만들어서 context에 넣어주도록 했다.
    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
            new UserAccount(account),
            account.getPassword(),
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
    );

    SecurityContextHolder.getContext().setAuthentication(token);
}
```
<br>

## 5. remember me
---
```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final DataSource dataSource; // jpa 사용하니까 빈으로 등록되어있음


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 가장 안전한 방법 사용
        // username, 토큰(랜덤,매번 바뀜), 시리즈(랜덤,고정) -> 이 3가지를 조합해서 db에 저장 -> 나중에 사용자가 rememberme 토큰을 보내면 일치하는지 확인
        // 탈취당하면 토큰은 바뀌게 되고, 피해자는 전 토큰으로 로그인 시도 -> 모든 토큰 자동 삭제
        http.rememberMe()
                .userDetailsService(userDetailsService) // tokenrepository 사용시에는 userdetailsservice도 같이 설정해야함
                .tokenRepository(tokenRepository()); // db에서 토큰 값을 읽어오거나 저장하는 인터페이스의 구현체를 주입
    }

    @Bean
    public PersistentTokenRepository tokenRepository(){
        // JdbcTokenRepositoryImpl는 jdbc 기반의 토큰 구현체
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource); // jdbc니까 당연히 datasource 필요 -> jpa를 사용하고 있으니까 datasource는 빈에 등록되어있음
        return jdbcTokenRepository;

        // JdbcTokenRepositoryImpl이 사용하는 table이 db에 반드시 있어야 한다.
        // 타고 들어가면 설명이 적혀있는데
        // "create table persistent_logins (username varchar(64) not null, series varchar(64) primary key, token varchar(64) not null, last_used timestamp not null)";
        // db에 위의 테이블이 있어야 한다는 뜻이다
        // 현재는 인메모리 db를 사용하고 있으니 위 테이블에 해당하는 엔티티를 만들어 테이블이 알아서 만들어도록 하면 된다.
    }
}

@Table(name = "persistent_logins")
@Entity
@Getter @Setter
// rememberme에서 jabctokenrepository가 사용하는 테이블이 있는데
// 그걸 그냥 엔티티로 만들면 테이블이 되니 그걸 그대로 엔티티로 옮긴것
public class PersistentLogins {
    @Id
    @Column(length =64)
    private String series;

    @Column(nullable = false,length = 64)
    private String username;

    @Column(nullable = false,length = 64)
    private String token;

    @Column(name = "last_used",nullable = false,length = 64)
    private LocalDateTime lastUsed;

}
```
<br>

## 6. 테스트를 위한 인증된 사용자 기능
---
인증된 사용자를 기준으로 테스트를 작성해야할 때가 있다. 따라서 인증된 사용자를 제공할 커스텀 애노테이션을 만들어보자.
```java
// 커스텀 애노테이션 생성
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithAccountSecurityContextFactory.class)
// WithSecurityContext의 팩토리를 WithAccountSecurityContextFactory로 사용
// 이제 WithAccountSecurityContextFactory는 WithSecurityContextFactory 인터페이스의 구현체로 만들어야함
public @interface WithAccount {

    String value(); // 애노테이션의 파람으로 넘어로는 값을 value로 받을 수 있음
}

// 이 클래스는 WithAccountSecurityContextFactory의 구현체로 빈으로 자동 등록되므로 빈 주입 가능
// WithSecurityContextFactory의 제네릭으로 커스텀한 애노테이션 넣기
@RequiredArgsConstructor
public class WithAccountSecurityContextFactory implements WithSecurityContextFactory<WithAccount> {

    private final AccountService accountService;

    @Override
    public SecurityContext createSecurityContext(WithAccount withAccount) {

        // 애노테이션 파라미터로 받은 이름
        String nickname = withAccount.value();

        // 유저 만들고 저장
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname(nickname);
        signUpForm.setEmail(nickname+"@email.com");
        signUpForm.setPassword("123456789");
        accountService.processNewAccount(signUpForm);


        // security context에 넣는 작업 시작 //

        // 뽑아오고
        UserDetails principle = accountService.loadUserByUsername(nickname);
        // 뽑아온걸로 토큰 만들고
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principle,principle.getPassword(),principle.getAuthorities()
        );
        // 빈 컨텍스트 만들고
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        // 컨텍스트에 토큰 만든 토큰 넣기
        context.setAuthentication(authentication);

        // 해당 컨텍스트 반환
        return context;
    }
}
```
해당 애노테이션을 사용하면 테스트 시작전에 계속 유저가 만들고 저장되어 AfterEach로 accountRepository.deleteAll로 지워주라고 하는데 그냥 Transactional 애노테이션을 사용하면 되지 않을까 라는 생각이 든다.

<br>

## 7. 엔티티 그래프를 사용한 성능 최적화
---
김영한님의 강의에서는 XXToOne 관계는 페치 조인(JPA에서는 join fetch, Querydsl에서는 fetchjoin)을 사용해서 N+1 문제를 해결, XXToMany관계는 페이징이 필요 없다면 페치조인과 distinct를 사용하고 페이징이 필요한 경우 BatchSize를 사용한다고 했다. 하지만 이번 백기선님 강의에서는 EntityGraph를 사용했다. 쿼리가 복잡할 경우는 직접 짜는게 좋은데 아직까지는 간단한 부분만 다루고 있으므로 현재 사용에서는 엔티티그래프가 훨씬 편했다.  
```java
// 해당 엔티티에 애노테이션 붙이기
@NamedEntityGraph(name = "Study.withAll", attributeNodes = {
       @NamedAttributeNode("tags"),
       @NamedAttributeNode("zones"),
       @NamedAttributeNode("managers"),
       @NamedAttributeNode("members")})

// 해당 엔티티리포지토리의 해당 쿼리문에 붙이기
@EntityGraph("Study.withAll")
```
엔티티 그래프의 기본적인 사용 방식은 해당 엔티티에 다음과 같이 fetch join으로 땡겨올 필드값들과 이 엔티티 그래프의 이름을 지정해주고 해당 엔티티 리포지토리의 쿼리문에 @EntityGraph와 이름을 함께 주면 해당 쿼리문을 실행할 때 정해진 필드값들을 fetch join으로 땡겨온다.  
사실 위의 방법이 가장 기본적이지만 작성하는 것이 번거롭기 때문에 아래와 같이 사용한다.
```java
@EntityGraph(attributePaths = {"managers","members"},type = EntityGraph.EntityGraphType.FETCH)
Study findStudyWithManagersAndMembersById(Long id);
```
페치 조인으로 땡겨올 필드값을 바로 주면 끝이다. type의 기본값은 FETCH로 안적어줘도 된다. type은 2가지가 있다.
+ FETCH : 명시한 필드만 EAGER, 나머지는 전부 LAZY 처리
+ LOAD : 명시한 필드는 EAGER, 나머지는 기본 FETCH 전략에 따라 수행
+ 엔티티 그래프를 사용하면 엔티티에 fetch타입을 지정해줘도 무시되고 위에서 설정한 타입으로 수행된다.

<br>

간단하게 N+1문제를 해결하기 위한 최적화 방법을 엔티티그래프로 알아봤다. 그런데 만약 Enrollment라는 엔티티가 Event라는 엔티티를 가지고 있고 Event라는 엔티티 안에 Study라는 엔티티가 있다고 가정해보자. Enrollment를 땡겨올 때 Event 엔티티가 필요하다면 위에서 설명한대로 땡겨오면 된다. 여기서 Event 엔티티의 Study라는 엔티티까지 사용해야하는 경우라면 Study도 같이 땡겨올 수 있는 방법이 필요하다. 이때 사용되는게 서브그래프이다.  
서브그래프는 간단한 방법은 사용할 수 없고 정석적으로 엔티티에 NamedEntityGraph를 붙여서 사용해야 한다.
```java
// 엔티티에 붙이기
@NamedEntityGraph(
        name = "Enrollment.withEventAndStudy",
        attributeNodes = {
                // event를 땡겨올때 event를 대상으로 subgraph가 실행되어 땡겨온다.(event의 study도 땡겨온다)
                @NamedAttributeNode(value = "event", subgraph = "study") 
        },
        //
        subgraphs = @NamedSubgraph(name = "study", attributeNodes = @NamedAttributeNode("study"))
)

// 리포지토리 해당 쿼리문에 붙이기
@EntityGraph("Enrollment.withEventAndStudy")
```
<br>

## 8. TEST DB 도커로 띄우기
---
현재 코딩에서 운영용 DB로는 PostgreSQL을 사용하고 있고 TEST DB는 H2를 사용하고 있다. JPA 또는 하이버네티으가 만들어주는 쿼리가 각 DB밴더에 따라 다르므로 일치시켜주는게 좋다. 하지만 테스트 DB를 따로 운영하는 것은 매우 번거로우므로 테스트용 PostgreSQL DB는 도커로 띄우도록 하자. 

```xml
<!-- TestContainers 설치 -->
<dependency>
<groupId>org.testcontainers</groupId>
<artifactId>junit-jupiter</artifactId>
<version>1.13.0</version>
<scope>test</scope>
</dependency>

<!-- TestContainers PostgreSQL 모듈 설치 -->
<dependency>
<groupId>org.testcontainers</groupId>
<artifactId>postgresql</artifactId>
<version>1.13.0</version>
<scope>test</scope>
</dependency
```
![그림4](https://backtony.github.io/assets/img/post/spring/project/1-4.PNG)

Test에서 디렉토리를 만들 때 resources를 선택해서 만들고 다음을 추가한다.
```java
spring.jpa.hibernate.ddl-auto=update

// 드라이버 클래스는 testContainer가 주는 걸 사용
spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver
// tc : testContainer을 의미, posetgresql: 뒤에는 아무상관 없다
spring.datasource.url=jdbc:tc:postgresql:///studytest
```
이제 test를 작성할 때 각 test 클래스마다 @Profile("test")를 붙이면 된다.  
매번 테스트 클래스마다 하나씩 컨테이너를 띄우면 하나 띄우는 것도 느린데 엄청 느려진다. 따라서 아래와 같이 추상 클래스를 만들어 테스트들이 이 추상 클래스를 상속받으면 하나의 컨테이너만 뜨게 만들 수 있다. 참고로 당연히 도커는 켜놔야 된다. [[해당자료링크](https://www.testcontainers.org/test_framework_integration/manual_lifecycle_control/#sin
gleton-containers
)]
```java
// 도커 test 컨테이너 띄우는 작업
// 이걸 각각의 TEST가 상속받으면 테스트 작동시 하나의 컨테이너만 뜬다.
public abstract class AbstractContainerBaseTest {

    static final PostgreSQLContainer POSTGRE_SQL_CONTAINER;

    // static 블록은 클래스 로딩할 때 한번 호출된다.
    static {
        POSTGRE_SQL_CONTAINER = new PostgreSQLContainer();
        POSTGRE_SQL_CONTAINER.start();
    }
}

// 각각의 테스트 예시
class EventControllerTest extends AbstractContainerBaseTest{}
public class StudyControllerTest extends AbstractContainerBaseTest {}
```
<br>

## 9. 알림 인프라 설정
---
만들고 있는 웹 프로젝트는 원하는 스터디가 생성,수정 등이 되면 알림을 주는 기능이 있다. 그럼 스터디가 생성, 수정되는 서비스계층 코드에서 알림을 보내는 코드를 추가해주면 된다. 하지만 이런 일 자체가 부가적인 기능이므로 메인 로직에 영향을 주고 싶지 않은 상황이다. 게다가 알림이 실패할 경우 해당 메인 로직자체가 수행이 안되기 때문에 스프링이 제공하는 EventPublisher와 @Async 기능을 사용해서 비동기 이벤트 기반으로 처리해서 해결할 수 있다.  
여기서는 스터디를 만들었을 때 알림을 주는 기능만 알아보자.  
```java
@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {
    private final ApplicationEventPublisher eventPublisher;

    public Study createNewStudy(Study study, Account account) {
        Study newStudy = studyRepository.save(study);
        newStudy.addManager(account);
        eventPublisher.publishEvent(new StudyCreatedEvent(newStudy));
        return newStudy;
    }

// 이벤트 만들었을 때 사용할 클래스
@Data
@RequiredArgsConstructor
public class StudyCreatedEvent {
    private final Study study;
}

// 수정했을 때 사용할 클래스
@Getter
@RequiredArgsConstructor
public class StudyUpdateEvent {
    private final Study study;
    private final String message;
}
```
먼저 서비스 계층에서 스터디를 만들 때 eventPublisher.publishEvent 코드가 추가된다. 이벤트리스너는 파라미터 타입으로 구분하기 때문에 알림 기능에서 이벤트가 만들어졌을 때 보내는 리스너랑 이벤트가 수정되었을 때 보내는 리스너가 다르므로 각각의 데이터를 구분할 클래스를 만들어 사용해야 한다.  
<Br>

리스너는 다음과 같다.
```java
@Component
@Slf4j
@Async // 비동기적으로 사용, 다른 스레드로 돈다는 뜻
@Transactional
@RequiredArgsConstructor
public class StudyEventListener {

    private final StudyRepository studyRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final NotificationRepository notificationRepository;

    // eventPublisher.publishEvent(new StudyCreatedEvent(newStudy));가 호출되면 아래 리스너가 동작하게 된다.
    @EventListener
    public void handleStudyCreatedEvent(StudyCreatedEvent studyCreatedEvent) {
        
        // StudyCreatedEvent의 study는 로직상 manager만 땡겨온 상태로 저장되있어서 나머지는 lazy 로 저장되어있다.
        // 현재는 detached 상태이므로 다시 프록시를 사용할 수 없으므로 다시 조회해야한다.
        Study study = studyRepository.findStudyWithTagsAndZonesById(studyCreatedEvent.getStudy().getId());
        // querydsl의 predicate 이용한 쿼리가져와서 사용
        // 여기서는 predicate를 사용했는데 그냥 리포지토리 따로 만들어 구현체 만들고 거기서
        // 작성하는 내가 원래 했던 방식이 편한것 같다. 
        Iterable<Account> accounts = accountRepository.findAll(AccountPredicates.findByTagsAndZones(study.getTags(), study.getZones()));

        accounts.forEach(account -> {
            if (account.isStudyCreatedByEmail()) {
                // 이메일 알림 보내기
                sendStudyCreatedEmail(study, account,"새로운 스터디가 생겼습니다.",
                        "스터디올래, '" + study.getTitle() + "' 스터딕가 생겼습니다.");
            }

            if (account.isStudyCreatedByWeb()) {
                // 웹으로 해당 계정에 알림 보내기
                createNotification(study,account,study.getShortDescription(),NotificationType.STUDY_CREATED);
            }
        });
    }

    // 수정할때 사용하는 리스너로 파라미터 타입으로 구분
    @EventListener
    public void handleStudyUpdateEvent(StudyUpdateEvent studyUpdateEvent){}

    // 로직 메서드로 뽑음 -> 다른 리스너에서도 재활용하기 위함
    private void createNotification(Study study, Account account, String message, NotificationType notificationType) {
        Notification notification = new Notification();
        notification.setTitle(study.getTitle());
        notification.setLink("/study/" + study.getEncodedPath());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(message);
        notification.setAccount(account);
        notification.setNotificationType(notificationType);
        notificationRepository.save(notification);
    }

    private void sendStudyCreatedEmail(Study study, Account account, String contextMessage, String emailSubject) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/study/" + study.getEncodedPath());
        context.setVariable("linkName", study.getTitle());
        context.setVariable("message", contextMessage);
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject(emailSubject)
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }
}
```
<br>

이제 로직은 다 구현했으니 Async가 동작하도록 설정만 해주면 된다.
```java
@Slf4j
@Configuration
@EnableAsync // 이 애노테이션만 적고 나둬도 aync하게 동작 가능하다.
public class AsyncConfig implements AsyncConfigurer {
    // Async Executor 설정
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int processors = Runtime.getRuntime().availableProcessors();
        log.info("processors count {}",processors);  // 몇개 있는지 찍어봄 시험삼아

        executor.setCorePoolSize(processors);
        executor.setMaxPoolSize(processors*2);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(60); // maxpoolsize로 인해 덤으로 더 돌아다니는 튜브는 60초 후에 수거해서 정리
        executor.setThreadNamePrefix("AsyncExecutor-"); // executor 이름주기 나중에 로깅에서 찾기 편한
        executor.initialize(); // 초기화후 반환, 초기화 꼭 해야함
        return executor;
    }
}
```
executor는 비유를 통해 설명해보자. 리스너가 튜브, 스터디가 사람이라고 생각해보면 풀장에 튜브가 10개만 있다고 해보자. 사람들이 3명이 와서 튜브를 타고 있다(activeThread:현재 일하는 스레드 개수). 사람들이 10명이 와서 꽉차면 11명부터는 줄을 세운다. 그것이 setQueueCapacity이다. 큐도 꽉차서 51번째 사람이 오면 큐의 만 앞에서 기다리고 있는 사람에게 튜브 하나를 만들어 준다. 이 행동은 maxPoolSize가 다 찰때까지 진행한다. maxPoolSize까지 꽉차면 더이상 executor가 task를 처리할 수 없게 된다.  
이렇게 설정까지 마치게 되면 리스너는 Async하게 비동기적으로 동작한다.  
<br>

알림기능을 완성했다면 웹알림의 경우 계정마다 알림이 잘 들어갔을 것이다. 그런데 사이트를 돌아다니다가 알림이 있으면 화면에 알림 아이콘을 활성화 시켜주고 싶을 수 있다. 그럼 매 요청이 들어올 때마다 해당 계정이 읽지 않은 알림이 있는지 확인해서 model에 담아 보내주면 된다. 그럼 모든 요청에 대해 이 로직을 일일이 붙여줘야할까? 이런 문제를 HandlerInterceptor가 해결해준다.
```java

// 모든 요청마다 알람을 확인해서 알람이 있는지 없는지 보여주고 싶다
// 그럼 모든 컨트롤러에 다 그 메서드를 붙여줘야하나?
// 그것을 해결하기 위해 mvc가 제공하는 HandlerInterceptor가 있다.
@Component
@RequiredArgsConstructor
public class NotificationInterceptor implements HandlerInterceptor {
    private final NotificationRepository notificationRepository;

    // prehandle : 핸들러 들어가기 전에 실행
    // aftercompletion : 뷰 랜더링 끝난 다음
    // posthandle : 핸들러 처리 이후 뷰 랜더링 전

    // 인증정보가 있는 사용자의 요청에서만 알림을 줘야함
    // 리다이렉트 요청에도 적용하지 않을것 -> 어쩌피 리다이렉트에서 핸들러를 다시 탐
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // modelAndView를 쓰는 경우에만 넣어줄 것임
        // 뷰를 랜더링 전에 model에 "hasNotification"를 넣어주는 작업
        if (modelAndView != null && !isRedirectView(modelAndView) && authentication != null && authentication.getPrincipal() instanceof UserAccount){
            Account account = ((UserAccount) authentication.getPrincipal()).getAccount();
            long count = notificationRepository.countByAccountAndChecked(account, false);
            modelAndView.addObject("hasNotification",count>0);
        }
    }

    private boolean isRedirectView(ModelAndView modelAndView) {
        // 뒤에 redirectview타입은 문자열로 redirect가 아니라 무슨 new RedirectView("/")이렇게 쓰는 경우를 말한다.
        return modelAndView.getViewName().startsWith("redirect:") || modelAndView.getView() instanceof RedirectView;
    }
}
```
로직은 구현했으니 이제 설정만 해주면 된다.  
<br>

```java
// 만든 인터셉터 추가하기
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final NotificationInterceptor notificationInterceptor;

    // 인터셉터 추가
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 인터셉터 추가하기 전에 인터셉터를 걸지 않을 URI에 대한 설정 작업
        List<String> staticResourcesPath = Arrays.stream(StaticResourceLocation.values())
                // StaticResourceLocation 리스트들을 하나의 리스트로 합침
                .flatMap(StaticResourceLocation::getPatterns)
                .collect(Collectors.toList());
        staticResourcesPath.add("/node_modules/**");

        // 인터셉터 적용 제외 범위
        registry.addInterceptor(notificationInterceptor)
                // 파라미터로 스트링 list타입으로 넣어야 한다.
                .excludePathPatterns(staticResourcesPath);
    }
}
```
+ values() 메소드는 해당 열거체의 모든 상수를 저장한 배열을 생성하여 stream으로 반환, 이 메소드는 자바의 모든 열거체에 컴파일러가 자동으로 추가해 주는 메소드
+ flatmap : 여러개의 스트림을 한개의 스트림으로 합쳐준다.


<Br>

## 10. 검색 기능
---
Querydsl을 사용해서 구현해보자.

```java
// mainController
@GetMapping("/search/study")
// pageable는 spring domain것 사용 
// pageable로 size, sort, page 파라미터를 하나로 받음
// pageable size 기본값은 20, @pageableDefault로 기본값 수정가능, 
// 해당 애노테이션 붙이면 기본값이 다르게 바뀌는데 그건 ctrl space로 확인해보면 된다.
public String searchStudy(@PageableDefault(size = 9,sort = "publishedDateTime",
                direction = Sort.Direction.ASC) Pageable pageable, String keyword, Model model){

    Page<Study> studyPage = studyRepository.findByKeyword(keyword, pageable);
    // 키 생략하고 넣으면 타입으로 첫글자 소문자로 들어간다고 했다. 그런데 여기서 만약 값이 null이면 model에 안넘어간다. 그래서 내 생각에는 키값을 명시해주는게 좋을 것 같다.
    model.addAttribute("studyPage",studyPage);
    model.addAttribute("keyword",keyword);
    model.addAttribute("sortProperty",
            pageable.getSort().toString().contains("publishedDateTime") ? "publishedDateTime" : "memberCount");
    return "search";
}

// querydsl용 확장 리포지토리
@Transactional(readOnly = true)
public interface StudyRepositoryExtension {
    Page<Study> findByKeyword(String keyword, Pageable pageable);
}

// StudyRepositoryExtension의 구현체 -> 클래스명이 반드시 인터페이스+Impl 이어야 한다.
// QueryDslRepositorySupport 는 Querydsl 을 편하게 사용할 수 있는 기능을 지원해준다.
public class StudyRepositoryExtensionImpl extends QuerydslRepositorySupport implements StudyRepositoryExtension{

    // QuerydslRepositorySupport는 기본생성자가 없고 인자가 있는 생성자가 있음 -> 그래서 인자 넣어줘야함함
    // intellij의 도움에 의해서 생성자를 자동으로 만들었는데 사실 우리는 어떤 도메인 타입을 다룰건지 이미 정했음 ->Study
    // 따라서 파라미터로 받을 필요도 없이 그냥 알고있는 도메인을 넣으면 된다.
   public StudyRepositoryExtensionImpl() {
        super(Study.class);
    }

    @Override
    public Page<Study> findByKeyword(String keyword, Pageable pageable) {
        QStudy study = QStudy.study;
        // QuerydslRepositorySupport을 사용하게 되면 from으로 시작하고 마지막에 select문을 작성한다.
        // select 생략시 기본적으로 from의 첫번째 엔티티가 프로젝션의 대상
        JPQLQuery<Study> query = from(study).where(study.published.isTrue()
                .and(study.title.containsIgnoreCase(keyword)) // 대소문자구분없이 keyword가 포함되어 있는지
                .or(study.tags.any().title.containsIgnoreCase(keyword)) // tag들중 어느 하나라도 대소문자 구분 없이 keyword포함하는지
                .or(study.zones.any().localNameOfCity.containsIgnoreCase(keyword)))
                .leftJoin(study.tags, tag).fetchJoin()
                .leftJoin(study.zones, zone).fetchJoin()
                .leftJoin(study.members, QAccount.account).fetchJoin() // 몇명 있는지도 화면에 띄워야해서 필요함
                // 현재 일다다 관계로 조인하면 데이터 뻥튀기된다. -> 페이징 처리는 아래서 수행 
                .distinct()
                ;

        //QuerydslRepositorySupport가 제공함 -> pageable과 쿼리를 넣으면 페이징이 적용된 쿼리를 반환
        JPQLQuery<Study> pageableQuery = getQuerydsl().applyPagination(pageable, query);
        QueryResults<Study> fetchResults = pageableQuery.fetchResults();// 그냥 fetch는 데이터만, 페이징 포함은 fectchResult
        
        // 페이징된 값을 반환할 것이니 반환타입도 Page
        // 결과, pageable, 총 개수
        return new PageImpl<>(fetchResults.getResults(),pageable, fetchResults.getTotal());

    }
}

// 원래 리포지토리에 상속
@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long>, StudyRepositoryExtension {}
```
처음에 김영한님의 강의에서는 QuerydslRepositorySupport를 사용한 적이 없어서 이건 뭔가 하고 찾아봤는데 김영한님 강의에서도 부록에 있었다. QuerydslRepositorySupport를 사용하면 JPAQueryFactoy에 EntityManager을 주입받는 코드를 줄일 수 있고 페이징을 조금 더 쉽게 처리할 수 있는 장점이 있긴 한데 from이 먼저 나오다보니 가독성이 떨어지고 Querydsl 3.xx 버전을 기준으로 나온 것으로 Querydsl 4.x에서 나온 JPAQueryFactory로 시작할 수 없다는 단점이 있기에 그냥 JPAQueryFactory를 사용하는 방식을 사용하는게 더 나은 것 같다.  

__cf) Maven Querydsl 설정__
```xml
<dependency>
<groupId>com.querydsl</groupId>
<artifactId>querydsl-jpa</artifactId>
</dependency>

<plugin>
    <groupId>com.mysema.maven</groupId>
    <artifactId>apt-maven-plugin</artifactId>
    <version>1.1.3</version>
    <executions>
        <execution>
            <goals>
                <goal>process</goal>
            </goals>
            <configuration>
                <outputDirectory>target/generated-sources/java</outputDirectory>
                <processor>com.querydsl.apt.jpa.JPAAnnotationProcessor</processor>
            </configuration>
        </execution>
    </executions>
    <dependencies>
        <dependency>
            <groupId>com.querydsl</groupId>
            <artifactId>querydsl-apt</artifactId>
            <version>${querydsl.version}</version>
        </dependency>
    </dependencies>
</plugin>
```
<br>

__cf) 참고__  
만들다가 컬렉션에서 원하는 것이 있는지 확인하는 JPA 쿼리를 작성해야하는 것이 있었는데 Containing을 이용해 쉽게 사용할 수 있었다. 첫 5개, 멤버컬렉션에 파라미터에 해당하는 account가 있는지 확인이 쉽게 가능했다.
```java
List<Study> findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);
```
<br>

## 11. 에러 핸들러 및 뷰 추가
---
에러 페이지는 그냥 templates에 error.html로 추가하면 끝이다. 추가적으로 어떤 요청으로 잘못된 요청을 보내는가 파악하기 위해 로그를 찍고 싶으면 아래와 같이 클래스르 하나 만들어 적용시키면 된다.
```java
@Slf4j
@ControllerAdvice // MVC 예외 처리하기 위한 애노테이션
public class ExceptionAdvice {

    // 어떤 요청으로 잘못된 요청을 보내는가 파악하기 위한 핸들러
    @ExceptionHandler
    public String handleRuntimeException(@CurrentUser Account account, HttpServletRequest request, RuntimeException e){
        if(account != null){
            log.info("'{}' requested '{}'",account.getNickname(),request.getRequestURI());
        } else{
            log.info("requested '{}'",request.getRequestURI());
        }
        log.error("bad request",e);
        return "error"; // 이렇게 하면 잘못된 요청이 들어오면 로깅하고 에러 페이지로 보냄
    }
}
```
<br>

## 12. InitBinder
---
앞선 API 포스팅에서는 @Valid로 값을 걸러주고 논리적 오류에 대해서는 클래스를 따로 만들어 메서드를 만들고 논리적 오류가 있는 경우 error에 담아도록 만들고 컨트롤러에서 사용했다. 이런 방법도 있지만 @InitBinder를 사용하면 조금 더 깔끔하게 처리할 수 있다. InitBinder를 사용하면 파라미터가 바인딩되는 시점에 검사해주기 때문에 따로 메서드를 불러올 필요가 없게 된다.
```java
// 예시
// 컨트롤러
@InitBinder("nicknameForm")
    public void initBinderNickname(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameValidator); // 빈등록 되어있어서 new 쓰면 안됨
    }

// 클래스 따로 만들기
// repository가 필요한데 빈주입은 빈끼리만 가능하므로 빈등록
// 가입할 때 들어오는 SignUpForm에서 nickname과 email 중복 검사
// validator 구현체
@Component
@RequiredArgsConstructor
public class NicknameValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> aClass) {
        return NicknameForm.class.isAssignableFrom(aClass); // 지원 여부 확인
    }

    @Override
    // 닉네임 중복이 있는지 확인
    public void validate(Object o, Errors errors) {
        NicknameForm nameForm = (NicknameForm) o;
        Account byNickname = accountRepository.findByNickname(nameForm.getNickname());
        if (byNickname != null){
            // 필드명, 에러코드, 메시지
            errors.rejectValue("nickname","wrong.value","입력하신 닉네임은 사용할 수 없습니다.");
        }
    }
}
```
Validator의 구현체를 만들어 작성하고 컨트롤러에서 @InitBinder("검증할클래스에 맨앞문자만소문자로") 설정해주고 만든 validator를 추가해주면 끝이다.





<br>

---
__본 포스팅은 인프런 백기선님의 '스프링과 JPA 기반 웹 애플리케이션 개발' 강의를 듣고 정리한 내용을 바탕으로 복습을 위해 작성하였습니다. [[강의 링크](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-JPA-%EC%9B%B9%EC%95%B1#){:target="_blank"}]__