package com.rfmajor.scrabblesolver.server.web.service;

import com.rfmajor.scrabblesolver.common.gaddag.calculate.MoveGenerator;
import com.rfmajor.scrabblesolver.common.scrabble.MoveGroup;
import com.rfmajor.scrabblesolver.common.scrabble.Rack;
import com.rfmajor.scrabblesolver.server.web.mapper.BoardMapper;
import com.rfmajor.scrabblesolver.server.web.mapper.BoardNotationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GaddagMoveGeneratorService implements MoveGeneratorService {
    private final BoardMapper boardMapper;
    private final BoardNotationMapper boardNotationMapper;
    private final MoveGenerator<Long> moveGenerator;

    @Override
    public List<MoveGroup> generateMoves(GenerateMovesRequest request) {
        Rack rack = new Rack(request.getRackLetters());
        return moveGenerator.generateAllPossibleMoves(rack, boardMapper.fromDto(request.getBoard()));
    }

    @Override
    public List<MoveGroup> generateMoves(String boardNotation, String rackNotation) {
        Rack rack = new Rack(rackNotation);
        return moveGenerator.generateAllPossibleMoves(rack, boardNotationMapper.fromBoardNotation(boardNotation));
    }
}
