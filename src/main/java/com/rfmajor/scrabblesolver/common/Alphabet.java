package com.rfmajor.scrabblesolver.common;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.rfmajor.scrabblesolver.common.exceptions.AlphabetIndexNotPresentException;
import com.rfmajor.scrabblesolver.common.exceptions.AlphabetLetterNotPresentException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class Alphabet {
    private final BiMap<Character, Integer> lettersToIndexes;

    public Alphabet(List<Character> letters) {
        Map<Character, Integer> lettersToIndexes =
                Stream.iterate(0, i -> i < letters.size(), i -> i + 1)
                        .collect(Collectors.toMap(letters::get, Function.identity()));
        this.lettersToIndexes = ImmutableBiMap.copyOf(lettersToIndexes);
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
}
