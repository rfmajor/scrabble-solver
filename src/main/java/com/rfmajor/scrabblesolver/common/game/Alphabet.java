package com.rfmajor.scrabblesolver.common.game;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.rfmajor.scrabblesolver.common.BitSetUtils;
import com.rfmajor.scrabblesolver.common.exceptions.AlphabetIndexNotPresentException;
import com.rfmajor.scrabblesolver.common.exceptions.AlphabetLetterNotPresentException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Alphabet {
    private final BiMap<Character, Integer> lettersToIndices;
    private final Map<Character, Integer> lettersToPoints;
    private final Map<Character, Integer> lettersToQuantities;

    public Alphabet(List<Character> letters, List<Integer> points, List<Integer> quantities) {
        this.lettersToIndices = ImmutableBiMap.copyOf(mapLettersToIndices(letters));
        this.lettersToPoints = ImmutableMap.copyOf(mapLettersToNumericValues(letters, points));
        this.lettersToQuantities = ImmutableMap.copyOf(mapLettersToNumericValues(letters, quantities));
    }

    public int getDelimiterIndex() {
        return lettersToIndices.size() - 1;
    }

    public char getDelimiter() {
        return lettersToIndices.inverse().get(getDelimiterIndex());
    }

    public char getLetter(int index) {
        if (!lettersToIndices.containsValue(index)) {
            throw new AlphabetIndexNotPresentException(
                    String.format("Index %d is not present in the alphabet", index));
        }
        return lettersToIndices.inverse().get(index);
    }

    public int getPoints(char letter) {
        return lettersToPoints.getOrDefault(letter, 0);
    }

    public Set<Character> letterSet() {
        return lettersToIndices.keySet();
    }

    public int getIndex(char letter) {
        if (!lettersToIndices.containsKey(letter)) {
            throw new AlphabetLetterNotPresentException(
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

    private static Map<Character, Integer> mapLettersToIndices(List<Character> letters) {
        return Stream.iterate(0, i -> i < letters.size(), i -> i + 1)
                        .collect(Collectors.toMap(letters::get, Function.identity()));
    }

    private static Map<Character, Integer> mapLettersToNumericValues(List<Character> letters, List<Integer> values) {
        Map<Character, Integer> lettersToPoints = new HashMap<>();
        for (int i = 0; i < letters.size() && i < values.size(); i++) {
            lettersToPoints.put(letters.get(i), values.get(i));
        }
        return lettersToPoints;
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

    public Map<Character, Integer> getLettersToQuantities() {
        return new HashMap<>(lettersToQuantities);
    }
}
