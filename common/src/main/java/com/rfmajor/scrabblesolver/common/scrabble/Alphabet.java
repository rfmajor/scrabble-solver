package com.rfmajor.scrabblesolver.common.scrabble;

import com.rfmajor.scrabblesolver.common.gaddag.utils.BitSetUtils;
import com.rfmajor.scrabblesolver.common.gaddag.utils.ExportingUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Alphabet {
    private final Map<Character, Integer> lettersToIndices;
    private final char[] indicesToLetters;
    private final int[] indicesToPoints;
    private final int[] indicesToQuantities;

    public static final int BYTE_ARRAY_DIVISOR = 10;

    public Alphabet(List<Character> letters, List<Integer> points, List<Integer> quantities) {
        this.lettersToIndices = new HashMap<>();
        this.indicesToLetters = new char[letters.size()];
        this.indicesToPoints = new int[letters.size()];
        this.indicesToQuantities = new int[letters.size()];
        init(letters, points, quantities);
    }

    public Alphabet(char[] indicesToLetters, int[] indicesToPoints, int[] indicesToQuantities) {
        this.lettersToIndices = new HashMap<>();
        this.indicesToLetters = indicesToLetters;
        this.indicesToPoints = indicesToPoints;
        this.indicesToQuantities = indicesToQuantities;
        init();
    }

    public int getDelimiterIndex() {
        return lettersToIndices.size() - 1;
    }

    public char getDelimiter() {
        return indicesToLetters[getDelimiterIndex()];
    }

    public char getLetter(int index) {
        if (!lettersToIndices.containsValue(index)) {
            throw new IllegalArgumentException(
                    String.format("Index %d is not present in the alphabet", index));
        }
        return indicesToLetters[index];
    }

    public int getPoints(char letter) {
        int index = lettersToIndices.get(letter);
        return indicesToPoints[index];
    }

    public Set<Character> letterSet() {
        return lettersToIndices.keySet();
    }

    public int getIndex(char letter) {
        if (!lettersToIndices.containsKey(letter)) {
            throw new IllegalArgumentException(
                    String.format("The letter %s is not present in the alphabet", letter));
        }
        return lettersToIndices.get(letter);
    }

    public int size() {
        return lettersToIndices.size();
    }

    public boolean containsLetter(char letter) {
        return lettersToIndices.containsKey(letter);
    }

    public boolean isLegalWord(String line) {
        for (int i = 0; i < line.length(); i++) {
            if (!containsLetter(line.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public List<Character> getAllowedLetters(int letterSet) {
        return lettersToIndices.keySet().stream()
                .filter(letter -> BitSetUtils.contains(letterSet, getIndex(letter)))
                .toList();
    }

    public byte[] asByteArray() {
        byte[] indicesToLettersBytes = ExportingUtils.charArrayToBytes(indicesToLetters);
        byte[] indicesToPointsBytes = ExportingUtils.intArrayToBytes(indicesToPoints);
        byte[] indicesToQuantitiesBytes = ExportingUtils.intArrayToBytes(indicesToQuantities);

        byte[] bytes = new byte[indicesToLettersBytes.length + indicesToPointsBytes.length + indicesToQuantitiesBytes.length];

        System.arraycopy(indicesToLettersBytes, 0, bytes, 0, indicesToLettersBytes.length);
        System.arraycopy(indicesToPointsBytes, 0, bytes, indicesToLettersBytes.length, indicesToPointsBytes.length);
        System.arraycopy(indicesToQuantitiesBytes, 0, bytes, indicesToPointsBytes.length, indicesToQuantitiesBytes.length);

        return bytes;
    }

    private void init(List<Character> letters, List<Integer> points, List<Integer> quantities) {
        for (int i = 0; i < letters.size(); i++) {
            char letter = letters.get(i);
            lettersToIndices.put(letter, i);
            indicesToLetters[i] = letter;
            indicesToPoints[i] = i < points.size() ? points.get(i) : 0;
            indicesToQuantities[i] = i < quantities.size() ? quantities.get(i) : 0;
        }
    }

    private void init() {
        for (int i = 0; i < indicesToLetters.length; i++) {
            lettersToIndices.put(indicesToLetters[i], i);
        }
    }
}
