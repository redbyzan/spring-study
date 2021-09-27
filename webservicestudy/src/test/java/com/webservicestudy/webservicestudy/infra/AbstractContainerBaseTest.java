package com.webservicestudy.webservicestudy.infra;

import org.testcontainers.containers.PostgreSQLContainer;

// 도커 test 컨테이너 띄우는 작업
// 테스트 db를 새로 만들지 않고 도커를 사용하는 이유
// 테스트를 실행하는 환경에서도 db를 새로 setup해야하고 운영해야하는 것은 너무 번거로움
// 테스트를 실행할 때 컨테이너를 실행해주는 dependency를 사용하면 db를 따로 운영하지 않아도 된다. 도커는 당연히 띄워나야함
public abstract class AbstractContainerBaseTest {

    static final PostgreSQLContainer POSTGRE_SQL_CONTAINER;

    static {
        POSTGRE_SQL_CONTAINER = new PostgreSQLContainer();
        POSTGRE_SQL_CONTAINER.start();
    }

}
