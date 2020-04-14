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
        String sql = "select id,subject,doc_mark,issuer,send_departmen,pack_date,status,create_time,year,edoc_type from TEMP_NUMBER20 where status='0'";
        String insertToaSql = "insert into T_OA (id,c1,c2,c3,mlh,ztm,zrz,sj,dw,wh,flag,lb) values(?,?,?,?,?,?,?,?,?,?,?,?) ";
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
                temproraryEntity = new TemproraryEntity();
                temproraryEntity.setId(resultSet.getString("id"));
                temproraryEntity.setSubject(resultSet.getString("subject"));
                temproraryEntity.setIssuer(resultSet.getString("issuer"));
                temproraryEntity.setDoc_mark(resultSet.getString("doc_mark"));
                temproraryEntity.setSend_departmen(resultSet.getString("send_departmen"));
                temproraryEntity.setPack_date(resultSet.getString("pack_date"));
                temproraryEntity.setStatus(resultSet.getString("status"));
                temproraryEntity.setCreate_time(resultSet.getString("create_time"));
                temproraryEntity.setYear(resultSet.getString("year"));
                temproraryEntity.setEdoc_type(resultSet.getString("edoc_type"));
                listTemp.add(temproraryEntity);
            }
            resultSet.close();
            preparedStatement.close();

            mConn = DbConnUtil.getInstance().getMiddleConnection();
            mPs = mConn.prepareStatement(insertToaSql);
            mConn.setAutoCommit(false);
            for (int i = 0; i < listTemp.size(); i++) {
                mPs.setString(1, listTemp.get(i).getId());
                mPs.setString(2, listTemp.get(i).getYear());
                mPs.setString(3, listTemp.get(i).getEdoc_type());
                mPs.setString(4, listTemp.get(i).getSend_departmen());
                mPs.setString(5, (listTemp.get(i).getYear()+"")+listTemp.get(i).getEdoc_type()+listTemp.get(i).getSend_departmen());
                mPs.setString(6,listTemp.get(i).getSubject());
                mPs.setString(7,listTemp.get(i).getSend_departmen());
                mPs.setString(8,listTemp.get(i).getCreate_time());
                mPs.setString(9,"");
                mPs.setString(10,listTemp.get(i).getDoc_mark());
                mPs.setString(11,listTemp.get(i).getId());
                mPs.setString(12,listTemp.get(i).getEdoc_type());
                mPs.addBatch();
            }
            mPs.executeBatch();
            mConn.commit();
            mPs.close();

            for (int i = 0; i < listTemp.size(); i++) {
                preparedStatement = connection.prepareStatement(selectFileInfo);
                preparedStatement.setString(1, listTemp.get(i).getId());
                resultSet = preparedStatement.executeQuery();
                mPs = mConn.prepareStatement(insertFileInfo);
                mConn.setAutoCommit(false);
                while (resultSet.next()) {
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

            String updateNumber10 = "update TEMP_NUMBER10 set status='1' where ID=?";
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
