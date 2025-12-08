package com.space.munova_chat.rsocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.http.codec.cbor.JacksonCborDecoder;
import org.springframework.http.codec.cbor.JacksonCborEncoder;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.http.codec.json.JacksonJsonEncoder;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;

@Configuration
@EnableR2dbcAuditing
@EnableReactiveMongoAuditing
@EnableReactiveMongoRepositories(
        basePackages = "com.space.munova_chat.rsocket.repository.mongodb")
@EnableR2dbcRepositories(basePackages = "com.space.munova_chat.rsocket.repository.r2dbc")
public class RsocketConfig {

    @Bean
    public RSocketStrategies rSocketStrategies() {
        return RSocketStrategies.builder()
                .encoders(encoders -> {
                    encoders.add(new JacksonJsonEncoder());
                    encoders.add(new JacksonCborEncoder());
                })
                .decoders(decoders -> {
//                    decoders.add(0, new RoutingMetadataDecoder());
                    decoders.add(new JacksonJsonDecoder());
                    decoders.add(new JacksonCborDecoder());
                }).routeMatcher(new PathPatternRouteMatcher())
//                .metadataExtractorRegistry(reg -> {
//
//                    // ROUTING DECODER 등록
//                    reg.metadataToExtract(
//                            MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_ROUTING.getString()),
//                            String.class,
//                            "route"
//                    );
//                    reg.metadataToExtract(
//                            MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString()),
//                            String.class,
//                            "auth"
//                    );
//                })
                .build();
    }

    @Bean
    public RSocketMessageHandler rSocketMessageHandler(RSocketStrategies strategies) {
        RSocketMessageHandler handler = new RSocketMessageHandler();
        handler.setRSocketStrategies(strategies);
        return handler;
    }

//    @Bean
//    public RSocketServerCustomizer rSocketServerCustomizer(PayloadSocketAcceptorInterceptor securityInterceptor, RSocketMessageHandler handler) {
//        return server -> server
//                .interceptors(reg -> reg.forSocketAcceptor(securityInterceptor))
//                .acceptor(handler.responder());
//    }

}
