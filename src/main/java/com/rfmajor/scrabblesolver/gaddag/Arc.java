package com.rfmajor.scrabblesolver.gaddag;

import com.rfmajor.scrabblesolver.common.game.Alphabet;
import com.rfmajor.scrabblesolver.common.BitSetUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class Arc {
    private char letter;
    private int letterBitVector;
    private State destinationState;

    public Arc(char letter) {
        this.letter = letter;
        this.destinationState = new State();
    }

    public void addLetterToSet(char letter, Alphabet alphabet) {
        int index = alphabet.getIndex(letter);
        letterBitVector = BitSetUtils.addToSet(letterBitVector, index);
    }

    public void removeLetterFromSet(char letter, Alphabet alphabet) {
        int index = alphabet.getIndex(letter);
        letterBitVector = BitSetUtils.removeFromSet(letterBitVector, index);
    }

    public boolean containsLetter(char letter, Alphabet alphabet) {
        int index = alphabet.getIndex(letter);
        return BitSetUtils.contains(letterBitVector, index);
    }

    public Arc getNextArc(char letter) {
        return this.getDestinationState().getArc(letter);
    }

    public boolean hasNextArc(char letter) {
        return destinationState.containsArc(letter);
    }

    @Override
    public String toString() {
        return String.valueOf(letter);
    }

    public List<Character> getNextLetters() {
        return this.getDestinationState().getOutArcs().keySet().stream().toList();
    }

    public List<Character> getNextAllowedLetters(int letterSet, char delimiter) {
        return this.getDestinationState().getOutArcs().keySet().stream()
                .filter(letter -> letter != delimiter)
                .filter(letter -> BitSetUtils.contains(letterSet, letter))
                .toList();
    }
}
