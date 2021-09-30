package com.example.cleancode.controller;


import com.example.cleancode.constants.ActionType;
import com.example.cleancode.policy.BrokerPolicy;
import com.example.cleancode.policy.BrokeragePolicyFactory;
import com.example.cleancode.policy.PurchaseBrokeragePolicy;
import com.example.cleancode.policy.RentBrokeragePolicy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BrokerageQueryController {
    // 중개 수수료 가격 반환
    // 매매인지, 임대차인지 타입을 받는다.

    @GetMapping("/api/calc/brokerage")
    public Long calcBrokerage(@RequestParam ActionType actionType,
                              @RequestParam Long price){

        BrokerPolicy policy = BrokeragePolicyFactory.of(actionType);
        return policy.calculate(price);
    }
}
