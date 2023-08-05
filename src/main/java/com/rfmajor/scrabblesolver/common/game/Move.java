package com.rfmajor.scrabblesolver.common.game;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Move {
    private String word;
    private Direction direction;
    private int x;
    private int y;
    private int points;
    private int blanks;
}
