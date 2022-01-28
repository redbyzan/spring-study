package com.example.springsecurity.exception.member;

import com.example.springsecurity.exception.ApplicationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;

public abstract class MemberException extends ApplicationException {
    protected MemberException(String errorCode, HttpStatus httpStatus, String message) {
        super(errorCode, httpStatus, message);
    }

    protected MemberException(String errorCode, HttpStatus httpStatus, String message, BindingResult errors) {
        super(errorCode, httpStatus, message, errors);
    }
}
