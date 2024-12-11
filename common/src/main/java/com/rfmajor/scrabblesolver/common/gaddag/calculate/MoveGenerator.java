package com.rfmajor.scrabblesolver.common.gaddag.calculate;

import com.rfmajor.scrabblesolver.common.gaddag.model.Gaddag;
import com.rfmajor.scrabblesolver.common.scrabble.Alphabet;
import com.rfmajor.scrabblesolver.common.scrabble.Board;
import com.rfmajor.scrabblesolver.common.scrabble.Direction;
import com.rfmajor.scrabblesolver.common.scrabble.Field;
import com.rfmajor.scrabblesolver.common.scrabble.Move;
import com.rfmajor.scrabblesolver.common.scrabble.Rack;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class MoveGenerator<A> {
    private final MoveAlgorithmExecutor<A> moveAlgorithmExecutor;
    private final MoveAlgorithmExecutor<A> transposedMoveAlgorithmExecutor;

    public MoveGenerator(Board board, Gaddag<A> gaddag) {
        this.moveAlgorithmExecutor = new MoveAlgorithmExecutor<>(board, gaddag, Direction.ACROSS);
        this.transposedMoveAlgorithmExecutor = new MoveAlgorithmExecutor<>(board.transpose(), gaddag, Direction.DOWN);
    }

    public Set<Move> generate(Rack rack, boolean computeCrossSets) {
        Set<Move> moves = new HashSet<>();
        generateMoves(moves, moveAlgorithmExecutor, rack, computeCrossSets);
        generateMoves(moves, transposedMoveAlgorithmExecutor, rack, computeCrossSets);
        return moves;
    }

    public Board getBoard() {
        return moveAlgorithmExecutor.getBoard();
    }

    public Board getTransposedBoard() {
        return transposedMoveAlgorithmExecutor.getBoard();
    }

    public Alphabet getAlphabet() {
        return moveAlgorithmExecutor.getAlphabet();
    }

    private void generateMoves(Set<Move> moves, MoveAlgorithmExecutor<A> moveAlgorithmExecutor, Rack rack,
                               boolean computeCrossSets) {
        if (computeCrossSets) {
            moveAlgorithmExecutor.computeAllCrossSets();
        }
        CrossSetCalculator<A> crossSetCalculator = moveAlgorithmExecutor.getCrossSetCalculator();
        for (Field anchor : crossSetCalculator.getAnchors()) {
            List<Move> result = moveAlgorithmExecutor.generate(anchor.getRow(), anchor.getColumn(), rack);
            moves.addAll(result);
        }
    }
}
