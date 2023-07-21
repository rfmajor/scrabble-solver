package com.rfmajor.scrabblesolver.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
        if (letters.containsKey(letter) {
            letters.compute(letter, (k, v) -> v > 1 ? v - 1 : null);
            size--;
        }
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

    public List<Character> getLetters() {
        return letters.entrySet().stream()
                .flatMap(e -> Stream.iterate(e.getKey(), letter -> letter).limit(e.getValue()))
                .toList();
    }

    public int getSize() {
        return size;
    }
}
