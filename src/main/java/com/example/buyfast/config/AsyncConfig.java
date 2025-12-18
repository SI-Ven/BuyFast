package com.example.buyfast.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);      // Processes 5 images at the same time
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        // FIXED: Changed setThreadPrefixName to setThreadNamePrefix
        executor.setThreadNamePrefix("ES-Sync-");
        executor.initialize();
        return executor;
    }
}