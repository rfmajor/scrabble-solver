package com.rfmajor.scrabblesolver.common.game;

import com.rfmajor.scrabblesolver.common.BitSetUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@AllArgsConstructor
public class Rack {
    private final Map<Character, Integer> letters;
    private int size;
    @Getter
    private int maxSize;
    public static final char BLANK = ' ';
    public static final int DEFAULT_MAX_SIZE = 7;

    public Rack() {
        this.letters = new HashMap<>();
        this.maxSize = DEFAULT_MAX_SIZE;
    }

    public Rack(String letters) {
        this();
        for (char letter : letters.toCharArray()) {
            addLetter(letter);
        }
    }

    private Rack copy() {
        return new Rack(new HashMap<>(letters), size, maxSize);
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
                .filter(letter -> letter != Rack.BLANK)
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
