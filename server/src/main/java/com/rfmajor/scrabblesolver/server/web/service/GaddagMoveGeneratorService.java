package com.rfmajor.scrabblesolver.server.web.service;

import com.rfmajor.scrabblesolver.common.gaddag.calculate.MoveGenerator;
import com.rfmajor.scrabblesolver.common.gaddag.calculate.MovePostProcessor;
import com.rfmajor.scrabblesolver.common.gaddag.calculate.PointCalculator;
import com.rfmajor.scrabblesolver.common.gaddag.model.Gaddag;
import com.rfmajor.scrabblesolver.common.scrabble.Field;
import com.rfmajor.scrabblesolver.common.scrabble.Move;
import com.rfmajor.scrabblesolver.common.scrabble.MoveGroup;
import com.rfmajor.scrabblesolver.common.scrabble.Rack;
import com.rfmajor.scrabblesolver.server.web.mapper.BoardMapper;
import com.rfmajor.scrabblesolver.server.web.mapper.MoveMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GaddagMoveGeneratorService implements MoveGeneratorService {
    private final BoardMapper boardMapper;
    private final Gaddag<Long> gaddag;
    private final MoveMapper moveMapper;
    private final PointCalculator pointCalculator;
    private final MovePostProcessor movePostProcessor = new MovePostProcessor();

    @Override
    public List<MoveDto> generateMoves(GenerateMovesRequest request) {
        MoveGenerator<Long> moveGenerator = new MoveGenerator<>(boardMapper.fromDto(request.getBoard()), gaddag);
        Rack rack = new Rack(request.getRackLetters());
        Set<Move> moves = moveGenerator.generate(rack, request.isComputeCrossSets());
        pointCalculator.calculatePoints(moves, moveGenerator.getBoard(), moveGenerator.getTransposedBoard(), gaddag.getAlphabet(), rack, mapIntArraysToFieldSet(request.getBlankFields()));
        List<MoveGroup> groups = movePostProcessor.groupMovesByNameAndPoints(moves);

        return moves.stream()
                .map(moveMapper::toDto)
                .sorted(Comparator.comparingInt(MoveDto::getPoints).reversed())
                .toList();
    }

    private Set<Field> mapIntArraysToFieldSet(Set<int[]> fields) {
        return fields.stream()
                .map(field -> new Field(field[0], field[1]))
                .collect(Collectors.toSet());
    }
}
