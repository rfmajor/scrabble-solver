package com.rfmajor.scrabblesolver.common.scrabble;

import java.util.List;

public record MoveGroup(String word, int points, List<MovePossibility> movePossibilities) {}
