package com.webservicestudy.webservicestudy.modules.study;

import com.webservicestudy.webservicestudy.modules.account.Account;
import com.webservicestudy.webservicestudy.modules.account.UserAccount;
import com.webservicestudy.webservicestudy.modules.tag.Tag;
import com.webservicestudy.webservicestudy.modules.zone.Zone;
import lombok.*;

import javax.persistence.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

//@NamedEntityGraph(name = "Study.withAll", attributeNodes = {
//        @NamedAttributeNode("tags"),
//        @NamedAttributeNode("zones"),
//        @NamedAttributeNode("managers"),
//        @NamedAttributeNode("members")})
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Study {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToMany
    private Set<Account> managers = new HashSet<>();

    @ManyToMany
    private Set<Account> members = new HashSet<>();

    @Column(unique = true)
    private String path;

    private String title;

    private String shortDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String fullDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String image;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;

    private LocalDateTime recruitingUpdatedDateTime;

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    private boolean useBanner;

    private int memberCount;

    public void addManager(Account account) {
        this.managers.add(account);
    }

    public boolean isJoinable(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        return this.isPublished() && this.isRecruiting()
                && !this.members.contains(account) && !this.managers.contains(account);

    }

    public boolean isMember(UserAccount userAccount) {
        return this.members.contains(userAccount.getAccount());
    }

    public boolean isManager(UserAccount userAccount) {
        return this.managers.contains(userAccount.getAccount());
    }

    public void publish() {
        if(!closed && !published){
            published = true;
            publishedDateTime = LocalDateTime.now();
        } else{
            throw new RuntimeException("스터디를 공개할 수 없는 상태입니다. 스터디를 이미 공개했거나 종료했습니다.");
        }
    }

    public String getImage() {
        return image != null ? image : "/images/default_banner.png";
    }

    public void close() {
        if(published && !closed){
            closed = true;
            closedDateTime = LocalDateTime.now();
        } else{
            throw new RuntimeException("스터디를 종료할 수 없습니다. 스터디를 공개하지 않았거나 이미 종료한 스터디입니다.");
        }
    }

    public boolean canUpdateRecruiting() {
        return published && recruitingUpdatedDateTime == null || recruitingUpdatedDateTime.isBefore(LocalDateTime.now().minusHours(1));
    }

    public void startRecruit() {
        if (canUpdateRecruiting()){
            recruitingUpdatedDateTime = LocalDateTime.now();
            recruiting = true;
        } else{
            throw new RuntimeException("인원 모집을 시작할 수 없습니다. 스터디를 공개하거나 한 시간 뒤 다시 시도하세요.");
        }
    }

    public void stopRecruit() {
        if (canUpdateRecruiting()) {
            this.recruiting = false;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("인원 모집을 멈출 수 없습니다. 스터디를 공개하거나 한 시간 뒤 다시 시도하세요.");
        }
    }

    public boolean isRemovable() {
        return !published;
    }
    public String getEncodedPath() {
        return URLEncoder.encode(this.path, StandardCharsets.UTF_8);
    }


    public void removeMember(Account account) {
        this.getMembers().remove(account);
        this.memberCount--;
    }

    public boolean isManagedBy(Account account) {
        return getManagers().contains(account);
    }

    public void addMember(Account account) {
        this.getMembers().add(account);
        this.memberCount++;
    }
}
