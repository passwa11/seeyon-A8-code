package com.seeyon.apps.synorg.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.synorg.constants.SynOrgConstants;
import com.seeyon.apps.synorg.dao.SyncDepartmentDao;
import com.seeyon.apps.synorg.dao.SyncUnitDao;
import com.seeyon.apps.synorg.po.SynDepartment;
import com.seeyon.apps.synorg.po.SynLog;
import com.seeyon.apps.synorg.po.SynUnit;
import com.seeyon.apps.synorg.scheduletask.SynOrgTask;
import com.seeyon.apps.synorg.util.ErrorMessageUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.OrganizationMessage;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.OrganizationMessage.OrgMessage;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.manager.OrgManagerDirect;

/**
 * @author Yang.Yinghai
 * @date 2015-8-18下午10:10:50
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class SyncDepartmentManagerImpl implements SyncDepartmentManager {
	private static final Log log = LogFactory.getLog(SyncDepartmentManagerImpl.class);
    /** 组织机构管理器 */
    private OrgManagerDirect orgManagerDirect;

    /** 组织机构管理器 */
    private OrgManager orgManager;

    /** 组织机构管理器 */
    private SyncOrgManager syncOrgManager;

    /** 部门实体查询接口 */
    private SyncDepartmentDao syncDepartmentDao;
    
	/** 中间表单位DAO */
	private SyncUnitDao syncUnitDao;

    /** 同步日志管理器 */
    private SyncLogManager syncLogManager;

    /**
     * {@inheritDoc}
     */
    @Override
    public void synAllDepartment() {
        List<SynDepartment> allDepartments = syncDepartmentDao.findAll();
        System.out.println("部门同步开始!,");
        if(allDepartments != null && allDepartments.size() > 0) {
            List<SynLog> logList = new ArrayList<SynLog>();
            for(int i = 0; i < allDepartments.size(); i++) {
                SynDepartment synDepartment = allDepartments.get(i);
                // 设置同步时间
                synDepartment.setSyncDate(new Date());
                SynLog synLog = new SynLog(SynOrgConstants.ORG_ENTITY_DEPARTMENT, synDepartment.getCode(), synDepartment.getName());
                V3xOrgDepartment department = (V3xOrgDepartment)syncOrgManager.getEntityByProperty(V3xOrgDepartment.class.getSimpleName(), "code", synDepartment.getCode(), SynOrgConstants.ORG_SYNC_IS_GROUP?null:SynOrgConstants.DEFAULT_ACCOUNT_ID);
                try {
                    if(department != null) {
                        boolean isUpdate = false;
                        String updateInfo = "";
                        synLog.setEntityName(department.getName());
                        synLog.setSynType(SynOrgConstants.SYN_OPERATION_TYPE_UPDATE);
                        
                        // 修改人员启用/停用状态
                        if(!department.getEnabled()) {
                            updateInfo += "部门启/停用状态改为:启用 ";
                            department.setEnabled(true);
                            isUpdate = true;
                        }
                        
                        // 修改部门名称
                        if(!department.getName().equals(synDepartment.getName().trim())) {
                            updateInfo += "名称改为:" + synDepartment.getName().trim() + " ";
                            department.setName(synDepartment.getName().trim());
                            isUpdate = true;
                        }
                        // 修改上级部门
                        if(synDepartment.getCode().equals(synDepartment.getParentCode())) {
                            synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
                            synLog.setSynLog("上级部门不能是当前部门 ");
                            logList.add(synLog);
                            synDepartment.setSyncState(SynOrgConstants.SYN_STATE_FAILURE);
                            continue;
                        }
                        V3xOrgDepartment oldDeptParent = orgManager.getParentDepartment(department.getId());
                        V3xOrgDepartment newDeptParent = (V3xOrgDepartment)syncOrgManager.getEntityByProperty(V3xOrgDepartment.class.getSimpleName(), "code", synDepartment.getParentCode(), SynOrgConstants.ORG_SYNC_IS_GROUP?department.getOrgAccountId():SynOrgConstants.DEFAULT_ACCOUNT_ID);
                        if(newDeptParent != null && newDeptParent.getId() != -1) {
                            if(oldDeptParent != null && oldDeptParent.getId() != -1) {
                                if(newDeptParent.getId().longValue() != oldDeptParent.getId().longValue()) {
                                    updateInfo += "上级部门改为:" + newDeptParent.getName() + " ";
                                    department.setSuperior(newDeptParent.getId());
                                    isUpdate = true;
                                }
                            } else {
                                updateInfo += "上级部门改为:" + newDeptParent.getName() + " ";
                                department.setSuperior(newDeptParent.getId());
                                isUpdate = true;
                            }
                        } else {
                            if(StringUtils.isBlank(synDepartment.getParentCode()) || SynOrgTask.getRootDeptCode().equals(synDepartment.getParentCode())) {
                                if(oldDeptParent != null && !"00000001".equals(oldDeptParent.getPath())) {
                                    updateInfo += "上级部门改为：根目录 ";
                                    department.setSuperior(SynOrgConstants.DEFAULT_ACCOUNT_ID);
                                    isUpdate = true;
                                }
                            } else {
                                synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
                                synLog.setSynLog("上级部门[" + synDepartment.getParentCode() + "]不存在");
                                logList.add(synLog);
                                synDepartment.setSyncState(SynOrgConstants.SYN_STATE_FAILURE);
                                continue;
                            }
                        }
                        /*
                        // 修改部门排序号
                        if(synDepartment.getSortId() != null) {
                            if(department.getSortId() == null || department.getSortId().longValue() != synDepartment.getSortId().longValue()) {
                                updateInfo += "排序号改为:" + synDepartment.getSortId() + " ";
                                department.setSortId(synDepartment.getSortId());
                                isUpdate = true;
                            }
                        }*/
                        // 更新部门
                        if(isUpdate) {
                            OrganizationMessage mes = orgManagerDirect.updateDepartment(department);
                            List<OrgMessage> errorMsgList = mes.getErrorMsgs();
                            if(errorMsgList.size() > 0) {
                                synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
                                synLog.setSynLog(ErrorMessageUtil.getErrorMessageString(errorMsgList));
                                synDepartment.setSyncState(SynOrgConstants.SYN_STATE_FAILURE);
                            } else {
                                synLog.setSynState(SynOrgConstants.SYN_STATE_SUCCESS);
                                synLog.setSynLog(updateInfo);
                                synDepartment.setSyncState(SynOrgConstants.SYN_STATE_SUCCESS);
                            }
                            logList.add(synLog);
                        } else {
                            synDepartment.setSyncState(SynOrgConstants.SYN_STATE_SUCCESS);
                            synLog = null;
                        }
                    } else {
                        synLog.setSynType(SynOrgConstants.SYN_OPERATION_TYPE_CREATE);
                        department = new V3xOrgDepartment();
                        department.setType(OrgConstants.UnitType.Department);
                        department.setOrgAccountId(SynOrgConstants.DEFAULT_ACCOUNT_ID);
                        // 部门名称
                        department.setName(synDepartment.getName().trim());
                        // 部门编码
                        department.setCode(synDepartment.getCode().trim());
                        // 设置上级部门
                        if(synDepartment.getCode().equals(synDepartment.getParentCode())) {
                            synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
                            synLog.setSynLog("上级部门不能是当前部门 ");
                            logList.add(synLog);
                            continue;
                        }
                        if(StringUtils.isBlank(synDepartment.getParentCode()) || SynOrgTask.getRootDeptCode().equals(synDepartment.getParentCode())) {
                            department.setSuperior(SynOrgConstants.DEFAULT_ACCOUNT_ID);
                        } else {
                            /** 获取父级部门编号 **/
                            V3xOrgDepartment orgDept = (V3xOrgDepartment)syncOrgManager.getEntityByProperty(V3xOrgDepartment.class.getSimpleName(), "code", synDepartment.getParentCode(), SynOrgConstants.DEFAULT_ACCOUNT_ID);
                            if(orgDept != null && orgDept.getId() != -1) {
                                department.setSuperior(orgDept.getId());
                            } else {
                                synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
                                synLog.setSynLog("上级部门[" + synDepartment.getParentCode() + "]不存在");
                                logList.add(synLog);
                                continue;
                            }
                        }
                        department.setEnabled(true);
                        department.setSortId(synDepartment.getSortId() != null ? synDepartment.getSortId() : 1L);
                        //department.setDescription(synDepartment.getDescription() != null ? synDepartment.getDescription().trim() : "");
                        // 更新时间
                        department.setUpdateTime(new Date());
                        // 创建时间
                        department.setCreateTime(new Date());
                        department.setIsInternal(true);
                        department.setLevelScope(-1);
                        department.setSortIdType("1");
                        department.setStatus(1);
                        orgManagerDirect.addDepartment(department);
                        synDepartment.setSyncState(SynOrgConstants.SYN_STATE_SUCCESS);
                        synLog.setSynState(SynOrgConstants.SYN_STATE_SUCCESS);
                        synLog.setSynLog("新增部门：" + department.getName() + "[" + department.getCode() + "]");
                    }
                } catch(Exception e) {
                    synDepartment.setSyncState(SynOrgConstants.SYN_STATE_FAILURE);
                    synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
                    synLog.setSynLog(e.getMessage());
                    logList.add(synLog);
                    continue;
                }
                if(synLog != null) {
                    logList.add(synLog);
                }
            }
            // 创建同步日志
            if(logList.size() > 0) {
                syncLogManager.createAll(logList);
                System.out.println("部门同步完成,共同步:"+ logList.size()+"条数据!!");
            }
            // 更新同步信息
            syncDepartmentDao.updateAll(allDepartments);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void create(List<SynDepartment> deptList) {
        syncDepartmentDao.createAll(deptList);
    }
    
	@Override
	public void create(SynDepartment dept) {
    	try{
    		SynDepartment findDep=syncDepartmentDao.findDepByCode(dept.getCode());
    		List<SynDepartment> upList=new ArrayList<SynDepartment>();
    		if(null==findDep){
				upList.add(dept);
				syncDepartmentDao.createAll(upList);
			}else{
				syncDepartmentDao.delete(findDep);
				syncDepartmentDao.create(dept);
			}
    	}catch (Exception e) {
			log.error("大渡河MQ导入数据异常！",e);
			System.out.println("大渡河MQ导入数据异常！");
		}
	}
	

	@Override
	public void synAllUnitsDepartment() {
    	//查询所有中间库单位
    	List<SynUnit> allunit = new ArrayList<SynUnit>();
    	allunit = getSyncUnitDao().findAllSynUnit();
    	
    	//查询所有中间库部门
    	List<SynDepartment> allDepartments = syncDepartmentDao.findAll();
    	System.out.println("部门同步开始!");
    	log.info("部门同步开始!");
    	
    	for(SynUnit synunit :allunit){
    		//单位下的所有部门
    		List<SynDepartment> accountChilddepts = findAllChilds(synunit.getCode(), allDepartments);
    		
    		if(accountChilddepts != null && accountChilddepts.size() > 0) {
    			//部门信息从中间库同步到OA
    			updateDepartment(accountChilddepts,Long.valueOf(synunit.getOa_Id()));

    		}
    	}
	}
	
    // 查找子部门
    private List<SynDepartment> findAllChilds(String parentCode, List<SynDepartment> allDept) {
    	List<SynDepartment> list = new ArrayList<SynDepartment>();
        for(SynDepartment dept : allDept) {
            if(dept.getParentCode().equals(parentCode)) {
                list.add(dept);
                list.addAll(findAllChilds(dept.getCode(), allDept));
            }
        }
        return list;
    }
    
    /**
     * 将部门列表同步到指定单位下
     * @param synDepartment
     * @return
     */
    private void updateDepartment(List<SynDepartment> accountChilddepts,long accountId){
    	List<SynDepartment> resDep = new ArrayList<SynDepartment>();
    	V3xOrgAccount account = null;
		try {
			account = orgManager.getAccountById(accountId);
		} catch (Exception e1) {
			log.error("单位查询失败！",e1);
			return ;
		}
		if(account==null){
			log.error("----单位不存在！");
			return ;
		}

		List<SynLog> logList = new ArrayList<SynLog>(); 

		for( SynDepartment synDepartment : accountChilddepts) {
			// 设置同步时间
	    	 synDepartment.setSyncDate(new Date());
	         SynLog synLog = new SynLog(SynOrgConstants.ORG_ENTITY_DEPARTMENT, synDepartment.getCode(), synDepartment.getName());
	         V3xOrgDepartment department = (V3xOrgDepartment)syncOrgManager.getEntityByProperty(V3xOrgDepartment.class.getSimpleName(), "code", synDepartment.getCode(),accountId);
	         try {
	             if(department != null) {
	                 boolean isUpdate = false;
	                 String updateInfo = "部门已经存在！";
	                 synLog.setEntityName(department.getName());
	                 synLog.setSynType(SynOrgConstants.SYN_OPERATION_TYPE_UPDATE);
	                 log.info("部门"+department.getName()+"已经存在！");
	                 // 修改人员启用/停用状态
	                 if(!department.getEnabled()) {
	                     updateInfo += "部门启/停用状态改为:启用 ";
	                     department.setEnabled(true);
	                     isUpdate = true;
	                 }
	                 
	                 // 修改部门名称
	                 if(!department.getName().equals(synDepartment.getName().trim())) {
	                     updateInfo += "名称改为:" + synDepartment.getName().trim() + " ";
	                     department.setName(synDepartment.getName().trim());
	                     isUpdate = true;
	                 }
	                 // 修改上级部门
	                 if(synDepartment.getCode().equals(synDepartment.getParentCode())) {
	                     synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
	                     synLog.setSynLog("上级部门不能是当前部门 ");
	                     synDepartment.setSyncState(SynOrgConstants.SYN_STATE_FAILURE);
	                     logList.add(synLog);
	                     continue; 
	                 }
	                 V3xOrgDepartment oldDeptParent = orgManager.getParentDepartment(department.getId());
	                 V3xOrgDepartment newDeptParent = (V3xOrgDepartment)syncOrgManager.getEntityByProperty(V3xOrgDepartment.class.getSimpleName(), "code", synDepartment.getParentCode(),accountId);
	                 if(newDeptParent != null && newDeptParent.getId() != -1) {
	                     if(oldDeptParent != null && oldDeptParent.getId() != -1) {
	                         if(newDeptParent.getId().longValue() != oldDeptParent.getId().longValue()) {
	                             updateInfo += "上级部门改为:" + newDeptParent.getName() + " ";
	                             department.setSuperior(newDeptParent.getId());
	                             isUpdate = true;
	                         }
	                     } else {
	                         updateInfo += "上级部门改为:" + newDeptParent.getName() + " ";
	                         department.setSuperior(newDeptParent.getId());
	                         isUpdate = true;
	                     }
	                 } else {
	                     if(StringUtils.isBlank(synDepartment.getParentCode()) || account.getCode().equals(synDepartment.getParentCode())) {
	                         if(oldDeptParent != null && !account.getPath().equals(oldDeptParent.getPath())) {
	                             updateInfo += "上级部门改为：根目录 ";
	                             department.setSuperior(accountId);
	                             isUpdate = true;
	                         }
	                     } else {
	                         synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
	                         synLog.setSynLog("上级部门[" + synDepartment.getParentCode() + "]不存在");
	                         synDepartment.setSyncState(SynOrgConstants.SYN_STATE_FAILURE);
	                         logList.add(synLog); 
	                         continue;
	                     }
	                 }
	                 /*
	                 // 修改部门排序号
	                 if(synDepartment.getSortId() != null) {
	                     if(department.getSortId() == null || department.getSortId().longValue() != synDepartment.getSortId().longValue()) {
	                         updateInfo += "排序号改为:" + synDepartment.getSortId() + " ";
	                         department.setSortId(synDepartment.getSortId());
	                         isUpdate = true;
	                     }
	                 }*/
	                 // 更新部门
	                 if(isUpdate) {
	                     OrganizationMessage mes = orgManagerDirect.updateDepartment(department);
	                     List<OrgMessage> errorMsgList = mes.getErrorMsgs();
	                     if(errorMsgList.size() > 0) {
	                         synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
	                         synLog.setSynLog(ErrorMessageUtil.getErrorMessageString(errorMsgList));
	                         synDepartment.setSyncState(SynOrgConstants.SYN_STATE_FAILURE);
	                     } else {
	                         synLog.setSynState(SynOrgConstants.SYN_STATE_SUCCESS);
	                         synLog.setSynLog(updateInfo);
	                         synDepartment.setSyncState(SynOrgConstants.SYN_STATE_SUCCESS);
	                     }
	                 } else {
	                     synDepartment.setSyncState(SynOrgConstants.SYN_STATE_SUCCESS);
                         synLog.setSynState(SynOrgConstants.SYN_STATE_SUCCESS);
                         synLog.setSynLog("部门已经存在，未做修改");
	                 }
	                 logList.add(synLog);
	             } else {
	                 synLog.setSynType(SynOrgConstants.SYN_OPERATION_TYPE_CREATE);
	                 department = new V3xOrgDepartment();
	                 department.setType(OrgConstants.UnitType.Department);
	                 department.setOrgAccountId(accountId);
	                 // 部门名称
	                 department.setName(synDepartment.getName().trim());
	                 // 部门编码
	                 department.setCode(synDepartment.getCode().trim());
	                 // 设置上级部门
	                 if(synDepartment.getCode().equals(synDepartment.getParentCode())) {
	                     synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
	                     synLog.setSynLog("上级部门不能是当前部门 ");
	                     logList.add(synLog);
	                     continue; 
	                 }
	                 if(StringUtils.isBlank(synDepartment.getParentCode()) || account.getCode().equals(synDepartment.getParentCode())) {
	                     department.setSuperior(accountId);
	                 } else {
	                     /** 获取父级部门编号 **/
	                     V3xOrgDepartment orgDept = (V3xOrgDepartment)syncOrgManager.getEntityByProperty(V3xOrgDepartment.class.getSimpleName(), "code", synDepartment.getParentCode(), accountId);
	                     if(orgDept != null && orgDept.getId() != -1) {
	                         department.setSuperior(orgDept.getId());
	                     } else {
	                         synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
	                         synLog.setSynLog("上级部门[" + synDepartment.getParentCode() + "]不存在");
	                       logList.add(synLog); 
	                         continue;
	                     }
	                 }
	                 department.setEnabled(true);
	                 department.setSortId(synDepartment.getSortId() != null ? synDepartment.getSortId() : 1L);
	                 //department.setDescription(synDepartment.getDescription() != null ? synDepartment.getDescription().trim() : "");
	                 // 更新时间
	                 department.setUpdateTime(new Date());
	                 // 创建时间
	                 department.setCreateTime(new Date());
	                 department.setIsInternal(true);
	                 department.setLevelScope(-1);
	                 department.setSortIdType("1");
	                 department.setStatus(1);
	                 OrganizationMessage res=orgManagerDirect.addDepartment(department);
	                 
	                 if(res.isSuccess()){
	                	 synDepartment.setSyncState(SynOrgConstants.SYN_STATE_SUCCESS);
		                 synLog.setSynState(SynOrgConstants.SYN_STATE_SUCCESS);
		                 synLog.setSynLog("新增部门：" + department.getName() + "[" + department.getCode() + "]");
		                 resDep.add(synDepartment);
	                 }else{
	                	 synDepartment.setSyncState(SynOrgConstants.SYN_STATE_FAILURE);
	    	             synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
	    	             synLog.setSynLog("新增部门：" + department.getName() + "[" + department.getCode() + "]异常："+res.getErrorMsgs().get(0).getCode().toString());
		                 resDep.add(synDepartment);
	                 }
	                 
	             }
	         } catch(Exception e) {
	             synDepartment.setSyncState(SynOrgConstants.SYN_STATE_FAILURE);
	             synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
	             synLog.setSynLog(e.getMessage());
	             logList.add(synLog); 
	             resDep.add(synDepartment);
	             continue;
	         }
	         
	         if(synLog != null) {
					logList.add(synLog);
				}
		}
		// 创建同步日志
		if(logList.size() > 0) {
			syncLogManager.createAll(logList);
			System.out.println("部门同步完成,共同步:"+ logList.size()+"条数据!!");
			log.info("部门同步完成,共同步:"+ logList.size()+"条数据!!");
		}
		// 更新同步信息
		syncDepartmentDao.updateAll(resDep); 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete() {
        syncDepartmentDao.deleteAll();
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
     * 设置syncDepartmentDao
     * @param syncDepartmentDao syncDepartmentDao
     */
    public void setSyncDepartmentDao(SyncDepartmentDao syncDepartmentDao) {
        this.syncDepartmentDao = syncDepartmentDao;
    }

    /**
     * 设置orgManager
     * @param orgManager orgManager
     */
    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    /**
     * 设置syncLogManager
     * @param syncLogManager syncLogManager
     */
    public void setSyncLogManager(SyncLogManager syncLogManager) {
        this.syncLogManager = syncLogManager;
    }	public SyncUnitDao getSyncUnitDao() {
		if (syncUnitDao == null) {
			syncUnitDao = (SyncUnitDao) AppContext.getBean("syncUnitDao");
		}
		return syncUnitDao;
	}
}
