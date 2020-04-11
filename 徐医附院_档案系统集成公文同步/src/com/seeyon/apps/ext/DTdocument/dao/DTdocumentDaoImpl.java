package com.seeyon.apps.ext.DTdocument.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.seeyon.apps.ext.DTdocument.po.TempDate;
import com.seeyon.apps.ext.DTdocument.util.ReadConfigTools;

public class DTdocumentDaoImpl implements DTdocumentDao {

    private ReadConfigTools configTools = null;
    private String url = "";
    private String username = "";
    private String password = "";
    private String driverClassName = "";

    public DTdocumentDaoImpl() {
        configTools = new ReadConfigTools();
        url = configTools.getString("lowPortalPlugin.db.url");
        username = configTools.getString("lowPortalPlugin.db.username");
        password = configTools.getString("lowPortalPlugin.db.password");
        driverClassName = configTools.getString("lowPortalPlugin.db.driverClassName");
    }

    @Override
    public List<TempDate> getAllTempDate() {
        String sql = "select * from TEMP_DATE ";
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        List<TempDate> list=new ArrayList<>();
        TempDate tempDate = null;
        try {
            Class.forName(driverClassName);
            conn = DriverManager.getConnection(url, username, password);
            pst = conn.prepareStatement(sql);
            //绑定参数
            rs = pst.executeQuery();
            if (rs != null) {
                while (rs.next()) {
                    tempDate = new TempDate();
                    tempDate.setId(rs.getLong("ID"));
                    tempDate.setStartdate(rs.getString("STARTDATE"));
                    tempDate.setEnddate(rs.getString("ENDDATE"));
                    list.add(tempDate);
                }
            }
        } catch (Exception e) {
            System.out.println("查询发生异常：异常信息------->" + e.getMessage());
        } finally {
            try {
                pst.close();
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

}
