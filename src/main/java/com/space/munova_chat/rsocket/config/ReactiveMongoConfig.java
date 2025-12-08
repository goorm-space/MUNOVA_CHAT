package com.space.munova_chat.rsocket.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;

@Configuration
@RequiredArgsConstructor
public class ReactiveMongoConfig {

    private static final String DB_NAME = "munova-msg-db";
    @Value("${spring.mongodb.uri}")
    private String mongoUri;

    @NotNull
    protected String getDatabaseName() {
        return DB_NAME;
    }

    public MongoClient reactiveMongoClient() {
        ConnectionString connectionString = new ConnectionString(mongoUri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        return MongoClients.create(settings);
    }

    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate() {
        return new ReactiveMongoTemplate(
                new SimpleReactiveMongoDatabaseFactory(reactiveMongoClient(), DB_NAME)
        );
    }

}
