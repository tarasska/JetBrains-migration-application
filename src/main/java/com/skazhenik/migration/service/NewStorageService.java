package com.skazhenik.migration.service;

import com.skazhenik.migration.exception.ServiceException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class NewStorageService {
    private static final String endPoint = "/newStorage/files";
    private static final String localhost = "http://localhost:8080";
    private static final String defaultURI = localhost + endPoint;

    private final CloseableHttpClient client;

    public NewStorageService() {
        client = HttpClientBuilder.create().build();
    }


    private boolean isRequestSuccessful(int responseCode) {
        // setting point
        return 200 == responseCode;
    }

    private InputStream getRequest(String uri) throws IOException, ServiceException {
        HttpGet request = new HttpGet(uri);
        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();

        int responseCode = response.getStatusLine().getStatusCode();

        if (!isRequestSuccessful(responseCode)) {
            throw new ServiceException("Bad request to " + request.getURI() + " with response code: " + responseCode);
        }

        return entity.getContent();
    }


    public List<String> getFiles() throws ServiceException {
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(getRequest(defaultURI)));
        } catch (IOException e) {
            throw new ServiceException("IOException occurred during the execution of the request", e);
        }
        StringBuilder jsonString = new StringBuilder();
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                jsonString.append(line);
            }
        } catch (IOException e) {
            throw new ServiceException("IOException occurred during parsing request content", e);
        }
        JSONArray jsonArray = new JSONArray(jsonString.toString());
        List<String> files = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            files.add(jsonArray.getString(i));
        }
        return files;
    }

    public void upload(String fileName) throws ServiceException {

    }

    public void delete(String fileName) throws ServiceException {

    }
}
