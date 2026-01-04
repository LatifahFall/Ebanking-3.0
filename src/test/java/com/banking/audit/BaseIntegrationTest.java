// ========================================
// BaseIntegrationTest.java
// ========================================
package com.banking.audit;

import com.banking.audit.repository.AuditEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.banking.audit.AuditServiceApplication;

@SpringBootTest(classes = AuditServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    // =============================
    // PostgreSQL Testcontainer
    // =============================
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("audit_test_db")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("schema-test.sql");

    // =============================
    // Kafka Testcontainer
    // =============================
    @Container
    public static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    // =============================
    // Elasticsearch Testcontainer
    // =============================
    @Container
    static ElasticsearchContainer elasticsearch = new ElasticsearchContainer(
            DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.11.0")
                    .asCompatibleSubstituteFor("docker.elastic.co/elasticsearch/elasticsearch"))
            .withEnv("xpack.security.enabled", "false")
            .withEnv("ES_JAVA_OPTS", "-Xms256m -Xmx256m");

    static {
        try {
            // Démarrage des containers
            postgres.start();
            kafka.start();
            elasticsearch.start();

            // Injection des propriétés Spring Boot
            System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
            System.setProperty("spring.datasource.username", postgres.getUsername());
            System.setProperty("spring.datasource.password", postgres.getPassword());
            System.setProperty("spring.kafka.bootstrap-servers", kafka.getBootstrapServers());
            System.setProperty("spring.elasticsearch.uris", elasticsearch.getHttpHostAddress());

            // Schema Hibernate par défaut
            System.setProperty("spring.jpa.properties.hibernate.default_schema", "audit");

        } catch (Throwable t) {
            // Fallback H2 si Docker/Testcontainers indisponible
            System.err.println("[TEST-CONTAINERS] Docker not available, using H2 fallback: " + t.getMessage());

            System.setProperty("spring.datasource.url",
                    "jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;" +
                            "INIT=CREATE SCHEMA IF NOT EXISTS audit\\;SET SCHEMA audit");
            System.setProperty("spring.datasource.username", "sa");
            System.setProperty("spring.datasource.password", "");

            System.setProperty("spring.kafka.bootstrap-servers", "localhost:9092");
            System.setProperty("spring.elasticsearch.uris", "http://localhost:9200");
            System.setProperty("spring.jpa.properties.hibernate.default_schema", "audit");
        }
    }

    // =============================
    // Injection du repository pour nettoyage automatique
    // =============================
    @Autowired
    protected AuditEventRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }
}
