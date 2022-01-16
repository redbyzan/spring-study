package com.example.querydsl;

import com.example.querydsl.QMember;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
@Transactional
public class MemberQueryRepositoryImpl implements MemberRepository{

    private final JPAQueryFactory query;


    @Override
    public Optional<Member> findById(Long id) {
        return null;

    }
}
