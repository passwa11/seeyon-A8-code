//package com.seeyon.v3x.edoc.dao;
//
//import com.seeyon.ctp.util.DBAgent;
//import com.seeyon.v3x.edoc.domain.CtpPdfSavepath;
//import org.hibernate.Session;
//import org.hibernate.SessionFactory;
//import org.hibernate.Transaction;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * 周刘成   2019/4/9
// */
//public class CtpPdfSavepathDaoImpl implements CtpPdfSavepathDao {
//    @Override
//    public void insertCtpPdfSavepath(CtpPdfSavepath ctpPdfSavepath) {
//        DBAgent.save(ctpPdfSavepath);
//    }
//
//    @Override
//    public CtpPdfSavepath selectCtpPdfSavepathBySummaryId(String edocSummaryId) {
//        return DBAgent.get(CtpPdfSavepath.class,Long.parseLong(edocSummaryId));
//    }
//}
