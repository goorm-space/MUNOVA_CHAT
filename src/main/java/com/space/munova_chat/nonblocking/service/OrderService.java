package com.space.munova_chat.nonblocking.service;

import com.space.munova_chat.nonblocking.dto.OrderReqDto;
import com.space.munova_chat.nonblocking.order.Orders;
import com.space.munova_chat.nonblocking.repo.OrderRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {
    private final OrderRepo orderRepo;

    /// 블로킹 없이 + backpressure 튜닝

    ///  TODO : 주문 생성, 주문 조회
    public Mono<Long> saveOrder(OrderReqDto dto) {
        Orders order = new Orders(dto.orderNum(), dto.description());
        return orderRepo.save(order).map(Orders::getId);
    }

    public Mono<Orders> findOrder(Long id) {
        return orderRepo.findById(id);
    }

    ///  한 번에 여러 개 받고 한 번에 여러 개 보내기 가능
    /// back pressure의 근본
    public Flux<Orders> findAllOrders() {
        return orderRepo.findAll();
    }

}
