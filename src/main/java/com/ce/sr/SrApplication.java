package com.ce.sr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class SrApplication {

	public static void main(String[] args) {
		SpringApplication.run(SrApplication.class, args);
	}

}
