package com.webservicestudy.webservicestudy.modules.main;

import com.webservicestudy.webservicestudy.modules.account.AccountRepository;
import com.webservicestudy.webservicestudy.modules.account.CurrentUser;
import com.webservicestudy.webservicestudy.modules.account.Account;
import com.webservicestudy.webservicestudy.modules.event.EnrollmentRepository;
import com.webservicestudy.webservicestudy.modules.study.Study;
import com.webservicestudy.webservicestudy.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final StudyRepository studyRepository;
    private final AccountRepository accountRepository;
    private final EnrollmentRepository enrollmentRepository;

    // 프론트 단에서 현재 사용자 정보를 가지고 뷰를 처리해야한다면 현재 사용자를 model에 담아서 넘긴다
    // 여기서는 이메일 인증 사용자인지 아닌지 구분하기 위해 사용자 정보를 모델로 넘김
    @GetMapping("/")
    public String home(@CurrentUser Account account, Model model){
        if (account != null){
            Account accountLoaded = accountRepository.findAccountWithTagsAndZonesById(account.getId());
            model.addAttribute(accountLoaded);
            // enrollment에 event에 들어있는 study에 title이 필요함
            // event까지 긁어오는 건 아는데 event 안에 있는 study까지 땡겨와야함
            // entitygraph의 서브그래프를 사용해야함 이때는 바로 못쓰고 named로 지정해야 함
            model.addAttribute("enrollmentList", enrollmentRepository.findByAccountAndAcceptedOrderByEnrolledAtDesc(accountLoaded, true));

            model.addAttribute("studyList", studyRepository.findByAccount(
                    accountLoaded.getTags(),
                    accountLoaded.getZones()));

            // containing은 컬렉션에 포함되었는지 문자열에 포함되어 있는지 등을 확인해준다.
            model.addAttribute("studyManagerOf",
                    studyRepository.findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(account, false));
            model.addAttribute("studyMemberOf",
                    studyRepository.findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(account, false));

            return "index-after-login";
        }

        model.addAttribute("studyList",studyRepository.findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(true,false));
        return "index";
    }




    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/search/study")
    // pageable spring domain것 사용 // pageable로 size, sort, page 파라미터를 하나로 받음
    // pageable size 기본값은 20, @pageableDefault로 기본값 수정가능, 해당 애노테이션 붙이면 기본값이 다르게 바뀌는데 그건 ctrl space로 확인해봐
    public String searchStudy(@PageableDefault(size = 9,sort = "publishedDateTime",direction = Sort.Direction.ASC) Pageable pageable, String keyword, Model model){
        Page<Study> studyPage = studyRepository.findByKeyword(keyword, pageable);
        // 키 생략하고 넣으면 타입으로 첫글자 소문자로 들어간다고 했다. 그런데 여기서 만약 값이 null이면 model에 안넘어간다. 그래서 내 생각에는 키값을 명시해주는게 좋을 것 같다.
        model.addAttribute("studyPage",studyPage);
        model.addAttribute("keyword",keyword);
        model.addAttribute("sortProperty",
                pageable.getSort().toString().contains("publishedDateTime") ? "publishedDateTime" : "memberCount");
        return "search";
    }



}
