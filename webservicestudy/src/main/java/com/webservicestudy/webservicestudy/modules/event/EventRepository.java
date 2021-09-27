package com.webservicestudy.webservicestudy.modules.event;

import com.webservicestudy.webservicestudy.modules.study.Study;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface EventRepository extends JpaRepository<Event,Long> {

    // 화면 뿌릴때 몇명 모집중, 몇명 마감 이런거 표시하려면 event의 enrollment에서 isaccept를 전부 확인해야함
    // 그럼 N+1 문제가 발생하므로
    // 땡겨올때 enrollment 한번에 땡겨야함
    @EntityGraph(attributePaths = {"enrollments"}, type = EntityGraph.EntityGraphType.LOAD)
    List<Event> findByStudyOrderByStartDateTime(Study study);
}
