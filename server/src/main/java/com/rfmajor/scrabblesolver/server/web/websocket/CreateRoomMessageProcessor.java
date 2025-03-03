package com.rfmajor.scrabblesolver.server.web.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CreateRoomMessageProcessor implements MessageProcessor {
    private final RoomServiceImpl roomService;
    private final IdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    @Override
    public void process(Object data, WebSocketSession session) throws IOException {
        String roomId = idGenerator.generateId();
        String sessionId = session.getId();

        try {
            roomService.createRoom(roomId, sessionId);
            sendResponse(session, true, roomId);
        } catch (Exception e) {
            sendResponse(session, false, null);
        }
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.CREATE_ROOM;
    }

    public record Response(ResponseType type, boolean success, String roomId) {}

    private void sendResponse(WebSocketSession session, boolean success, String roomId) throws IOException {
        Response response = new Response(ResponseType.CREATE_ROOM, success, roomId);
        session.sendMessage(new BinaryMessage(objectMapper.writeValueAsBytes(response)));
    }
}
