package com.rfmajor.scrabblesolver.web.service;

import com.rfmajor.scrabblesolver.common.CrossSetCalculator;
import com.rfmajor.scrabblesolver.common.game.Field;
import com.rfmajor.scrabblesolver.common.game.Rack;
import com.rfmajor.scrabblesolver.common.game.Move;
import com.rfmajor.scrabblesolver.gaddag.MoveGenerator;
import com.rfmajor.scrabblesolver.web.mapper.MoveMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GaddagMoveGeneratorService implements MoveGeneratorService {
    private final MoveGeneratorProvider moveGeneratorProvider;
    private final MoveMapper moveMapper;

    @Override
    public Set<MoveDto> generateMoves(GenerateMovesRequest request) {
        MoveGenerator moveGenerator = moveGeneratorProvider.getMoveGenerator(request);
        Rack rack = new Rack(request.getRackLetters());
        CrossSetCalculator crossSetCalculator = moveGenerator.getCrossSetCalculator();
        Set<MoveDto> moveDtos = new HashSet<>();
        for (Field anchor : crossSetCalculator.getAnchors()) {
            List<Move> moves = moveGenerator.generate(anchor.getRow(), anchor.getColumn(), rack);
            moves.stream()
                    .map(moveMapper::toDto)
                    .forEach(moveDtos::add);
        }
        return moveDtos;
    }
}
