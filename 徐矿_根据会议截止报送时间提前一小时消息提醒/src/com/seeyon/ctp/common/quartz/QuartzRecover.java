package com.seeyon.ctp.common.quartz;

import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.JDBCAgent;
import org.apache.commons.logging.Log;
import org.quartz.Job;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class QuartzRecover {
	
	private static Log log = CtpLogFactory.getLog(QuartzRecover.class);

	private static AtomicBoolean hasInitializeSql = new AtomicBoolean(false);
	
	private static final String SELECT_DISTINCT_JOB_CLASS_NAME = "select distinct JOB_CLASS_NAME from JK_JOB_DETAILS";
	
	private static String deleteSimpleTrigger ="from JK_SIMPLE_TRIGGERS st where exists(select 1 from JK_TRIGGERS tr where st.TRIGGER_NAME=tr.TRIGGER_NAME and st.TRIGGER_GROUP=tr.TRIGGER_GROUP and exists(select 1 from JK_JOB_DETAILS jd where jd.JOB_NAME=tr.JOB_NAME and jd.JOB_GROUP=tr.JOB_GROUP and jd.JOB_CLASS_NAME = ?))";
	private static String deleteCronTrigger ="from JK_CRON_TRIGGERS st where exists(select 1 from JK_TRIGGERS tr where st.TRIGGER_NAME=tr.TRIGGER_NAME and st.TRIGGER_GROUP=tr.TRIGGER_GROUP and exists(select 1 from JK_JOB_DETAILS jd where jd.JOB_NAME=tr.JOB_NAME and jd.JOB_GROUP=tr.JOB_GROUP and jd.JOB_CLASS_NAME = ?))";
	private static String deleteTrigger ="from JK_TRIGGERS tr where exists(select 1 from JK_JOB_DETAILS jd where jd.JOB_NAME=tr.JOB_NAME and jd.JOB_GROUP=tr.JOB_GROUP and jd.JOB_CLASS_NAME = ?)";

	private static String deleteRubbishSimpleTrigger ="from JK_SIMPLE_TRIGGERS st where not exists(select 1 from JK_TRIGGERS tr where st.TRIGGER_NAME=tr.TRIGGER_NAME and st.TRIGGER_GROUP=tr.TRIGGER_GROUP) or exists(select 1 from JK_TRIGGERS tr where st.TRIGGER_NAME=tr.TRIGGER_NAME and st.TRIGGER_GROUP=tr.TRIGGER_GROUP and not exists(select 1 from JK_JOB_DETAILS jd where jd.JOB_NAME=tr.JOB_NAME and jd.JOB_GROUP=tr.JOB_GROUP))";
	private static String deleteRubbishCronTrigger ="from JK_CRON_TRIGGERS st where not exists(select 1 from JK_TRIGGERS tr where st.TRIGGER_NAME=tr.TRIGGER_NAME and st.TRIGGER_GROUP=tr.TRIGGER_GROUP) or exists(select 1 from JK_TRIGGERS tr where st.TRIGGER_NAME=tr.TRIGGER_NAME and st.TRIGGER_GROUP=tr.TRIGGER_GROUP and not exists(select 1 from JK_JOB_DETAILS jd where jd.JOB_NAME=tr.JOB_NAME and jd.JOB_GROUP=tr.JOB_GROUP))";
	private static String deleteRubbishTrigger ="from JK_TRIGGERS tr where not exists(select 1 from JK_JOB_DETAILS jd where jd.JOB_NAME=tr.JOB_NAME and jd.JOB_GROUP=tr.JOB_GROUP)";
	private static String deleteRubbishJob ="from JK_JOB_DETAILS jd where not exists(select 1 from JK_TRIGGERS tr where jd.JOB_NAME=tr.JOB_NAME and jd.JOB_GROUP=tr.JOB_GROUP)";

	private static final String DELETE_JOB ="delete from JK_JOB_DETAILS where JOB_CLASS_NAME = ?";
	
	public static void clearIllegalTriggers(){
		log.info("**************** Begin Clear Illegal Triggers **************** ");
		Connection conn = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		
		int jobCount = 0;
		int triggerCount = 0;
		try {
			conn = JDBCAgent.getRawConnection();
			conn.setAutoCommit(false);
			String databaseType = conn.getMetaData().getDatabaseProductName().toUpperCase();
			initializeSql(databaseType);
			pStmt = conn.prepareStatement(SELECT_DISTINCT_JOB_CLASS_NAME);
			Set<String> badClassSet = new HashSet<String>();
			rs = pStmt.executeQuery();
			while(rs.next()){
				String clzName = rs.getString(1);
				try{
					if(!Job.class.isAssignableFrom(Class.forName(clzName))){
						log.info("----发现没有实现Job接口的JobDetail，ClassName："+ clzName);
						badClassSet.add(clzName);
					}
				}catch(ClassNotFoundException ce){
					log.info("----发现没有相应Class类的JobDetail，ClassName："+ clzName);
					badClassSet.add(clzName);
				}
			}
			closeResultSet(rs);
			closePStmt(pStmt);
			
			for(String badClzName:badClassSet){
				pStmt = conn.prepareStatement(deleteSimpleTrigger);
				pStmt.setString(1, badClzName);
				pStmt.executeUpdate();
				closePStmt(pStmt);
				
				pStmt = conn.prepareStatement(deleteCronTrigger);
				pStmt.setString(1, badClzName);
				pStmt.executeUpdate();
				closePStmt(pStmt);
				
				pStmt = conn.prepareStatement(deleteTrigger);
				pStmt.setString(1, badClzName);
				triggerCount += pStmt.executeUpdate();
				closePStmt(pStmt);
				
				pStmt = conn.prepareStatement(DELETE_JOB);
				pStmt.setString(1, badClzName);
				jobCount += pStmt.executeUpdate();
				closePStmt(pStmt);
				
			}

			pStmt = conn.prepareStatement(deleteRubbishSimpleTrigger);
			closePStmt(pStmt);

			pStmt = conn.prepareStatement(deleteRubbishCronTrigger);
			closePStmt(pStmt);

			pStmt = conn.prepareStatement(deleteRubbishTrigger);
			triggerCount += pStmt.executeUpdate();
			closePStmt(pStmt);

			pStmt = conn.prepareStatement(deleteRubbishJob);
			jobCount += pStmt.executeUpdate();
			closePStmt(pStmt);
			
			conn.commit();
			
		} catch (SQLException e) {
			rollbackConn(conn);
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			rollbackConn(conn);
			log.error(e.getMessage(), e);
		} finally{
			closeResultSet(rs);
			closePStmt(pStmt);
			closeConn(conn);
			log.info("本次共清理 "+ triggerCount +"条  triggers 和  "+ jobCount +"条 jobs");
		}
		
	}

	private static synchronized void initializeSql(String databaseType) {
		if(hasInitializeSql.get()){
			return;
		}
		if("MYSQL".equals(databaseType) || databaseType.contains("SQL SERVER")){
			deleteSimpleTrigger = "delete st " + deleteSimpleTrigger;
			deleteCronTrigger = "delete st " + deleteCronTrigger;
			deleteTrigger = "delete tr " + deleteTrigger;
			deleteRubbishSimpleTrigger = "delete st " + deleteRubbishSimpleTrigger;
			deleteRubbishCronTrigger = "delete st " + deleteRubbishCronTrigger;
			deleteRubbishTrigger = "delete tr " + deleteRubbishTrigger;
			deleteRubbishJob = "delete jd " + deleteRubbishJob;
		}else{
			deleteSimpleTrigger = "delete " + deleteSimpleTrigger;
			deleteCronTrigger = "delete " + deleteCronTrigger;
			deleteTrigger = "delete " + deleteTrigger;
			deleteRubbishSimpleTrigger = "delete " + deleteRubbishSimpleTrigger;
			deleteRubbishCronTrigger = "delete " + deleteRubbishCronTrigger;
			deleteRubbishTrigger = "delete " + deleteRubbishTrigger;
			deleteRubbishJob = "delete " + deleteRubbishJob;
		}
		hasInitializeSql.set(true);

	}

	private static void rollbackConn(Connection conn) {
		if(conn!=null){
			try {
				conn.rollback();
			} catch (SQLException e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	private static void closeConn(Connection conn) {
		if(conn!=null){
			try {
				conn.close();
			} catch (SQLException e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	private static void closeResultSet(ResultSet rs) {
		if(rs != null){
			try {
				rs.close();
			} catch (SQLException e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	private static void closePStmt(PreparedStatement pStmt) {
		if(pStmt != null){
			try {
				pStmt.close();
			} catch (SQLException e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	
}
