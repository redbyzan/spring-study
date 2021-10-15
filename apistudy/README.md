## 소개
인프런의 백기선님의 '스프링 기반 REST API 개발' 강의를 듣고 실습 내용을 코드와 함께 복습용으로 정리하였습니다.  

<a href="https://www.inflearn.com/course/spring_rest-api" target="_blank">강의 링크</a>

## 관련해 작성한 포스팅
<a href="https://backtony.github.io/spring/2021-04-16-spring-api-1/" target="_blank">Spring REST API - 정리</a>



## 1. REST API
---
+ API : Application Programming Interface
+ REST 
    - Representational State Trnsfer
    - 인터넷 상의 시스템 간의 상호 운용성을 제공하는 방법 중 하나
    - 시스템 제각각의 독립적인 진화를 보장하기 위한 방법
+ REST API : REST 아키텍처 스타일을 따르는 API
    - 지켜야 할 것
        - self-descrive messages
            - 메시지 스스로 메시지에 대한 설명이 가능해야 한다.
            - 서버가 변해서 메시지가 변해도 클라이언트는 그 메시지를 보고 해석이 가능해야 한다
            - 확장 가능한 커뮤니케이션
        - HATEOAS
            - 하이퍼링크(미디어)를 통해 애플리케이션 상태 변화가 가능해야 한다.
            - 링크 정보를 동적으로 바꿀 수 있다.
    - 해결 방안
        - Self-descriptive message 해결
            - HAL의 스펙을 사용하여 응답 바디에 profile 링크 추가            
        - HATEOAS 해결
            - HAL의 스펙을 사용하여 데이터에 링크 제공


<br>

## 2. 전체적 시나리오 - EVENT 만들기
---
profile 에 대한 문서화는 스프링 REST Docs로 만들기  

+ GET /api/events
    + 이벤트 목록 조회 - 로그인 안 한 상태
        - 응답에 보여줘야 할 데이터
            - 이벤트 목록
            - 링크
                - self
                - profile : 이벤트 목록 조회 API 문서로의 이동 링크
                - get-an-event : 이벤트 하나 조회하는 API 링크
                - next : 다음 페이지
                - prev : 이전 페이지
    + 이벤트 목록 조회 - 로그인 한 상태
        - 응답에 보여줘야 할 데이터
            - 이벤트 목록
            - 링크
                - self
                - profile : 이벤트 목록 조회 API 문서로의 이동 링크
                - get-an-event : 이벤트 하나 조회하는 API 링크
                - create-new-event : 이벤트 생성할 수 있는 API 링크
                - next : 다음 페이지
                - prev : 이전 페이지
+ POST /api/events
    - 이벤트 생성
+ GET /api/events/{id}
    - 이벤트 하나 조회
+ PUT /api/events/{id}
    - 이벤트 수정

## 3. 시작하기 전
---
### 엔티티 애노테이션
```java
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @NoArgsConstructor @AllArgsConstructor
```
모든 엔티티는 대부분 위와 같은 애노테이션을 붙인다. EqualsAndHashCode에서 id만 선택해준 이유는 무한루프 때문이다. setter을 엔티티에 붙여주는 건 좋지 않다고 알고 있지만 클래스의 편리한 변환을 제공하는 Modelmapper가 setter로 동작한다. 백기선님의 의견에 따르면 Setter 자체가 위험한게 아니라 어떻게 쓰느냐의 문제라고 한다. 따라서 내 기준에서는 Setter을 붙여서 modelmapper을 사용하지만, 따로 setter을 사용하지 않는 쪽을 선택했다.  
<br>

### Modelmapper
편리하게 객체끼리의 변환을 지원한다. 사용하기 위해서는 dependency를 추가해줘야 한다. Modelmapper은 공용으로 사용할 수 있는 객체이므로 AppConfig에 빈으로 등록해 놓고 autowired로 편하게 주입받아서 사용하도록 하자.
```xml
<dependency>
    <groupId>org.modelmapper</groupId>
    <artifactId>modelmapper</artifactId>
    <version>2.4.0</version>
</dependency>
```
```java
// appconfig
@Bean
public ModelMapper modelMapper() {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper.getConfiguration()
            // 지금전부 캐멀케이스니까 destination과 source 모두 underscore로 구분하도록 진행
            // 필드명이 비슷할 경우 modelmapper는 어떻게 잘라서 구분해야할지 모르므로 그에 대한 세팅해주는 것
            // studyUpdatedByEmail, studyCreatedByEmail 이런걸 이해서 구분시켜주는 설정임
            .setDestinationNameTokenizer(NameTokenizers.UNDERSCORE)
            .setSourceNameTokenizer(NameTokenizers.UNDERSCORE);
    return modelMapper;
}

// 사용법 1
// (변경할객체,변경되고자하는클래스)
// eventDto를 Event 클래스로 변환시키고 변환된 event를 반환한다.
// 해당 클래스에게 없는 필드는 무시된다.
Event event = modelMapper.map(eventDto, Event.class);

// 사용법 2
//(덮어씌울객체,덮어씌움을당하는객체)
// 클래스가 달라도 필드별로 매핑
// 반환하지 않고 덮어씌울대상에게 그대로 덮어씌워지고 끝
modelMapper.map(eventDto,existingEvent);
```
<br>

### objectMapper
JSON으로 변환하기 위해 사용
```java
// 해당 객체를 필드별로 JSON형식으로 변환하고 결과를 반환
objectMapper.writeValueAsString(event)
```
<br>

### JsonSerializer
스프링이 제공하는 Errors는 자바 빈 스펙을 준수하고 있지 않기 때문에 기본적으로 사용하는 Beanserializer을 사용할 수 없다. 따라서 그냥 Errors을 응답으로 내리면 JSON 변환이 불가능하다. 결론은 JsonSerializer(xml것)을 상속받아 Errors를 JSON으로 넘길 수 있는 serializer을 직접 만들어서 등록시켜야만 Errors를 넘겼을 때 JSON으로 변환하여 넘길 수 있게 된다.  
Error의 종류에는 FieldError와 GlobalError(objectError) 두 가지가 있다. errors.rejectValue("필드","에러코드","메시지")로 필드별로 에러가 들어가는 것이 FieldError이고 여러 개의 값이 조합되어 에러가 발생하여 errors.reject("에러코드","메시지")로 주는 경우가 GlobalError이다. 따라서 두 가지 모두 세팅을 해줘야 한다.  
Error은 다음을 가지고 있다.  
+ objectName : error에 담긴 객체 이름
+ defaultMessage : 메시지
+ code : 에러코드
+ field : 필드명
+ rejectedValue : 필드의 값

objectMapper에 내가 만든 Serializer을 등록하기 위해서는 @JsonComponent 애노테이션만 붙여주면 된다.
```java
// 해당 serializer을 objectMapper에 등록 하는 애노테이션
@JsonComponent 
// JsonSerializer를 상속받아 제네릭에 Errors를 넣고 재정의하면 된다.
public class ErrorsSerializer extends JsonSerializer<Errors> {
    @Override
    public void serialize(Errors errors, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        // jsongenerator로 json 만든다고 보면 된다. 
        gen.writeFieldName("errors"); // 키값 errors
        gen.writeStartArray(); // value값으로 배열 만들기
        
        // FieldError 처리
        // 에러에서 fieldErrors를 가져와서 foreach로 각각 돌리면서 Json으로 변환
        errors.getFieldErrors().forEach( e->{
            try {
                // 현재 배열 안에 있고 {} object형태로 시작FieldError 정보 JSON으로 변환
                gen.writeStartObject(); 
                // FieldError 정보 JSON으로 변환
                gen.writeStringField("field",e.getField());
                gen.writeStringField("objectName",e.getObjectName());
                gen.writeStringField("code",e.getCode());
                gen.writeStringField("defaultMessage",e.getDefaultMessage());

                Object rejectedValue = e.getRejectedValue(); // 있을 수도 없을 수도 있음
                if (rejectedValue!=null){
                    gen.writeStringField("rejectedValue",rejectedValue.toString());
                }
                gen.writeEndObject(); // {} 종료
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        // GlobalError json으로 만들기 -> gobal이니까 "field"는 없음
        // 따라서 위에 작성한 것에서 field를 제외
        errors.getGlobalErrors().forEach(e->{
            try {
                gen.writeStartObject();                 
                gen.writeStringField("objectName",e.getObjectName());
                gen.writeStringField("code",e.getCode());
                gen.writeStringField("defaultMessage",e.getDefaultMessage());
                gen.writeEndObject();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        gen.writeEndArray();
    }
}
```


<Br>

### ResponseEntity
요청에 대한 응답을 내리기 위해서 사용하는 클래스로 응답 코드, 헤더, 본문 모두를 다루기 편한 API
<br>

### linkTo, methodOn
+ Location URI를 편하게 만들기 위해 HATEOS가 제공
+ linkTo : 컨트롤러 클래스를 가리키는 webmvclinkbuilder 객체를 반환
+ methodOn : 타켓 메서드(현재 메서드)의 가짜 메서드 콜이 있는 컨트롤러 프록시 클래스 생성

사실 위에 설명만으로는 무슨 말인지 모르겠고 실제로 코딩하면서 이해한 바로는 다음과 같다.
```java
@RequestMapping(value = "/api/events",produces = MediaTypes.HAL_JSON_VALUE)
public class EventController {
    @PostMapping()    
    public ResponseEntity createEvent(){      
        URI createdUri = linkTo(EventController.class).toUri();
}

@RequestMapping(produces = MediaTypes.HAL_JSON_VALUE)
public class IndexController {
    @PostMapping("/api/events")    
    public ResponseEntity createEvent(){      
        // id는 나중에 클라이언트가 id값 받아서 교체해서 사용
        URI createdUri = linkTo(methodOn(EventController.class).createEvent()).slash("{id}").toUri();
}
```
+ linkTo는 EventController 클래스에 붙은 /api/events"를 uri 링크로 만든다.
+ linkTo는 EventController 클래스의 createEvent메서드에 붙은 "/api/events"에 /id 까지 추가하여 URI로 만든다. 만약 메서드에 파라미터가 있다면 null로 채워줘야 한다.  

<br>

### EntityModel< > - 리소스 만들기
앞서 링크를 만들었다면 ResponseEntity로 응답을 내릴 때 링크를 추가해야 할 것이다. EventModel는 파라미터를 받아 그 내용을 JSON으로 언랩(Event 객체가 들어오면 Event 껍데기는 버리고 안에 key와 value만으로 JSON형태로 만듦)해주고 링크를 추가할 수 있도록 해준다.
```java
public class EventResource extends EntityModel<Event> {
    public EventResource(Event event, Link... links) {
        super(event, links);
        //매번 self 링크를 만들어줘야 하니 생성자에 넣었다.
        add(linkTo(EventController.class).slash(event.getId()).withSelfRel());
    }
}

// ErrorResource의 경우에는 홈으로 이동하도록 생성자에 index로 가는 링크를 추가해준다.
```
Event를 만들 때의 예시로 Event를 만들면 항상 자신의 URL을 추가해줘야 하니 리소스를 추가할 수 있는 객체로 변환하면서 생성자에 self 링크를 추가하도록 넣어줬다. 이제 컨트롤러에서 응답을 내릴 때는 단순 객체를 넘기는게 아니라 EntityModel를 상속받아 리소스를 추가한 객체를 넘기면 된다.
<br>

### @AuthenticationPrincipal
컨트롤러에서 @AuthenticationPrincipal를 사용하면 SecurityContextHolder에 있는 Principal을 파라미터로 받을 수 있다. 
```java
// loadUserByUsername에서 User타입으로 반환받았다고 가정
// userDetails 타입을 쉽게 만들도록 스프링에서 User 클래스를 제공함
@PostMapping()
public ResponseEntity createEvent(@AuthenticationPrincipal User user){
```
하지만 principle이 아니라 실제 Acount 엔티티가 필요한 상황이 있을 수 있다. 예를 들면 이벤트를 만드는데 현재 사용자가 누군지 알아야 이벤트의 매니저를 넣을 수 있는 상황이 있다. 이때는 UserDetailsService를 구현한 service의 loadUserByUsername 메서드를 재정의할 때 User 그대로 반환하는게 아니라 User을 상속받은 하나의 클래스를 만들고 그 안에 account 필드를 두고 getter을 열어두면 @AuthenticationPrincipal 애노테이션에 Spring expresstion Language를 사용해 원하는 필드값을 꺼낼 수 있다.
```java
@Getter
public class AccountAdapter extends User {
    // 컨트롤러에서 인증정보 account 엔티티로 받게 하기
    private Account account;

    public AccountAdapter(Account account) {
        super(account.getEmail(), account.getPassword(), authorities(account.getRoles()));
        this.account=account;
    }

    private static Collection<? extends GrantedAuthority> authorities(Set<AccountRole> roles) {
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_"+r.name())) // 한줄이라 람다식 리턴 생략 가능
                .collect(Collectors.toSet());
    }
}
```
위와 같이 User을 상속받은 클래스를 만들고 loadUserByUsername에서 리턴값으로 이 클래스를 사용하면 된다.
```java
@GetMapping
public ResponseEntity queryEvents(@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null:account") Account account)
```
위와 같이 사용하면 이제 Principle인 AccountAdapter에서 account 필드를 getter로 꺼내 주입시켜 준다. expression의 설명은 만약 익명 사용자라면 principal에 단순 문자열로 anonymousUser가 들어오므로 principle이 anonymousUser라면 account에 null을 넣어주라는 의미이다.
<br>

이렇게 매번 길게 애노테이션을 작성하기는 번거로우니 직접 애노테이션을 하나 만들어서 사용하는게 좋다.
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@AuthenticationPrincipal(expression = "#this=='anonymousUser' ? null : account")
public @interface CurrentUser {
}
```
컨트롤러의 파라미터로 엔티티를 받으면 좋지 않다는 것을 알고 있다. 그런데 이건 요청으로 받는게 아니라 SecurityContext에서 꺼내서 주입해주는 것이니 괜찮(?)은 것 같다. 이 부분에 대해서는 조금 더 알아볼 필요가 있을 것 같다.
<br>

### 테스트 중복 코드 제거
```java
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs // spring restdocs 애노테이션으로 의존성 추가 필요
@Import(RestDocsConfiguration.class) // docs 포맷팅 하기 위해서 만든 config import
@ActiveProfiles("test") // resource를 application-test 를 사용하게 된다 
@Disabled // 테스트를 가지고 있는 클래스로 간주되지 않도록 무시하는 애노테이션
public class BaseControllerTest {

    // protected로 외부 사용 막기
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ModelMapper modelMapper;
}
```
테스트를 작성할 때 중복되는 애노테이션과 필드들이 있을 것이다. 그것들을 test 디렉토리에 common 패키지에 하나 클래스를 만들어 두고 이를 상속받아서 test를 작성하면 중복 코드를 줄일 수 있다. Test와 main의 properties를 따로 가져가는 경우가 많은데 main의 properties를 그대로 가져오면서 수정하고 싶은게 있다면 application-test.properties라고 test 리소스에 따로 만들어주고 수정하고 싶은 부분만 적어주면 실행 시 우선 main의 properties를 가져오고 거기 위에다가 test를 덮어씌운 것으로 동작하게 된다. 테스트 작성시 @ActiveProfiles 애노테이션을 붙여주면 된다.  
<br>

### 문자열 외부 설정으로 빼기
테스트를 작성하다 보면 같은 사용자를 계속 작성해야할 때가 있다. 이럴 때마다 하드코딩으로 직접 작성하는 것보다 한 곳에 빼놓고 getter로 사용하는게 편하다. main에 common 패키지에 AppProperties클래스르 만든다.
```java
@Component
@ConfigurationProperties(prefix = "my-app")
@Getter @Setter
public class AppProperties {

    @NotEmpty
    private String adminUsername;

    @NotEmpty
    private String adminPassword;

    @NotEmpty
    private String userUsername;

    @NotEmpty
    private String userPassword;

    @NotEmpty
    private String clientId;

    @NotEmpty
    private String clientSecret;

}
```
프로젝트를 리빌드 해준 뒤 properties에 다음과 같이 작성해두면
```
my-app.admin-username=admin@email.com
my-app.admin-password=admin
my-app.user-username=user@email.com
my-app.user-password=user
my-app.client-id=myApp
my-app.client-secret=pass
```
스프링이 뜰 때마다 자동으로 값이 주입된다. 따라서 이제 사용할 때는 AppProperties.getXXX로 사용하면 된다.
<br>

### Spring restdocs format 설정
```java
//restdocs를 좀더 깔끔하게 해주기 위해 만든 config
// test에서만 사용하는 config라고 알려주는 애노테이션
@TestConfiguration
public class RestDocsConfiguration {
    // docs의 json을 포맷팅해서 이쁘게 정렬하도록 하는 것
    @Bean
    public RestDocsMockMvcConfigurationCustomizer restDocsMockMvcConfigurationCustomizer(){
        return configurer -> configurer.operationPreprocessors()
                .withRequestDefaults(prettyPrint())
                .withResponseDefaults(prettyPrint());
    }
}

@Import(RestDocsConfiguration.class) 
class EventControllerTest{}
```
spring restdoc를 사용해서 돌려보면 json이 한줄로 쭉 길게 나온다. 보기 불편하므로 이쁘게 정렬해주도록 만들어주는 설정이다. main에서 RestDocsConfiguration 클래스 만들어주고 위처럼 작성하면 된다. 마지막으로 document를 만드는 test클래스에 @Import(만들어놓은클래스) 애노테이션을 붙여주면 된다.

<br>

### REST Docs 문서화 의존성 추가와 조각 문서 html로 만들기
```xml
<dependency>
    <groupId>org.springframework.restdocs</groupId>
    <artifactId>spring-restdocs-mockmvc</artifactId>
    <scope>test</scope>
</dependency>

<plugin>
    <groupId>org.asciidoctor</groupId>
    <artifactId>asciidoctor-maven-plugin</artifactId>
    <version>1.5.8</version>
    <executions>
        <execution>
            <id>generate-docs</id>
            <phase>prepare-package</phase>
            <goals>
                <goal>process-asciidoc</goal>
            </goals>
            <configuration>
                <backend>html</backend>
                <doctype>book</doctype>
            </configuration>
        </execution>
    </executions>
    <dependencies>
        <dependency>
            <groupId>org.springframework.restdocs</groupId>
            <artifactId>spring-restdocs-asciidoctor</artifactId>
            <version>${spring-restdocs.version}</version>
        </dependency>
    </dependencies>
</plugin>
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <excludes>
            <exclude>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
            </exclude>
        </excludes>
    </configuration>
</plugin>
<plugin>
    <groupId>org.asciidoctor</groupId>
    <artifactId>asciidoctor-maven-plugin</artifactId>
    <!-- … -->
</plugin>
<plugin>
    <artifactId>maven-resources-plugin</artifactId>
    <version>3.1.0</version>
    <executions>
        <execution>
            <id>copy-resources</id>
            <phase>prepare-package</phase>
            <goals>
                <goal>copy-resources</goal>
            </goals>
            <configuration>
                <outputDirectory>
                    ${project.build.outputDirectory}/static/docs
                </outputDirectory>
                <resources>
                    <resource>
                        <directory>
                            ${project.build.directory}/generated-docs
                        </directory>
                    </resource>
                </resources>
            </configuration>
        </execution>
    </executions>
</plugin>
```
우선 의존성을 추가하고 main바로 아래 새로운 디렉토리로 asciidoc 폴더를 만들고 index.adoc(어떤 이름이든 무관)만들고 [[링크](https://gitlab.com/whiteship/natural/-/blob/master/src/main/asciidoc/index.adoc)] 를 openraw로 열어서 내용을 복붙한다. 이 문서는 spring docs의 예제 프로젝트를 백기선님이 한글로 수정하신거라고 한다. 각각의 operation::참조폴더[snippets='조각난문서,...,...'] 로 조각난 문서들을 추가해주고 Maven - lifecycle - package 또는 터미널에서 mvn package 를 입력하면 조각난 문서들이 문서화된다. target - classes - static - docs - index.html 로 생성되어 있을 것이다. 이제 응답에 profile로 이 문서를 링크로 걸어주면 된다.
<Br>

### 논리적 validator
@valid 애노테이션으로 1차적으로 거르고 논리적으로 맞는지도 검증해야 한다. 따라서 하나의 클래스를 만들어 검증해야 한다.
```java
// 정상적으로 값이 들어오지만 논리적으로 오류인 경우를 걸러줄 것
// 빈으로 등록하고 사용
@Component
public class EventValidator {
    public void validate(EventDto eventDto, Errors errors){
        // 값의 논리적 오류
        if (eventDto.getBasePrice() > eventDto.getMaxPrice() && eventDto.getMaxPrice()!=0){
            // 에러에 에러 코드 넣기
            // 문제의필드, 에러코드, 메시지
            errors.rejectValue("basePrice","wrongValue","BasePrice is wrong.");
            errors.rejectValue("MaxPrice","wrongValue","MaxPrice is wrong.");
        }

        // 이하 생략
}
```

<br>


## 4. 이벤트 생성
---
```java
// Event 엔티티
// 빌더를 사용하기 위해서는 기본 생성자와 모든 필드 포함한 생성자가 있어야한다.
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Setter @Getter @EqualsAndHashCode(of="id")
@Entity
public class Event {

    @Id @GeneratedValue
    private Integer id;
    private String name;
    private String description;
    private LocalDateTime beginEnrollmentDateTime;
    private LocalDateTime closeEnrollmentDateTime;
    private LocalDateTime beginEventDateTime;
    private LocalDateTime endEventDateTime;
    private String location; // (optional) 이게 없으면 온라인 모임
    private int basePrice; // (optional)
    private int maxPrice; // (optional)
    private int limitOfEnrollment;
    private boolean offline;
    private boolean free;

    @ManyToOne // 단방향
    // json내릴때 만든 serializer 사용하도록 설정, 반드시 xml것 사용!!!!
    // 그냥 내리면 manager 의 개인정보까지 내려짐
    @JsonSerialize(using = AccountSerializer.class) 
    private Account manager;

    @Enumerated(EnumType.STRING)
    private EventStatus eventStatus = EventStatus.DRAFT;

    public void update() {
        // Update free
        if (this.basePrice == 0 && this.maxPrice == 0) {
            this.free = true;
        } else {
            this.free = false;
        }
        // Update offline
        // isblank는 자바 11에서 지원하는 것 -> 빈 문자열, 공백까지 다 확인해서 비어있는지 확인
        if (this.location == null || this.location.isBlank()) {
            this.offline = false;
        } else {
            this.offline = true;
        }
    }
}

// 컨트롤러에서 받을 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDto {

    // string 값은 반드시 들어와야함 -> notempty
    @NotEmpty
    private String name;
    @NotEmpty
    private String description;

    @NotNull // 시간 값들은 null이면 안된다
    private LocalDateTime beginEnrollmentDateTime;
    @NotNull
    private LocalDateTime closeEnrollmentDateTime;
    @NotNull
    private LocalDateTime beginEventDateTime;
    @NotNull
    private LocalDateTime endEventDateTime;

    private String location;
    @Min(0)
    private int basePrice;
    @Min(0)
    private int maxPrice;
    @Min(0)
    private int limitOfEnrollment;
}

// 컨트롤러
@Controller
// 이 클래스 안에 있는 모든 핸들러들은 HAL Json 타입으로 응답을 보낸다.
@RequestMapping(value = "/api/events",produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;

    @PostMapping()
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto ,
                                      Errors errors, // error 스프링 것 사용
                                      @CurrentUser Account currentUser){     

        // 1차적으로 valid에 걸리면 
        if(errors.hasErrors()){
            return badRequest(errors); // 자주 사용하므로 메서드로 뽑음
        }

        // 2차적으로 논리적 검증
        eventValidator.validate(eventDto,errors);
        if (errors.hasErrors()){
            return badRequest(errors);
        }
        
        Event event = modelMapper.map(eventDto, Event.class); // dto를 Event 엔티티로 변환
        event.update(); // free와 offline 필드 논리적으로 맞도록 수정
        event.setManager(currentUser); // 이벤트 매니저 설정
        Event newEvent = eventRepository.save(event); // 간단해서 서비스 계층 안 만들고 바로 save
        
        // 만들 이벤트의 URI -> 현재 위치를 나타내는 Location의 URI이 될 것임
        URI createdUri = linkTo(EventController.class).slash(newEvent.getId()).toUri();

        // 리소스 추가할 수 있도록 만들기
        EventResource eventResource = new EventResource(event);
        // 이벤트 조회링크, 업데이트링크, profile 링크 추가
        eventResource.add(linkTo(EventController.class).withRel("query-events")); 
        eventResource.add(linkTo(EventController.class).slash(newEvent.getId()).withRel("update-event"));
        eventResource.add(new Link("/docs/index.html#resources-events-create").withRel("profile"));
        
        // create 201응답에 파라미터로 Lcation 링크 -> 헤더로 들어감
        // 바디에는 리소스와 객체 담은 클래스 eventResource
        return ResponseEntity.created(createdUri).body(eventResource);
    }

    private ResponseEntity<ErrorsResource> badRequest(Errors errors) {
        // 에러 받아서 badrequest 보내면서 바디에 error 정보 담기
        return ResponseEntity.badRequest().body(new ErrorsResource(errors));
    }
}

// 리소스 추가할 수 있는 객체 만들기
public class ErrorsResource extends EntityModel<Errors> {
    // 에러에는 홈으로 가도록 index 페이지 리소스 넣음
    public ErrorsResource(Errors content, Link... links) {
        super(content, links);
        add(linkTo(methodOn(IndexController.class).index()).withRel("index"));
    }
}
```
__컨트롤러 설명__  
+ @ResponseBody 애노테이션에 의해 JSON으로 요청온 값들이 EventDto의 필드명과 매칭되어 세팅된다.
    - 매칭되지 않는 필드들은 무시된다. properties에 spring.jackson.deserialization.fail-on-unknown-properties=true 를 작성하면 매칭되지 않는 값들이 들어오면 오류를 발생시키는데 사실 이건 사용하지 않고 유연하게 사용하는게 좋은 것 같다.
+ @valid 애노테이션을 통해 DTO 각 필드마다 걸어두었던 애노테이션 제약을이 검증된다. -> 만약 애노테이션의 검증에 위배된다면 스프링에서 제공하는 Errors에 담긴다.
+ @CurrentUser은 만든 애노테이션으로 principal에서 account 엔티티를 꺼낸건데 '시작하기전에' 설명했다. 
+ 오류에 대한 응답은 자주 사용하므로 badRequest 메서드로 따로 뽑았다.
    - 앞서 errors를 JSON으로 만들 수 있도록 Serializer을 등록해 뒀다.
    - ResponseEntity의 badRequest를 사용해서 badRequest 응답을 내릴 것인데 HATEOAS를 만족하기 위해 이 경우 index 페이지로 이동하라고 링크를 걸어줘야 한다. 따라서 '시작하기전에' 에서 설명한 리소스를 추가하기 위한 객체를 만들고 그 객체를 body에 넣어서 url이 추가된 형태로 반환해준다.
+ @valid로 값을 걸러도 논리적으로 값이 맞지 않을 경우도 있다. 따라서 한 번 더 걸러줘야 한다. 예를 들면 이벤트가 끝나는 시점이 시작 시점보다 전에 있는 경우가 있다. 이런 논리적인 값의 오류들은 따로 validator 클래스를 만들어서 검증해야 하고 오류가 있다면 마찬가지로 badRequest를 보내야 한다.

사실 여기서 문제가 있다. Event에 manager을 세팅해주고 JSON으로 내려줄 때 Manager의 모든 정보가 같이 내려간다. 일단 저장을 하고 리소스로 만들 때 DTO를 새로 만들어서 덮어씌우고 그걸 리소스로 넘기는 방법이 있고 JSON을 내릴 때 manager을 JSON으로 내릴 때의 설정을 주는 방법이 있다. 첫 번째 방법은 쉬우니 생략하고 두 번째 방법을 살펴보자.  
방법은 간단하다 ErrorSerializer 했던 것과 똑같이 JsonSerialzer을 상속받아 만들어주면 된다.
```java
public class AccountSerializer extends JsonSerializer<Account> {
    @Override
    public void serialize(Account account, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("id",account.getId());
        gen.writeEndObject();
    }
}
```
ErrorSerializer는 @JsonComponent를 붙여서 objectMapper에 등록했지만 이걸 등록해버리면 모든 Account가 위의 JSON으로 내려가는 불상사가 생긴다. 따라서 Event를 JSON으로 내릴 때 manager만 이처럼 동작하게 하기 위해서 다음과 같은 애노테이션을 붙인다.
```java
// json내릴때 만든 serializer 사용하도록 설정, 반드시 xml것 사용!!!!
@JsonSerialize(using = AccountSerializer.class) 
private Account manager;
```


<br>

## 5. 이벤트 TEST
---
```java
@DisplayName("이벤트 생성")
@Test
void createEvent() throws Exception{ 
    EventDto event = EventDto.builder()
                // 생략
                .build()

    // 이벤트 생성 요청
    mockMvc.perform(post("/api/events/")
            // getBearerToken는 계정 만들고 계정에 대한 Oauth토큰을 반환하는 메서드로 만듦            
            .header(HttpHeaders.AUTHORIZATION, getBearerToken(true)) // 헤더에 사용자 Oauth 토큰
            .contentType(MediaType.APPLICATION_JSON) //  요청에 JSON을 담아서 보내고 있다.
            .accept(MediaTypes.HAL_JSON) // HAL_JSON 응답을 원한다.
            .content(objectMapper.writeValueAsString(event))) // event를 JSON문자열로 변환하여 본문에 삽입
            .andDo(print()) // 콘솔에 응답 찍기
            .andExpect(header().exists(HttpHeaders.LOCATION)) // 헤더에 location 존재하는지
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE,MediaTypes.HAL_JSON_VALUE)) // 헤더에 CONTENT_TYPE이 HAL_JSON_VALUE 인지
            .andExpect(jsonPath("id").value(Matchers.not(100))) // 응답 json의 id값이 100이 아니다.
            .andExpect(jsonPath("free").value(false)) // 응답 json의 free값이 true이 아니다.
            .andExpect(jsonPath("offline").value(true))
            .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name())) // enum.name()을 해야 enum타입의 문자열이 나온다.
            .andExpect(jsonPath("_links.self").exists()) // 사실 link는 docs에서 검증해주기 때문에 굳이 안해도 아래서 한다
            .andExpect(jsonPath("_links.query-events").exists())
            .andExpect(jsonPath("_links.update-event").exists())
            .andExpect(status().isCreated()) // 201
            // spring restdocs 의존성 추가로 profile 링크로 남겨줄 사용 설명서를 만들 수 있다
            .andDo(document("create-event", // create-event 라는 폴더에 아래 내용들을 docs 만들기
            // 딱 위에까지만 작성하면 test에서 사용했던 요청,응답이 각각 조각으로 문서화된다.
            // 하지만 더 나아가 링크, 헤더, 필드가 무엇을 뜻하는지 문서화가 필요하다면
            // 아래와 같이 작성하면 된다.
                    links(
                            linkWithRel("self").description("link to self"),
                            linkWithRel("query-events").description("link to query"),
                            linkWithRel("update-event").description("link to update"),
                            linkWithRel("profile").description("link to profile")
                    ),
                    requestHeaders(
                            headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                            headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                    ),
                    requestFields(
                            fieldWithPath("name").description("name of new event"),
                            fieldWithPath("description").description("description of new event"),
                            fieldWithPath("beginEnrollmentDateTime").description("date time of begin of begin event"),
                            fieldWithPath("closeEnrollmentDateTime").description("date time of close of close event"),
                            fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
                            fieldWithPath("endEventDateTime").description("date time of end of new event"),
                            fieldWithPath("location").description("location of new event"),
                            fieldWithPath("basePrice").description("base price of new event"),
                            fieldWithPath("maxPrice").description("max price of new event"),
                            fieldWithPath("limitOfEnrollment").description("limit of enrollment")
                    ),
                    responseHeaders(
                            headerWithName(HttpHeaders.LOCATION).description("location header"),
                            headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                    ),
                    relaxedResponseFields(
                            fieldWithPath("id").description("identifier of new event"),
                            fieldWithPath("name").description("name of new event"),
                            fieldWithPath("description").description("description of new event"),
                            fieldWithPath("beginEnrollmentDateTime").description("date time of begin of begin event"),
                            fieldWithPath("closeEnrollmentDateTime").description("date time of close of close event"),
                            fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
                            fieldWithPath("endEventDateTime").description("date time of end of new event"),
                            fieldWithPath("location").description("location of new event"),
                            fieldWithPath("basePrice").description("base price of new event"),
                            fieldWithPath("maxPrice").description("max price of new event"),
                            fieldWithPath("limitOfEnrollment").description("limit of enrollment"),
                            fieldWithPath("free").description("it tells if this event is free"),
                            fieldWithPath("offline").description("it tells if this event is offline"),
                            fieldWithPath("eventStatus").description("event status"),
                            fieldWithPath("_links.self.href").description("link of self"),
                            fieldWithPath("_links.query-events.href").description("link to query event list"),
                            fieldWithPath("_links.update-event.href").description("link to update existing event"),
                            fieldWithPath("_links.profile.href").description("link to profile")
                    )));

    // relaxed를 prefix로 붙이지 않으면 하나라도 빠지게 되면 오류가 난다.
    // 모든것을 문서화하고 싶지 않다면 prefix로 relaxed를 붙이면된다.
    // 이러한 docs들은 성공에 대한 테스트만 작성해주면 된다.
}

// 여기부터는 CreatEvent에 필요한 메서드 뽑아놓은 것 //
// 계정 만들고 토큰 반환하는 메서드들이다. //

private String getBearerToken(boolean needToCreateAccount) throws Exception {
    // Bearer 는 Oauth 토큰의 타입
    return "Bearer " + getAccessToken(needToCreateAccount);
}

private String getAccessToken(boolean needToCreateAccount) throws Exception {
    // Given
    if (needToCreateAccount) {
        createAccount(); // 계정 생성
    }

    // Oauth 토큰 발급 받기
    ResultActions perform = mockMvc.perform(post("/oauth/token")
            // 인증 토큰을 발급받으려면 http basic 인증 헤더에 클라이언트 아이디와 클라이언트 시크릿을 줘야한다.
            .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
            .param("username", appProperties.getUserUsername())
            .param("password", appProperties.getUserPassword())
            .param("grant_type", "password"));

    // perform 수행의 리턴의 응답을 문자열로 가져와서
    var responseBody = perform.andReturn().getResponse().getContentAsString();
    Jackson2JsonParser parser = new Jackson2JsonParser();
    // jsonparser로 map으로 json을 파싱한다음 토큰 꺼내서 스트링 변환해서 토큰 반환
    return parser.parseMap(responseBody).get("access_token").toString();

}

private Account createAccount() {
    Account backtony = Account.builder()
            .email(appProperties.getUserUsername())
            .password(appProperties.getUserPassword())
            .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
            .build();
    return accountService.saveAccount(backtony);
}
```
<br>

## 6. 이벤트 전체 조회
---
/api가 index 페이지라면 /api/events로 들어왔다면 여러 event들이 보일 것이다. 이 페이지를 처리하는 이벤트 조회를 만들어 보자.  
응답으로 내려줘야할 것
+ 현재 페이지 링크
+ 이전 페이지 링크
+ 다음 페이지 링크
+ 마지막 페이지 링크
+ 각 Event의 정보와 링크
+ profile 링크

pageable에 대한 정보로 repository에서 정보를 가져오면 위의 링크 정보들을 추가해줘야 한다. 그때 유용한 것이 spring data jpa에서 제공하는 PagedResourcesAssembler이다. PagedResourcesAssembler는 repository에서 pageable로 꺼내온 page 데이터를 리소스로 변환해준다. assembler.toMode(리소스로변경할page) 를 하게 되면 각각의 pageable로 꺼내온 각각의 event 정보, 페이지 정보와  first,prev,self,next,last 페이지에 대한 링크 정보를 링크로 추가한 객체를 만들어 준다. 하지만 여기서 각각의 event는 리소스 객체가 아니라 자신에 대한 self 링크를 가지고 있지 않다. 이에 대한 해답은 아래 코드에서 설명한다.

```java
@Controller
@RequestMapping(value = "/api/events",produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
public class EventController {
    // 생략

    @GetMapping
    public ResponseEntity queryEvents(Pageable pageable,
                                      PagedResourcesAssembler<Event> assembler,
                                      @CurrentUser Account account){

        Page<Event> page = eventRepository.findAll(pageable); // 페이징 정보대로 찾아오기

        //e -> new EventResource(e) : page에 들어있는 각각의 event를
        // 전에 만들었던 EventResource를 이용해 리소스로 변환
        var pageResources = assembler.toModel(page, e -> new EventResource(e));
        // profile 링크 추가
        pageResources.add(new Link("/docs/index.html#resources-events-list").withRel("profile"));
        if (account != null){
            // 사용자 정보가 있으면 이벤트 생성 url추가
            pageResources.add(linkTo(EventController.class).withRel("create-event"));
        }

        return ResponseEntity.ok(pageResources);
    }
}
```

<br>

## 7. 이벤트 전체 테스트
---
```java
@DisplayName("30개의 이벤트를 10개씩 두번째 페이지 조회")
@Test
void queryEventsWithAuthentication() throws Exception {
    //given
    IntStream.range(0, 30).forEach(i -> {
        generateEvent(i); // 이벤트 생성 메서드 따로 뽑음
    });

    //when & then
    mockMvc.perform(get("/api/events")
            // 인증된 사용자의 경우
            .header(HttpHeaders.AUTHORIZATION, getBearerToken(true))
            .param("page", "1")
            .param("size", "10")
            .param("sort", "name,DESC"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("page").exists())
            .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
            .andExpect(jsonPath("_links.self").exists()) // 몇 가지 링크 생략했음
            .andExpect(jsonPath("_links.profile").exists())
            .andExpect(jsonPath("_links.create-event").exists())
            .andDo(document("query-events")) // doc 이하 생략
    ;
}
```
<br>

## 8. 이벤트 단건 조회
---
```java
@Controller
@RequestMapping(value = "/api/events",produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
public class EventController {
    // 생략

    @GetMapping("/{id}")
    public ResponseEntity getEvent(@PathVariable Integer id,
                                   @CurrentUser Account currentUser){
        // 없을 수도 있으므로 리포지토리에서 만들 때 optional로 감싸서 만듦
        Optional<Event> optionalEvent = eventRepository.findById(id);
        if (optionalEvent.isEmpty()){
            // 404 응답 // 이것도 index 링크를 넣어줘야 되지 않나 싶다.
            return ResponseEntity.notFound().build(); 
        }
        // 있으면 꺼내서 리소스로 만들고 profile 추가
        Event event = optionalEvent.get();
        EventResource eventResource = new EventResource(event);
        eventResource.add(new Link("/docs/index.html#resources-events-get").withRel("profile"));

        // 만약 해당 이벤트의 매니저라면 수정 uri 추가
        // self랑 일치할 것인데 메서드가 put으로 올것임
        if (event.getManager().equals(currentUser)){
            eventResource.add(linkTo(EventController.class).slash(event.getId()).withRel("update-event"));
        }
        return ResponseEntity.ok(eventResource);
    }
}
```
<br>

## 9. 이벤트 단건 조회 테스트
---
```java
@DisplayName("기존의 이벤트를 하나 인증 사용자 조회하기")
@Test
void getEventWithAuth() throws Exception {
    //given
    Account account = createAccount();
    Event event = generateEvent(100,account);

    //when
    // 쉼표찍고 뒤에 값주면 pathvaraible 값 줄 수 있음
    mockMvc.perform(get("/api/events/{id}", event.getId())
            .header(HttpHeaders.AUTHORIZATION, getBearerToken(false)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("name").exists())
            .andExpect(jsonPath("id").exists())
            .andExpect(jsonPath("_links.self").exists())
            .andExpect(jsonPath("_links.profile").exists())
            .andExpect(jsonPath("_links.update-event").exists())
            .andDo(print())
            .andDo(document("get-an-event"))
    ;
}
```
<br>

## 10. 이벤트 수정
---
```java
 @Controller
@RequestMapping(value = "/api/events",produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
public class EventController {
    // 생략

    @PutMapping("/{id}")
    public ResponseEntity updateEvent(@PathVariable Integer id,
                                      @RequestBody @Valid EventDto eventDto,
                                      Errors errors,
                                      @CurrentUser Account currentUser){
        // 해당 이벤트 찾아오기
        Optional<Event> optionalEvent = eventRepository.findById(id);
        // 이벤트가 없으면
        if (optionalEvent.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        // 바인딩 validate 애노테이션으로 거르기
        if(errors.hasErrors()){
            return badRequest(errors);
        }

        // 2차적 로직으로 거르기
        eventValidator.validate(eventDto,errors);
        if (errors.hasErrors()){
            return badRequest(errors);
        }
        // 해당 이벤트 꺼내기
        Event existingEvent = optionalEvent.get();

        // 수정인데 매니저가 아닌경우
        if(!existingEvent.getManager().equals(currentUser)){
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);// 인가 오류
        }

        // eventDto를 existingEvent에 덮어씌우기
        modelMapper.map(eventDto,existingEvent);
        Event savedEvent = eventRepository.save(existingEvent);// 서비스를 안만들어서 트랜잭션 유지가 안되서 여기서 처리

        // 리소스로 만들기
        EventResource eventResource = new EventResource(savedEvent);
        eventResource.add(new Link("/docs/index.html#resources-events-update").withRel("profile"));

        return ResponseEntity.ok(eventResource);
    }
}
```
<br>

## 11. 이벤트 테스트
---
```java
@DisplayName("이벤트를 정상적으로 수정하기")
@Test
void updateEvent() throws Exception {
    //given
    Account account = createAccount();
    Event event = generateEvent(200,account);
    String eventName = "Updated Event";
    EventDto eventDto = modelMapper.map(event, EventDto.class);
    eventDto.setName(eventName);

    mockMvc.perform(put("/api/events/{id}", event.getId())
            .header(HttpHeaders.AUTHORIZATION, getBearerToken(false))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(eventDto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("name").value(eventName))
            .andExpect(jsonPath("_links.self").exists())
            .andDo(document("update-event")) // doc 생략
    ;

}
```
<br>

## 12. 보안 설정
---
이전에는 form Login을 공부했었는데 이 강의에서는 Oauth2로 인증을 하고 있다. 시큐리티 강의가 아니라 정말 간단하게만 설명하시기 때문에 따로 공부가 필요할 것 같다. 일단 설정만 이해한대로 적겠다.
```java
@Configuration
@EnableWebSecurity
// spring security 기본 설정
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    AccountService accountService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Bean // oauth 토큰을 저장하는 저장소, 원래는 inmemory에 하면 안됨
    public TokenStore tokenStore(){
        return new InMemoryTokenStore();
    }

    // authenticationmanager를 빈으로 노출 시켜야한다.
    // 다른 authorization 서버나 resource 서버가 참조할 수 있도록해야 하기 때문이다
    @Bean // 재정의로 불러오고 그대로 빈으로 등록하면 된다.
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    // authenticationmanager을 어떻게 만들거냐 -> 재정의
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 내가 만든 accountservice와 password인코더를 사용해서 매니저를 만들도록
        auth.userDetailsService(accountService)
                .passwordEncoder(passwordEncoder);
    }
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().mvcMatchers("/docs/**");//docs 보안 검사 무시
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations()); // 정적 리소스 보안 검사 무시
    }
}

// Oauth2 인증 서버 설정
@Configuration
@EnableAuthorizationServer // Oauth 인증 서버 설정
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    PasswordEncoder passwordEncoder;

    // 빈설정에서 다른 곳에서 사용할 수 있도록 빈으로 등록해놨음
    // AuthenticationManager는 유저인증 정보를 가지고 있음
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    AccountService accountService;

    // 이전 시큐리티 config에 빈 등록 해줬음 가져온것
    @Autowired
    TokenStore tokenStore;

    @Autowired
    AppProperties appProperties;

    // configure 3개만 override 하면 된다.

    // 시큐리티에서는 패스워드 인코더 설정
    // 클라이언트의 시크릿를 검증할때 패스워드 인코더 사용
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.passwordEncoder(passwordEncoder);
    }

    // 클라이언트 설정
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        // 이건 인메모리도 유저를 등록한거나 마찬가지인데
        // 이거 말고 jdbc를 사용해서 db에 저장하는게 좋다
        clients.inMemory()
                .withClient(appProperties.getClientId())
                // 이 인증 서버가 지원할 grant 타입 설정
                // refresh token은 oauth 토큰을 발급받을 때 refresh 토큰도 같이 발급해주는데
                // 이걸 가지고 새로운 access 토큰을 발급받는 그런 타입이다.
                .authorizedGrantTypes("password","refresh_token")
                .scopes("read","write") // 앱에서 정의하기 나름,
                .secret(passwordEncoder.encode(appProperties.getClientSecret())) // 이 앱의 시크릿
                .accessTokenValiditySeconds(10*60) // 토큰 유효 시간
                .refreshTokenValiditySeconds(6*10*60);
    }

    // endpoint는 authenticationmanager, token store, userdetails를 설정할 수 있다.
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authenticationManager) // 우리의 인증정보를 알고있는 authenticationManager로 설정
                .userDetailsService(accountService) // 우리가 만든 서비스로 설정
                .tokenStore(tokenStore);
    }
}

// 리소스 서버 설정
// 리소스 서버는 앞서 설정해두었던 Oauth서버와 연동되어 사용된다.
// 어떤 외부의 요청이 리소스에 접근할 때 인증이 필요하다면 Oauth 서버에서 제공하는 토큰을 이용해 확인
// 리소스 서버는 토큰기반으로 인증 정보가 있는지 없는지 확인하고 리소스 서버에 접근 제한
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {


    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId("event");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .anonymous()// 익명 사용자 허용
                    .and()
                .authorizeRequests()
                    .mvcMatchers(HttpMethod.GET,"/api/**")
                        .permitAll() // 전부 허용 anoymoous로 해버리면 익명만 사용 가능함
                    .anyRequest().authenticated()// 다른 요청은 인증 해야함
                    .and()
                .exceptionHandling() // 접근 권히 없는 경우 oauth2 핸들러 사용
                    .accessDeniedHandler(new OAuth2AccessDeniedHandler());
    }
}
```
<br>

## 13. Swagger 설정
---
위에서는 api 문서화를 test를 통해 작성했는데 Swagger라는 방법이 많이 간단한 것 같다.
```xml
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger2</artifactId>
    <version>2.9.2</version>
</dependency>
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger-ui</artifactId>
    <version>2.9.2</version>
</dependency>
```
사용하려면 위의 의존성을 추가해야한다. ui는 조금 더 시각화 시켜주는 것이라고 보면 된다.
<br>

이제 config를 하나 만들어서 설정하면 된다.
```java
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    /**
    * 여기서부터 static은 그냥 옵션이다. 해도되고 안해도 된다.
    */
    // 사용자 정보
    // contact는 springfox.documentation.service 것 사용
    // 이름 url email 순
    private static final Contact DEFAULT_CONTACT = new Contact("backtony",
            "http://www.backtony.github.io","cjs1863@gmail.com");

    // 타이틀, 설명, 버전, uniform resource name, contact, license, license url
    private static final ApiInfo DEFAULT_API_INFO = new ApiInfo("title",
            "description","1.0","urn:tos",
            DEFAULT_CONTACT,"Apache 2.0",
            "http://apache.org/licenses/LICENESE-2.0",new ArrayList<>()); // array는 나중에 추가정보 넣을 것


    private static final Set<String> DEFAULT_PRODUCES_AND_CONSUMES = new HashSet<>(Arrays.asList("application/json","application/xml"));

    // doc 적용
    // 위에 static으로 내가 정해준 것을 문서에 넣으려면 아래와 같이 추가하면 되고
    // 그냥 기존 설정대로 하고 싶다면 return new Docket(DocumentationType.SWAGGER_2); 만 주면 된다.
    @Bean
    public Docket api(){
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(DEFAULT_API_INFO)
                .produces(DEFAULT_PRODUCES_AND_CONSUMES)
                .consumes(DEFAULT_PRODUCES_AND_CONSUMES)
                ;
    }

    // swagger와 hateoas 문제 해결하려면 이거 넣어야함
    @Bean
    public LinkDiscoverers discoverers() {
        List<LinkDiscoverer> plugins = new ArrayList<>();
        plugins.add(new CollectionJsonLinkDiscoverer());
        return new LinkDiscoverers(SimplePluginRegistry.create(plugins));

    }
}
```
<br>

추가적으로 문서에 각 도메인의 설명과 필드의 설명을 추가하고 싶다면 아래와 같이 설정하면 된다. description이라고 생각하면 된다.
```java
@Data
@AllArgsConstructor
@ApiModel(description = "사용자 상세 정보를 위한 도메인 객체")
public class User {
    private Integer id;

    @ApiModelProperty(notes = "사용자 이름을 입력해주세요.")
    private String name;

    @ApiModelProperty(notes = "사용자 등록일을 입력해주세요.")
    private Date joinDate;

    @ApiModelProperty(notes = "사용자 패스워드 입력해주세요.")
    private String password;

    @ApiModelProperty(notes = "사용자 주민번호 입력해주세요.")
    private String ssn;
}
```
<br>

## 14. JsonIgnore
---
위에서 json 내릴때 내리면 안되는 정보를 막기 위한 방법으로 dto변환과 JsonSerializer를 사용하는 방법이 있다고 했다. 그런데 여기 더 간단한 방법이 있다. @JsonIgnore라는 애노테이션을 붙이면 json으로 내려가지 않고 name과 joinDate만 내리게 된다. 일일이 주는게 불편하다면 한번에 @JsonIgnoreProperties(value = {"필드명","필드명"})으로 줄 수 도 있다.
```java
//@JsonIgnoreProperties(value = {"password","name"})
public class User {
    private String name;
    private Date joinDate;

    @JsonIgnore 
    private String password;
    @JsonIgnore
    private String ssn;
}
```


