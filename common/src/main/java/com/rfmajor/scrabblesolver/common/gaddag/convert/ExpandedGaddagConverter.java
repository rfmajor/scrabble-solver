package com.rfmajor.scrabblesolver.common.gaddag.convert;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.rfmajor.scrabblesolver.common.scrabble.Alphabet;
import com.rfmajor.scrabblesolver.common.gaddag.model.ExpandedGaddag;
import com.rfmajor.scrabblesolver.common.gaddag.model.Gaddag;
import com.rfmajor.scrabblesolver.common.gaddag.utils.BitSetUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExpandedGaddagUtils.LETTER_MAP_END;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExpandedGaddagUtils.LETTER_MAP_START;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExpandedGaddagUtils.getDestinationStateId;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExpandedGaddagUtils.getLetterBitMapId;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExpandedGaddagUtils.setDestinationStateId;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExpandedGaddagUtils.setLetterBitMapId;


@Slf4j
public class ExpandedGaddagConverter implements GaddagConverter<Long> {
    @Setter
    private int initialStates = 1_048_576;

    @Override
    public Gaddag<Long> convert(Iterable<String> wordIterable, Alphabet alphabet) {
        return convert(wordIterable, alphabet, word -> true);
    }

    @Override
    public Gaddag<Long> convert(Iterable<String> wordIterable, Alphabet alphabet, Predicate<String> wordPredicate) {
        return new Converter(alphabet).convert(wordIterable, wordPredicate);
    }

    private class Converter {
        private int nextStateId;
        private double loadFactor = 0.95;
        private int maximumStates = Integer.MAX_VALUE;
        private long[][] arcs;
        private final Alphabet alphabet;
        private int currentStateId;
        private int forceStateId;
        private final BiMap<Integer, Integer> letterSets;
        private int nextLetterSetId;
        private int lastLetterSetId;
        private int forceLetterSetId;
        private final Set<Integer> initializedStates;

        public Converter(Alphabet alphabet) {
            this.nextStateId = 2;
            this.arcs = new long[initialStates][alphabet.size()];
            this.alphabet = alphabet;
            this.letterSets = HashBiMap.create();
            this.nextLetterSetId = 1;
            this.lastLetterSetId = 0;
            this.forceLetterSetId = 0;
            this.initializedStates = new HashSet<>(Set.of(1));
        }

        public Gaddag<Long> convert(Iterable<String> wordIterable, Predicate<String> wordPredicate) {
            for (String word : wordIterable) {
                if (!wordPredicate.test(word)) {
                    continue;
                }
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
                    forceLetterSetId = lastLetterSetId;
                    currentStateId = 1;
                    lastLetterSetId = 0;
                    for (int i = m; i >= 0; i--) {
                        addArcIfNoneExists(alphabet.getIndex(word.charAt(i)));
                    }
                    addArcIfNoneExists(alphabet.getDelimiterIndex());
                    forceArc(alphabet.getIndex(word.charAt(m + 1)));
                }
            }
            int maxBitmapId = (int) Math.pow(2, LETTER_MAP_END - LETTER_MAP_START - 1);
            // just set to some non-zero value as only the state id (1) matters
            long rootArc = setLetterBitMapId(0L, maxBitmapId);
            rootArc = setDestinationStateId(rootArc, 1);
            return new ExpandedGaddag(rootArc, alphabet, alphabet.getDelimiter(), downSizeArcs(),
                    mapLetterSetsToArray(letterSets));
        }

        private long[][] downSizeArcs() {
            return Arrays.copyOf(arcs, nextStateId);
        }

        private void forceArc(int letterId) {
            int currentDestinationStateId = getDestinationStateId(arcs[currentStateId][letterId]);
            if (currentDestinationStateId == 0) {
                setDestinationStateId(currentStateId, letterId, forceStateId, arcs);
            }
            setLetterBitMapId(currentStateId, letterId, forceLetterSetId, arcs);
        }

        private void addFinalArcIfNoneExists(final int letterId, int letterIdToAdd) {
            if (arcs[currentStateId][letterId] == 0L) {
                boolean incrementNextStateId = !isStateInitialized(nextStateId);
                setDestinationStateId(currentStateId, letterId, nextStateId, arcs);
                initializedStates.add(nextStateId);

                if (incrementNextStateId) {
                    incrementStateId();
                }
            }
            addLetterToSet(currentStateId, letterId, letterIdToAdd);
            lastLetterSetId = getLetterBitMapId(arcs[currentStateId][letterId]);
            currentStateId = getDestinationStateId(arcs[currentStateId][letterId]);
        }

        private void addArcIfNoneExists(final int letterId) {
            if (arcs[currentStateId][letterId] == 0L) {
                boolean incrementNextStateId = !isStateInitialized(nextStateId);
                setDestinationStateId(currentStateId, letterId, nextStateId, arcs);
                initializedStates.add(nextStateId);

                if (incrementNextStateId) {
                    incrementStateId();
                }
            }
            lastLetterSetId = getLetterBitMapId(arcs[currentStateId][letterId]);
            currentStateId = getDestinationStateId(arcs[currentStateId][letterId]);
        }

        private void incrementStateId() {
            tryResize();
            ++nextStateId;
        }

        private void tryResize() {
            double load = ((double) (nextStateId + 1)) / arcs.length;
            if (load > loadFactor) {
                int oldLength = arcs.length;
                int newLength = oldLength * 2;
                System.out.printf(("Allocating %s states\n"), newLength);
                if (newLength > maximumStates) {
                    throw new RuntimeException(String.format("Exceeded the maximum states space (tried to allocate %d)", newLength));
                }
                arcs = Arrays.copyOf(arcs, newLength);

                for (int i = oldLength; i < arcs.length; i++) {
                    arcs[i] = new long[alphabet.size()];
                }
            }
        }

        private boolean isStateInitialized(int stateId) {
            return initializedStates.contains(stateId);
        }

        private void addLetterToSet(final int stateId, final int letterId, int letterIdToAdd) {
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

        }
    }

    private int[] mapLetterSetsToArray(BiMap<Integer, Integer> letterSets) {
        // +1 because sets start from 1 in the original map
        int[] result = new int[letterSets.size() + 1];
        letterSets.forEach((k, v) -> result[k] = v);
        return result;
    }
}
