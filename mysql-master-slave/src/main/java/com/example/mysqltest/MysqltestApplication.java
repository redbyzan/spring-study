package com.example.mysqltest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
//@SpringBootApplication
public class MysqltestApplication {

    public static void main(String[] args) {
        SpringApplication.run(MysqltestApplication.class, args);
    }

}
