package com.apistudy.restapi.common;


import com.apistudy.restapi.index.IndexController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.validation.Errors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

// 에러 넘길 때 루트 페이지 인덱스까지 추가해서 넘기기
public class ErrorsResource extends EntityModel<Errors> {

    // 에러에는 홈으로 가도록 index 페이지 리소스 넣음
    public ErrorsResource(Errors content, Link... links) {
        super(content, links);
        add(linkTo(methodOn(IndexController.class).index()).withRel("index"));
    }



}
