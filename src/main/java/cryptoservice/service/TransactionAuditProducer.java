package cryptoservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import cryptoservice.model.CryptoTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TransactionAuditProducer {

    private static final Logger log = LoggerFactory.getLogger(TransactionAuditProducer.class);

    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.kafka.topic.transaction-audit:audit.topics.crypto-transaction}")
    private String topic;

    public void publish(CryptoTransaction transaction) {
        if (kafkaTemplate == null) {
            log.warn("Kafka not configured - skipping audit event for transaction: {}", transaction.getId());
            return;
        }
        
        try {
            Map<String, Object> payload = buildPayload(transaction);
            kafkaTemplate.send(topic, transaction.getWalletId().toString(), payload);
            log.info("Transaction audit event published for wallet: {}, transaction: {}", 
                    transaction.getWalletId(), transaction.getId());
        } catch (Exception ex) {
            log.warn("Failed to publish audit event for wallet {}: {}", 
                    transaction.getWalletId(), ex.getMessage());
        }
    }

    private Map<String, Object> buildPayload(CryptoTransaction transaction) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("transactionId", transaction.getId());
        payload.put("walletId", transaction.getWalletId());
        payload.put("type", transaction.getType());
        payload.put("symbol", transaction.getCryptoSymbol());
        payload.put("cryptoAmount", transaction.getCryptoAmount());
        payload.put("eurAmount", transaction.getEurAmount());
        payload.put("eurPricePerUnit", transaction.getEurPricePerUnit());
        payload.put("fee", transaction.getFee());
        payload.put("status", transaction.getStatus());
        payload.put("createdAt", transaction.getCreatedAt());
        return payload;
    }
}
