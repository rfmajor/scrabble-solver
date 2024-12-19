package com.rfmajor.scrabblesolver.common.scrabble;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Board represented by a 2-dimensional array
 * Indexed by x (row) and y (columns), where x == 0 is the first row from the top and
 * y == 0 is the first column from the left. Example, (1, 2) coordinates on a 5x5 board:
 *
 *     (y)--------->
 * (x)
 *  |     0 0 0 0 0
 *  |     0 0 X 0 0
 *  |     0 0 0 0 0
 *  |     0 0 0 0 0
 *  |     0 0 0 0 0
 *  v
 */

@Data
public class Board {
    private final char[][] fields;
    private final Set<Field> blankFields;

    public Board() {
        this.fields = new char[DEFAULT_SIZE][DEFAULT_SIZE];
        this.blankFields = new HashSet<>();
    }

    public Board(char[][] fields, Set<Field> blankFields) {
        this.fields = fields;
        this.blankFields = blankFields;
    }

    public static final int DEFAULT_SIZE = 15;

    public void clear() {
        fillBoardWithEmptyChars();
        blankFields.clear();
    }

    public char getField(int x, int y) {
        return fields[x][y];
    }

    /**
     * Check if the field is empty without validating indices
     */
    public boolean isEmpty(int x, int y) {
        return fields[x][y] == 0;
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

    public boolean isValidAndEmpty(int x, int y) {
        return isValid(x, y) && isEmpty(x, y);
    }

    public boolean isEmpty() {
        for (int i = 0; i < length(); i++) {
            for (int j = 0; j < length(); j++) {
                if (fields[i][j] != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean hasLettersAbove(int row, int column) {
        return row > 0 && fields[row - 1][column] != 0;
    }

    public boolean hasLettersBelow(int row, int column) {
        return row < length() - 1 && fields[row + 1][column] != 0;
    }

    public String readWordUpwards(int row, int column, char delimiter) {
        return readWordUpwards(row, column, true, delimiter, true);
    }

    public String readWordUpwards(int row, int column, boolean reversed) {
        return readWordUpwards(row, column, false, '0', reversed);
    }

    private String readWordUpwards(int row, int column, boolean appendDelimiter, char delimiter, boolean reversed) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = row; i >= 0 && fields[i][column] != 0; i--) {
            stringBuilder.append(fields[i][column]);
        }
        if (appendDelimiter) {
            stringBuilder.append(delimiter);
        }
        if (!reversed) {
            stringBuilder.reverse();
        }

        return stringBuilder.toString();
    }

    public String readWordDownwards(int row, int column, boolean reversed) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = row; i < length() && fields[i][column] != 0; i++) {
            stringBuilder.append(fields[i][column]);
        }
        if (reversed) {
            stringBuilder.reverse();
        }
        return stringBuilder.toString();
    }

    public void addLetter(char letter, int x, int y) {
        if (!isEmpty(x, y)) {
            throw new IllegalArgumentException("Letter %s is already present on field (%d, %d)"
                    .formatted(letter, x, y));
        }
        fields[x][y] = letter;
    }

    public int length() {
        return fields.length;
    }

    public Board transpose() {
        char[][] transposedFields = new char[length()][length()];
        for (int i = 0; i < length(); i++) {
            for (int j = 0; j < length(); j++) {
                transposedFields[j][i] = fields[i][j];
            }
        }

        Set<Field> transposedBlankFields = blankFields.stream()
                .map(field -> new Field(field.column(), field.row()))
                .collect(Collectors.toSet());

        return new Board(transposedFields, transposedBlankFields);
    }

    private void fillBoardWithEmptyChars() {
        for (int i = 0; i < length(); i++) {
            for (int j = 0; j < length(); j++) {
                fields[i][j] = 0;
            }
        }
    }
}
