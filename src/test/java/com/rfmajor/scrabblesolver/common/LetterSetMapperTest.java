package com.rfmajor.scrabblesolver.common;

import com.rfmajor.scrabblesolver.common.exceptions.AlphabetLetterNotPresentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class LetterSetMapperTest {
    private LetterSetMapper letterSetMapper;

    @BeforeEach
    void setUp() {
        letterSetMapper = new LetterSetMapper(new Alphabet(mapStringToLettersList("aąbcćdeęfghijklłmnńoóprsśtuwyzźż")));
    }

    @Test
    void givenAllLettersWith32LettersInAlphabet_whenMapLetterSetToHexVector_thenReturn8Fs() {
        String mapped = letterSetMapper.mapLetterSetToHexVector(mapStringToLettersSet("aąbcćdeęfghijklłmnńoóprsśtuwyzźż"));
        assertEquals("FFFFFFFF", mapped);
    }

    @Test
    void givenAllLettersWith32LettersInAlphabet_whenMapLetterSetToLong_thenReturn8Fs() {
        long mapped = letterSetMapper.mapLetterSetToLong(mapStringToLettersSet("aąbcćdeęfghijklłmnńoóprsśtuwyzźż"));
        assertEquals((long) Math.pow(2, 32) - 1, mapped);
    }

    @Test
    void givenNoLettersWith32LettersInAlphabet_whenMapLetterSetToHexVector_thenReturn0() {
        String mapped = letterSetMapper.mapLetterSetToHexVector(mapStringToLettersSet(""));
        assertEquals("0", mapped);
    }

    @Test
    void givenNoLettersWith32LettersInAlphabet_whenMapLetterSetToLong_thenReturn0() {
        long mapped = letterSetMapper.mapLetterSetToLong(mapStringToLettersSet(""));
        assertEquals(0L, mapped);
    }

    @Test
    void givenABCDEFLettersWith32LettersInAlphabet_whenMapLetterSetToHexVector_thenReturn16D() {
        String mapped = letterSetMapper.mapLetterSetToHexVector(mapStringToLettersSet("abcdef"));
        assertEquals("16D", mapped);
    }

    @Test
    void givenABCDEFLettersWith32LettersInAlphabet_whenMapLetterSetToLong_thenReturn365() {
        long mapped = letterSetMapper.mapLetterSetToLong(mapStringToLettersSet("abcdef"));
        assertEquals(365L, mapped);
    }

    @Test
    void givenInvalidLetterWith32LettersInAlphabet_whenMapLetterSetToHexVector_thenThrowException() {
        assertThrows(AlphabetLetterNotPresentException.class, () -> letterSetMapper.mapLetterSetToHexVector(Set.of('v')));
    }

    @Test
    void givenInvalidLetterWith32LettersInAlphabet_whenMapLetterSetToLong_thenThrowException() {
        assertThrows(AlphabetLetterNotPresentException.class, () -> letterSetMapper.mapLetterSetToLong(Set.of('v')));
    }

    @Test
    void given8FsHexVectorWith32LettersInAlphabet_whenMapToSet_thenReturnAllLetters() {
        Set<Character> mapped = letterSetMapper.mapToSet("FFFFFFFF");
        assertTrue(mapped.containsAll(mapStringToLettersSet("aąbcćdeęfghijklłmnńoóprsśtuwyzźż")));
    }

    @Test
    void given8FsLongWith32LettersInAlphabet_whenMapToSet_thenReturnAllLetters() {
        Set<Character> mapped = letterSetMapper.mapToSet((long) Math.pow(2, 32) - 1);
        assertTrue(mapped.containsAll(mapStringToLettersSet("aąbcćdeęfghijklłmnńoóprsśtuwyzźż")));
    }

    @Test
    void given0HexVectorWith32LettersInAlphabet_whenMapToSet_thenReturnEmptySet() {
        Set<Character> mapped = letterSetMapper.mapToSet("0");
        assertTrue(mapped.isEmpty());
    }

    @Test
    void given0LongWith32LettersInAlphabet_whenMapToSet_thenReturnEmptySet() {
        Set<Character> mapped = letterSetMapper.mapToSet(0L);
        assertTrue(mapped.isEmpty());
    }

    @Test
    void given16DHexVectorWith32LettersInAlphabet_whenMapToSet_thenReturnABCDEF() {
        Set<Character> mapped = letterSetMapper.mapToSet("16D");
        assertTrue(mapped.containsAll(mapStringToLettersSet("abcdef")));
    }

    @Test
    void given16DLongWith32LettersInAlphabet_whenMapToSet_thenReturnABCDEF() {
        Set<Character> mapped = letterSetMapper.mapToSet(365L);
        assertTrue(mapped.containsAll(mapStringToLettersSet("abcdef")));
    }

    private static List<Character> mapStringToLettersList(String letters) {
        return letters.chars().mapToObj(c -> (char) c).toList();
    }

    private static Set<Character> mapStringToLettersSet(String letters) {
        return letters.chars().mapToObj(c -> (char) c).collect(Collectors.toSet());
    }
}