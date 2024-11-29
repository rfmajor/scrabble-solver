package com.rfmajor.scrabblesolver.movegen.gaddag;

import com.rfmajor.scrabblesolver.movegen.common.CrossSetCalculator;
import com.rfmajor.scrabblesolver.movegen.common.model.Alphabet;
import com.rfmajor.scrabblesolver.movegen.common.model.Board;
import com.rfmajor.scrabblesolver.movegen.common.model.Field;
import com.rfmajor.scrabblesolver.movegen.common.model.Move;
import com.rfmajor.scrabblesolver.movegen.common.model.Rack;
import com.rfmajor.scrabblesolver.movegen.common.model.Direction;
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

    public Set<Move> generate(Rack rack) {
        Set<Move> moves = new HashSet<>();
        generateMoves(moves, moveAlgorithmExecutor, rack);
        generateMoves(moves, transposedMoveAlgorithmExecutor, rack);
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

    private void generateMoves(Set<Move> moves, MoveAlgorithmExecutor<A> moveAlgorithmExecutor, Rack rack) {
        CrossSetCalculator crossSetCalculator = moveAlgorithmExecutor.getCrossSetCalculator();
        for (Field anchor : crossSetCalculator.getAnchors()) {
            List<Move> result = moveAlgorithmExecutor.generate(anchor.getRow(), anchor.getColumn(), rack);
            moves.addAll(result);
        }
    }
}
