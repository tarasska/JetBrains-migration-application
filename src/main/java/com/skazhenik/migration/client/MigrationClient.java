package com.skazhenik.migration.client;

import com.skazhenik.migration.exception.ServiceException;
import com.skazhenik.migration.service.NewStorageService;
import com.skazhenik.migration.util.HttpFileLoader;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class MigrationClient {
    public static void main(String[] args) {
        new MigrationClient().run();
    }

    public void run() {
//        HttpFileLoader fileDownloader = new HttpFileLoader();
//        try {
//            //fileDownloader.download(new URI("http://localhost:8080/oldStorage/files/411.txt"), "/home/taras/jet_brains/411.txt");
//            fileDownloader.upload(new URI("http://localhost:8080/newStorage/files"), "/home/taras/jet_brains/411.txt");
//        } catch (URISyntaxException | IOException e) {
//            e.printStackTrace();
//        }
        NewStorageService service = new NewStorageService();
        try {
            List<String> files = service.getFiles();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }
}
