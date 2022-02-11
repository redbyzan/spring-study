package com.example.orderservice.messagequeue;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
// kafka 접속 정보 설정
public class KafkaProducerConfig {

    @Bean
    //kafka의 producer로서의 설정 접속 정보
    public ProducerFactory<String,String> producerFactory(){
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"172.18.0.101:9092"); // kafka 서버 주소
        // 지금은 하나의 컨슈머가 메시지를 가져가기 때문에 별 상관은 없지만, 여러개의 컨슈머가 데이터를 가져간다면 특정한 컨슈머 그룹을 만들어놓고
        // 전달하고자 하는 그룹을 만들 수가 있음
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    // 애는 데이터 보내는 거니까 리스너가 아니라 template이 필요
    public KafkaTemplate<String,String> kafkaTemplate(){
        return new KafkaTemplate<>(producerFactory());
    }

}
