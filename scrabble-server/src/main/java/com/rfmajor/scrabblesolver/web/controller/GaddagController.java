package com.rfmajor.scrabblesolver.web.controller;

import com.rfmajor.scrabblesolver.web.service.GaddagMoveGeneratorService;
import com.rfmajor.scrabblesolver.web.service.GenerateMovesRequest;
import com.rfmajor.scrabblesolver.web.service.MoveDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GaddagController {
    private final GaddagMoveGeneratorService moveGeneratorService;

    @PostMapping("/generate")
    public List<MoveDto> generate(@RequestBody GenerateMovesRequest request) {
        return moveGeneratorService.generateMoves(request);
    }
}
