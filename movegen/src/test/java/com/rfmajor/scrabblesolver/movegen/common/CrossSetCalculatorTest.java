package com.rfmajor.scrabblesolver.movegen.common;

import com.rfmajor.scrabblesolver.movegen.common.model.Alphabet;
import com.rfmajor.scrabblesolver.movegen.common.model.Board;
import com.rfmajor.scrabblesolver.movegen.gaddag.Arc;
import com.rfmajor.scrabblesolver.movegen.gaddag.Gaddag;
import com.rfmajor.scrabblesolver.movegen.gaddag.GaddagConverter;
import com.rfmajor.scrabblesolver.movegen.gaddag.SimpleGaddagConverter;
import com.rfmajor.scrabblesolver.movegen.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Collections;
import java.util.List;

import static com.rfmajor.scrabblesolver.movegen.utils.TestUtils.addWordToBoardVertically;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CrossSetCalculatorTest {
    private Gaddag<Arc> gaddag;
    private Alphabet alphabet;
    private Board board;
    private CrossSetCalculator crossSetCalculator;

    private static final int ROW = 1;
    private static final int COLUMN = 3;

    @BeforeAll
    void setUpAll() {
        board = new Board();
        alphabet = new Alphabet(
                TestUtils.mapStringToLettersList("abcdefghijklmnopqrstuvwxyz#"),
                Collections.emptyList(),
                Collections.emptyList()
        );
        GaddagConverter<Arc> gaddagObjectConverter = new SimpleGaddagConverter();
        gaddag = gaddagObjectConverter.convert(
                List.of("pa", "able", "payable", "parable", "pay", "par", "part", "park", "cable"),
                alphabet);
        crossSetCalculator = new CrossSetCalculator(board, gaddag);
    }

    @BeforeEach
    void setUp() {
        board.clear();
    }

    @Test
    void givenWord_whenComputeCrossSetsBelow_thenReturnCorrectCrossSet() {
        int row = 1;
        int column = 3;
        String word = "par";
        addWordToBoardVertically(word, row, column, board);
        crossSetCalculator.computeCrossSets(row + word.length());
        int crossSet = crossSetCalculator.getCrossSet(row + word.length(), column);
        assertTrue(BitSetUtils.containsOnly(crossSet, alphabet.getIndex('k'), alphabet.getIndex('t')));
    }

    @Test
    void givenWord_whenComputeCrossSetsAbove_thenReturnCorrectCrossSet() {
        String word = "able";
        addWordToBoardVertically(word, ROW, COLUMN, board);
        crossSetCalculator.computeCrossSets(ROW - 1);
        int crossSet = crossSetCalculator.getCrossSet(ROW - 1, COLUMN);
        assertTrue(BitSetUtils.containsOnly(crossSet, alphabet.getIndex('c')));
    }

    @Test
    void given2Words_whenComputeCrossSets_thenReturnCorrectCrossSet() {
        String firstWord = "pa";
        String secondWord = "able";
        addWordToBoardVertically(firstWord, ROW, COLUMN, board);
        addWordToBoardVertically(secondWord, ROW + firstWord.length() + 1, COLUMN, board);
        crossSetCalculator.computeCrossSets(ROW + firstWord.length());
        int crossSet = crossSetCalculator.getCrossSet(ROW + firstWord.length(), COLUMN);
        assertTrue(BitSetUtils.containsOnly(crossSet, alphabet.getIndex('y'), alphabet.getIndex('r')));
    }

    @Test
    void givenNonCompletableWord_whenComputeCrossSetsBelow_thenReturn0() {
        String word = "payable";
        addWordToBoardVertically(word, ROW, COLUMN, board);
        crossSetCalculator.computeCrossSets(ROW + word.length());
        int crossSet = crossSetCalculator.getCrossSet(ROW + word.length(), COLUMN);
        assertEquals(0, crossSet);
    }

    @Test
    void givenNonCompletableWord_whenComputeCrossSetsAbove_thenReturn0() {
        String word = "part";
        addWordToBoardVertically(word, ROW, COLUMN, board);
        crossSetCalculator.computeCrossSets(ROW - 1);
        int crossSet = crossSetCalculator.getCrossSet(ROW - 1, COLUMN);
        assertEquals(0, crossSet);
    }

    @Test
    void givenWord_whenComputeAnchors_thenReturnCorrectVerticalAnchors() {
        String word = "part";
        addWordToBoardVertically(word, ROW, COLUMN, board);
        crossSetCalculator.computeAnchors(COLUMN);
        assertTrue(areAnchorsPresent(word, ROW));
    }

    @Test
    void givenWordStartingFrom0Row_whenComputeAnchors_thenReturnCorrectVerticalAnchors() {
        String word = "part";
        addWordToBoardVertically(word, 0, COLUMN, board);
        crossSetCalculator.computeAnchors(COLUMN);
        assertTrue(areAnchorsPresent(word, 0));
    }

    private boolean areAnchorsPresent(String word, int row) {
        for (int i = Math.max(0, row - 1); i < row + word.length() + 1; i++) {
            if (!crossSetCalculator.isAnchor(i, COLUMN)) {
                return false;
            }
        }
        return true;
    }
}