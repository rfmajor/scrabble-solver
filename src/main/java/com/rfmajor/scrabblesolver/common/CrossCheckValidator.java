package com.rfmajor.scrabblesolver.common;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CrossCheckValidator {
    private final Board board;
    private final Alphabet alphabet;
    private final boolean[][] anchors = new boolean[Board.DEFAULT_SIZE][Board.DEFAULT_SIZE];
    private final long[][] crossSets = new long[Board.DEFAULT_SIZE][Board.DEFAULT_SIZE];

    public void updateAnchors() {

    }

    public boolean isAnchor(int x, int y) {
        return anchors[x][y];
    }

    public boolean containsLetter(long vector, char letter) {
        int index = alphabet.getIndex(letter);
        return ((vector >> index) & 1) == 1L;
    }

    public void computeCrossSets(int row) {
        
    }
}
