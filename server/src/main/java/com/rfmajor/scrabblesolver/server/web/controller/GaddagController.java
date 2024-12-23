package com.rfmajor.scrabblesolver.server.web.controller;

import com.rfmajor.scrabblesolver.common.scrabble.MoveGroup;
import com.rfmajor.scrabblesolver.server.web.service.BoardDto;
import com.rfmajor.scrabblesolver.server.web.service.GenerateMovesRequest;
import com.rfmajor.scrabblesolver.server.web.service.MoveGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GaddagController {
    private final MoveGeneratorService moveGeneratorService;

    @PostMapping("/generate")
    public List<MoveGroup> generate(@RequestBody GenerateMovesRequest request) {
        return moveGeneratorService.generateMoves(request);
    }

    @GetMapping("/board")
    public BoardDto getBoard() {
        return new BoardDto();
    }
}
