package com.dbbackup.service;

import com.dbbackup.model.BackupConfig;
import com.dbbackup.model.BackupResult;
import com.dbbackup.model.StorageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orchestrates the backup process, coordinating between backup services and storage
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BackupOrchestrator {

    private final List<BackupService> backupServices;
    private final StorageService storageService;
    private final BackupLogService logService;

    /**
     * Executes a complete backup operation
     *
     * @param config the backup configuration
     * @return the result of the backup operation
     */
    public BackupResult executeBackup(BackupConfig config) {
        log.info("Starting backup operation for database: {} ({})", 
                config.getDatabaseName(), config.getDatabaseType());

        // Log the backup attempt
        logService.logBackupAttempt(config);

        // Find the appropriate backup service
        BackupService backupService = backupServices.stream()
                .filter(service -> service.supports(config))
                .findFirst()
                .orElse(null);

        if (backupService == null) {
            String message = "No backup service found for database type: " + config.getDatabaseType();
            log.error(message);
            BackupResult result = BackupResult.builder()
                    .success(false)
                    .message(message)
                    .build();
            logService.logBackupResult(config, result);
            return result;
        }

        // Perform the backup
        BackupResult result = backupService.backup(config);

        // Log the result
        logService.logBackupResult(config, result);

        // If backup was successful and cloud storage is configured, upload to cloud
        if (result.isSuccess() && config.getStorageType() != StorageType.LOCAL) {
            try {
                log.info("Uploading backup to cloud storage: {}", config.getStorageType());
                storageService.uploadToCloud(result.getBackupFilePath(), config);
                log.info("Cloud upload completed successfully");
            } catch (Exception e) {
                log.error("Failed to upload backup to cloud storage", e);
                // Don't fail the entire backup if cloud upload fails
                result.setMessage(result.getMessage() + " (Cloud upload failed: " + e.getMessage() + ")");
            }
        }

        return result;
    }
}
