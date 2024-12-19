package com.rfmajor.scrabblesolver.server.web.service;

import lombok.Data;

import java.util.List;

@Data
public class BoardDto {
    private char[][] fields;
    private char emptyChar;
    private List<int[]> blankFields;
}
