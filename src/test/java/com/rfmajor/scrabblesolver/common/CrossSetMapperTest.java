package com.rfmajor.scrabblesolver.common;

import com.rfmajor.scrabblesolver.common.exceptions.AlphabetLetterNotPresentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CrossSetMapperTest {
    private CrossSetMapper crossSetMapper;

    @BeforeEach
    void setUp() {
        crossSetMapper = new CrossSetMapper(new Alphabet(mapStringToLettersList("aąbcćdeęfghijklłmnńoóprsśtuwyzźż")));
    }

    @Test
    void givenAllLettersWith32LettersInAlphabet_whenMapCrossSetToHexVector_thenReturn8Fs() {
        String mapped = crossSetMapper.mapCrossSetToHexVector(mapStringToLettersSet("aąbcćdeęfghijklłmnńoóprsśtuwyzźż"));
        assertEquals("FFFFFFFF", mapped);
    }

    @Test
    void givenAllLettersWith32LettersInAlphabet_whenMapCrossSetToLong_thenReturn8Fs() {
        long mapped = crossSetMapper.mapCrossSetToLong(mapStringToLettersSet("aąbcćdeęfghijklłmnńoóprsśtuwyzźż"));
        assertEquals((long) Math.pow(2, 32) - 1, mapped);
    }

    @Test
    void givenNoLettersWith32LettersInAlphabet_whenMapCrossSetToHexVector_thenReturn0() {
        String mapped = crossSetMapper.mapCrossSetToHexVector(mapStringToLettersSet(""));
        assertEquals("0", mapped);
    }

    @Test
    void givenNoLettersWith32LettersInAlphabet_whenMapCrossSetToLong_thenReturn0() {
        long mapped = crossSetMapper.mapCrossSetToLong(mapStringToLettersSet(""));
        assertEquals(0L, mapped);
    }

    @Test
    void givenABCDEFLettersWith32LettersInAlphabet_whenMapCrossSetToHexVector_thenReturn16D() {
        String mapped = crossSetMapper.mapCrossSetToHexVector(mapStringToLettersSet("abcdef"));
        assertEquals("16D", mapped);
    }

    @Test
    void givenABCDEFLettersWith32LettersInAlphabet_whenMapCrossSetToLong_thenReturn365() {
        long mapped = crossSetMapper.mapCrossSetToLong(mapStringToLettersSet("abcdef"));
        assertEquals(365L, mapped);
    }

    @Test
    void givenInvalidLetterWith32LettersInAlphabet_whenMapCrossSetToHexVector_thenThrowException() {
        assertThrows(AlphabetLetterNotPresentException.class, () -> crossSetMapper.mapCrossSetToHexVector(Set.of('v')));
    }

    @Test
    void givenInvalidLetterWith32LettersInAlphabet_whenMapCrossSetToLong_thenThrowException() {
        assertThrows(AlphabetLetterNotPresentException.class, () -> crossSetMapper.mapCrossSetToLong(Set.of('v')));
    }

    @Test
    void given8FsHexVectorWith32LettersInAlphabet_whenMapToSet_thenReturnAllLetters() {
        Set<Character> mapped = crossSetMapper.mapToSet("FFFFFFFF");
        assertTrue(mapped.containsAll(mapStringToLettersSet("aąbcćdeęfghijklłmnńoóprsśtuwyzźż")));
    }

    @Test
    void given8FsLongWith32LettersInAlphabet_whenMapToSet_thenReturnAllLetters() {
        Set<Character> mapped = crossSetMapper.mapToSet((long) Math.pow(2, 32) - 1);
        assertTrue(mapped.containsAll(mapStringToLettersSet("aąbcćdeęfghijklłmnńoóprsśtuwyzźż")));
    }

    @Test
    void given0HexVectorWith32LettersInAlphabet_whenMapToSet_thenReturnEmptySet() {
        Set<Character> mapped = crossSetMapper.mapToSet("0");
        assertTrue(mapped.isEmpty());
    }

    @Test
    void given0LongWith32LettersInAlphabet_whenMapToSet_thenReturnEmptySet() {
        Set<Character> mapped = crossSetMapper.mapToSet(0L);
        assertTrue(mapped.isEmpty());
    }

    @Test
    void given16DHexVectorWith32LettersInAlphabet_whenMapToSet_thenReturnABCDEF() {
        Set<Character> mapped = crossSetMapper.mapToSet("16D");
        assertTrue(mapped.containsAll(mapStringToLettersSet("abcdef")));
    }

    @Test
    void given16DLongWith32LettersInAlphabet_whenMapToSet_thenReturnABCDEF() {
        Set<Character> mapped = crossSetMapper.mapToSet(365L);
        assertTrue(mapped.containsAll(mapStringToLettersSet("abcdef")));
    }

    private static List<Character> mapStringToLettersList(String letters) {
        return letters.chars().mapToObj(c -> (char) c).toList();
    }

    private static Set<Character> mapStringToLettersSet(String letters) {
        return letters.chars().mapToObj(c -> (char) c).collect(Collectors.toSet());
    }
}