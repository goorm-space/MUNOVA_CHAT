package com.space.munova_chat.rsocket.service;

import com.space.munova_chat.rsocket.dto.*;
import com.space.munova_chat.rsocket.entity.MemberRole;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ChatService {

    Mono<OneToOneChatResponseDto> createOneToOneChatRoom(Long productId, Long memberId);

    Flux<ChatItemDto> getOneToOneChatRoomsByMember(Long memberId);

    Flux<ChatItemDto> getOneToOneChatRoomsBySeller(Long memberId, MemberRole role);

    Mono<ChatInfoResponseDto> setChatRoomClosed(Long chatId, Long memberId, MemberRole role);

    Mono<GroupChatInfoResponseDto> createGroupChatRoom(GroupChatRequestDto requestDto, Long memberId);

    Flux<GroupChatInfoResponseDto> searchGroupChatRooms(String keyword, List<Long> tagIds, Boolean isMine, Long memberId);

    Flux<GroupChatInfoResponseDto> getMyGroupChatRooms(Long memberId);

    Mono<GroupChatInfoResponseDto> updateGroupChatInfo(Long chatId, GroupChatUpdateRequestDto updateDto, Long memberId);

    Mono<Void> joinGroupChat(Long chatId, Long memberId);

    Mono<Void> leaveGroupChat(Long chatId, Long memberId);

    Mono<Void> closeGroupChat(Long chatId, Long memberId);

    Mono<Void> openGroupChat(Long chatId, Long memberId);

    Mono<GroupChatDetailResponseDto> getGroupChatDetail(Long chatId, Long memberId);
}
