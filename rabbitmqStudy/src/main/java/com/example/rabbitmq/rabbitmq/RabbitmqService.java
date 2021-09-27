package com.example.rabbitmq.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RabbitmqService {

    private final RabbitTemplate rabbitTemplate;


    public void sendMessage() throws IOException {
        Coupon coupon = Coupon.builder()
                .id(1L)
                .text("text")
                .build();
        Reward reward = Reward.builder()
                .id(1L)
                .amount(3000)
                .build();

        Member member = Member.builder()
                .coupon(coupon)
                .reward(reward)
                .name("member")
                .age(20)
                .build();

        rabbitTemplate.convertAndSend(RabbitUtil.TOPIC_EXCHANGE_NAME, RabbitUtil.ROUTING_KEY, member,
                new MessagePostProcessor() {
                    @Override
                    public Message postProcessMessage(Message message) throws AmqpException {
                        MessageProperties props = message.getMessageProperties();
                        props.setHeader("x-delay",RabbitUtil.DELAY_TIME);
                        return message;
                    }
                }
        );


    }
}
