package com.rfmajor.scrabblesolver.movegen.gaddag;

import com.rfmajor.scrabblesolver.movegen.common.BitSetUtils;
import lombok.AllArgsConstructor;

import java.util.Arrays;

import static com.rfmajor.scrabblesolver.movegen.gaddag.ExpandedGaddagUtils.LETTER_MAP_END;

public class ExpandedGaddagCompressor {
    private static final int INITIAL_SIZE = 16;

    public Gaddag<Long> minimize(ExpandedGaddag expandedGaddag) {
        ValueHolder valueHolder =
                new ValueHolder(new long[INITIAL_SIZE], 0, expandedGaddag.getAlphabet().size());

        // copy the states and arcs to a one dimensional array
        for (int i = 1; i < expandedGaddag.arcs.length; i++) {
            long[] state = expandedGaddag.arcs[i];
            long stateBitMap = getArcsBitMap(state, valueHolder.alphabetSize);

            int stateIdx = writeToArrayAtCurrentIdx(stateBitMap, valueHolder);

            writeArcs(state, valueHolder);
            writeNewDestinationStateIdx(i, stateIdx, valueHolder);
        }

        compressArray(valueHolder.arcStateIdx, valueHolder);

        System.out.printf("Arcs and states: %d, letter sets: %d\n", valueHolder.arcsAndStates.length, expandedGaddag.letterSets.length);
        // iterate over the arcs and set their destination state ids to the new ids
        int i = 0;
        while (i < valueHolder.arcsAndStates.length) {
            int numberOfSetBits = getNumberOfBitMapBits(valueHolder.arcsAndStates[i], valueHolder.alphabetSize);
            for (int j = 1; j <= numberOfSetBits; j++) {
                long arc = valueHolder.arcsAndStates[i + j];
                int oldDestinationStateIdx = ExpandedGaddagUtils.getDestinationStateId(arc);
                int newDestinationStateIdx = getNewDestinationStateIdx(oldDestinationStateIdx, valueHolder);
                valueHolder.arcsAndStates[i + j] = ExpandedGaddagUtils.setDestinationStateId(arc, newDestinationStateIdx);
            }
            i += numberOfSetBits + 1;
        }

        return new CompressedGaddag(0L, expandedGaddag.alphabet, expandedGaddag.delimiter,
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
        if (valueHolder.arcStateIdx >= valueHolder.arcsAndStates.length) {
            reallocate(valueHolder);
        }
        valueHolder.arcsAndStates[valueHolder.arcStateIdx] = value;

        int oldIdx = valueHolder.arcStateIdx;
        valueHolder.arcStateIdx++;

        return oldIdx;
    }

    private void writeArcs(long[] state, ValueHolder valueHolder) {
        for (long arc : state) {
            if (arc == 0L) {
                continue;
            }
            writeToArrayAtCurrentIdx(arc, valueHolder);
        }
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

    private void writeNewDestinationStateIdx(int oldStateIdx, int newStateIdx, ValueHolder valueHolder) {
        if (oldStateIdx >= valueHolder.arcsAndStates.length) {
            reallocate(valueHolder);
        }
        long record = valueHolder.arcsAndStates[oldStateIdx];
        valueHolder.arcsAndStates[oldStateIdx] =
                BitSetUtils.setBitsInRange(record, LETTER_MAP_END, Long.SIZE, newStateIdx);
    }

    private int getNewDestinationStateIdx(int oldStateIdx, ValueHolder valueHolder) {
        long record = valueHolder.arcsAndStates[oldStateIdx];
        return (int) BitSetUtils.getBitsInRange(record, LETTER_MAP_END, Long.SIZE);
    }

    private void reallocate(ValueHolder valueHolder) {
        int newLength = valueHolder.arcsAndStates.length * 2;
        valueHolder.arcsAndStates = Arrays.copyOf(valueHolder.arcsAndStates, newLength);
    }


    @AllArgsConstructor
    private static class ValueHolder {
        private long[] arcsAndStates;
        private int arcStateIdx;
        private int alphabetSize;
    }
}
