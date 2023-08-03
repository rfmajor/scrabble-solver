package com.rfmajor.scrabblesolver.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rfmajor.scrabblesolver.common.game.Field;
import com.rfmajor.scrabblesolver.common.game.SpecialFields;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SpecialFieldsReader {
    private final ObjectMapper objectMapper;
    @Value("classpath:specialFields.json")
    private Resource resourceFile;

    public SpecialFields read() throws IOException {
        File jsonFile = resourceFile.getFile();
        SpecialFieldsUnparsed specialFieldsUnparsed = objectMapper.readValue(jsonFile, SpecialFieldsUnparsed.class);
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

    private List<Field> mapFields(List<int[]> fields) {
        return fields.stream()
                .map(this::mapField)
                .toList();
    }
}
