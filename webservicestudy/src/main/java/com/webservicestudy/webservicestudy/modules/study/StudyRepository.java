package com.webservicestudy.webservicestudy.modules.study;

import com.webservicestudy.webservicestudy.modules.account.Account;
import com.webservicestudy.webservicestudy.modules.event.Event;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long>, StudyRepositoryExtension {

    boolean existsByPath(String path);


    // 성능상 튜닝 가능점이 이것임
    // 해당 study를 보는 페이지에서는 해당 study의 모든 정보가 필요하다
    // study 엔티티의 경우 XXToMany로 전부 lazy로 걸려있다.
    // 여기서는 batchsize가 의미가 없는게
    // batchsize는 여러개를 뽑아내서 거기서 뽑는것임
    // 예를 들어 findbypath로 결과로 study가 여러개 나오고
    // study의 필드 컬렉션을 모두 사용해야 하는 경우라면 첫번째의 study를 사용할때 in쿼리로 study_id로 batchsize만큼 땡겨오는 것
    // 다음 study의 것의 lazy까지 땡겨와버리는 것인데
    // 그런데 여기서는 study가 딱 하나만 나오므로 batchsize 최적화의 의미가 없다.

    // 따라서 원하는 단 하나를 조회할때 원하는 필드를 lazy가 아니라 eager로 바로 가져오는 조인 쿼리가 필요하다
    // entitiygraph를 사용하면 된다.
    // load 타입은 엔티티그래프에 명시한 연관관계는 eager로 조회, 나머지는 기본 fetch 전략에 따른다
    // fetch 타입은 명시한 것은 eager, 나머지는 다 lazy로 가져온다.
    // 기본 타입이 fetch로 fetch는 명시 안해도 된다.

    // 참고로 entitygraph를 사용하면 엔티티에 XXToOne은 lazy고 eager고 그런 기본값이 적용 안됨

    @EntityGraph(attributePaths = {"tags","zones","managers","members"}, type = EntityGraph.EntityGraphType.LOAD)
    Study findByPath(String path);

    @EntityGraph(attributePaths = {"tags","managers"},type = EntityGraph.EntityGraphType.FETCH)
    Study findTagsByPath(String path);

    @EntityGraph(attributePaths = {"zones","managers"},type = EntityGraph.EntityGraphType.FETCH)
    Study findZoneByPath(String path);

    @EntityGraph(attributePaths = {"managers"}, type = EntityGraph.EntityGraphType.FETCH)
    Study findStatusByPath(String path);

    @EntityGraph(attributePaths = {"members"}, type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithMembersByPath(String path);

    Study findStudyToEnrollByPath(String path);

    @EntityGraph(attributePaths = {"tags","zones"},type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithTagsAndZonesById(Long id);

    @EntityGraph(attributePaths = {"managers","members"},type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithManagersAndMembersById(Long id);


    @EntityGraph(attributePaths = "managers")
    Study findStudyWithManagersByPath(String path);

    @EntityGraph(attributePaths = {"zones","tags"})
    List<Study> findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(boolean published, boolean closed);

    List<Study> findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);

    List<Study> findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);
}

















