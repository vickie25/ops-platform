package com.cymelle.ops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.cymelle.ops")
public class OpsApplication {

	public static void main(String[] args) {
		SpringApplication.run(OpsApplication.class, args);
	}

}
