package com.webservicestudy.webservicestudy.modules.notification;

import com.webservicestudy.webservicestudy.modules.account.Account;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
public class Notification {
    @Id @GeneratedValue
    private Long id;

    private String title;

    private String message;

    private String link;

    private boolean checked;

    @ManyToOne
    private Account account;

    private LocalDateTime createdDateTime;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;
}
