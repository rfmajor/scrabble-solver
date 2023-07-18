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
    }

    private static boolean isSequencePresent(String sequence, State state) {
        int i = 0;
        while (state != null) {
            Arc arc = getArcByLetter(state, sequence.charAt(i));
            if (arc == null || arc.getLetter() != sequence.charAt(i)) {
                return false;
            }
            state = arc.getDestinationState();
            i++;
        }
        return true;
    }

    private static Arc getArcByLetter(State state, char letter) {
        for (Arc outArc : state.getOutArcs()) {
            if (outArc.getLetter() == letter) {
                return outArc;
            }
        }
        return null;
    }
}