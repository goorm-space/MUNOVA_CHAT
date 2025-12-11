package com.space.munova_chat.rsocket.config;

import io.netty.channel.ChannelOption;
import io.netty.channel.WriteBufferWaterMark;
import io.rsocket.frame.decoder.PayloadDecoder;
import org.springframework.boot.reactor.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
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

    @Bean
    public RSocketServerCustomizer rSocketServerCustomizer() {
        return server -> server
//                .fragment(64 * 1024)
//                .maxInboundPayloadSize(16 * 1024 * 1024)
                .payloadDecoder(PayloadDecoder.ZERO_COPY);
    }

    @Bean
    public NettyReactiveWebServerFactory nettyFactory() {
        NettyReactiveWebServerFactory factory = new NettyReactiveWebServerFactory();

        factory.addServerCustomizers(httpServer ->
                httpServer.tcpConfiguration(tcp ->
                        tcp.option(ChannelOption.WRITE_BUFFER_WATER_MARK,
                                new WriteBufferWaterMark(64 * 1024, 128 * 1024))
                )
        );
        return factory;
    }

}
