package com.rfmajor.scrabblesolver.common.gaddag.model;


import com.rfmajor.scrabblesolver.common.scrabble.Alphabet;
import com.rfmajor.scrabblesolver.common.gaddag.utils.BitSetUtils;
import lombok.Getter;

import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExpandedGaddagUtils.getDestinationStateId;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExpandedGaddagUtils.getLetterBitMapId;

@Getter
public class ExpandedGaddag extends Gaddag<Long> {
    protected final long[][] arcs;
    protected final int[] letterSets;

    public ExpandedGaddag(Long rootArc, Alphabet alphabet, char delimiter,
                          long[][] arcs, int[] letterSets) {
        super(rootArc, alphabet, delimiter);
        this.arcs = arcs;
        this.letterSets = letterSets;
    }

    @Override
    public Long findNextArc(Long arc, char letter) {
        int nextStateId = getDestinationStateId(arc);
        return arcs[nextStateId][alphabet.getIndex(letter)];
    }

    @Override
    public boolean hasNextArc(Long arc, char letter) {
        int stateId = getDestinationStateId(arc);
        return arcs[stateId][alphabet.getIndex(letter)] != 0;
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
        for (int i = 0; i < arcs[0].length; i++) {
            if (arcs[stateId][i] != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isPresent(Long arc) {
        return arc != null && arc != 0;
    }
}
