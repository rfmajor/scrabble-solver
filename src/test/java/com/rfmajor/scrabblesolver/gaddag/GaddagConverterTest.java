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
        assertTrue(isSequencePresent("c#are", state));
        assertTrue(isSequencePresent("ac#re", state));
        assertTrue(isSequencePresent("rac#e", state));
        assertTrue(isSequencePresent("erac", state));
        assertFalse(isSequencePresent("erac#", state));
    }

    private static boolean isSequencePresent(String sequence, State state) {
        for (int i = 0; i < sequence.length(); i++) {
            if (state == null) {
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