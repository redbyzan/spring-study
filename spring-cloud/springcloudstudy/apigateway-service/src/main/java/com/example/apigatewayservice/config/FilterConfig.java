package com.example.apigatewayservice.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

//@Configuration
// 앞서 yml 파일에서 routing 정보를 등록했따.
// 하지만 자바 코드로도 라우팅 정보를 등록할 수 있다.
// 기존 yml 코드에서 필터까지 추가한 방식이라고 보면 된다.
// yml도 필터 추가가 가능한긴 한데 타입세이프하지가 않음 자동완성도 안되고
public class FilterConfig {
  //  @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder){
        return builder.routes()
                // 라우트 정보 추가
                // path로 들어오면 필터링해서 uri로 보낸다.
                .route(r -> r.path("/first-service/**")
                        // key value 형식
                            .filters(f -> f.addRequestHeader("first-request","first-request-value")
                            .addResponseHeader("first-response","first-response-header"))
                            .uri("http://localhost:8081"))
                .route(r -> r.path("/second-service/**")
                        // key value 형식
                        .filters(f -> f.addRequestHeader("second-request","second-request-value")
                                .addResponseHeader("second-response","second-response-header"))
                        .uri("http://localhost:8082"))
                .build();
    }
}

//exchange : 서비스 요청/응답값을 담고있는 변수로, 요청/응답값을 출력하거나 변환할 때 사용한다
