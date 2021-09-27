package com.example.catalogservice.messagequeue;

import com.example.catalogservice.domain.Catalog;
import com.example.catalogservice.repository.CatalogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

// 컨슈머접속정보로 세팅한 컨슈머 리스너를 통해서
// 가져온 값들을 실제 db에 반영하는 서비스
@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {
    private final CatalogRepository repository;
    private final ObjectMapper objectMapper;

    // 주시할 kafka의 토픽 지정
    @KafkaListener(topics = "example-catalog-topic")
    public void updateQty(String kafkaMessage){
        log.info("kafka message : " + kafkaMessage);

        Map<Object,Object> map = new HashMap<>();
        try {
            // objectmapper은 애초에 json으로 읽거나 json으로 변환을 위한 것
            // 첫번째인자는 처리할 데이터, 이걸 어떤 타입으로 변환할지 다음인자
            // 2인자에서는 class객체, typereference 객체 가능
            // typereference는 제네릭에 원하는 타입을 주면 그냥 그 타입을 의미
            // 아마 메시지가 json으로 오는데 어쨋든 그냥 string으로 오거든
            // objectmapper로 이걸 json으로 읽어서 map 형태로 바꿔주는 코드라고 보면 될듯
            map = objectMapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {});
        } catch (JsonProcessingException e){
            e.printStackTrace();
        }

        // object니까 string으로 캐스팅
        Catalog catalog = repository.findByProductId((String) map.get("productId"));
        if (catalog != null){
            catalog.setStock(catalog.getStock()-(Integer) map.get("qty"));
            repository.save(catalog);
        }
    }
}
