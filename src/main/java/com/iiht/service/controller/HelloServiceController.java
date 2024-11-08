package com.iiht.service.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloServiceController {
	
	@GetMapping
	public String hello() 
	{
		return "<h1 style="color : "blue"> Congratulations Application is Deployed </h1>";
	}

	@GetMapping("/greet")
	public String greet() 
	{
		return "Good Morning, Welcome To Demo Project";
	}
	
	@GetMapping("/add/{a}/{b}")
	public String add(@PathVariable int a,@PathVariable int b) {
		return (a+b) +"";
	}
	
	@GetMapping("/fact/{a}")
	public String add(@RequestHeader HttpHeaders header,@PathVariable int a) {
		int fact = 1;
		for(int i=1;i<=a;i++) {
			fact *=i;
		}
		return fact+""+header.toString();
	}
}
