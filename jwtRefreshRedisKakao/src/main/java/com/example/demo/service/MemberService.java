package com.example.demo.service;

import com.example.demo.controller.dto.MemberResponseDto;
import com.example.demo.repository.MemberRepository;
import com.example.demo.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberResponseDto getMemberInfo(String username){
        return memberRepository.findByUsername(username)
                .map(MemberResponseDto::new)
                .orElseThrow(() -> new RuntimeException("유저 정보가 없습니다."));
    }

    // 현재 SecurityContext 에 있는 유저 정보 가져오기
    public MemberResponseDto getMyInfo(){
        String username = SecurityUtil.getCurrentUsername()
                .orElseThrow(() -> new RuntimeException("현재 유저 정보가 없습니다."));

        return memberRepository.findByUsername(username)
                .map(MemberResponseDto::new)
                .orElseThrow(() -> new RuntimeException("유저 정보가 없습니다."));
    }

}
