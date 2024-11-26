package com.rfmajor.scrabblesolver.web.service;

import com.rfmajor.scrabblesolver.gaddag.Gaddag;
import com.rfmajor.scrabblesolver.gaddag.MoveGenerator;
import com.rfmajor.scrabblesolver.web.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MoveGeneratorFactory {
    private final LexiconRegistry lexiconRegistry;
    private final BoardMapper boardMapper;

    public MoveGenerator<Long> getMoveGenerator(String alphabetLanguage, BoardDto board) {
        if (!lexiconRegistry.hasLexicon(alphabetLanguage)) {
            throw new LexiconNotFoundException("Lexicon not found for language: " + alphabetLanguage);
        }
        Gaddag<Long> gaddag = lexiconRegistry.getLexicon(alphabetLanguage);
        return new MoveGenerator<>(boardMapper.fromDto(board), gaddag);
    }
}
