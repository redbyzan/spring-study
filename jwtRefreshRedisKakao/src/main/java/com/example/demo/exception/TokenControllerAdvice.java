package com.example.demo.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TokenControllerAdvice {
    
    // localdatetime으로 수정 필요
    
    /*
    // HTTP Status code
    // 2XX -> OK
    // 4XX -> Client
    // 5XX -> Server
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
    위 처럼 사전에 exception 날릴때 내가 만든 exception을 날리게 하고
    advice에서 각각의 exception에 대한 처리를 진행한다.
     */

    /*
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        ExceptionResponse exceptionResponse =
                new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false));

        return new ResponseEntity(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public final ResponseEntity<Object> handleUserNotFoundException(Exception ex, WebRequest request) {
        ExceptionResponse exceptionResponse =
                new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false));

        return new ResponseEntity(exceptionResponse, HttpStatus.NOT_FOUND);
    }

    // CustomizedResponseEntityExceptionHandler // 사용자가 입력한 값에 문제가 생겼을 때 사용하는 예외 메소드 재정의
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(),
                "Validation Failed", ex.getBindingResult().toString());

        return new ResponseEntity(exceptionResponse, HttpStatus.BAD_REQUEST);
    }
    */

}
