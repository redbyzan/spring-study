package com.example.mysqltest.member;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/save")
    public void save(){
        memberService.saveMember();
    }

    @GetMapping("/find")
    public void find(){
        memberService.findMember();
    }
}
