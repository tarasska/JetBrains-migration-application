package com.skazhenik.migration.util;

import com.skazhenik.migration.exception.MigrationException;
import com.skazhenik.migration.exception.ServiceException;
import com.skazhenik.migration.service.AbstractStorageService;
import org.apache.http.HttpStatus;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * Provides methods of interacting with a server with multiple attempts to obtain a result in case of failure.
 */
public class MigrationUtils {
    private static final int UnsuccessfulRequestCount = 100;

    /**
     * Method attempts to get a list of files in the storage.
     *
     * @param service service for interacting with storage
     * @return {@link List} of of file names from the storage
     * @throws MigrationException if it was not possible to get a
     *                            successful response in {@link #UnsuccessfulRequestCount} attempts
     */
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

    /**
     * Method attempts to download file from the storage and save it in {@code tempDir}.
     *
     * @param service  service for interacting with storage
     * @param tempDir  directory for storing temporary data
     * @param fileName file name to download
     * @throws MigrationException if it was not possible to get a
     *                            successful response in {@link #UnsuccessfulRequestCount} attempts
     */
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

    /**
     * Method attempts to upload file to the storage. If the return code matches {@link HttpStatus#SC_CONFLICT}
     * then the method considers it as an existing file and tries to overwrite it. In case of an erroneous assumption,
     * it throws an exception with a full description.
     *
     * @param service  service for interacting with storage
     * @param file     file to upload
     * @param fileName file to upload
     * @throws MigrationException if it was not possible to get a
     *                            successful response in {@link #UnsuccessfulRequestCount} attempts
     */
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

    /**
     * Method attempts to delete file from the storage. {@link HttpStatus#SC_NOT_FOUND} is not considered
     * a mistake and is regarded as the successful execution of the method.
     *
     * @param service  service for interacting with storage
     * @param fileName file name to delete
     * @throws MigrationException if it was not possible to get a
     *                            successful response in {@link #UnsuccessfulRequestCount} attempts
     */
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
