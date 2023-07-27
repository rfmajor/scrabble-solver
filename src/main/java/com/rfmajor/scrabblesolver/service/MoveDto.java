package com.rfmajor.scrabblesolver.service;

import lombok.Builder;

@Builder
public class MoveDto {
    private String word;
    private int points;
    private int[][] beginningField;
    private Direction direction;
}
