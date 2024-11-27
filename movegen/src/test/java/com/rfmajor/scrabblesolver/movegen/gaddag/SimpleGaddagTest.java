package com.rfmajor.scrabblesolver.movegen.gaddag;

public class SimpleGaddagTest extends GaddagTest<Arc> {
    @Override
    protected GaddagConverter<Arc> createConverter() {
        return new SimpleGaddagConverter();
    }
}
