package com.example.BeGroom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BeGroomApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeGroomApplication.class, args);
	}

}
