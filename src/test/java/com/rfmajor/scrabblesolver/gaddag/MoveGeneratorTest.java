package com.rfmajor.scrabblesolver.gaddag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rfmajor.scrabblesolver.common.game.Alphabet;
import com.rfmajor.scrabblesolver.common.game.Board;
import com.rfmajor.scrabblesolver.common.game.Direction;
import com.rfmajor.scrabblesolver.common.game.Move;
import com.rfmajor.scrabblesolver.common.game.Rack;
import com.rfmajor.scrabblesolver.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.rfmajor.scrabblesolver.utils.TestUtils.addWordToBoardHorizontally;
import static com.rfmajor.scrabblesolver.utils.TestUtils.addWordToBoardVertically;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveGeneratorTest {
    private Board board;
    private MoveGenerator<Long> moveGenerator;
    private boolean initialized;

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

    @BeforeEach
    void setUp() {
        board = new Board();
        if (!initialized) {
            Alphabet alphabet = new Alphabet(
                    TestUtils.mapStringToLettersList("abcdefghijklmnopqrstuvwxyz#"),
                    Collections.emptyList(),
                    Collections.emptyList()
            );
            List<String> words = List.of("able", "cable", "care", "abler", "ar", "be");
            GaddagConverter<Arc> simpleGaddagConverter = new SimpleGaddagConverter();
            ExpandedGaddagConverter expandedGaddagConverter = new ExpandedGaddagConverter();
            expandedGaddagConverter.setMaxNumberOfAllocatedStates(100);

            Gaddag<Arc> simpleGaddag = simpleGaddagConverter.convert(words, alphabet);
            Gaddag<Long> expandedGaddag = expandedGaddagConverter.convert(words, alphabet);
            moveGenerator = new MoveGenerator<>(board, expandedGaddag, Direction.ACROSS);
            initialized = true;
        }
    }

    @ParameterizedTest
    @MethodSource("getAllTestSets")
    void executeTestCases(TestSet testSet) {
        System.out.println(testSet.description);
        assertNotNull(testSet);

        for (WordSetup setup : testSet.wordsSetup) {
            if (setup.direction == Direction.ACROSS) {
                addWordToBoardHorizontally(setup.word, setup.row, setup.column, board);
            } else if (setup.direction == Direction.DOWN) {
                addWordToBoardVertically(setup.word, setup.row, setup.column, board);
            } else {
                throw new AssertionError(String.format("Invalid direction: %s", setup.direction));
            }
        }

        moveGenerator.computeAllCrossSets();
        assertAll(testSet.testCases.stream()
                .map(testCase -> () -> {
                    List<Move> moves = moveGenerator.generate(testCase.startX, testCase.startY, new Rack(testSet.rack));
                    assertEquals(testCase.expected.size(), moves.size());
                    assertAllMovesAreContained(testCase.expected, moves);
                })
        );
    }

    private static void assertAllMovesAreContained(Set<ExpectedMove> expectedMoves, List<Move> actualMoves) {
        assertAll(expectedMoves.stream()
                .map(expectedMove -> () -> assertTrue(moveIsContained(expectedMove, actualMoves)))
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
                .map(MoveGeneratorTest::loadTestSetFromFile)
                .map(Arguments::of);
    }

    private static TestSet loadTestSetFromFile(String filename) {
        try {
            return objectMapper.readValue(MoveGeneratorTest.class.getResourceAsStream(filename), TestSet.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private record TestSet(String description, List<WordSetup> wordsSetup,
                           Direction playDirection, String rack, List<TestCase> testCases) {}

    private record TestCase(int startX, int startY, Set<ExpectedMove> expected) {}

    private record WordSetup(String word, int row, int column, Direction direction) {}

    private record ExpectedMove(String word, int x, int y, int points, java.util.Set<Integer> blanks) {}
}