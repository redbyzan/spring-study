package com.example.apigatewayservice.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {
    public LoggingFilter() {
        super(Config.class);
    }

//    // 작동하고자 하는 내용
//    // apply 메서드는 gatewayfilter을 반환시켜줌으로서 어떠한 작업을 할지 정의가능
    @Override
    public GatewayFilter apply(Config config) {
//        //custom pre filter
//        return (exchange, chain) -> {
//            // 동기방식의 톰켓이 아니라 비동기방식의 netty 내장서버이므로
//            // servlet이 아니라 server
//            ServerHttpRequest request = exchange.getRequest();
//            ServerHttpResponse response = exchange.getResponse();
//            log.info("logging filter basemessage : {}",config.getBaseMessage());
//            if(config.isPreLogger()){
//                log.info("logging filter start :request id -> {}",request.getId());
//            }
//
//            // 처리가 다 끝난 다음에는 custom post filter 추가 가능
//            // mono 객체는 webflux라고 비동기방식의 서버 지원에서 단일값 전달에서 사용
//            return chain.filter(exchange).then(Mono.fromRunnable(()-> {
//                        if (config.isPostLogger()) {
//                            log.info("logging filter end :response code -> {}", response.getStatusCode());
//                        }
//                    }
//            ));
//        };
        // 위에는 필터를 만들고 반환한 건데 그냥 람다로 한줄로 표현한거임

        //OrderedGatewayFilter 는 gatewayfilter을 구현시키는 자식 클래스
        // 들어가서 살펴보면
        // 생성자 파라미터로 gatewayfilter와 order , gatewayfilter는 인터페이스임
        // filter 메서드와 상수들이 있는데 filter만 재정의하여 익명 구현 객체 만들 수 있음
        // 중요한건 filter라는 메서드
        // fiter라는 메서드를 재정의함으로 필터가 해야하는 역할을 정의
        // 파라미터로 serverwebexchange, gatewayfilterchain을 받는다.
        // webflux를 사용하는 방식이다
        // 기존에 spring은 mvc 패턴을 이용해서 servlet response request 객체를 가지고 작업했다.
        // webflux에서는 더이상 위를 지원하지 않는다.
        // serverresponse와 serverrequest 두가지 인스턴스를 가지고 작업을 해야한다.
        // 그러한 res,req를 사용할수 있도록 도와주는게 ServerWebExchange라는 객체
        // 이 객체로부터 res, rep 가져올수있음
        // getwayfilterchain 이라는 객체가 해주는 역할은,
        // 다양한 필터들, pre post 필터들은 연결시켜서 작업할수 있게 해주는 역할
        GatewayFilter filter = new OrderedGatewayFilter((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            log.info("logging filter basemessage : {}",config.getBaseMessage());
            if(config.isPreLogger()){
                log.info("logging pre filter :request id -> {}",request.getId());
            }

            // 처리가 다 끝난 다음에는 custom post filter 추가 가능
            // mono 객체는 webflux라고 비동기방식의 서버 지원에서 단일값 전달에서 사용
            return chain.filter(exchange).then(Mono.fromRunnable(()-> {
                        if (config.isPostLogger()) {
                            log.info("logging post filter  :response code -> {}", response.getStatusCode());
                        }
                    }
            ));
        }, Ordered.LOWEST_PRECEDENCE); // 우선순위 조정

        // 이렇게 만든거랑 똑같음 람다식이라 위에처럼 가능한것 메서드가 한개이니 파라미터만 맞춰서 만들면 생성되는듯
//        GatewayFilter filter = new OrderedGatewayFilter(new GatewayFilter() {
//            @Override
//            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//                return null;
//            }
//        }, Ordered.HIGHEST_PRECEDENCE)

        //

        return filter;
    }

    @Data
    public static class Config{
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;

    }
}
