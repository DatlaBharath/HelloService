package com.iiht.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

@SpringBootTest
class HelloServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}

@RestController
class HelloServiceController {

    private static final String DEFAULT_NAME = "World";

    @GetMapping("/hello")
    public String sayHello(@RequestParam(name = "name", required = false) String name) {
        if (!StringUtils.hasText(name)) {
            name = DEFAULT_NAME;
        }
        if (!name.matches("[a-zA-Z]+")) {
            throw new IllegalArgumentException("Invalid input");
        }
        return "Hello, " + name + "!";
    }
}

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid input provided");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
    }
}