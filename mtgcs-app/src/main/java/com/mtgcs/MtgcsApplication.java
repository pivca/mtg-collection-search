package com.mtgcs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class MtgcsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MtgcsApplication.class, args);
    }
}
