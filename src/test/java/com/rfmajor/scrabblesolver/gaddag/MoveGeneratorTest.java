package com.rfmajor.scrabblesolver.gaddag;

import com.rfmajor.scrabblesolver.TestUtils;
import com.rfmajor.scrabblesolver.common.Alphabet;
import com.rfmajor.scrabblesolver.common.Board;
import com.rfmajor.scrabblesolver.common.CrossSetCalculator;
import com.rfmajor.scrabblesolver.common.Rack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collections;
import java.util.List;

import static com.rfmajor.scrabblesolver.TestUtils.addWordToBoardVertically;
import static org.junit.jupiter.api.Assertions.*;

class MoveGeneratorTest {
    private Gaddag gaddag;
    private GaddagConverter gaddagConverter;
    private Alphabet alphabet;
    private Board board;
    private CrossSetCalculator crossSetCalculator;
    private MoveGenerator moveGenerator;
    private boolean initialized;

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
                    List.of("able", "cable", "care", "abler", "ar", "be"),
                    alphabet);
            gaddag = new Gaddag(parentArc, alphabet);
            crossSetCalculator = new CrossSetCalculator(board, alphabet, gaddag);
            crossSetCalculator.setDelimiter('#');
            moveGenerator = new MoveGenerator(board, crossSetCalculator, alphabet, gaddag);
            moveGenerator.setDelimiter('#');
            initialized = true;
        }
    }

    @ParameterizedTest
    @CsvSource({"3, 1", "4, 2", "5, 1", "6, 0", "7, 1", "8, 2"})
    void givenBoardWithVerticalWord_whenGenerate_thenReturnAllPossibleMoves(int row, int expectedSize) {
        addWordToBoardVertically("able", 4, 4, board);
        for (int i = 0; i < board.length(); i++) {
            crossSetCalculator.computeCrossSets(i);
        }
        List<Move> moves = moveGenerator.generate(row, 4, new Rack("care"));
        moves.forEach(move -> System.out.println(move.toString()));
        assertEquals(expectedSize, moves.size());
    }

    @Test
    void givenEmptyBoard_whenGenerate_thenReturnAllPossibleMoves() {
        for (int i = 0; i < board.length(); i++) {
            crossSetCalculator.computeCrossSets(i);
        }
        List<Move> moves = moveGenerator.generate(board.length() / 2, board.length() / 2, new Rack("care"));
        moves.forEach(move -> System.out.println(move.toString()));
        assertEquals(6, moves.size());
    }
}