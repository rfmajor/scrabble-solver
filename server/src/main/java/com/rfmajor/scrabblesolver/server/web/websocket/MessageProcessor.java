package com.rfmajor.scrabblesolver.server.web.websocket;

import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public interface MessageProcessor {

    void process(Object data, WebSocketSession session) throws IOException;

    MessageType getMessageType();
}
