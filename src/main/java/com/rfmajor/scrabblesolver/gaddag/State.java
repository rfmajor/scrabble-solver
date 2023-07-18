package com.rfmajor.scrabblesolver.gaddag;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class State {
    private final Set<Arc> outArcs;

    public State() {
        this.outArcs = new HashSet<>();
    }

    public void addArc(Arc arc) {
        outArcs.add(arc);
    }
}
