package com.rfmajor.scrabblesolver.common.gaddag.calculate;

import com.rfmajor.scrabblesolver.common.gaddag.model.Gaddag;
import com.rfmajor.scrabblesolver.common.gaddag.utils.BitSetUtils;
import com.rfmajor.scrabblesolver.common.scrabble.Alphabet;
import com.rfmajor.scrabblesolver.common.scrabble.Board;
import com.rfmajor.scrabblesolver.common.scrabble.Direction;
import com.rfmajor.scrabblesolver.common.scrabble.Field;
import com.rfmajor.scrabblesolver.common.scrabble.Move;
import com.rfmajor.scrabblesolver.common.scrabble.MoveGroup;
import com.rfmajor.scrabblesolver.common.scrabble.Rack;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class responsible for the move generation on the scrabble board
 */
public class MoveGenerator<A> {
    @Getter
    private final Alphabet alphabet;
    @Getter
    private final Gaddag<A> gaddag;
    @Getter
    private final char delimiter;
    private final MovePostProcessor movePostProcessor;
    private final PointCalculator pointCalculator;
    private final CrossSetCalculator<A> crossSetCalculator;

    public MoveGenerator(Gaddag<A> gaddag, MovePostProcessor movePostProcessor,
                         PointCalculator pointCalculator, CrossSetCalculator<A> crossSetCalculator) {
        this.alphabet = gaddag.getAlphabet();
        this.gaddag = gaddag;
        this.delimiter = gaddag.getDelimiter();
        this.movePostProcessor = movePostProcessor;
        this.pointCalculator = pointCalculator;
        this.crossSetCalculator = crossSetCalculator;
    }

    public List<MoveGroup> generateAllPossibleMoves(Rack rack, Board board) {
        Set<Move> moves = new HashSet<>();
        generateAllPossibleMoves(moves, rack, board, Direction.ACROSS);
        generateAllPossibleMoves(moves, rack, board, Direction.DOWN);

        List<Move> movesWithPoints = pointCalculator.calculatePoints(moves, board, alphabet, rack);
        return movePostProcessor.groupMovesByNameAndPoints(movesWithPoints);
    }

    public List<MoveGroup> generateGroups(int row, int column, Rack rack, Board board,
                                    FieldSet fieldSet, Direction moveDirection) {
        List<Move> moves = generate(row, column, rack, board, fieldSet, moveDirection);
        return movePostProcessor.groupMovesByNameAndPoints(moves);
    }

    public List<Move> generate(int row, int column, Rack rack, Board board,
                               FieldSet fieldSet, Direction moveDirection) {
        InternalMoveExecutor internalMoveExecutor = new InternalMoveExecutor(board, fieldSet, moveDirection);
        List<Move> moves = internalMoveExecutor.generateMoves(0, row, column, new Word(), rack, gaddag.getRootArc());
        return pointCalculator.calculatePoints(moves, board, alphabet, rack);
    }

    private void generateAllPossibleMoves(Set<Move> moves, Rack rack, Board board, Direction moveDirection) {
        board = moveDirection == Direction.ACROSS ? board : board.transpose();
        FieldSet fieldSet = crossSetCalculator.computeAllCrossSetsAndAnchors(board);

        for (Field anchor : fieldSet.anchors()) {
            List<Move> result = generate(anchor.row(), anchor.column(), rack, board, fieldSet, moveDirection);
            moves.addAll(result);
        }
    }

    @RequiredArgsConstructor
    private final class InternalMoveExecutor {
        private final Board board;
        private final FieldSet fieldSet;
        private final Direction moveDirection;
        private final List<Move> moves = new ArrayList<>();

        public List<Move> generateMoves(int offset, int row, int column, Word word, Rack rack, A arc) {
            generate(offset, row , column, word, rack, arc);
            return moves;
        }

        /**
         * Generates the moves from left to right using the GADDAG move generation algorithm created by Steven Gordon.
         *
         * @param offset offset from the anchor square
         * @param row starting row
         * @param column starting column
         * @param word word being generated
         * @param rack rack which is used for the computation
         * @param arc starting arc
         **/
        private void generate(int offset, int row, int column, Word word, Rack rack, A arc) {
            if (board.isOccupiedByLetter(row, column + offset)) {
                char letter = board.getField(row, column + offset);
                boolean isBlank = board.isBlank(row, column + offset);
                goOn(offset, row, column, letter, word, rack, gaddag.findNextArc(arc, letter), arc, moves, isBlank);
            }
            else if (!rack.isEmpty()) {
                for (char letter : rack.getAllowedLetters(fieldSet.getCrossSet(row, column + offset), alphabet)) {
                    goOn(offset, row, column, letter, word, rack.withRemovedLetter(letter), gaddag.findNextArc(arc, letter), arc, moves, false);
                }
                if (rack.contains(Rack.BLANK)) {
                    for (char letter : alphabet.getAllowedLetters(fieldSet.getCrossSet(row, column + offset))) {
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
                        generate(offset - 1, row, column, word, rack, newArc);
                    }
                    newArc = gaddag.findNextArc(newArc, delimiter);
                    // if newArc != 0 && no letter directly left && room to the right
                    if (gaddag.isPresent(newArc) && !board.isOccupiedByLetter(row, column + offset - 1) && board.isValid(row, column + 1)) {
                        generate(1, row, column, word, rack, newArc);
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
                    generate(offset + 1, row, column, word, rack, newArc);
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

    }


    /**
     * Immutable class used for holding the data about the current word and creating other words by adding letters
     */
    @Data
    @AllArgsConstructor
    private static final class Word {
        private final String letters;
        private final int blanks;

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
