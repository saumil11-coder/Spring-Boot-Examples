package com.howtodoinjava.app.config;

import com.howtodoinjava.app.model.GroceryItem;
import com.howtodoinjava.app.repositories.ItemRepository;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import static java.util.Collections.singletonList;

@Configuration
@EnableMongoRepositories(basePackageClasses = ItemRepository.class, mongoTemplateRef = "primaryMongoTemplate")
@EnableConfigurationProperties
public class MongoDbConfig {

    @Bean(name = "primaryMongoProperties")
    @ConfigurationProperties(prefix = "spring.data.mongodb")
    @Primary
    public MongoProperties primaryProperties() {
        return new MongoProperties();
    }

    @Bean(name = "primaryMongoClient")
    public MongoClient mongoClient(@Qualifier("primaryMongoProperties") MongoProperties mongoProperties) {
    MongoCredential credential = null;
        if (mongoProperties.getUsername() != null && mongoProperties.getPassword() != null) {
        credential  = MongoCredential.createCredential(
                    mongoProperties.getUsername(), mongoProperties.getAuthenticationDatabase(), mongoProperties.getPassword());
        }

        return MongoClients.create(MongoClientSettings.builder()
          .applyToClusterSettings(builder -> builder.hosts(singletonList(
                  new ServerAddress(mongoProperties.getHost(), mongoProperties.getPort()))))
          //.credential(credential)
          .build());
    }

    @Primary
    @Bean(name = "primaryMongoDBFactory")
    public MongoDatabaseFactory mongoDatabaseFactory(@Qualifier("primaryMongoClient") MongoClient mongoClient,
                                                     @Qualifier("primaryMongoProperties") MongoProperties mongoProperties) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, mongoProperties.getDatabase());
    }

    @Primary
    @Bean(name = "primaryMongoTemplate")
    public MongoTemplate mongoTemplate(@Qualifier("primaryMongoDBFactory") MongoDatabaseFactory mongoDatabaseFactory) {
        return new MongoTemplate(mongoDatabaseFactory);
    }
}