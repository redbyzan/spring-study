package com.example.springsecurity.member.controller.utils;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // runtime시 까지 유지
@Target(ElementType.PARAMETER) // 파라미터에 사용할 것
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : member")
public @interface LoginUser {
}
