package com.example.demo.controller;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithAccountSecurityContextFactory.class)
// WithSecurityContext의 팩토리를 WithAccountSecurityContextFactory
// 이제 WithAccountSecurityContextFactory의 구현체를 만들어야함
public @interface WithAccount {

    String value(); // 애노테이션에 value로 넘어오는 값을 가질 수 있음
}
