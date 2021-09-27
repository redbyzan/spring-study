package com.apistudy.restapi.events;


import com.apistudy.restapi.accounts.Account;
import com.apistudy.restapi.accounts.AccountSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;



// 빌더를 사용하기 위해서는 기본 생성자와 모든 필드 포함한 생성자가 있어야한다.
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Setter @Getter @EqualsAndHashCode(of="id")
@Entity
public class Event {

    @Id @GeneratedValue
    private Integer id;
    private String name;
    private String description;
    private LocalDateTime beginEnrollmentDateTime;
    private LocalDateTime closeEnrollmentDateTime;
    private LocalDateTime beginEventDateTime;
    private LocalDateTime endEventDateTime;
    private String location; // (optional) 이게 없으면 온라인 모임
    private int basePrice; // (optional)
    private int maxPrice; // (optional)
    private int limitOfEnrollment;
    private boolean offline;
    private boolean free;

    @ManyToOne // 단방향
    @JsonSerialize(using = AccountSerializer.class) // json내릴때 만든 serializer 사용하도록 설정, 반드시 xml것 사용!!!!
    private Account manager;

    @Enumerated(EnumType.STRING)
    private EventStatus eventStatus = EventStatus.DRAFT;


    public void update() {
        // Update free
        if (this.basePrice == 0 && this.maxPrice == 0) {
            this.free = true;
        } else {
            this.free = false;
        }
        // Update offline
        if (this.location == null || this.location.isBlank()) {
            this.offline = false;
        } else {
            this.offline = true;
        }
    }
//    public void update() {
//        // update free
//        if (this.basePrice==0 && this.maxPrice==0){
//            this.free = true;
//        } else{
//            this.free=false;
//        }
//
//        // update offline
//        // isblank는 자바 11에서 지원하는 것 -> 빈 문자열, 공백까지 다 확인해서 비어있는지 확인
//        if(this.location == null || this.location.isBlank()){
//            this.offline=false;
//        } else{
//            this.offline=true;
//        }
//    }
}