package com.skazhenik.migration.service;

/**
 * Provides interaction with old storage.
 *
 * @see AbstractStorageService
 */
public class OldStorageService extends AbstractStorageService {
    private static final String endPoint = "/oldStorage/files";
    private static final String defaultURI = localhost + endPoint;

    @Override
    String getDefaultURI() {
        return defaultURI;
    }
}
