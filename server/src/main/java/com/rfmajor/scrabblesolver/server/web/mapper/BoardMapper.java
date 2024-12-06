package com.rfmajor.scrabblesolver.server.web.mapper;

import com.rfmajor.scrabblesolver.common.scrabble.Board;
import com.rfmajor.scrabblesolver.server.web.service.BoardDto;
import org.mapstruct.Mapper;

@Mapper
public interface BoardMapper {
    Board fromDto(BoardDto dto);
}
