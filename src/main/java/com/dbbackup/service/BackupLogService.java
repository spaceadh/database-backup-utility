package com.dbbackup.service;

import com.dbbackup.model.BackupConfig;
import com.dbbackup.model.BackupResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for logging backup activities
 */
@Service
@Slf4j
public class BackupLogService {

    private static final String LOG_FILE = "backup_log.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Logs a backup attempt
     *
     * @param config the backup configuration
     */
    public void logBackupAttempt(BackupConfig config) {
        String logMessage = String.format("[%s] Starting backup - Database: %s, Type: %s, Host: %s",
                LocalDateTime.now().format(FORMATTER),
                config.getDatabaseName(),
                config.getDatabaseType(),
                config.getHost());

        writeToLog(logMessage);
        log.info(logMessage);
    }

    /**
     * Logs the result of a backup operation
     *
     * @param config the backup configuration
     * @param result the backup result
     */
    public void logBackupResult(BackupConfig config, BackupResult result) {
        String status = result.isSuccess() ? "SUCCESS" : "FAILURE";
        String logMessage = String.format("[%s] Backup %s - Database: %s, File: %s, Size: %d bytes, Duration: %d ms, Message: %s",
                LocalDateTime.now().format(FORMATTER),
                status,
                config.getDatabaseName(),
                result.getBackupFilePath(),
                result.getFileSizeBytes(),
                result.getDurationMillis(),
                result.getMessage());

        writeToLog(logMessage);
        if (result.isSuccess()) {
            log.info(logMessage);
        } else {
            log.error(logMessage);
        }
    }

    /**
     * Writes a message to the log file
     *
     * @param message the message to log
     */
    private void writeToLog(String message) {
        try {
            File logFile = new File(LOG_FILE);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                writer.write(message);
                writer.newLine();
            }
        } catch (IOException e) {
            log.error("Failed to write to log file", e);
        }
    }

    /**
     * Logs a scheduled backup execution
     *
     * @param cronExpression the cron expression
     */
    public void logScheduledBackup(String cronExpression) {
        String logMessage = String.format("[%s] Scheduled backup executed - Cron: %s",
                LocalDateTime.now().format(FORMATTER),
                cronExpression);

        writeToLog(logMessage);
        log.info(logMessage);
    }
}
