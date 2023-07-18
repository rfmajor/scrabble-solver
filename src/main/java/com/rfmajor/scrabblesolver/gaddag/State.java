package com.rfmajor.scrabblesolver.gaddag;

import java.util.HashSet;
import java.util.Set;

public class State {
    private Set<Arc> outArcs;

    public State() {
        this.outArcs = new HashSet<>();
    }
}
