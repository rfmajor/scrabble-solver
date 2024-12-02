package com.rfmajor.scrabblesolver.gaddag.converter;

import com.rfmajor.scrabblesolver.common.scrabble.Alphabet;
import com.rfmajor.scrabblesolver.common.scrabble.Board;
import com.rfmajor.scrabblesolver.common.gaddag.model.CompressedByteGaddag;
import com.rfmajor.scrabblesolver.common.gaddag.model.ExpandedGaddag;
import com.rfmajor.scrabblesolver.common.gaddag.model.Gaddag;
import com.rfmajor.scrabblesolver.gaddag.converter.gaddag.CompressedGaddagFileWriter;
import com.rfmajor.scrabblesolver.common.gaddag.convert.ExpandedGaddagByteArrayCompressor;
import com.rfmajor.scrabblesolver.common.gaddag.convert.ExpandedGaddagConverter;
import com.rfmajor.scrabblesolver.gaddag.converter.gaddag.FileWordIterable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        final int maxLength = Integer.parseInt(args[0]);
        Board board = new Board();
        Alphabet alphabet = new Alphabet(
                mapStringToLettersList("aąbcćdeęfghijklłmnńoóprsśtuwyzźż#"),
                Collections.emptyList(),
                Collections.emptyList()
        );

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
        CompressedGaddagFileWriter writer = new CompressedGaddagFileWriter();
        writer.write(compressedGaddag);
    }

    public static List<Character> mapStringToLettersList(String letters) {
        return letters.chars().mapToObj(c -> (char) c).toList();
    }
}
