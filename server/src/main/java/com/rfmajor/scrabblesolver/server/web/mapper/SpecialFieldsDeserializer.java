package com.rfmajor.scrabblesolver.server.web.mapper;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.rfmajor.scrabblesolver.common.scrabble.Field;
import com.rfmajor.scrabblesolver.common.scrabble.SpecialFields;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.rfmajor.scrabblesolver.common.scrabble.SpecialFields.DOUBLE_LETTER;
import static com.rfmajor.scrabblesolver.common.scrabble.SpecialFields.DOUBLE_WORD;
import static com.rfmajor.scrabblesolver.common.scrabble.SpecialFields.TRIPLE_LETTER;
import static com.rfmajor.scrabblesolver.common.scrabble.SpecialFields.TRIPLE_WORD;

public class SpecialFieldsDeserializer extends StdDeserializer<SpecialFields> {

    public SpecialFieldsDeserializer() {
        this(null);
    }

    protected SpecialFieldsDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public SpecialFields deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        return SpecialFields.builder()
                .doubleLetterFields(getFieldsFromNode(DOUBLE_LETTER, node))
                .tripleLetterFields(getFieldsFromNode(TRIPLE_LETTER, node))
                .doubleWordFields(getFieldsFromNode(DOUBLE_WORD, node))
                .tripleWordFields(getFieldsFromNode(TRIPLE_WORD, node))
                .build();
    }

    private Set<Field> getFieldsFromNode(String fieldsName, JsonNode jsonNode) {
        JsonNode fieldsNode = jsonNode.get(fieldsName);
        Set<Field> fields = new HashSet<>();
        for (JsonNode field : fieldsNode) {
            int x = field.get(0).asInt();
            int y = field.get(1).asInt();

            fields.add(new Field(x, y));
        }
        return fields;
    }
}
