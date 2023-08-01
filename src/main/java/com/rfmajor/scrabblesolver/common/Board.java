package com.rfmajor.scrabblesolver.common;

import lombok.Getter;

/**
 * Board represented by a 2-dimensional array
 * Indexed by x (row) and y (columns), where x == 0 is the first row from the top and
 * y == 0 is the first column from the left. Example - (1, 2) coordinates on a 5x5 board:
 *
 * 0 0 0 0 0
 * 0 0 X 0 0
 * 0 0 0 0 0
 * 0 0 0 0 0
 * 0 0 0 0 0
 */

@Getter
public class Board {
    private final char[][] fields = new char[DEFAULT_SIZE][DEFAULT_SIZE];

    public static final int DEFAULT_SIZE = 15;
    public static final char EMPTY_CHAR = '\u0000';

    public char getField(int x, int y) {
        return fields[x][y];
    }

    public boolean isEmpty(int x, int y) {
        return fields[x][y] == EMPTY_CHAR;
    }

    public void addLetter(char letter, int x, int y) {
        if (!isEmpty(x, y)) {
            throw new LetterAlreadyPresentException("Letter %s is already present on field (%d, %d)"
                    .formatted(letter, x, y));
        }
        fields[x][y] = letter;
    }

    public int length() {
        return fields.length;
    }

    public static class LetterAlreadyPresentException extends RuntimeException {
        public LetterAlreadyPresentException(String message) {
            super(message);
        }
    }
}
