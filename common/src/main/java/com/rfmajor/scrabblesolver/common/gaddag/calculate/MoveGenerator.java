package com.rfmajor.scrabblesolver.common.gaddag.calculate;

import com.rfmajor.scrabblesolver.common.scrabble.Board;
import com.rfmajor.scrabblesolver.common.scrabble.Direction;
import com.rfmajor.scrabblesolver.common.scrabble.Field;
import com.rfmajor.scrabblesolver.common.scrabble.Move;
import com.rfmajor.scrabblesolver.common.scrabble.MoveGroup;
import com.rfmajor.scrabblesolver.common.scrabble.Rack;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class MoveGenerator<A> {
    private final MoveAlgorithmExecutor<A> moveAlgorithmExecutor;
    private final CrossSetCalculator<A> crossSetCalculator;
    private final MovePostProcessor movePostProcessor;


    public List<MoveGroup> generate(Rack rack, Board board) {
        Set<Move> moves = new HashSet<>();
        generateMoves(moves, rack, board, Direction.ACROSS);
        generateMoves(moves, rack, board, Direction.DOWN);

        return movePostProcessor.groupMovesByNameAndPoints(moves);
    }

    private void generateMoves(Set<Move> moves, Rack rack, Board board, Direction moveDirection) {
        board = moveDirection == Direction.ACROSS ? board : board.transpose();
        FieldSet fieldSet = crossSetCalculator.computeAllCrossSetsAndAnchors(board);

        for (Field anchor : fieldSet.anchors()) {
            List<Move> result = moveAlgorithmExecutor.generate(
                    anchor.row(), anchor.column(), rack, board, fieldSet, moveDirection);
            moves.addAll(result);
        }
    }
}
