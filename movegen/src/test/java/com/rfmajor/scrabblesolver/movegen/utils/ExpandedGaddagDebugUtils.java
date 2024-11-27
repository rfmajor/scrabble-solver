package com.rfmajor.scrabblesolver.movegen.utils;

import com.google.common.collect.BiMap;
import com.rfmajor.scrabblesolver.movegen.common.BitSetUtils;
import com.rfmajor.scrabblesolver.movegen.common.model.Alphabet;
import com.rfmajor.scrabblesolver.movegen.gaddag.ExpandedGaddagUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public final class ExpandedGaddagDebugUtils {
    public static List<String> getArcsToString(long[][] arcs, BiMap<Integer, Integer> letterSets, Alphabet alphabet) {
        List<String> rows = new ArrayList<>();

        for (int i = 0; i < arcs.length; i++) {
            StringJoiner joiner = new StringJoiner(",", "[", "]");
            for (int j = 0; j < arcs[0].length; j++) {
                if (arcs[i][j] == 0) {
                    joiner.add("-");
                } else {
                    joiner.add(arcToString(i, j, arcs, letterSets, alphabet));
                }
            }
            rows.add(joiner.toString());
        }
        return rows;
    }

    private static String arcToString(int stateId, int letterId, long[][] arcs, BiMap<Integer, Integer> letterSets, Alphabet alphabet) {
        long arc = arcs[stateId][letterId];
        int destinationState = ExpandedGaddagUtils.getDestinationStateId(arc);
        int letterBitMap = letterSets.getOrDefault(ExpandedGaddagUtils.getLetterBitMapId(arc), 0);
        String letters = BitSetUtils.toSet(letterBitMap)
                .stream()
                .map(alphabet::getLetter)
                .map(Object::toString)
                .collect(Collectors.joining());

        return String.format("%d:%d:%s:(%s)", stateId, destinationState, alphabet.getLetter(letterId), letters);
    }

    private ExpandedGaddagDebugUtils() {
    }
}
