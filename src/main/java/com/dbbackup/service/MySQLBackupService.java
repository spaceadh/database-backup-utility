package com.dbbackup.service;

import com.dbbackup.model.BackupConfig;
import com.dbbackup.model.BackupResult;
import com.dbbackup.model.DatabaseType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * MySQL database backup service using mysqldump
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MySQLBackupService implements BackupService {

    private final CompressionService compressionService;

    @Override
    public BackupResult backup(BackupConfig config) {
        long startTime = System.currentTimeMillis();
        log.info("Starting MySQL backup for database: {}", config.getDatabaseName());

        try {
            // Create backup directory if it doesn't exist
            File backupDir = new File(config.getBackupPath());
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            // Generate backup filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = String.format("%s_%s_mysql.sql", config.getDatabaseName(), timestamp);
            String backupFilePath = config.getBackupPath() + File.separator + backupFileName;

            // Build mysqldump command
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "mysqldump",
                    "--host=" + config.getHost(),
                    "--port=" + config.getPort(),
                    "--user=" + config.getUsername(),
                    "--password=" + config.getPassword(),
                    "--result-file=" + backupFilePath,
                    "--single-transaction",
                    "--routines",
                    "--triggers",
                    config.getDatabaseName()
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Read output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("mysqldump output: {}", line);
                }
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                return BackupResult.builder()
                        .success(false)
                        .message("MySQL backup failed with exit code: " + exitCode)
                        .timestamp(LocalDateTime.now())
                        .durationMillis(System.currentTimeMillis() - startTime)
                        .build();
            }

            // Compress if requested
            if (config.isCompress()) {
                backupFilePath = compressionService.compressFile(backupFilePath);
            }

            long fileSize = Files.size(Path.of(backupFilePath));
            long duration = System.currentTimeMillis() - startTime;

            log.info("MySQL backup completed successfully: {}", backupFilePath);

            return BackupResult.builder()
                    .success(true)
                    .message("MySQL backup completed successfully")
                    .backupFilePath(backupFilePath)
                    .fileSizeBytes(fileSize)
                    .timestamp(LocalDateTime.now())
                    .durationMillis(duration)
                    .build();

        } catch (Exception e) {
            log.error("Error during MySQL backup", e);
            return BackupResult.builder()
                    .success(false)
                    .message("MySQL backup failed: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .durationMillis(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    @Override
    public boolean supports(BackupConfig config) {
        return config.getDatabaseType() == DatabaseType.MYSQL || 
               config.getDatabaseType() == DatabaseType.MARIADB;
    }
}
