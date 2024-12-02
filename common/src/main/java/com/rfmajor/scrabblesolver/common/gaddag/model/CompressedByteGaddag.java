package com.rfmajor.scrabblesolver.common.gaddag.model;


import com.rfmajor.scrabblesolver.common.scrabble.Alphabet;
import com.rfmajor.scrabblesolver.common.gaddag.utils.BitSetUtils;
import lombok.Getter;

import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExpandedGaddagUtils.getDestinationStateId;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExpandedGaddagUtils.getLetterBitMapId;

@Getter
public class CompressedByteGaddag extends Gaddag<Long> {
    protected final byte[] arcsAndStates;

    protected final int[] letterSets;
    private static final int INDEX_MULTIPLIER = 5;

    public CompressedByteGaddag(Long rootArc, Alphabet alphabet, char delimiter, byte[] arcsAndStates, int[] letterSets) {
        super(rootArc, alphabet, delimiter);
        this.arcsAndStates = arcsAndStates;
        this.letterSets = letterSets;
    }

    @Override
    public Long findNextArc(Long arc, char letter) {
        int nextStateId = getDestinationStateId(arc) * INDEX_MULTIPLIER;
        long state = getRecord(nextStateId);
        int letterIdx = alphabet.getIndex(letter);
        int offset = getLetterOffset(state, letterIdx) * INDEX_MULTIPLIER;

        if (offset < 0 || nextStateId + offset >= arcsAndStates.length || nextStateId + offset < 0) {
            return null;
        }
        return getRecord(nextStateId + offset);
    }

    @Override
    public boolean hasNextArc(Long arc, char letter) {
        int nextStateId = getDestinationStateId(arc) * INDEX_MULTIPLIER;
        if (nextStateId >= arcsAndStates.length) {
            return false;
        }
        long state = getRecord(nextStateId);
        if (state == 0 || getStateBitMap(state) == 0) {
            return false;
        }

        int letterIdx = alphabet.getIndex(letter);
        int offset = getLetterOffset(state, letterIdx) * INDEX_MULTIPLIER;

        if (offset < 0) {
            return false;
        }
        return nextStateId + offset < arcsAndStates.length;
    }

    @Override
    public boolean containsLetter(Long arc, char letter) {
        int letterSet = getLetterIndicesBitMap(arc);
        return BitSetUtils.contains(letterSet, alphabet.getIndex(letter));
    }

    @Override
    public int getLetterIndicesBitMap(Long arc) {
        int letterSetId = getLetterBitMapId(arc);
        if (letterSetId >= letterSets.length) {
            return 0;
        }
        return letterSets[letterSetId];
    }

    @Override
    public boolean isLastArc(Long arc) {
        int stateId = getDestinationStateId(arc) * INDEX_MULTIPLIER;
        long bitMap = getStateBitMap(getRecord(stateId));
        return bitMap == 0;
    }

    @Override
    public boolean isPresent(Long arc) {
        return arc != null;
    }

    private long getStateBitMap(long state) {
        return BitSetUtils.getBitsInRange(state, 0, alphabet.size());
    }

    private int getLetterOffset(long state, int letterIdx) {
        long bitMap = getStateBitMap(state);
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

    private long getRecord(int idx) {
        byte[] subArray = getSubArray(idx);
        return mergeBytes(subArray);
    }

    private byte[] getSubArray(int start) {
        byte[] subArray = new byte[INDEX_MULTIPLIER];
        System.arraycopy(arcsAndStates, start, subArray, 0, subArray.length);
        return subArray;
    }

    private long mergeBytes(byte[] array) {
        long result = 0L;
        for (int i = 0; i < array.length; i++) {
            result = BitSetUtils.setBitsInRange(result, i * 8, (i + 1) * 8, array[i]);
        }
        return result;
    }
}
