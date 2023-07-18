package com.rfmajor.scrabblesolver.gaddag;

import lombok.AllArgsConstructor;

import java.util.Set;

@AllArgsConstructor
public class Arc {
    private char letter;
    private Set<Character> letterSet;
    private State destinationState;
}
