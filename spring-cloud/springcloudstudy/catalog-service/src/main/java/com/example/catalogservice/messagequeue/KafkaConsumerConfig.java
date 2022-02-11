package com.example.catalogservice.messagequeue;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
// kafka 접속 정보 설정
public class KafkaConsumerConfig {

    @Bean
    //kafka의 소비자로서의 설정 접속 정보
    public ConsumerFactory<String,String> consumerFactory(){
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"172.18.0.101:9092"); // kafka 서버 주소
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,"consumerGroupId"); // kafka에서 토픽에 쌓여있는 메시지를 가져가는 컨슈머를 그룹핑할 수 있는데
        // 지금은 하나의 컨슈머가 메시지를 가져가기 때문에 별 상관은 없지만, 여러개의 컨슈머가 데이터를 가져간다면 특정한 컨슈머 그룹을 만들어놓고
        // 전달하고자 하는 그룹을 만들 수가 있음
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(properties);
    }

    @Bean
    // kafka에서 변화발생시 알아차리는 설정
    public ConcurrentKafkaListenerContainerFactory<String,String> kafkaListenerContainerFactory(){
        ConcurrentKafkaListenerContainerFactory<String,String> kafkaListenerContainerFactory
                = new ConcurrentKafkaListenerContainerFactory<>();
        kafkaListenerContainerFactory.setConsumerFactory(consumerFactory()); // 소비자 설정 접속 정보 저장
        return kafkaListenerContainerFactory;
    }
}
