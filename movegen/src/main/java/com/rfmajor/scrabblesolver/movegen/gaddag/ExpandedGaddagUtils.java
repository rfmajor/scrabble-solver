package com.rfmajor.scrabblesolver.movegen.gaddag;

import com.rfmajor.scrabblesolver.movegen.common.BitSetUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExpandedGaddagUtils {
    private static final int SOURCE_ID_START = 0;
    private static final int SOURCE_ID_END = 32;
    private static final int DEST_ID_START = 0;
    private static final int DEST_ID_END = 32;
    private static final int LETTER_MAP_START = 32;
    private static final int LETTER_MAP_END = Long.SIZE;

    public static int getSourceStateId(long arc) {
        return (int) BitSetUtils.getBitsInRange(arc, SOURCE_ID_START, SOURCE_ID_END);
    }

    public static int getDestinationStateId(long arc) {
        return (int) BitSetUtils.getBitsInRange(arc, DEST_ID_START, DEST_ID_END);
    }

    public static int getLetterBitMapId(long arc) {
        return (int) BitSetUtils.getBitsInRange(arc, LETTER_MAP_START, LETTER_MAP_END);
    }

    public static void setSourceStateId(final int stateId, final int letterId, int destStateIdx, long[][] arcs) {
        setValue(stateId, letterId, destStateIdx, SOURCE_ID_START, SOURCE_ID_END, arcs);
    }

    public static void setDestinationStateId(final int stateId, final int letterId, int destStateIdx, long[][] arcs) {
        setValue(stateId, letterId, destStateIdx, DEST_ID_START, DEST_ID_END, arcs);
    }

    public static long setDestinationStateId(long arc, int destStateIdx) {
        return BitSetUtils.setBitsInRange(arc, DEST_ID_START, DEST_ID_END, destStateIdx);
    }

    public static void setLetterBitMapId(final int stateId, final int letterId, int letterMapIdx, long[][] arcs) {
        setValue(stateId, letterId, letterMapIdx, LETTER_MAP_START, LETTER_MAP_END, arcs);
    }

    public static long setLetterBitMapId(long arc, int letterMapIdx) {
        return BitSetUtils.setBitsInRange(arc, LETTER_MAP_START, LETTER_MAP_END, letterMapIdx);
    }

    private static void setValue(final int stateId, final int letterId, int value, int start, int end, long[][] arcs) {
        arcs[stateId][letterId] = BitSetUtils.setBitsInRange(arcs[stateId][letterId], start, end, value);
    }
}
