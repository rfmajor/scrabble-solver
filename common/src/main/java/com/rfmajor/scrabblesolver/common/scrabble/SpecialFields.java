package com.rfmajor.scrabblesolver.common.scrabble;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.Builder;
import lombok.Data;

import java.io.IOException;
import java.util.Set;

@Data
@Builder
public class SpecialFields {
    private final Set<Field> doubleLetterFields;
    private final Set<Field> tripleLetterFields;
    private final Set<Field> doubleWordFields;
    private final Set<Field> tripleWordFields;

    public static final String DOUBLE_LETTER = "doubleLetter";
    public static final String TRIPLE_LETTER = "tripleLetter";
    public static final String DOUBLE_WORD = "doubleWord";
    public static final String TRIPLE_WORD = "tripleWord";

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DEFAULT_FIELDS_FILENAME = "specialFields.json";

    static {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(SpecialFields.class, new SpecialFieldsDeserializer());
        objectMapper.registerModule(module);
    }

    public boolean isDoubleLetter(int x, int y) {
        return doubleLetterFields.contains(new Field(x, y));
    }

    public boolean isTripleLetter(int x, int y) {
        return tripleLetterFields.contains(new Field(x, y));
    }

    public boolean isDoubleWord(int x, int y) {
        return doubleWordFields.contains(new Field(x, y));
    }

    public boolean isTripleWord(int x, int y) {
        return tripleWordFields.contains(new Field(x, y));
    }

    public static SpecialFields loadDefault() throws IOException { return objectMapper.readValue(
                SpecialFields.class.getClassLoader().getResourceAsStream(DEFAULT_FIELDS_FILENAME),
                SpecialFields.class);
    }
}
