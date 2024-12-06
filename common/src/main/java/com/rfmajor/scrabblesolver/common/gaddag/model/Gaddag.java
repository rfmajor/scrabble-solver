package com.rfmajor.scrabblesolver.common.gaddag.model;

import com.rfmajor.scrabblesolver.common.gaddag.utils.BitSetUtils;
import com.rfmajor.scrabblesolver.common.scrabble.Alphabet;
import lombok.Getter;

import java.util.Objects;

@Getter
public abstract class Gaddag<A> {
    protected final A rootArc;
    protected final Alphabet alphabet;
    protected final char delimiter;

    protected Gaddag(A rootArc, Alphabet alphabet, char delimiter) {
        this.rootArc = rootArc;
        this.alphabet = alphabet;
        this.delimiter = delimiter;
    }

    public abstract A findNextArc(A arc, char letter);
    public abstract boolean hasNextArc(A arc, char letter);
    public abstract boolean containsLetter(A arc, char letter);
    public abstract int getLetterIndicesBitMap(A arc);
    public abstract boolean isLastArc(A arc);
    public abstract boolean isPresent(A arc);

    public int getOneLetterCompletion(String word) {
        char[] letters = word.toCharArray();
        A arc = rootArc;
        for (char letter : letters) {
            if (!hasNextArc(arc, letter)) {
                return 0;
            }
            arc = findNextArc(arc, letter);
        }
        return getLetterIndicesBitMap(arc);
    }

    /**
     * Calculates the set of indices of letters which, when inserted between 2 word parts passed as arguments,
     * form a valid word.
     * @param firstWord First part of the word to complete
     * @param secondWord Second part of the word to complete
     * @return An indices' set being represented by a bit map encoded in a 32-bit integer
     */
    public int getOneLetterCompletion(String firstWord, String secondWord) {
        char[] firstWordLetters = firstWord.toCharArray();
        int vector = 0;

        A arc = rootArc;
        for (char letter : firstWordLetters) {
            if (!hasNextArc(arc, letter)) {
                return 0;
            }
            arc = findNextArc(arc, letter);
        }

        for (char letter : alphabet.letterSet()) {
            if (hasNextArc(arc, letter) && hasWordCompletion(findNextArc(arc, letter), secondWord)) {
                int index = alphabet.getIndex(letter);
                vector = BitSetUtils.addToSet(vector, index);
            }
        }

        return vector;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gaddag<?> gaddag = (Gaddag<?>) o;
        return delimiter == gaddag.delimiter &&
                Objects.equals(rootArc, gaddag.rootArc) &&
                Objects.equals(alphabet, gaddag.alphabet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootArc, alphabet, delimiter);
    }

    private boolean hasWordCompletion(A arc, String word) {
        char[] letters = word.toCharArray();
        for (int i = 0; i < letters.length - 1; i++) {
            char letter = letters[i];
            if (!hasNextArc(arc, letter)) {
                return false;
            }
            arc = findNextArc(arc, letter);
        }
        return containsLetter(arc, letters[letters.length - 1]);
    }
}
