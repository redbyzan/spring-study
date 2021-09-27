package com.webservicestudy.webservicestudy.modules.main;

import com.webservicestudy.webservicestudy.modules.account.Account;
import com.webservicestudy.webservicestudy.modules.account.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
// 어떤 요청으로 잘못된 요청을 보내는가 파악하기 위한 핸들러
public class ExceptionAdvice {

    @ExceptionHandler
    public String handleRuntimeException(@CurrentUser Account account, HttpServletRequest request, RuntimeException e){
        if(account != null){
            log.info("'{}' requested '{}'",account.getNickname(),request.getRequestURI());
        } else{
            log.info("requested '{}'",request.getRequestURI());
        }
        log.error("bad request",e);
        return "error"; // 이렇게 하면 잘못된 요청이 들어오면 로깅하고 에러 페이지로 보냄


    }
}
