package com.space.munova_chat.customize.config.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebSocketIdleHandler extends ChannelInboundHandlerAdapter {    // 새로운 inbound handler pipeline에 추가

    // 기본 inbound 이벤트가 아닌 특별 이벤트를 처리
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // idleStateHandler가 발생시키는 이벤트인가?
        if (evt instanceof IdleStateEvent idle) {

            switch (idle.state()) {

                case WRITER_IDLE:   // 일정 시간 동안 서버가 클라이언트에게 아무것도 안보냄
                    ctx.writeAndFlush(new PingWebSocketFrame());
                    log.info("[PING SENT] session={}", ctx.channel().id());
                    break;
                case READER_IDLE:   // 일정 시간 동안 클라이언트가 아무것도 안보냄
                    log.warn("[PING TIMEOUT] Closing idle session={}", ctx.channel().id());
                    ctx.close();
                    break;
            }
        } else {
            // idle이 아니면 부모에게 넘김
            super.userEventTriggered(ctx, evt);
        }
    }
}