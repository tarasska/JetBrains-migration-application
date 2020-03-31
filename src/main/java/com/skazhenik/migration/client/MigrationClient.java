package com.skazhenik.migration.client;

import com.skazhenik.migration.exception.MigrationException;
import com.skazhenik.migration.exception.ServiceException;
import com.skazhenik.migration.service.AbstractStorageService;
import com.skazhenik.migration.service.NewStorageService;
import com.skazhenik.migration.service.OldStorageService;
import com.skazhenik.migration.util.FileManager;
import com.skazhenik.migration.util.MigrationManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static com.skazhenik.migration.util.FileManager.*;
import static com.skazhenik.migration.util.MigrationManager.*;

public class MigrationClient {
    private static final Path temporaryDirLocation = Path.of("..");
    private final OldStorageService oldStorageService = new OldStorageService();
    private final NewStorageService newStorageService = new NewStorageService();

    private void migrate(final Path temporaryDir) throws MigrationException {
        List<String> oldFiles = getFilesList(oldStorageService);

    }

    private void run() {
        Path temporaryDir;
        try {
            temporaryDir = createTempDir(temporaryDirLocation);
        } catch (IOException e) {
            System.err.println("Migration failed with msg: " + e.getMessage());
            return;
        }
        try {
            migrate(temporaryDir);
            deleteTempDir(temporaryDir);
        } catch (MigrationException e) {
            System.err.println("Migration failed with msg: " + e.getMessage());
        } catch (IOException ignored) {
            System.err.println("Unable to delete temp dir");
        }
    }

    public static void main(String[] args) {
        new MigrationClient().run();
    }

}
