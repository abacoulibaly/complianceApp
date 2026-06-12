package com.threasury.compliance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Configuration
public class AsyncConfig {

    @Bean
    Executor complianceVirtualExecutor(ComplianceProperties properties) {
        var semaphore = new Semaphore(properties.batch().maxConcurrency());
        return command -> Thread.startVirtualThread(() -> {
            try {
                semaphore.acquire();
                command.run();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                semaphore.release();
            }
        });
    }

    @Bean
    Executor batchQueueExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
