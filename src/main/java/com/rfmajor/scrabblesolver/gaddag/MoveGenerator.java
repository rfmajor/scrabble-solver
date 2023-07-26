package com.rfmajor.scrabblesolver.gaddag;

import com.rfmajor.scrabblesolver.common.Board;
import com.rfmajor.scrabblesolver.common.Rack;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MoveGenerator {
    private Board board;
    private AnchorTracker anchorTracker;
    @Value("${gaddag.delimiter}")
    private char delimiter;

    /**
     * Method for left-right move generation
     * 'position' is an offset from the anchor square
    **/
    public void generate(int position, int row, String word, Rack rack, Arc arc) {
        if (/*if a letter, L, is already on this square*/ !board.isEmpty(row, position)) {
            char letter = board.getLetter(row, position);
            goOn(position, row, letter, word, rack, arc.getNextArc(letter), arc);
        } else if (/*letters remain on the rack*/ !rack.isEmpty()) {
            for (char letter : rack.getLetters()) {
                goOn(position, row, letter, word, rack.withRemovedLetter(letter), arc.getNextArc(letter), arc);
            }
            if (rack.contains(Rack.BLANK)) {
                for (/*for each letter the blank could be, L, allowed on this square*/ char letter : arc.getNextLetters()) {
                    goOn(position, row, letter, word, rack.withRemovedLetter(Rack.BLANK), arc.getNextArc(letter), arc);
                }
            }
        }
    }

    private void goOn(int position, int x, char letter, String word, Rack rack, Arc newArc, Arc oldArc) {
        if (position <= 0) {
            word = letter + word;
            if (oldArc.hasNextArc(letter) && !oldArc.containsLetter(letter)) {
                recordPlay();
            }
            if (newArc != 0 /*? idk yet what to do here*/) {
                if (/*if room to the left*/) {
                    generate(position - 1, x, word, rack, newArc);
                }
                newArc = newArc.getNextArc(delimiter);
                if (newArc != 0 /*idk again*/ && /*no letter directly left*/ && /*room to the right*/) {
                    generate(1, x, word, rack, newArc);
                }
            }
        } else {
            word = word + letter;
            if (oldArc.getLetter() == letter && /*no letter directly right*/) {
                recordPlay();
            }
            if (newArc != 0 /*¯\_(ツ)_/¯*/ && /*room to the right*/) {
                generate(position + 1, x, word, rack, newArc);
            }
        }
    }

    private void recordPlay() {

    }
}
