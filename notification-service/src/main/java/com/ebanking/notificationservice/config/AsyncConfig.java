package com.ebanking.notificationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration pour le traitement asynchrone et les retries
 *
 * FONCTIONNALITÉS :
 * - @EnableAsync : Active l'exécution asynchrone (envoi en masse)
 * - @EnableRetry : Active les retries automatiques en cas d'échec
 * - ThreadPool configuré pour gérer les tâches asynchrones
 */
@Configuration
@EnableAsync
@EnableRetry
public class AsyncConfig {

    /**
     * Configure le ThreadPool pour les tâches asynchrones
     *
     * UTILISATION :
     * - Envoi de notifications en masse
     * - Traitement d'événements Kafka
     * - Tâches en arrière-plan
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Nombre de threads minimum
        executor.setCorePoolSize(5);

        // Nombre de threads maximum
        executor.setMaxPoolSize(10);

        // Taille de la queue de tâches en attente
        executor.setQueueCapacity(100);

        // Préfixe du nom des threads
        executor.setThreadNamePrefix("notification-async-");

        // Initialisation
        executor.initialize();

        return executor;
    }
}