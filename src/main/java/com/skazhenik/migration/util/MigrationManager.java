package com.skazhenik.migration.util;

import com.skazhenik.migration.exception.MigrationException;
import com.skazhenik.migration.exception.ServiceException;
import com.skazhenik.migration.service.AbstractStorageService;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class MigrationManager {
    private static final int UnsuccessfulRequestCount = 100;

    public static List<String> getFilesList(final AbstractStorageService service) throws MigrationException {
        int remainingAttempts = UnsuccessfulRequestCount;
        while (remainingAttempts > 0) {
            try {
                return service.getFilesList();
            } catch (ServiceException e) {
                System.err.println("Log `getFilesList` failed: " + e.getMessage());
                remainingAttempts--;
            }
        }
        throw new MigrationException("Waiting too long for the correct response to the file list request");
    }

    public static void downloadFile(final AbstractStorageService service,
                                    final Path tempDir,
                                    final String fileName) throws MigrationException {
        int remainingAttempts = UnsuccessfulRequestCount;
        while (remainingAttempts > 0) {
            try {
                service.download(tempDir, fileName);
                return;
            } catch (ServiceException e) {
                System.err.println("Log `download` (file: " + fileName + ") failed: " + e.getMessage());
                remainingAttempts--;
            }
        }
        throw new MigrationException("Waiting too long for the correct response to the file download request");
    }

    public static void uploadFile(final AbstractStorageService service,
                                  final File file,
                                  final String fileName) throws MigrationException {
        int remainingAttempts = UnsuccessfulRequestCount;
        while (remainingAttempts > 0) {
            try {
                service.upload(file);
                return;
            } catch (ServiceException e) {
                System.err.println("Log `upload` (file: " + fileName + ") failed: " + e.getMessage());
                remainingAttempts--;
            }
        }
        throw new MigrationException("Waiting too long for the correct response to the file upload request");
    }
}
