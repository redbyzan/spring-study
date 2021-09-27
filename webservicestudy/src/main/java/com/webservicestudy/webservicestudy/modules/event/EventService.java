package com.webservicestudy.webservicestudy.modules.event;

import com.webservicestudy.webservicestudy.modules.account.Account;
import com.webservicestudy.webservicestudy.modules.event.event.EnrollmentAcceptedEvent;
import com.webservicestudy.webservicestudy.modules.event.event.EnrollmentRejectedEvent;
import com.webservicestudy.webservicestudy.modules.study.Study;
import com.webservicestudy.webservicestudy.modules.event.form.EventForm;
import com.webservicestudy.webservicestudy.modules.study.study.StudyUpdateEvent;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EnrollmentRepository enrollmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Event createEvent(Event event, Account account, Study study) {
        event.setCreatedBy(account);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setStudy(study);
        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(),
                "'" + event.getTitle() +"' 모임을 만들었습니다."));
        return eventRepository.save(event);
    }


    public void updateEvent(Event event, EventForm eventForm) {
        modelMapper.map(eventForm,event);
        // event의 인원이 늘어나면 선입선출의 경우 대기중이던 인원 등록 처리해줘야한
        event.acceptWaitingList();
        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(),
                "'" + event.getTitle() +"' 모임 정보를 수정했으니 확인하세요."));
    }

    public void deleteEvent(Event event) {
        eventRepository.delete(event);
        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(),
                "'" + event.getTitle() +"' 모임을 취소했습니다."));
    }

    // 먼저 enrollment를 시도하려는 account가 전에 enrollment에 들어있는지 확인
    // enrollment 리포지토리에 event랑 account를 엮어서 찾아보면 될듯
    //있는지 없는지만 확인하면 되므로 exists사용
    public void newEnrollment(Event event, Account account) {
        if(!enrollmentRepository.existsByEventAndAccount(event,account)){
            Enrollment enrollment = Enrollment.builder()
                    .account(account)
                    .enrolledAt(LocalDateTime.now())
                    .accepted(event.isAbleToAcceptWaitingEnrollment()) // 선입선출의 경우 가능한 경우 바로 accepted 처리
                    .build();
            event.addEnrollment(enrollment);
            enrollmentRepository.save(enrollment);
        }
    }

    // 양방향이라 event에서 수동으로 양쪽 지우고 enroll 리포에서 enroll 지우기
    public void cancelEnrollment(Event event, Account account) {
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, account);
        if (!enrollment.isAttended()){
            event.removeEnrollment(enrollment);
            enrollmentRepository.delete(enrollment);
            event.acceptNextWaitingEnrollment(); // 선입선출의 경우 기다리던 다음 사람 등록

        }

    }

    public void acceptEnrollment(Event event, Enrollment enrollment) {
        event.accept(enrollment);
        eventPublisher.publishEvent(new EnrollmentAcceptedEvent(enrollment));
    }

    public void rejectEnrollment(Event event, Enrollment enrollment) {
        event.reject(enrollment);
        eventPublisher.publishEvent(new EnrollmentRejectedEvent(enrollment));
    }

    public void checkInEnrollment(Enrollment enrollment) {
        enrollment.setAttended(true);
    }

    public void cancelCheckInEnrollment(Enrollment enrollment) {
        enrollment.setAttended(false);
    }
}
