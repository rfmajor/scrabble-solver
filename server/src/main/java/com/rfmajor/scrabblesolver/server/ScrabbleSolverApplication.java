package com.rfmajor.scrabblesolver.server;

import org.glassfish.tyrus.server.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ScrabbleSolverApplication {
    public static void main(String[] args) throws Exception {
        var ctx = SpringApplication.run(ScrabbleSolverApplication.class, args);
    }
}
