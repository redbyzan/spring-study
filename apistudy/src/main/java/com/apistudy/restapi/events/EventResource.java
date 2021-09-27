package com.apistudy.restapi.events;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

// hateoas를 따르도록 링크 정보를 넣어주기 위해서는
// spring hateoas가 지원하는 기능 중에 EntityModel 기능을 사용한다
public class EventResource extends EntityModel<Event> {


    // EventModel은 인자를 받아 리소스를 추가할 수 있도록 만들어준다.
    // 현재 엔티티는 event고 이걸 기준으로 현재 위치 리소스를 추가하력 하는 것이기 때문에
    // 생성자에 현재 리소스 추가해줬다.
    // 이걸 json으로 내리면 event와 links로 추가한 self가 붙는다.

    public EventResource(Event event, Link... links) {
        super(event, links);
        //매번 self 링크를 만들어줘야 하니 생성자에 넣었다.
        add(linkTo(EventController.class).slash(event.getId()).withSelfRel());
    }


}
/*
// 위에가 정석적 방법이고 아래는 간단한 방법 인데 업데이트되서 안되는듯
public class EventResource extends EntityModel<Event> {
    d
}
 */
