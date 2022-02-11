package com.example.catalogservice.domain;

import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "catalog")
//말씀하신 것처럼 Serializable을 상속받는 이유는 직렬화를 위해서이고,
// 직렬화는 객체를 다른 상태로 변화하거나 전송할 때 바이트 배열의 형태로 변경해 주는 과정과 같습니다.
// 과정에서 특정 클래스에만 직렬화를 한것은 특별한 이유없이 작업된 것 같습니다.
// CatalogEntity에도 직렬화를 해 주는 것이 좋습니다.
public class Catalog implements Serializable {

    // insert문 sql넣는데 id값 널이면 안됨 -> 전략 수정으로 db에서 자동 생성하게 만들어야함
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20,unique = true)
    private String productId;
    @Column(nullable = false)
    private String productName;
    @Column(nullable = false)
    private Integer stock;
    @Column(nullable = false)
    private Integer unitPrice;

    //업데이트, 인서트도 불가능
    @Column(nullable = false,updatable = false,insertable = false)
    @ColumnDefault(value = "CURRENT_TIMESTAMP") // DB에서 현재 시간 호출하는 함수
    private LocalDateTime createdAt;
}
