//package com.space.munova_chat.rsocket.config;
//
//import com.space.munova_chat.rsocket.exception.JwtAuthException;
//import com.space.munova_chat.rsocket.jwt.JwtHelper;
//import com.space.munova_chat.rsocket.jwt.JwtSetupMetadataExtractor;
//import io.jsonwebtoken.Claims;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import reactor.core.publisher.Mono;
//
//@Slf4j
//@Configuration
//@RequiredArgsConstructor
//public class RSocketSetupAuthConfig {
//
//    private final JwtSetupMetadataExtractor extractor;
//    private final JwtHelper jwtHelper;
//    private final RoomSessionManager sessionManager;
//
//    private static String bytesToHex(byte[] bytes) {
//        StringBuilder sb = new StringBuilder();
//        for (byte b : bytes) sb.append(String.format("%02X", b));
//        return sb.toString();
//    }
//
//    @Bean
//    public RSocketServerCustomizer setupAuthentication() {
//        return server -> server.acceptor((setupPayload, sendingSocket) -> {
//
//            // 1. raw metadata hex 로그
//            if (setupPayload.getMetadata() != null) {
//                var metadataBuf = setupPayload.getMetadata();
//                byte[] arr = new byte[metadataBuf.remaining()];
//                metadataBuf.duplicate().get(arr);   // duplicate() → position 영향 없음
//
//                log.info("🔍 [SETUP] RAW METADATA HEX = {}", bytesToHex(arr));
//                log.info("🔍 [SETUP] RAW METADATA UTF-8 = {}", new String(arr));
//            } else {
//                log.warn("⚠️ [SETUP] Metadata is NULL!");
//            }
//
//            // 2. extractor 단계
//            return extractor.extract(setupPayload)
//                    .doOnNext(token -> {
//                        log.info("🔍 [SETUP] Extracted token = '{}'", token);
//                        log.info("🔍 [SETUP] Token length = {}", token.length());
//                    })
//                    .switchIfEmpty(Mono.defer(() -> {
//                        log.error("❌ [SETUP] Metadata found but extractor returned EMPTY");
//                        return Mono.error(JwtAuthException.invalidSignature());
//                    }))
//                    // 3. JWT parsing
//                    .map(token -> {
//                        try {
//                            Claims claims = jwtHelper.parseClaims(token);
//                            log.info("🔍 [SETUP] Successfully parsed JWT claims: {}", claims);
//                            return claims;
//                        } catch (Exception e) {
//                            log.error("❌ [SETUP] JWT parsing failed: {}", e.getMessage());
//                            throw e;
//                        }
//                    })
//                    // 4. memberId 가져오기
//                    .map(claims -> {
//                        Long memberId = jwtHelper.getMemberId(claims);
//                        log.info("🔍 [SETUP] memberId extracted = {}", memberId);
//                        return memberId;
//                    })
//                    // 5. Session 등록
//                    .flatMap(memberId -> {
//                        sessionManager.registerRawSocket(sendingSocket, memberId);
//                        log.info("✅ [SETUP] Session registered successfully for userId={}", memberId);
//                        return Mono.just(sendingSocket);
//                    });
//        });
//    }
//
//}
