package com.rfmajor.scrabblesolver.common.scrabble;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Field {
    private int row;
    private int column;
}
