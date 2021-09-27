package com.example.rabbitmq.rabbitmq;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class RabbitController {

    private final RabbitmqService rabbitmqService;

    @PostMapping
    public String rabbitmq() throws IOException {
        rabbitmqService.sendMessage();
        return "OK";
    }
}
