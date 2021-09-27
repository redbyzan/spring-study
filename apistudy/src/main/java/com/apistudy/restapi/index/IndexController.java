package com.apistudy.restapi.index;


import com.apistudy.restapi.events.EventController;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
public class IndexController {
    // 메인 페이지?
    @GetMapping("/api")
    public RepresentationModel index(){
        var index = new RepresentationModel<>();
        index.add(linkTo(EventController.class).withRel("events"));
        return index;
    }
}
