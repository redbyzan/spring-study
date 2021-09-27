package com.example.mysqltest.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public void findMember(){
        List<Member> all = memberRepository.findAll();
        for (Member member : all) {
            System.out.println("member = " + member);
        }
    }

    public void saveMember(){
        Member member = Member.builder()
                .age(26)
                .name("test")
                .build();
        memberRepository.save(member);
    }

}
