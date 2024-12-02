package com.rfmajor.scrabblesolver.common.gaddag.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BitSetUtils {
    public static int addToSet(int bitSet, int index) {
        return bitSet | (1 << index);
    }

    public static long addToSet(long bitSet, long index) {
        return bitSet | (1L << index);
    }

    public static int removeFromSet(int bitSet, int index) {
        return bitSet & ~(1 << index);
    }

    public static boolean contains(int bitSet, int index) {
        return ((bitSet >> index) & 1) == 1;
    }

    public static Set<Integer> toSet(int bitSet) {
        Set<Integer> set = new HashSet<>();
        for (int i = 0; bitSet != 0; i++) {
            if (((bitSet >> i) & 1) == 1L) {
                set.add(i);
                bitSet = bitSet & ~(1 << i);
            }
        }
        return set;
    }

    public static boolean containsOnly(int bitSet, int... indices) {
        int testSet = 0;
        for (int index : indices) {
            testSet = addToSet(testSet, index);
        }
        return bitSet == testSet;
    }

    public static long getBitsInRange(long bitSet, int startIndex, int /*exclusive*/ endIndex) {
        if (startIndex < 0 || endIndex > Long.SIZE) {
            throw new IllegalArgumentException("Invalid value for bitshift");
        }
        int length = endIndex - startIndex;
        return (bitSet & (((1L << length) - 1) << startIndex)) >>> startIndex;
    }

    public static long setBitsInRange(long bitSet, int startIndex, int /*exclusive*/ endIndex, long value) {
        if (startIndex < 0 || endIndex > Long.SIZE) {
            throw new IllegalArgumentException("Invalid value for bitshift");
        }
        int length = endIndex - startIndex;
        int valueBitLength = Long.SIZE - Long.numberOfLeadingZeros(value);
        if (valueBitLength > length) {
            throw new IllegalArgumentException(String.format(
                    "Value %d with %d bit length too large to be saved in the given range (%d, %d)",
                    value, valueBitLength, startIndex, endIndex));
        }
        bitSet = setZerosInRange(bitSet, startIndex, endIndex);

        return bitSet | (value << startIndex);
    }

    public static long setBitsInRange(long bitSet, int startIndex, int /*exclusive*/ endIndex, byte value) {
        if (startIndex < 0 || endIndex > Long.SIZE) {
            throw new IllegalArgumentException("Invalid value for bitshift");
        }
        bitSet = setZerosInRange(bitSet, startIndex, endIndex);

        return bitSet | ((long) value << startIndex);
    }

    public static long setZerosInRange(long bitSet, int startIndex, int endIndex) {
        int length = endIndex - startIndex;
        return bitSet &  ~(((1L << length) - 1) << startIndex);
    }
}
