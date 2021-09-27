package com.example.orderservice.repository;

import com.example.orderservice.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface OrderRepository extends JpaRepository<Order,Long> {
    Order findByOrderId(String orderId);
    List<Order> findByUserId(String userId);
}
