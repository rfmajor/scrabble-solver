package com.rfmajor.scrabblesolver.common.gaddag.calculate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rfmajor.scrabblesolver.common.TestUtils;
import com.rfmajor.scrabblesolver.common.gaddag.convert.ExpandedGaddagByteArrayCompressor;
import com.rfmajor.scrabblesolver.common.gaddag.convert.ExpandedGaddagCompressor;
import com.rfmajor.scrabblesolver.common.gaddag.convert.ExpandedGaddagConverter;
import com.rfmajor.scrabblesolver.common.gaddag.convert.GaddagConverter;
import com.rfmajor.scrabblesolver.common.gaddag.convert.SimpleGaddagConverter;
import com.rfmajor.scrabblesolver.common.gaddag.model.Arc;
import com.rfmajor.scrabblesolver.common.gaddag.model.ExpandedGaddag;
import com.rfmajor.scrabblesolver.common.gaddag.model.Gaddag;
import com.rfmajor.scrabblesolver.common.scrabble.Alphabet;
import com.rfmajor.scrabblesolver.common.scrabble.Board;
import com.rfmajor.scrabblesolver.common.scrabble.Direction;
import com.rfmajor.scrabblesolver.common.scrabble.Move;
import com.rfmajor.scrabblesolver.common.scrabble.Rack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MoveAlgorithmExecutorTest {
    private Board board;
    private MoveAlgorithmExecutor<Long> expandedMoveAlgorithmExecutor;
    private MoveAlgorithmExecutor<Arc> simpleMoveAlgorithmExecutor;
    private MoveAlgorithmExecutor<Long> compressedMoveAlgorithmExecutor;
    private MoveAlgorithmExecutor<Long> compressedByteMoveAlgorithmExecutor;
    private CrossSetCalculator<Long> expandedCrossSetCalculator;
    private CrossSetCalculator<Arc> simpleCrossSetCalculator;
    private CrossSetCalculator<Long> compressedCrossSetCalculator;
    private CrossSetCalculator<Long> compressedByteCrossSetCalculator;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String[] TEST_FILENAMES = new String[]{
            "/moveGenerator/emptyBoard_middle.json",
            "/moveGenerator/vertical_able_anywhere.json",
            "/moveGenerator/vertical_able_upperLeft.json",
            "/moveGenerator/vertical_able_lowerLeft.json",
            "/moveGenerator/vertical_able_upperRight.json",
            "/moveGenerator/vertical_able_lowerRight.json",
            "/moveGenerator/horizontal_able_anywhere.json",
            "/moveGenerator/horizontal_able_upperLeft.json",
            "/moveGenerator/horizontal_able_lowerLeft.json",
            "/moveGenerator/horizontal_able_upperRight.json",
            "/moveGenerator/horizontal_able_lowerRight.json",
    };


    @BeforeAll
    void setUpClass() {
        board = new Board();
        Alphabet alphabet = new Alphabet(
                TestUtils.mapStringToLettersList("abcdefghijklmnopqrstuvwxyz#"),
                Collections.emptyList(),
                Collections.emptyList()
        );
        List<String> words = List.of("able", "cable", "care", "abler", "ar", "be");

        GaddagConverter<Arc> simpleGaddagConverter = new SimpleGaddagConverter();
        ExpandedGaddagConverter expandedGaddagConverter = new ExpandedGaddagConverter();
        ExpandedGaddagCompressor expandedGaddagCompressor = new ExpandedGaddagCompressor();
        ExpandedGaddagByteArrayCompressor expandedGaddagByteArrayCompressor = new ExpandedGaddagByteArrayCompressor();

        Gaddag<Arc> simpleGaddag = simpleGaddagConverter.convert(words, alphabet);
        Gaddag<Long> expandedGaddag = expandedGaddagConverter.convert(words, alphabet);
        Gaddag<Long> compressedGaddag = expandedGaddagCompressor.minimize((ExpandedGaddag) (expandedGaddag));
        Gaddag<Long> compressedByteGaddag = expandedGaddagByteArrayCompressor.minimize((ExpandedGaddag) expandedGaddag);

        expandedMoveAlgorithmExecutor = new MoveAlgorithmExecutor<>(expandedGaddag);
        expandedCrossSetCalculator = new CrossSetCalculator<>(expandedGaddag);
        simpleMoveAlgorithmExecutor = new MoveAlgorithmExecutor<>(simpleGaddag);
        simpleCrossSetCalculator = new CrossSetCalculator<>(simpleGaddag);
        compressedMoveAlgorithmExecutor = new MoveAlgorithmExecutor<>(compressedGaddag);
        compressedCrossSetCalculator = new CrossSetCalculator<>(compressedGaddag);
        compressedByteMoveAlgorithmExecutor = new MoveAlgorithmExecutor<>(compressedByteGaddag);
        compressedByteCrossSetCalculator = new CrossSetCalculator<>(compressedByteGaddag);
    }

    @BeforeEach
    void setUp() {
        board.clear();
    }

    @ParameterizedTest
    @MethodSource("getAllTestSets")
    void executeTestCases_simpleGaddag(TestSet testSet) {
        executeTestCase(testSet, simpleMoveAlgorithmExecutor, simpleCrossSetCalculator);
    }

    @ParameterizedTest
    @MethodSource("getAllTestSets")
    void executeTestCases_expandedGaddag(TestSet testSet) {
        executeTestCase(testSet, expandedMoveAlgorithmExecutor, expandedCrossSetCalculator);
    }

    @ParameterizedTest
    @MethodSource("getAllTestSets")
    void executeTestCases_compressedGaddag(TestSet testSet) {
        executeTestCase(testSet, compressedMoveAlgorithmExecutor, compressedCrossSetCalculator);
    }

    @ParameterizedTest
    @MethodSource("getAllTestSets")
    void executeTestCases_compressedByteGaddag(TestSet testSet) {
        executeTestCase(testSet, compressedByteMoveAlgorithmExecutor, compressedByteCrossSetCalculator);
    }

    private <A> void executeTestCase(TestSet testSet,
                                     MoveAlgorithmExecutor<A> algorithmExecutor,
                                     CrossSetCalculator<A> crossSetCalculator) {
        assertNotNull(testSet);

        for (WordSetup setup : testSet.wordsSetup) {
            if (setup.direction == Direction.ACROSS) {
                TestUtils.addWordToBoardHorizontally(setup.word, setup.row, setup.column, board);
            } else if (setup.direction == Direction.DOWN) {
                TestUtils.addWordToBoardVertically(setup.word, setup.row, setup.column, board);
            } else {
                throw new AssertionError(String.format("Invalid direction: %s", setup.direction));
            }
        }

        FieldSet fieldSet = crossSetCalculator.computeAllCrossSetsAndAnchors(board);
        assertAll(testSet.testCases.stream()
                .map(testCase -> () -> {
                    List<Move> moves = algorithmExecutor.generate(
                            testCase.startX, testCase.startY, new Rack(testSet.rack),
                            board, fieldSet, testSet.playDirection);

                    assertEquals(testCase.expected.size(), moves.size(), "Expected and actual moves differ in length");
                    assertAllMovesAreContained(testCase.expected, moves);
                })
        );
    }

    private static void assertAllMovesAreContained(Set<ExpectedMove> expectedMoves, List<Move> actualMoves) {
        assertAll(expectedMoves.stream()
                .map(expectedMove -> () -> assertTrue(moveIsContained(expectedMove, actualMoves),
                        String.format("Move %s is not contained within the results", expectedMove)))
        );
    }

    private static boolean moveIsContained(ExpectedMove expectedMove, List<Move> actualMoves) {
        for (Move actualMove : actualMoves) {
            if (expectedMove.word.equals(actualMove.getWord()) && expectedMove.x == actualMove.getX() &&
                    expectedMove.y == actualMove.getY() && expectedMove.points == actualMove.getPoints() &&
                    expectedMove.blanks.equals(actualMove.getBlanks())) {
                return true;
            }
        }
        return false;
    }

    private static Stream<Arguments> getAllTestSets() {
        return Arrays.stream(TEST_FILENAMES)
                .map(MoveAlgorithmExecutorTest::loadTestSetFromFile)
                .map(testSet -> Named.of(testSet.title, testSet))
                .map(Arguments::of);
    }

    private static TestSet loadTestSetFromFile(String filename) {
        try {
            return objectMapper.readValue(MoveAlgorithmExecutorTest.class.getResourceAsStream(filename), TestSet.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private record TestSet(String title, List<WordSetup> wordsSetup,
                           Direction playDirection, String rack, List<TestCase> testCases) {}

    private record TestCase(int startX, int startY, Set<ExpectedMove> expected) {}

    private record WordSetup(String word, int row, int column, Direction direction) {}

    private record ExpectedMove(String word, int x, int y, int points, java.util.Set<Integer> blanks) {}
}