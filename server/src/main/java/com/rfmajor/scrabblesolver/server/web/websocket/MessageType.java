package com.rfmajor.scrabblesolver.server.web.websocket;

public enum MessageType {
    CREATE_ROOM,
    JOIN_ROOM,
    LEAVE_ROOM,
    SEND_PAYLOAD,
    UNKNOWN;

    public static MessageType fromString(String type) {
        for (MessageType messageType : MessageType.values()) {
            if (messageType.toString().equals(type)) {
                return messageType;
            }
        }
        return UNKNOWN;
    }
}
