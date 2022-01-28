package com.example.springsecurity.exception.member;

import org.springframework.http.HttpStatus;

public class DuplicatedMemberException extends MemberException{

    private static final String MESSAGE = "중복되는 아이디가 존재합니다.";
    private static final String CODE = "SIGNUP-400";

    public DuplicatedMemberException() {
        super(CODE, HttpStatus.BAD_REQUEST, MESSAGE);
    }
}
