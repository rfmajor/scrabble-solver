package com.rfmajor.scrabblesolver.web.config;

import com.rfmajor.scrabblesolver.gaddag.GaddagConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {
    @Bean
    public GaddagConverter gaddagConverter() {
        return new GaddagConverter();
    }
}
