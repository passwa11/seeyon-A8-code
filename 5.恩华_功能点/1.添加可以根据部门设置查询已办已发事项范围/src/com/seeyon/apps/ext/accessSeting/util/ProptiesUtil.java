package com.seeyon.apps.ext.accessSeting.util;

import java.io.*;
import java.util.Properties;

public class ProptiesUtil {

    private String leaveTemplateId;
    private Properties properties;

    public ProptiesUtil() {
        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        File file = new File(path, "config/id.properties");
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            properties.load(new InputStreamReader(is, "UTF-8"));
            leaveTemplateId = properties.getProperty("leave.template.id");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLeaveTemplateId() {
        return leaveTemplateId;
    }

    public void setLeaveTemplateId(String leaveTemplateId) {
        this.leaveTemplateId = leaveTemplateId;
    }
}
