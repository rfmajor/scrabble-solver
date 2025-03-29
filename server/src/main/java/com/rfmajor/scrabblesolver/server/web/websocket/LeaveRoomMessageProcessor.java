package com.rfmajor.scrabblesolver.server.web.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LeaveRoomMessageProcessor implements MessageProcessor {
    private final RoomServiceImpl roomService;
    private final ObjectMapper objectMapper;

    private static final Logger log = LoggerFactory.getLogger(LeaveRoomMessageProcessor.class);

    @Override
    public void process(Object data, WebSocketSession session) throws IOException {
        if (!(data instanceof String roomId)) {
            sendResponse(session, false);
            return;
        }
        try {
            roomService.leaveRoom(roomId, session.getId());
            sendResponse(session, true);
        } catch (Exception e) {
            log.error("Error while processing leave room", e);
            sendResponse(session, false);
        }
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.LEAVE_ROOM;
    }

    public record Response(ResponseType type, boolean success) {}

    private void sendResponse(WebSocketSession session, boolean success) throws IOException {
        Response response = new Response(ResponseType.LEAVE_ROOM, success);
        session.sendMessage(new BinaryMessage(objectMapper.writeValueAsBytes(response)));
    }
}
