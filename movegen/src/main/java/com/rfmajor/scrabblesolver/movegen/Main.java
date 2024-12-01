package com.rfmajor.scrabblesolver.movegen;

import com.rfmajor.scrabblesolver.movegen.common.model.Alphabet;
import com.rfmajor.scrabblesolver.movegen.common.model.Board;
import com.rfmajor.scrabblesolver.movegen.gaddag.ExpandedGaddag;
import com.rfmajor.scrabblesolver.movegen.gaddag.ExpandedGaddagCompressor;
import com.rfmajor.scrabblesolver.movegen.gaddag.ExpandedGaddagConverter;
import com.rfmajor.scrabblesolver.movegen.gaddag.FileWordIterable;
import com.rfmajor.scrabblesolver.movegen.gaddag.Gaddag;

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
        ExpandedGaddagCompressor expandedGaddagCompressor = new ExpandedGaddagCompressor();

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
        Gaddag<Long> compressedGaddag = expandedGaddagCompressor.minimize((ExpandedGaddag) expandedGaddag);
    }

    public static List<Character> mapStringToLettersList(String letters) {
        return letters.chars().mapToObj(c -> (char) c).toList();
    }
}
