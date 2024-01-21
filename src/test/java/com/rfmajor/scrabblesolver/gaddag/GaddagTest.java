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
    private Gaddag<Arc> gaddag;
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
            GaddagConverter<Arc> gaddagObjectConverter = new GaddagObjectConverter();
            gaddagObjectConverter.setDelimiter('#');
            Arc parentArc = gaddagObjectConverter.convert(
                    List.of("pa", "pi", "op", "able", "payable", "parable", "pay", "par", "part", "park"),
                    alphabet);
            gaddag = new SimpleGaddag(parentArc, alphabet, gaddagObjectConverter.getDelimiter());
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

}