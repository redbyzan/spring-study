package com.example.springsecurity.member.controller;

import com.example.springsecurity.member.controller.utils.LoginUser;
import com.example.springsecurity.member.entity.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
public class MemberController {

    @GetMapping("/hello")
    public String hello(){
        return "hello world";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/member")
    public ResponseEntity<Member> getMember(@LoginUser Member member){

        return ResponseEntity.ok(member);
    }






}
