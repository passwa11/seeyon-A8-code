package com.seeyon.apps.synorg.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.organization.bo.OrganizationMessage;
import com.seeyon.ctp.organization.bo.OrganizationMessage.OrgMessage;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.manager.OrgManagerDirect;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.rest.util.MemberUtil;

public class SyncUnitManagerImpl implements SyncUnitManager {
	private static final Log log = LogFactory.getLog(SyncUnitManagerImpl.class);
	
    /**创建单位密码追加*/
    public static final String ACCOUNT_PWD_ADD = "unitadmin";

	/** 组织机构管理器 */
	private SyncOrgManager syncOrgManager;
	/** 组织机构服务方法 */
	private OrgManager orgManager;
	/** 中间表单位DAO */
	private SyncUnitDao syncUnitDao;
	/** 组织机构服务方法 */
	private OrgManagerDirect orgManagerDirect;
	/** 同步日志管理器 */
	private SyncLogManager syncLogManager;
    /** 部门实体查询接口 */
    private SyncDepartmentDao syncDepartmentDao;
    

	@Override
	public void synAllUnit() {
		List<SynUnit> resUnit = new ArrayList<SynUnit>();
		List<SynUnit> allUnit = getSyncUnitDao().findAll();
		
		if(null!=allUnit){
			allUnit = unitSort(allUnit);
		}
		log.info("单位同步开始!!");
		if (allUnit != null && allUnit.size() > 0) {
			List<SynLog> logList = new ArrayList<SynLog>();
			for (int i = 0; i < allUnit.size(); i++) {
				SynUnit synUnit = allUnit.get(i);
				synUnit.setSyncDate(new Date());
				SynLog synLog = new SynLog(SynOrgConstants.ORG_ENTITY_UNIT,
						synUnit.getCode(), synUnit.getName());
				V3xOrgAccount unit = (V3xOrgAccount) syncOrgManager
						.getEntityByProperty(
								V3xOrgAccount.class.getSimpleName(), "code",
								synUnit.getCode(), null);
				try {
					if (unit != null) {
						log.info("unit==========="+unit.getCode()+"--"+unit.getId());
						boolean isUpdate = false;
						String updateInfo = "";
						synLog.setEntityName(unit.getName());
						synLog.setSynType(SynOrgConstants.SYN_OPERATION_TYPE_UPDATE);
						// 修改单位名
						if (!unit.getName().equals(synUnit.getName().trim())) {
							updateInfo += "名称改为:" + synUnit.getName().trim()
									+ " ";
							unit.setName(synUnit.getName().trim());
							isUpdate = true;
						}
                        // 修改排序号
//                        if(unit.getSortId() != null && synUnit.getSortId() != null && unit.getSortId().longValue() != synUnit.getSortId().longValue()) {
//                            updateInfo += "排序号改为:" + synUnit.getSortId() + " ";
//                            unit.setSortId(synUnit.getSortId());
//                            isUpdate = true;
//                        }
						// 修改描述
						if (synUnit.getDescription() != null
								&& !"".equals(synUnit.getDescription().trim())) {
							if (unit.getDescription() == null
									|| !synUnit
											.getDescription()
											.trim()
											.equals(unit.getDescription()
													.trim())) {
								updateInfo += "描述改为:"
										+ synUnit.getDescription().trim() + " ";
								unit.setDescription(synUnit.getDescription()
										.trim());
								isUpdate = true;
							}
						} else {
							if (unit.getDescription() != null
									&& !"".equals(unit.getDescription().trim())) {
								updateInfo += "描述改为: 空字符串 ";
								unit.setDescription("");
								isUpdate = true;
							}
						}
						if (isUpdate) {
							OrganizationMessage mes = orgManagerDirect
									.updateAccount(unit);
							List<OrgMessage> errorMsgList = mes.getErrorMsgs();
							if (errorMsgList.size() > 0) {
								synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
								synLog.setSynLog(ErrorMessageUtil
										.getErrorMessageString(errorMsgList));
								synUnit.setSyncState(SynOrgConstants.SYN_STATE_FAILURE);
							} else {
								synLog.setSynState(SynOrgConstants.SYN_STATE_SUCCESS);
								synLog.setSynLog(updateInfo);
								synUnit.setSyncState(SynOrgConstants.SYN_STATE_SUCCESS);
							}
							synUnit.setOa_Id(String.valueOf(unit.getId()));
							resUnit.add(synUnit);
						} else {
							updateInfo="已经存在单位，无修改项目！";
							synUnit.setSyncState(SynOrgConstants.SYN_STATE_SUCCESS);
							synUnit.setOa_Id(String.valueOf(unit.getId()));
							synLog.setSynState(SynOrgConstants.SYN_STATE_SUCCESS);
							synLog.setSynLog(updateInfo);
							resUnit.add(synUnit);
						}
					} else {
						/** 添加单位 */
						synLog.setSynType(SynOrgConstants.SYN_OPERATION_TYPE_CREATE);
						unit = new V3xOrgAccount();
						unit.setIdIfNew();
						unit.setOrgAccountId(unit.getId());
						unit.setName(synUnit.getName());
						unit.setCode(synUnit.getCode());
						//unit.setSortId(sortId);
						// unit.setShortName(synUnit.g);
						if (null == synUnit.getParentCode()) {
							unit.setSuperior(getOrgManager().getRootAccount()
									.getId());
						} else {
							String accountName = getSyncUnitDao()
									.findAllByCode(synUnit.getParentCode())
									.getName();
							unit.setSuperior(getOrgManager().getAccountByName(
									accountName).getId());
						}
						
				        //创建单位时为新建单位复制公文单需要当前人员ID，这里这是为-1L
				        User user = new User();
				        user.setName("ddh-creatAccount");
				        user.setId(-1L);
				        AppContext.putThreadContext(GlobalNames.SESSION_CONTEXT_USERINFO_KEY,user);
				        
				        //设置默认管理员（默认单位组织机构编码为管理员账号）
				        Map admininfo=new HashMap();
				        admininfo.put("loginName", synUnit.getCode()+ACCOUNT_PWD_ADD);
				        admininfo.put("password", SynOrgTask.getDefaultPassword().trim()+ACCOUNT_PWD_ADD);
				        V3xOrgMember adminmember = MemberUtil.createMember(admininfo);
				        adminmember.setName(synUnit.getCode()+ACCOUNT_PWD_ADD);
				        
				        OrganizationMessage res=getOrgManagerDirect().addAccount(unit,adminmember);
				        
						if(res.isSuccess()){
							synLog.setSynState(SynOrgConstants.SYN_STATE_SUCCESS);
							synUnit.setSyncState(SynOrgConstants.SYN_STATE_SUCCESS);
							synUnit.setOa_Id(String.valueOf(unit.getId()));
							synLog.setSynLog("新增单位：" + unit.getName() + "["
									+ unit.getCode() + "]");
							resUnit.add(synUnit);
						}else{
							synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
							logList.add(synLog);
							synUnit.setSyncState(SynOrgConstants.SYN_STATE_FAILURE);
							synLog.setSynLog("新增单位：" + unit.getName() + "["
									+ unit.getCode() + "]异常:"+res.getErrorMsgs().get(0).getCode().toString());
							resUnit.add(synUnit);
						}
						
					}
				} catch (Exception e) {
					synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
					synLog.setSynLog(e.getMessage());
					logList.add(synLog);
					synUnit.setSyncState(SynOrgConstants.SYN_STATE_FAILURE);
					resUnit.add(synUnit);
					continue;
				}
				if (synLog != null) {
					logList.add(synLog);
				}
			}
			// 创建同步日志
			if (logList.size() > 0) {
				syncLogManager.createAll(logList);
			}
			// 更新同步信息
			getSyncUnitDao().updateAll(resUnit);
			log.info("单位同步完成,共同步:"+ logList.size()+"条数据!!");
			
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
    	
    	for(SynUnit synunit :allunit){
    		//单位下的所有部门
    		List<SynDepartment> accountChilddepts = findAllChilds(synunit.getCode(), allDepartments);
    		
    		if(accountChilddepts != null && accountChilddepts.size() > 0) {
    			//部门信息从中间库同步到OA
    			updateDepartment(accountChilddepts,Long.valueOf(synunit.getOa_Id()));

    		}
    	}
	}
	
	 /**
     * 将部门list同步到OA
     * @param synDepartment
     * @return
     */
    private void updateDepartment(List<SynDepartment> accountChilddepts,long accountId){
    	V3xOrgAccount account = null;
		try {
			account = getOrgManager().getAccountById(accountId);
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
	                     OrganizationMessage mes = getOrgManagerDirect().updateDepartment(department);
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
	                 getOrgManagerDirect().addDepartment(department);
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
		syncDepartmentDao.updateAll(accountChilddepts); 
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
    
    //单位父子排序
    private List<SynUnit> unitSort(List<SynUnit> allUnit) {
    	String parentCode ="";
    	List<SynUnit> list = new ArrayList<SynUnit>();
    	
    	for(SynUnit unit:allUnit){
    		if(null==unit.getParentCode()){
    			parentCode=unit.getCode();
    			list.add(unit);
    			allUnit.remove(unit);
    			break;
    		}
    	}
    	if(!"".equals(parentCode)){
    		list.addAll(findUnitChilds(parentCode,allUnit));
    	}
    	if(list.size()<allUnit.size()){
    		list.addAll(getDiffrent(allUnit,list,parentCode));
    	}

    	return list;
    }
    
    private List<SynUnit> findUnitChilds(String parentCode,List<SynUnit> allUnit) {
    	List<SynUnit> list = new ArrayList<SynUnit>();
        for(SynUnit unit : allUnit) {
            if(unit.getParentCode().equals(parentCode)) {
                list.add(unit);
                list.addAll(findUnitChilds(unit.getCode(), allUnit));
            }
        }
        return list;
    }
    
    /** 
    * 查找不在父子结构中的独立单位，将此单位ParentCode 设置为parentCode
    */
    private List<SynUnit> getDiffrent(List<SynUnit> allUnit,List<SynUnit> select,String parentCode){
    	List<SynUnit> diff = new ArrayList<SynUnit>();
        for(SynUnit unit:allUnit)
        {
            if(!select.contains(unit))
            {
            	unit.setParentCode(parentCode);
                diff.add(unit);
            }
        }
        return diff;
    }
    
    
	@Override
	public void create(List<SynUnit> unitList) {
		getSyncUnitDao().createAll(unitList);
	}
	
	@Override
	public void create(SynUnit unit) {
    	try{
    		SynUnit findUnit=getSyncUnitDao().findAllByCode(unit.getCode());
    		List<SynUnit> upList=new ArrayList<SynUnit>();
    		if(null==findUnit){
				getSyncUnitDao().create(unit);
			}else{
				getSyncUnitDao().delete(findUnit);
				getSyncUnitDao().create(unit);
			}
    	}catch (Exception e) {
			log.error("大渡河MQ导入单位数据异常！",e);
			System.out.println("大渡河MQ导入单位数据异常！");
		}
	}

	/**
	 * 设置syncOrgManager
	 * 
	 * @param syncOrgManager
	 *            syncOrgManager
	 */
	public void setSyncOrgManager(SyncOrgManager syncOrgManager) {
		this.syncOrgManager = syncOrgManager;
	}

	public OrgManager getOrgManager() {
		if (orgManager == null) {
			orgManager = (OrgManager) AppContext.getBean("orgManager");
		}
		return orgManager;
	}

	public SyncUnitDao getSyncUnitDao() {
		if (syncUnitDao == null) {
			syncUnitDao = (SyncUnitDao) AppContext.getBean("syncUnitDao");
		}
		return syncUnitDao;
	}

	public OrgManagerDirect getOrgManagerDirect() {
		if (orgManagerDirect == null) {
			orgManagerDirect = (OrgManagerDirect) AppContext
					.getBean("orgManagerDirect");
		}
		return orgManagerDirect;
	}
	
    /**
     * 设置syncLogManager
     * @param syncLogManager syncLogManager
     */
    public void setSyncLogManager(SyncLogManager syncLogManager) {
        this.syncLogManager = syncLogManager;
    }
    /**
     * 设置syncDepartmentDao
     * @param syncDepartmentDao syncDepartmentDao
     */
    public void setSyncDepartmentDao(SyncDepartmentDao syncDepartmentDao) {
        this.syncDepartmentDao = syncDepartmentDao;
    }
    
}
