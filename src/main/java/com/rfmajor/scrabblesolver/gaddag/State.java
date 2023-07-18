package com.rfmajor.scrabblesolver.gaddag;

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
}
