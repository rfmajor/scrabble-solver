package com.rfmajor.scrabblesolver.common;

import com.rfmajor.scrabblesolver.common.game.Alphabet;
import com.rfmajor.scrabblesolver.common.game.Board;
import com.rfmajor.scrabblesolver.common.game.Direction;
import com.rfmajor.scrabblesolver.common.game.Move;
import com.rfmajor.scrabblesolver.common.game.Rack;
import com.rfmajor.scrabblesolver.common.game.SpecialFields;
import com.rfmajor.scrabblesolver.gaddag.MoveGeneratorFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class PointCalculator {
    private final SpecialFields specialFields;

    public static final int FULL_RACK_BONUS = 50;

    public void calculatePoints(Set<Move> moves, MoveGeneratorFacade moveGenerator, Rack rack) {
        Set<Move> invalidMoves = new HashSet<>();
        moves.forEach(move -> calculatePoints(
                move, moveGenerator.getBoard(),
                moveGenerator.getTransposedBoard(),
                moveGenerator.getAlphabet(),
                rack, invalidMoves
        ));
        moves.removeAll(invalidMoves);
    }

    private void calculatePoints(Move move, Board board, Board transposedBoard, Alphabet alphabet,
                                 Rack rack, Set<Move> invalidMoves) {
        if (move.getDirection() == Direction.DOWN) {
            board = transposedBoard;
        }
        char[] word = move.getWord().toCharArray();
        int points = 0;
        int wordMultiplier = 1;
        int wordPoints = 0;
        int newlyPopulatedFields = 0;
        for (int i = 0; i < word.length; i++) {
            char letter = word[i];
            int letterPoints = alphabet.getPoints(letter);
            int crossWordMultiplier = 1;
            int x = getXBasedOnMoveDirection(move);
            int y = getYBasedOnMoveDirection(move) + i;
            if (board.isEmpty(x, y)) {
                newlyPopulatedFields++;
                if (specialFields.isDoubleLetter(x, y)) {
                    letterPoints *= 2;
                } else if (specialFields.isTripleLetter(x, y)) {
                    letterPoints *= 3;
                } else if (specialFields.isDoubleWord(x, y)) {
                    wordMultiplier *= 2;
                    crossWordMultiplier *= 2;
                } else if (specialFields.isTripleWord(x, y)) {
                    wordMultiplier *= 3;
                    crossWordMultiplier *= 3;
                }
                points += calculateCrossWordPoints(x, y, letterPoints, crossWordMultiplier, board, alphabet);
            }
            wordPoints += letterPoints;

        }
        if (newlyPopulatedFields == 0) {
            invalidMoves.add(move);
            return;
        }
        points += wordPoints * wordMultiplier;
        if (newlyPopulatedFields >= rack.getMaxSize()) {
            points += FULL_RACK_BONUS;
        }
        move.setPoints(points);
    }

    private int calculateCrossWordPoints(int x, int y, int letterPoints, int crossWordMultiplier,
                                         Board board, Alphabet alphabet) {
        int crossWordPoints = 0;
        if (board.hasLettersAbove(x, y) && board.hasLettersBelow(x, y)) {
            crossWordPoints += letterPoints;
            String aboveWord = board.readWordUpwards(x - 1, y, false);
            String belowWord = board.readWordDownwards(x + 1, y, false);
            crossWordPoints += calculateRawPoints(aboveWord, alphabet);
            crossWordPoints += calculateRawPoints(belowWord, alphabet);
            crossWordPoints *= crossWordMultiplier;
        }
        else if (board.hasLettersAbove(x, y)) {
            crossWordPoints += letterPoints;
            String aboveWord = board.readWordUpwards(x - 1, y, false);
            crossWordPoints += calculateRawPoints(aboveWord, alphabet);
            crossWordPoints *= crossWordMultiplier;
        }
        else if (board.hasLettersBelow(x, y)) {
            crossWordPoints += letterPoints;
            String belowWord = board.readWordDownwards(x + 1, y, false);
            crossWordPoints += calculateRawPoints(belowWord, alphabet);
            crossWordPoints *= crossWordMultiplier;
        }
        return crossWordPoints;
    }

    private int calculateRawPoints(String word, Alphabet alphabet) {
        int points = 0;
        for (int i = 0; i < word.length(); i++) {
            points += alphabet.getPoints(word.charAt(i));
        }
        return points;
    }

    private int getXBasedOnMoveDirection(Move move) {
        return move.getDirection() == Direction.ACROSS ? move.getX() : move.getY();
    }

    private int getYBasedOnMoveDirection(Move move) {
        return move.getDirection() == Direction.ACROSS ? move.getY() : move.getX();
    }
}
