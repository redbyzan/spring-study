package com.example.springsecurity.member.repository;

import com.example.springsecurity.member.entity.Member;
import com.example.springsecurity.member.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {


    Optional<Member> findByEmailAndSocial(String email,boolean social);

    Optional<Member> findByEmailAndAuthProvider(String email, AuthProvider authProvider);

    boolean existsByEmailAndAuthProvider(String email, AuthProvider authProvider);
}
