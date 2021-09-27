package com.example.demo.controller.dto;

import com.example.demo.domain.Member;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberResponseDto {
    @NotNull
    @Size(min=3,max=50)
    private String username;

    @NotNull
    @Size(min=3,max=50)
    private String nickname;

    public MemberResponseDto(Member member) {
        this.username = member.getUsername();
        this.nickname = member.getNickname();
    }
}
