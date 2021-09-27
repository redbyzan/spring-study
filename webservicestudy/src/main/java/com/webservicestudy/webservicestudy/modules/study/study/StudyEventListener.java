package com.webservicestudy.webservicestudy.modules.study.study;

import com.webservicestudy.webservicestudy.infra.config.AppProperties;
import com.webservicestudy.webservicestudy.infra.mail.EmailMessage;
import com.webservicestudy.webservicestudy.infra.mail.EmailService;
import com.webservicestudy.webservicestudy.modules.account.Account;
import com.webservicestudy.webservicestudy.modules.account.AccountPredicates;
import com.webservicestudy.webservicestudy.modules.account.AccountRepository;
import com.webservicestudy.webservicestudy.modules.notification.Notification;
import com.webservicestudy.webservicestudy.modules.notification.NotificationRepository;
import com.webservicestudy.webservicestudy.modules.notification.NotificationType;
import com.webservicestudy.webservicestudy.modules.study.Study;
import com.webservicestudy.webservicestudy.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
@Async // 비동기적으로 사용, 다른 스레드로 돈다는 뜻 여기
@Transactional
@RequiredArgsConstructor
public class StudyEventListener {

    private final StudyRepository studyRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final NotificationRepository notificationRepository;


    @EventListener
    public void handleStudyCreatedEvent(StudyCreatedEvent studyCreatedEvent) {

        // study가 publish 되면 이 리스너가 호출된다.
        // study 정보 조회해서 study가 가지고 있는 태그와 존에 대한 정보에 매핑되는 account 정보 조회

        // studyCreatedEvent에서 전달받은 study는 getStudyToUpdateStatus에서부터 가져온 study인데 이건 manager만 detached 상태의 study로 저장되었다.
        // 트랜잭션 상태가 아니므로 lazy로 프록시로 땡겨온 것들은 사용할 수가 없다. 따라서 다시 조회가 필요하다

        Study study = studyRepository.findStudyWithTagsAndZonesById(studyCreatedEvent.getStudy().getId());
        // querydsl의 predicate 이용한 쿼리가져와서 사용
        Iterable<Account> accounts = accountRepository.findAll(AccountPredicates.findByTagsAndZones(study.getTags(), study.getZones()));

        accounts.forEach(account -> {
            if (account.isStudyCreatedByEmail()) {
                sendStudyCreatedEmail(study, account,"새로운 스터디가 생겼습니다.",
                        "스터디올래, '" + study.getTitle() + "' 스터딕가 생겼습니다.");
            }

            if (account.isStudyCreatedByWeb()) {
                createNotification(study,account,study.getShortDescription(),NotificationType.STUDY_CREATED);
            }
        });
    }

    // 딱히 구분 안해줘도 파라미터 타입으로 알아서 선택되는듯하다.
    // 스터디 수정으로 날라가는 알림이므로 해당 스터디의 매니저 맴버만 찾아오면 된다.
    @EventListener
    public void handleStudyUpdateEvent(StudyUpdateEvent studyUpdateEvent){
        Study study = studyRepository.findStudyWithManagersAndMembersById(studyUpdateEvent.getStudy().getId());
        Set<Account> accounts = new HashSet<>();

        accounts.addAll(study.getMembers());
        accounts.addAll(study.getManagers());
        accounts.forEach(account -> {
            if(account.isStudyUpdatedByEmail()){
                sendStudyCreatedEmail(study,account,studyUpdateEvent.getMessage(),
                        "스터디올래, '" + study.getTitle()+"'스터디에 새소식이 있습니다.");

            }
            if(account.isStudyUpdatedByWeb()){
                createNotification(study,account, studyUpdateEvent.getMessage(), NotificationType.STUDY_UPDATED);
            }

        });


    }




    private void createNotification(Study study, Account account, String message, NotificationType notificationType) {
        Notification notification = new Notification();
        notification.setTitle(study.getTitle());
        notification.setLink("/study/" + study.getEncodedPath());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(message);
        notification.setAccount(account);
        notification.setNotificationType(notificationType);
        notificationRepository.save(notification);
    }

    private void sendStudyCreatedEmail(Study study, Account account, String contextMessage, String emailSubject) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/study/" + study.getEncodedPath());
        context.setVariable("linkName", study.getTitle());
        context.setVariable("message", contextMessage);
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject(emailSubject)
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }
}




























