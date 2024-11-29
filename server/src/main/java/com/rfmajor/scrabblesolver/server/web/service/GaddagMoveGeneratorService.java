package com.rfmajor.scrabblesolver.server.web.service;

import com.rfmajor.scrabblesolver.movegen.common.PointCalculator;
import com.rfmajor.scrabblesolver.movegen.common.model.Field;
import com.rfmajor.scrabblesolver.movegen.common.model.Move;
import com.rfmajor.scrabblesolver.movegen.common.model.Rack;
import com.rfmajor.scrabblesolver.movegen.gaddag.MoveGenerator;
import com.rfmajor.scrabblesolver.server.web.mapper.MoveMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GaddagMoveGeneratorService implements MoveGeneratorService {
    private final MoveGeneratorFactory moveGeneratorFactory;
    private final MoveMapper moveMapper;
    private final PointCalculator pointCalculator;

    @Override
    public List<MoveDto> generateMoves(GenerateMovesRequest request) {
        MoveGenerator<?> moveGenerator = moveGeneratorFactory.getMoveGenerator(
                request.getAlphabetLanguage(), request.getBoard());
        Rack rack = new Rack(request.getRackLetters());
        Set<Move> moves = moveGenerator.generate(rack);
        pointCalculator.calculatePoints(moves, moveGenerator, rack, mapIntArraysToFieldSet(request.getBlankFields()));

        return moves.stream()
                .map(moveMapper::toDto)
                .sorted(Comparator.comparingInt(MoveDto::getPoints).reversed())
                .toList();
    }

    private Set<Field> mapIntArraysToFieldSet(Set<int[]> fields) {
        Set<Field> mappedFields = new HashSet<>();
        for (int[] field : fields) {
            mappedFields.add(new Field(field[0], field[1]));
        }
        return mappedFields;
    }
}
