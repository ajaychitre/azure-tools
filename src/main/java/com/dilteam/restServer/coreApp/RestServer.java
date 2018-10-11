package com.dilteam.restServer.coreApp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.web.bind.annotation.CrossOrigin;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.IOException;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.dilteam"})
@PropertySources({@PropertySource(value = "classpath:application.properties")})
@CrossOrigin(allowedHeaders = "*", allowCredentials = "true")
public class RestServer {

    @Value("${authConsumerKey}")
    private String authConsumerKey;

    @Value("${authConsumerSecret}")
    private String authConsumerSecret;

    @Value("${authAccessToken}")
    private String authAccessToken;

    @Value("${authAccessTokenSecret}")
    private String authAccessTokenSecret;

    @Value("${namespace}")
    private String namespace;

    @Value("${eventHubName}")
    private String eventHubName;

    @Value("${sasKeyName}")
    private String sasKeyName;

    @Value("${sasKey}")
    private String sasKey;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(RestServer.class);
    }

    @Bean
    public ConfigurationBuilder getConfigurationBuilder() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(authConsumerKey)
                .setOAuthConsumerSecret(authConsumerSecret)
                .setOAuthAccessToken(authAccessToken)
                .setOAuthAccessTokenSecret(authAccessTokenSecret);
        cb.setJSONStoreEnabled(true);
        return cb;
    }

    @Bean
    public TweetsPublisher getTweetsPublisher() throws IOException {
        TweetsPublisher publisher = new TweetsPublisher(namespace, eventHubName, sasKeyName, sasKey);
        return publisher;
    }

    @Bean
    public TwitterStream getTwitterStream() throws IOException {
        TwitterStream twitterStream = new TwitterStreamFactory(getConfigurationBuilder().build()).getInstance();
        twitterStream.addListener(getTweetsPublisher());
        twitterStream.sample();
        return twitterStream;
    }
}
