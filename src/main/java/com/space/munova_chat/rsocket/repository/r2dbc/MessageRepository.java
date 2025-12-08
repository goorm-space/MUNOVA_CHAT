package com.space.munova_chat.rsocket.repository.r2dbc;

import com.space.munova_chat.rsocket.entity.Message;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface MessageRepository extends ReactiveCrudRepository<Message, Long> {
}
