package com.rfmajor.scrabblesolver.gaddag.converter.input;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rfmajor.scrabblesolver.common.scrabble.Alphabet;

import java.io.File;
import java.io.IOException;

public class AlphabetReader {
    public Alphabet readFromFile(String filename) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        AlphabetRecords.Alphabet alphabet =
                objectMapper.readValue(new File(filename), new TypeReference<AlphabetRecords.Alphabet>(){});

        int length = alphabet.letters().length + 1;
        char[] indicesToLetters = new char[length];
        int[] indicesToPoints = new int[length];
        int[] indicesToQuantities = new int[length];

        for (AlphabetRecords.Letter letter : alphabet.letters()) {
            indicesToLetters[letter.index()] = letter.letter();
            indicesToPoints[letter.index()] = letter.points();
            indicesToQuantities[letter.index()] = letter.quantity();
        }
        indicesToLetters[alphabet.delimiter().index()] = alphabet.delimiter().letter();

        return new Alphabet(indicesToLetters, indicesToPoints, indicesToQuantities);
    }
}
