package com.space.munova_chat.rsocket.repository;

import com.space.munova_chat.rsocket.entity.ChatMember;
import com.space.munova_chat.rsocket.enums.ChatStatus;
import com.space.munova_chat.rsocket.enums.ChatType;
import com.space.munova_chat.rsocket.enums.ChatUserType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatMemberRepository extends ReactiveCrudRepository<ChatMember, Long> {

    @Query("""
                SELECT cm.*
                FROM chat_member cm
                JOIN chat c ON cm.chat_id = c.chat_id
                WHERE cm.member_id = :memberId
                    AND c.product_id = :productId
                    AND c.status = 'OPENED'
                LIMIT 1
            """)
    Mono<ChatMember> findExistingChatRoom(Long memberId, Long productId);


    @Query("""
                SELECT cm.*
                FROM chat_member cm
                JOIN chat c ON cm.chat_id = c.chat_id
                WHERE cm.chat_id = :chatId
                    AND cm.member_id = :memberId
                    AND c.type = :chatType
                    AND cm.chat_member_type = :chatUserType
                    AND (:chatStatus IS NULL OR c.status = :chatStatus)
            """)
    Mono<ChatMember> findChatMember(Long chatId, Long memberId, ChatStatus status, ChatType chatType, ChatUserType chatUserType);

    // 참여자 여부 확인 (메시지 전송/조회용)
    @Query("""
                SELECT COUNT(cm) > 0
                FROM chat_member cm
                WHERE cm.chat_id = :chatId
                    AND cm.member_id = :memberId
            """)
    Mono<Boolean> existsMemberInChat(Long chatId, Long memberId);

    Flux<ChatMember> findByChatId(Long chatId);
}
