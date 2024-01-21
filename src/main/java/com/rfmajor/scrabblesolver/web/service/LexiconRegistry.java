package com.rfmajor.scrabblesolver.web.service;

import com.rfmajor.scrabblesolver.common.game.Alphabet;
import com.rfmajor.scrabblesolver.gaddag.Arc;
import com.rfmajor.scrabblesolver.gaddag.ExpandedGaddag;
import com.rfmajor.scrabblesolver.gaddag.Gaddag;
import com.rfmajor.scrabblesolver.gaddag.GaddagConverter;
import com.rfmajor.scrabblesolver.gaddag.GaddagType;
import com.rfmajor.scrabblesolver.gaddag.SimpleGaddag;
import com.rfmajor.scrabblesolver.input.DictionaryReader;
import lombok.RequiredArgsConstructor;
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

    public void init(GaddagType gaddagType) {
        String[] lines = dictionaryReader.readAlphabetLines();
        List<Character> letters = dictionaryReader.getAlphabetLetters(lines);
        List<Integer> points = dictionaryReader.getAlphabetPoints(lines);
        List<Integer> quantities = dictionaryReader.getAlphabetQuantities(lines);
        Alphabet alphabet = new Alphabet(letters, points, quantities);
        List<String> words = dictionaryReader.readAllWords(alphabet);
        A parentArc = gaddagConverter.convert(words, alphabet);
        Gaddag<A> gaddag = buildGaddag(parentArc, alphabet, gaddagConverter.getDelimiter(), gaddagType);
        lexicons.put("pl", gaddag);
    }

    @SuppressWarnings("unchecked")
    private Gaddag<A> buildGaddag(A parentArc, Alphabet alphabet, char delimiter, GaddagType gaddagType) {
        if (gaddagType == GaddagType.SIMPLE) {
            return (Gaddag<A>) (new SimpleGaddag((Arc) parentArc, alphabet, delimiter));
        } else if (gaddagType == GaddagType.EXPANDED) {
            return (Gaddag<A>) (new ExpandedGaddag((Long) parentArc, alphabet, delimiter));
        } else {
            throw new IllegalArgumentException("No gaddag implementation for type: " + gaddagType);
        }
    }

    public Gaddag<A> getLexicon(String language) {
        return lexicons.get(language);
    }

    public boolean hasLexicon(String language) {
        return lexicons.containsKey(language);
    }
}
