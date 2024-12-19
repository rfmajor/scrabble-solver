package com.rfmajor.scrabblesolver.server.web.service;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GenerateMovesRequest {
    private BoardDto board;
    private int[][] crossSets;
    private boolean[][] anchors;
    private String rackLetters;
    private boolean computeCrossSets;
}
