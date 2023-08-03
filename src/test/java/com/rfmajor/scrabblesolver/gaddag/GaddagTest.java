package com.rfmajor.scrabblesolver.gaddag;

import com.rfmajor.scrabblesolver.TestUtils;
import com.rfmajor.scrabblesolver.common.game.Alphabet;
import com.rfmajor.scrabblesolver.common.BitSetUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GaddagTest {
    private Gaddag gaddag;
    private GaddagConverter gaddagConverter;
    private Alphabet alphabet;
    private boolean initialized;

    @BeforeEach
    void setUp() {
        if (!initialized) {
            alphabet = new Alphabet(
                    TestUtils.mapStringToLettersList("abcdefghijklmnopqrstuvwxyz"),
                    Collections.emptyList(),
                    Collections.emptyList()
            );
            gaddagConverter = new GaddagConverter();
            gaddagConverter.setDelimiter('#');
            Arc parentArc = gaddagConverter.convert(
                    List.of("pa", "able", "payable", "parable", "pay", "par", "part", "park"),
                    alphabet);
            gaddag = new Gaddag(parentArc, alphabet, gaddagConverter.getDelimiter());
            initialized = true;
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"p#ar","ap#r", "rap#"})
    void givenWord_whenGetOneLetterCompletion_thenReturnCompletion(String word) {
        int bitSet = gaddag.getOneLetterCompletion(word);
        assertTrue(BitSetUtils.containsOnly(bitSet, alphabet.getIndex('t'), alphabet.getIndex('k')));
    }

    @Test
    void given2Words_whenGetOneLetterCompletion_thenReturnCompletion() {
        String firstWord = "ap#";
        String secondWord = "able";
        int bitSet = gaddag.getOneLetterCompletion(firstWord, secondWord);
        assertTrue(BitSetUtils.containsOnly(bitSet, alphabet.getIndex('r'), alphabet.getIndex('y')));
    }

}