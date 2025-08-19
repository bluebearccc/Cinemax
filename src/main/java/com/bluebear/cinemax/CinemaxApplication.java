package com.bluebear.cinemax;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CinemaxApplication {

    public static void main(String[] args) {
        SpringApplication.run(CinemaxApplication.class, args);
    }

}
