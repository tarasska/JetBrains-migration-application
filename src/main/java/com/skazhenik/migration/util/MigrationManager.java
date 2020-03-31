package com.skazhenik.migration.util;

import com.skazhenik.migration.exception.MigrationException;
import com.skazhenik.migration.exception.ServiceException;
import com.skazhenik.migration.service.AbstractStorageService;

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
}
