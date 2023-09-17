package com.rfmajor.scrabblesolver.simulator;

import com.rfmajor.scrabblesolver.common.game.Board;
import com.rfmajor.scrabblesolver.common.game.Direction;
import com.rfmajor.scrabblesolver.common.game.Move;
import com.rfmajor.scrabblesolver.common.game.Rack;
import lombok.Data;


@Data
public class Player {
    private final Game game;
    private final Rack rack;

    public void play(Move move) {
        Board board = game.getBoard();
        int row = move.getX();
        int column = move.getY();
        char[] word = move.getWord().toCharArray();
        int i = 0;
        while (row < board.length() && column < board.length()) {
            char letter = word[i];
            if (board.isEmpty()) {
                board.addLetter(letter, row, column);
                rack.removeLetter(letter);
            }
            if (move.getDirection() == Direction.ACROSS) {
                row++;
            } else {
                column++;
            }
            i++;
        }

    }

    private void drawLetters() {

    }


}
