package com.example.userservice.client;

import com.example.userservice.vo.ResponseOrder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "order-service")
public interface OrderServiceClient {

    // 그냥 feignclient에 가져오려는 애플리케이션 이름적고
    // 메서드는 완전 똑같음 기본이 public이라 안적어도 되고
    // 반환값만 resposeentity에 바디로 들어가는 값으로 바꿔주면 됨
    @GetMapping("/{userId}/orders")
    List<ResponseOrder> getOrder(@PathVariable String userId);
}
