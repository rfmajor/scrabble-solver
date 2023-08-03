package com.rfmajor.scrabblesolver.common;

import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@AllArgsConstructor
public class Rack {
    private final Map<Character, Integer> letters;
    private int size;
    public static final char BLANK = ' ';

    public Rack() {
        this.letters = new HashMap<>();
    }

    public Rack(String letters) {
        this();
        for (char letter : letters.toCharArray()) {
            addLetter(letter);
        }
    }

    private Rack copy() {
        return new Rack(new HashMap<>(letters), size);
    }

    public boolean isEmpty() {
        return letters.isEmpty();
    }

    public void removeLetter(char letter) {
        if (letters.containsKey(letter)) {
            letters.compute(letter, (k, v) -> v > 1 ? v - 1 : null);
            size--;
        }
    }

    public Rack withRemovedLetter(char letter) {
        Rack rack = this.copy();
        rack.removeLetter(letter);
        return rack;
    }

    public void addLetter(char letter) {
        letters.compute(letter, (k, v) -> v == null ? 1 : v + 1);
        size++;
    }

    public boolean contains(char letter) {
        return letters.containsKey(letter);
    }

    public List<Character> getLetters() {
        return getLettersStream().toList();
    }

    public List<Character> getAllowedLetters(int letterSet, Alphabet alphabet) {
        return getLettersStream()
                .filter(letter -> BitSetUtils.contains(letterSet, alphabet.getIndex(letter)))
                .toList();
    }

    public int getSize() {
        return size;
    }

    private Stream<Character> getLettersStream() {
        return letters.entrySet().stream()
                .flatMap(e ->
                        Stream.iterate(e.getKey(), letter -> letter)
                                .limit(e.getValue())
                );
    }
}
