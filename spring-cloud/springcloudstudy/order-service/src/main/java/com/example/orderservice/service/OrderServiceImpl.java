package com.example.orderservice.service;

import com.example.orderservice.domain.Order;
import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{

    private final OrderRepository repository;
    private final ModelMapper modelMapper;

    @Override
    public OrderDto createOrder(OrderDto orderDto) {
        orderDto.setOrderId(UUID.randomUUID().toString());
        orderDto.setTotalPrice(orderDto.getQty() * orderDto.getUnitPrice());
        Order order = modelMapper.map(orderDto, Order.class);
        Order savedOrder = repository.save(order);
        OrderDto dto = modelMapper.map(savedOrder, OrderDto.class);
        return dto;
    }

    @Override
    public OrderDto getOrderByOrderId(String orderId) {
        Order order = repository.findByOrderId(orderId);
        OrderDto dto = modelMapper.map(order, OrderDto.class);
        return dto;
    }

    // 계층관계 전달은 dto
    // 내생각에는 dto로 변환해서 리턴하는게 맞음
    @Override
    public List<Order> getOrdersByUserId(String userId) {
        return repository.findByUserId(userId);

    }

}
