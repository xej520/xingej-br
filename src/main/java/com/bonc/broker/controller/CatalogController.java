package com.bonc.broker.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * @author xingej
 */

@RestController
@RequestMapping(value = "/v2/catalog")
public class CatalogController {

    private final Object catalog;

    public CatalogController() {


        StringBuffer stringBuffer = new StringBuffer();
        try {
            InputStream stream = getClass().getClassLoader().getResourceAsStream("catalog.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                stringBuffer.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String jsonData = stringBuffer.toString();

        if (null == jsonData) {
            this.catalog = null;
            return ;
        }

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
