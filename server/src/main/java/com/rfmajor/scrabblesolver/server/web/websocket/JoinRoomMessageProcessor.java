package com.rfmajor.scrabblesolver.server.web.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JoinRoomMessageProcessor implements MessageProcessor {
    private final RoomServiceImpl roomService;
    private final ObjectMapper objectMapper;

    @Override
    public void process(Object data, WebSocketSession session) throws IOException {
        if (!(data instanceof String roomId)) {
            sendResponse(session, false);
            return;
        }

        try {
            roomService.joinRoom(roomId, session.getId());
            sendResponse(session, true);
        } catch (Exception e) {
            sendResponse(session, false);
        }
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.JOIN_ROOM;
    }

    public record Response(ResponseType type, boolean success) {}

    private void sendResponse(WebSocketSession session, boolean success) throws IOException {
        Response response = new Response(ResponseType.JOIN_ROOM, success);
        session.sendMessage(new BinaryMessage(objectMapper.writeValueAsBytes(response)));
    }
}
