package com.skazhenik.migration.exception;

public class ServiceException extends Exception {
    private int responseCode = 0;

    public ServiceException() {
    }

    public ServiceException(final String message) {
        super(message);
    }

    public ServiceException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ServiceException(final Throwable cause) {
        super(cause);
    }

    public ServiceException(final String message, final Throwable cause, final int code) {
        super(message, cause);
        responseCode = code;
    }

    public ServiceException(final String message, final int code) {
        super(message);
        responseCode = code;
    }

    public int getResponseCode() {
        return responseCode;
    }
}