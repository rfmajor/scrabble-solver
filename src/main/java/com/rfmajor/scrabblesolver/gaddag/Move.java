package com.rfmajor.scrabblesolver.gaddag;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Move {
    private String word;
    private int x;
    private int y;
}
