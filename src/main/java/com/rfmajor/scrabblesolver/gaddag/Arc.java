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

    public void addLetterToSet(char letter) {
        letterSet.add(letter);
    }

    public Arc getNextArc(char letter) {
        return this.getDestinationState().getArc(letter);
    }

    public boolean hasNextArc(char letter) {
        return destinationState.containsArc(letter);
    }

    public boolean containsLetter(char letter) {
        return letterSet.contains(letter);
    }

    @Override
    public String toString() {
        return String.valueOf(letter);
    }
}
