package com.rfmajor.scrabblesolver.common.gaddag.convert;

import com.rfmajor.scrabblesolver.common.gaddag.model.CompressedByteGaddag;
import com.rfmajor.scrabblesolver.common.gaddag.model.ExpandedGaddag;
import com.rfmajor.scrabblesolver.common.gaddag.utils.BitSetUtils;
import com.rfmajor.scrabblesolver.common.gaddag.utils.ExpandedGaddagUtils;
import lombok.AllArgsConstructor;

import java.util.Arrays;

import static com.rfmajor.scrabblesolver.common.gaddag.utils.CompressedGaddagUtils.getArcsBitMap;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.CompressedGaddagUtils.getNumberOfBitMapBits;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.CompressedGaddagUtils.getRecord;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.CompressedGaddagUtils.setRecord;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExpandedGaddagUtils.DEST_ID_END;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExpandedGaddagUtils.DEST_ID_START;


// needed bytes: 24 + 14 = 38
// actual bytes: 24 + 16 = 40
// 40 / 8 = 5 => 5 times larger array, 5 times larger index growth
public class ExpandedGaddagByteArrayCompressor {
    private static final int INITIAL_SIZE = 80;
    private static final int INDEX_MULTIPLIER = 5;

    public CompressedByteGaddag minimize(ExpandedGaddag expandedGaddag) {
        ValueHolder valueHolder = new ValueHolder(new byte[INITIAL_SIZE], 0,
                expandedGaddag.getAlphabet().size(), new int[expandedGaddag.getArcs().length]);
        int emptyStateIdx = -1;

        // copy the states and arcs to a one dimensional array
        for (int i = 1; i < expandedGaddag.getArcs().length; i++) {
            long[] state = expandedGaddag.getArcs()[i];
            long stateBitMap = getArcsBitMap(state, valueHolder.alphabetSize);

            // eliminate empty states
            if (stateBitMap == 0) {
                if (emptyStateIdx == -1) {
                    emptyStateIdx = writeToArrayAtCurrentIdx(stateBitMap, valueHolder);
                }
                writeNewDestinationStateIdx(i, emptyStateIdx, valueHolder.stateMappings);
                continue;
            }

            int stateIdx = writeToArrayAtCurrentIdx(stateBitMap, valueHolder);

            writeArcs(state, valueHolder);
            writeNewDestinationStateIdx(i, stateIdx, valueHolder.stateMappings);
        }

        compressArray(valueHolder.arcStateIdx, valueHolder);

        System.out.printf("Arcs and states: %d, letter sets: %d\n", valueHolder.arcsAndStates.length / 5, expandedGaddag.getLetterSets().length);
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

        return new CompressedByteGaddag(0L, expandedGaddag.getAlphabet(), expandedGaddag.getDelimiter(),
                valueHolder.arcsAndStates, expandedGaddag.getLetterSets());
    }

    private void compressArray(int idx, ValueHolder valueHolder) {
        valueHolder.arcsAndStates = Arrays.copyOf(valueHolder.arcsAndStates, idx);
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

    private void writeArcs(long[] state, ValueHolder valueHolder) {
        for (long arc : state) {
            if (arc == 0L) {
                continue;
            }
            writeToArrayAtCurrentIdx(arc, valueHolder);
        }
    }

    private void writeNewDestinationStateIdx(int oldStateIdx, int newStateIdx, int[] stateMappings) {
        stateMappings[oldStateIdx] = (int) BitSetUtils.setBitsInRange(0L, DEST_ID_START, DEST_ID_END, newStateIdx);
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
