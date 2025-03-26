package com.iiht.service.controller;

import org.springframework.http.HttpHeaders;
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

@Controller
public class HelloServiceController {

    private static final Logger logger = LoggerFactory.getLogger(HelloServiceController.class);

    @GetMapping
    public String hello(Model model) {
        logger.info("hello() method called");
        model.addAttribute("message", HtmlUtils.htmlEscape("Congratulations! The app is Deployed for first time again !!! 👍 😁"));
        model.addAttribute("description", HtmlUtils.htmlEscape("Your application is up and running successfully!"));
        return "hello";
    }

    @GetMapping("/greet")
    public String greet() {
        logger.info("greet() method called");
        return HtmlUtils.htmlEscape("Good Morning, Welcome To Demo Project");
    }

    @GetMapping("/add/{a}/{b}")
    public String add(@PathVariable String a, @PathVariable String b) {
        try {
            int numA = validateAndParseInt(a);
            int numB = validateAndParseInt(b);
            int result = numA + numB;
            logger.info("add() method called with parameters: result={}", result);
            return HtmlUtils.htmlEscape(String.valueOf(result));
        } catch (NumberFormatException e) {
            logger.error("Invalid input for add() method", e);
            return HtmlUtils.htmlEscape("Invalid input. Please provide valid integers.");
        } catch (Exception e) {
            logger.error("Unexpected error in add() method: ", e);
            return HtmlUtils.htmlEscape("An unexpected error occurred. Please try again later.");
        }
    }

    @GetMapping("/calculateFactorial/{a}")
    public String calculateFactorial(@PathVariable String a) {
        try {
            int numA = validateAndParseInt(a);
            if (numA < 0) {
                return HtmlUtils.htmlEscape("Invalid input. Please provide a non-negative integer.");
            }
            int fact = 1;
            for (int i = 1; i <= numA; i++) {
                fact *= i;
            }
            logger.info("calculateFactorial() method called with parameter: result={}", fact);
            return HtmlUtils.htmlEscape(fact + "");
        } catch (NumberFormatException e) {
            logger.error("Invalid input for calculateFactorial() method", e);
            return HtmlUtils.htmlEscape("Invalid input. Please provide a valid integer.");
        } catch (Exception e) {
            logger.error("Unexpected error in calculateFactorial() method: ", e);
            return HtmlUtils.htmlEscape("An unexpected error occurred. Please try again later.");
        }
    }

    private int validateAndParseInt(String input) throws NumberFormatException {
        if (input == null || !input.matches("\\d+")) {
            throw new NumberFormatException("Invalid input");
        }
        return Integer.parseInt(input);
    }
}