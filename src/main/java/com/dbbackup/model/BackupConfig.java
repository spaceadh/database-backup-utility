package com.dbbackup.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration model for database backup operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupConfig {
    private DatabaseType databaseType;
    private String host;
    private int port;
    private String databaseName;
    private String username;
    private String password;
    private String backupPath;
    private boolean compress;
    private StorageType storageType;
    private String cloudBucket;
    private String cloudRegion;
    private String cloudAccessKey;
    private String cloudSecretKey;
}
