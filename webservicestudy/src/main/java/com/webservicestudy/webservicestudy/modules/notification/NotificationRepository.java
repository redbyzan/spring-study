package com.webservicestudy.webservicestudy.modules.notification;


import com.webservicestudy.webservicestudy.modules.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface NotificationRepository extends JpaRepository<Notification,Long> {
    long countByAccountAndChecked(Account account, boolean b);

    @Transactional // notification 읽음 처리 상태 변경 할것이므로
    List<Notification> findByAccountAndCheckedOrderByCreatedDateTimeDesc(Account account, boolean b);

    @Transactional
    void deleteByAccountAndChecked(Account account, boolean b);
}
