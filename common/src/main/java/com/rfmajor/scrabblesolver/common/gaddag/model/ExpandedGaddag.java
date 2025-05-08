package com.rfmajor.scrabblesolver.common.gaddag.model;


import com.rfmajor.scrabblesolver.common.gaddag.utils.BitSetUtils;
import com.rfmajor.scrabblesolver.common.gaddag.utils.LongBitEntry;
import com.rfmajor.scrabblesolver.common.scrabble.Alphabet;
import lombok.Getter;

import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExpandedGaddagUtils.getDestinationStateId;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExpandedGaddagUtils.getLetterBitMapId;

@Getter
public class ExpandedGaddag extends Gaddag<Long> {
    protected final long[][] arcs;
    protected final int[] letterSets;
    protected final Stats stats;

    public ExpandedGaddag(Long rootArc, Alphabet alphabet, char delimiter,
                          long[][] arcs, int[] letterSets) {
        super(rootArc, alphabet, delimiter);
        this.arcs = arcs;
        this.letterSets = letterSets;
        this.stats = collectStats(arcs, letterSets);
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

    public record Stats(
            LongBitEntry nonEmptyArcs,
            LongBitEntry emptyArcs,
            LongBitEntry allArcs,
            LongBitEntry nonEmptyStates,
            LongBitEntry emptyStates,
            LongBitEntry allStates,
            LongBitEntry letterSets
    ) {
        public Stats(long nonEmptyArcs, long emptyArcs, long allArcs, long nonEmptyStates, long emptyStates, long allStates, long letterSets) {
            this(
                    LongBitEntry.of(nonEmptyArcs),
                    LongBitEntry.of(emptyArcs),
                    LongBitEntry.of(allArcs),
                    LongBitEntry.of(nonEmptyStates),
                    LongBitEntry.of(emptyStates),
                    LongBitEntry.of(allStates),
                    LongBitEntry.of(letterSets)
            );
        }

        @Override
        public String toString() {
            return "{" +
                    "\nnonEmptyArcs: " + nonEmptyArcs +
                    "\nemptyArcs: " + emptyArcs +
                    "\nallArcs: " + allArcs +
                    "\nnonEmptyStates: " + nonEmptyStates +
                    "\nemptyStates: " + emptyStates +
                    "\nallStates: " + allStates +
                    "\nletterSets: " + letterSets +
                    "\n}";
        }
    }

    private static Stats collectStats(long[][] arcs, int[] letterSetsArr) {
        long nonEmptyArcs = 0L;
        long emptyArcs = 0L;
        long allArcs = 0L;
        long nonEmptyStates = 0L;
        long emptyStates = 0L;
        long allStates = arcs.length;
        long letterSets = letterSetsArr.length;

        for (long[] state : arcs) {
            boolean stateHasArcs = false;
            for (long arc : state) {
                if (arc != 0) {
                    stateHasArcs = true;
                    nonEmptyArcs++;
                } else {
                    emptyArcs++;
                }
                allArcs++;
            }

            if (stateHasArcs) {
                nonEmptyStates++;
            } else {
                emptyStates++;
            }
        }

        return new Stats(nonEmptyArcs, emptyArcs, allArcs, nonEmptyStates, emptyStates, allStates, letterSets);
    }
}
