package com.iiht.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

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
            try {
                return "Hello, " + sanitizeAndValidateInput(name) + "!";
            } catch (IllegalArgumentException e) {
                return "Invalid input provided.";
            } catch (Exception e) {
                return "An unexpected error occurred.";
            }
        }

        private String sanitizeAndValidateInput(String input) {
            if (input == null || input.isEmpty() || input.length() > 50) {
                throw new IllegalArgumentException("Invalid input");
            }
            String sanitizedInput = HtmlUtils.htmlEscape(input.replaceAll("[^a-zA-Z0-9]", ""));
            if (!sanitizedInput.matches("^[a-zA-Z0-9]+$")) {
                throw new IllegalArgumentException("Invalid input");
            }
            return sanitizedInput;
        }
    }

    @EnableWebSecurity
    public class SecurityConfig extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                .authorizeRequests()
                .antMatchers("/hello").permitAll()
                .anyRequest().authenticated()
                .and()
                .csrf().disable();
        }
    }
}