package com.rfmajor.scrabblesolver.web.service;

import com.rfmajor.scrabblesolver.common.game.Direction;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MoveDto {
    private String word;
    private int points;
    private int x;
    private int y;
    private Direction direction;
    private int blanks;
}
