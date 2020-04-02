package com.skazhenik.migration;

import com.skazhenik.migration.exception.MigrationException;
import com.skazhenik.migration.service.NewStorageService;
import com.skazhenik.migration.util.MigrationUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class StorageServiceTest {
    private final Path tempLocation = Path.of("..");
    private final Random random = new Random();
    private final int ALPHABET_SIZE = 255;
    private final int SMALL_TEST_SIZE = 100;


    private void generateRandomFile(final Path file, final int size) {
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            for (int i = 0; i < size; i++) {
                writer.write(random.nextInt(ALPHABET_SIZE));
            }
        } catch (IOException e) {
            System.err.println("Unable to create random file:" + e.getMessage());
        }
    }

    private Path createDir() {
        try {
            return Files.createTempDirectory(tempLocation, "temp");
        } catch (IOException e) {
            return null;
        }
    }

    @Test
    public void loadNewStorageTest() {
        Path tempDir1 = createDir();
        Path tempDir2 = createDir();
        Objects.requireNonNull(tempDir1);
        Objects.requireNonNull(tempDir2);

        NewStorageService newStorageService = new NewStorageService();
        for (int i = 1; i < SMALL_TEST_SIZE; i++) {
            final String name = "file" + i + ".txt";
            final Path file = tempDir1.resolve(name);
            generateRandomFile(file, i);
            try {
                MigrationUtils.uploadFile(newStorageService, new File(file.toString()), name);
            } catch (MigrationException e) {
                Assert.fail("Uploading failed");
            }
            try {
                MigrationUtils.downloadFile(newStorageService, tempDir2, name);
            } catch (MigrationException e) {
                Assert.fail("Downloading failed");
            }
            try {
                Assert.assertEquals(
                        FileUtils.readFileToString(new File(file.toString()), StandardCharsets.UTF_8),
                        FileUtils.readFileToString(new File(tempDir2.resolve(name).toString()), StandardCharsets.UTF_8)
                );
            } catch (IOException e) {
                Assert.fail("Files comparing failed");
            }
        }
    }

    @Test
    public void getFilesNewStorageTest() {
        Path tempDir = createDir();
        Objects.requireNonNull(tempDir);

        Set<String> names = new HashSet<>();
        NewStorageService newStorageService = new NewStorageService();
        for (int i = 1; i < SMALL_TEST_SIZE; i++) {
            final String name = "file" + i + ".txt";
            final Path file = tempDir.resolve(name);
            generateRandomFile(file, i);
            names.add(name);
            try {
                MigrationUtils.uploadFile(newStorageService, new File(file.toString()), name);
            } catch (MigrationException ignored) {
            }
        }

        try {
            Assert.assertEquals(names, new HashSet<>(MigrationUtils.getFilesList(newStorageService)));
        } catch (MigrationException e) {
            Assert.fail("getFilesList failed");
        }
    }
}
