package com.rfmajor.scrabblesolver.common;

import com.rfmajor.scrabblesolver.common.exceptions.AlphabetTooLargeException;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

@Component
public class CrossSetMapper {
    private final Alphabet alphabet;

    public CrossSetMapper(Alphabet alphabet) {
        if (alphabet.size() >= Long.SIZE) {
            throw new AlphabetTooLargeException("The alphabet is too large: " + alphabet.size());
        }
        this.alphabet = alphabet;
    }

    public String mapCrossSetToHexVector(Set<Character> letters) {
        return Long.toHexString(mapCrossSetToLong(letters)).toUpperCase();
    }

    public long mapCrossSetToLong(Set<Character> letters) {
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
}
