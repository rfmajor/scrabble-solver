package com.rfmajor.scrabblesolver.gaddag;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GaddagConverter {
    public State convert(List<String> words) {
        State parentState = new State();
        processWords(words, parentState);
        postProcessWords(words, parentState);
        return parentState;
    }

    private void processWords(List<String> words, State state) {

    }

    private void postProcessWords(List<String> words, State state) {

    }
}
