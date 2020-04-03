package com.skazhenik.migration;

import com.skazhenik.migration.exception.MigrationException;
import com.skazhenik.migration.service.NewStorageService;
import com.skazhenik.migration.service.OldStorageService;
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

public class MigrationUtilsTest extends BaseTest {
    @Test
    public void loadNewStorageTest() {
        Path tempDir1 = createDir();
        Path tempDir2 = createDir();
        Objects.requireNonNull(tempDir1);
        Objects.requireNonNull(tempDir2);

        NewStorageService newStorageService = new NewStorageService();
        for (int i = 1; i < SMALL_TEST_SIZE; i++) {
            final String name = "loadNewStorageTestFile" + i + ".txt";
            final Path file = tempDir1.resolve(name);
            generateRandomFile(file, i);
            try {
                MigrationUtils.uploadFile(newStorageService, new File(file.toString()), name);
            } catch (MigrationException e) {
                Assert.fail("Uploading failed " + e.getMessage());
            }
            try {
                MigrationUtils.downloadFile(newStorageService, tempDir2, name);
            } catch (MigrationException e) {
                Assert.fail("Downloading failed " + e.getMessage());
            }
            try {
                Assert.assertEquals(
                        FileUtils.readFileToString(new File(file.toString()), StandardCharsets.UTF_8),
                        FileUtils.readFileToString(new File(tempDir2.resolve(name).toString()), StandardCharsets.UTF_8)
                );
            } catch (IOException e) {
                Assert.fail("Files comparing failed " + e.getMessage());
            }
        }

        deleteDir(tempDir1);
        deleteDir(tempDir2);
    }

    @Test
    public void getFilesUploadNewStorageTest() {
        Path tempDir = createDir();
        Objects.requireNonNull(tempDir);

        Set<String> names = new HashSet<>();
        NewStorageService newStorageService = new NewStorageService();
        for (int i = 1; i < SMALL_TEST_SIZE; i++) {
            final String name = "getFilesUploadNewStorageTestFile" + i + ".txt";
            final Path file = tempDir.resolve(name);
            generateRandomFile(file, i);
            names.add(name);
            try {
                MigrationUtils.uploadFile(newStorageService, new File(file.toString()), name);
            } catch (MigrationException ignored) {
            }
        }

        try {
            Assert.assertTrue((new HashSet<>(MigrationUtils.getFilesList(newStorageService))).containsAll(names));
        } catch (MigrationException e) {
            Assert.fail("getFilesList failed " + e.getMessage());
        }

        deleteDir(tempDir);
    }

    @Test
    public void getFilesDeleteOldStorageTest() {
        OldStorageService oldStorageService = new OldStorageService();
        Set<String> oldStorageNames = new HashSet<>();
        try {
            oldStorageNames.addAll(MigrationUtils.getFilesList(oldStorageService));
        } catch (MigrationException e) {
            Assert.fail("getFilesList from old storage failed " + e.getMessage());
        }
        int i = 0;
        for (Iterator<String> it = oldStorageNames.iterator(); it.hasNext(); ) {
            try {
                MigrationUtils.deleteFile(oldStorageService, it.next());
                it.remove();
            } catch (MigrationException ignored) {
            }
            if (i++ > SMALL_TEST_SIZE) {
                break;
            }
        }

        try {
            Assert.assertEquals(oldStorageNames, new HashSet<>(MigrationUtils.getFilesList(oldStorageService)));
        } catch (MigrationException e) {
            Assert.fail("getFilesList failed " + e.getMessage());
        }
    }
}
