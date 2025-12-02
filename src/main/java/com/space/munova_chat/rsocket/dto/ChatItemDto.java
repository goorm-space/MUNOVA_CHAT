package com.space.munova_chat.rsocket.dto;

import com.space.munova_chat.rsocket.entity.Chat;

import java.time.LocalDateTime;

public record ChatItemDto(
        Long chatId,

        String name,

        String lastMessageContent,

        LocalDateTime lastMessageTime
) {
    public static ChatItemDto of(Long chatId, String name, String lastMessageContent, LocalDateTime lastMessageTime) {
        return new ChatItemDto(chatId, name, lastMessageContent, lastMessageTime);
    }

    public static ChatItemDto of(Chat chat) {
        return new ChatItemDto(chat.getId(), chat.getName(), chat.getLastMessageContent(), chat.getLastMessageTime());
    }
}
