package com.apistudy.restapi.events;

import com.apistudy.restapi.accounts.Account;
import com.apistudy.restapi.accounts.AccountAdapter;
import com.apistudy.restapi.accounts.CurrentUser;
import com.apistudy.restapi.common.ErrorsResource;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
// 이 클래스 안에 있는 모든 핸들러들은 HAL Json 타입으로 응답을 보낸다.
@RequestMapping(value = "/api/events",produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;

    //ResponseEntity는 사용자의 HttpRequest에 대한 응답 데이터를 포함하는 클래스
    // 따라서 HttpStatus, HttpHeaders, HttpBody를 포함한다.
    @PostMapping()
    // valid로 검증한 결과를 errors에 담아준다. error은 spring것 사용
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto ,
                                      Errors errors,
                                      @CurrentUser Account currentUser){

        // json으로 응답을 넘기기 위해서 위에 미디어 타입으로 설정을 해두었지만
        // errors는 자바 빈 스펙을 준수하고 있는 객체가 아니기 때문에 beanserializer을 통해 json 변환이 불가능하다
        // 즉 바디부에 그냥 넣으면 json으로 변환해서 내리지 못한다는 뜻이다.
        // 따라서 JsonSerializer을 상속받아 error를 Json으로 처리할 수 있는 serializer을 만들어야 등록해야만
        // body로 그냥 넘겼을 때 error을 json으로 넘길 수 있게 된다.

        // 1차적으로 valid로 걸러주고
        if(errors.hasErrors()){
            return badRequest(errors); // 메서드로 뺏는데 에러 보낼때 루트 리소스도 추가해서 내림
        }
        // 2차적으로 논리적 검증

        eventValidator.validate(eventDto,errors);
        if (errors.hasErrors()){
            return badRequest(errors);
        }

        // modelmapper은 setter을 사용하는데 엔티티에 setter쓰면 안된다고 했는데 우짜나
        // 엔티티에 메서드를 하나 만들어서 옮겨야하지 않나 싶다
        // 질문 글을 보니 같은 생각에 질문한 사람이 있던데 백기선님은 setter 자체가 위험한게 아니라
        // 어떻게 쓰느냐의 문제라고 한다
        Event event = modelMapper.map(eventDto, Event.class);
        event.update();
        event.setManager(currentUser); // 이 이벤트 매니저 설정
        Event newEvent = eventRepository.save(event);

        // Location URI를 만들껀데 HATEOS가 제공하는 linkto와 methodon을 사용
        // webmvclinkbuilder static import
        // linkto : 컨트롤러 클래스를 가리키는 webmvclinkbuilder 객체를 반환
        // methodon : 타켓 메서드(현재 메서드)의 가짜 메서드 콜이 있는 컨트롤러 프록시 클래스 생성
        // EventController 클래스의 createEvent 메서드에 {id}값이 붙은 링크를 URI로 변환하는 작업이라고 보면 된다.
        // Link를 만들때에는 @PostMapping("/api/events")처럼 메서드 레벨에 붙은 메서드를 호출하는 방식처럼 이루어지는데
        // 만약 URL이 메서드 레벨이 아니라 클래스레벨에 붙어있다면 methodon을 사용할 필요가 없다.

        // 메서드레벨에 붙어 있다면
        // 쉽게 생각해보면 링크로 바꿀껀데 어떤 클래스의 어떤 메서드 위에 있는 URI를 링크로 바꿀꺼다? 이정도인듯
        //URI createdUri = linkTo(methodOn(EventController.class).createEvent(null)).slash("{id}").toUri();

        // 이건 클래스에 붙었으니 메서드를 선택할 필요가 없는거지
        URI createdUri = linkTo(EventController.class).slash(newEvent.getId()).toUri();

        //HTTP Header에 Location=http://localhost/api/events/{id} 정보가 들어간다
        // body에는 event가 json으로 변환되어 들어감
        // 현재 event는 hateoas 하지 않음 -> 리소스 정보들이 들어가야함
        // hateoas에서는 RepresentationModel로 리소스 정보를 추가해줄 수 있는 방법을 제시
        // RepresentationModel를 상속받은 EventResource를 따로 만듦
        // 이렇게 리소스로 변환하면 따로 링크를 추가할 수 있음
        EventResource eventResource = new EventResource(event);
        eventResource.add(linkTo(EventController.class).withRel("query-events")); // rel 링크 추가, _links 키값 안에 query-events 키값에 href 키값에 만든 링크가 들어간다
        // 같은 링크인데 상관 없다. 들어오는 메서드가 다르기 때문 수정할때는 put 요청하기 때문
        eventResource.add(linkTo(EventController.class).slash(newEvent.getId()).withRel("update-event")); // rel 링크 추가
        // self 링크는 매번 작성해야 하니 생성자 안에 넣어두었따.
        eventResource.add(new Link("/docs/index.html#resources-events-create").withRel("profile"));

        return ResponseEntity.created(createdUri).body(eventResource);
    }

    private ResponseEntity<ErrorsResource> badRequest(Errors errors) {
        // 에러 받아서 badrequest 보내면서 바디에 error 정보 담기
        return ResponseEntity.badRequest().body(new ErrorsResource(errors));
    }

    // pageable을 받아서 repository에서 찾으면 pageable로 준 정보대로만 찾아옴
    // 리소스에는 현재,이전, 다음, 마지막 페이지 정보를 넣어줘야함
    // 그걸 위해서 PagedResourcesAssembler를 사용
    @GetMapping
    public ResponseEntity queryEvents(Pageable pageable,
                                      PagedResourcesAssembler<Event> assembler,
                                      @CurrentUser Account account){
        // userdetails에서 반환한 accoutnadapter을 가져오는게 정상
        // 그런데 사실상 get으로 account꺼내쓰는게 불편함
        // spring expression language 사용하면 원하는 필드값을 꺼낼 수 있음
        // 여기서 주의해야할 것이 principal에서 account를 getter로 꺼내는 건데
        // 만약 익명 사용자면 principal이 만든 adapter객체가 아니라 그냥 단순 문자열 'anonymousUser로 들어온다.
        // 그럼 당연히 내가 만든 객체가 아니니 account를 getter로 꺼내올 수도 없다
        // 이 경우를 대비해서 expression을 만든다.
        // anonymousUser이면 null을 넣어주고 아니면 account를 넣어줘라
        // 이거 너무 기니까 애노테이션 따로 만듦

        Page<Event> page = eventRepository.findAll(pageable); // 페이징 정보대로 찾아오기
        // 첫 파라미터로 이전 다음 마지막 처음 리소스를 _links부로 추가
        // 검색된 entity는 그대로 내려지는데 각각의 event에 대해 그 event로 클릭해서 이동하는 url을 넣어줘야함
        // 전에 event 내릴때 리소스 추가해서 내렸던 eventresource 활용해서 url도 추가한다.
        var pageResources = assembler.toModel(page, e -> new EventResource(e));
        pageResources.add(new Link("/docs/index.html#resources-events-list").withRel("profile"));
        if (account != null){
            // 사용자 정보가 있으면 이벤트 생성 url추가
            pageResources.add(linkTo(EventController.class).withRel("create-event"));
        }

        return ResponseEntity.ok(pageResources);
    }

    @GetMapping("/{id}")
    public ResponseEntity getEvent(@PathVariable Integer id,
                                   @CurrentUser Account currentUser){
        Optional<Event> optionalEvent = eventRepository.findById(id);
        if (optionalEvent.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        Event event = optionalEvent.get();
        EventResource eventResource = new EventResource(event);
        eventResource.add(new Link("/docs/index.html#resources-events-get").withRel("profile"));
        // 조회했을 때 매니저면 수정 uri 추가
        if (event.getManager().equals(currentUser)){
            eventResource.add(linkTo(EventController.class).slash(event.getId()).withRel("update-event"));
        }
        return ResponseEntity.ok(eventResource);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateEvent(@PathVariable Integer id,
                                      @RequestBody @Valid EventDto eventDto,
                                      Errors errors,
                                      @CurrentUser Account currentUser){
        Optional<Event> optionalEvent = eventRepository.findById(id);
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
        Event existingEvent = optionalEvent.get();

        // 수정인데 매니저가 아닌경우
        if(!existingEvent.getManager().equals(currentUser)){
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);// 인가되지 않았따.
        }

        // modelmapper.map (변경할것,변경할클래스)
        // modelmapper.map (덮어씌울내용,덮어씌울대상) // 클래스가 달라도 필드별로 매핑되는듯함
        // 덮어씌울 대상에는 그대로 덮어씐 내용으로 변함
        modelMapper.map(eventDto,existingEvent);
        Event savedEvent = eventRepository.save(existingEvent);// 서비스를 안만들어서 트랜잭션 유지가 안되서 여기서 처리

        EventResource eventResource = new EventResource(savedEvent);
        eventResource.add(new Link("/docs/index.html#resources-events-update").withRel("profile"));

        return ResponseEntity.ok(eventResource);
    }

}



























