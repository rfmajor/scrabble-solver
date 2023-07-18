package com.rfmajor.scrabblesolver.gaddag;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GaddagConverter {
    public Edge convert(List<String> words) {
        Edge edge = new Edge(' ', null, null);
        processWords(words, edge);
        postProcessWords(words, edge);
        return edge;
    }

    private void processWords(List<String> words, Edge edge) {

    }

    private void postProcessWords(List<String> words, Edge edge) {

    }
}
