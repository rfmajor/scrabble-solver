package com.rfmajor.scrabblesolver.gaddag;

import com.rfmajor.scrabblesolver.common.game.Alphabet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class SimpleGaddagConverter implements GaddagConverter<Arc> {
    @Override
    public Gaddag<Arc> convert(List<String> words, Alphabet alphabet) {
        Arc parentArc = new Arc();
        State parentState = new State();
        parentArc.setDestinationState(parentState);

        processWords(words, parentState, alphabet);
        return new SimpleGaddag(parentArc, alphabet, alphabet.getDelimiter());
    }

    private void processWords(List<String> words, State state, Alphabet alphabet) {
        for (String word : words) {
            log.info("Processing word: {}", word);
            addSingleWord(word, state, alphabet);
        }
    }

    private void addSingleWord(String word, State state, Alphabet alphabet) {
        State parentState = state;
        Arc arc = null;
        char[] wordChars = word.toCharArray();
        int delimiterIndex = 1;
        while (delimiterIndex <= wordChars.length) {
            state = parentState;
            // add rev(x)
            for (int i = delimiterIndex - 1; i >= 0; i--) {
                arc = addSingleLetter(wordChars[i], arc, state, isLastIteration(i, delimiterIndex, wordChars), alphabet);
                state = arc.getDestinationState();
            }
            // add delimiter if it's not the last character of the sequence
            if (delimiterIndex != wordChars.length) {
                arc = addSingleLetter(alphabet.getDelimiter(), arc, state, false, alphabet);
                state = arc.getDestinationState();
            }
            // add y
            for (int i = delimiterIndex; i < wordChars.length; i++) {
                arc = addSingleLetter(wordChars[i], arc, state, isLastIteration(i, delimiterIndex, wordChars), alphabet);
                state = arc.getDestinationState();
            }
            delimiterIndex++;
        }
    }

    private Arc addSingleLetter(char letter, Arc arc, State state, boolean isLastLetter, Alphabet alphabet) {
        if (isLastLetter) {
            arc.addLetterIndexToSet(alphabet.getIndex(letter));
            return arc;
        }
        if (!state.containsArc(letter)) {
            state.addArc(letter);
            return state.getArc(letter);
        }
        return state.getArc(letter);
    }

    private boolean isLastIteration(int index, int delimiterIndex, char[] array) {
        if (delimiterIndex == array.length) {
            return index == 0;
        }
        return index == array.length - 1;
    }
}
