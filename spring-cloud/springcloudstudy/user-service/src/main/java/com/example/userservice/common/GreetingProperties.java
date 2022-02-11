package com.example.userservice.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("greeting")
@Getter
@AllArgsConstructor
public class GreetingProperties {
    private String message;
}
