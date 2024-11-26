package com.rfmajor.scrabblesolver.gaddag;

import com.rfmajor.scrabblesolver.common.game.Alphabet;

public class SimpleGaddag extends Gaddag<Arc> {
    public SimpleGaddag(Arc parentArc, Alphabet alphabet, char delimiter) {
        super(parentArc, alphabet, delimiter);
    }

    @Override
    public Arc findNextArc(Arc arc, char letter) {
        return arc.getNextArc(letter);
    }

    @Override
    public boolean hasNextArc(Arc arc, char letter) {
        return arc.hasNextArc(letter);
    }

    @Override
    public boolean containsLetter(Arc arc, char letter) {
        return arc.containsLetterIndex(alphabet.getIndex(letter));
    }

    @Override
    public int getLetterIndicesBitMap(Arc arc) {
        return arc.getLetterIndicesBitMap();
    }

    @Override
    public boolean isLastArc(Arc arc) {
        return arc.getDestinationState().getOutArcs().isEmpty();
    }

    @Override
    public boolean isPresent(Arc arc) {
        return arc != null;
    }
}
