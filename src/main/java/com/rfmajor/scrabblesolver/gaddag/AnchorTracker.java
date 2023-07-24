package com.rfmajor.scrabblesolver.gaddag;

import com.rfmajor.scrabblesolver.common.Board;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class AnchorTracker {
    private final Board board;
    private final boolean[][] anchors = new boolean[Board.DEFAULT_SIZE][Board.DEFAULT_SIZE];

    public void updateAnchors() {

    }

    public boolean isAnchor(int x, int y) {
        return anchors[x][y];
    }
}
