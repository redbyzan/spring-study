package com.webservicestudy.webservicestudy.modules.account;

import com.webservicestudy.webservicestudy.modules.study.Study;
import com.webservicestudy.webservicestudy.modules.tag.Tag;
import com.webservicestudy.webservicestudy.modules.zone.Zone;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
// equal hash 사용할 때 id값만 사용
// id만 사용하는 이유 : 연관관계가 복잡해질때 equalshash코드에서
// 서로 다른 연관관계를 순환참조하느라 무한 루프가 발생하고 stackoverflow가 발생할 수 있기 때문
@Getter @Setter @EqualsAndHashCode(of ="id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Account {

    @Id @GeneratedValue
    private Long id;

    // 이메일, 닉네임으로 로그인 할수 있도록 지원
    // 중복되면 안된다.

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    /**
     *  이메일 인증 절차
     */
    // 이메일 인증 절차 만들 것이기에
    // 현재 이메일 인증이 되었는지 확인 하기 위함
    private boolean emailVerified;

    // 이메일 검증에 사용할 토큰값
    private String emailCheckToken;

    // 인증 거친 사용자들은 그때 가입이 된것으로 기록
    private LocalDateTime joinedAt;

    /**
     * 기본적인 프로필 정보들
     */


    // 자기 소개
    private String bio;

    // 블로그 url
    private String url;

    // 직업
    private String occupation;

    // 지역
    private String location;

    /**
     *  이미지
     */
    // String은 기본적으로 varchar(255) 타입으로 지정되는데
    // 사진의 경우는 더 많은 자리수를 요구하므로
    // Large Object인 @Lob를 사용하면 적절하게 저장된다.

    @Lob
    private String profileImage;


    /**
     * 알림 설정
     */

    // 스터디 완성 정보 이메일로 받을 것인가
    private boolean studyCreatedByEmail;

    // 스터디 완성 정보 웹으로 받을 것인가
    private boolean studyCreatedByWeb;

    // 가입 신청 결과 이메일으로 받을 것인가
    private boolean studyEnrollmentResultByEmail;

    // 가입 신청 결과 웹으로 받을 것인가
    private boolean studyEnrollmentResultByWeb;

    // 스터디 갱신 정보 이메일로 받을 것인가
    private boolean studyUpdatedByEmail;

    // 스터디 갱신 정보 웹으로 받을 것인가
    private boolean studyUpdatedByWeb;

    private LocalDateTime emailTokenGeneratedAt;

    // tag와의 관계, list, set 둘중에 선호하는 것 사용
    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();


    public void generateEmailCheckToken() {
        // 랜덤값 부여
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailTokenGeneratedAt = LocalDateTime.now();
    }

    public void completeSignUp() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();

    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    public boolean canSendConfirmEmail() {
        return true; //this.emailTokenGeneratedAt.isBefore(LocalDateTime.now().minusMinutes(3)); // todo change
    }


}
