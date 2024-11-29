package com.rfmajor.scrabblesolver.movegen.gaddag;

import com.rfmajor.scrabblesolver.movegen.common.model.Alphabet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Predicate;

@RequiredArgsConstructor
@Slf4j
public class SimpleGaddagConverter implements GaddagConverter<Arc> {
    @Override
    public Gaddag<Arc> convert(Iterable<String> wordIterable, Alphabet alphabet) {
        return convert(wordIterable, alphabet, word -> true);
    }

    @Override
    public Gaddag<Arc> convert(Iterable<String> wordIterable, Alphabet alphabet, Predicate<String> wordPredicate) {
        Arc parentArc = new Arc();
        State parentState = new State();
        parentArc.setDestinationState(parentState);

        processWords(wordIterable, parentState, alphabet, wordPredicate);
        return new SimpleGaddag(parentArc, alphabet, alphabet.getDelimiter());
    }

    private void processWords(Iterable<String> wordIterable, State state,
                              Alphabet alphabet, Predicate<String> wordPredicate) {
        for (String word : wordIterable) {
            if (!wordPredicate.test(word)) {
                continue;
            }
            log.info("Processing word: {}", word);
            addSingleWord(word, state, alphabet);
        }
    }

    private void addSingleWord(String word, State state, Alphabet alphabet) {
        State parentState = state;
        Arc arc = null;
        char[] wordChars = word.toCharArray();
        int delimiterIndex = 1;
        while (delimiterIndex <= wordChars.length) {
            state = parentState;
            // add rev(x)
            for (int i = delimiterIndex - 1; i >= 0; i--) {
                arc = addSingleLetter(wordChars[i], arc, state, isLastIteration(i, delimiterIndex, wordChars), alphabet);
                state = arc.getDestinationState();
            }
            // add delimiter if it's not the last character of the sequence
            if (delimiterIndex != wordChars.length) {
                arc = addSingleLetter(alphabet.getDelimiter(), arc, state, false, alphabet);
                state = arc.getDestinationState();
            }
            // add y
            for (int i = delimiterIndex; i < wordChars.length; i++) {
                arc = addSingleLetter(wordChars[i], arc, state, isLastIteration(i, delimiterIndex, wordChars), alphabet);
                state = arc.getDestinationState();
            }
            delimiterIndex++;
        }
    }

    private Arc addSingleLetter(char letter, Arc arc, State state, boolean isLastLetter, Alphabet alphabet) {
        if (isLastLetter) {
            arc.addLetterIndexToSet(alphabet.getIndex(letter));
            return arc;
        }
        if (!state.containsArc(letter)) {
            state.addArc(letter);
            return state.getArc(letter);
        }
        return state.getArc(letter);
    }

    private boolean isLastIteration(int index, int delimiterIndex, char[] array) {
        if (delimiterIndex == array.length) {
            return index == 0;
        }
        return index == array.length - 1;
    }
}
