package com.educonnect.service;

import com.educonnect.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service managing physical storage, validation, security checks, and streaming of educational files.
 */
@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    public static final long MAX_FILE_SIZE_BYTES = 25 * 1024 * 1024; // 25 MB

    public static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".pdf", ".docx", ".doc", ".pptx", ".ppt", ".zip", ".txt"
    );

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${app.upload.dir:uploads/resources}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.fileStorageLocation);
            logger.info("File storage directory initialized at: {}", this.fileStorageLocation);
        } catch (IOException ex) {
            throw new FileStorageException("Could not create the storage directory for uploaded files.", ex);
        }
    }

    /**
     * Validates and securely stores an uploaded multipart file to disk.
     */
    public StoredFile storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Cannot store empty file.");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new FileStorageException("File exceeds maximum allowed size of 25 MB.");
        }

        String rawFilename = file.getOriginalFilename();
        if (rawFilename == null || rawFilename.trim().isEmpty()) {
            rawFilename = "unnamed_resource.pdf";
        }

        String cleanFilename = StringUtils.cleanPath(rawFilename);

        // Security check: Guard against directory traversal attacks
        if (cleanFilename.contains("..")) {
            throw new FileStorageException("Filename contains invalid path sequence: " + cleanFilename);
        }

        // Validate file extension against allowlist
        int lastDotIndex = cleanFilename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            throw new FileStorageException("File must have a valid extension (.pdf, .docx, .pptx, .zip, .txt)");
        }
        String extension = cleanFilename.substring(lastDotIndex).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new FileStorageException("Unsupported file extension: " + extension + ". Allowed formats: " + ALLOWED_EXTENSIONS);
        }

        // Generate collision-resistant unique filename
        String storageFilename = UUID.randomUUID().toString() + "_" + cleanFilename.replaceAll("[^a-zA-Z0-9._-]", "_");

        try {
            Path targetLocation = this.fileStorageLocation.resolve(storageFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            String fileType = extension.startsWith(".") ? extension.substring(1).toUpperCase() : extension.toUpperCase();

            logger.info("Stored file: original='{}', stored='{}', size={} bytes", cleanFilename, storageFilename, file.getSize());

            return new StoredFile(cleanFilename, targetLocation.toString(), fileType, file.getSize());
        } catch (IOException ex) {
            throw new FileStorageException("Failed to store file: " + cleanFilename, ex);
        }
    }

    /**
     * Loads a stored file as a Spring Resource for HTTP download streaming.
     */
    public Resource loadFileAsResource(String fileLocation) {
        try {
            Path filePath = Paths.get(fileLocation).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("File not found or not readable at: " + fileLocation);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("Malformed file path: " + fileLocation, ex);
        }
    }

    /**
     * Deletes a stored file from disk.
     */
    public void deleteFile(String fileLocation) {
        if (fileLocation == null || fileLocation.trim().isEmpty()) {
            return;
        }
        try {
            Path path = Paths.get(fileLocation).normalize();
            Files.deleteIfExists(path);
            logger.info("Deleted stored file at: {}", path);
        } catch (IOException ex) {
            logger.warn("Could not delete file at {}: {}", fileLocation, ex.getMessage());
        }
    }

    /**
     * Result record encapsulating stored file metadata.
     */
    public static class StoredFile {
        private final String originalFilename;
        private final String fileLocation;
        private final String fileType;
        private final long fileSize;

        public StoredFile(String originalFilename, String fileLocation, String fileType, long fileSize) {
            this.originalFilename = originalFilename;
            this.fileLocation = fileLocation;
            this.fileType = fileType;
            this.fileSize = fileSize;
        }

        public String getOriginalFilename() {
            return originalFilename;
        }

        public String getFileLocation() {
            return fileLocation;
        }

        public String getFileType() {
            return fileType;
        }

        public long getFileSize() {
            return fileSize;
        }
    }
}
