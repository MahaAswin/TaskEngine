package com.taskengine.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TaskEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskEngineApplication.class, args);
	}

}
