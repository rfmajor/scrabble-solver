package com.rfmajor.scrabblesolver.gaddag.converter.input;

public class AlphabetRecords {
    public record Letter(char letter, int index, int points, int quantity) {}
    public record Delimiter(char letter, int index) {}
    public record Alphabet(Letter[] letters, Delimiter delimiter) {}
}
