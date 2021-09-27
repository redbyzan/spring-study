package com.example.orderservice.controller;

import com.example.orderservice.domain.Order;
import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.messagequeue.KafkaProducer;
import com.example.orderservice.messagequeue.OrderProducer;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.vo.RequestOrder;
import com.example.orderservice.vo.ResponseOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final ModelMapper modelMapper;
    private final Environment env;
    private final KafkaProducer kafkaProducer;
    private final OrderProducer orderProducer;

    @GetMapping("/health_check")
    public String status(){
        return "its working in order service port "+ env.getProperty("local.server.port");
    }

    @PostMapping("/{userId}/orders")
    public ResponseEntity createOrder(@RequestBody RequestOrder user, @PathVariable String userId){

        log.info("before add order data");
        OrderDto orderDto = modelMapper.map(user, OrderDto.class);
        orderDto.setUserId(userId);

        /* 바로 DB로 저장했던 작업 주석 처리*/
        OrderDto createdOrder = orderService.createOrder(orderDto);
        ResponseOrder responseOrder = modelMapper.map(createdOrder, ResponseOrder.class);

        /* kafka로 전달하기 */
        // orderservice에서 했던 작업 여기서 하고 kafka로 보내야함

//        orderDto.setOrderId(UUID.randomUUID().toString());
//        orderDto.setTotalPrice(orderDto.getQty() * orderDto.getUnitPrice());
//
//        // kafka에 전달하기
        kafkaProducer.send("example-catalog-topic",orderDto);
//        orderProducer.send("orders",orderDto);
//
//        ResponseOrder responseOrder = modelMapper.map(orderDto, ResponseOrder.class);

        log.info("after add order data");
        return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder);
    }

    @GetMapping("/{userId}/orders")
    public ResponseEntity getOrder(@PathVariable String userId){
        log.info("before retrieve order data");
        List<Order> orders = orderService.getOrdersByUserId(userId);
        List<ResponseOrder> result = new ArrayList<>();

        orders.forEach(o->{
            result.add(modelMapper.map(o,ResponseOrder.class));
        });
        log.info("after retrieve order data");
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

}
