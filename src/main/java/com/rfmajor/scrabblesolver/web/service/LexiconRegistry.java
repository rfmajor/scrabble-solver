package com.rfmajor.scrabblesolver.web.service;

import com.rfmajor.scrabblesolver.common.game.Alphabet;
import com.rfmajor.scrabblesolver.gaddag.Arc;
import com.rfmajor.scrabblesolver.gaddag.Gaddag;
import com.rfmajor.scrabblesolver.gaddag.GaddagConverter;
import com.rfmajor.scrabblesolver.input.DictionaryReader;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LexiconRegistry {
    private final Map<String, Gaddag> lexicons = new HashMap<>();
    private final GaddagConverter gaddagConverter;
    private final DictionaryReader dictionaryReader;

    @PostConstruct
    public void init() {
        String[] lines = dictionaryReader.readAlphabetLines();
        List<Character> letters = dictionaryReader.getAlphabetLetters(lines);
        List<Integer> points = dictionaryReader.getAlphabetPoints(lines);
        List<Integer> quantities = dictionaryReader.getAlphabetQuantities(lines);
        Alphabet alphabet = new Alphabet(letters, points, quantities);
        List<String> words = dictionaryReader.readAllWords(alphabet);
        Arc parentArc = gaddagConverter.convert(words, alphabet);
        Gaddag gaddag = new Gaddag(parentArc, alphabet, gaddagConverter.getDelimiter());
        lexicons.put("pol", gaddag);
    }

    public Gaddag getLexicon(String language) {
        return lexicons.get(language);
    }

    public boolean hasLexicon(String language) {
        return lexicons.containsKey(language);
    }
}
