package com.space.munova_chat.rsocket.service;

import com.space.munova_chat.rsocket.dto.*;
import com.space.munova_chat.rsocket.entity.*;
import com.space.munova_chat.rsocket.enums.ChatStatus;
import com.space.munova_chat.rsocket.enums.ChatType;
import com.space.munova_chat.rsocket.enums.ChatUserType;
import com.space.munova_chat.rsocket.enums.ProductCategory;
import com.space.munova_chat.rsocket.exception.ChatException;
import com.space.munova_chat.rsocket.exception.MemberException;
import com.space.munova_chat.rsocket.exception.ProductException;
import com.space.munova_chat.rsocket.repository.r2dbc.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final ChatRepository chatRepository;
    private final ChatTagRepository chatTagRepository;
    private final ChatRepositoryCustom chatRepositoryCustom;

    /// 구매자의 1:1 문의 채팅방 생성
    @Override
    public Mono<OneToOneChatResponseDto> createOneToOneChatRoom(Long productId, Long memberId) {
        return getMember(memberId)
                .zipWith(
                        productRepository.findById(productId)
                                .switchIfEmpty(Mono.error(ProductException.notFoundProductException("productId : " + productId)))
                ).flatMap(tuple -> {
                    Member buyer = tuple.getT1();
                    Product product = tuple.getT2();
                    Long sellerId = product.getMemberId();

                    if (sellerId.equals(memberId)) {
                        return Mono.error(ChatException.notAllowedToCreateChatWithSelf("sellerId : " + sellerId));
                    }

                    return chatMemberRepository.findExistingChatRoom(memberId, productId)
                            .flatMap(chatMember ->
                                    Mono.just(OneToOneChatResponseDto.of(chatMember.getChatId(), buyer.getId(), sellerId)))
                            .switchIfEmpty(createOneToOneChatRoom(buyer, sellerId, product));
                });
    }

    /// 구매자가 참여중인 1:1 문의 채팅방 조회
    @Override
    public Flux<ChatItemDto> getOneToOneChatRoomsByMember(Long memberId) {
        return chatRepository.findChatByTypeAndStatus(
                        memberId, ChatType.ONE_ON_ONE, ChatUserType.MEMBER, ChatStatus.OPENED)
                .map(ChatItemDto::of);
    }

    /// 판매자가 참여중인 1:1 문의 채팅방 조회
    @Override
    public Flux<ChatItemDto> getOneToOneChatRoomsBySeller(Long memberId, MemberRole role) {

        if (role != MemberRole.SELLER) {
            return Flux.error(ChatException.unauthorizedParticipantException("role : " + role));
        }

        return chatRepository.findChatByTypeAndStatus(
                        memberId, ChatType.ONE_ON_ONE, ChatUserType.OWNER, null)
                .map(ChatItemDto::of);
    }

    /// 판매자의 1:1 문의 채팅방 비활성화(OPENED -> CLOSED)
    @Override
    public Mono<ChatInfoResponseDto> setChatRoomClosed(Long chatId, Long memberId, MemberRole role) {

        if (role != MemberRole.SELLER) {
            return Mono.error(ChatException.unauthorizedParticipantException("role : " + role));
        }

        return getChatMemberOrThrow(chatId, memberId, ChatUserType.OWNER, ChatType.ONE_ON_ONE)
                .map(ChatMember::getChatId)
                .flatMap(chatRepository::findById)
                .switchIfEmpty(Mono.error(ChatException.cannotFindChatException("chatId : " + chatId)))
                .flatMap(chat ->
                        chatRepository.save(chat.updateChatStatus(ChatStatus.CLOSED)))
                .map(ChatInfoResponseDto::of);
    }

    /// Group 채팅방 생성
    @Override
    public Mono<GroupChatInfoResponseDto> createGroupChatRoom(GroupChatRequestDto requestDto, Long memberId) {
        return getMember(memberId)
                .flatMap(member ->
                        chatRepository.existsByName(requestDto.chatName())
                                .flatMap(exists -> exists
                                        ? Mono.error(ChatException.duplicateChatNameException("chatName : " + requestDto.chatName()))
                                        : Mono.just(member)
                                )
                )
                .flatMap(member ->
                        chatRepository.save(
                                Chat.createChat(requestDto.chatName(), ChatStatus.OPENED, ChatType.GROUP, null, 1, requestDto.maxParticipants())
                        ).map(chat -> new ChatWithMember(chat, member))
                )
                .flatMap(tuple -> saveTagsAndOwner(tuple.chat(), tuple.member(), requestDto))
                .map(chatWithTags -> GroupChatInfoResponseDto.of(chatWithTags.chat(), chatWithTags.tags()));
    }


    ///  그룹 채팅방 검색
    @Override
    public Flux<GroupChatInfoResponseDto> searchGroupChatRooms(String keyword, List<Long> tagIds, Boolean isMine, Long memberId) {
        List<Long> safeList = (tagIds == null) ? List.of() : tagIds;
        return chatRepositoryCustom.searchGroupChats(keyword, safeList, memberId, isMine)
                .flatMap(this::convertToInfoDto);
    }

    ///  내가 생성한 그룹 채팅방 리스트 확인
    @Override
    public Flux<GroupChatInfoResponseDto> getMyGroupChatRooms(Long memberId) {
        return chatRepository.findChatByTypeAndStatus(memberId, ChatType.GROUP, ChatUserType.OWNER, null)
                .flatMap(this::convertToInfoDto);
    }

    ///  내가 생성한 그룹 채팅방 정보 수정(최대 인원, 상태 변경)
    @Override
    public Mono<GroupChatInfoResponseDto> updateGroupChatInfo(Long chatId, GroupChatUpdateRequestDto updateDto, Long memberId) {
        return getChatMemberOrThrow(chatId, memberId, ChatUserType.OWNER, ChatType.GROUP)
                .flatMap(cm -> chatRepository.findById(chatId)
                        .switchIfEmpty(Mono.error(ChatException.cannotFindChatException("chatId : " + chatId))))
                .flatMap(chat -> {
                    chat.updateInfo(updateDto);

                    List<Long> newCategoryIds = Optional.ofNullable(updateDto.productCategoryId())
                            .orElse(List.of());

                    if (newCategoryIds.isEmpty()) {
                        return chatRepository.save(chat).flatMap(this::convertToInfoDto);
                    }

                    return updateChatTags(chat, updateDto.productCategoryId())
                            .then(chatRepository.save(chat))
                            .flatMap(this::convertToInfoDto);
                });
    }

    ///  특정 그룹 채팅방 참여
    @Override
    public Mono<Void> joinGroupChat(Long chatId, Long memberId) {
        return getMember(memberId)
                .flatMap(member ->
                        chatRepository.findChatByIdAndType(chatId, ChatType.GROUP)
                                .switchIfEmpty(Mono.error(ChatException.invalidChatRoomException("chatId : " + chatId)))
                                .map(chat -> new ChatWithMember(chat, member))
                )
                .flatMap(chatWithMember -> {  // ← 여기서 부터가 하나의 flatMap
                    Chat chat = chatWithMember.chat();
                    Member mem = chatWithMember.member();

                    return chatMemberRepository.existsMemberInChat(chatId, mem.getId())
                            .flatMap(cnt -> {
                                if (cnt > 0)
                                    return Mono.error(ChatException.alreadyJoinedChat("chatId : " + chatId));   // 이미 참여중이면 종료

                                chat.incrementParticipant();
                                return chatRepository.save(chat).then(
                                        chatMemberRepository.save(ChatMember.createChatMember(chat.getId(), mem.getId(), ChatUserType.MEMBER, mem.getUsername()))
                                ).then();
                            });
                });
    }

    @Override
    public Mono<Void> leaveGroupChat(Long chatId, Long memberId) {
        return getChatMemberOrThrow(chatId, memberId, ChatUserType.MEMBER, ChatType.GROUP)
                .flatMap(cm -> chatRepository.findById(chatId)
                        .switchIfEmpty(Mono.error(ChatException.cannotFindChatException("chatId : " + chatId)))
                        .map(chat -> new ChatWithChatMember(chat, cm)))
                .flatMap(ccm -> {
                    Chat chat = ccm.chat();
                    ChatMember chatMember = ccm.member();

                    chat.decrementParticipant();

                    return chatRepository.save(chat)
                            .then(chatMemberRepository.delete(chatMember))
                            .then();
                });
    }

    @Override
    public Mono<Void> closeGroupChat(Long chatId, Long memberId) {
        return getChatMemberOrThrow(chatId, memberId, ChatUserType.OWNER, ChatType.GROUP)
                .flatMap(cm -> chatRepository.findById(chatId)
                        .switchIfEmpty(Mono.error(ChatException.cannotFindChatException("chatId : " + chatId))))
                .flatMap(chat ->
                        chatRepository.save(chat.updateChatStatus(ChatStatus.CLOSED)))
                .then();
    }

    @Override
    public Mono<Void> openGroupChat(Long chatId, Long memberId) {
        return getChatMemberOrThrow(chatId, memberId, ChatUserType.OWNER, ChatType.GROUP)
                .flatMap(cm -> chatRepository.findById(chatId)
                        .switchIfEmpty(Mono.error(ChatException.cannotFindChatException("chatId : " + chatId))))
                .flatMap(chat ->
                        chatRepository.save(chat.updateChatStatus(ChatStatus.OPENED)))
                .then();

    }

    @Override
    public Mono<GroupChatDetailResponseDto> getGroupChatDetail(Long chatId, Long memberId) {

        return chatMemberRepository.existsMemberInChat(chatId, memberId)
                .flatMap(cnt -> {
                    log.info("cnt : " + cnt);
                    log.info("cnt : " + cnt.getClass());
                    if (cnt > 0) {
                        return Mono.empty();
                    } else {
                        return Mono.error(ChatException.unauthorizedParticipantException("chatId : " + chatId));
                    }
                })
                .then(
                        Mono.zip(
                                chatRepository.findById(chatId)
                                        .switchIfEmpty(Mono.error(ChatException.cannotFindChatException("chatId=" + chatId))),

                                chatTagRepository.findAllByChatId(chatId)
                                        .map(t -> ProductCategory.findById(t.getProductCategoryId()).getDescription())
                                        .collectList(),

                                chatMemberRepository.findByChatId(chatId)
                                        .map(MemberInfoDto::of)
                                        .collectList()
                        )
                )
                .map(tuple -> GroupChatDetailResponseDto.of(
                        tuple.getT1(),
                        tuple.getT2(),
                        tuple.getT3()
                ));
    }


    private Mono<Void> updateChatTags(Chat chat, List<Long> newCategoryIds) {
        List<ChatTag> newTags = newCategoryIds.stream()
                .map(id -> ChatTag.createChatTag(chat.getId(), id, ProductCategory.findById(id).getDescription()))
                .toList();

        return chatTagRepository.deleteByChatId(chat.getId())
                .then(chatTagRepository.saveAll(newTags).then());
    }

    private Mono<GroupChatInfoResponseDto> convertToInfoDto(Chat chat) {
        return chatTagRepository.findAllByChatId(chat.getId())
                .collectList()
                .map(tags -> GroupChatInfoResponseDto.of(chat, tags));
    }

    private Mono<ChatWithTags> saveTagsAndOwner(Chat chat, Member member, GroupChatRequestDto requestDto) {
        List<Long> categories = Optional.ofNullable(requestDto.productCategoryId())
                .orElse(List.of());

        List<ChatTag> tags = categories.stream()
                .map(cid -> ChatTag.createChatTag(chat.getId(), cid, ProductCategory.findById(cid).getDescription()))
                .toList();

        return chatTagRepository.saveAll(tags)
                .collectList()
                .flatMap(saved -> addOwnerMember(chat.getId(), member.getId(), chat.getName())
                        .thenReturn(new ChatWithTags(chat, saved)));
    }

    private Mono<OneToOneChatResponseDto> createOneToOneChatRoom(Member buyer, Long sellerId, Product product) {

        return chatRepository.save(Chat.createChat(generateChatRoomName(product.getName(), buyer.getUsername()), ChatStatus.OPENED, ChatType.ONE_ON_ONE, product.getId(), 2, 2))
                .flatMap(cm ->
                        chatMemberRepository.save(
                                ChatMember.createChatMember(cm.getId(), sellerId, ChatUserType.OWNER, null)
                        ).then(
                                chatMemberRepository.save(
                                        ChatMember.createChatMember(cm.getId(), buyer.getId(), ChatUserType.MEMBER, buyer.getUsername()))
                        ).thenReturn(
                                OneToOneChatResponseDto.of(cm.getId(), buyer.getId(), sellerId)
                        )
                );
    }

    public Mono<Member> getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .switchIfEmpty(Mono.error(MemberException.notFoundException("memberId : " + memberId)));
    }

    private String generateChatRoomName(String productName, String buyerName) {
        return productName + " - " + buyerName;
    }

    private Mono<ChatMember> getChatMemberOrThrow(Long chatId, Long memberId, ChatUserType type, ChatType chatType) {
        return chatMemberRepository.findChatMember(chatId, memberId, chatType, type)
                .switchIfEmpty(Mono.error(ChatException.unauthorizedParticipantException("chatId : " + chatId)));
    }

    private Mono<ChatMember> addOwnerMember(Long chatId, Long memberId, String name) {
        return chatMemberRepository.save(ChatMember.createChatMember(chatId, memberId, ChatUserType.OWNER, name));
    }

    private record ChatWithTags(Chat chat, List<ChatTag> tags) {
    }

    private record ChatWithMember(Chat chat, Member member) {
    }

    private record ChatWithChatMember(Chat chat, ChatMember member) {
    }

}
