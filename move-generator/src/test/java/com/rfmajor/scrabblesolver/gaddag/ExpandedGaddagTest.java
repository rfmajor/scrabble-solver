package com.rfmajor.scrabblesolver.gaddag;

public class ExpandedGaddagTest extends GaddagTest<Long> {
    @Override
    protected GaddagConverter<Long> createConverter() {
        return new ExpandedGaddagConverter();
    }
}
