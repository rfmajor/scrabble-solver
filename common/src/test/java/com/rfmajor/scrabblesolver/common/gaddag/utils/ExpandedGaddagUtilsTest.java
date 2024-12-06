package com.rfmajor.scrabblesolver.common.gaddag.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpandedGaddagUtilsTest {
    @Test
    void testGetDestinationStateId() {
        long arc = 0b101010;
        int expected = (int) BitSetUtils.getBitsInRange(
                arc,
                ExpandedGaddagUtils.DEST_ID_START,
                ExpandedGaddagUtils.DEST_ID_END
        );

        assertEquals(expected, ExpandedGaddagUtils.getDestinationStateId(arc));
    }

    @Test
    void testGetLetterBitMapId() {
        long arc = 0b11100000_00000000_00000000_00001110L;
        int expected = (int) BitSetUtils.getBitsInRange(
                arc,
                ExpandedGaddagUtils.LETTER_MAP_START,
                ExpandedGaddagUtils.LETTER_MAP_END
        );

        assertEquals(expected, ExpandedGaddagUtils.getLetterBitMapId(arc));
    }

    @Test
    void testSetDestinationStateId_singleArc() {
        long arc = 0L;
        int destStateIdx = 12345;

        long expected = BitSetUtils.setBitsInRange(
                arc,
                ExpandedGaddagUtils.DEST_ID_START,
                ExpandedGaddagUtils.DEST_ID_END,
                destStateIdx
        );

        assertEquals(expected, ExpandedGaddagUtils.setDestinationStateId(arc, destStateIdx));
    }

    @Test
    void testSetDestinationStateId_arcsArray() {
        long[][] arcs = new long[10][10];
        int stateId = 2;
        int letterId = 3;
        int destStateIdx = 5678;

        ExpandedGaddagUtils.setDestinationStateId(stateId, letterId, destStateIdx, arcs);

        long expected = BitSetUtils.setBitsInRange(
                0L,
                ExpandedGaddagUtils.DEST_ID_START,
                ExpandedGaddagUtils.DEST_ID_END,
                destStateIdx
        );

        assertEquals(expected, arcs[stateId][letterId]);
    }

    @Test
    void testSetLetterBitMapId_singleArc() {
        long arc = 0L;
        int letterMapIdx = 15;

        long expected = BitSetUtils.setBitsInRange(
                arc,
                ExpandedGaddagUtils.LETTER_MAP_START,
                ExpandedGaddagUtils.LETTER_MAP_END,
                letterMapIdx
        );

        assertEquals(expected, ExpandedGaddagUtils.setLetterBitMapId(arc, letterMapIdx));
    }

    @Test
    void testSetLetterBitMapId_arcsArray() {
        long[][] arcs = new long[10][10];
        int stateId = 4;
        int letterId = 5;
        int letterMapIdx = 42;

        ExpandedGaddagUtils.setLetterBitMapId(stateId, letterId, letterMapIdx, arcs);

        long expected = BitSetUtils.setBitsInRange(
                0L,
                ExpandedGaddagUtils.LETTER_MAP_START,
                ExpandedGaddagUtils.LETTER_MAP_END,
                letterMapIdx
        );

        assertEquals(expected, arcs[stateId][letterId]);
    }
}