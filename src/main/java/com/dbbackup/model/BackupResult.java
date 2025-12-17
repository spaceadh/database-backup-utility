package com.dbbackup.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Result of a backup operation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupResult {
    private boolean success;
    private String message;
    private String backupFilePath;
    private long fileSizeBytes;
    private LocalDateTime timestamp;
    private long durationMillis;
}
