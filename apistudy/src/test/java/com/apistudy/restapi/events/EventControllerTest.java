package com.apistudy.restapi.events;






import com.apistudy.restapi.accounts.Account;
import com.apistudy.restapi.accounts.AccountRepository;
import com.apistudy.restapi.accounts.AccountRole;
import com.apistudy.restapi.accounts.AccountService;
import com.apistudy.restapi.common.AppProperties;
import com.apistudy.restapi.common.BaseControllerTest;
import com.apistudy.restapi.common.RestDocsConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EventControllerTest extends BaseControllerTest {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    AppProperties appProperties;

    @Autowired
    AccountRepository accountRepository;

    // 스프링 뜰때 저장된 계정 지우기
    // 직접 account 해당 계정이 테스트 과정에서 필요하기 때문에
    // 데이터에서 꺼내오기보다는 그냥 여기서 만드는게 편함
    @BeforeEach
    public void setUp() {
        this.eventRepository.deleteAll();
        this.accountRepository.deleteAll();
    }

    @DisplayName("이벤트 생성")
    @Test
    void createEvent() throws Exception{
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("REST API development")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 23, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 24, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 25, 14, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 26, 14, 21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역")
                .build();


        // event 객체를 만들었는데 이걸 JSON 타입으로 보내줘야 한다 어떻게해줘야할까?
        // 스프링 부트를 사용하면 mappingjacksonjson이 의존성으로 들어가 있으면
        // objectmapper가 자동으로 빈으로 등록된다. 따라서 autowired로 주입받아서 사용할 수 있따.

        // get 요청 이외의 작업은 권한이 있도록 막아놨다. -> 요청헤더에 인증정보 넣어줘야함
        mockMvc.perform(post("/api/events/")
                // get 요청 이외의 작업은 권한이 있도록 막아놨다. -> 요청헤더에 인증정보 넣어줘야함
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(true))
                .contentType(MediaType.APPLICATION_JSON) //  요청에 JSON을 담아서 보내고 있다.
                .accept(MediaTypes.HAL_JSON) // HAL_JSON 응답을 원한다.
                .content(objectMapper.writeValueAsString(event))) // 객체를 json 문자열로 바꾸고 요청 본문 content에 넣기
                .andDo(print())
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
                .andDo(document("create-event", // create-event 라는 폴더에 docs 만들기
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

        // 모든것을 문서화하고 싶지 않다면 prefix로 relaxed를 붙이면된다.
        // relaxedResponseFields
        // 이렇게 문서 조각들을 만들었으면 완성된 api 문서를 만들어야지!


    }

    private String getBearerToken(boolean needToCreateAccount) throws Exception {
        return "Bearer " + getAccessToken(needToCreateAccount);
    }

    private String getAccessToken(boolean needToCreateAccount) throws Exception {
        // Given
        if (needToCreateAccount) {
            createAccount();
        }
        // 토큰 받기
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
        Account keesun = Account.builder()
                .email(appProperties.getUserUsername())
                .password(appProperties.getUserPassword())
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();
        return accountService.saveAccount(keesun);
    }


    // properties 설정으로 이외의 값이 주입되면 fail시키는 설정이 있는데 그냥 dto로 무시하게 하는게 더 유연하고 나은듯
    @DisplayName("들어오는 값이 dto 이외의 필드가 필어올 경우 실패 -> properties 설정")
    @Test
    void createEvent_bad_request() throws Exception {
        Event event = Event.builder()
                .id(100)
                .name("Spring")
                .description("REST API development")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 23, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 24, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 25, 14, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 26, 14, 21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역")
                .free(true) // 잘못된 입력 -> dto로 걸러서 false되게 해놨음
                .offline(false) // 잘못된 입력
                .eventStatus(EventStatus.PUBLISHED)
                .build();


        // event 객체를 만들었는데 이걸 JSON 타입으로 보내줘야 한다 어떻게해줘야할까?
        // 스프링 부트를 사용하면 mappingjacksonjson이 의존성으로 들어가 있으면
        // objectmapper가 자동으로 빈으로 등록된다. 따라서 autowired로 주입받아서 사용할 수 있따.
        mockMvc.perform(post("/api/events/")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(true))
                .contentType(MediaType.APPLICATION_JSON) //  요청에 JSON을 담아서 보내고 있다.
                .accept(MediaTypes.HAL_JSON) // HAL_JSON 응답을 원한다.
                .content(objectMapper.writeValueAsString(event))) // 객체를 json 문자열로 바꾸고 요청 본문 content에 넣기
                .andDo(print())
                .andExpect(status().isBadRequest()); //
    }

    @Test
    void createEvent_Bad_Request_Empty_Input() throws Exception {
        EventDto eventDto = EventDto.builder().build();

        mockMvc.perform(post("/api/events")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(true))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest()); // 컨트롤러 로직을 보면 vaildation에 의해 에러가 발생하면 badrequest를 반환하고 있음
    }

    @DisplayName("값은 정상적으로 들어왔는데 논리적으로 날짜 앞뒤가 다름")
    @Test
    void createEvent_Bad_Request_Wrong_Input() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("REST API development")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 26, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 25, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 23, 14, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 24, 14, 21))
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역")
                .build();

        mockMvc.perform(post("/api/events")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(true))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest()); // 컨트롤러 로직을 보면 vaildation에 의해 에러가 발생하면 badrequest를 반환하고 있음
    }

    @DisplayName("입력값이 잘못된 경우에 에러가 발생하는 테스트")
    @Test
    void createEvent_Bad_Request_Wrong_Input2() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("REST API development")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 26, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 25, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 23, 14, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 24, 14, 21))
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역")
                .build();

        mockMvc.perform(post("/api/events")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(true))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest()) // 컨트롤러 로직을 보면 만들어 놓은 validator에 의해 오류 발생하고 badrequest보냄
                .andExpect(jsonPath("errors[0].objectName").exists()) // $가 error 배열을 의미하나보다
                .andExpect(jsonPath("errors[0].defaultMessage").exists())
                .andExpect(jsonPath("errors[0].code").exists())
                .andExpect(jsonPath("_links.index").exists())
        ;

    }

    @DisplayName("30개의 이벤트를 10개씩 두번째 페이지 조회")
    @Test
    void queryEventsWithAuthentication() throws Exception {
        //given
        IntStream.range(0, 30).forEach(i -> {
            generateEvent(i);
        });

        //when

        mockMvc.perform(get("/api/events")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(true))
                .param("page", "1")
                .param("size", "10")
                .param("sort", "name,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.create-event").exists())
                .andDo(document("query-events"))
        ;
        //then
    }

    @DisplayName("기존의 이벤트를 하나 조회하기")
    @Test
    void getEvent() throws Exception {
        //given
        Account account = createAccount();
        Event event = generateEvent(100,account);

        //when
        // 쉼표찍고 뒤에 값주면 pathvaraible 값 줄 수 있음
        mockMvc.perform(get("/api/events/{id}", event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("get-an-event"))
        ;

        //then
    }

    @DisplayName("없는 이벤트를 조회했을 때 404 응답받기")
    @Test
    void getEvent404() throws Exception {
        mockMvc.perform(get("/api/events/11883"))
                .andExpect(status().isNotFound());

    }

    // 첫 테스트 이후 doc을 생략하고 있는데
    // 오류테스트 말고 정상적인 테스트에 대해서는 doc를 link 다 걸어서 작성해줘야한다

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
                .andDo(document("update-event"))
        ;

    }

    @DisplayName("입력값이 잘못된 경우에 이벤트 수정 실패")
    @Test
    void updateEvent400Wrong() throws Exception {
        //given
        Event event = generateEvent(200);
        String eventName = "Updated Event";
        EventDto eventDto = modelMapper.map(event, EventDto.class);
        eventDto.setName(eventName);
        eventDto.setBasePrice(20000);
        eventDto.setMaxPrice(100);

        mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(true))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;

    }

    @DisplayName("입력값이 비어있는 경우에 이벤트 수정 실패")
    @Test
    void updateEvent400Empty() throws Exception {
        //given
        Event event = generateEvent(200);
        String eventName = "Updated Event";
        EventDto eventDto = modelMapper.map(event, EventDto.class);
        eventDto.setName(eventName);

        mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(true))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;

    }

    @DisplayName("존재하지않는 이벤트 이벤트 수정 실패")
    @Test
    void updateEvent400() throws Exception {
        //given
        Event event = generateEvent(200);
        EventDto eventDto = modelMapper.map(event, EventDto.class);

        mockMvc.perform(put("/api/events/123123")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(true))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isNotFound())

        ;

    }

    // 메서드 오버로딩 // 이벤트 수정시 사용할 것
    private Event generateEvent(int i, Account account) {
        Event event = Event.builder()
                .name("event" + i)
                .description("test event")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 23, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 24, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 25, 14, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 26, 14, 21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역")
                .free(false)
                .offline(true)
                .eventStatus(EventStatus.DRAFT)
                .manager(account)
                .build();

        return eventRepository.save(event);
    }


    private Event generateEvent(int i) {
        Event event = Event.builder()
                .name("event" + i)
                .description("test event")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 23, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 24, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 25, 14, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 26, 14, 21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역")
                .free(false)
                .offline(true)
                .eventStatus(EventStatus.DRAFT)
                .build();

        return eventRepository.save(event);
    }


}



















