package com.skazhenik.migration.service;

import com.skazhenik.migration.exception.ServiceException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractStorageService {
    protected static final String localhost = "http://localhost:8080";

    private final CloseableHttpClient client;

    public AbstractStorageService() {
        client = HttpClients.createDefault();
    }


    private boolean isResponseSuccessful(int responseCode) {
        // setting point
        return 200 == responseCode;
    }

    private void checkResponse(final HttpResponse response) throws ServiceException {
        final int responseCode = response.getStatusLine().getStatusCode();
        if (!isResponseSuccessful(responseCode)) {
            throw new ServiceException("Bad response with code: " + responseCode);
        }
    }

    private InputStream executeGetRequest(final String uri) throws ServiceException {
        HttpGet request = new HttpGet(uri);
        try {
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();

            checkResponse(response);

            return entity.getContent();
        } catch (IOException e) {
            throw new ServiceException("IOException occurred during the execution of the GET request", e);
        }
    }

    private void executeDeleteRequest(final String uri) throws ServiceException {
        HttpDelete request = new HttpDelete(uri);
        request.setHeader("Accept", "*/*");
        try {
            HttpResponse response = client.execute(request);
            checkResponse(response);
        } catch (IOException e) {
            throw new ServiceException("IOException occurred during the execution of the DELETE request", e);
        }
    }

    private String getFileURI(final String fileName) {
        return getDefaultURI().concat("/").concat(fileName);
    }

    abstract String getDefaultURI();

    public List<String> getFiles() throws ServiceException {
        try (BufferedReader bufferedReader =
                     new BufferedReader(new InputStreamReader(executeGetRequest(getDefaultURI())))) {
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
        } catch (IOException e) {
            throw new ServiceException("IOException occurred during reading request content", e);
        }
    }

    public void upload(final String fileName) throws ServiceException {

    }

    public void delete(final String fileName) throws ServiceException {
        executeDeleteRequest(getFileURI(fileName));
    }
}
