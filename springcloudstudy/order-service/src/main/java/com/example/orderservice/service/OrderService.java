package com.example.orderservice.service;

import com.example.orderservice.domain.Order;
import com.example.orderservice.dto.OrderDto;

import java.util.List;

public interface OrderService {
    OrderDto createOrder(OrderDto orderDto);
    OrderDto getOrderByOrderId(String orderId);
    List<Order> getOrdersByUserId(String userId);
    //List<Order> getOrdersByOrderId(String userId);
}
