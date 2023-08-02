package com.rfmajor.scrabblesolver.web.service;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class GenerateMovesRequest {
    private BoardDto board;
    private int[][] crossSets;
    private boolean[][] anchors;
    private String rackLetters;
    private String alphabetLanguage;
}
