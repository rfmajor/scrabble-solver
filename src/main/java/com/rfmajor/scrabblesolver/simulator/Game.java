package com.rfmajor.scrabblesolver.simulator;

import com.rfmajor.scrabblesolver.common.game.Board;
import com.rfmajor.scrabblesolver.common.game.Move;
import com.rfmajor.scrabblesolver.common.game.Rack;
import com.rfmajor.scrabblesolver.gaddag.MoveGeneratorFacade;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Data
public class Game {
    private final Board board;
    private final List<Player> players;
    private final MoveGeneratorFacade moveGenerator;
    private int numberOfPlayers;
    private boolean active;

    public void start() {
        active = true;
        while (active) {
            for (Player player : players) {
                Set<Move> allMoves = moveGenerator.generate(new Rack("dsa"));
            }
        }
    }

    public void stop() {
        active = false;
    }
}
