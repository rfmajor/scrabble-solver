package com.rfmajor.scrabblesolver.web.service;

import com.rfmajor.scrabblesolver.gaddag.Gaddag;
import com.rfmajor.scrabblesolver.gaddag.MoveGenerator;
import com.rfmajor.scrabblesolver.web.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MoveGeneratorFactory<A> {
    private final LexiconRegistry<A> lexiconRegistry;
    private final BoardMapper boardMapper;

    public MoveGenerator<A> getMoveGenerator(String alphabetLanguage, BoardDto board) {
        if (!lexiconRegistry.hasLexicon(alphabetLanguage)) {
            throw new LexiconNotFoundException("Lexicon not found for language: " + alphabetLanguage);
        }
        Gaddag<A> gaddag = lexiconRegistry.getLexicon(alphabetLanguage);
        return new MoveGenerator<>(boardMapper.fromDto(board), gaddag);
    }
}
