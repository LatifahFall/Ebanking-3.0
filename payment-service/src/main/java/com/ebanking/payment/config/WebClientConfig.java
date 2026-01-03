package com.ebanking.payment.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${payment.service.account.service.url}")
    private String accountServiceUrl;

    @Value("${payment.service.account.service.timeout.connect:5000}")
    private int connectTimeout;

    @Value("${payment.service.account.service.timeout.read:10000}")
    private int readTimeout;

    @Bean(name = "accountServiceWebClient")
    public WebClient accountServiceWebClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(readTimeout))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);

        return WebClient.builder()
                .baseUrl(accountServiceUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    // Note: Azure Face API WebClient est créé directement dans AzureFaceApiClient
    // car il nécessite des headers spécifiques (Ocp-Apim-Subscription-Key)
}

