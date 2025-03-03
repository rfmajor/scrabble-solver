package com.rfmajor.scrabblesolver.server.web.websocket;

public interface RoomService {
    void createRoom(String roomId, String hostSessionId);

    void joinRoom(String roomId, String sessionId);

    void leaveRoom(String roomId, String sessionId);

    void deleteRoom(String roomId);
}
