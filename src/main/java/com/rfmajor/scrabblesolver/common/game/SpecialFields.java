package com.rfmajor.scrabblesolver.common.game;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SpecialFields {
    private final List<Field> doubleLetterFields;
    private final List<Field> tripleLetterFields;
    private final List<Field> doubleWordFields;
    private final List<Field> tripleWordFields;
}
