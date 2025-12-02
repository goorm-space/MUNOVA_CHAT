package com.space.munova_chat.rsocket.dto;

import com.space.munova_chat.rsocket.entity.Chat;
import com.space.munova_chat.rsocket.entity.ChatTag;
import com.space.munova_chat.rsocket.enums.ChatStatus;

import java.time.LocalDateTime;
import java.util.List;

public record GroupChatInfoResponseDto(
        Long chatId,
        String name,
        Integer maxParticipant,
        Integer currentParticipant,
        ChatStatus status,
        LocalDateTime createdAt,
        List<String> productCategoryList
) {
    public static GroupChatInfoResponseDto of(Chat chat, List<ChatTag> tags) {
        List<String> descriptions = (tags == null ? List.<ChatTag>of() : tags)
                .stream()
                .filter(c -> c != null) // null 제거
                .map(ChatTag::getCategoryType)
                .toList();

        return new GroupChatInfoResponseDto(
                chat.getId(), chat.getName(), chat.getMaxParticipant(), chat.getCurParticipant(), chat.getStatus(), chat.getCreatedAt(), descriptions
        );
    }
}