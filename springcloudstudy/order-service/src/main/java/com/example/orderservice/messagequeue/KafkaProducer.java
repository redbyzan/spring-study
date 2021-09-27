package com.example.orderservice.messagequeue;

import com.example.orderservice.dto.OrderDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
// kafka로 데이터 전달시킬 서비스
public class KafkaProducer {
    private final KafkaTemplate<String,String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // 주문서비스가 들어오면 카탈로그에는 그에 대한 주문 양만큼 차감해야함
    // 그럼 kafka에 토픽에다가 주문양을 보내줘야함
    public OrderDto send(String topic, OrderDto orderDto) {

        String value ="";
        // try catch로 묶는데 강의에서는 어쩌피 문제되면 에러 날리는데 그냥 간단하게 위에 추가해주는게 편해보여서
        // 위로 뺏는데 뺏더니 메서드 사용하는 곳 마다 족족 exception 날려줘야함
        // 그래서 그냥 try catch쓰는게 나아보임

        try {
            value = objectMapper.writeValueAsString(orderDto);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        kafkaTemplate.send(topic, value);
        log.info("kafka producer sent data from the order microservice : "+ orderDto);
        return orderDto;

    }

}
