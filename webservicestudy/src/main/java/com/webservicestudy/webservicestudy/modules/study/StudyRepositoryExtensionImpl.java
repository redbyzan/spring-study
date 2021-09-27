package com.webservicestudy.webservicestudy.modules.study;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQuery;
import com.webservicestudy.webservicestudy.modules.account.QAccount;
import com.webservicestudy.webservicestudy.modules.tag.QTag;
import com.webservicestudy.webservicestudy.modules.tag.Tag;
import com.webservicestudy.webservicestudy.modules.zone.QZone;
import com.webservicestudy.webservicestudy.modules.zone.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.Set;

import static com.webservicestudy.webservicestudy.modules.study.QStudy.*;
import static com.webservicestudy.webservicestudy.modules.tag.QTag.*;
import static com.webservicestudy.webservicestudy.modules.zone.QZone.zone;

// QueryDslRepositorySupport 는 Querydsl 을 사용할 수 있도록 해준다.
public class StudyRepositoryExtensionImpl extends QuerydslRepositorySupport implements StudyRepositoryExtension{

    // QuerydslRepositorySupport는 기본생성자가 없고 인자가 있는 생성자가 있음 -> 그래서 인자 넣어줘야함함
    // intellij의 도움에 의해서 생성자를 자동으로 만들었는데 사실 우리는 어떤 도메인 타입을 다룰건지 이미 정했음 ->Study
    // 따라서 파라미터로 받을 필요도 없이 그냥 알고있는 도메인을 넣으면 된다.
   public StudyRepositoryExtensionImpl() {
        super(Study.class);
    }

    @Override
    public Page<Study> findByKeyword(String keyword, Pageable pageable) {
        QStudy study = QStudy.study;
        // select 생략시 기본적으로 from의 첫번째 엔티티가 프로젝션의 대상
        JPQLQuery<Study> query = from(study).where(study.published.isTrue()
                .and(study.title.containsIgnoreCase(keyword)) // 소문자대문자무시
                .or(study.tags.any().title.containsIgnoreCase(keyword)) // tag들중 어느 하나라도 keyword포함
                .or(study.zones.any().localNameOfCity.containsIgnoreCase(keyword)))
                .leftJoin(study.tags, tag).fetchJoin()
                .leftJoin(study.zones, zone).fetchJoin()
                .leftJoin(study.members, QAccount.account).fetchJoin() // 몇명 있는지도 화면에 띄움
                // 현재 일다다 관계로 조인하면 데이터 뻥튀기된다. -> 페이징할거 아니니까 distinct 사용
                .distinct()
                ;

        //QuerydslRepositorySupport가 제공함 -> pageable과 쿼리를 넣으면 페이징이 적용된 쿼리를 반환
        JPQLQuery<Study> pageableQuery = getQuerydsl().applyPagination(pageable, query);
        QueryResults<Study> fetchResults = pageableQuery.fetchResults();// 그냥 fetch는 데이터만, 페이징 포함은 fectchResult
// 페이징된 값을 반환할 것이니 반환타입도 Page

        return new PageImpl<>(fetchResults.getResults(),pageable, fetchResults.getTotal());

    }


    // 사용자 계정의 tag과 zone을 이용해서 연관된 스터디 뽑아서 반환하면 된다. 리미트 몇개인지는 나중에 확인
//    @Override
//    public List<Study> findByAccount(Set<Tag> tags, Set<Zone> zones) {
//        JPQLQuery<Study> query = from(study)
//                .where(study.tags.any().in(tags)
//                        .and(study.zones.any().in(zones))
//                        .and(study.closed.isFalse())
//                        .and(study.published.isTrue()))
//                .leftJoin(study.tags, tag).fetchJoin()
//                .leftJoin(study.zones, zone).fetchJoin()
//                .orderBy(study.publishedDateTime.desc())
//                .distinct()
//                .limit(9);
//        return query.fetch();
//
//    }
    @Override
    public List<Study> findByAccount(Set<Tag> tags, Set<Zone> zones) {
        QStudy study = QStudy.study;
        JPQLQuery<Study> query = from(study).where(study.published.isTrue()
                .and(study.closed.isFalse())
                .and(study.tags.any().in(tags))
                .and(study.zones.any().in(zones)))
                .leftJoin(study.tags, QTag.tag).fetchJoin()
                .leftJoin(study.zones, QZone.zone).fetchJoin()
                .orderBy(study.publishedDateTime.desc())
                .distinct()
                .limit(9);
        return query.fetch();
    }
}

















