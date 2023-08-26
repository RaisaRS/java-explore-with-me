package ru.practicum.explore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"ru.practicum.explore"})
public class EvmMainApp {
    public static void main(String[] args) {
        SpringApplication.run(EvmMainApp.class, args);
    }
}
