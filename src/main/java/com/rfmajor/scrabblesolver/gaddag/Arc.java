package com.rfmajor.scrabblesolver.gaddag;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
public class Arc {
    private char letter;
    private final Set<Character> letterSet;
    private State destinationState;

    public Arc(char letter) {
        this.letter = letter;
        this.letterSet = new HashSet<>();
        this.destinationState = new State();
    }

    @Override
    public String toString() {
        return String.valueOf(letter);
    }
}
