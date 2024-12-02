package com.rfmajor.scrabblesolver.common.scrabble;

import com.rfmajor.scrabblesolver.common.scrabble.Board;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardTest {
    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    void givenEmptyBoard_whenAddLetter_thenNoExceptionsThrown() {
        assertDoesNotThrow(() -> board.addLetter('c', 1, 2));
    }

    @Test
    void givenBoardWithAddedLetter_whenAddLetterToAlreadyOccupiedField_thenThrowException() {
        board.addLetter('f', 1, 2);
        assertThrows(Board.LetterAlreadyPresentException.class,
                () -> board.addLetter('c', 1, 2));
    }

    @Test
    void givenBoardWithAddedLetter_whenGetLetter_thenLetterPresent() {
        board.addLetter('c', 1, 2);
        assertEquals('c', board.getField(1, 2));
    }

    @Test
    void givenBoardWithNoLetters_whenIsEmpty_thenReturnTrue() {
        assertTrue(board.isEmpty(1, 2));
    }

    @Test
    void givenBoardWithAddedLetter_whenIsEmpty_thenReturnFalse() {
        board.addLetter('c', 1, 2);
        assertFalse(board.isEmpty(1, 2));
    }
}