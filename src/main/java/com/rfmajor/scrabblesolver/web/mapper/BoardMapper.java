package com.rfmajor.scrabblesolver.web.mapper;

import com.rfmajor.scrabblesolver.common.Board;
import com.rfmajor.scrabblesolver.web.service.BoardDto;
import org.mapstruct.Mapper;

@Mapper
public interface BoardMapper {
    Board fromDto(BoardDto dto);
}
