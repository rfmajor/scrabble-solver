package com.rfmajor.scrabblesolver.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BitSetUtils {
    public static int addToSet(int bitSet, int index) {
        return bitSet | (1 << index);
    }

    public static int removeFromSet(int bitSet, int index) {
        return bitSet & ~(1 << index);
    }

    public static boolean contains(int bitSet, int index) {
        return ((bitSet >> index) & 1) == 1;
    }

    public static boolean containsOnly(int bitSet, int... indexes) {
        int testSet = 0;
        for (int index : indexes) {
            testSet = addToSet(testSet, index);
        }
        return bitSet == testSet;
    }
}
