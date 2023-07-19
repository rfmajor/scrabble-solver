package com.rfmajor.scrabblesolver;

import com.rfmajor.scrabblesolver.gaddag.GaddagConverter;
import com.rfmajor.scrabblesolver.gaddag.State;
import com.rfmajor.scrabblesolver.input.DictionaryReader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class ScrabbleSolverApplication {

    public static void main(String[] args) {
        var ctx = SpringApplication.run(ScrabbleSolverApplication.class, args);
        List<String> words = ctx.getBean(DictionaryReader.class).readAllWords();
        State state = ctx.getBean(GaddagConverter.class).convert(words);
        System.out.println();
    }

}
