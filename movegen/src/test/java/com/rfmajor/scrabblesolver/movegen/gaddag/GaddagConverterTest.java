package com.rfmajor.scrabblesolver.movegen.gaddag;

import com.rfmajor.scrabblesolver.movegen.common.model.Alphabet;
import com.rfmajor.scrabblesolver.movegen.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.rfmajor.scrabblesolver.movegen.utils.TestUtils.isSequencePresent;
import static com.rfmajor.scrabblesolver.movegen.utils.TestUtils.isWordPresent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GaddagConverterTest {
    private Gaddag<Long> expandedGaddag;
    private Gaddag<Long> compressedGaddag;
    private Gaddag<Long> compressedByteGaddag;
    private Gaddag<Arc> simpleGaddag;

    private static final String[] SEQUENCE_PRESENT = new String[]{
            "a#bl", "ba#l", "lba#", "elb",
            "c#abl", "ac#bl", "bac#l", "lbac#", "elba",
            "c#ar", "ac#r", "rac#", "era",
            "a#ble", "ba#le", "lba#e", "elba#", "relb",
            "a#", "r",
            "b#", "e"
    };

    private static final String[] WORD_PRESENT = new String[]{
            "a#ble", "ba#le", "lba#e", "elba",
            "c#able", "ac#ble", "bac#le", "lbac#e", "elbac",
            "c#are", "ac#re", "rac#e", "erac",
            "a#bler", "ba#ler", "lba#er", "elba#r", "relba",
            "a#r", "ra",
            "b#e", "eb"
    };

    private static final String[] WORD_NOT_PRESENT = new String[]{
            "c#ar", "ac#r", "rac",
            "a#bl", "ba#l", "lba"
    };

    private static final TestSet[] TEST_SETS = new TestSet[] {
            new TestSet(SEQUENCE_PRESENT, TestType.SEQUENCE, true),
            new TestSet(WORD_PRESENT, TestType.WORD, true),
            new TestSet(WORD_NOT_PRESENT, TestType.WORD, false)
    };

    @BeforeAll
    void setUp() {
        SimpleGaddagConverter simpleGaddagConverter = new SimpleGaddagConverter();
        ExpandedGaddagConverter expandedGaddagConverter = new ExpandedGaddagConverter();
        ExpandedGaddagCompressor expandedGaddagCompressor = new ExpandedGaddagCompressor();
        ExpandedGaddagByteArrayCompressor expandedByteGaddagCompressor = new ExpandedGaddagByteArrayCompressor();

        Alphabet alphabet = new Alphabet(
                TestUtils.mapStringToLettersList("aąbcćdeęfghijklłmnńoóprsśtuwyzźż#"),
                Collections.emptyList(),
                Collections.emptyList()
        );
        List<String> words = List.of("able", "cable", "care", "abler", "ar", "be");
        simpleGaddag = simpleGaddagConverter.convert(words, alphabet);
        expandedGaddag = expandedGaddagConverter.convert(words, alphabet);
        compressedGaddag = expandedGaddagCompressor.minimize((ExpandedGaddag) expandedGaddag);
        compressedByteGaddag = expandedByteGaddagCompressor.minimize((ExpandedGaddag) expandedGaddag);
    }

    @ParameterizedTest
    @MethodSource("getArgumentsForTheTests")
    void executeTestCases_simpleGaddag(TestCase testCase) {
        executeTestCases(testCase, simpleGaddag);
    }

    @ParameterizedTest
    @MethodSource("getArgumentsForTheTests")
    void executeTestCases_expandedGaddag(TestCase testCase) {
        executeTestCases(testCase, expandedGaddag);
    }

    @ParameterizedTest
    @MethodSource("getArgumentsForTheTests")
    void executeTestCases_compressedGaddag(TestCase testCase) {
        executeTestCases(testCase, compressedGaddag);
    }

    @ParameterizedTest
    @MethodSource("getArgumentsForTheTests")
    void executeTestCases_compressedByteGaddag(TestCase testCase) {
        executeTestCases(testCase, compressedByteGaddag);
    }

    private <A> void executeTestCases(TestCase testCase, Gaddag<A> gaddag) {
        assertNotNull(testCase);

        switch (testCase.testType) {
            case WORD -> assertEquals(isWordPresent(testCase.word, gaddag), testCase.expected);
            case SEQUENCE -> assertEquals(isSequencePresent(testCase.word, gaddag), testCase.expected);
            default -> throw new IllegalStateException("Invalid test type");
        }
    }

    private static Stream<Arguments> getArgumentsForTheTests() {
        return Arrays.stream(TEST_SETS)
                .flatMap(testSet -> Arrays.stream(testSet.words)
                        .map(word -> new TestCase(word, testSet.testType, testSet.expected)))
                .map(testCase -> Named.of(getTestName(testCase), testCase))
                .map(Arguments::of);
    }

    private static String getTestName(TestCase testCase) {
        return String.format(
                "%s: %s %s", testCase.word, testCase.testType, testCase.expected ? "PRESENT" : "NOT PRESENT");
    }

    private record TestSet(String[] words, TestType testType, boolean expected) {}
    private enum TestType { SEQUENCE, WORD }
    private record TestCase(String word, TestType testType, boolean expected) {}
}