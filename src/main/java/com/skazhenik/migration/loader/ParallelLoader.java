package com.skazhenik.migration.loader;

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

import static com.skazhenik.migration.util.MigrationManager.*;

public class ParallelLoader implements AutoCloseable {
    private final Path tempDir;
    private final OldStorageService oldStorageService;
    private final NewStorageService newStorageService;
    private final ExecutorService executorService;

    public ParallelLoader(final int threads,
                          final Path tempDir,
                          final OldStorageService oldStorageService,
                          final NewStorageService newStorageService) {
        this.tempDir = tempDir;
        this.oldStorageService = oldStorageService;
        this.newStorageService = newStorageService;
        this.executorService = Executors.newFixedThreadPool(threads);
    }

    public void load(final List<String> files) throws ExecutionException {
        final List<Future<Boolean>> futures = new ArrayList<>();
        for (final String name : files) {
            futures.add(executorService.submit(() -> {
                downloadFile(oldStorageService, tempDir, name);
                uploadFile(newStorageService, new File(tempDir.resolve(name).toString()), name);
                deleteFile(oldStorageService, name);
                return true;
            }));
        }
        final List<ExecutionException> exceptions = new ArrayList<>();
        for (final Future<Boolean> future : futures) {
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

    @Override
    public void close() {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
