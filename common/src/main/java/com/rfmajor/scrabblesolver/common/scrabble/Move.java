package com.rfmajor.scrabblesolver.common.scrabble;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@Data
public class Move {
    private String word;
    private Direction direction;
    private int x;
    private int y;
    private int points;
    private Set<Integer> blanks;
    private Set<Field> newBlankFields;

    public Move(String word, Direction direction, int x, int y, Set<Integer> blanks) {
        this.word = word;
        this.direction = direction;
        this.x = x;
        this.y = y;
        this.points = 0;
        this.blanks = blanks;
        this.newBlankFields = new HashSet<>();
    }

    public boolean isBlankLetter(int letterIndex) {
        return blanks.contains(letterIndex);
    }

    public void addBlankFieldInfo(int row, int column) {
        newBlankFields.add(new Field(row, column));
    }
}
