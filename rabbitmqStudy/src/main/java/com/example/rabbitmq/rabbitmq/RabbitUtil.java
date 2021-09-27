package com.example.rabbitmq.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class RabbitUtil {
    static final String TOPIC_EXCHANGE_NAME = "order-direct";
    static final String QUEUE_NAME = "order-queue";
    static final String ROUTING_KEY = "order-queue";
    static final long DELAY_TIME = 5 * 60 * 1000;
}
