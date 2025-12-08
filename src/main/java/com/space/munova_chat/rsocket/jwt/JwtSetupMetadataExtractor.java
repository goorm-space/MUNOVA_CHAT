package com.space.munova_chat.rsocket.jwt;

import io.netty.buffer.Unpooled;
import io.rsocket.Payload;
import io.rsocket.metadata.CompositeMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class JwtSetupMetadataExtractor {
    private static final String MIME = "message/x.rsocket.authentication.v0";

    public Mono<String> extract(Payload payload) {

        ByteBuffer metadata = payload.getMetadata();
        if (metadata == null || !metadata.hasRemaining()) {
            return Mono.empty();
        }

        CompositeMetadata composite = new CompositeMetadata(Unpooled.wrappedBuffer(metadata), false);

        for (CompositeMetadata.Entry entry : composite) {
            String mimeType = entry.getMimeType().toString().replace("\0", "");
            if (MIME.equals(mimeType)) {
                return Mono.just(entry.getContent().toString(StandardCharsets.UTF_8));
            }
        }
        return Mono.empty();
    }
}
