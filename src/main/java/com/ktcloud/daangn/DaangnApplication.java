package com.ktcloud.daangn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DaangnApplication {

	public static void main(String[] args) {
		SpringApplication.run(DaangnApplication.class, args);
	}

}