package com.webservicestudy.webservicestudy.modules.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void markAsRead(List<Notification> notifications) {
        for (Notification notification : notifications) {
            notification.setChecked(true); // 트랜잭션 상태라서 더티체킹이긴 한데 일일이 쿼리 날리지 말고
            notificationRepository.saveAll(notifications); // 한방 쿼리
        }
    }
}
