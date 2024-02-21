package com.rfmajor.scrabblesolver.web.service;

import com.rfmajor.scrabblesolver.common.game.Alphabet;
import com.rfmajor.scrabblesolver.gaddag.Gaddag;
import com.rfmajor.scrabblesolver.gaddag.GaddagConverter;
import com.rfmajor.scrabblesolver.input.DictionaryReader;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LexiconRegistry<A> {
    private final Map<String, Gaddag<A>> lexicons = new HashMap<>();
    private final GaddagConverter<A> gaddagConverter;
    private final DictionaryReader dictionaryReader;
    @Value("${gaddag.delimiter}")
    private char delimiter;

    public void init() {
        String[] lines = dictionaryReader.readAlphabetLines();
        List<Character> letters = dictionaryReader.getAlphabetLetters(lines);
        letters.add(delimiter);
        List<Integer> points = dictionaryReader.getAlphabetPoints(lines);
        List<Integer> quantities = dictionaryReader.getAlphabetQuantities(lines);
        Alphabet alphabet = new Alphabet(letters, points, quantities);
        List<String> words = dictionaryReader.readAllWords(alphabet);
        Gaddag<A> gaddag = gaddagConverter.convert(words, alphabet);
        lexicons.put("pl", gaddag);
    }

    public Gaddag<A> getLexicon(String language) {
        return lexicons.get(language);
    }

    public boolean hasLexicon(String language) {
        return lexicons.containsKey(language);
    }
}
