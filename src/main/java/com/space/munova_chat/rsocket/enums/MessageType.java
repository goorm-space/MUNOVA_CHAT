package com.space.munova_chat.rsocket.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum MessageType {
    JOIN, SEND, UNSUB, PONG
}
