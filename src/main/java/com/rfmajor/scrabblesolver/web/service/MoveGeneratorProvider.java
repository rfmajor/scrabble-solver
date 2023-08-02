package com.rfmajor.scrabblesolver.web.service;

import com.rfmajor.scrabblesolver.common.Alphabet;
import com.rfmajor.scrabblesolver.common.Board;
import com.rfmajor.scrabblesolver.common.CrossSetCalculator;
import com.rfmajor.scrabblesolver.gaddag.Gaddag;
import com.rfmajor.scrabblesolver.gaddag.MoveGenerator;
import com.rfmajor.scrabblesolver.web.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class MoveGeneratorProvider {
    private final Map<String, Gaddag> lexicons;
    private final BoardMapper boardMapper;

    public MoveGenerator getMoveGenerator(GenerateMovesRequest request) {
        if (!lexicons.containsKey(request.getAlphabetLanguage())) {
            return null;
        }
        Board board = getBoard(request);
        Gaddag gaddag = getGaddag(request);
        Alphabet alphabet = gaddag.getAlphabet();
        CrossSetCalculator crossSetCalculator = getCrossSetCalculator(board, gaddag);
        return new MoveGenerator(board, crossSetCalculator, alphabet, gaddag);
    }

    private Board getBoard(GenerateMovesRequest request) {
        return boardMapper.fromDto(request.getBoard());
    }

    private Gaddag getGaddag(GenerateMovesRequest request) {
        return lexicons.get(request.getAlphabetLanguage());
    }

    private CrossSetCalculator getCrossSetCalculator(Board board, Gaddag gaddag) {
        return new CrossSetCalculator(board, gaddag);
    }
}
