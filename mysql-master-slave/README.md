# 1. DB Replication 이란?
---
데이터베이스 이중화 방식 중 하나로 __하나의 Master DB와 여러대의 Slave DB를 구성하는 방식__ 을 의미합니다.

## 사용하는 이유
![그림1](https://backtony.github.io/assets/img/post/spring/mysql/1-1.PNG)

__부하 분산__  
서비스에 사용자가 많아져 트래픽이 늘어날 경우, DB에 쿼리를 날리는 일이 빈번하게 일어납니다. DB에서는 쿼리를 모두 처리하기 힘들어지게 되고 이에 따라 부하를 줄이기 위해 DB를 이중화하여 Master에서는 쓰기/수정/삭제 연산을 처리하고 Slave에서는 읽기 연산만을 처리하여 병목 현상을 줄일 수 있습니다.
<br>

__데이터 백업__  
Master의 데이터가 날아가더라도 Slave에 데이터가 저장되어 있으므로 어느정도 복구할 수 있습니다. MySQL Replication은 비동기 방식이기 때문에 100% 정합성을 보장할 수 없습니다.

## MySQL Replication 동작 원리
![그림2](https://backtony.github.io/assets/img/post/spring/mysql/1-2.PNG)

1. 클라이언트(Application)에서 Commit 을 수행한다.
2. Connection Thead 는 스토리지 엔진에게 해당 트랜잭션에 대한 Prepare(Commit 준비)를 수행한다.
3. Commit 을 수행하기 전에 먼저 Binary Log 에 변경사항을 기록한다.
4. 스토리지 엔진에게 트랜잭션 Commit 을 수행한다.
5. Master Thread 는 시간에 구애받지 않고(비동기적으로) Binary Log 를 읽어서 Slave 로 전송한다.
6. Slave 의 I/O Thread 는 Master 로부터 수신한 변경 데이터를 Relay Log 에 기록한다. (기록하는 방식은 Master 의 Binary Log 와 동일하다)
7. Slave 의 SQL Thread 는 Relay Log 에 기록된 변경 데이터를 읽어서 스토리지 엔진에 적용한다.

<Br>

# 2. 다중 AZ 배포
---
![그림3](https://backtony.github.io/assets/img/post/spring/mysql/1-3.PNG)

다중 AZ 배포 방식은 Amazon RDS가 다른 가용 영역에 __동기식 예비 복제본__ 을 자동으로 프로비저닝하고, DB 인스턴스 장애나 가용 영역 장애가 발생할 경우 Amazon RDS가 자동으로 예비 복제본에 장애 조치를 수행해 예비 복제본이 __마스터로 승격__ 되게 하는 관리하는 방식입니다.  
다중 AZ 배포의 경우, 동기식이기 때문에 데이터의 정합성을 보장할 수 있지만 복제본의 경우 읽기 작업을 할 수 없습니다. 이는 가용성을 위한 것이지 부하 분산을 통한 성능 향상을 위한 것이 아니기 때문입니다.
<br>


# 3. RDS 생성하기
---
![그림4](https://backtony.github.io/assets/img/post/spring/mysql/1-4.PNG)

다중 AZ 배포 방식과 Replication을 함께 사용하면 서로의 장점을 이용할 수 있습니다.  
위 그림처럼 마스터는 AZ배포 방식으로 복제본을 만들어 주고, 마스터의 Replication을 따로 만들어주도록 구성하면 됩니다.
<Br><Br>

![그림5](https://backtony.github.io/assets/img/post/spring/mysql/1-5.PNG)  
기본적인 RDS를 만들면 됩니다. 다중 AZ 배포 옵션은 프리티어에서 제공하지 않으므로 개발/테스트를 선택해줍니다.
<br><Br>


![그림6](https://backtony.github.io/assets/img/post/spring/mysql/1-6.PNG)  
AZ 옵션을 활성화 시켜줍니다.
<Br><Br>

![그림7](https://backtony.github.io/assets/img/post/spring/mysql/1-7.PNG)  
테스트 용이므로 퍼블릭 액세스를 허용해줍니다.
<Br><Br>

![그림8](https://backtony.github.io/assets/img/post/spring/mysql/1-8.PNG)  
방금 생성한 DB를 선택하시고 읽기 전용 복제본을 생성해줍니다. 기본 옵션으로 진행하면 되고 복제본 생성에서는 AZ 옵션을 꺼주시고, 퍼플릭 엑세스를 허용해줍니다.
<br><Br>

# 4. Spring에 적용하기
---
## 구성
+ @Transactional(readOnly = true) 인 경우는 Slave DB 접근
+ @Transactional(readOnly = false) 인 경우에는 Master DB 접근

## XXApplication
```java
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class MysqltestApplication {
    public static void main(String[] args) {
        SpringApplication.run(MysqltestApplication.class, args);
    }
}
```
DataSource를 직접 설정해야하기 때문에 Spring에서 DataSourceAutoConfiguration 클래스를 제외해야합니다.


## application.yml
```yml
spring:
  datasource:
    url: jdbc:mysql://test-master.cvowj9xkrrgz.ap-northeast-2.rds.amazonaws.com:3306/test?useSSL=false&useUnicode=true&characterEncoding=utf8
    slave-list:
      - name: slave_1
        url: jdbc:mysql://test-replica-1.cvowj9xkrrgz.ap-northeast-2.rds.amazonaws.com/test?useSSL=false&useUnicode=true&characterEncoding=utf8
      - name: slave_2
        url: jdbc:mysql://test-replica-2.cvowj9xkrrgz.ap-northeast-2.rds.amazonaws.com/test?useSSL=false&useUnicode=true&characterEncoding=utf8
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: test
    password: testtest

  jpa:
    defer-datasource-initialization: true
    database-platform: org.hibernate.dialect.MySQL8Dialect
    hibernate:
      ddl-auto: update
      naming:
        physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
    properties:
      hibernate:
        format_sql: true
    open-in-view: false
    show-sql: true

logging:
  level:
    com.zaxxer.hikari: INFO
```
slave-list를 적어준 부분이고 이는 자동 설정이 아니며, 코드상에 사용할 값들입니다.  
스프링 자동 설정을 제외하고 직접 세팅하는 작업을 진행하기 때문에 추가적인 작업들이 몇가지 필요합니다.  
스프링 자동 설정 중 테이블 네이밍 설정이 빠져있기 때문에 테이블 네이밍 설정을 해줘야 합니다. 이 설정이 위의 naming 옵션입니다.  
이외에는 기본적인 기본적인 MySQL DB 설정입니다.

## DbProperty.java
```java
@Getter @Setter @Component
@ConfigurationProperties("spring.datasource")
public class DbProperty {

    private String url;
    private List<Slave> slaveList;

    private String driverClassName;
    private String username;
    private String password;

    @Getter @Setter
    public static class Slave {
        private String name;
        private String url;
    }
}
```
앞서 yml에 명시해줬던 값들을 주입받아서 사용하는 클래스입니다.

## ReplicationRoutingCircularList.java
```java
public class ReplicationRoutingCircularList<T> {
    private List<T> list;
    private static Integer counter = 0;

    public ReplicationRoutingCircularList(List<T> list) {
        this.list = list;
    }

    public T getOne() {
        int circularSize = list.size();
        if (counter + 1 > circularSize) {
            counter = 0;
        }
        return list.get(counter++ % circularSize);
    }
}
```
여러개의 Replication DB의 DataSource를 순서대로 로드밸런싱 하기 위해 사용하는 클래스입니다.

## ReplicationRoutingDataSource.java
```java
public class ReplicationRoutingDataSource extends AbstractRoutingDataSource {

    private ReplicationRoutingCircularList<String> replicationRoutingDataSourceNameList;

    @Override
    public void setTargetDataSources(Map<Object, Object> targetDataSources) {
        super.setTargetDataSources(targetDataSources);

        replicationRoutingDataSourceNameList = new ReplicationRoutingCircularList<>(
                targetDataSources.keySet()
                        .stream()
                        .filter(key -> key.toString().contains("slave"))
                        .map(Object::toString)
                        .collect(toList()));
    }

    @Override
    protected Object determineCurrentLookupKey() {
        boolean isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        if (isReadOnly) {
            return replicationRoutingDataSourceNameList.getOne();
        }
        return "master";
    }
}
```
여러개의 DataSource를 묶고 필요에 따라 분기처리를 하기 위해 AbstractRoutingDataSource클래스를 사용합니다.  
setTargetDataSources 에 의해서 모든 데이터소스는 부모 생성자에 넘기고, determineCurrentLookupKey에서 사용할 replicationRoutingDataSourceNameList 를 키가 slave를 포함하는 것들로 구성해 줍니다. (yml에서 작성한 slave-list의 name들이 들어가게 됩니다.)  
determineCurrentLookupKey 메서드에서 현재 트랜잭션이 readOnly일 시 slave db의 키를, 아닐 시 master db의 DataSource의 키를 리턴하도록 작성해줍니다.

## DbConfig.java
설정에 필요한 부가적인 것들은 모두 만들었으니 이제 최종적으로 이제 최종적으로 DataSource, TransactionManager, EntityManagerFactory를 설정해야합니다.
```java
@Configuration
@RequiredArgsConstructor
public class DbConfig {

    private final DbProperty dbProperty;
    private final JpaProperties jpaProperties;

    // 바로 아래 routingDataSource 에서 사용할 메서드
    public DataSource createDataSource(String url) {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(url);
        hikariDataSource.setDriverClassName(dbProperty.getDriverClassName());
        hikariDataSource.setUsername(dbProperty.getUsername());
        hikariDataSource.setPassword(dbProperty.getPassword());
        return hikariDataSource;
    }

    @Bean
    public DataSource routingDataSource() {
        // 앞서 AbstractRoutingDataSource 를 상속받아 재정의한 ReplicationRoutingDataSource 생성
        ReplicationRoutingDataSource replicationRoutingDataSource = new ReplicationRoutingDataSource();

        // master와 slave 정보를 키(name), 밸류(dataSource) 형식으로 Map에 저장
        Map<Object, Object> dataSourceMap = new LinkedHashMap<>();
        DataSource masterDataSource = createDataSource(dbProperty.getUrl());
        dataSourceMap.put("master", masterDataSource);
        dbProperty.getSlaveList().forEach(slave -> {
            dataSourceMap.put(slave.getName(), createDataSource(slave.getUrl()));
        });

        // ReplicationRoutingDataSource의 replicationRoutingDataSourceNameList 세팅 -> slave 키 이름 리스트 세팅
        replicationRoutingDataSource.setTargetDataSources(dataSourceMap);

        // 디폴트는 Master 로 설정
        replicationRoutingDataSource.setDefaultTargetDataSource(masterDataSource);
        return replicationRoutingDataSource;
    }

    @Bean
    public DataSource dataSource() {
        // 아래서 설명
        return new LazyConnectionDataSourceProxy(routingDataSource());
    }


     // JPA 에서 사용할 entityManager 설정
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();

        entityManagerFactoryBean.setDataSource(dataSource());
        entityManagerFactoryBean.setPackagesToScan("com.example.mysqltest");        
        entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);
        
        // 스프링 JPA를 사용하면 hibernate naming 전략이 snake case로 설정됩니다.
        // 하지만 자동설정을 못하니 naming 전략이 camel case로 설정 되어 실행되므로 snake 전략으로 설정해 줍니다.
        Map<String, String> properties = jpaProperties.getProperties();
        properties.put("hibernate.physical_naming_strategy", "org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy");        
        entityManagerFactoryBean.setJpaPropertyMap(properties);

        return entityManagerFactoryBean;
    }

    // JPA 에서 사용할 TransactionManager 설정
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager tm = new JpaTransactionManager();
        tm.setEntityManagerFactory(entityManagerFactory);
        return tm;
    }

    // jdbc Template 빈 등록
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
```
다른 내용은 간단하게 주석으로 적어놨으므로 LazyConnectionDataSourceProxy에 대한 설명만 진행하겠습니다.  
기본적으로 Spring은 @Transactional을 만나면 다음 순서로 처리를 진행합니다.

> transactionManager 선별 -> Datasource에서 connection 획득 -> transaction 동기화


하지만 transaction 동기화가 먼저 되고 ReplicationRoutingDataSource에서 커넥션을 획득해야만 지금까지 한 설정을 사용할 수 있습니다. 이는  ReplicationRoutingDataSource.java를 LazyConnectionDataSoruceProxy로 감싸주어 해결할 수 있습니다.  
LazyConnectionDataSoruceProxy는 실질적인 쿼리 실행 여부와 상관없이 트랜잭션이 걸리면 무조건 Connection 객체를 확보하는 Spring의 단점을 보완하여 트랜잭션 시작시에 Connection Proxy 객체를 리턴하고 실제로 쿼리가 발생할 때 데이터소스에서 getConnection()을 호출하는 역할을 합니다. 따라서 다음과 같이 동작하게 됩니다.

> TransactionManager 선별 -> LazyConnectionDataSourceProxy에서 Connection Proxy 객체 획득 -> Transaction 동기화(Synchronization) -> 실제 쿼리 호출시에 ReplicationRoutingDataSource.getConnection().determineCurrentLookupKey() 호출

<br>

# 5. 테스트해보기
---
__Member.java__
```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int age;
}
```
<Br>

__MemberRepository.java__
```java
public interface MemberRepository extends JpaRepository<Member,Long> {}
```
<Br>

__MemberService.java__
```java
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public void findMember(){
        List<Member> all = memberRepository.findAll();
        for (Member member : all) {
            System.out.println("member = " + member);
        }
    }

    public void saveMember(){
        Member member = Member.builder()
                .age(26)
                .name("test")
                .build();
        memberRepository.save(member);
    }
}
```
<Br>

__MemberController.java__
```java
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/save")
    public void save(){
        memberService.saveMember();
    }

    @GetMapping("/find")
    public void find(){
        memberService.findMember();
    }
}
```
<br>

__DB 테이블 생성__
```java
CREATE TABLE `member` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `age` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```
직접 세팅해주었기 때문에 yml에 ddl 설정을 추가하여 동작하게 하는 방식도 따로 세팅해줘야 하는 것 같습니다. 이 부분에 대해서는 아직 부족해서 테이블을 직접 생성했습니다.  
스프링에서 제공하는 자동 설정에 어떤 것들이 있는지는 org.springframework.boot.autoconfigure.orm.jpa 해당 패키지 아래 jpa, hibernate autoconfiguration에서 확인할 수 있습니다.  
<br>

AbstractRoutingDataSource 의 224번 줄에 있는 determineTargetDataSource 에 브레이크포인트를 찍어주고 디버그로 돌린 뒤 save와 find 요청을 보내보겠습니다.
![그림9](https://backtony.github.io/assets/img/post/spring/mysql/1-9.PNG)  
<Br>

![그림10](https://backtony.github.io/assets/img/post/spring/mysql/1-10.PNG)

save의 경우 master, find의 경우 replica가 오는 것을 확인할 수 있습니다.



<Br><Br>



[[http://cloudrain21.com/mysql-replication](http://cloudrain21.com/mysql-replication){:target="_blank"}] 
[[https://www.bespinglobal.com/techblog-rds-20180627/](https://www.bespinglobal.com/techblog-rds-20180627/){:target="_blank"}] 
[[https://velog.io/@kingcjy/Spring-Boot-JPA-DB-Replication-%EC%84%A4%EC%A0%95%ED%95%98%EA%B8%B0](https://velog.io/@kingcjy/Spring-Boot-JPA-DB-Replication-%EC%84%A4%EC%A0%95%ED%95%98%EA%B8%B0){:target="_blank"}] 
[[http://kwon37xi.egloos.com/m/5364167](http://kwon37xi.egloos.com/m/5364167){:target="_blank"}] 
{:.note title="참고"}