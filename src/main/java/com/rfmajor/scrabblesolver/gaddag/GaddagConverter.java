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
                state = addSingleLetter(wordChars[i], state);
            }
            // add delimiter if it's not the last character of the sequence
            if (delimiterIndex != wordChars.length) {
                state = addSingleLetter(delimiter, state);
            }
            // add y
            for (int i = delimiterIndex; i < wordChars.length; i++) {
                state = addSingleLetter(wordChars[i], state);
            }
            delimiterIndex++;
        }
    }

    private State addSingleLetter(char letter, State state) {
        state.addArc(letter);
        return state.getArc(letter).getDestinationState();
    }
}
