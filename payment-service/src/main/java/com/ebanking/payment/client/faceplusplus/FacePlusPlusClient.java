package com.ebanking.payment.client.faceplusplus;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Client pour Face++ API (Megvii) - COMPLÈTEMENT GRATUIT
 * - 30 000 appels/mois gratuits
 * - Pas besoin de carte de crédit
 * - Inscription gratuite : https://www.faceplusplus.com/
 * Documentation: https://www.faceplusplus.com/api-doc/
 */
@Component
@Slf4j
public class FacePlusPlusClient {

    private final WebClient webClient;
    private final String apiKey;
    private final String apiSecret;
    private final boolean enabled;
    private static final String BASE_URL = "https://api-us.faceplusplus.com";

    public FacePlusPlusClient(
            @Value("${biometric.faceplusplus.api.key:}") String apiKey,
            @Value("${biometric.faceplusplus.api.secret:}") String apiSecret,
            @Value("${biometric.faceplusplus.api.enabled:false}") boolean enabled) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.enabled = enabled;

        if (enabled && (apiKey == null || apiKey.isEmpty() || apiSecret == null || apiSecret.isEmpty())) {
            log.warn("Face++ API is enabled but credentials are missing. Biometric verification will use fallback mode.");
            this.webClient = null;
        } else {
            this.webClient = enabled ? WebClient.builder()
                    .baseUrl(BASE_URL)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .build() : null;
        }
    }

    /**
     * Détecte un visage dans une image et retourne son face_token
     */
    public Mono<String> detectFace(String imageBase64) {
        if (!enabled || webClient == null) {
            return Mono.error(new FacePlusPlusException("Face++ API is not enabled or configured"));
        }

        log.debug("Detecting face in image using Face++ API");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("api_key", apiKey);
        formData.add("api_secret", apiSecret);
        formData.add("image_base64", imageBase64);
        formData.add("return_landmark", "0");
        formData.add("return_attributes", "none");

        return webClient.post()
                .uri("/facepp/v3/detect")
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(FaceDetectResponse.class)
                .flatMap(response -> {
                    if (response.getFaces() == null || response.getFaces().isEmpty()) {
                        return Mono.error(new FacePlusPlusException("No face detected in image"));
                    }
                    if (response.getFaces().size() > 1) {
                        log.warn("Multiple faces detected, using the first one");
                    }
                    String faceToken = response.getFaces().get(0).getFaceToken();
                    if (faceToken == null || faceToken.isEmpty()) {
                        return Mono.error(new FacePlusPlusException("Face token is empty"));
                    }
                    return Mono.just(faceToken);
                })
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.backoff(2, Duration.ofMillis(500))
                        .filter(throwable -> throwable instanceof WebClientResponseException
                                && ((WebClientResponseException) throwable).getStatusCode().is5xxServerError()))
                .doOnError(error -> log.error("Error detecting face with Face++ API", error))
                .onErrorMap(WebClientResponseException.class, this::mapException);
    }

    /**
     * Compare deux visages (face_token) et retourne la similarité
     */
    public Mono<Boolean> compareFaces(String faceToken1, String faceToken2) {
        if (!enabled || webClient == null) {
            return Mono.error(new FacePlusPlusException("Face++ API is not enabled or configured"));
        }

        log.debug("Comparing faces using Face++ API: {} vs {}", faceToken1, faceToken2);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("api_key", apiKey);
        formData.add("api_secret", apiSecret);
        formData.add("face_token1", faceToken1);
        formData.add("face_token2", faceToken2);

        return webClient.post()
                .uri("/facepp/v3/compare")
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(FaceCompareResponse.class)
                .map(response -> {
                    // Face++ retourne un score de confiance entre 0 et 100
                    // On considère qu'un score >= 70 est une correspondance
                    double confidence = response.getConfidence();
                    boolean isMatch = confidence >= 70.0;
                    log.debug("Face comparison result: confidence={}, isMatch={}", confidence, isMatch);
                    return isMatch;
                })
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.backoff(2, Duration.ofMillis(500))
                        .filter(throwable -> throwable instanceof WebClientResponseException
                                && ((WebClientResponseException) throwable).getStatusCode().is5xxServerError()))
                .doOnError(error -> log.error("Error comparing faces with Face++ API", error))
                .onErrorMap(WebClientResponseException.class, this::mapException);
    }

    /**
     * Vérifie un visage en comparant un face_token avec un template stocké (base64)
     */
    public Mono<Boolean> verifyFaceWithTemplate(String faceToken, String templateBase64) {
        if (!enabled || webClient == null) {
            return Mono.error(new FacePlusPlusException("Face++ API is not enabled or configured"));
        }

        // Détecter le visage dans le template
        return detectFace(templateBase64)
                .flatMap(templateFaceToken -> compareFaces(faceToken, templateFaceToken))
                .onErrorResume(error -> {
                    log.error("Error verifying face with template", error);
                    return Mono.just(false);
                });
    }

    private RuntimeException mapException(WebClientResponseException ex) {
        String errorMessage = "Face++ API error: " + ex.getStatusCode() + " - " + ex.getMessage();
        log.error(errorMessage);
        
        if (ex.getStatusCode().value() == 401 || ex.getStatusCode().value() == 403) {
            return new FacePlusPlusException("Face++ API authentication failed. Check your API key and secret.", ex);
        } else if (ex.getStatusCode().value() == 429) {
            return new FacePlusPlusException("Face++ API rate limit exceeded. Free tier: 30,000 calls/month.", ex);
        } else if (ex.getStatusCode().is5xxServerError()) {
            return new FacePlusPlusException("Face++ API server error", ex);
        } else {
            return new FacePlusPlusException(errorMessage, ex);
        }
    }

    public boolean isEnabled() {
        return enabled && webClient != null;
    }

    // DTOs pour les réponses Face++ API

    @Data
    private static class FaceDetectResponse {
        @JsonProperty("faces")
        private java.util.List<Face> faces;

        @JsonProperty("image_id")
        private String imageId;

        @JsonProperty("request_id")
        private String requestId;

        @JsonProperty("time_used")
        private Integer timeUsed;
    }

    @Data
    private static class Face {
        @JsonProperty("face_token")
        private String faceToken;

        @JsonProperty("face_rectangle")
        private FaceRectangle faceRectangle;
    }

    @Data
    private static class FaceRectangle {
        @JsonProperty("top")
        private Integer top;

        @JsonProperty("left")
        private Integer left;

        @JsonProperty("width")
        private Integer width;

        @JsonProperty("height")
        private Integer height;
    }

    @Data
    private static class FaceCompareResponse {
        @JsonProperty("confidence")
        private Double confidence;

        @JsonProperty("thresholds")
        private Thresholds thresholds;

        @JsonProperty("request_id")
        private String requestId;

        @JsonProperty("time_used")
        private Integer timeUsed;
    }

    @Data
    private static class Thresholds {
        @JsonProperty("1e-3")
        private Double threshold1e3;

        @JsonProperty("1e-4")
        private Double threshold1e4;

        @JsonProperty("1e-5")
        private Double threshold1e5;
    }

    public static class FacePlusPlusException extends RuntimeException {
        public FacePlusPlusException(String message) {
            super(message);
        }

        public FacePlusPlusException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

