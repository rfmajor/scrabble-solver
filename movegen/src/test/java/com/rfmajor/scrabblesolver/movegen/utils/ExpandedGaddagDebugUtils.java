package com.rfmajor.scrabblesolver.movegen.utils;

import com.rfmajor.scrabblesolver.movegen.common.BitSetUtils;
import com.rfmajor.scrabblesolver.movegen.common.model.Alphabet;
import com.rfmajor.scrabblesolver.movegen.gaddag.ExpandedGaddagUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static com.rfmajor.scrabblesolver.movegen.gaddag.ExpandedGaddagUtils.DEST_ID_END;
import static com.rfmajor.scrabblesolver.movegen.gaddag.ExpandedGaddagUtils.DEST_ID_START;
import static com.rfmajor.scrabblesolver.movegen.gaddag.ExpandedGaddagUtils.LETTER_MAP_END;
import static com.rfmajor.scrabblesolver.movegen.gaddag.ExpandedGaddagUtils.LETTER_MAP_START;

public final class ExpandedGaddagDebugUtils {
    private static final int INDEX_MULTIPLIER = 5;
    public static List<String> getArcsToString(long[][] arcs, int[] letterSets, Alphabet alphabet) {
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

    public static String getArcsToString(byte[] arcsAndStates, int[] letterSets, Alphabet alphabet) {
        StringJoiner joiner = new StringJoiner("\n");

        int i = 0;
        while (i < arcsAndStates.length) {
            StringJoiner innerJoiner = new StringJoiner(",");
            long state = getRecord(i, arcsAndStates);
            int numberOfSetBits = getNumberOfBitMapBits(state, alphabet.size());
            innerJoiner.add(stateToString(state, i / INDEX_MULTIPLIER, alphabet));
            long bitMap = BitSetUtils.getBitsInRange(state, 0, alphabet.size());
            int letterId = 0;

            for (int j = INDEX_MULTIPLIER; j <= numberOfSetBits * INDEX_MULTIPLIER; j += INDEX_MULTIPLIER) {
                while ((bitMap & 1L) == 0) {
                    bitMap >>>= 1;
                    letterId++;
                }
                long arc = getRecord(i + j, arcsAndStates);
                innerJoiner.add(arcToString(arc, letterId, letterSets, alphabet));
                bitMap >>>= 1;
                letterId++;
            }
            joiner.add(innerJoiner.toString());
            i += numberOfSetBits * INDEX_MULTIPLIER + INDEX_MULTIPLIER;
        }
        return joiner.toString();
    }

    public static String stateToString(long state, int stateId, Alphabet alphabet) {
        Set<Integer> set = new HashSet<>();
        for (int i = 0; state != 0; i++) {
            if (((state >> i) & 1) == 1L) {
                set.add(i);
                state = state & ~(1L << i);
            }
        }
        List<Character> chars = set.stream().map(alphabet::getLetter).sorted().toList();
        String prefix = String.format("%d:(", stateId);
        StringBuilder b = new StringBuilder(prefix);
        for (Character c : chars) {
            b.append(c);
        }
        b.append(")");
        return b.toString();
    }

    public static String arcToString(long arc, int letterId, int[] letterSets, Alphabet alphabet) {
        int destinationState = ExpandedGaddagUtils.getDestinationStateId(arc);
        int letterSetId = ExpandedGaddagUtils.getLetterBitMapId(arc);
        int letterBitMap = letterSetId < letterSets.length ? letterSets[letterSetId] : 0;
        String letters = BitSetUtils.toSet(letterBitMap)
                .stream()
                .map(alphabet::getLetter)
                .map(Object::toString)
                .collect(Collectors.joining());

        return String.format("%s:(%s):%d", alphabet.getLetter(letterId), letters, destinationState);

    }

    public static String valueToBinaryString(long value) {
        return String.format("%64s", Long.toBinaryString(value)).replace(' ', '0');
    }

    public static String stateToBinaryStringWithDescription(long value, int alphabetSize) {
        String binaryString = valueToBinaryString(value);
        String bitMap = java.lang.String.valueOf(BitSetUtils.getBitsInRange(value, 0, alphabetSize));
        String otherData = java.lang.String.valueOf(BitSetUtils.getBitsInRange(value, alphabetSize, Long.SIZE));

        binaryString = binaryString.substring(0, alphabetSize) + "/" + binaryString.substring(alphabetSize, Long.SIZE);

        StringBuilder decimalValues = new StringBuilder(" ".repeat(binaryString.length()));
        decimalValues.replace(0, bitMap.length(), bitMap);
        decimalValues.replace(alphabetSize + 1, alphabetSize + 1 + otherData.length(), otherData);

        String bitMapDesc = "bitMap";
        String otherDataDesc = "other";
        StringBuilder descriptions = new StringBuilder(" ".repeat(binaryString.length()));
        descriptions.replace(0, bitMapDesc.length(), bitMapDesc);
        descriptions.replace(alphabetSize + 1, alphabetSize + 1 + otherDataDesc.length(), otherDataDesc);

        return binaryString + "\n" + decimalValues + "\n" + descriptions;
    }

    public static String arcToBinaryStringWithDescription(long value) {
        String binaryString = valueToBinaryString(value);
        String destStateId = java.lang.String.valueOf(BitSetUtils.getBitsInRange(value, DEST_ID_START, DEST_ID_END));
        String letterSetId = java.lang.String.valueOf(BitSetUtils.getBitsInRange(value, LETTER_MAP_START, LETTER_MAP_END));
        String otherData = java.lang.String.valueOf(BitSetUtils.getBitsInRange(value, LETTER_MAP_END, Long.SIZE));

        binaryString = binaryString.substring(DEST_ID_START, DEST_ID_END) + "/"
                + binaryString.substring(LETTER_MAP_START, LETTER_MAP_END) + "/"
                + binaryString.substring(LETTER_MAP_END, Long.SIZE);

        StringBuilder decimalValues = new StringBuilder(" ".repeat(binaryString.length()));
        decimalValues.replace(DEST_ID_START, DEST_ID_START + destStateId.length(), destStateId);
        decimalValues.replace(LETTER_MAP_START + 1, LETTER_MAP_START + 1 + letterSetId.length(), letterSetId);
        decimalValues.replace(LETTER_MAP_END + 2, LETTER_MAP_END + 2 + otherData.length(), otherData);

        String destStateDesc = "destStateId";
        String letterSetDesc = "letterSetId";
        String otherDataDesc = "other";
        StringBuilder descriptions = new StringBuilder(" ".repeat(binaryString.length()));
        descriptions.replace(DEST_ID_START, DEST_ID_START + destStateDesc.length(), destStateDesc);
        descriptions.replace(LETTER_MAP_START + 1, LETTER_MAP_START + 1 + letterSetDesc.length(), letterSetDesc);
        descriptions.replace(LETTER_MAP_END + 2, LETTER_MAP_END + 2 + otherDataDesc.length(), otherDataDesc);

        return binaryString + "\n" + decimalValues + "\n" + descriptions;
    }

    private static String arcToString(int stateId, int letterId, long[][] arcs, int[] letterSets, Alphabet alphabet) {
        long arc = arcs[stateId][letterId];
        int destinationState = ExpandedGaddagUtils.getDestinationStateId(arc);
        int letterSetId = ExpandedGaddagUtils.getLetterBitMapId(arc);
        int letterBitMap = letterSetId < letterSets.length ? letterSets[letterSetId] : 0;
        String letters = BitSetUtils.toSet(letterBitMap)
                .stream()
                .map(alphabet::getLetter)
                .map(Object::toString)
                .collect(Collectors.joining());

        return java.lang.String.format("%d:%d:%s:(%s)", stateId, destinationState, alphabet.getLetter(letterId), letters);
    }

    private ExpandedGaddagDebugUtils() {
    }

    private static int getNumberOfBitMapBits(long state, int alphabetSize) {
        int count = 0;
        long bitMap = BitSetUtils.getBitsInRange(state, 0, alphabetSize);
        while (bitMap > 0) {
            count += (int) (bitMap & 1L);
            bitMap >>>= 1;
        }
        return count;
    }

    private static long getRecord(int idx, byte[] array) {
        byte[] subArray = getSubArray(idx, array);
        return mergeBytes(subArray);
    }

    private static byte[] getSubArray(int start, byte[] array) {
        byte[] subArray = new byte[INDEX_MULTIPLIER];
        System.arraycopy(array, start, subArray, 0, subArray.length);
        return subArray;
    }

    private static long mergeBytes(byte[] array) {
        long result = 0L;
        for (int i = 0; i < array.length; i++) {
            result = BitSetUtils.setBitsInRange(result, i * 8, (i + 1) * 8, array[i]);
        }
        return result;
    }
}
