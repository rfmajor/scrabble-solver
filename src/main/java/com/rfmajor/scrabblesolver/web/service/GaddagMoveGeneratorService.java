package com.rfmajor.scrabblesolver.web.service;

import com.rfmajor.scrabblesolver.common.PointCalculator;
import com.rfmajor.scrabblesolver.common.game.Move;
import com.rfmajor.scrabblesolver.common.game.Rack;
import com.rfmajor.scrabblesolver.gaddag.MoveGeneratorFacade;
import com.rfmajor.scrabblesolver.web.mapper.MoveMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GaddagMoveGeneratorService implements MoveGeneratorService {
    private final MoveGeneratorProvider moveGeneratorProvider;
    private final MoveMapper moveMapper;
    private final PointCalculator pointCalculator;

    @Override
    public List<MoveDto> generateMoves(GenerateMovesRequest request) {
        MoveGeneratorFacade moveGenerator = moveGeneratorProvider.getMoveGenerator(request);
        Rack rack = new Rack(request.getRackLetters());
        Set<Move> moves = moveGenerator.generate(rack);
        pointCalculator.calculatePoints(moves, moveGenerator, rack);

        return moves.stream()
                .map(moveMapper::toDto)
                .sorted(Comparator.comparingInt(MoveDto::getPoints).reversed())
                .toList();
    }
}
