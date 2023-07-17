package com.rfmajor.scrabblesolver.gaddag;

import lombok.AllArgsConstructor;

import java.util.Set;

@AllArgsConstructor
public class Edge {
    private char character;
    private Set<Character> letterSet;
    private Edge nextEdge;
}
