package com.rfmajor.scrabblesolver.movegen.gaddag;

import com.rfmajor.scrabblesolver.movegen.common.model.Alphabet;

import java.util.function.Predicate;

public interface GaddagConverter<A> {
    Gaddag<A> convert(Iterable<String> wordIterable, Alphabet alphabet);

    Gaddag<A> convert(Iterable<String> wordIterable, Alphabet alphabet, Predicate<String> wordPredicate);
}
