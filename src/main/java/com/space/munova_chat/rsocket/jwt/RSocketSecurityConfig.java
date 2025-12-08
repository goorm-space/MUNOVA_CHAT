//package com.space.munova_chat.rsocket.jwt;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
//import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
//import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
//import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;
//
//@Configuration
//@EnableRSocketSecurity
//@RequiredArgsConstructor
//public class RSocketSecurityConfig {
//
//    private final JwtReactiveAuthenticationManager authenticationManager;
//    private final JwtServerAuthenticationConverter authenticationConverter;
//
//    @Bean
//    public PayloadSocketAcceptorInterceptor rsocketInterceptor(RSocketSecurity security, RSocketMessageHandler handler) {
//
//        security
//                .authorizePayload(auth -> auth
//                        .route("join").permitAll()
//                        .route("chat.send").permitAll()
//                        .anyExchange().permitAll()
//                )
//                .authenticationManager(authenticationManager);
/// /                .jwt(jwtSpec -> {
/// /                    jwtSpec.authenticationManager(authenticationManager);
/// /                });
//
//        return security.build();
//    }
//
//}
