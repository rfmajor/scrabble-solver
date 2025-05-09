package com.rfmajor.scrabblesolver.server.web.service;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GenerateMovesRequest {
    private BoardDto board;
    private String rackLetters;
}
