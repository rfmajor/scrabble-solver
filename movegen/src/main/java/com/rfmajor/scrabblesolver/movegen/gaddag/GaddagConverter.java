package com.rfmajor.scrabblesolver.movegen.gaddag;

import com.rfmajor.scrabblesolver.movegen.common.model.Alphabet;

import java.util.List;

public interface GaddagConverter<A> {
    Gaddag<A> convert(List<String> words, Alphabet alphabet);
}
