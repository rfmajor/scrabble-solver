package com.rfmajor.scrabblesolver.common;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
public class Rack {
    private List<Character> letters;
    public static final char BLANK = ' ';

    public Rack(Set<Character> letters) {
        this.letters = new ArrayList<>();
    }

    public boolean isEmpty() {
        return letters.isEmpty();
    }

    public char removeLetter(int index) {
        return letters.remove(index);
    }

    public boolean contains(char letter) {
        return letters.contains(letter);
    }
}
