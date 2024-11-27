package com.rfmajor.scrabblesolver.server;

import com.rfmajor.scrabblesolver.movegen.common.model.SpecialFields;
import com.rfmajor.scrabblesolver.server.web.service.LexiconRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ScrabbleSolverApplication {

    public static void main(String[] args) {
        var ctx = SpringApplication.run(ScrabbleSolverApplication.class, args);
        SpecialFields specialFields = ctx.getBean(SpecialFields.class);
        ctx.getBean(LexiconRegistry.class).init();
//        System.out.println(State.states);
        /*var dictionaryReader = ctx.getBean(DictionaryReader.class);
        String[] alphabetLines = dictionaryReader.readAlphabetLines();
        List<Character> alphabetLetters = dictionaryReader.getAlphabetLetters(alphabetLines);
        List<Integer> alphabetPoints = dictionaryReader.getAlphabetPoints(alphabetLines);
        List<Integer> alphabetQuantities = dictionaryReader.getAlphabetQuantities(alphabetLines);
        Alphabet alphabet = new Alphabet(alphabetLetters, alphabetPoints, alphabetQuantities);
        List<String> words = dictionaryReader.readAllWords(alphabet);
        Arc parentArc = ctx.getBean(GaddagConverter.class).convert(words, alphabet);
        Gaddag gaddag = new Gaddag(parentArc, alphabet);
        Board board = new Board();
        CrossSetCalculator crossSetCalculator = new CrossSetCalculator(board, alphabet, gaddag);
        for (int row = 0; row < board.length(); row++){
            crossSetCalculator.computeCrossSets(row);
        }
        MoveGenerator moveGenerator = new MoveGenerator(board, crossSetCalculator, alphabet, gaddag);
        for (Point anchor : crossSetCalculator.getAnchors()) {
            List<Move> moves = moveGenerator.generate(anchor.getRow(), anchor.getColumn(), new Rack("jsdgopi"));
            moves.forEach(System.out::println);
        }

        // 5073 distinct letter sets -> 13 bits as a representation of the set after compression
        // 5449001 states -> 23 bits

        System.out.println();*/
    }

}
