package com.rfmajor.scrabblesolver.server.web.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class WebSocketMessageHandler extends BinaryWebSocketHandler {
    private final Map<String, WebSocketSession> sessionRegistry;
    private final List<MessageProcessor> messageProcessors;
    private final ObjectMapper objectMapper;

    private static final Logger log = LoggerFactory.getLogger(WebSocketMessageHandler.class);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Opened session {} to the remote url: {}", session.getId(), session.getRemoteAddress());
        sessionRegistry.put(session.getId(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("Closed session {} to the remote url: {}", session.getId(), session.getRemoteAddress());
        sessionRegistry.remove(session.getId());
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        RawMessage rawMessageObject = objectMapper.readValue(message.getPayload().array(), RawMessage.class);
        MessageProcessor messageProcessor = determineMessageProcessor(rawMessageObject);
        messageProcessor.process(rawMessageObject.data(), session);
    }

    private MessageProcessor determineMessageProcessor(RawMessage rawMessageObject) {
        for (MessageProcessor messageProcessor : messageProcessors) {
            if (messageProcessor.getMessageType() == MessageType.fromString(rawMessageObject.type())) {
                return messageProcessor;
            }
        }
        throw new IllegalArgumentException("No message processor configured for the message type: " + rawMessageObject.type());
    }
}
