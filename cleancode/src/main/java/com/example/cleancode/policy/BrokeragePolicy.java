package com.example.cleancode.policy;

public interface BrokeragePolicy {

    default Long calculate(Long price){
        BrokerageRule rule = createRule(price);
        return rule.cacMaxBrokerage(price);
    }

    BrokerageRule createRule(Long price);
}
