package com.example.apigatewayservice.filter;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

    private final Environment env;

    public AuthorizationHeaderFilter(Environment env) {
        super(Config.class); // config 사용하려면 config 정보를 필터에 적용할 수 있는 부가적 정보를 부모에 알려줘야함
        this.env = env;
    }

    public static class Config{

    }

    @Override
    public GatewayFilter apply(Config config) {
        //custom pre filter
        return (exchange, chain) -> {
            // 헤더에서 토큰 빼와서 검증
            ServerHttpRequest request = exchange.getRequest();

            // 헤더에 authorization 관련값 없으면
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)){
                return onError(exchange,"no authorization header", HttpStatus.UNAUTHORIZED);
            }
            String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);// 반환값 배열이라 0번째 꺼냄
            String jwt = authorizationHeader.replace("Bearer", "");// Bearer 빈문자로 교체

            String userId = request.getHeaders().get("userId").get(0); // 헤더의 userId값 뽑기


            // 토큰 불일치
            if (!isJwtValid(jwt,userId)){
                return onError(exchange,"no authorization header", HttpStatus.UNAUTHORIZED);
            }


            // pre 필터만 추가해서 리턴
            return chain.filter(exchange);
        };
    }

    private boolean isJwtValid(String jwt, String userId) {
        boolean returnValue = true;

        String subject = null;
        try {
            // jwt토큰 decode
            subject = Jwts.parser()
                    .setSigningKey(env.getProperty("token.secret")) // 암호화했던 코드
                    .parseClaimsJws(jwt).getBody() // 복호화 대상
                    .getSubject(); // 추출

        } catch (Exception e){
            returnValue=false; // 파싱도중 에러 false
        }
        if(subject==null || subject.isEmpty()){
            returnValue=false; // 비어있거나 널이면 false
        }
        // 복화화한 subject(userid)와 헤더로 들어온 userid 일치하는지 확인
        if (!isEqual(subject,userId))
            returnValue=false;

        return returnValue; // 문제가 없다면 true반환 문제 있으면 false반환

    }

    private boolean isEqual(String subject, String userId) {
        return subject.equals(userId);
    }


    // apigatewayservice는 spring cloud gateway service는 기존에 알던 mvc로 구성되지 않음
    // servlet이 아님
    // spring webflux로 비동기로 데이터를 처리함
    // webflux 비동기로 데이터 처리 2가지 단위중 하나가 mono로 단일 값이라고 보면 된다.
    // Mono가 단일값, 단일값이 아닌 형태는 Flux
    // spring 5.0 webflux에서 나오는 새로운 개념으로
    // 클라이언트 요청이 왔을때 반환시켜주는 데이터 타입으로 보면 된다.
    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);

        log.error(error);
        return response.setComplete(); // setComplete라는 메소드를 호출하여 해당 응답 종료
    }
}













