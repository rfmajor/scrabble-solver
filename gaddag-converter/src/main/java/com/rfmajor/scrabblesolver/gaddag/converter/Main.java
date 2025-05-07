package com.rfmajor.scrabblesolver.gaddag.converter;

import com.rfmajor.scrabblesolver.common.gaddag.convert.ExpandedGaddagByteArrayCompressor;
import com.rfmajor.scrabblesolver.common.gaddag.convert.ExpandedGaddagConverter;
import com.rfmajor.scrabblesolver.common.gaddag.export.FileWordIterable;
import com.rfmajor.scrabblesolver.common.gaddag.export.GaddagFileExporter;
import com.rfmajor.scrabblesolver.common.gaddag.export.GaddagFileReader;
import com.rfmajor.scrabblesolver.common.gaddag.model.CompressedByteGaddag;
import com.rfmajor.scrabblesolver.common.gaddag.model.ExpandedGaddag;
import com.rfmajor.scrabblesolver.common.gaddag.model.Gaddag;
import com.rfmajor.scrabblesolver.common.scrabble.Alphabet;
import com.rfmajor.scrabblesolver.common.scrabble.Board;
import com.rfmajor.scrabblesolver.gaddag.converter.input.AlphabetReader;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
public class Main {
    private static final int ARGS_ALPHABET_FILE = 0;
    private static final int ARGS_DICTIONARY_FILE = 1;
    private static final int ARGS_GADDAG_FILE = 2;
    private static final int ARGS_MAX_WORD_LENGTH = 3;

    private static final int MIN_ARGS_LENGTH = 3;
    private static final int DEFAULT_MAX_WORD_LENGTH = 15;

    public static void main(String[] args) throws IOException {
        Args parsedArgs = readArgs(args);
        executeCompression(parsedArgs.alphabetFile, parsedArgs.dictionaryFile,
                parsedArgs.gaddagFile, parsedArgs.maxWordLength);
    }

    private static Args readArgs(String[] args) {
        if (args.length < MIN_ARGS_LENGTH) {
            throw new IllegalArgumentException("At least " + MIN_ARGS_LENGTH + " arguments required but got " + args.length);
        }

        int maxWordLength = DEFAULT_MAX_WORD_LENGTH;
        if (args.length >= ARGS_MAX_WORD_LENGTH + 1) {
            try {
                maxWordLength = Integer.parseInt(args[ARGS_MAX_WORD_LENGTH]);
            } catch (NumberFormatException e) {
                log.warn("Invalid max word length: {}. Defaulting to {}", args[ARGS_MAX_WORD_LENGTH], DEFAULT_MAX_WORD_LENGTH);
            }
        }

        return new Args(args[ARGS_ALPHABET_FILE], args[ARGS_DICTIONARY_FILE], args[ARGS_GADDAG_FILE], maxWordLength);
    }

    private static void executeCompression(String alphabetFile, String dictionaryFile,
                                           String gaddagFile, int maxWordLength) throws IOException {
        Alphabet alphabet = new AlphabetReader().readFromFile(alphabetFile);

        ExpandedGaddagConverter expandedGaddagConverter = new ExpandedGaddagConverter();
        Gaddag<Long> expandedGaddag;
        ExpandedGaddagByteArrayCompressor expandedGaddagByteArrayCompressor = new ExpandedGaddagByteArrayCompressor();

        try (FileWordIterable fileWordIterable = new FileWordIterable(new FileInputStream(dictionaryFile))) {
            expandedGaddag = expandedGaddagConverter.convert(fileWordIterable, alphabet, word -> {
                boolean correctLength = word.length() <= maxWordLength;
                boolean validChars = true;
                for (int i = 0; i < word.length(); i++) {
                    if (!alphabet.containsLetter(word.charAt(i))) {
                        validChars = false;
                        break;
                    }
                }
                return correctLength && validChars;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        CompressedByteGaddag compressedGaddag = expandedGaddagByteArrayCompressor.minimize((ExpandedGaddag) expandedGaddag);
        GaddagFileExporter writer = new GaddagFileExporter();
        writer.export(compressedGaddag, gaddagFile);
    }

    private record Args(
            String alphabetFile,
            String dictionaryFile,
            String gaddagFile,
            int maxWordLength
    ) {}
}
