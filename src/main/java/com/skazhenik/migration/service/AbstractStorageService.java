package com.skazhenik.migration.service;

import com.skazhenik.migration.exception.ServiceException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private void executePostRequest(final String uri, final File file) throws ServiceException {
        HttpPost request = new HttpPost(uri);
        request.setHeader("Accept", "*/*");
        request.setEntity(MultipartEntityBuilder.create().addPart("file", new FileBody(file)).build());

        HttpResponse response;
        try {
            response = client.execute(request);
        } catch (IOException e) {
            throw new ServiceException("IOException occurred during the execution of the POST request", e);
        }

        checkResponse(response);
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

    public List<String> getFilesList() throws ServiceException {
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

    public void upload(final File file, final String fileName) throws ServiceException {
        executePostRequest(getFileURI(fileName), file);
    }

    public void download(final Path tempDir, final String fileName) throws ServiceException {
        Path file = tempDir.resolve(fileName);
        try (InputStream inputStream = executeGetRequest(getFileURI(fileName))) {
            BufferedWriter writer = Files.newBufferedWriter(file);
            int data;
            while ((data = inputStream.read()) != -1) {
                writer.write(data);
            }
        } catch (IOException e) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {
            }
            throw new ServiceException("IOException occurred during reading request content", e);
        }
    }

    public void delete(final String fileName) throws ServiceException {
        executeDeleteRequest(getFileURI(fileName));
    }
}
