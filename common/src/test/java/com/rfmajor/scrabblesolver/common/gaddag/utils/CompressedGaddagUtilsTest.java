package com.rfmajor.scrabblesolver.common.gaddag.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompressedGaddagUtilsTest {

    @Test
    void testGetRecord_regularScenario() {
        byte[] array = new byte[]{8, 0, 0, 1, 4, 0, 0, 0, 2};
        long record = CompressedGaddagUtils.getRecord(3, array);
        long expected = 1L + (4L << 8);

        assertEquals(expected, record);
    }

    @Test
    void testGetRecord_negativeByteBecomesUnsignedLong() {
        byte[] array = new byte[]{8, 0, 0, -1, 4, 0, 0, 0, 2};
        long record = CompressedGaddagUtils.getRecord(3, array);
        long expected = 255L + 1024L;

        assertEquals(expected, record);
    }
    @Test
    void testGetRecord_negativeMostSignificantByteDoesNotChangeTheLongValue() {
        byte[] array = new byte[]{8, 0, 0, -1, 4, 0, 0, -1, 2};
        long record = CompressedGaddagUtils.getRecord(3, array);
        long expected = 255L + 1024L + (255L << (4 * 8));

        assertEquals(expected, record);
    }

    @Test
    void testSetRecord_regularScenario() {
        byte[] array = new byte[]{8, 0, 0, 0, 0, 0, 0, -1, 2};
        long record = 1L + (4L << 8);

        CompressedGaddagUtils.setRecord(record, 3, array);

        assertAll(
                () -> assertEquals(1, array[3]),
                () -> assertEquals(4, array[4]),
                () -> assertEquals(0, array[5]),
                () -> assertEquals(0, array[6]),
                () -> assertEquals(0, array[7])
        );
    }

    @Test
    void testSetRecord_longBecomesNegativeByte() {
        byte[] array = new byte[]{8, 0, 0, 0, 0, 0, 0, 0, 2};
        long record = 255L + 1024L;

        CompressedGaddagUtils.setRecord(record, 3, array);

        assertAll(
                () -> assertEquals(-1, array[3]),
                () -> assertEquals(4, array[4]),
                () -> assertEquals(0, array[5]),
                () -> assertEquals(0, array[6]),
                () -> assertEquals(0, array[7])
        );
    }

    @Test
    void testSetRecord_mostSignificantByteBecomesMinusOne() {
        byte[] array = new byte[]{8, 0, 0, 0, 0, 0, 0, 0, 2};
        long record = 255L + 1024L + (255L << (4 * 8));

        CompressedGaddagUtils.setRecord(record, 3, array);

        assertAll(
                () -> assertEquals(-1, array[3]),
                () -> assertEquals(4, array[4]),
                () -> assertEquals(0, array[5]),
                () -> assertEquals(0, array[6]),
                () -> assertEquals(-1, array[7])
        );
    }

    @Test
    void testGetArcsBitMap_normalCase() {
        long[] state = {1, 0, 2};
        int alphabetSize = 3;
        // only 0th and 2nd bits set
        long expected = BitSetUtils.setBitsInRange(0L, 0, alphabetSize, 0b101);
        assertEquals(expected, CompressedGaddagUtils.getArcsBitMap(state, alphabetSize));
    }

    @Test
    void testGetArcsBitMap_emptyStateArray() {
        long[] state = new long[]{};
        int alphabetSize = 5;
        long expected = 0L;
        assertEquals(expected, CompressedGaddagUtils.getArcsBitMap(state, alphabetSize));
    }

    @Test
    void testGetArcsBitMap_allZerosInTheStateArray() {
        long[] state = new long[]{0, 0, 0};
        int alphabetSize = 3;
        long expected = 0L;
        assertEquals(expected, CompressedGaddagUtils.getArcsBitMap(state, alphabetSize));
    }

    @Test
    void testGetArcsBitMap_stateArrayLargerThanAlphabetSize_throwsException() {
        long[] state = new long[]{1, 1, 1, 1};
        int alphabetSize = 3; // only bits 0, 1, and 2 considered
        assertThrows(IllegalArgumentException.class, () -> CompressedGaddagUtils.getArcsBitMap(state, alphabetSize));
    }

    @Test
    void testGetNumberOfBitMapBits_normalCase() {
        long state = 0b10101;
        int alphabetSize = 5;
        int expected = 3;
        assertEquals(expected, CompressedGaddagUtils.getNumberOfBitMapBits(state, alphabetSize));
    }

    @Test
    void testGetNumberOfBitMapBits_noBitsSet() {
        long state = 0b0;
        int alphabetSize = 5;
        int expected = 0;
        assertEquals(expected, CompressedGaddagUtils.getNumberOfBitMapBits(state, alphabetSize));
    }

    @Test
    void testGetNumberOfBitMapBits_alphabetSizeLessThanBitsInState() {
        long state = 0b11101;
        int alphabetSize = 3;
        int expected = 2;
        assertEquals(expected, CompressedGaddagUtils.getNumberOfBitMapBits(state, alphabetSize));
    }

    @Test
    void testGetNumberOfBitMapBits_alphabetSizeGreaterThanBitsInState() {
        long state = 0b101;
        int alphabetSize = 6;
        int expected = 2;
        assertEquals(expected, CompressedGaddagUtils.getNumberOfBitMapBits(state, alphabetSize));
    }

    @Test
    void testGetNumberOfBitMapBits_zeroAlphabetSize() {
        long state = 0b101;
        int alphabetSize = 0;
        assertThrows(IllegalArgumentException.class,
                () -> CompressedGaddagUtils.getNumberOfBitMapBits(state, alphabetSize));
    }

    @Test
    void testGetNumberOfBitMapBits_negativeAlphabetSize() {
        long state = 0b101;
        int alphabetSize = -1;
        assertThrows(IllegalArgumentException.class,
                () -> CompressedGaddagUtils.getNumberOfBitMapBits(state, alphabetSize));
    }

    @Test
    void testGetLetterOffset_letterPresent() {
        long state = 0b101010;
        int letterIdx = 5;
        int alphabetSize = 6;

        int expectedOffset = 3;
        assertEquals(expectedOffset, CompressedGaddagUtils.getLetterOffset(state, letterIdx, alphabetSize));
    }

    @Test
    void testGetLetterOffset_letterNotPresent() {
        long state = 0b101010;
        int letterIdx = 2;
        int alphabetSize = 6;

        assertEquals(-1, CompressedGaddagUtils.getLetterOffset(state, letterIdx, alphabetSize));
    }

    @Test
    void testGetLetterOffset_firstLetter() {
        long state = 0b100010;
        int letterIdx = 1;
        int alphabetSize = 6;

        int expectedOffset = 1;
        assertEquals(expectedOffset, CompressedGaddagUtils.getLetterOffset(state, letterIdx, alphabetSize));
    }

    @Test
    void testGetLetterOffset_lastLetter() {
        long state = 0b100000;
        int letterIdx = 5;
        int alphabetSize = 6;

        int expectedOffset = 1;
        assertEquals(expectedOffset, CompressedGaddagUtils.getLetterOffset(state, letterIdx, alphabetSize));
    }

    @Test
    void testGetStateBitMap_fullRange() {
        long state = 0b101010;
        int alphabetSize = 6;

        long expectedBitMap = BitSetUtils.getBitsInRange(state, 0, alphabetSize);
        assertEquals(expectedBitMap, CompressedGaddagUtils.getStateBitMap(state, alphabetSize));
    }

    @Test
    void testGetStateBitMap_partialRange() {
        long state = 0b00011100_00000000_00000000_00011100;
        int alphabetSize = 5;

        long expectedBitMap = BitSetUtils.getBitsInRange(state, 0, alphabetSize);
        assertEquals(expectedBitMap, CompressedGaddagUtils.getStateBitMap(state, alphabetSize));
    }

    @Test
    void testGetStateBitMap_noBitsSet() {
        long state = 0L;
        int alphabetSize = 6;

        assertEquals(0L, CompressedGaddagUtils.getStateBitMap(state, alphabetSize));
    }
}