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
        char[] wordChars = word.toCharArray();
        int delimiterIndex = 1;
        while (delimiterIndex <= wordChars.length) {
            state = parentState;
            // add rev(x)
            for (int i = delimiterIndex - 1; i >= 0; i--) {
                state = addSingleLetter(wordChars[i], state, isLastIteration(i, delimiterIndex, wordChars));
            }
            // add delimiter (except when the delimiter would be the last character of the sequence)
            if (delimiterIndex != wordChars.length) {
                state = addSingleLetter(delimiter, state, false);
            }
            // add y
            for (int i = delimiterIndex; i < wordChars.length; i++) {
                state = addSingleLetter(wordChars[i], state, isLastIteration(i, delimiterIndex, wordChars));
            }
            delimiterIndex++;
        }
    }

    private State addSingleLetter(char letter, State state, boolean isLastLetter) {
        Arc arc = new Arc(letter);
        state.addArc(arc);
        State newState = isLastLetter ? null : new State();
        arc.setDestinationState(newState);
        return newState;
    }

    private boolean isLastIteration(int index, int delimiterIndex, char[] array) {
        if (delimiterIndex == array.length) {
            return index == 0;
        }
        return index == array.length - 1;
    }
}
