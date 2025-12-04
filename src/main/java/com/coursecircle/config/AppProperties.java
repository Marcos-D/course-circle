package com.coursecircle.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Binds custom configuration under the coursecircle.* namespace so services can rely on typed values
 * instead of scattered magic numbers or paths.
 */
@Configuration
@ConfigurationProperties(prefix = "coursecircle")
public class AppProperties {

    private final SessionsProperties sessions = new SessionsProperties();
    private final FilesProperties files = new FilesProperties();

    public SessionsProperties getSessions() {
        return sessions;
    }

    public FilesProperties getFiles() {
        return files;
    }

    public static class SessionsProperties {
        private int maxActivePerUser = 1;

        public int getMaxActivePerUser() {
            return maxActivePerUser;
        }

        public void setMaxActivePerUser(int maxActivePerUser) {
            this.maxActivePerUser = maxActivePerUser;
        }
    }

    public static class FilesProperties {
        private String storageDir = "./uploads";

        public String getStorageDir() {
            return storageDir;
        }

        public void setStorageDir(String storageDir) {
            this.storageDir = storageDir;
        }
    }
}
