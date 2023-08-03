package com.rfmajor.scrabblesolver.gaddag;

import com.rfmajor.scrabblesolver.common.game.Alphabet;
import com.rfmajor.scrabblesolver.common.BitSetUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Gaddag {
    private final Arc parentArc;
    private final Alphabet alphabet;
    private final char delimiter;

    public int getOneLetterCompletion(String word) {
        char[] letters = word.toCharArray();
        Arc arc = parentArc;
        for (char letter : letters) {
            if (!arc.hasNextArc(letter)) {
                return 0;
            }
            arc = arc.getNextArc(letter);
        }
        return arc.getLetterBitVector();
    }

    public int getOneLetterCompletion(String firstWord, String secondWord) {
        char[] firstWordLetters = firstWord.toCharArray();
        int vector = 0;

        Arc arc = parentArc;
        for (char letter : firstWordLetters) {
            if (!arc.hasNextArc(letter)) {
                return 0;
            }
            arc = arc.getNextArc(letter);
        }

        for (char letter : alphabet.letterSet()) {
            if (arc.hasNextArc(letter) && hasWordCompletion(arc.getNextArc(letter), secondWord)) {
                int index = alphabet.getIndex(letter);
                vector = BitSetUtils.addToSet(vector, index);
            }
        }

        return vector;
    }

    private boolean hasWordCompletion(Arc arc, String word) {
        char[] letters = word.toCharArray();
        for (int i = 0; i < letters.length - 1; i++) {
            char letter = letters[i];
            if (!arc.hasNextArc(letter)) {
                return false;
            }
            arc = arc.getNextArc(letter);
        }
        return arc.containsLetter(letters[letters.length - 1], alphabet);
    }
}
