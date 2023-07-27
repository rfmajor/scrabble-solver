package com.rfmajor.scrabblesolver.service;

import com.rfmajor.scrabblesolver.gaddag.MoveGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GaddagMoveGeneratorService implements MoveGeneratorService {
    private final MoveGenerator moveGenerator;

    @Override
    public List<MoveDto> generateMoves(GenerateMovesRequest request) {
        return List.of(
                MoveDto.builder()
                        .direction(Direction.DOWN)
                        .beginningField(new int[1][1])
                        .points(5)
                        .word("something")
                        .build()
        );
    }
}
