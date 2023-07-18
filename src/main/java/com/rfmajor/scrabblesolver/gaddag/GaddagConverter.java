package com.rfmajor.scrabblesolver.gaddag;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Setter
public class GaddagConverter {
    @Value("${gaddag.delimiter}")
    private char delimiter;

    public State convert(List<String> words) {
        State parentState = new State();
        processWords(words, parentState);
        postProcessWords(words, parentState);
        return parentState;
    }

    private void processWords(List<String> words, State state) {
        for (String word : words) {
            addSingleWord(word, state);
        }
    }

    private void postProcessWords(List<String> words, State state) {

    }

    private void addSingleWord(String word, State state) {
        State parentState = state;
        Arc arc = null;
        char[] wordChars = word.toCharArray();
        int delimiterIndex = 1;
        while (delimiterIndex <= wordChars.length) {
            state = parentState;
            // add rev(x)
            for (int i = delimiterIndex - 1; i >= 0; i--) {
                arc = addSingleLetter(wordChars[i], arc, state, isLastIteration(i, delimiterIndex, wordChars));
                state = arc.getDestinationState();
            }
            // add delimiter if it's not the last character of the sequence
            if (delimiterIndex != wordChars.length) {
                arc = addSingleLetter(delimiter, arc, state, false);
                state = arc.getDestinationState();
            }
            // add y
            for (int i = delimiterIndex; i < wordChars.length; i++) {
                arc = addSingleLetter(wordChars[i], arc, state, isLastIteration(i, delimiterIndex, wordChars));
                state = arc.getDestinationState();
            }
            delimiterIndex++;
        }
    }

    private Arc addSingleLetter(char letter, Arc arc, State state, boolean isLastLetter) {
        if (isLastLetter) {
            arc.addLetterToSet(letter);
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
