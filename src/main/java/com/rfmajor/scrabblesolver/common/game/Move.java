package com.rfmajor.scrabblesolver.common.game;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Move {
    private String word;
    private int x;
    private int y;
}
