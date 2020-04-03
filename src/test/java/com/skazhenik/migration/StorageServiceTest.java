package com.skazhenik.migration;

import com.skazhenik.migration.exception.ServiceException;
import com.skazhenik.migration.service.AbstractStorageService;
import com.skazhenik.migration.service.NewStorageService;
import com.skazhenik.migration.service.OldStorageService;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class StorageServiceTest extends BaseTest {

    private void uploadTest(final AbstractStorageService storageService) {
        Path tempDir = createDir();
        Objects.requireNonNull(tempDir);

        File file = new File("Alea jacta est + " + new Date().getTime() + ".txt");
        generateRandomFile(file.toPath(), 100);
        int attempts = 100;
        while (attempts > 0) {
            try {
                storageService.upload(file);
                break;
            } catch (ServiceException e) {
                if (e.getResponseCode() != HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                    Assert.fail("Unexpected error: " + e.getMessage());
                    break;
                }
            }
            attempts--;
        }
        Assert.assertTrue(attempts != 0);
        attempts = 100;
        while (attempts > 0) {
            try {
                storageService.upload(file);
                Assert.fail("Error expected");
            } catch (ServiceException e) {
                if (e.getResponseCode() != HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                    break;
                }
            }
            attempts--;
        }

        deleteDir(tempDir);
    }

    private List<String> getFiles(final AbstractStorageService service) {
        int attempts = 100;
        List<String> names = null;
        while (attempts > 0) {
            try {
                names = service.getFilesList();
                break;
            } catch (ServiceException e) {
                if (e.getResponseCode() != HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                    Assert.fail("Unexpected error" + e);
                }
            }
            attempts--;
        }
        return names;
    }


    private void deleteTest(final AbstractStorageService service) {
        List<String> names = getFiles(service);

        Assert.assertNotNull(names);
        Assert.assertFalse(names.isEmpty());

        int attempts = 100;
        while (attempts > 0) {
            try {
                service.delete(names.get(0));
                break;
            } catch (ServiceException e) {
                if (e.getResponseCode() != HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                    Assert.fail("Unexpected error" + e);
                }
            }
            attempts--;
        }
        List<String> newNames = getFiles(service);
        Assert.assertNotNull(names);
        Assert.assertFalse(newNames.contains(names.get(0)));
    }


    @Test
    public void newStorageUploadTest() {
        uploadTest(new NewStorageService());
    }


    @Test
    public void deleteOldStorageTest() {
        deleteTest(new OldStorageService());
    }

    @Test
    public void deleteNewStorageTest() {
        NewStorageService service = new NewStorageService();
        uploadTest(service);
        deleteTest(service);
    }
}
