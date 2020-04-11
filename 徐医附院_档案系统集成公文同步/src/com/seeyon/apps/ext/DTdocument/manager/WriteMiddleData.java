package com.seeyon.apps.ext.DTdocument.manager;

import com.seeyon.apps.ext.DTdocument.po.TemproraryEntity;
import com.seeyon.apps.ext.DTdocument.util.DbConnUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 周刘成   2020-4-8
 */
public class WriteMiddleData {

    public static WriteMiddleData writeMiddleData;

    public static WriteMiddleData getInstance() {
        if (null == writeMiddleData) {
            writeMiddleData = new WriteMiddleData();
        }
        return writeMiddleData;
    }

    public void getListData() {
        Connection mConn = null;
        PreparedStatement mPs = null;
        ResultSet mRs = null;
        String sql = "select ID,SUBJECT,ISSUER from TEMP_NUMBER20 where status='0'";
        String insertSql = "insert into T_OA (id,flag,ztm,zrz) values(?,?,?,?) ";
        String updateSql = "update TEMP_NUMBER20 set status='1' where ID=?";
        String selectFileInfo = "select id,C_MIDRECID,C_FILETITLE,C_FTPFILEPATH,C_TYPE,I_SIZE,meta_type from TEMP_NUMBER30 where C_MIDRECID=?";
        String insertFileInfo = "insert into T_OATX(id,aid,ztm,filename,filesize) values(?,?,?,?,?)";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Connection connection = null;
        try {
            connection = DbConnUtil.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            List<TemproraryEntity> listTemp = new ArrayList<>();
            TemproraryEntity temproraryEntity = null;
            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String subject = resultSet.getString("subject");
                String issuer = resultSet.getString("issuer");
                temproraryEntity = new TemproraryEntity();
                temproraryEntity.setId(id);
                temproraryEntity.setSubject(subject);
                temproraryEntity.setIssuer(issuer);
                listTemp.add(temproraryEntity);
            }
            resultSet.close();
            preparedStatement.close();

            mConn = DbConnUtil.getInstance().getMiddleConnection();
            mPs = mConn.prepareStatement(insertSql);
            mConn.setAutoCommit(false);
            for (int i = 0; i < listTemp.size(); i++) {
                mPs.setString(1, listTemp.get(i).getId());
                mPs.setString(2, listTemp.get(i).getId());
                mPs.setString(3, listTemp.get(i).getSubject());
                mPs.setString(4, listTemp.get(i).getIssuer());
                mPs.addBatch();
            }
            mPs.executeBatch();
            mConn.commit();
            mPs.close();

            for (int i = 0; i < listTemp.size(); i++) {
                preparedStatement = connection.prepareStatement(selectFileInfo);
                preparedStatement.setString(1,listTemp.get(i).getId());
                resultSet = preparedStatement.executeQuery();
                mPs = mConn.prepareStatement(insertFileInfo);
                mConn.setAutoCommit(false);
                while(resultSet.next()){
                    mPs.setString(1, resultSet.getString("id"));
                    mPs.setString(2, resultSet.getString("c_midrecid"));
                    mPs.setString(3, resultSet.getString("c_filetitle"));
                    mPs.setString(4, resultSet.getString("c_ftpfilepath"));
                    mPs.setString(5, resultSet.getString("i_size"));
                    mPs.addBatch();
                }
                mPs.executeBatch();
                mConn.commit();

                mPs.close();
                resultSet.close();
                preparedStatement.close();

            }

            preparedStatement = connection.prepareStatement(updateSql);
            for (int i = 0; i < listTemp.size(); i++) {
                preparedStatement.setString(1, listTemp.get(i).getId());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            preparedStatement.close();

            String updateNumber10="update TEMP_NUMBER10 set status='1' where ID=?";
            preparedStatement = connection.prepareStatement(updateNumber10);
            for (int i = 0; i < listTemp.size(); i++) {
                preparedStatement.setString(1, listTemp.get(i).getId());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            preparedStatement.close();

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
