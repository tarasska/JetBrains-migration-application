package com.skazhenik.migration.service;

public class NewStorageService extends AbstractStorageService {
    private static final String endPoint = "/newStorage/files";
    private static final String defaultURI = localhost + endPoint;

    public NewStorageService() {
        super();
    }


    @Override
    String getDefaultURI(){
        return localhost + endPoint;
    }
}
