/**
* $Author: tanmf $
* $Rev: 49624 $
* $Date:: 2015-05-27 14:13:19#$:
*
* Copyright (C) 2012 Seeyon, Inc. All rights reserved.
*
* This software is the proprietary information of Seeyon, Inc.
* Use is subject to license terms.
*/
package com.seeyon.ctp.workflow.manager.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.workflow.dao.IBPMCaseDao;
import com.seeyon.ctp.workflow.dao.ICaseRunDao;
import com.seeyon.ctp.workflow.engine.listener.ActionRunner;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.exception.DatabaseException;
import com.seeyon.ctp.workflow.manager.CaseManager;
import com.seeyon.ctp.workflow.manager.ProcessManager;
import com.seeyon.ctp.workflow.po.CaseRunDAO;
import com.seeyon.ctp.workflow.po.HistoryCaseRunDAO;
import com.seeyon.ctp.workflow.util.WorkflowUtil;
import com.seeyon.ctp.workflow.xml.StringXMLElement;

import net.joinwork.bpm.definition.BPMActivity;
import net.joinwork.bpm.definition.BPMHumenActivity;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.engine.execute.BPMCase;
import net.joinwork.bpm.engine.execute.BPMCaseInfo;
import net.joinwork.bpm.engine.wapi.CaseDetailLog;
import net.joinwork.bpm.engine.wapi.CaseInfo;
import net.joinwork.bpm.engine.wapi.CaseLog;

/**
 * 
 * <p>Title: T4工作流</p>
 * <p>Description: 代码描述</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: seeyon.com</p>
 * <p>Author: wangchw</p>
 * @since CTP2.0
 */
public class CaseManagerImpl implements CaseManager {
    
    private static Log logger = CtpLogFactory.getLog(CaseManagerImpl.class);
    
    private IBPMCaseDao bpmCaseDao;
    private ICaseRunDao caseRunDao;
    private ProcessManager processManager;

    /**
     * @return the processManager
     */
    public ProcessManager getProcessManager() {
        return processManager;
    }

    /**
     * @param processManager the processManager to set
     */
    public void setProcessManager(ProcessManager processManager) {
        this.processManager = processManager;
    }

    public CaseManagerImpl() {
        //do nothing;
    }

    /**
     * @return the caseRunDao
     */
    public ICaseRunDao getCaseRunDao() {
        return caseRunDao;
    }

    /**
     * @param caseRunDao the caseRunDao to set
     */
    public void setCaseRunDao(ICaseRunDao caseRunDao) {
        this.caseRunDao = caseRunDao;
    }

    /**
     * @return the bpmCaseDao
     */
    public IBPMCaseDao getBpmCaseDao() {
        return bpmCaseDao;
    }

    /**
     * @param bpmCaseDao the bpmCaseDao to set
     */
    public void setBpmCaseDao(IBPMCaseDao bpmCaseDao) {
        this.bpmCaseDao = bpmCaseDao;
    }

    /**
     * 新增流程实例到wf_case_run或wf_case_history表中
     * @param theCase
     * @return
     * @throws BPMException
     */
    public long addCase(BPMCase theCase) throws BPMException {
        long caseId = -1l;
        Map caseObject = new HashMap();
        caseObject.put("status", theCase.getReadyStatusList());
        caseObject.put("datamap", theCase.getDataMap());
        //caseObject.put("subdatamap", theCase.getNodeDataMap());
        caseObject.put("activity", theCase.getReadyActivityList());
        caseObject.put("readyInformActivityList", theCase.getReadyInformActivityList());
        //caseObject.put("subCase", theCase.getSubCaseList());
        caseObject.put("caseLog", theCase.getCaseLogList());
        CaseRunDAO caseRun = (CaseRunDAO) theCase;
        if (theCase.getState() == CaseInfo.STATE_FINISHED) {
            HistoryCaseRunDAO hcase = new HistoryCaseRunDAO();
            hcase.copy(caseRun);
            hcase.setCaseObject(caseObject);
            hcase.setUpdateDate(new Date(System.currentTimeMillis()));
            caseId = caseRunDao.addHistoryCase(hcase);
        } else {
            caseRun.setCaseObject(caseObject);
            caseId = caseRunDao.addCase(caseRun);
        }
        return caseId;
    }

    /**
     * 创建流程实例对象,但不保存到数据库表中
     * @param process 流程模版内容
     * @param startUserId 发起者ID
     * @param caseName 流程实例名称
     * @param isSubProcess 是否为子流程
     * @return BPMCase
     * @throws BPMException
     */
    public BPMCase createCase(BPMProcess process, String startUserId, String caseName, boolean isSubProcess)
            throws BPMException {
        CaseRunDAO dao = new CaseRunDAO();
        dao.setId(WorkflowUtil.getTableKey());
        if (caseName == null || "".equals(caseName.trim())) {
            caseName = String.valueOf(dao.getId());
        }
        dao.setName(caseName);
        dao.setStartUser(startUserId);
        dao.setProcessIndex(process.getIndex());
        dao.setProcessId(process.getId());
        dao.setProcessName(process.getName());
        dao.setReadyActivityList(new ArrayList());
        dao.setReadyInformActivityList(new ArrayList());
        dao.setReadyStatusList(new ArrayList());
        //流程是否为子流程
        if (isSubProcess) {
            dao.setSubProcess(1);
        } else {
            dao.setSubProcess(0);
        }
        return dao;
    }

    /**
     * 根据流程实例ID查询流程实例信息,如果wf_case_run中没有,则到wf_case_history中去查询
     * @param caseId 流程实例ID
     * @return
     * @throws BPMException
     */
    public BPMCase getCase(long caseId) throws BPMException{
    	return getCase(caseId, false);
    }
    
    public BPMCase getCase(long caseId, boolean isHistoryFlag) throws BPMException {
    	if(isHistoryFlag){
    	    ICaseRunDao caseRunDaoFK = (ICaseRunDao)AppContext.getBean("ICaseRunDaoFK");
    	    if(caseRunDaoFK!=null){
    	        CaseRunDAO caserun = caseRunDaoFK.getCasebyId(caseId);
    	        if (null != caserun) {
    	            return caserun;
    	        }
    	        HistoryCaseRunDAO casehistory = caseRunDaoFK.getHistoryCasebyId(caseId);
    	        if (null != casehistory) {
                  return casehistory;
    	        }
    	    }
    	}else{
	        CaseRunDAO caserun = caseRunDao.getCasebyId(caseId);
	        if (null != caserun) {
	            return caserun;
	        }
	        HistoryCaseRunDAO casehistory = caseRunDao.getHistoryCasebyId(caseId);
	        if (null != casehistory) {
	            return casehistory;
	        }
    	}
        return null;
    }

    /**
     * 根据流程实例ID从wf_case_history中查询流程实例信息
     * @param caseId 流程实例ID
     * @return
     * @throws BPMException
     */
    public BPMCase getHistoryCase(long caseId) throws BPMException {
        return caseRunDao.getHistoryCasebyId(caseId);
    }
    public BPMCase getHistoryCaseHis(long caseId) throws BPMException {
    	if(AppContext.hasPlugin("fk")){
    		ICaseRunDao caseRunDaoFK = (ICaseRunDao)AppContext.getBean("ICaseRunDaoFK");
    		if(caseRunDaoFK!=null){
    			return caseRunDaoFK.getHistoryCasebyId(caseId);
    		}
    	}
        return null;
    }

    /**
     * 根据流程实例ID从wf_his_case_run中查询流程实例信息
     * @param caseId 流程实例ID
     * @return
     * @throws BPMException
     */
    public BPMCase getHisCase(long caseId) throws BPMException{
    	return getHisCase(caseId, false);
    }
    public BPMCase getHisCase(long caseId, boolean isHistoryFlag) throws BPMException {
      if(isHistoryFlag){
          ICaseRunDao caseRunDaoFK = (ICaseRunDao)AppContext.getBean("ICaseRunDaoFK");
          if(caseRunDaoFK!=null){
              caseRunDaoFK.getHisCasebyId(caseId);
          }
      }
        return caseRunDao.getHisCasebyId(caseId);
    }

    /**
     * 根据流程实例ID从wf_his_case_history中查询流程实例信息
     * @param caseId 流程实例ID
     * @return
     * @throws BPMException
     */
    public BPMCase getHisHistoryCase(long caseId) throws BPMException {
        try {
            return caseRunDao.getHisHistoryCasebyId(caseId);
        } catch (DatabaseException e) {
            throw new BPMException(BPMException.EXCEPTION_CODE_DATABASE_ERROR);
        }
    }

    /**
     * 根据流程实例ID从wf_case_run中查询流程实例信息,不查大字段，速度快
     * @param caseId 流程实例ID
     * @return
     * @throws BPMException
     */
    public BPMCase getCaseInfo(long caseId) throws BPMException {
        try {
            return bpmCaseDao.getCasebyId(caseId);
        } catch (DatabaseException e) {
            throw new BPMException(BPMException.EXCEPTION_CODE_DATABASE_ERROR);
        }
    }

    /**
     * @param theCase
     */
    public void _save(BPMCase theCase, boolean isUpdateState) throws BPMException {
        if (isUpdateState) {
            theCase.isFinished();
        }
        caseRunDao.save(theCase);
    }

    /**
     * @param theCase
     */
    public void save(BPMCase theCase) throws BPMException {
        theCase.isFinished();
        caseRunDao.save(theCase);
    }

    /**
     * @param theCase
     */
    public void cancel(BPMCase theCase) throws BPMException {
        theCase.setState(CaseInfo.STATE_CANCEL);
        caseRunDao.save(theCase);
    }

    public void stop(BPMCase theCase) throws BPMException {
        theCase.setState(CaseInfo.STATE_STOP);
        caseRunDao.save(theCase);
    }

    /**
     * @param theCase
     * @param isSuspend
     */
    public void suspend(BPMCase theCase, boolean isSuspend) throws BPMException {
        if (isSuspend) {
            theCase.setState(CaseInfo.STATE_SUSPEND);
        } else {
            theCase.setState(CaseInfo.STATE_RUNNING);
        }
        save(theCase);
    }

    /**
     * @param processId
     * @return
     */
    public List<? extends BPMCase> getCaseByProcessId(String processId) throws BPMException {
        List<CaseRunDAO> caseList= caseRunDao.getCaseListByProcessId(processId);
        if(null==caseList || caseList.size()<=0){
            List<HistoryCaseRunDAO> historyCaseList= new ArrayList<HistoryCaseRunDAO>();
            HistoryCaseRunDAO historyCase= caseRunDao.getHistoryCaseByProcessId(processId);
            historyCaseList.add(historyCase);
            return historyCaseList;
        }else{
            return caseList;
        }
    }

    /**
     * 
     * @param processIndex
     * @return
     * @throws BPMException
     */
    public List<? extends BPMCase> getCaseByProcessIndex(String processIndex) throws BPMException {
        return caseRunDao.getCaseListByProcessIndex(processIndex);
    }

    public void remove(BPMCase theCase) throws BPMException {
        caseRunDao.remove(theCase);
    }

    public void saveHisCase(BPMCase case1) throws BPMException {
        caseRunDao.saveHisCase(case1);
    }

    public List<BPMCaseInfo> getProcessCaseList() throws BPMException {
        List<BPMCaseInfo> result = new ArrayList<BPMCaseInfo>();
        List<BPMCaseInfo> list1 = bpmCaseDao.getCaseList(null, null, null, null, 0, -1, 0);
        List<BPMCaseInfo> list2 = bpmCaseDao.getHistoryCaseList(null, null, null, null, 0, -1, 0);
        if (null != list1) {
            result.addAll(list1);
        }
        if (null != list2) {
            result.addAll(list2);
        }
        return result;
    }

    @Override
    public List<BPMCaseInfo> getHistoryCaseList(String processId, String processName, String caseName,
            String startUser, int state, int begin, int length) throws BPMException {
        return bpmCaseDao.getHistoryCaseList(processId, processName, caseName, startUser, state, begin, length);
    }

    @Override
    public List<BPMCaseInfo> getCaseList(String processId, String processName, String caseName, String startUser,
            int state, int begin, int length) throws BPMException {
        return bpmCaseDao.getCaseList(processId, processName, caseName, startUser, state, begin, length);
    }

    @Override
    public String[] getCaseLogXml(BPMCase theCase) throws BPMException {
        String caseLogXml = "";
        String debugCaseLogXML = "";
        String stepCountStr= "0";
        if (null != theCase) {
            List list = theCase.getCaseLogList();
            
            StringXMLElement rootNode = new StringXMLElement("caseLog");
            StringXMLElement caseLog4Debug = new StringXMLElement("caseLog");
            
            if (list != null) {
                
                //给前台输出完整的caseLog方便数据分析
                for (int i = 0; i < list.size(); i++) {
                    //这是为了分析流程用的
                    CaseLog caseLog = (CaseLog) list.get(i);
                    caseLog.toXML(caseLog4Debug);
                }
                
                //过滤重复， 前端性能优化
                List<CaseLog> caseLogs = new ArrayList<CaseLog>(list.size());
                Map<String, String> caseIdMap = new HashMap<String, String>(list.size());
                Map<String, String> detailLogListMap = new HashMap<String, String>(list.size() * 2);
                for(int i = list.size() - 1; i > -1; i--){
                    
                    CaseLog caseLog = (CaseLog) list.get(i);
                    
                    boolean hasSub = false;
                    List<CaseDetailLog> detailLogList = caseLog.getDetailLogList();
                    if (detailLogList != null && detailLogList.size() > 0) {
                        //做了封装，这种做法有问题， 先这么过滤
                        Collections.reverse(detailLogList);
                        Iterator<CaseDetailLog> it = detailLogList.iterator();
                        while(it.hasNext()){
                            CaseDetailLog detailLog = it.next();
                            if(detailLogListMap.containsKey(detailLog.nodeId)){
                                it.remove();
                            }else{
                                hasSub = true;
                                detailLogListMap.put(detailLog.nodeId, detailLog.nodeId);
                            }
                        }
                    }
                    
                    if(!caseIdMap.containsKey(caseLog.getNodeId()) || hasSub){
                        caseIdMap.put(caseLog.getNodeId(), caseLog.getNodeId());
                        caseLogs.add(caseLog);
                    }
                }
                
                //重新排序
                Collections.reverse(caseLogs);
                
                for (int i = 0; i < caseLogs.size(); i++) {
                    CaseLog caseLog = (CaseLog) caseLogs.get(i);
                    caseLog.toXML(rootNode);
                }
                /*for (int i = 0; i < list.size(); i++) {
                    CaseLog caseLog = (CaseLog) list.get(i);
                    caseLog.toXML(rootNode);
                }*/
            }
            caseLogXml = rootNode.toString();
            int stepCount = theCase.getDataMap().get(ActionRunner.STEPBACK_COUNT) == null ? 0 : Integer.valueOf(String
                    .valueOf(theCase.getDataMap().get(ActionRunner.STEPBACK_COUNT)));
            stepCountStr= String.valueOf(stepCount);
        }
        String[] result= new String[3];
        result[0]= caseLogXml;
        result[1]= stepCountStr;
        result[2] = debugCaseLogXML;
        return result;
    }

    @Override
    public void updateCase(BPMCase theCase) throws BPMException {
        caseRunDao.save(theCase);
    }

    @Override
    public void deleteCase(long caseId) throws BPMException {
        caseRunDao.deleteCase(caseId);
    }
    public void deleteHistoryCasebyId(long caseId) throws BPMException{
        caseRunDao.deleteHistoryCasebyId(caseId);
    }

    @Override
    public Map<String, String[]> getNodesStatus(long caseId) throws BPMException {
        BPMCase theCase = null;
        Map<String,String[]> result= new HashMap<String, String[]>();
        theCase = getCase(caseId);
        if(theCase == null){
        	theCase = getHistoryCase(caseId);
        }
        if(theCase == null){
        	theCase = this.getHistoryCaseHis(caseId);
        }
        
        if (null != theCase) {
            String processId= theCase.getProcessId();
            BPMProcess process= processManager.getRunningProcess(processId);
            if(null==process){
                process= processManager.getHisRunningProcess(processId);
            }
            List list = theCase.getCaseLogList();
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    CaseLog caseLog = (CaseLog) list.get(i);
                    List<CaseDetailLog> detailLogList= caseLog.getDetailLogList();
                    if (detailLogList != null) {
                        for (int k = 0; k < detailLogList.size(); k++) {
                            CaseDetailLog detailLog = (CaseDetailLog) detailLogList.get(k);
                            int myState= detailLog.getState();
                            String myNodeId= detailLog.nodeId;
                            boolean isHumenNode= false;
                            BPMActivity activity=  null;
                            if(!"start".equals(myNodeId) && !"end".equals(myNodeId)){
                                activity= process.getActivityById(myNodeId);
                                if(null!=activity && activity instanceof BPMHumenActivity){
                                    isHumenNode= true;
                                }
                            }
                            if(isHumenNode){
                                String nodeName= activity.getName();
                                String nodeStateArr[]= result.get(myNodeId);
                                if(null== nodeStateArr){
                                    
                                    if(null!=myNodeId && !"".equals(myNodeId.trim())){
                                        nodeStateArr= new String[3];
                                        nodeStateArr[0]= String.valueOf(myState);
                                        nodeStateArr[1]= String.valueOf(false);
                                        nodeStateArr[2] = nodeName;
                                        result.put(myNodeId,nodeStateArr);
                                    }
                                }else{
                                    String nodeStateStr= nodeStateArr[0];
                                    if(null!=myNodeId && !"".equals(myNodeId.trim())){
                                        if(null== nodeStateStr){
                                            nodeStateArr[0]= String.valueOf(myState);
                                        }else{
                                            int nodeState= 1;
                                            if(WorkflowUtil.isLong(nodeStateStr)){
                                                nodeState= Integer.parseInt(nodeStateStr);
                                            }
                                            if(nodeState != 6 || nodeState==2){
                                                nodeStateArr[0]= String.valueOf(myState);
                                            }
                                        }
                                        
                                        nodeStateArr[1]= String.valueOf(false);
                                        result.put(myNodeId,nodeStateArr);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public BPMCase getCaseOrHistoryCaseByProcessId(String processId) throws BPMException {
        CaseRunDAO caserun = caseRunDao.getCaseByProcessId(processId);
        if (null != caserun) {
            return caserun;
        }
        HistoryCaseRunDAO casehistory = caseRunDao.getHistoryCaseByProcessId(processId);
        if (null != casehistory) {
            return casehistory;
        }
        return null;
    }
    
    @Override
	public void updateCaseOnly(BPMCase theCase) throws BPMException {
		caseRunDao.update(theCase);
	}
}
