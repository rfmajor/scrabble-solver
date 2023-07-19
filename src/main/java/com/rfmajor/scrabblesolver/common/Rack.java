package com.rfmajor.scrabblesolver.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Rack {
    private final Map<Character, Integer> letters;
    private int size;
    public static final char BLANK = ' ';

    public Rack() {
        this.letters = new HashMap<>();
    }

    public boolean isEmpty() {
        return letters.isEmpty();
    }

    public void removeLetter(char letter) {
        letters.computeIfPresent(letter, (k, v) -> v > 1 ? v - 1 : null);
        size--;
    }

    public Rack withRemovedLetter(char letter) {
        this.removeLetter(letter);
        return this;
    }

    public void addLetter(char letter) {
        letters.compute(letter, (k, v) -> v == null ? 1 : v + 1);
        size++;
    }

    public boolean contains(char letter) {
        return letters.containsKey(letter);
    }

    public Set<Character> getLetters() {
        return letters.keySet();
    }

    public int getSize() {
        return size;
    }
}
