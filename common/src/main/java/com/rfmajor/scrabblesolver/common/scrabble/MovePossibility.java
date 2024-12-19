package com.rfmajor.scrabblesolver.common.scrabble;

import java.util.Set;

public record MovePossibility(int x, int y, String position, Direction direction, Set<Field> newBlankFields) {}
