package com.example.cleancode.controller;


import com.example.cleancode.constants.ActionType;
import com.example.cleancode.policy.BrokerPolicy;
import com.example.cleancode.policy.BrokeragePolicyFactory;
import com.example.cleancode.policy.PurchaseBrokeragePolicy;
import com.example.cleancode.policy.RentBrokeragePolicy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author backtony
 *
 * 중개수수료가 얼마인지 조회하는 컨트롤러
 */

@RestController
public class BrokerageQueryController {
    @GetMapping("/api/calc/brokerage")
    public Long calcBrokerage(@RequestParam ActionType actionType,
                              @RequestParam Long price){

        BrokerPolicy policy = BrokeragePolicyFactory.of(actionType);
        return policy.calculate(price);
    }
}
