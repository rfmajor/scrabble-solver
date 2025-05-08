package com.rfmajor.scrabblesolver.common.gaddag.model;


import com.rfmajor.scrabblesolver.common.gaddag.utils.BitSetUtils;
import com.rfmajor.scrabblesolver.common.gaddag.utils.LongBitEntry;
import com.rfmajor.scrabblesolver.common.scrabble.Alphabet;
import lombok.Getter;

import java.io.Serializable;

import static com.rfmajor.scrabblesolver.common.gaddag.utils.CompressedGaddagUtils.INDEX_MULTIPLIER;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.CompressedGaddagUtils.getLetterOffset;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.CompressedGaddagUtils.getNumberOfBitMapBits;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.CompressedGaddagUtils.getRecord;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.CompressedGaddagUtils.getStateBitMap;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExpandedGaddagUtils.getDestinationStateId;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExpandedGaddagUtils.getLetterBitMapId;

@Getter
public class CompressedByteGaddag extends Gaddag<Long> implements Serializable {
    protected final transient byte[] arcsAndStates;
    protected final int[] letterSets;
    protected final Stats stats;

    public CompressedByteGaddag(Long rootArc, Alphabet alphabet, char delimiter, byte[] arcsAndStates, int[] letterSets) {
        super(rootArc, alphabet, delimiter);
        this.arcsAndStates = arcsAndStates;
        this.letterSets = letterSets;
        this.stats = collectStats(arcsAndStates, letterSets, alphabet);
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

    public record Stats(
            LongBitEntry arcs,
            LongBitEntry states,
            LongBitEntry arcsAndStates,
            LongBitEntry letterSets
    ) {
        public Stats(long arcs, long states, long arcsAndStates, long letterSets) {
            this(
                    LongBitEntry.of(arcs),
                    LongBitEntry.of(states),
                    LongBitEntry.of(arcsAndStates),
                    LongBitEntry.of(letterSets)
            );
        }

        @Override
        public String toString() {
            return "{" +
                    "\narcs: " + arcs +
                    "\nstates: " + states +
                    "\narcsAndStates: " + arcsAndStates +
                    "\nletterSets: " + letterSets +
                    "\n}";
        }
    }

    private static Stats collectStats(byte[] arcsAndStatesArr, int[] letterSetsArr, Alphabet alphabet) {
        long arcs = 0L;
        long states = 0L;
        long arcsAndStates = arcsAndStatesArr.length / INDEX_MULTIPLIER;
        long letterSets = letterSetsArr.length;

        int i = 0;
        while (i < arcsAndStatesArr.length) {
            states++;

            long state = getRecord(i, arcsAndStatesArr);
            int numberOfSetBits = getNumberOfBitMapBits(state, alphabet.size());
            arcs += numberOfSetBits;

            i += INDEX_MULTIPLIER * (numberOfSetBits + 1);
        }

        return new Stats(arcs, states, arcsAndStates, letterSets);
    }
}
