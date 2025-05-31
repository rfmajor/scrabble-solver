package com.rfmajor.scrabblesolver.server.web.mapper;

import com.rfmajor.scrabblesolver.common.scrabble.Board;
import com.rfmajor.scrabblesolver.common.scrabble.Field;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class BoardNotationMapper {

    // TODO: input validation
    public Board fromBoardNotation(String boardNotation) {
        char[][] boardChars = new char[Board.DEFAULT_SIZE][Board.DEFAULT_SIZE];
        String[] parts = boardNotation.split(" ");
        String[] rows = parts[0].split("/");
        String[] blanks;
        if (parts.length >= 2) {
            blanks = parts[1].substring(2).split(",");
        } else {
            blanks = new String[0];
        }

        // fill the fields
        for (int r = 0; r < Board.DEFAULT_SIZE; r++) {
            String row = rows[r];
            StringBuilder digits = new StringBuilder();
            int c = 0;
            for (int i = 0; i < row.length(); i++) {
                if (Character.isDigit(row.charAt(i))) {
                    digits.append(row.charAt(i));
                } else {
                    // fill the board with numOfEmpty empty fields
                    int numOfEmpty = !digits.isEmpty() ? Integer.parseInt(digits.toString()) : 0;
                    digits = new StringBuilder();
                    for (int j = 0; j < numOfEmpty; j++) {
                        boardChars[r][c++] = '\0';
                    }

                    boardChars[r][c++] = row.charAt(i);
                }
            }
            // fill the board with numOfEmpty empty fields
            int numOfEmpty = !digits.isEmpty() ? Integer.parseInt(digits.toString()) : 0;
            for (int j = 0; j < numOfEmpty; j++) {
                boardChars[r][c++] = '\0';
            }
        }

        // fill the blanks
        Set<Field> blankFields = new HashSet<>();
        for (String blank : blanks) {
            String[] blankParts = blank.split(";");
            int column = Integer.parseInt(blankParts[0]);
            int row = Integer.parseInt(blankParts[1]);
            blankFields.add(new Field(row, column));
        }

        return new Board(boardChars, blankFields);
    }
}
