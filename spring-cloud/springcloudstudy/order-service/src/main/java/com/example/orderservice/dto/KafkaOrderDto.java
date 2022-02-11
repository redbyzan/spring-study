package com.example.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class KafkaOrderDto {
    private Schema schema;
    private Payload payload;
}
