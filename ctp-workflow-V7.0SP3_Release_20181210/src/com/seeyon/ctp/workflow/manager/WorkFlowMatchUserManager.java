/**
 * Author: wangchw
 * Rev: WorkFlowMatchUserManager.java
 * Date: 20122012-7-3下午09:44:30
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
*/
package com.seeyon.ctp.workflow.manager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.seeyon.ctp.common.po.processlog.ProcessLogDetail;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.po.WFProcessProperty;
import com.seeyon.ctp.workflow.vo.User;
import com.seeyon.ctp.workflow.bo.WorkflowFormFieldBO;
import com.seeyon.ctp.workflow.vo.WorkflowMatchLogVO;
import com.seeyon.ctp.workflow.wapi.WorkflowNodeUsersMatchResult;

import net.joinwork.bpm.definition.BPMHumenActivity;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;

/**
 * <p>Title: 工作流（V3XWorkflow）</p>
 * <p>Description: 工作流人员匹配接口</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: 北京致远协创软件有限公司</p>
 * <p>Author: wangchw
 * <p>Time: 2012-7-3 下午09:44:30
*/
public interface WorkFlowMatchUserManager {
    
    
    /**
     * 当前节点，值为{@value}。    
     */
    public static final String ORGENT_META_KEY_CURRENT_NODE = "CurrentNode";
    
    /**
     * 当前节点上级部门，值为{@value}。    
     */
    public static final String ORGENT_META_KEY_CURRENT_NODE_SENDERSUPERDEPT  = "CurrentNodeSuperDept";
    
    /**
     * 角色名称：发起者，值为{@value}。  
     */
    public static final String ORGENT_META_KEY_SEDNER = "Sender";
    public static final String ORGENT_META_KEY_SEDNER_I18N_KEY = "sys.role.rolename.Sender";

    /**
     * 上节点，值为{@value}。    
     */
    public static final String ORGENT_META_KEY_NODE = "Node";

    /**
     * 上节点，值为{@value}。    
     */
    public static final String ORGENT_META_KEY_NODEUSER = "NodeUser";
    public static final String ORGENT_META_KEY_NODEUSER_I18N_KEY = "sys.role.rolename.NodeUser";
    
    /**
     * 角色名称：空节点，值为{@value}。    
     */ 
    public static final String ORGENT_META_KEY_BlankNode = "BlankNode";
    public static final String ORGENT_META_KEY_BlankNode_I18N_KEY = "sys.role.rolename.BlankNode";
    
    /**
     * 部门主管
     */
   public static final String ORGENT_META_KEY_DepManager = "DepManager";
   public static final String ORGENT_META_KEY_DepManager_I18N_KEY = "sys.role.rolename.DepManager";
   
    /**
     * 部门分管领导
     */
    public static final String ORGENT_META_KEY_DepLeader = "DepLeader";
    public static final String ORGENT_META_KEY_DepLeader_I18N_KEY = "sys.role.rolename.DepLeader";
    
    /**
     * 主管各部门
     */
    public static final String ORGENT_META_KEY_MANAGEDEPTMEMBER = "ManageDep";
    
    /*
    * 部门管理员
    */
    public static final String ORGENT_META_KEY_DepAdmin = "DepAdmin";
    public static final String ORGENT_META_KEY_DepAdmin_I18N_KEY = "sys.role.rolename.DepAdmin";

    /**
     * 分管各部门
     */
    public static final String ORGENT_META_KEY_ADMINDEPTMEMBER = "LeaderDep";
    
    /**
     * 发起者上级部门
     */
    public static final String ORGENT_META_KEY_SENDERSUPERDEPT = "SenderSuperDept";
    public static final String ORGENT_META_KEY_SENDERSUPERDEPT_I18N_KEY = "sys.role.rolename.SenderSuperDept";
    
    /**
     * 上节点上级部门
     */
    public static final String ORGENT_META_KEY_NODEUSERSUPERDEPT = "NodeUserSuperDept";
    public static final String ORGENT_META_KEY_NODEUSERSUPERDEPT_I18N_KEY = "sys.role.rolename.NodeUserSuperDept";

    /**
     * 发起者主管各部门
     */
    public static final String ORGENT_META_KEY_SENDERMANAGEDEPTMEMBER = "SenderManageDep";
    public static final String ORGENT_META_KEY_SENDERMANAGEDEPTMEMBER_I18N_KEY = "sys.role.rolename.SenderManageDep";

    /**
     * 发起者分管各部门
     */
    public static final String ORGENT_META_KEY_SENDERLEADERDEPTMEMBER = "SenderLeaderDep";
    public static final String ORGENT_META_KEY_SENDERLEADERDEPTMEMBER_I18N_KEY = "sys.role.rolename.SenderLeaderDep";

    /**
     * 上节点主管各部门
     */
    public static final String ORGENT_META_KEY_NODEUSERMANAGEDEPTMEMBER = "NodeUserManageDep";
    public static final String ORGENT_META_KEY_NODEUSERMANAGEDEPTMEMBER_I18N_KEY = "sys.role.rolename.NodeUserManageDep";

    /**
     * 上节点分管各部门
     */
    public static final String ORGENT_META_KEY_NODEUSERLEADERDEPTMEMBER = "NodeUserLeaderDep";
    public static final String ORGENT_META_KEY_NODEUSERLEADEREPTMEMBER_I18N_KEY = "sys.role.rolename.NodeUserLeaderDep";

    
    /**
     * 上级部门
     */
    public static final String ORGENT_META_KEY_SUPERDEPT = "SuperDept";
    
    /**
     * 发起者上级单位
     */
    public static final String ORGENT_META_KEY_CURRENT_NODE_SUPERACCOUNT = "CurrentNodeSuperAccount";
    
    /**
     * 发起者上级单位
     */
    public static final String ORGENT_META_KEY_SENDERSUPERACCOUNT = "SenderSuperAccount";
    
    /**
     * 上节点上级单位
     */
    public static final String ORGENT_META_KEY_NODEUSERSUPERACCOUNT = "NodeUserSuperAccount";
    
    /**
     * 上级单位
     */
    public static final String ORGENT_META_KEY_SUPERACCOUNT = "SuperAccount";
    
    /**
     * 角色名称：单位角色，值为{@value}。    
     */ 
    public static final String ORGREL_TYPE_ACCOUNT_ROLE = "Account_Role";
    
    /**
     * 角色名称：个人组，值为{@value}。    
     */ 
    public static final int TEAM_TYPE_PERSONAL = 1;
    
    /**
     * 角色名称：上级部门主管，值为{@value}。    
     */
    public static final String ORGENT_META_KEY_SUPERDEPMANAGER= "SuperManager";
    
    /**
     * 角色名称：上级部门，值为{@value}。    
     */
    public static final String ORGENT_META_KEY_SUPER = "SuperDept";
    
    /**
     * 角色名称：部门成员，值为{@value}。
     */
    public static final String ORGENT_META_KEY_DEPMEMBER= "DeptMember";
    public static final String ORGENT_META_KEY_DEPMEMBER_I18N_KEY= "guestbook.leaveword.departmentpeople";
    
    /** 相对角色下的汇报人 **/
    public static final String ReciprocalRoleReporter  = "ReciprocalRoleReporter";
    
    /**
     * 角色名称：单位公文管理员
     */
    public static final String AccountEdocAdminRoleName="AccountEdocAdmin";
    
    /**
     * 角色名称：单位公文交换员
     */
    public static final String acountExchangeRoleName="account_exchange";
    
    /**
     * 角色名称：部门公文交换员
     */
    public static final String departmentExchangeRoleName="departmentexchange";
    public static final String departmentExchangeRoleName_I18N_KEY="sys.role.rolename.Departmentexchange";
    
    /**
     * 角色名称：单位公文拟文人员
     */
    public static final String accountEdocCreateRoleName="account_edoccreate";
    
    /**
     * 负责匹配根据流程上下文信息匹配指定节点符合条件的人员列表
     * @param domain 所属流程域名称
     * @param humenActivity 指定流程节点
     * @param context 工作流上下文数据对象
     * @return 人员列表
     * @throws BPMException
     */
    public List<V3xOrgMember> getUserList(String domain, BPMHumenActivity humenActivity, WorkflowBpmContext context,boolean isUseAdditonUserIds)
            throws BPMException;
    
    /**
     * 根据表单控件的值，查询到将要匹配到的人，返回List<User>
     * @param matchMesssage
     * @param formFiledValueMap
     * @return
     */
    public List<User> getUserListFormField(String matchMesssage, Map<String, Object> formFiledValueMap,WorkflowBpmContext context,boolean up, String rScope);

    /**
     * 根据类型及ID查询所属人员，对应orgManager.getMembersByTypeAndIds(newflowSender);
     * 
     * 把多项组织类型和id用","以及“|”连接，格式必须与../SelectPeople/Element.js中产生的一致。该方法自动分解，返回对应数据
     * 
     * @param typeAndIds
     *            先用","，再用"|"组合的字符串，如：Member|-92874958395,Department|3461234123458,Department|true|5435234764545
     * @return
     */
    public List<User> getUserListByTypeAndId(String typeAndIds);
    
    /**
     * 
     * @param context
     */
    public void removeWorkflowMatchResult(String key);
    
    /**
     * 
     * @param key
     * @param activityId
     * @return
     */
    public List<V3xOrgMember> getWorkflowMatchedCacheResult(String key, String activityId);
    
    /**
     * 
     * @param key
     * @param activityId
     * @return
     */
    public WorkflowNodeUsersMatchResult getWorkflowMatchedCacheResultMsg(String key, String activityId);
    
    /**
     * 
     * @param key
     * @param activityId
     * @param members
     */
    public void putWorkflowMatchedCacheResult(String key, String activityId,WorkflowNodeUsersMatchResult matchResult);
    
    /**
     * 
     * @param key
     * @param activityId
     * @return
     */
    public Integer getWorkflowNeedSelectPeopleNode(String key, String activityId);

    /**
     * 
     * @param matchRequestToken
     * @param autoSkipNodeId
     * @param canNotSkipMsgList
     */
    public void putWorkflowMatchLogToCache(String stepIndex,String key, String autoSkipNodeId,String nodeName,List<String> canNotSkipMsgList);
	
	/**
	 * 
	 * @param matchRequestToken
	 * @param autoSkipNodeId
	 * @param canNotSkipMsg
	 */
    public void putWorkflowMatchLogMsgToCache(String stepIndex,String key, String autoSkipNodeId,String nodeName, String canNotSkipMsg);

	/**
	 * 
	 * @param matchRequestToken
	 * @param id
	 * @param canNotSkipMsgList
	 */
	public void putWorkflowMatchLogToCacheHead(String stepIndex,String key, String autoSkipNodeId, String nodeName,List<String> canNotSkipMsgList);
	
	/**
	 * 
	 * @param key
	 * @param nodeId
	 * @return
	 */
	public WorkflowMatchLogVO getWorkflowMatchLogList(String key, String nodeId);

	/**
	 * 
	 * @param key
	 * @return
	 */
	public List<ProcessLogDetail> getAllWorkflowMatchLogStr(String key,Set<String> selectNodeIds,ProcessLogDetail processLogDetail); 
	
	/**
	 * 
	 * @param key
	 * @param activityId
	 * @param nodeName
	 */
	public void putWorkflowNeedSelectBranchNodeCacheMap(String key, String activityId,String nodeName);
	
	/**
	 * 
	 * @param key
	 * @param activityId
	 * @param nodeName
	 */
	public LinkedHashMap<String,WorkflowMatchLogVO> getAllWorkflowNeedSelectBranchNodeCacheMap(String key);

	/**
	 * 
	 * @param matchRequestToken
	 * @param autoSkipNodeId
	 */
	public void removeWorkflowMatchResult(String matchRequestToken, String autoSkipNodeId);

	/**
	 * @param matchRequestToken
	 * @param autoSkipNodeId
	 * @param autoSkipNodeName
	 * @param matchState
	 */
	public void putWorkflowMatchLogMatchStateToCache(String matchRequestToken, String autoSkipNodeId,String autoSkipNodeName, int matchState);
	
	/**
	 * 
	 * @param matchRequestToken
	 * @param autoSkipNodeId
	 * @param autoSkipNodeName
	 * @param processMode
	 */
	public void putWorkflowMatchLogProcessModeToCache(String matchRequestToken, String autoSkipNodeId,String autoSkipNodeName, List<String> processMode);

	/**
	 * 
	 * @param matchRequestToken
	 * @param autoSkipNodeId
	 * @param autoSkipNodeName
	 * @param nodeType
	 */
	public void putWorkflowMatchLogNodeTypeToCache(String matchRequestToken, String autoSkipNodeId,String autoSkipNodeName, String nodeType);
	
	/**
	 * 将预发送、发送和预处理、处理时的BPMProcess对象进行缓存，确保发送和处理一次操作只查询一次BPMProcess；
	 * 注意：批处理除外
	 * @param matchRequestToken
	 * @param process
	 */
	public void putBPMProcessToCacheRequestScope(String matchRequestToken,BPMProcess process); 
	
	/**
	 * 从cache中获取BPMProcess对象(注意：批处理除外)
	 * @param matchRequestToken
	 * @return
	 */
	public BPMProcess getBPMProcessFromCacheRequestScope(String matchRequestToken);
	
	/**
	 * 将预发送、发送和预处理、处理时的BPMProcess对象进行缓存，确保发送和处理一次操作只查询一次processState
	 * 注意：批处理除外
	 * @param matchRequestToken
	 * @param process
	 */
	public void putProcessStateToCacheRequestScope(String matchRequestToken,Integer processState); 
	
	/**
	 * 从cache中获取BPMProcess对象(注意：批处理除外)
	 * @param matchRequestToken
	 * @return
	 */
	public Integer getProcessStateFromCacheRequestScope(String matchRequestToken);
	
	/**
	 * 将预发送、发送和预处理、处理时的BPMProcess对象进行缓存，确保发送和处理一次操作只查询一次ProcessXml；
	 * 注意：批处理除外
	 * @param matchRequestToken
	 * @param process
	 */
	public void putProcessXmlToCacheRequestScope(String matchRequestToken,String processXml); 
	
	/**
	 * 从cache中获取BPMProcess对象(注意：批处理除外)
	 * @param matchRequestToken
	 * @return
	 */
	public String getProcessXmlFromCacheRequestScope(String matchRequestToken);
	
	/**
	 * 将预发送、发送和预处理、处理时的FormData象进行缓存，确保发送和处理一次操作只查询一次FormData；
	 * 注意：批处理除外
	 * @param matchRequestToken
	 * @param process
	 */
	public void putWorkflowFormDataToCacheRequestScope(String matchRequestToken,Map<String,Object> formData); 
	
	/**
	 * 从cache中获取FormData对象(注意：批处理除外)
	 * @param matchRequestToken
	 * @return
	 */
	public Map<String,Object> getWorkflowFormDataFromCacheRequestScope(String matchRequestToken);

	/**
	 * 
	 * @param matchRequestToken
	 * @param autoSkipNodeId
	 * @param nodeName
	 */
	public void putWorkflowMatchLogMatchNodeNameToCache(String matchRequestToken, String autoSkipNodeId,String nodeName);

	/**
	 * 
	 * @param matchRequestToken
	 * @param formDataObjDef
	 */
	public void putWorkflowFormDataDefToCacheRequestScope(String matchRequestToken,Map<String, WorkflowFormFieldBO> formDataObjDef);

	/**
	 * 
	 * @param matchRequestToken
	 * @return
	 */
	public Map<String, WorkflowFormFieldBO> getWorkflowFormDataDefFromCacheRequestScope(String matchRequestToken);

	/**
	 * 
	 * @param matchRequestToken
	 * @param processId
	 * @return
	 */
	public WFProcessProperty getCaseProcessPropertyFromCache(String matchRequestToken, String processId);

	/**
	 * 
	 * @param matchRequestToken
	 * @param processProperty
	 */
	public void putCaseProcessPropertyToCache(String matchRequestToken, WFProcessProperty processProperty);
	
	/**
     * 
     * 缓存分支匹配时手动选择人员的日志信息，后续会进行日志输出
     * 
     * 这个方法会重复执行，内部做了重复判断
     * 
     * @param process
     *
     * @Since A8-V5 6.1SP1
     * @Author      : xuqw
     * @Date        : 2018年1月20日下午5:34:55
     *
     */
	public void cacheManualSelectLog(String cacheKey, BPMProcess process, 
            Map<String, Map<String,Object>> selectPeopleParams);
    
}
