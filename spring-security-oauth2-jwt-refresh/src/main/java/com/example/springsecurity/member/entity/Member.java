package com.example.springsecurity.member.entity;

import com.example.springsecurity.member.dto.SignUpRequest;
import com.example.springsecurity.member.enums.AuthProvider;
import com.example.springsecurity.member.enums.Role;
import lombok.*;

import javax.persistence.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class Member extends BaseEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    private String password;

    @Column(nullable = false)
    private String name;

    private String picture;

    @Column(nullable = false)
    private boolean social;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;


    public static Member ofLocal(SignUpRequest signUpRequest){
        return Member.builder()
                .social(false)
                .authProvider(AuthProvider.local)
                .role(Role.USER)
                .name(signUpRequest.getName())
                .email(signUpRequest.getEmail())
                .password(signUpRequest.getPassword())
                .build();
    }


    public Member update(String name, String picture){
        this.name = name;
        this.picture = picture;
        return this;
    }

    public String getRoleKey(){
        return this.role.getKey();
    }
}
