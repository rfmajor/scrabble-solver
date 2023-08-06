package com.rfmajor.scrabblesolver.web.service;

import com.rfmajor.scrabblesolver.common.game.Field;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class GenerateMovesRequest {
    private BoardDto board;
    private int[][] crossSets;
    private boolean[][] anchors;
    private String rackLetters;
    private String alphabetLanguage;
    private Set<int[]> blankFields;
}
