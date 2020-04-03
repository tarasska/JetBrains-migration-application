package com.skazhenik.migration.client;

import com.skazhenik.migration.exception.MigrationException;
import com.skazhenik.migration.loader.ParallelMigrationManager;
import com.skazhenik.migration.service.NewStorageService;
import com.skazhenik.migration.service.OldStorageService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.skazhenik.migration.util.FileUtils.createTempDir;
import static com.skazhenik.migration.util.FileUtils.deleteTempDir;
import static com.skazhenik.migration.util.MigrationUtils.getFilesList;

/**
 * @author Skazhenik Taras
 * <p>
 * The main class that migrates data from the old storage to the new one.
 */
public class MigrationClient {
    private static final Path temporaryDirLocation = Path.of("..");
    private static final int MAX_THREAD_COUNT = 10;
    // fixes the stored number of files, does not allow to expand the local storage indefinitely
    private static final int MAX_LOAD_FACTOR = 50;
    private final OldStorageService oldStorageService = new OldStorageService();
    private final NewStorageService newStorageService = new NewStorageService();

    /**
     * Provides a complete migration cycle.
     * Download a list of old storage files. Creates a {@link ParallelMigrationManager} which transfers
     * files between storages using a locally fixed buffer for files in a temporary directory.
     * Deletes all files in the old storage if the migration is successful.
     *
     * @param tempDir directory for storing temporary data
     * @throws MigrationException if an error occurs during migration
     */
    private void migrate(final Path tempDir) throws MigrationException {
        List<String> oldFiles = getFilesList(oldStorageService);
        try (ParallelMigrationManager parallelMigrationManager = new ParallelMigrationManager(MAX_THREAD_COUNT,
                tempDir, oldStorageService, newStorageService)) {
            System.out.println("Transfer files...");
            parallelMigrationManager.load(oldFiles, MAX_LOAD_FACTOR);
            System.out.println("Delete old files...");
            parallelMigrationManager.delete(oldStorageService, oldFiles);
        } catch (ExecutionException e) {
            throw new MigrationException(e);
        }
    }

    /**
     * Creates a temporary directory and starts migration.
     * Cleans all temporary directory in finally block.
     */
    private void run() {
        Path tempDir;
        try {
            tempDir = createTempDir(temporaryDirLocation);
        } catch (IOException e) {
            System.err.println("Unable to create temporary directory " + e.getMessage());
            return;
        }
        try {
            System.out.println("Start migration...");
            migrate(tempDir);
            System.out.println("Migration completed successfully");
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

    /**
     * Run migration.
     *
     * @param args command line options not expected
     */
    public static void main(String[] args) {
        new MigrationClient().run();
    }
}
