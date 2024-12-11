package com.rfmajor.scrabblesolver.common.gaddag.calculate;

import com.rfmajor.scrabblesolver.common.scrabble.Board;
import com.rfmajor.scrabblesolver.common.scrabble.Field;
import com.rfmajor.scrabblesolver.common.gaddag.model.Gaddag;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;


@Setter
public class CrossSetCalculator<A> {
    private final Board board;
    private final Gaddag<A> gaddag;
    private final boolean[][] anchors;
    private final Set<Field> anchorsSet;
    private final int[][] crossSets;
    private char delimiter;

    public CrossSetCalculator(Board board, Gaddag<A> gaddag) {
        this.board = board;
        this.crossSets = new int[board.length()][board.length()];
        this.anchors = new boolean[board.length()][board.length()];
        this.anchorsSet = new HashSet<>();
        this.gaddag = gaddag;
        this.delimiter = gaddag.getDelimiter();
        initialize();
    }

    public void computeAnchors(int column) {
        for (int row = 0; row < board.length(); row++) {
            if (!board.isEmpty(row, column) || board.hasLettersAbove(row, column) || board.hasLettersBelow(row, column)) {
                addAnchor(row, column);
            } else {
                removeAnchor(row, column);
            }
        }
    }

    public Set<Field> getAnchors() {
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

            boolean hasLettersAbove = board.hasLettersAbove(row, column);
            boolean hasLettersBelow = board.hasLettersBelow(row, column);

            if (hasLettersAbove && hasLettersBelow) {
                // result - reversed word with delimiter at the tail
                String aboveWord = board.readWordUpwards(row - 1, column, delimiter);
                // result - word WITHOUT delimiter at the tail
                String belowWord = board.readWordDownwards(row + 1, column, false);
                crossSets[row][column] = gaddag.getOneLetterCompletion(aboveWord, belowWord);
            } else if (hasLettersAbove) {
                // result - reversed word with delimiter at the tail
                String word = board.readWordUpwards(row - 1, column, delimiter);
                crossSets[row][column] = gaddag.getOneLetterCompletion(word);
            } else if (hasLettersBelow) {
                // result - reversed word WITHOUT delimiter at the tail
                String word = board.readWordDownwards(row + 1, column, true);
                crossSets[row][column] = gaddag.getOneLetterCompletion(word);
            } else {
                // allow all letters
                crossSets[row][column] = -1;
            }
        }
    }

    private void addAnchor(int row, int column) {
        anchors[row][column] = true;
        anchorsSet.add(new Field(row, column));
    }

    private void removeAnchor(int row, int column) {
        anchors[row][column] = false;
        anchorsSet.remove(new Field(row, column));
    }

    private void initialize() {
        if (board.isEmpty()) {
            addAnchor(board.length() / 2, board.length() / 2);
            computeCrossSets(board.length() / 2);
        } else {
            for (int i = 0; i < board.length(); i++) {
                computeAnchors(i);
                computeCrossSets(i);
            }
        }
    }
}
