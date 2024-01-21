package com.rfmajor.scrabblesolver.gaddag;

import com.rfmajor.scrabblesolver.common.game.Alphabet;

import java.util.List;

public interface GaddagConverter<A> {
    A convert(List<String> words, Alphabet alphabet);
    char getDelimiter();
    void setDelimiter(char delimiter);
}
