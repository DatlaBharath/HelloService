package com.iiht.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableDiscoveryClient
public class HelloServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HelloServiceApplication.class, args);
    }

    @RestController
    class HelloController {

        @GetMapping("/hello")
        public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
            return "Hello, " + sanitizeInput(name) + "!";
        }

        private String sanitizeInput(String input) {
            return input.replaceAll("[^a-zA-Z0-9]", "");
        }
    }
}