package com.rfmajor.scrabblesolver.gaddag;

import com.rfmajor.scrabblesolver.TestUtils;
import com.rfmajor.scrabblesolver.common.game.Alphabet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collections;
import java.util.List;

import static com.rfmajor.scrabblesolver.TestUtils.isSequencePresent;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpandedGaddagConverterTest {
    private ExpandedGaddagConverter expandedGaddagConverter;
    private Alphabet alphabet;

    @BeforeEach
    void setUp() {
        expandedGaddagConverter = new ExpandedGaddagConverter();
        expandedGaddagConverter.setMaxNumberOfAllocatedStates(100);
        alphabet = new Alphabet(
                TestUtils.mapStringToLettersList("aąbcćdeęfghijklłmnńoóprsśtuwyzźż#"),
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    @ParameterizedTest
    @CsvSource({"c#ar, true", "ac#r, true", "rac#, true", "era, true", "era#, false"})
    void givenSomeWord_whenConvertCalled_thenSequencePresent(String sequence, boolean actual) {
        Gaddag<Long> gaddag = expandedGaddagConverter.convert(List.of("care"), alphabet);
        assertEquals(isSequencePresent(sequence, gaddag), actual);
    }
}