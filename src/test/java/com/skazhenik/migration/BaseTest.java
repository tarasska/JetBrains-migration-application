package com.skazhenik.migration;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Random;

public class BaseTest {
    private final Random random = new Random();
    private final int ALPHABET_SIZE = 255;
    protected final Path tempLocation = Path.of("..");
    protected final int SMALL_TEST_SIZE = 100;


    protected void generateRandomFile(final Path file, final int size) {
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            for (int i = 0; i < size; i++) {
                writer.write(random.nextInt(ALPHABET_SIZE));
            }
        } catch (IOException e) {
            System.err.println("Unable to create random file:" + e.getMessage());
        }
    }

    protected Path createDir() {
        try {
            return Files.createTempDirectory(tempLocation, "temp");
        } catch (IOException e) {
            return null;
        }
    }

    protected void deleteDir(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.err.println("Unable to delete temp dir" + e.getMessage());
        }
    }
}
