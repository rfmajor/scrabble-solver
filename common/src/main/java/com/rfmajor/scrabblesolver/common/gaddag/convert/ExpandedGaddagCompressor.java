package com.rfmajor.scrabblesolver.common.gaddag.convert;

import com.rfmajor.scrabblesolver.common.gaddag.model.CompressedGaddag;
import com.rfmajor.scrabblesolver.common.gaddag.model.ExpandedGaddag;
import com.rfmajor.scrabblesolver.common.gaddag.model.Gaddag;
import com.rfmajor.scrabblesolver.common.gaddag.utils.BitSetUtils;
import com.rfmajor.scrabblesolver.common.gaddag.utils.ExpandedGaddagUtils;
import lombok.AllArgsConstructor;

import java.util.Arrays;

import static com.rfmajor.scrabblesolver.common.gaddag.utils.CompressedGaddagUtils.getArcsBitMap;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.CompressedGaddagUtils.getNumberOfBitMapBits;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExpandedGaddagUtils.LETTER_MAP_END;

public class ExpandedGaddagCompressor {
    private static final int INITIAL_SIZE = 16;

    public Gaddag<Long> minimize(ExpandedGaddag expandedGaddag) {
        ValueHolder valueHolder =
                new ValueHolder(new long[INITIAL_SIZE], 0, expandedGaddag.getAlphabet().size());

        // copy the states and arcs to a one dimensional array
        for (int i = 1; i < expandedGaddag.getArcs().length; i++) {
            long[] state = expandedGaddag.getArcs()[i];
            long stateBitMap = getArcsBitMap(state, valueHolder.alphabetSize);

            int stateIdx = writeToArrayAtCurrentIdx(stateBitMap, valueHolder);

            writeArcs(state, valueHolder);
            writeNewDestinationStateIdx(i, stateIdx, valueHolder);
        }

        compressArray(valueHolder.arcStateIdx, valueHolder);

        System.out.printf("Arcs and states: %d, letter sets: %d\n", valueHolder.arcsAndStates.length, expandedGaddag.getLetterSets().length);
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

        return new CompressedGaddag(0L, expandedGaddag.getAlphabet(), expandedGaddag.getDelimiter(),
                valueHolder.arcsAndStates, expandedGaddag.getLetterSets());
    }

    private void compressArray(int idx, ValueHolder valueHolder) {
        valueHolder.arcsAndStates = Arrays.copyOf(valueHolder.arcsAndStates, idx);
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
