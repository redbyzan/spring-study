package com.example.apigatewayservice.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class GlobalFilter extends AbstractGatewayFilterFactory<GlobalFilter.Config> {
    public GlobalFilter() {
        super(Config.class);
    }

    // 작동하고자 하는 내용
    // apply 메서드는 gatewayfilter을 반환시켜줌으로서 어떠한 작업을 할지 정의가능
    @Override
    public GatewayFilter apply(Config config) {
        //custom pre filter
        return (exchange, chain) -> {
            // 동기방식의 톰켓이 아니라 비동기방식의 netty 내장서버이므로
            // servlet이 아니라 server
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            log.info("global filter basemessage : {}",config.getBaseMessage());
            if(config.isPreLogger()){
                log.info("global filter start :request id -> {}",request.getId());
            }

            // 처리가 다 끝난 다음에는 custom post filter 추가 가능
            // mono 객체는 webflux라고 비동기방식의 서버 지원에서 단일값 전달에서 사용
            return chain.filter(exchange).then(Mono.fromRunnable(()-> {
                        if (config.isPostLogger()) {
                            log.info("global filter end :response code -> {}", response.getStatusCode());
                        }
                    }
            ));
        };
    }

    @Data
    public static class Config{
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;

    }
}
