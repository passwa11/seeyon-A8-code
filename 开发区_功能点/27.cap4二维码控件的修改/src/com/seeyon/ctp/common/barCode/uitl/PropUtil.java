package com.seeyon.ctp.common.barCode.uitl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class PropUtil {

    private String imageName;
    private int width;
    private int height;

    private Properties properties;

    public PropUtil() {
        properties = new Properties();
        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        try {
            File file = new File(path, "config/qrcode.properties");
            InputStream in = new FileInputStream(file);
            properties.load(new InputStreamReader(in, "UTF-8"));
            imageName = properties.getProperty("qrcode.image.name");
            width = Integer.parseInt(properties.getProperty("qrcode.x"));
            height = Integer.parseInt(properties.getProperty("qrcode.y"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
