package com.dentalCare.be_core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class BeCoreApplication {

	public static void main(String[] args) {
		// Configurar la zona horaria de la JVM a Buenos Aires
		TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
		SpringApplication.run(BeCoreApplication.class, args);
	}

}
