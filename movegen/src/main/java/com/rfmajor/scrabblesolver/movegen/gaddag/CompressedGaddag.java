package com.rfmajor.scrabblesolver.movegen.gaddag;

import com.rfmajor.scrabblesolver.movegen.common.BitSetUtils;
import com.rfmajor.scrabblesolver.movegen.common.model.Alphabet;

import static com.rfmajor.scrabblesolver.movegen.gaddag.ExpandedGaddagUtils.getDestinationStateId;
import static com.rfmajor.scrabblesolver.movegen.gaddag.ExpandedGaddagUtils.getLetterBitMapId;

public class CompressedGaddag extends Gaddag<Long> {
    protected final long[] arcsAndStates;

    protected final int[] letterSets;

    protected CompressedGaddag(Long rootArc, Alphabet alphabet, char delimiter, long[] arcsAndStates, int[] letterSets) {
        super(rootArc, alphabet, delimiter);
        this.arcsAndStates = arcsAndStates;
        this.letterSets = letterSets;
    }

    @Override
    public Long findNextArc(Long arc, char letter) {
        int nextStateId = getDestinationStateId(arc);
        long state = arcsAndStates[nextStateId];
        int letterIdx = alphabet.getIndex(letter);
        int offset = getLetterOffset(state, letterIdx);

        if (offset == -1 || nextStateId + offset >= arcsAndStates.length || nextStateId + offset < 0) {
            return null;
        }
        return arcsAndStates[nextStateId + offset];
    }

    @Override
    public boolean hasNextArc(Long arc, char letter) {
        int nextStateId = getDestinationStateId(arc);
        if (nextStateId >= arcsAndStates.length || arcsAndStates[nextStateId] == 0 ||
                getStateBitMap(arcsAndStates[nextStateId]) == 0) {
            return false;
        }
        long state = arcsAndStates[nextStateId];
        int letterIdx = alphabet.getIndex(letter);

        int offset = getLetterOffset(state, letterIdx);

        if (offset == -1) {
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
        int stateId = getDestinationStateId(arc);
        long bitMap = getStateBitMap(arcsAndStates[stateId]);
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
}
