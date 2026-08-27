package com.caliper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CaliperApplication {

	public static void main(String[] args) {
		SpringApplication.run(CaliperApplication.class, args);
	}
}