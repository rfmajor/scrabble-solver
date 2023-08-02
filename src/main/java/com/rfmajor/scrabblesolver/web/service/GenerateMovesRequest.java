package com.rfmajor.scrabblesolver.web.service;

import com.rfmajor.scrabblesolver.common.Board;
import lombok.Builder;

@Builder
public class GenerateMovesRequest {
    private Board board;
    private int[][] crossSets;
    private boolean[][] anchors;
    private String rackLetters;
    private String alphabetLanguage;
}
