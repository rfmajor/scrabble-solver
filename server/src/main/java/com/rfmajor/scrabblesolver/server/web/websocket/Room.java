package com.rfmajor.scrabblesolver.server.web.websocket;

import java.util.Set;

public record Room(String id, String hostSessionId, Set<String> clients) {
}
