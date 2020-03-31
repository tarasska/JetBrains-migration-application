package com.skazhenik.migration.exception;

public class MigrationException extends Exception {
    public MigrationException() {
    }

    public MigrationException(final String message) {
        super(message);
    }

    public MigrationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public MigrationException(final Throwable cause) {
        super(cause);
    }
}
