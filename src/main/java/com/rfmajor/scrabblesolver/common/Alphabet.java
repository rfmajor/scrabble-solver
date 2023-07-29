package com.rfmajor.scrabblesolver.common;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.rfmajor.scrabblesolver.common.exceptions.AlphabetIndexNotPresentException;
import com.rfmajor.scrabblesolver.common.exceptions.AlphabetLetterNotPresentException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class Alphabet {
    private final BiMap<Character, Integer> lettersToIndexes;
    private final Map<Character, Integer> lettersToPoints;
    private final Map<Character, Integer> lettersToQuantities;

    public Alphabet(List<Character> letters, List<Integer> points, List<Integer> quantities) {
        this.lettersToIndexes = ImmutableBiMap.copyOf(mapLettersToIndexes(letters));
        this.lettersToPoints = ImmutableMap.copyOf(mapLettersToNumericValues(letters, points));
        this.lettersToQuantities = ImmutableMap.copyOf(mapLettersToNumericValues(letters, quantities));
    }

    public char getLetter(int index) {
        if (!lettersToIndexes.containsValue(index)) {
            throw new AlphabetIndexNotPresentException(
                    String.format("Index %d is not present in the alphabet", index));
        }
        return lettersToIndexes.inverse().get(index);
    }

    public int getIndex(char letter) {
        if (!lettersToIndexes.containsKey(letter)) {
            throw new AlphabetLetterNotPresentException(
                    String.format("The letter %s is not present in the alphabet", letter));
        }
        return lettersToIndexes.get(letter);
    }

    public int size() {
        return lettersToIndexes.size();
    }

    public boolean containsLetter(char letter) {
        return lettersToIndexes.containsKey(letter);
    }

    private static Map<Character, Integer> mapLettersToIndexes(List<Character> letters) {
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
}
