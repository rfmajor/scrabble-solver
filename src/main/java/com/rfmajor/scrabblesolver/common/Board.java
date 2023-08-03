package com.rfmajor.scrabblesolver.common;

import lombok.Data;

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

@Data
public class Board {
    private char[][] fields;
    private char emptyChar;

    public Board() {
        this.fields = new char[DEFAULT_SIZE][DEFAULT_SIZE];
        this.emptyChar = DEFAULT_EMPTY_CHAR;
    }

    public Board(int size, char emptyChar) {
        this.fields = new char[size][size];
        this.emptyChar = emptyChar;
        fillBoardWithEmptyChars();
    }

    public Board(char[][] fields, char emptyChar) {
        this.fields = fields;
        this.emptyChar = emptyChar;
    }

    public static final int DEFAULT_SIZE = 15;
    private static final char DEFAULT_EMPTY_CHAR = '\u0000';

    public char getField(int x, int y) {
        return fields[x][y];
    }

    /**
     * Check if the field is empty without validating indices
     */
    public boolean isEmpty(int x, int y) {
        return fields[x][y] == emptyChar;
    }

    /**
     * Check if the field is valid
     */
    public boolean isValid(int x, int y) {
        return x >= 0 && x < this.length() && y >= 0 && y < this.length();
    }

    /**
     * Check if the field has a letter on it regardless whether it's valid or not
     */
    public boolean isOccupiedByLetter(int x, int y) {
        return isValid(x, y) && !isEmpty(x, y);
    }

    public boolean isEmpty() {
        for (int i = 0; i < length(); i++) {
            for (int j = 0; j < length(); j++) {
                if (fields[i][j] != emptyChar) {
                    return false;
                }
            }
        }
        return true;
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

    private void fillBoardWithEmptyChars() {
        for (int i = 0; i < length(); i++) {
            for (int j = 0; j < length(); j++) {
                fields[i][j] = emptyChar;
            }
        }
    }
}
