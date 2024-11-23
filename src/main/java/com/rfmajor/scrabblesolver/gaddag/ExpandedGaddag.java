package com.rfmajor.scrabblesolver.gaddag;

import com.google.common.collect.BiMap;
import com.rfmajor.scrabblesolver.common.BitSetUtils;
import com.rfmajor.scrabblesolver.common.game.Alphabet;

import static com.rfmajor.scrabblesolver.gaddag.ExpandedGaddagUtils.getDestinationStateId;
import static com.rfmajor.scrabblesolver.gaddag.ExpandedGaddagUtils.getLetterBitMapId;

public class ExpandedGaddag extends Gaddag<Long> {
    private final long[][] arcs;
    private final BiMap<Integer, Integer> letterSets;

    public ExpandedGaddag(Long parentArc, Alphabet alphabet, char delimiter,
                          long[][] arcs, BiMap<Integer, Integer> letterSets) {
        super(parentArc, alphabet, delimiter);
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
        return letterSets.getOrDefault(letterSetId, 0);
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
