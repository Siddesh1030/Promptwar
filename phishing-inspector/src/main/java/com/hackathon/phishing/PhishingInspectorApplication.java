package com.hackathon.phishing;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.hackathon.phishing.repository")
@EntityScan("com.hackathon.phishing.model")
public class PhishingInspectorApplication {

	public static void main(String[] args) {
		// Load environment variables from .env file securely
		try {
			Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
			dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
		} catch (Exception e) {
			System.out.println("Warning: No .env file found or error loading it. Falling back to system environment variables.");
		}
		SpringApplication.run(PhishingInspectorApplication.class, args);
	}

}
