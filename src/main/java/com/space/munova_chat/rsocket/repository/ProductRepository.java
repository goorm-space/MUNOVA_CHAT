package com.space.munova_chat.rsocket.repository;

import com.space.munova_chat.rsocket.entity.Product;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ProductRepository extends ReactiveCrudRepository<Product, Long> {
}
