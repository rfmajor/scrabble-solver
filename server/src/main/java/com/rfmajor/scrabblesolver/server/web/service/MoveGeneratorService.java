package com.rfmajor.scrabblesolver.server.web.service;

import com.rfmajor.scrabblesolver.common.scrabble.MoveGroup;

import java.util.List;

/**
 * Arguments:
 *
 * 1. The board containing all letters already played
 * 2. Computed cross-sets of the board represented as hex vectors
 * 3. The player’s rack
 * 4.1 The alphabet containing mappings between letters and their respective points
 * 4.2 Alternatively, just the language parameter - the server should know which alphabet to load
 *
 * Response:
 *
 * 1. List of possible words, and additional info associated with each word, including:
 * beginning field, direction (across, ), points
 * 2.
 */

public interface MoveGeneratorService {
    List<MoveGroup> generateMoves(GenerateMovesRequest request);
}
