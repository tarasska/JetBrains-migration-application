package com.skazhenik.migration.loader;

import com.skazhenik.migration.exception.MigrationException;
import com.skazhenik.migration.service.AbstractStorageService;
import com.skazhenik.migration.service.NewStorageService;
import com.skazhenik.migration.service.OldStorageService;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.skazhenik.migration.util.FileUtils.deleteFileIfPossible;
import static com.skazhenik.migration.util.MigrationUtils.*;

/**
 * Provides concurrent file migration processing.
 * Each thread performs a complete download-upload cycle.
 */
public class ParallelMigrationManager implements AutoCloseable {
    private final Path tempDir;
    private final OldStorageService oldStorageService;
    private final NewStorageService newStorageService;
    private final ExecutorService executorService;

    /**
     * Creates a new instance of the class for parallel migration with
     * a given number of threads, a temporary directory and storages.
     * Uses {@link ExecutorService} for multithreaded execution.
     *
     * @param threads           number of threads
     * @param tempDir           directory for storing temporary data
     * @param oldStorageService service for old storage
     * @param newStorageService service for new storage
     *                          
     * @see java.util.concurrent.Executors#newFixedThreadPool(int) 
     */
    public ParallelMigrationManager(final int threads,
                                    final Path tempDir,
                                    final OldStorageService oldStorageService,
                                    final NewStorageService newStorageService) {
        this.tempDir = tempDir;
        this.oldStorageService = oldStorageService;
        this.newStorageService = newStorageService;
        this.executorService = Executors.newFixedThreadPool(threads);
    }

    /**
     * If one of the tasks ended with an exception, then it collects
     * all the exceptions in the first and throws it.
     *
     * @param futures list of {@link Future}
     * @param <T>     {@link Future#get()} return value
     * @throws ExecutionException if one of the future throw exception
     */
    private <T> void throwIfPresent(List<Future<T>> futures) throws ExecutionException {
        final List<ExecutionException> exceptions = new ArrayList<>();
        for (final Future<T> future : futures) {
            try {
                future.get();
            } catch (InterruptedException ignored) {
            } catch (ExecutionException e) {
                exceptions.add(e);
            }
        }
        if (!exceptions.isEmpty()) {
            ExecutionException e = exceptions.get(0);
            exceptions.subList(1, exceptions.size()).forEach(e::addSuppressed);
            throw e;
        }
    }

    /**
     * Performs parallel migration.
     *
     * @param files      {@link List} of file's names
     * @param loadFactor parameter providing a fixed size of the directory with local files
     * @throws ExecutionException if one of the tasks ended with an exception
     */
    public void load(final List<String> files, final int loadFactor) throws ExecutionException {
        final List<Future<Object>> futures = new ArrayList<>();
        for (final String name : files) {
            futures.add(executorService.submit(() -> {
                final int cnt = tempDir.getNameCount();
                // it's ok if some threads will not get the exact value
                if (cnt <= loadFactor) {
                    downloadFile(oldStorageService, tempDir, name);
                } else {
                    throw new MigrationException("Temp storage is broken, unable to download file:" + name);
                }
                Path filePath = tempDir.resolve(name);
                uploadFile(newStorageService, new File(filePath.toString()), name);
                deleteFileIfPossible(filePath);
                return null;
            }));
        }
        throwIfPresent(futures);
    }

    /**
     * Delete all files from storage using {@code service}.
     *
     * @param service service for interacting with storage
     * @param files   {@link List} of file's names to delete
     * @throws ExecutionException if one of the tasks ended with an exception
     */
    public void delete(final AbstractStorageService service, final List<String> files) throws ExecutionException {
        final List<Future<Object>> futures = new ArrayList<>();
        for (final String name : files) {
            futures.add(executorService.submit(() -> {
                deleteFile(service, name);
                return null;
            }));
        }
        throwIfPresent(futures);
    }

    @Override
    public void close() {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
