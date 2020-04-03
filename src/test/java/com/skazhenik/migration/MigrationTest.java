package com.skazhenik.migration;

import com.skazhenik.migration.exception.MigrationException;
import com.skazhenik.migration.loader.ParallelMigrationManager;
import com.skazhenik.migration.service.NewStorageService;
import com.skazhenik.migration.service.OldStorageService;
import com.skazhenik.migration.util.MigrationUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MigrationTest extends BaseTest {
    @Test
    public void mainTest() {
        OldStorageService oldStorageService = new OldStorageService();
        NewStorageService newStorageService = new NewStorageService();
        try {
            List<String> oldNames = MigrationUtils.getFilesList(oldStorageService);
            final int size = Math.min(oldNames.size(), SMALL_TEST_SIZE);
            try (ParallelMigrationManager manager = new ParallelMigrationManager(
                    10, tempLocation, oldStorageService, newStorageService
            )) {
                manager.load(oldNames.subList(0, size), 50);
            }
            List<String> newNames = MigrationUtils.getFilesList(newStorageService);
            for (final String name : oldNames.subList(0, size)) {
                Assert.assertTrue(newNames.contains(name));
            }
            Path dirNew = createDir();
            Path dirOld = createDir();
            Objects.requireNonNull(dirNew);
            Objects.requireNonNull(dirOld);

            for (final String name : oldNames.subList(0, size)) {
                MigrationUtils.downloadFile(newStorageService, dirNew, name);
                MigrationUtils.downloadFile(oldStorageService, dirOld, name);
                try {
                    Assert.assertEquals(
                            FileUtils.readFileToString(
                                    new File(dirNew.resolve(name).toString()),
                                    StandardCharsets.UTF_8
                            ),
                            FileUtils.readFileToString(
                                    new File(dirOld.resolve(name).toString()),
                                    StandardCharsets.UTF_8
                            )
                    );
                } catch (IOException e) {
                    Assert.fail("Files comparing failed " + e.getMessage());
                }
            }
            for (final String name : oldNames.subList(0, size)) {
                MigrationUtils.deleteFile(oldStorageService, name);
            }

            Set<String> refreshedOldNames = new HashSet<>(MigrationUtils.getFilesList(oldStorageService));
            for (final String name : oldNames.subList(0, size)) {
                Assert.assertFalse(refreshedOldNames.contains(name));
            }

            deleteDir(dirNew);
            deleteDir(dirOld);
        } catch (MigrationException | ExecutionException e) {
            Assert.fail("Unexpected error // something bad happened with server: " + e.getMessage());
        }
    }
}
