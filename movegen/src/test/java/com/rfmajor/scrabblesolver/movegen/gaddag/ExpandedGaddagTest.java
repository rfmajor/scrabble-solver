package com.rfmajor.scrabblesolver.movegen.gaddag;

public class ExpandedGaddagTest extends GaddagTest<Long> {
    @Override
    protected GaddagConverter<Long> createConverter() {
        return new ExpandedGaddagConverter();
    }
}
