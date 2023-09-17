package com.rfmajor.scrabblesolver.common.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Bag {
    private final Map<Character, Integer> lettersToQuantities;

    public Bag(Alphabet alphabet) {
        this.lettersToQuantities = alphabet.getLettersToQuantities();
    }

    public List<Character> drawLetters(int letters) {
        // TODO: 17.09.2023 implement this method properly
        List<Character> result = new ArrayList<>();
        for (int i = 0; i < letters; i++) {
        }
        return null;
    }
}
