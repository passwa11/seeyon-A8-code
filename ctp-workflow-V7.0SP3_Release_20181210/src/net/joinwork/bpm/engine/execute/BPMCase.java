/*
 * Created on 2004-11-8
 *
 */
package net.joinwork.bpm.engine.execute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.workflow.util.MessageUtil;

import net.joinwork.bpm.definition.BPMAbstractNode;
import net.joinwork.bpm.definition.BPMSeeyonPolicy;
import net.joinwork.bpm.definition.BPMSubProcess;
import net.joinwork.bpm.engine.wapi.CaseInfo;
import net.joinwork.bpm.engine.wapi.CaseLog;
import net.joinwork.bpm.engine.wapi.NodeInfo;

/**
 * @author dinghong
 *
 */
public class BPMCase {
    
    private static Log         log                      = CtpLogFactory.getLog(BPMCase.class);

    protected Long           id;
    protected String         Name;

    private int              subProcess;

    protected String         processIndex;
    protected String         processId;
    protected String         processName;
    protected String         startUser;
    protected String         lastPerformer;

    protected List           readyStatusList;
    protected List           readyActivityList;
    protected List           readyInformActivityList;//所有处于就绪的知会节点列表
    protected List           subCaseList;
    protected List           caseLogList;

    protected Map            dataMap;
    protected Map            nodeDataMap;

    /**
     * @return the nodeDataMap
     */
    public Map getNodeDataMap() {
        return nodeDataMap;
    }

    /**
     * @param nodeDataMap the nodeDataMap to set
     */
    public void setNodeDataMap(Map nodeDataMap) {
        this.nodeDataMap = nodeDataMap;
    }

    private static int       readyStatusNum   = 0;
    private static int       readyActivityNum = 0;

    private int              state;

    protected java.util.Date startDate;
    protected java.util.Date updateDate;
    //执行任务的会话流程
    //如果父流程也是会话流程
    private BPMCase          parentCase;
    private long             workitemId;
    private BPMSubProcess    subProcessDef;
    //
    private String           endStatusId;

    protected Map            caseObject       = new HashMap();

    private static boolean   isFinishCase     = false;

    public BPMCase() {
        startDate = new java.util.Date();
        updateDate = new java.util.Date();
        state = CaseInfo.STATE_RUNNING;
        subProcess = 0;
    }

    public void copy(BPMCase theCase) {
        id = theCase.id;
        Name = theCase.Name;
        subProcess = theCase.subProcess;

        processIndex = theCase.processIndex;
        processId = theCase.processId;
        processName = theCase.processName;
        startUser = theCase.startUser;
        lastPerformer = theCase.lastPerformer;

        readyStatusList = theCase.readyStatusList;
        readyActivityList = theCase.readyActivityList;
        readyInformActivityList = theCase.readyInformActivityList;
        subCaseList = theCase.subCaseList;
        caseLogList = theCase.caseLogList;

        dataMap = theCase.dataMap;
        nodeDataMap = theCase.nodeDataMap;

        state = theCase.state;

        startDate = theCase.startDate;
        updateDate = theCase.updateDate;
        caseObject = theCase.caseObject;
    }

    /**
     * @return
     */
    public long getId() {
        return id;
    }

    /**
     * @return
     */
    public String getName() {
        return Name;
    }

    /**
     * @return
     */
    public String getProcessIndex() {
        return processIndex;
    }

    /**
     * @return
     */
    public java.util.Date getStartDate() {
        return startDate;
    }

    public long getStartDate2() {
        if (startDate == null)
            return new java.util.Date().getTime();
        return startDate.getTime();
    }

    /**
     * @return
     */
    public String getStartUser() {
        return startUser;
    }

    /**
     * @return
     */
    public int getState() {
        return state;
    }

    /**
     * @return
     */
    public java.util.Date getUpdateDate() {
        return updateDate;
    }

    public long getUpdateDate2() {
        if (updateDate == null)
            return new java.util.Date().getTime();
        return updateDate.getTime();
    }

    /**
     * @param i
     */
    public void setId(long i) {
        id = i;
    }

    /**
     * @param string
     */
    public void setName(String string) {
        Name = string;
    }

    /**
     * @param i
     */
    public void setProcessIndex(String i) {
        processIndex = i;
    }

    /**
     * @param date
     */
    public void setStartDate(java.util.Date date) {
        startDate = date;
    }

    public void setStartDate2(long date) {
        startDate = new java.util.Date(date);
    }

    /**
     * @param string
     */
    public void setStartUser(String string) {
        startUser = string;
    }

    /**
     * @param i
     */
    public void setState(int i) {
        state = i;
    }

    /**
     * @param date
     */
    public void setUpdateDate(java.util.Date date) {
        updateDate = date;
    }

    public void setUpdateDate2(long date) {
        updateDate = new java.util.Date(date);
    }

    /**
     * @return
     */
    public String getProcessId() {
        return processId;
    }

    /**
     * @return
     */
    public String getProcessName() {
        return processName;
    }

    /**
     * @param string
     */
    public void setProcessId(String string) {
        processId = string;
    }

    /**
     * @param string
     */
    public void setProcessName(String string) {
        processName = string;
    }

    /**
     * @return
     */
    public List getReadyActivityList() {
        return readyActivityList;
    }

    /**
     * @return
     */
    public List getReadyStatusList() {
        return readyStatusList;
    }

    /**
     * @param list
     */
    public void setReadyActivityList(List list) {
        readyActivityList = list;
        this.caseObject.put("activity", this.readyActivityList);
    }

    /**
     * @return the readyInformActivityList
     */
    public List getReadyInformActivityList() {
        return readyInformActivityList;
    }

    /**
     * @param readyInformActivityList the readyInformActivityList to set
     */
    public void setReadyInformActivityList(List readyInformActivityList) {
        this.readyInformActivityList = readyInformActivityList;
        this.caseObject.put("readyInformActivityList", this.readyInformActivityList);
    }

    /**
     * @param list
     */
    public void setReadyStatusList(List list) {
        readyStatusList = list;
        this.caseObject.put("status", this.readyStatusList);
    }

    /**
     * @param status
     */
    public void addReadyStatus(String statusId) {
        readyStatusList.add(statusId);
    }

    /**
     * @param activity
     */
    public void addReadyActivity(ReadyNode node) {
        String policy = node.getPolicy();
        if (null != policy) {//知会节点不算这个里面的
            if (!policy.equals(BPMSeeyonPolicy.SEEYON_POLICY_INFORM.getId())
                    && !policy.equals(BPMSeeyonPolicy.EDOC_POLICY_ZHIHUI.getId())) {
                readyActivityList.add(node);
            }else{
                log.debug("该节点为知会节点，没将该节点加入得到readyActivityList:"+node);
                if(null== readyInformActivityList){
                    readyInformActivityList= new ArrayList();
                }
                readyInformActivityList.add(node);
            }
        } else {
            readyActivityList.add(node);
        }
    }

    /**
     * @param status
     */
    public String removeReadyStatus(String statusId) {
        for (int i = 0; i < readyStatusList.size(); i++) {
            if (readyStatusList.get(i).equals(statusId)) {
                return (String) readyStatusList.remove(i);
            }
        }
        return null;
    }

    /**
     * @param activityId
     * @return
     */
    public String removeReadyActivity(String activityId) {
        if(null!=readyActivityList){
            for (int i = 0; i < readyActivityList.size(); i++) {
                ReadyNode node = (ReadyNode) readyActivityList.get(i);
                if (node.getId().equals(activityId)) {
                    readyActivityList.remove(i);
                    return node.getId();
                }
            }
        }
        if(null!=readyInformActivityList){
            for (int i = 0; i < readyInformActivityList.size(); i++) {
                ReadyNode node = (ReadyNode) readyInformActivityList.get(i);
                if (node.getId().equals(activityId)) {
                    readyInformActivityList.remove(i);
                    return node.getId();
                }
            }
        }
        return null;
    }
   
    /**
    	 * @param string
    	 * @return
    	 */
    public ReadyNode getReadyActivityById(String activityId) {
        if (readyActivityList == null)
            return null;
        if(null!=readyActivityList){
            for (int i = 0; i < readyActivityList.size(); i++) {
                ReadyNode node = (ReadyNode) readyActivityList.get(i);
                if (node.getId().equals(activityId)) {
                    return node;
                }
            }
        }
        if(null!=readyInformActivityList){
            for (int i = 0; i < readyInformActivityList.size(); i++) {
                ReadyNode node = (ReadyNode) readyInformActivityList.get(i);
                if (node.getId().equals(activityId)) {
                    return node;
                }
            }
        }
        return null;
    }

    /**
     * @return
     */
    public boolean isFinished() {
        // 所有ReadyActivity为空 
        //if ((readyActivityList == null || readyActivityList.size() > 0) && isFinishCase)
        if (readyActivityList == null || readyActivityList.size() > 0 || readyStatusList == null
                || readyStatusList.size() > 0) {
            return false;
        } else {
            state = CaseInfo.STATE_FINISHED;
            return true;
        }
    }

    public BPMCaseInfo createCaseInfo() {
        BPMCaseInfo info = new BPMCaseInfo();
        info.setCaseId(id);
        info.setCaseName(Name);
        info.setFinishDate(updateDate);
        info.setProcessName(processName);
        info.setProcessId(processId);
        info.setStartDate(startDate);
        info.setStartUser(startUser);
        info.setLastPerformer(lastPerformer);
        info.setState(state);
        info.setProcessIndex(processIndex);
        if (getSubProcess() >= 1)
            info.setIsSubCase(true);
        else
            info.setIsSubCase(false);
        if (state == CaseInfo.STATE_CANCEL)
            info.setStateName(MessageUtil.getString("CASE.STATE.CANCEL"));
        else if (state == CaseInfo.STATE_FINISHED)
            info.setStateName(MessageUtil.getString("CASE.STATE.FINISHED"));
        else if (state == CaseInfo.STATE_RUNNING)
            info.setStateName(MessageUtil.getString("CASE.STATE.STARTED"));
        else
            info.setStateName(MessageUtil.getString("CASE.STATE.SUSPEND"));

        return info;
    }

    /**
     * @return
     */
    public int getSubProcess() {
        return subProcess;
    }

    /**
     * @param b
     */
    public void setSubProcess(int b) {
        subProcess = b;
    }

    /**
     * @return
     */
    public int getStartSubProcessNum() {
        if (subCaseList == null)
            return 0;
        return subCaseList.size();
    }

    /**
     * @return
     */
    public List getSubCaseList() {
        return subCaseList;
    }

    /**
     * @param list
     */
    public void setSubCaseList(List list) {
        subCaseList = list;
        this.caseObject.put("subCase", this.subCaseList);
    }

    /**
     * @param dao
     */
    public void addSubCase(Object dao) {
        if (subCaseList == null)
            subCaseList = new ArrayList();
        subCaseList.add(dao);
    }

    public int removeSubCase(int subCaseId) {
        if (subCaseList == null)
            return 0;
        for (int i = 0; i < subCaseList.size(); i++) {
            SubCaseInfo subCase = (SubCaseInfo) subCaseList.get(i);
            if (subCaseId == subCase.caseId) {
                subCaseList.remove(i);
                break;
            }
        }
        return subCaseList.size();
    }

    public void addCaseLog(CaseLog log) {
        if (caseLogList == null) {
            caseLogList = new ArrayList();
        }
        caseLogList.add(log);
        this.caseObject.put("caseLog", this.caseLogList);
    }

    public CaseLog getLastCaseLog() {
        if (caseLogList == null || caseLogList.size() == 0)
            return null;
        else
            return (CaseLog) caseLogList.get(caseLogList.size() - 1);
    }
    /**
     * @param dataid
     * @param inputObject
     */
    private void addNodeData(String nodeId, String dataid, Object inputObject) {
        if (nodeId == null || dataid == null)
            return;
        if (nodeDataMap == null)
            nodeDataMap = new HashMap();
        Map nodeData = (Map) nodeDataMap.get(nodeId);
        if (nodeData == null) {
            nodeData = new HashMap();
            nodeDataMap.put(nodeId, nodeData);
        }
        nodeData.put(dataid, inputObject);
    }

    public Iterator getDataIterator() {
        if (dataMap == null)
            dataMap = new HashMap();
        return dataMap.values().iterator();

    }

    public Object getNextData(Iterator iter) {
        if (iter.hasNext()) {
            return iter.next();
        } else
            return null;
    }

    public Object getData(String dataKey) {
        if (dataMap == null)
            return null;
        return dataMap.get(dataKey);
    }

    /**
     * @param dataMap
     */
    public void setDataMap(Map dataMap) {
        this.dataMap = dataMap;
        this.caseObject.put("datamap", this.dataMap);
    }

    /**
     * @return
     */
    public Map getDataMap() {
        if (dataMap == null)
            dataMap = new HashMap();
        return dataMap;
    }

    /**
     * @return
     */
    public Map getNodeDataMap(String nodeId) {
        if (nodeId == null)
            return null;
        if (nodeDataMap == null)
            return null;
        return (Map) nodeDataMap.get(nodeId);
    }

    /**
     * @param map
     */
    public void setNodeDataMap(String nodeId, Map map) {
        if (nodeId == null)
            return;
        if (nodeDataMap == null && map == null)
            return;
        if (nodeDataMap == null)
            nodeDataMap = new HashMap();
        nodeDataMap.put(nodeId, map);
        this.caseObject.put("subdatamap", this.nodeDataMap);
    }

    /**
    	 * @return
    	 */
    public NodeInfo createNodeInfo(BPMAbstractNode node) {
        if (node == null)
            return null;
        BPMNodeInfo nodeInfo = new BPMNodeInfo();
        nodeInfo.setDesc(node.getDesc());
        nodeInfo.setId(node.getId());
        nodeInfo.setName(node.getName());
        return nodeInfo;
    }

    /**
     * @return
     */
    public String getLastPerformer() {
        return lastPerformer;
    }

    /**
     * @param string
     */
    public void setLastPerformer(String string) {
        lastPerformer = string;
    }

    /**
     * @return
     */
    public BPMCase getParentCase() {
        return parentCase;
    }

    /**
     * @return
     */
    public long getWorkitemId() {
        return workitemId;
    }

    /**
     * @param case1
     */
    public void setParentCase(BPMCase case1) {
        parentCase = case1;
    }

    /**
     * @param i
     */
    public void setWorkitemId(long i) {
        workitemId = i;
    }

    /**
     * @return
     */
    public BPMSubProcess getSubProcessDef() {
        return subProcessDef;
    }

    /**
     * @param process
     */
    public void setSubProcessDef(BPMSubProcess process) {
        subProcessDef = process;
    }

    /**
     * @return
     */
    public String getEndStatusId() {
        return endStatusId;
    }

    /**
     * @param string
     */
    public void setEndStatusId(String string) {
        endStatusId = string;
    }

    /**
     * @return
     */
    public List getCaseLogList() {
        return caseLogList;
    }

    /**
     * @param list
     */
    public void setCaseLogList(List list) {
        caseLogList = list;
        this.caseObject.put("caseLog", this.caseLogList);
    }

    public boolean getIsFinishCase() {
        return isFinishCase;
    }

    public void setIsFinishCase(boolean _isFinishCase) {
        isFinishCase = _isFinishCase;
    }

}
