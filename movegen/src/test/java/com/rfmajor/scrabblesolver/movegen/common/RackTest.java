package com.rfmajor.scrabblesolver.movegen.common;

import com.rfmajor.scrabblesolver.movegen.common.model.Rack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RackTest {
    private Rack rack;

    @BeforeEach
    void setUp() {
        rack = new Rack();
    }

    @Test
    void givenSingleLetter_whenAddLetter_thenLetterPresent() {
        rack.addLetter('c');
        assertTrue(rack.getLetters().contains('c'));
    }

    @Test
    void givenDuplicateLetter_whenAddLetter_then2LettersPresent() {
        rack.addLetter('c');
        rack.addLetter('c');
        assertTrue(rack.getLetters().contains('c'));
        assertEquals(2, rack.getSize());
    }

    @Test
    void givenDuplicateLetterAndAnotherLetter_whenAddLetter_then3LettersPresent() {
        rack.addLetter('c');
        rack.addLetter('c');
        rack.addLetter('b');
        assertTrue(rack.getLetters().contains('c'));
        assertTrue(rack.getLetters().contains('b'));
        assertEquals(3, rack.getSize());
    }

    @Test
    void givenNoLetters_whenIsEmpty_thenReturnTrue() {
        assertTrue(rack.isEmpty());
    }

    @Test
    void givenAddedAndRemovedLetter_whenIsEmpty_thenReturnTrue() {
        rack.addLetter('c');
        rack.removeLetter('c');
        assertTrue(rack.isEmpty());
    }

    @Test
    void givenAddedLetter_whenIsEmpty_thenReturnFalse() {
        rack.addLetter('c');
        assertFalse(rack.isEmpty());
    }

    @Test
    void givenOneLetter_whenRemoveLetter_thenNoValuePresent() {
        rack.addLetter('c');
        rack.removeLetter('c');
        assertFalse(rack.getLetters().contains('c'));
    }

    @Test
    void givenTwoLetters_whenRemoveLetter_thenValuePresent() {
        rack.addLetter('c');
        rack.addLetter('c');
        rack.removeLetter('c');
        assertTrue(rack.getLetters().contains('c'));
    }

    @Test
    void givenTwoLetters_whenContains_thenReturnTrue() {
        rack.addLetter('c');
        rack.addLetter('a');
        assertTrue(rack.contains('c'));
        assertTrue(rack.contains('a'));
    }

    @Test
    void givenTwoLetters_whenContainsOnNonExistentLetter_thenReturnFalse() {
        rack.addLetter('c');
        rack.addLetter('a');
        assertFalse(rack.contains('b'));
    }

    @Test
    void givenThreeLetters_whenGetLetters_thenValuesPresent() {
        rack.addLetter('a');
        rack.addLetter('b');
        rack.addLetter('c');
        assertTrue(rack.getLetters().containsAll(Set.of('a', 'b', 'c')));
    }

    @Test
    void givenThreeLetters_whenGetLettersWithNonExistent_thenReturnFalse() {
        rack.addLetter('a');
        rack.addLetter('b');
        rack.addLetter('c');
        assertFalse(rack.getLetters().containsAll(Set.of('a', 'b', 'c', 'd')));
    }

    @Test
    void givenThreeLettersWithDuplicates_whenGetLettersSize_thenReturn3() {
        rack.addLetter('a');
        rack.addLetter('b');
        rack.addLetter('b');
        assertTrue(rack.getLetters().containsAll(Set.of('a', 'b')));
        assertEquals(3, rack.getLetters().size());
    }

    @Test
    void givenThreeLetters_whenGetSize_thenReturn3() {
        rack.addLetter('a');
        rack.addLetter('a');
        rack.addLetter('c');
        assertEquals(3, rack.getSize());
    }

    @Test
    void givenThreeLettersAndOneRemoval_whenGetSize_thenReturn2() {
        rack.addLetter('a');
        rack.addLetter('a');
        rack.addLetter('c');
        rack.removeLetter('a');
        assertEquals(2, rack.getSize());
    }
}