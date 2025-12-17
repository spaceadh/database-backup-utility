package com.dbbackup.service;

import com.dbbackup.model.BackupConfig;
import com.dbbackup.model.BackupResult;

/**
 * Interface for database backup operations
 */
public interface BackupService {
    /**
     * Performs a backup of the database according to the provided configuration
     *
     * @param config the backup configuration
     * @return the result of the backup operation
     */
    BackupResult backup(BackupConfig config);

    /**
     * Checks if this service supports the given database type
     *
     * @param config the backup configuration
     * @return true if supported, false otherwise
     */
    boolean supports(BackupConfig config);
}
