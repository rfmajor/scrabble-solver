package com.rfmajor.scrabblesolver.gaddag;

import com.rfmajor.scrabblesolver.common.CrossSetCalculator;
import com.rfmajor.scrabblesolver.common.game.Alphabet;
import com.rfmajor.scrabblesolver.common.game.Board;
import com.rfmajor.scrabblesolver.common.game.Field;
import com.rfmajor.scrabblesolver.common.game.Move;
import com.rfmajor.scrabblesolver.common.game.Rack;
import com.rfmajor.scrabblesolver.common.game.Direction;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class MoveGeneratorFacade {
    private final MoveGenerator moveGenerator;
    private final MoveGenerator transposedMoveGenerator;

    public MoveGeneratorFacade(Board board, Alphabet alphabet, Gaddag gaddag) {
        this.moveGenerator = new MoveGenerator(
                board, new CrossSetCalculator(board, gaddag), alphabet, gaddag, Direction.ACROSS);
        Board transposedBoard = board.transpose();
        this.transposedMoveGenerator = new MoveGenerator(
                transposedBoard, new CrossSetCalculator(transposedBoard, gaddag), alphabet, gaddag, Direction.DOWN);
    }

    public Set<Move> generate(Rack rack) {
        Set<Move> moves = new HashSet<>();
        generateMoves(moves, moveGenerator, rack);
        generateMoves(moves, transposedMoveGenerator, rack);
        return moves;
    }

    private void generateMoves(Set<Move> moves, MoveGenerator moveGenerator, Rack rack) {
        CrossSetCalculator crossSetCalculator = moveGenerator.getCrossSetCalculator();
        for (Field anchor : crossSetCalculator.getAnchors()) {
            List<Move> result = moveGenerator.generate(anchor.getRow(), anchor.getColumn(), rack);
            moves.addAll(result);
        }
    }
}
