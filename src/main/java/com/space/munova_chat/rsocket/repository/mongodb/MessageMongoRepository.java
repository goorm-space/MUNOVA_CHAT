package com.space.munova_chat.rsocket.repository.mongodb;

import com.space.munova_chat.rsocket.entity.Message;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface MessageMongoRepository extends ReactiveMongoRepository<Message, String> {
}
