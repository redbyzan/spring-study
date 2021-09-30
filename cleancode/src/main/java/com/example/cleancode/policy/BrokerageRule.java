package com.example.cleancode.policy;


import lombok.AllArgsConstructor;
import org.springframework.lang.Nullable;

/**
 * 가격이 특정 범위일 때 상한효율과 상한 금액이 가지는 클래스
 */
@AllArgsConstructor
public class BrokerageRule {
    private Double brokeragePercent;

    @Nullable // null이 들어올 수 있다는 주석 annotation
    private Long limitAmount;

    public Long cacMaxBrokerage(Long price){
        if (limitAmount == null){
            return multiplyPercent(price);
        }
        return Math.min(multiplyPercent(price),limitAmount);
    }

    private long multiplyPercent(Long price) {
        return Double.valueOf(Math.floor(brokeragePercent / 100 * price)).longValue();
    }
}
