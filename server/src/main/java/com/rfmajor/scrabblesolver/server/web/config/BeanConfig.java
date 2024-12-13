package com.rfmajor.scrabblesolver.server.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.rfmajor.scrabblesolver.common.gaddag.calculate.PointCalculator;
import com.rfmajor.scrabblesolver.common.gaddag.export.GaddagFileReader;
import com.rfmajor.scrabblesolver.common.gaddag.model.Gaddag;
import com.rfmajor.scrabblesolver.common.scrabble.Alphabet;
import com.rfmajor.scrabblesolver.common.scrabble.SpecialFields;
import com.rfmajor.scrabblesolver.common.scrabble.SpecialFieldsDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class BeanConfig {
    @Bean
    public ObjectMapper defaultObjectMapper() {
        ObjectMapper mapper = new JsonMapper();
        SimpleModule module = new SimpleModule();

        module.addDeserializer(SpecialFields.class, new SpecialFieldsDeserializer());
        mapper.registerModule(module);

        return mapper;
    }

    @Bean
    public SpecialFields specialFields() throws IOException {
        return SpecialFields.loadDefault();
    }

    @Bean
    public Gaddag<Long> gaddag(@Value("${gaddag.directory}") String directory) {
        return new GaddagFileReader().read(directory);
    }

    @Bean
    public Alphabet alphabet(Gaddag<Long> gaddag) {
        return gaddag.getAlphabet();
    }

    @Bean
    public PointCalculator pointCalculator(SpecialFields specialFields) {
        return new PointCalculator(specialFields);
    }
}
