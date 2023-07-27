package com.rfmajor.scrabblesolver.service;

import com.rfmajor.scrabblesolver.common.Alphabet;
import com.rfmajor.scrabblesolver.common.Board;
import com.rfmajor.scrabblesolver.common.Rack;
import lombok.Builder;

@Builder
public class GenerateMovesRequest {
    private Board board;
    private int[][] crossSets;
    private Rack rack;
    private Alphabet alphabet;
}
