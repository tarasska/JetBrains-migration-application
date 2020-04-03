package com.skazhenik.migration.service;

import com.skazhenik.migration.exception.ServiceException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.json.JSONArray;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractStorageService {
    protected static final String localhost = "http://localhost:8080";

    private final int MAX_CONNECTION = 100;
    private final PoolingHttpClientConnectionManager httpClientConnectionManager;
    private final CloseableHttpClient client;

    /**
     * Creates {@link org.apache.http.client.HttpClient} with {@link #MAX_CONNECTION} connections.
     */
    public AbstractStorageService() {
        httpClientConnectionManager = new PoolingHttpClientConnectionManager();
        httpClientConnectionManager.setMaxTotal(MAX_CONNECTION);
        client = HttpClients.custom().setConnectionManager(httpClientConnectionManager).build();
    }

    /**
     * @param response {@link HttpResponse} response
     * @return int value represents response code
     */
    private int getResponseCode(final HttpResponse response) {
        return response.getStatusLine().getStatusCode();
    }

    /**
     * @param response {@link HttpResponse} response
     * @return {@link Boolean#TRUE} if response code equals to {@link HttpStatus#SC_OK}
     */
    private boolean isResponseSuccessful(final HttpResponse response) {
        // setting point
        return getResponseCode(response) == HttpStatus.SC_OK;
    }

    /**
     * @param response {@link HttpResponse} response
     * @throws ServiceException if {@link #isResponseSuccessful(HttpResponse)} return {@link Boolean#FALSE}
     */
    private void checkResponse(final HttpResponse response) throws ServiceException {
        if (!isResponseSuccessful(response)) {
            final int responseCode = getResponseCode(response);
            throw new ServiceException(
                    "Bad response with code: " + responseCode,
                    responseCode
            );
        }
    }

    /**
     * Execute GET HTTP request.
     *
     * @param uri {@link String} representation of URI
     * @return {@link InputStream} with response content
     * @throws ServiceException if the request failed
     */
    private InputStream executeGetRequest(final String uri) throws ServiceException {
        HttpGet request = new HttpGet(uri);
        try {
            CloseableHttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            if (isResponseSuccessful(response)) {
                return entity.getContent();
            } else {
                final int responseCode = getResponseCode(response);
                entity.getContent().close();
                throw new ServiceException(
                        "Bad response with code: " + responseCode,
                        responseCode
                );
            }
        } catch (IOException e) {
            throw new ServiceException("IOException occurred during the execution of the GET request", e);
        }
    }

    /**
     * Execute (file) POST HTTP request.
     *
     * @param uri  {@link String} representation of URI
     * @param file file to post
     * @throws ServiceException if the request failed
     */
    private void executePostRequest(final String uri, final File file) throws ServiceException {
        HttpPost request = new HttpPost(uri);
        request.setHeader("Accept", "*/*");
        request.setEntity(MultipartEntityBuilder.create().addPart("file", new FileBody(file)).build());
        try (CloseableHttpResponse response = client.execute(request)) {
            checkResponse(response);
        } catch (IOException e) {
            throw new ServiceException("IOException occurred during the execution of the POST request", e);
        }
    }

    /**
     * Execute DELETE HTTP request.
     *
     * @param uri {@link String} representation of URI
     * @throws ServiceException if the request failed
     */
    private void executeDeleteRequest(final String uri) throws ServiceException {
        HttpDelete request = new HttpDelete(uri);
        request.setHeader("Accept", "*/*");
        try (CloseableHttpResponse response = client.execute(request)) {
            checkResponse(response);
        } catch (IOException e) {
            throw new ServiceException("IOException occurred during the execution of the DELETE request", e);
        }
    }

    /**
     * Construct file's URI from default URI and file's name.
     *
     * @param fileName file name
     * @return {@link String} containing file's URI.
     */
    private String getFileURI(final String fileName) {
        return getDefaultURI().concat("/").concat(fileName);
    }

    /**
     * @return {@link String} containing default storage URI
     */
    abstract String getDefaultURI();

    /**
     * Construct a {@link List} from {@link InputStream} received from GET request
     *
     * @return {@link List} of files names from storage
     * @throws ServiceException if the request or it's processing failed
     * @see #executeGetRequest(String)
     */
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

    /**
     * Upload {@code file} to the storage.
     *
     * @param file file to upload
     * @throws ServiceException if upload failed
     * @see #executePostRequest(String, File)
     */
    public void upload(final File file) throws ServiceException {
        executePostRequest(getDefaultURI(), file);
    }

    /**
     * Download file with {@code fileName} to {@code tempDir} from storage.
     *
     * @param tempDir  directory for storing temporary data
     * @param fileName file's name to download
     * @throws ServiceException if download failed
     * @see #executeGetRequest(String)
     */
    public void download(final Path tempDir, final String fileName) throws ServiceException {
        Path file = tempDir.resolve(fileName);
        try (InputStream inputStream = executeGetRequest(getFileURI(fileName))) {
            try (OutputStream writer = Files.newOutputStream(file)) {
                int data;
                while ((data = inputStream.read()) != -1) {
                    writer.write(data);
                }
            }
        } catch (IOException e) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {
            }
            throw new ServiceException("IOException occurred during processing request content", e);
        }

    }

    /**
     * Delete file with {@code fileName} from storage.
     *
     * @param fileName file's name to delete
     * @throws ServiceException if deletion failed
     * @see #executeDeleteRequest(String)
     */
    public void delete(final String fileName) throws ServiceException {
        executeDeleteRequest(getFileURI(fileName));
    }
}
