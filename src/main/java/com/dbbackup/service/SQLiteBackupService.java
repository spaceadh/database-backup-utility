package com.dbbackup.service;

import com.dbbackup.model.BackupConfig;
import com.dbbackup.model.BackupResult;
import com.dbbackup.model.DatabaseType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * SQLite database backup service using file copy
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SQLiteBackupService implements BackupService {

    private final CompressionService compressionService;

    @Override
    public BackupResult backup(BackupConfig config) {
        long startTime = System.currentTimeMillis();
        log.info("Starting SQLite backup for database: {}", config.getDatabaseName());

        try {
            // Create backup directory if it doesn't exist
            File backupDir = new File(config.getBackupPath());
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            // SQLite database is a file, so we just copy it
            File sourceFile = new File(config.getDatabaseName());
            if (!sourceFile.exists()) {
                return BackupResult.builder()
                        .success(false)
                        .message("SQLite database file not found: " + config.getDatabaseName())
                        .timestamp(LocalDateTime.now())
                        .durationMillis(System.currentTimeMillis() - startTime)
                        .build();
            }

            // Generate backup filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = String.format("%s_%s_sqlite.db", 
                    sourceFile.getName().replaceFirst("[.][^.]+$", ""), timestamp);
            String backupFilePath = config.getBackupPath() + File.separator + backupFileName;

            // Copy the SQLite database file
            Files.copy(sourceFile.toPath(), Path.of(backupFilePath), StandardCopyOption.REPLACE_EXISTING);

            // Compress if requested
            if (config.isCompress()) {
                backupFilePath = compressionService.compressFile(backupFilePath);
            }

            long fileSize = Files.size(Path.of(backupFilePath));
            long duration = System.currentTimeMillis() - startTime;

            log.info("SQLite backup completed successfully: {}", backupFilePath);

            return BackupResult.builder()
                    .success(true)
                    .message("SQLite backup completed successfully")
                    .backupFilePath(backupFilePath)
                    .fileSizeBytes(fileSize)
                    .timestamp(LocalDateTime.now())
                    .durationMillis(duration)
                    .build();

        } catch (Exception e) {
            log.error("Error during SQLite backup", e);
            return BackupResult.builder()
                    .success(false)
                    .message("SQLite backup failed: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .durationMillis(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    @Override
    public boolean supports(BackupConfig config) {
        return config.getDatabaseType() == DatabaseType.SQLITE;
    }
}
