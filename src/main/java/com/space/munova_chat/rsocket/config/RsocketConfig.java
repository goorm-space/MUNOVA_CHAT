package com.space.munova_chat.rsocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.http.codec.cbor.JacksonCborDecoder;
import org.springframework.http.codec.cbor.JacksonCborEncoder;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.http.codec.json.JacksonJsonEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.util.MimeType;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;

import java.net.URI;

@Configuration
@EnableR2dbcAuditing
public class RsocketConfig {

    @Bean
    public RSocketStrategies rSocketStrategies() {
        return RSocketStrategies.builder()
                .encoders(encoders -> {
                    encoders.add(new JacksonJsonEncoder());
                    encoders.add(new JacksonCborEncoder());
                })
                .decoders(decoders -> {
                    decoders.add(new JacksonJsonDecoder());
                    decoders.add(new JacksonCborDecoder());
                })
                .routeMatcher(new PathPatternRouteMatcher())
                .build();
    }

    @Bean
    public RSocketMessageHandler rSocketMessageHandler(RSocketStrategies strategies) {
        RSocketMessageHandler handler = new RSocketMessageHandler();
        handler.setRSocketStrategies(strategies);
        return handler;
    }


    // Spring에서 RSocket에 연결을 생성하는 RSocketRequester 객체 생성
//    @Bean
//    public RSocketRequester rSocketRequester(RSocketStrategies strategies) {
//        return RSocketRequester.builder()
//                .rsocketStrategies(strategies)
//                .websocket(URI.create("ws://localhost:7000/rs"));
//    }
}
