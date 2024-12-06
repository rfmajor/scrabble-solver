package com.rfmajor.scrabblesolver.common.gaddag.convert;

import com.rfmajor.scrabblesolver.common.scrabble.Alphabet;
import com.rfmajor.scrabblesolver.common.gaddag.model.Gaddag;

import java.util.function.Predicate;

public interface GaddagConverter<A> {
    Gaddag<A> convert(Iterable<String> wordIterable, Alphabet alphabet);

    Gaddag<A> convert(Iterable<String> wordIterable, Alphabet alphabet, Predicate<String> wordPredicate);
}
