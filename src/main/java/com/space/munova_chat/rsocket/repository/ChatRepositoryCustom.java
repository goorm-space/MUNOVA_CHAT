package com.space.munova_chat.rsocket.repository;

import com.space.munova_chat.rsocket.entity.Chat;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ChatRepositoryCustom {

    Flux<Chat> searchGroupChats(String keyword, List<Long> tagIds, Long memberId, boolean isMine);
}
