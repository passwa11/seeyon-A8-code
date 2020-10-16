package com.seeyon.v3x.edoc.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.owasp.esapi.Logger;

import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.govdoc.exchange.util.GovDocUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ProductEditionEnum;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.dao.V3XFileDAO;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.template.CtpTemplateCategory;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormAuthViewBean;
import com.seeyon.ctp.form.bean.FormAuthViewFieldBean;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormFieldBean;
import com.seeyon.ctp.form.bean.FormFieldComBean.FormFieldComEnum;
import com.seeyon.ctp.form.bean.FormViewBean;
import com.seeyon.ctp.form.biz.exception.BizException;
import com.seeyon.ctp.form.biz.vo.BizValidateResultVO;
import com.seeyon.ctp.form.dee.design.DeeDesignManager;
import com.seeyon.ctp.form.manager.GovdocFormExtendManager;
import com.seeyon.ctp.form.manager.GovdocFormOpinionSortManager;
import com.seeyon.ctp.form.po.FormOwner;
import com.seeyon.ctp.form.po.FormPermissionConfig;
import com.seeyon.ctp.form.po.FormResource;
import com.seeyon.ctp.form.po.GovdocFormExtend;
import com.seeyon.ctp.form.po.GovdocFormOpinionSort;
import com.seeyon.ctp.form.util.Enums;
import com.seeyon.ctp.form.util.Enums.FieldAccessType;
import com.seeyon.ctp.form.util.Enums.FormResourcePropertyTypeEnum;
import com.seeyon.ctp.form.util.Enums.FormType;
import com.seeyon.ctp.form.util.infopath.ElementUtil;
import com.seeyon.ctp.form.util.infopath.InfoPathObject;
import com.seeyon.ctp.form.util.infopath.InfoPath_xsl;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.OrgConstants.ORGENT_STATUS;
import com.seeyon.ctp.organization.OrgConstants.RelationshipObjectiveName;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgRelationship;
import com.seeyon.ctp.organization.bo.V3xOrgRole;
import com.seeyon.ctp.organization.dao.OrgDao;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.manager.OrgManagerDirect;
import com.seeyon.ctp.organization.po.OrgRelationship;
import com.seeyon.ctp.privilege.bo.PrivMenuBO;
import com.seeyon.ctp.privilege.bo.PrivTreeNodeBO;
import com.seeyon.ctp.privilege.dao.PrivilegeCacheImpl;
import com.seeyon.ctp.privilege.dao.RoleMenuDao;
import com.seeyon.ctp.privilege.enums.MenuTypeEnums;
import com.seeyon.ctp.privilege.manager.MenuCacheManager;
import com.seeyon.ctp.privilege.manager.MenuManager;
import com.seeyon.ctp.privilege.manager.PrivilegeMenuManager;
import com.seeyon.ctp.privilege.po.PrivMenu;
import com.seeyon.ctp.privilege.po.PrivRoleMenu;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.edoc.dao.EdocFormElementDao;
import com.seeyon.v3x.edoc.dao.UpgradeDao;
import com.seeyon.v3x.edoc.domain.EdocCategory;
import com.seeyon.v3x.edoc.domain.EdocElementFlowPermAcl;
import com.seeyon.v3x.edoc.domain.EdocForm;
import com.seeyon.v3x.edoc.domain.EdocFormElement;
import com.seeyon.v3x.edoc.domain.EdocFormExtendInfo;
import com.seeyon.v3x.edoc.domain.EdocFormFlowPermBound;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.webmodel.FormBoundPerm;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;
import com.seeyon.v3x.exchange.domain.EdocSendRecord;

import www.seeyon.com.biz.enums.BizOperationEnum;
import www.seeyon.com.utils.FileUtil;

public class EdocUpgradeManagerImpl implements EdocUpgradeManager{
	
	private  UpgradeDao edocUpgradeDao;
	private  TemplateManager templateManager;
	private  OrgManager orgManager;
	private  V3XFileDAO v3xFileDAO;
	private  FileManager fileManager;
	private  FormApi4Cap3 formApi4Cap3;
	private  GovdocFormExtendManager govdocFormExtendManager;
	private  GovdocFormOpinionSortManager govdocFormOpinionSortManager;
	private  DeeDesignManager deeDesignManager;
	private  EdocFormElementDao edocFormElementDao;
	private  EdocFormManager edocFormManager;
	private  EdocElementFlowPermAclManager edocElementFlowPermAclManager;

	private  OrgManagerDirect orgManagerDirect;
	private  PrivilegeMenuManager  privilegeMenuManager;
	private  MenuManager menuManager;
	private  RoleMenuDao roleMenuDao;
	private  MenuCacheManager menuCacheManager;
	private  AffairManager affairManager;
	private  OrgDao orgDao;
	private  PrivilegeCacheImpl privilegeCache;
	private ConfigManager configManager;
	
	public PrivilegeCacheImpl getPrivilegeCache() {
		return privilegeCache;
	}
	public void setPrivilegeCache(PrivilegeCacheImpl privilegeCache) {
		this.privilegeCache = privilegeCache;
	}

	private static Log LOG = CtpLogFactory.getLog(EdocUpgradeManagerImpl.class);
	public boolean NO_NEEDTO_CHECKED = false;//是否不需要检查升级
	
	//所有的发文种类升级，老数据的id，对应新数据的对象。
	Map<Long, CtpTemplateCategory> categories = new HashMap<Long, CtpTemplateCategory>();
	//保存老的文单，对应的新的文单。 键值分别是老文单id和新文单id
	private final Map<Long, Long> formIds = new HashMap<Long, Long>();
	//保存所有的单位
	List<V3xOrgAccount> units = new ArrayList<V3xOrgAccount>();	
	//保存预置表单数据
	private List<Long> presetForms= new ArrayList<Long>();
	//保存新增的公文元素
	List<Long> edocElementIds = new ArrayList<Long>();
	//新增的角色的id
	List<Long> saveRoleIds = new ArrayList<Long>();
		
	@Override
	public synchronized String upgrade() throws Exception {
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		LOG.info("升级时间显示1"+f.format(new Date()));
		//这里的注释必须去掉，因为整个程序只能执行一次 
		//jdqxUpgrade(); 这个的升级迁移到升级程序里面去了
		try {
			units = orgManager.getAllAccounts();
			//菜单权限升级  -- 注释掉 在升级程序中重写
			//createUnitRole();
			LOG.info("升级时间显示2"+f.format(new Date()));
			//公文元素隐藏
			//disableEdocElements();
			//公文元素添加
			insterEdocElements();
			LOG.info("升级时间显示3"+f.format(new Date()));
			//表单升级
			createFormByEdocForm();
			LOG.info("升级时间显示4"+f.format(new Date()));
			//预置表单数据
			presetgovdocForms();
			LOG.info("升级时间显示5"+f.format(new Date()));
			//模板升级
			LOG.info("模板升级开始...");
			edocUpgradeDao.templateUpgrade(formIds,categories);
			LOG.info("升级时间显示6"+f.format(new Date()));
			LOG.info("模板升级结束...");
			//兼容老公文数据  数据拷贝
			try{
//				LOG.info("公文数据升级开始...copySummarySecretLevelToAffair");
//				copySummarySecretLevelToAffair();
				LOG.info("公文数据升级开始...copyRegisterEdocSummaryAndAffair");
				copyRegisterEdocSummaryAndAffair(); 
				LOG.info("公文数据升级开始...copyExchangeSendToEdocSummary");
				copyExchangeSendToEdocSummary();
				LOG.info("公文数据升级开始...copyExchangeRecToEdocSummary");
				copyExchangeRecToEdocSummary();
				
				LOG.info("公文数据升级开始...公文交换数据升级");
				updateExchangeData();
				LOG.info("公文数据升级开始...公文运行中流程数据升级开始");
				updateProcessRunningData();
				LOG.info("升级时间显示7"+f.format(new Date()));
				LOG.info("公文数据升级开始...公文运行中流程数据升级结束");
			}catch (Exception e) {
				LOG.error("复制交换和登记表数据出错：========="+e.getMessage(),e);
				//e.printStackTrace();
			}
			
			//升级form表单  receive_unit  sql
			try{
				LOG.info("升级表单 receive_unit 字段数据开始...");
				executeFormmainUpLevel();
				LOG.info("升级时间显示11"+f.format(new Date()));
				LOG.info("升级表单 receive_unit 字段数据结束...");
			}catch (Exception e) {
				LOG.error("升级表单 receive_unit 字段数据出错：========="+e.getMessage(),e);
				//e.printStackTrace();
			}
			LOG.info("升级时间显示10"+f.format(new Date()));
			//deleteOrgRoleAndMenuInfo();
		} catch (Exception e) {
			//e.printStackTrace();
			LOG.error("公文升级出现异常：========="+e.getMessage(),e);
			throw e;
		}finally{
			//保存新老数据关系
			saveDataRelation();
			LOG.info("升级时间显示9"+f.format(new Date()));
		}
		return "-1";
	
	
	}
	//企业版创建5个公文角色
	private void createOrgRoleForAccount() throws BusinessException{
		// 单位收文员 单位送文员 统计员 部门收文员 部门送文员
		Date date = new Date();
//		String AccountGovdocRec ="INSERT INTO ORG_ROLE(ID, NAME, CATEGORY, CODE, TYPE, BOND, IS_BENCHMARK, ORG_ACCOUNT_ID, SORT_ID, IS_ENABLE, IS_DELETED, STATUS, CREATE_TIME, UPDATE_TIME, DESCRIPTION,EXTERNAL_TYPE)"
//				+ " VALUES (2019012500001,'AccountGovdocRec','0','AccountGovdocRec',1,1,0,670869647114347,5,1,0,1,'2016-1-20 00:00:00','2016-1-20 00:00:00','',null)";
//		String AccountGovdocSend ="INSERT INTO ORG_ROLE(ID, NAME, CATEGORY, CODE, TYPE, BOND, IS_BENCHMARK, ORG_ACCOUNT_ID, SORT_ID, IS_ENABLE, IS_DELETED, STATUS, CREATE_TIME, UPDATE_TIME, DESCRIPTION,EXTERNAL_TYPE)"
//				+ " VALUES (2019012500002,'AccountGovdocSend','0','AccountGovdocSend',1,1,0,670869647114347,6,1,0,1,'2016-1-20 00:00:00','2016-1-20 00:00:00','',null)";
//		String AccountGovdocStat ="INSERT INTO ORG_ROLE(ID, NAME, CATEGORY, CODE, TYPE, BOND, IS_BENCHMARK, ORG_ACCOUNT_ID, SORT_ID, IS_ENABLE, IS_DELETED, STATUS, CREATE_TIME, UPDATE_TIME, DESCRIPTION,EXTERNAL_TYPE)"
//				+ " VALUES (2019012500003,'AccountGovdocStat','0','AccountGovdocStat',1,1,0,670869647114347,6,1,0,1,'2016-1-20 00:00:00','2016-1-20 00:00:00','',null)";
		V3xOrgRole AccountGovdocRec = new V3xOrgRole();
		AccountGovdocRec.setId(2019012500001L);
		AccountGovdocRec.setName("AccountGovdocRec");
		AccountGovdocRec.setCategory("0");
		AccountGovdocRec.setCode("AccountGovdocRec");
		AccountGovdocRec.setType(1);
		AccountGovdocRec.setBond(1);
		AccountGovdocRec.setIsBenchmark(false);
		AccountGovdocRec.setOrgAccountId(OrgConstants.ACCOUNTID);
		AccountGovdocRec.setSortId(5L);
		AccountGovdocRec.setEnabled(true);
		AccountGovdocRec.setIsDeleted(false);
		AccountGovdocRec.setStatus(1);
		AccountGovdocRec.setCreateTime(date);
		AccountGovdocRec.setUpdateTime(date);
		AccountGovdocRec.setDescription("单位收文员");
		orgManagerDirect.addRole(AccountGovdocRec);
		
		V3xOrgRole AccountGovdocSend = new V3xOrgRole();
		AccountGovdocSend.setId(2019012500002L);
		AccountGovdocSend.setName("AccountGovdocSend");
		AccountGovdocSend.setCategory("0");
		AccountGovdocSend.setCode("AccountGovdocSend");
		AccountGovdocSend.setType(1);
		AccountGovdocSend.setBond(1);
		AccountGovdocSend.setIsBenchmark(false);
		AccountGovdocSend.setOrgAccountId(OrgConstants.ACCOUNTID);
		AccountGovdocSend.setSortId(6L);
		AccountGovdocSend.setEnabled(true);
		AccountGovdocSend.setIsDeleted(false);
		AccountGovdocSend.setStatus(1);
		AccountGovdocSend.setCreateTime(date);
		AccountGovdocSend.setUpdateTime(date);
		AccountGovdocSend.setDescription("单位送文员");
		orgManagerDirect.addRole(AccountGovdocSend);
		
		V3xOrgRole AccountGovdocStat = new V3xOrgRole();
		AccountGovdocStat.setId(2019012500003L);
		AccountGovdocStat.setName("AccountGovdocStat");
		AccountGovdocStat.setCategory("0");
		AccountGovdocStat.setCode("AccountGovdocStat");
		AccountGovdocStat.setType(1);
		AccountGovdocStat.setBond(1);
		AccountGovdocStat.setIsBenchmark(false);
		AccountGovdocStat.setOrgAccountId(OrgConstants.ACCOUNTID);
		AccountGovdocStat.setSortId(7L);
		AccountGovdocStat.setEnabled(true);
		AccountGovdocStat.setIsDeleted(false);
		AccountGovdocStat.setStatus(1);
		AccountGovdocStat.setCreateTime(date);
		AccountGovdocStat.setUpdateTime(date);
		AccountGovdocStat.setDescription("单位统计员");
		orgManagerDirect.addRole(AccountGovdocStat);
//		String DepartmentGovdocRec ="INSERT INTO ORG_ROLE(ID, NAME, CATEGORY, CODE, TYPE, BOND, IS_BENCHMARK, ORG_ACCOUNT_ID, SORT_ID, IS_ENABLE, IS_DELETED, STATUS, CREATE_TIME, UPDATE_TIME, DESCRIPTION,EXTERNAL_TYPE)"
//				+ " VALUES (2019012500004,'DepartmentGovdocRec','0','DepartmentGovdocRec',1,1,0,670869647114347,5,1,0,1,'2016-1-20 00:00:00','2016-1-20 00:00:00','',null)";
//		String DepartmentGovdocSend ="INSERT INTO ORG_ROLE(ID, NAME, CATEGORY, CODE, TYPE, BOND, IS_BENCHMARK, ORG_ACCOUNT_ID, SORT_ID, IS_ENABLE, IS_DELETED, STATUS, CREATE_TIME, UPDATE_TIME, DESCRIPTION,EXTERNAL_TYPE)"
//				+ " VALUES (2019012500005,'DepartmentGovdocSend','0','DepartmentGovdocSend',1,1,0,670869647114347,6,1,0,1,'2016-1-20 00:00:00','2016-1-20 00:00:00','',null)";

		V3xOrgRole DepartmentGovdocRec = new V3xOrgRole();
		DepartmentGovdocRec.setId(2019012500004L);
		DepartmentGovdocRec.setName("DepartmentGovdocRec");
		DepartmentGovdocRec.setCategory("0");
		DepartmentGovdocRec.setCode("DepartmentGovdocRec");
		DepartmentGovdocRec.setType(1);
		DepartmentGovdocRec.setBond(2);
		DepartmentGovdocRec.setIsBenchmark(false);
		DepartmentGovdocRec.setOrgAccountId(OrgConstants.ACCOUNTID);
		DepartmentGovdocRec.setSortId(8L);
		DepartmentGovdocRec.setEnabled(true);
		DepartmentGovdocRec.setIsDeleted(false);
		DepartmentGovdocRec.setStatus(1);
		DepartmentGovdocRec.setCreateTime(date);
		DepartmentGovdocRec.setUpdateTime(date);
		DepartmentGovdocRec.setDescription("部门收文员");
		orgManagerDirect.addRole(DepartmentGovdocRec);
		
		V3xOrgRole DepartmentGovdocSend = new V3xOrgRole();
		DepartmentGovdocSend.setId(2019012500005L);
		DepartmentGovdocSend.setName("DepartmentGovdocSend");
		DepartmentGovdocSend.setCategory("0");
		DepartmentGovdocSend.setCode("DepartmentGovdocSend");
		DepartmentGovdocSend.setType(1);
		DepartmentGovdocSend.setBond(2);
		DepartmentGovdocSend.setIsBenchmark(false);
		DepartmentGovdocSend.setOrgAccountId(OrgConstants.ACCOUNTID);
		DepartmentGovdocSend.setSortId(9L);
		DepartmentGovdocSend.setEnabled(true);
		DepartmentGovdocSend.setIsDeleted(false);
		DepartmentGovdocSend.setStatus(1);
		DepartmentGovdocSend.setCreateTime(date);
		DepartmentGovdocSend.setUpdateTime(date);
		DepartmentGovdocSend.setDescription("部门送文员");
		orgManagerDirect.addRole(DepartmentGovdocSend);
		
//		Object[] insertOrgRole1 = new Object[]{959477643164863881L, "AccountGovdocRec", "0", "AccountGovdocRec", 1, 1, 0, -1730833917365171641L, 5, 1, 0, 1, "2016-1-20 00:00:00", "2016-1-20 00:00:00", "",null};
//		Object[] insertOrgRole2 = new Object[]{959477643164863882L, "AccountGovdocSend", "0", "AccountGovdocSend", 1, 1, 0, -1730833917365171641L, 6, 1, 0, 1, "2016-1-20 00:00:00", "2016-1-20 00:00:00", "",null};
//		Object[] insertOrgRole3 = new Object[]{959477643164863889L, "AccountGovdocStat", "0", "AccountGovdocStat", 1, 1, 0, -1730833917365171641L, 6, 1, 0, 1, "2016-1-20 00:00:00", "2016-1-20 00:00:00", "",null};
		
		
	}
	
	private void deleteOrgRoleAndMenuInfo()
	  {
	    String sql1 = "DELETE FROM ORG_ROLE WHERE ID IN (-7773497729743630111,-7903497729743630359,7998243536501097313) or code='EdocModfiy' ";
	    String sql2 = "DELETE FROM PRIV_ROLE_MENU WHERE ROLEID IN (-7773497729743630111,-7903497729743630359,1343960019065655086,7998243536501097313) ";
	    JDBCAgent jdbc = new JDBCAgent();
	    List batchedSql = new ArrayList();
	    batchedSql.add(sql1);
	    batchedSql.add(sql2);
	    try {
	      jdbc.executeBatch(batchedSql);
	    } catch (Exception e) {
	      LOG.error("删除角色报错了", e);
	    }
	  }

	
	private void updateExchangeData() {

		String getdbType = getdbType();
		LOG.info("getdbType="+getdbType);
		if("MySQL".equalsIgnoreCase(getdbType)){
			List<String>  batchedSql = new ArrayList<String>();
			//-- 公文交换列表兼容老公文已分送数据
			String sql1 ="UPDATE EDOC_SUMMARY S, EDOC_EXCHANGE_SEND EX, CTP_AFFAIR A SET S.EXCHANGE_SEND_AFFAIRID = A.ID"+
			" WHERE S.ID = EX.ID AND EX.STATUS = 1 AND A.SUB_OBJECT_ID = EX.ID AND A.OBJECT_ID = EX.EDOC_ID"+
			" AND A.APP = 4 AND A.SUB_APP = 22 AND A.STATE = 4 AND S.EDOC_TYPE = 99 AND EX.SEND_USER_ID = A.MEMBER_ID ";
			batchedSql.add(sql1);
			//-- 老公文交换已发送、已回退数据同步到待办状态
	        String sql2 ="UPDATE CTP_AFFAIR A INNER JOIN EDOC_EXCHANGE_SEND EX ON A.SUB_OBJECT_ID = EX.ID AND EX.STATUS IN (1,2) SET A.STATE = 4"+
			" WHERE A.APP = 4 AND A.SUB_APP = 22 AND A.IS_DELETE = 0 AND A.STATE = 3";
	        batchedSql.add(sql2);
			//-- 老公文交换已签收、已登记、已回退数据同步到待办状态
			String sql3 ="UPDATE CTP_AFFAIR A INNER JOIN EDOC_EXCHANGE_RECIEVE EX ON A.SUB_OBJECT_ID = EX.ID AND EX.STATUS IN (1,2,3) SET A.STATE = 4"+
			" WHERE A.APP = 4 AND A.SUB_APP = 23 AND A.IS_DELETE = 0 AND A.STATE = 3";
			batchedSql.add(sql3);
			//-- 老公文交换已登记数据同步到待办状态
			String sql4 ="UPDATE CTP_AFFAIR A INNER JOIN EDOC_EXCHANGE_RECIEVE EX ON A.SUB_OBJECT_ID = EX.ID AND EX.STATUS = 2 SET A.STATE = 4"+
			" WHERE A.APP = 4 AND A.SUB_APP = 24 AND A.IS_DELETE = 0 AND A.STATE = 3";
			batchedSql.add(sql4);
			//-- 老公文交换已登记数据同步到待办状态
			String sql5 = "UPDATE CTP_AFFAIR A INNER JOIN EDOC_REGISTER ER ON A.OBJECT_ID = ER.ID AND ER.STATE = 2 SET A.STATE = 4"+
			" WHERE A.APP = 4 AND A.SUB_APP = 24 AND A.IS_DELETE = 0 AND A.STATE = 3";
			batchedSql.add(sql5);
			//-- 老公文交换已分发数据同步到待办状态
			String sql6 = "UPDATE CTP_AFFAIR A INNER JOIN EDOC_REGISTER ER ON A.OBJECT_ID = ER.ID AND ER.DISTRIBUTE_STATE = 2 SET A.STATE = 4"+
			" WHERE A.APP = 4 AND A.SUB_APP = 34 AND A.IS_DELETE = 0 AND A.STATE = 3";
			batchedSql.add(sql6);
			
			//-- 转发文数据升级
			String sql7 = "INSERT INTO GOVDOC_EXCHANGE_MAIN ( ID, SUBJECT, CREATE_TIME, SUMMARY_ID, EXCHANGE_TYPE, REFERENCE_ID)"+
			" SELECT ID, SUBJECT, TURN_DATE, SUMMARY_ID, 3, REFERENCE FROM GOVDOC_RELATION WHERE GOVDOC_TYPE = 1";
			batchedSql.add(sql7);
			//-- 升级EDOC_SUMMARY字段EXCHANGE_TYPE
			String sql8 = "UPDATE EDOC_SUMMARY A, GOVDOC_EXCHANGE_MAIN B,GOVDOC_EXCHANGE_DETAIL C SET A.EXCHANGE_TYPE = B.EXCHANGE_TYPE"+
			" WHERE C.MAIN_ID = B.ID AND C.SUMMARY_ID = A.ID";
			batchedSql.add(sql8);
			//-- 设置交换类型默认值
			String sql9 = "UPDATE EDOC_SUMMARY SET EXCHANGE_TYPE = 0 WHERE EXCHANGE_TYPE IS NULL";
			batchedSql.add(sql9);
			//-- 老公文回退流转状态维护
			String sql10 = "UPDATE EDOC_SUMMARY A, GOVDOC_EXCHANGE_DETAIL_LOG B SET A.TRANSFER_STATUS = 8 WHERE A.ID = B.BACK_SUMMARY_ID AND B.STATUS = 10";
			batchedSql.add(sql10);
			//-- 修改退文时间 
			String sql11 = "UPDATE EDOC_SUMMARY E, GOVDOC_EXCHANGE_DETAIL_LOG L SET E.COMPLETE_TIME = L.CREATE_TIME"+ 
			" WHERE L.BACK_SUMMARY_ID = E.ID AND E.TRANSFER_STATUS = 8 AND E.COMPLETE_TIME IS NULL";
			batchedSql.add(sql11);
			
			//-- 已签收的数据修改删除状态
			String sql12 = "update ctp_affair affair,edoc_exchange_recieve recieve set affair.is_delete = 0"+
			" where affair.SUB_OBJECT_ID = recieve.id and affair.state = 4 and recieve.status in (1,2,10)";
			batchedSql.add(sql12);
			//-- 已发送的数据修改删除状态
			String sql13 = "update ctp_affair affair,edoc_exchange_send send set affair.is_delete = 0"+
			" where affair.SUB_OBJECT_ID = send.id and affair.state = 4 and send.status = 1";
			batchedSql.add(sql13);
			
			JDBCAgent jdbc = new JDBCAgent();
			try {
				jdbc.executeBatch(batchedSql);
			} catch (BusinessException e) {
				LOG.error("升级公文交换数据报错1");
			} catch (SQLException e) {
				LOG.error("升级公文交换数据报错2");
			}
			
		}else if("Oracle".equalsIgnoreCase(getdbType)){
			List<String>  batchedSql = new ArrayList<String>();
			//-- 公文交换列表兼容老公文已分送数据
			String sql1 = "UPDATE EDOC_SUMMARY S SET S.EXCHANGE_SEND_AFFAIRID = ( SELECT A.ID FROM CTP_AFFAIR A, EDOC_EXCHANGE_SEND EX WHERE S.ID = EX.ID AND EX.STATUS = 1"+
			" AND A.SUB_OBJECT_ID = EX.ID AND A.OBJECT_ID = EX.EDOC_ID AND A.APP = 4 AND A.SUB_APP = 22 AND A.STATE = 4 AND EX.SEND_USER_ID = A.MEMBER_ID )"+
			" WHERE S.EDOC_TYPE = 99 AND S.EXCHANGE_SEND_AFFAIRID IS NULL AND EXISTS ( SELECT A.ID FROM CTP_AFFAIR A, EDOC_EXCHANGE_SEND EX WHERE S.ID = EX.ID AND EX.STATUS = 1"+
			" AND A.SUB_OBJECT_ID = EX.ID AND A.OBJECT_ID = EX.EDOC_ID AND A.APP = 4 AND A.SUB_APP = 22 AND A.STATE = 4 AND EX.SEND_USER_ID = A.MEMBER_ID)";
			batchedSql.add(sql1);
			//-- 老公文交换已发送、已回退数据同步到待办状态
			String sql2 = "UPDATE CTP_AFFAIR A SET A.STATE = 4 WHERE A.APP = 4 AND A.SUB_APP = 22 AND A.IS_DELETE = 0 AND A.STATE = 3"+
			" AND A.SUB_OBJECT_ID = (SELECT EX.ID FROM EDOC_EXCHANGE_SEND EX WHERE A.SUB_OBJECT_ID = EX.ID AND EX.STATUS IN (1,2))"+
			" AND EXISTS (SELECT EX.ID FROM EDOC_EXCHANGE_SEND EX WHERE A.SUB_OBJECT_ID = EX.ID AND EX.STATUS IN (1,2))";
			batchedSql.add(sql2);
			//-- 老公文交换已签收、已登记、已回退数据同步到待办状态
			String sql3 = "UPDATE CTP_AFFAIR A SET A.STATE = 4 WHERE A.APP = 4 AND A.SUB_APP = 23 AND A.IS_DELETE = 0 AND A.STATE = 3"+
			" AND A.SUB_OBJECT_ID = (SELECT EX.ID FROM EDOC_EXCHANGE_RECIEVE EX WHERE A.SUB_OBJECT_ID = EX.ID AND EX.STATUS IN (1,2,3))"+
			" AND EXISTS (SELECT EX.ID FROM EDOC_EXCHANGE_RECIEVE EX WHERE A.SUB_OBJECT_ID = EX.ID AND EX.STATUS IN (1,2,3))";
			batchedSql.add(sql3);
			//-- 老公文交换已登记数据同步到待办状态
			String sql4 ="UPDATE CTP_AFFAIR A SET A.STATE = 4 WHERE A.APP = 4 AND A.SUB_APP = 24 AND A.IS_DELETE = 0 AND A.STATE = 3"+
			" AND A.SUB_OBJECT_ID = (SELECT EX.ID FROM EDOC_EXCHANGE_RECIEVE EX WHERE A.SUB_OBJECT_ID = EX.ID AND EX.STATUS = 2)"+
			" AND EXISTS (SELECT EX.ID FROM EDOC_EXCHANGE_RECIEVE EX WHERE A.SUB_OBJECT_ID = EX.ID AND EX.STATUS = 2)";
			batchedSql.add(sql4);
			//-- 老公文交换已登记数据同步到待办状态
			String sql5 = "UPDATE CTP_AFFAIR A SET A.STATE = 4 WHERE A.APP = 4 AND A.SUB_APP = 24 AND A.IS_DELETE = 0 AND A.STATE = 3"+
			" AND A.OBJECT_ID = (SELECT ER.ID FROM EDOC_REGISTER ER WHERE A.OBJECT_ID = ER.ID AND ER.STATE = 2)"+
			" AND EXISTS (SELECT ER.ID FROM EDOC_REGISTER ER WHERE A.OBJECT_ID = ER.ID AND ER.STATE = 2)";
			batchedSql.add(sql5);
			//-- 老公文交换已分发数据同步到待办状态
			String sql6 = "UPDATE CTP_AFFAIR A SET A.STATE = 4 WHERE A.APP = 4 AND A.SUB_APP = 34 AND A.IS_DELETE = 0 AND A.STATE = 3"+
			" AND A.OBJECT_ID = (SELECT ER.ID FROM EDOC_REGISTER ER WHERE A.OBJECT_ID = ER.ID AND ER.DISTRIBUTE_STATE = 2)"+
			" AND EXISTS (SELECT ER.ID FROM EDOC_REGISTER ER WHERE A.OBJECT_ID = ER.ID AND ER.DISTRIBUTE_STATE = 2)";
			batchedSql.add(sql6);
			
			//-- 转发文数据升级
			String sql7 = "INSERT INTO GOVDOC_EXCHANGE_MAIN ( ID, SUBJECT, CREATE_TIME, SUMMARY_ID, EXCHANGE_TYPE, REFERENCE_ID)"+
			" SELECT ID, SUBJECT, TURN_DATE, SUMMARY_ID, 3, REFERENCE FROM GOVDOC_RELATION WHERE GOVDOC_TYPE = 1";
			batchedSql.add(sql7);
			//-- 升级EDOC_SUMMARY字段EXCHANGE_TYPE
			String sql8 = "UPDATE EDOC_SUMMARY A SET A.EXCHANGE_TYPE = (SELECT EXCHANGE_TYPE FROM GOVDOC_EXCHANGE_MAIN B,GOVDOC_EXCHANGE_DETAIL C WHERE C.MAIN_ID = B.ID AND C.SUMMARY_ID = A.ID)";
			batchedSql.add(sql8);
			//-- 设置交换类型默认值
			String sql9 = "UPDATE EDOC_SUMMARY SET EXCHANGE_TYPE = 0 WHERE EXCHANGE_TYPE IS NULL";
			batchedSql.add(sql9);
			//-- 老公文回退流转状态维护
			String sql10 = "UPDATE EDOC_SUMMARY A SET A.TRANSFER_STATUS = 8 WHERE A.ID IN (SELECT BACK_SUMMARY_ID FROM GOVDOC_EXCHANGE_DETAIL_LOG WHERE STATUS = 10)";
			batchedSql.add(sql10);
			//-- 修改退文时间
			String sql11 = "UPDATE EDOC_SUMMARY E SET COMPLETE_TIME = (SELECT MAX(CREATE_TIME) FROM GOVDOC_EXCHANGE_DETAIL_LOG L WHERE L.BACK_SUMMARY_ID = E.ID)"+
			" WHERE E.TRANSFER_STATUS = 8 AND E.COMPLETE_TIME IS NULL";
			batchedSql.add(sql11);
			
			//-- 已签收的数据修改删除状态
			String sql12 = "update ctp_affair affair set affair.is_delete = 0 where exists (select recieve.id from edoc_exchange_recieve recieve"+
			" where affair.SUB_OBJECT_ID = recieve.id and affair.state = 4 and recieve.status in (1,2,10))";
			batchedSql.add(sql12);
			//-- 已发送的数据修改删除状态
			String sql13 ="update ctp_affair affair set affair.is_delete = 0 where exists (select send.id from edoc_exchange_send send"+
			" where affair.SUB_OBJECT_ID = send.id and affair.state = 4 and send.status = 1)";
			batchedSql.add(sql13);
			
			JDBCAgent jdbc = new JDBCAgent();
			try {
				jdbc.executeBatch(batchedSql);
			} catch (BusinessException e) {
				LOG.error("升级公文交换数据报错1");
			} catch (SQLException e) {
				LOG.error("升级公文交换数据报错2");
			}
			
		}else if("postgresql".equalsIgnoreCase(getdbType)){
			List<String>  batchedSql = new ArrayList<String>();
			//-- 公文交换列表兼容老公文已分送数据
			String sql1 = "UPDATE EDOC_SUMMARY S SET EXCHANGE_SEND_AFFAIRID = ( SELECT A.ID FROM CTP_AFFAIR A, EDOC_EXCHANGE_SEND EX WHERE S.ID = EX.ID AND EX.STATUS = 1"+
			" AND A.SUB_OBJECT_ID = EX.ID AND A.OBJECT_ID = EX.EDOC_ID AND A.APP = 4 AND A.SUB_APP = 22 AND A.STATE = 4 AND EX.SEND_USER_ID = A.MEMBER_ID )"+
			" WHERE S.EDOC_TYPE = 99 AND S.EXCHANGE_SEND_AFFAIRID IS NULL AND EXISTS ( SELECT A.ID FROM CTP_AFFAIR A, EDOC_EXCHANGE_SEND EX WHERE S.ID = EX.ID AND EX.STATUS = 1"+
			" AND A.SUB_OBJECT_ID = EX.ID AND A.OBJECT_ID = EX.EDOC_ID AND A.APP = 4 AND A.SUB_APP = 22 AND A.STATE = 4 AND EX.SEND_USER_ID = A.MEMBER_ID)";
			batchedSql.add(sql1);
			//-- 老公文交换已发送、已回退数据同步到待办状态
			String sql2 = "UPDATE CTP_AFFAIR A SET STATE = 4 WHERE A.APP = 4 AND A.SUB_APP = 22 AND A.IS_DELETE = 0 AND A.STATE = 3"+
			" AND A.SUB_OBJECT_ID = (SELECT EX.ID FROM EDOC_EXCHANGE_SEND EX WHERE A.SUB_OBJECT_ID = EX.ID AND EX.STATUS IN (1,2))"+
			" AND EXISTS (SELECT EX.ID FROM EDOC_EXCHANGE_SEND EX WHERE A.SUB_OBJECT_ID = EX.ID AND EX.STATUS IN (1,2))";
			batchedSql.add(sql2);
			//-- 老公文交换已签收、已登记、已回退数据同步到待办状态
			String sql3 = "UPDATE CTP_AFFAIR A SET STATE = 4 WHERE A.APP = 4 AND A.SUB_APP = 23 AND A.IS_DELETE = 0 AND A.STATE = 3"+
			" AND A.SUB_OBJECT_ID = (SELECT EX.ID FROM EDOC_EXCHANGE_RECIEVE EX WHERE A.SUB_OBJECT_ID = EX.ID AND EX.STATUS IN (1,2,3))"+
			" AND EXISTS (SELECT EX.ID FROM EDOC_EXCHANGE_RECIEVE EX WHERE A.SUB_OBJECT_ID = EX.ID AND EX.STATUS IN (1,2,3))";
			batchedSql.add(sql3);
			//-- 老公文交换已登记数据同步到待办状态
			String sql4 ="UPDATE CTP_AFFAIR A SET STATE = 4 WHERE A.APP = 4 AND A.SUB_APP = 24 AND A.IS_DELETE = 0 AND A.STATE = 3"+
			" AND A.SUB_OBJECT_ID = (SELECT EX.ID FROM EDOC_EXCHANGE_RECIEVE EX WHERE A.SUB_OBJECT_ID = EX.ID AND EX.STATUS = 2)"+
			" AND EXISTS (SELECT EX.ID FROM EDOC_EXCHANGE_RECIEVE EX WHERE A.SUB_OBJECT_ID = EX.ID AND EX.STATUS = 2)";
			batchedSql.add(sql4);
			//-- 老公文交换已登记数据同步到待办状态
			String sql5 = "UPDATE CTP_AFFAIR A SET STATE = 4 WHERE A.APP = 4 AND A.SUB_APP = 24 AND A.IS_DELETE = 0 AND A.STATE = 3"+
			" AND A.OBJECT_ID = (SELECT ER.ID FROM EDOC_REGISTER ER WHERE A.OBJECT_ID = ER.ID AND ER.STATE = 2)"+
			" AND EXISTS (SELECT ER.ID FROM EDOC_REGISTER ER WHERE A.OBJECT_ID = ER.ID AND ER.STATE = 2)";
			batchedSql.add(sql5);
			//-- 老公文交换已分发数据同步到待办状态
			String sql6 = "UPDATE CTP_AFFAIR A SET STATE = 4 WHERE A.APP = 4 AND A.SUB_APP = 34 AND A.IS_DELETE = 0 AND A.STATE = 3"+
			" AND A.OBJECT_ID = (SELECT ER.ID FROM EDOC_REGISTER ER WHERE A.OBJECT_ID = ER.ID AND ER.DISTRIBUTE_STATE = 2)"+
			" AND EXISTS (SELECT ER.ID FROM EDOC_REGISTER ER WHERE A.OBJECT_ID = ER.ID AND ER.DISTRIBUTE_STATE = 2)";
			batchedSql.add(sql6);
			
			//-- 转发文数据升级
			String sql7 = "INSERT INTO GOVDOC_EXCHANGE_MAIN ( ID, SUBJECT, CREATE_TIME, SUMMARY_ID, EXCHANGE_TYPE, REFERENCE_ID)"+
			" SELECT ID, SUBJECT, TURN_DATE, SUMMARY_ID, 3, REFERENCE FROM GOVDOC_RELATION WHERE GOVDOC_TYPE = 1";
			batchedSql.add(sql7);
			//-- 升级EDOC_SUMMARY字段EXCHANGE_TYPE
			String sql8 = "UPDATE EDOC_SUMMARY A SET EXCHANGE_TYPE = (SELECT EXCHANGE_TYPE FROM GOVDOC_EXCHANGE_MAIN B,GOVDOC_EXCHANGE_DETAIL C WHERE C.MAIN_ID = B.ID AND C.SUMMARY_ID = A.ID)";
			batchedSql.add(sql8);
			//-- 设置交换类型默认值
			String sql9 = "UPDATE EDOC_SUMMARY SET EXCHANGE_TYPE = 0 WHERE EXCHANGE_TYPE IS NULL";
			batchedSql.add(sql9);
			//-- 老公文回退流转状态维护
			String sql10 = "UPDATE EDOC_SUMMARY A SET TRANSFER_STATUS = 8 WHERE A.ID IN (SELECT BACK_SUMMARY_ID FROM GOVDOC_EXCHANGE_DETAIL_LOG WHERE STATUS = 10)";
			batchedSql.add(sql10);
			//-- 修改退文时间
			String sql11 = "UPDATE EDOC_SUMMARY E SET COMPLETE_TIME = (SELECT MAX(CREATE_TIME) FROM GOVDOC_EXCHANGE_DETAIL_LOG L WHERE L.BACK_SUMMARY_ID = E.ID)"+
			" WHERE E.TRANSFER_STATUS = 8 AND E.COMPLETE_TIME IS NULL";
			batchedSql.add(sql11);
			
			//-- 已签收的数据修改删除状态
			String sql12 = "update ctp_affair affair set is_delete = 0 where exists (select recieve.id from edoc_exchange_recieve recieve"+
			" where affair.SUB_OBJECT_ID = recieve.id and affair.state = 4 and recieve.status in (1,2,10))";
			batchedSql.add(sql12);
			//-- 已发送的数据修改删除状态
			String sql13 ="update ctp_affair affair set is_delete = 0 where exists (select send.id from edoc_exchange_send send"+
			" where affair.SUB_OBJECT_ID = send.id and affair.state = 4 and send.status = 1)";
			batchedSql.add(sql13);
			
			JDBCAgent jdbc = new JDBCAgent();
			try {
				jdbc.executeBatch(batchedSql);
			} catch (BusinessException e) {
				LOG.error("升级公文交换数据报错1");
			} catch (SQLException e) {
				LOG.error("升级公文交换数据报错2");
			}
			
		}
		else if("Microsoft SQL Server".equalsIgnoreCase(getdbType)){
			List<String>  batchedSql = new ArrayList<String>();
			//-- 公文交换列表兼容老公文已分送数据
			String sql1 = "UPDATE S SET S.EXCHANGE_SEND_AFFAIRID = A.ID FROM EDOC_SUMMARY S INNER JOIN EDOC_EXCHANGE_SEND EX ON S.ID = EX.ID AND EX.STATUS = 1"+
			" INNER JOIN CTP_AFFAIR A ON A.SUB_OBJECT_ID = EX.ID AND A.OBJECT_ID = EX.EDOC_ID WHERE A.APP = 4 AND A.SUB_APP = 22 AND A.STATE = 4"+
			" AND S.EDOC_TYPE = 99 AND EX.SEND_USER_ID = A.MEMBER_ID";
			batchedSql.add(sql1);
			//-- 老公文交换已发送、已回退数据同步到待办状态
			String sql2= "UPDATE A SET A.STATE = 4 FROM CTP_AFFAIR A INNER JOIN EDOC_EXCHANGE_SEND EX ON A.SUB_OBJECT_ID = EX.ID AND EX.STATUS IN (1,2)"+
			" WHERE A.APP = 4 AND A.SUB_APP = 22 AND A.IS_DELETE = 0 AND A.STATE = 3";
			batchedSql.add(sql2);
			//-- 老公文交换已签收、已登记、已回退数据同步到待办状态
			String sql3 = "UPDATE A SET A.STATE = 4 FROM CTP_AFFAIR A INNER JOIN EDOC_EXCHANGE_RECIEVE EX ON A.SUB_OBJECT_ID = EX.ID AND EX.STATUS IN (1,2,3)"+
			" WHERE A.APP = 4 AND A.SUB_APP = 23 AND A.IS_DELETE = 0 AND A.STATE = 3";
			batchedSql.add(sql3);
			//-- 老公文交换已登记数据同步到待办状态
			String sql4 = "UPDATE A SET A.STATE = 4 FROM CTP_AFFAIR A INNER JOIN EDOC_EXCHANGE_RECIEVE EX ON A.SUB_OBJECT_ID = EX.ID AND EX.STATUS = 2"+
			" WHERE A.APP = 4 AND A.SUB_APP = 24 AND A.IS_DELETE = 0 AND A.STATE = 3";
			batchedSql.add(sql4);
			//-- 老公文交换已登记数据同步到待办状态
			String sql5 = "UPDATE A SET A.STATE = 4 FROM CTP_AFFAIR A INNER JOIN EDOC_REGISTER ER ON A.OBJECT_ID = ER.ID AND ER.STATE = 2"+
			" WHERE A.APP = 4 AND A.SUB_APP = 24 AND A.IS_DELETE = 0 AND A.STATE = 3";
			batchedSql.add(sql5);
			//-- 老公文交换已分发数据同步到待办状态
			String sql6 = "UPDATE A SET A.STATE = 4 FROM CTP_AFFAIR A INNER JOIN EDOC_REGISTER ER ON A.OBJECT_ID = ER.ID AND ER.DISTRIBUTE_STATE = 2"+
			" WHERE A.APP = 4 AND A.SUB_APP = 34 AND A.IS_DELETE = 0 AND A.STATE = 3";
			batchedSql.add(sql6);
			
			//-- 转发文数据升级
			String sql7 = "INSERT INTO GOVDOC_EXCHANGE_MAIN ( ID, SUBJECT, CREATE_TIME, SUMMARY_ID, EXCHANGE_TYPE, REFERENCE_ID)"+ 
			" SELECT ID, SUBJECT, TURN_DATE, SUMMARY_ID, 3, REFERENCE FROM GOVDOC_RELATION WHERE GOVDOC_TYPE = 1";
			batchedSql.add(sql7);
			//-- 升级EDOC_SUMMARY字段EXCHANGE_TYPE
			String sql8 = "UPDATE A SET A.EXCHANGE_TYPE = B.EXCHANGE_TYPE FROM EDOC_SUMMARY A, GOVDOC_EXCHANGE_MAIN B,GOVDOC_EXCHANGE_DETAIL C"+
			" WHERE C.MAIN_ID = B.ID AND C.SUMMARY_ID = A.ID";
			batchedSql.add(sql8);
			//-- 设置交换类型默认值
			String sql9 = "UPDATE EDOC_SUMMARY SET EXCHANGE_TYPE = 0 WHERE EXCHANGE_TYPE IS NULL";
			batchedSql.add(sql9);
			//-- 老公文回退流转状态维护
			String sql10 = "UPDATE A SET A.TRANSFER_STATUS=8 FROM EDOC_SUMMARY A, GOVDOC_EXCHANGE_DETAIL_LOG B WHERE A.ID = B.BACK_SUMMARY_ID AND B.STATUS = 10";
			batchedSql.add(sql10);
			//-- 修改退文时间
			String sql11 = "UPDATE E SET E.COMPLETE_TIME = L.CREATE_TIME FROM EDOC_SUMMARY E, GOVDOC_EXCHANGE_DETAIL_LOG L"+ 
			" WHERE L.BACK_SUMMARY_ID = E.ID AND E.TRANSFER_STATUS = 8 AND E.COMPLETE_TIME IS NULL";
			batchedSql.add(sql11);
			
			//-- 已签收的数据修改删除状态
			String sql12="update affair set affair.is_delete = 0 from ctp_affair affair inner join edoc_exchange_recieve recieve on affair.sub_object_id=recieve.id"+
			" where affair.state = 4 and recieve.status in (1,2,10)";
			batchedSql.add(sql12);
			//-- 已发送的数据修改删除状态
			String sql13= "update affair set affair.is_delete = 0 from ctp_affair affair"+
			" inner join edoc_exchange_send send on affair.sub_object_id=send.id"+
			" where affair.state = 4 and send.status = 1";
			batchedSql.add(sql13);
			
			JDBCAgent jdbc = new JDBCAgent();
			try {
				jdbc.executeBatch(batchedSql);
			} catch (BusinessException e) {
				LOG.error("升级公文交换数据报错1");
			} catch (SQLException e) {
				LOG.error("升级公文交换数据报错2");
			}
			
		}
	
	}

	private void updateProcessRunningData(){
		FlipInfo flipInfo = new FlipInfo(0, -1);
		flipInfo.setNeedTotal(false);
		String hql  = "select wf.id ,wf.processobject,s.edoc_type from edoc_summary s, wf_process_running   wf where s.process_id = wf.id and s.state  = :state";
		String updateSql = "update wf_process_running set processobject = ? where id = ?";
		Map<String,Object> param = new HashMap<String, Object>();
		param.put("state", EdocConstant.flowState.run.ordinal());
		JDBCAgent jdbc = new JDBCAgent();
		
		try {
			
			jdbc.findNameByPaging(hql, param, flipInfo);
			List<Map<String,Object>> result = flipInfo.getData();
			if (Strings.isNotEmpty(result)) {
				jdbc.batch1Prepare(updateSql);
				List<Object> updateDataList;
				
				for (Map<String,Object> map : result) {
					String processXml = parseBpmProcess(map.get("processobject"));
					Long processId = Long.valueOf(map.get("id").toString());
					Object edocTypeObj = map.get("edoc_type");
					
					if(null==edocTypeObj){
						continue;
					}
					Integer edocType = Integer.valueOf(edocTypeObj.toString());
					if (Strings.isNotBlank(processXml) && processXml.contains("Departmentexchange")) {

						if (edocType == 0 || edocType == 2) {
							processXml = processXml.replaceAll("Departmentexchange", "DepartmentGovdocSend")
									.replaceAll("公文收发员", "公文送文员");
						} else if (edocType == 1) {
							processXml = processXml.replaceAll("Departmentexchange", "DepartmentGovdocRec")
									.replaceAll("公文收发员", "公文收文员");
						}
						updateDataList = new ArrayList<Object>();
						updateDataList.add(transBpmObjectToByte(processXml));
						updateDataList.add(processId);
						jdbc.batch2Add(updateDataList);
					}
				}

				jdbc.batch3Execute();
			} 
		} catch (Exception e) {
			LOG.error("", e);
		}finally {
			jdbc.close();
		}
		 
		
	}
	
	private byte[] transBpmObjectToByte(String xml){
		ByteArrayOutputStream byt = null;
		ObjectOutputStream oos = null;
		try {
			byt = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(byt);
			oos.writeObject(xml);					
		} catch(IOException e) {
			LOG.error("", e);
		} finally {
			if(oos != null){
                try {
                	oos.close();
                } catch (IOException e) {
                	LOG.error("", e);
                }
            }
			if(byt != null){
                try {
                	byt.close();
                } catch (IOException e) {
                	LOG.error("", e);
                }
            }
		}
		byte[] bytes = byt.toByteArray();
		return bytes;
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String parseBpmProcess(Object caseobject) {
		byte[] byteArray = null;
		String dbms = JDBCAgent.getDBType();
		Object obj = null;
		InputStream is = null;
        ObjectInputStream ois = null;
        try {
			if ("MySQL".equalsIgnoreCase(dbms)) {
				byteArray = (byte[])caseobject;
				if (null != byteArray) {
					is = new ByteArrayInputStream(byteArray);
					ois = new ObjectInputStream(is);
	                obj = ois.readObject();
				}
			} else if ("Microsoft SQL Server".equalsIgnoreCase(dbms) || "sqlserver".equalsIgnoreCase(dbms)) {
				
				try{
					byteArray = (byte[])caseobject;
					if (null != byteArray) {
						is = new ByteArrayInputStream(byteArray);
						ois = new ObjectInputStream(is);
		                obj = ois.readObject();
					}
                }catch(Throwable e){
                    LOG.warn("blob大字段解析失败，兼容方式解析", e);
                    java.sql.Blob blob = (java.sql.Blob)caseobject;
                    is = blob.getBinaryStream();
                    ois = new ObjectInputStream(is);
                    obj = ois.readObject();
                }
				
				
				
			}else if("Oracle".equalsIgnoreCase(dbms)){
				
				java.sql.Blob blob = (java.sql.Blob)caseobject;
				is = blob.getBinaryStream();
				ois = new ObjectInputStream(is);
				obj = ois.readObject();
			}
		} catch (Throwable e) {
        	LOG.error(e.getMessage(), e);
        } finally {
            if(is != null){
                try {
                	is.close();
                } catch (IOException e) {
                	LOG.error("", e);
                }
            }
            if(ois != null){
                try {
                	ois.close();
                } catch (IOException e) {
                	LOG.error("", e);
                }
            }
        }
		return (String) obj;
	}
	
	
	
	private String getdbType(){
		String dbms = JDBCAgent.getDBType();
		return dbms;
	}
	
	public AffairManager getAffairManager() {
		return affairManager;
	}
	
	
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	
	private void copyRegisterEdocSummaryAndAffair(){
		String hql  = "from EdocRegister where state in (:state)";
		FlipInfo fileinfo = new FlipInfo();
		Map<String,Object> param = new HashMap<String, Object>();
		List<Integer> states = new ArrayList<Integer>();
		states.add(0);
		states.add(1);
		states.add(2);
		param.put("state", states);
		int page = 1;
		fileinfo.setSize(500);
		fileinfo.setPage(page);
		int rowsCount = 0;
		List<CtpAffair> affairs = new ArrayList<CtpAffair>();
		List<EdocSummary> summarys = new ArrayList<EdocSummary>();
		List<EdocRegister> edocSendRecords= (List<EdocRegister> )edocUpgradeDao.getDataByHql(hql,param ,fileinfo);
		rowsCount = fileinfo.getTotal();
		do{
			LOG.info("正在操作<EdocRegister>第" + ((fileinfo.getPage() - 1) * fileinfo.getSize() + 1) + "条数据");
			for(EdocRegister record : edocSendRecords){
				//复制部分信息到summary中
				EdocSummary edocSummary = new EdocSummary();
				edocSummary.setId(record.getId());
				edocSummary.setSubject(record.getSubject());
				edocSummary.setEdocType(99);
				edocSummary.setDeadline(-1L);
				edocSummary.setHasArchive(false);
				edocSummary.setGovdocType(0);
				int state = record.getState();
				edocSummary.setState(7);//ColConstant.flowState.oldexchange.ordinal()
				edocSummary.setDocMark(record.getDocMark());
				edocSummary.setSecretLevel(record.getSecretLevel());
				edocSummary.setUrgentLevel(record.getUrgentLevel());
				edocSummary.setCreateTime(record.getCreateTime());
				edocSummary.setStartTime(record.getCreateTime());
				edocSummary.setCreatePerson(record.getRegisterUserName());
				edocSummary.setStartUserId(record.getRegisterUserId());
				edocSummary.setOrgAccountId(record.getSendUnitId());
				//将 edoc_id  写入 edoc_summary中的caseid  保存 没实际意义，只是为了前台 打开以前界面参数
				//量receive_id  写入 edoc_summary中的processid保存 没实际意义，只是为了前台 打开以前界面参数
				edocSummary.setCaseId(record.getEdocId());
				edocSummary.setProcessId(record.getRecieveId()+"");
				//复制部分信息到affair中   待登记的 不需要在affair中添加数据
				if(0 == state || 2 == state){
					CtpAffair affair = new CtpAffair();
					affair.setId(UUIDLong.longUUID());
					if(0 == state){
						affair.setState(3);
					}else if(2 == state){
						affair.setState(4);
						affair.setCompleteTime(new java.sql.Timestamp(record.getRegisterDate().getTime()));
						edocSummary.setCompleteTime(new java.sql.Timestamp(record.getRegisterDate().getTime()));
					}
					affair.setSubject(record.getSubject());
					affair.setTrack(0);
					affair.setObjectId(record.getId());
					affair.setSubObjectId(record.getRecieveId());
					affair.setApp(4);//
					affair.setCreateDate(record.getCreateTime());
					affair.setMemberId(record.getDistributerId());
					affair.setSenderId(record.getDistributerId());
					if(StringUtils.isNotBlank(record.getUrgentLevel())){
						affair.setImportantLevel(Integer.valueOf(record.getUrgentLevel()));
					}
					affairs.add(affair);
				}
				summarys.add(edocSummary);
			}
			edocUpgradeDao.saveOrUpdateAll(summarys);
			edocUpgradeDao.saveAll(affairs);
			summarys.clear();
			affairs.clear();
			if(rowsCount < page*500){
				break;
			}
			fileinfo.setPage(++page);
			fileinfo.setNeedTotal(false);
			edocSendRecords= (List<EdocRegister> )edocUpgradeDao.getDataByHql(hql,param ,fileinfo);
		}
		while(fileinfo.getSize() > 0);
	}
	private void copyExchangeRecToEdocSummary(){
		//该处只将待签收/已签收/已登记/登记待发的签收数据生成edoc_summary
		String hql  = "from EdocRecieveRecord where status in (0,1,2,10)";//待签收0/已签收1/已登记2/登记保存待发10/回退或取回3/被退回到退件箱4/已签收-被删除5/待签收-被删除6
		FlipInfo fileinfo = new FlipInfo();
		fileinfo.setSize(500);
		int page = 1;
		fileinfo.setPage(page);
		int rowsCount = 0;
		List<EdocSummary> summarys = new ArrayList<EdocSummary>();
		List<CtpAffair> affairs = new ArrayList<CtpAffair>();
		List<EdocRecieveRecord> edocSendRecords= (List<EdocRecieveRecord> )edocUpgradeDao.getDataByHql(hql,new HashMap<String, Object>() ,fileinfo);
		rowsCount = fileinfo.getTotal();
		do{
			LOG.info("正在操作<EdocRecieveRecord>第" + ((fileinfo.getPage() - 1) * fileinfo.getSize() + 1) + "条数据");
			for(EdocRecieveRecord rec : edocSendRecords){
				EdocSummary edocSummary = new EdocSummary();
				edocSummary.setId(rec.getId());
				edocSummary.setSubject(rec.getSubject());
				edocSummary.setEdocType(99);
				edocSummary.setDeadline(-1L);
				edocSummary.setHasArchive(false);
				edocSummary.setGovdocType(0);
				//edocSummary.setState(rec.getStatus());
				edocSummary.setState(7);//ColConstant.flowState.oldexchange.ordinal()
				edocSummary.setDocMark(rec.getDocMark());
				edocSummary.setSecretLevel(rec.getSecretLevel());
				edocSummary.setUrgentLevel(rec.getUrgentLevel());
				edocSummary.setCreateTime(rec.getCreateTime());
				edocSummary.setCreatePerson(rec.getSender());
				edocSummary.setStartUserId(rec.getRecUserId());
				edocSummary.setOrgAccountId(rec.getExchangeOrgId());
				edocSummary.setStartTime(rec.getRecTime());
				summarys.add(edocSummary);
				try {
					List<CtpAffair>  aff = affairManager.getAffairsByAppAndObjectId(ApplicationCategoryEnum.exSign, rec.getEdocId());
					if(aff != null && !aff.isEmpty()){
						for(CtpAffair affair : aff){
							if(affair.getMemberId().longValue() == rec.getRecUserId()){
								affair.setCompleteTime(rec.getRecTime());
								affairs.add(affair);
								//affairManager.updateAffair(affair);
							}
						}
					}
					
				} catch (BusinessException e) {
				}
			}
			edocUpgradeDao.saveOrUpdateAll(summarys);
			edocUpgradeDao.updateAll(affairs);
			summarys.clear();
			if(rowsCount < page*500){
				break;
			}
			fileinfo.setPage(++page);
			fileinfo.setNeedTotal(false);
			edocSendRecords= (List<EdocRecieveRecord> )edocUpgradeDao.getDataByHql(hql,new HashMap<String, Object>() ,fileinfo);
		}while(fileinfo.getSize() > 0);
	}
	
	private void copyExchangeSendToEdocSummary(){
		String hql  = "from EdocSendRecord where status in (0,1,3,4)";//待发送0/已发送1/已回退2/退回后生成新数据3/撤销后生成的新数据4/已发送删除5/待发送删除6
		FlipInfo fileinfo = new FlipInfo();
		fileinfo.setSize(500);
		int page = 1;
		fileinfo.setPage(page);
		int rowsCount = 0;
		List<EdocSummary> summarys = new ArrayList<EdocSummary>();
		List<CtpAffair> affairs = new ArrayList<CtpAffair>();
		List<EdocSendRecord> edocSendRecords= (List<EdocSendRecord> )edocUpgradeDao.getDataByHql(hql,new HashMap<String, Object>() ,fileinfo);
		rowsCount = fileinfo.getTotal();
		Map<Long,EdocSendRecord> m = new HashMap<Long,EdocSendRecord>();
		List ids = new ArrayList();
		Map<Long, String> summaryMap = new HashMap<Long, String>();
		do{
			LOG.info("正在操作<EdocSendRecord>第" + ((fileinfo.getPage() - 1) * fileinfo.getSize() + 1) + "条数据");
			for(EdocSendRecord record : edocSendRecords){
				EdocSummary edocSummary = new EdocSummary();
				edocSummary.setId(record.getId());
				edocSummary.setSubject(record.getSubject());
				edocSummary.setEdocType(99);
				edocSummary.setDeadline(-1L);
				edocSummary.setHasArchive(false);
				edocSummary.setGovdocType(0);
				edocSummary.setState(7);//ColConstant.flowState.oldexchange.ordinal()
				edocSummary.setDocMark(record.getDocMark());
				edocSummary.setSecretLevel(record.getSecretLevel());
				edocSummary.setUrgentLevel(record.getUrgentLevel());
				edocSummary.setCreateTime(record.getCreateTime());
				Long sourceId = record.getEdocId();
				if(sourceId != null) {
					if(!summaryMap.containsKey(sourceId)) {
						try {
							EdocSummary sourceSummary = edocUpgradeDao.getSummarySerialNo(sourceId);
							summaryMap.put(sourceSummary.getId(), sourceSummary.getSerialNo());
						} catch(Exception e) {}
					}
					if(Strings.isNotBlank(summaryMap.get(sourceId))) {
						edocSummary.setSerialNo(summaryMap.get(sourceId));
					}
				}
				try {
					V3xOrgMember mem = orgManager.getMemberById(record.getSendUserId());
					if(null != mem){
						edocSummary.setCreatePerson(mem.getName());
					}
				} catch (BusinessException e) {
				}
				edocSummary.setStartUserId(record.getSendUserId());
				edocSummary.setOrgAccountId(record.getExchangeOrgId());
				edocSummary.setStartTime(record.getSendTime());
//				try {
//					CtpAffair  affair = affairManager.getAffairBySubObjectId(record.getId());
//					if(affair != null){
//						edocSummary.setExchangeSendAffairId(affair.getId());
//					}
//					
//				} catch (BusinessException e) {
//				}
				
				summarys.add(edocSummary);
				ids.add(record.getId());
				m.put(record.getId(), record);
			}
			affairs = edocUpgradeDao.getAffairBySubObjectIds(ids);
			for(CtpAffair a:affairs){
				EdocSendRecord record = m.get(a.getSubObjectId());
				if(record!=null){
					a.setCompleteTime(record.getSendTime());
				}
			}
			edocUpgradeDao.updateAll(affairs);
			edocUpgradeDao.saveOrUpdateAll(summarys);
			summarys.clear();
			m.clear();
			ids.clear();
			if(rowsCount < page*500){
				break;
			}
			fileinfo.setPage(++page);
			fileinfo.setNeedTotal(false);
			edocSendRecords= (List<EdocSendRecord> )edocUpgradeDao.getDataByHql(hql,new HashMap<String, Object>() ,fileinfo);
		}
		while(fileinfo.getSize() > 0);
	}
	
	
	@Override
	public List<String> getTableName(String tables) throws Exception{
		List<String> tableNames = new ArrayList<String>();
		String mysql = "SELECT DISTINCT TABLE_NAME from information_schema.tables where  table_name like '"+tables+"%' and TABLE_SCHEMA=(select DATABASE())";
		String oracle = "select Table_Name from dba_tables where OWNER = (select user from dual) AND table_name like '"+tables+"%'";
		String sqlserver = "SELECT DISTINCT NAME from sys.tables where name like '"+tables+"%' AND type='U'";
		JDBCAgent agent = null;
		try{
			agent = new JDBCAgent(true);
			String dbType = agent.getDBType().toLowerCase();
			if(dbType.indexOf("oracle") > -1 && null != oracle){
				agent.execute(oracle);
			}else if (dbType.indexOf("mysql") > -1 && null != mysql){
				agent.execute(mysql);
			}else if(dbType.indexOf("sqlserver") > -1 && null != sqlserver){
				agent.execute(sqlserver);
			}else{//兼容 达梦 等其他数据库  据说和sqlserver相同
				agent.execute(sqlserver);
			}
			ResultSet res = agent.getQueryResult();
			while(res.next()){
				tableNames.add(res.getString(1));
			}
		} finally{
			if(agent != null){	
				try{
					agent.close();
				}catch (Exception e){
					LOG.error(e);
				}
			}
		}
		
		
		return tableNames;
	}
	
	private void executeFormmainUpLevel(){
		//获取所有formmain表名
		String dbType = edocUpgradeDao.getDbType();
		List<String> tableNames = new ArrayList<String>();
		try {
			if(dbType.indexOf("oracle") > -1){
				tableNames = edocUpgradeDao.getTableName("FORMMAIN_");
			}else{
				tableNames = edocUpgradeDao.getTableName("formmain_");
			}
			
		} catch (Exception e) {
			LOG.info("查询数据库中表 ================="+e.getMessage());
		}
		//执行升级
		if(tableNames != null && tableNames.size() > 0){
			for(int i=0; i<tableNames.size(); i++){
				try {
					if(dbType.indexOf("oracle") > -1){
						edocUpgradeDao.excuteSql("alter table "+tableNames.get(i)+" add receive_unit VARCHAR2(255)");
					}else if (dbType.indexOf("mysql") > -1){
						edocUpgradeDao.excuteSql("alter table "+tableNames.get(i)+" add COLUMN receive_unit VARCHAR(255)");
					}else if(dbType.indexOf("sqlserver") > -1){
						edocUpgradeDao.excuteSql("alter table "+tableNames.get(i)+" add  receive_unit NVARCHAR(255)");
					}else{//兼容 达梦 等其他数据库  据说和sqlserver相同
						edocUpgradeDao.excuteSql("alter table "+tableNames.get(i)+" add  receive_unit NVARCHAR(255)");
					}
				} catch (Exception e) {
					LOG.error(" 升级出错 如果错误信息是字段重复，那么该错误忽略  "+e.getMessage());
				}
			}
		}
	}
	
	/**
	 * 保存老数据和新数据的对应关系
	 * @throws SQLException 
	 * @throws BusinessException 
	 */
	private void saveDataRelation(){
		Map<Long, Long> tempMap = new HashMap<Long, Long>();
		int i = 0;
		for (Long oldId : formIds.keySet()) {
			i++;
			tempMap.put(oldId, formIds.get(oldId));
			if (i % 100 == 0 || i == formIds.size()) {
				try {
					edocUpgradeDao.saveDataRelation(tempMap, "form");
				} catch (BusinessException e) {
					LOG.error("保存新老数据关系发生异常！(升级表单数据)");
					LOG.error(tempMap);
					LOG.error(e.getMessage(),e);
				} catch (SQLException e) {
					LOG.error("保存新老数据关系发生异常！(升级表单数据)");
					LOG.error(tempMap);
					LOG.error(e.getMessage(),e);
				}
				tempMap = new HashMap<Long, Long>();
			}
		}
		List<Long> tempList = new ArrayList<Long>();
		i = 0;
		for (Long oldId : presetForms) {
			i++;
			tempList.add(oldId);
			if (i % 100 == 0 || i == presetForms.size()) {
				try {
					edocUpgradeDao.saveDataRelation2(tempList, "presetForm");
				} catch (BusinessException e) {
					LOG.error("保存新老数据关系发生异常！(预置表单数据)");
					LOG.error(tempMap);
					LOG.error(e.getMessage(),e);
				} catch (SQLException e) {
					LOG.error("保存新老数据关系发生异常！(预置表单数据)");
					LOG.error(tempMap);
					LOG.error(e.getMessage(),e);
				}
				tempList = new ArrayList<Long>();
			}
		}
		
		
		i = 0;
		for (Long oldId : categories.keySet()) {
			i++;
			tempMap.put(oldId, categories.get(oldId).getId());
			if (i % 100 == 0 || i == categories.size()) {
				try {
					edocUpgradeDao.saveDataRelation(tempMap, "category");
				} catch (BusinessException e) {
					LOG.error("保存新老数据关系发生异常！(升级分类数据)");
					LOG.error(tempMap);
					LOG.error(e.getMessage(),e);
				} catch (SQLException e) {
					LOG.error("保存新老数据关系发生异常！(升级分类数据)");
					LOG.error(tempMap);
					LOG.error(e.getMessage(),e);
				}
				tempMap = new HashMap<Long, Long>();
			}
		}
		
		
		i = 0;
		for (Long newId : edocElementIds) {
			i++;
			tempList.add(newId);
			if (i % 100 == 0 || i == edocElementIds.size()) {
				try {
					edocUpgradeDao.saveDataRelation2(tempList, "edocElement");
				} catch (BusinessException e) {
					LOG.error("保存新老数据关系发生异常！(新增公文元素数据)");
					LOG.error(tempMap);
					LOG.error(e.getMessage(),e);
				} catch (SQLException e) {
					LOG.error("保存新老数据关系发生异常！(新增公文元素数据)");
					LOG.error(tempMap);
					LOG.error(e.getMessage(),e);
				}
				tempList = new ArrayList<Long>();
			}
		}
		
		i = 0;
		for (Long newId : saveRoleIds) {
			i++;
			tempList.add(newId);
			if (i % 100 == 0 || i == saveRoleIds.size()) {
				try {
					edocUpgradeDao.saveDataRelation2(tempList, "role");
				} catch (BusinessException e) {
					LOG.error("保存新老数据关系发生异常！(角色数据)");
					LOG.error(tempMap);
					LOG.error(e.getMessage(),e);
				} catch (SQLException e) {
					LOG.error("保存新老数据关系发生异常！(角色数据)");
					LOG.error(tempMap);
					LOG.error(e.getMessage(),e);
				}
				tempList = new ArrayList<Long>();
			}
		}
	}
	
	
	public Map<String, List<PrivTreeNodeBO>> getTreeNodes(String roleId,
			 String appResCategory,String isAllocated, 
			List<PrivTreeNodeBO> treeNodes4Back0, List<PrivTreeNodeBO> treeNodes4Front0)
			throws BusinessException {
		List<PrivTreeNodeBO> treeNodes4Back = new ArrayList<PrivTreeNodeBO>();
		List<PrivTreeNodeBO> treeNodes4Front = new ArrayList<PrivTreeNodeBO>();
		Map<String, List<PrivTreeNodeBO>> hasMap = new ConcurrentHashMap<String, List<PrivTreeNodeBO>>();
		User user = AppContext.getCurrentUser();
		// 当前登录人员拥有的所有菜单列表
		Map<Long, PrivMenuBO> menus = new HashMap<Long, PrivMenuBO>();
		// 不可修改的角色资源关系列表
		Map<Long, PrivRoleMenu> roleResUnEditable = null;
		// 如果人员ID和单位ID不为空则为查看所拥有的资源

		Long[] roleIds = new Long[1];
		if (roleId != null) {
			roleIds[0] = Long.parseLong(roleId);
		}
		
		// 获得当前角色包含的菜单
		Map<Long, PrivMenuBO> menuMap = privilegeMenuManager.getByRole(roleIds);
		if (menuMap != null && menuMap.size() != 0) {
			menus = menuMap;
		}
		// 查找不能修改的角色资源关系
		roleResUnEditable =privilegeMenuManager.findUnModifiableRoleMenuByRole(Long.parseLong(roleId));
	
		if (menus != null && menus.size() != 0) {
			PrivTreeNodeBO node = null;
			PrivRoleMenu menuUnEidtable = null;
			String menuType = null;
			List<PrivTreeNodeBO> result = null;
			String sysback = MenuTypeEnums.systemback.getValue();
			String appfront = MenuTypeEnums.applicationfront.getValue();

			// 过滤停用和不可分配的菜单
			List<PrivMenuBO> dislist = privilegeMenuManager.getConfigDisableMenu();
			if (dislist != null) {
				for (PrivMenuBO privMenuBO : dislist) {
					menus.remove(privMenuBO.getId());
				}
			}

			for (PrivMenuBO privMenuBO : menus.values()) {
				result = new ArrayList<PrivTreeNodeBO>();
				// 将菜单对象转换为树节点对象
				node = new PrivTreeNodeBO(privMenuBO, null);
				// 菜单是否可编辑
				if (roleResUnEditable != null) {
					menuUnEidtable = roleResUnEditable.get(privMenuBO.getId());
					if (menuUnEidtable != null && isAllocated != null && "true".equals(isAllocated)) {
						node.setEditKey("false");
					}
				}

				// 菜单是否可勾选
				if (privMenuBO.getExt5() != null && privMenuBO.getExt5() == 0) {
					node.setEditKey("false");
				}

				result.add(node);
				// 判断当前菜单类型
				menuType = privMenuBO.getExt1();
				if (sysback.equals(menuType)) {
					treeNodes4Back.addAll(result);
				} else if (appfront.equals(menuType)) {
					treeNodes4Front.addAll(result);
				}
			}

			// 过滤由于不可分配的菜单过滤后引起的没有子菜单且没有入口资源的菜单
			if (appResCategory != null && "1".equals(appResCategory)) {
				Set<String> idStr = childNodeList(treeNodes4Front);
				List<PrivTreeNodeBO> templist = new ArrayList<PrivTreeNodeBO>();
				for (int i = 0; i < treeNodes4Front.size(); i++) {
					PrivTreeNodeBO privTreeNodeBO = treeNodes4Front.get(i);
					if (privTreeNodeBO == null || privTreeNodeBO.getIdKey() == null) {
						continue;
					}
					templist.add(privTreeNodeBO);

				}
				treeNodes4Front.clear();
				treeNodes4Front.addAll(templist);
			}
		}

		for(PrivTreeNodeBO tb : treeNodes4Back){
			if(isShow(tb)){
				treeNodes4Back0.add(tb);
			}
		}
		
		for(PrivTreeNodeBO tf : treeNodes4Front){
			if(isShow(tf)){
				treeNodes4Front0.add(tf);
			}
		}
		
		hasMap.put("treeNodes4Back", treeNodes4Back0);
		hasMap.put("treeNodes4Front", treeNodes4Front0);
		return hasMap;
	}
	
	
	private boolean isShow(PrivTreeNodeBO node){
		//屏蔽掉一些菜单
		Long menuId = node.getMenu().getId();
        //A6-S屏蔽的菜单
        Integer productId = SystemProperties.getInstance().getIntegerProperty("system.ProductId");
        if (productId != null && productId.intValue() == ProductEditionEnum.a6s.ordinal()) {
        	if(menuId.equals(-5229428449906440478L)
             || menuId.equals(-5229428449906440478L)//系统邮箱设置
             || menuId.equals(6692870513281941850L)//HR管理-组织机构设置
             || menuId.equals(7464083475998789371L)//HR管理-员工档案管理
             || menuId.equals(-2933570987085508976L)//HR管理-统计分析
             || menuId.equals(-6400113376465108785L)){//HR管理-信息项设置
        		return false;
        	}
        }

        if(ProductEditionEnum.isU8OEM()){
        	//屏蔽集成平台配置
        	if(menuId.equals(-1726650702758329325L)//系统注册
        	 || menuId.equals(-1461605504161199936L)//用户管理
        	 || menuId.equals(1619292301944899707L)//消息配置
        	 || menuId.equals(-4628815616839168895L)//待办配置
        	 || menuId.equals(-2176243471020961962L)//门户配置
        		//屏蔽移动office授权
        	 || menuId.equals(7811846044579348610L)//移动office授权
        		//屏蔽应用中心设置
        	 || menuId.equals(-329292124654313901L)){//云应用中心设置
        		return false;
        	}
        }
        
        return true;
	}
	
	private Set<String> childNodeList(List<PrivTreeNodeBO> list) {
		Set<String> id2PrivTreeNodeBO = new HashSet<String>();
		for (PrivTreeNodeBO privTreeNodeBO2 : list) {
			id2PrivTreeNodeBO.add(privTreeNodeBO2.getpIdKey().split("_")[1]);
		}
		return id2PrivTreeNodeBO;
	}
	
	private void refreshRole(Long roleId, List<PrivRoleMenu> tempPrivRoleResources) throws BusinessException{
		Long[] roleIds = new Long[1];
		roleIds[0] = roleId;
		//Map<Long, PrivMenuBO> menuMap = privilegeManage.getMenuByRole(roleIds);
		PrivTreeNodeBO node = null;
        String nodeId = null;
        PrivMenu menu = null;
        PrivRoleMenu roleRes = null;
        Long resId = null;
        Long menuId = null;
        Long enterId = null;
        
        // 后台管理资源列表
 		List<PrivTreeNodeBO> treeNodes4Back = new ArrayList<PrivTreeNodeBO>();
 		// 前台应用资源列表
 		List<PrivTreeNodeBO> treeNodes4Front = new ArrayList<PrivTreeNodeBO>();
        List<PrivRoleMenu> roleReources = new ArrayList<PrivRoleMenu>();
        getTreeNodes(roleId.toString(), "1", "true", treeNodes4Back, treeNodes4Front);
        for (PrivRoleMenu PrivRoleMenu : tempPrivRoleResources) {
        	PrivTreeNodeBO privTreeNodeBO = new PrivTreeNodeBO();
        	privTreeNodeBO.setEditKey("false");
        	privTreeNodeBO.setIdKey("menu_" + PrivRoleMenu.getMenuid());
        	privTreeNodeBO.setNameKey("");
        	privTreeNodeBO.setpIdKey("menu_8060011731890556579");
        	treeNodes4Front.add(privTreeNodeBO);
		}
        // 先删除已存在的关系
        roleRes = new PrivRoleMenu();
        roleRes.setRoleid(roleId);
        roleMenuDao.deleteRoleMenu(roleRes);
        //如果是集团基准角色，批量删除单位关系
        List<V3xOrgRelationship> rellist = new ArrayList<V3xOrgRelationship>();
        if(orgManager.getAccountById(orgManager.getRoleById(roleId).getOrgAccountId()).isGroup()){
        	EnumMap<RelationshipObjectiveName, Object> enummap = new EnumMap<RelationshipObjectiveName, Object>(RelationshipObjectiveName.class);
            enummap.put(OrgConstants.RelationshipObjectiveName.objective0Id, roleId);
        	rellist = orgManager.getV3xOrgRelationship(OrgConstants.RelationshipType.Banchmark_Role, null, null, enummap);
        	for (V3xOrgRelationship v3xOrgRelationship : rellist) {
        		roleRes = new PrivRoleMenu();
                roleRes.setRoleid(v3xOrgRelationship.getSourceId());
                roleMenuDao.deleteRoleMenu(roleRes);
			}
        }
        // 新建角色资源关系
        for (int i = 0; i < treeNodes4Front.size(); i++) {
            node = (PrivTreeNodeBO) treeNodes4Front.get(i);
            nodeId = node.getIdKey();
            if (nodeId.indexOf(PrivTreeNodeBO.resourceflag) != -1) {
                roleRes = new PrivRoleMenu();
                roleRes.setNewId();
                resId = Long.parseLong(node.getIdKey().replace(PrivTreeNodeBO.resourceflag, ""));
                roleRes.setResourceid(resId);
                roleRes.setRoleid(roleId);
                menuId = Long.parseLong(node.getpIdKey().replace(PrivTreeNodeBO.menuflag, ""));
                menu = menuManager.findById(menuId);
                roleRes.setMenuid(menu.getId());
                roleReources.add(roleRes);
                
            } else if (nodeId.indexOf(PrivTreeNodeBO.menuflag) != -1) {
                // 勾选菜单时自动将入口资源勾选
                menuId = Long.parseLong(node.getIdKey().replace(PrivTreeNodeBO.menuflag, ""));
                PrivMenuBO menuBO = menuManager.findById(menuId);
                if (menuBO != null) {
                    enterId = menuBO.getEnterResourceId();
                    if (enterId != null) {
                        roleRes = new PrivRoleMenu();
                        roleRes.setNewId();
                        roleRes.setResourceid(enterId);
                        roleRes.setRoleid(roleId);
                        roleRes.setMenuid(menuId);
                        roleReources.add(roleRes);
                    }
                    //勾选菜单下不可见的不可分配资源
//                    PrivMenuBO menuBO = menuManager.findById(menuId);
//
//                	if(menuBO!=null && menuBO.getId()!=null && 
//                			( !menuBO.isControl() || menuBO.isShow()==null || !menuBO.isShow()) ){
//                		roleRes = new PrivRoleMenu();
//                		roleRes.setNewId();
//                		//roleRes.setResourceid(privResource.getId());
//                		roleRes.setRoleid(roleId);
//                		roleRes.setMenuid(menuId);
//                		roleReources.add(roleRes);
//                	}
				
                }
            }
        }
        roleMenuDao.insertRoleMenuPatchAll(roleReources);
        //如果是集团基准角色，更新各单位的关系
        if(orgManager.getAccountById(orgManager.getRoleById(roleId).getOrgAccountId()).isGroup()){
            	for (V3xOrgRelationship v3xOrgRelationship : rellist) {
        		List<PrivRoleMenu> accroleReources = new ArrayList<PrivRoleMenu>();
        		//accroleReources.addAll(roleReources);
        		for (PrivRoleMenu PrivRoleMenu : roleReources) {
        			PrivRoleMenu accprivRoleResource = new PrivRoleMenu();
        			accprivRoleResource.setRoleid(v3xOrgRelationship.getSourceId());
        			accprivRoleResource.setId(UUIDLong.longUUID());
        			accprivRoleResource.setMenuid(PrivRoleMenu.getMenuid());
        			accprivRoleResource.setModifiable(PrivRoleMenu.getModifiable()); 
        			accprivRoleResource.setResourceid(PrivRoleMenu.getResourceid());
        			accroleReources.add(accprivRoleResource);
				}
        		roleMenuDao.insertRoleMenuPatchAll(accroleReources);
        	}
        }
        menuCacheManager.updateBiz();
	}
	
	private void createUnitRole() throws BusinessException, SQLException{
		LOG.info("角色权限升级开始...");
		if (ProductEditionEnum.getCurrentProductEditionEnum().getValue().equals(ProductEditionEnum.enterprise.getValue())) {
			
			List<V3xOrgRole> roles = orgManager.getAllRoles(OrgConstants.ACCOUNTID);
			PrivRoleMenu selectCondition = new PrivRoleMenu();
			List<PrivRoleMenu> privRoleMenuList = new ArrayList<PrivRoleMenu>();
			
			//循环单位所有角色，对比老公文资源，添加新公文资源。
			Set<V3xOrgEntity> edocSend = new HashSet<V3xOrgEntity>();
			Set<V3xOrgEntity> edocQuickSend = new HashSet<V3xOrgEntity>();
			Set<V3xOrgEntity> edocRegister = new HashSet<V3xOrgEntity>();
			Set<V3xOrgEntity> edocSignSend = new HashSet<V3xOrgEntity>();
			Set<V3xOrgEntity> accountGovdocStat = new HashSet<V3xOrgEntity>();
			for (V3xOrgRole v3xOrgRole : roles) {
				privRoleMenuList = new ArrayList<PrivRoleMenu>();
				if("Accountexchange".equals(v3xOrgRole.getCode())
		   				|| "Departmentexchange".equals(v3xOrgRole.getCode())
		   				|| "FenfaEdoc".equals(v3xOrgRole.getCode())){
					continue;
				}
				List<V3xOrgEntity> entities = orgManager.getEntitysByRoleAllowRepeat(null, v3xOrgRole.getId());
				selectCondition.setRoleid(v3xOrgRole.getId());
				Long[] menusByRole = privilegeMenuManager.getMenusByRole(new Long[]{v3xOrgRole.getId()});
				for (Long menuId : menusByRole) {
					//拟文
					PrivMenuBO menuBO = menuManager.findById(menuId);
					if(menuBO == null){
						continue;
					}
					if ("F20_newSend".equals(menuBO.getResourceCode())) {
						edocSend.addAll(entities);
						edocQuickSend.addAll(entities);
						continue;
					}
					//登记
					if ("F20_newDengji".equals(menuBO.getResourceCode())) {
						edocRegister.addAll(entities);
						continue;
					}
					//签报拟文
					if ("F20_newSign".equals(menuBO.getResourceCode())	) {
						edocSignSend.addAll(entities);
						continue;
					}
					this.getPrivRoleResources(menuBO.getResourceCode(), new Long[]{v3xOrgRole.getId()}, privRoleMenuList);
				}
				if(!v3xOrgRole.getCode().equals(OrgConstants.Role_NAME.SendEdoc.name()) && Strings.isNotEmpty(privRoleMenuList)) {
					refreshRole(v3xOrgRole.getId(),privRoleMenuList);
				}
			}
			
//			//单组织发文拟文菜单不对
//			ArrayList<PrivRoleMenu> sendEdocList = new ArrayList<PrivRoleMenu>();
//			PrivRoleMenu m = new PrivRoleMenu();
//			m.setIdIfNew();
//			m.setMenuid(-2063501733767461511L);
//			m.setRoleid(1343960019065655086L);
//			sendEdocList.add(m); 
//			roleMenuDao.insertRoleMenuPatchAll(sendEdocList,true);
			
			//获取有单位公文收发员这个角色的所有人员
			V3xOrgRole accountexchangeRole = orgManager.getRoleByName("Accountexchange", OrgConstants.ACCOUNTID);
			List<V3xOrgEntity> accountExchangeRoleEntities = orgManager.getEntitysByRoleAllowRepeat(null, accountexchangeRole.getId());
			
			//获取单位公文收发员下所有资源
			/*PrivRoleMenu privRoleMenu = new PrivRoleMenu();
			privRoleMenu.setRoleid(accountexchangeRole.getId());*/
			
			/**
			 * 将公文统计下面的6个菜单追加给单位公文收发员角色
			 */
			addEdocStaticAllMenuToAccountExchangeRole(accountexchangeRole);
			/**
			 * 公文交换菜单追加到角色中去
			 */
			addEdocExchangeMenuToAccountExchangeRole(accountexchangeRole);
			
			
			V3xOrgRole accountGovdocSend = orgManager.getRoleByName("AccountGovdocSend", OrgConstants.ACCOUNTID);
			if(accountGovdocSend == null){
				createOrgRoleForAccount();
				accountGovdocSend = orgManager.getRoleByName("AccountGovdocSend", OrgConstants.ACCOUNTID);
			}
			orgManagerDirect.addRole2Entities(accountGovdocSend.getId(), OrgConstants.ACCOUNTID, accountExchangeRoleEntities, null);
			
			
			V3xOrgRole AccountGovdocRec = orgManager.getRoleByName("AccountGovdocRec", OrgConstants.ACCOUNTID);
			orgManagerDirect.addRole2Entities(AccountGovdocRec.getId(), OrgConstants.ACCOUNTID, accountExchangeRoleEntities, null);
			
			V3xOrgRole AccountGovdocStat = orgManager.getRoleByName("AccountGovdocStat", OrgConstants.ACCOUNTID);
			accountGovdocStat.addAll(accountExchangeRoleEntities);
			
			//为各个角色分配资源
			List<PrivRoleMenu> oldPrivRoleResources = new ArrayList<PrivRoleMenu>();
			
			PrivRoleMenu sendrm = new PrivRoleMenu();
			sendrm.setNewId();
			sendrm.setMenuid(-2063501733767461537l);
			sendrm.setRoleid(accountGovdocSend.getId());
			oldPrivRoleResources.add(sendrm);
			
			PrivRoleMenu recrm= new PrivRoleMenu();
			recrm.setNewId();
			recrm.setMenuid(-2063501733767461537l);
			recrm.setRoleid(AccountGovdocRec.getId());
			oldPrivRoleResources.add(recrm);
			
			Long[] newRoleIds = new Long[]{AccountGovdocStat.getId()};
			
			Long[] accountExchangeRoleMenuIds = privilegeMenuManager.getMenusByRole(new Long[]{accountexchangeRole.getId()});
			for (Long menuId : accountExchangeRoleMenuIds) {
				PrivMenuBO menuBO = menuManager.findById(menuId);
				if(menuBO == null){
					continue;
				}
				boolean result = this.getPrivRoleResources(menuBO.getResourceCode(), newRoleIds, oldPrivRoleResources);
				if (result) {
					continue;
				}
//				PrivRoleMenu privRoleResource1 = new PrivRoleMenu();
//				privRoleResource1.setNewId();
//				privRoleResource1.setMenuid(menuBO.getId());
//				privRoleResource1.setRoleid(accountGovdocSend.getId());
//				oldPrivRoleResources.add(privRoleResource1);
//				
//				PrivRoleMenu privRoleResource2 = new PrivRoleMenu();
//				privRoleResource2.setNewId();
//				privRoleResource2.setMenuid(menuBO.getId());
//				privRoleResource2.setRoleid(AccountGovdocRec.getId());
//				oldPrivRoleResources.add(privRoleResource2);
				
				PrivRoleMenu privRoleResource3 = new PrivRoleMenu();
				privRoleResource3.setNewId();
				privRoleResource3.setMenuid(menuBO.getId());
				privRoleResource3.setRoleid(AccountGovdocStat.getId());
				oldPrivRoleResources.add(privRoleResource3);
			}


			
			//部门
			V3xOrgRole departmentexchange = orgManager.getRoleByName("Departmentexchange", OrgConstants.ACCOUNTID);
			List<V3xOrgEntity> departmentExchangeRoleEntities = orgManager.getEntitysByRoleAllowRepeat(null, departmentexchange.getId());
			
			/**
			 * 公文交换菜单追加到角色中去
			 */
			addEdocExchangeMenuToAccountExchangeRole(departmentexchange);
			
			Long[] menusByRole = privilegeMenuManager.getMenusByRole(new Long[]{departmentexchange.getId()});
			
			V3xOrgRole DepartmentGovdocRec = orgManager.getRoleByName("DepartmentGovdocRec", OrgConstants.ACCOUNTID);
			
			V3xOrgRole DepartmentGovdocSend = orgManager.getRoleByName("DepartmentGovdocSend", OrgConstants.ACCOUNTID);
			accountGovdocStat.addAll(departmentExchangeRoleEntities);
			

			
			newRoleIds = new Long[]{DepartmentGovdocRec.getId(),DepartmentGovdocSend.getId()};
			for (Long menuId : menusByRole) {
				PrivMenuBO menuBO = menuManager.findById(menuId);
				if(menuBO == null){
					continue;
				}
				boolean result = this.getPrivRoleResources(menuBO.getResourceCode(), newRoleIds, oldPrivRoleResources);
				if (result) {
					continue;
				}
				
				PrivRoleMenu privRoleResource1 = new PrivRoleMenu();
				privRoleResource1.setNewId();
				privRoleResource1.setMenuid(menuBO.getId());
				privRoleResource1.setRoleid(DepartmentGovdocRec.getId());
				oldPrivRoleResources.add(privRoleResource1);
				
				PrivRoleMenu privRoleResource2 = new PrivRoleMenu();
				privRoleResource2.setNewId();
				privRoleResource2.setMenuid(menuBO.getId());
				privRoleResource2.setRoleid(DepartmentGovdocSend.getId());
				oldPrivRoleResources.add(privRoleResource2);
				
			}

			
			roleMenuDao.insertRoleMenuPatchAll(oldPrivRoleResources);
			
			orgManagerDirect.addRole2Entities(1343960019065655086L, OrgConstants.ACCOUNTID, new ArrayList<V3xOrgEntity>(edocSend), null);
			orgManagerDirect.addRole2Entities(2019012500007L, OrgConstants.ACCOUNTID, new ArrayList<V3xOrgEntity>(edocQuickSend), null);
			//orgManagerDirect.addRole2Entities(-1036889258487886968L, OrgConstants.ACCOUNTID, new ArrayList<V3xOrgEntity>(edocRegister), null);
			orgManagerDirect.addRole2Entities(-143786696673705329L, OrgConstants.ACCOUNTID, new ArrayList<V3xOrgEntity>(edocSignSend), null);
			orgManagerDirect.addRole2Entities(AccountGovdocStat.getId(), OrgConstants.ACCOUNTID, new ArrayList<V3xOrgEntity>(accountGovdocStat), null);
			
			
			
			try {
				List<V3xOrgDepartment> departments = orgManager.getAllDepartments(OrgConstants.ACCOUNTID);
				List<OrgRelationship> newOrgRelationship = new ArrayList<OrgRelationship>();
				for (V3xOrgDepartment department : departments) {
					//objective0Id 单位id
					//objective1Id 角色id
					EnumMap<RelationshipObjectiveName, Object> params = new EnumMap<OrgConstants.RelationshipObjectiveName, Object>(OrgConstants.RelationshipObjectiveName.class);
					params.put(RelationshipObjectiveName.objective0Id, department.getId());
					params.put(RelationshipObjectiveName.objective1Id, departmentexchange.getId());
					List<OrgRelationship> allOrgRelationship = this.orgDao.getOrgRelationshipPO(null, null, null, params, null);
					for (OrgRelationship relationship : allOrgRelationship) {
							OrgRelationship departmentGovdocRecRelationship = (OrgRelationship) relationship.clone();
							OrgRelationship departmentGovdocSendRecRelationship = (OrgRelationship) relationship.clone();
							departmentGovdocRecRelationship.setNewId();
							departmentGovdocRecRelationship.setObjective1Id(DepartmentGovdocRec.getId());
							departmentGovdocSendRecRelationship.setNewId();
							departmentGovdocSendRecRelationship.setObjective1Id(DepartmentGovdocSend.getId());
							newOrgRelationship.add(departmentGovdocRecRelationship);
							newOrgRelationship.add(departmentGovdocSendRecRelationship);
					}
				}
				orgDao.insertOrgRelationship(newOrgRelationship);
			} catch (Exception e) {
				LOG.info("部门公文送文员，收文员数据升级出错");
				LOG.error(e.getMessage(),e);
			}
			
			//删除公文部门收发员
			orgManagerDirect.deleteRole(departmentexchange);
			orgManagerDirect.deleteRole(accountexchangeRole);
		}else {
			PrivRoleMenu selectCondition = new PrivRoleMenu();
			List<V3xOrgRole> roles = orgManager.getAllRoles(-1730833917365171641l);
			List<PrivRoleMenu> tempPrivRoleResources = new ArrayList<PrivRoleMenu>();
			//循环单位所有角色，对比老公文资源，添加新公文资源。
			for (V3xOrgRole v3xOrgRole : roles) {
				tempPrivRoleResources = new ArrayList<PrivRoleMenu>();
				if( "Accountexchange".equals(v3xOrgRole.getCode())
		   				|| "Departmentexchange".equals(v3xOrgRole.getCode())
		   				|| "FenfaEdoc".equals(v3xOrgRole.getCode())){
					continue;
				}
				selectCondition.setRoleid(v3xOrgRole.getId());
				Long[] menusByRole = privilegeMenuManager.getMenusByRole(new Long[]{v3xOrgRole.getId()});
				
				for (Long menuId : menusByRole) {
					PrivMenuBO menuBO = menuManager.findById(menuId);
					if(null != menuBO){
						this.getPrivRoleResources(menuBO.getResourceCode(), new Long[]{v3xOrgRole.getId()}, tempPrivRoleResources);
					}
				}
				refreshRole(v3xOrgRole.getId(),tempPrivRoleResources);
			}
			
			
			Long[]  accountexchangePrivRoleResources= null;
			Long[] departmentexchangePrivRoleResources = null;
			for (V3xOrgAccount v3xOrgAccount : units) {
				if (v3xOrgAccount.getId().equals(-1730833917365171641l)) {
					V3xOrgRole Accountexchange = orgManager.getRoleByName("Accountexchange", v3xOrgAccount.getId());
					if(null == Accountexchange){
						continue;
					}
					PrivRoleMenu selectResource = new PrivRoleMenu();
					selectResource.setRoleid(Accountexchange.getId());
					accountexchangePrivRoleResources = privilegeMenuManager.getMenusByRole(new Long[]{Accountexchange.getId()});
					List<PrivRoleMenu> oldPrivRoleResources = new ArrayList<PrivRoleMenu>();

					Long[] newRoleIds = new Long[]{959477643164863881L,959477643164863882L,959477643164863889L};
					for (Long menuId : accountexchangePrivRoleResources) {
						PrivMenuBO menuBO = menuManager.findById(menuId);
						//这里判断有没有公文的其他资源。如果有的话，需要添加对应的资源。
						if(menuBO == null){
							continue;
						}
						boolean result = this.getPrivRoleResources(menuBO.getResourceCode(), newRoleIds, oldPrivRoleResources);
						if (result) {
							continue;
						}
						PrivRoleMenu privRoleResource1 = new PrivRoleMenu();
						privRoleResource1.setNewId();
						privRoleResource1.setMenuid(menuBO.getId());
//						privRoleResource1.setModifiable(resource.getModifiable());
//						privRoleResource1.setResourceid(resource.getResourceid());
						privRoleResource1.setRoleid(959477643164863882L);
						oldPrivRoleResources.add(privRoleResource1);
						
						PrivRoleMenu privRoleResource2 = new PrivRoleMenu();
						privRoleResource2.setNewId();
						privRoleResource2.setMenuid(menuBO.getId());
//						privRoleResource2.setModifiable(resource.getModifiable());
//						privRoleResource2.setResourceid(resource.getResourceid());
						privRoleResource2.setRoleid(959477643164863881L);
						oldPrivRoleResources.add(privRoleResource2);
						
						PrivRoleMenu privRoleResource3 = new PrivRoleMenu();
						privRoleResource3.setNewId();
						privRoleResource3.setMenuid(menuBO.getId());
//						privRoleResource3.setModifiable(resource.getModifiable());
//						privRoleResource3.setResourceid(resource.getResourceid());
						privRoleResource3.setRoleid(959477643164863889L);
						oldPrivRoleResources.add(privRoleResource3);
					}

					
					
					V3xOrgRole Departmentexchange = orgManager.getRoleByName("Departmentexchange", v3xOrgAccount.getId());
					selectResource = new PrivRoleMenu();
					selectResource.setRoleid(Departmentexchange.getId());
					departmentexchangePrivRoleResources = privilegeMenuManager.getMenusByRole(new Long[]{Departmentexchange.getId()});
					newRoleIds = new Long[]{959477643164863883L,959477643164863884L};
					for (Long menuId : departmentexchangePrivRoleResources) {
						PrivMenuBO menuBO = menuManager.findById(menuId);
						//这里判断有没有公文的其他资源。如果有的话，需要添加对应的资源。
						if(menuBO == null){
							continue;
						}
						boolean result = this.getPrivRoleResources(menuBO.getResourceCode(), newRoleIds, oldPrivRoleResources);
						if (result) {
							continue;
						}
						PrivRoleMenu privRoleResource1 = new PrivRoleMenu();
						privRoleResource1.setNewId();
						privRoleResource1.setMenuid(menuBO.getId());
//						privRoleResource1.setModifiable(resource.getModifiable());
//						privRoleResource1.setResourceid(resource.getResourceid());
						privRoleResource1.setRoleid(959477643164863883L);
						oldPrivRoleResources.add(privRoleResource1);
						
						PrivRoleMenu privRoleResource2 = new PrivRoleMenu();
						privRoleResource2.setNewId();
						privRoleResource2.setMenuid(menuBO.getId());
//						privRoleResource2.setModifiable(resource.getModifiable());
//						privRoleResource2.setResourceid(resource.getResourceid());
						privRoleResource2.setRoleid(959477643164863884L);
						oldPrivRoleResources.add(privRoleResource2);
					}
					roleMenuDao.insertRoleMenuPatchAll(oldPrivRoleResources);
					continue;
				}
				roles = orgManager.getAllRoles(v3xOrgAccount.getId());
				tempPrivRoleResources = new ArrayList<PrivRoleMenu>();
				//循环单位所有角色，对比老公文资源，添加新公文资源。
				Set<V3xOrgEntity> edocSend = new HashSet<V3xOrgEntity>();
				Set<V3xOrgEntity> edocQuickSend = new HashSet<V3xOrgEntity>();
				Set<V3xOrgEntity> edocRegister = new HashSet<V3xOrgEntity>();
				Set<V3xOrgEntity> edocSignSend = new HashSet<V3xOrgEntity>();
				Set<V3xOrgEntity> accountGovdocStat = new HashSet<V3xOrgEntity>(); 
				for (V3xOrgRole v3xOrgRole : roles) {
					if( "Accountexchange".equals(v3xOrgRole.getCode())
			   				|| "Departmentexchange".equals(v3xOrgRole.getCode())
			   				|| "FenfaEdoc".equals(v3xOrgRole.getCode())){
						continue;
					}
					tempPrivRoleResources = new ArrayList<PrivRoleMenu>();
					List<V3xOrgEntity> entities = orgManager.getEntitysByRoleAllowRepeat(null, v3xOrgRole.getId());
					selectCondition.setRoleid(v3xOrgRole.getId());
					Long[] tempResources = privilegeMenuManager.getMenusByRole(new Long[]{v3xOrgRole.getId()});
//					boolean hasfw = false;
//					boolean hassw = false;
//					boolean hasqb = false;
//					Long fawenguanliMenuID= -2063501733767461522L;
//					Long shouwenguanliMenuID= -2063501733767461523L;
//					Long qianbaoguanliMenuID= -2063501733767461531L;
					for (Long menuId : tempResources) {
						//拟文
						PrivMenuBO menuBO = menuManager.findById(menuId);
//						if(menuId.equals(fawenguanliMenuID)){
//							hasfw =true;
//						}
//						if(menuId.equals(shouwenguanliMenuID)){
//							hassw =true;
//						}
//						if(menuId.equals(qianbaoguanliMenuID)){
//							hasqb =true;
//						}
						if(menuBO == null || (menuBO != null && Strings.isBlank(menuBO.getResourceCode())) ){
							continue;
						}
						if ("F20_newSend".equals(menuBO.getResourceCode())) {//2718897185706164469 -2063501733767461511
							edocSend.addAll(entities); 
							edocQuickSend.addAll(entities);
							continue;
						}
						//登记
						if ("F20_newDengji".equals(menuBO.getResourceCode())) {//-17096606087754455L -2063501733767461512
							edocRegister.addAll(entities);
							continue;
						}
						//签报拟文
						if ("F20_newSign".equals(menuBO.getResourceCode())) {//-6954715937605598651L -2063501733767461526
							edocSignSend.addAll(entities);
							continue;
						}
						this.getPrivRoleResources(menuBO.getResourceCode(), new Long[]{v3xOrgRole.getId()}, tempPrivRoleResources);
					}
//					if(Strings.isNotBlank(v3xOrgRole.getCode()) && v3xOrgRole.getCode().equals(OrgConstants.Role_NAME.GeneralStaff.name())) {
//						if(!hasfw) {
//							PrivRoleMenu m1 = new PrivRoleMenu();
//							m1.setIdIfNew();
//							m1.setMenuid(fawenguanliMenuID);
//							m1.setRoleid(v3xOrgRole.getId());
//							tempPrivRoleResources.add(m1);
//						}
//						if(!hassw) {
//							PrivRoleMenu m2 = new PrivRoleMenu();
//							m2.setIdIfNew();
//							m2.setMenuid(shouwenguanliMenuID);
//							m2.setRoleid(v3xOrgRole.getId());
//							tempPrivRoleResources.add(m2);
//						}
//						if(!hasqb) {
//							PrivRoleMenu m3 = new PrivRoleMenu();
//							m3.setIdIfNew();
//							m3.setMenuid(qianbaoguanliMenuID);
//							m3.setRoleid(v3xOrgRole.getId());
//							tempPrivRoleResources.add(m3);
//						}
//						
//					}
					refreshRole(v3xOrgRole.getId(),tempPrivRoleResources); 
				}
				
				List<PrivRoleMenu> accroleReources = new ArrayList<PrivRoleMenu>();
				V3xOrgRole Accountexchange = orgManager.getRoleByName("Accountexchange", v3xOrgAccount.getId());
				//创建角色
				//单位公文统计拿出来，创建部门角色的时候会用到。
				V3xOrgRole AccountGovdocStat = null;
				V3xOrgRole EdocQuickSend = null;
				if (Accountexchange != null) {
					List<V3xOrgEntity> entities = orgManager.getEntitysByRoleAllowRepeat(null, Accountexchange.getId());

					PrivRoleMenu selectResource = new PrivRoleMenu();
					selectResource.setRoleid(Accountexchange.getId());
					accountexchangePrivRoleResources =privilegeMenuManager.getMenusByRole(new Long[]{Accountexchange.getId()});

					//创建一个单位送文员，并且分配人员
					V3xOrgRole AccountGovdocSend = new V3xOrgRole();
					AccountGovdocSend.setIdIfNew();
					AccountGovdocSend.setBond(1);
					AccountGovdocSend.setCategory(Accountexchange.getCategory());
					AccountGovdocSend.setCode("AccountGovdocSend");
					AccountGovdocSend.setCreateTime(new Date());
					AccountGovdocSend.setEnabled(true);
					AccountGovdocSend.setIsBenchmark(false);
					AccountGovdocSend.setIsDeleted(false);
					AccountGovdocSend.setName("AccountGovdocSend");
					AccountGovdocSend.setOrgAccountId(v3xOrgAccount.getId());
					AccountGovdocSend.setSortId(Accountexchange.getSortId());
					
					int accountExchangeStatus = Accountexchange.getStatus().equals(ORGENT_STATUS.NULL.ordinal())?ORGENT_STATUS.NORMAL.ordinal():Accountexchange.getStatus();
					AccountGovdocSend.setStatus(accountExchangeStatus);
					
					AccountGovdocSend.setType(Accountexchange.getType());
					AccountGovdocSend.setUpdateTime(new Date());
					orgManagerDirect.addRole(AccountGovdocSend);
					orgManagerDirect.addRole2Entities(AccountGovdocSend.getId(), v3xOrgAccount.getId(), entities, null);
					
					PrivRoleMenu accprivRoleResource1 = new PrivRoleMenu();
					accprivRoleResource1.setRoleid(AccountGovdocSend.getId());
					accprivRoleResource1.setId(UUIDLong.longUUID());
					accprivRoleResource1.setMenuid(-2063501733767461537L);//-2063501733767461522 变成 -2063501733767461537
					accprivRoleResource1.setModifiable(true); 
					accprivRoleResource1.setResourceid(-876749380180031728L);
					accroleReources.add(accprivRoleResource1);

					saveRoleIds.add(AccountGovdocSend.getId());
					
					//创建一个单位收文员，并且分配人员
					V3xOrgRole AccountGovdocRec = new V3xOrgRole();
					AccountGovdocRec.setIdIfNew();
					AccountGovdocRec.setBond(1);
					AccountGovdocRec.setCategory(Accountexchange.getCategory());
					AccountGovdocRec.setCode("AccountGovdocRec");
					AccountGovdocRec.setCreateTime(new Date());
					AccountGovdocRec.setEnabled(true);
					AccountGovdocRec.setIsBenchmark(false);
					AccountGovdocRec.setIsDeleted(false);
					AccountGovdocRec.setName("AccountGovdocRec");
					AccountGovdocRec.setOrgAccountId(v3xOrgAccount.getId());
					AccountGovdocRec.setSortId(Accountexchange.getSortId());
					
					AccountGovdocSend.setStatus(accountExchangeStatus);
					
					AccountGovdocRec.setType(Accountexchange.getType());
					AccountGovdocRec.setUpdateTime(new Date());
					orgManagerDirect.addRole(AccountGovdocRec);
					orgManagerDirect.addRole2Entities(AccountGovdocRec.getId(), v3xOrgAccount.getId(), entities, null);
					
					PrivRoleMenu accprivRoleResource2 = new PrivRoleMenu();
					accprivRoleResource2.setRoleid(AccountGovdocRec.getId());
					accprivRoleResource2.setId(UUIDLong.longUUID());
					accprivRoleResource2.setMenuid(-2063501733767461537L);//-2063501733767461523变成-2063501733767461537
					accprivRoleResource2.setModifiable(true); 
					accprivRoleResource2.setResourceid(-876749380180031729L);
					accroleReources.add(accprivRoleResource2);

					saveRoleIds.add(AccountGovdocRec.getId());
					
					
					//创建一个单位统计员，并且分配人员
					AccountGovdocStat = new V3xOrgRole();
					AccountGovdocStat.setIdIfNew();
					AccountGovdocStat.setBond(1);
					AccountGovdocStat.setCategory(Accountexchange.getCategory());
					AccountGovdocStat.setCode("AccountGovdocStat");
					AccountGovdocStat.setCreateTime(new Date());
					AccountGovdocStat.setEnabled(true);
					AccountGovdocStat.setIsBenchmark(false);
					AccountGovdocStat.setIsDeleted(false);
					AccountGovdocStat.setName("AccountGovdocStat");
					AccountGovdocStat.setOrgAccountId(v3xOrgAccount.getId());
					AccountGovdocStat.setSortId(Accountexchange.getSortId());
					
					AccountGovdocStat.setStatus(accountExchangeStatus);
					
					AccountGovdocStat.setType(Accountexchange.getType());
					AccountGovdocStat.setUpdateTime(new Date());
					orgManagerDirect.addRole(AccountGovdocStat);
					
					accountGovdocStat.addAll(entities);
					
					PrivRoleMenu accprivRoleResource3 = new PrivRoleMenu();
					accprivRoleResource3.setRoleid(AccountGovdocStat.getId());
					accprivRoleResource3.setId(UUIDLong.longUUID());
					accprivRoleResource3.setMenuid(-2063501733767461518L);
					accprivRoleResource3.setModifiable(true); 
					accprivRoleResource3.setResourceid(-876749380180031726L);
					accroleReources.add(accprivRoleResource3);

					saveRoleIds.add(AccountGovdocStat.getId());
					
					//创建一个快速发文角色，并且分配人员
					EdocQuickSend = new V3xOrgRole();
					EdocQuickSend.setIdIfNew();
					EdocQuickSend.setBond(1);
					EdocQuickSend.setCategory(Accountexchange.getCategory());
					EdocQuickSend.setCode("EdocQuickSend");
					EdocQuickSend.setCreateTime(new Date());
					EdocQuickSend.setEnabled(true);
					EdocQuickSend.setIsBenchmark(false);
					EdocQuickSend.setIsDeleted(false);
					EdocQuickSend.setName("EdocQuickSend");
					EdocQuickSend.setOrgAccountId(v3xOrgAccount.getId());
					EdocQuickSend.setSortId(Accountexchange.getSortId());
					
					EdocQuickSend.setStatus(accountExchangeStatus);
					
					EdocQuickSend.setType(Accountexchange.getType());
					EdocQuickSend.setUpdateTime(new Date());
					orgManagerDirect.addRole(EdocQuickSend);
					
					
					PrivRoleMenu accprivRoleResource4 = new PrivRoleMenu();
					accprivRoleResource4.setRoleid(EdocQuickSend.getId());
					accprivRoleResource4.setId(UUIDLong.longUUID());
					accprivRoleResource4.setMenuid(-2063501733767461525L);
					accprivRoleResource4.setModifiable(true); 
					accprivRoleResource4.setResourceid(-876749380180031732L);
					accroleReources.add(accprivRoleResource4);
					
					saveRoleIds.add(EdocQuickSend.getId());
					
					
//					//创建一个单位会议联络员，不分配资源及人员
//					V3xOrgRole UnitMeetContact = new V3xOrgRole();
//					UnitMeetContact.setIdIfNew();
//					UnitMeetContact.setBond(1);
//					UnitMeetContact.setCategory(Accountexchange.getCategory());
//					UnitMeetContact.setCode("UnitMeetContact");
//					UnitMeetContact.setCreateTime(new Date());
//					UnitMeetContact.setEnabled(true);
//					UnitMeetContact.setIsBenchmark(false);
//					UnitMeetContact.setIsDeleted(false);
//					UnitMeetContact.setName("UnitMeetContact");
//					UnitMeetContact.setOrgAccountId(v3xOrgAccount.getId());
//					UnitMeetContact.setSortId(Accountexchange.getSortId());
//					UnitMeetContact.setStatus(Accountexchange.getStatus());
//					UnitMeetContact.setType(Accountexchange.getType());
//					UnitMeetContact.setUpdateTime(new Date());
//					orgManagerDirect.addRole(UnitMeetContact);
//
//					saveRoleIds.add(UnitMeetContact.getId());
					
//					//创建一个单位督办联络员，不分配资源及人员
//					V3xOrgRole UnitSupervision = new V3xOrgRole();
//					UnitSupervision.setIdIfNew();
//					UnitSupervision.setBond(1);
//					UnitSupervision.setCategory(Accountexchange.getCategory());
//					UnitSupervision.setCode("UnitSupervision");
//					UnitSupervision.setCreateTime(new Date());
//					UnitSupervision.setEnabled(true);
//					UnitSupervision.setIsBenchmark(false);
//					UnitSupervision.setIsDeleted(false);
//					UnitSupervision.setName("UnitSupervision");
//					UnitSupervision.setOrgAccountId(v3xOrgAccount.getId());
//					UnitSupervision.setSortId(Accountexchange.getSortId());
//					UnitSupervision.setStatus(Accountexchange.getStatus());
//					UnitSupervision.setType(Accountexchange.getType());
//					UnitSupervision.setUpdateTime(new Date());
//					orgManagerDirect.addRole(UnitSupervision);
//					
//					saveRoleIds.add(UnitSupervision.getId());
					
					orgManagerDirect.deleteRole(Accountexchange); 
					

					Long[] roleIds = new Long[]{AccountGovdocSend.getId(),AccountGovdocRec.getId(),AccountGovdocStat.getId()};
					for (Long menuId : accountexchangePrivRoleResources) {
						PrivMenuBO menuBO = menuManager.findById(menuId);
						//拟文
						if(menuBO == null){
							continue;
						}
						if ("F20_newSend".equals(menuBO.getResourceCode())) {//2718897185706164469 -2063501733767461511
							edocSend.addAll(entities);
							edocQuickSend.addAll(entities);
							continue;
						}
						//登记
						if ("F20_newDengji".equals(menuBO.getResourceCode())) {//-17096606087754455L -2063501733767461512
							edocRegister.addAll(entities);
							continue;
						}
						//签报拟文
						if ("F20_newSign".equals(menuBO.getResourceCode())) {//-6954715937605598651L -2063501733767461526
							edocSignSend.addAll(entities);
							continue;
						}
						//这里判断有没有公文的其他资源。如果有的话，需要添加对应的资源。
						boolean result = this.getPrivRoleResources(menuBO.getResourceCode(), roleIds, accroleReources);
						if (result) {
							continue;
						}
						PrivRoleMenu privRoleResource1 = new PrivRoleMenu();
						privRoleResource1.setNewId();
						privRoleResource1.setMenuid(menuBO.getId());
//						privRoleResource1.setModifiable(resource.getModifiable());
//						privRoleResource1.setResourceid(resource.getResourceid());
						privRoleResource1.setRoleid(AccountGovdocSend.getId());
						accroleReources.add(privRoleResource1);
						
						PrivRoleMenu privRoleResource2 = new PrivRoleMenu();
						privRoleResource2.setNewId();
						privRoleResource2.setMenuid(menuBO.getId());
//						privRoleResource2.setModifiable(resource.getModifiable());
//						privRoleResource2.setResourceid(resource.getResourceid());
						privRoleResource2.setRoleid(AccountGovdocRec.getId());
						accroleReources.add(privRoleResource2);
						
						PrivRoleMenu privRoleResource3 = new PrivRoleMenu();
						privRoleResource3.setNewId();
						privRoleResource3.setMenuid(menuBO.getId());
//						privRoleResource3.setModifiable(resource.getModifiable());
//						privRoleResource3.setResourceid(resource.getResourceid());
						privRoleResource3.setRoleid(AccountGovdocStat.getId());
						accroleReources.add(privRoleResource3);
					}

				}
				V3xOrgRole Departmentexchange = orgManager.getRoleByName("Departmentexchange", v3xOrgAccount.getId());
				if (Departmentexchange != null) {
					List<V3xOrgEntity> entities = orgManager.getEntitysByRoleAllowRepeat(null, Departmentexchange.getId());
					
					PrivRoleMenu selectResource = new PrivRoleMenu();
					selectResource.setRoleid(Departmentexchange.getId());
					departmentexchangePrivRoleResources = privilegeMenuManager.getMenusByRole(new Long[]{Departmentexchange.getId()});
					
					//创建一个部门公文收文员，并且分配人员
					V3xOrgRole DepartmentGovdocRec = new V3xOrgRole();
					DepartmentGovdocRec.setIdIfNew();
					DepartmentGovdocRec.setBond(2);
					DepartmentGovdocRec.setCategory(Departmentexchange.getCategory());
					DepartmentGovdocRec.setCode("DepartmentGovdocRec");
					DepartmentGovdocRec.setCreateTime(new Date());
					DepartmentGovdocRec.setEnabled(true);
					DepartmentGovdocRec.setIsBenchmark(false);
					DepartmentGovdocRec.setIsDeleted(false);
					DepartmentGovdocRec.setName("DepartmentGovdocRec");
					DepartmentGovdocRec.setOrgAccountId(v3xOrgAccount.getId());
					DepartmentGovdocRec.setSortId(Departmentexchange.getSortId());
					
					int departmentExchangeStatus = Departmentexchange.getStatus().equals(ORGENT_STATUS.NULL.ordinal()) ? ORGENT_STATUS.NORMAL.ordinal() : Departmentexchange.getStatus();
					DepartmentGovdocRec.setStatus(departmentExchangeStatus);
					
					DepartmentGovdocRec.setType(Departmentexchange.getType());
					DepartmentGovdocRec.setUpdateTime(new Date());
					

					PrivRoleMenu accprivRoleResource1 = new PrivRoleMenu();
					accprivRoleResource1.setRoleid(DepartmentGovdocRec.getId());
					accprivRoleResource1.setId(UUIDLong.longUUID());
					accprivRoleResource1.setMenuid(-2063501733767461537L);////-2063501733767461522变成-2063501733767461537
					accprivRoleResource1.setModifiable(true); 
					accprivRoleResource1.setResourceid(-876749380180031728L);
					accroleReources.add(accprivRoleResource1);
					
					orgManagerDirect.addRole(DepartmentGovdocRec);
					//orgManagerDirect.addRole2Entities(DepartmentGovdocRec.getId(), v3xOrgAccount.getId(), entities, null);
					
					saveRoleIds.add(DepartmentGovdocRec.getId());
					
					//创建一个部门公文送文员，并且分配人员
					V3xOrgRole DepartmentGovdocSend = new V3xOrgRole();
					DepartmentGovdocSend.setIdIfNew();
					DepartmentGovdocSend.setBond(2);
					DepartmentGovdocSend.setCategory(Departmentexchange.getCategory());
					DepartmentGovdocSend.setCode("DepartmentGovdocSend");
					DepartmentGovdocSend.setCreateTime(new Date());
					DepartmentGovdocSend.setEnabled(true);
					DepartmentGovdocSend.setIsBenchmark(false);
					DepartmentGovdocSend.setIsDeleted(false);
					DepartmentGovdocSend.setName("DepartmentGovdocSend");
					DepartmentGovdocSend.setOrgAccountId(v3xOrgAccount.getId());
					DepartmentGovdocSend.setSortId(Departmentexchange.getSortId());
					DepartmentGovdocSend.setStatus(departmentExchangeStatus);
					DepartmentGovdocSend.setType(Departmentexchange.getType());
					DepartmentGovdocSend.setUpdateTime(new Date());
					
					PrivRoleMenu accprivRoleResource2 = new PrivRoleMenu();
					accprivRoleResource2.setRoleid(DepartmentGovdocSend.getId());
					accprivRoleResource2.setId(UUIDLong.longUUID());
					accprivRoleResource2.setMenuid(-2063501733767461537L);////-2063501733767461523变成-2063501733767461537
					accprivRoleResource2.setModifiable(true); 
					accprivRoleResource2.setResourceid(-876749380180031729L);
					accroleReources.add(accprivRoleResource2);
					
					orgManagerDirect.addRole(DepartmentGovdocSend);
					
					accountGovdocStat.addAll(entities);

					saveRoleIds.add(DepartmentGovdocSend.getId());
					
//					//创建一个部门督办联络员
//					V3xOrgRole DeptSupervision = new V3xOrgRole();
//					DeptSupervision.setIdIfNew();
//					DeptSupervision.setBond(2);
//					DeptSupervision.setCategory(Departmentexchange.getCategory());
//					DeptSupervision.setCode("DeptSupervision");
//					DeptSupervision.setCreateTime(new Date());
//					DeptSupervision.setEnabled(true);
//					DeptSupervision.setIsBenchmark(false);
//					DeptSupervision.setIsDeleted(false);
//					DeptSupervision.setName("DeptSupervision");
//					DeptSupervision.setOrgAccountId(v3xOrgAccount.getId());
//					DeptSupervision.setSortId(Departmentexchange.getSortId());
//					DeptSupervision.setStatus(Departmentexchange.getStatus());
//					DeptSupervision.setType(Departmentexchange.getType());
//					DeptSupervision.setUpdateTime(new Date());
//					orgManagerDirect.addRole(DeptSupervision);
//
//					saveRoleIds.add(DeptSupervision.getId());
					
//					//创建一个部门会议联络员
//					V3xOrgRole DepMeetContact = new V3xOrgRole();
//					DepMeetContact.setIdIfNew();
//					DepMeetContact.setBond(2);
//					DepMeetContact.setCategory(Departmentexchange.getCategory());
//					DepMeetContact.setCode("DepMeetContact");
//					DepMeetContact.setCreateTime(new Date());
//					DepMeetContact.setEnabled(true);
//					DepMeetContact.setIsBenchmark(false);
//					DepMeetContact.setIsDeleted(false);
//					DepMeetContact.setName("DepMeetContact");
//					DepMeetContact.setOrgAccountId(v3xOrgAccount.getId());
//					DepMeetContact.setSortId(Departmentexchange.getSortId());
//					DepMeetContact.setStatus(Departmentexchange.getStatus());
//					DepMeetContact.setType(Departmentexchange.getType());
//					DepMeetContact.setUpdateTime(new Date());
//					orgManagerDirect.addRole(DepMeetContact);
//
//					saveRoleIds.add(DepMeetContact.getId());
					
					
					Long[] roleIds = new Long[]{DepartmentGovdocSend.getId(),DepartmentGovdocRec.getId()};
					for (Long menuId : departmentexchangePrivRoleResources) {
						PrivMenuBO menuBO = menuManager.findById(menuId);
						//拟文
						if(menuBO == null){
							continue;
						}
						if ("F20_newSend".equals(menuBO.getResourceCode())) {// 2718897185706164469 -2063501733767461511
							edocSend.addAll(entities);
							edocQuickSend.addAll(entities);
							continue;
						}
						//登记
						if ("F20_newDengji".equals(menuBO.getResourceCode())) {// -17096606087754455L -2063501733767461512
							edocRegister.addAll(entities);
							continue;
						}
						//签报拟文
						if ("F20_newSign".equals(menuBO.getResourceCode())) {//-6954715937605598651L -2063501733767461526
							edocSignSend.addAll(entities);
							continue;
						}
						//这里判断有没有公文的其他资源。如果有的话，需要添加对应的资源。
						boolean result = this.getPrivRoleResources(menuBO.getResourceCode(), roleIds, accroleReources);
						if (result) {
							continue;
						}
						PrivRoleMenu privRoleResource1 = new PrivRoleMenu();
						privRoleResource1.setNewId();
						privRoleResource1.setMenuid(menuBO.getId());
//						privRoleResource1.setModifiable(resource.getModifiable());
//						privRoleResource1.setResourceid(resource.getResourceid());
						privRoleResource1.setRoleid(DepartmentGovdocSend.getId());
						accroleReources.add(privRoleResource1);
						
						PrivRoleMenu privRoleResource2 = new PrivRoleMenu();
						privRoleResource2.setNewId();
						privRoleResource2.setMenuid(menuBO.getId());
//						privRoleResource2.setModifiable(resource.getModifiable());
//						privRoleResource2.setResourceid(resource.getResourceid());
						privRoleResource2.setRoleid(DepartmentGovdocRec.getId());
						accroleReources.add(privRoleResource2);
					}
					try {
						List<V3xOrgDepartment> departments = orgManager.getAllDepartments(v3xOrgAccount.getId());
						List<OrgRelationship> newOrgRelationship = new ArrayList<OrgRelationship>();
						for (V3xOrgDepartment department : departments) {
							//objective0Id 单位id
							//objective1Id 角色id
							EnumMap<RelationshipObjectiveName, Object> params = new EnumMap<OrgConstants.RelationshipObjectiveName, Object>(OrgConstants.RelationshipObjectiveName.class);
							params.put(RelationshipObjectiveName.objective0Id, department.getId());
							params.put(RelationshipObjectiveName.objective1Id, Departmentexchange.getId());
							List<OrgRelationship> allOrgRelationship = this.orgDao.getOrgRelationshipPO(null, null, null, params, null);
							for (OrgRelationship relationship : allOrgRelationship) {
									OrgRelationship departmentGovdocRecRelationship = (OrgRelationship) relationship.clone();
									OrgRelationship departmentGovdocSendRecRelationship = (OrgRelationship) relationship.clone();
									departmentGovdocRecRelationship.setNewId();
									departmentGovdocRecRelationship.setObjective1Id(DepartmentGovdocRec.getId());
									departmentGovdocSendRecRelationship.setNewId();
									departmentGovdocSendRecRelationship.setObjective1Id(DepartmentGovdocSend.getId());
									newOrgRelationship.add(departmentGovdocRecRelationship);
									newOrgRelationship.add(departmentGovdocSendRecRelationship);
							}
						}
						orgDao.insertOrgRelationship(newOrgRelationship);
					} catch (Exception e) {
						LOG.info("部门公文送文员，收文员数据升级出错");
						LOG.error(e.getMessage(),e);
					}
				}
				roleMenuDao.insertRoleMenuPatchAll(accroleReources);
				//删除公文部门收发员
				orgManagerDirect.deleteRole(Departmentexchange);
				if (Accountexchange != null) {
					V3xOrgRole SendEdoc = orgManager.getRoleByName("SendEdoc", v3xOrgAccount.getId());
					V3xOrgRole RegisterEdoc = orgManager.getRoleByName("RegisterEdoc", v3xOrgAccount.getId());
					V3xOrgRole SignEdoc = orgManager.getRoleByName("SignEdoc", v3xOrgAccount.getId());
					if (SendEdoc != null && SendEdoc.getId()!=null) {
						edocUpgradeDao.deleteRoleMember(SendEdoc);
						orgManagerDirect.addRole2Entities(SendEdoc.getId(), v3xOrgAccount.getId(), new ArrayList<V3xOrgEntity>(edocSend), null);
					}
					if (RegisterEdoc != null && RegisterEdoc.getId()!=null) {
						edocUpgradeDao.deleteRoleMember(RegisterEdoc);
						orgManagerDirect.addRole2Entities(RegisterEdoc.getId(), v3xOrgAccount.getId(), new ArrayList<V3xOrgEntity>(edocRegister), null);
					}
					if (SignEdoc != null && SignEdoc.getId()!=null) {
						edocUpgradeDao.deleteRoleMember(SignEdoc);
						orgManagerDirect.addRole2Entities(SignEdoc.getId(), v3xOrgAccount.getId(), new ArrayList<V3xOrgEntity>(edocSignSend), null);
					}
					if (EdocQuickSend != null && EdocQuickSend.getId()!=null) {
						orgManagerDirect.addRole2Entities(EdocQuickSend.getId(), v3xOrgAccount.getId(), new ArrayList<V3xOrgEntity>(edocQuickSend), null);
					}
					if (AccountGovdocStat != null && AccountGovdocStat.getId()!=null) {
						orgManagerDirect.addRole2Entities(AccountGovdocStat.getId(), v3xOrgAccount.getId(), new ArrayList<V3xOrgEntity>(accountGovdocStat), null);
					}
				}
			}
		}
		LOG.info("角色权限升级结束...");
	}
	
	private void addEdocExchangeMenuToAccountExchangeRole(V3xOrgRole accountexchangeRole)  throws BusinessException{
		PrivRoleMenu exchangeMenu = new PrivRoleMenu();
		exchangeMenu.setNewId();
		exchangeMenu.setMenuid(-2063501733767461537l); /*公文交换*/
		exchangeMenu.setRoleid(accountexchangeRole.getId());
		
		
		ArrayList<PrivRoleMenu> roleMenuList = new ArrayList<PrivRoleMenu>();
		roleMenuList.add(exchangeMenu);

		roleMenuDao.insertRoleMenuPatchAll(roleMenuList,true);
	}
	private void addEdocStaticAllMenuToAccountExchangeRole(V3xOrgRole accountexchangeRole) throws BusinessException {
		
		/**
		 * 协同V5OA-168297 6.1sp2-a81sqlserver个人空间首页发文登记簿、收文登记簿，公文统计栏目不存在
		 * 原因：数据库单位收发员角色和priv_menu表没有 priv_role_menu表的对应关系 所以分配给收文员送文员统计员的菜单资源就少了
		 * 由于priv_menu 表全集团一套 所以id是写死的 这里直接加上
		 */
		
		PrivRoleMenu F20_sendandreportAuth = new PrivRoleMenu();
		F20_sendandreportAuth.setNewId();
		F20_sendandreportAuth.setMenuid(2475081594935788013L); /*收文登记薄*/
		F20_sendandreportAuth.setRoleid(accountexchangeRole.getId());
		
		PrivRoleMenu F20_recandreportAuth = new PrivRoleMenu();
		F20_recandreportAuth.setNewId();
		F20_recandreportAuth.setMenuid(2475081594935788012L); /*发文登记薄*/
		F20_recandreportAuth.setRoleid(accountexchangeRole.getId());
		
		
		
		PrivRoleMenu F20_workStatistics = new PrivRoleMenu();
		F20_workStatistics.setNewId();
		F20_workStatistics.setMenuid(2475081594935789021l); /*工作统计*/
		F20_workStatistics.setRoleid(accountexchangeRole.getId());
		
		
		PrivRoleMenu F20_exchangeStatistics = new PrivRoleMenu();
		F20_exchangeStatistics.setNewId();
		F20_exchangeStatistics.setMenuid(2475081594935789020l); /*交换统计*/
		F20_exchangeStatistics.setRoleid(accountexchangeRole.getId());
		
		
		PrivRoleMenu F20_signandreportAuth = new PrivRoleMenu();
		F20_signandreportAuth.setNewId();
		F20_signandreportAuth.setMenuid(2475081594935788014l); /*签报登记薄*/
		F20_signandreportAuth.setRoleid(accountexchangeRole.getId());
		
		
		PrivRoleMenu F20_recSendStatistics = new PrivRoleMenu();
		F20_recSendStatistics.setNewId();
		F20_recSendStatistics.setMenuid(2475081594935788011l); /*收发统计*/
		F20_recSendStatistics.setRoleid(accountexchangeRole.getId());
		
		ArrayList<PrivRoleMenu> roleMenuList = new ArrayList<PrivRoleMenu>();
		roleMenuList.add(F20_sendandreportAuth);
		roleMenuList.add(F20_recandreportAuth);
		roleMenuList.add(F20_workStatistics);
		roleMenuList.add(F20_exchangeStatistics);
		roleMenuList.add(F20_signandreportAuth);
		roleMenuList.add(F20_recSendStatistics);
		
		roleMenuDao.insertRoleMenuPatchAll(roleMenuList,true);
	}
	
	private boolean getPrivRoleResources(String resourceCode,Long[] roleIds,List<PrivRoleMenu> list){
		boolean result = false;
		for (Long roleId : roleIds) {
			PrivRoleMenu PrivRoleMenu = this.createPrivRoleResource(resourceCode, roleId);
			if (PrivRoleMenu != null) {
				list.add(PrivRoleMenu);
				result = true;
			}
		}
		return result;
	}
	
	/*-- 发文拟文
	select * from priv_resource where id = 2718897185706164469;-2063501733767461511
	-- 发文管理
	select * from priv_resource where id = 2566360125886710729;
	-- 收文管理
	select * from priv_resource where id = 3950218449190995274;
	-- 收文登记
	select * from priv_resource where id = -17096606087754455;-2063501733767461512
	-- 公文查询
	select * from priv_resource where id = -2777395800345653453;
	-- 公文统计
	select * from priv_resource where id = 6205084687982806970;
	-- 公文督办
	select * from priv_resource where id = 5626904693062124199;
	-- 公文应用设置
	select * from priv_resource where id = -8291035991014028340;*/
	/*-签报拟文
	 * -6954715937605598651 -2063501733767461526
	 * 签报管理页面
	 * 7729561389181520152
	 * */
	private PrivRoleMenu createPrivRoleResource(String  resourceCode,Long roleId){
		PrivRoleMenu PrivRoleMenu = new PrivRoleMenu();
		if(Strings.isBlank(resourceCode)){
			return null;
		}
		if ("F20_govDocSendManage".equals(resourceCode)) {//2566360125886710729 F07_sendManager
			PrivRoleMenu.setNewId();
			PrivRoleMenu.setMenuid(-2063501733767461522L);
			PrivRoleMenu.setRoleid(roleId);
		}
		if ("F20_receiveManage".equals(resourceCode)) {//3950218449190995274 F07_recManager
			PrivRoleMenu.setNewId();
			PrivRoleMenu.setMenuid(-2063501733767461523L);
			PrivRoleMenu.setRoleid(roleId);
		}
		
		if ("F20_search".equals(resourceCode)) {//-2777395800345653453 F07_edocSearch
			PrivRoleMenu.setNewId();
			PrivRoleMenu.setMenuid(-2063501733767461517L);
			PrivRoleMenu.setRoleid(roleId);
		}
		if ("F20_supervise".equals(resourceCode)) {//5626904693062124199 F07_edocSupervise
			PrivRoleMenu.setNewId();
			PrivRoleMenu.setMenuid(-2063501733767461519L);
			//PrivRoleMenu.setResourceid(-876749380180031727L);
			PrivRoleMenu.setRoleid(roleId);
		}
//		if (resourceCode.equals("7729561389181520152L")) {//7729561389181520152 签报管理页面  TODO
//			PrivRoleMenu.setNewId();
//			PrivRoleMenu.setMenuid(-2063501733767461527L);
//			//PrivRoleMenu.setResourceid(-876749380180031734L);
//			PrivRoleMenu.setRoleid(roleId);
//		}
		if (PrivRoleMenu.getId() != null) {
			return PrivRoleMenu;
		}
		return null;
	}
	
	private void insterEdocElements() {
		LOG.info("添加公文元素开始...");
		if (ProductEditionEnum.getCurrentProductEditionEnum().getValue().equals(ProductEditionEnum.enterprise.getValue())
			|| ProductEditionEnum.getCurrentProductEditionEnum().getValue().equals(ProductEditionEnum.a6.getValue()) ) {
			return;
		}
		for (V3xOrgAccount v3xOrgAccount : units) {
			if (v3xOrgAccount.getId().equals(-1730833917365171641l)) {
				continue;
			}
			try {
				List<String> sqls = new ArrayList<String>();
				Long id1 = UUIDLong.longUUID();
				Long id2 = UUIDLong.longUUID();
				Long id3 = UUIDLong.longUUID();
				Long id4 = UUIDLong.longUUID();
				sqls.add("INSERT INTO EDOC_ELEMENT(ID,ELEMENT_ID,NAME,FIELD_NAME,TYPE,METADATA_ID,IS_SYSTEM,STATUS,DOMAIN_ID,INPUT_MODE) VALUES ('"+id1+"', '371', 'edoc.element.receiveunit', 'receive_unit', '0', null, '1', '1','"+v3xOrgAccount.getId()+"','')");
				sqls.add("INSERT INTO EDOC_ELEMENT(ID,ELEMENT_ID,NAME,FIELD_NAME,TYPE,METADATA_ID,IS_SYSTEM,STATUS,DOMAIN_ID,INPUT_MODE) VALUES ('"+id2+"', '372', 'edoc.element.signmark', 'sign_mark', '0', null, '1', '1','"+v3xOrgAccount.getId()+"','')");
				sqls.add("INSERT INTO edoc_element(ID,ELEMENT_ID,NAME,FIELD_NAME,TYPE,METADATA_ID,IS_SYSTEM,STATUS,DOMAIN_ID,INPUT_MODE) VALUES ('"+id3+"', '374', 'edoc.element.baopiyijian', 'baopiyijian', 0, NULL, 1, 1, '"+v3xOrgAccount.getId()+"', '')");
				sqls.add("INSERT INTO EDOC_ELEMENT(ID,ELEMENT_ID,NAME,FIELD_NAME,TYPE,METADATA_ID,IS_SYSTEM,STATUS,DOMAIN_ID,INPUT_MODE) VALUES ('"+id4+"', '375', 'edoc.element.signperson', 'sign_person', '0', null, '1', '1','"+v3xOrgAccount.getId()+"','')");
				edocElementIds.add(id1);
				edocElementIds.add(id2);
				edocElementIds.add(id3);
				edocElementIds.add(id4);
				edocUpgradeDao.insterEdocElements(sqls);
			} catch (BusinessException e) {
				LOG.error("添加公文元素出错！单位:" + v3xOrgAccount.getName());
				LOG.error(e.getMessage(),e);
			} catch (SQLException e) {
				LOG.error("添加公文元素出错！单位:" + v3xOrgAccount.getName());
				LOG.error(e.getMessage(),e);
			}
		}
		LOG.info("添加公文元素结束...");
	}
	
	/**
	 * 节点权限部分数据升级
	 * 1.升级老单位对应的新节点权限
	 * 2.升级老单位自定义的节点权限
	 */
	private void jdqxUpgrade() {
		LOG.info("节点权限升级开始...");
		List<ConfigItem> sourList = new ArrayList<ConfigItem>();
		List<ConfigItem> qianItems = new ArrayList<ConfigItem>();
		// --lib 需要修改成A8集团版
		if (ProductEditionEnum.getCurrentProductEditionEnum().getValue().equals(ProductEditionEnum.entgroup.getValue())) {
			String sql = "from ConfigItem c where c.orgAccountId != :orgAccountId and c.configType = :configType and c.configCategory = 'edoc_new_qianbao_permission_policy'";
			HashMap<String, Object> parmMap = new HashMap<String, Object>();
			parmMap.put("configType", "0");
			parmMap.put("orgAccountId", 1l);
			qianItems.addAll(DBAgent.find(sql,parmMap));
			if (CollectionUtils.isNotEmpty(qianItems)) {
				sql = "from ConfigItem c where c.orgAccountId = :orgAccountId and c.configType = :configType and (c.configCategory = 'edoc_new_send_permission_policy' or c.configCategory = 'edoc_new_rec_permission_policy' or c.configCategory = 'edoc_new_change_permission_policy' )";
			}else {
				sql = "from ConfigItem c where c.orgAccountId = :orgAccountId and c.configType = :configType and (c.configCategory = 'edoc_new_send_permission_policy' or c.configCategory = 'edoc_new_rec_permission_policy' or c.configCategory = 'edoc_new_change_permission_policy' or c.configCategory = 'edoc_new_qianbao_permission_policy')";
			}
			parmMap = new HashMap<String, Object>();
			parmMap.put("configType", "0");
			parmMap.put("orgAccountId", 1l);
			sourList.addAll(DBAgent.find(sql,parmMap));
		}
		try {
			List<Long> accounts = new ArrayList<Long>();
			//TODO --lib 需要修改成A8集团版
			if (ProductEditionEnum.getCurrentProductEditionEnum().getValue().equals(ProductEditionEnum.entgroup.getValue())) {
				OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
				for(V3xOrgAccount account:orgManager.getAllAccounts()){
					if (account.getId().equals(-1730833917365171641l)) {
						continue;
					}
					accounts.add(account.getId());
				}
			}else {
				//TODO --lib 需要修改成A8集团版
				accounts.add(OrgConstants.ACCOUNTID);
			}
			edocUpgradeDao.doBasejdqx(sourList,accounts);
		} catch (BusinessException e) {
			//e.printStackTrace();
			LOG.error(e.getMessage(),e);
		}
		LOG.info("节点权限升级结束...");
	}
	
	
	private void presetgovdocForms() {
		LOG.info("预置公文单开始...");
		for (V3xOrgAccount v3xOrgAccount : units) {
			if (v3xOrgAccount.getId().equals(-1730833917365171641l)) {
				continue;
			}
			govdocForm(v3xOrgAccount);
		}
		LOG.info("预置公文单结束...");
	}
	
	private Map<String, Object> govdocForm(File xsnFile,Long fileId,V3xOrgAccount v3xOrgAccount, String state, long categoryId, String rootPath, int formType, long formId) throws BusinessException {
		Map<String, Object> result = new HashMap<String, Object>();
        result.put("success", false);
        FormBean fb = null;
		if (fileId != -1L) {
			File file = xsnFile;
			try {
				file = this.fileManager.getFile(fileId, DateUtil.currentDate());
				if (file == null) {
					file = xsnFile;
				}else {
					file = this.fileManager.decryptionFile(file);
				}
			} catch (Exception e) {
				file = xsnFile;
			}
			V3XFile v3 = this.fileManager.getV3XFile(fileId);
			try {
				InfoPathObject xsf = formApi4Cap3.parseXSN(file);
				//infopath的各种校验
				String check = validateInfopath(xsf,formType,formId);
				if (!"".equals(check)) {
                    result.put("msg", check);
                    return result;
				}
				// 这里主要是进行表格控件查核,如果查核有问题,则会抛出BusinessException,后面会接住此Exception,并return
				for (int i = 0; i < xsf.getViewList().size(); i++) {
					InfoPath_xsl xsl = xsf.getViewList().get(i);
					xsl.covertContent(null);
				}
				if (formId == -1L) {
					fb = formApi4Cap3.getFormBeanForUpgrade2(xsf,formType,v3xOrgAccount.getId());
					// OA-1063 上传infopath表名和实际的不一致
					String fileName = v3.getFilename();
					fb.setFormName(fileName.substring(0, fileName.lastIndexOf(".")));
					fb.setFormType(formType);// 类型
					if(fb.getFormType() == Enums.FormType.govDocSendForm.getKey()||fb.getFormType() == Enums.FormType.govDocReceiveForm.getKey()||fb.getFormType() == Enums.FormType.govDocExchangeForm.getKey()){
						fb.setGovDocFormType(fb.getFormType());
						fb.setFormType(Enums.FormType.processesForm.getKey());
			        }
					// 增加此表单的源xsn文件关系
					List<FormResource> resourceList = (List<FormResource>) fb.getExtraAttr("infoPathResource");
					FormResource fr = new FormResource();
					fr.setId(fileId);
					fr.setFormId(fb.getId());
					fr.setResourceName("name.xsn");
					fr.setPropertyType(FormResourcePropertyTypeEnum.InfoPath.getKey());
					fr.setPropertyName("name.xsn");
					fr.setContent("name.xsn");
					resourceList.add(fr);
					fb.putExtraAttr("infoPathResource", resourceList);
//					formCacheManager.addFormBean(userId, fb);
					fb.setCreatorId(v3xOrgAccount.getId());
			        fb.setOwnerId(v3xOrgAccount.getId());
				}
			} catch (Exception e) {
			    LOG.error(e.getMessage(), e);
                result.put("msg", e.getMessage());
                return result;
			}
		} else {
            result.put("msg", ResourceUtil.getString("form.fielddesign.cantfindfileid"));
            return result;
		}

        //发文单
		//String state = "2";  //state=2

		//预置交换单应用绑定开始
		if(formType==FormType.govDocExchangeForm.getKey()){
			String wfProcessTempleteXML0 = System.currentTimeMillis()+"0";
			String wfProcessTempleteXML1 = System.currentTimeMillis()+"1";
			String wfProcessTempleteXML2 = System.currentTimeMillis()+"2";
			String wfProcessTempleteXML4 = System.currentTimeMillis()+"4";
			String wfProcessTempleteXML5 = System.currentTimeMillis()+"5";
			List<FormAuthViewBean> operationList = fb.getFormViewList().get(0).getAllOperations();
			Long tianxieId = 0l;
			Long shenpiId = 0l;
			String fieldName = "";
			for (FormAuthViewBean operation : operationList) {
        		if("add".equals(operation.getType())){
        			tianxieId = operation.getId();
        		}else if("update".equals(operation.getType())){
        			shenpiId = operation.getId();
        		}
            }
			
			List<FormFieldBean> formFieldBeans = fb.getAllFieldBeans();
			for (FormFieldBean formFieldBean : formFieldBeans) {
				if (ElementUtil.GovDocElement.receive_unit.getCode().equals(formFieldBean.getMappingField())) {
					fieldName = formFieldBean.getName();
					break;
				}
			}
			
			Map<String, String> map = new HashMap<String, String>();
			map.put("id", "");
			map.put("subject", "单位签收流程");
			map.put("govdocContent", "");
			map.put("govdocContentType", " }");
			map.put("govdocBodyType", "OfficeWord");
			map.put("isFlowCopy", "0");
			map.put("process_id", "");
			map.put("taohongTemplete", "-1");
			map.put("process_desc_by", "xml");
			map.put("process_subsetting", "{}");
			map.put("process_rulecontent", "");
			map.put("workflow_newflow_input", "");
			map.put("workflow_node_peoples_input", "");
			map.put("workflow_node_condition_input", "");
			map.put("process_xml_clone", "");
			map.put("process_xml_clone2flowCopy", "");
			map.put("process_event", "{}");
			map.put("process_info", "接收单位单位公文收文员(签收)、上节点部门公文送文员(分办)");
			map.put("colSubject", "");
			map.put("contentType", "41");
			map.put("contentRed", "-1");
			map.put("uploadFile", "");
			map.put("markBind", "");
			map.put("importantLevel", "1");
			map.put("projectId", "");
			map.put("cycleState", "");
			map.put("cycleSender", "");
			map.put("cycleSender_txt", "");
			map.put("cycleStartDate", "");
			map.put("cycleEndDate", "");
			map.put("cycleType", "");
			map.put("cycleMonth", "");
			map.put("cycleOrder", "");
			map.put("cycleDay", "");
			map.put("cycleWeek", "");
			map.put("cycleHour", "");
			map.put("oldDeadlineValue", "");
			map.put("deadline", "0");
			map.put("standardDuration", "0");
			map.put("advanceremind", "0");
			map.put("archiveId", "");
			map.put("archiveName", "");
			map.put("archiveFieldName", "");
			map.put("archiveIsCreate", "true");
			map.put("canTrackWorkFlow", "0");
			map.put("canForward", "");
			map.put("canModify", "1");
			map.put("canArchive", "1");
			map.put("canEditAttachment", "");
			map.put("canMergeDeal", "");
			map.put("templateNumber", "");
			map.put("showSupervisors", "");
			map.put("canSupervise", "1");
			map.put("authRelation_txt", "");
			map.put("authRelation", "");
			map.put("dep_auth_txt", v3xOrgAccount.getName());
			map.put("dep_auth", "Account|"+v3xOrgAccount.getId());
			map.put("contentFileId", UUID.randomUUID().getLeastSignificantBits()+"");
			map.put("view_"+fb.getFormViewList().get(0).getId(), "null");
			map.put("auth_"+fb.getFormViewList().get(0).getId(), tianxieId+"");
			map.put("process_xml", "<ps><p s=\"false\" u=\"\" y=\"0\" x=\"0\" " +
					"t=\"p\" d=\"\" n=\"\" i=\"\"><n a=\"1\" b=\"normal\" c=\"false\" " +
					"l=\"1000\" e=\"0\" f=\"\" g=\"\" h=\"\" y=\"50\" x=\"260\" t=\"6\" " +
					"d=\"\" n=\"上节点部门公文送文员\" " +
					"i=\""+wfProcessTempleteXML4+"\">" +
					"<a a=\"\" b=\""+v3xOrgAccount.getId()+"\" c=\"1\" " +
					"d=\"上节点部门公文送文员\" e=\"\" f=\"NodeUserDepartmentGovdocSend\" " +
					"g=\"Node\" h=\"false\" i=\"false\" j=\"false\" k=\"roleadmin\" />" +
					"<s sa=\"0\" a=\"\" b=\"0\" c=\"1\" z=\"\" " +
					"r=\""+shenpiId+"\" " +
					"e=\""+fb.getFormViewList().get(0).getId()+"\" " +
					"f=\""+fb.getId()+"\" g=\"0\" na=\"0\" w=\"1\" " +
					"v=\"1\" sid=\"\" qid=\"\" h=\"-1\" u=\"-1\" tm=\"1\" j=\"single\" k=\"0\" " +
					"l=\"0\" m=\"false\" s=\"success\" o=\"null\" p=\"null\" q=\"0\" d=\"\" " +
					"t=\"\" n=\"分办\" i=\"fenban\" /></n><n f=\"\" g=\"\" h=\"\" y=\"50\" x=\"50\" " +
					"t=\"8\" d=\"\" n=\"发起者\" i=\"start\"><a a=\"\" b=\"\" c=\"1\" d=\"\" e=\"\" " +
					"f=\"\" g=\"\" h=\"false\" i=\"false\" j=\"false\" k=\"\" /><s sa=\"0\" a=\"\" b=\"0\" " +
					"c=\"1\" z=\"\" r=\""+tianxieId+"\" " +
					"e=\""+fb.getFormViewList().get(0).getId()+"\" " +
					"f=\""+fb.getId()+"\" g=\"0\" na=\"-1\" " +
					"w=\"-1\" v=\"-1\" sid=\"\" qid=\"\" h=\"-1\" u=\"-1\" tm=\"1\" j=\"\" " +
					"k=\"\" l=\"\" m=\"false\" s=\"success\" o=\"\" p=\"\" q=\"\" d=\"\" t=\"\" " +
					"n=\"签收\" i=\"qianshou\" /></n><n f=\"\" g=\"\" h=\"\" y=\"50\" x=\"365\" t=\"4\" " +
					"d=\"\" n=\"end\" i=\"end\"><a a=\"\" b=\"\" c=\"1\" d=\"\" e=\"\" f=\"\" g=\"\" " +
					"h=\"false\" i=\"false\" j=\"false\" k=\"\" /><s sa=\"0\" a=\"\" b=\"0\" c=\"1\" " +
					"z=\"\" r=\""+shenpiId+"\" " +
					"e=\""+fb.getFormViewList().get(0).getId()+"\" " +
					"f=\""+fb.getId()+"\" g=\"0\" na=\"-1\" w=\"-1\" " +
					"v=\"-1\" sid=\"\" qid=\"\" h=\"-1\" u=\"-1\" tm=\"1\" j=\"\" k=\"\" " +
					"l=\"\" m=\"false\" s=\"success\" o=\"\" p=\"\" q=\"\" d=\"\" t=\"\" n=\"签收\" " +
					"i=\"qianshou\" /></n><n a=\"1\" b=\"normal\" c=\"false\" l=\"1000\" e=\"0\" " +
					"f=\"\" g=\"\" h=\"\" y=\"50\" x=\"155\" t=\"6\" d=\"\" n=\"接收单位单位公文收文员\" " +
					"i=\""+wfProcessTempleteXML1+"\"><a a=\"\" " +
					"b=\""+v3xOrgAccount.getId()+"\" c=\"1\" d=\"接收单位单位公文收文员\" e=\"\" " +
					"f=\"AccountAndDepartment@"+fieldName+"#接收单位#"+orgManager.getRoleByName(OrgConstants.Role_NAME.AccountGovdocRec.name(), v3xOrgAccount.getId()).getId()+
					"\" g=\"FormField\" h=\"false\" i=\"false\" j=\"false\" k=\"roleadmin\" />" +
					"<s sa=\"0\" a=\"\" b=\"0\" c=\"1\" z=\"\" " +
					"r=\""+shenpiId+"\" " +
					"e=\""+fb.getFormViewList().get(0).getId()+"\" " +
					"f=\""+fb.getId()+"\" g=\"0\" na=\"-1\" " +
					"w=\"-1\" v=\"-1\" sid=\"\" qid=\"\" h=\"-1\" u=\"-1\" tm=\"1\" j=\"competition\" " +
					"k=\"\" l=\"\" m=\"false\" s=\"success\" o=\"\" p=\"\" q=\"\" d=\"\" t=\"\" " +
					"n=\"签收\" i=\"qianshou\" /></n><l a=\"\" b=\"\" c=\"\" m=\"\" e=\"\" " +
					"h=\"3\" o=\"0\" j=\"end\" " +
					"k=\""+wfProcessTempleteXML4+"\" " +
					"t=\"11\" d=\"\" n=\"\" " +
					"i=\""+wfProcessTempleteXML0+"\" />" +
					"<l a=\"\" b=\"\" c=\"\" m=\"\" e=\"\" h=\"3\" o=\"0\" " +
					"j=\""+wfProcessTempleteXML4+"\" " +
					"k=\""+wfProcessTempleteXML1+"\" " +
					"t=\"11\" d=\"\" n=\"\" i=\""+wfProcessTempleteXML5+"\" />" +
					"<l a=\"\" b=\"\" c=\"\" m=\"\" e=\"\" h=\"3\" o=\"0\" " +
					"j=\""+wfProcessTempleteXML1+"\" " +
					"k=\"start\" t=\"11\" d=\"\" n=\"\" " +
					"i=\""+wfProcessTempleteXML2+"\" /></p></ps>");

			map.put("groupNewAccountId", v3xOrgAccount.getId()+"");
			map.put("groupNewMemberId", v3xOrgAccount.getId()+"");
			map.put("groupNewBatchId", UUID.randomUUID().getLeastSignificantBits()+"");
			
			formApi4Cap3.saveOrUpdateFlowTemplate4Upgrade(map, new ArrayList<Map<String,String>>(),v3xOrgAccount,fb);
		}
		//预置交换单应用绑定结束
		
		fb.setCategoryId(categoryId);
		fb.setOwnerId(v3xOrgAccount.getId());
		GovDocUtil.saveGovdocExtendAndSort(fb, govdocFormExtendManager, govdocFormOpinionSortManager);
		fb.setState(Integer.parseInt(state));
		
		fb.putExtraAttr("groupNewAccountId", v3xOrgAccount.getId()+"");

		List<FormViewBean> viewList = fb.getFormViewList();
        for (FormViewBean view : viewList) {
            List<FormAuthViewBean> operationList = view.getAllOperations();
            for (FormAuthViewBean operation : operationList) {
            	List<FormAuthViewFieldBean> authViewFieldBeans = operation.getFormAuthorizationFieldList();
            	for (FormAuthViewFieldBean formAuthViewFieldBean : authViewFieldBeans) {
            		if("add".equals(operation.getType())){
            			formAuthViewFieldBean.setAccess(FieldAccessType.edit.getKey());
            		}else{
            			formAuthViewFieldBean.setAccess(FieldAccessType.browse.getKey());
            		}
				}
            }
            
        }
		
		FormOwner formOwner = formApi4Cap3.getFormOwner(fb.getId());
		//屏蔽--许才兵
//		deeDesignManager.setDeeTask4FormField(fb);
		try {
			if (formApi4Cap3.isNewForm(fb.getId())) {
				if (fb.needCheckTotalNum()) {
					BizValidateResultVO resultVO = formApi4Cap3.validateBiz(BizOperationEnum.create_form_save);
					if (!resultVO.isSuccess()) {
						throw new BizException(resultVO.getCode());
					}
				}
				fb.setSourceStr(formApi4Cap3.getEncodeString(formApi4Cap3.getCreateSourceType(BizOperationEnum.create_form_save), fb.getId()));
			}
			if (fb.getGovDocFormType() == Enums.FormType.govDocSendForm.ordinal()) {
				String item = EnumNameEnum.edoc_send_permission_policy.name();
				String newItem = EnumNameEnum.edoc_new_send_permission_policy.name();
				fb.setFormPermissionConfig(this.getFormPermissionConfig(item, newItem, v3xOrgAccount.getId(), fb,null));
			}else if (fb.getGovDocFormType() == Enums.FormType.govDocReceiveForm.ordinal()) {
				String item = EnumNameEnum.edoc_rec_permission_policy.name();
				String newItem = EnumNameEnum.edoc_new_rec_permission_policy.name();
				fb.setFormPermissionConfig(this.getFormPermissionConfig(item, newItem, v3xOrgAccount.getId(), fb,null));
			}
			presetForms.add(fb.getId());
			formApi4Cap3.saveOrUpdateFormBean(fb);
		} catch (BizException be) {
			result.put("msg", ResourceUtil.getString(be.getErrorCode()));
            return result;
		} catch (BusinessException e) {
			if(e.getCause()!=null && e.getCause().getMessage().contains("65535")){
				//OA-60624 MYSQL字段超出范围 报错控制
				result.put("msg", ResourceUtil.getString("form.baseinfo.totalLength.formDesignSave.error"));
	            return result;
			}else{
				throw e;
			}
		} catch (Exception e) {
			result.put("msg", "单位"+ v3xOrgAccount.getName() + "预置文单《"+fb.getFormName()+"》发生异常！");
			LOG.error(e.getMessage(),e);
            return result;
		}
		
		//设置为默认文单
//		boolean defaultFlag = false;
//		if(formType==6 || formType==8 || formType==9){//以前是5 7 8 5被表单动态表占用 Enum.java
//			defaultFlag = formApi4Cap3.setDefaultGovdocForm(String.valueOf(fb.getId()), formType, String.valueOf(v3xOrgAccount.getId()), String.valueOf(categoryId));
//		}else{
//			defaultFlag = true;
//		}
//		if(!defaultFlag){
//        	result.put("msg", ResourceUtil.getString("form.formlist.setdefaultgovdocfailed"));
//            return result;
//        }
		result.put("success", true);
        
		return result;
	}
	
	
	/**
	 * 预置发文单，收文单，签收单数据
	 * @param v3xOrgAccount
	 */
	private void govdocForm(V3xOrgAccount v3xOrgAccount) {
		long userId = v3xOrgAccount.getId();
		String applicationFolder = SystemEnvironment.getApplicationFolder();
		String baseFolder = SystemEnvironment.getBaseFolder();
		
		try {
			fileManager.getFolder(new Date(), true);
		} catch (BusinessException e1) {
			// TODO Auto-generated catch block
			LOG.error(e1);
			//e1.printStackTrace();
		}
		
		try {
			Map<String, Object> result = new HashMap<String, Object>();
			LOG.info("G6-V5.7初始化单位："+v3xOrgAccount.getName()+"发文单数据开始...");
			File fileInGovDocSendForm = new File(applicationFolder+File.separator+"apps_res"+File.separator+"form"+File.separator+"file"+File.separator+"govDocSendForm");
			Date dateGovDocSendForm = new Date();
			V3XFile v3xFileGovDocSendForm = new V3XFile();
			v3xFileGovDocSendForm.setIdIfNew();
			v3xFileGovDocSendForm.setCategory(1);
			v3xFileGovDocSendForm.setType(0);
			v3xFileGovDocSendForm.setFilename("发文单.xsn");
			v3xFileGovDocSendForm.setMimeType("application/octet-stream");
			v3xFileGovDocSendForm.setCreateDate(dateGovDocSendForm);
			v3xFileGovDocSendForm.setCreateMember(userId);
			v3xFileGovDocSendForm.setSize(fileInGovDocSendForm.length());
			v3xFileGovDocSendForm.setDescription("");
			v3xFileGovDocSendForm.setUpdateDate(dateGovDocSendForm);
			v3xFileGovDocSendForm.setAccountId(v3xOrgAccount.getId());
			v3xFileDAO.save(v3xFileGovDocSendForm);
			File fileOutGovDocSendForm = new File(baseFolder+File.separator+"upload"+File.separator+
					new SimpleDateFormat("yyyy").format(dateGovDocSendForm)+
					File.separator+new SimpleDateFormat("MM").format(dateGovDocSendForm)+
					File.separator+new SimpleDateFormat("dd").format(dateGovDocSendForm)+
					File.separator+v3xFileGovDocSendForm.getId());

			try {
				FileUtil.copyFile(fileInGovDocSendForm, fileOutGovDocSendForm);
			} catch (Exception e) {
				fileOutGovDocSendForm = fileInGovDocSendForm;
			}
			result = govdocForm(fileInGovDocSendForm,v3xFileGovDocSendForm.getId(), v3xOrgAccount, "2", ModuleType.govdocSend.getKey(), SystemEnvironment.getContextPath(), FormType.govDocSendForm.getKey(), -1l);
			if((Boolean) result.get("success")){
				LOG.info("G6-V5.7初始化单位："+v3xOrgAccount.getName()+"发文单数据成功...");
			}else{
				LOG.error("G6-V5.7初始化单位："+v3xOrgAccount.getName()+"发文单数据失败..."+result.get("msg"));
			}
			LOG.info("G6-V5.7初始化单位："+v3xOrgAccount.getName()+"发文单数据结束...");
			LOG.info("G6-V5.7初始化单位："+v3xOrgAccount.getName()+"收文单数据开始...");
			File fileInGovDocReceiveForm = new File(applicationFolder+File.separator+"apps_res"+File.separator+"form"+File.separator+"file"+File.separator+"govDocReceiveForm");
			Date dateGovDocReceiveForm = new Date();
			V3XFile v3xFileGovDocReceiveForm = new V3XFile();
			v3xFileGovDocReceiveForm.setIdIfNew();
			v3xFileGovDocReceiveForm.setCategory(1);
			v3xFileGovDocReceiveForm.setType(0);
			v3xFileGovDocReceiveForm.setFilename("收文单.xsn");
			v3xFileGovDocReceiveForm.setMimeType("application/octet-stream");
			v3xFileGovDocReceiveForm.setCreateDate(dateGovDocReceiveForm);
			v3xFileGovDocReceiveForm.setCreateMember(userId);
			v3xFileGovDocReceiveForm.setSize(fileInGovDocReceiveForm.length());
			v3xFileGovDocReceiveForm.setDescription("");
			v3xFileGovDocReceiveForm.setUpdateDate(dateGovDocReceiveForm);
			v3xFileGovDocReceiveForm.setAccountId(v3xOrgAccount.getId());
			v3xFileDAO.save(v3xFileGovDocReceiveForm);
			File fileOutGovDocReceiveForm = new File(baseFolder+File.separator+"upload"+File.separator+
					new SimpleDateFormat("yyyy").format(dateGovDocReceiveForm)+
					File.separator+new SimpleDateFormat("MM").format(dateGovDocReceiveForm)+
					File.separator+new SimpleDateFormat("dd").format(dateGovDocReceiveForm)+
					File.separator+v3xFileGovDocReceiveForm.getId());
			try {
				FileUtil.copyFile(fileInGovDocReceiveForm, fileOutGovDocReceiveForm);
			} catch (Exception e) {
				fileOutGovDocReceiveForm = fileInGovDocReceiveForm;
			}
			result = govdocForm(fileInGovDocReceiveForm,v3xFileGovDocReceiveForm.getId(), v3xOrgAccount, "2", ModuleType.govdocRec.getKey(), SystemEnvironment.getContextPath(), FormType.govDocReceiveForm.getKey(), -1l);
			if((Boolean) result.get("success")){
				LOG.info("G6-V5.7初始化单位："+v3xOrgAccount.getName()+"收文单数据成功...");
			}else{
				LOG.error("G6-V5.7初始化单位："+v3xOrgAccount.getName()+"收文单数据失败..."+result.get("msg"));
			}
			LOG.info("G6-V5.7初始化单位："+v3xOrgAccount.getName()+"收文单数据结束...");
			LOG.info("G6-V5.7初始化单位："+v3xOrgAccount.getName()+"签报单数据开始...");
			File fileInGovDocSignForm = new File(applicationFolder+File.separator+"apps_res"+File.separator+"form"+File.separator+"file"+File.separator+"govDocSignForm");
			Date dateGovDocSignForm = new Date();
			V3XFile v3xFileGovDocSignForm = new V3XFile();
			v3xFileGovDocSignForm.setIdIfNew();
			v3xFileGovDocSignForm.setCategory(1);
			v3xFileGovDocSignForm.setType(0);
			v3xFileGovDocSignForm.setFilename("签报单.xsn");
			v3xFileGovDocSignForm.setMimeType("application/octet-stream");
			v3xFileGovDocSignForm.setCreateDate(dateGovDocSignForm);
			v3xFileGovDocSignForm.setCreateMember(userId);
			v3xFileGovDocSignForm.setSize(fileInGovDocSignForm.length());
			v3xFileGovDocSignForm.setDescription("");
			v3xFileGovDocSignForm.setUpdateDate(dateGovDocSignForm);
			v3xFileGovDocSignForm.setAccountId(v3xOrgAccount.getId());
			v3xFileDAO.save(v3xFileGovDocSignForm);
			File fileOutGovDocSignForm = new File(baseFolder+File.separator+"upload"+File.separator+
					new SimpleDateFormat("yyyy").format(dateGovDocSignForm)+
					File.separator+new SimpleDateFormat("MM").format(dateGovDocSignForm)+
					File.separator+new SimpleDateFormat("dd").format(dateGovDocSignForm)+
					File.separator+v3xFileGovDocSignForm.getId());
			try {
				FileUtil.copyFile(fileInGovDocSignForm, fileOutGovDocSignForm);
			} catch (Exception e) {
				fileOutGovDocSignForm = fileInGovDocSignForm;
			}
			result = govdocForm(fileInGovDocSignForm,v3xFileGovDocSignForm.getId(), v3xOrgAccount, "2", ModuleType.govdocSign.getKey(), SystemEnvironment.getContextPath(), FormType.govDocSignForm.getKey(), -1l);
			if((Boolean) result.get("success")){
				LOG.info("G6-V5.7初始化单位："+v3xOrgAccount.getName()+"签报单数据成功...");
			}else{
				LOG.error("G6-V5.7初始化单位："+v3xOrgAccount.getName()+"签报单数据失败..."+result.get("msg"));
			}
			LOG.info("G6-V5.7初始化单位："+v3xOrgAccount.getName()+"签报单数据结束...");
			LOG.info("G6-V5.7初始化单位："+v3xOrgAccount.getName()+"交换单数据开始...");
			File fileInGovDocExchangeForm = new File(applicationFolder+File.separator+"apps_res"+File.separator+"form"+File.separator+"file"+File.separator+"govDocExchangeForm");
			Date dateGovDocExchangeForm = new Date();
			V3XFile v3xFileGovDocExchangeForm = new V3XFile();
			v3xFileGovDocExchangeForm.setIdIfNew();
			v3xFileGovDocExchangeForm.setCategory(1);
			v3xFileGovDocExchangeForm.setType(0);
			v3xFileGovDocExchangeForm.setFilename("签收单.xsn");
			v3xFileGovDocExchangeForm.setMimeType("application/octet-stream");
			v3xFileGovDocExchangeForm.setCreateDate(dateGovDocExchangeForm);
			v3xFileGovDocExchangeForm.setCreateMember(userId);
			v3xFileGovDocExchangeForm.setSize(fileInGovDocExchangeForm.length());
			v3xFileGovDocExchangeForm.setDescription("");
			v3xFileGovDocExchangeForm.setUpdateDate(dateGovDocExchangeForm);
			v3xFileGovDocExchangeForm.setAccountId(v3xOrgAccount.getId());
			v3xFileDAO.save(v3xFileGovDocExchangeForm);
			File fileOutGovDocExchangeForm = new File(baseFolder+File.separator+"upload"+File.separator+
					new SimpleDateFormat("yyyy").format(dateGovDocExchangeForm)+
					File.separator+new SimpleDateFormat("MM").format(dateGovDocExchangeForm)+
					File.separator+new SimpleDateFormat("dd").format(dateGovDocExchangeForm)+
					File.separator+v3xFileGovDocExchangeForm.getId());
			try {
				FileUtil.copyFile(fileInGovDocExchangeForm, fileOutGovDocExchangeForm);
			} catch (Exception e) {
				fileOutGovDocExchangeForm = fileInGovDocExchangeForm;
			}
			result = govdocForm(fileInGovDocExchangeForm,v3xFileGovDocExchangeForm.getId(), v3xOrgAccount, "2", ModuleType.govdocExchange.getKey(), SystemEnvironment.getContextPath(), FormType.govDocExchangeForm.getKey(), -1l);
			if((Boolean) result.get("success")){
				LOG.info("G6-V5.7初始化单位："+v3xOrgAccount.getName()+"交换单数据成功...");
			}else{
				LOG.error("G6-V5.7初始化单位："+v3xOrgAccount.getName()+"交换单数据失败..."+result.get("msg"));
			}
			LOG.info("G6-V5.7初始化单位："+v3xOrgAccount.getName()+"交换单数据结束...");
		} catch (BusinessException e) {
			LOG.error("创建单位:"+v3xOrgAccount.getName()+"初始化文单出错！",e);
			//e.printStackTrace();
		}
	}
	
	private void createFormByEdocForm() throws BusinessException {
		LOG.info("公文单升级开始...");

		//升级所有的发文种类
		List<EdocCategory> edocCategories = edocUpgradeDao.getAllEdocCategory();
		
		for (EdocCategory edocCategory : edocCategories) {
			if (categories.get(edocCategory.getId()) == null) {
				CtpTemplateCategory category = new CtpTemplateCategory();
				category.setNewId();
				category.setParentId(401L);
				category.setType(401);
				category.setName(edocCategory.getName());
				category.setSort(1);
				category.setOrgAccountId(edocCategory.getAccountId());
				templateManager.saveCtpTemplateCategory(category);
				categories.put(edocCategory.getId(), category);
			}
		}
		List<EdocForm> edocForms = edocUpgradeDao.getAllEdocForm();
		V3XFile v3xFileGovDocSendForm = null;
		Long fileId = null;
		Long userId = null;
		Long categoryId = null;
		String rootPath = SystemEnvironment.getContextPath();
		Integer formType = null;
		boolean isDefaultForm = false;
		for (EdocForm edocForm : edocForms) {
			int useFlag = 1;
			try {
				LOG.info("edocForm.name="+edocForm.getName()+",id="+edocForm.getId()
				+",size="+ (null != edocForm.getEdocFormExtendInfo() ? edocForm.getEdocFormExtendInfo().size() : "0"));
				for (EdocFormExtendInfo info : edocForm.getEdocFormExtendInfo()) {
					LOG.info(edocForm.getDomainId()+"**"+info.getAccountId()+"**"+edocForm.getDomainId().equals(info.getAccountId())+"**"+info.getStatus());
					if (edocForm.getDomainId().equals(info.getAccountId())) {
						useFlag = info.getStatus();
						isDefaultForm = info.getIsDefault();
						break;
					}
				}
			} catch (Exception e2) {
			}
			if (formIds.get(edocForm.getId()) != null) {
				continue;
			}
			if (edocForm.getDomainId().equals(1l)) {
				continue;
			}
			String item = "";
			String newItem = "";
			if (edocForm.getType() == 0) {
				categoryId = Long.valueOf(ModuleType.govdocSend.getKey());
				formType = FormType.govDocSendForm.getKey();
				item = EnumNameEnum.edoc_send_permission_policy.name();
				newItem = EnumNameEnum.edoc_new_send_permission_policy.name();
			}else if (edocForm.getType() == 1) {
				categoryId = Long.valueOf(ModuleType.govdocRec.getKey());
				formType = FormType.govDocReceiveForm.getKey();
				item = EnumNameEnum.edoc_rec_permission_policy.name();
				newItem = EnumNameEnum.edoc_new_rec_permission_policy.name();
			}else if (edocForm.getType() == 2) {
				categoryId = Long.valueOf(ModuleType.govdocSign.getKey());
				formType = FormType.govDocSignForm.getKey();
				item = EnumNameEnum.edoc_qianbao_permission_policy.name();
				newItem = EnumNameEnum.edoc_new_qianbao_permission_policy.name();
			}
//			if (edocForm.getSubType() != null) { 去掉原因：协同V5 OA-164085公文升级7.1后，升级前系统预制文单升级后所属应用跑到行政公文分类下了 
//				CtpTemplateCategory category = categories.get(edocForm.getSubType());
//				if (category != null) {
//					categoryId = category.getId();
//				}
//			}
			userId = edocForm.getCreateUserId();
			
			/**
			 * OA-168206 老文单升级不成功 createuserid 为-8060678314709529794 在org_member表里面找不到这里统一改成单位管理员ID
			 * a6设置成系统管理员 A8设置成单位管理员
			 */
			if(userId != null && userId.equals(-8060678314709529794L)){
				if(ProductEditionEnum.getCurrentProductEditionEnum().getValue().equals(ProductEditionEnum.a6.getValue())
				 || ProductEditionEnum.getCurrentProductEditionEnum().getValue().equals(ProductEditionEnum.a6s.getValue())){
					edocForm.setCreateUserId(-7273032013234748168L);
					userId = -7273032013234748168L;
				}else{
					edocForm.setCreateUserId(-884316703172445L);
					userId = -884316703172445L;
				}
			}
			
			V3xOrgMember member = orgManager.getMemberById(userId);
//            if (member == null || member.getId() == 5725175934914479521L) {//5725175934914479521集团管理员
//            	userId = edocForm.getDomainId();
//            }
            if (member == null) {//5725175934914479521集团管理员
            	LOG.error("公文单《" + edocForm.getName()+"》跳过升级，原因：没有创建者ID" + edocForm.getId());
            	continue;
            }
			File xsnFile = null;
			if (edocForm.getIsSystem()) {
				xsnFile = new File(SystemEnvironment.getApplicationFolder()+File.separator+"apps_res"+File.separator+"edoc"+File.separator+"file"+File.separator+"form"+File.separator+edocForm.getFileId());
			}else{
				V3XFile file = v3xFileDAO.get(edocForm.getFileId());
				if (file != null) {
					xsnFile = fileManager.getFile(edocForm.getFileId(), file.getCreateDate());
				}
			}
			if(xsnFile == null ){
				LOG.error("表单《" + edocForm.getName()+"》xsn文件未找到，请检查upload文件夹！");
				continue;
			}
			v3xFileGovDocSendForm = new V3XFile();
			v3xFileGovDocSendForm.setIdIfNew();
			v3xFileGovDocSendForm.setCategory(1);
			v3xFileGovDocSendForm.setType(0);
			v3xFileGovDocSendForm.setFilename(edocForm.getName() + ".xsn");
			v3xFileGovDocSendForm.setMimeType("application/octet-stream");
			v3xFileGovDocSendForm.setCreateDate(edocForm.getCreateTime());
			v3xFileGovDocSendForm.setCreateMember(edocForm.getCreateUserId());
			v3xFileGovDocSendForm.setSize(xsnFile.length());
			v3xFileGovDocSendForm.setDescription("");
			v3xFileGovDocSendForm.setUpdateDate(edocForm.getLastUpdate());
			v3xFileGovDocSendForm.setAccountId(edocForm.getDomainId());
			v3xFileDAO.save(v3xFileGovDocSendForm);
			fileId = v3xFileGovDocSendForm.getId();
			String baseFolder = SystemEnvironment.getBaseFolder();
			File fileOutGovDocSendForm = new File(baseFolder + File.separator + "upload" + File.separator
					+ new SimpleDateFormat("yyyy").format(edocForm.getCreateTime()) + File.separator
					+ new SimpleDateFormat("MM").format(edocForm.getCreateTime()) + File.separator
					+ new SimpleDateFormat("dd").format(edocForm.getCreateTime()) + File.separator
					+ v3xFileGovDocSendForm.getId());
			try {
				FileUtil.copyFile(xsnFile, fileOutGovDocSendForm);
			} catch (Exception e1) {
				LOG.error("文单：《" + edocForm.getName() + "》(" + edocForm.getId() + ")升级发生异常！");
				LOG.error(e1.getMessage(),e1);
				continue;
			}
			File file = xsnFile;
			try {
				file = this.fileManager.getFile(v3xFileGovDocSendForm.getId(), edocForm.getCreateTime());
				if (file == null) {
					file = xsnFile;
				}else {
					file = this.fileManager.decryptionFile(file);
				}
			} catch (Exception e) {
				file = xsnFile;
			}
			V3XFile v3 = this.fileManager.getV3XFile(v3xFileGovDocSendForm.getId());
	        Map<String, String> opitionSort = new HashMap<String, String>();
	        FormBean fb = null;
			try {
				InfoPathObject xsf = formApi4Cap3.parseXSN(file);
				// infopath的各种校验
				String check = validateInfopath(xsf, formType, -1);
				if (!"".equals(check)) {
					LOG.error("文单：《" + edocForm.getName() + "》(" + edocForm.getId() + ")升级发生异常！(infopath的各种校验发生异常)");
					LOG.error(check);
					continue;
				}
				// 这里主要是进行表格控件查核,如果查核有问题,则会抛出BusinessException,后面会接住此Exception,并return
				for (int i = 0; i < xsf.getViewList().size(); i++) {
					InfoPath_xsl xsl = xsf.getViewList().get(i);
					xsl.covertContent(null);
				}
				fb = formApi4Cap3.getFormBeanForUpgrade(xsf, formType,edocForm.getDomainId(),opitionSort);
				// OA-1063 上传infopath表名和实际的不一致
				String fileName = v3.getFilename();

				if (edocForm.getIsSystem()) {
					fb.setFormName(edocForm.getName()+"(老数据预置文单)");
				}else{
					fb.setFormName(edocForm.getName());
				}
				fb.setUseFlag(useFlag);
				fb.setFormType(formType);// 类型
				if(fb.getFormType() == Enums.FormType.govDocSendForm.getKey()||fb.getFormType() == Enums.FormType.govDocReceiveForm.getKey()||fb.getFormType() == Enums.FormType.govDocExchangeForm.getKey()){
					fb.setGovDocFormType(fb.getFormType());
					fb.setFormType(Enums.FormType.processesForm.getKey());
		        }
				// 增加此表单的源xsn文件关系
				List<FormResource> resourceList = (List<FormResource>) fb.getExtraAttr("infoPathResource");
				FormResource fr = new FormResource();
				fr.setId(fileId);
				fr.setFormId(fb.getId());
				fr.setResourceName("name.xsn");
				fr.setPropertyType(FormResourcePropertyTypeEnum.InfoPath.getKey());
				fr.setPropertyName("name.xsn");
				fr.setContent("name.xsn");
				resourceList.add(fr);
				fb.putExtraAttr("infoPathResource", resourceList);
				// formCacheManager.addFormBean(userId, fb);
				fb.setCreateDate(edocForm.getCreateTime());
				fb.setCreatorId(edocForm.getCreateUserId());
			} catch (Exception e) {
				LOG.error("文单：《" + edocForm.getName() + "》(" + edocForm.getId() + ")升级发生异常！");
				LOG.error(e.getMessage(),e);
				continue;
			}
			// 发文单
			// String state = "2"; //state=2
			fb.setCategoryId(categoryId);
			fb.setOwnerId(userId);
			GovDocUtil.saveGovdocExtendAndSort(fb, govdocFormExtendManager, govdocFormOpinionSortManager);
			fb.setState(2);
			fb.putExtraAttr("groupNewAccountId", edocForm.getDomainId() + "");
			List<FormViewBean> viewList = fb.getFormViewList();
			
			try {
				formApi4Cap3.saveOrUpdate(this.getFormPermissionConfig(item, newItem, edocForm.getDomainId(), fb,edocForm));
			} catch (Exception e1) {
				LOG.error("文单：《" + edocForm.getName() + "》(" + edocForm.getId() + ")升级发生异常！");
				LOG.error("文单：《" + edocForm.getName() + "》(" + edocForm.getId() + ")权限设置异常！");
				LOG.error(e1.getMessage(),e1);
				continue;
			}
			//屏蔽--许才兵
//			deeDesignManager.setDeeTask4FormField(fb);
			
			//保存意见设置。
			String optionFormatSet = "";
			Set<EdocFormExtendInfo> infos = edocForm.getEdocFormExtendInfo();
	        if(infos != null) {
	            for(EdocFormExtendInfo info : infos) {
	                if(info.getAccountId().longValue() == edocForm.getDomainId()) {
	                	edocForm.setStatus(info.getStatus());
	                	edocForm.setIsDefault(info.getIsDefault());
	                    //bean.setWebOpinionSet(info.getOptionFormatSet());//公文单配置，JSON字符串
	                    //公文配置详细，和新建保持一致
	                    optionFormatSet = info.getOptionFormatSet();
	                }
	            }
	        }
	        GovdocFormExtend govdocFormExtend = new GovdocFormExtend();
        	govdocFormExtend.setAccountId(edocForm.getDomainId());
        	govdocFormExtend.setId(UUIDLong.longUUID());
        	govdocFormExtend.setFormId(fb.getId());
        	if (!"".equals(optionFormatSet)) {
        		govdocFormExtend.setOptionFormatSet(optionFormatSet);
			}else{
				govdocFormExtend.setOptionFormatSet(FormOpinionConfig.getDefualtConfig());
			}
        	govdocFormExtendManager.saveOrUpdate(govdocFormExtend);
        	
        	List<FormFieldBean> formFieldBeans = fb.getAllFieldBeans();
            List<FormFieldBean> list  = new ArrayList<FormFieldBean>();
            for (FormFieldBean formFieldBean : formFieldBeans) {
            	//如果是意见类型，加入到list中
            	if("edocflowdealoption".equals(formFieldBean.getInputType())){
    						list.add(formFieldBean);
            	}
    		}
            List<String> listStr = new ArrayList<String>();
            for (FormFieldBean formFieldBean : list) {
				listStr.add(formFieldBean.getMappingField());
			}
        	
			try {
				List<FormBoundPerm> processList = EdocHelper.getProcessOpinionByEdocFormId4Upgrade(listStr, edocForm.getId(), edocForm.getType(), edocForm.getDomainId(), false);
				List<GovdocFormOpinionSort> govdocFormOpinionSorts = new ArrayList<GovdocFormOpinionSort>();
				for (FormFieldBean formFieldBean : formFieldBeans) {
					if(formFieldBean.getInputType().equals(FormFieldComEnum.EDOCFLOWDEALOPITION.getKey())){
						GovdocFormOpinionSort govdocFormOpinionSort = null;
						if(Strings.isNotEmpty(processList)){
							for (FormBoundPerm formBoundPerm : processList) {
								if (formBoundPerm.getProcessName() != null && formBoundPerm.getProcessName().equals(formFieldBean.getMappingField())) {
									govdocFormOpinionSort = new GovdocFormOpinionSort();
									govdocFormOpinionSort.setId(UUIDLong.longUUID());
									govdocFormOpinionSort.setIdIfNew();
									govdocFormOpinionSort.setFormId(fb.getId());
									govdocFormOpinionSort.setProcessName(formFieldBean.getName());
									govdocFormOpinionSort.setSortType(formBoundPerm.getSortType());
									govdocFormOpinionSort.setDomainId(edocForm.getDomainId());
									govdocFormOpinionSorts.add(govdocFormOpinionSort);
									
								}
							}
						}
					}
				}
				for (String key : opitionSort.keySet()) {
					GovdocFormOpinionSort govdocFormOpinionSort = new GovdocFormOpinionSort();
					govdocFormOpinionSort.setId(UUIDLong.longUUID());
					govdocFormOpinionSort.setIdIfNew();
					govdocFormOpinionSort.setFormId(fb.getId());
					govdocFormOpinionSort.setProcessName(key);
					govdocFormOpinionSort.setSortType(opitionSort.get(key));
					govdocFormOpinionSort.setDomainId(edocForm.getDomainId());
					govdocFormOpinionSorts.add(govdocFormOpinionSort);
				}
				govdocFormOpinionSortManager.saveOrUpdateList(govdocFormOpinionSorts);
			} catch (Exception e1) {
				LOG.error("文单：《" + edocForm.getName() + "》(" + edocForm.getId() + ")升级发生异常！");
				LOG.error("文单：《" + edocForm.getName() + "》(" + edocForm.getId() + ")文单格式设置异常！");
				LOG.error(e1.getMessage(),e1);
			}
			try {
				if (formApi4Cap3.isNewForm(fb.getId())) {
					if (fb.needCheckTotalNum()) {
						BizValidateResultVO resultVO = formApi4Cap3.validateBiz(BizOperationEnum.create_form_save);
						if (!resultVO.isSuccess()) {
							throw new BizException(resultVO.getCode());
						}
					}
					fb.setSourceStr(formApi4Cap3.getEncodeString(
					        formApi4Cap3.getCreateSourceType(BizOperationEnum.create_form_save), fb.getId()));
				}
				formApi4Cap3.saveOrUpdateFormBean(fb);
				
				//升级默认文单
				if(isDefaultForm){
					boolean defaultFlag = false;
					if(formType==6 || formType==8 || formType==9){//以前是5 7 8 5被表单动态表占用 Enum.java
						defaultFlag = formApi4Cap3.setDefaultGovdocForm(String.valueOf(fb.getId()), formType, String.valueOf(edocForm.getDomainId()), String.valueOf(categoryId));
					}
					if(!defaultFlag){
						LOG.info("公文单默认升级"+ResourceUtil.getString("form.formlist.setdefaultgovdocfailed"));
			        }
				}
				
			} catch (BizException be) {
				LOG.error("文单：《" + edocForm.getName() + "》(" + edocForm.getId() + ")升级发生异常！");
				LOG.error(be);
				//be.printStackTrace();
				continue;
			} catch (BusinessException e) {
				LOG.error("文单：《" + edocForm.getName() + "》(" + edocForm.getId() + ")升级发生异常！");
				//e.printStackTrace();
				if (e.getCause() != null && e.getCause().getMessage().contains("65535")) {
					// OA-60624 MYSQL字段超出范围 报错控制
					LOG.error(ResourceUtil.getString("form.baseinfo.totalLength.formDesignSave.error"));
					continue;
				} else {
					LOG.error("文单：《" + edocForm.getName() + "》(" + edocForm.getId() + ")升级发生异常！");
					LOG.error(e.getMessage(),e);
					continue;
				}
			}
			edocUpgradeDao.upgradeAIP(edocForm.getId(), fb.getId());
			formIds.put(edocForm.getId(), fb.getId());
			
		}
		LOG.info("公文单升级结束...");
	}

	private FormPermissionConfig getFormPermissionConfig(String oldItem,String newItem,Long unitId,FormBean formBean,EdocForm edocForm) throws Exception{
		if (edocForm == null) {
			FormPermissionConfig formPermissionConfig = new FormPermissionConfig();
			formPermissionConfig.setId(UUIDLong.longUUID());
			formPermissionConfig.setFormId(formBean.getId());
			Map<String, String> map = new HashMap<String, String>();
			FlipInfo flipInfo = new FlipInfo();
	    	flipInfo.setPage(-1);
	    	flipInfo.setSize(-1);
	        List<PermissionVO> p_list = edocUpgradeDao.getPermission(newItem,unitId);
			for (PermissionVO permissionVO : p_list) {
				String relation = null;
				for (FormViewBean formViewBean : formBean.getAllViewList()) {
					if (relation != null) {
						break;
					}
					for (FormAuthViewBean formAuthViewBean : formViewBean.getAllOperations()) {
						if ("niwen".equals(permissionVO.getName()) || "dengji".equals(permissionVO.getName())) {
							if ("add".equals(formAuthViewBean.getType())) {
								relation = formViewBean.getId() + "." + formAuthViewBean.getId() + ".isDefault";
								break;
							}
						}else{
							if ("readonly".equals(formAuthViewBean.getType())) {
								relation = formViewBean.getId() + "." + formAuthViewBean.getId() + ".isDefault";
								break;
							}
						}
					}
				}
				map.put(permissionVO.getFlowPermId().toString(), relation);
			}
			formPermissionConfig.setConfig(JSONUtil.toJSONString(map));
			return formPermissionConfig;
		}
		//老公文节点权限集合
        List<PermissionVO> oldPermissionList = edocUpgradeDao.getPermission(oldItem,unitId);
        //新公文节点权限集合
        List<PermissionVO> newPermissionList = edocUpgradeDao.getPermission(newItem,unitId);
        FormPermissionConfig formPermissionConfig = new FormPermissionConfig();
        formPermissionConfig.setIdIfNew();
        formPermissionConfig.setFormId(formBean.getId());
        Map<String, String> config = new HashMap<String, String>();
        List<FormViewBean> viewList = formBean.getAllViewList();
        if (CollectionUtils.isNotEmpty(viewList)) {
        	FormViewBean view = viewList.get(0);
        	List<FormAuthViewBean> operationList = view.getAllOperations();
        	FormAuthViewBean tempFormAuthViewBean = null;
        	for (FormAuthViewBean operation : operationList) {
        		List<FormAuthViewFieldBean> authViewFieldBeans = operation.getFormAuthorizationFieldList();
        		for (FormAuthViewFieldBean formAuthViewFieldBean : authViewFieldBeans) {
        			if ("add".equals(operation.getType())) {
        				formAuthViewFieldBean.setAccess(FieldAccessType.edit.getKey());
        			} else {
        				formAuthViewFieldBean.setAccess(FieldAccessType.browse.getKey());
        			}
        		}
        		if ("update".equals(operation.getType())) {
        			tempFormAuthViewBean = (FormAuthViewBean) operation.clone();
        		}
        	}
        	for (PermissionVO permission : oldPermissionList) {
        		List<EdocFormFlowPermBound> list = null;
        		if (edocForm != null) {
        			list = edocFormManager.findBoundByFormId(edocForm.getId(), unitId, permission.getName());
        		}
        		List<EdocElementFlowPermAcl> acl_list = edocElementFlowPermAclManager.getEdocElementFlowPermAcls(permission.getFlowPermId());
        		FormAuthViewBean formAuthViewBean = null;
        		if ("niwen".equals(permission.getName())) {
        			for (FormAuthViewBean viewBean : operationList) {
        				if ("add".equals(viewBean.getType())) {
        					formAuthViewBean = viewBean;
        					break;
        				}
        			}
        		}else{
        			formAuthViewBean = (FormAuthViewBean) tempFormAuthViewBean.clone();
        			formAuthViewBean.setNewId();
        			if("fengfa".equals(permission.getName())){
        				formAuthViewBean.setName("分送");
        			}else{
        				formAuthViewBean.setName(permission.getLabel());
        			}
        		}
        		List<FormAuthViewFieldBean> authViewFieldBeans = formAuthViewBean
        				.getFormAuthorizationFieldList();
        		for (FormAuthViewFieldBean formAuthViewFieldBean : authViewFieldBeans) {
        			if (list != null && list.size() > 0 && formAuthViewFieldBean.getFormFieldBean().getInputType().equals(FormFieldComEnum.EDOCFLOWDEALOPITION.getKey())) {
        				for (EdocFormFlowPermBound bound : list) {
        					if (bound.getProcessName().equals(formAuthViewFieldBean.getFormFieldBean().getMappingField())) {
        						formAuthViewFieldBean.setAccess(FieldAccessType.edit.getKey());
        					}
        				}
        			}else if (acl_list != null && acl_list.size() > 0) {
        				for (EdocElementFlowPermAcl acl : acl_list) {
        					if (acl.getEdocElement().getFieldName()
        							.equals(formAuthViewFieldBean.getFormFieldBean().getMappingField())) {
        						formAuthViewFieldBean.setAccess(acl.getAccess() == 1
        								? FieldAccessType.edit.getKey() : FieldAccessType.browse.getKey());
        						List<EdocFormElement> edocFormElements = edocFormElementDao.getEdocFormElementByElementIdAndFormId(Long.valueOf(acl.getEdocElement().getElementId()), edocForm.getId());
        						for (EdocFormElement edocFormElement : edocFormElements) {
        							if (edocFormElement.getRequired()) {
        								formAuthViewFieldBean.setIsNotNull(1);
        							}
        						}
        						break;
        					} else {
        						formAuthViewFieldBean.setAccess(FieldAccessType.browse.getKey());
        					}
        				}
        			}
        		}
        		for (PermissionVO newPermission : newPermissionList) {
        			if ("niwen".equals(newPermission.getName()) || "dengji".equals(newPermission.getName())) {
        				for (FormAuthViewBean viewBean : operationList) {
        					if ("add".equals(viewBean.getType())) {
        						config.put(newPermission.getFlowPermId().toString(), view.getId() + "." + viewBean.getId() + ".isDefault");
        						break;
        					}
        				}
        				continue;
        			}
        			if (permission.getName().equals(newPermission.getName())) {
        				if (config.get(newPermission.getFlowPermId()) != null) {
							if (permission.getCategory().equals(EnumNameEnum.edoc_qianbao_permission_policy.name())) {
								break;
							}
						}
        				config.put(newPermission.getFlowPermId().toString(), view.getId() + "." + formAuthViewBean.getId() + ".isDefault");
        				break;
        			}
        		}
        		if (!"niwen".equals(permission.getName())) {
        			operationList.add(formAuthViewBean);
        		}
        	}
        	view.setOperations(operationList);
		}
		formPermissionConfig.setConfig(JSONUtil.toJSONString(config));
		return formPermissionConfig;
	}
	
	/**
	 * 检查infopath文件的控件是否有超过950个
	 * 
	 * @param xsf
	 * @return
	 */
	private String checkFieldCount(InfoPathObject xsf) {
		int count = xsf.getIntoxsd().getMasternamelst().size();
		for (int i = 0; i < xsf.getIntoxsd().getTablst().size(); i++) {
			Map ht = (HashMap) xsf.getIntoxsd().getSlavelst().get(i);
			count += ht.size();
		}
		return count <= 950 ? "" : ResourceUtil.getString("form.fielddesign.infopathfiledlengthbigger");
	}
	
	/**
	 * 对上传的infopath中的控件进行查核
	 * 
	 * @param xsf
	 * @param formType
	 *            表单类型
	 * @param formId
	 *            表单id新建的时候为-1,导入infopath修改的时候为表单id
	 * @return
	 */
	private String validateInfopath(InfoPathObject xsf, int formType, long formId) {
		String checks = ResourceUtil.getString("form.fielddesign.fieldcheck");
		StringBuilder sb = new StringBuilder(",");
		String fieldName = "";
		// 校验控件个数
		String checkFileds = checkFieldCount(xsf);
		if (!"".equals(checkFileds)) {
			return checkFileds;
		}

		// 计划格式不支持多重复表
		// if(formType == FormType.planForm.getKey()){
		// if (xsf.getIntoxsd().getTablst().size() > 1){
		// return ResourceUtil.getString("form.fielddesign.planformonetable");
		// }
		// }
		// 有高级表单插件的情况下,信息管理以及流程表单允许多视图
		if (xsf.getViewList().size() > 1) {
			if (AppContext.hasPlugin("formAdvanced")) {
				if (formType == FormType.baseInfo.getKey()) {
					return ResourceUtil.getString("form.fielddesign.baseolnyallowsigleview");
				} else if (formType == FormType.planForm.getKey()) {
					return ResourceUtil.getString("form.formcreate.basedata.plan.nomultiview.label");
				}
			} else {
				return ResourceUtil.getString("form.formcreate.isnotadvancedform.multiform.abel");
			}
		}
		// 必须有主表字段
		if (xsf.getIntoxsd().getMasternamelst().size() == 0) {
			return ResourceUtil.getString("form.fielddesign.infopathnomaintable");
		}
		// 校验视图名称是否修改
		if (formId != -1L) {
			if (checkDelFormView(xsf, formId)) {
				// 您导入的infopath文件中删除了视图，此操作不允许
				return ResourceUtil.getString("form.fielddesign.viewname.delete.label");
			}
		}
		// 主表字段
		for (int i = 0; i < xsf.getIntoxsd().getMasternamelst().size(); i++) {
			fieldName = ((String) xsf.getIntoxsd().getMasternamelst().get(i)).toLowerCase();
			if (checks.contains("," + fieldName + ",")) {
				return ResourceUtil.getString("form.system.check");
			}
			if (sb.toString().contains("," + fieldName + ",")) {
				return ResourceUtil.getString("form.infopath.sameFiledName");
			}
			sb.append(fieldName).append(",");
		}
		// 从表
		for (int i = 0; i < xsf.getIntoxsd().getTablst().size(); i++) {
			Map ht = (HashMap) xsf.getIntoxsd().getSlavelst().get(i);
			Object[] ob = ht.values().toArray();
			// 从表字段
			for (int j = 0; j < ob.length; j++) {
				fieldName = ((String) ob[j]).toLowerCase();
				if (checks.contains("," + fieldName + ",")) {
					return ResourceUtil.getString("form.system.check");
				}
				if (sb.toString().contains("," + fieldName + ",")) {
					return ResourceUtil.getString("form.infopath.sameFiledName");
				}
				sb.append(fieldName).append(",");
			}
		}
		return "";
	}
	
	/**
	 * 校验表单infopath文件是否删除了视图
	 * 
	 * @param xsf
	 * @param formId
	 * @return
	 */
	private boolean checkDelFormView(InfoPathObject xsf, long formId) {
		boolean isDelete = false;
		FormBean fb = formApi4Cap3.getForm(formId);
		if (fb != null) {
			List<FormViewBean> fvbList = fb.getFormViewList();
			if (fvbList != null) {
				for (FormViewBean formViewBean : fvbList) {
					String name = xsf.getViewFileCaption(formViewBean.getFormViewFileName());
					if (Strings.isBlank(name)) {
						isDelete = true;
						break;
					}
				}
			}
		}
		return isDelete;
	}
	
	public ConfigManager getConfigManager() {
		return configManager;
	}
	public void setConfigManager(ConfigManager configManager) {
		this.configManager = configManager;
	}
	@Override
	public int getUpgradState() {
		//JDBCAgent agent = new JDBCAgent(true);
		int v = UPGRADE_STATE_NEEDTODO_NO;
		//if(NO_NEEDTO_CHECKED){
		if(false){
			return UPGRADE_STATE_NEEDTODO_NO;
		}
        try {
        	ConfigItem configItem = configManager.getConfigItem(9999999888L);
            //if (agent.execute("select config_value from ctp_config where id=?", 9999999888L) == -1) {
            	//Object value = agent.resultSetToMap().get("config_value");
        	if(configItem != null &&  Strings.isNotBlank(configItem.getConfigValue())){
        		v = Integer.parseInt(configItem.getConfigValue());
        	}
            //}
        } catch (Exception e) {
        	LOG.error("获取升级状态出错", e);
        } 
//        finally {
//            //agent.close();
//        }
        if(v == UPGRADE_STATE_DONE || v == UPGRADE_STATE_NEEDTODO_NO){
        	NO_NEEDTO_CHECKED = true;
        }
		return v;
	}

	@Override
	public void setUpgradeState(int state) {
		try {
			ConfigItem c = configManager.getConfigItem(9999999888L);
			c.setConfigValue(String.valueOf(state));
			configManager.updateConfigItem(c);
		} catch (Exception e) {
			//e.printStackTrace();
			LOG.error(" 修改升级状态出错  "+e.getMessage());
		} 
	}

	@Override
	public String getTemplateStr() throws BusinessException {
		StringBuilder sBuilder = new StringBuilder("模板:");
		List<String> tempList = edocUpgradeDao.getTempList();
		for (String cur:tempList) {
			sBuilder.append(cur).append(",");
		}
		sBuilder.append(",需要手动完成自动分支条件设置！");
		LOG.info(sBuilder.toString());
		return sBuilder.toString();
	}

	public void setEdocUpgradeDao(UpgradeDao edocUpgradeDao) {
		this.edocUpgradeDao = edocUpgradeDao;
	}

	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	public void setV3xFileDAO(V3XFileDAO v3xFileDAO) {
		this.v3xFileDAO = v3xFileDAO;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
		this.formApi4Cap3 = formApi4Cap3;
	}

	public void setGovdocFormExtendManager(GovdocFormExtendManager govdocFormExtendManager) {
		this.govdocFormExtendManager = govdocFormExtendManager;
	}

	public void setGovdocFormOpinionSortManager(GovdocFormOpinionSortManager govdocFormOpinionSortManager) {
		this.govdocFormOpinionSortManager = govdocFormOpinionSortManager;
	}

	public void setDeeDesignManager(DeeDesignManager deeDesignManager) {
		this.deeDesignManager = deeDesignManager;
	}

	public void setEdocFormElementDao(EdocFormElementDao edocFormElementDao) {
		this.edocFormElementDao = edocFormElementDao;
	}

	public void setEdocFormManager(EdocFormManager edocFormManager) {
		this.edocFormManager = edocFormManager;
	}

	public void setEdocElementFlowPermAclManager(EdocElementFlowPermAclManager edocElementFlowPermAclManager) {
		this.edocElementFlowPermAclManager = edocElementFlowPermAclManager;
	}

	public void setOrgManagerDirect(OrgManagerDirect orgManagerDirect) {
		this.orgManagerDirect = orgManagerDirect;
	}

	public void setPrivilegeMenuManager(PrivilegeMenuManager privilegeMenuManager) {
		this.privilegeMenuManager = privilegeMenuManager;
	}

	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

	public void setRoleMenuDao(RoleMenuDao roleMenuDao) {
		this.roleMenuDao = roleMenuDao;
	}

	public void setMenuCacheManager(MenuCacheManager menuCacheManager) {
		this.menuCacheManager = menuCacheManager;
	}

	public void setOrgDao(OrgDao orgDao) {
		this.orgDao = orgDao;
	}
	
	@Override
	@AjaxAccess
	public void activeSessionTime(Map<String, String> params) throws BusinessException {
		
	}

}
