package com.iiht.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.WebSecurity;

@SpringBootApplication
@EnableDiscoveryClient
public class HelloServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HelloServiceApplication.class, args);
    }

    @RestController
    @Validated
    public class HelloController {

        @GetMapping("/hello")
        public String sayHello(@RequestParam @NotBlank @Pattern(regexp = "^[a-zA-Z0-9]*$") String name) {
            try {
                return "Hello, " + sanitizeInput(name);
            } catch (Exception e) {
                throw new RuntimeException("An error occurred while processing your request.");
            }
        }

        private String sanitizeInput(String input) {
            return input.replaceAll("[^a-zA-Z0-9]", "");
        }

        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred. Please try again later.");
        }
    }

    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        return HeaderHttpSessionIdResolver.xAuthToken();
    }

    @EnableWebSecurity
    public class SecurityConfig extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                .authorizeRequests()
                    .antMatchers("/hello").authenticated()
                    .anyRequest().permitAll()
                .and()
                .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyTrue());
        }

        @Override
        public void configure(WebSecurity web) throws Exception {
            web.ignoring().antMatchers("/public/**");
        }
    }
}