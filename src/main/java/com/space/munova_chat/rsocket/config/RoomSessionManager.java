package com.space.munova_chat.rsocket.config;

import com.space.munova_chat.rsocket.model.ChatMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
@Component
public class RoomSessionManager {

    // RSocketRequester -> userId
    private final Map<RSocketRequester, Long> requesterUserMap = new ConcurrentHashMap<>();
    // userId -> Sessions
    private final Map<Long, Set<RSocketRequester>> userSessions = new ConcurrentHashMap<>();
    // chatId -> Sessions
    private final Map<Long, Set<RSocketRequester>> roomSessions = new ConcurrentHashMap<>();
    // RSocketRequester -> chatId
    private final Map<RSocketRequester, Set<Long>> requesterChatMap = new ConcurrentHashMap<>();
    // chatId -> RSocketRequester : Sinks
    private final Map<Long, Map<RSocketRequester, Sinks.Many<ChatMessage>>> roomSinks1 = new ConcurrentHashMap<>();

    // connect
    public void onConnect(RSocketRequester requester) {
        requester.rsocket()
                .onClose()
                .doFinally(signalType -> cleanUp(requester))
                .subscribe();
    }

    // chat join
    public void joinChat(Long chatId, Long userId, RSocketRequester requester) {
        // RSocketRequester -> userId
        requesterUserMap.put(requester, userId);
        // user -> RSocketRequester
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                .add(requester);
        // chatId -> RSocketRequester
        roomSessions.computeIfAbsent(chatId, k -> ConcurrentHashMap.newKeySet())
                .add(requester);
        // RSocketRequester -> chatId
        requesterChatMap.computeIfAbsent(requester, k -> ConcurrentHashMap.newKeySet())
                .add(chatId);
    }

    // leave chat
    public void leaveChat(Long chatId, Long userId, RSocketRequester requester) {
        // ChatId -> RSocketRequester 제거
        Set<RSocketRequester> roomSet = roomSessions.get(chatId);
        if (roomSet != null) {
            roomSet.remove(requester);
        }

        // Requester -> ChatId 제거
        Set<Long> chats = requesterChatMap.get(requester);
        if (chats != null) {
            chats.remove(chatId);
        }

        // ChatId -> RSocket, Sink 제거
        Map<RSocketRequester, Sinks.Many<ChatMessage>> sinkMap = roomSinks1.get(chatId);
        if (sinkMap != null) {
            Sinks.Many<ChatMessage> sink = sinkMap.remove(requester);
            if (sink != null) {
                sink.tryEmitComplete(); // STREAM 정상 종료
            }
        }
    }

    // clean up -> onClose
    public void cleanUp(RSocketRequester requester) {
        log.info("🔴 Clean up requester {}", requester);

        // RSocketRequester -> ChatId 제거
        Set<Long> chatIds = requesterChatMap.remove(requester);
        if (chatIds != null) {
            for (Long chatId : chatIds) {

                // chatId -> RSocketRequester 제거
                Set<RSocketRequester> roomSet = roomSessions.get(chatId);
                if (roomSet != null) roomSet.remove(requester);

                // ChatId -> Sinks 제거
                Map<RSocketRequester, Sinks.Many<ChatMessage>> sinkMap = roomSinks1.get(chatId);
                if (sinkMap != null) {
                    Sinks.Many<ChatMessage> sink = sinkMap.remove(requester);
                    if (sink != null) sink.tryEmitComplete();
                }
            }
        }
        // RSocketRequester -> userId 제거
        Long userId = requesterUserMap.remove(requester);
        if (userId != null) {
            // userId -> RSocketRequestser 제거
            Set<RSocketRequester> userSet = userSessions.get(userId);
            if (userSet != null) userSet.remove(requester);
        }
    }

    public Sinks.Many<ChatMessage> getOrCreateSink(Long chatId, RSocketRequester requester) {

        // 채팅방 sinkMap 확보 (chatId → requester → sink)
        Map<RSocketRequester, Sinks.Many<ChatMessage>> sinkMap =
                roomSinks1.computeIfAbsent(chatId, k -> new ConcurrentHashMap<>());

        return sinkMap.computeIfAbsent(requester, r -> {
            log.info("➡️ Create personal Sink for chat {} requester {}", chatId, requester);
            return Sinks.many().unicast().onBackpressureBuffer();
        });
    }

    public void broadcast(ChatMessage chatMessage) {
        Map<RSocketRequester, Sinks.Many<ChatMessage>> sinkMap = roomSinks1.get(chatMessage.getChatId());
        if (sinkMap == null) return;

        sinkMap.values().forEach(sink -> sink.tryEmitNext(chatMessage));
    }

}

