package com.space.munova_chat.rsocket.jwt;

import com.space.munova_chat.rsocket.entity.MemberRole;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtHelper jwtHelper;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();
        log.info("Received JWT authentication token: {}", token);

        Claims claims = jwtHelper.parseClaims(token);
        Long memberId = jwtHelper.getMemberId(claims);
        MemberRole role = jwtHelper.getRole(claims);

        return Mono.just(
                new UsernamePasswordAuthenticationToken(
                        memberId,
                        null,
                        List.of(role)
                ));
    }
}
