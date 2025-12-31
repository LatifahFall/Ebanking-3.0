package com.ebanking.payment.config;

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

    @Bean
    public WebClient accountServiceWebClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(readTimeout))
                .option(reactor.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);

        return WebClient.builder()
                .baseUrl(accountServiceUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}

