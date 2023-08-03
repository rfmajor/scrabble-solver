package com.rfmajor.scrabblesolver.web.service;

import com.rfmajor.scrabblesolver.common.game.Rack;
import com.rfmajor.scrabblesolver.gaddag.MoveGeneratorFacade;
import com.rfmajor.scrabblesolver.web.mapper.MoveMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GaddagMoveGeneratorService implements MoveGeneratorService {
    private final MoveGeneratorProvider moveGeneratorProvider;
    private final MoveMapper moveMapper;

    @Override
    public Set<MoveDto> generateMoves(GenerateMovesRequest request) {
        MoveGeneratorFacade moveGenerator = moveGeneratorProvider.getMoveGenerator(request);
        Rack rack = new Rack(request.getRackLetters());
        return moveGenerator.generate(rack).stream()
                .map(moveMapper::toDto)
                .collect(Collectors.toSet());
    }
}
