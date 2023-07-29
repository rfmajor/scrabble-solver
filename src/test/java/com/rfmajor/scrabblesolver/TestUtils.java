package com.rfmajor.scrabblesolver;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TestUtils {
    public static List<Character> mapStringToLettersList(String letters) {
        return letters.chars().mapToObj(c -> (char) c).toList();
    }

    public static Set<Character> mapStringToLettersSet(String letters) {
        return letters.chars().mapToObj(c -> (char) c).collect(Collectors.toSet());
    }
}
