package com.dbbackup.command;

import com.dbbackup.model.BackupConfig;
import com.dbbackup.model.BackupResult;
import com.dbbackup.model.DatabaseType;
import com.dbbackup.model.StorageType;
import com.dbbackup.service.BackupOrchestrator;
import com.dbbackup.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Spring Shell commands for database backup operations
 */
@ShellComponent
@Slf4j
@RequiredArgsConstructor
public class BackupCommands {

    private final BackupOrchestrator backupOrchestrator;
    private final SchedulerService schedulerService;

    @ShellMethod(value = "Backup a MySQL database", key = "backup-mysql")
    public String backupMySQL(
            @ShellOption(help = "Database host") String host,
            @ShellOption(help = "Database port", defaultValue = "3306") int port,
            @ShellOption(help = "Database name") String database,
            @ShellOption(help = "Username") String username,
            @ShellOption(help = "Password") String password,
            @ShellOption(help = "Backup directory path", defaultValue = "./backups") String backupPath,
            @ShellOption(help = "Compress backup", defaultValue = "true") boolean compress) {

        BackupConfig config = BackupConfig.builder()
                .databaseType(DatabaseType.MYSQL)
                .host(host)
                .port(port)
                .databaseName(database)
                .username(username)
                .password(password)
                .backupPath(backupPath)
                .compress(compress)
                .storageType(StorageType.LOCAL)
                .build();

        BackupResult result = backupOrchestrator.executeBackup(config);
        return formatResult(result);
    }

    @ShellMethod(value = "Backup a PostgreSQL database", key = "backup-postgresql")
    public String backupPostgreSQL(
            @ShellOption(help = "Database host") String host,
            @ShellOption(help = "Database port", defaultValue = "5432") int port,
            @ShellOption(help = "Database name") String database,
            @ShellOption(help = "Username") String username,
            @ShellOption(help = "Password") String password,
            @ShellOption(help = "Backup directory path", defaultValue = "./backups") String backupPath,
            @ShellOption(help = "Compress backup", defaultValue = "true") boolean compress) {

        BackupConfig config = BackupConfig.builder()
                .databaseType(DatabaseType.POSTGRESQL)
                .host(host)
                .port(port)
                .databaseName(database)
                .username(username)
                .password(password)
                .backupPath(backupPath)
                .compress(compress)
                .storageType(StorageType.LOCAL)
                .build();

        BackupResult result = backupOrchestrator.executeBackup(config);
        return formatResult(result);
    }

    @ShellMethod(value = "Backup a MongoDB database", key = "backup-mongodb")
    public String backupMongoDB(
            @ShellOption(help = "Database host") String host,
            @ShellOption(help = "Database port", defaultValue = "27017") int port,
            @ShellOption(help = "Database name") String database,
            @ShellOption(help = "Username") String username,
            @ShellOption(help = "Password") String password,
            @ShellOption(help = "Backup directory path", defaultValue = "./backups") String backupPath,
            @ShellOption(help = "Compress backup", defaultValue = "true") boolean compress) {

        BackupConfig config = BackupConfig.builder()
                .databaseType(DatabaseType.MONGODB)
                .host(host)
                .port(port)
                .databaseName(database)
                .username(username)
                .password(password)
                .backupPath(backupPath)
                .compress(compress)
                .storageType(StorageType.LOCAL)
                .build();

        BackupResult result = backupOrchestrator.executeBackup(config);
        return formatResult(result);
    }

    @ShellMethod(value = "Backup a SQLite database", key = "backup-sqlite")
    public String backupSQLite(
            @ShellOption(help = "SQLite database file path") String databaseFile,
            @ShellOption(help = "Backup directory path", defaultValue = "./backups") String backupPath,
            @ShellOption(help = "Compress backup", defaultValue = "true") boolean compress) {

        BackupConfig config = BackupConfig.builder()
                .databaseType(DatabaseType.SQLITE)
                .databaseName(databaseFile)
                .backupPath(backupPath)
                .compress(compress)
                .storageType(StorageType.LOCAL)
                .build();

        BackupResult result = backupOrchestrator.executeBackup(config);
        return formatResult(result);
    }

    @ShellMethod(value = "Backup with cloud storage (AWS S3)", key = "backup-to-s3")
    public String backupToS3(
            @ShellOption(help = "Database type (MYSQL, POSTGRESQL, MONGODB, SQLITE)") String dbType,
            @ShellOption(help = "Database host") String host,
            @ShellOption(help = "Database port") int port,
            @ShellOption(help = "Database name") String database,
            @ShellOption(help = "Username") String username,
            @ShellOption(help = "Password") String password,
            @ShellOption(help = "Backup directory path", defaultValue = "./backups") String backupPath,
            @ShellOption(help = "S3 bucket name") String bucket,
            @ShellOption(help = "AWS region", defaultValue = "us-east-1") String region,
            @ShellOption(help = "AWS access key") String accessKey,
            @ShellOption(help = "AWS secret key") String secretKey,
            @ShellOption(help = "Compress backup", defaultValue = "true") boolean compress) {

        BackupConfig config = BackupConfig.builder()
                .databaseType(DatabaseType.valueOf(dbType.toUpperCase()))
                .host(host)
                .port(port)
                .databaseName(database)
                .username(username)
                .password(password)
                .backupPath(backupPath)
                .compress(compress)
                .storageType(StorageType.AWS_S3)
                .cloudBucket(bucket)
                .cloudRegion(region)
                .cloudAccessKey(accessKey)
                .cloudSecretKey(secretKey)
                .build();

        BackupResult result = backupOrchestrator.executeBackup(config);
        return formatResult(result);
    }

    @ShellMethod(value = "Schedule automatic backups", key = "schedule-backup")
    public String scheduleBackup(
            @ShellOption(help = "Database type (MYSQL, POSTGRESQL, MONGODB, SQLITE)") String dbType,
            @ShellOption(help = "Database host") String host,
            @ShellOption(help = "Database port") int port,
            @ShellOption(help = "Database name") String database,
            @ShellOption(help = "Username") String username,
            @ShellOption(help = "Password") String password,
            @ShellOption(help = "Backup directory path", defaultValue = "./backups") String backupPath,
            @ShellOption(help = "Cron expression (e.g., '0 0 2 * * ?' for daily at 2 AM)") String cron,
            @ShellOption(help = "Compress backup", defaultValue = "true") boolean compress) {

        try {
            BackupConfig config = BackupConfig.builder()
                    .databaseType(DatabaseType.valueOf(dbType.toUpperCase()))
                    .host(host)
                    .port(port)
                    .databaseName(database)
                    .username(username)
                    .password(password)
                    .backupPath(backupPath)
                    .compress(compress)
                    .storageType(StorageType.LOCAL)
                    .build();

            schedulerService.scheduleBackup(config, cron);
            return String.format("✓ Backup scheduled successfully for database '%s' with cron: %s", database, cron);
        } catch (SchedulerException e) {
            log.error("Failed to schedule backup", e);
            return "✗ Failed to schedule backup: " + e.getMessage();
        }
    }

    @ShellMethod(value = "Cancel a scheduled backup", key = "cancel-schedule")
    public String cancelSchedule(@ShellOption(help = "Database name") String database) {
        try {
            schedulerService.cancelScheduledBackup(database);
            return String.format("✓ Cancelled scheduled backup for database '%s'", database);
        } catch (SchedulerException e) {
            log.error("Failed to cancel scheduled backup", e);
            return "✗ Failed to cancel scheduled backup: " + e.getMessage();
        }
    }

    @ShellMethod(value = "List all scheduled backups", key = "list-schedules")
    public String listSchedules() {
        try {
            schedulerService.listScheduledBackups();
            return "✓ Check logs for scheduled backups list";
        } catch (SchedulerException e) {
            log.error("Failed to list scheduled backups", e);
            return "✗ Failed to list scheduled backups: " + e.getMessage();
        }
    }

    private String formatResult(BackupResult result) {
        if (result.isSuccess()) {
            return String.format("""
                    ✓ Backup completed successfully!
                    File: %s
                    Size: %.2f MB
                    Duration: %.2f seconds
                    """,
                    result.getBackupFilePath(),
                    result.getFileSizeBytes() / (1024.0 * 1024.0),
                    result.getDurationMillis() / 1000.0);
        } else {
            return String.format("✗ Backup failed: %s", result.getMessage());
        }
    }
}
