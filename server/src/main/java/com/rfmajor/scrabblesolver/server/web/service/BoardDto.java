package com.rfmajor.scrabblesolver.server.web.service;

import lombok.Data;

@Data
public class BoardDto {
    private char[][] fields;
    private char emptyChar;
}
