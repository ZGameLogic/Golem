package com.zgamelogic.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;

@Configuration
public class AwsConfig {
    private final String region;
    private final AwsBasicCredentials credentials;

    public AwsConfig(
        @Value("${aws.credentials.access-key}") String accessKey,
        @Value("${aws.credentials.secret-key}") String secretKey,
        @Value("${aws.region}") String region
    ) {
        this.region = region;
        credentials = AwsBasicCredentials.create(accessKey, secretKey);
    }

    @Bean
    public Route53Client route53Client() {
        return Route53Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build();
    }
}
