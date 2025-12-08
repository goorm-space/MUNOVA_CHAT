package com.space.munova_chat.rsocket.jwt;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Decoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
public class RoutingMetadataDecoder implements Decoder<String> {

    @Override
    public boolean canDecode(ResolvableType type, MimeType mimeType) {
        if (mimeType == null) return false;
        return "message/x.rsocket.routing.v0".equals(mimeType.toString())
                && type.resolve() == String.class;
    }

    @Override
    public List<MimeType> getDecodableMimeTypes() {
        return List.of(MimeType.valueOf("message/x.rsocket.routing.v0"));
    }

    private String extractRoute(DataBuffer buffer) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(buffer.asByteBuffer());

        int len = byteBuf.readUnsignedByte();  // length prefix
        log.info("📦 RAW LENGTH PREFIX = {}", len);

        byte[] bytes = new byte[len];
        byteBuf.readBytes(bytes);

        String decoded = new String(bytes, StandardCharsets.UTF_8);
        log.info("📨 DECODED ROUTE = [{}]", decoded);

        return decoded;
    }

    @Override
    public Flux<String> decode(Publisher<DataBuffer> inputStream, ResolvableType type,
                               MimeType mimeType, Map<String, Object> hints) {

        return Flux.from(inputStream)
                .map(this::extractRoute);
    }

    @Override
    public Mono<String> decodeToMono(Publisher<DataBuffer> inputStream, ResolvableType type,
                                     MimeType mimeType, Map<String, Object> hints) {

        return Mono.from(inputStream)
                .map(this::extractRoute);
    }
}