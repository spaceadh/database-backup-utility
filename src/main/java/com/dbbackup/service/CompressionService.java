package com.dbbackup.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Service for compressing backup files
 */
@Service
@Slf4j
public class CompressionService {

    /**
     * Compresses a file using GZIP
     *
     * @param sourceFile the file to compress
     * @return the path to the compressed file
     * @throws IOException if compression fails
     */
    public String compressFile(String sourceFile) throws IOException {
        String compressedFile = sourceFile + ".gz";
        log.info("Compressing file: {} to {}", sourceFile, compressedFile);

        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(compressedFile);
             GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(fos)) {

            byte[] buffer = new byte[8192];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                gzos.write(buffer, 0, len);
            }
        }

        // Delete the original uncompressed file
        Files.deleteIfExists(Path.of(sourceFile));
        log.info("Compression completed. Compressed file size: {} bytes", Files.size(Path.of(compressedFile)));

        return compressedFile;
    }

    /**
     * Compresses a file into a tar.gz archive
     *
     * @param sourceFile the file to compress
     * @return the path to the compressed archive
     * @throws IOException if compression fails
     */
    public String compressToTarGz(String sourceFile) throws IOException {
        String compressedFile = sourceFile + ".tar.gz";
        log.info("Compressing file to tar.gz: {} to {}", sourceFile, compressedFile);

        File source = new File(sourceFile);

        try (FileOutputStream fos = new FileOutputStream(compressedFile);
             GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(fos);
             TarArchiveOutputStream taos = new TarArchiveOutputStream(gzos)) {

            TarArchiveEntry entry = new TarArchiveEntry(source, source.getName());
            taos.putArchiveEntry(entry);

            try (FileInputStream fis = new FileInputStream(source)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    taos.write(buffer, 0, len);
                }
            }

            taos.closeArchiveEntry();
        }

        // Delete the original uncompressed file
        Files.deleteIfExists(Path.of(sourceFile));
        log.info("Tar.gz compression completed. Compressed file size: {} bytes", Files.size(Path.of(compressedFile)));

        return compressedFile;
    }
}
