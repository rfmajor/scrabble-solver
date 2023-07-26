package com.rfmajor.scrabblesolver.input;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class DictionaryReader {
    @Value("${dictionary.words.filepath}")
    private Resource dictionaryResource;
    @Value("${dictionary.alphabet.filepath}")
    private Resource alphabetResource;
    @Value("${gaddag.wordlength.constraint}")
    private int maxWordLength;

    public List<String> readAllWords() {
        List<String> words = new ArrayList<>();
        try (
                FileInputStream fis = new FileInputStream(dictionaryResource.getFile());
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(isr)
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() <= maxWordLength) {
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

    public List<Character> readAlphabet() {
        try (
                FileInputStream fis = new FileInputStream(dictionaryResource.getFile());
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(isr)
        ) {
            return reader.readLine().chars()
                    .mapToObj(num -> (char) num)
                    .toList();
        } catch (FileNotFoundException e) {
            log.error("Alphabet file not found, exception is: ", e);
        } catch (IOException e) {
            log.error("{}", e.getMessage());
        }
        return Collections.emptyList();
    }
}
