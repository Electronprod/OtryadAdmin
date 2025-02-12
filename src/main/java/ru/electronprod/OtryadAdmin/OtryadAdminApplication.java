package ru.electronprod.OtryadAdmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class OtryadAdminApplication {
	public static void main(String[] args) {
		SpringApplication.run(OtryadAdminApplication.class, args);
	}
}