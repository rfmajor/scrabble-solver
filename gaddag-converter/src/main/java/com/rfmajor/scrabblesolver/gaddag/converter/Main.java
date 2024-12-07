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

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
//        executeRead(args);
        executeCompression(args);
    }

    public static List<Character> mapStringToLettersList(String letters) {
        return letters.chars().mapToObj(c -> (char) c).toList();
    }

    private static void executeRead(String[] args) {
        GaddagFileReader reader = new GaddagFileReader();
        CompressedByteGaddag gaddag = reader.read("output");
    }

    private static void executeCompression(String[] args) throws IOException {
        final int maxLength = Integer.parseInt(args[0]);
        Board board = new Board();
        Alphabet alphabet = new AlphabetReader().readFromFile("output/alphabet.json");
        System.out.println(alphabet.toString());

        ExpandedGaddagConverter expandedGaddagConverter = new ExpandedGaddagConverter();
        Gaddag<Long> expandedGaddag;
        ExpandedGaddagByteArrayCompressor expandedGaddagByteArrayCompressor = new ExpandedGaddagByteArrayCompressor();

        try (FileWordIterable fileWordIterable = new FileWordIterable(Main.class.getResourceAsStream("/slowa.txt"))) {
            expandedGaddag = expandedGaddagConverter.convert(fileWordIterable, alphabet, word -> {
                boolean correctLength = word.length() < maxLength;
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
