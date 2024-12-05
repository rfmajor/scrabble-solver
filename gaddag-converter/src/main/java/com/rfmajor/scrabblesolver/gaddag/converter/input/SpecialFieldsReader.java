package com.rfmajor.scrabblesolver.gaddag.converter.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rfmajor.scrabblesolver.common.scrabble.Field;
import com.rfmajor.scrabblesolver.common.scrabble.SpecialFields;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SpecialFieldsReader {
    private ObjectMapper objectMapper = new ObjectMapper();
    private String fileName;

    public SpecialFields read() throws IOException {
        SpecialFieldsUnparsed specialFieldsUnparsed =
                objectMapper.readValue(getClass().getResource(fileName), SpecialFieldsUnparsed.class);
        return SpecialFields.builder()
                .doubleLetterFields(mapFields(specialFieldsUnparsed.getDoubleLetterFields()))
                .tripleLetterFields(mapFields(specialFieldsUnparsed.getTripleLetterFields()))
                .doubleWordFields(mapFields(specialFieldsUnparsed.getDoubleWordFields()))
                .tripleWordFields(mapFields(specialFieldsUnparsed.getTripleWordFields()))
                .build();
    }

    @Data
    @NoArgsConstructor
    public static class SpecialFieldsUnparsed {
        @JsonProperty("doubleLetter")
        private List<int[]> doubleLetterFields;
        @JsonProperty("tripleLetter")
        private List<int[]> tripleLetterFields;
        @JsonProperty("doubleWord")
        private List<int[]> doubleWordFields;
        @JsonProperty("tripleWord")
        private List<int[]> tripleWordFields;
    }

    private Field mapField(int[] field) {
        return new Field(field[0], field[1]);
    }

    private Set<Field> mapFields(List<int[]> fields) {
        return fields.stream()
                .map(this::mapField)
                .collect(Collectors.toSet());
    }
}
