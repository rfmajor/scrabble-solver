package com.rfmajor.scrabblesolver.gaddag;

import com.rfmajor.scrabblesolver.common.BitSetUtils;
import com.rfmajor.scrabblesolver.common.CrossSetCalculator;
import com.rfmajor.scrabblesolver.common.game.Alphabet;
import com.rfmajor.scrabblesolver.common.game.Board;
import com.rfmajor.scrabblesolver.common.game.Direction;
import com.rfmajor.scrabblesolver.common.game.Move;
import com.rfmajor.scrabblesolver.common.game.Rack;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for the move generation on the scrabble board
 */
@Setter
@Getter
public class MoveAlgorithmExecutor<A> {
    private final Board board;
    private final CrossSetCalculator crossSetCalculator;
    private final Alphabet alphabet;
    private final Gaddag<A> gaddag;
    private char delimiter;
    private Direction moveDirection;

    public MoveAlgorithmExecutor(Board board, Gaddag<A> gaddag, Direction moveDirection) {
        this.board = board;
        this.crossSetCalculator = new CrossSetCalculator(board, gaddag);
        this.alphabet = gaddag.getAlphabet();
        this.gaddag = gaddag;
        this.delimiter = gaddag.getDelimiter();
        this.moveDirection = moveDirection;
    }

    public void computeAllCrossSets() {
        for (int i = 0; i < board.length(); i++) {
            crossSetCalculator.computeCrossSets(i);
        }
    }

    public List<Move> generate(int row, int column, Rack rack) {
        List<Move> moves = new ArrayList<>();
        generate(0, row, column, new Word(), rack, gaddag.getRootArc(), moves);
        return moves;
    }

    /**
     * Generates the moves from left to right using the GADDAG move generation algorithm created by Steven Gordon.
     *
     * @param offset - offset from the anchor square
     * @param row - starting row
     * @param column - starting column
     * @param word - word being generated
     * @param rack - rack which is used for the computation
     * @param arc - starting arc
     * @param moves - list of moves which the algorithm will append to
     **/
    private void generate(int offset, int row, int column, Word word, Rack rack, A arc, List<Move> moves) {
        if (board.isOccupiedByLetter(row, column + offset)) {
            char letter = board.getField(row, column + offset);
            goOn(offset, row, column, letter, word, rack, gaddag.findNextArc(arc, letter), arc, moves, false);
        }
        else if (!rack.isEmpty()) {
            for (char letter : rack.getAllowedLetters(crossSetCalculator.getCrossSet(row, column + offset), alphabet)) {
                goOn(offset, row, column, letter, word, rack.withRemovedLetter(letter), gaddag.findNextArc(arc, letter), arc, moves, false);
            }
            if (rack.contains(Rack.BLANK)) {
                for (char letter : alphabet.getAllowedLetters(crossSetCalculator.getCrossSet(row, column + offset))) {
                    goOn(offset, row, column, letter, word, rack.withRemovedLetter(Rack.BLANK), gaddag.findNextArc(arc, letter), arc, moves, true);
                }
            }
        }
    }

    private void goOn(int offset, int row, int column, char letter, Word word, Rack rack,
                      A newArc, A oldArc, List<Move> moves, boolean isBlankLetter) {
        if (offset <= 0) {
            word = word.prependLetter(letter, isBlankLetter);
            if (gaddag.containsLetter(oldArc, letter) && !board.isOccupiedByLetter(row, column + offset - 1)
                    && !board.isOccupiedByLetter(row, column + offset + word.length())) {
                recordPlay(word, row, column + offset, moves);
            }
            if (gaddag.isPresent(newArc)) {
                if (/*if room to the left*/ board.isValid(row, column + offset - 1)) {
                    generate(offset - 1, row, column, word, rack, newArc, moves);
                }
                newArc = gaddag.findNextArc(newArc, delimiter);
                // if newArc != 0 && no letter directly left && room to the right
                if (gaddag.isPresent(newArc) && !board.isOccupiedByLetter(row, column + offset - 1) && board.isValid(row, column + 1)) {
                    generate(1, row, column, word, rack, newArc, moves);
                }
            }
        } else {
            word = word.appendLetter(letter, isBlankLetter);
            // oldArc.getLetter() == letter && no letter directly right
            if (gaddag.containsLetter(oldArc, letter) && !board.isOccupiedByLetter(row, column + offset + 1)) {
                recordPlay(word, row,column + offset + 1 - word.length(), moves);
            }
            // newArc != 0 && room to the right
            if (gaddag.isPresent(newArc) && board.isValid(row, column + offset + 1)) {
                generate(offset + 1, row, column, word, rack, newArc, moves);
            }
        }
    }

    private void recordPlay(Word word, int x, int y, List<Move> moves) {
        if (moveDirection == Direction.ACROSS) {
            moves.add(new Move(word.getLetters(), moveDirection, x, y, BitSetUtils.toSet(word.getBlanks())));
        }
        else if (moveDirection == Direction.DOWN) {
            moves.add(new Move(word.getLetters(), moveDirection, y, x, BitSetUtils.toSet(word.getBlanks())));
        }
    }

    @Data
    @AllArgsConstructor
    private static class Word {
        private String letters;
        private int blanks;

        public Word() {
            letters = "";
            blanks = 0;
        }

        public Word appendLetter(char letter, boolean blank) {
            int newBlanks = blank ? BitSetUtils.addToSet(blanks, letters.length()) : blanks;
            return new Word(letters + letter, newBlanks);
        }

        public Word prependLetter(char letter, boolean blank) {
            int newBlanks = blank ? BitSetUtils.addToSet(blanks << 1, 0) : blanks << 1;
            return new Word(letter + letters, newBlanks);
        }

        public int length() {
            return letters.length();
        }
    }
}
