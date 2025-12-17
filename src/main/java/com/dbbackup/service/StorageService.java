package com.dbbackup.service;

import com.dbbackup.model.BackupConfig;
import com.dbbackup.model.StorageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.nio.file.Path;

/**
 * Service for managing backup storage (local and cloud)
 */
@Service
@Slf4j
public class StorageService {

    /**
     * Uploads a backup file to cloud storage
     *
     * @param filePath the path to the backup file
     * @param config   the backup configuration containing cloud credentials
     */
    public void uploadToCloud(String filePath, BackupConfig config) {
        if (config.getStorageType() == StorageType.AWS_S3) {
            uploadToS3(filePath, config);
        } else if (config.getStorageType() == StorageType.AZURE_BLOB) {
            log.warn("Azure Blob Storage not yet implemented");
            // TODO: Implement Azure Blob Storage upload
        } else if (config.getStorageType() == StorageType.GOOGLE_CLOUD) {
            log.warn("Google Cloud Storage not yet implemented");
            // TODO: Implement Google Cloud Storage upload
        }
    }

    /**
     * Uploads a file to AWS S3
     *
     * @param filePath the path to the file
     * @param config   the backup configuration
     */
    private void uploadToS3(String filePath, BackupConfig config) {
        log.info("Uploading to AWS S3 bucket: {}", config.getCloudBucket());

        try {
            // Create AWS credentials
            AwsBasicCredentials credentials = AwsBasicCredentials.create(
                    config.getCloudAccessKey(),
                    config.getCloudSecretKey()
            );

            // Create S3 client
            S3Client s3Client = S3Client.builder()
                    .region(Region.of(config.getCloudRegion()))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();

            // Get file name from path
            File file = new File(filePath);
            String key = file.getName();

            // Upload file to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(config.getCloudBucket())
                    .key(key)
                    .build();

            s3Client.putObject(putObjectRequest, Path.of(filePath));

            log.info("Successfully uploaded to S3: {}/{}", config.getCloudBucket(), key);

        } catch (Exception e) {
            log.error("Failed to upload to S3", e);
            throw new RuntimeException("S3 upload failed: " + e.getMessage(), e);
        }
    }

    /**
     * Lists backup files in the local backup directory
     *
     * @param backupPath the backup directory path
     * @return array of backup file names
     */
    public String[] listLocalBackups(String backupPath) {
        File backupDir = new File(backupPath);
        if (!backupDir.exists() || !backupDir.isDirectory()) {
            return new String[0];
        }
        return backupDir.list();
    }
}
