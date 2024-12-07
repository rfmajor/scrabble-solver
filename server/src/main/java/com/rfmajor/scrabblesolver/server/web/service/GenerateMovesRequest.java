package com.rfmajor.scrabblesolver.server.web.service;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class GenerateMovesRequest {
    private BoardDto board;
    private int[][] crossSets;
    private boolean[][] anchors;
    private String rackLetters;
    private Set<int[]> blankFields;
    private boolean computeCrossSets;
}
