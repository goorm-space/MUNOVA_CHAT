package com.space.munova_chat.rsocket.dto;

import com.space.munova_chat.rsocket.entity.Chat;
import com.space.munova_chat.rsocket.enums.ChatStatus;

import java.time.LocalDateTime;

public record ChatInfoResponseDto(
        Long chatId,
        String name,
        int maxParticipant,
        ChatStatus status,
        LocalDateTime createdAt
) {
    public static ChatInfoResponseDto of(Chat chat) {
        return new ChatInfoResponseDto(
                chat.getId(), chat.getName(), chat.getMaxParticipant(), chat.getStatus(), chat.getCreatedAt()
        );
    }
}
