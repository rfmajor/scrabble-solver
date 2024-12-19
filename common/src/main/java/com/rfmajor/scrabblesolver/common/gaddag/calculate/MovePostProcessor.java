package com.rfmajor.scrabblesolver.common.gaddag.calculate;

import com.rfmajor.scrabblesolver.common.scrabble.Move;
import com.rfmajor.scrabblesolver.common.scrabble.MoveGroup;
import com.rfmajor.scrabblesolver.common.scrabble.MovePossibility;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MovePostProcessor {
    public List<MoveGroup> groupMovesByNameAndPoints(Collection<Move> moves) {
        Map<MoveKey, List<MovePossibility>> movePossibilities = moves.stream().collect(
                Collectors.groupingBy(this::getMoveKey, Collectors.mapping(this::getMovePossibility, Collectors.toList()))
        );

        return movePossibilities.entrySet().stream()
                .map(entry -> new MoveGroup(entry.getKey().word(), entry.getKey().points, entry.getValue()))
                .sorted(Comparator.comparing(MoveGroup::points).reversed().thenComparing(MoveGroup::word))
                .toList();
    }

    private record MoveKey(String word, int points) {}

    private MovePossibility getMovePossibility(Move move) {
        return new MovePossibility(move.getX(), move.getY(), "", move.getDirection(), move.getNewBlankFields());
    }

    private MoveKey getMoveKey(Move move) {
        return new MoveKey(move.getWord(), move.getPoints());
    }
}
