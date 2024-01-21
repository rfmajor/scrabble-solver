package com.rfmajor.scrabblesolver.web.service;

import com.rfmajor.scrabblesolver.gaddag.Gaddag;
import com.rfmajor.scrabblesolver.gaddag.MoveGeneratorFacade;
import com.rfmajor.scrabblesolver.web.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MoveGeneratorFactory<A> {
    private final LexiconRegistry<A> lexiconRegistry;
    private final BoardMapper boardMapper;

    public MoveGeneratorFacade<A> getMoveGenerator(String alphabetLanguage, BoardDto board) {
        if (!lexiconRegistry.hasLexicon(alphabetLanguage)) {
            throw new LexiconNotFoundException("Lexicon not found for language: " + alphabetLanguage);
        }
        Gaddag<A> gaddag = lexiconRegistry.getLexicon(alphabetLanguage);
        return new MoveGeneratorFacade<>(boardMapper.fromDto(board), gaddag);
    }
}
