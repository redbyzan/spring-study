package com.webservicestudy.webservicestudy.modules.event;

import com.webservicestudy.webservicestudy.modules.account.Account;
import com.webservicestudy.webservicestudy.modules.account.UserAccount;
import com.webservicestudy.webservicestudy.modules.study.Study;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor @AllArgsConstructor
public class Event {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Study study;

    @ManyToOne
    private Account createdBy;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdDateTime;

    @Column(nullable = false)
    private LocalDateTime endEnrollmentDateTime;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    private Integer limitOfEnrollments;

    @OneToMany(mappedBy = "event")
    private List<Enrollment> enrollments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    // 이미 신청함
    public boolean isDisenrollableFor(UserAccount userAccount) {
        return isNotClosed() && isAlreadyEnrolled(userAccount) && isAttended(userAccount);
    }


    // 신청 가능
    public boolean isEnrollableFor(UserAccount userAccount){
        return isNotClosed() && !isAlreadyEnrolled(userAccount)&& !isAttended(userAccount);
    }

    // 참석 완료
    public boolean isAttended(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (Enrollment e : this.enrollments) {
            if (e.getAccount().equals(account) && e.isAttended()) {
                return true;
            }
        }

        return false;
    }

    private boolean isAlreadyEnrolled(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getAccount().equals(account))
                return true;
        }
        return false;
    }
    public int numberOfRemainSpots(){
        return limitOfEnrollments - (int) enrollments.stream().filter(Enrollment::isAccepted).count();


    }

    private boolean isNotClosed() {
        return endEnrollmentDateTime.isAfter(LocalDateTime.now());
    }

    public long getNumberOfAcceptedEnrollments() {
        return enrollments.stream().filter(Enrollment::isAccepted).count();
    }

    // view에서 사용하는 메서드
    // 매니저가 현재 이벤트에서 enrollment를 수락 가능한 상태인지 확인
    public boolean canAccept(Enrollment enrollment){
        return eventType == EventType.CONFIRMATIVE
                && enrollments.contains(enrollment)
                && !enrollment.isAttended()
                && !enrollment.isAccepted();
    }

    public boolean canReject(Enrollment enrollment) {
        return this.eventType == EventType.CONFIRMATIVE
                && this.enrollments.contains(enrollment)
                && !enrollment.isAttended()
                && enrollment.isAccepted();
    }

    // 양방향 관계
    public void addEnrollment(Enrollment enrollment) {
        getEnrollments().add(enrollment);
        enrollment.setEvent(this);
    }

    // 선입선출이면서 현재 event에 신청자와 허용 인원보자 작으면 true를 리턴
    public boolean isAbleToAcceptWaitingEnrollment() {
        return eventType == EventType.FCFS && limitOfEnrollments>getNumberOfAcceptedEnrollments();
    }

    public void removeEnrollment(Enrollment enrollment) {
        enrollments.remove(enrollment);
        enrollment.setEvent(null);
    }

    public void acceptNextWaitingEnrollment() {
        if (isAbleToAcceptWaitingEnrollment()){
            Enrollment nextEnrollment = getTheFirstWaitingEnrollment();
            if (nextEnrollment != null){
                nextEnrollment.setAccepted(true);
            }

        }
    }

    private Enrollment getTheFirstWaitingEnrollment() {
        for (Enrollment enrollment : enrollments) {
            if (!enrollment.isAccepted()){
                return enrollment;
            }
        }
        return null;
    }

    public void acceptWaitingList() {
        if (isAbleToAcceptWaitingEnrollment()){
            List<Enrollment> waitingList = getWaitingList();
            int numberToAccept = (int)Math.min(limitOfEnrollments - getNumberOfAcceptedEnrollments(),waitingList.size());
            waitingList.subList(0,numberToAccept).forEach(e->e.setAccepted(true));

        }
    }

    private List<Enrollment> getWaitingList() {
        return getEnrollments().stream().filter(e -> !e.isAccepted()).collect(Collectors.toList());
    }

    public void accept(Enrollment enrollment) {
        if(eventType == EventType.CONFIRMATIVE
                && limitOfEnrollments > getNumberOfAcceptedEnrollments()){
            enrollment.setAccepted(true);
        }
    }

    public void reject(Enrollment enrollment) {
        if(eventType == EventType.CONFIRMATIVE){
            enrollment.setAccepted(false);
        }
    }
}
