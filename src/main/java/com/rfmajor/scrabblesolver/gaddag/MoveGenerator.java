package com.rfmajor.scrabblesolver.gaddag;

import com.rfmajor.scrabblesolver.common.Alphabet;
import com.rfmajor.scrabblesolver.common.Board;
import com.rfmajor.scrabblesolver.common.CrossSetCalculator;
import com.rfmajor.scrabblesolver.common.Rack;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Setter
public class MoveGenerator {
    private final Board board;
    private final CrossSetCalculator crossSetCalculator;
    private final Alphabet alphabet;
    private final Gaddag gaddag;
    @Value("${gaddag.delimiter}")
    private char delimiter;

    public List<Move> generate(int row, int column, Rack rack) {
        List<Move> moves = new ArrayList<>();
        generate(0, row, column, "", rack, gaddag.getParentArc(), moves);
        return moves;
    }

    /**
     * Method for left-right move generation
     * 'offset' is an offset from the anchor square
    **/
    private void generate(int offset, int row, int column, String word, Rack rack, Arc arc, List<Move> moves) {
        if (board.isOccupiedByLetter(row, column + offset)) {
            char letter = board.getField(row, column + offset);
            goOn(offset, row, column, letter, word, rack, arc.getNextArc(letter), arc, moves);
        }
        else if (!rack.isEmpty()) {
            for (char letter : rack.getAllowedLetters(crossSetCalculator.getCrossSet(row, column + offset), alphabet)) {
                goOn(offset, row, column, letter, word, rack.withRemovedLetter(letter), arc.getNextArc(letter), arc, moves);
            }
            if (rack.contains(Rack.BLANK)) {
                for (char letter : arc.getNextAllowedLetters(crossSetCalculator.getCrossSet(row, column + offset))) {
                    goOn(offset, row, column, letter, word, rack.withRemovedLetter(Rack.BLANK), arc.getNextArc(letter), arc, moves);
                }
            }
        }
    }

    private void goOn(int offset, int row, int column, char letter, String word, Rack rack, Arc newArc, Arc oldArc, List<Move> moves) {
        if (offset <= 0) {
            word = letter + word;
            if (oldArc.containsLetter(letter, alphabet) && !board.isOccupiedByLetter(row, column + offset - 1)) {
                recordPlay(word, row, column + offset, moves);
            }
            if (newArc != null) {
                if (/*if room to the left*/ board.isValid(row, column + offset - 1)) {
                    generate(offset - 1, row, column, word, rack, newArc, moves);
                }
                newArc = newArc.getNextArc(delimiter);
                // if newArc != 0 && no letter directly left && room to the right
                if (newArc != null && !board.isOccupiedByLetter(row, column + offset - 1) && board.isValid(row, column + 1)) {
                    generate(1, row, column, word, rack, newArc, moves);
                }
            }
        } else {
            word = word + letter;
            // oldArc.getLetter() == letter && no letter directly right
            if (oldArc.containsLetter(letter, alphabet) && !board.isOccupiedByLetter(row, column + offset + 1)) {
                recordPlay(word, row,column + offset + 1 - word.length(), moves);
            }
            // newArc != 0 && room to the right
            if (newArc != null ) {
                generate(offset + 1, row, column, word, rack, newArc, moves);
            }
        }
    }

    private void recordPlay(String word, int x, int y, List<Move> moves) {
        moves.add(new Move(word, x, y));
    }
}
