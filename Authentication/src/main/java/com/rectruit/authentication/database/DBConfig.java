package com.rectruit.authentication.database;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.rectruit.authentication.ssl.SslContextInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DBConfig {

    @Autowired
    public SslContextInitializer sslContextInitializer;

    // Define the constants for database connection
    @Value("${spring.data.mongodb.host}")
    public String HOST;

    @Value("${spring.data.mongodb.port}")
    public int PORT;

    @Value("${spring.data.mongodb.database}")
    public String DATABASE;

    // Create a MongoClient bean with SSL enabled
    @Bean
    public MongoClient mongoClient() {

        // Create a MongoCredential object with username, database and password
        //MongoCredential credential = MongoCredential.createCredential("", DATABASE, "".toCharArray());

        // Create a MongoClientSettings object with SSL settings and credential
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToSslSettings(builder -> {
                    try {
                        builder.enabled(true).invalidHostNameAllowed(true).context(sslContextInitializer.initSslContext()); // Enable SSL
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
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
