package com.example.orderservice.domain;

import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@Table(name = "orders") // orderby 때문에 order로 테이블 생성불가능
// 직렬화 -> 가지고 있는 객체를 다른 네트워크로 전송하기 위한 작업
public class Order implements Serializable {
    // insert문 sql넣는데 id값 널이면 안됨 -> 전략 수정으로 db에서 자동 생성하게 만들어야함
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20,unique = true)
    private String productId;

    @Column(nullable = false)
    private Integer qty;

    @Column(nullable = false)
    private Integer totalPrice;

    @Column(nullable = false)
    private Integer unitPrice;

    @Column(nullable = false)
    private String userId;
    @Column(nullable = false, unique = true)
    private String orderId;
    //업데이트, 인서트도 불가능
    @Column(nullable = false,updatable = false,insertable = false)

    @ColumnDefault(value = "CURRENT_TIMESTAMP") // DB에서 현재 시간 호출하는 함수
    private Date createdAt;
}
