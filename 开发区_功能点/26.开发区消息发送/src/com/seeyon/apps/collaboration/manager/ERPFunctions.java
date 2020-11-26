//package com.seeyon.apps.collaboration.manager;
//
///**
//* <p>Title: ERPFunctions.java</p>
//* <p>Copyright: Copyright (c) 2018</p>
//* <p>Company: www.seeyon.com</p>
//* @author muj
//* @date 2018年12月18日
//* @version 1.0
//*/
//import com.seeyon.ctp.common.log.CtpLogFactory;
//import com.seeyon.ctp.util.annotation.Function;
//import org.apache.commons.logging.Log;
//
//public class ERPFunctions {
//    private static Log LOG = CtpLogFactory.getLog(CollDocFilingEnable.class);
//    @Function(title = "titlexxxx", description = "descriptionsssss", category = "wf_branch_function")
//    public static boolean isBudgetAvailable(String department, double amount) {
//        LOG.info("部门:" + department + "未超出预算.");
//        return true;
//    }
//
//    @Function(title = "titlexxxx1111", description = "descriptionsssss111", category = "wf_branch_function")
//    public static boolean checkMember(String member, int month) {
//        LOG.info("人员" + member + " " + month + "全勤");
//        return false;
//    }
//}