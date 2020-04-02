package com.skazhenik.migration.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.Objects;

public class FileUtils {
    /**
     * Creates a new temporary directory from {@link Path}
     *
     * @param path {@link Path} to location where to create temporary directory
     * @return {@link Path} for temporary directory
     * @throws IOException if an error occurs while creating temporary directory by given {@code path}
     * @see Files#createTempDirectory(Path, String, FileAttribute[])
     */
    public static Path createTempDir(final Path path) throws IOException {
        Objects.requireNonNull(path);

        return Files.createTempDirectory(path, "temporary");

    }

    /**
     * Delete temporary directory which was created by {@link #createTempDir(Path)}
     *
     * @param path {@link Path} to location of temporary directory
     * @throws IOException if an error occurs while deleting directories
     */
    public static void deleteTempDir(final Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            /**
             *  Visits file and deletes it from file system.
             *
             * @param file {@link Path} to file to be deleted
             * @param attrs {@link BasicFileAttributes} file attributes of given {@code file}
             * @return {@link FileVisitResult#CONTINUE} if no error occurs
             * @throws IOException if file deletion fails
             */
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            /**
             * Visits directory and deleted it from file system.
             *
             * @param dir {@link Path} to directory to be deleted
             * @param exc {@link IOException} instance if any error occurs during directory visiting
             * @return {@link FileVisitResult#CONTINUE} if no error occurs
             * @throws IOException if directory deletion fails
             */
            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Delete the file corresponding to the {@code path}.
     * Does not throw {@link IOException} if deletion failed.
     *
     * @param path file path
     */
    public static void deleteFileIfPossible(final Path path) {
        try {
            Files.delete(path);
        } catch (Exception ignored) {
        }
    }
}
