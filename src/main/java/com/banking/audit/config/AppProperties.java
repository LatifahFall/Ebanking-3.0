// ========================================
// AppProperties.java
// ========================================
package com.banking.audit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "audit")
@Data
public class AppProperties {

    private Map<String, String> topics;
    private Retention retention;
    private Compliance compliance;
    private Security security;

    @Data
    public static class Retention {
        private int hotDays;
        private int warmDays;
        private int coldDays;
    }

    @Data
    public static class Compliance {
        private boolean rgpdEnabled;
        private boolean psd2Enabled;
        private int autoAnonymizeAfterDays;
    }

    @Data
    public static class Security {
        private String checksumAlgorithm;
        private boolean encryptionEnabled;
    }
}