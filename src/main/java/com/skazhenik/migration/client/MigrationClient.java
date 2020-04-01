package com.skazhenik.migration.client;

import com.skazhenik.migration.exception.MigrationException;
import com.skazhenik.migration.loader.ParallelLoader;
import com.skazhenik.migration.service.NewStorageService;
import com.skazhenik.migration.service.OldStorageService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.skazhenik.migration.util.FileManager.createTempDir;
import static com.skazhenik.migration.util.FileManager.deleteTempDir;
import static com.skazhenik.migration.util.MigrationManager.*;

public class MigrationClient {
    private static final Path temporaryDirLocation = Path.of("..");
    private static final int MAX_LOCAL_FILE_COUNT = 100;
    private final OldStorageService oldStorageService = new OldStorageService();
    private final NewStorageService newStorageService = new NewStorageService();

    private void migrate(final Path tempDir) throws MigrationException {
        List<String> oldFiles = getFilesList(oldStorageService);
        try (ParallelLoader parallelLoader = new ParallelLoader(MAX_LOCAL_FILE_COUNT,
                tempDir, oldStorageService, newStorageService);) {
            parallelLoader.load(oldFiles);
        } catch (ExecutionException e) {
            throw new MigrationException("Loading files failed", e);
        }
        System.err.println("migrate");
    }

    private void run() {
        Path tempDir;
        try {
            tempDir = createTempDir(temporaryDirLocation);
        } catch (IOException e) {
            System.err.println("Unable to create temporary directory " + e.getMessage());
            return;
        }
        try {
            migrate(tempDir);
        } catch (MigrationException e) {
            System.err.println("Migration failed with msg: " + e.getMessage());
        } finally {
            try {
                deleteTempDir(tempDir);
            } catch (IOException e) {
                System.err.println("Unable to delete temporary directory: " + tempDir);
            }
        }
    }

    public static void main(String[] args) {
        new MigrationClient().run();
    }

}
