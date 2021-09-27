package com.webservicestudy.webservicestudy.modules.account;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;


// 기본적으로 jparepository에는 transactional이 붙어있다.
// 여기서 내가 만들어준 메서드에도 transaction 처리를 하기 위해서는 애노테이션을 붙여줘야 한다
// repository에서 querydsl의 predicateexecutor에서 제공하는 기능을 추가,
@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<Account,Long>, QuerydslPredicateExecutor<Account> {
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Account findByEmail(String email);

    Account findByNickname(String nickname);

    @EntityGraph(attributePaths = {"tags", "zones"})
    Account findAccountWithTagsAndZonesById(Long id);
}
