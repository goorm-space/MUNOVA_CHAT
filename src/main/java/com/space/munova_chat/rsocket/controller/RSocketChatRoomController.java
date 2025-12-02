package com.space.munova_chat.rsocket.controller;

import com.space.munova_chat.rsocket.core.ResponseApi;
import com.space.munova_chat.rsocket.dto.*;
import com.space.munova_chat.rsocket.entity.MemberRole;
import com.space.munova_chat.rsocket.jwt.JwtHelper;
import com.space.munova_chat.rsocket.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class RSocketChatRoomController {

    private final JwtHelper jwtHelper;
    private final ChatService chatService;

    ///  구매자의 1:1 문의 채팅방 생성
    @PostMapping("/one-to-one/{productId}")
    public Mono<ResponseEntity<ResponseApi<OneToOneChatResponseDto>>> createOneToOne(
            @PathVariable Long productId,
            Authentication authentication) {

        Long memberId = (Long) authentication.getPrincipal();

        return chatService.createOneToOneChatRoom(productId, memberId)
                .map(dto -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(ResponseApi.created(dto)));
    }

    ///  구매자가 현재 참여중인 1:1 문의 채팅방 목록 확인
    @GetMapping("/one-to-one")
    public Mono<ResponseApi<List<ChatItemDto>>> getBuyerChatRooms(Authentication authentication) {

        Long memberId = (Long) authentication.getPrincipal();

        return chatService.getOneToOneChatRoomsByMember(memberId)
                .collectList()
                .map(ResponseApi::ok);
    }

    ///  판매자가 현재 참여 중인 1:1 문의 채팅방 확인
    @GetMapping("/seller/one-to-one")
    public Mono<ResponseApi<List<ChatItemDto>>> getSellerChatRooms(Authentication authentication) {

        Long memberId = (Long) authentication.getPrincipal();

        return chatService.getOneToOneChatRoomsBySeller(memberId)
                .collectList()
                .map(ResponseApi::ok);
    }

    ///  판매자 현재 1:1 문의 채팅방 비활성화
    @PostMapping("/seller/{chatId}")
    public Mono<ResponseApi<ChatInfoResponseDto>> setChatClosed(
            @PathVariable Long chatId,
            Authentication authentication) {

        Long memberId = (Long) authentication.getPrincipal();
        MemberRole role = (MemberRole) authentication.getAuthorities().iterator().next();

        return chatService.setChatRoomClosed(chatId, memberId, role)
                .map(ResponseApi::ok);
    }

    ///  그룹 채팅방 생성
    @PostMapping("/group")
    public Mono<ResponseApi<GroupChatInfoResponseDto>> createGroupChatRoom(
            @RequestBody @Valid GroupChatRequestDto requestDto,
            Authentication authentication) {

        Long memberId = (Long) authentication.getPrincipal();
        return chatService.createGroupChatRoom(requestDto, memberId)
                .map(ResponseApi::ok);
    }


    ///  그룹 채팅방 조건 검색
    @GetMapping("/group/search")
    public Mono<ResponseApi<List<GroupChatInfoResponseDto>>> searchGroupChatRooms(
            @RequestParam(required = false, name = "keyword") String keyword,
            @RequestParam(required = false, name = "tagIds") List<Long> tagIds,
            @RequestParam(defaultValue = "false", name = "isMine") Boolean isMine,
            Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        return chatService.searchGroupChatRooms(keyword, tagIds, isMine, memberId)
                .collectList()
                .map(ResponseApi::ok);
    }


    ///  내가 생성한 그룹 채팅방 확인
    @GetMapping("/group/owner")
    public Mono<ResponseApi<List<GroupChatInfoResponseDto>>> getMyGroupChatRooms(Authentication authentication) {

        Long memberId = (Long) authentication.getPrincipal();
        return chatService.getMyGroupChatRooms(memberId)
                .collectList()
                .map(ResponseApi::ok);
    }

    ///  그룹 채팅방 정보 변경 (이름, 최대 참여자, 태그)
    @PatchMapping("/group/{chatId:\\d+}")
    public Mono<ResponseApi<GroupChatInfoResponseDto>> updateGroupChatRoom(
            @PathVariable Long chatId, @RequestBody GroupChatUpdateRequestDto updateDto, Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        return chatService.updateGroupChatInfo(chatId, updateDto, memberId)
                .map(ResponseApi::ok);
    }


    ///  그룹 채팅방 참여
    @PostMapping("/group/{chatId:\\d+}")
    public Mono<ResponseApi<Void>> joinGroupChat(
            @PathVariable Long chatId, Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();

        return chatService.joinGroupChat(chatId, memberId)
                .thenReturn(ResponseApi.ok());
    }

    ///  그룹채팅 채팅방 나가기
    @PostMapping("/group/leave/{chatId}")
    public Mono<ResponseApi<Void>> leaveGroupChatRoom(@PathVariable Long chatId, Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();

        return chatService.leaveGroupChat(chatId, memberId)
                .thenReturn(ResponseApi.ok());
    }

    ///  그룹채팅(Owner) 채팅방 비활성화
    @PatchMapping("/group/close/{chatId}")
    public Mono<ResponseApi<Void>> closeGroupChat(@PathVariable Long chatId, Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        return chatService.closeGroupChat(chatId, memberId)
                .thenReturn(ResponseApi.ok());
    }

    ///  그룹채팅(Owner) 채팅방 비활성화
    @PatchMapping("/group/open/{chatId}")
    public Mono<ResponseApi<Void>> openGroupChat(@PathVariable Long chatId, Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        return chatService.openGroupChat(chatId, memberId)
                .thenReturn(ResponseApi.ok());
    }

    // 그룹채팅방 상세 조회 API
    @GetMapping("/group/{chatId}")
    public Mono<ResponseApi<GroupChatDetailResponseDto>> getGroupChatDetail(
            @PathVariable Long chatId, Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        return chatService.getGroupChatDetail(chatId, memberId)
                .map(ResponseApi::ok);
    }
}
