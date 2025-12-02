package com.space.munova_chat.rsocket.dto;

public record OneToOneChatResponseDto(
        Long chatId,
        Long sellerId, // 판매자
        Long buyerId  // 구매자
) {
    public static OneToOneChatResponseDto of(Long chatId, Long buyer, Long seller) {
        return new OneToOneChatResponseDto(chatId, seller, buyer);
    }
}
