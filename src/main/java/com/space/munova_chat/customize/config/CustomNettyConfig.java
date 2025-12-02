package com.space.munova_chat.customize.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomNettyConfig {
//    @Bean
//    public NettyServerCustomizer nettyServerCustomizer() {
//
//        return httpServer ->
//                httpServer.doOnConnection(conn -> {
//                    // 유휴 상태 감지 (30초 동안 메시지 없음 → Idle 이벤트 발생)
//                    conn.addHandlerLast(new IdleStateHandler(10, 10, 0));
//
//                    // Idle 이벤트 처리 핸들러
//                    conn.addHandlerLast(new WebSocketIdleHandler());
//                });
//    }
}
