package com.example.restdocs.member;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Transactional
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/{id}")
    public MemberResponse getMember(@PathVariable Long id){
        return new MemberResponse(memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Not Found")));
    }

    @PostMapping
    public void createMember(@RequestBody @Valid MemberSignUpRequest dto){
        memberRepository.save(dto.toEntity());
    }

    @PutMapping("/{id}")
    public void modify(@PathVariable Long id
            ,@RequestBody @Valid MemberModificationRequest dto){
        Member member = memberRepository.findById(id).get();
        member.modify(dto.getAge());
    }

    @GetMapping
    public Page<MemberResponse> getMembers(
            @PageableDefault(sort = "id",direction = Sort.Direction.DESC)Pageable pageable
            ){
        return memberRepository.findAll(pageable).map(MemberResponse::new);
    }

}
