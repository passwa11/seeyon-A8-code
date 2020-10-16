package com.seeyon.apps.ext.DTdocument.util;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by Administrator on 2019-9-10.
 */
public class DbConnUtil {

    public static DbConnUtil dbConnUtil;
    private String url = "";
    private String username = "";
    private String password = "";
    private String driverClassName = "";


    public static DbConnUtil getInstance() {
        if (null == dbConnUtil) {
            dbConnUtil = new DbConnUtil();
        }
        return dbConnUtil;
    }

    public DbConnUtil(){
        ReadConfigTools configTools = new ReadConfigTools();
        url = configTools.getString("lowPortalPlugin.db.url");
        username = configTools.getString("lowPortalPlugin.db.username");
        password = configTools.getString("lowPortalPlugin.db.password");
        driverClassName = configTools.getString("lowPortalPlugin.db.driverClassName");
    }

    public Connection getConnection() {
        Connection conn = null;
        try{
            Class.forName(driverClassName);
            conn = DriverManager.getConnection(url, username, password);
        }catch (Exception e){
            e.printStackTrace();
        }
        return conn;
    }


}
