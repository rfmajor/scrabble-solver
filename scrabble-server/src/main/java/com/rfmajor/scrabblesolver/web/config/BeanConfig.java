package com.rfmajor.scrabblesolver.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.rfmajor.scrabblesolver.common.PointCalculator;
import com.rfmajor.scrabblesolver.common.game.SpecialFields;
import com.rfmajor.scrabblesolver.gaddag.ExpandedGaddagConverter;
import com.rfmajor.scrabblesolver.gaddag.SimpleGaddagConverter;
import com.rfmajor.scrabblesolver.input.DictionaryReader;
import com.rfmajor.scrabblesolver.input.SpecialFieldsReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class BeanConfig {
    @Bean
    public SimpleGaddagConverter simpleGaddagConverter() {
        return new SimpleGaddagConverter();
    }

    @Bean
    public ExpandedGaddagConverter expandedGaddagConverter() {
        return new ExpandedGaddagConverter();
    }

    @Bean
    public ObjectMapper defaultObjectMapper() {
        return new JsonMapper();
    }

    @Bean
    public SpecialFieldsReader specialFieldsReader() {
        return new SpecialFieldsReader();
    }

    @Bean
    public SpecialFields specialFields(SpecialFieldsReader specialFieldsReader) throws IOException {
        return specialFieldsReader.read();
    }

    @Bean
    public PointCalculator pointCalculator(SpecialFields specialFields) {
        return new PointCalculator(specialFields);
    }

    @Bean
    public DictionaryReader dictionaryReader() {
        return new DictionaryReader();
    }
}
