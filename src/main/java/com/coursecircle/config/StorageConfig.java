package com.coursecircle.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Ensures the configured file storage directory exists so uploads can succeed from the start
 * without relying on implicit defaults.
 */
@Configuration
public class StorageConfig {

    private static final Logger log = LoggerFactory.getLogger(StorageConfig.class);

    private final AppProperties appProperties;

    public StorageConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @PostConstruct
    public void ensureStorageDirectoryExists() {
        Path storagePath = Path.of(appProperties.getFiles().getStorageDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(storagePath);
            log.info("Using file storage directory: {}", storagePath);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create storage directory: " + storagePath, e);
        }
    }
}
