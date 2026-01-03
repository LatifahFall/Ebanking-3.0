package com.ebanking.payment.client.azure;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Client pour Azure Face API (gratuit jusqu'à 30 000 transactions/mois)
 * Documentation: https://learn.microsoft.com/en-us/azure/ai-services/computer-vision/
 */
@Component
@Slf4j
public class AzureFaceApiClient {

    private final WebClient webClient;
    private final String subscriptionKey;
    private final String endpoint;
    private final boolean enabled;

    public AzureFaceApiClient(
            @Value("${biometric.azure.face.api.key:}") String subscriptionKey,
            @Value("${biometric.azure.face.api.endpoint:}") String endpoint,
            @Value("${biometric.azure.face.api.enabled:false}") boolean enabled) {
        this.subscriptionKey = subscriptionKey;
        this.endpoint = endpoint;
        this.enabled = enabled;

        if (enabled && (subscriptionKey == null || subscriptionKey.isEmpty() || endpoint == null || endpoint.isEmpty())) {
            log.warn("Azure Face API is enabled but credentials are missing. Biometric verification will use fallback mode.");
            this.webClient = null;
        } else {
            this.webClient = enabled ? WebClient.builder()
                    .baseUrl(endpoint)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader("Ocp-Apim-Subscription-Key", subscriptionKey)
                    .build() : null;
        }
    }

    /**
     * Détecte un visage dans une image et retourne son faceId
     */
    public Mono<String> detectFace(String imageBase64) {
        if (!enabled || webClient == null) {
            return Mono.error(new AzureFaceApiException("Azure Face API is not enabled or configured"));
        }

        log.debug("Detecting face in image using Azure Face API");

        return webClient.post()
                .uri("/face/v1.0/detect?returnFaceId=true&returnFaceAttributes=")
                .body(BodyInserters.fromValue(new FaceDetectRequest(imageBase64)))
                .retrieve()
                .bodyToFlux(FaceDetectResponse.class)
                .collectList()
                .flatMap(faces -> {
                    if (faces.isEmpty()) {
                        return Mono.error(new AzureFaceApiException("No face detected in image"));
                    }
                    if (faces.size() > 1) {
                        log.warn("Multiple faces detected, using the first one");
                    }
                    return Mono.just(faces.get(0).getFaceId());
                })
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.backoff(2, Duration.ofMillis(500))
                        .filter(throwable -> throwable instanceof WebClientResponseException
                                && ((WebClientResponseException) throwable).getStatusCode().is5xxServerError()))
                .doOnError(error -> log.error("Error detecting face with Azure Face API", error))
                .onErrorMap(WebClientResponseException.class, this::mapException);
    }

    /**
     * Vérifie si deux visages correspondent (compare deux faceIds)
     */
    public Mono<Boolean> verifyFace(String faceId1, String faceId2) {
        if (!enabled || webClient == null) {
            return Mono.error(new AzureFaceApiException("Azure Face API is not enabled or configured"));
        }

        log.debug("Verifying faces match using Azure Face API: {} vs {}", faceId1, faceId2);

        return webClient.post()
                .uri("/face/v1.0/verify")
                .body(BodyInserters.fromValue(new FaceVerifyRequest(faceId1, faceId2)))
                .retrieve()
                .bodyToMono(FaceVerifyResponse.class)
                .map(response -> {
                    // Azure retourne un score de confiance entre 0 et 1
                    // On considère qu'un score >= 0.5 est une correspondance
                    boolean isIdentical = response.isIdentical() && response.getConfidence() >= 0.5;
                    log.debug("Face verification result: identical={}, confidence={}", 
                            response.isIdentical(), response.getConfidence());
                    return isIdentical;
                })
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.backoff(2, Duration.ofMillis(500))
                        .filter(throwable -> throwable instanceof WebClientResponseException
                                && ((WebClientResponseException) throwable).getStatusCode().is5xxServerError()))
                .doOnError(error -> log.error("Error verifying face with Azure Face API", error))
                .onErrorMap(WebClientResponseException.class, this::mapException);
    }

    /**
     * Vérifie un visage en comparant un faceId avec un template stocké (base64)
     * Cette méthode détecte d'abord le visage dans le template, puis compare
     */
    public Mono<Boolean> verifyFaceWithTemplate(String faceId, String templateBase64) {
        if (!enabled || webClient == null) {
            return Mono.error(new AzureFaceApiException("Azure Face API is not enabled or configured"));
        }

        // Détecter le visage dans le template
        return detectFace(templateBase64)
                .flatMap(templateFaceId -> verifyFace(faceId, templateFaceId))
                .onErrorResume(error -> {
                    log.error("Error verifying face with template", error);
                    return Mono.just(false);
                });
    }

    private RuntimeException mapException(WebClientResponseException ex) {
        String errorMessage = "Azure Face API error: " + ex.getStatusCode() + " - " + ex.getMessage();
        log.error(errorMessage);
        
        if (ex.getStatusCode().value() == 401 || ex.getStatusCode().value() == 403) {
            return new AzureFaceApiException("Azure Face API authentication failed. Check your API key.", ex);
        } else if (ex.getStatusCode().value() == 429) {
            return new AzureFaceApiException("Azure Face API rate limit exceeded. Free tier: 30,000 transactions/month.", ex);
        } else if (ex.getStatusCode().is5xxServerError()) {
            return new AzureFaceApiException("Azure Face API server error", ex);
        } else {
            return new AzureFaceApiException(errorMessage, ex);
        }
    }

    public boolean isEnabled() {
        return enabled && webClient != null;
    }

    // DTOs pour les requêtes/réponses Azure Face API

    @Data
    private static class FaceDetectRequest {
        @JsonProperty("url")
        private String url;

        public FaceDetectRequest(String imageBase64) {
            // Azure accepte soit une URL, soit directement l'image en base64
            // Pour base64, on utilise le format data URI
            this.url = "data:image/jpeg;base64," + imageBase64;
        }
    }

    @Data
    private static class FaceDetectResponse {
        @JsonProperty("faceId")
        private String faceId;

        @JsonProperty("faceRectangle")
        private FaceRectangle faceRectangle;
    }

    @Data
    private static class FaceRectangle {
        @JsonProperty("top")
        private int top;

        @JsonProperty("left")
        private int left;

        @JsonProperty("width")
        private int width;

        @JsonProperty("height")
        private int height;
    }

    @Data
    private static class FaceVerifyRequest {
        @JsonProperty("faceId1")
        private String faceId1;

        @JsonProperty("faceId2")
        private String faceId2;

        public FaceVerifyRequest(String faceId1, String faceId2) {
            this.faceId1 = faceId1;
            this.faceId2 = faceId2;
        }
    }

    @Data
    private static class FaceVerifyResponse {
        @JsonProperty("isIdentical")
        private boolean identical;

        @JsonProperty("confidence")
        private double confidence;
    }

    public static class AzureFaceApiException extends RuntimeException {
        public AzureFaceApiException(String message) {
            super(message);
        }

        public AzureFaceApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

