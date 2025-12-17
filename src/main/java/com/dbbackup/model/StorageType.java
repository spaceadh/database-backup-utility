package com.dbbackup.model;

/**
 * Supported storage types for backups
 */
public enum StorageType {
    LOCAL,
    AWS_S3,
    AZURE_BLOB,
    GOOGLE_CLOUD
}
