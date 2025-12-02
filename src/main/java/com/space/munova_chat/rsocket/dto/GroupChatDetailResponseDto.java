package com.space.munova_chat.rsocket.dto;

import com.space.munova_chat.rsocket.entity.Chat;
import com.space.munova_chat.rsocket.enums.ChatStatus;

import java.time.LocalDateTime;
import java.util.List;

public record GroupChatDetailResponseDto(
        Long chatId,
        String name,
        Integer maxParticipant,
        Integer currentParticipant,
        ChatStatus status,
        LocalDateTime createdAt,
        List<String> productCategoryList,
        List<MemberInfoDto> memberList
) {
    public static GroupChatDetailResponseDto of(Chat chat, List<String> chatTags, List<MemberInfoDto> members) {
        return new GroupChatDetailResponseDto(chat.getId(), chat.getName(), chat.getMaxParticipant(), chat.getCurParticipant(), chat.getStatus(), chat.getCreatedAt(), chatTags, members);
    }
}
