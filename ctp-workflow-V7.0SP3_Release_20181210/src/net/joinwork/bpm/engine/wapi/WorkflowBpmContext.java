/**
 * Id: SeeyonBpmContext.java, v1.0 2012-6-30 wangchw Exp
 * Copyright (c) 2011 Seeyon, Ltd. All rights reserved
 */
package net.joinwork.bpm.engine.wapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ctp.workflow.util.WorkflowUtil;
import com.seeyon.ctp.workflow.vo.BPMChangeMergeVO;

import net.joinwork.bpm.definition.BPMAbstractNode;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.engine.execute.BPMCase;
import net.joinwork.bpm.engine.execute.DynamicFormMasterInfo;
import net.joinwork.bpm.task.WorkitemInfo;

/**
 * 
 * <p>Title: 工作流（V3XWorkflow）</p>
 * <p>Description: 代码描述</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: 北京致远协创软件有限公司</p>
 * <p>Author: wangchw
 * <p>Time: 2012-7-2 上午11:22:43
 */
public class WorkflowBpmContext {
    
    /**
     * 粘贴复制节点人员情况
     */
    private Map<String, String[]> copyNodePeopleMap= new HashMap<String, String[]>();
    
    /**
     * 所有没选中的节点集合
     */
    private Set<String> allNotSelectNodes= new HashSet<String>();
    
    /**
     * 是否校验锁和可提交
     */
    private boolean isValidate= false;
    
    /**
     * 上下文版本标识
     */
    private String version= "2.0";
	
	/**
	 * 流程类别标识：协同、表单、公文、信息报送（政务扩展）等(可根据业务模块扩展)
	 */
	private String appName= null;
	
	/**
	 * 应用模块业务主键值
	 */
	private String bussinessId= null;
	
	/**
	 * 事项ID
	 */
	private String affairId = null;
	/**
	 * 工作流流程定义xml内容
	 */
	private String processXml= null;
	
	/**
	 * 流程模版ID
	 */
	private String processTemplateId= null;
	
	/**
	 * 是否为子流程
	 */
	private boolean isSubProcess= false;
	
	/**
     * 发起者人员id
     */
    private String startUserId= null;
    
    /**
     * 发起者人员名称
     */
    private String startUserName= null;
    
    /**
     * 发起者所在单位id
     */
    private String startAccountId= null;
    
    /**
     * 发起者所在单位名称
     */
    private String startAccountName= null;

    /**
     * 流程发起节点id(默认为start，可以不传)
     */
    private String startStatusId= "start";
	
	/**
	 * 当前节点id
	 */
	private String currentActivityId= null;
	
	/**
	 * 当前任务事项id
	 */
	private long currentWorkitemId= -1l;
	
	/**
	 * 当前处理人id
	 */
	private String currentUserId= null;
	
	/**
     * 当前处理人名称
     */
    private String currentUserName= null;
	
	/**
	 * 当前处理人所在单位id
	 */
	private String currentAccountId= null;
	
	/**
	 * 当前处理人所在单位名称
	 */
	private String currentAccountName= null;
	
	/**
	 * 当前人员选择信息<nodeid,[userid1,userid2,...,useridn]>
	 */
	private String selectedPeoplesOfNodes= null;
	
	/**
	 * 当前分支选择信息<nodeid,"true/false">或者<linkid,"true/false">
	 */
	private String conditionsOfNodes= null;
	
	/**
     * 下一节点id（可以通过该参数指定跳转到某一个指定节点进行流转）
     */
    private String nextActivityId= null;
    
    /**
     * 选择目标回退节点
     */
    private String selectTargetNodeId= null;
	
	/**
	 * 流程定义模版id
	 */
	private String processId= null;
	
	/**
	 * 流程实例名称
	 */
	private String caseName= null;
	
	/**
	 * 流程实例ID
	 */
	private long caseId= -1l;
	
	/**
	 * 业务模块唯一标识对象(ColSummary或EdocSummary等)
	 */
	private Object appObject= null;
	
	/**
     * 业务数据集合(包括跨表)
     */
    private Map<String, Object> businessData= new HashMap<String, Object>();
	
	/**
	 * 结构化表单数据串
	 */
	private String formData= null;
	
	/**
	 * 表单主键值ID
	 */
	private String mastrid= null;
	
	/**
	 * 工作流模版定义对象
	 */
	private BPMProcess process= null;
	
	/**
	 * 工作流流程实例对象
	 */
	private BPMCase theCase= null;
	
	/**
	 * 引擎内部要当前处理的人工活动节点
	 */
	private BPMAbstractNode activateNode= null;
	
	/**
	 * 是否发送消息
	 */
	private boolean isSendMessage= false;
	
	/**
	 * 是否查找节点替换人员
	 */
	private boolean isFindReplaceNodeUser= false;
	
	/**
	 * 存放工作流引擎内部生成的workitem对象的主键值
	 */
	private List<String> subObjectIds= new ArrayList<String>();
	
	/**
	 * 是否为调试模式
	 */
	private boolean isDebugMode= false;
	
	/**
	 * 是否为移动端请求
	 */
	private boolean isMobile= false;
	
	/**
	 * 是否为自动跳过模式
	 */
	private boolean sysAutoFinishFlag= false;
	
	/**
	 * 节点触发子流程所必须的数据
	 */
	private String popNodeSubProcessJson;
	
	/**
	 * 当前会签json字符串
	 */
	private String readyObjectJson = null;
	
	/**
	 * 下节点信息
	 */
	private StringBuffer nextMembers= null;
	
	/**
	 * 下节点信息，不包含节点权限名称
	 */
	private StringBuffer nextMembersWithoutPolicyInfo= null;
	
	/**
	 * 是否流程发起就处于结束状态
	 */
	private boolean isStartFinished= false;
	
	/**
	 * 本次调用所有需要assgin的workitem列表
	 */
	private List<EventDataContext> eventDataContextList= new ArrayList<EventDataContext>();
	
	/**
	 * 将要被删除的流程节点Id集合
	 */
	private Set<String> willDeleteNodes= new HashSet<String>();
	
	/**
     * 将要被删除的subObjectId集合
     */
    private Set<Long> willDeleteSubObjectIds= new HashSet<Long>();
    
    /**
     * 将要被删的workitem集合
     */
    private List<WorkitemInfo> willDeleteWorkItems= new ArrayList<WorkitemInfo>();
    
    /**
     * 是否使用批量删除特性
     */
    private boolean isBatchCancel= true;
    
    //本次加签之前所添加或删除的节点信息，需要与本次加签信息合并
    private String changeMessageJSON;
    
    //本次加签之前所添加或删除的节点信息转换成的对象。
    private BPMChangeMergeVO ceMevo;
    /**
     * 是否是流程仿真模式
     */
    private String simulationId = null;
    
    /**
     * 子流程能否跳过第一个发起节点
     */
    private boolean canSubProcessSkipFSender = false;
    
    /**
     * 缓存节点上面的选人信息，以便统一存储到case_run的caseObject大字段中去，主要为回退功能找人服务
     */
    private Map<String,String> nodeAdditionMap= new HashMap<String, String>();
    /**
     * 缓存节点上面的选人信息，以便统一存储到case_run的caseObject大字段中去，主要为流程节点复制功能找人服务
     */
    private Map<String,String> nodeRAdditionMap= new HashMap<String, String>();
    /**
     * 缓存节点上面的分支匹配结果信息，以便统一存储到case_run的caseObject大字段中去，主要为流程分支判断和流程分支显示功能服务
     */
    private Map<String,Map<String,String>> nodeConditionChangeInfoMap= new HashMap<String, Map<String,String>>();
    
    private Map<String,DynamicFormMasterInfo> dynamicFormMap = new HashMap<String,DynamicFormMasterInfo>();
    
    /**
     * 本次处理需要选人的节点集合
     */
    private Map<String, Map<String,Object>> needSelectPeopleNodeMap= new HashMap<String,Map<String,Object>>();
    
    /**
     * split->join映射表
     */
    private Map<String,String> splitJoinMap= new HashMap<String, String>();
    
    
    /**
     * 是否需要由直接提交给我变化为流程重走的模式。
     */
    private boolean isToReGo ;
    
    /** 是否是CAP4表单 **/
    private boolean isCAP4 = false;
    
    /**
     * 表单ID
     */
    private String formAppId;
    
    
    private String formViewOperation; 
    
    /**
     * 是否为自由流程
     */
    private boolean isFreeFlow= false;
    
    /**
     * 每次人员匹配唯一标识ID（性能优化）
     */
    private String matchRequestToken="";
    
    /**
     * 自动跳过判定节点ID
     */
    private String autoSkipNodeId= "";
    
    /**
     * 自动跳过判定节点名称
     */
    private String autoSkipNodeName= "";
    
    /**
     * 校验环节WorkflowMatchLogMessageConstants.step5或WorkflowMatchLogMessageConstants.step6:只有超期和合并处理用到，其它地方不允许使用这个参数
     */
    private String validateStep= "";
    
    /*流程动态路径匹配，底表数据Id,数据结构：底表FormAppId|节点ID|数据记录1ID#数据记录2ID,底表FormAppId|数据记录1ID#数据记录2ID 例如：123|1#2#,32|3#4#*/
    private String dynamicFormMasterIds ;
    
    /**
     * 是否用当前时间激活锁的有效时间  ("true"/"false")
     */
    private String useNowExpirationTime;
    /**
     * 节点人员匹配抛出的阻塞提示信息
     */
    private String humenNodeMatchAlertMsg;
    
    /*是否是当前节点的最后一个处理人*/
    private String currentNodeLast ;
    
    
    public String getCurrentNodeLast() {
        return currentNodeLast;
    }

    public void setCurrentNodeLast(String currentNodeLast) {
        this.currentNodeLast = currentNodeLast;
    }

    public String getDynamicFormMasterIds() {
		return dynamicFormMasterIds;
	}

	public void setDynamicFormMasterIds(String dynamicFormMasterIds) {
		this.dynamicFormMasterIds = dynamicFormMasterIds;
	}

	public boolean isCanSubProcessSkipFSender() {
        return canSubProcessSkipFSender;
    }

    public void setCanSubProcessSkipFSender(boolean canSubProcessSkipFSender) {
        this.canSubProcessSkipFSender = canSubProcessSkipFSender;
    }

    public boolean isToReGo() {
		return isToReGo;
	}

	public String getUseNowExpirationTime() {
		return useNowExpirationTime;
	}

	public void setUseNowExpirationTime(String useNowExpirationTime) {
		this.useNowExpirationTime = useNowExpirationTime;
	}

	public void setToReGo(boolean isToReGo) {
		this.isToReGo = isToReGo;
	}

	public String getChangeMessageJSON() {
		return changeMessageJSON;
	}

	public void setChangeMessageJSON(String changeMessageJSON) {
		this.changeMessageJSON = changeMessageJSON;
		if(WorkflowUtil.isNotNull(changeMessageJSON)){
			ceMevo = BPMChangeMergeVO.parseBPMChangeMergeVOJSON(changeMessageJSON);
		}
	}

	public BPMChangeMergeVO getCeMevo() {
		return ceMevo;
	}

	public void setCeMevo(BPMChangeMergeVO ceMevo) {
		this.ceMevo = ceMevo;
	}

    /**
     * @return the eventDataContextList
     */
    public List<EventDataContext> getEventDataContextList() {
        return eventDataContextList;
    }

    /**
     * @param eventDataContextList the eventDataContextList to set
     */
    public void setEventDataContextList(List<EventDataContext> eventDataContextList) {
        this.eventDataContextList = eventDataContextList;
    }

    /**
     * @return the isStartFinished
     */
    public boolean isStartFinished() {
        return isStartFinished;
    }

    /**
     * @param isStartFinished the isStartFinished to set
     */
    public void setStartFinished(boolean isStartFinished) {
        this.isStartFinished = isStartFinished;
    }

    /**
     * @return the nextMembersWithoutPolicyInfo
     */
    public StringBuffer getNextMembersWithoutPolicyInfo() {
        return nextMembersWithoutPolicyInfo;
    }

    /**
     * @param nextMembersWithoutPolicyInfo the nextMembersWithoutPolicyInfo to set
     */
    public void setNextMembersWithoutPolicyInfo(StringBuffer nextMembersWithoutPolicyInfo) {
        this.nextMembersWithoutPolicyInfo = nextMembersWithoutPolicyInfo;
    }

    /**
	 * 回退后的节点提交方式
	 */
	private String submitStyleAfterStepBack= null;
	
	private String mainCaseId= null;
	
	private List<Long> mainNextNodeIds= null;
	
	/**
	 * 是否需要在发起者之后自动增加一个自身节点
	 */
	private boolean isAddFirstNode= false;
	
	private long subProcessRunningId;
	
	private boolean isProcessChanged= false;

	/**
	 * 正常回退到的目标节点集合
	 */
    private List<String> normalStepBackTargetNodes= new ArrayList<String>();

	/**
     * @return the isProcessChanged
     */
    public boolean isProcessChanged() {
        return isProcessChanged;
    }

    /**
     * @param isProcessChanged the isProcessChanged to set
     */
    public void setProcessChanged(boolean isProcessChanged) {
        this.isProcessChanged = isProcessChanged;
    }

    /**
     * @return the subProcessRunningId
     */
    public long getSubProcessRunningId() {
        return subProcessRunningId;
    }

    /**
     * @param subProcessRunningId the subProcessRunningId to set
     */
    public void setSubProcessRunningId(long subProcessRunningId) {
        this.subProcessRunningId = subProcessRunningId;
    }

    /**
     * @return the isAddFirstNode
     */
    public boolean isAddFirstNode() {
        return isAddFirstNode;
    }

    /**
     * @param isAddFirstNode the isAddFirstNode to set
     */
    public void setAddFirstNode(boolean isAddFirstNode) {
        this.isAddFirstNode = isAddFirstNode;
    }

    /**
     * @return the mainNextNodeIds
     */
    public List<Long> getMainNextNodeIds() {
        return mainNextNodeIds;
    }

    /**
     * @param mainNextNodeIds the mainNextNodeIds to set
     */
    public void setMainNextNodeIds(List<Long> mainNextNodeIds) {
        this.mainNextNodeIds = mainNextNodeIds;
    }

    /**
     * @return the mainCaseId
     */
    public String getMainCaseId() {
        return mainCaseId;
    }

    /**
     * @return the submitStyleAfterStepBack
     */
    public String getSubmitStyleAfterStepBack() {
        return submitStyleAfterStepBack;
    }

    /**
     * @param submitStyleAfterStepBack the submitStyleAfterStepBack to set
     */
    public void setSubmitStyleAfterStepBack(String submitStyleAfterStepBack) {
        this.submitStyleAfterStepBack = submitStyleAfterStepBack;
    }

    /**
     * @return the nextMembers
     */
    public StringBuffer getNextMembers() {
        return nextMembers;
    }

    /**
     * @param nextMembers the nextMembers to set
     */
    public void setNextMembers(StringBuffer nextMembers) {
        this.nextMembers = nextMembers;
    }

    public WorkflowBpmContext(){
        
    }

    public String getPopNodeSubProcessJson() {
        return popNodeSubProcessJson;
    }

    public void setPopNodeSubProcessJson(String popNodeSubProcessJson) {
        this.popNodeSubProcessJson = popNodeSubProcessJson;
    }

    /**
	 * @return the currentActivityId
	 */
	public String getCurrentActivityId() {
		return currentActivityId;
	}

	/**
	 * @param currentActivityId the currentActivityId to set
	 */
	public void setCurrentActivityId(String currentActivityId) {
		this.currentActivityId = currentActivityId;
	}

	/**
	 * @return the processId
	 */
	public String getProcessId() {
		return processId;
	}

	/**
	 * @param processId the processId to set
	 */
	public void setProcessId(String processId) {
		this.processId = processId;
	}

	/**
	 * @return the isSubProcess
	 */
	public boolean isSubProcess() {
		return isSubProcess;
	}

	/**
	 * @param isSubProcess the isSubProcess to set
	 */
	public void setSubProcess(boolean isSubProcess) {
		this.isSubProcess = isSubProcess;
	}

	/**
	 * @return the currentWorkitemId
	 */
	public long getCurrentWorkitemId() {
		return currentWorkitemId;
	}

	/**
	 * @param currentWorkitemId the currentWorkitemId to set
	 */
	public void setCurrentWorkitemId(long currentWorkitemId) {
		this.currentWorkitemId = currentWorkitemId;
	}

    /**
     * @return the nextActivityId
     */
    public String getNextActivityId() {
        return nextActivityId;
    }

    /**
     * @param nextActivityId the nextActivityId to set
     */
    public void setNextActivityId(String nextActivityId) {
        this.nextActivityId = nextActivityId;
    }

    /**
     * @return the caseName
     */
    public String getCaseName() {
        return caseName;
    }

    /**
     * @param caseName the caseName to set
     */
    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }

    /**
     * @return the startStatusId
     */
    public String getStartStatusId() {
        return startStatusId;
    }

    /**
     * @param startStatusId the startStatusId to set
     */
    public void setStartStatusId(String startStatusId) {
        this.startStatusId = startStatusId;
    }

    /**
     * @return the startUserId
     */
    public String getStartUserId() {
        return startUserId;
    }

    /**
     * @param startUserId the startUserId to set
     */
    public void setStartUserId(String startUserId) {
        this.startUserId = startUserId;
    }

    /**
     * @return the startAccountId
     */
    public String getStartAccountId() {
        return startAccountId;
    }

    /**
     * @param startAccountId the startAccountId to set
     */
    public void setStartAccountId(String startAccountId) {
        this.startAccountId = startAccountId;
    }

    /**
     * @return the process
     */
    public BPMProcess getProcess() {
        return process;
    }

    /**
     * @param process the process to set
     */
    public void setProcess(BPMProcess process) {
        this.process = process;
    }

    /**
     * @return the theCase
     */
    public BPMCase getTheCase() {
        return theCase;
    }

    /**
     * @param theCase the theCase to set
     */
    public void setTheCase(BPMCase theCase) {
        this.theCase = theCase;
    }

    /**
     * @return the currentAccountId
     */
    public String getCurrentAccountId() {
        return currentAccountId;
    }

    /**
     * @param currentAccountId the currentAccountId to set
     */
    public void setCurrentAccountId(String currentAccountId) {
        this.currentAccountId = currentAccountId;
    }

    /**
     * @return the isSendMessage
     */
    public boolean isSendMessage() {
        return isSendMessage;
    }

    /**
     * @param isSendMessage the isSendMessage to set
     */
    public void setSendMessage(boolean isSendMessage) {
        this.isSendMessage = isSendMessage;
    }

    /**
     * @return the subObjectIds
     */
    public List<String> getSubObjectIds() {
        return subObjectIds;
    }

    /**
     * @param subObjectIds the subObjectIds to set
     */
    public void setSubObjectIds(List<String> subObjectIds) {
        this.subObjectIds = subObjectIds;
    }

    /**
     * @return the activateNode
     */
    public BPMAbstractNode getActivateNode() {
        return activateNode;
    }

    /**
     * @param activateNode the activateNode to set
     */
    public void setActivateNode(BPMAbstractNode activateNode) {
        this.activateNode = activateNode;
    }

    /**
     * @return the isDebugMode
     */
    public boolean isDebugMode() {
        return isDebugMode;
    }

    /**
     * @param isDebugMode the isDebugMode to set
     */
    public void setDebugMode(boolean isDebugMode) {
        this.isDebugMode = isDebugMode;
    }

    /**
     * @return the appName
     */
    public String getAppName() {
        return appName;
    }

    /**
     * @param appName the appName to set
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }

    /**
     * @return the startUserName
     */
    public String getStartUserName() {
        return startUserName;
    }

    /**
     * @param startUserName the startUserName to set
     */
    public void setStartUserName(String startUserName) {
        this.startUserName = startUserName;
    }

    /**
     * @return the startAccountName
     */
    public String getStartAccountName() {
        return startAccountName;
    }

    /**
     * @param startAccountName the startAccountName to set
     */
    public void setStartAccountName(String startAccountName) {
        this.startAccountName = startAccountName;
    }

    /**
     * @return the appObject
     */
    public Object getAppObject() {
        return appObject;
    }

    /**
     * @param appObject the appObject to set
     */
    public void setAppObject(Object appObject) {
        this.appObject = appObject;
    }

    /**
     * @return the currentUserId
     */
    public String getCurrentUserId() {
        return currentUserId;
    }

    /**
     * @param currentUserId the currentUserId to set
     */
    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    /**
     * @return the currentUserName
     */
    public String getCurrentUserName() {
        return currentUserName;
    }

    /**
     * @param currentUserName the currentUserName to set
     */
    public void setCurrentUserName(String currentUserName) {
        this.currentUserName = currentUserName;
    }

    /**
     * @return the currentAccountName
     */
    public String getCurrentAccountName() {
        return currentAccountName;
    }

    /**
     * @param currentAccountName the currentAccountName to set
     */
    public void setCurrentAccountName(String currentAccountName) {
        this.currentAccountName = currentAccountName;
    }

    /**
     * @return the processXml
     */
    public String getProcessXml() {
        return processXml;
    }

    /**
     * @param processXml the processXml to set
     */
    public void setProcessXml(String processXml) {
        this.processXml = processXml;
    }

    /**
     * @return the processTemplateId
     */
    public String getProcessTemplateId() {
        return processTemplateId;
    }

    /**
     * @param processTemplateId the processTemplateId to set
     */
    public void setProcessTemplateId(String processTemplateId) {
        this.processTemplateId = processTemplateId;
    }

    /**
     * @return the caseId
     */
    public long getCaseId() {
        return caseId;
    }

    /**
     * @param caseId the caseId to set
     */
    public void setCaseId(long caseId) {
        this.caseId = caseId;
    }

    /**
     * @return the sysAutoFinishFlag
     */
    public boolean isSysAutoFinishFlag() {
        return sysAutoFinishFlag;
    }

    /**
     * @param sysAutoFinishFlag the sysAutoFinishFlag to set
     */
    public void setSysAutoFinishFlag(boolean sysAutoFinishFlag) {
        this.sysAutoFinishFlag = sysAutoFinishFlag;
    }

    /**
     * @return the selectTargetNodeId
     */
    public String getSelectTargetNodeId() {
        return selectTargetNodeId;
    }

    /**
     * @param selectTargetNodeId the selectTargetNodeId to set
     */
    public void setSelectTargetNodeId(String selectTargetNodeId) {
        this.selectTargetNodeId = selectTargetNodeId;
    }

    /**
     * @return the formData
     */
    public String getFormData() {
        return formData;
    }

    /**
     * @param formData the formData to set
     */
    public void setFormData(String formData) {
        this.formData = formData;
    }

    /**
     * @return the mastrid
     */
    public String getMastrid() {
        return mastrid;
    }

    /**
     * @param mastrid the mastrid to set
     */
    public void setMastrid(String mastrid) {
        this.mastrid = mastrid;
    }

    /**
     * @return the bussinessId
     */
    public String getBussinessId() {
        return bussinessId;
    }

    /**
     * @param bussinessId the bussinessId to set
     */
    public void setBussinessId(String bussinessId) {
        this.bussinessId = bussinessId;
    }
    
    /**
     * @return the businessData
     */
    public Map<String, Object> getBusinessData() {
        return businessData;
    }
    
    /**
     * @return the businessData
     */
    public Object getBusinessData(String key) {
        return businessData.get(key);
    }

    /**
     * @param businessData the businessData to set
     */
    public void setBusinessData(String key,Object value) {
        this.businessData.put(key, value);
    }

    /**
     * @return the readyObjectJson
     */
    public String getReadyObjectJson() {
        return readyObjectJson;
    }

    /**
     * @param readyObjectJson the readyObjectJson to set
     */
    public void setReadyObjectJson(String readyObjectJson) {
        this.readyObjectJson = readyObjectJson;
    }

    /**
     * @return the selectedPeoplesOfNodes
     */
    public String getSelectedPeoplesOfNodes() {
        return selectedPeoplesOfNodes;
    }

    /**
     * @param selectedPeoplesOfNodes the selectedPeoplesOfNodes to set
     */
    public void setSelectedPeoplesOfNodes(String selectedPeoplesOfNodes) {
        this.selectedPeoplesOfNodes = selectedPeoplesOfNodes;
    }

    /**
     * @return the conditionsOfNodes
     */
    public String getConditionsOfNodes() {
        return conditionsOfNodes;
    }

    /**
     * @param conditionsOfNodes the conditionsOfNodes to set
     */
    public void setConditionsOfNodes(String conditionsOfNodes) {
        this.conditionsOfNodes = conditionsOfNodes;
    }

    public void setMainCaseId(String mainCaseId) {
        this.mainCaseId= mainCaseId;
    }

    /**
     * @param businessData the businessData to set
     */
    public void setBusinessData(Map<String, Object> businessData) {
        this.businessData = businessData;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the nodeAdditionMap
     */
    public Map<String, String> getNodeAdditionMap() {
        return nodeAdditionMap;
    }

    /**
     * @param nodeAdditionMap the nodeAdditionMap to set
     */
    public void setNodeAdditionMap(Map<String, String> nodeAdditionMap) {
        this.nodeAdditionMap = nodeAdditionMap;
    }

    /**
     * @return the nodeRAdditionMap
     */
    public Map<String, String> getNodeRAdditionMap() {
        return nodeRAdditionMap;
    }

    /**
     * @param nodeRAdditionMap the nodeRAdditionMap to set
     */
    public void setNodeRAdditionMap(Map<String, String> nodeRAdditionMap) {
        this.nodeRAdditionMap = nodeRAdditionMap;
    }

    /**
     * @return the nodeConditionChangeInfoMap
     */
    public Map<String, Map<String, String>> getNodeConditionChangeInfoMap() {
        return nodeConditionChangeInfoMap;
    }

    /**
     * @param nodeConditionChangeInfoMap the nodeConditionChangeInfoMap to set
     */
    public void setNodeConditionChangeInfoMap(Map<String, Map<String, String>> nodeConditionChangeInfoMap) {
        this.nodeConditionChangeInfoMap = nodeConditionChangeInfoMap;
    }

    public void setNormalStepBackTargetNodes(List<String> nodeIdList) {
        this.normalStepBackTargetNodes= nodeIdList;
    }

    /**
     * @return the normalStepBackTargetNodes
     */
    public List<String> getNormalStepBackTargetNodes() {
        return normalStepBackTargetNodes;
    }

    public Map<String, DynamicFormMasterInfo> getDynamicFormMap() {
		return dynamicFormMap;
	}

	public void setDynamicFormMap(Map<String, DynamicFormMasterInfo> dynamicFormMap) {
		this.dynamicFormMap = dynamicFormMap;
	}

	/**
     * @return the affairId
     */
    public String getAffairId() {
        return affairId;
    }

    /**
     * @param affairId the affairId to set
     */
    public void setAffairId(String affairId) {
        this.affairId = affairId;
    }

    /**
     * @return the isMobile
     */
    public boolean isMobile() {
        return isMobile;
    }

    /**
     * @param isMobile the isMobile to set
     */
    public void setMobile(boolean isMobile) {
        this.isMobile = isMobile;
    }

    /**
     * @return the isValidate
     */
    public boolean isValidate() {
        return isValidate;
    }

    /**
     * @param isValidate the isValidate to set
     */
    public void setIsValidate(boolean isValidate) {
        this.isValidate = isValidate;
    }

    /**
     * @return the copyNodePeopleMap
     */
    public Map<String, String[]> getCopyNodePeopleMap() {
        return copyNodePeopleMap;
    }

    /**
     * @param copyNodePeopleMap the copyNodePeopleMap to set
     */
    public void setCopyNodePeopleMap(Map<String, String[]> copyNodePeopleMap) {
        this.copyNodePeopleMap = copyNodePeopleMap;
    }

    /**
     * @return the allNotSelectNodes
     */
    public Set<String> getAllNotSelectNodes() {
        return allNotSelectNodes;
    }

    /**
     * @param allNotSelectNodes the allNotSelectNodes to set
     */
    public void setAllNotSelectNodes(Set<String> allNotSelectNodes) {
        this.allNotSelectNodes = allNotSelectNodes;
    }

    /**
     * @return the willDeleteNodes
     */
    public Set<String> getWillDeleteNodes() {
        return willDeleteNodes;
    }

    /**
     * @param willDeleteNodes the willDeleteNodes to set
     */
    public void setWillDeleteNodes(Set<String> willDeleteNodes) {
        this.willDeleteNodes = willDeleteNodes;
    }

    /**
     * @return the willDeleteSubObjectIds
     */
    public Set<Long> getWillDeleteSubObjectIds() {
        return willDeleteSubObjectIds;
    }

    /**
     * @param willDeleteSubObjectIds the willDeleteSubObjectIds to set
     */
    public void setWillDeleteSubObjectIds(Set<Long> willDeleteSubObjectIds) {
        this.willDeleteSubObjectIds = willDeleteSubObjectIds;
    }

    /**
     * @return the isBatchCancel
     */
    public boolean isBatchCancel() {
        return isBatchCancel;
    }

    /**
     * @param isBatchCancel the isBatchCancel to set
     */
    public void setBatchCancel(boolean isBatchCancel) {
        this.isBatchCancel = isBatchCancel;
    }

    /**
     * @return the willDeleteWorkItems
     */
    public List<WorkitemInfo> getWillDeleteWorkItems() {
        return willDeleteWorkItems;
    }

    /**
     * @param willDeleteWorkItems the willDeleteWorkItems to set
     */
    public void setWillDeleteWorkItems(List<WorkitemInfo> willDeleteWorkItems) {
        this.willDeleteWorkItems = willDeleteWorkItems;
    }

    /**
     * @return the needSelectPeopleNodeMap
     */
    public Map<String, Map<String,Object>> getNeedSelectPeopleNodeMap() {
        return needSelectPeopleNodeMap;
    }

    /**
     * @param needSelectPeopleNodeMap the needSelectPeopleNodeMap to set
     */
    public void setNeedSelectPeopleNodeMap(Map<String, Map<String,Object>> needSelectPeopleNodeMap) {
        this.needSelectPeopleNodeMap = needSelectPeopleNodeMap;
    }

    /**
     * @return the isFindReplaceNodeUser
     */
    public boolean isFindReplaceNodeUser() {
        return isFindReplaceNodeUser;
    }

    /**
     * @param isFindReplaceNodeUser the isFindReplaceNodeUser to set
     */
    public void setFindReplaceNodeUser(boolean isFindReplaceNodeUser) {
        this.isFindReplaceNodeUser = isFindReplaceNodeUser;
    }

    /**
     * @return the splitJoinMap
     */
    public Map<String, String> getSplitJoinMap() {
        return splitJoinMap;
    }

    /**
     * @param splitJoinMap the splitJoinMap to set
     */
    public void setSplitJoinMap(Map<String, String> splitJoinMap) {
        this.splitJoinMap = splitJoinMap;
    }


	public String getSimulationId() {
		return simulationId;
	}

	public void setSimulationId(String simulationId) {
		this.simulationId = simulationId;
	}

	public void setValidate(boolean isValidate) {
		this.isValidate = isValidate;
	}

	public String getFormAppId() {
		return formAppId;
	}

	public void setFormAppId(String formAppId) {
		this.formAppId = formAppId;
	}

	public boolean isFreeFlow() {
		return isFreeFlow;
	}

	public void setFreeFlow(boolean isFreeFlow) {
		this.isFreeFlow = isFreeFlow;
	}

	public String getMatchRequestToken() {
		return matchRequestToken;
	}

	public void setMatchRequestToken(String matchRequestToken) {
		this.matchRequestToken = matchRequestToken;
	}

	public String getAutoSkipNodeId() {
		return autoSkipNodeId;
	}

	public void setAutoSkipNodeId(String autoSkipNodeId) {
		this.autoSkipNodeId = autoSkipNodeId;
	}

	public String getAutoSkipNodeName() {
		return autoSkipNodeName;
	}

	public void setAutoSkipNodeName(String autoSkipNodeName) {
		this.autoSkipNodeName = autoSkipNodeName;
	}

	public String getValidateStep() {
		return validateStep;
	}

	public void setValidateStep(String validateStep) {
		this.validateStep = validateStep;
	}

	public String getHumenNodeMatchAlertMsg() {
		return humenNodeMatchAlertMsg;
	}
	
	public String getHumenNodeMatchAlertMsgById(String activityId){
		if(Strings.isBlank(humenNodeMatchAlertMsg)){
			return "";
		}else{
			Map<String,String> nodeMatchMsgMap =  JSONUtil.parseJSONString(humenNodeMatchAlertMsg, Map.class);
			return nodeMatchMsgMap.get(activityId);
		}
	}
	
	public void setHumenNodeMatchAlertMsg(String activityId,String msg){
		if(Strings.isNotBlank(msg) && Strings.isNotBlank(activityId)){
			Map<String,String> nodeMatchMsgMap = new HashMap<String, String>();
			if(Strings.isNotBlank(humenNodeMatchAlertMsg)){
				nodeMatchMsgMap = JSONUtil.parseJSONString(humenNodeMatchAlertMsg, Map.class);
			}
			nodeMatchMsgMap.put(activityId, msg);
			humenNodeMatchAlertMsg = JSONUtil.toJSONString(nodeMatchMsgMap);
		}
	}
	public void setHumenNodeMatchAlertMsg(String humenNodeMatchAlertMsg) {
		this.humenNodeMatchAlertMsg = humenNodeMatchAlertMsg;
	}

    public String getFormViewOperation() {
        
        if(formViewOperation == null)
            formViewOperation = "";
        
        return formViewOperation;
    }

    public void setFormViewOperation(String formViewOperation) {
        this.formViewOperation = formViewOperation;
    }

    public boolean isCAP4() {
        return isCAP4;
    }

    public void setCAP4(boolean isCAP4) {
        this.isCAP4 = isCAP4;
    }
}
