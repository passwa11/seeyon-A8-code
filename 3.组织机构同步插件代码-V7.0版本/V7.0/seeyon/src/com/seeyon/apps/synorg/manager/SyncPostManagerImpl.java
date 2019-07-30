package com.seeyon.apps.synorg.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.synorg.constants.SynOrgConstants;
import com.seeyon.apps.synorg.dao.SyncOrgDao;
import com.seeyon.apps.synorg.dao.SyncPostDao;
import com.seeyon.apps.synorg.po.SynLog;
import com.seeyon.apps.synorg.po.SynPost;
import com.seeyon.apps.synorg.po.SynUnit;
import com.seeyon.apps.synorg.util.ErrorMessageUtil;
import com.seeyon.ctp.organization.bo.OrganizationMessage;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.OrganizationMessage.OrgMessage;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgPost;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.organization.manager.OrgManagerDirect;

/**
 * @author Yang.Yinghai
 * @date 2015-8-18下午5:15:47
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class SyncPostManagerImpl implements SyncPostManager {
	private static final Log log = LogFactory.getLog(SyncPostManagerImpl.class);
    /** 组织机构管理器 */
    private OrgManagerDirect orgManagerDirect;

    /** 组织机构管理器 */
    private SyncOrgManager syncOrgManager;

    /** 岗位实体查询接口 */
    private SyncPostDao syncPostDao;

    /** 同步日志管理器 */
    private SyncLogManager syncLogManager;
    
    /**组织机构同步DAO*/
    private SyncOrgDao syncOrgDao;

    /**
     * {@inheritDoc}
     */
    public void synAllPost() {
    	List<SynPost> resPost = new ArrayList<SynPost>();
        List<SynPost> allPosts = syncPostDao.findAll();
        log.info("岗位同步开始!!");
        if(allPosts != null && allPosts.size() > 0) {
            List<SynLog> logList = new ArrayList<SynLog>();
            for(int i = 0; i < allPosts.size(); i++) {
                SynPost synPost = allPosts.get(i);
                synPost.setSyncDate(new Date());
                SynLog synLog = new SynLog(SynOrgConstants.ORG_ENTITY_POST, synPost.getCode(), synPost.getName());
                V3xOrgPost post = (V3xOrgPost)syncOrgManager.getEntityByProperty(V3xOrgPost.class.getSimpleName(), "code", synPost.getCode(), null);
                try {
                    if(post != null) {
                        boolean isUpdate = false;
                        String updateInfo = "";
                        synLog.setEntityName(post.getName());
                        synLog.setSynType(SynOrgConstants.SYN_OPERATION_TYPE_UPDATE);
                        // 修改岗位名
                        if(!post.getName().equals(synPost.getName().trim())) {
                            updateInfo += "名称改为:" + synPost.getName().trim() + " ";
                            post.setName(synPost.getName().trim());
                            isUpdate = true;
                        }
                        // 修改岗位排序号
                        if(post.getSortId() != null && synPost.getSortId() != null && post.getSortId().longValue() != synPost.getSortId().longValue()) {
                            updateInfo += "排序号改为:" + synPost.getSortId() + " ";
                            post.setSortId(synPost.getSortId());
                            isUpdate = true;
                        }
                        // 修改岗位描述
                        if(synPost.getDescription() != null && !"".equals(synPost.getDescription().trim())) {
                            if(post.getDescription() == null || !synPost.getDescription().trim().equals(post.getDescription().trim())) {
                                updateInfo += "描述改为:" + synPost.getDescription().trim() + " ";
                                post.setDescription(synPost.getDescription().trim());
                                isUpdate = true;
                            }
                        } else {
                            if(post.getDescription() != null && !"".equals(post.getDescription().trim())) {
                                updateInfo += "描述改为: 空字符串 ";
                                post.setDescription("");
                                isUpdate = true;
                            }
                        }
                        if(isUpdate) {
                            OrganizationMessage mes = orgManagerDirect.updatePost(post);
                            List<OrgMessage> errorMsgList = mes.getErrorMsgs();
                            if(errorMsgList.size() > 0) {
                                synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
                                synLog.setSynLog(ErrorMessageUtil.getErrorMessageString(errorMsgList));
                                synPost.setSyncState(SynOrgConstants.SYN_STATE_FAILURE);
                            } else {
                                synLog.setSynState(SynOrgConstants.SYN_STATE_SUCCESS);
                                synLog.setSynLog(updateInfo);
                                synPost.setSyncState(SynOrgConstants.SYN_STATE_SUCCESS);
                            }
                            resPost.add(synPost);
                        } else {
                            synPost.setSyncState(SynOrgConstants.SYN_STATE_SUCCESS);
                            synLog = null;
                        }
                    } else {
                        /** 添加岗位 **/
                        synLog.setSynType(SynOrgConstants.SYN_OPERATION_TYPE_CREATE);
                        post = new V3xOrgPost();
                        post.setIdIfNew();
                        // 默认为 管理类
                        post.setTypeId(1L);
                        post.setEnabled(true);
                        
                        /**大渡河岗位编码规则：orgcode@人员账号,集团版需要单位ID，通过orgcode 获取单位ID***/
                        if(synPost.getCode().indexOf("@")>-1){
                        	String orgcode=synPost.getCode().split("@")[0];
                        	V3xOrgEntity org = syncOrgManager.getEntityByProperty(V3xOrgDepartment.class.getSimpleName(), "code", orgcode, null);
                        	
                        	if(null==org){
                        		org = syncOrgManager.getEntityByProperty(V3xOrgAccount.class.getSimpleName(), "code", orgcode, null);
                        	}
                        	
                        	if(null==org){
                        		synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
                                synLog.setSynLog(" 新增岗位：" + post.getName() + "[" + post.getCode() + "]:所属部门信息为空");
                                logList.add(synLog);
                                synPost.setSyncState(SynOrgConstants.SYN_STATE_FAILURE);
                                resPost.add(synPost);
                                continue;
                        	}
                        	
                        	post.setOrgAccountId(org.getOrgAccountId());
                        }else{
                        	post.setOrgAccountId(SynOrgConstants.DEFAULT_ACCOUNT_ID);
                        }
                        /*****/
                        
                        
                        post.setName(synPost.getName().trim());
                        post.setCode(synPost.getCode());
                        post.setSortId(synPost.getSortId() != null ? synPost.getSortId() : 1L);
                        post.setDescription(synPost.getDescription() != null ? synPost.getDescription().trim() : "");
                        post.setUpdateTime(new Date());
                        post.setCreateTime(new Date());
                        post.setStatus(1);
                        OrganizationMessage res=orgManagerDirect.addPost(post);
                        if(res.isSuccess()){
                        	synLog.setSynState(SynOrgConstants.SYN_STATE_SUCCESS);
                            synLog.setSynLog(" 新增岗位：" + post.getName() + "[" + post.getCode() + "]");
                            synPost.setSyncState(SynOrgConstants.SYN_STATE_SUCCESS);
                            resPost.add(synPost);
                        }else{
                        	synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
                            synLog.setSynLog(" 新增岗位：" + post.getName() + "[" + post.getCode() + "]异常："+res.getErrorMsgs().get(0).getCode().toString());
                            synPost.setSyncState(SynOrgConstants.SYN_STATE_FAILURE);
                            resPost.add(synPost);
                        }
                        
                    }
                } catch(Exception e) {
                    synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
                    synLog.setSynLog(e.getMessage());
                    logList.add(synLog);
                    synPost.setSyncState(SynOrgConstants.SYN_STATE_FAILURE);
                    continue;
                }
                if(synLog != null) {
                    logList.add(synLog);
                }
            }
            // 创建同步日志
            if(logList.size() > 0) {
                syncLogManager.createAll(logList);
            }
            // 更新同步信息
            syncPostDao.updateAll(resPost);
            log.info("岗位同步完成!!"+logList.size());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void create(List<SynPost> postList) {
        syncPostDao.createAll(postList);
    }
    
	@Override
	public void create(SynPost post) {
		try{
			SynPost findpost=syncPostDao.findPostByCode(post.getCode());
			if(null==findpost){
				syncPostDao.create(post);
			}else{
				System.out.println("-------------存在同名---"+findpost.getCode());
				log.info("-------------存在同名---"+findpost.getCode());
				syncPostDao.delete(findpost);
				syncPostDao.create(post);
			}
		}catch (Exception e) {
			log.error("大渡河MQ导入岗位数据异常！",e);
			System.out.println("大渡河MQ导入岗位数据异常！");
		}
		
		
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete() {
        syncPostDao.deleteAll();
    }

    /**
     * 设置orgManagerDirect
     * @param orgManagerDirect orgManagerDirect
     */
    public void setOrgManagerDirect(OrgManagerDirect orgManagerDirect) {
        this.orgManagerDirect = orgManagerDirect;
    }

    /**
     * 设置syncPostDao
     * @param syncPostDao syncPostDao
     */
    public void setSyncPostDao(SyncPostDao syncPostDao) {
        this.syncPostDao = syncPostDao;
    }

    /**
     * 设置syncOrgManager
     * @param syncOrgManager syncOrgManager
     */
    public void setSyncOrgManager(SyncOrgManager syncOrgManager) {
        this.syncOrgManager = syncOrgManager;
    }

    /**
     * 设置syncLogManager
     * @param syncLogManager syncLogManager
     */
    public void setSyncLogManager(SyncLogManager syncLogManager) {
        this.syncLogManager = syncLogManager;
    }
    /**
     * 设置syncOrgDao
     * @param syncOrgDao syncOrgDao
     */
    public void setSyncOrgDao(SyncOrgDao syncOrgDao) {
        this.syncOrgDao = syncOrgDao;
    }

}
