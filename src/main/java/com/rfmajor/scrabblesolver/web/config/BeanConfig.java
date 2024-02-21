package com.rfmajor.scrabblesolver.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.rfmajor.scrabblesolver.common.game.SpecialFields;
import com.rfmajor.scrabblesolver.gaddag.SimpleGaddagConverter;
import com.rfmajor.scrabblesolver.input.SpecialFieldsReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class BeanConfig {
    @Bean
    public SimpleGaddagConverter gaddagConverter() {
        return new SimpleGaddagConverter();
    }

    @Bean
    public ObjectMapper defaultObjectMapper() {
        return new JsonMapper();
    }

    @Bean
    public SpecialFields specialFields(SpecialFieldsReader specialFieldsReader) throws IOException {
        return specialFieldsReader.read();
    }
}
