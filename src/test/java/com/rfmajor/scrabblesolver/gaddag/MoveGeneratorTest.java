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

import static com.rfmajor.scrabblesolver.TestUtils.addWordToBoardHorizontally;
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
            gaddag = new Gaddag(parentArc, alphabet, gaddagConverter.getDelimiter());
            crossSetCalculator = new CrossSetCalculator(board, gaddag);
            moveGenerator = new MoveGenerator(board, crossSetCalculator, alphabet, gaddag);
            initialized = true;
        }
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

    // vertical initial word

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

    @ParameterizedTest
    @CsvSource({"0, 1", "1, 1", "2, 0", "3, 0", "4, 0"})
    void givenBoardWithVerticalWordInUpperLeftCorner_whenGenerate_thenReturnAllPossibleMoves(int row, int expectedSize) {
        addWordToBoardVertically("able", 0, 0, board);
        for (int i = 0; i < board.length(); i++) {
            crossSetCalculator.computeCrossSets(i);
        }
        List<Move> moves = moveGenerator.generate(row, 0, new Rack("care"));
        moves.forEach(move -> System.out.println(move.toString()));
        assertEquals(expectedSize, moves.size());
    }

    @ParameterizedTest
    @CsvSource({"10, 1", "11, 1", "12, 1", "13, 0", "14, 0"})
    void givenBoardWithVerticalWordInLowerLeftCorner_whenGenerate_thenReturnAllPossibleMoves(int row, int expectedSize) {
        addWordToBoardVertically("able", board.length() - 4, 0, board);
        for (int i = 0; i < board.length(); i++) {
            crossSetCalculator.computeCrossSets(i);
        }
        List<Move> moves = moveGenerator.generate(row, 0, new Rack("care"));
        moves.forEach(move -> System.out.println(move.toString()));
        assertEquals(expectedSize, moves.size());
    }

    @ParameterizedTest
    @CsvSource({"0, 0", "1, 0", "2, 0", "3, 1", "4, 1"})
    void givenBoardWithVerticalWordInUpperRightCorner_whenGenerate_thenReturnAllPossibleMoves(int row, int expectedSize) {
        addWordToBoardVertically("able", 0, board.length() - 1, board);
        for (int i = 0; i < board.length(); i++) {
            crossSetCalculator.computeCrossSets(i);
        }
        List<Move> moves = moveGenerator.generate(row, board.length() - 1, new Rack("care"));
        moves.forEach(move -> System.out.println(move.toString()));
        assertEquals(expectedSize, moves.size());
    }

    @ParameterizedTest
    @CsvSource({"10, 0", "11, 0", "12, 0", "13, 0", "14, 1"})
    void givenBoardWithVerticalWordInLowerRightCorner_whenGenerate_thenReturnAllPossibleMoves(int row, int expectedSize) {
        addWordToBoardVertically("able", board.length() - 4, board.length() - 1, board);
        for (int i = 0; i < board.length(); i++) {
            crossSetCalculator.computeCrossSets(i);
        }
        List<Move> moves = moveGenerator.generate(row, board.length() - 1, new Rack("care"));
        moves.forEach(move -> System.out.println(move.toString()));
        assertEquals(expectedSize, moves.size());
    }

    // horizontal initial word

    @ParameterizedTest
    @CsvSource({"4, 3", "5, 3", "6, 3", "7, 3"})
    void givenBoardWithHorizontalWord_whenGenerate_thenReturnAllPossibleMoves(int column, int expectedSize) {
        addWordToBoardHorizontally("able", 4, 4, board);
        for (int i = 0; i < board.length(); i++) {
            crossSetCalculator.computeCrossSets(i);
        }
        List<Move> moves = moveGenerator.generate(4, column, new Rack("care"));
        moves.forEach(move -> System.out.println(move.toString()));
        assertEquals(expectedSize, moves.size());
    }

    @ParameterizedTest
    @CsvSource({"0, 2", "1, 2", "2, 2", "3, 2"})
    void givenBoardWithHorizontalWordInUpperLeftCorner_whenGenerate_thenReturnAllPossibleMoves(int column, int expectedSize) {
        addWordToBoardHorizontally("able", 0, 0, board);
        for (int i = 0; i < board.length(); i++) {
            crossSetCalculator.computeCrossSets(i);
        }
        List<Move> moves = moveGenerator.generate(0, column, new Rack("care"));
        moves.forEach(move -> System.out.println(move.toString()));
        assertEquals(expectedSize, moves.size());
    }

    @ParameterizedTest
    @CsvSource({"0, 2", "1, 2", "2, 2", "3, 2"})
    void givenBoardWithHorizontalWordInLowerLeftCorner_whenGenerate_thenReturnAllPossibleMoves(int column, int expectedSize) {
        addWordToBoardHorizontally("able", board.length() - 1, 0, board);
        for (int i = 0; i < board.length(); i++) {
            crossSetCalculator.computeCrossSets(i);
        }
        List<Move> moves = moveGenerator.generate(board.length() - 1, column, new Rack("care"));
        moves.forEach(move -> System.out.println(move.toString()));
        assertEquals(expectedSize, moves.size());
    }

    @ParameterizedTest
    @CsvSource({"11, 2", "12, 2", "13, 2", "14, 2"})
    void givenBoardWithHorizontalWordInUpperRightCorner_whenGenerate_thenReturnAllPossibleMoves(int column, int expectedSize) {
        addWordToBoardHorizontally("able", 0, board.length() - 4, board);
        for (int i = 0; i < board.length(); i++) {
            crossSetCalculator.computeCrossSets(i);
        }
        List<Move> moves = moveGenerator.generate(0, column, new Rack("care"));
        moves.forEach(move -> System.out.println(move.toString()));
        assertEquals(expectedSize, moves.size());
    }

    @ParameterizedTest
    @CsvSource({"11, 2", "12, 2", "13, 2", "14, 2"})
    void givenBoardWithHorizontalWordInLowerRightCorner_whenGenerate_thenReturnAllPossibleMoves(int column, int expectedSize) {
        addWordToBoardHorizontally("able", board.length() - 1, board.length() - 4, board);
        for (int i = 0; i < board.length(); i++) {
            crossSetCalculator.computeCrossSets(i);
        }
        List<Move> moves = moveGenerator.generate(board.length() - 1, column, new Rack("care"));
        moves.forEach(move -> System.out.println(move.toString()));
        assertEquals(expectedSize, moves.size());
    }
}