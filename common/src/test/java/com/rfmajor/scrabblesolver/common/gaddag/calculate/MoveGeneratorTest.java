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
import com.rfmajor.scrabblesolver.common.scrabble.Field;
import com.rfmajor.scrabblesolver.common.scrabble.Move;
import com.rfmajor.scrabblesolver.common.scrabble.Rack;
import com.rfmajor.scrabblesolver.common.scrabble.SpecialFields;
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MoveGeneratorTest {
    private Board board;
    private MoveGenerator<Long> expandedMoveGenerator;
    private MoveGenerator<Arc> simpleMoveGenerator;
    private MoveGenerator<Long> compressedMoveGenerator;
    private MoveGenerator<Long> compressedByteMoveGenerator;
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
            "/moveGenerator/horizontal_able_anywhere_blank.json",
            "/moveGenerator/horizontal_able_lowerLeft_blank.json",
            "/moveGenerator/horizontal_able_lowerRight_blank.json",
            "/moveGenerator/horizontal_able_upperLeft_blank.json",
            "/moveGenerator/horizontal_able_upperRight_blank.json",
            "/moveGenerator/vertical_able_anywhere_blank.json",
            "/moveGenerator/vertical_able_upperLeft_blank.json",
            "/moveGenerator/vertical_able_lowerLeft_blank.json",
            "/moveGenerator/vertical_able_upperRight_blank.json",
            "/moveGenerator/vertical_able_lowerRight_blank.json",
            "/moveGenerator/vertical_able_middle_blankRack.json",
            "/moveGenerator/vertical_able_middle_blankRack_blank.json",
    };


    @BeforeAll
    void setUpClass() {
        board = new Board();
        MovePostProcessor movePostProcessor = new MovePostProcessor();
        PointCalculator pointCalculator = new PointCalculator(SpecialFields.loadDefault());

        // Points per letter according to the english rules:
        // abcdefghijklmnopqrstuvwxyz#
        // 1332142418513113A11114484A (A == 10)
        Alphabet alphabet = new Alphabet(
                TestUtils.mapStringToLettersList("abcdefghijklmnopqrstuvwxyz#"),
                List.of(1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 5, 1, 3, 1, 1, 3, 10, 1, 1, 1, 1, 4, 4, 8, 4, 10),
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

        expandedCrossSetCalculator = new CrossSetCalculator<>(expandedGaddag);
        expandedMoveGenerator = new MoveGenerator<>(expandedGaddag,
                movePostProcessor, pointCalculator, expandedCrossSetCalculator);
        simpleCrossSetCalculator = new CrossSetCalculator<>(simpleGaddag);
        simpleMoveGenerator = new MoveGenerator<>(simpleGaddag,
                movePostProcessor, pointCalculator, simpleCrossSetCalculator);
        compressedCrossSetCalculator = new CrossSetCalculator<>(compressedGaddag);
        compressedMoveGenerator = new MoveGenerator<>(compressedGaddag,
                movePostProcessor, pointCalculator, compressedCrossSetCalculator);
        compressedByteCrossSetCalculator = new CrossSetCalculator<>(compressedByteGaddag);
        compressedByteMoveGenerator = new MoveGenerator<>(compressedByteGaddag,
                movePostProcessor, pointCalculator, compressedByteCrossSetCalculator);
    }

    @BeforeEach
    void setUp() {
        board.clear();
    }

    @ParameterizedTest
    @MethodSource("getAllTestSets")
    void executeTestCases_simpleGaddag(TestSet testSet) {
        executeTestCase(testSet, simpleMoveGenerator, simpleCrossSetCalculator);
    }

    @ParameterizedTest
    @MethodSource("getAllTestSets")
    void executeTestCases_expandedGaddag(TestSet testSet) {
        executeTestCase(testSet, expandedMoveGenerator, expandedCrossSetCalculator);
    }

    @ParameterizedTest
    @MethodSource("getAllTestSets")
    void executeTestCases_compressedGaddag(TestSet testSet) {
        executeTestCase(testSet, compressedMoveGenerator, compressedCrossSetCalculator);
    }

    @ParameterizedTest
    @MethodSource("getAllTestSets")
    void executeTestCases_compressedByteGaddag(TestSet testSet) {
        executeTestCase(testSet, compressedByteMoveGenerator, compressedByteCrossSetCalculator);
    }

    private <A> void executeTestCase(TestSet testSet,
                                     MoveGenerator<A> algorithmExecutor,
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

        for (BlankSetup setup : testSet.blanksSetup) {
            board.getBlankFields().add(new Field(setup.row, setup.column));
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
                .map(expectedMove -> () -> moveIsContained(expectedMove, actualMoves))
        );
    }

    private static void moveIsContained(ExpectedMove expectedMove, List<Move> actualMoves) {
        for (Move actualMove : actualMoves) {
            if (expectedMove.word.equals(actualMove.getWord()) &&
                    expectedMove.x == actualMove.getX() &&
                    expectedMove.y == actualMove.getY()) {
                assertEquals(expectedMove.points, actualMove.getPoints());
                assertEquals(expectedMove.blanks, actualMove.getBlanks());
                return;
            }
        }
        throw new AssertionError(String.format("No actual move found for %s", expectedMove.toString()));
    }

    private static Stream<Arguments> getAllTestSets() {
        return Arrays.stream(TEST_FILENAMES)
                .map(MoveGeneratorTest::loadTestSetFromFile)
                .map(testSet -> Named.of(testSet.title, testSet))
                .map(Arguments::of);
    }

    private static TestSet loadTestSetFromFile(String filename) {
        try {
            return objectMapper.readValue(MoveGeneratorTest.class.getResourceAsStream(filename), TestSet.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private record TestSet(String title, List<WordSetup> wordsSetup, List<BlankSetup> blanksSetup,
                           Direction playDirection, String rack, List<TestCase> testCases) {}

    private record TestCase(int startX, int startY, Set<ExpectedMove> expected) {}

    private record WordSetup(String word, int row, int column, Direction direction) {}

    private record BlankSetup(int row, int column) {}

    private record ExpectedMove(String word, int x, int y, int points, Set<Integer> blanks) {}
}