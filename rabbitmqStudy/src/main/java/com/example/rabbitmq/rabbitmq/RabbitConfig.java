package com.example.rabbitmq.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig {


    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    Queue queue() {
        return new Queue(RabbitUtil.QUEUE_NAME, false);
    }

    @Bean
    CustomExchange exchange() {
        Map<String,Object> headers = new HashMap<>();
        headers.put("x-delayed-type","direct");
        return new CustomExchange(RabbitUtil.TOPIC_EXCHANGE_NAME,"x-delayed-message",true,false,headers);
    }

    @Bean
    Binding binding(Queue queue, CustomExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(RabbitUtil.ROUTING_KEY).noargs();
    }
}
