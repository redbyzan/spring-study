package com.webservicestudy.webservicestudy.modules.event;

import com.webservicestudy.webservicestudy.infra.AbstractContainerBaseTest;
import com.webservicestudy.webservicestudy.infra.MockMvcTest;
import com.webservicestudy.webservicestudy.modules.account.WithAccount;
import com.webservicestudy.webservicestudy.modules.account.Account;
import com.webservicestudy.webservicestudy.modules.account.AccountRepository;
import com.webservicestudy.webservicestudy.modules.study.Study;
import com.webservicestudy.webservicestudy.modules.study.StudyService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class EventControllerTest extends AbstractContainerBaseTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired EventService eventService;
    @Autowired EnrollmentRepository enrollmentRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired StudyService studyService;


    @Test
    @DisplayName("선착순 모임에 참가 신청 - 자동 수락")
    @WithAccount("backtony")
    void newEnrollment_to_FCFS_event_accepted() throws Exception {
        // test1 계정 만들고 test1이 study -> event 개설
        Account test1 = createAccount("test1");
        Study study = createStudy("test-study", test1);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, test1);
        // backtony로 참기 선청
        mockMvc.perform(post("/study/"+study.getPath()+"/events/"+event.getId()+"/enroll")
                        .with(csrf()))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(redirectedUrl("/study/"+study.getPath()+"/events/"+event.getId()))
        ;
        // backtony가 참가 됬는지 확인
        Account backtony = accountRepository.findByNickname("backtony");
        isAccepted(backtony,event);
    }

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 대기중 (이미 인원이 꽉차서)")
    @WithAccount("backtony")
    void newEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account test1 = createAccount("test1");
        Study study = createStudy("test-study", test1);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, test1);

        Account test2 = createAccount("test2");
        Account test3 = createAccount("test3");
        eventService.newEnrollment(event,test2);
        eventService.newEnrollment(event,test3);
        mockMvc.perform(post("/study/"+study.getPath()+"/events/"+event.getId()+"/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/"+study.getPath()+"/events/"+event.getId()))
        ;
        Account backtony = accountRepository.findByNickname("backtony");
        isNotAccepted(backtony,event);


    }

    @Test
    @DisplayName("참가신청 확정자가 선착순 모임에 참가 신청을 취소하는 경우, 바로 다음 대기자를 자동으로 신청 확인한다.")
    @WithAccount("backtony")
    void accepted_account_cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account test1 = createAccount("test1");
        Study study = createStudy("test-study", test1);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, test1);
        Account backtony = accountRepository.findByNickname("backtony");
        Account test2 = createAccount("test2");
        eventService.newEnrollment(event,test2);
        eventService.newEnrollment(event,backtony);
        eventService.newEnrollment(event,test1);

        mockMvc.perform(post("/study/"+study.getPath()+"/events/"+event.getId()+"/disenroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/"+study.getPath()+"/events/"+event.getId()))
        ;

        isAccepted(test2,event);
    }

    @Test
    @DisplayName("참가신청 비확정자가 선착순 모임에 참가 신청을 취소하는 경우, 기존 확정자를 그대로 유지하고 새로운 확정자는 없다.")
    @WithAccount("backtony")
    void not_accepterd_account_cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account test1 = createAccount("test1");
        Study study = createStudy("test-study", test1);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, test1);
        Account backtony = accountRepository.findByNickname("backtony");
        Account test2 = createAccount("test2");
        Account test3 = createAccount("test3");
        eventService.newEnrollment(event,test2);
        eventService.newEnrollment(event,test1);
        eventService.newEnrollment(event,backtony);
        eventService.newEnrollment(event,test3);

        mockMvc.perform(post("/study/"+study.getPath()+"/events/"+event.getId()+"/disenroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/"+study.getPath()+"/events/"+event.getId()))
        ;
        isAccepted(test2,event);
        isAccepted(test1,event);
        isNotAccepted(test3,event);
    }

    @Test
    @DisplayName("관리자 확인 모임에 참가 신청 - 대기중")
    @WithAccount("backtony")
    void newEnrollment_to_CONFIMATIVE_event_not_accepted() throws Exception {
        Account test1 = createAccount("test1");
        Study study = createStudy("test-study", test1);
        Event event = createEvent("test-event", EventType.CONFIRMATIVE, 2, study, test1);

        mockMvc.perform(post("/study/"+study.getPath()+"/events/"+event.getId()+"/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/"+study.getPath()+"/events/"+event.getId()))
        ;
        Account backtony = accountRepository.findByNickname("backtony");
        isNotAccepted(backtony,event);
    }



    private void isNotAccepted( Account account,Event event) {

        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, account);
        assertFalse(enrollment.isAccepted());
    }


    private void isAccepted(Account account, Event event) {
        assertTrue(enrollmentRepository.findByEventAndAccount(event,account).isAccepted());
    }

    protected Study createStudy(String path, Account manager) {
        Study study = new Study();
        study.setPath(path);
        studyService.createNewStudy(study, manager);
        return study;
    }

    protected Account createAccount(String nickname) {
        Account whiteship = new Account();
        whiteship.setNickname(nickname);
        whiteship.setEmail(nickname + "@email.com");
        accountRepository.save(whiteship);
        return whiteship;
    }
    private Event createEvent(String eventTitle, EventType eventType, int limit, Study study, Account account) {
        Event event = new Event();
        event.setEventType(eventType);
        event.setLimitOfEnrollments(limit);
        event.setTitle(eventTitle);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setEndEnrollmentDateTime(LocalDateTime.now().plusDays(1));
        event.setStartDateTime(LocalDateTime.now().plusDays(1).plusHours(5));
        event.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(7));
        return eventService.createEvent(event, account, study);
    }


}