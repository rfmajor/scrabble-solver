package com.rfmajor.scrabblesolver.common.scrabble;

import com.rfmajor.scrabblesolver.common.gaddag.utils.BitSetUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Alphabet {
    private final Map<Character, Integer> lettersToIndices;
    private final char[] indicesToLetters;
    private final int[] indicesToPoints;
    private final int[] indicesToQuantities;

    public Alphabet(List<Character> letters, List<Integer> points, List<Integer> quantities) {
        this.lettersToIndices = new HashMap<>();
        this.indicesToLetters = new char[letters.size()];
        this.indicesToPoints = new int[letters.size()];
        this.indicesToQuantities = new int[letters.size()];
        mapLetters(letters, points, quantities);
    }

    private void mapLetters(List<Character> letters, List<Integer> points, List<Integer> quantities) {
        for (int i = 0; i < letters.size(); i++) {
            char letter = letters.get(i);
            lettersToIndices.put(letter, i);
            indicesToLetters[i] = letter;
            indicesToPoints[i] = i < points.size() ? points.get(i) : 0;
            indicesToQuantities[i] = i < quantities.size() ? quantities.get(i) : 0;
        }
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
}
