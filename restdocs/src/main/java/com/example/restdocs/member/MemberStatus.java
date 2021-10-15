package com.example.restdocs.member;

import com.example.restdocs.EnumType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MemberStatus implements EnumType {
    LOCK("일시 정지"),
    NORMAL("정상"),
    BAN("영구 정지");

    private final String description;

    @Override
    public String getId() {
        return this.name();
    }

    @Override
    public String getText() {
        return this.description;
    }
}