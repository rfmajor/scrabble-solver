package com.rfmajor.scrabblesolver.common.gaddag.utils;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BitSetUtilsTest {
    @Test
    void testAddToSetWithInt_normalCase() {
        int bitSet = 0b1010;
        int index = 0;
        int expected = 0b1011;
        assertEquals(expected, BitSetUtils.addToSet(bitSet, index));
    }

    @Test
    void testAddToSetWithLong_normalCase() {
        long bitSet = 0b1010L;
        long index = 2;
        long expected = 0b1110L;
        assertEquals(expected, BitSetUtils.addToSet(bitSet, index));
    }

    @Test
    void testAddToSetWithInt_overridingExistingValue() {
        int bitSet = 0b1010;
        int index = 1;
        int expected = 0b1010;
        assertEquals(expected, BitSetUtils.addToSet(bitSet, index));
    }

    @Test
    void testAddToSetWithLong_overridingExistingValue() {
        long bitSet = 0b1010L;
        long index = 1;
        long expected = 0b1010L;
        assertEquals(expected, BitSetUtils.addToSet(bitSet, index));
    }

    @Test
    void testRemoveFromSet_normalCase() {
        int bitSet = 0b1011;
        int index = 0;
        int expected = 0b1010;
        assertEquals(expected, BitSetUtils.removeFromSet(bitSet, index));
    }

    @Test
    void testRemoveFromSet_alreadyEmpty() {
        int bitSet = 0b1001;
        int index = 1;
        int expected = 0b1001;
        assertEquals(expected, BitSetUtils.removeFromSet(bitSet, index));
    }

    @Test
    void testContains_normalCase() {
        int bitSet = 0b1010;

        assertTrue(BitSetUtils.contains(bitSet, 3));
        assertFalse(BitSetUtils.contains(bitSet, 0));
    }

    @Test
    void testToSet() {
        int bitSet = 0b10101;
        Set<Integer> expected = Set.of(0, 2, 4);

        assertEquals(expected, BitSetUtils.toSet(bitSet));
    }

    @Test
    void testContainsOnly() {
        int bitSet = 0b10101;

        assertTrue(BitSetUtils.containsOnly(bitSet, 0, 2, 4));
        assertFalse(BitSetUtils.containsOnly(bitSet, 0, 1));
        assertFalse(BitSetUtils.containsOnly(bitSet, 0, 2));
    }

    @Test
    void testGetBitsInRange_validRange() {
        long bitSet = 0b1111101L;
        int startIndex = 1;
        int endIndex = 5;
        long expected = 0b1110L;

        assertEquals(expected, BitSetUtils.getBitsInRange(bitSet, startIndex, endIndex));
    }

    @Test
    void testGetBitsInRange_invalidRange() {
        long bitSet = 0b1111L;

        assertThrows(IllegalArgumentException.class, () -> BitSetUtils.getBitsInRange(bitSet, -1, 4));
        assertThrows(IllegalArgumentException.class, () -> BitSetUtils.getBitsInRange(bitSet, 1, 65));
        assertThrows(IllegalArgumentException.class, () -> BitSetUtils.getBitsInRange(bitSet, 5, 4));
    }

    @Test
    void testSetBitsInRange_valid() {
        long bitSet = 0b1010L;
        int startIndex = 1;
        int endIndex = 4;
        long value = 0b111L;
        long expected = 0b1110L;

        assertEquals(expected, BitSetUtils.setBitsInRange(bitSet, startIndex, endIndex, value));
    }

    @Test
    void testSetBitsInRange_invalidRange() {
        long bitSet = 0b1010L;

        assertThrows(IllegalArgumentException.class,
                () -> BitSetUtils.setBitsInRange(bitSet, -1, 4, 0b111L));
        assertThrows(IllegalArgumentException.class,
                () -> BitSetUtils.setBitsInRange(bitSet, 1, 65, 0b111L));
        assertThrows(IllegalArgumentException.class,
                () -> BitSetUtils.setBitsInRange(bitSet, 2, 4, 0b1111L));
    }

    @Test
    void testSetBitsInRangeWithByte_normalCase() {
        long bitSet = 0b1000L;
        int startIndex = 1;
        int endIndex = 3;
        byte value = 0b01;
        long expected = 0b1010L;

        assertEquals(expected, BitSetUtils.setBitsInRange(bitSet, startIndex, endIndex, value));
    }

    @Test
    void testSetBitsInRangeWithByte_negativeValueByte() {
        long bitSet = 0b00000001_00001000L;
        int startIndex = 8;
        int endIndex = 16;
        byte value = -0b1;
        long expected = 0b11111111_00001000L;

        assertEquals(expected, BitSetUtils.setBitsInRange(bitSet, startIndex, endIndex, value));
    }

    @Test
    void testSetBitsInRangeWithByte_negativeValueByteInTheMostSignificantOctet() {
        long bitSet = 0b00001000L;
        int startIndex = 56;
        int endIndex = 64;
        byte value = -0b1;
        long expected = 0b11111111_00000000_00000000_00000000_00000000_00000000_00000000_00001000L;

        assertEquals(expected, BitSetUtils.setBitsInRange(bitSet, startIndex, endIndex, value));
    }

    @Test
    void testSetBitsInRangeWithByte_invalidRange() {
        long bitSet = 0b1010L;
        byte value = 0b111;

        assertThrows(IllegalArgumentException.class,
                () -> BitSetUtils.setBitsInRange(bitSet, -1, 4, value));
        assertThrows(IllegalArgumentException.class,
                () -> BitSetUtils.setBitsInRange(bitSet, 1, 65, value));
        assertThrows(IllegalArgumentException.class,
                () -> BitSetUtils.setBitsInRange(bitSet, 2, 4, value));
        assertThrows(IllegalArgumentException.class,
                () -> BitSetUtils.setBitsInRange(bitSet, 4, 2, value));
    }

    @Test
    void testSetZerosInRange_normalCase() {
        long bitSet = 0b111111L;
        int startIndex = 2;
        int endIndex = 5;
        long expected = 0b100011L;

        assertEquals(expected, BitSetUtils.setZerosInRange(bitSet, startIndex, endIndex));
    }

    @Test
    void testSetZerosInRange_mostSignificantOctet() {
        long bitSet = 0b11111111_00000000_00000000_00000000_00000000_00000000_00000000_00111111L;
        int startIndex = 56;
        int endIndex = 64;
        long expected = 0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_00111111L;

        assertEquals(expected, BitSetUtils.setZerosInRange(bitSet, startIndex, endIndex));
    }
}