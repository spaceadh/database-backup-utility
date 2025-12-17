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
 * MongoDB database backup service using mongodump
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MongoDBBackupService implements BackupService {

    private final CompressionService compressionService;

    @Override
    public BackupResult backup(BackupConfig config) {
        long startTime = System.currentTimeMillis();
        log.info("Starting MongoDB backup for database: {}", config.getDatabaseName());

        try {
            // Create backup directory if it doesn't exist
            File backupDir = new File(config.getBackupPath());
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            // Generate backup directory with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupDirName = String.format("%s_%s_mongodb", config.getDatabaseName(), timestamp);
            String backupDirPath = config.getBackupPath() + File.separator + backupDirName;

            // Build mongodump command
            String connectionString = String.format("mongodb://%s:%s@%s:%d/%s",
                    config.getUsername(),
                    config.getPassword(),
                    config.getHost(),
                    config.getPort(),
                    config.getDatabaseName());

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "mongodump",
                    "--uri=" + connectionString,
                    "--out=" + backupDirPath
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Read output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("mongodump output: {}", line);
                }
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                return BackupResult.builder()
                        .success(false)
                        .message("MongoDB backup failed with exit code: " + exitCode)
                        .timestamp(LocalDateTime.now())
                        .durationMillis(System.currentTimeMillis() - startTime)
                        .build();
            }

            String finalPath = backupDirPath;

            // Compress if requested (archive the entire directory)
            if (config.isCompress()) {
                // Create a tar.gz of the backup directory
                String archivePath = backupDirPath + ".tar.gz";
                finalPath = archiveBackupDirectory(backupDirPath, archivePath);
            }

            long fileSize = getDirectorySize(Path.of(finalPath));
            long duration = System.currentTimeMillis() - startTime;

            log.info("MongoDB backup completed successfully: {}", finalPath);

            return BackupResult.builder()
                    .success(true)
                    .message("MongoDB backup completed successfully")
                    .backupFilePath(finalPath)
                    .fileSizeBytes(fileSize)
                    .timestamp(LocalDateTime.now())
                    .durationMillis(duration)
                    .build();

        } catch (Exception e) {
            log.error("Error during MongoDB backup", e);
            return BackupResult.builder()
                    .success(false)
                    .message("MongoDB backup failed: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .durationMillis(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    private String archiveBackupDirectory(String dirPath, String archivePath) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "tar",
                "-czf",
                archivePath,
                "-C",
                new File(dirPath).getParent(),
                new File(dirPath).getName()
        );

        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Failed to archive backup directory");
        }

        // Delete the original directory
        deleteDirectory(new File(dirPath));

        return archivePath;
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    private long getDirectorySize(Path path) throws Exception {
        File file = path.toFile();
        if (file.isFile()) {
            return file.length();
        }

        long size = 0;
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    size += f.length();
                } else {
                    size += getDirectorySize(f.toPath());
                }
            }
        }
        return size;
    }

    @Override
    public boolean supports(BackupConfig config) {
        return config.getDatabaseType() == DatabaseType.MONGODB;
    }
}
