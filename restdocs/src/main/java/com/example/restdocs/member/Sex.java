package com.example.restdocs.member;

import com.example.restdocs.EnumType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public enum Sex implements EnumType {
    MALE("남자"),
    FEMALE("여자")
    ;

    private String description;

    @Override
    public String getId() {
        return this.name();
    }

    @Override
    public String getText() {
        return this.description;
    }
}
