package com.seeyon.apps.synorg.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.ddhsynorg.manager.impl.DdhMqListenerManagerImpl;
import com.seeyon.apps.synorg.constants.SynOrgConstants;
import com.seeyon.apps.synorg.dao.SyncMemberDao;
import com.seeyon.apps.synorg.po.SynLog;
import com.seeyon.apps.synorg.po.SynMember;
import com.seeyon.apps.synorg.po.SynPost;
import com.seeyon.apps.synorg.scheduletask.SynOrgTask;
import com.seeyon.apps.synorg.util.ErrorMessageUtil;
import com.seeyon.ctp.organization.bo.OrganizationMessage;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgLevel;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgPost;
import com.seeyon.ctp.organization.bo.V3xOrgPrincipal;
import com.seeyon.ctp.organization.bo.OrganizationMessage.OrgMessage;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.organization.manager.OrgManagerDirect;

/**
 * 人员同步实现类
 * @author Yang.Yinghai
 * @date 2015-8-18下午4:35:46
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class SyncMemberManagerImpl implements SyncMemberManager {
	private static final Log log = LogFactory.getLog(SyncMemberManagerImpl.class);
    // 职务影射表
    private static String[][] otype = new String[][]{{"01", "公司总裁"}, {"02", "公司副总裁"}, {"021", "产出线总裁"}, {"022", "产出线副总裁"}, {"03", "总经理"}, {"04", "副总经理"}, {"041", "总监　"}, {"05", "经理"}, {"06", "副经理"}, {"07", "主管"}, {"08", "员工"}};

    /** 组织机构管理器 */
    private OrgManagerDirect orgManagerDirect;

    /** 组织机构管理器 */
    private SyncOrgManager syncOrgManager;

    /** 人员实体查询接口 */
    private SyncMemberDao syncMemberDao;

    /** 同步日志管理器 */
    private SyncLogManager syncLogManager;

    /**
     * {@inheritDoc}
     */
    @Override
    public void synAllMember() {
        /** 获取中间表人员数据信息 **/
    	List<SynMember> resMember = new ArrayList<SynMember>();
        List<SynMember> allMembers = syncMemberDao.findAll();
        System.out.println("人员同步开始!");
        log.info("人员同步开始!");
        if(allMembers != null && allMembers.size() > 0) {
            List<SynLog> logList = new ArrayList<SynLog>();
            for(int i = 0; i < allMembers.size(); i++) {
                SynMember synMember = allMembers.get(i);
                synMember.setSyncDate(new Date());
                SynLog synLog = new SynLog(SynOrgConstants.ORG_ENTITY_MEMBER, synMember.getCode(), synMember.getName());
                V3xOrgMember member = (V3xOrgMember)syncOrgManager.getEntityByProperty(V3xOrgMember.class.getSimpleName(), "code", synMember.getCode(), SynOrgConstants.ORG_SYNC_IS_GROUP?null:SynOrgConstants.DEFAULT_ACCOUNT_ID);
                try {
                    // 人员存在：做更新操作
                    if(member != null) {
//                        if("jqfu".equals(member.getLoginName())){
//                            System.out.println("111111111111111111");
//                        }
                        synLog.setEntityName(member.getName());
                        synLog.setSynType(SynOrgConstants.SYN_OPERATION_TYPE_UPDATE);
                        boolean isUpdate = false;
                        String updateInfo = "";
                        // 修改人员启用/停用状态
                        if(member.getEnabled().booleanValue() != synMember.getEnable().booleanValue()) {
                            updateInfo += "人员启/停用状态改为:" + synMember.getEnable() + " ";
                            member.setEnabled(synMember.getEnable());
                            isUpdate = true;
                        }
                        
                        //
                        if(synMember.getEnable()) {
                            // 修改人员登录名
                            if(!member.getLoginName().equals(synMember.getLoginName().trim())){
                                V3xOrgPrincipal principal  = new V3xOrgPrincipal(member.getId(), synMember.getLoginName(), SynOrgTask.getDefaultPassword().trim());
                                updateInfo += "登录名改为:" + synMember.getLoginName().trim() + " ";
                                member.setV3xOrgPrincipal(principal);
                                isUpdate = true;
                            }
                            // 修改人员名
                            if(!member.getName().equals(synMember.getName().trim())) {
                                updateInfo += "姓名改为:" + synMember.getName().trim() + " ";
                                member.setName(synMember.getName().trim());
                                isUpdate = true;
                            }
                            /*
                            // 修改人员排序号
                            if(member.getSortId() != null && synMember.getSortId() != null && member.getSortId().longValue() != synMember.getSortId().longValue()) {
                                updateInfo += "序号改为:" + synMember.getSortId() + " ";
                                member.setSortId(synMember.getSortId());
                                isUpdate = true;
                            }*/
                            // 修改人员部门
                            V3xOrgDepartment oldDept = (V3xOrgDepartment)syncOrgManager.getEntityByProperty(V3xOrgDepartment.class.getSimpleName(), "id", member.getOrgDepartmentId(), SynOrgConstants.ORG_SYNC_IS_GROUP?member.getOrgAccountId():SynOrgConstants.DEFAULT_ACCOUNT_ID);
                            V3xOrgDepartment newDept = (V3xOrgDepartment)syncOrgManager.getEntityByProperty(V3xOrgDepartment.class.getSimpleName(), "code", synMember.getDepartmentCode(), SynOrgConstants.ORG_SYNC_IS_GROUP?member.getOrgAccountId():SynOrgConstants.DEFAULT_ACCOUNT_ID);
                            if(oldDept != null && oldDept.getId() != -1) {
                                if(newDept != null && newDept.getId() != -1) {
                                    if(oldDept.getId().longValue() != newDept.getId().longValue()) {
                                        updateInfo += " 所属部门改为：" + newDept.getName() + " ";
                                        member.setOrgDepartmentId(newDept.getId());
                                        isUpdate = true;
                                    }
                                } else {
                                    synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
                                    synLog.setSynLog("所在部门(" + synMember.getDepartmentCode() + ")不存在");
                                    logList.add(synLog);
                                    synMember.setSyncState(SynOrgConstants.SYN_STATE_FAILURE);
                                    resMember.add(synMember);
                                    continue;
                                }
                            } else {
                                if(newDept != null && newDept.getId() != -1) {
                                    updateInfo += " 所属部门改为：" + newDept.getName() + " ";
                                    member.setOrgDepartmentId(newDept.getId());
                                    isUpdate = true;
                                } else {
                                    synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
                                    synLog.setSynLog("所在部门(" + synMember.getDepartmentCode() + ")不存在");
                                    logList.add(synLog);
                                    synMember.setSyncState(SynOrgConstants.SYN_STATE_FAILURE);
                                    resMember.add(synMember);
                                    continue;
                                }
                            }
                            // 修改人员岗位
                            if(SynOrgTask.getSynScope().contains("Post")) {
                                V3xOrgPost oldPost = (V3xOrgPost)syncOrgManager.getEntityByProperty(V3xOrgPost.class.getSimpleName(), "id", member.getOrgPostId(), SynOrgConstants.ORG_SYNC_IS_GROUP?member.getOrgAccountId():SynOrgConstants.DEFAULT_ACCOUNT_ID);
                                V3xOrgPost newPost = (V3xOrgPost)syncOrgManager.getEntityByProperty(V3xOrgPost.class.getSimpleName(), "name", synMember.getPostCode(), SynOrgConstants.ORG_SYNC_IS_GROUP?member.getOrgAccountId():SynOrgConstants.DEFAULT_ACCOUNT_ID);
                                if(oldPost != null && oldPost.getId() != -1) {
                                    if(newPost != null && newPost.getId() != -1) {
                                        if(oldPost.getId().longValue() != newPost.getId().longValue()) {
                                            updateInfo += " 所属岗位改为：" + newPost.getName() + " ";
                                            member.setOrgPostId(newPost.getId());
                                            isUpdate = true;
                                        }
                                    } else {
                                        member.setOrgPostId(addOrGetPostByName(null==synMember.getPostCode()?null:synMember.getPostCode().trim(),member).getId());
                                        updateInfo += " 所属岗位改为：" + synMember.getPostCode() + " ";
                                        isUpdate = true;
                                    }
                                } else {
                                    if(newPost != null && newPost.getId() != -1) {
                                        updateInfo += " 所属岗位改为：" + newPost.getName() + " ";
                                        member.setOrgPostId(newPost.getId());
                                        isUpdate = true;
                                    } else {
                                        // 如果没有岗位：则人员岗位默认设置为：无
                                        updateInfo += " 所属岗位为空由系统设置为默认岗位  ";
                                        member.setOrgPostId(addOrGetPostByName(null==synMember.getPostCode()?null:synMember.getPostCode().trim(),member).getId());
                                        isUpdate = true;
                                    }
                                }
                            }
                            // 修改人员职务
                            if(SynOrgTask.getSynScope().contains("Level")) {
                                V3xOrgLevel oldLevel = (V3xOrgLevel)syncOrgManager.getEntityByProperty(V3xOrgLevel.class.getSimpleName(), "id", member.getOrgLevelId(), SynOrgConstants.ORG_SYNC_IS_GROUP?member.getOrgAccountId():SynOrgConstants.DEFAULT_ACCOUNT_ID);
                                V3xOrgLevel newLevel = (V3xOrgLevel)syncOrgManager.getEntityByProperty(V3xOrgLevel.class.getSimpleName(), "code", synMember.getLevelCode(), SynOrgConstants.ORG_SYNC_IS_GROUP?member.getOrgAccountId():SynOrgConstants.DEFAULT_ACCOUNT_ID);
                                if(oldLevel != null && oldLevel.getId() != -1) {
                                    if(newLevel != null && newLevel.getId() != -1) {
                                        if(oldLevel.getId().longValue() != newLevel.getId().longValue()) {
                                            updateInfo += " 所属职务改为：" + newLevel.getName() + " ";
                                            member.setOrgLevelId(newLevel.getId());
                                            isUpdate = true;
                                        }
                                    } else {
                                        // 如果没有职务：则人员职务默认设置为：无
                                        updateInfo += " 所属职务为空由系统设置为默认职务:员工  ";
                                        member.setOrgLevelId(addOrGetLevelByName(synMember.getLevelCode().trim(),member).getId());
                                        isUpdate = true;
                                    }
                                } else {
                                    if(newLevel != null && newLevel.getId() != -1) {
                                        updateInfo += " 所属职务改为：" + newLevel.getName() + " ";
                                        member.setOrgLevelId(newLevel.getId());
                                        isUpdate = true;
                                    } else {
                                        // 如果没有职务：则人员职务默认设置为：无
                                        updateInfo += " 所属职务为空由系统设置为默认职务:员工  ";
                                        member.setOrgLevelId(addOrGetLevelByName(synMember.getLevelCode().trim(),member).getId());
                                        isUpdate = true;
                                    }
                                }
                            }
                            // 修改人员描述
                            if(synMember.getDescription() != null && !synMember.getDescription().trim().equals(member.getDescription())) {
                                updateInfo += "描述改为:" + synMember.getDescription().trim() + " ";
                                member.setDescription(synMember.getDescription().trim());
                                isUpdate = true;
                            }
                            // 修改邮件
                            if(synMember.getEmail() != null && !synMember.getEmail().trim().equals(member.getEmailAddress())) {
                                updateInfo += "邮件改为:" + synMember.getEmail().trim() + " ";
                                member.setProperty("emailaddress", synMember.getEmail().trim());
                                isUpdate = true;
                            }
                            // 修改办公电话
                            if(synMember.getOfficeNum() != null && !synMember.getOfficeNum().trim().equals(member.getOfficeNum())) {
                                updateInfo += "办公电话改为:" + synMember.getOfficeNum().trim() + " ";
                                member.setProperty("officenumber", synMember.getOfficeNum().trim());
                                isUpdate = true;
                            }
                            // 修改手机
                            if(synMember.getTelNumber() != null && !synMember.getTelNumber().trim().equals(member.getTelNumber())) {
                                updateInfo += "手机改为:" + synMember.getTelNumber().trim() + " ";
                                member.setProperty("telnumber", synMember.getTelNumber().trim());
                                isUpdate = true;
                            }
                            // 修改住址
                            if(synMember.getAddress() != null && !synMember.getAddress().trim().equals(member.getAddress())) {
                                updateInfo += "住址改为:" + synMember.getAddress().trim() + " ";
                                member.setProperty("address", synMember.getAddress().trim());
                                isUpdate = true;
                            }
                            // 修改邮编
                            if(synMember.getPostalCode() != null && !synMember.getPostalCode().trim().equals(member.getPostalcode())) {
                                updateInfo += "邮编改为:" + synMember.getPostalCode().trim() + " ";
                                member.setProperty("postalcode", synMember.getPostalCode().trim());
                                isUpdate = true;
                            }
                            // 修改网站
                            if(synMember.getWebsite() != null && !synMember.getWebsite().trim().equals(member.getWebsite())) {
                                updateInfo += "网站改为:" + synMember.getWebsite().trim() + " ";
                                member.setProperty("website", synMember.getWebsite().trim());
                                isUpdate = true;
                            }
                            // 修改博客
                            if(synMember.getBlog() != null && !synMember.getBlog().trim().equals(member.getBlog())) {
                                updateInfo += "博客改为:" + synMember.getBlog().trim() + " ";
                                member.setProperty("blog", synMember.getBlog().trim());
                                isUpdate = true;
                            }
                            // 修改性别
                            if(synMember.getGender() != null && synMember.getGender().intValue() != member.getGender().intValue()) {
                                updateInfo += "性别改为:" + synMember.getGender() + " ";
                                member.setProperty("gender", synMember.getGender());
                                isUpdate = true;
                            }
                            // 修改生日
                            if(synMember.getBirthday() != null && !synMember.getBirthday().equals(member.getBirthday())) {
                                updateInfo += "生日改为:" + synMember.getBirthday() + " ";
                                member.setProperty("birthday", synMember.getBirthday());
                                isUpdate = true;
                            }
                            // 修改微博
                            if(synMember.getWeibo() != null && !synMember.getWeibo().trim().equals(member.getWeibo())) {
                                updateInfo += "微博改为:" + synMember.getWeibo().trim() + " ";
                                member.setProperty("weibo", synMember.getWeibo().trim());
                                isUpdate = true;
                            }
                            // 修改微信
                            if(synMember.getWeixin() != null && !synMember.getWeixin().trim().equals(member.getWeixin())) {
                                updateInfo += "微信改为:" + synMember.getWeixin().trim() + " ";
                                member.setProperty("weixin", synMember.getWeixin().trim());
                                isUpdate = true;
                            }
                            // 修改身份证号
                            if(synMember.getIdNum() != null && !synMember.getIdNum().trim().equals(member.getIdNum())) {
                                updateInfo += "身份证号改为:" + synMember.getIdNum().trim() + " ";
                                member.setProperty("idnum", synMember.getIdNum().trim());
                                isUpdate = true;
                            }
                            // 修改学位
                            if(synMember.getDegree() != null && !synMember.getDegree().trim().equals(member.getDegree())) {
                                updateInfo += "学位改为:" + synMember.getDegree().trim() + " ";
                                member.setProperty("degree", synMember.getDegree().trim());
                                isUpdate = true;
                            }
                            // 修改通信地址
                            if(synMember.getPostAddress() != null && !synMember.getPostAddress().trim().equals(member.getPostAddress())) {
                                updateInfo += "通信地址改为:" + synMember.getPostAddress().trim() + " ";
                                member.setProperty("postAddress", synMember.getPostAddress().trim());
                                isUpdate = true;
                            }
                        }
                        // 更新人员信息
                        if(isUpdate) {
                            OrganizationMessage mes = orgManagerDirect.updateMember(member);
                            List<OrgMessage> errorMsgList = mes.getErrorMsgs();
                            if(errorMsgList.size() > 0) {
                                synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
                                synLog.setSynLog(ErrorMessageUtil.getErrorMessageString(errorMsgList));
                                synMember.setSyncState(SynOrgConstants.SYN_STATE_FAILURE);
                            } else {
                                synLog.setSynState(SynOrgConstants.SYN_STATE_SUCCESS);
                                synLog.setSynLog(updateInfo);
                                synMember.setSyncState(SynOrgConstants.SYN_STATE_SUCCESS);
                            }
                            logList.add(synLog);
                        } else {
                            // 无更新则不记录日志
                            synLog = null;
                            synMember.setSyncState(SynOrgConstants.SYN_STATE_SUCCESS);
                        }
                        // 人员不存在：做插入操作
                        resMember.add(synMember);
                    } else {
                        synLog.setSynType(SynOrgConstants.SYN_OPERATION_TYPE_CREATE);
                        member = new V3xOrgMember();
                        member.setCreateTime(new Date());
                        member.setUpdateTime(new Date());
                        member.setIdIfNew();
                        member.setIsAdmin(false);
                        member.setIsAssigned(true);
                        member.setIsDeleted(false);
                        member.setIsInternal(true);
                        member.setIsLoginable(true);
                        member.setIsVirtual(false);
                        // 默认为正式
                        member.setType(1);
                        // 默认为在职
                        member.setState(1);
                        member.setStatus(1);
                        
                        // 是否同步人员密码(不同步则人员密码默认为123456)
                        if(SynOrgTask.isSynPassword()) {
                            /** 设置登录名,及密码 **/
                            if(synMember.getPassword() != null && !"".equals(synMember.getPassword())) {
                                V3xOrgPrincipal principal = new V3xOrgPrincipal(member.getId(), synMember.getLoginName(), synMember.getPassword().trim());
                                member.setV3xOrgPrincipal(principal);
                            } else {
                                V3xOrgPrincipal principal = null;
                                // 有默认密码则统一设置默认密码
                                if(StringUtils.isNotBlank(SynOrgTask.getDefaultPassword())) {
                                    principal = new V3xOrgPrincipal(member.getId(), synMember.getLoginName(), SynOrgTask.getDefaultPassword().trim());
                                }
                                // 无默认密码则密码预置为登录名
                                else {
                                    principal = new V3xOrgPrincipal(member.getId(), synMember.getLoginName(), synMember.getLoginName().trim());
                                }
                                member.setV3xOrgPrincipal(principal);
                            }
                        } else {
                            V3xOrgPrincipal principal = null;
                            // 有默认密码则统一设置默认密码
                            if(SynOrgTask.getDefaultPassword() != null && !"".equals(SynOrgTask.getDefaultPassword())) {
                                principal = new V3xOrgPrincipal(member.getId(), synMember.getLoginName(), SynOrgTask.getDefaultPassword().trim());
                            }
                            // 无默认密码则密码预置为登录名
                            else {
                                principal = new V3xOrgPrincipal(member.getId(), synMember.getLoginName(), synMember.getLoginName().trim());
                            }
                            member.setV3xOrgPrincipal(principal);
                        }
                        member.setCode(synMember.getCode());
                        member.setName(synMember.getName().trim());
                        member.setEnabled(synMember.getEnable());
                        
                        member.setSortId(synMember.getSortId());
//                        Pattern p = Pattern.compile("[0-9]");
//                        Matcher m = p.matcher(synMember.getCode());
//                        if(m.find()){
//                        	member.setSortId(Long.parseLong(synMember.getCode().replaceAll("[A-Za-z]", "")));
//                        }else{
//                        	member.setSortId(null);
//                        }
                        
                        /** 所在部门 **/
                        V3xOrgDepartment orgDept = (V3xOrgDepartment)syncOrgManager.getEntityByProperty(V3xOrgDepartment.class.getSimpleName(), "code", synMember.getDepartmentCode(), SynOrgConstants.ORG_SYNC_IS_GROUP?null:SynOrgConstants.DEFAULT_ACCOUNT_ID);
                        if(orgDept != null && orgDept.getId() != -1) {
                            member.setOrgDepartmentId(orgDept.getId());
                            member.setOrgAccountId(orgDept.getOrgAccountId());
                        } else {
                            synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
                            synLog.setSynLog("部门编码(" + synMember.getDepartmentCode() + ")不存在");
                            logList.add(synLog);
                            synMember.setSyncState(SynOrgConstants.SYN_STATE_FAILURE);
                            continue;
                        }
                        
                        member.setOrgPostId(addOrGetPostByName(null==synMember.getPostCode()?null:synMember.getPostCode().trim(),member).getId());
                        member.setOrgLevelId(addOrGetLevelByName(null==synMember.getLevelCode()?null:synMember.getLevelCode().trim(),member).getId());
                        
                        member.setDescription(synMember.getDescription() != null ? synMember.getDescription().trim() : "");
                        member.setProperty("emailaddress", synMember.getEmail() != null ? synMember.getEmail().trim() : "");
                        member.setProperty("officenumber", synMember.getOfficeNum() != null ? synMember.getOfficeNum().trim() : "");;
                        member.setProperty("telnumber", synMember.getTelNumber() != null ? synMember.getTelNumber().trim() : "");
                        member.setProperty("address", synMember.getAddress() != null ? synMember.getAddress().trim() : "");
                        member.setProperty("postalcode", synMember.getPostalCode() != null ? synMember.getPostalCode().trim() : "");;
                        member.setProperty("website", synMember.getWebsite() != null ? synMember.getWebsite().trim() : "");
                        member.setProperty("blog", synMember.getBlog() != null ? synMember.getBlog().trim() : "");
                        member.setProperty("gender", synMember.getGender() != null ? synMember.getGender() : 1);
                        if(synMember.getBirthday() != null) {
                            member.setProperty("birthday", synMember.getBirthday());
                        }
                        member.setProperty("weibo", synMember.getWeibo() != null ? synMember.getWeibo().trim() : "");
                        member.setProperty("weixin", synMember.getWeixin() != null ? synMember.getWeixin().trim() : "");
                        member.setProperty("idnum", synMember.getIdNum() != null ? synMember.getIdNum().trim() : "");
                        member.setProperty("degree", synMember.getDegree() != null ? synMember.getDegree().trim() : "");
                        member.setProperty("postAddress", synMember.getPostAddress() != null ? synMember.getPostAddress().trim() : "");
                        OrganizationMessage res=orgManagerDirect.addMember(member);
                        if(res.isSuccess()){
                        	synLog.setSynState(SynOrgConstants.SYN_STATE_SUCCESS);
                            synLog.setSynLog("新增人员：" + member.getName() + "[" + member.getCode() + "]");
                            synMember.setSyncState(SynOrgConstants.SYN_STATE_SUCCESS);
                            resMember.add(synMember);
                        }else{
                        	synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
                            synLog.setSynLog("新增人员：" + member.getName() + "[" + member.getCode() + "]异常："+res.getErrorMsgs().get(0).getCode().toString());
                            logList.add(synLog);
                            synMember.setSyncState(SynOrgConstants.SYN_STATE_FAILURE);
                            resMember.add(synMember);
                            continue;
                        }
                        
                    }
                } catch(Exception e) {
                    synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
                    synLog.setSynLog(e.getMessage());
                    logList.add(synLog);
                    synMember.setSyncState(SynOrgConstants.SYN_STATE_FAILURE);
                    continue;
                }
                if(synLog != null) {
                    logList.add(synLog);
                }
            }
            // 创建同步日志
            if(logList.size() > 0) {
                syncLogManager.createAll(logList);
                System.out.println("人员同步完成,共同步:"+ logList.size()+"条数据!!");
                log.info("人员同步完成,共同步:"+ logList.size()+"条数据!!");
            }
            // 更新同步信息
            syncMemberDao.updateAll(resMember);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void create(List<SynMember> memberList) {
    	try{
    		syncMemberDao.createAll(memberList);
    	}catch (Exception e) {
			log.error("大渡河MQ导入数据异常！",e);
			System.out.println("大渡河MQ导入数据异常！");
		}
        
    }
    

	@Override
	public void create(SynMember member) {
    	try{
    		SynMember findMember=syncMemberDao.findPersonByAccount(member.getLoginName());
    		List<SynMember> upList=new ArrayList<SynMember>();
    		if(null==findMember){
				upList.add(member);
				syncMemberDao.createAll(upList);
			}else{
				System.out.println("-------------存在同名---"+member.getLoginName());
				log.info("-------------存在同名---"+member.getLoginName());
				syncMemberDao.delete(findMember);
				upList.add(member);
				syncMemberDao.createAll(upList);
			}
    	}catch (Exception e) {
			log.error("大渡河MQ导入数据异常！",e);
			System.out.println("大渡河MQ导入数据异常！");
		}
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete() {
        syncMemberDao.deleteAll();
    }

    /**
     * 设置orgManagerDirect
     * @param orgManagerDirect orgManagerDirect
     */
    public void setOrgManagerDirect(OrgManagerDirect orgManagerDirect) {
        this.orgManagerDirect = orgManagerDirect;
    }

    /**
     * 设置syncOrgManager
     * @param syncOrgManager syncOrgManager
     */
    public void setSyncOrgManager(SyncOrgManager syncOrgManager) {
        this.syncOrgManager = syncOrgManager;
    }

    /**
     * 设置memberDao
     * @param memberDao memberDao
     */
    public void setSyncMemberDao(SyncMemberDao syncMemberDao) {
        this.syncMemberDao = syncMemberDao;
    }

    /**
     * 判断并添加岗位(如果传入人员岗位信息为空，则判断是否有普通人员岗位，没有创建，有返回)
     * @throws Exception
     */
    private V3xOrgPost addOrGetPostByName(String postName,V3xOrgMember member) throws Exception {
    	if(null==postName||"".equals(postName)){
    		V3xOrgPost postOA = (V3xOrgPost)syncOrgManager.getEntityByProperty(V3xOrgPost.class.getSimpleName(), "name", SynOrgConstants.ORG_SYNC_CREATE_POST_NAME, null);
    		if(postOA == null){
                postOA = new V3xOrgPost();
                postOA.setName(SynOrgConstants.ORG_SYNC_CREATE_POST_NAME);
                postOA.setTypeId(1L);
                postOA.setEnabled(true);
                postOA.setIdIfNew();
                postOA.setSortId(null);
                postOA.setCreateTime(new Date());
                postOA.setUpdateTime(new Date());
                postOA.setDescription("");
                postOA.setOrgAccountId(member.getOrgAccountId()!=null?member.getOrgAccountId():SynOrgConstants.DEFAULT_ACCOUNT_ID);
                orgManagerDirect.addPost(postOA);
                return postOA;
    		}else{
    			return postOA;
    		}
    	}
        V3xOrgPost postOA = (V3xOrgPost)syncOrgManager.getEntityByProperty(V3xOrgPost.class.getSimpleName(), "name", postName, null);
        if(postOA == null) {
            postOA = new V3xOrgPost();
            postOA.setName(postName);
            postOA.setTypeId(1L);
            postOA.setEnabled(true);
            postOA.setIdIfNew();
            postOA.setSortId(null);
            postOA.setCreateTime(new Date());
            postOA.setUpdateTime(new Date());
            postOA.setDescription("");
            postOA.setOrgAccountId(member.getOrgAccountId()!=null?member.getOrgAccountId():SynOrgConstants.DEFAULT_ACCOUNT_ID);
            orgManagerDirect.addPost(postOA);
        }
        return postOA;
    }

    private static String getLevelByCode(String code) {
        String a = "";
        for(String[] s : otype) {
            if(s[0].equals(code)) {
                a = s[1];
            }
        }
        return a;
    }

    /**
     * 判断并添加职务
     * @throws Exception
     */
    private V3xOrgLevel addOrGetLevelByName(String levelCode,V3xOrgMember member) throws Exception {
    	if(null==levelCode||"".equals(levelCode)){
    		V3xOrgLevel levelOA = (V3xOrgLevel)syncOrgManager.getEntityByProperty(V3xOrgLevel.class.getSimpleName(), "name", SynOrgConstants.ORG_SYNC_CREATE_LEVEL_NAME, null);
    		if(levelOA == null){
                levelOA = new V3xOrgLevel();
                levelOA.setCode(SynOrgConstants.ORG_SYNC_CREATE_LEVEL_CODE);
                levelOA.setName(SynOrgConstants.ORG_SYNC_CREATE_LEVEL_NAME);
                levelOA.setEnabled(true);
                levelOA.setIdIfNew();
                levelOA.setCreateTime(new Date());
                levelOA.setUpdateTime(new Date());
                levelOA.setDescription("");
                levelOA.setOrgAccountId(member.getOrgAccountId()!=null?member.getOrgAccountId():SynOrgConstants.DEFAULT_ACCOUNT_ID);
                levelOA.setLevelId(3);
                orgManagerDirect.addLevel(levelOA);
                return levelOA;
    		}else{
    			return levelOA;
    		}
    	}
        V3xOrgLevel levelOA = (V3xOrgLevel)syncOrgManager.getEntityByProperty(V3xOrgLevel.class.getSimpleName(), "code", levelCode, null);
        if(levelOA == null) {
            levelOA = new V3xOrgLevel();
            levelOA.setCode(levelCode);
            levelOA.setName(getLevelByCode(levelCode));
            levelOA.setEnabled(true);
            levelOA.setIdIfNew();
            levelOA.setSortId(1L);
            levelOA.setCreateTime(new Date());
            levelOA.setUpdateTime(new Date());
            levelOA.setDescription("");
            levelOA.setOrgAccountId(member.getOrgAccountId()!=null?member.getOrgAccountId():SynOrgConstants.DEFAULT_ACCOUNT_ID);
            levelOA.setLevelId(3);
            orgManagerDirect.addLevel(levelOA);
        }
        return levelOA;
    }

    /**
     * 设置syncLogManager
     * @param syncLogManager syncLogManager
     */
    public void setSyncLogManager(SyncLogManager syncLogManager) {
        this.syncLogManager = syncLogManager;
    }
}
