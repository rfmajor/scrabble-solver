package com.rfmajor.scrabblesolver.common.gaddag.model;

import com.rfmajor.scrabblesolver.common.scrabble.Alphabet;
import com.rfmajor.scrabblesolver.common.gaddag.utils.BitSetUtils;

import static com.rfmajor.scrabblesolver.common.gaddag.utils.CompressedGaddagUtils.getLetterOffset;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.CompressedGaddagUtils.getStateBitMap;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExpandedGaddagUtils.getDestinationStateId;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExpandedGaddagUtils.getLetterBitMapId;

public class CompressedGaddag extends Gaddag<Long> {
    protected final long[] arcsAndStates;

    protected final int[] letterSets;

    public CompressedGaddag(Long rootArc, Alphabet alphabet, char delimiter, long[] arcsAndStates, int[] letterSets) {
        super(rootArc, alphabet, delimiter);
        this.arcsAndStates = arcsAndStates;
        this.letterSets = letterSets;
    }

    @Override
    public Long findNextArc(Long arc, char letter) {
        int nextStateId = getDestinationStateId(arc);
        long state = arcsAndStates[nextStateId];
        int letterIdx = alphabet.getIndex(letter);
        int offset = getLetterOffset(state, letterIdx, alphabet.size());

        if (offset == -1 || nextStateId + offset >= arcsAndStates.length || nextStateId + offset < 0) {
            return null;
        }
        return arcsAndStates[nextStateId + offset];
    }

    @Override
    public boolean hasNextArc(Long arc, char letter) {
        int nextStateId = getDestinationStateId(arc);
        if (nextStateId >= arcsAndStates.length) {
            return false;
        }
        long state = arcsAndStates[nextStateId];
        if (state == 0 || getStateBitMap(state, alphabet.size()) == 0) {
            return false;
        }

        int letterIdx = alphabet.getIndex(letter);
        int offset = getLetterOffset(state, letterIdx, alphabet.size());

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
        long bitMap = getStateBitMap(arcsAndStates[stateId], alphabet.size());
        return bitMap == 0;
    }

    @Override
    public boolean isPresent(Long arc) {
        return arc != null;
    }
}
