package com.skazhenik.migration.loader;

import com.skazhenik.migration.exception.MigrationException;
import com.skazhenik.migration.service.NewStorageService;
import com.skazhenik.migration.service.OldStorageService;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.skazhenik.migration.util.MigrationManager.downloadFile;
import static com.skazhenik.migration.util.MigrationManager.uploadFile;

public class ParallelLoader {
    private static final int MAX_FILE_COUNT = 100;
    private static final int MAX_QUEUE_SIZE = 100;

    private final Path tempDir;
    private final OldStorageService oldStorageService;
    private final NewStorageService newStorageService;
    private final LinkedBlockingQueue<Runnable> downloadTasks = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
    private final LinkedBlockingQueue<Runnable> uploadTasks = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
    private final List<Thread> workers;

    private final Counter filesCount = new Counter(0);

    public ParallelLoader(final int downloadThreads,
                          final int upThreads,
                          final Path tempDir,
                          final OldStorageService oldStorageService,
                          final NewStorageService newStorageService) {
        this.tempDir = tempDir;
        this.oldStorageService = oldStorageService;
        this.newStorageService = newStorageService;
        final Runnable downloadTask = () -> {
            try {
                while (true) {
                    Runnable task = downloadTasks.take();
                    synchronized (filesCount) {
                        while (filesCount.getValue() == MAX_FILE_COUNT) {
                            filesCount.wait();
                        }
                        filesCount.increase();
                        filesCount.notifyAll();
                    }
                    task.run();
                }
            } catch (InterruptedException ignored) {
            }
        };
        final Runnable uploadTask = () -> {
            try {
                while (true) {
                    uploadTasks.take().run();
                    synchronized (filesCount) {
                        filesCount.decrease();
                        filesCount.notifyAll();
                    }
                }
            } catch (InterruptedException ignored) {
            }
        };
        workers = Stream.generate(() -> new Thread(downloadTask)).limit(downloadThreads).collect(Collectors.toList());
        Stream.generate(() -> new Thread(uploadTask)).limit(downloadThreads).forEach(workers::add);
        workers.forEach(Thread::start);
    }

    public void load(final List<String> files) {
        for (final String name : files) {
            try {
                downloadTasks.put(() -> {
                    try {
                        downloadFile(oldStorageService, tempDir, name);
                        try {
                            uploadTasks.put(() -> {
                                try {
                                    uploadFile(newStorageService,
                                            new File(String.valueOf(tempDir.resolve(name))),
                                            name);
                                } catch (MigrationException e) {

                                }
                            });
                        } catch (InterruptedException ignored) {
                        }
                    } catch (MigrationException e) {

                    }
                });
            } catch (InterruptedException ignored) {
            }
        }
    }

    private static class Counter {
        private int value;

        public Counter(final int base) {
            value = base;
        }

        public void increase() {
            value = value + 1;
        }

        public void decrease() {
            value = value - 1;
        }

        public int getValue() {
            return value;
        }
    }
}
