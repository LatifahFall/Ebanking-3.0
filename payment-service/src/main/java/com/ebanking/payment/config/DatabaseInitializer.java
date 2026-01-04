package com.ebanking.payment.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database Initializer
 * 
 * Automatically creates the database if it doesn't exist.
 * This runs before Flyway migrations, ensuring the database exists.
 * 
 * Can be disabled by setting: database.auto-create=false
 */
@Slf4j
@Component
@Order(1) // Execute before Flyway
@ConditionalOnProperty(name = "database.auto-create", havingValue = "true", matchIfMissing = true)
public class DatabaseInitializer implements ApplicationListener<ContextRefreshedEvent> {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            initializeDatabase();
        } catch (Exception e) {
            log.error("Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private void initializeDatabase() throws SQLException {
        // Extract database name from JDBC URL
        String dbName = extractDatabaseName(datasourceUrl);
        if (dbName == null) {
            log.warn("Could not extract database name from URL: {}. Skipping auto-creation.", datasourceUrl);
            return;
        }

        // Connect to default 'postgres' database to create our database
        String adminUrl = buildAdminUrl(datasourceUrl, dbName);
        
        log.info("Checking if database '{}' exists...", dbName);
        
        try (Connection conn = DriverManager.getConnection(adminUrl, username, password);
             Statement stmt = conn.createStatement()) {
            
            // Check if database exists
            boolean dbExists = checkDatabaseExists(stmt, dbName);
            
            if (!dbExists) {
                log.info("Database '{}' does not exist. Creating it...", dbName);
                createDatabase(stmt, dbName);
                log.info("Database '{}' created successfully", dbName);
            } else {
                log.info("Database '{}' already exists", dbName);
            }
            
            // Create extensions in the target database
            createExtensions(dbName);
            
        } catch (SQLException e) {
            log.error("Error during database initialization", e);
            throw e;
        }
    }

    private String extractDatabaseName(String jdbcUrl) {
        // Extract database name from: jdbc:postgresql://host:port/database
        try {
            int lastSlash = jdbcUrl.lastIndexOf('/');
            if (lastSlash == -1) return null;
            
            String dbPart = jdbcUrl.substring(lastSlash + 1);
            // Remove query parameters if any
            int questionMark = dbPart.indexOf('?');
            if (questionMark != -1) {
                dbPart = dbPart.substring(0, questionMark);
            }
            return dbPart;
        } catch (Exception e) {
            log.warn("Could not extract database name from URL: {}", jdbcUrl);
            return null;
        }
    }

    private String buildAdminUrl(String originalUrl, String dbName) {
        // Replace the database name with 'postgres' (default admin database)
        return originalUrl.replace("/" + dbName, "/postgres");
    }

    private boolean checkDatabaseExists(Statement stmt, String dbName) throws SQLException {
        String sql = "SELECT 1 FROM pg_database WHERE datname = '" + dbName + "'";
        try (ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next();
        }
    }

    private void createDatabase(Statement stmt, String dbName) throws SQLException {
        // PostgreSQL doesn't support CREATE DATABASE IF NOT EXISTS
        // So we use a DO block
        String sql = String.format(
            "DO $$ " +
            "BEGIN " +
            "  IF NOT EXISTS (SELECT FROM pg_database WHERE datname = '%s') THEN " +
            "    CREATE DATABASE %s WITH OWNER = %s ENCODING = 'UTF8' LC_COLLATE = 'en_US.utf8' LC_CTYPE = 'en_US.utf8' TEMPLATE = template0; " +
            "  END IF; " +
            "END $$",
            dbName, dbName, username
        );
        stmt.execute(sql);
    }

    private void createExtensions(String dbName) {
        // Connect to the target database to create extensions
        String targetUrl = buildAdminUrl(datasourceUrl, "postgres").replace("/postgres", "/" + dbName);
        
        try (Connection conn = DriverManager.getConnection(targetUrl, username, password);
             Statement stmt = conn.createStatement()) {
            
            log.info("Creating required extensions in database '{}'...", dbName);
            
            // Create UUID extension
            try {
                stmt.execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"");
                log.debug("Extension 'uuid-ossp' created or already exists");
            } catch (SQLException e) {
                log.warn("Could not create extension 'uuid-ossp': {}", e.getMessage());
            }
            
            // Create pgcrypto extension (optional, for cryptographic functions)
            try {
                stmt.execute("CREATE EXTENSION IF NOT EXISTS \"pgcrypto\"");
                log.debug("Extension 'pgcrypto' created or already exists");
            } catch (SQLException e) {
                log.warn("Could not create extension 'pgcrypto': {}", e.getMessage());
            }
            
        } catch (SQLException e) {
            log.warn("Could not create extensions in database '{}': {}", dbName, e.getMessage());
            // Don't fail if extensions can't be created, they might already exist or not be needed
        }
    }
}

