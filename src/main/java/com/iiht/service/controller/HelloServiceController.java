package com.iiht.service.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.HtmlUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Value;

@Controller
public class HelloServiceController {

    private static final Logger logger = LoggerFactory.getLogger(HelloServiceController.class);

    @Value("${app.hello.message}")
    private String helloMessage;

    @Value("${app.hello.description}")
    private String helloDescription;

    @Value("${app.greet.message}")
    private String greetMessage;

    @Value("${app.error.invalidInput}")
    private String invalidInputMessage;

    @Value("${app.error.unexpected}")
    private String unexpectedErrorMessage;

    @GetMapping
    public ResponseEntity<String> hello(Model model) {
        logger.info("hello() method called");
        model.addAttribute("message", HtmlUtils.htmlEscape(helloMessage));
        model.addAttribute("description", HtmlUtils.htmlEscape(helloDescription));
        HttpHeaders headers = createSecurityHeaders();
        return ResponseEntity.ok().headers(headers).body("hello");
    }

    @GetMapping("/greet")
    public ResponseEntity<String> greet() {
        logger.info("greet() method called");
        HttpHeaders headers = createSecurityHeaders();
        return ResponseEntity.ok().headers(headers).body(HtmlUtils.htmlEscape(greetMessage));
    }

    @GetMapping("/add/{a}/{b}")
    public ResponseEntity<String> add(@PathVariable String a, @PathVariable String b) {
        try {
            int numA = validateAndParseInt(a);
            int numB = validateAndParseInt(b);
            int result = numA + numB;
            logger.info("add() method called with parameters: a={}, b={}", numA, numB);
            HttpHeaders headers = createSecurityHeaders();
            return ResponseEntity.ok().headers(headers).body(HtmlUtils.htmlEscape(String.valueOf(result)));
        } catch (NumberFormatException e) {
            logger.error("Invalid input for add() method");
            HttpHeaders headers = createSecurityHeaders();
            return ResponseEntity.badRequest().headers(headers).body(HtmlUtils.htmlEscape(invalidInputMessage));
        } catch (ArithmeticException e) {
            logger.error("Arithmetic error in add() method");
            HttpHeaders headers = createSecurityHeaders();
            return ResponseEntity.badRequest().headers(headers).body(HtmlUtils.htmlEscape("Arithmetic error occurred"));
        } catch (Exception e) {
            logger.error("Unexpected error in add() method");
            HttpHeaders headers = createSecurityHeaders();
            return ResponseEntity.badRequest().headers(headers).body(HtmlUtils.htmlEscape(unexpectedErrorMessage));
        }
    }

    @GetMapping("/calculateFactorial/{a}")
    public ResponseEntity<String> calculateFactorial(@PathVariable String a) {
        try {
            int numA = validateAndParseInt(a);
            if (numA < 0) {
                HttpHeaders headers = createSecurityHeaders();
                return ResponseEntity.badRequest().headers(headers).body(HtmlUtils.htmlEscape(invalidInputMessage));
            }
            int fact = 1;
            for (int i = 1; i <= numA; i++) {
                fact = Math.multiplyExact(fact, i);
            }
            logger.info("calculateFactorial() method called with parameter: a={}", numA);
            HttpHeaders headers = createSecurityHeaders();
            return ResponseEntity.ok().headers(headers).body(HtmlUtils.htmlEscape(String.valueOf(fact)));
        } catch (NumberFormatException e) {
            logger.error("Invalid input for calculateFactorial() method");
            HttpHeaders headers = createSecurityHeaders();
            return ResponseEntity.badRequest().headers(headers).body(HtmlUtils.htmlEscape(invalidInputMessage));
        } catch (ArithmeticException e) {
            logger.error("Arithmetic error in calculateFactorial() method");
            HttpHeaders headers = createSecurityHeaders();
            return ResponseEntity.badRequest().headers(headers).body(HtmlUtils.htmlEscape("Arithmetic error occurred"));
        } catch (Exception e) {
            logger.error("Unexpected error in calculateFactorial() method");
            HttpHeaders headers = createSecurityHeaders();
            return ResponseEntity.badRequest().headers(headers).body(HtmlUtils.htmlEscape(unexpectedErrorMessage));
        }
    }

    private int validateAndParseInt(String input) throws NumberFormatException {
        if (input == null || !input.matches("\\d+")) {
            throw new NumberFormatException("Invalid input");
        }
        return Integer.parseInt(input);
    }

    private HttpHeaders createSecurityHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Content-Type-Options", "nosniff");
        headers.add("X-Frame-Options", "DENY");
        headers.add("X-XSS-Protection", "1; mode=block");
        headers.add("Content-Security-Policy", "default-src 'self'; script-src 'self'");
        return headers;
    }
}