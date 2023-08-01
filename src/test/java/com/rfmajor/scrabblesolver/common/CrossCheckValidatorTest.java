package com.rfmajor.scrabblesolver.common;

import com.rfmajor.scrabblesolver.TestUtils;
import com.rfmajor.scrabblesolver.gaddag.Arc;
import com.rfmajor.scrabblesolver.gaddag.Gaddag;
import com.rfmajor.scrabblesolver.gaddag.GaddagConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.rfmajor.scrabblesolver.TestUtils.addWordToBoardVertically;
import static org.junit.jupiter.api.Assertions.*;

class CrossCheckValidatorTest {
    private Gaddag gaddag;
    private GaddagConverter gaddagConverter;
    private Alphabet alphabet;
    private Board board;
    private CrossCheckValidator crossCheckValidator;
    private boolean initialized;

    private static final int ROW = 1;
    private static final int COLUMN = 3;

    @BeforeEach
    void setUp() {
        board = new Board();
        if (!initialized) {
            alphabet = new Alphabet(
                    TestUtils.mapStringToLettersList("abcdefghijklmnopqrstuvwxyz"),
                    Collections.emptyList(),
                    Collections.emptyList()
            );
            gaddagConverter = new GaddagConverter();
            gaddagConverter.setDelimiter('#');
            Arc parentArc = gaddagConverter.convert(
                    List.of("pa", "able", "payable", "parable", "pay", "par", "part", "park", "cable"),
                    alphabet);
            gaddag = new Gaddag(parentArc, alphabet);
            crossCheckValidator = new CrossCheckValidator(board, alphabet, gaddag);
            crossCheckValidator.setDelimiter('#');
            initialized = true;
        }
    }

    @Test
    void givenWord_whenComputeCrossSetsBelow_thenReturnCorrectCrossSet() {
        int row = 1;
        int column = 3;
        String word = "par";
        addWordToBoardVertically(word, row, column, board);
        crossCheckValidator.computeCrossSets(row + word.length());
        int crossSet = crossCheckValidator.getCrossSet(row + word.length(), column);
        assertTrue(BitSetUtils.containsOnly(crossSet, alphabet.getIndex('k'), alphabet.getIndex('t')));
    }

    @Test
    void givenWord_whenComputeCrossSetsAbove_thenReturnCorrectCrossSet() {
        String word = "able";
        addWordToBoardVertically(word, ROW, COLUMN, board);
        crossCheckValidator.computeCrossSets(ROW - 1);
        int crossSet = crossCheckValidator.getCrossSet(ROW - 1, COLUMN);
        assertTrue(BitSetUtils.containsOnly(crossSet, alphabet.getIndex('c')));
    }

    @Test
    void given2Words_whenComputeCrossSets_thenReturnCorrectCrossSet() {
        String firstWord = "pa";
        String secondWord = "able";
        addWordToBoardVertically(firstWord, ROW, COLUMN, board);
        addWordToBoardVertically(secondWord, ROW + firstWord.length() + 1, COLUMN, board);
        crossCheckValidator.computeCrossSets(ROW + firstWord.length());
        int crossSet = crossCheckValidator.getCrossSet(ROW + firstWord.length(), COLUMN);
        assertTrue(BitSetUtils.containsOnly(crossSet, alphabet.getIndex('y'), alphabet.getIndex('r')));
    }

    @Test
    void givenNonCompletableWord_whenComputeCrossSetsBelow_thenReturn0() {
        String word = "payable";
        addWordToBoardVertically(word, ROW, COLUMN, board);
        crossCheckValidator.computeCrossSets(ROW + word.length());
        int crossSet = crossCheckValidator.getCrossSet(ROW + word.length(), COLUMN);
        assertEquals(0, crossSet);
    }

    @Test
    void givenNonCompletableWord_whenComputeCrossSetsAbove_thenReturn0() {
        String word = "part";
        addWordToBoardVertically(word, ROW, COLUMN, board);
        crossCheckValidator.computeCrossSets(ROW - 1);
        int crossSet = crossCheckValidator.getCrossSet(ROW - 1, COLUMN);
        assertEquals(0, crossSet);
    }

    @Test
    void givenWord_whenComputeAnchors_thenReturnCorrectVerticalAnchors() {
        String word = "part";
        addWordToBoardVertically(word, ROW, COLUMN, board);
        crossCheckValidator.computeAnchors(COLUMN);
        assertTrue(areAnchorsPresent(word, ROW));
    }

    @Test
    void givenWordStartingFrom0Row_whenComputeAnchors_thenReturnCorrectVerticalAnchors() {
        String word = "part";
        addWordToBoardVertically(word, 0, COLUMN, board);
        crossCheckValidator.computeAnchors(COLUMN);
        assertTrue(areAnchorsPresent(word, 0));
    }

    private boolean areAnchorsPresent(String word, int row) {
        for (int i = Math.max(0, row - 1); i < row + word.length() + 1; i++) {
            if (!crossCheckValidator.isAnchor(i, COLUMN)) {
                return false;
            }
        }
        return true;
    }
}