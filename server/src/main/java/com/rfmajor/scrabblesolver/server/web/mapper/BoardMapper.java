package com.rfmajor.scrabblesolver.server.web.mapper;

import com.rfmajor.scrabblesolver.common.scrabble.Board;
import com.rfmajor.scrabblesolver.common.scrabble.Field;
import com.rfmajor.scrabblesolver.server.web.service.BoardDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mapper
public interface BoardMapper {
    @Mapping(source = "blankFields", target = "blankFields", qualifiedByName = "listOfIntArraysToFields")
    Board fromDto(BoardDto dto);

    @Named("listOfIntArraysToFields")
    static Set<Field> listOfIntArraysToFields(List<int[]> fields) {
        Set<Field> mappedFields = new HashSet<>();
        for (int[] field : fields) {
            mappedFields.add(new Field(field[0], field[1]));
        }
        return mappedFields;
    }
}
