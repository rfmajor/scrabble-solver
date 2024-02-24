package com.rfmajor.scrabblesolver.gaddag;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.rfmajor.scrabblesolver.common.BitSetUtils;
import com.rfmajor.scrabblesolver.common.game.Alphabet;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.rfmajor.scrabblesolver.gaddag.ExpandedGaddagUtils.getDestinationStateId;
import static com.rfmajor.scrabblesolver.gaddag.ExpandedGaddagUtils.getLetterBitMapId;
import static com.rfmajor.scrabblesolver.gaddag.ExpandedGaddagUtils.setDestinationStateId;
import static com.rfmajor.scrabblesolver.gaddag.ExpandedGaddagUtils.setLetterBitMapId;

public class ExpandedGaddagConverter implements GaddagConverter<Long> {
    @Setter
    private int maxNumberOfAllocatedStates = 100;

    @Override
    public Gaddag<Long> convert(List<String> words, Alphabet alphabet) {
        return new Converter(alphabet).convert(words);
    }

    private class Converter {
        private int nextStateId;
        private final long[][] arcs;
        private final Alphabet alphabet;
        private int currentStateId;
        private int forceStateId;
        private final BiMap<Integer, Integer> letterSets;
        private int nextLetterSetId;
        private int currentLetterSetId;
        private int forceLetterSetId;
        private Set<Integer> initializedStates;

        public Converter(Alphabet alphabet) {
            this.nextStateId = 2;
            this.arcs = new long[maxNumberOfAllocatedStates][alphabet.size()];
            this.alphabet = alphabet;
            this.letterSets = HashBiMap.create();
            this.nextLetterSetId = 1;
            this.currentLetterSetId = 0;
            this.forceLetterSetId = 0;
            this.initializedStates = new HashSet<>(Set.of(1));
        }

        public Gaddag<Long> convert(List<String> words) {
            for (String word : words) {
                currentStateId = 1;
                for (int i = word.length() - 1; i >= 2; i--) {
                    addArcIfNoneExists(alphabet.getIndex(word.charAt(i)));
                }
                addFinalArcIfNoneExists(alphabet.getIndex(word.charAt(1)), alphabet.getIndex(word.charAt(0)));

                currentStateId = 1;
                for (int i = word.length() - 2; i >= 0; i--) {
                    addArcIfNoneExists(alphabet.getIndex(word.charAt(i)));
                }
                addFinalArcIfNoneExists(alphabet.getDelimiterIndex(), alphabet.getIndex(word.charAt(word.length() - 1)));

                for (int m = word.length() - 3; m >= 0; m--) {
                    forceStateId = currentStateId;
                    forceLetterSetId = currentLetterSetId;
                    currentStateId = 1;
                    currentLetterSetId = 0;
                    for (int i = m; i >= 0; i--) {
                        addArcIfNoneExists(alphabet.getIndex(word.charAt(i)));
                    }
                    addArcIfNoneExists(alphabet.getDelimiterIndex());
                    forceArc(alphabet.getIndex(word.charAt(m + 1)), alphabet.getIndex(word.charAt(m + 2)));
                }
            }
            // just set to some non-zero value as only the state id (1) matters
            long parentArc = setLetterBitMapId(0L, Integer.MAX_VALUE);
            parentArc = setDestinationStateId(parentArc, 1);
            return new ExpandedGaddag(parentArc, alphabet, alphabet.getDelimiter(), downSizeArcs(), letterSets);
        }

        private long[][] downSizeArcs() {
            return Arrays.copyOf(arcs, nextStateId);
        }

        private void forceArc(int letterId, int letterIdToAdd) {
//            setSourceStateId(currentStateId, letterId, currentStateId, arcs);
            int currentDestinationStateId = getDestinationStateId(arcs[currentStateId][letterId]);
            if (currentDestinationStateId == 0) {
                setDestinationStateId(currentStateId, letterId, forceStateId, arcs);
                setLetterBitMapId(currentStateId, letterId, forceLetterSetId, arcs);
            } else {
                addLetterToSet(currentStateId, letterId, letterIdToAdd);
            }
        }

        private void addFinalArcIfNoneExists(final int letterId, int letterIdToAdd) {
            if (arcs[currentStateId][letterId] == 0L) {
                boolean incrementNextStateId = !isStateInitialized(nextStateId);
//                setSourceStateId(currentStateId, letterId, currentStateId, arcs);
                setDestinationStateId(currentStateId, letterId, nextStateId, arcs);
                initializedStates.add(nextStateId);

                if (incrementNextStateId) {
                    ++nextStateId;
                }
            }
            addLetterToSet(currentStateId, letterId, letterIdToAdd);
            currentLetterSetId = getLetterBitMapId(arcs[currentStateId][letterId]);
            currentStateId = getDestinationStateId(arcs[currentStateId][letterId]);
        }

        private void addArcIfNoneExists(final int letterId) {
            if (arcs[currentStateId][letterId] == 0L) {
                boolean incrementNextStateId = !isStateInitialized(nextStateId);
//                setSourceStateId(currentStateId, letterId, currentStateId, arcs);
                setDestinationStateId(currentStateId, letterId, nextStateId, arcs);
                initializedStates.add(nextStateId);

                if (incrementNextStateId) {
                    ++nextStateId;
                }
            }
            currentLetterSetId = getLetterBitMapId(arcs[currentStateId][letterId]);
            currentStateId = getDestinationStateId(arcs[currentStateId][letterId]);
        }

        private boolean isStateInitialized(int stateId) {
            return initializedStates.contains(stateId);
        }

        private int addLetterToSet(final int stateId, final int letterId, int letterIdToAdd) {
            int letterSetId = getLetterBitMapId(arcs[stateId][letterId]);
            int letterSet = letterSets.getOrDefault(letterSetId, 0);
            letterSet = BitSetUtils.addToSet(letterSet, letterIdToAdd);
            if (letterSets.containsValue(letterSet)) {
                letterSetId = letterSets.inverse().get(letterSet);
            } else {
                letterSetId = nextLetterSetId;
                ++nextLetterSetId;
                letterSets.put(letterSetId, letterSet);
            }
            setLetterBitMapId(stateId, letterId, letterSetId, arcs);

            return letterSetId;
        }
    }
}
