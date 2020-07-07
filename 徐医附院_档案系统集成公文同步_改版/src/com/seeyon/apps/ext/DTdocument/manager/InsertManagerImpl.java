package com.seeyon.apps.ext.DTdocument.manager;

import com.seeyon.ctp.util.JDBCAgent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;

/**
 * Created by Administrator on 2020-7-1.
 */
public class InsertManagerImpl {

    public static InsertManagerImpl manager;

    public static InsertManagerImpl getInstance() {
        if (manager == null) {
            manager = new InsertManagerImpl();
        }
        return manager;
    }



//    正文 10，30

    public void queryPro4() throws Exception {
        String sendSql = "select id,subject,doc_mark,issuer,send_department,pack_date,val,create_time,year,edoc_type,org from ( " +
                "select A.id,subject,doc_mark, issuer,A.send_department,A.pack_date, 0 val,to_char(a.create_time,'yyyyMMdd') create_time,to_char(a.create_time,'yyyy') year,  CASE A .EDOC_TYPE WHEN 0 THEN  '发文' WHEN 1 THEN  '收文' ELSE  '签报' END EDOC_TYPE,'' org " +
                "from edoc_summary A, ( " +
                "SELECT  zall.* FROM  (select * from TEMP_NUMBER40 where CONTENT_TYPE in (10,30) ) zall " +
                ") b where A.has_archive = 1 " +
                "and a.id = b.MODULE_ID " +
                " and a.EDOC_TYPE =0) cd";
        invokeInsert20(sendSql);

        String reciverSql = "select id,subject,doc_mark,issuer,send_department,pack_date,val,create_time,year,edoc_type,org from ( " +
                "select A.id,subject,doc_mark, issuer,A.send_department,A.pack_date, 0 val,to_char(a.create_time,'yyyyMMdd') create_time,to_char(a.create_time,'yyyy') year,  CASE A .EDOC_TYPE WHEN 0 THEN  '发文' WHEN 1 THEN  '收文' ELSE  '签报' END EDOC_TYPE,'' org " +
                "from edoc_summary A, ( " +
                "SELECT  zall.* FROM  (select * from TEMP_NUMBER40 where CONTENT_TYPE in (10,30) ) zall " +
                ") b where A.has_archive = 1 " +
                "and a.id = b.MODULE_ID " +
                " and a.EDOC_TYPE =1) cd";
        invokeInsert20(reciverSql);

        String qianBaoSql = "select id,subject,doc_mark,issuer,send_department,pack_date,val,create_time,year,edoc_type,org from ( " +
                "select A.id,subject,doc_mark, issuer,A.send_department,A.pack_date, 0 val,to_char(a.create_time,'yyyyMMdd') create_time,to_char(a.create_time,'yyyy') year,  CASE A .EDOC_TYPE WHEN 0 THEN  '发文' WHEN 1 THEN  '收文' ELSE  '签报' END EDOC_TYPE,'' org " +
                "from edoc_summary A, ( " +
                "SELECT  zall.* FROM  (select * from TEMP_NUMBER40 where CONTENT_TYPE in (10,30) ) zall " +
                ") b where A.has_archive = 1 " +
                "and a.id = b.MODULE_ID " +
                "and a.EDOC_TYPE =2) cd";
        invokeInsert20(qianBaoSql);

        String wenDanSql = "select id,c_midrecid,c_filetitle,c_ftpfilepath,c_type,i_size,meta_type,status from ( " +
                "select B.id,A.id C_MIDRECID, " +
                "a.subject || '.html' c_filetitle, " +
                "'/upload/' || " +
                "substr(to_char(a.Create_time, 'yyyy-mm-dd'), 0, 4) || '/' || " +
                "substr(to_char(a.Create_time, 'yyyy-mm-dd'), 6, 2) || '/' || " +
                "substr(to_char(a.Create_time, 'yyyy-mm-dd'), 9, 2) || '/' || " +
                "a.id C_FTPFILEPATH, " +
                "'文单' C_TYPE, " +
                "2048 I_SIZE, " +
                "'.html' META_TYPE, " +
                "0 status " +
                "from edoc_summary A " +
                "left join ( " +
                "SELECT  zall.* FROM  (select * from TEMP_NUMBER40 where  CONTENT_TYPE IN (10,30) ) zall " +
                ") B " +
                "on B.MODULE_ID = A.Id and  A.has_archive = 1 " +
                "where B.Id is not null ) cd";

        insertTemp30(wenDanSql);

        String fileSql = "select id,c_midrecid,c_filetitle,c_ftpfilepath,c_type,i_size,meta_type,status from ( " +
                "select C.id, A.Id C_MIDRECID, " +
                "C.Filename C_FILETITLE, " +
                "'/upload/' || " +
                "substr(to_char(C.createdate, 'yyyy-mm-dd'), 0, 4) || '/' || " +
                "substr(to_char(C.createdate, 'yyyy-mm-dd'), 6, 2) || '/' || " +
                "substr(to_char(C.createdate, 'yyyy-mm-dd'), 9, 2) || '/' || " +
                "C.file_url  C_FTPFILEPATH, " +
                "'附件' C_TYPE, " +
                "C.attachment_size I_SIZE, " +
                "substr(C.Filename, instr(C.Filename, '.', -1, 1)) META_TYPE, " +
                "0 status " +
                "from edoc_summary A " +
                "left join ( " +
                "SELECT  zall.* FROM  (select * from TEMP_NUMBER40 where   CONTENT_TYPE IN (10,30) ) zall " +
                ") B " +
                "on A.Id = B.MODULE_ID and  A.has_archive = 1 " +
                "left join ctp_attachment C " +
                "on b.MODULE_ID = c.att_reference " +
                "where C.id is not null ) cd";
        insertTemp30(fileSql);


    }


    public void querySqlInsert30() throws Exception {
        String zhengWenSql = "select id,c_midrecid,c_filetitle,c_ftpfilepath,c_type,i_size,meta_type,status from( " +
                "select B.id,A.id C_MIDRECID,case when instr(b.mime_type,'office')>0  then a.subject ||'.doc' when instr(b.mime_type,'msword')>0 then a.subject ||'.doc' when instr(b.mime_type,'pdf')>0 then a.subject ||'.pdf' when instr(b.mime_type,'excel')>0 then a.subject ||'.xls' end c_filetitle, " +
                "'/upload/' || " +
                "substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 0, 4) || '/' || " +
                "substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 6, 2) || '/' || " +
                "substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 9, 2) || '/' || " +
                "case when instr(b.mime_type,'office')>0  then C.Filename  when instr(b.mime_type,'msword')>0 then C.Filename  when instr(b.mime_type,'pdf')>0 then C.Filename when instr(b.mime_type,'excel')>0 then C.Filename  end C_FTPFILEPATH, " +
                "'正文' C_TYPE, " +
                "C.FILE_SIZE I_SIZE, " +
                "case when instr(b.mime_type,'office')>0  then '.doc' when instr(b.mime_type,'msword')>0 then '.doc' when instr(b.mime_type,'pdf')>0 then '.pdf' when instr(b.mime_type,'excel')>0 then '.xls' end META_TYPE, " +
                "0 status " +
                "from edoc_summary A " +
                "left join ( " +
                "select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from temp_number40 where to_char(content) in (select to_char(id) from ctp_file) and CONTENT_TYPE in (41,42,43,44,45) " +
                ") zall,ctp_file cf where ZALL.CONTENT=CF.id " +
                ") B on B.MODULE_ID = A.Id and  A.has_archive = 1 " +
                " left join ctp_file C on to_char(B.content) = C.Id " +
                "where B.Id is not null ) cd";
        insertTemp30(zhengWenSql);

        String wenDanSql = "select id,c_midrecid,c_filetitle,c_ftpfilepath,c_type,i_size,meta_type,status from ( " +
                "select B.id,A.id C_MIDRECID, " +
                "a.subject || '.html' c_filetitle, " +
                "'/upload/' || " +
                "substr(to_char(a.Create_time, 'yyyy-mm-dd'), 0, 4) || '/' || " +
                "substr(to_char(a.Create_time, 'yyyy-mm-dd'), 6, 2) || '/' || " +
                "substr(to_char(a.Create_time, 'yyyy-mm-dd'), 9, 2) || '/' || " +
                "a.id C_FTPFILEPATH, " +
                "'文单' C_TYPE, " +
                "2048 I_SIZE, " +
                "'.html' META_TYPE, " +
                "0 status " +
                "from edoc_summary A " +
                "left join ( " +
                "select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from temp_number40 where to_char(content) in (select to_char(id) from ctp_file) and CONTENT_TYPE in (41,42,43,44,45) " +
                ") zall,ctp_file cf where ZALL.CONTENT=CF.id) B " +
                "on B.MODULE_ID = A.Id and  A.has_archive = 1 " +
                "where B.Id is not null ) cd";

        insertTemp30(wenDanSql);

//        附件信息
        String fileSql = "select id,c_midrecid,c_filetitle,c_ftpfilepath,c_type,i_size,meta_type,status from ( " +
                "select C.id, A.Id C_MIDRECID, " +
                "C.Filename C_FILETITLE, " +
                "'/upload/' || " +
                "substr(to_char(C.createdate, 'yyyy-mm-dd'), 0, 4) || '/' || " +
                "substr(to_char(C.createdate, 'yyyy-mm-dd'), 6, 2) || '/' || " +
                "substr(to_char(C.createdate, 'yyyy-mm-dd'), 9, 2) || '/' || " +
                "C.file_url  C_FTPFILEPATH, " +
                "'附件' C_TYPE, " +
                "C.attachment_size I_SIZE, " +
                "substr(C.Filename, instr(C.Filename, '.', -1, 1)) META_TYPE, " +
                "0 status " +
                "from edoc_summary A " +
                "left join (select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from CTP_CONTENT_ALL where to_char(content) in (select to_char(id) from ctp_file) and CONTENT_TYPE in (41,42,43,44,45) " +
                ") zall,ctp_file cf where ZALL.CONTENT=CF.id) B " +
                "on A.Id = B.MODULE_ID and  A.has_archive = 1 " +
                "left join ctp_attachment C " +
                "on b.MODULE_ID = c.att_reference " +
                "where C.id is not null ) cd";
        insertTemp30(fileSql);
    }

    public void insertTemp30(String sql) throws Exception {
        Connection connection = JDBCAgent.getRawConnection();
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        HashSet<String> temp10List = queryTempNumber10Id(connection);
        String insert30 = "insert into TEMP_NUMBER30(ID, C_MIDRECID, C_FILETITLE, C_FTPFILEPATH, C_TYPE, I_SIZE, META_TYPE, STATUS) values(?,?,?,?,?,?,?,?)";
        PreparedStatement psInsert = null;

        try {
            psInsert = connection.prepareStatement(insert30);
            connection.setAutoCommit(false);
            while (rs.next()) {
                if (temp10List.contains(rs.getString("c_midrecid"))) {
                    psInsert.setString(1, rs.getString("id"));
                    psInsert.setString(2, rs.getString("c_midrecid"));
                    psInsert.setString(3, rs.getString("c_filetitle"));
                    psInsert.setString(4, rs.getString("c_ftpfilepath"));
                    psInsert.setString(5, rs.getString("c_type"));
                    psInsert.setString(6, rs.getString("i_size"));
                    psInsert.setString(7, rs.getString("meta_type"));
                    psInsert.setString(8, rs.getString("status"));
                    psInsert.addBatch();
                }
            }
            psInsert.executeBatch();
            connection.commit();
        } finally {
            if (null != rs) {
                rs.close();
            }
            if (null != psInsert) {
                psInsert.close();
            }
            if (null != ps) {
                ps.close();
            }
            if (null != connection) {
                connection.close();
            }

        }
    }

    public void querySqlInsert20() throws Exception {
//        发文
        String sqlSend = "select id,subject,doc_mark,issuer,send_department,pack_date,val,create_time,year,edoc_type,org from ( " +
                "select A.id,subject,doc_mark, issuer,A.send_department,A.pack_date, 0 val, " +
                "to_char(a.create_time,'yyyyMMdd') create_time,to_char(a.create_time,'yyyy') year, " +
                "CASE A .EDOC_TYPE WHEN 0 THEN  '发文' WHEN 1 THEN  '收文' ELSE  '签报' END EDOC_TYPE,'' org " +
                "from " +
                "(select  es.* from (select * from  EDOC_SUMMARY where has_archive = 1 and EDOC_TYPE in (0,1,2)) es, " +
                "(select * from temp_number40 where content_type in (41,42,43,44,45) and  content is not null) con " +
                "where con.module_id=es.id ) " +
                "A, ( " +
                "select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from temp_number40 where to_char(content) " +
                "in (select to_char(id) from ctp_file) and CONTENT_TYPE in (41,42,43,44,45) " +
                ") zall,ctp_file cf where ZALL.CONTENT=CF.id " +
                ") b   where A.has_archive = 1 " +
                "and a.id = b.MODULE_ID " +
                "and a.EDOC_TYPE = 0 ) c";
        invokeInsert20(sqlSend);
//      收文
        String sqlReciver = "select id,subject,doc_mark,issuer,send_department,pack_date,val,create_time,year,edoc_type,org from ( " +
                "  select A.id,subject,doc_mark, issuer,A.send_department,A.pack_date, 0 val, " +
                "  to_char(a.create_time,'yyyyMMdd') create_time,to_char(a.create_time,'yyyy') year, " +
                "  CASE A .EDOC_TYPE WHEN 0 THEN  '发文' WHEN 1 THEN  '收文' ELSE  '签报' END EDOC_TYPE,'' org " +
                "  from " +
                "(select  es.* from (select * from  EDOC_SUMMARY where has_archive = 1 and EDOC_TYPE in (0,1,2)) es, " +
                "(select * from temp_number40 where content_type in (41,42,43,44,45) and  content is not null) con " +
                "where con.module_id=es.id ) " +
                "A, ( " +
                "    select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from temp_number40 where to_char(content) " +
                "    in (select to_char(id) from ctp_file) and CONTENT_TYPE in (41,42,43,44,45) " +
                "    ) zall,ctp_file cf where ZALL.CONTENT=CF.id " +
                "  ) b   where A.has_archive = 1  and a.id = b.MODULE_ID and a.EDOC_TYPE =1) c";
        invokeInsert20(sqlReciver);

//        签报
        String sqlQianbao = "select id,subject,doc_mark,issuer,send_department,pack_date,val,create_time,year,edoc_type,org from ( " +
                "select A.id,subject,doc_mark, issuer,A.send_department,A.pack_date, 0 val, " +
                "to_char(a.create_time,'yyyyMMdd') create_time,to_char(a.create_time,'yyyy') year, " +
                "CASE A .EDOC_TYPE WHEN 0 THEN  '发文' WHEN 1 THEN  '收文' ELSE  '签报' END EDOC_TYPE,'' org " +
                "from (select  es.* from (select * from  EDOC_SUMMARY where has_archive = 1 and EDOC_TYPE in (0,1,2)) es, " +
                "(select * from temp_number40 where content_type in (41,42,43,44,45) and  content is not null) con " +
                "where con.module_id=es.id ) " +
                "A, (select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from temp_number40 where to_char(content) " +
                "in (select to_char(id) from ctp_file) and CONTENT_TYPE in (41,42,43,44,45) " +
                ") zall,ctp_file cf where ZALL.CONTENT=CF.id " +
                ") b where A.has_archive = 1 " +
                "and a.id = b.MODULE_ID " +
                "and a.EDOC_TYPE =2) c";
        invokeInsert20(sqlQianbao);

    }

    //向temp_number20表中插入发文信息
    public void invokeInsert20(String sql) throws Exception {
        Connection connection = JDBCAgent.getRawConnection();
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        String insert20 = "insert into TEMP_NUMBER20(ID, SUBJECT, DOC_MARK, ISSUER,SEND_DEPARTMENT, PACK_DATE, STATUS, CREATE_TIME,YEAR, EDOC_TYPE, ORGANIZER) " +
                " values(?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement psInsert = null;
        try {
            ps = connection.prepareStatement(sql);
            resultSet = ps.executeQuery();
            psInsert = connection.prepareStatement(insert20);
            connection.setAutoCommit(false);
            HashSet<String> temp10List = queryTempNumber10Id(connection);
            while (resultSet.next()) {
                if (temp10List.contains(resultSet.getString("id"))) {
                    psInsert.setString(1, resultSet.getString("id"));
                    psInsert.setString(2, resultSet.getString("subject"));
                    psInsert.setString(3, resultSet.getString("doc_mark"));
                    psInsert.setString(4, resultSet.getString("issuer"));
                    psInsert.setString(5, resultSet.getString("send_department"));
                    psInsert.setString(6, resultSet.getString("pack_date"));
                    psInsert.setString(7, resultSet.getString("val"));
                    psInsert.setString(8, resultSet.getString("create_time"));
                    psInsert.setString(9, resultSet.getString("year"));
                    psInsert.setString(10, resultSet.getString("edoc_type"));
                    psInsert.setString(11, resultSet.getString("org"));
                    psInsert.addBatch();
                }
            }
            psInsert.executeBatch();
            connection.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != resultSet) {
                resultSet.close();
            }
            if (null != psInsert) {
                psInsert.close();
            }
            if (null != ps) {
                ps.close();
            }
            if (null != connection) {
                connection.close();
            }
        }
    }

    public HashSet<String> queryTempNumber10Id(Connection connection) throws Exception {
        String temp10 = "select id from temp_number10 t where status='0'";
        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = connection.prepareStatement(temp10);
        rs = ps.executeQuery();
        HashSet<String> list = new HashSet<>();
        while (rs.next()) {
            list.add(rs.getString("id"));
        }
        return list;
    }

}
