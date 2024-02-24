package com.example;

import com.example.configs.NewsFeedKafkaService;
import com.example.service.NewsGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.util.Random;

@SpringBootApplication
public class MockNewsFeedApplication {

    public static void main(String[] args) {
        SpringApplication.run(MockNewsFeedApplication.class, args);
    }


    @Bean
    Random random() {
        return new Random();
    }

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }


    @Bean
    CommandLineRunner run(NewsFeedKafkaService newsFeedKafkaService, NewsGenerator newsGenerator) {
        return args -> {

            while (true) newsFeedKafkaService.sendDataToNewsAnalyzerTopic(newsGenerator.randomNewsGenerator());

        };
    }
}