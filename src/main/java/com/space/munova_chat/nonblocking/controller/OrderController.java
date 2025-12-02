package com.space.munova_chat.nonblocking.controller;

import com.space.munova_chat.nonblocking.dto.OrderReqDto;
import com.space.munova_chat.nonblocking.order.Orders;
import com.space.munova_chat.nonblocking.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    // 주문 생성
    @PostMapping
    public Mono<Long> saveOrder(@Valid @RequestBody OrderReqDto dto) {
        return orderService.saveOrder(dto);
    }

    @GetMapping("/{id}")
    public Mono<Orders> findOrder(@PathVariable Long id) {
        return orderService.findOrder(id);
    }

}
