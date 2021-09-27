package com.example.rabbitmq.rabbitmq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitReceiver {

    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitUtil.QUEUE_NAME)
    public void receive(Member member){
        System.out.println("member = " + member.getName());
        System.out.println("member = " + member.getAge());
        System.out.println("member.getCoupon().getText() = " + member.getCoupon().getText());
        System.out.println("member.getCoupon().getText() = " + member.getCoupon().getId());
        System.out.println("member.getReward().getId() = " + member.getReward().getId());
        System.out.println("member.getReward().getId() = " + member.getReward().getAmount());
    }

}
