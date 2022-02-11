## 소개
JPA 쿼리 생성의 다양한 방식을 정리한다.  
<br>

+ MemberRepository.class 코드만 보면 되고 아래와 같은 내용을 작성했다.
    + 네이밍 자동 완성 쿼리 방식
    + EntityGraph 방식
    + @Query 직접 사용 방식
    + @Modifying
    + 일대다 데이터 펌핑 제거
    + join fetch
+ 테스트 코드는 MemberRepositoryTest에서 확인하면 된다.    