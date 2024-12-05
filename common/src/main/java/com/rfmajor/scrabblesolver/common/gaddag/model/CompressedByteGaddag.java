package com.rfmajor.scrabblesolver.common.gaddag.model;


import com.rfmajor.scrabblesolver.common.gaddag.utils.BitSetUtils;
import com.rfmajor.scrabblesolver.common.scrabble.Alphabet;
import lombok.Getter;

import java.io.Serializable;

import static com.rfmajor.scrabblesolver.common.gaddag.utils.CompressedGaddagUtils.INDEX_MULTIPLIER;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.CompressedGaddagUtils.getLetterOffset;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.CompressedGaddagUtils.getRecord;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.CompressedGaddagUtils.getStateBitMap;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExpandedGaddagUtils.getDestinationStateId;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExpandedGaddagUtils.getLetterBitMapId;

@Getter
public class CompressedByteGaddag extends Gaddag<Long> implements Serializable {
    protected final transient byte[] arcsAndStates;
    protected final int[] letterSets;

    public CompressedByteGaddag(Long rootArc, Alphabet alphabet, char delimiter, byte[] arcsAndStates, int[] letterSets) {
        super(rootArc, alphabet, delimiter);
        this.arcsAndStates = arcsAndStates;
        this.letterSets = letterSets;
    }

    @Override
    public Long findNextArc(Long arc, char letter) {
        int nextStateId = getDestinationStateId(arc) * INDEX_MULTIPLIER;
        long state = getRecord(nextStateId, arcsAndStates);
        int letterIdx = alphabet.getIndex(letter);
        int offset = getLetterOffset(state, letterIdx, alphabet.size()) * INDEX_MULTIPLIER;

        if (offset < 0 || nextStateId + offset >= arcsAndStates.length || nextStateId + offset < 0) {
            return null;
        }
        return getRecord(nextStateId + offset, arcsAndStates);
    }

    @Override
    public boolean hasNextArc(Long arc, char letter) {
        int nextStateId = getDestinationStateId(arc) * INDEX_MULTIPLIER;
        if (nextStateId >= arcsAndStates.length) {
            return false;
        }
        long state = getRecord(nextStateId, arcsAndStates);
        if (state == 0 || getStateBitMap(state, alphabet.size()) == 0) {
            return false;
        }

        int letterIdx = alphabet.getIndex(letter);
        int offset = getLetterOffset(state, letterIdx, alphabet.size()) * INDEX_MULTIPLIER;

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
        long record = getRecord(stateId, arcsAndStates);
        long bitMap = getStateBitMap(record, alphabet.size());

        return bitMap == 0;
    }

    @Override
    public boolean isPresent(Long arc) {
        return arc != null;
    }
}
