package com.example.demo.controller;

import com.example.demo.controller.dto.MemberResponseDto;
import com.example.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;



    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('USER','ADMIN')") // 둘다 접근 가능
    public ResponseEntity<MemberResponseDto> getMyUserInfo(){
        return ResponseEntity.ok(memberService.getMyInfo());
    }

    @GetMapping("/user/{username}")
    @PreAuthorize("hasAnyRole('ADMIN')") // admin만 접근 가능
    public ResponseEntity<MemberResponseDto> getUserInfo(@PathVariable String username){
        return ResponseEntity.ok(memberService.getMemberInfo(username));
    }
}
