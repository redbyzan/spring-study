package com.example.demo.controller.dto;

import com.example.demo.domain.Authority;
import com.example.demo.domain.Member;
import lombok.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberRequestDto {

    @NotNull
    @Size(min =3, max =50)
    private String username;

    @NotNull
    @Size(min=3,max=100)
    private String password;

    @NotNull
    @Size(min=3,max=100)
    private String nickname;

    public UsernamePasswordAuthenticationToken toAuthentication() {
        return new UsernamePasswordAuthenticationToken(username, password);
    }


    public Member toMember(PasswordEncoder passwordEncoder) {
        return Member.builder()
                .username(username)
                .nickname(nickname)
                .password(passwordEncoder.encode(password))
                .authority(Authority.ROLE_USER)
                .build();
    }
}
