package com.dbbackup.service;

import com.dbbackup.model.BackupConfig;
import com.dbbackup.model.BackupResult;
import com.dbbackup.model.DatabaseType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * PostgreSQL database backup service using pg_dump
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PostgreSQLBackupService implements BackupService {

    private final CompressionService compressionService;
    
    @Value("${backup.executables.pg-dump:pg_dump}")
    private String pgDumpPath;

    @Override
    public BackupResult backup(BackupConfig config) {
        long startTime = System.currentTimeMillis();
        log.info("Starting PostgreSQL backup for database: {}", config.getDatabaseName());

        try {
            // Create backup directory if it doesn't exist
            File backupDir = new File(config.getBackupPath());
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            // Generate backup filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = String.format("%s_%s_postgresql.sql", config.getDatabaseName(), timestamp);
            String backupFilePath = config.getBackupPath() + File.separator + backupFileName;

            // Build pg_dump command
            String executable = (pgDumpPath == null || pgDumpPath.isEmpty()) ? "pg_dump" : pgDumpPath;
            log.debug("Using PostgreSQL executable: {}", executable);
            
            ProcessBuilder processBuilder = new ProcessBuilder(
                    executable,
                    "--host=" + config.getHost(),
                    "--port=" + config.getPort(),
                    "--username=" + config.getUsername(),
                    "--dbname=" + config.getDatabaseName(),
                    "--file=" + backupFilePath,
                    "--format=plain",
                    "--verbose"
            );

            // Set PGPASSWORD environment variable
            Map<String, String> env = new HashMap<>(processBuilder.environment());
            env.put("PGPASSWORD", config.getPassword());
            processBuilder.environment().putAll(env);

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Read output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("pg_dump output: {}", line);
                }
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                return BackupResult.builder()
                        .success(false)
                        .message("PostgreSQL backup failed with exit code: " + exitCode)
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

            log.info("PostgreSQL backup completed successfully: {}", backupFilePath);

            return BackupResult.builder()
                    .success(true)
                    .message("PostgreSQL backup completed successfully")
                    .backupFilePath(backupFilePath)
                    .fileSizeBytes(fileSize)
                    .timestamp(LocalDateTime.now())
                    .durationMillis(duration)
                    .build();

        } catch (Exception e) {
            log.error("Error during PostgreSQL backup", e);
            return BackupResult.builder()
                    .success(false)
                    .message("PostgreSQL backup failed: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .durationMillis(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    @Override
    public boolean supports(BackupConfig config) {
        return config.getDatabaseType() == DatabaseType.POSTGRESQL;
    }
}
