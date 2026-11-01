package se.iths.martin.productserviceprojekt2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Startar produkttjänstens Spring Boot-applikation.
 */
@SpringBootApplication
public class ProductServiceProjekt2Application {

	/**
	 * Startar applikationen.
	 *
	 * @param args argument från kommandoraden
	 */
	public static void main(String[] args) {
		SpringApplication.run(ProductServiceProjekt2Application.class, args);
	}

}
