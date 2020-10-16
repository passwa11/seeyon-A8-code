/*
 * Created on 2004-5-17
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.joinwork.bpm.engine.wapi;

import java.util.Date;
import java.util.List;
import java.util.Map;

import net.joinwork.bpm.definition.BPMActivity;
import net.joinwork.bpm.definition.BPMHumenActivity;
import net.joinwork.bpm.definition.BPMParticipant;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.definition.ReadyObject;
import net.joinwork.bpm.engine.execute.BPMCase;

import com.seeyon.ctp.workflow.exception.BPMException;

/**
 * 
 * <p>Title: 工作流（V3XWorkflow）</p>
 * <p>Description: 工作流引擎接口</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: 北京致远协创软件有限公司</p>
 * <p>Author: wangchw
 * <p>Time: 2012-7-2 下午02:56:09
 */
public interface ProcessEngine {

    public static String VERSION = "2.0";

    /**
    * 得到流程引擎ID.
    * @return 引擎的作用域
    */
    public String getDomain();

    /**
     * 设置流程引擎ID.
     * @param domain 流程引擎ID
     */
    public void setDomain(String domain) throws BPMException;

    /**
     * 撤销流程:将处于生成或运行状态的流程实例转为取消状态。处于取消状态中的流程不能在运行，流程实例相关的工作项也被一同取消。
     * @param caseId 要取消的流程实例Id
     * @return
     * @throws BPMException
     */
    public int cancelCase(WorkflowBpmContext context) throws BPMException;

    /**
     * 取回流程
     * 
     * 用来表示本次调用结果代码:
     * -2(流程中已有节点进行终止操作),
     * -1(流程不允许回退),
     * 0(正常回退),
     * 1(流程撤销)
     * 2(回退至上一指定节点)
     * 3(流程不允许撤销)
     * @param workitemId
     * @return
     * @throws BPMException
     */
    public int takeBack(WorkflowBpmContext context) throws BPMException;

    /**
     * 终止流程
     * @param workitemId
     * @throws BPMException
     */
    public void stopCase(WorkflowBpmContext context) throws BPMException;

    /**
     * 启动流程实例(自由流程).
     * @param appName
     * @param processXml
     * @param startUserId
     * @param startUserName
     * @param startAccountId
     * @param startAccountName
     * @return
     * @throws BPMException
     */
    public String[] runCase(WorkflowBpmContext context) throws BPMException;

    /**
     * 启动流程实例(模版流程).
     * @param appName
     * @param processTempateId
     * @param startUserId
     * @param startUserName
     * @param startAccountId
     * @param startAccountName
     * @return
     * @throws BPMException
     */
    public String[] runCaseFromTemplate(WorkflowBpmContext context) throws BPMException;

    /**
     * 处理任务事项.
     * @param appName 应用类型：collabortion表示协同,edoc表示公文等
     * @param currentUserId 当前处理用户
     * @param currentWorkitemId 当前sub_object_id
     * @param currentAccountId 当前处理用户所属单位
     * @param statusId 选择的上一回退节点id
     * @param sysAutoFinishTag true表示自动跳过;false表示人工处理
     * @throws BPMException
     */
    public String[] finishWorkItem(WorkflowBpmContext context) throws BPMException;

    /**
     * 标志任务事项为读取状态
     * @param itemId 任务事项ID
     * @throws BPMException
     */
    public void readWorkItem(Long workitemId) throws BPMException;

    /**
     * 获得流程实例相关的流程模板.
     * @param userId 查询流程实例模板的用户ID
     * @param caseId 流程实例Id
     * @return 流程模板
     * @throws BPMException
     */
    public ProcessObject getCaseProcess(Long caseId) throws BPMException;

    /**
     * 获得流程实例运行过程日志.
     * 用户只能察看到有权限访问的纪录
     * @param userId 查询流程实例过程日志的用户ID
     * @param caseId 流程实例Id
     * @return 包含CaseRunLog对象的List。如果指定的caseId不存在返回NULL值。
     * @throws BPMException
     * @see CaseRunLog
     */

    //public List getCaseRunLog(String userId, int caseId) throws BPMException;

    /**
     * 获得指定流程实例中处于就绪状态的活动实例.
     * 用户只能察看到有权限访问的纪录
     * @param userId 查询的用户ID
     * @param caseId 流程实例Id
     * @return 包含ReadyActivity对象的List。如果指定的caseId不存在返回NULL值。
     * @throws BPMException
     * @see ReadyActivity
     */

    //	public List getReadyActivity(String userId, int caseId)
    //		throws BPMException;

    /**
     * 查询所用与用户相关的流程实例.
     * @param userId 用户Id
     * @param processId 指定流程实例的模板Id。如果为""或NULL，查所有模板相关的流程
     * @param processName 指定流程实例的模板名称。如果为""或NULL，查所有模板相关的流程
     * @param caseName 指定流程实例的描述。如果为""或NULL，不作为查询条件
     * @param startUser 指定启动此流程实例的用户。如果为""或NULL，查所有用户启动的流程
     * @param startDateAfter 查询在此日期（包含此日期）之后启动的流程，可以为空
     * @param startDateBefore 查询在此日期（包含此日期）之前启动的流程，可以为空
     * @param state 指定流程当前所处的状态，为0表示所有状态
     * @param begin 指定返回结果的起始序号
     * @param length 指定希望返回的最大纪录条数
     * @return 包含CaseInfo对象的List。即使没有符合条件的结果，也会返回一个空LIST，而不是一个NULL值
     * @see CaseInfo
     */
    public List getCaseList(String userId, String processId, String processName, String caseName, String startUser,
            Date startDateAfter, Date startDateBefor, int state, int begin, int length) throws BPMException;

    /**
             * 查询流程实例的运行日志.
             * @param userId 用户Id
             * @param caseId 指定的流程实例Id
             * @return 包含CaseLog对象的List。即使没有符合条件的结果，也会返回一个空LIST，而不是一个NULL值
             * @see CaseLog
             */
    public List getCaseLogList(String userId, Long caseId) throws BPMException;

    /**
    * 查询流程实例的运行日志.
    * XML格式作为流程运行监控图的输入数据
    * @param userId 用户Id
    * @param caseId 指定的流程实例Id
    * @return XML字符串
    */
    public String getCaseLogXML(String userId, Long caseId) throws BPMException;

    public String getHisCaseLogXML(String userId, Long caseId) throws BPMException;

    /**
    * 查询流程实例的流程模板定义.
    * XML格式作为流程运行监控图的输入数据
    * @param processId 指定的流程定义的processId
    * @return XML字符串
    */
    public String getCaseProcessXML(String processId) throws BPMException;

    public String getHisCaseProcessXML(String processId) throws BPMException;

    /**
     * 查询流程实例的流程模板定义.
     * XML格式作为流程运行监控图的输入数据
     * @param userId 用户Id
     * @param caseId 指定的流程实例Id
     * @return XML字符串
     */
    public String getCaseProcessXML(String userId, Long caseId) throws BPMException;

    /**
     * 查询指定流程实例的信息.
     * @param userId 用户Id
     * @param caseId 指定的流程实例Id
     * @return CaseInfo。没有符合条件的结果，返回NULL
     * @see CaseInfo
     */
    public CaseInfo getCaseInfo(String userId, Long caseId) throws BPMException;

    /**
     * 暂存待办
     * @param itemId
     * @throws BPMException
     */
    public void temporaryPending(WorkflowBpmContext context) throws BPMException;

    /**
             * 查询所用与用户相关的已完成流程实例.
             * @param userId 用户Id
             * @param processId 指定流程实例的模板Id。如果为""或NULL，查所有模板相关的流程
             * @param processName 指定流程实例的模板名称。如果为""或NULL，查所有模板相关的流程
             * @param caseName 指定流程实例的描述。如果为""或NULL，不作为查询条件
             * @param startUser 指定启动此流程实例的用户。如果为""或NULL，查所有用户启动的流程
             * @param finishDateAfter 查询在此日期（包含此日期）之后完成的流程，可以为空
             * @param finishDateBefore 查询在此日期（包含此日期）之前完成的流程，可以为空
             * @param state 指定流程当前所处的状态，为0表示所有状态
             * @param begin 指定返回结果的起始序号
             * @param length 指定希望返回的最大纪录条数
             * @return 包含CaseInfo对象的List。即使没有符合条件的结果，也会返回一个空LIST，而不是一个NULL值
             * @see CaseInfo
             */
    public List getHistoryCaseList(String userId, String processId, String processName, String caseName,
            String startUser, Date finishDateAfter, Date finishDateBefor, int state, int begin, int length)
            throws BPMException;

    /**
     * 得到指定节点的所有下游人工活动节点.
     * @param processIndex 流程模板编号
     * @param fromNodeId 指定节点
     * @return 包含NodeInfo对象的List。
     */
    public List getNextHumenActivitis(Long caseId, String fromNodeId) throws BPMException;

    /** 删除节点
     * Edit by jincm
     * @param userId
     * @param process
     * @param theCase
     * @param activity
     * @throws BPMException
     * @return 0: 正常删除 1：删除异常
     */
    public int deleteActivity(String userId, BPMProcess process, BPMCase theCase, BPMActivity activity,
            boolean frontad, WorkflowBpmContext context) throws BPMException;

    /**
     * 使指定的activity列表处于ready状态
     * @param userId
     * @param process
     * @param theCase
     * @param activity
     * @return
     * @throws BPMException
     */
    public void addReadyActivity(String userId, BPMProcess process, BPMCase theCase, List<BPMActivity> activity,
            WorkflowBpmContext context,boolean isChange) throws BPMException;

    /**
     * seeyon >>>
     * 根据 caseId取到 BPMCase对象。如果没找到，就从history里找。
     * @param caseId
     * @return
     * @throws BPMException
     */
    public BPMCase getCase(Long caseId) throws BPMException;

    /**
     * 得到人员图
     * @param userId
     * @param theCase
     * @return
     * @throws BPMException
     */
    //public String getPeopleMap(String userId,
    //BPMCase theCase)throws BPMException;

    public List<BPMParticipant> getParticipantByActivity(String userId, BPMCase theCase, BPMActivity activity)
            throws BPMException;

    /**
     * 获取运行中的process，先从缓存中读取
     * @param processId
     * @return
     * @throws BPMException
     */
    public BPMProcess getProcessRunningById(String processId) throws BPMException;

    public BPMProcess getHisProcessRunningById(String processId) throws BPMException;

    /**
     * 
     * @param processId
     * @param isFromCache 是否从缓存中读取
     * @return
     * @throws BPMException
     */
    public BPMProcess getProcessRunningById(String processId, boolean isFromCache) throws BPMException;

    /**
     * 更新运行中的process
     * @param processId
     * @return
     * @throws BPMException
     */
    public BPMProcess updateRunningProcess(BPMProcess process,BPMCase theCase,WorkflowBpmContext context) throws BPMException;

    /**
     * 更新修改中的process
     * @param process
     * @return
     * @throws BPMException
     */
    public void updateModifyingProcess(BPMProcess process) throws BPMException;

    /**
     * 删除修改中的process
     * @param process
     * @return
     * @throws BPMException
     */
    public void delModifyingProcess(String processId, String userId) throws BPMException;

    /**
     * 检查当前流程是否正处于被修改状态
     * @param process
     * @return
     * @throws BPMException
     */
    public String checkModifyingProcess(String processId, String userId) throws BPMException;

    public Map<Long, ReadyObject> getReadyAddedMap();

    public void setReadyAddedMap(Map<Long, ReadyObject> readyAddedMap);

    /**
     * 用于获取缓存中的流程
     * @return
     */
    public Map getUpdateProcessMap();

    /**
     * 用于缓存修改中的流程
     * @return
     */
    public void setUpdateProcessMap(Map updateProcessMap);

    /**
    * 查询修改中流程实例的流程模板定义.
    * XML格式作为流程运行监控图的输入数据
    * @param userId 用户Id
    * @param caseId 指定的流程实例Id
    * @return XML字符串
    */
    public String getModifyingProcessXML(String userId, String processId) throws BPMException;

    public void addReadyActivityMap(String processId, ReadyObject readyObject) throws BPMException;

    public ReadyObject getReadyObject(String processId, String userId) throws BPMException;

    public void addHisProcess(String processId) throws BPMException;

    public void saveHisCase(BPMCase case1) throws BPMException;

    /**
     * 物理删除case
     * 
     * @param theCase
     * @throws BPMException
     */
    public void deleteCase(BPMCase theCase) throws BPMException;

    /**
     * 物理删除process
     * @param processId
     * @throws BPMException
     */
    public void deleteProcess(String processId) throws BPMException;

    /**
     * 回退流程
     * @param selectTargetNodeId 
     * @param workitemId 
     * @param activityId 
     * @param processId 
     */
    public String[] stepBack(WorkflowBpmContext context) throws BPMException;

    /**
     * 加签、会签、知会接口
     * @param processId 流程id
     * @param currentActivityId 当前活动节点Id
     * @param targetActivityId 目标活动节点Id（将要执行加签或知会、会签动作的节点Id）
     * @param userId 执行加签操作的用户Id
     * @param changeType 流程修改类型（1加签、2知会、3当前会签）
     * @param message 流程修改信息
     * @param baseProcessXML 该参数可以为null，此时针对id=processId的工作流执行流程修改操作
     * @param baseReadyObjectJSON 该参数可以为null，此时表示没有针对当前流程的Ready状态的节点
     * 如果不为空，并且格式符合BPMProcess的话，在该xml的基础上执行流程修改操作。
     * @param messageDataList 流程操作日志列表
     * @return 流程修改后生成的xml
     * @throws BPMException 抛出异常
     */
    public String[] addNode(String processId, String currentActivityId, String targetActivityId, String userId,
            int changeType, Map<Object, Object> message, String baseProcessXML, String baseReadyObjectJSON,
            String messageDataList, String changeMessageJSON) throws BPMException;

    /**
     * 加签、会签、知会接口
     * @param processId 流程id
     * @param currentActivityId 当前活动节点Id
     * @param targetActivityId 目标活动节点Id（将要执行加签或知会、会签动作的节点Id）
     * @param userId 执行加签操作的用户Id
     * @param changeType 流程修改类型（1加签、2知会、3当前会签）
     * @param message 流程修改信息
     * @param baseProcessXML 该参数可以为null，此时针对id=processId的工作流执行流程修改操作
     * @param baseReadyObjectJSON 该参数可以为null，此时表示没有针对当前流程的Ready状态的节点
     * 如果不为空，并且格式符合BPMProcess的话，在该xml的基础上执行流程修改操作。
     * @param messageDataList 流程操作日志列表
     * @param addHumanNodes 新增节点列表，传入空列表， 自动填值
     * @return 流程修改后生成的xml
     * @throws BPMException 抛出异常
     */
    public String[] addNode(String processId, String currentActivityId, String targetActivityId, String userId, int changeType,
            Map<Object, Object> message, String baseProcessXML, String baseReadyObjectJSON, String messageDataList,
            String changeMessageJSON, List<BPMHumenActivity> addHumanNodes) throws BPMException;
    
    /**
     * 减签接口
     * @param processId 流程id
     * @param currentActivityId 当前活动节点Id
     * @param userId 执行加签操作的用户Id
     * @param activityIdList 将要被减签的活动节点Id
     * @param baseProcessXML 该参数可以为null，此时针对id=processId的工作流执行流程修改操作；
     * 如果不为空，并且格式符合BPMProcess的话，在该xml的基础上执行流程修改操作。
     * @param messageDataList 流程减签日志
     * @param summaryId
     * @param affairId
     * @return 流程修改后生成的xml
     * @throws BPMException 抛出异常
     */
    public String[] deleteNode(String processId, String currentActivityId, String userId, List<String> activityIdList,
            String baseProcessXML,String messageDataList, String changeMessageJSON, String summaryId,String affairId) throws BPMException;

    /**
     * 预减签接口
     * @param processId 流程id
     * @param currentActivityId 当前活动节点Id
     * @param baseProcessXML 该参数可以为null，此时针对id=processId的工作流执行流程修改操作；
     * 如果不为空，并且格式符合BPMProcess的话，在该xml的基础上执行流程修改操作。
     * @return 流程修改后生成的xml
     * @throws BPMException 抛出异常
     */
    public List<BPMHumenActivity> preDeleteNode(String processId, String currentActivityId, String baseProcessXML)
            throws BPMException;

    /**
     * 唤醒流程
     * @param caseId 将要被唤醒的流程实例Id
     * @throws BPMException 抛出异常
     */
    public void awakeCase(WorkflowBpmContext context) throws BPMException;

    /**
     * 激活新增加的节点或删除节点
     * @param process
     * @param userId
     * @param context
     * @param theCase
     * @throws BPMException
     */
    public void saveAcitivityModify(BPMProcess process, String userId, WorkflowBpmContext context, BPMCase theCase,boolean isChange)
            throws BPMException;

    /**
     * 直接提交给某个节点
     * @param context
     * @return
     * @throws BPMException
     */
    public String[] runCaseToMe(WorkflowBpmContext context) throws BPMException;
}
