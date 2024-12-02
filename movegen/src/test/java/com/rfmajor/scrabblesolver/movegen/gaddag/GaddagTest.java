package com.rfmajor.scrabblesolver.movegen.gaddag;

import com.rfmajor.scrabblesolver.movegen.common.BitSetUtils;
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
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GaddagTest {
    private Gaddag<Long> expandedGaddag;
    private Gaddag<Arc> simpleGaddag;
    private Gaddag<Long> compressedGaddag;
    private Gaddag<Long> compressedByteGaddag;
    private Alphabet alphabet;

    private static final TestSet[] TEST_SETS = new TestSet[] {
            new TestSet("One word",
                    new TestCase("p#ar", null, 't', 'k'),
                    new TestCase("ap#r", null, 't', 'k'),
                    new TestCase("rap#", null, 't', 'k')
            ),
            new TestSet("Two words",
                    new TestCase("ap#", "able", 'r', 'y')
            ),
            new TestSet("One letter",
                    new TestCase("p", null, 'o')
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
    @MethodSource("getArgumentsForTesting")
    void executeTestCases_simpleGaddag(TestSet testSet) {
        executeTestSet(testSet, simpleGaddag);
    }

    @ParameterizedTest
    @MethodSource("getArgumentsForTesting")
    void executeTestCases_expandedGaddag(TestSet testSet) {
        executeTestSet(testSet, expandedGaddag);
    }

    @ParameterizedTest
    @MethodSource("getArgumentsForTesting")
    void executeTestCases_compressedGaddag(TestSet testSet) {
        executeTestSet(testSet, compressedGaddag);
    }

    @ParameterizedTest
    @MethodSource("getArgumentsForTesting")
    void executeTestCases_compressedByteGaddag(TestSet testSet) {
        executeTestSet(testSet, compressedByteGaddag);
    }

    private <A> void executeTestSet(TestSet testSet, Gaddag<A> gaddag) {
        assertNotNull(testSet);

        assertAll(Arrays.stream(testSet.testCases)
                .map(testCase -> () -> {
                    int bitSet;
                    if (testCase.word2 == null) {
                        bitSet = gaddag.getOneLetterCompletion(testCase.word1);
                    } else {
                        bitSet = gaddag.getOneLetterCompletion(testCase.word1, testCase.word2);
                    }
                    int[] expectedIndices = IntStream.range(0, testCase.expected.length)
                            .mapToObj(i -> testCase.expected[i])
                            .map(alphabet::getIndex)
                            .mapToInt(Integer::intValue)
                            .toArray();
                    assertTrue(BitSetUtils.containsOnly(bitSet, expectedIndices));
                })
        );
    }

    private static Stream<Arguments> getArgumentsForTesting() {
        return Arrays.stream(TEST_SETS)
                .map(testSet -> Named.of(testSet.name, testSet))
                .map(Arguments::of);
    }

    private record TestSet(String name, TestCase... testCases) {}
    private record TestCase(String word1, String word2, char... expected) {}
}
