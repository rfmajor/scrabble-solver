package com.rfmajor.scrabblesolver.common.gaddag.calculate;

import com.rfmajor.scrabblesolver.common.TestUtils;
import com.rfmajor.scrabblesolver.common.gaddag.convert.GaddagConverter;
import com.rfmajor.scrabblesolver.common.gaddag.convert.SimpleGaddagConverter;
import com.rfmajor.scrabblesolver.common.gaddag.model.Arc;
import com.rfmajor.scrabblesolver.common.gaddag.model.Gaddag;
import com.rfmajor.scrabblesolver.common.gaddag.utils.BitSetUtils;
import com.rfmajor.scrabblesolver.common.scrabble.Alphabet;
import com.rfmajor.scrabblesolver.common.scrabble.Board;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CrossSetCalculatorTest {
    private Alphabet alphabet;
    private Board board;
    private CrossSetCalculator<Arc> crossSetCalculator;

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
        Gaddag<Arc> gaddag = gaddagObjectConverter.convert(
                List.of("pa", "able", "payable", "parable", "pay", "par", "part", "park", "cable"),
                alphabet);
        crossSetCalculator = new CrossSetCalculator<>(gaddag);
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
        TestUtils.addWordToBoardVertically(word, row, column, board);

        FieldSet fieldSet = crossSetCalculator.computeCrossSetsAndAnchors(row + word.length(), column, board);

        int crossSet = fieldSet.getCrossSet(row + word.length(), column);
        assertTrue(BitSetUtils.containsOnly(crossSet, alphabet.getIndex('k'), alphabet.getIndex('t')));
    }

    @Test
    void givenWord_whenComputeCrossSetsAbove_thenReturnCorrectCrossSet() {
        String word = "able";
        TestUtils.addWordToBoardVertically(word, ROW, COLUMN, board);

        FieldSet fieldSet = crossSetCalculator.computeCrossSetsAndAnchors(ROW - 1, COLUMN, board);

        int crossSet = fieldSet.getCrossSet(ROW - 1, COLUMN);
        assertTrue(BitSetUtils.containsOnly(crossSet, alphabet.getIndex('c')));
    }

    @Test
    void given2Words_whenComputeCrossSets_thenReturnCorrectCrossSet() {
        String firstWord = "pa";
        String secondWord = "able";
        TestUtils.addWordToBoardVertically(firstWord, ROW, COLUMN, board);
        TestUtils.addWordToBoardVertically(secondWord, ROW + firstWord.length() + 1, COLUMN, board);

        FieldSet fieldSet = crossSetCalculator.computeCrossSetsAndAnchors(ROW + firstWord.length(), COLUMN, board);

        int crossSet = fieldSet.getCrossSet(ROW + firstWord.length(), COLUMN);
        assertTrue(BitSetUtils.containsOnly(crossSet, alphabet.getIndex('y'), alphabet.getIndex('r')));
    }

    @Test
    void givenNonCompletableWord_whenComputeCrossSetsBelow_thenReturn0() {
        String word = "payable";
        TestUtils.addWordToBoardVertically(word, ROW, COLUMN, board);

        FieldSet fieldSet = crossSetCalculator.computeCrossSetsAndAnchors(ROW + word.length(), COLUMN, board);

        int crossSet = fieldSet.getCrossSet(ROW + word.length(), COLUMN);
        assertEquals(0, crossSet);
    }

    @Test
    void givenNonCompletableWord_whenComputeCrossSetsAbove_thenReturn0() {
        String word = "part";
        TestUtils.addWordToBoardVertically(word, ROW, COLUMN, board);

        FieldSet fieldSet = crossSetCalculator.computeCrossSetsAndAnchors(ROW - 1, COLUMN, board);

        int crossSet = fieldSet.getCrossSet(ROW - 1, COLUMN);
        assertEquals(0, crossSet);
    }

    @Test
    void givenWord_whenComputeAnchors_thenReturnCorrectVerticalAnchors() {
        String word = "part";
        TestUtils.addWordToBoardVertically(word, ROW, COLUMN, board);

        FieldSet fieldSet = crossSetCalculator.computeCrossSetsAndAnchors(ROW, COLUMN, board);

        assertTrue(areAnchorsPresent(word, ROW, fieldSet));
    }

    @Test
    void givenWordStartingFrom0Row_whenComputeAnchors_thenReturnCorrectVerticalAnchors() {
        String word = "part";
        TestUtils.addWordToBoardVertically(word, 0, COLUMN, board);

        FieldSet fieldSet = crossSetCalculator.computeCrossSetsAndAnchors(ROW, COLUMN, board);

        assertTrue(areAnchorsPresent(word, 0, fieldSet));
    }

    private boolean areAnchorsPresent(String word, int row, FieldSet fieldSet) {
        for (int i = Math.max(0, row - 1); i < row + word.length() + 1; i++) {
            if (!fieldSet.isAnchor(i, COLUMN)) {
                return false;
            }
        }
        return true;
    }
}