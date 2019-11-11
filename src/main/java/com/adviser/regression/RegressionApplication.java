package com.adviser.regression;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class}
)
public class RegressionApplication {

	public static void main(String[] args) {
		SpringApplication.run(RegressionApplication.class, args);
	}

}
