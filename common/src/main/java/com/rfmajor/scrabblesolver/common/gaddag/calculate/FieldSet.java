package com.rfmajor.scrabblesolver.common.gaddag.calculate;

import com.rfmajor.scrabblesolver.common.scrabble.Field;

import java.util.HashSet;
import java.util.Set;

public record FieldSet(Set<Field> anchors, int[][] crossSets) {
    public int getCrossSet(int row, int column) {
        if (row < 0 || row >= crossSets.length || column < 0 || column >= crossSets[0].length) {
            return 0;
        }
        return crossSets[row][column];
    }

    public Set<Field> getAnchors() {
        return new HashSet<>(anchors);
    }

    public boolean isAnchor(int row, int column) {
        return anchors.contains(new Field(row, column));
    }
}
