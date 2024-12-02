package com.rfmajor.scrabblesolver.server.web.mapper;

import com.rfmajor.scrabblesolver.common.scrabble.Field;
import com.rfmajor.scrabblesolver.common.scrabble.Move;
import com.rfmajor.scrabblesolver.server.web.service.MoveDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mapper
public interface MoveMapper {
    @Mapping(source = "newBlankFields", target = "newBlankFields", qualifiedByName = "fieldToListOfIntArrays")
    MoveDto toDto(Move move);

    @Named("fieldToListOfIntArrays")
    static List<int[]> fieldToListOfIntArrays(Set<Field> fields) {
        List<int[]> mappedFields = new ArrayList<>();
        for (Field field : fields) {
            mappedFields.add(new int[]{field.getRow(), field.getColumn()});
        }
        return mappedFields;
    }

    @AfterMapping
    static void positionToStringPosition(@MappingTarget MoveDto moveDto) {
        char xCoord = 'A';
        xCoord += moveDto.getX();
        int yCoord = moveDto.getY() + 1;
        moveDto.setPosition("" + xCoord + yCoord);
    }
}
