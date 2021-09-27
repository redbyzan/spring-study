package com.apistudy.restapi.accounts;


import lombok.*;
import org.springframework.web.bind.annotation.GetMapping;

import javax.persistence.*;
import java.util.Set;

@Entity
@Getter @Setter @EqualsAndHashCode(of="id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class Account {

    @Id @GeneratedValue
    private Integer id;

    @Column(unique = true)
    private String email;

    private String password;

    // 하나의 enum만 가지는게 아니라 여러개의 enum을 가지므로 elementcollection 애노테이션을 적어줘야한다.
    @ElementCollection(fetch = FetchType.EAGER) //기본이 lazy인데 가져올 롤이 적고 매번 필요하므로 eager로 패치
    @Enumerated(EnumType.STRING)
    private Set<AccountRole> roles;
}
