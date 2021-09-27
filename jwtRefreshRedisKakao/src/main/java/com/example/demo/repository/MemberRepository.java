package com.example.demo.repository;

import com.example.demo.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {

    Optional<Member> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<Member> findByUsernameAndProvider(String username, String provider);
}
