package com.matchwork.user_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@Configuration
public class AWSConfig {
    
    @Value("${aws.region}")
    private String region;
    

    @Bean
    public CognitoIdentityProviderClient cognitoClient(
    @Value("${aws.accessKeyId}") String key,
    @Value("${aws.secretKey}") String secret,
    @Value("${aws.region}") String region
    ) {
    AwsCredentialsProvider creds = StaticCredentialsProvider.create(
        AwsBasicCredentials.create(key, secret)
    );

    return CognitoIdentityProviderClient.builder()
        .credentialsProvider(creds)
        .region(Region.of(region))
        .build();
    }
}