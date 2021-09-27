package com.webservicestudy.webservicestudy.modules.notification;

import com.webservicestudy.webservicestudy.modules.account.Account;
import com.webservicestudy.webservicestudy.modules.account.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    // 현재 유저가 읽지 않은 알람 가져오기
    // 읽은 알람 개수 가져오기
    // 읽지 않은 알람 분류하기
    // 알람 읽음 처리하기

    @GetMapping("/notifications")
    public String getNotifications(@CurrentUser Account account, Model model) {
        List<Notification> notifications = notificationRepository.findByAccountAndCheckedOrderByCreatedDateTimeDesc(account, false);
        long numberOfChecked = notificationRepository.countByAccountAndChecked(account, true);
        putCategorizeNotifications(model, notifications, numberOfChecked,notifications.size());
        model.addAttribute("isNew", true);
        notificationService.markAsRead(notifications);

        return "notification/list";
    }

    // 읽은 알람
    @GetMapping("/notifications/old")
    public String getOldNotifications(@CurrentUser Account account, Model model) {
        List<Notification> notifications = notificationRepository.findByAccountAndCheckedOrderByCreatedDateTimeDesc(account, true);
        long numberOfNotChecked = notificationRepository.countByAccountAndChecked(account, false);
        putCategorizeNotifications(model, notifications, notifications.size(),numberOfNotChecked);
        model.addAttribute("isNew", false);
        return "notification/list";
    }

    @PostMapping("/notifications")
    public String deleteNotifications(@CurrentUser Account account) {
        notificationRepository.deleteByAccountAndChecked(account, true);
        return "redirect:/notifications";
    }


    private void putCategorizeNotifications(Model model, List<Notification> notifications, long numberOfChecked, long numberOfNotChecked) {
        List<Notification> newStudyNotifications = new ArrayList<>();
        List<Notification> eventEnrollmentNotifications = new ArrayList<>();
        List<Notification> watchingStudyNotifications = new ArrayList<>();
        for (Notification notification : notifications) {
            switch (notification.getNotificationType()){
                case STUDY_CREATED:newStudyNotifications.add(notification); break;
                case EVENT_ENROLLMENT:eventEnrollmentNotifications.add(notification); break;
                case STUDY_UPDATED:watchingStudyNotifications.add(notification); break;
            }
        }
        model.addAttribute("numberOfNotChecked", numberOfNotChecked);
        model.addAttribute("numberOfChecked", numberOfChecked);
        model.addAttribute("notifications", notifications);
        model.addAttribute("newStudyNotifications", newStudyNotifications);
        model.addAttribute("eventEnrollmentNotifications", eventEnrollmentNotifications);
        model.addAttribute("watchingStudyNotifications", watchingStudyNotifications);
    }

}
