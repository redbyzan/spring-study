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
    public String getDescription() {
        return this.description;
    }

    @Override
    public String getName() {
        return this.name();
    }
}
