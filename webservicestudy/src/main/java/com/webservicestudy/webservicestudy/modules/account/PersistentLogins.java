package com.webservicestudy.webservicestudy.modules.account;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Table(name = "persistent_logins")
@Entity
@Getter @Setter
// rememberme에서 jabctokenrepository가 사용하는 테이블이 있는데
// 그걸 그냥 엔티티로 만들면 테이블이 되니 그걸 그대로 엔티티로 옮긴것
public class PersistentLogins {

    @Id
    @Column(length =64)
    private String series;

    @Column(nullable = false,length = 64)
    private String username;

    @Column(nullable = false,length = 64)
    private String token;

    @Column(name = "last_used",nullable = false,length = 64)
    private LocalDateTime lastUsed;

}










