package com.rfmajor.scrabblesolver.gaddag;

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
    private int letterIndicesBitMap;
    private State destinationState;

    public Arc(char letter) {
        this.letter = letter;
        this.destinationState = new State();
    }

    public void addLetterIndexToSet(int index) {
        letterIndicesBitMap = BitSetUtils.addToSet(letterIndicesBitMap, index);
    }

    public void removeLetterIndexFromSet(int index) {
        letterIndicesBitMap = BitSetUtils.removeFromSet(letterIndicesBitMap, index);
    }

    public boolean containsLetterIndex(int index) {
        return BitSetUtils.contains(letterIndicesBitMap, index);
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
