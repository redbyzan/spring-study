package com.example.querydsl;

import java.util.Optional;

public interface MemberRepository {

    Optional<Member> findById(Long id);
}
