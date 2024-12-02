package com.rfmajor.scrabblesolver.movegen.gaddag;

import com.rfmajor.scrabblesolver.movegen.common.BitSetUtils;
import lombok.AllArgsConstructor;

import java.util.Arrays;

import static com.rfmajor.scrabblesolver.movegen.gaddag.ExpandedGaddagUtils.DEST_ID_END;
import static com.rfmajor.scrabblesolver.movegen.gaddag.ExpandedGaddagUtils.DEST_ID_START;

// needed bytes: 24 + 14 = 38
// actual bytes: 24 + 16 = 40
// 40 / 8 = 5 => 5 times larger array, 5 times larger index growth
public class ExpandedGaddagByteArrayCompressor {
    private static final int INITIAL_SIZE = 80;
    private static final int INDEX_MULTIPLIER = 5;

    public Gaddag<Long> minimize(ExpandedGaddag expandedGaddag) {
        ValueHolder valueHolder = new ValueHolder(new byte[INITIAL_SIZE], 0,
                expandedGaddag.getAlphabet().size(), new int[expandedGaddag.arcs.length]);

        // copy the states and arcs to a one dimensional array
        for (int i = 1; i < expandedGaddag.arcs.length; i++) {
            long[] state = expandedGaddag.arcs[i];
            long stateBitMap = getArcsBitMap(state, valueHolder.alphabetSize);

            int stateIdx = writeToArrayAtCurrentIdx(stateBitMap, valueHolder);

            writeArcs(state, valueHolder);
            writeNewDestinationStateIdx(i, stateIdx, valueHolder.stateMappings);
        }

        compressArray(valueHolder.arcStateIdx, valueHolder);

        System.out.printf("Arcs and states: %d, letter sets: %d\n", valueHolder.arcsAndStates.length / 5, expandedGaddag.letterSets.length);
        // iterate over the arcs and set their destination state ids to the new ids
        int i = 0;
        while (i < valueHolder.arcsAndStates.length) {
            long state = getRecord(i, valueHolder.arcsAndStates);
            int numberOfSetBits = getNumberOfBitMapBits(state, valueHolder.alphabetSize);
            for (int j = INDEX_MULTIPLIER; j <= numberOfSetBits * INDEX_MULTIPLIER; j += INDEX_MULTIPLIER) {
                long arc = getRecord(i + j, valueHolder.arcsAndStates);
                int oldDestinationStateIdx = ExpandedGaddagUtils.getDestinationStateId(arc);
                int newDestinationStateIdx = getNewDestinationStateIdx(oldDestinationStateIdx, valueHolder.stateMappings);
                arc = ExpandedGaddagUtils.setDestinationStateId(arc, newDestinationStateIdx);
                setRecord(arc, i + j, valueHolder.arcsAndStates);
            }
            i += numberOfSetBits * INDEX_MULTIPLIER + INDEX_MULTIPLIER;
        }

        return new CompressedByteGaddag(0L, expandedGaddag.alphabet, expandedGaddag.delimiter,
                valueHolder.arcsAndStates, expandedGaddag.letterSets);
    }

    private void compressArray(int idx, ValueHolder valueHolder) {
        valueHolder.arcsAndStates = Arrays.copyOf(valueHolder.arcsAndStates, idx);
    }

    private long getArcsBitMap(long[] state, int alphabetSize) {
        long results = 0;
        for (int i = 0; i < state.length; i++) {
            if (state[i] == 0) {
                continue;
            }
            results = BitSetUtils.addToSet(results, i);
        }
        return BitSetUtils.setBitsInRange(0L, 0, alphabetSize, results);
    }

    private int writeToArrayAtCurrentIdx(long value, ValueHolder valueHolder) {
        if (valueHolder.arcStateIdx >= valueHolder.arcsAndStates.length - INDEX_MULTIPLIER + 1) {
            reallocate(valueHolder);
        }
        setRecord(value, valueHolder.arcStateIdx, valueHolder.arcsAndStates);

        int oldIdx = valueHolder.arcStateIdx / INDEX_MULTIPLIER;
        valueHolder.arcStateIdx += INDEX_MULTIPLIER;

        return oldIdx;
    }

    private byte[] divideValue(long value) {
        byte[] result = new byte[INDEX_MULTIPLIER];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) BitSetUtils.getBitsInRange(value, i * 8, (i + 1) * 8);
        }
        return result;
    }

    private void writeArcs(long[] state, ValueHolder valueHolder) {
        for (long arc : state) {
            if (arc == 0L) {
                continue;
            }
            writeToArrayAtCurrentIdx(arc, valueHolder);
        }
    }

    private long getRecord(int idx, byte[] arcsAndStates) {
        byte[] subArray = getSubArray(idx, arcsAndStates);
        return mergeBytes(subArray);
    }

    private void setRecord(long value, int idx, byte[] arcsAndStates) {
        byte[] valueParts = divideValue(value);
        System.arraycopy(valueParts, 0, arcsAndStates, idx, valueParts.length);
    }

    private int getNumberOfBitMapBits(long state, int alphabetSize) {
        int count = 0;
        long bitMap = BitSetUtils.getBitsInRange(state, 0, alphabetSize);
        while (bitMap > 0) {
            count += (int) (bitMap & 1L);
            bitMap >>>= 1;
        }
        return count;
    }

    private byte[] getSubArray(int start, byte[] original) {
        byte[] subArray = new byte[INDEX_MULTIPLIER];
        System.arraycopy(original, start, subArray, 0, subArray.length);
        return subArray;
    }

    private long mergeBytes(byte[] array) {
        long result = 0L;
        for (int i = 0; i < array.length; i++) {
            result = BitSetUtils.setBitsInRange(result, i * 8, (i + 1) * 8, array[i]);
        }
        return result;
    }

    private void writeNewDestinationStateIdx(int oldStateIdx, int newStateIdx, int[] stateMappings) {
        stateMappings[oldStateIdx] = (int)BitSetUtils.setBitsInRange(0L, DEST_ID_START, DEST_ID_END, newStateIdx);
    }

    private int getNewDestinationStateIdx(int oldStateIdx, int[] stateMappings) {
        return (int) BitSetUtils.getBitsInRange(stateMappings[oldStateIdx], DEST_ID_START, DEST_ID_END);
    }

    private void reallocate(ValueHolder valueHolder) {
        int newLength = valueHolder.arcsAndStates.length * 2;
        valueHolder.arcsAndStates = Arrays.copyOf(valueHolder.arcsAndStates, newLength);
    }


    @AllArgsConstructor
    private static class ValueHolder {
        private byte[] arcsAndStates;
        private int arcStateIdx;
        private int alphabetSize;
        private int[] stateMappings;
    }
}
