package com.yelp.restaurantFinder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;


/**
 * This is our Spring Boot application that allows the user to type in their favorite restuarant name and recieve two
 * recommendations based off of our custom similarity metric.
 *
 * @author David Hennigan and Anthony Impellizzeri
 */

@SpringBootApplication
public class RestaurantFinderApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(RestaurantFinderApplication.class, args);
	}

}
