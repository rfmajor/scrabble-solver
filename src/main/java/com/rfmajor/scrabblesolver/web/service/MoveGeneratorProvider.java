package com.rfmajor.scrabblesolver.web.service;

import com.rfmajor.scrabblesolver.common.game.Alphabet;
import com.rfmajor.scrabblesolver.common.game.Board;
import com.rfmajor.scrabblesolver.gaddag.Gaddag;
import com.rfmajor.scrabblesolver.gaddag.MoveGeneratorFacade;
import com.rfmajor.scrabblesolver.web.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MoveGeneratorProvider {
    private final LexiconRegistry lexiconRegistry;
    private final BoardMapper boardMapper;

    public MoveGeneratorFacade getMoveGenerator(GenerateMovesRequest request) {
        if (!lexiconRegistry.hasLexicon(request.getAlphabetLanguage())) {
            return null;
        }
        Board board = getBoard(request);
        Gaddag gaddag = getGaddag(request);
        Alphabet alphabet = gaddag.getAlphabet();
        return new MoveGeneratorFacade(board, alphabet, gaddag);
    }

    private Board getBoard(GenerateMovesRequest request) {
        return boardMapper.fromDto(request.getBoard());
    }

    private Gaddag getGaddag(GenerateMovesRequest request) {
        return lexiconRegistry.getLexicon(request.getAlphabetLanguage());
    }
}
