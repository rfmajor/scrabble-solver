package com.rfmajor.scrabblesolver.web.service;

import com.rfmajor.scrabblesolver.common.game.Direction;
import com.rfmajor.scrabblesolver.common.game.Field;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class MoveDto {
    private String word;
    private int points;
    private int x;
    private int y;
    private Direction direction;
    private List<int[]> newBlankFields;
}
