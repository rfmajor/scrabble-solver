package com.rfmajor.scrabblesolver.movegen.input;

import com.rfmajor.scrabblesolver.movegen.common.model.Alphabet;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class DictionaryReader {
    private String dictionaryFilename;
    private String alphabetFilename;
    private int maxWordLength;

    private static final int NUM_OF_ALPHABET_LINES = 3;
    private static final int LETTERS_INDEX = 0;
    private static final int POINTS_INDEX = 1;
    private static final int QUANTITIES_INDEX = 2;

    public List<String> readAllWords(Alphabet alphabet) {
        List<String> words = new ArrayList<>();
        try (
                FileInputStream fis = new FileInputStream(Objects.requireNonNull(getClass().getResource(dictionaryFilename)).getFile());
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(isr)
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() <= maxWordLength && alphabet.isLegalWord(line)) {
                    words.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            log.error("Dictionary file not found, exception is: ", e);
        } catch (IOException e) {
            log.error("{}", e.getMessage());
        }
        return words;
    }

    public String[] readAlphabetLines() {
        String[] lines = new String[NUM_OF_ALPHABET_LINES];
        try (
                FileInputStream fis = new FileInputStream(Objects.requireNonNull(getClass().getResource(alphabetFilename)).getFile());
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(isr)
        ) {
            for (int i = 0; i < NUM_OF_ALPHABET_LINES; i++) {
                lines[i] = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            log.error("Alphabet file not found, exception is: ", e);
        } catch (IOException e) {
            log.error("{}", e.getMessage());
        }
        return lines;
    }

    public List<Character> getAlphabetLetters(String[] lines) {
        return lines[LETTERS_INDEX].chars()
                .mapToObj(num -> (char) num)
                .toList();
    }

    public List<Integer> getAlphabetPoints(String[] lines) {
        return getNumericValues(lines[POINTS_INDEX]);
    }

    public List<Integer> getAlphabetQuantities(String[] lines) {
        return getNumericValues(lines[QUANTITIES_INDEX]);
    }

    private List<Integer> getNumericValues(String line) {
        List<Integer> points = new ArrayList<>();
        for (char p : line.toCharArray()) {
            points.add(Integer.parseInt(String.valueOf(p)));
        }
        return points;
    }
}
