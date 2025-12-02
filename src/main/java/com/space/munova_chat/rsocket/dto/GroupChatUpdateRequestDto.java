package com.space.munova_chat.rsocket.dto;

import java.util.List;

public record GroupChatUpdateRequestDto(
        String name,

        Integer maxParticipants,

        List<Long> productCategoryId
) {
    public static GroupChatUpdateRequestDto of(String name, Integer maxParticipants, List<Long> productCategoryId) {
        return new GroupChatUpdateRequestDto(name, maxParticipants, productCategoryId);
    }
}
