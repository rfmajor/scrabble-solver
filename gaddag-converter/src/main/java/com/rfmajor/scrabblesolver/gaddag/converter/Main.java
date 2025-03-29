package com.rfmajor.scrabblesolver.gaddag.converter;

import com.rfmajor.scrabblesolver.common.gaddag.convert.ExpandedGaddagByteArrayCompressor;
import com.rfmajor.scrabblesolver.common.gaddag.convert.ExpandedGaddagConverter;
import com.rfmajor.scrabblesolver.common.gaddag.export.FileWordIterable;
import com.rfmajor.scrabblesolver.common.gaddag.export.GaddagFileExporter;
import com.rfmajor.scrabblesolver.common.gaddag.model.CompressedByteGaddag;
import com.rfmajor.scrabblesolver.common.gaddag.model.ExpandedGaddag;
import com.rfmajor.scrabblesolver.common.gaddag.model.Gaddag;
import com.rfmajor.scrabblesolver.common.scrabble.Alphabet;
import com.rfmajor.scrabblesolver.gaddag.converter.input.AlphabetReader;

import java.io.IOException;

public class Main {
    private static final int MAX_WORD_LENGTH = 15;

    public static void main(String[] args) throws IOException {
        executeCompression(args);
    }

    private static void executeCompression(String[] args) throws IOException {
        Alphabet alphabet = new AlphabetReader().readFromFile("output/alphabet.json");
        System.out.println(alphabet.toString());

        ExpandedGaddagConverter expandedGaddagConverter = new ExpandedGaddagConverter();
        Gaddag<Long> expandedGaddag;
        ExpandedGaddagByteArrayCompressor expandedGaddagByteArrayCompressor = new ExpandedGaddagByteArrayCompressor();

        try (FileWordIterable fileWordIterable = new FileWordIterable(Main.class.getResourceAsStream("/slowa.txt"))) {
            expandedGaddag = expandedGaddagConverter.convert(fileWordIterable, alphabet, word -> {
                boolean correctLength = word.length() <= MAX_WORD_LENGTH;
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
        writer.export(compressedGaddag, "output");
    }
}
