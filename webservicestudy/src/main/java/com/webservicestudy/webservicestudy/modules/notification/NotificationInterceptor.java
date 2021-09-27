package com.webservicestudy.webservicestudy.modules.notification;

import com.webservicestudy.webservicestudy.modules.account.Account;
import com.webservicestudy.webservicestudy.modules.account.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


// 모든 요청마다 알람을 확인해서 알람이 있는지 없는지 보여주고 싶다
// 그럼 모든 컨트롤러에 다 그 메서드를 붙여줘야하나?
// 그것을 해결하기 위해 mvc가 제공하는 HandlerInterceptor가 있다.
@Component
@RequiredArgsConstructor
public class NotificationInterceptor implements HandlerInterceptor {
    private final NotificationRepository notificationRepository;

    // prehandle : 핸들러 들어가기 전에 실행
    // aftercompletion : 뷰 랜더링 끝난 다음
    // posthandle : 핸들러 처리 이후 뷰 랜더링 전


    // 인증정보가 있는 요청에서만 알림을 줘야함
    // 리다이렉트 요청에도 적용하지 않을것
    // 어쩌피 리다이렉트에서 핸들러를 다시 탐
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // modelAndView를 쓰는 경우에만 넣어줄 것임
        // 뷰를 랜더링 전에 model에 "hasNotification"를 넣어주는 작업을 하는 것임
        // 이제 만들었으니 webconfig로 설정
        if (modelAndView != null && !isRedirectView(modelAndView) && authentication != null && authentication.getPrincipal() instanceof UserAccount){
            Account account = ((UserAccount) authentication.getPrincipal()).getAccount();
            long count = notificationRepository.countByAccountAndChecked(account, false);
            modelAndView.addObject("hasNotification",count>0);
        }
    }

    private boolean isRedirectView(ModelAndView modelAndView) {
        // 뒤에 redirectview타입은 문자열로 redirect가 아니라 무슨 new RedirectView("/")이렇게 쓰는 경우를 말한다.
        return modelAndView.getViewName().startsWith("redirect:") || modelAndView.getView() instanceof RedirectView;
    }
}
