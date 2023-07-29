package com.rfmajor.scrabblesolver;

import com.rfmajor.scrabblesolver.common.Alphabet;
import com.rfmajor.scrabblesolver.gaddag.Arc;
import com.rfmajor.scrabblesolver.gaddag.GaddagConverter;
import com.rfmajor.scrabblesolver.gaddag.State;
import com.rfmajor.scrabblesolver.input.DictionaryReader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SpringBootApplication
public class ScrabbleSolverApplication {

    public static void main(String[] args) {
        var ctx = SpringApplication.run(ScrabbleSolverApplication.class, args);
        var dictionaryReader = ctx.getBean(DictionaryReader.class);
        String[] alphabetLines = dictionaryReader.readAlphabetLines();
        List<Character> alphabetLetters = dictionaryReader.getAlphabetLetters(alphabetLines);
        List<Integer> alphabetPoints = dictionaryReader.getAlphabetPoints(alphabetLines);
        List<Integer> alphabetQuantities = dictionaryReader.getAlphabetQuantities(alphabetLines);
        Alphabet alphabet = new Alphabet(alphabetLetters, alphabetPoints, alphabetQuantities);
        List<String> words = dictionaryReader.readAllWords(alphabet);
        State parentState = ctx.getBean(GaddagConverter.class).convert(words, alphabet);
        // 5073 distinct letter sets -> 13 bits as a representation of the set after compression

        System.out.println();
    }

}
