package com.rfmajor.scrabblesolver.gaddag;

import com.rfmajor.scrabblesolver.common.game.Alphabet;

public class ExpandedGaddag extends Gaddag<Long> {
    public ExpandedGaddag(Long parentArc, Alphabet alphabet, char delimiter) {
        super(parentArc, alphabet, delimiter);
    }

    @Override
    public Long findNextArc(Long arc, char letter) {
        return null;
    }

    @Override
    public boolean hasNextArc(Long arc, char letter) {
        return false;
    }

    @Override
    public boolean containsLetter(Long arc, char letter) {
        return false;
    }

    @Override
    public int getLetterIndicesBitMap(Long arc) {
        return 0;
    }
}
