package com.space.munova_chat.nonblocking.repo;

import com.space.munova_chat.nonblocking.order.Orders;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface OrderRepo extends R2dbcRepository<Orders, Long> {
    // save, findById ... 등등


}
