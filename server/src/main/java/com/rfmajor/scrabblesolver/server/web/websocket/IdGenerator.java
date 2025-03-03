package com.rfmajor.scrabblesolver.server.web.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@RequiredArgsConstructor
public class IdGenerator {
    private final Random random;

    private static final int DEFAULT_ID_LENGTH = 6;
    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public String generateId() {
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < DEFAULT_ID_LENGTH; i++) {
            id.append(ALPHANUMERIC_CHARS.charAt(random.nextInt(ALPHANUMERIC_CHARS.length())));
        }
        return id.toString();
    }
}
