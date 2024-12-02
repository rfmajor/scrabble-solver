package com.rfmajor.scrabblesolver.server.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.rfmajor.scrabblesolver.common.scrabble.SpecialFields;
import com.rfmajor.scrabblesolver.common.gaddag.calculate.PointCalculator;
import com.rfmajor.scrabblesolver.common.gaddag.convert.ExpandedGaddagConverter;
import com.rfmajor.scrabblesolver.common.gaddag.convert.SimpleGaddagConverter;
import com.rfmajor.scrabblesolver.gaddag.converter.input.DictionaryReader;
import com.rfmajor.scrabblesolver.gaddag.converter.input.SpecialFieldsReader;
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
