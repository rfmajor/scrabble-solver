package com.rfmajor.scrabblesolver.common.gaddag.model;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class State {
    private final Map<Character, Arc> outArcs;

    public State() {
        this.outArcs = new HashMap<>();
    }

    public void addArc(char letter) {
        outArcs.put(letter, new Arc(letter));
    }

    public Arc getArc(char letter) {
        return outArcs.get(letter);
    }

    public boolean containsArc(char letter) {
        return outArcs.containsKey(letter);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        outArcs.keySet().forEach(stringBuilder::append);
        return stringBuilder.toString();
    }
}
