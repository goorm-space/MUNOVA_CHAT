package com.space.munova_chat.rsocket.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class ChatException extends BaseException {

    public ChatException(String code, String message, HttpStatusCode statusCode, String... detailMessage) {
        super(code, message, statusCode, detailMessage);
    }

    public static ChatException cannotFindChatException(String... detailMessage) {
        return new ChatException("CHAT_01", "채팅방을 찾을 수 없습니다.", HttpStatus.NOT_FOUND, detailMessage);
    }

    public static ChatException notAllowedToCreateChatWithSelf(String... detailMessage) {
        return new ChatException("CHAT_02", "자신이 등록한 상품에는 문의할 수 없습니다.", HttpStatus.BAD_REQUEST, detailMessage);
    }

    public static ChatException unauthorizedParticipantException(String... detailMessage) {
        return new ChatException("CHAT_03", "채팅방에 대한 권한이 없습니다.", HttpStatus.UNAUTHORIZED, detailMessage);
    }

    public static ChatException chatClosedException(String... detailMessage) {
        return new ChatException("CHAT_04", "이미 종료된 채팅방입니다.", HttpStatus.CONFLICT, detailMessage);
    }

    public static ChatException duplicateChatNameException(String... detailMessage) {
        return new ChatException("CHAT_05", "중복된 채팅방 이름입니다.", HttpStatus.CONFLICT, detailMessage);
    }

    public static ChatException invalidOperationException(String... detailMessage) {
        return new ChatException("CHAT_06", "현재 참여 인원이 최대 정원보다 많아 정원 변경이 불가합니다.", HttpStatus.BAD_REQUEST, detailMessage);
    }

    public static ChatException invalidChatRoomException(String... detailMessage) {
        return new ChatException("CHAT_07", "유효하지 않은 채팅방입니다.", HttpStatus.BAD_REQUEST, detailMessage);
    }

    public static ChatException exceedMaxParticipantsException(String... detailMessage) {
        return new ChatException("CHAT_08", "채팅방 정원 초과", HttpStatus.BAD_REQUEST, detailMessage);
    }

    public static ChatException cannotDecrementParticipantsException() {
        return new ChatException("CHAT_09", "참여자 수 0 이하로 감소 불가", HttpStatus.BAD_REQUEST);
    }
}
