package com.rfmajor.scrabblesolver.server.web.service;

import com.rfmajor.scrabblesolver.common.gaddag.calculate.MoveGenerator;
import com.rfmajor.scrabblesolver.common.scrabble.MoveGroup;
import com.rfmajor.scrabblesolver.common.scrabble.Rack;
import com.rfmajor.scrabblesolver.server.web.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GaddagMoveGeneratorService implements MoveGeneratorService {
    private final BoardMapper boardMapper;
    private final MoveGenerator<Long> moveGenerator;

    @Override
    public List<MoveGroup> generateMoves(GenerateMovesRequest request) {
        Rack rack = new Rack(request.getRackLetters());
        return moveGenerator.generate(rack, boardMapper.fromDto(request.getBoard()));
    }
}
