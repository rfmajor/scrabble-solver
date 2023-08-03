package com.rfmajor.scrabblesolver.web.mapper;

import com.rfmajor.scrabblesolver.common.game.Move;
import com.rfmajor.scrabblesolver.web.service.MoveDto;
import org.mapstruct.Mapper;

@Mapper
public interface MoveMapper {
    MoveDto toDto(Move move);
}
