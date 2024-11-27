package com.rfmajor.scrabblesolver.movegen.gaddag;

import com.rfmajor.scrabblesolver.movegen.common.model.Alphabet;
import com.rfmajor.scrabblesolver.movegen.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;

import static com.rfmajor.scrabblesolver.movegen.utils.TestUtils.isSequencePresent;
import static com.rfmajor.scrabblesolver.movegen.utils.TestUtils.isWordPresent;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpandedGaddagConverterTest {
    private ExpandedGaddagConverter expandedGaddagConverter;
    private Alphabet alphabet;
    private Gaddag<Long> gaddag;

    @BeforeEach
    void setUp() {
        expandedGaddagConverter = new ExpandedGaddagConverter();
        expandedGaddagConverter.setMaxNumberOfAllocatedStates(100);
        alphabet = new Alphabet(
                TestUtils.mapStringToLettersList("aąbcćdeęfghijklłmnńoóprsśtuwyzźż#"),
                Collections.emptyList(),
                Collections.emptyList()
        );
        gaddag = expandedGaddagConverter.convert(List.of("able", "cable", "care", "abler", "ar", "be"), alphabet);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "a#bl", "ba#l", "lba#", "elb",
            "c#abl", "ac#bl", "bac#l", "lbac#", "elba",
            "c#ar", "ac#r", "rac#", "era",
            "a#ble", "ba#le", "lba#e", "elba#", "relb",
            "a#", "r",
            "b#", "e"
    })
    void givenSomeWord_whenConvertCalled_thenSequencePresent(String sequence) {
        assertTrue(isSequencePresent(sequence, gaddag));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "a#ble", "ba#le", "lba#e", "elba",
            "c#able", "ac#ble", "bac#le", "lbac#e", "elbac",
            "c#are", "ac#re", "rac#e", "erac",
            "a#bler", "ba#ler", "lba#er", "elba#r", "relba",
            "a#r", "ra",
            "b#e", "eb"
    })
    void givenSomeWord_whenConvertCalled_thenWordPresent(String sequence) {
        assertTrue(isWordPresent(sequence, gaddag));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "c#ar", "ac#r", "rac",
            "a#bl", "ba#l", "lba"
    })
    void givenSomeWord_whenConvertCalled_thenWordNotPresent(String sequence) {
        assertFalse(isWordPresent(sequence, gaddag));
    }
}