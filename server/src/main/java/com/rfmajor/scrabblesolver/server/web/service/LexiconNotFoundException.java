package com.rfmajor.scrabblesolver.server.web.service;

public class LexiconNotFoundException extends RuntimeException {
    public LexiconNotFoundException(String message) {
        super(message);
    }
}
