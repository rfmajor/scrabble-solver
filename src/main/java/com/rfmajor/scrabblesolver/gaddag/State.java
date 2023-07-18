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

    public void addArc(Arc arc) {
        outArcs.put(arc.getLetter(), arc);
    }

    public Arc getArc(char letter) {
        return outArcs.get(letter);
    }
}
