package com.rfmajor.scrabblesolver.web.config;

import com.rfmajor.scrabblesolver.common.Board;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

@Configuration
public class BeanDefinitions {
    @Bean
    @RequestScope
    public Board board() {

    }
}
