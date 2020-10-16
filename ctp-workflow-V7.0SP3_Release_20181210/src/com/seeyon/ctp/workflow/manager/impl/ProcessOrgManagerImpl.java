/**
 * Author: wangchw
 * Rev: ProcessOrgManagerImpl.java
 * Date: 20122012-8-3上午09:34:32
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
*/
package com.seeyon.ctp.workflow.manager.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.login.online.OnlineManager;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgLevel;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgPost;
import com.seeyon.ctp.organization.bo.V3xOrgRole;
import com.seeyon.ctp.organization.bo.V3xOrgTeam;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.organization.manager.JoinOrgManagerDirect;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.workflow.bo.WorkflowFormFieldBO;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.manager.ProcessOrgManager;
import com.seeyon.ctp.workflow.manager.WorkFlowMatchUserManager;
import com.seeyon.ctp.workflow.util.WorkflowUtil;
import com.seeyon.ctp.workflow.util.condition.ConditionValidateUtil;
import com.seeyon.ctp.workflow.vo.User;
import com.seeyon.ctp.workflow.vo.ValidateResultVO;
import com.seeyon.ctp.workflow.vo.WFMoreSignSelectPerson;

import net.joinwork.bpm.definition.BPMAbstractNode;
import net.joinwork.bpm.definition.BPMActor;
import net.joinwork.bpm.definition.BPMParticipant;
import net.joinwork.bpm.definition.BPMTransition;
import net.joinwork.bpm.engine.wapi.CaseWorkItemLog;

/**
 * <p>Title: 工作流（V3XWorkflow）</p>
 * <p>Description: 工作流内部组织模型统一接口实现类</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: 北京致远协创软件有限公司</p>
 * <p>Author: wangchw
 * <p>Time: 2012-8-3 上午09:34:32
*/
public class ProcessOrgManagerImpl implements ProcessOrgManager {

    private static Log    log           = CtpLogFactory.getLog(ProcessOrgManagerImpl.class);

    private OrgManager    orgManager    = null;
    
    private JoinOrgManagerDirect joinOrgManagerDirect;

    private OnlineManager onLineManager = null;
    
    /**
     * @param onLineManager the onLineManager to set
     */
    public void setOnLineManager(OnlineManager onLineManager) {
        this.onLineManager = onLineManager;
    }

    @Override
	public List<V3xOrgDepartment> getChildDepartments(Long parentDepId,
			boolean firtLayer) throws BPMException {
    	List<V3xOrgDepartment> result = null;
    	try {
			result = orgManager.getChildDepartments(parentDepId, firtLayer);
		} catch (BusinessException e) {
			log.error("", e);
			throw new BPMException(e);
		}
		return result;
	}

	/* (non-Javadoc)
     * @see com.seeyon.ctp.workflow.manager.ProcessOrgManager#getNodePerformerInfo(java.lang.String)
     */
    @Override
    public String[] getNodePerformerInfo(String performer, String workitemName){
        //String[] result = {"测试人员", null, "测试单位"};
        if (Strings.isBlank(performer)){
            return null;
        }
        try {
            V3xOrgMember member = orgManager.getMemberById(Long.parseLong(performer));
            if (member != null) {
                String[] result= { workitemName, null, null };
                result[0] = member.getName();

                if (member.getIsDeleted()) {
                    result[1] = CaseWorkItemLog.delete;
                } else if (member.getState() == 2) {
                    result[1] = CaseWorkItemLog.dimission;
                } else if (!member.getIsAssigned()) {
                    result[1] = CaseWorkItemLog.UnAssign;
                } else if (!member.getEnabled()) {
                    result[1] = CaseWorkItemLog.truce;
                }

                com.seeyon.ctp.common.authenticate.domain.User user = AppContext.getCurrentUser();
                Object object = SysFlag.sys_isGroupVer.getFlag();
                if (object != null && (Boolean) object && user != null) {
                    if (null!=user.getLoginAccount() && null!=member.getOrgAccountId() 
                            && user.getLoginAccount().longValue()!= member.getOrgAccountId().longValue()) {
                        V3xOrgAccount account1 = orgManager.getAccountById(member.getOrgAccountId());
                        if (account1 != null) {
                            result[2] = account1.getShortName();
                        }
                    }
                }
                return result;
            }
            
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    @Override
    public String getProcessModifyUserId(String modifyUserId) throws BPMException {
        return modifyUserId;
    }

    /**
     * 获取到某一个用户的Id
     * @param userId
     * @return
     */
    public User getUserById(String userId,boolean isNeedTitle) throws BPMException {
        V3xOrgMember member = null;
        try {
            member = orgManager.getMemberById(Long.parseLong(userId));
        } catch (Throwable e) {
            log.error("工作流通过userId查询一个人报错,传进来的userId不是一个long型",e);
        }
        if(null!=member){
        	return WorkflowUtil.v3xOrgMemberToWorkflowUser(member,false,isNeedTitle);
        }
        return null;
    }
    
    public User getUserById(String userId,boolean isNotValidate,boolean isNeedTitle) throws BPMException{
        V3xOrgMember member = null;
        try {
            member = orgManager.getMemberById(Long.parseLong(userId));
        } catch (Throwable e) {
            log.error("工作流通过userId查询一个人报错,传进来的userId不是一个long型",e);
        }
        if(null!=member){
            return WorkflowUtil.v3xOrgMemberToWorkflowUser(member,isNotValidate,isNeedTitle);
        }
        return null;
    }

    /**
     * 根据名称取得单位。
     * 
     * @param id
     * @return
     * @throws BPMException
     */
    public V3xOrgAccount getAccountById(String id) throws BPMException {
        V3xOrgAccount account = null;
        try {
            account = orgManager.getAccountById(Long.parseLong(id));
        } catch (Exception e) {
            throw new BPMException(e);
        }
        return account;
    }

    @Override
    public String getPersonStatus(String addition, String partyId) throws BPMException {
        String _personStatus = "normal";
        V3xOrgMember member = null;
        try {
            if (addition != null && !"".equals(addition)) {
                String[] partyAdditions = addition.split(",");
                if (null != getOrgManager()) {
                    member = getOrgManager().getMemberById(Long.parseLong(partyAdditions[0]));
                }

            } else {
                if (null != getOrgManager()) {
                    member = getOrgManager().getMemberById(Long.parseLong(partyId));
                }
            }
            if (member != null) {
                if (member.getIsDeleted()) {
                    _personStatus = "delete";//删除
                } else if (member.getState() == 2) {
                    _personStatus = "dimission";//离职
                } else if (!member.getIsAssigned()) {
                    _personStatus = "unAssign";//未分配
                } else if (!member.getEnabled()) {
                    _personStatus = "truce";//停用
                }
            }else{
                _personStatus = "delete";//删除
            }
        } catch (Throwable e) {
            log.error("", e);
        }
        return _personStatus;

    }

    @Override
    public String getRoleShowNameByName(String id,Long accountId) throws BPMException {
        String roleName = "";
        Long roleId = null;
        if(id.startsWith(WorkflowUtil.VJOIN)){
    		String roleIdTemp= id.substring(WorkflowUtil.VJOIN.length());
    		if(WorkflowUtil.isLong(roleIdTemp)){
    			id= roleIdTemp;
    		}
    	}
        if(WorkflowUtil.isLong(id)){//id为数字
             roleId = Long.parseLong(id);
             V3xOrgRole role = getRoleById(roleId);
             if(role!=null){
                 roleName = role.getShowName();
                 if(role.getExternalType()!=0){
                	 roleName +=ResourceUtil.getString("workflow.process.org.outer"); //（外）
                 }
             }
        }
        if( WorkflowUtil.isLong(id) || Strings.isBlank(roleName)){//非数字
            Locale locale= AppContext.getLocale();
            String sperateChar= "";
            if(null!= locale){
                String language= locale.getLanguage();
                if("en".equals(language)){
                    sperateChar= " ";
                }
            }
            String sysRole01= id;
            String sysRole02= "";
            if(id.startsWith(WorkFlowMatchUserManager.ORGENT_META_KEY_SENDERMANAGEDEPTMEMBER)){//发起者主管各部门
                sysRole01 = WorkFlowMatchUserManager.ORGENT_META_KEY_SENDERMANAGEDEPTMEMBER;
            }else if(id.startsWith(WorkFlowMatchUserManager.ORGENT_META_KEY_SENDERLEADERDEPTMEMBER)){//发起者分管各部门
                sysRole01 = WorkFlowMatchUserManager.ORGENT_META_KEY_SENDERLEADERDEPTMEMBER;
            }else if(id.startsWith(WorkFlowMatchUserManager.ORGENT_META_KEY_NODEUSERMANAGEDEPTMEMBER)){//上节点主管各部门
                sysRole01 = WorkFlowMatchUserManager.ORGENT_META_KEY_NODEUSERMANAGEDEPTMEMBER;
            }else if(id.startsWith(WorkFlowMatchUserManager.ORGENT_META_KEY_NODEUSERLEADERDEPTMEMBER)){//上节点分管各部门
                sysRole01 = WorkFlowMatchUserManager.ORGENT_META_KEY_NODEUSERLEADERDEPTMEMBER;
            }else if(id.startsWith("SenderSuperDept")){ //发起者上级部门XXX
                sysRole01 = "SenderSuperDept";
            }else if(id.startsWith("NodeUserSuperDept")){
                sysRole01 = "NodeUserSuperDept";
            }else if(id.startsWith("CurrentNodeSuperDept")){
                sysRole01 = "CurrentNodeSuperDept";
            }else if(id.startsWith("SenderSuperAccount")){ //发起者上级部门XXX
                sysRole01 = "SenderSuperAccount";
            }else if(id.startsWith("NodeUserSuperAccount")){
                sysRole01 = "NodeUserSuperAccount";
            }else if(id.startsWith("CurrentNodeSuperAccount")){
                sysRole01 = "CurrentNodeSuperAccount";
            }else if(id.startsWith("Sender")){//发起者XXX
                sysRole01= "Sender";
            }else if(id.startsWith("NodeUser")){//上节点XXX
                sysRole01= "NodeUser";
            }else if(id.startsWith("CurrentNode")){//上节点XXX
                sysRole01= "CurrentNode";
            }
            
            sysRole02= id.substring(sysRole01.length());
            if(Strings.isBlank(sysRole02)){
                if( "BlankNode".equals(sysRole01) || "Sender".equals(sysRole01) || "ReciprocalRoleReporter".equals(sysRole01)){
                    roleName= OrgHelper.getRoleShowNameByName(sysRole01);
                }else{//部门角色
                    if("DeptMember".equals(sysRole01.trim())){//部门成员,国际化资源工作流自己维护
                        roleName= ResourceUtil.getString("workflow.sys.role.rolename.deptmember");
                    }else{
                        try {
                            //V3xOrgRole role = orgManager.getRoleByName(sysRole01, accountId);
                            V3xOrgRole role = null;
                        	if(sysRole01.startsWith("Vjoin")){//可能是vjoin角色
                        		role= joinOrgManagerDirect.getRoleByCode(sysRole01, null);
                        	}
                        	if(null==role){
                        		role = orgManager.getRoleByName(sysRole01, accountId);
                        	}
                            if(null!=role){
                                roleName= role.getShowName();
                                if(role.getExternalType()!=0){
                               	 roleName +=ResourceUtil.getString("workflow.process.org.outer");//"（外）"
                                }
                            }
                        } catch (BusinessException e) {
                            log.error("",e);
                        }
                    }
                }
            }else{//相对角色
                String sysRole01Name= "";
                String sysRole02Name= "";
                sysRole01Name= OrgHelper.getRoleShowNameByName(sysRole01);
                if(sysRole02.startsWith(WorkflowUtil.VJOIN)){
            		String roleIdTemp= sysRole02.substring(WorkflowUtil.VJOIN.length());
            		if(WorkflowUtil.isLong(roleIdTemp)){
            			sysRole02= roleIdTemp;
            		}
            	}
                if(WorkflowUtil.isLong(sysRole02)){//是数字
                    roleId = Long.parseLong(sysRole02);
                    V3xOrgRole role = getRoleById(roleId);
                    if(role!=null){
                        sysRole02Name = role.getShowName();
                        if(role.getExternalType()!=0){
                        	sysRole02Name +=ResourceUtil.getString("workflow.process.org.outer");//"（外）"
                        }
                        roleName= sysRole01Name+sperateChar+sysRole02Name;
                    }
                }else{//非数字
                    if("DeptMember".equals(sysRole02.trim()) || "DeptMember|1".equals(sysRole02.trim())){//部门成员,国际化资源工作流自己维护
                        sysRole02Name= ResourceUtil.getString("workflow.sys.role.rolename.deptmember");
                        roleName= sysRole01Name+sperateChar+sysRole02Name;
                    }else if("DeptMember|0".equals(sysRole02.trim())){
                    	sysRole02Name= ResourceUtil.getString("workflow.sys.role.rolename.deptmember")
                    			+"("+ResourceUtil.getString("workflow.branch.excludeChildren")+")";
                        roleName= sysRole01Name+sperateChar+sysRole02Name;
                    }else{
                        try {
                        	V3xOrgRole role = null;
                        	if(sysRole02.startsWith("Vjoin")){//可能是vjoin角色
                        		role= joinOrgManagerDirect.getRoleByCode(sysRole02, null);
                        	}
                        	if(null==role){
                        		role = orgManager.getRoleByName(sysRole02, accountId);
                        	}
                            if(null!=role){
                                sysRole02Name= role.getShowName();
                                if(role.getExternalType()!=0){
                                	sysRole02Name +=ResourceUtil.getString("workflow.process.org.outer");//"（外）"
                                }
                                roleName= sysRole01Name+sperateChar+sysRole02Name;
                            }else{
                            	if("SuperManager".equals(sysRole02)){
                            		sysRole02Name= ResourceUtil.getString("selectPeople.node.SuperManager"); 
                            	}else{
                            		sysRole02Name= OrgHelper.getRoleShowNameByName(sysRole02);
                            	}
                            	roleName= sysRole01Name+sperateChar+sysRole02Name;
                            }
                        } catch (BusinessException e) {
                            log.error("",e);
                        }
                    }
                }
            }
        }
        return roleName;
    }

    @Override
    public String[] getNameByEntity(String type, String id, String defaultName) throws BPMException {
        String[] result= new String[2];
        boolean wbdw= false;
        V3xOrgEntity entity = null;
        try {
            if(Strings.isBlank(id) || !WorkflowUtil.isLong(id)){
                return null;
            }
            entity = orgManager.getEntity(type, Long.parseLong(id));
            if (entity == null) {
                return null;
            }
            if(type.equals(ProcessOrgManager.ORGENT_TYPE_DEPARTMENT)){
                V3xOrgDepartment department= (V3xOrgDepartment)entity;
                if(!department.getIsInternal()){
                    wbdw= true;
                }
            }
            result[0]= entity.getName();
            result[1]= String.valueOf(wbdw);
            return result;
        } catch (Throwable e) {
            log.error("", e);
            throw new BPMException(e);
        }
    }

    @Override
    public V3xOrgEntity getEntity(String entityType, Long id) throws BPMException {
        try {
            return orgManager.getEntity(entityType, id);
        } catch (BusinessException e) {
            log.error("Workflow:工作流调用orgManager.getEntity(String, Long)时出现异常！第一个参数：" + entityType + ",第二个参数=" + id, e);
            throw new BPMException(e);
        }
    }

    @Override
    public V3xOrgEntity getEntity(String entityTypeAndId) throws BPMException {
        try {
            return orgManager.getEntity(entityTypeAndId);
        } catch (BusinessException e) {
            //log.info("Workflow:工作流调用orgManager.getEntity(String, Long)时出现异常！第一个参数：" + entityTypeAndId);
            throw new BPMException(e);
        }
    }

    @Override
    public List<V3xOrgEntity> getEntityByDepartmentPostorRole(String entityTypeAndId) throws BPMException {
        try {
            return orgManager.getEntitys4Merge(entityTypeAndId);
        } catch (BusinessException e) {
            //log.info("Workflow:工作流调用orgManager.getEntityByDepartmentPostorRole(String)时出现异常！参数：" + entityTypeAndId);
            throw new BPMException(e);
        }
    }

    @Override
    public V3xOrgRole getRoleById(Long id) throws BPMException {
        try {
            return orgManager.getRoleById(id);
        } catch (BusinessException e) {
            //log.info("Workflow:工作流调用查询角色时出现异常！id=" + id);
            throw new BPMException(e);
        }
    }

    @Override
    public V3xOrgRole getRoleByName(String roleName, Long accountId) throws BPMException {
        try {
            return orgManager.getRoleByName(roleName, accountId);
        } catch (BusinessException e) {
            //log.info("Workflow:工作流调用查询角色时出现异常！roleName=" + roleName + ",accountId=" + accountId);
            throw new BPMException(e);
        }
    }

    @Override
    public List<User> getMembersByTypeAndIds(String typeAndIds) throws BPMException {
        List<User> uses = new ArrayList<User>();
        try {
            Set<V3xOrgMember> ms = orgManager.getMembersByTypeAndIds(typeAndIds);
            if (ms != null && !ms.isEmpty()) {
                for (V3xOrgMember m : ms) {
                    if (m != null && m.isValid()) {//对人员不可用这种情况进行处理
                        User p = new User();
                        p.setId(m.getId() + "");
                        p.setName(m.getName());
                        p.setAccountId(String.valueOf(m.getOrgAccountId()));
                        p.setSortId(m.getSortId().intValue());
                        uses.add(p);
                    }
                }
            }
        } catch (Throwable e) {
            throw new BPMException(e);
        }
        return uses;
    }

    public OrgManager getOrgManager() {
        return orgManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    @Override
    public String[] getElementNames(String[] ids, String[] types) throws BPMException {
        String[] names = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            try {
                names[i] = orgManager.getEntity(types[i], Long.parseLong(ids[i])).getName();
            } catch (NumberFormatException e) {
                log.error("", e);
            } catch (BusinessException e) {
                log.error("", e);
            }
        }
        return names;
    }

    @Override
    public String getUserTypeByField(String userTypeFieldName) throws BPMException {
        if (StringUtils.isBlank(userTypeFieldName) || V3xOrgEntity.ORGENT_TYPE_MEMBER.equals(userTypeFieldName)) {
            return "user";
        }
        return userTypeFieldName;
    }

    @Override
    public String branchTranslateAccount(String group, String functionName, String operator) throws BPMException {
        String translateGroup = "";
        String errorMsg = "";
        String accountI18n = ResourceUtil.getString("workflow.branchGroup.1");
        //String paramFormatI18n = ResourceUtil.getString("workflow.branchTranslate.3");
        String zhuI18n = ResourceUtil.getString("workflow.branchGroup.1.1");
        String jianI18n = ResourceUtil.getString("workflow.branchGroup.1.2");
        try {
        	String[] paramArray = ConditionValidateUtil.parseFunctionParamWithFunName(group);
        	if(paramArray!=null && paramArray.length>=3){
	            //translateGroup += "[单位(";
	            translateGroup += "["+accountI18n+"(";
	            boolean isBeforeTrue = false;
	            if ("true".equals(paramArray[0])) {//所属单位
	                isBeforeTrue = true;
	                translateGroup += zhuI18n;//"所属单位";
	            }
	            if ("true".equals(paramArray[1])) {//兼职单位
	                if (isBeforeTrue) {
	                    translateGroup += ","+jianI18n;//",兼职单位";
	                } else {
	                    translateGroup += jianI18n;//"兼职单位";
	                }
	            }
	            translateGroup += ")]";
	            translateGroup += operator;
	            translateGroup += "\"";
	            String accountIdStr = paramArray[2].substring(1, paramArray[2].length()-1);
	            String accountId = accountIdStr.trim();
	            V3xOrgAccount account = getAccountById(accountId);
	            if (null == account) {
	                //errorMsg = "单位分支条件：" + accountId + "存在问题,该单位ID在系统中不存在。";
	                String name = accountI18n;
	                errorMsg = ResourceUtil.getString("workflow.branchTranslate.4", name, accountId, name);
	                throw new BPMException(errorMsg);
	            } else {
	                translateGroup += account.getName();
	            }
	            translateGroup += "\"";
        	}
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            if (!"".equals(errorMsg)) {
                throw new BPMException(errorMsg, e);
            } else {
//                throw new BPMException("单位分支条件：" + group + "存在问题,请检查是否符合：" + functionName
//                        + "true/false,true/false,\"单位Id\")分支函数定义格式！", e);
                String name = accountI18n;
                String forrmat = functionName + "true/false,true/false,\""+name+"Id\")";
                errorMsg = ResourceUtil.getString("workflow.branchTranslate.5", name, group, forrmat);
                throw new BPMException(errorMsg);
            }
        }
        return translateGroup;
    }

    @Override
	public String branchTranslateLoginAccount(String group, String operator)
			throws BPMException {
    	//发起者登录单位分支翻译
    	String translateGroup = "";
    	String loginAccountI18n = ResourceUtil.getString("workflow.branchGroup.1.3");
    	String[] paramArray = ConditionValidateUtil.parseFunctionParamWithFunName(group);
    	if(paramArray!=null && paramArray.length>0){
    		String accountIdStr = paramArray[0].substring(1, paramArray[0].length()-1);
    		String accountId = accountIdStr.trim();
            V3xOrgAccount account = getAccountById(accountId);
            if(account!=null){
            	translateGroup = "[" + loginAccountI18n + "]" + operator + "\"" + account.getName() + "\"";
            }
    	}
		return translateGroup;
	}

	@Override
    public String branchTranslateLevel(String group, String functionName, String operator) throws BPMException {
        String translateGroup = "";
        String errorMsg = "";
        String groupI18n = ResourceUtil.getString("workflow.branchGroup.0");
        String accountI18n = ResourceUtil.getString("workflow.branchGroup.1");
        String paramFormatI18n = ResourceUtil.getString("workflow.branchTranslate.3");
        String levelI18n = ResourceUtil.getString("workflow.branchGroup.3");
        String zhuLevelI18n = ResourceUtil.getString("workflow.branchGroup.3.1");
        String jianLevelI18n = ResourceUtil.getString("workflow.branchGroup.3.2");
    	String loginAccountI18n = ResourceUtil.getString("workflow.branchGroup.1.3");
        try {
        	String[] paramArray = ConditionValidateUtil.parseFunctionParamWithFunName(group);
        	if(paramArray!=null){
	            String param0 = paramArray[0].trim();//是否为集团职务级别true/false
	            String param1 = paramArray[1].trim();//是否为主职务级别true/false
	            String param2 = paramArray[2].trim();//是否为兼职职务级别true/false
        		String param3 = "";
        		if(paramArray.length==5 && "true".equals(paramArray[3])){
        			param3 = paramArray[4].trim();//职务级别ID
        			translateGroup += "["+levelI18n+"("+loginAccountI18n+")]";
        		} else {
		            if (!("true".equals(param0) || "false".equals(param0))
		                    || !("true".equals(param1) || "false".equals(param1))
		                    || !("true".equals(param2) || "false".equals(param2))) {
		                throw new BPMException(group + paramFormatI18n);//"参数格式不对");
		            }
		            param3 = paramArray[3].trim();//职务级别ID
		            if(paramArray.length==5){
		            	param3 = paramArray[4].trim();
		            }
		            //translateGroup += "[职务级别(";
		            translateGroup += "["+levelI18n+"(";
		            boolean isBeforeTrue = false;
		            if ("true".equals(param1)) {
		                isBeforeTrue = true;
		                translateGroup += zhuLevelI18n;//"主职务级别";
		            }
		            if ("true".equals(param2)) {
		                if (isBeforeTrue) {
		                    translateGroup += ","+jianLevelI18n;//",兼职职务级别";
		                } else {
		                    translateGroup += jianLevelI18n;//"兼职职务级别";
		                }
		            }
		            translateGroup += ")]";
        		}
	            translateGroup += operator;
	            translateGroup += "\"";
	            param3 = param3.substring(1, param3.length()-1);
	            V3xOrgLevel level = orgManager.getLevelById(Long.parseLong(param3));
	            if (null == level) {
	                if ("true".equals(param0)) {
	                    translateGroup += groupI18n+levelI18n+":";//"集团职务级别:";
	                } else {
	                    translateGroup += accountI18n+levelI18n+":";//"单位职务级别:";
	                }
	//                errorMsg = "职务级别分支条件：" + param3 + "存在问题,该" + translateGroup + "ID在系统中不存在。";
	//                throw new BPMException("职务级别分支条件：" + param3 + "存在问题,该" + translateGroup + "ID在系统中不存在。");
	                String name = levelI18n;
	                errorMsg = ResourceUtil.getString("workflow.branchTranslate.4", name, param3, translateGroup);
	                throw new BPMException(errorMsg);
	            } else {
	                if(level.getOrgAccountId().equals(OrgConstants.GROUPID)){
	                	translateGroup += groupI18n+levelI18n+":";//"集团职务级别:";
	                }else{
	                	V3xOrgAccount account = getAccountById(level.getOrgAccountId().toString());
	                    translateGroup += account.getShortName() + ":";
	                }
//	                if ("true".equals(param0)) {
//	                    translateGroup += groupI18n+levelI18n+":";//"集团职务级别:";
//	                } else {
//	                    V3xOrgAccount account = getAccountById(level.getOrgAccountId().toString());
//	                    translateGroup += account.getShortName() + ":";
//	                }
	                translateGroup += level.getName();
	            }
	            translateGroup += "\"";
        	}
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            if (!"".equals(errorMsg)) {
                throw new BPMException(errorMsg, e);
            } else {
//                throw new BPMException("职务级别分支条件：" + group + "存在问题,请检查是否符合：" + functionName
//                        + "true/false,true/false,true/false,\"职务级别Id\")分支函数定义格式！", e);
                String name = levelI18n;
                String forrmat = functionName + "true/false,true/false,true/false,\""+name+"Id\")";
                errorMsg = ResourceUtil.getString("workflow.branchTranslate.5", name, group, forrmat);
                throw new BPMException(errorMsg);
            }
        }
        return translateGroup;
    }
	
    @Override
	public String branchTranslateCompareLevel(String group, Map<String, WorkflowFormFieldBO> fieldMap) throws BPMException {
        StringBuilder translateGroup = new StringBuilder();
        String errorMsg = "";
    	String[] paramArray = ConditionValidateUtil.parseFunctionParamWithFunName(group);
    	if(paramArray!=null && paramArray.length>=3){
    		String left = "", right = "";
    		String operator = paramArray[0].substring(1, paramArray[0].length()-1);
    		String operatorBranch = "";
            String groupI18n = ResourceUtil.getString("workflow.branchGroup.0");
            String levelI18n = ResourceUtil.getString("workflow.branchGroup.3");
			if("--".equals(operator)){
				operatorBranch = "  "+ResourceUtil.getString("workflow.branchGroup.3.3")+"  ";
			} else if (">>".equals(operator)){
				operatorBranch = "  "+ResourceUtil.getString("workflow.branchGroup.3.4")+"  ";
			} else if ("<<".equals(operator)){
				operatorBranch = "  "+ResourceUtil.getString("workflow.branchGroup.3.5")+"  ";
			}
    		if(paramArray.length==3){//三个参数表示是表单职务级别比较
    			String fieldName1 = paramArray[1];
    			fieldName1= ConditionValidateUtil.parseFormFieldName(fieldName1);
    			WorkflowFormFieldBO field1 = fieldMap.get(fieldName1);
    			if(field1==null){
    				throw new BPMException("the form field '" + fieldName1 + "' is not exists表");
    			}
    			left = field1.getDisplay();
    			if(ConditionValidateUtil.isStartEndLikeField(paramArray[2])){
    				String fieldName2 = paramArray[2];
    				fieldName2= ConditionValidateUtil.parseFormFieldName(fieldName2);
    				WorkflowFormFieldBO field2 = fieldMap.get(fieldName2);
        			if(field2==null){
        				throw new BPMException("the form field '" + fieldName1 + "' is not exists表");
        			}
        			right = field2.getDisplay();
    			}else{
    				String levelIdString = paramArray[2].substring(1, paramArray[2].length()-1);
    				if(ConditionValidateUtil.isLong(levelIdString)){
    					long levelId = Long.parseLong(levelIdString);
    					try {
    						V3xOrgLevel level = orgManager.getLevelById(levelId);
    						if(level==null){//职务级别不存在
    							String entityName = ResourceUtil.getString("workflow.branchGroup.3");
    							errorMsg = ResourceUtil.getString("workflow.branchValidate.39", levelIdString, entityName);
    							throw new BPMException(errorMsg);
    						}
    						if(level.getOrgAccountId().equals(OrgConstants.GROUPID)){
    							right = groupI18n+levelI18n+":"+level.getName();
    						} else {
    							V3xOrgAccount account = orgManager.getAccountById(level.getOrgAccountId());
    							if(account!=null){
    								right = level.getName()+"("+account.getShortName()+")";
    							} else {
    								right = level.getName();
    							}
    						}
						} catch (BusinessException e) {
							throw new BPMException("search org level error,id="+levelIdString, e);
						}
    				}else{
    					throw new BPMException("the param id of level is illegal");
    				}
    			}
    		} else if(paramArray.length==5){//五个参数表示是组织模型职务级别比较
	            String param1 = paramArray[1].trim();//是否为主职务级别true/false
	            String param2 = paramArray[2].trim();//是否为兼职职务级别true/false
	            String param3 = paramArray[3].trim();//是否依据发起者登录单位判断true/false
	            String levelIdString = paramArray[4].substring(1, paramArray[4].length()-1);
	            String zhuLevelI18n = ResourceUtil.getString("workflow.branchGroup.3.1");
	            String jianLevelI18n = ResourceUtil.getString("workflow.branchGroup.3.2");
	        	String loginAccountI18n = ResourceUtil.getString("workflow.branchGroup.1.3");
	            left = "["+levelI18n+"(";
        		if("true".equals(param3)){
        			left += loginAccountI18n;
        		}else{
        			if("true".equals(param1)){
    	            	left += zhuLevelI18n;
    	            	if("true".equals(param2)){
    	            		left += ","+jianLevelI18n;
    	            	}
    	            } else {
    	            	if("true".equals(param2)){
    	            		left += jianLevelI18n;
    	            	}
    	            }
        		}
	            left += ")]";
	            if(ConditionValidateUtil.isLong(levelIdString)){
					long levelId = Long.parseLong(levelIdString);
					try {
						V3xOrgLevel level = orgManager.getLevelById(levelId);
						if(level==null){
							String name = levelI18n;
			                errorMsg = ResourceUtil.getString("workflow.branchTranslate.4", name, param3, translateGroup);
							throw new BPMException(errorMsg);
						}
						if(level.getOrgAccountId().equals(OrgConstants.GROUPID)){
							right = groupI18n+levelI18n+":"+level.getName();
						} else {
							V3xOrgAccount account = orgManager.getAccountById(level.getOrgAccountId());
							if(account!=null){
								right = account.getShortName()+":"+level.getName();
							} else {
								right = level.getName();
							}
						}
					} catch (BusinessException e) {
						throw new BPMException("search org level error,id="+levelIdString, e);
					}
				}else{
					throw new BPMException("the param id of level is illegal");
				}
    		}
        	translateGroup.append(left).append(operatorBranch).append(right);
    	} else {
    		throw new BPMException("the params count is incorrect!");
    	}
		return translateGroup.toString();
	}

	@Override
    public String branchTranslateDepartment(String group, String functionName, String operator) throws BPMException {
        String translateGroup = "";
        String errorMsg = "";
        String paramFormatI18n = ResourceUtil.getString("workflow.branchTranslate.3");
        String departmentI18n = ResourceUtil.getString("workflow.branchGroup.2");
        String zhuI18n = ResourceUtil.getString("org.member_form.departments.label");
        String fuI18n = ResourceUtil.getString("workflow.branchGroup.2.2");
        String jianI18n = ResourceUtil.getString("workflow.branchGroup.2.3");
        String includeI18n = ResourceUtil.getString("workflow.branchGroup.2.4");
        String excludeI18n = ResourceUtil.getString("workflow.branchGroup.2.5");
        try {
            int startPos = functionName.length();
            int endPos = group.length() - 1;
            String paramStr = group.substring(startPos, endPos);
            int firstDoubleQuote = paramStr.indexOf("\"");
            int lastDoubleQuote = paramStr.lastIndexOf("\"");
            //字符串类型的参数也可以以单引号存在
            if (firstDoubleQuote < 0) {
                firstDoubleQuote = paramStr.indexOf("'");
            }
            if (lastDoubleQuote < 0) {
                lastDoubleQuote = paramStr.lastIndexOf("'");
            }
            String booleanParamStr = paramStr.substring(0, firstDoubleQuote).trim();
            if (!booleanParamStr.endsWith(",")) {
                throw new BPMException(group + paramFormatI18n);//"参数格式不对");
            }
            String departmentIdStr = paramStr.substring(firstDoubleQuote + 1, lastDoubleQuote);
            booleanParamStr = booleanParamStr.substring(0, booleanParamStr.length() - 1);
            String[] booleanParams = booleanParamStr.split(",", -1);
            if (booleanParams.length != 3) {
                throw new BPMException(group + paramFormatI18n);//"参数格式不对");
            }
            String[] departmentParams = departmentIdStr.split(",", -1);
            String param0 = booleanParams[0].trim();//是否为所属部门
            String param1 = booleanParams[1].trim();//是否为副岗部门
            String param2 = booleanParams[2].trim();//是否为兼职部门
            if (!("true".equals(param0) || "false".equals(param0))
                    || !("true".equals(param1) || "false".equals(param1))
                    || !("true".equals(param2) || "false".equals(param2))) {
                throw new BPMException(group + paramFormatI18n);//"参数格式不对");
            }
            //translateGroup += "[部门(";
            translateGroup += "["+departmentI18n+"(";
            boolean isBeforeTrue = false;
            if ("true".equals(param0)) {
                isBeforeTrue = true;
                translateGroup += zhuI18n;//"所属部门";
            }
            if ("true".equals(param1)) {
                if (isBeforeTrue) {
                    translateGroup += ","+fuI18n;//",副岗部门";
                } else {
                    translateGroup += fuI18n;//"副岗部门";
                    isBeforeTrue = true;
                }
            }
            if ("true".equals(param2)) {
                if (isBeforeTrue) {
                    translateGroup += ","+jianI18n;//",兼职部门";
                } else {
                    translateGroup += jianI18n;//"兼职部门";
                }
            }
            translateGroup += ")]";
            translateGroup += operator;
            translateGroup += "\"";
            boolean firstDepartment = true;
            for (String departmentIdParam : departmentParams) {
                String[] departmentIdParamTemp = departmentIdParam.trim().split("[|]", -1);
                if (departmentIdParamTemp.length != 2) {
                    throw new BPMException(group + paramFormatI18n);//"参数格式不对");
                }
                String departmentIdStr1 = departmentIdParamTemp[0].trim();
                String isIncludeChild = departmentIdParamTemp[1].trim();
                if (!("true".equals(isIncludeChild) || "false".equals(isIncludeChild))) {
                    throw new BPMException(group + paramFormatI18n);//"参数格式不对");
                }
                long departmentId = Long.parseLong(departmentIdStr1);
                V3xOrgDepartment department = orgManager.getDepartmentById(departmentId);
                if (null == department) {
//                    errorMsg = "部门分支条件：" + departmentId + "存在问题,该部门ID在系统中不存在。";
//                    throw new BPMException("部门分支条件：" + departmentId + "存在问题,该部门ID在系统中不存在。");
                    String name = departmentI18n;
                    errorMsg = ResourceUtil.getString("workflow.branchTranslate.4", name, departmentId, name);
                    throw new BPMException(errorMsg);
                } else {
                    String deparetmentName = department.getName();
                    if (firstDepartment) {
                        firstDepartment = false;
                        translateGroup += deparetmentName;
                    } else {
                        translateGroup += "," + deparetmentName;
                    }
                }
                if ("true".equals(isIncludeChild)) {
                    //有子部门时才显示"(包含子部门)",没子部门时不显示包含子部门;
                	List<V3xOrgDepartment> departs = this.getChildDepartments(departmentId, true);
                	if(Strings.isNotEmpty(departs)){
                		translateGroup += "("+includeI18n+")";
                	}
                } else {
                    //translateGroup += "(不包含子部门)";
                    translateGroup += "("+excludeI18n+")";
                }
            }
            translateGroup += "\"";
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            if (!"".equals(errorMsg)) {
                throw new BPMException(errorMsg, e);
            } else {
//                throw new BPMException("部门分支条件:" + group + "存在问题,请检查是否符合:" + functionName
//                        + "true/false,true/false,true/false,\"部门Id1|true/false[,部门Idn|true/false]\")分支函数定义格式！", e);
                String name = departmentI18n;
                String forrmat = functionName + "true/false,true/false,true/false,\""+name+"Id1|true/false[,"+name+"Idn|true/false]\")";
                errorMsg = ResourceUtil.getString("workflow.branchTranslate.5", name, group, forrmat);
                throw new BPMException(errorMsg);
            }
        }
        return translateGroup;
    }

    @Override
    public String branchTranslatePost(String group, String functionName, String operator) throws BPMException {
        String translateGroup = "";
        String errorMsg = "";
        String paramFormatI18n = ResourceUtil.getString("workflow.branchTranslate.3");
        String postI18n = ResourceUtil.getString("workflow.branchGroup.4");
        String zhuI18n = ResourceUtil.getString("workflow.branchGroup.4.1");
        String fuI18n = ResourceUtil.getString("workflow.branchGroup.4.2");
        String jianI18n = ResourceUtil.getString("workflow.branchGroup.4.3");
        String gpostI18n = ResourceUtil.getString("workflow.branchGroup.4.4");
        String apostI18n = ResourceUtil.getString("workflow.branchGroup.4.5");
    	String loginAccountI18n = ResourceUtil.getString("workflow.branchGroup.1.3");
        try {
        	String[] paramArray = ConditionValidateUtil.parseFunctionParamWithFunName(group);
        	if(paramArray!=null){
        		String postId = "";
        		String param0 = "";
        		if(paramArray.length==6 && "true".equals(paramArray[4].trim())){
        			translateGroup  += "["+postI18n+"("+loginAccountI18n+")]";
		            postId = paramArray[5].trim();
		            param0 = "true";
        		} else {
		            param0 = paramArray[0].trim();//是否为集团基准岗
		            String param1 = paramArray[1].trim();//是否为主岗
		            String param2 = paramArray[2].trim();//是否为副岗
		            String param3 = paramArray[3].trim();//是否为兼职岗
		            if (paramArray.length==5){
		            	postId = paramArray[4].trim();
		            } else if (paramArray.length==6){
		            	postId = paramArray[5].trim();
		            }
		            //translateGroup += "[岗位(";
		            translateGroup += "["+postI18n+"(";
		            boolean isBeforeTrue = false;
		            if ("true".equals(param1)) {
		                isBeforeTrue = true;
		                translateGroup += zhuI18n;//"主岗";
		            }
		            if ("true".equals(param2)) {
		                if (isBeforeTrue) {
		                    translateGroup += ","+fuI18n;//",副岗";
		                } else {
		                    translateGroup += fuI18n;//"副岗";
		                    isBeforeTrue = true;
		                }
		            }
		            if ("true".equals(param3)) {
		                if (isBeforeTrue) {
		                    translateGroup += ","+jianI18n;//,兼职岗";
		                } else {
		                    translateGroup += jianI18n;//"兼职岗";
		                }
		            }
		            translateGroup += ")]";
        		}
	            translateGroup += operator;
	            translateGroup += "\"";
	            postId = postId.substring(1, postId.length()-1);
	            V3xOrgPost post = orgManager.getPostById(Long.parseLong(postId));
	            if (null == post) {
	                if ("true".equals(param0)) {
	                    translateGroup += gpostI18n+":";//"集团基准岗:";
	                } else {
	                    translateGroup += apostI18n+":";//"单位自建岗:";
	                }
	//                errorMsg = "岗位分支条件：" + postId + "存在问题,该" + translateGroup + "ID在系统中不存在。";
	//                throw new BPMException("岗位分支条件：" + postId + "存在问题,该" + translateGroup + "ID在系统中不存在。");
	                String name = postI18n;
	                errorMsg = ResourceUtil.getString("workflow.branchTranslate.4", name, postId, name);
	                throw new BPMException(errorMsg);
	            } else {
                    if(post.getOrgAccountId().equals(OrgConstants.GROUPID)){
                    	translateGroup += gpostI18n+":";//"集团基准岗:";
                    }else{
                    	V3xOrgAccount account = getAccountById(post.getOrgAccountId().toString());
	                    translateGroup += account.getShortName() + ":";
                    }
	                translateGroup += post.getName();
	            }
	            translateGroup += "\"";
        	}
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            if (!"".equals(errorMsg)) {
                throw new BPMException(errorMsg, e);
            } else {
//                throw new BPMException("岗位分支条件:" + group + "存在问题,请检查是否符合:" + functionName
//                        + "true/false,true/false,true/false,true/false,\"岗位Id\")分支函数定义格式！", e);
                String name = postI18n;
                String forrmat = functionName + "true/false,true/false,true/false,true/false,\""+name+"Id\")";
                errorMsg = ResourceUtil.getString("workflow.branchTranslate.5", name, group, forrmat);
                throw new BPMException(errorMsg);
            }
        }
        return translateGroup;
    }

    @Override
    public String branchTranslateRole(String group, String functionName, String operator) throws BPMException {
        String translateGroup = "";
        boolean isDepManager = false;
        String roleI18n = ResourceUtil.getString("workflow.branchGroup.5");
        String formFiled = "";
        String paramFormatI18n = ResourceUtil.getString("workflow.branchTranslate.3");
        try {
        	String[] paramArray = ConditionValidateUtil.parseFunctionParamWithFunName(group);
        	if(paramArray!=null && paramArray.length>0){
	            String roleIdStr = paramArray[0].substring(1, paramArray[0].length()-1);
	            String tempTranslateGroup = "";
	            String departmentStr = "";
	            String roleId = roleIdStr.trim();
	            if ("AccountManager".equals(roleId)) {//单位主管
	            	if(paramArray.length==5 && "true".equals(paramArray[4].trim())){
	                	String loginAccountI18n = ResourceUtil.getString("workflow.branchGroup.1.3");
	                	departmentStr = "("+loginAccountI18n+")";
	                }
	                tempTranslateGroup += ResourceUtil.getString("sys.role.rolename.AccountManager");//"单位主管";
	            } else if ("AccountAdmin".equals(roleId)) {//单位管理员
	            	if(paramArray.length==5 && "true".equals(paramArray[4].trim())){
	                	String loginAccountI18n = ResourceUtil.getString("workflow.branchGroup.1.3");
	                	departmentStr = "("+loginAccountI18n+")";
	                }
	                tempTranslateGroup += ResourceUtil.getString("sys.role.rolename.AccountAdmin");//"单位管理员";
	            } else if ("HrAdmin".equals(roleId)) {//HR管理员
	            	if(paramArray.length==5 && "true".equals(paramArray[4].trim())){
	                	String loginAccountI18n = ResourceUtil.getString("workflow.branchGroup.1.3");
	                	departmentStr = "("+loginAccountI18n+")";
	                }
	                tempTranslateGroup += ResourceUtil.getString("sys.role.rolename.HrAdmin");//"HR管理员";
	            } else if ("FormAdmin".equals(roleId)) {//表单管理员
	            	if(paramArray.length==5 && "true".equals(paramArray[4].trim())){
	                	String loginAccountI18n = ResourceUtil.getString("workflow.branchGroup.1.3");
	                	departmentStr = "("+loginAccountI18n+")";
	                }
	                tempTranslateGroup += ResourceUtil.getString("sys.role.rolename.FormAdmin");//"表单管理员";
	            } else if ("SalaryAdmin".equals(roleId)) {//工资管理员
	            	if(paramArray.length==5 && "true".equals(paramArray[4].trim())){
	                	String loginAccountI18n = ResourceUtil.getString("workflow.branchGroup.1.3");
	                	departmentStr = "("+loginAccountI18n+")";
	                }
	                tempTranslateGroup += ResourceUtil.getString("sys.role.rolename.SalaryAdmin");//"工资管理员";
	            } else { 
	            	isDepManager = true;
	                if(roleId.matches("^-?\\d+$")){
	                    V3xOrgRole role = this.getRoleByName(roleId, AppContext.getCurrentUser().getLoginAccount());
	                    if(role!=null){
	                        tempTranslateGroup += role.getShowName();
	                    }
	                }else{
	                    //获取国际化KEY
	                    String i18nKey = WorkFlowMatchUserManager.ORGENT_META_KEY_DepManager_I18N_KEY;
	                    if(WorkFlowMatchUserManager.ORGENT_META_KEY_DepLeader.equals(roleId)){
	                        i18nKey = WorkFlowMatchUserManager.ORGENT_META_KEY_DepLeader_I18N_KEY;
	                    }else if(WorkFlowMatchUserManager.ORGENT_META_KEY_DepAdmin.equals(roleId)){
	                        i18nKey = WorkFlowMatchUserManager.ORGENT_META_KEY_DepAdmin_I18N_KEY;
	                    }else if(WorkFlowMatchUserManager.departmentExchangeRoleName.equals(roleId)){
	                        i18nKey = WorkFlowMatchUserManager.departmentExchangeRoleName_I18N_KEY;
	                    }
	                    tempTranslateGroup += ResourceUtil.getString(i18nKey);//"部门主管";
	                }
	                if(paramArray.length == 6){
	                    formFiled = paramArray[5];
	                }
	                if((paramArray.length==5 || paramArray.length==6) && "true".equals(paramArray[4].trim())){
	                	String loginAccountI18n = ResourceUtil.getString("workflow.branchGroup.1.3");
	                	departmentStr = "("+loginAccountI18n+")";
	                } else if(paramArray.length >= 4){
		                String param1 = paramArray[1].trim();//是否为所属部门
		                String param2 = paramArray[2].trim();//是否为副岗部门
		                String param3 = paramArray[3].trim();//是否为兼职部门
		                if (!("true".equals(param1) || "false".equals(param1))
		                        || !("true".equals(param2) || "false".equals(param2))
		                        || !("true".equals(param3) || "false".equals(param3))) {
		                    throw new BPMException(group + paramFormatI18n);//"参数格式不对");
		                }
		                departmentStr += "(";
		                if("true".equals(param1)){
		                    departmentStr += ResourceUtil.getString("org.member_form.departments.label")+",";//"所属部门,";
		                }
		                if("true".equals(param2)){
		                    departmentStr += ResourceUtil.getString("workflow.branchGroup.2.6")+",";//"非主岗部门,";
		                }
		                if("true".equals(param3)){
		                    departmentStr += ResourceUtil.getString("workflow.branchGroup.2.3")+",";//"兼职部门,";
		                }
		                if(departmentStr.endsWith(",")){
		                    departmentStr = departmentStr.substring(0, departmentStr.length() - 1);
		                }
		                departmentStr += ")";
	                }
	            }
	            String roleName = "";
	            try{
	            	Long roleIdLong = Long.parseLong(roleId);
	            	V3xOrgRole role = this.getRoleById(roleIdLong);
	            	if(role!=null){
	            		roleName = role.getShowName();
	            	}
	            }catch(NumberFormatException e){
	            	V3xOrgRole role = this.getRoleByName(roleId, AppContext.getCurrentUser().getLoginAccount());
	            	if(role!=null){
	            		roleName = role.getShowName();
	            	}
	            }
	            if(!"".equals(roleName)){
	            	tempTranslateGroup = roleName;
	            }
	            //translateGroup += "[角色" + departmentStr + "]";
	            translateGroup += "["+formFiled +roleI18n+"" + departmentStr + "]";
	            translateGroup += operator;
	            translateGroup += "'";
	            translateGroup += tempTranslateGroup;
	            translateGroup += "'";
	            if(!isDepManager){
	                if(paramArray.length != 1){
	                    throw new BPMException(group + paramFormatI18n);//"参数格式不对");
	                }
	            }
        	}
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            String exceptionDescribe = "";
            String name = roleI18n;
            if(isDepManager){
//                exceptionDescribe = "角色分支条件:" + group + "存在问题,请检查是否符合:" + functionName + "\"角色Id\",true/false,true/false,true/false)分支函数定义格式！";
                String forrmat = functionName + "\""+name+"Id\",true/false,true/false,true/false,true/false)";
                exceptionDescribe = ResourceUtil.getString("workflow.branchTranslate.5", name, group, forrmat);
            } else {
//                exceptionDescribe = "角色分支条件:" + group + "存在问题,请检查是否符合:" + functionName + "\"角色Id\")分支函数定义格式！";
                String forrmat = functionName + "\""+name+"Id\")";
                exceptionDescribe = ResourceUtil.getString("workflow.branchTranslate.5", name, group, forrmat);
            }
            throw new BPMException(exceptionDescribe, e);
        }
        return translateGroup;
    }

    @Override
    public String branchTranslateTeam(String group, String functionName, String operator) throws BPMException {
        String translateGroup = "";
        String errorMsg = "";
        String zuI18n = ResourceUtil.getString("workflow.branchGroup.6");
        String baoI18n = ResourceUtil.getString("workflow.branchGroup.6.1");
        String bubaoI18n = ResourceUtil.getString("workflow.branchGroup.6.2");
        try {
            String[] paramArray = ConditionValidateUtil.parseFunctionParamWithFunName(group);
            String type = paramArray[0].substring(1, paramArray[0].length()-1).trim();
            if ("Team".equalsIgnoreCase(type)) {
                String teamId =  paramArray[1].substring(1, paramArray[1].length()-1).trim();
                translateGroup += "["+zuI18n+"]";
                translateGroup += operator;
                translateGroup += "\"";
                V3xOrgTeam team = orgManager.getTeamById(Long.parseLong(teamId));
                if (null == team) {
                    String name = zuI18n;
                    errorMsg = ResourceUtil.getString("workflow.branchTranslate.4", name, teamId, name);
                    throw new BPMException(errorMsg);
                } else {
                    translateGroup += team.getName();
                }
                translateGroup += "\"";
            } else {
                if(paramArray!=null && paramArray.length==3){
                    if(operator.indexOf("==")>-1){
                        translateGroup = paramArray[1] + " "+baoI18n+" " + paramArray[2];
                    } else {
                        translateGroup = paramArray[1] + " "+bubaoI18n+" " + paramArray[2];
                    }
                } else {
                    translateGroup = group;
                }
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            if (!"".equals(errorMsg)) {
                throw new BPMException(errorMsg, e);
            } else {
//                throw new BPMException(
//                        "组分支条件:" + group + "存在问题,请检查是否符合:" + functionName + "\"team\",\"组Id\")分支函数定义格式！", e);
                String name = zuI18n;
                String forrmat = functionName + "\"team\",\""+name+"Id\")";
                errorMsg = ResourceUtil.getString("workflow.branchTranslate.5", name, group, forrmat);
                throw new BPMException(errorMsg);
            }
        }
        return translateGroup;
    }

    @Override
    public User getCurrentUser() throws BPMException {
        com.seeyon.ctp.common.authenticate.domain.User user = AppContext.getCurrentUser();
        if (null != user) {
            User myUser = new User();
            if (null != user.getLoginAccount()) {
                myUser.setAccountId(String.valueOf(user.getLoginAccount()));
            }
            if (null != user.getLoginAccountName()) {
                myUser.setAccountShortName(user.getLoginAccountName());
            }
            if (null != user.getId()) {
                myUser.setId(String.valueOf(user.getId()));
            }
            if (null != user.getName()) {
                myUser.setName(user.getName());
            }
            return myUser;
        }
        return null;
    }

    @Override
    public boolean isRootPost(String account, String partyType, String partyId) throws BPMException {
        try {
            V3xOrgPost post = null;
            if(WorkflowUtil.isLong(partyId)){//id为数字
                post = orgManager.getBMPostByPostId(Long.parseLong(partyId));
            }
            if (null == post) {
                return false;
            } else {
                String returnId = post.getId().toString();
                if (returnId.equals(partyId)) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new BPMException(e);
        }
    }

    @Override
    public List<WFMoreSignSelectPerson> findMoreSignPersons(String typeAndIds) {
        List<WFMoreSignSelectPerson> msps = new ArrayList<WFMoreSignSelectPerson>();
        try {
            List<V3xOrgEntity> ents = orgManager.getEntities(typeAndIds);
            String edocManagerRoleName = ResourceUtil.getString("sys.role.rolename."
                    + OrgConstants.Role_NAME.Departmentexchange.name());
            String depManagerRoleName = ResourceUtil.getString("sys.role.rolename."
                    + OrgConstants.Role_NAME.DepManager.name());
            String depAdminRoleName = ResourceUtil.getString("sys.role.rolename."
                    + OrgConstants.Role_NAME.DepAdmin.name());
            List<V3xOrgMember> memList = new ArrayList<V3xOrgMember>();
            for (V3xOrgEntity ent : ents) {
                WFMoreSignSelectPerson msp = new WFMoreSignSelectPerson();
                msp.setSelObj(ent);
                List<V3xOrgMember> selPersons = new ArrayList<V3xOrgMember>();
                if (V3xOrgEntity.ORGENT_TYPE_MEMBER.equals(ent.getEntityType())) {
                    selPersons.add((V3xOrgMember) ent);
                } else if (V3xOrgEntity.ORGENT_TYPE_DEPARTMENT.equals(ent.getEntityType())) {
                    V3xOrgRole r1= orgManager.getRoleByName(OrgConstants.Role_NAME.Departmentexchange.name(), ent.getOrgAccountId());
                    if(null!=r1){
                        edocManagerRoleName = r1.getShowName();
                    }
                    V3xOrgRole r2= orgManager.getRoleByName(OrgConstants.Role_NAME.DepManager.name(), ent.getOrgAccountId());
                    if(null!=r2){
                        depManagerRoleName = r2.getShowName();
                    }
                    V3xOrgRole r3= orgManager.getRoleByName(OrgConstants.Role_NAME.DepAdmin.name(), ent.getOrgAccountId());
                    if(null!=r3){
                        depAdminRoleName= r3.getShowName();
                    }
                    memList = orgManager.getMembersByDepartmentRole(ent.getId(),
                            OrgConstants.Role_NAME.Departmentexchange.name());
                    memList = addRoleName(edocManagerRoleName, memList);
                    selPersons.addAll(memList);

                    memList = orgManager.getMembersByDepartmentRole(ent.getId(),
                            OrgConstants.Role_NAME.DepManager.name());
                    memList = addRoleName(depManagerRoleName, memList);
                    selPersons.addAll(memList);

                    memList = orgManager.getMembersByDepartmentRole(ent.getId(),
                            OrgConstants.Role_NAME.DepAdmin.name());
                    memList = addRoleName(depAdminRoleName, memList);
                    selPersons.addAll(memList);
                }
                msp.setSelPersons(selPersons);
                msps.add(msp);
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return msps;
    }

    /**
     * 人员名称前加角色名称
     * @param roleaName
     * @return
     */
    private List<V3xOrgMember> addRoleName(String roleName, List<V3xOrgMember> ml) {
        List<V3xOrgMember> rml = new ArrayList<V3xOrgMember>();
        if(null!=ml){
            for (V3xOrgMember mem : ml) {
                V3xOrgMember tm = new V3xOrgMember();
                tm.setId(mem.getId());
                tm.setName(roleName + " " + mem.getName());
                tm.setOrgAccountId(mem.getOrgAccountId());
                rml.add(tm);
            }
        }
        return rml;
    }

    @Override
    public boolean isCurrentLockUserEffective(String modifyUserId) {
        if (modifyUserId != null && !"".equals(modifyUserId)) {
            V3xOrgMember member = null;
            try {
                member = orgManager.getMemberById(Long.parseLong(modifyUserId));
            } catch (NumberFormatException e) {
                log.error("", e);
            } catch (BusinessException e) {
                log.error("", e);
            }
            if (member != null) {
                if (onLineManager.isOnline(member.getLoginName())) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public String[] getAcountExcludeElements() {
        try {
            List<String> set= new ArrayList<String>();
            List<String> set1= new ArrayList<String>();
            Map<Long,Integer> allAccountMemberNums = orgManager.getMemberNumsMapWithConcurrent();
            
            for (Long accountId : allAccountMemberNums.keySet()) {
                Integer num= allAccountMemberNums.get(accountId);
                
                if(num > 1500){
                    String excludeStr= "Account|"+accountId+"|wf|"+accountId;
                    set.add(excludeStr);
                }
                else{
                    String excludeStr1= "Account|"+accountId+"|"+num+"|-";
                    set1.add(excludeStr1);
                }
            }

            String result1= StringUtils.join(set,",");
            String result2= StringUtils.join(set1,",");
            String[] result= new String[]{result1,result2};
            
            return result;
        }
        catch (BusinessException e) {
            log.error("", e);
        }
        return new String[]{"",""};
    }

    @Override
    public boolean getDeptAndPostRelation(Long departmentId, Long postId) {
        try {
            List<V3xOrgPost> posts= orgManager.getDepartmentPost(departmentId);
            if(null!=posts){
                for (V3xOrgPost v3xOrgPost : posts) {
                    if(v3xOrgPost.getId().longValue()==postId.longValue()){
                        return true;
                    }
                }
            }
        } catch (BusinessException e) {
            log.error("", e);
        }
        return false;
    }

    @Override
    public List<ValidateResultVO> validateLinkCondition(String templateId,String processName,String flowType,List<BPMTransition> links) {
        List<ValidateResultVO> list1= new ArrayList<ValidateResultVO>();
        List<ValidateResultVO> list0= new ArrayList<ValidateResultVO>();
        if(null!=links && !links.isEmpty()){
            int linkSize= links.size();
            //对分支表达式进行转换
            Vector<String> regExpCells = new Vector<String>();
            regExpCells.add("(include\\(.*?\\))");
            regExpCells.add("(exclude\\(.*?\\))");
            regExpCells.add("(isRole\\(.*?\\))");
            regExpCells.add("(isNotRole\\(.*?\\))");
            regExpCells.add("(isPost\\(.*?\\))");
            regExpCells.add("(isNotPost\\(.*?\\))");
            regExpCells.add("(isDep\\(.*?\\))");
            regExpCells.add("(isNotDep\\(.*?\\))");
            regExpCells.add("(isAccount\\(.*?\\))");
            regExpCells.add("(isNotAccount\\(.*?\\))");
            regExpCells.add("(isLoginAccount\\(.*?\\))");
            regExpCells.add("(isNotLoginAccount\\(.*?\\))");
            regExpCells.add("(isLevel\\(.*?\\))");
            regExpCells.add("(isNotLevel\\(.*?\\))");
            regExpCells.add("(compareLevel\\(.*?\\))");
            regExpCells.add("(compareField\\(.*?\\))");
            for(int j=0;j<linkSize;j++){
                BPMTransition link= links.get(j);
                if( link.getConditionType()==1 || link.getConditionType()==4){
                    //自动强制和非强制分支进行下面的校验
                    String linkId= link.getId();
                    String conditionTitle= link.getConditionTitle();
                    String branchExpression= link.getFormCondition();
                    if(Strings.isNotBlank(branchExpression)){
                        //正式开始组织模型和表单分支条件的翻译
                        boolean isFind= false;
                        for (String regExp : regExpCells) {
                            if(isFind){
                                break;
                            }
                            Pattern p = Pattern.compile(regExp);
                            Matcher m = p.matcher(branchExpression);
                            try {
                                while (m.find()) {
                                    String group = m.group().trim();
                                    if (group.startsWith("include(") || group.startsWith("exclude(")) {//包含函数、不包含函数
                                        String[] paramArray = ConditionValidateUtil.parseFunctionParamWithFunName(group);
                                        String type = paramArray[0].substring(1, paramArray[0].length()-1).trim();
                                        if ("Team".equals(type)) {
                                            String teamId =  paramArray[1].substring(1, paramArray[1].length()-1).trim();
                                            V3xOrgTeam team = orgManager.getTeamById(Long.parseLong(teamId));
                                            if (null == team || !team.isValid()) {
                                                ValidateResultVO vo= new ValidateResultVO(templateId, processName, linkId, 
                                                        "", "", teamId, ResourceUtil.getString("workflow.branchGroup.6")/*"组"*/, V3xOrgEntity.ORGENT_TYPE_TEAM, flowType, "link",conditionTitle,1);
                                                list1.add(vo);
                                                isFind= true;
                                                break;
                                            }
                                        }
                                    } else if (group.startsWith("isRole(") || group.startsWith("isNotRole(")) {//角色等于函数、角色不等于函数
                                        String[] paramArray = ConditionValidateUtil.parseFunctionParamWithFunName(group);
                                        if(paramArray!=null && paramArray.length>0){
                                            String roleIdStr = paramArray[0].substring(1, paramArray[0].length()-1);
                                            String roleId = roleIdStr.trim();
                                            V3xOrgRole role =null;
                                            if(!WorkflowUtil.isLong(roleId)){
                                                role = this.getRoleByName(roleId, AppContext.getCurrentUser().getLoginAccount());
                                            }else{
                                                Long roleIdLong = Long.parseLong(roleId);
                                                role = this.getRoleById(roleIdLong);
                                            }
                                            if(role==null || !role.isValid()){
                                                ValidateResultVO vo= new ValidateResultVO(templateId, processName, linkId, 
                                                        "", "", roleId, ResourceUtil.getString("workflow.branchGroup.5")/*"角色"*/, V3xOrgEntity.ORGENT_TYPE_ROLE, flowType, "link",conditionTitle,1);
                                                list1.add(vo);
                                                isFind= true;
                                                break;
                                            }
                                        }
                                    } else if (group.startsWith("isPost(") || group.startsWith("isNotPost(")) {//岗位等于函数
                                        String[] paramArray = ConditionValidateUtil.parseFunctionParamWithFunName(group);
                                        if(paramArray!=null){
                                            String postId = "";
                                            if(paramArray.length==6 && "true".equals(paramArray[4].trim())){
                                                postId = paramArray[5].trim();
                                            } else {
                                                if (paramArray.length==5){
                                                    postId = paramArray[4].trim();
                                                } else if (paramArray.length==6){
                                                    postId = paramArray[5].trim();
                                                }
                                            }
                                            if(Strings.isNotBlank(postId)){
                                                postId = postId.substring(1, postId.length()-1);
                                                V3xOrgPost post = null;
                                                if(WorkflowUtil.isLong(postId)){
                                                    post = orgManager.getPostById(Long.parseLong(postId));
                                                }
                                                if (null == post || !post.isValid()) {
                                                    ValidateResultVO vo= new ValidateResultVO(templateId, processName, linkId, 
                                                            "", "", postId, ResourceUtil.getString("workflow.branchGroup.4")/*"岗位"*/, V3xOrgEntity.ORGENT_TYPE_POST, flowType, "link",conditionTitle,1);
                                                    list1.add(vo);
                                                    isFind= true;
                                                    break;
                                                }
                                            }
                                        }      
                                    } else if (group.startsWith("isDep(") || group.startsWith("isNotDep(")) {//部门等于函数
                                        String functionName= "";
                                        if(group.startsWith("isDep(")){
                                            functionName= "isDep(";
                                        }else if(group.startsWith("isNotDep(")){
                                            functionName= "isNotDep(";
                                        }
                                        if(Strings.isNotBlank(functionName)){
                                            int startPos = functionName.length();
                                            int endPos = group.length() - 1;
                                            String paramStr = group.substring(startPos, endPos);
                                            int firstDoubleQuote = paramStr.indexOf("\"");
                                            int lastDoubleQuote = paramStr.lastIndexOf("\"");
                                            //字符串类型的参数也可以以单引号存在
                                            if (firstDoubleQuote < 0) {
                                                firstDoubleQuote = paramStr.indexOf("'");
                                            }
                                            if (lastDoubleQuote < 0) {
                                                lastDoubleQuote = paramStr.lastIndexOf("'");
                                            }
                                            String booleanParamStr = paramStr.substring(0, firstDoubleQuote).trim();
                                            if (booleanParamStr.endsWith(",")) {
                                                String departmentIdStr = paramStr.substring(firstDoubleQuote + 1, lastDoubleQuote);
                                                booleanParamStr = booleanParamStr.substring(0, booleanParamStr.length() - 1);
                                                String[] booleanParams = booleanParamStr.split(",", -1);
                                                if (booleanParams.length != 3) {
                                                    String[] departmentParams = departmentIdStr.split(",", -1);
                                                    String param0 = booleanParams[0].trim();//是否为所属部门
                                                    String param1 = booleanParams[1].trim();//是否为副岗部门
                                                    String param2 = booleanParams[2].trim();//是否为兼职部门
                                                    if (!("true".equals(param0) || "false".equals(param0))
                                                            || !("true".equals(param1) || "false".equals(param1))
                                                            || !("true".equals(param2) || "false".equals(param2))) {
                                                        
                                                    }else{
                                                        for (String departmentIdParam : departmentParams) {
                                                            String[] departmentIdParamTemp = departmentIdParam.trim().split("[|]", -1);
                                                            if (departmentIdParamTemp.length != 2) {
                                                                break;
                                                            }
                                                            String departmentIdStr1 = departmentIdParamTemp[0].trim();
                                                            String isIncludeChild = departmentIdParamTemp[1].trim();
                                                            if (!("true".equals(isIncludeChild) || "false".equals(isIncludeChild))) {
                                                                break;
                                                            }
                                                            long departmentId = Long.parseLong(departmentIdStr1);
                                                            V3xOrgDepartment department = orgManager.getDepartmentById(departmentId);
                                                            if (null == department || !department.isValid()) {
                                                                ValidateResultVO vo= new ValidateResultVO(templateId, processName, linkId, 
                                                                        "", "", departmentIdStr1, ResourceUtil.getString("workflow.branchGroup.2")/*"部门"*/, V3xOrgEntity.ORGENT_TYPE_DEPARTMENT, flowType, "link",conditionTitle,1);
                                                                list1.add(vo);
                                                                isFind= true;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else if (group.startsWith("isAccount(") || group.startsWith("isNotAccount(")) {//单位等于函数
                                        String[] paramArray = ConditionValidateUtil.parseFunctionParamWithFunName(group);
                                        if(paramArray!=null && paramArray.length>=3){
                                            String accountIdStr = paramArray[2].substring(1, paramArray[2].length()-1);
                                            String accountId = accountIdStr.trim();
                                            V3xOrgAccount account = orgManager.getAccountById(Long.parseLong(accountId));
                                            if (null == account || !account.isValid()) {
                                                ValidateResultVO vo= new ValidateResultVO(templateId, processName, linkId, 
                                                        "", "", accountId, ResourceUtil.getString("workflow.branchGroup.1")/*"单位"*/, V3xOrgEntity.ORGENT_TYPE_ACCOUNT, flowType, "link",conditionTitle,1);
                                                list1.add(vo); 
                                                isFind= true;
                                                break;
                                            }
                                        }
                                    }  else if (group.startsWith("isLoginAccount(") || group.startsWith("isNotLoginAccount(")) {//单位等于函数
                                        String[] paramArray = ConditionValidateUtil.parseFunctionParamWithFunName(group);
                                        if(paramArray!=null && paramArray.length>0){
                                            String accountIdStr = paramArray[0].substring(1, paramArray[0].length()-1);
                                            String accountId = accountIdStr.trim();
                                            V3xOrgAccount account = orgManager.getAccountById(Long.parseLong(accountId));
                                            if(account==null || !account.isValid()){
                                                ValidateResultVO vo= new ValidateResultVO(templateId, processName, linkId, 
                                                        "", "", accountId, ResourceUtil.getString("workflow.branchGroup.1")/*"单位"*/, V3xOrgEntity.ORGENT_TYPE_ACCOUNT, flowType, "link",conditionTitle,1);
                                                list1.add(vo); 
                                                isFind= true;
                                                break;
                                            }
                                        }
                                    }  else if (group.startsWith("isLevel(") || group.startsWith("isNotLevel(")) {//职务级别等于函数
                                        String[] paramArray = ConditionValidateUtil.parseFunctionParamWithFunName(group);
                                        if(paramArray!=null){
                                            String param0 = paramArray[0].trim();//是否为集团职务级别true/false
                                            String param1 = paramArray[1].trim();//是否为主职务级别true/false
                                            String param2 = paramArray[2].trim();//是否为兼职职务级别true/false
                                            String param3 = "";
                                            if(paramArray.length==5 && "true".equals(paramArray[3])){
                                                param3 = paramArray[4].trim();//职务级别ID
                                            } else {
                                                if (!("true".equals(param0) || "false".equals(param0))
                                                        || !("true".equals(param1) || "false".equals(param1))
                                                        || !("true".equals(param2) || "false".equals(param2))) {
                                                    
                                                }else{
                                                    param3 = paramArray[3].trim();//职务级别ID
                                                    if(paramArray.length==5){
                                                        param3 = paramArray[4].trim();
                                                    }
                                                }
                                            }
                                            if(Strings.isNotBlank(param3)){
                                                param3 = param3.substring(1, param3.length()-1);
                                                V3xOrgLevel level = orgManager.getLevelById(Long.parseLong(param3));
                                                if (null == level || !level.isValid()) {
                                                    ValidateResultVO vo= new ValidateResultVO(templateId, processName, linkId, 
                                                            "", "", param3, ResourceUtil.getString("workflow.branchGroup.3")/*"职务级别"*/, V3xOrgEntity.ORGENT_TYPE_LEVEL, flowType, "link",conditionTitle,1);
                                                    list1.add(vo); 
                                                    isFind= true;
                                                    break;
                                                }
                                            }
                                        }
                                    }  else if (group.startsWith("compareLevel(")) {//职务级别高于与低于函数
                                        String[] paramArray = ConditionValidateUtil.parseFunctionParamWithFunName(group);
                                        if(paramArray!=null && paramArray.length>=3){
                                            String levelIdString= "";
                                            if(paramArray.length==3){//三个参数表示是表单职务级别比较
                                                levelIdString = paramArray[2].substring(1, paramArray[2].length()-1);
                                            } else if(paramArray.length==5){//五个参数表示是组织模型职务级别比较
                                                levelIdString = paramArray[4].substring(1, paramArray[4].length()-1);
                                            }
                                            V3xOrgLevel level = null;
                                            if(WorkflowUtil.isLong(levelIdString)){
                                                long levelId = Long.parseLong(levelIdString);
                                                level = orgManager.getLevelById(levelId);
                                            }
                                            if(level==null || !level.isValid()){
                                                ValidateResultVO vo= new ValidateResultVO(templateId, processName, linkId, 
                                                        "", "", levelIdString, ResourceUtil.getString("workflow.branchGroup.3")/*"职务级别"*/, V3xOrgEntity.ORGENT_TYPE_LEVEL, flowType, "link",conditionTitle,1);
                                                list1.add(vo); 
                                                isFind= true;
                                                break;
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                log.error("",e);
                            }
                        }
                    }
                }
            }
        }
        return list1;
    }

    @Override
    public List<ValidateResultVO> validateNodeInfo(String templateId,String processName,String flowType,List<BPMAbstractNode> nodes) {
        List<ValidateResultVO> list1= new ArrayList<ValidateResultVO>();
       // List<ValidateResultVO> list0= new ArrayList<ValidateResultVO>();
        if(null!=nodes && !nodes.isEmpty()){
            String regex= "[-]{0,1}[\\d]+?";
            int nodeSize= nodes.size();
            for(int j=0;j<nodeSize;j++){
                BPMAbstractNode node= nodes.get(j);
                if(node.getNodeType().equals(BPMAbstractNode.NodeType.humen)){//人工节点
                    try {
                      //节点ID和名称
                        String nodeId= node.getId();
                        String nodeName= node.getName();
                        //节点权限
                        String policyId= node.getSeeyonPolicy().getId();
                        String policyName= node.getSeeyonPolicy().getName();
                        //节点类型
                        List<BPMActor> actors = node.getActorList();
                        if(actors!=null && actors.size()>0){
                            BPMActor actor = actors.get(0);
                            BPMParticipant party = actor.getParty();
                            //partyId就是actor标签的partyType属性的值
                            String partyId = party.getId();
                            //partyTypeId就是actor标签的partyId属性的值
                            String partyTypeId = party.getType().id;
                            if ("user".equals(partyTypeId)|| partyTypeId.equals(V3xOrgEntity.ORGENT_TYPE_MEMBER)){
                                User user = null;
                                if(partyId.matches(regex)){//id为数字
                                    user = this.getUserById(partyId,false);
                                }
                                if(user==null){//人员不存在！
                                    ValidateResultVO vo= new ValidateResultVO(templateId, processName, nodeId, 
                                            "", "", partyId, ResourceUtil.getString("workflow.branchGroup.7")/*"人员"*/, V3xOrgEntity.ORGENT_TYPE_MEMBER, flowType, "node",nodeName,1);
                                    list1.add(vo);
                                }else{
                                    ValidateResultVO vo= new ValidateResultVO(templateId, processName, nodeId, 
                                            "", "", partyId, ResourceUtil.getString("workflow.branchGroup.7")/*"人员"*/, V3xOrgEntity.ORGENT_TYPE_MEMBER, flowType, "node",nodeName,0);
                             //       list0.add(vo);
                                }
                            }else if (partyTypeId.equals(V3xOrgEntity.ORGENT_TYPE_ACCOUNT)
                                    || partyTypeId.equals(V3xOrgEntity.ORGENT_TYPE_DEPARTMENT)
                                    || partyTypeId.equals(V3xOrgEntity.ORGENT_TYPE_LEVEL)
                                    || partyTypeId.equals(V3xOrgEntity.ORGENT_TYPE_TEAM)
                                    || partyTypeId.equals(WorkFlowMatchUserManager.ORGREL_TYPE_ACCOUNT_ROLE)
                                    || partyTypeId.equals(V3xOrgEntity.ORGENT_TYPE_POST)){ //岗位
                                V3xOrgEntity entity = null;
                                if(partyId.matches(regex)){//id为数字
                                    Long partyIdLong = Long.parseLong(partyId);
                                    entity = this.getEntity(partyTypeId, partyIdLong);
                                }
                                if(entity==null || !entity.isValid()){
                                    ValidateResultVO vo= new ValidateResultVO(templateId, processName, nodeId, 
                                            "", "", partyId, ResourceUtil.getString("workflow.branchGroup.4")/*"岗位"*/, partyTypeId, flowType, "node",nodeName,1);
                                    list1.add(vo);
                                }else{
                                    ValidateResultVO vo= new ValidateResultVO(templateId, processName, nodeId, 
                                            "", "", partyId, ResourceUtil.getString("workflow.branchGroup.4")/*"岗位"*/, partyTypeId, flowType, "node",nodeName,0);
                              //      list0.add(vo);
                                }
                            }else if(partyTypeId.equals(V3xOrgEntity.ORGREL_TYPE_DEP_POST)){//部门岗位
                                //部门角色和部门岗位的查询方式不一样
                                List<V3xOrgEntity> entityList = this.getEntityByDepartmentPostorRole(partyTypeId+"|"+partyId);
                                if(entityList==null || entityList.size()==0){
                                    ValidateResultVO vo= new ValidateResultVO(templateId, processName, nodeId, 
                                            "", "",partyTypeId+"|"+partyId, ResourceUtil.getString("workflow.match.log.matchType.deptPost")/*"部门岗位"*/, V3xOrgEntity.ORGREL_TYPE_DEP_POST, flowType, "node",nodeName,1);
                                    list1.add(vo);
                                }else{
                                    for(V3xOrgEntity e : entityList){
                                        if(!e.isValid()){
                                            ValidateResultVO vo= new ValidateResultVO(templateId, processName, nodeId, 
                                                    "", "",e.getId().toString(), ResourceUtil.getString("workflow.branchGroup.4")/*"岗位"*/, V3xOrgEntity.ORGREL_TYPE_DEP_POST, flowType, "node",nodeName,1);
                                            list1.add(vo);
                                        }else{
                                            ValidateResultVO vo= new ValidateResultVO(templateId, processName, nodeId, 
                                                    "", "",e.getId().toString(), ResourceUtil.getString("workflow.branchGroup.4")/*"岗位"*/, V3xOrgEntity.ORGREL_TYPE_DEP_POST, flowType, "node",nodeName,0);
                                           // list0.add(vo);
                                        }
                                    }
                                }
                            }else if(partyTypeId.equals(V3xOrgEntity.ORGREL_TYPE_DEP_ROLE)){//部门角色
                                Long roleId = null;
                                V3xOrgRole role = null;
                                String roleIdorName = partyId.split("_")[1];
                                String roleIdStr= partyId.split("_")[0];
                                if(null==roleIdStr || !roleIdStr.matches(regex)){
                                    ValidateResultVO vo= new ValidateResultVO(templateId, processName, nodeId, 
                                            "", "",partyId, ResourceUtil.getString("workflow.match.log.matchType.deptRole")/*"部门角色"*/, V3xOrgEntity.ORGREL_TYPE_DEP_ROLE, flowType, "node",nodeName,1);
                                    list1.add(vo);
                                }else{
                                    long departmentId = Long.parseLong(roleIdStr);
                                    V3xOrgDepartment department = orgManager.getDepartmentById(departmentId);
                                    if(null==department || !department.isValid()){
                                        ValidateResultVO vo= new ValidateResultVO(templateId, processName, nodeId, 
                                                "", "",partyId, ResourceUtil.getString("workflow.match.log.matchType.deptRole")/*"部门角色"*/, V3xOrgEntity.ORGREL_TYPE_DEP_ROLE, flowType, "node",nodeName,1);
                                        list1.add(vo);
                                    }else{
                                        if(null==roleIdorName){
                                            ValidateResultVO vo= new ValidateResultVO(templateId, processName, nodeId, 
                                                    "", "",partyId, ResourceUtil.getString("workflow.match.log.matchType.deptRole")/*"部门角色"*/, V3xOrgEntity.ORGREL_TYPE_DEP_ROLE, flowType, "node",nodeName,1);
                                            list1.add(vo);
                                        }else{
                                            if(!roleIdorName.matches(regex)){
                                                role = this.getRoleByName(roleIdorName, department.getOrgAccountId());
                                            }else{
                                                roleId = Long.parseLong(roleIdorName);
                                                role = this.getRoleById(roleId);
                                            }
                                            if(role==null || !role.isValid() || role.getStatus().equals(0)){
                                                ValidateResultVO vo= new ValidateResultVO(templateId, processName, nodeId, 
                                                        "", "",partyId, ResourceUtil.getString("workflow.match.log.matchType.deptRole")/*"部门角色"*/, V3xOrgEntity.ORGREL_TYPE_DEP_ROLE, flowType, "node",nodeName,1);
                                                list1.add(vo);
                                            }else{
                                                ValidateResultVO vo= new ValidateResultVO(templateId, processName, nodeId, 
                                                        "", "",partyId, ResourceUtil.getString("workflow.match.log.matchType.deptRole")/*"部门角色"*/, V3xOrgEntity.ORGREL_TYPE_DEP_ROLE, flowType, "node",nodeName,0);
                                                list1.add(vo);
                                            }
                                        } 
                                    }
                                }
                            }else if(partyTypeId.equals(V3xOrgEntity.ORGENT_TYPE_DYNAMIC_ROLE) 
                                    || partyTypeId.equals(WorkFlowMatchUserManager.ORGENT_META_KEY_NODE)){//相对角色
                                //获取到角色名或者角色id
                                String roleIdorName = partyId;
                                if (partyId.startsWith(WorkFlowMatchUserManager.ORGENT_META_KEY_SEDNER)){
                                    roleIdorName = partyId.substring(WorkFlowMatchUserManager.ORGENT_META_KEY_SEDNER.length());
                                } else if(partyId.startsWith(WorkFlowMatchUserManager.ORGENT_META_KEY_NODEUSER)){
                                    roleIdorName = partyId.substring(WorkFlowMatchUserManager.ORGENT_META_KEY_NODEUSER.length());
                                }
                                V3xOrgRole role = null;
                                if(roleIdorName.startsWith(WorkFlowMatchUserManager.ORGENT_META_KEY_SUPER)){
                                    roleIdorName = roleIdorName.substring(WorkFlowMatchUserManager.ORGENT_META_KEY_SUPER.length());
                                }
                                //如果是空节点认为合法,部门成员和上级部门主管默认合法
                                if("".equals(roleIdorName) || WorkFlowMatchUserManager.ORGENT_META_KEY_BlankNode.equals(roleIdorName)
                                        || WorkFlowMatchUserManager.ORGENT_META_KEY_DEPMEMBER.equals(roleIdorName)
                                        || WorkFlowMatchUserManager.ORGENT_META_KEY_SUPERDEPMANAGER.equals(roleIdorName)){
                                    
                                } else {
                                    if(null==roleIdorName){
                                        ValidateResultVO vo= new ValidateResultVO(templateId, processName, nodeId, 
                                                "", "",partyId, ResourceUtil.getString("workflow.match.log.matchType.relativeRole")/*"相对角色"*/, V3xOrgEntity.ORGENT_TYPE_DYNAMIC_ROLE, flowType, "node",nodeName,1);
                                        list1.add(vo);
                                    }else {
                                        if(!roleIdorName.matches(regex)){
                                        	Long accountId = AppContext.currentAccountId();
                                            role = this.getRoleByName(roleIdorName, accountId);
                                        }else{
                                            Long partyIdLong = Long.parseLong(roleIdorName);
                                            role = this.getRoleById(partyIdLong);
                                        }
                                        if(role==null || !role.isValid() || role.getStatus().equals(0)){
                                            ValidateResultVO vo= new ValidateResultVO(templateId, processName, nodeId, 
                                                    "", "",partyId, ResourceUtil.getString("workflow.match.log.matchType.relativeRole")/*"相对角色"*/, V3xOrgEntity.ORGENT_TYPE_DYNAMIC_ROLE, flowType, "node",nodeName,1);
                                            list1.add(vo);
                                        }else{
                                            ValidateResultVO vo= new ValidateResultVO(templateId, processName, nodeId, 
                                                    "", "",partyId, ResourceUtil.getString("workflow.match.log.matchType.relativeRole")/*"相对角色"*/, V3xOrgEntity.ORGENT_TYPE_DYNAMIC_ROLE, flowType, "node",nodeName,0);
                                          //  list0.add(vo);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("",e);
                    }
                }
            }
        }
        List<ValidateResultVO> all= new ArrayList<ValidateResultVO>();
        all.addAll(list1);
        //all.addAll(list0);
        return all;
    }

	public void setJoinOrgManagerDirect(JoinOrgManagerDirect joinOrgManagerDirect) {
		this.joinOrgManagerDirect = joinOrgManagerDirect;
	}
    
}
