package com.rfmajor.scrabblesolver.server.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.rfmajor.scrabblesolver.common.gaddag.calculate.CrossSetCalculator;
import com.rfmajor.scrabblesolver.common.gaddag.calculate.MoveGenerator;
import com.rfmajor.scrabblesolver.common.gaddag.calculate.MovePostProcessor;
import com.rfmajor.scrabblesolver.common.gaddag.calculate.PointCalculator;
import com.rfmajor.scrabblesolver.common.gaddag.export.GaddagFileReader;
import com.rfmajor.scrabblesolver.common.gaddag.model.Gaddag;
import com.rfmajor.scrabblesolver.common.scrabble.Alphabet;
import com.rfmajor.scrabblesolver.common.scrabble.SpecialFields;
import com.rfmajor.scrabblesolver.common.scrabble.SpecialFieldsDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public SpecialFields specialFields() {
        return SpecialFields.loadDefault();
    }

    @Bean
    public Gaddag<Long> gaddag(@Value("${gaddag.file}") String gaddagFile) {
        return new GaddagFileReader().read(gaddagFile);
    }

    @Bean
    public Alphabet alphabet(Gaddag<Long> gaddag) {
        return gaddag.getAlphabet();
    }

    @Bean
    public PointCalculator pointCalculator(SpecialFields specialFields) {
        return new PointCalculator(specialFields);
    }

    @Bean
    public CrossSetCalculator<Long> crossSetCalculator(Gaddag<Long> gaddag) {
        return new CrossSetCalculator<>(gaddag);
    }

    @Bean
    public MovePostProcessor movePostProcessor() {
        return new MovePostProcessor();
    }

    @Bean
    public MoveGenerator<Long> moveAlgorithmExecutor(Gaddag<Long> gaddag, MovePostProcessor movePostProcessor,
                                                     PointCalculator pointCalculator,
                                                     CrossSetCalculator<Long> crossSetCalculator) {
        return new MoveGenerator<>(gaddag, movePostProcessor, pointCalculator, crossSetCalculator);
    }
}
