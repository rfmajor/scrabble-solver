package com.rfmajor.scrabblesolver.common;

import com.rfmajor.scrabblesolver.gaddag.Gaddag;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashSet;
import java.util.Set;


@Setter
public class CrossSetCalculator {
    private final Board board;
    private final Alphabet alphabet;
    private final Gaddag gaddag;
    private final boolean[][] anchors = new boolean[Board.DEFAULT_SIZE][Board.DEFAULT_SIZE];
    private final Set<Point> anchorsSet = new HashSet<>();
    private final int[][] crossSets = new int[Board.DEFAULT_SIZE][Board.DEFAULT_SIZE];
    @Value("${gaddag.delimiter}")
    private char delimiter;

    public CrossSetCalculator(Board board, Alphabet alphabet, Gaddag gaddag) {
        this.board = board;
        this.alphabet = alphabet;
        this.gaddag = gaddag;
        addAnchor(board.length() / 2,board.length() / 2);
    }

    public void computeAnchors(int column) {
        for (int row = 0; row < board.length(); row++) {
            if (!board.isEmpty(row, column) || hasLettersAbove(row, column) || hasLettersBelow(row, column)) {
                addAnchor(row, column);
            } else {
                removeAnchor(row, column);
            }
        }
    }

    public Set<Point> getAnchors() {
        return new HashSet<>(anchorsSet);
    }

    public boolean isAnchor(int x, int y) {
        return anchors[x][y];
    }

    public int getCrossSet(int row, int column) {
        if (row < 0 || row >= board.length() || column < 0 || column >= board.length()) {
            return 0;
        }
        return crossSets[row][column];
    }

    public void computeCrossSets(int row) {
        for (int column = 0; column < board.getFields()[row].length; column++) {
            if (!board.isEmpty(row, column)) {
                crossSets[row][column] = 0;
                continue;
            }

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
            } else {
                crossSets[row][column] = Integer.MAX_VALUE;
            }
        }
    }

    private String readWordUpwards(int row, int column) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = row; i >= 0 && board.getField(i, column) != Board.EMPTY_CHAR; i--) {
            stringBuilder.append(board.getField(i, column));
        }
        stringBuilder.append(delimiter);
        return stringBuilder.toString();
    }

    private String readWordDownwards(int row, int column, boolean reversed) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = row; i < board.length() && board.getField(i, column) != Board.EMPTY_CHAR; i++) {
            stringBuilder.append(board.getField(i, column));
        }
        if (reversed) {
            stringBuilder.reverse();
        }
        return stringBuilder.toString();
    }

    private boolean hasLettersAbove(int row, int column) {
        return row > 0 && board.getField(row - 1, column) != Board.EMPTY_CHAR;
    }

    private boolean hasLettersBelow(int row, int column) {
        return row < board.length() - 1 && board.getField(row + 1, column) != Board.EMPTY_CHAR;
    }

    private void addAnchor(int row, int column) {
        anchors[row][column] = true;
        anchorsSet.add(new Point(row, column));
    }

    private void removeAnchor(int row, int column) {
        anchors[row][column] = false;
        anchorsSet.remove(new Point(row, column));
    }
}