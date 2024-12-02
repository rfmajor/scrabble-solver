package com.rfmajor.scrabblesolver.common.gaddag.calculate;

import com.rfmajor.scrabblesolver.common.scrabble.Alphabet;
import com.rfmajor.scrabblesolver.common.scrabble.Board;
import com.rfmajor.scrabblesolver.common.scrabble.Direction;
import com.rfmajor.scrabblesolver.common.scrabble.Field;
import com.rfmajor.scrabblesolver.common.scrabble.Move;
import com.rfmajor.scrabblesolver.common.scrabble.Rack;
import com.rfmajor.scrabblesolver.common.scrabble.SpecialFields;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class PointCalculator {
    private final SpecialFields specialFields;

    public static final int FULL_RACK_BONUS = 50;

    public <A> void calculatePoints(Set<Move> moves, MoveGenerator<A> moveGenerator, Rack rack, Set<Field> blankFields) {
        Set<Move> invalidMoves = new HashSet<>();
        moves.forEach(move -> calculatePoints(
                move, moveGenerator.getBoard(),
                moveGenerator.getTransposedBoard(),
                moveGenerator.getAlphabet(),
                rack, invalidMoves, blankFields
        ));
        moves.removeAll(invalidMoves);
    }

    private void calculatePoints(Move move, Board board, Board transposedBoard, Alphabet alphabet,
                                 Rack rack, Set<Move> invalidMoves, Set<Field> blankFields) {
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
            boolean isBlank = isBlank(i, move, blankFields);
            int letterPoints = isBlank ? 0 : alphabet.getPoints(letter);
            int crossWordMultiplier = 1;
            int x = getXBasedOnMoveDirection(move);
            int y = getYBasedOnMoveDirection(move) + i;
            if (board.isEmpty(x, y)) {
                newlyPopulatedFields++;
                int[] pointsAndMultipliers = checkForSpecialFields(x, y, letterPoints, wordMultiplier, crossWordMultiplier);
                letterPoints = pointsAndMultipliers[0];
                wordMultiplier = pointsAndMultipliers[1];
                crossWordMultiplier = pointsAndMultipliers[2];
                if (isBlank) {
                    if (move.getDirection() == Direction.ACROSS) {
                        move.addBlankFieldInfo(x, y);
                    } else {
                        move.addBlankFieldInfo(y, x);
                    }
                }
                points += calculateCrossWordPoints(
                        x, y, letterPoints, crossWordMultiplier,
                        board, alphabet, getBlankIndices(blankFields, move, y));
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

    private int[] checkForSpecialFields(int x, int y, int letterPoints, int wordMultiplier, int crossWordMultiplier) {
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
        return new int[]{letterPoints, wordMultiplier, crossWordMultiplier};
    }

    private int calculateCrossWordPoints(int x, int y, int letterPoints, int crossWordMultiplier,
                                         Board board, Alphabet alphabet, Set<Integer> blankIndices) {
        int crossWordPoints = 0;
        if (board.hasLettersAbove(x, y) && board.hasLettersBelow(x, y)) {
            crossWordPoints += letterPoints;
            String aboveWord = replaceWithBlanks(
                    board.readWordUpwards(x - 1, y, false), blankIndices, x - 1, true);
            String belowWord = replaceWithBlanks(
                    board.readWordDownwards(x + 1, y, false), blankIndices, x + 1, false);
            crossWordPoints += calculateRawPoints(aboveWord, alphabet);
            crossWordPoints += calculateRawPoints(belowWord, alphabet);
            crossWordPoints *= crossWordMultiplier;
        }
        else if (board.hasLettersAbove(x, y)) {
            crossWordPoints += letterPoints;
            String aboveWord = replaceWithBlanks(
                    board.readWordUpwards(x - 1, y, false), blankIndices, x - 1, true);
            crossWordPoints += calculateRawPoints(aboveWord, alphabet);
            crossWordPoints *= crossWordMultiplier;
        }
        else if (board.hasLettersBelow(x, y)) {
            crossWordPoints += letterPoints;
            String belowWord = replaceWithBlanks(
                    board.readWordDownwards(x + 1, y, false), blankIndices, x + 1, false);
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

    private Set<Integer> getBlankIndices(Set<Field> blankFields, Move move, int y) {
        Set<Integer> blankIndices = new HashSet<>();
        for (Field blankField : blankFields) {
            if (move.getDirection() == Direction.ACROSS && blankField.getColumn() == y) {
                blankIndices.add(blankField.getRow());
            }
            else if (move.getDirection() == Direction.DOWN && blankField.getRow() == y){
                blankIndices.add(blankField.getColumn());
            }
        }
        return blankIndices;
    }

    private String replaceWithBlanks(String word, Set<Integer> blankIndices, int startIndex, boolean upwards) {
        StringBuilder stringBuilder = new StringBuilder(word);
        for (int blankIndex : blankIndices) {
            int index = upwards ? startIndex - blankIndex : blankIndex - startIndex;
            if (index >= 0 && index < word.length()) {
                stringBuilder.setCharAt(index, '0');
            }
        }
        return stringBuilder.toString();
    }

    private boolean isBlank(int index, Move move, Set<Field> blankFields) {
        return move.isBlankLetter(index) || blankFields.contains(new Field(move.getX() + index, move.getY()));
    }
}
