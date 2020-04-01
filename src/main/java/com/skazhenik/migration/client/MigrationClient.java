package com.skazhenik.migration.client;

import com.skazhenik.migration.exception.MigrationException;
import com.skazhenik.migration.loader.ParallelLoader;
import com.skazhenik.migration.service.NewStorageService;
import com.skazhenik.migration.service.OldStorageService;
import com.skazhenik.migration.util.HttpFileLoader;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

import static com.skazhenik.migration.util.FileManager.createTempDir;
import static com.skazhenik.migration.util.FileManager.deleteTempDir;
import static com.skazhenik.migration.util.MigrationManager.getFilesList;

public class MigrationClient {
    private static final Path temporaryDirLocation = Path.of("..");
    private final OldStorageService oldStorageService = new OldStorageService();
    private final NewStorageService newStorageService = new NewStorageService();

    private void migrate(final Path tempDir) throws MigrationException {
        List<String> oldFiles = getFilesList(oldStorageService);
        ParallelLoader parallelLoader = new ParallelLoader(5, 5,
                tempDir, oldStorageService, newStorageService);
        parallelLoader.load(oldFiles);
        List<String> newFiles = getFilesList(newStorageService);
    }

    private void run() {
        Path tempDir;
        try {
            tempDir = createTempDir(temporaryDirLocation);
        } catch (IOException e) {
            System.err.println("Migration failed with msg: " + e.getMessage());
            return;
        }
        try {
            migrate(tempDir);
            deleteTempDir(tempDir);
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
