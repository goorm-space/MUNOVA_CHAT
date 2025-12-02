package com.space.munova_chat.rsocket.repository;

import com.space.munova_chat.rsocket.entity.Chat;
import com.space.munova_chat.rsocket.enums.ChatStatus;
import com.space.munova_chat.rsocket.enums.ChatType;
import com.space.munova_chat.rsocket.enums.ChatUserType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatRepository extends ReactiveCrudRepository<Chat, Long> {

    @Query("""
                SELECT c.*
                FROM chat c
                JOIN chat_member cm ON cm.chat_id = c.chat_id
                WHERE cm.member_id = :memberId
                    AND c.type = :chatType
                    AND c.status = :chatStatus
                    AND cm.chat_member_type = :chatUserType
                ORDER BY COALESCE(c.last_message_time, c.created_at) DESC
            """)
    Flux<Chat> findChatByTypeAndStatus(Long memberId, ChatType chatType, ChatUserType chatUserType, ChatStatus chatStatus);


    Mono<Boolean> existsByName(String name);


    @Query(""" 
                SELECT c.*
                FROM chat c
                WHERE c.chat_id = : chatId
                    AND c.type = :chatType
                    AND c.status = 'OPENED'
            """)
    Mono<Chat> findChatByIdAndType(
            @Param("chatId") Long chatId,
            @Param("chatType") ChatType chatType);


}
