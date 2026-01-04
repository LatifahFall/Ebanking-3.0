package com.banking.audit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.lang.NonNull;

import java.time.Duration;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.banking.audit.repository.elasticsearch")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUri;

    @Value("${spring.elasticsearch.username:}")
    private String username;

    @Value("${spring.elasticsearch.password:}")
    private String password;

    @Override
    @NonNull
    public ClientConfiguration clientConfiguration() {
        // V11__create_audit_partitioning.sql. Clean the URI
        String cleanedHost = elasticsearchUri
                .replace("http://", "")
                .replace("https://", "")
                .replaceAll("/$", "");

        // 2. Start the builder
        ClientConfiguration.MaybeSecureClientConfigurationBuilder builder = ClientConfiguration.builder()
                .connectedTo(cleanedHost);

        // 3. Apply SSL FIRST (or before the terminal timeout settings)
        if (elasticsearchUri.startsWith("https://")) {
            builder.usingSsl();
        }

        // 4. Apply Authentication and Timeouts
        if (username != null && !username.isBlank()) {
            builder.withBasicAuth(username, password);
        }

        return builder.withConnectTimeout(Duration.ofSeconds(5))
                .withSocketTimeout(Duration.ofSeconds(3))
                .build();
    }
}