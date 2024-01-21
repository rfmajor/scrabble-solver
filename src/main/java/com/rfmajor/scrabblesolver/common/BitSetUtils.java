package com.rfmajor.scrabblesolver.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

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
}
