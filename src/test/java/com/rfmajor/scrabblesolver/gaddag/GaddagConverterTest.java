package com.rfmajor.scrabblesolver.gaddag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GaddagConverterTest {
    private GaddagConverter gaddagConverter = new GaddagConverter();

    @BeforeEach
    void setUp() {
        gaddagConverter.setDelimiter('#');
    }

    @Test
    void givenSomeWord_whenConvertCalled_thenSequencePresent() {
        State state = gaddagConverter.convert(List.of("care"));
        assertTrue(isSequencePresent("c#ar", state));
        assertTrue(isSequencePresent("ac#r", state));
        assertTrue(isSequencePresent("rac#", state));
        assertTrue(isSequencePresent("era", state));
        assertFalse(isSequencePresent("era#", state));
    }

    private static boolean isSequencePresent(String sequence, State state) {
        for (int i = 0; i < sequence.length(); i++) {
            if (state.getOutArcs().isEmpty()) {
                return false;
            }
            char letter = sequence.charAt(i);
            Arc arc = state.getArc(letter);
            if (arc == null) {
                return false;
            }
            state = arc.getDestinationState();
        }
        return true;
    }
}