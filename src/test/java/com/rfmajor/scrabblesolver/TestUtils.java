package com.rfmajor.scrabblesolver;

import com.rfmajor.scrabblesolver.common.Board;

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
}
