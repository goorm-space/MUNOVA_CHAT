package com.space.munova_chat.customize.config.session;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Getter
@Component
public class SessionManager {

    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<Long>> sessionSubscriptions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, CopyOnWriteArraySet<WebSocketSession>> chatRoomSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> heartbeat = new ConcurrentHashMap<>();

    // CONNECT
    public void addSession(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        sessionSubscriptions.put(sessionId, ConcurrentHashMap.newKeySet());
        heartbeat.put(sessionId, System.currentTimeMillis());
    }

    // SUBSCRIBE
    public void subscribe(String sessionId, Long chatId) {
        WebSocketSession session = sessions.get(sessionId);
        if (session == null) return;

        sessionSubscriptions.get(sessionId).add(chatId);
        chatRoomSessions.computeIfAbsent(chatId, id -> new CopyOnWriteArraySet<>())
                .add(session);
    }

    // UNSUBSCRIBE
    public void unSubscribe(String sessionId, Long chatId) {
        sessionSubscriptions.getOrDefault(sessionId, Set.of()).remove(chatId);
        Set<WebSocketSession> set = chatRoomSessions.get(chatId);
        if (set != null) set.removeIf(s -> s.getId().equals(sessionId));
    }

    // DISCONNECT
    public void removeSession(String sessionId) {
        heartbeat.remove(sessionId);
        Set<Long> rooms = sessionSubscriptions.getOrDefault(sessionId, Set.of());
        for (Long chatId : rooms) {
            Set<WebSocketSession> conn = chatRoomSessions.get(chatId);
            if (conn != null) conn.removeIf(s -> s.getId().equals(sessionId));
        }
        WebSocketSession s = sessions.remove(sessionId);
        sessionSubscriptions.remove(sessionId);
    }

    public Set<WebSocketSession> getSessionsForChat(Long chatId) {
        return chatRoomSessions.getOrDefault(chatId, new CopyOnWriteArraySet<>());
    }

    public void touchHeartbeat(String sessionId) {
        heartbeat.put(sessionId, System.currentTimeMillis());
    }
}
