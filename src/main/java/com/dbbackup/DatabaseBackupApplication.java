package com.dbbackup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Database Backup Utility
 * A Spring Boot CLI application for backing up various database systems
 */
@SpringBootApplication
@EnableScheduling
public class DatabaseBackupApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatabaseBackupApplication.class, args);
    }
}
