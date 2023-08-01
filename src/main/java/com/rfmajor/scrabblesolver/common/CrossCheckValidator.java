package com.rfmajor.scrabblesolver.common;

import com.rfmajor.scrabblesolver.gaddag.Gaddag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;


@RequiredArgsConstructor
public class CrossCheckValidator {
    private final Board board;
    private final Alphabet alphabet;
    private final Gaddag gaddag;
    private final boolean[][] anchors = new boolean[Board.DEFAULT_SIZE][Board.DEFAULT_SIZE];
    private final int[][] crossSets = new int[Board.DEFAULT_SIZE][Board.DEFAULT_SIZE];
    @Value("${gaddag.delimiter}")
    private char delimiter;

    public void updateAnchors() {

    }

    public boolean isAnchor(int x, int y) {
        return anchors[x][y];
    }

    public boolean containsLetter(long vector, char letter) {
        int index = alphabet.getIndex(letter);
        return ((vector >> index) & 1) == 1L;
    }

    public void computeCrossSets(int row) {
        for (int column = 0; column < board.getFields()[row].length; column++) {
            boolean hasLettersAbove = hasLettersAbove(row, column);
            boolean hasLettersBelow = hasLettersBelow(row, column);
            if (hasLettersAbove && hasLettersBelow) {
                String aboveWord = readWordUpwards(row - 1, column);
                String belowWord = readWordDownwards(row + 1, column, false);
                crossSets[row][column] = gaddag.getOneLetterCompletion(aboveWord, belowWord);
            } else if (hasLettersAbove) {
                String word = readWordUpwards(row - 1, column);
                crossSets[row][column] = gaddag.getOneLetterCompletion(word);
            } else if (hasLettersBelow) {
                String word = readWordDownwards(row + 1, column, true);
                crossSets[row][column] = gaddag.getOneLetterCompletion(word);
            }
        }
    }

    private String readWordUpwards(int row, int column) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = row; i >= 0 && board.getField(i, column) != Board.EMPTY_CHAR; i--) {
            stringBuilder.append(board.getField(i, column));
        }
        return stringBuilder.toString();
    }

    private String readWordDownwards(int row, int column, boolean includeDelimiter) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = row; i < board.length() && board.getField(i, column) != Board.EMPTY_CHAR; i++) {
            stringBuilder.append(board.getField(i, column));
        }
        if (includeDelimiter) {
            stringBuilder.insert(1, delimiter);
        }
        return stringBuilder.toString();
    }

    private boolean hasLettersAbove(int row, int column) {
        return row > 0 && board.getField(row - 1, column) != Board.EMPTY_CHAR;
    }

    private boolean hasLettersBelow(int row, int column) {
        return row < board.length() - 1 && board.getField(row + 1, column) != Board.EMPTY_CHAR;
    }
}
