package com.space.munova_chat.rsocket.repository.r2dbc;

import com.space.munova_chat.rsocket.entity.Member;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface MemberRepository extends ReactiveCrudRepository<Member, Long> {
}
