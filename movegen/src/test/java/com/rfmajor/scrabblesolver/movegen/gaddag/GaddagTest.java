package com.rfmajor.scrabblesolver.movegen.gaddag;

import com.rfmajor.scrabblesolver.movegen.common.BitSetUtils;
import com.rfmajor.scrabblesolver.movegen.common.model.Alphabet;
import com.rfmajor.scrabblesolver.movegen.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class GaddagTest<A> {
    protected Gaddag<A> gaddag;
    protected Alphabet alphabet;
    protected boolean initialized;

    protected abstract GaddagConverter<A> createConverter();

    @BeforeEach
    void setUp() {
        if (!initialized) {
            alphabet = new Alphabet(
                    TestUtils.mapStringToLettersList("abcdefghijklmnopqrstuvwxyz#"),
                    Collections.emptyList(),
                    Collections.emptyList()
            );
            GaddagConverter<A> gaddagConverter = createConverter();
            gaddag = gaddagConverter.convert(
                    List.of("pa", "pi", "op", "able", "payable", "parable", "pay", "par", "part", "park"),
                    alphabet);
            initialized = true;
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"p#ar","ap#r", "rap#"})
    void givenWord_whenGetOneLetterCompletion_thenReturnCompletion(String word) {
        int bitSet = gaddag.getOneLetterCompletion(word);
        Assertions.assertTrue(BitSetUtils.containsOnly(bitSet, alphabet.getIndex('t'), alphabet.getIndex('k')));
    }

    @Test
    void givenOneLetter_whenGetOneLetterCompletion_thenReturnCompletion() {
        int bitSet = gaddag.getOneLetterCompletion("p");
        assertTrue(BitSetUtils.containsOnly(bitSet, alphabet.getIndex('o')));
    }

    @Test
    void given2Words_whenGetOneLetterCompletion_thenReturnCompletion() {
        String firstWord = "ap#";
        String secondWord = "able";
        int bitSet = gaddag.getOneLetterCompletion(firstWord, secondWord);
        assertTrue(BitSetUtils.containsOnly(bitSet, alphabet.getIndex('r'), alphabet.getIndex('y')));
    }

    @Test
    void name() {
        GaddagConverter<A> gaddagConverter = createConverter();
        gaddag = gaddagConverter.convert(
                List.of("park", "part"),
                alphabet);
        String word = "p#ar";
        int bitSet = gaddag.getOneLetterCompletion(word);
        assertTrue(BitSetUtils.containsOnly(bitSet, alphabet.getIndex('k'), alphabet.getIndex('t')));
    }
}