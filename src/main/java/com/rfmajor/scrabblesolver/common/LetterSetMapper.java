package com.rfmajor.scrabblesolver.common;

import com.rfmajor.scrabblesolver.common.exceptions.AlphabetTooLargeException;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

@Component
public class LetterSetMapper {
    private final Alphabet alphabet;

    public LetterSetMapper(Alphabet alphabet) {
        if (alphabet.size() >= Long.SIZE) {
            throw new AlphabetTooLargeException("The alphabet is too large: " + alphabet.size());
        }
        this.alphabet = alphabet;
    }

    public String mapLetterSetToHexVector(Set<Character> letters) {
        return Long.toHexString(mapLetterSetToLong(letters)).toUpperCase();
    }

    public long mapLetterSetToLong(Set<Character> letters) {
        long sum = 0;
        for (char letter : letters) {
            sum += Math.pow(2, alphabet.getIndex(letter));
        }
        return sum;
    }

    public Set<Character> mapToSet(String hexVector) {
        long decoded = new BigInteger(hexVector, 16).longValue();
        return mapToSet(decoded);
    }

    public Set<Character> mapToSet(long hexVector) {
        Set<Character> letterSet = new HashSet<>();
        for (int i = 0; i < alphabet.size(); i++) {
            if (((hexVector >> i) & 1) == 1L) {
                letterSet.add(alphabet.getLetter(i));
            }
        }
        return letterSet;
    }

    public int addLetterToVector(int vector, char letter) {
        int index = alphabet.getIndex(letter);
        return vector | (1 << index);
    }

    public int removeLetterFromVector(int vector, char letter) {
        int index = alphabet.getIndex(letter);
        return vector & ~(1 << index);
    }

    public boolean containsLetter(int vector, char letter) {
        int index = alphabet.getIndex(letter);
        return ((vector >> index) & 1) == 1;
    }
}
