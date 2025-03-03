package com.rfmajor.scrabblesolver.server.web.websocket;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

@Service
public class RoomServiceImpl implements RoomService {
    private final ConcurrentMap<String, Room> activeRooms;

    private static final String ROOM_EXISTS_MSG = "Room with id %s already exists";
    private static final String ROOM_NOT_EXISTENT_MSG = "Room with id %s doesn't exist";

    public RoomServiceImpl(@Qualifier("activeRooms") ConcurrentMap<String, Room> activeRooms) {
        this.activeRooms = activeRooms;
    }

    @Override
    public void createRoom(String roomId, String hostSessionId) {
        activeRooms.compute(roomId, (id, room) -> {
            if (room != null) {
                throw new IllegalArgumentException(String.format(ROOM_EXISTS_MSG, roomId));
            }
            Set<String> clientIds = new HashSet<>();
            clientIds.add(hostSessionId);
            return new Room(roomId, hostSessionId, clientIds);
        });
    }

    @Override
    public void joinRoom(String roomId, String sessionId) {
        activeRooms.compute(roomId, (id, room) -> {
            if (room == null) {
                throw new IllegalArgumentException(String.format(ROOM_NOT_EXISTENT_MSG, roomId));
            }
            room.clients().add(sessionId);
            return room;
        });
    }

    @Override
    public void leaveRoom(String roomId, String sessionId) {
        activeRooms.compute(roomId, (id, room) -> {
            if (room == null) {
                throw new IllegalArgumentException(String.format(ROOM_NOT_EXISTENT_MSG, roomId));
            }
            room.clients().remove(sessionId);
            return room;
        });
    }

    @Override
    public void deleteRoom(String roomId) {
        activeRooms.remove(roomId);
    }
}
