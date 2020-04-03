package com.skazhenik.migration.service;

/**
 * Provides interaction with new storage.
 *
 * @see AbstractStorageService
 */
public class NewStorageService extends AbstractStorageService {
    private static final String endPoint = "/newStorage/files";
    private static final String defaultURI = localhost + endPoint;

    public NewStorageService() {
        super();
    }

    @Override
    public String getDefaultURI() {
        return defaultURI;
    }
}
