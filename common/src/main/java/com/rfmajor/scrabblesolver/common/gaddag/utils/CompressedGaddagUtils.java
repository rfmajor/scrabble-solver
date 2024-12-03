package com.rfmajor.scrabblesolver.common.gaddag.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CompressedGaddagUtils {

    public static final int INDEX_MULTIPLIER = 5;

    public static long getRecord(int idx, byte[] arcsAndStates) {
        byte[] subArray = getSubArray(idx, arcsAndStates);
        return mergeBytes(subArray);
    }

    public static void setRecord(long value, int idx, byte[] arcsAndStates) {
        byte[] valueParts = divideValue(value);
        System.arraycopy(valueParts, 0, arcsAndStates, idx, valueParts.length);
    }

    public static long getArcsBitMap(long[] state, int alphabetSize) {
        long results = 0;
        for (int i = 0; i < state.length; i++) {
            if (state[i] == 0) {
                continue;
            }
            results = BitSetUtils.addToSet(results, i);
        }
        return BitSetUtils.setBitsInRange(0L, 0, alphabetSize, results);
    }

    public static int getNumberOfBitMapBits(long state, int alphabetSize) {
        int count = 0;
        long bitMap = BitSetUtils.getBitsInRange(state, 0, alphabetSize);
        while (bitMap > 0) {
            count += (int) (bitMap & 1L);
            bitMap >>>= 1;
        }
        return count;
    }

    public static int getLetterOffset(long state, int letterIdx, int alphabetSize) {
        long bitMap = getStateBitMap(state, alphabetSize);
        if ((bitMap & (1L << letterIdx)) == 0L) {
            return -1;
        }
        int offset = 1;
        for (int i = 0; i < letterIdx; i++) {
            offset += (int) (bitMap & 1L);
            bitMap >>>= 1L;
        }
        return offset;
    }

    public static long getStateBitMap(long state, int alphabetSize) {
        return BitSetUtils.getBitsInRange(state, 0, alphabetSize);
    }

    private static byte[] getSubArray(int start, byte[] original) {
        byte[] subArray = new byte[INDEX_MULTIPLIER];
        System.arraycopy(original, start, subArray, 0, subArray.length);
        return subArray;
    }

    private static long mergeBytes(byte[] array) {
        long result = 0L;
        for (int i = 0; i < array.length; i++) {
            result = BitSetUtils.setBitsInRange(result, i * 8, (i + 1) * 8, array[i]);
        }
        return result;
    }

    private static byte[] divideValue(long value) {
        byte[] result = new byte[INDEX_MULTIPLIER];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) BitSetUtils.getBitsInRange(value, i * 8, (i + 1) * 8);
        }
        return result;
    }
}
