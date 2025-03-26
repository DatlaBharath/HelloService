package com.iiht.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootTest
class HelloServiceApplicationTests {

    private static final Logger logger = LoggerFactory.getLogger(HelloServiceApplicationTests.class);

    @Test
    void contextLoads() {
        // Example of input validation and sanitization
        String input = getInputFromUser();
        if (isValidInput(input)) {
            // Proceed with the test
        } else {
            logger.error("Invalid input detected.");
            throw new IllegalArgumentException("Invalid input");
        }
    }

    private String getInputFromUser() {
        // Simulate dynamic user input for testing purposes
        String userInput = System.getProperty("userInput");
        if (userInput == null || userInput.isEmpty()) {
            throw new IllegalArgumentException("User input is required");
        }
        return userInput;
    }

    private boolean isValidInput(String input) {
        // Enhanced validation logic
        return input != null && input.matches("[a-zA-Z0-9]*") && isSafeInput(input);
    }

    private boolean isSafeInput(String input) {
        // Additional sanitization to prevent injection attacks
        String sanitizedInput = Encode.forJava(input);
        return sanitizedInput.equals(input) && input.equals(Encode.forHtml(input));
    }
}