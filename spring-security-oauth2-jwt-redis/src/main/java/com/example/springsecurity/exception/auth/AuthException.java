package com.example.springsecurity.exception.auth;

import com.example.springsecurity.exception.ApplicationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;

public abstract class AuthException extends ApplicationException {
    protected AuthException(String errorCode, HttpStatus httpStatus, String message) {
        super(errorCode, httpStatus, message);
    }

    protected AuthException(String errorCode, HttpStatus httpStatus, String message, BindingResult errors) {
        super(errorCode, httpStatus, message, errors);
    }
}
