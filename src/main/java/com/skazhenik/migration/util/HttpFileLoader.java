package com.skazhenik.migration.util;


import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class HttpFileLoader implements AutoCloseable {
    private final CloseableHttpClient client;

    public HttpFileLoader() {
        client = HttpClientBuilder.create().build();
    }

    public void download(URI uri, String filePath) {
        try {
            HttpGet request = new HttpGet(uri);
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();

            int responseCode = response.getStatusLine().getStatusCode();

            System.out.println("Request Url: " + request.getURI());
            System.out.println("Response Code: " + responseCode);

            InputStream is = entity.getContent();

            BufferedWriter fos = Files.newBufferedWriter(Path.of(filePath));

            int inByte;
            while ((inByte = is.read()) != -1) {
                fos.write(inByte);
            }

            is.close();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void upload(URI uri, String filePath) throws IOException {
        HttpPost request = new HttpPost(uri);
        request.setHeader("Accept", "*/*");
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        // fileParamName should be replaced with parameter name your REST API expect.
        builder.addPart("file", new FileBody(new File(filePath)));
        builder.addPart("optionalParam", new StringBody("true", ContentType.create("text/plain", Consts.UTF_8)));
        request.setEntity(builder.build());
        HttpResponse response = client.execute(request);
        int httpStatus = response.getStatusLine().getStatusCode();
        System.out.println(EntityUtils.toString(response.getEntity(), "UTF-8"));
    }

    public void close() throws Exception {
        client.close();
    }
}
