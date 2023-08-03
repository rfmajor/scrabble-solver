package com.rfmajor.scrabblesolver.common.game;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SpecialFields {
    private List<Field> doubleLetterFields;
    private List<Field> tripleLetterFields;
    private List<Field> doubleWordFields;
    private List<Field> tripleWordFields;
}
