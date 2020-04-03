package com.skazhenik.migration.util;

import com.skazhenik.migration.exception.MigrationException;
import com.skazhenik.migration.exception.ServiceException;
import com.skazhenik.migration.service.AbstractStorageService;
import org.apache.http.HttpStatus;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class MigrationUtils {
    private static final int UnsuccessfulRequestCount = 100;

    public static List<String> getFilesList(final AbstractStorageService service) throws MigrationException {
        int remainingAttempts = UnsuccessfulRequestCount;
        while (remainingAttempts > 0) {
            try {
                return service.getFilesList();
            } catch (ServiceException e) {
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
                if (e.getResponseCode() == HttpStatus.SC_CONFLICT) {
                    try {
                        deleteFile(service, fileName);
                    } catch (MigrationException eDelete) {
                        e.addSuppressed(eDelete);
                        throw new MigrationException("Unable to upload file ", e);
                    }
                } else {
                    remainingAttempts--;
                }
            }
        }
        throw new MigrationException("Waiting too long for the correct response to the file upload request");
    }

    public static void deleteFile(final AbstractStorageService service,
                                  final String fileName) throws MigrationException {
        int remainingAttempts = UnsuccessfulRequestCount;
        while (remainingAttempts > 0) {
            try {
                service.delete(fileName);
                return;
            } catch (ServiceException e) {
                if (e.getResponseCode() == HttpStatus.SC_NOT_FOUND) {
                    return;
                } else {
                    remainingAttempts--;
                }
            }
        }
        throw new MigrationException("Waiting too long for the correct response to the file upload request");
    }
}
