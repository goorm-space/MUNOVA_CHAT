package com.space.munova_chat.rsocket.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
@Component
public class RoomSessionManager {

    // chatId -> Sessions
    private final Map<Long, Set<RSocketRequester>> roomSessions = new ConcurrentHashMap<>();
    // userId -> Sessions
    private final Map<Long, Set<RSocketRequester>> userSessions = new ConcurrentHashMap<>();
    // RSocketRequester -> userId
    private final Map<RSocketRequester, Long> requesterUserMap = new ConcurrentHashMap<>();
    // RSocketRequester -> chatId
    private final Map<RSocketRequester, Set<Long>> requesterChatMap = new ConcurrentHashMap<>();

    // connect
    public void onConnect(RSocketRequester requester) {
        requester.rsocket()
                .onClose()
                .doFinally(signalType -> cleanUp(requester))
                .subscribe();
    }

    // chat join
    public void joinChat(Long chatId, Long userId, RSocketRequester requester) {
        // userId -> requester
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                .add(requester);
        log.info("Join chat with id {} and user id {}", chatId, userId);

        // chatId -> requester
        roomSessions.computeIfAbsent(chatId, k -> ConcurrentHashMap.newKeySet())
                .add(requester);

        // requester -> chatId
        requesterChatMap.computeIfAbsent(requester, k -> ConcurrentHashMap.newKeySet())
                .add(chatId);

        log.info("roomSessions raw = {}", roomSessions);
        log.info("userSessions raw = {}", userSessions);
        log.info("requesterChatMap raw = {}", requesterChatMap);
    }

    // leave chat
    public void leaveChat(Long chatId, Long userId, RSocketRequester requester) {
        Set<RSocketRequester> roomSet = roomSessions.get(chatId);
        if (roomSet != null) {
            roomSet.remove(requester);
        }

        Set<Long> chats = requesterChatMap.get(requester);
        if (chats != null) {
            chats.remove(chatId);
        }
    }

    // clean up -> onClose
    public void cleanUp(RSocketRequester requester) {
        Set<Long> chatIds = requesterChatMap.remove(requester);
        if (chatIds != null) {
            for (Long chatId : chatIds) {
                Set<RSocketRequester> roomSet = roomSessions.get(chatId);
                if (roomSet != null) {
                    roomSet.remove(requester);
                }
            }
        }

        Long userId = requesterUserMap.remove(requester);
        if (userId != null) {
            Set<RSocketRequester> userSet = userSessions.get(userId);
            if (userSet != null) {
                userSet.remove(requester);
            }
        }
    }

    // broadCast 용도 -> chatId에 연결된 Session 목록 조회
    public Set<RSocketRequester> getRoomSessions(Long chatId) {
        return roomSessions.getOrDefault(chatId, Set.of());
    }

    public void registerRequester(RSocketRequester requester, Long userId) {
        requesterUserMap.put(requester, userId);
    }
}

