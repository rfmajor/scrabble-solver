package com.rfmajor.scrabblesolver.gaddag.converter.utils;

import com.rfmajor.scrabblesolver.common.scrabble.Board;
import com.rfmajor.scrabblesolver.common.gaddag.model.Gaddag;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class TestUtils {
    public static List<Character> mapStringToLettersList(String letters) {
        return letters.chars().mapToObj(c -> (char) c).toList();
    }

    public static Set<Character> mapStringToLettersSet(String letters) {
        return letters.chars().mapToObj(c -> (char) c).collect(Collectors.toSet());
    }

    public static void addWordToBoardVertically(String word, int row, int column, Board board) {
        for (int i = 0; i < word.length(); i++) {
            char letter = word.charAt(i);
            board.addLetter(letter, row + i, column);
        }
    }

    public static void addWordToBoardHorizontally(String word, int row, int column, Board board) {
        for (int i = 0; i < word.length(); i++) {
            char letter = word.charAt(i);
            board.addLetter(letter, row, column + i);
        }
    }

    public static <A> boolean isSequencePresent(String sequence, Gaddag<A> gaddag) {
        A arc = gaddag.getRootArc();
        for (int i = 0; i < sequence.length(); i++) {
            if (gaddag.isLastArc(arc)) {
                return false;
            }
            char letter = sequence.charAt(i);
            A nextArc = gaddag.findNextArc(arc, letter);
            if (!gaddag.isPresent(nextArc)) {
                return false;
            }

            arc = nextArc;
        }
        return true;
    }

    public static <A> boolean isWordPresent(String sequence, Gaddag<A> gaddag) {
        char lastChar = sequence.charAt(sequence.length() - 1);

        A arc = gaddag.getRootArc();
        for (int i = 0; i < sequence.length() - 1; i++) {
            if (gaddag.isLastArc(arc)) {
                return false;
            }
            char letter = sequence.charAt(i);
            A nextArc = gaddag.findNextArc(arc, letter);
            if (!gaddag.isPresent(nextArc)) {
                return false;
            }

            arc = nextArc;
        }
        return gaddag.containsLetter(arc, lastChar);
    }

    private TestUtils() {
    }
}
