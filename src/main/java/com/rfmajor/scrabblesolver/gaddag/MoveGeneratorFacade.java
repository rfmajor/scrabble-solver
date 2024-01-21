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
public class MoveGeneratorFacade<A> {
    private final MoveGenerator<A> moveGenerator;
    private final MoveGenerator<A> transposedMoveGenerator;

    public MoveGeneratorFacade(Board board, Gaddag<A> gaddag) {
        this.moveGenerator = new MoveGenerator<>(board, gaddag, Direction.ACROSS);
        this.transposedMoveGenerator = new MoveGenerator<>(board.transpose(), gaddag, Direction.DOWN);
    }

    public Set<Move> generate(Rack rack) {
        Set<Move> moves = new HashSet<>();
        generateMoves(moves, moveGenerator, rack);
        generateMoves(moves, transposedMoveGenerator, rack);
        return moves;
    }

    public Board getBoard() {
        return moveGenerator.getBoard();
    }

    public Board getTransposedBoard() {
        return transposedMoveGenerator.getBoard();
    }

    public Alphabet getAlphabet() {
        return moveGenerator.getAlphabet();
    }

    private void generateMoves(Set<Move> moves, MoveGenerator<A> moveGenerator, Rack rack) {
        CrossSetCalculator crossSetCalculator = moveGenerator.getCrossSetCalculator();
        for (Field anchor : crossSetCalculator.getAnchors()) {
            List<Move> result = moveGenerator.generate(anchor.getRow(), anchor.getColumn(), rack);
            moves.addAll(result);
        }
    }
}
