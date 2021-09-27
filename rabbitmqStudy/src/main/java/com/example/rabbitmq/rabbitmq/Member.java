package com.example.rabbitmq.rabbitmq;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.sql.rowset.serial.SerialArray;
import java.io.Serializable;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Member implements Serializable {

    private String name;

    private int age;

    private Coupon coupon;

    private Reward reward;
}
