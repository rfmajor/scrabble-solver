package com.rfmajor.scrabblesolver.gaddag;

import com.rfmajor.scrabblesolver.common.Board;
import com.rfmajor.scrabblesolver.common.Rack;
import org.springframework.stereotype.Component;

@Component
public class MoveGenerator {
    private Board board;

    /**
     * Method for left-right move generation
     * 'position' is y
    **/
    public void generate(int position, int x, String word, Rack rack, Arc arc) {
        if (/*if a letter, L, is already on this square*/ !board.isEmpty(x, position)) {
            char letter = board.getLetter(x, position);
            goOn(position, letter, word, rack, arc.getNextArc(letter), arc);
        } else if (/*letters remain on the rack*/ !rack.isEmpty()) {
            for (char letter : rack.getLetters()) {
                goOn(position, letter, word, rack.withRemovedLetter(letter), arc.getNextArc(letter), arc);
            }
            if (rack.contains(Rack.BLANK)) {
                for (/*for each letter the blank could be, L, allowed on this square*/ ) {
                    goOn(position, letter, word, rack.withRemovedLetter(Rack.BLANK), arc.getNextArc(letter), arc);
                }
            }
        }
    }

    private void goOn(int position, char letter, String word, Rack rack, Arc newArc, Arc oldArc) {
        if (position <= 0) {
            word = letter + word;
            if (oldArc.hasNextArc(letter) && !oldArc.containsLetter(letter)) {
                recordPlay();
            }
            if (newArc)
        }
    }

    private void recordPlay() {

    }
}
