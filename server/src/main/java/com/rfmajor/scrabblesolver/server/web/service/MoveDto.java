package com.rfmajor.scrabblesolver.server.web.service;

import com.rfmajor.scrabblesolver.common.scrabble.Direction;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class MoveDto {
    private String word;
    private int points;
    private int x;
    private int y;
    private String position;
    private Direction direction;
    private List<int[]> newBlankFields;
}
