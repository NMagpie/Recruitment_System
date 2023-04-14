package com.rectruit.authentication;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.rectruit.authentication.ssl.SslContextInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

@SpringBootApplication
public class AuthenticationApplication {

	@Autowired
	public SslContextInitializer sslContextInitializer;

	// Define the constants for database connection
	public final String HOST = "localhost";
	public final int PORT = 27017;
	public final String DATABASE = "authentication";

	public static void main(String[] args) {
		SpringApplication.run(AuthenticationApplication.class, args);
	}

	// Create a MongoClient bean with SSL enabled
	@Bean
	public MongoClient mongoClient() {

		// Create a MongoCredential object with username, database and password
		MongoCredential credential = MongoCredential.createCredential("", DATABASE, "".toCharArray());

		// Create a MongoClientSettings object with SSL settings and credential
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyToSslSettings(builder -> {
					try {
						builder.enabled(true).invalidHostNameAllowed(true).context(sslContextInitializer.initSslContext()); // Enable SSL
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
				//.credential(credential) // Set the credential
				.applyToClusterSettings(builder -> {
					builder.hosts(List.of(new ServerAddress(HOST, PORT))); // Set the host and port
				})
				.build();

		// Create and return a MongoClient object with the settings
		return MongoClients.create(settings);
	}

	// Create a MongoTemplate bean with the MongoClient and database name
	@Bean
	public MongoTemplate mongoTemplate(MongoClient mongoClient) {
		return new MongoTemplate(mongoClient, DATABASE);
	}

}