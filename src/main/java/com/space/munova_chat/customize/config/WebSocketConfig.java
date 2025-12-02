//package com.space.munova_chat.customize.config;
//
//import com.space.munova_chat.customize.config.websocket.ChatWebSocketHandler;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.reactive.HandlerMapping;
//import org.springframework.web.reactive.config.EnableWebFlux;
//import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
//import org.springframework.web.reactive.socket.WebSocketHandler;
//import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//@EnableWebFlux  // webflux 기반 (Netty 기반)
//@RequiredArgsConstructor
//public class WebSocketConfig {
//
////     메시지 처리 핸들러
//    private final ChatWebSocketHandler chatWebSocketHandler;
//
//    // end-point 등록
//    @Bean
//    public HandlerMapping webSocketMapping() {
//
//        // 해당 url 을 처리할 handler를 map으로 등록
//        Map<String, WebSocketHandler> map = new HashMap<>();
//        map.put("/ws/chat", chatWebSocketHandler);
//
//        // 우선순위 제일 높기
//        int order = -1;
//        return new SimpleUrlHandlerMapping(map, order);
//    }
//
//    // websocket handler 등록
//    @Bean
//    public WebSocketHandlerAdapter handlerAdapter() {
//        return new WebSocketHandlerAdapter();
//    }
//}