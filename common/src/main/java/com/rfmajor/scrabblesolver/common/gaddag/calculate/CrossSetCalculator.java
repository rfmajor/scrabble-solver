package com.rfmajor.scrabblesolver.common.gaddag.calculate;

import com.rfmajor.scrabblesolver.common.gaddag.model.Gaddag;
import com.rfmajor.scrabblesolver.common.scrabble.Board;
import com.rfmajor.scrabblesolver.common.scrabble.Field;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;


@Setter
public class CrossSetCalculator<A> {
    private final Gaddag<A> gaddag;
    private char delimiter;

    public CrossSetCalculator(Gaddag<A> gaddag) {
        this.gaddag = gaddag;
        this.delimiter = gaddag.getDelimiter();
    }

    public FieldSet computeAllCrossSetsAndAnchors(Board board) {
        Set<Field> anchors;
        int[][] crossSets;
        if (!board.isEmpty()) {
            anchors = computeAllAnchors(board);
            crossSets = computeAllCrossSets(board);
        } else {
            anchors = new HashSet<>();
            crossSets = new int[board.length()][board.length()];

            anchors.add(new Field(board.length() / 2, board.length() / 2));
            computeCrossSets(board.length() / 2, board, crossSets);
        }

        return new FieldSet(anchors, crossSets);
    }

    public FieldSet computeCrossSetsAndAnchors(int row, int column, Board board) {
        int[][] crossSets = computeCrossSets(row, board);
        Set<Field> anchors = computeAnchors(column, board);

        return new FieldSet(anchors, crossSets);
    }

    public void computeCrossSetsAndAnchors(int row, int column, Board board, FieldSet fieldSet) {
        computeCrossSets(row, board, fieldSet.crossSets());
        computeAnchors(column, board, fieldSet.anchors());
    }

    public Set<Field> computeAllAnchors(Board board) {
        Set<Field> anchors = new HashSet<>();
        for (int i = 0; i < board.length(); i++) {
            computeAnchors(i, board, anchors);
        }

        return anchors;
    }

    public Set<Field> computeAnchors(int column, Board board) {
        Set<Field> anchors = new HashSet<>();
        computeAnchors(column, board, anchors);

        return anchors;
    }

    public void computeAnchors(int column, Board board, Set<Field> existingAnchors) {
        for (int row = 0; row < board.length(); row++) {
            if (!board.isEmpty(row, column) || board.hasLettersAbove(row, column) || board.hasLettersBelow(row, column)) {
                existingAnchors.add(new Field(row, column));
            } else {
                existingAnchors.remove(new Field(row, column));
            }
        }
    }

    public int[][] computeAllCrossSets(Board board) {
        int[][] crossSets = new int[board.length()][board.length()];
        for (int i = 0; i < board.length(); i++) {
            computeCrossSets(i, board, crossSets);
        }

        return crossSets;
    }

    public int[][] computeCrossSets(int row, Board board) {
        int[][] crossSets = new int[board.length()][board.length()];
        computeCrossSets(row, board, crossSets);

        return crossSets;
    }

    public void computeCrossSets(int row, Board board, int[][] existingCrossSets) {
        for (int column = 0; column < board.length(); column++) {
            if (!board.isEmpty(row, column)) {
                existingCrossSets[row][column] = 0;
                continue;
            }

            boolean hasLettersAbove = board.hasLettersAbove(row, column);
            boolean hasLettersBelow = board.hasLettersBelow(row, column);

            if (hasLettersAbove && hasLettersBelow) {
                // result - reversed word with delimiter at the tail
                String aboveWord = board.readWordUpwards(row - 1, column, delimiter);
                // result - word WITHOUT delimiter at the tail
                String belowWord = board.readWordDownwards(row + 1, column, false);
                existingCrossSets[row][column] = gaddag.getOneLetterCompletion(aboveWord, belowWord);
            } else if (hasLettersAbove) {
                // result - reversed word with delimiter at the tail
                String word = board.readWordUpwards(row - 1, column, delimiter);
                existingCrossSets[row][column] = gaddag.getOneLetterCompletion(word);
            } else if (hasLettersBelow) {
                // result - reversed word WITHOUT delimiter at the tail
                String word = board.readWordDownwards(row + 1, column, true);
                existingCrossSets[row][column] = gaddag.getOneLetterCompletion(word);
            } else {
                // allow all letters
                existingCrossSets[row][column] = -1;
            }
        }
    }
}
