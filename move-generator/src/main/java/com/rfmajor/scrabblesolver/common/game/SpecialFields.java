package com.rfmajor.scrabblesolver.common.game;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class SpecialFields {
    private final Set<Field> doubleLetterFields;
    private final Set<Field> tripleLetterFields;
    private final Set<Field> doubleWordFields;
    private final Set<Field> tripleWordFields;

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
}
