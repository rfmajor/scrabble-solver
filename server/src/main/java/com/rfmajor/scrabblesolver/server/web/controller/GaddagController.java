package com.rfmajor.scrabblesolver.server.web.controller;

import com.rfmajor.scrabblesolver.server.web.service.GenerateMovesRequest;
import com.rfmajor.scrabblesolver.server.web.service.MoveDto;
import com.rfmajor.scrabblesolver.server.web.service.MoveGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GaddagController {
    private final MoveGeneratorService moveGeneratorService;

    @PostMapping("/generate")
    public List<MoveDto> generate(@RequestBody GenerateMovesRequest request) {
        return moveGeneratorService.generateMoves(request);
    }
}
