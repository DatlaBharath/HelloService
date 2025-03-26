```java
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
            return "Hello, " + name;
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
                .csrf().disable();
        }
    }
}
```