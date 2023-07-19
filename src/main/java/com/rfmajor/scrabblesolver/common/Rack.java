package com.rfmajor.scrabblesolver.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Rack {
    private final Map<Character, Integer> letters;
    public static final char BLANK = ' ';

    public Rack() {
        this.letters = new HashMap<>();
    }

    public boolean isEmpty() {
        return letters.isEmpty();
    }

    public Rack removeLetter(char letter) {
        letters.computeIfPresent(letter, (k, v) -> v > 1 ? v - 1 : null);
        return this;
    }

    public void addLetter(char letter) {
        letters.compute(letter, (k, v) -> v == null ? 1 : v + 1);
    }

    public boolean contains(char letter) {
        return letters.containsKey(letter);
    }

    public Set<Character> getLetters() {
        return letters.keySet();
    }

    public int getSize() {
        return letters.values().stream()
                .reduce(0, Integer::sum);
    }
}
