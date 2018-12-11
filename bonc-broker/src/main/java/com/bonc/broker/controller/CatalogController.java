package com.bonc.broker.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

@RestController
@RequestMapping(value = "/mysql/v2/catalog")
public class CatalogController {

    private final Object catalog;

    public CatalogController(@Value("classpath:catalog.json") Resource catalog) throws IOException {
        File catalogFile = catalog.getFile();
        String jsonData = this.jsonRead(catalogFile);
        this.catalog = JSONObject.parseObject(jsonData);
    }

    @GetMapping
    public Object catalog() {
        return this.catalog;
    }

    private String jsonRead(File file) {
        Scanner scanner = null;
        StringBuilder buffer = new StringBuilder();
        try {
            scanner = new Scanner(file, "utf-8");
            while (scanner.hasNextLine()) {
                buffer.append(scanner.nextLine());
            }
        } catch (Exception e) {

        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return buffer.toString();
    }


}
