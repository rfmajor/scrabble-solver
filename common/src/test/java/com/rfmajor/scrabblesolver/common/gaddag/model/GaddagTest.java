package com.rfmajor.scrabblesolver.common.gaddag.model;

import com.rfmajor.scrabblesolver.common.TestUtils;
import com.rfmajor.scrabblesolver.common.gaddag.convert.ExpandedGaddagByteArrayCompressor;
import com.rfmajor.scrabblesolver.common.gaddag.convert.ExpandedGaddagCompressor;
import com.rfmajor.scrabblesolver.common.gaddag.convert.ExpandedGaddagConverter;
import com.rfmajor.scrabblesolver.common.gaddag.convert.GaddagConverter;
import com.rfmajor.scrabblesolver.common.gaddag.convert.SimpleGaddagConverter;
import com.rfmajor.scrabblesolver.common.gaddag.utils.BitSetUtils;
import com.rfmajor.scrabblesolver.common.scrabble.Alphabet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GaddagTest {
    private Gaddag<Long> expandedGaddag;
    private Gaddag<Arc> simpleGaddag;
    private Gaddag<Long> compressedGaddag;
    private Gaddag<Long> compressedByteGaddag;
    private Alphabet alphabet;

    private static final CompletionTestSet[] COMPLETION_TEST_SETS = new CompletionTestSet[] {
            new CompletionTestSet("One word",
                    new CompletionTestCase("p#ar", null, 't', 'k'),
                    new CompletionTestCase("ap#r", null, 't', 'k'),
                    new CompletionTestCase("rap#", null, 't', 'k'),
                    new CompletionTestCase("p#a", null, 'y', 'r'),
                    new CompletionTestCase("ap#", null, 'y', 'r')
            ),
            new CompletionTestSet("Two words",
                    new CompletionTestCase("ap#", "able", 'r', 'y')
            ),
            new CompletionTestSet("One letter",
                    new CompletionTestCase("p", null, 'o')
            )
    };

    @BeforeAll
    void setUp() {
        alphabet = new Alphabet(
                TestUtils.mapStringToLettersList("abcdefghijklmnopqrstuvwxyz#"),
                Collections.emptyList(),
                Collections.emptyList()
        );
        List<String> words = List.of("pa", "pi", "op", "able", "payable", "parable", "pay", "par", "part", "park");

        GaddagConverter<Long> expandedGaddagConverter = new ExpandedGaddagConverter();
        GaddagConverter<Arc> simpleGaddagConverter = new SimpleGaddagConverter();
        ExpandedGaddagCompressor expandedGaddagCompressor = new ExpandedGaddagCompressor();
        ExpandedGaddagByteArrayCompressor expandedGaddagByteArrayCompressor = new ExpandedGaddagByteArrayCompressor();

        expandedGaddag = expandedGaddagConverter.convert(words, alphabet);
        simpleGaddag = simpleGaddagConverter.convert(words, alphabet);
        compressedGaddag = expandedGaddagCompressor.minimize((ExpandedGaddag) expandedGaddag);
        compressedByteGaddag = expandedGaddagByteArrayCompressor.minimize((ExpandedGaddag) expandedGaddag);
    }

    @ParameterizedTest
    @MethodSource("getArgumentsForCompletionTesting")
    void executeCompletionTestCases_simpleGaddag(CompletionTestSet completionTestSet) {
        executeCompletionTestSet(completionTestSet, simpleGaddag);
    }

    @ParameterizedTest
    @MethodSource("getArgumentsForCompletionTesting")
    void executeCompletionTestCases_expandedGaddag(CompletionTestSet completionTestSet) {
        executeCompletionTestSet(completionTestSet, expandedGaddag);
    }

    @ParameterizedTest
    @MethodSource("getArgumentsForCompletionTesting")
    void executeCompletionTestCases_compressedGaddag(CompletionTestSet completionTestSet) {
        executeCompletionTestSet(completionTestSet, compressedGaddag);
    }

    @ParameterizedTest
    @MethodSource("getArgumentsForCompletionTesting")
    void executeCompletionTestCases_compressedByteGaddag(CompletionTestSet completionTestSet) {
        executeCompletionTestSet(completionTestSet, compressedByteGaddag);
    }

    private <A> void executeCompletionTestSet(CompletionTestSet completionTestSet, Gaddag<A> gaddag) {
        assertNotNull(completionTestSet);

        assertAll(Arrays.stream(completionTestSet.completionTestCases)
                .map(completionTestCase -> () -> {
                    int bitSet;
                    if (completionTestCase.word2 == null) {
                        bitSet = gaddag.getOneLetterCompletion(completionTestCase.word1);
                    } else {
                        bitSet = gaddag.getOneLetterCompletion(completionTestCase.word1, completionTestCase.word2);
                    }
                    int[] expectedIndices = IntStream.range(0, completionTestCase.expected.length)
                            .mapToObj(i -> completionTestCase.expected[i])
                            .map(alphabet::getIndex)
                            .mapToInt(Integer::intValue)
                            .toArray();
                    Assertions.assertTrue(BitSetUtils.containsOnly(bitSet, expectedIndices));
                })
        );
    }

    private static Stream<Arguments> getArgumentsForCompletionTesting() {
        return Arrays.stream(COMPLETION_TEST_SETS)
                .map(completionTestSet -> Named.of(completionTestSet.name, completionTestSet))
                .map(Arguments::of);
    }

    private record CompletionTestSet(String name, CompletionTestCase... completionTestCases) {}
    private record CompletionTestCase(String word1, String word2, char... expected) {}
}
