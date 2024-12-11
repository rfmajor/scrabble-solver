package com.rfmajor.scrabblesolver.common.scrabble;

import com.rfmajor.scrabblesolver.common.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
public class AlphabetTest {
    private Alphabet alphabet;

    @BeforeAll
    void setUp() {
        alphabet = new Alphabet(
                TestUtils.mapStringToLettersList("abcdefghijklmnopqrstuvwxyz#"),
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    @Test
    void givenFullLetterSet_whenGetAllowedLetters_thenDelimiterNotContained() {
        List<Character> allowedChars = alphabet.getAllowedLetters(-1);
        assertFalse(allowedChars.contains('#'));
    }
}
