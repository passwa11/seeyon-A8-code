package com.seeyon.v3x.edoc.manager;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.collaboration.vo.SeeyonPolicy;
import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.apps.govdoc.constant.GovdocEnum.GovdocWorkflowTypeEnum;
import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.config.IConfigPublicKey;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.excel.DataRecord;
import com.seeyon.ctp.common.excel.DataRow;
import com.seeyon.ctp.common.excel.FileToExcelManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.LocaleContext;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.processlog.manager.ProcessLogManager;
import com.seeyon.ctp.common.processlog.po.ProcessLog;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.organization.bo.MemberPost;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgPost;
import com.seeyon.ctp.organization.bo.V3xOrgTeam;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.portal.api.PortalApi;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.IdentifierUtil;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.common.dao.paginate.Pagination;
import com.seeyon.v3x.common.metadata.MetadataNameEnum;
import com.seeyon.v3x.common.taglibs.functions.Functions;
import com.seeyon.v3x.edoc.constants.EdocCategoryStoreTypeEnum;
import com.seeyon.v3x.edoc.constants.EdocElementConstants;
import com.seeyon.v3x.edoc.constants.EdocQueryColConstants;
import com.seeyon.v3x.edoc.consts.EdocElementEnum;
import com.seeyon.v3x.edoc.domain.EdocCategory;
import com.seeyon.v3x.edoc.domain.EdocDocTemplate;
import com.seeyon.v3x.edoc.domain.EdocElement;
import com.seeyon.v3x.edoc.domain.EdocForm;
import com.seeyon.v3x.edoc.domain.EdocFormAcl;
import com.seeyon.v3x.edoc.domain.EdocFormElement;
import com.seeyon.v3x.edoc.domain.EdocFormExtendInfo;
import com.seeyon.v3x.edoc.domain.EdocFormFlowPermBound;
import com.seeyon.v3x.edoc.domain.EdocInnerMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocKeyWord;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocStat;
import com.seeyon.v3x.edoc.domain.EdocStatCondObj;
import com.seeyon.v3x.edoc.domain.EdocStatDisObj;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.domain.WebEdocStat;
import com.seeyon.v3x.edoc.exception.EdocException;
import com.seeyon.v3x.edoc.util.Constants;
import com.seeyon.v3x.edoc.util.EdocUtil;
import com.seeyon.v3x.edoc.webmodel.EdocMarkModel;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryModel;
import com.seeyon.v3x.edoc.webmodel.FormBoundPerm;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;
import com.seeyon.v3x.edoc.webmodel.SummaryModel;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;

import net.joinwork.bpm.definition.BPMAbstractNode;
import net.joinwork.bpm.definition.BPMActivity;
import net.joinwork.bpm.definition.BPMActor;
import net.joinwork.bpm.definition.BPMAndRouter;
import net.joinwork.bpm.definition.BPMConRouter;
import net.joinwork.bpm.definition.BPMEnd;
import net.joinwork.bpm.definition.BPMHumenActivity;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.definition.BPMSeeyonPolicy;
import net.joinwork.bpm.definition.BPMStatus;
import net.joinwork.bpm.definition.BPMTimeActivity;
import net.joinwork.bpm.definition.BPMTransition;
import net.joinwork.bpm.engine.wapi.WorkItem;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;


public class EdocHelper{
	/**
	 * 常量定义
	 */
	public static final String ORGENT_META_KEY_SEDNER = "Sender";
	public static final String ORGENT_TYPE_ACCOUNT = "Account";
	public static final String ORGENT_TYPE_DEPARTMENT = "Department";
	public static final String ORGENT_TYPE_LEVEL = "Level";
	public static final String ORGREL_TYPE_CONCURRENT_ACCOUNT= "Concurrent_Acunt"; // 兼职单位
    public static final String ORGREL_TYPE_CONCURRENT_LEVEL= "Concurrent_Levl"; // 兼职职务级别
    public static final String ORGENT_TYPE_POST = "Post";
    public static final String ORGREL_TYPE_CONCURRENT_DEPARTMENT= "Concurrent_Dept"; // 兼职部门
    public static final String ORGREL_TYPE_SECOND_POST_DEPARTMENT= "SecondPst_Dept"; // 副岗部门
	public static final String ORGREL_TYPE_MEMBER_POST = "Member_Post"; // 副岗
	public static final String ORGREL_TYPE_DEPARTMENT= "belongDept";
	public static final String ORGREL_TYPE_CONCURRENT_POST = "Concurrent_Post"; // 兼职
	
/*	产品版本区隔
 *  a6(0),              // A6版
    enterprise(1),      // 企业版
    entgroup(2),        // 企业集团版
    government(3),      // 政务版
    governmentgroup(4), // 政务多组织版
    ufidanc(5);         // UFIDA-NC协同套件
	ufidau8(6);         // UFIDA-U8协同套件
	a6-s(7);         // A6-S版
*/	
	public static final int SYSTEM_PRODUCTID_A6 =0 ;
	public static final int SYSTEM_PRODUCTID_enterprise =1 ;
	public static final int SYSTEM_PRODUCTID_entgroup =2 ;
	public static final int SYSTEM_PRODUCTID_government =3 ;
	public static final int SYSTEM_PRODUCTID_governmentgroup =4 ;
	public static final int SYSTEM_PRODUCTID_ufidanc =5 ;
	public static final int SYSTEM_PRODUCTID_ufidau8 =6 ;
	public static final int SYSTEM_PRODUCTID_A6S =7 ;
	
	
	private static final Log LOGGER = LogFactory.getLog(EdocHelper.class);
	private static String baseFileFolder = SystemProperties.getInstance().getProperty("edoc.folder");
	private static String formFolder = "/form";
	private static String templateFolder = "/template";
	private static OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
	private static TemplateManager templateManager = (TemplateManager) AppContext.getBean("templateManager");
	private static EdocDocTemplateManager edocDocTemplateManager = (EdocDocTemplateManager) AppContext.getBean("edocDocTemplateManager");
	private static final WorkflowApiManager wapi           = (WorkflowApiManager) AppContext.getBean("wapi");
	private static EdocManager edocManager = (EdocManager) AppContext.getBean("edocManager");
	private static AffairManager affairManager = (AffairManager) AppContext.getBean("affairManager");
	private static PortalApi portalApi = (PortalApi) AppContext.getBean("portalApi");
	private static EdocFormManager  edocFormManager = (EdocFormManager)AppContext.getBean("edocFormManager");
	private static EdocCategoryManager edocCategoryManager = (EdocCategoryManager)AppContext.getBean("edocCategoryManager");
	private static EdocElementManager edocElementManager = (EdocElementManager)AppContext.getBean("edocElementManager");
	
	public static String getWorkFlowInfoScript(Long summaryId,EdocManager edocManager) throws Exception
	{
		StringBuilder sb=new StringBuilder();
        EdocSummary summary = edocManager.getEdocSummaryById(summaryId, false);
        
        String caseLogXML = null;
        String caseProcessXML = null;
        String caseWorkItemLogXML = null;
        
        if (summary != null){
            if (summary.getCaseId() != null) {
                long caseId = summary.getCaseId();
                caseLogXML = edocManager.getCaseLogXML(caseId);
                caseProcessXML = edocManager.getCaseProcessXML(caseId);
                caseWorkItemLogXML = edocManager.getCaseWorkItemLogXML(caseId);
            }
            else if (summary.getProcessId() != null && !"".equals(summary.getProcessId())) {
                String processId = summary.getProcessId();
                caseProcessXML = edocManager.getProcessXML(processId);
            }
        }
        
        caseProcessXML = StringEscapeUtils.escapeJavaScript(caseProcessXML);
        caseLogXML = StringEscapeUtils.escapeJavaScript(caseLogXML);
        caseWorkItemLogXML = StringEscapeUtils.escapeJavaScript(caseWorkItemLogXML);

        sb.append("<script>");
        sb.append("parent.caseProcessXML = \"" + caseProcessXML + "\";");
        sb.append("parent.caseLogXML = \"" + caseLogXML + "\";");
        sb.append("parent.caseWorkItemLogXML = \"" + caseWorkItemLogXML + "\";");
        sb.append("parent.selectInsertPeopleOK();");
        sb.append("</script>");     
        
		return sb.toString();
	}
	/**
	 * 检查策略中是否包含“归档”，如果不是封发，去掉归档
	 * @param list
	 * @param permKey
	 */
    public static List<String> checkPerm(List<String> list, String permKey) {
        if (list == null) {
            return list;
        }
        List<String> tempList = new ArrayList<String>();
        if (!"fengfa".equals(permKey)) {
            if (list.contains("Archive") == false) {
                return list;
            }
            for (String item : list) {
                if (!"Archive".equals(item)) {
                    tempList.add(item);
                }
            }
            return tempList;
        } else {
            return list;
        }
    }
	
    public static List<FormBoundPerm> getProcessOpinionByEdocFormId4Upgrade(List<String> elementList, long edocFormId, int edocType,long accountId, boolean isUpload) throws Exception{
		String category = "";
		if(edocType == EdocEnum.edocType.sendEdoc.ordinal()){
			category = MetadataNameEnum.edoc_send_permission_policy.name();
		}else if(edocType == EdocEnum.edocType.recEdoc.ordinal()){
			category = MetadataNameEnum.edoc_rec_permission_policy.name();
		}else if(edocType == EdocEnum.edocType.signReport.ordinal()){
			category =  MetadataNameEnum.edoc_qianbao_permission_policy.name();
		}
		PermissionManager permissionManager= (PermissionManager)AppContext.getBean("permissionManager");
		EdocFormManager edocFormManager= (EdocFormManager)AppContext.getBean("edocFormManager");
		EdocElementManager edocElementManager = (EdocElementManager)AppContext.getBean("edocElementManager");		
		
		List<FormBoundPerm> boundPermList = new ArrayList<FormBoundPerm>();

		String value = "";
		String processName = "";
		String processItemName = "";
		FormBoundPerm otherOpinionPerm = null;
		
		for(String fieldName : elementList){
			
			EdocElement ele = edocElementManager.getByFieldName4upgrade(fieldName,accountId);
			
			if(null!=ele && ele.getType() != EdocElement.C_iElementType_Comment)continue;
			
			if(null!=ele && null!=ele.getName()) {
				if(ele.getIsSystem() == true){
					value = ResourceUtil.getString(ele.getName());
					processName = ele.getFieldName();
					
					if(processName.equals(EdocOpinion.REPORT)){
						processItemName = ResourceUtil.getString("node.policy.report");
					}else{
						processItemName = value;
					}
					
					if(isUpload) {//编辑时重新上传
						Permission f = permissionManager.getPermission(category,ele.getFieldName(),accountId);
						if(f==null || !ele.getFieldName().equals(f.getName())){//通过getPermission获取到的节点可能是只会节点
							processName = "";
							processItemName = "";
						}
					}
				}else{
					value = ele.getName();
					processName = "";
					processItemName = "";
					if(isUpload) {//编辑时重新上传
						Permission f = permissionManager.getPermission(category,ele.getFieldName(),accountId);
						if(f!=null && ele.getFieldName().equals(f.getName())){//f 目前为止不可能为null
							processName = ele.getFieldName();
							processItemName = value;
						}
					}
				}
				FormBoundPerm formBoundPerm = new FormBoundPerm();
				formBoundPerm.setPermItem(ele.getFieldName());
				formBoundPerm.setPermName(value);
				if("otherOpinion".equalsIgnoreCase(fieldName)){
					otherOpinionPerm = formBoundPerm;
					continue;
				}
				formBoundPerm.setPermItemName(processItemName);
				formBoundPerm.setProcessName(processName);
				formBoundPerm.setProcessItemName(processItemName);
				if(isUpload) {//编辑时重新上传，默认为同名权限
					formBoundPerm.setPermItemList(processName);
				}
				boundPermList.add(formBoundPerm);	
			}
		}
		if(null!=otherOpinionPerm){
			List<EdocFormFlowPermBound> list = edocFormManager.findBoundByFormId(edocFormId, otherOpinionPerm.getPermItem());
			for (EdocFormFlowPermBound bound : list) {
				otherOpinionPerm.setSortType(bound.getSortType());
			}
			boundPermList.add(otherOpinionPerm);// 把处理意见加到最后
		}
		
		//if(!isUpload) {
		if(Strings.isNotEmpty(boundPermList)){
			for(FormBoundPerm perm : boundPermList){
				if (null != perm.getPermItem() && "otherOpinion".equalsIgnoreCase(perm.getPermItem()))
					continue;
				//此处判断当前文单是否属于本单位的文单
				//boolean flag=edocFormManager.getFormAccountEdoc(accountId, edocFormId);
				//if(flag){
				List<EdocFormFlowPermBound> list = edocFormManager.findBoundByFormId(edocFormId, perm.getPermItem(),accountId);
				String str_temp = "";
				String str_temp_b = "";
				for(EdocFormFlowPermBound bound : list){
					str_temp += bound.getFlowPermNameLabel();
					str_temp += ",";
					EdocElement element = null;
					element = edocElementManager.getByFieldName4upgrade(bound.getFlowPermName(),accountId);
					if(null!=element && element.getIsSystem()){//查找元素，如果非空证明是系统预置的fuhe,shenpi....节点权限和系统预置的处理意见
						str_temp_b += bound.getFlowPermName();
					}else if(element == null){//如果element为空，那么
						element = edocElementManager.getByFieldName4upgrade(bound.getProcessName(),accountId);//在用processName查
						if(null!=element && element.getIsSystem()){//节点权限为自定义但处理意见元素为系统的
							str_temp_b += bound.getFlowPermName();
						}else if(null!=element && (element.getIsSystem()==false)){//节点权限为自定义而且处理意见元素也是扩展的
							str_temp_b += bound.getFlowPermName();
						}
					}
					str_temp_b += ",";
					perm.setSortType(bound.getSortType());
				}
				if(str_temp.endsWith(",") && str_temp_b.endsWith(",")){
					str_temp = str_temp.substring(0, str_temp.length()-1);
					str_temp_b = str_temp_b.substring(0, str_temp_b.length()-1);
				}
				
				if(Strings.isNotEmpty(list)){
				    perm.setPermItemName(str_temp);
	                perm.setPermItemList(str_temp_b);
				}
				
				//}
			}	
		}
		
					
		//}
		return boundPermList;

	}
    
	public static DataRecord exportStat(HttpServletRequest request,List<EdocStatDisObj> results, EdocStatCondObj esco,String stat_title){

		DataRecord dataRecord = new DataRecord();

		Locale local = LocaleContext.getLocale(request);
		String resource = "com.seeyon.v3x.edoc.resources.i18n.EdocResource";
//		导出excel文件的国际化

		String level_receive = ResourceBundleUtil.getString(resource, local, "edoc.docmark.inner.receive");//收文
		String level_send = ResourceBundleUtil.getString(resource, local, "edoc.docmark.inner.send");//发文
		String stat_signandreport = ResourceBundleUtil.getString(resource, local, "edoc.docmark.inner.signandreport");//签报
		String stat_sum = ResourceBundleUtil.getString(resource, local, "edoc.stat.sum.label");//合计
		String stat_dep = ResourceBundleUtil.getString(resource, local, "edoc.stat.group.dept.label");//发起部门
		String stat_type = ResourceBundleUtil.getString(resource, local, "edoc.stat.group.doctype.label");//公文种类
		
		String stat_groupType = "";
		if(esco.getGroupType() == Constants.EDOC_STAT_GROUPBY_DOCTYPE){
			stat_groupType = stat_type;
		}else if(esco.getGroupType() == Constants.EDOC_STAT_GROUPBY_DEPT){
			stat_groupType = stat_dep;
		}
			
		Pagination.setNeedCount(false);

		if (null != results && results.size() > 0) {
			DataRow[] datarow = new DataRow[results.size()];
			for (int i = 0; i < results.size(); i++) {
				EdocStatDisObj stat = results.get(i);
				DataRow row = new DataRow();
				
				//--start-- 如果行为最后一行或使用公文类型来进行查询,显示的行名国际化
				if(i==results.size()-1 || esco.getGroupType() == Constants.EDOC_STAT_GROUPBY_DOCTYPE){
					String columnName = ResourceBundleUtil.getString(resource, stat.getColumnName());
					row.addDataCell(columnName, 1);
				}else{
					row.addDataCell(stat.getColumnName(), 1);
				}
				//--end--
				
				row.addDataCell(String.valueOf(stat.getRecieveNum()), 1);
				row.addDataCell(String.valueOf(stat.getSendNum()), 1);
				row.addDataCell(String.valueOf(stat.getSignNum()), 1);
				row.addDataCell(String.valueOf(stat.getTotalNum()), 1);

				datarow[i] = row;
			}
			try {
				dataRecord.addDataRow(datarow);
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		String[] columnName = { stat_groupType,level_receive,level_send, stat_signandreport,stat_sum};
		dataRecord.setColumnName(columnName);
		dataRecord.setTitle(stat_title);
		dataRecord.setSheetName(stat_title);
	
		return dataRecord;
	}
	
	
	//公文查询组装成页面要显示的excel数据
	public static DataRecord exportQueryToWebModel2(HttpServletRequest request,List<EdocSummaryModel> results,String excel_title){
	    EnumManager enumManager = (EnumManager)AppContext.getBean("enumManagerNew");
	    CtpEnumBean edocKeepPeriodData = enumManager.getEnum(EnumNameEnum.edoc_keep_period.name());
	    CtpEnumBean sendUnitTypeData = enumManager.getEnum(EnumNameEnum.send_unit_type.name());
		List<String> labelList = null; 
		String colId = request.getParameter("colId");  
		int edocType = Integer.parseInt(request.getParameter("edocType"));  
		if(colId != null && !"".equals(colId)){
			
			Map<Integer,String> map = null;
			if(edocType == 0){
				map = EdocQueryColConstants.sendEdocColMap;
			}else if(edocType == 1){
				map = EdocQueryColConstants.recEdocColMap;
			}
			labelList = new ArrayList<String>();
			String[] cids = colId.split(",");
			for(int i=0;i<cids.length;i++){
				labelList.add(map.get(Integer.parseInt(cids[i])));
			}
		}
		
		DataRecord dataRecord = new DataRecord();
		
		Locale local = LocaleContext.getLocale(request);
		String resource = "com.seeyon.v3x.edoc.resources.i18n.EdocResource";
		
		//列名
		String[] columnName = null;
		
		EnumManager metadataManager = (EnumManager)AppContext.getBean("enumManagerNew");
		CtpEnumBean urgentMeta = metadataManager.getEnum(MetadataNameEnum.edoc_urgent_level.name());
		
		if(edocType == EdocEnum.edocType.sendEdoc.ordinal()){
			if(labelList == null){
				columnName=new String[10];
				columnName[0] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.SEND_SUBJECT);
				columnName[1] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.SEND_DEPARTMENT);
				columnName[2] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.CREATE_DATE);
				columnName[3] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.SEND_WORD);
				columnName[4] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.SEND_SERIALNO);
				columnName[5] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.ISSUER);
				columnName[6] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.SEND_SENDINGDATE);
				columnName[7] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.SEND_CREATEPERSON);
				columnName[8] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.SEND_REVIEW);
				columnName[9] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.SEND_DISTRIBUTER);

			}else{
				columnName=new String[labelList.size()];
				for(int j=0;j<labelList.size();j++){
					String label = labelList.get(j);
					columnName[j] = ResourceBundleUtil.getString(resource, local, label);
				}
			}
			if (null != results && results.size() > 0) {
				DataRow[] datarow = new DataRow[results.size()];
				for (int i = 0; i < results.size(); i++) {
					EdocSummaryModel summaryModel = (EdocSummaryModel)results.get(i);
					EdocSummary summary=summaryModel.getSummary();
					DataRow row = new DataRow();
					
					if(labelList == null){
						row.addDataCell(null!=summary.getSubject() ? String.valueOf(summary.getSubject()) : "", 1);
						row.addDataCell(null != summaryModel.getDepartmentName() ? summaryModel.getDepartmentName() : "",1);
						//row.addDataCell(null!=summary.getSendUnit() ? String.valueOf(summary.getSendUnit()) : "", 1);
						row.addDataCell(null != summary.getCreateTime() ? String.valueOf(summary.getCreateTime()).substring(0,10) : "",1);
						row.addDataCell(null!=summary.getDocMark() ? String.valueOf(summary.getDocMark()) : "", 1);
						row.addDataCell(null!=summary.getSerialNo() ? String.valueOf(summary.getSerialNo()) : "", 1);
						row.addDataCell(null != summary.getIssuer() ? summary.getIssuer() : "",1);
						row.addDataCell(null!=summary.getSigningDate() ? String.valueOf(summary.getSigningDate()) : "", 1);
						row.addDataCell(null!=summary.getCreatePerson()? String.valueOf(summary.getCreatePerson()) : "", 1);		
						row.addDataCell(null!=summary.getReview()? summary.getReview() : "", 1);
						//row.addDataCell(null!=summary.getSecretLevel()? String.valueOf(summary.getSecretLevel()) : "", 1);
						//String keepLabel = ResourceBundleUtil.getString(resource, local,edocKeepPeriodData.getItemLabel(String.valueOf(summary.getKeepPeriod()))); 
						//row.addDataCell(null!=summary.getKeepPeriod()? keepLabel : "", 1);
						//String urgentLabel = urgentMeta.getItemLabel(String.valueOf(summary.getUrgentLevel())) ;
						//String urgentName = ResourceBundleUtil.getString(resource, local, urgentLabel);
						//row.addDataCell(null!=summary.getUrgentLevel()?  urgentName : "", 1);
						row.addDataCell(null!=summaryModel.getSender() ? summaryModel.getSender() : "", 1);
                        //row.addDataCell(null != summary.getSendUnit() ? summary.getSendUnit() : "",1);
                        //row.addDataCell(null != summary.getCopyTo() ? summary.getCopyTo() : "",1);
                        //row.addDataCell(null!=summary.getCopies()? String.valueOf(summary.getCopies()) : "", 1);

					}else{
						for(int j=0;j<labelList.size();j++){ 
							String label = labelList.get(j);
							if(label.equals(EdocQueryColConstants.SEND_WORD)){
								row.addDataCell(null!=summary.getDocMark() ? String.valueOf(summary.getDocMark()) : "", 1);
							}
							else if(label.equals(EdocQueryColConstants.SEND_SUBJECT)){
								row.addDataCell(null!=summary.getSubject() ? String.valueOf(summary.getSubject()) : "", 1);
							}
							else if(label.equals(EdocQueryColConstants.SEND_SENDINGDATE)){
								row.addDataCell(null!=summary.getSigningDate() ? String.valueOf(summary.getSigningDate()) : "", 1);
							}
							else if(label.equals(EdocQueryColConstants.SEND_CREATEPERSON)){
								row.addDataCell(null!=summary.getCreatePerson()? String.valueOf(summary.getCreatePerson()) : "", 1);
							}
							else if(label.equals(EdocQueryColConstants.SEND_SECRETLEVEL)){
								row.addDataCell(null!=summary.getSecretLevel()? String.valueOf(summary.getSecretLevel()) : "", 1);
							}
							else if(label.equals(EdocQueryColConstants.SEND_KEEPPERIOD)){
							    String keepLabel = ResourceBundleUtil.getString(resource, local,edocKeepPeriodData.getItemLabel(String.valueOf(summary.getKeepPeriod()))); 
		                        row.addDataCell(null!=summary.getKeepPeriod()? keepLabel : "", 1);
							}
							else if(label.equals(EdocQueryColConstants.SEND_URGENTLEVEL)){
								String urgentLabel = urgentMeta.getItemLabel(String.valueOf(summary.getUrgentLevel())) ;
								String urgentName = ResourceBundleUtil.getString(resource, local, urgentLabel);
								row.addDataCell(null!=summary.getUrgentLevel()?  urgentName : "", 1);
							}
							else if(label.equals(EdocQueryColConstants.SEND_COPIES)){
								row.addDataCell(null!=summary.getCopies()? String.valueOf(summary.getCopies()) : "", 1);
							}
							else if(label.equals(EdocQueryColConstants.SEND_SENDUNIT)){
								row.addDataCell(null!=summary.getSendUnit() ? String.valueOf(summary.getSendUnit()) : "", 1);
							}
							else if(label.equals(EdocQueryColConstants.SEND_DISTRIBUTER)){
								row.addDataCell(null!=summaryModel.getSender() ? summaryModel.getSender() : "", 1);
							}
							
							else if(label.equals(EdocQueryColConstants.SEND_TO_UNIT)){
							    row.addDataCell(null != summary.getSendTo() ? summary.getSendTo() : "",1);
	                        }
	                        else if(label.equals(EdocQueryColConstants.COPY_TO_UNIT)){
	                            row.addDataCell(null != summary.getCopyTo() ? summary.getCopyTo() : "",1);
	                        }
	                        else if(label.equals(EdocQueryColConstants.ISSUER)){
	                            row.addDataCell(null != summary.getIssuer() ? summary.getIssuer() : "",1);
	                        }
	                        else if(label.equals(EdocQueryColConstants.CREATE_DATE)){
	                            row.addDataCell(null != summary.getCreateTime() ? String.valueOf(summary.getCreateTime()).substring(0,10) : "",1);
	                        }
	                        else if(label.equals(EdocQueryColConstants.SEND_DEPARTMENT)){
	                            row.addDataCell(null != summaryModel.getDepartmentName() ? summaryModel.getDepartmentName() : "",1);
	                        }else if(label.equals(EdocQueryColConstants.SEND_SERIALNO)){
	                            row.addDataCell(null != summary.getSerialNo() ? summary.getSerialNo() : "",1);
	                        }else if(label.equals(EdocQueryColConstants.SEND_REVIEW)){
	                            row.addDataCell(null != summary.getReview() ? summary.getReview() : "",1);
	                        }else {
	                        	row.addDataCell("",1);
	                        }
							
						}
					}
					
					datarow[i] = row;
				}
				try {
					dataRecord.addDataRow(datarow);
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);;
				}
			}
			
		}else if(edocType == EdocEnum.edocType.recEdoc.ordinal()){
			if(labelList == null){
				columnName=new String[7];
				int idx = 0;
				columnName[idx++] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.REC_SUBJECT);
				columnName[idx++] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.REC_COMMUNICATION_DATE);
				columnName[idx++] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.REC_FROMUNIT);
				columnName[idx++] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.REC_DOCMARK);
				columnName[idx++] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.REC_SERIALNO);
				//columnName[idx++] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.REC_UNDERTAKER_ACCOUNT);
				//columnName[idx++] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.REC_UNDERTAKER_DEP);
				columnName[idx++] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.REC_UNDERTAKER);
				//columnName[idx++] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.REC_URGENTLEVEL);
				//columnName[idx++] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.SEND_SENDUNIT);
				//columnName[idx++] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.COPY_TO_UNIT);
				//columnName[idx++] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.ISSUER);
				//columnName[idx++] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.C_PERSON);
				//columnName[idx++] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.REC_SECRETLEVEL);
				//columnName[idx++] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.REC_KEEPPERIOD);
				//columnName[idx++] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.KEYWORD);
				//columnName[idx++] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.REG_PERSON);
				columnName[idx++] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.REG_DATE);	
//				columnName[idx++] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.SEND_DISTRIBUTER);	
//				columnName[8] = ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.REC_COPIES);
				
				
				
			}else{
				columnName=new String[labelList.size()];
				for(int j=0;j<labelList.size();j++){
					String label = labelList.get(j);
					columnName[j] = ResourceBundleUtil.getString(resource, local, label);
				}
			}
			
			if (null != results && results.size() > 0) {
				DataRow[] datarow = new DataRow[results.size()];
				for (int i = 0; i < results.size(); i++) {
					EdocSummaryModel summaryModel = (EdocSummaryModel)results.get(i);
					EdocSummary summary=summaryModel.getSummary();
					DataRow row = new DataRow();
					if(labelList == null){
						row.addDataCell(null!=summary.getSubject() ? String.valueOf(summary.getSubject()) : "", 1);
						row.addDataCell(null!=summaryModel.getRecieveDate()? String.valueOf(summaryModel.getRecieveDate()) : "", 1);
						row.addDataCell(null!=summary.getSendUnit() ? String.valueOf(summary.getSendUnit()) : "", 1);
						row.addDataCell(null!=summary.getDocMark() ? String.valueOf(summary.getDocMark()) : "", 1);
						row.addDataCell(null!=summary.getSerialNo() ? String.valueOf(summary.getSerialNo()) : "", 1);
						//row.addDataCell(null!=summary.getUndertakerAccount()? String.valueOf(summary.getUndertakerAccount()) : "", 1);
						//row.addDataCell(null!=summary.getUndertakerDep()? String.valueOf(summary.getUndertakerDep()) : "", 1);
						row.addDataCell(null!=summary.getUndertaker()? String.valueOf(summary.getUndertaker()) : "", 1);
						
						//String urgentLabel = urgentMeta.getItemLabel(String.valueOf(summary.getUrgentLevel())) ;
						//String urgentName = ResourceBundleUtil.getString(resource, local, urgentLabel);
						//row.addDataCell(null!=summary.getUrgentLevel()?  urgentName : "", 1);
						
						//row.addDataCell(null!=summary.getSendTo()? String.valueOf(summary.getSendTo()) : "", 1);
						//row.addDataCell(null!=summary.getCopyTo()? String.valueOf(summary.getCopyTo()) : "", 1);
						//row.addDataCell(null!=summary.getIssuer()? String.valueOf(summary.getIssuer()) : "", 1);
						//row.addDataCell(null!=summaryModel.getSigner()? String.valueOf(summaryModel.getSigner()) : "", 1);
						//row.addDataCell(null!=summary.getSecretLevel()? String.valueOf(summary.getSecretLevel()) : "", 1);
						
						//String keepLabel = ResourceBundleUtil.getString(resource, local,edocKeepPeriodData.getItemLabel(String.valueOf(summary.getKeepPeriod()))); 
						//row.addDataCell(null!=summary.getKeepPeriod()? keepLabel : "", 1);
						//row.addDataCell(null!=summary.getKeywords()? String.valueOf(summary.getKeywords()) : "", 1);
						//row.addDataCell(null!=summaryModel.getRegisterUserName()? String.valueOf(summaryModel.getRegisterUserName()) : "", 1);
						row.addDataCell(null!=summaryModel.getRegisterDate()? String.valueOf(summaryModel.getRegisterDate()) : "", 1);
//						row.addDataCell(null!=summaryModel.getDistributer()? String.valueOf(summaryModel.getDistributer()) : "", 1);
						
//						row.addDataCell(null!=summary.getCopies() ? String.valueOf(summary.getCopies()) : "", 1);
						
					}else{
						for(int j=0;j<labelList.size();j++){
							String label = labelList.get(j);
							if(label.equals(EdocQueryColConstants.REC_SERIALNO)){
								row.addDataCell(null!=summary.getSerialNo()? String.valueOf(summary.getSerialNo()) : "", 1);
							}
							else if(label.equals(EdocQueryColConstants.REC_SUBJECT)){
								row.addDataCell(null!=summary.getSubject()? String.valueOf(summary.getSubject()) : "", 1);
							}
							else if(label.equals(EdocQueryColConstants.REC_SECRETLEVEL)){
								row.addDataCell(null!=summary.getSecretLevel()? String.valueOf(summary.getSecretLevel()) : "", 1);
							}
							else if(label.equals(EdocQueryColConstants.REC_KEEPPERIOD)){
							    String keepLabel = ResourceBundleUtil.getString(resource, local,edocKeepPeriodData.getItemLabel(String.valueOf(summary.getKeepPeriod()))); 
		                        row.addDataCell(null!=summary.getKeepPeriod()? keepLabel : "", 1);
							}
							else if(label.equals(EdocQueryColConstants.REC_URGENTLEVEL)){
								String urgentLabel = urgentMeta.getItemLabel(String.valueOf(summary.getUrgentLevel())) ;
								String urgentName = ResourceBundleUtil.getString(resource, local, urgentLabel);
								row.addDataCell(null!=summary.getUrgentLevel()?  urgentName : "", 1);
							}
							else if(label.equals(EdocQueryColConstants.REC_FROMUNIT)){
								row.addDataCell(null!=summary.getSendUnit()? String.valueOf(summary.getSendUnit()) : "", 1);
							}
							else if(label.equals(EdocQueryColConstants.REC_DOCMARK)){
								row.addDataCell(null!=summary.getDocMark()? String.valueOf(summary.getDocMark()) : "", 1);
							}
							else if(label.equals(EdocQueryColConstants.REC_COMMUNICATION_DATE)){
								row.addDataCell(null!=summaryModel.getRecieveDate()? String.valueOf(summaryModel.getRecieveDate()) : "", 1);
							}
							else if(label.equals(EdocQueryColConstants.REC_COPIES)){
								row.addDataCell(null!=summary.getCopies()? String.valueOf(summary.getCopies()) : "", 1);
							}
							else if(label.equals(EdocQueryColConstants.SEND_SENDUNIT)){
								row.addDataCell(null!=summary.getSendTo()? String.valueOf(summary.getSendTo()) : "", 1);
							}
							else if(label.equals(EdocQueryColConstants.COPY_TO_UNIT)){
								row.addDataCell(null!=summary.getCopyTo()? String.valueOf(summary.getCopyTo()) : "", 1);
							}
							else if(label.equals(EdocQueryColConstants.ISSUER)){
								row.addDataCell(null!=summary.getIssuer()? String.valueOf(summary.getIssuer()) : "", 1);
							}
							else if(label.equals(EdocQueryColConstants.C_PERSON)){
								row.addDataCell(null!=summaryModel.getSigner()? String.valueOf(summaryModel.getSigner()) : "", 1);
							}
							else if(label.equals(EdocQueryColConstants.KEYWORD)){
								row.addDataCell(null!=summary.getKeywords()? String.valueOf(summary.getKeywords()) : "", 1);
							}
							else if(label.equals(EdocQueryColConstants.REG_PERSON)){
								row.addDataCell(null!=summaryModel.getRegisterUserName()? String.valueOf(summaryModel.getRegisterUserName()) : "", 1);
							}
							else if(label.equals(EdocQueryColConstants.REG_DATE)){
								row.addDataCell(null!=summaryModel.getRegisterDate()? String.valueOf(summaryModel.getRegisterDate()) : "", 1);
							}
							else if(label.equals(EdocQueryColConstants.SEND_DISTRIBUTER)){
								row.addDataCell(null!=summaryModel.getDistributer()? String.valueOf(summaryModel.getDistributer()) : "", 1);
							}else if(label.equals(EdocQueryColConstants.REC_UNDERTAKER)){
								row.addDataCell(null!=summary.getUndertaker()? String.valueOf(summary.getUndertaker()) : "", 1);
							}else if(label.equals(EdocQueryColConstants.REC_UNDERTAKER_DEP)){
								row.addDataCell(null!=summary.getUndertakerDep()? String.valueOf(summary.getUndertakerDep()) : "", 1);
							}else if(label.equals(EdocQueryColConstants.REC_UNDERTAKER_ACCOUNT)){
								row.addDataCell(null!=summary.getUndertakerAccount()? String.valueOf(summary.getUndertakerAccount()) : "", 1);
							}else if(label.equals(EdocQueryColConstants.COME_EDOC_TYPE)){
							    String doctypeLabel = ResourceBundleUtil.getString(resource, local,sendUnitTypeData.getItemLabel(String.valueOf(summaryModel.getSendUnitType()))); 
		                        row.addDataCell(null!=summaryModel.getSendUnitType()? doctypeLabel : "", 1);
							}
							else {
								row.addDataCell("", 1);
							}
						}
					} 
					
					datarow[i] = row;
				}
				try {
					dataRecord.addDataRow(datarow);
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);;
				}
			}
		}
		//OA-21129  发文登记簿，自定义查询，只选择一个条件，查询，导出excel，excel中没有列头。两个以上的条件都是正常的。  
		if(columnName != null && columnName.length >= 1){
			dataRecord.setColumnName(columnName);
		}
		if(Strings.isNotBlank(excel_title)){
			dataRecord.setTitle(excel_title);
			dataRecord.setSheetName(excel_title);
		}
		return dataRecord;
	}
	
	/**
	 * 获取SummaryModel的属性值
	 * @Author      : xuqiangwei
	 * @Date        : 2014年12月17日下午4:11:54
	 * @param attr 属性
	 * @param codecfg 枚举code
	 * @param summaryModel 对象
	 * @return
	 */
	private static String getSummaryModelValue(String filedName, String codecfg, 
                            SummaryModel summaryModel) {
	    
	    String value = "";
        try {
            if (summaryModel != null && Strings.isNotBlank(filedName)) {

                Class<SummaryModel> c = SummaryModel.class;
                Field field = c.getDeclaredField(filedName);
                field.setAccessible(true);

                Object fieldValue = field.get(summaryModel);
                if (fieldValue != null) {

                    if (Strings.isNotBlank(codecfg)) {
                        EnumManager enumManager = (EnumManager) AppContext.getBean("enumManagerNew");
                        CtpEnumBean ctpEnum = enumManager.getEnum(codecfg);
                        CtpEnumItem item = ctpEnum.getItem(fieldValue.toString());
                        if (item != null) {
                            String label = item.getLabel();
                            value = ResourceUtil.getString(label);
                        }
                    } else {
                        //时间格式只保留日期
                        if(fieldValue instanceof java.util.Date){
                            java.util.Date date = (java.util.Date)fieldValue;
                            if("sendTime".equals(filedName)){
                            	value = DateUtil.format(date, "yyyy-MM-dd HH:mm");
                            }else{
                            	value = DateUtil.format(date);
                            }
                        }else{
                            value = fieldValue.toString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("收发文登记薄导出Excel解析错误", e);
        } 
        return value;
    }
	
	/**
	 * 导出指定列的数据
	 * @Author      : xuqiangwei
	 * @Date        : 2014年12月17日下午3:48:35
	 * @param columnDomainList 列数据{
     *                               "display" : "密级",//显示名称
     *                               "name" : "secretLevel",//SummaryModel.java 的属性
     *                               "codecfg" : "edoc_secret_level" //枚举配置Code
     *                           }
	 * @param summaryModel 数据
	 * @param excel_title Excel名称
	 * @return
	 */
	public static DataRecord generateExeclData(List<Map<String, String>> columnDomainList, 
	        List<SummaryModel> summaryModels, String excel_title){
	    
	    DataRecord dataRecord = new DataRecord();
	    
	    DataRow[] datarow = new DataRow[summaryModels.size()];
	    
	    for(int i = 0; i < summaryModels.size(); i++){

	        DataRow row = new DataRow();
	        
	        SummaryModel summaryModel = summaryModels.get(i);
	        for(Map<String, String> columnMap : columnDomainList){
	            
	            String name = columnMap.get("name");
	            String codecfg = columnMap.get("codecfg");
	            
	            String methodName = getSummaryModelValue(name, codecfg, summaryModel);
	            row.addDataCell(methodName, 1);
	        }
	        datarow[i] = row;
	    }
	    
	    //列名
        String[] columnName = null;
        if(Strings.isNotEmpty(columnDomainList)){
            
            columnName = new String[columnDomainList.size()];
            for(int i = 0; i < columnDomainList.size(); i++){
                Map<String, String> columnMap = columnDomainList.get(i);
                columnName[i] = columnMap.get("display");
            }
        }
        
        
	    if(columnName != null && columnName.length >= 1){
            dataRecord.setColumnName(columnName);
        }
	    
        try {
            dataRecord.addDataRow(datarow);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);;
        }
	    
	    if(Strings.isNotBlank(excel_title)){
            dataRecord.setTitle(excel_title);
            dataRecord.setSheetName(excel_title);
        }
	    return dataRecord;
	}
	
	
	
	
	
	
	//公文查询组装成页面要显示的excel数据
	public static DataRecord exportQueryToWebModel(HttpServletRequest request,List<EdocSummaryModel> results,String excel_title,Integer edocType,Map<String,String> edocSettingMap){	
	    EnumManager metadataManager = (EnumManager)AppContext.getBean("enumManagerNew");
        CtpEnumBean docTypeMeta = metadataManager.getEnum(MetadataNameEnum.edoc_doc_type.name());
        CtpEnumBean sendTypeMeta = metadataManager.getEnum(MetadataNameEnum.edoc_send_type.name());
	    List<String> labelList = null;    
		String colId = request.getParameter("colId");   
		if(colId != null && !"".equals(colId)){ 
			
//			Map<Integer,String> map = null;
//			if(edocType == 0){
//				map = EdocQueryColConstants.sendSearchEdocColMap;
//				map.putAll(EdocQueryColConstants.addedSearchLabel);
//			}else if(edocType == 1){
//				map = EdocQueryColConstants.recSearchEdocColMap;
//				map.putAll(EdocQueryColConstants.addedSearchLabel);
//			}else if(edocType == 2){
//				map = EdocQueryColConstants.signSearchEdocColMap;
//				map.putAll(EdocQueryColConstants.addedSearchLabel);
//			}else if(edocType == 3){//全部类型，包括发文，收文，签报
//			    map = EdocQueryColConstants.sendSearchEdocColMap;
//			    map.putAll(EdocQueryColConstants.recSearchEdocColMap);
//			    map.putAll(EdocQueryColConstants.signSearchEdocColMap);
//			    map.putAll(EdocQueryColConstants.addedSearchLabel);
//			}
			labelList = new ArrayList<String>();
			String[] cids = colId.split(",");
			for(int i=0;i<cids.length;i++){
				labelList.add(cids[i]);
			}
		}
		DataRecord dataRecord = new DataRecord();
		
		Locale local = LocaleContext.getLocale(request);
		String resource = "com.seeyon.v3x.edoc.resources.i18n.EdocResource";
		String com_resouce = "com.seeyon.v3x.common.resources.i18n.SeeyonCommonResources";
		
		//列名
		String[] columnName=new String[15];
		columnName[0] = ResourceBundleUtil.getString(resource, local, "edoc.element.secretlevel.simple");//文件密级
		columnName[1] = ResourceBundleUtil.getString(com_resouce, local, "common.subject.label");//标题
		columnName[2] = ResourceBundleUtil.getString(resource, local, "edoc.element.wordno.label");//文号
		
		columnName[6] = ResourceBundleUtil.getString(resource, local, "edoc.element.author");//拟稿人
		columnName[7] = ResourceBundleUtil.getString(resource, local, "edoc.element.doctype");//公文种类
		columnName[8] = ResourceBundleUtil.getString(resource, local, "edoc.element.sendtype");//行文类型
		columnName[9] = ResourceBundleUtil.getString(resource, local, "edoc.docmark.inner.title");//内部文号
		columnName[10] = ResourceBundleUtil.getString(resource, local, "edoc.element.sendunit");//发文单位
		columnName[11] = ResourceBundleUtil.getString(resource, local, "edoc.edoctitle.ispig.label");//是否归档
		columnName[12] = ResourceBundleUtil.getString(resource, local, "edoc.edoctitle.pigeonholePath.label");//归档路径
		columnName[13] = ResourceBundleUtil.getString(resource, local, "edoc.element.copies");//抄送份数
//        //根据国家行政公文规范,去掉主题词
//        //columnName[12] = ResourceBundleUtil.getString(resource, local, "menu.edoc.keyword.label");//主题词
		/** SP1 新增需求  公文查询列表头显示所有的查询条件  --xiangfan  Start*/
        /** SP1 新增需求  公文查询列表头显示所有的查询条件  --xiangfan  End*/
		
		String datePattern=ResourceBundleUtil.getString(com_resouce, local, "common.datetime.pattern");
		if(edocType.intValue() == EdocEnum.edocType.sendEdoc.ordinal()){
			columnName[3] = ResourceBundleUtil.getString(resource, local, "edoc.element.sendtounit");//主送单位
			columnName[4] = ResourceBundleUtil.getString(resource, local, "edoc.element.issuer");//建文人
			columnName[5] = ResourceBundleUtil.getString(resource, local, "edoc.element.sendingdate");//发文时间
			
			if(labelList != null){
				columnName=new String[labelList.size()];
				for(int j=0;j<labelList.size();j++){
					String label = labelList.get(j);
					columnName[j] = edocSettingMap.get(label);
				}
			}
			
			if (null != results && results.size() > 0) {
				DataRow[] datarow = new DataRow[results.size()];
				for (int i = 0; i < results.size(); i++) {
					EdocSummaryModel summaryModel = (EdocSummaryModel)results.get(i);
					EdocSummary summary=summaryModel.getSummary();
					String hasArchive="";
					if(summary.getHasArchive()){
						hasArchive = ResourceBundleUtil.getString(com_resouce, local, "common.true");
					}else if(!summary.getHasArchive()){
						hasArchive = ResourceBundleUtil.getString(com_resouce, local, "common.false");
					}
					DataRow row = new DataRow();
					if(labelList == null){
						row.addDataCell(null!=summary.getSecretLevel() ? String.valueOf(summary.getSecretLevel()) : "", 1);
						row.addDataCell(null!=summary.getSubject() ? String.valueOf(summary.getSubject()) : "", 1);
						row.addDataCell(null!=summary.getDocMark() ? String.valueOf(summary.getDocMark()) : "", 1);
						row.addDataCell(null!=summaryModel.getSendToUnit()? String.valueOf(summaryModel.getSendToUnit()) : "", 1);				
						row.addDataCell(null!=summary.getIssuer()? String.valueOf(summary.getIssuer()) : "", 1);
						row.addDataCell(null!=summary.getSigningDate()? String.valueOf(Datetimes.format(summary.getSigningDate(), datePattern)) : "", 1);
						row.addDataCell(null!=summary.getCreatePerson() ? String.valueOf(summary.getCreatePerson()) : "", 1);
						row.addDataCell(null!=summary.getDocType() ? ResourceBundleUtil.getString(resource, local, docTypeMeta.getItemLabel(String.valueOf(summary.getDocType())) ) : "", 1);
						row.addDataCell(null!=summary.getSendType() ? ResourceBundleUtil.getString(resource, local, sendTypeMeta.getItemLabel(String.valueOf(summary.getSendType()))) : "", 1);
						row.addDataCell(null!=summary.getSerialNo() ? String.valueOf(summary.getSerialNo()) : "", 1);
						row.addDataCell(null!=summary.getSendUnit() ? String.valueOf(summary.getSendUnit()) : "", 1);
						row.addDataCell(null!=hasArchive ? String.valueOf(hasArchive) : "", 1);
						row.addDataCell(null!=summaryModel.getArchiveName()? String.valueOf(summaryModel.getArchiveName()) : "", 1);
						row.addDataCell(null!=summary.getCopies() ? String.valueOf(summary.getCopies()) : "", 1);
						
						
                        /*根据国家行政公文规范,去掉主题词
                         * row.addDataCell(null!=summary.getKeywords() ? String.valueOf(summary.getKeywords()) : "", 1);*/
//                        row.addDataCell(null!=summary.getCreateTime() ? String.valueOf(sDateformat.format(summary.getCreateTime())) : "", 1);
					}
					else{
						rowAddDataCellForSearchEdocSetting(request, row, labelList, summaryModel,edocType);
					} 
					datarow[i] = row;
				}
				try {
					dataRecord.addDataRow(datarow);
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);;
				}
			}
			
		}else if(edocType.intValue() == EdocEnum.edocType.recEdoc.ordinal()){
			columnName[3] = ResourceBundleUtil.getString(resource, local, "edoc.edoctitle.fromUnit.label");//主送单位
			columnName[4] = ResourceBundleUtil.getString(resource, local, "edoc.edoctitle.regPerson.label");//建文人
			columnName[5] = ResourceBundleUtil.getString(resource, local, "edoc.edoctitle.regDate.label");//发文时间
			
			if(labelList != null){
				columnName=new String[labelList.size()];
				for(int j=0;j<labelList.size();j++){
					String label = labelList.get(j);
//					columnName[j] = ResourceBundleUtil.getString(resource, local, label);
					columnName[j] = edocSettingMap.get(label);
				}
			}
			if (null != results && results.size() > 0) {
				DataRow[] datarow = new DataRow[results.size()];
				for (int i = 0; i < results.size(); i++) {
					EdocSummaryModel summaryModel = (EdocSummaryModel)results.get(i);
					EdocSummary summary=summaryModel.getSummary();
					String hasArchive="";
					if(summary.getHasArchive()){
						hasArchive = ResourceBundleUtil.getString(com_resouce, local, "common.true");
					}else if(!summary.getHasArchive()){
						hasArchive = ResourceBundleUtil.getString(com_resouce, local, "common.false");
					}
					DataRow row = new DataRow();
					if(labelList == null){
						row.addDataCell(null!=summary.getSecretLevel() ? String.valueOf(summary.getSecretLevel()) : "", 1);
						row.addDataCell(null!=summary.getSubject() ? String.valueOf(summary.getSubject()) : "", 1);
						row.addDataCell(null!=summary.getDocMark() ? String.valueOf(summary.getDocMark()) : "", 1);
						row.addDataCell(null!=summary.getSendUnit()? String.valueOf(summary.getSendUnit()) : "", 1);				
						row.addDataCell(null!=summary.getCreatePerson()? String.valueOf(summary.getCreatePerson()) : "", 1);
						row.addDataCell(null!=summary.getCreateTime()? String.valueOf(Datetimes.format(summary.getCreateTime(), datePattern)) : "", 1);
						row.addDataCell(null!=summary.getCreatePerson() ? String.valueOf(summary.getCreatePerson()) : "", 1);
						row.addDataCell(null!=summary.getDocType() ? ResourceBundleUtil.getString(resource, local, docTypeMeta.getItemLabel(String.valueOf(summary.getDocType())) ) : "", 1);
						row.addDataCell(null!=summary.getSendType() ? ResourceBundleUtil.getString(resource, local, sendTypeMeta.getItemLabel(String.valueOf(summary.getSendType()))) : "", 1);
						row.addDataCell(null!=summary.getSerialNo() ? String.valueOf(summary.getSerialNo()) : "", 1);
						row.addDataCell(null!=summary.getSendUnit() ? String.valueOf(summary.getSendUnit()) : "", 1);
						row.addDataCell(null!=hasArchive ? String.valueOf(hasArchive) : "", 1);
						row.addDataCell(null!=summaryModel.getArchiveName()? String.valueOf(summaryModel.getArchiveName()) : "", 1);
						row.addDataCell(null!=summary.getCopies() ? String.valueOf(summary.getCopies()) : "", 1);
						
                        /*根据国家行政公文规范,去掉主题词
                         * row.addDataCell(null!=summary.getKeywords() ? String.valueOf(summary.getKeywords()) : "", 1);*/
           
					}
					else{
						rowAddDataCellForSearchEdocSetting(request, row, labelList, summaryModel,edocType);
					} 
					datarow[i] = row;
				}
				try {
					dataRecord.addDataRow(datarow);
				} catch (Exception e) {
								LOGGER.error(e.getMessage(), e);;
				}
			}
		}else if(edocType.intValue() == EdocEnum.edocType.signReport.ordinal()){
			columnName[3] = ResourceBundleUtil.getString(resource, local, "edoc.element.sendtounit");//主送单位
			columnName[4] = ResourceBundleUtil.getString(resource, local, "edoc.create.person");//建文人
			columnName[5] = ResourceBundleUtil.getString(resource, local, "edoc.edoctitle.createDate.label");//发文时间
			  List<String> wordList = new ArrayList<String>(Arrays.asList(columnName));
	            wordList.remove(columnName[4]);
				columnName = wordList.toArray(columnName);
			if(labelList != null){
				columnName=new String[labelList.size()];
				for(int j=0;j<labelList.size();j++){
					String label = labelList.get(j);
//					columnName[j] = ResourceBundleUtil.getString(resource, local, label);
					columnName[j] = edocSettingMap.get(label);
					
				}
			}
			if (null != results && results.size() > 0) {
				DataRow[] datarow = new DataRow[results.size()];
				for (int i = 0; i < results.size(); i++) {
					EdocSummaryModel summaryModel = (EdocSummaryModel)results.get(i);
					EdocSummary summary=summaryModel.getSummary();
					String hasArchive="";
					if(summary.getHasArchive()){
						hasArchive = ResourceBundleUtil.getString(com_resouce, local, "common.true");
					}else if(!summary.getHasArchive()){
						hasArchive = ResourceBundleUtil.getString(com_resouce, local, "common.false");
					}
					DataRow row = new DataRow();
					if(labelList == null){
						row.addDataCell(null!=summary.getSecretLevel() ? String.valueOf(summary.getSecretLevel()) : "", 1);
						row.addDataCell(null!=summary.getSubject() ? String.valueOf(summary.getSubject()) : "", 1);
						row.addDataCell(null!=summary.getDocMark() ? String.valueOf(summary.getDocMark()) : "", 1);
						row.addDataCell(null!=summaryModel.getSendToUnit()? String.valueOf(summaryModel.getSendToUnit()) : "", 1);				
//						row.addDataCell(null!=summary.getCreatePerson()? String.valueOf(summary.getCreatePerson()) : "", 1);
						row.addDataCell(null!=summary.getCreateTime()? String.valueOf(Datetimes.format(summary.getCreateTime(), datePattern)) : "", 1);
						row.addDataCell(null!=summary.getCreatePerson() ? String.valueOf(summary.getCreatePerson()) : "", 1);
						row.addDataCell(null!=summary.getDocType() ? ResourceBundleUtil.getString(resource, local, docTypeMeta.getItemLabel(String.valueOf(summary.getDocType())) ) : "", 1);
						row.addDataCell(null!=summary.getSendType() ? ResourceBundleUtil.getString(resource, local, sendTypeMeta.getItemLabel(String.valueOf(summary.getSendType()))) : "", 1);
						row.addDataCell(null!=summary.getSerialNo() ? String.valueOf(summary.getSerialNo()) : "", 1);
						row.addDataCell(null!=summary.getSendUnit() ? String.valueOf(summary.getSendUnit()) : "", 1);
						row.addDataCell(null!=hasArchive ? String.valueOf(hasArchive) : "", 1);
						row.addDataCell(null!=summaryModel.getArchiveName()? String.valueOf(summaryModel.getArchiveName()) : "", 1);
						row.addDataCell(null!=summary.getCopies() ? String.valueOf(summary.getCopies()) : "", 1);
					}
					else{
						rowAddDataCellForSearchEdocSetting(request, row, labelList, summaryModel,edocType);
					}
					
					datarow[i] = row;
				}
				try {
					dataRecord.addDataRow(datarow);
				} catch (Exception e) {
								LOGGER.error(e.getMessage(), e);;
				}
			}
		}else if(edocType.intValue() == 3){//导出 发文，收文，签文三种类型的全部数据
            columnName[3] = ResourceBundleUtil.getString(resource, local, "edoc.element.sendtounit");//主送单位
            columnName[4] = ResourceBundleUtil.getString(resource, local, "edoc.create.person");//建文人
            columnName[5] = ResourceBundleUtil.getString(resource, local, "edoc.edoctitle.createDate.label");//发文时间
            List<String> wordList = new ArrayList<String>(Arrays.asList(columnName));
            wordList.remove(columnName[3]);
            wordList.remove(columnName[4]);
            wordList.remove(columnName[5]);
			columnName = wordList.toArray(new String[wordList.size()]);
            if(labelList != null){
                columnName=new String[labelList.size()];
                for(int j=0;j<labelList.size();j++){
                    String label = labelList.get(j);
//                    columnName[j] = ResourceBundleUtil.getString(resource, local, label);
                    columnName[j] = edocSettingMap.get(label);
                }
            }
            if (null != results && results.size() > 0) {
                DataRow[] datarow = new DataRow[results.size()];
                for (int i = 0; i < results.size(); i++) {
                    EdocSummaryModel summaryModel = (EdocSummaryModel)results.get(i);
                    EdocSummary summary=summaryModel.getSummary();
                    String hasArchive="";
                    if(summary.getHasArchive()){
                        hasArchive = ResourceBundleUtil.getString(com_resouce, local, "common.true");
                    }else if(!summary.getHasArchive()){
                        hasArchive = ResourceBundleUtil.getString(com_resouce, local, "common.false");
                    }
                    DataRow row = new DataRow();
                    if(labelList == null){
                        row.addDataCell(null!=summary.getSecretLevel() ? String.valueOf(summary.getSecretLevel()) : "", 1);
                        row.addDataCell(null!=summary.getSubject() ? String.valueOf(summary.getSubject()) : "", 1);
                        row.addDataCell(null!=summary.getDocMark() ? String.valueOf(summary.getDocMark()) : "", 1);
//                        row.addDataCell(null!=summaryModel.getSendToUnit()? String.valueOf(summaryModel.getSendToUnit()) : "", 1);              
//                        row.addDataCell(null!=summary.getCreatePerson()? String.valueOf(summary.getCreatePerson()) : "", 1);
//                        row.addDataCell(null!=summary.getCreateTime()? String.valueOf(sDateformat.format(summary.getCreateTime())) : "", 1);
                        row.addDataCell(null!=summary.getCreatePerson() ? String.valueOf(summary.getCreatePerson()) : "", 1);
						row.addDataCell(null!=summary.getDocType() ? ResourceBundleUtil.getString(resource, local, docTypeMeta.getItemLabel(String.valueOf(summary.getDocType())) ) : "", 1);
						row.addDataCell(null!=summary.getSendType() ? ResourceBundleUtil.getString(resource, local, sendTypeMeta.getItemLabel(String.valueOf(summary.getSendType()))) : "", 1);
						row.addDataCell(null!=summary.getSerialNo() ? String.valueOf(summary.getSerialNo()) : "", 1);
						row.addDataCell(null!=summary.getSendUnit() ? String.valueOf(summary.getSendUnit()) : "", 1);
						row.addDataCell(null!=hasArchive ? String.valueOf(hasArchive) : "", 1);
						row.addDataCell(null!=summaryModel.getArchiveName()? String.valueOf(summaryModel.getArchiveName()) : "", 1);
						row.addDataCell(null!=summary.getCopies() ? String.valueOf(summary.getCopies()) : "", 1);
                    }
                    else{
                    	rowAddDataCellForSearchEdocSetting(request, row, labelList, summaryModel,edocType);
                    }
                    
                    datarow[i] = row;
                }
                try {
                    dataRecord.addDataRow(datarow);
                } catch (Exception e) {
                                LOGGER.error(e.getMessage(), e);;
                }
            }
        }
		dataRecord.setColumnName(columnName);
		dataRecord.setTitle(excel_title);
		dataRecord.setSheetName(excel_title);	
		return dataRecord;
	}
	private static void rowAddDataCellForSearchEdocSetting(HttpServletRequest request,DataRow row,List<String> labelList,EdocSummaryModel summaryModel,Integer edocType){
		Locale local = LocaleContext.getLocale(request);
		EdocSummary summary = summaryModel.getSummary();
		EnumManager metadataManager = (EnumManager)AppContext.getBean("enumManagerNew");
        CtpEnumBean urgentMeta = metadataManager.getEnum(MetadataNameEnum.edoc_urgent_level.name());
        CtpEnumBean unitLevelMeta = metadataManager.getEnum(MetadataNameEnum.edoc_unit_level.name());
        String resource = "com.seeyon.v3x.edoc.resources.i18n.EdocResource";
        String com_resouce = "com.seeyon.v3x.common.resources.i18n.SeeyonCommonResources";
        CtpEnumBean edocKeepPeriodData = metadataManager.getEnum(EnumNameEnum.edoc_keep_period.name());
        CtpEnumBean docTypeData = metadataManager.getEnum(EnumNameEnum.edoc_doc_type.name());
        CtpEnumBean sendTypeData = metadataManager.getEnum(EnumNameEnum.edoc_send_type.name());
        String datePattern=ResourceBundleUtil.getString(com_resouce, local, "common.datetime.pattern");
		String hasArchive="";
		if(summary.getHasArchive()){
			hasArchive = ResourceBundleUtil.getString(com_resouce, local, "common.true");
		}else if(!summary.getHasArchive()){
			hasArchive = ResourceBundleUtil.getString(com_resouce, local, "common.false");
		}
		for(int j=0;j<labelList.size();j++){
			String label = labelList.get(j);
			if(label.equals(EdocElementConstants.EDOC_ELEMENT_SUBJECT)){
				   row.addDataCell(null!=summary.getSubject() ? summary.getSubject() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DOC_MARK)){
				   row.addDataCell(null!=summary.getDocMark() ? summary.getDocMark() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_SERIAL_NO)){
				   row.addDataCell(null!=summary.getSerialNo() ? summary.getSerialNo() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_CREATE_PERSON)){
				   row.addDataCell(null!=summary.getCreatePerson() ? summary.getCreatePerson() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_SEND_UNIT)){
				   row.addDataCell(null!=summary.getSendUnit() ? summary.getSendUnit() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_ISSUER)){
				   row.addDataCell(null!=summary.getIssuer() ? summary.getIssuer() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_SEND_TO)){
				   row.addDataCell(null!=summary.getSendTo() ? summary.getSendTo() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_COPY_TO)){
				   row.addDataCell(null!=summary.getCopyTo() ? summary.getCopyTo() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_REPORT_TO)){
				   row.addDataCell(null!=summary.getReportTo() ? summary.getReportTo() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_PRINT_UNIT)){
				   row.addDataCell(null!=summary.getPrintUnit() ? summary.getPrintUnit() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_PRINTER)){
				   row.addDataCell(null!=summary.getPrinter() ? summary.getPrinter() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DOC_MARK2)){
				   row.addDataCell(null!=summary.getDocMark2() ? summary.getDocMark2() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_SEND_TO2)){
				   row.addDataCell(null!=summary.getSendTo2() ? summary.getSendTo2() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_COPY_TO2)){
				   row.addDataCell(null!=summary.getCopyTo2() ? summary.getCopyTo2() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_REPORT_TO2)){
				   row.addDataCell(null!=summary.getReportTo2() ? summary.getReportTo2() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_SEND_UNIT2)){
				   row.addDataCell(null!=summary.getSendUnit2() ? summary.getSendUnit2() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_SEND_DEPARTMENT)){
				   row.addDataCell(null!=summary.getSendDepartment() ? summary.getSendDepartment() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_SEND_DEPARTMENT2)){
				   row.addDataCell(null!=summary.getSendDepartment2() ? summary.getSendDepartment2() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_PHONE)){
				   row.addDataCell(null!=summary.getPhone() ? summary.getPhone() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_AUDITOR)){
				   row.addDataCell(null!=summary.getAuditor() ? summary.getAuditor() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_REVIEW)){
				   row.addDataCell(null!=summary.getReview() ? summary.getReview() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_UNDERTAKER)){
				   row.addDataCell(null!=summary.getUndertaker() ? summary.getUndertaker() : "", 1);
				}else if(label.equals(EdocElementConstants.EDOC_ELEMENT_UNDERTAKENOFFICE)){
				    row.addDataCell(null!=summary.getUndertakenoffice() ? summary.getUndertakenoffice() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_COPIES)){
				   row.addDataCell(null!=summary.getCopies() ? String.valueOf(summary.getCopies()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_COPIES2)){
				   row.addDataCell(null != summary.getCopies2() ? String.valueOf(summary.getCopies2()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_SIGNING_DATE)){
				   row.addDataCell(null!=summary.getSigningDate() ? Datetimes.format(summary.getSigningDate(), datePattern) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_CREATEDATE)){
					if(edocType.intValue() == 1){
						row.addDataCell("", 1);
					}else{
						row.addDataCell(null!=summary.getCreateTime() ? Datetimes.format(summary.getCreateTime(), datePattern) : "", 1);
					}
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_PACKDATE)){
				   row.addDataCell(null!=summary.getPackTime() ? Datetimes.format(summary.getPackTime(), datePattern) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_RECEIPT_DATE)){
				   row.addDataCell(null!=summary.getReceiptDate() ? Datetimes.format(summary.getReceiptDate(), datePattern) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_REGISTRATION_DATE)){
					if(edocType.intValue() == 0){
						row.addDataCell("", 1);
					}else{
						row.addDataCell(null!=summary.getCreateTime() ? Datetimes.format(summary.getCreateTime(), datePattern) : "", 1);
					}
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DOC_TYPE)){
//					row.addDataCell(null!=summary.getDocType() ? ResourceBundleUtil.getString(colMetadata.get("edoc_doc_type").getResourceBundle(),colMetadata.get("edoc_doc_type").getItemLabel(summary.getDocType())) : "", 1);
					String docTypeLabel = docTypeData.getItemLabel(String.valueOf(summary.getDocType())) ;
					String docTypeName = ResourceBundleUtil.getString(resource, local, docTypeLabel);
					row.addDataCell(null!=summary.getDocType() ? docTypeName : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_SEND_TYPE)){
//					row.addDataCell(null!=summary.getSendType() ? ResourceBundleUtil.getString(colMetadata.get("edoc_send_type").getResourceBundle(),colMetadata.get("edoc_send_type").getItemLabel(summary.getSendType())) : "", 1);
					String sendTypeLabel = sendTypeData.getItemLabel(String.valueOf(summary.getSendType())) ;
					String sendTypeName = ResourceBundleUtil.getString(resource, local, sendTypeLabel);
					row.addDataCell(null!=summary.getSendType() ? sendTypeName : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_SECRET_LEVEL)){
				   row.addDataCell(null!=summary.getSecretLevel() ? summary.getSecretLevel() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_URGENT_LEVEL)){
					String urgentLabel = urgentMeta.getItemLabel(String.valueOf(summary.getUrgentLevel())) ;
					String urgentName = ResourceBundleUtil.getString(resource, local, urgentLabel);
					row.addDataCell(null!=summary.getUrgentLevel() ? urgentName : "", 1);
				}
                else if(label.equals(EdocElementConstants.EDOC_ELEMENT_UNIT_LEVEL)){//公文级别
                    String unitLabel = unitLevelMeta.getItemLabel(String.valueOf(summary.getUnitLevel())) ;
                    String unitName = ResourceBundleUtil.getString(resource, local, unitLabel);
                    row.addDataCell(null!=summary.getUnitLevel() ? unitName : "", 1);
                }
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_KEEP_PERIOD)){
//				   row.addDataCell(null!=summary.getKeepPeriod() ? String.valueOf(summary.getKeepPeriod()) : "", 1);
					String keepLabel = ResourceBundleUtil.getString(resource, local,edocKeepPeriodData.getItemLabel(String.valueOf(summary.getKeepPeriod()))); 
                    row.addDataCell(null!=summary.getKeepPeriod()? keepLabel : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING1)){
				   row.addDataCell(null!=summary.getVarchar1() ? summary.getVarchar1() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING2)){
				   row.addDataCell(null!=summary.getVarchar2() ? summary.getVarchar2() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING3)){
				   row.addDataCell(null!=summary.getVarchar3() ? summary.getVarchar3() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING4)){
				   row.addDataCell(null!=summary.getVarchar4() ? summary.getVarchar4() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING5)){
				   row.addDataCell(null!=summary.getVarchar5() ? summary.getVarchar5() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING6)){
				   row.addDataCell(null!=summary.getVarchar6() ? summary.getVarchar6() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING7)){
				   row.addDataCell(null!=summary.getVarchar7() ? summary.getVarchar7() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING8)){
				   row.addDataCell(null!=summary.getVarchar8() ? summary.getVarchar8() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING9)){
				   row.addDataCell(null!=summary.getVarchar9() ? summary.getVarchar9() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING10)){
				   row.addDataCell(null!=summary.getVarchar10() ? summary.getVarchar10() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING11)){
				   row.addDataCell(null!=summary.getVarchar11() ? summary.getVarchar11() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING12)){
				   row.addDataCell(null!=summary.getVarchar12() ? summary.getVarchar12() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING13)){
				   row.addDataCell(null!=summary.getVarchar13() ? summary.getVarchar13() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING14)){
				   row.addDataCell(null!=summary.getVarchar14() ? summary.getVarchar14() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING15)){
				   row.addDataCell(null!=summary.getVarchar15() ? summary.getVarchar15() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING16)){
				   row.addDataCell(null!=summary.getVarchar16() ? summary.getVarchar16() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING17)){
				   row.addDataCell(null!=summary.getVarchar17() ? summary.getVarchar17() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING18)){
				   row.addDataCell(null!=summary.getVarchar18() ? summary.getVarchar18() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING19)){
				   row.addDataCell(null!=summary.getVarchar19() ? summary.getVarchar19() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING20)){
				   row.addDataCell(null!=summary.getVarchar20() ? summary.getVarchar20() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING21)){
				   row.addDataCell(null!=summary.getVarchar21() ? summary.getVarchar21() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING22)){
				   row.addDataCell(null!=summary.getVarchar22() ? summary.getVarchar22() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING23)){
				   row.addDataCell(null!=summary.getVarchar23() ? summary.getVarchar23() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING24)){
				   row.addDataCell(null!=summary.getVarchar24() ? summary.getVarchar24() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING25)){
				   row.addDataCell(null!=summary.getVarchar25() ? summary.getVarchar25() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING26)){
				   row.addDataCell(null!=summary.getVarchar26() ? summary.getVarchar26() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING27)){
				   row.addDataCell(null!=summary.getVarchar27() ? summary.getVarchar27() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING28)){
				   row.addDataCell(null!=summary.getVarchar28() ? summary.getVarchar28() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING29)){
				   row.addDataCell(null!=summary.getVarchar29() ? summary.getVarchar29() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_STRING30)){
				   row.addDataCell(null!=summary.getVarchar30() ? summary.getVarchar30() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT1)){
				   row.addDataCell(null!=summary.getText1() ? summary.getText1() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT2)){
				   row.addDataCell(null!=summary.getText2() ? summary.getText2() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT3)){
				   row.addDataCell(null!=summary.getText3() ? summary.getText3() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT4)){
				   row.addDataCell(null!=summary.getText4() ? summary.getText4() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT5)){
				   row.addDataCell(null!=summary.getText5() ? summary.getText5() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT6)){
				   row.addDataCell(null!=summary.getText6() ? summary.getText6() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT7)){
				   row.addDataCell(null!=summary.getText7() ? summary.getText7() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT8)){
				   row.addDataCell(null!=summary.getText8() ? summary.getText8() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT9)){
				   row.addDataCell(null!=summary.getText9() ? summary.getText9() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT10)){
				   row.addDataCell(null!=summary.getText10() ? summary.getText10() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT11)){
				   row.addDataCell(null!=summary.getText11() ? summary.getText11() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT12)){
				   row.addDataCell(null!=summary.getText12() ? summary.getText12() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT13)){
				   row.addDataCell(null!=summary.getText13() ? summary.getText13() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT14)){
				   row.addDataCell(null!=summary.getText14() ? summary.getText14() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT15)){
				   row.addDataCell(null!=summary.getText15() ? summary.getText15() : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER1)){
				   row.addDataCell(null!=summary.getInteger1() ? String.valueOf(summary.getInteger1()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER2)){
				   row.addDataCell(null!=summary.getInteger2() ? String.valueOf(summary.getInteger2()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER3)){
				   row.addDataCell(null!=summary.getInteger3() ? String.valueOf(summary.getInteger3()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER4)){
				   row.addDataCell(null!=summary.getInteger4() ? String.valueOf(summary.getInteger4()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER5)){
				   row.addDataCell(null!=summary.getInteger5() ? String.valueOf(summary.getInteger5()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER6)){
				   row.addDataCell(null!=summary.getInteger6() ? String.valueOf(summary.getInteger6()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER7)){
				   row.addDataCell(null!=summary.getInteger7() ? String.valueOf(summary.getInteger7()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER8)){
				   row.addDataCell(null!=summary.getInteger8() ? String.valueOf(summary.getInteger8()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER9)){
				   row.addDataCell(null!=summary.getInteger9() ? String.valueOf(summary.getInteger9()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER10)){
				   row.addDataCell(null!=summary.getInteger10() ? String.valueOf(summary.getInteger10()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER11)){
				   row.addDataCell(null!=summary.getInteger11() ? String.valueOf(summary.getInteger11()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER12)){
				   row.addDataCell(null!=summary.getInteger12() ? String.valueOf(summary.getInteger12()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER13)){
				   row.addDataCell(null!=summary.getInteger13() ? String.valueOf(summary.getInteger13()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER14)){
				   row.addDataCell(null!=summary.getInteger14() ? String.valueOf(summary.getInteger14()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER15)){
				   row.addDataCell(null!=summary.getInteger15() ? String.valueOf(summary.getInteger15()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER16)){
				   row.addDataCell(null!=summary.getInteger16() ? String.valueOf(summary.getInteger16()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER17)){
				   row.addDataCell(null!=summary.getInteger17() ? String.valueOf(summary.getInteger17()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER18)){
				   row.addDataCell(null!=summary.getInteger18() ? String.valueOf(summary.getInteger18()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER19)){
				   row.addDataCell(null!=summary.getInteger19() ? String.valueOf(summary.getInteger19()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER20)){
				   row.addDataCell(null!=summary.getInteger20() ? String.valueOf(summary.getInteger20()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL1)){
				   row.addDataCell(null!=summary.getDecimal1() ? String.valueOf(summary.getDecimal1()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL2)){
				   row.addDataCell(null!=summary.getDecimal2() ? String.valueOf(summary.getDecimal2()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL3)){
				   row.addDataCell(null!=summary.getDecimal3() ? String.valueOf(summary.getDecimal3()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL4)){
				   row.addDataCell(null!=summary.getDecimal4() ? String.valueOf(summary.getDecimal4()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL5)){
				   row.addDataCell(null!=summary.getDecimal5() ? String.valueOf(summary.getDecimal5()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL6)){
				   row.addDataCell(null!=summary.getDecimal6() ? String.valueOf(summary.getDecimal6()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL7)){
				   row.addDataCell(null!=summary.getDecimal7() ? String.valueOf(summary.getDecimal7()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL8)){
				   row.addDataCell(null!=summary.getDecimal8() ? String.valueOf(summary.getDecimal8()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL9)){
				   row.addDataCell(null!=summary.getDecimal9() ? String.valueOf(summary.getDecimal9()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL10)){
				   row.addDataCell(null!=summary.getDecimal10() ? String.valueOf(summary.getDecimal10()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL11)){
				   row.addDataCell(null!=summary.getDecimal11() ? String.valueOf(summary.getDecimal11()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL12)){
				   row.addDataCell(null!=summary.getDecimal12() ? String.valueOf(summary.getDecimal12()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL13)){
				   row.addDataCell(null!=summary.getDecimal13() ? String.valueOf(summary.getDecimal13()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL14)){
				   row.addDataCell(null!=summary.getDecimal14() ? String.valueOf(summary.getDecimal14()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL15)){
				   row.addDataCell(null!=summary.getDecimal15() ? String.valueOf(summary.getDecimal15()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL16)){
				   row.addDataCell(null!=summary.getDecimal16() ? String.valueOf(summary.getDecimal16()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL17)){
				   row.addDataCell(null!=summary.getDecimal17() ? String.valueOf(summary.getDecimal17()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL18)){
				   row.addDataCell(null!=summary.getDecimal18() ? String.valueOf(summary.getDecimal18()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL19)){
				   row.addDataCell(null!=summary.getDecimal19() ? String.valueOf(summary.getDecimal19()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL20)){
				   row.addDataCell(null!=summary.getDecimal20() ? String.valueOf(summary.getDecimal20()) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DATE1)){
				   row.addDataCell(null!=summary.getDate1() ? Datetimes.format(summary.getDate1(), datePattern) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DATE2)){
				   row.addDataCell(null!=summary.getDate2() ? Datetimes.format(summary.getDate2(), datePattern) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DATE3)){
				   row.addDataCell(null!=summary.getDate3() ? Datetimes.format(summary.getDate3(), datePattern) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DATE4)){
				   row.addDataCell(null!=summary.getDate4() ? Datetimes.format(summary.getDate4(), datePattern) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DATE5)){
				   row.addDataCell(null!=summary.getDate5() ? Datetimes.format(summary.getDate5(), datePattern) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DATE6)){
				   row.addDataCell(null!=summary.getDate6() ? Datetimes.format(summary.getDate6(), datePattern) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DATE7)){
				   row.addDataCell(null!=summary.getDate7() ? Datetimes.format(summary.getDate7(), datePattern) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DATE8)){
				   row.addDataCell(null!=summary.getDate8() ? Datetimes.format(summary.getDate8(), datePattern) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DATE9)){
				   row.addDataCell(null!=summary.getDate9() ? Datetimes.format(summary.getDate9(), datePattern) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DATE10)){
				   row.addDataCell(null!=summary.getDate10() ? Datetimes.format(summary.getDate10(), datePattern) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DATE11)){
				   row.addDataCell(null!=summary.getDate11() ? Datetimes.format(summary.getDate11(), datePattern) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DATE12)){
				   row.addDataCell(null!=summary.getDate12() ? Datetimes.format(summary.getDate12(), datePattern) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DATE13)){
				   row.addDataCell(null!=summary.getDate13() ? Datetimes.format(summary.getDate13(), datePattern) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DATE14)){
				   row.addDataCell(null!=summary.getDate14() ? Datetimes.format(summary.getDate14(), datePattern) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DATE15)){
				   row.addDataCell(null!=summary.getDate15() ? Datetimes.format(summary.getDate15(), datePattern) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DATE16)){
				   row.addDataCell(null!=summary.getDate16() ? Datetimes.format(summary.getDate16(), datePattern) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DATE17)){
				   row.addDataCell(null!=summary.getDate17() ? Datetimes.format(summary.getDate17(), datePattern) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DATE18)){
				   row.addDataCell(null!=summary.getDate18() ? Datetimes.format(summary.getDate18(), datePattern) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DATE19)){
				   row.addDataCell(null!=summary.getDate19() ? Datetimes.format(summary.getDate19(), datePattern) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_DATE20)){
				   row.addDataCell(null!=summary.getDate20() ? Datetimes.format(summary.getDate20(), datePattern) : "", 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_LIST1)){
					String listElementValue = "";
					if(null!=summary.getList1()){
						String list = summary.getList1();
						listElementValue = getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST1, summary.getOrgAccountId(), list, local, resource);
					}
				   row.addDataCell(listElementValue, 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_LIST2)){
					String listElementValue = "";
					if(null!=summary.getList2()){
						String list = summary.getList2();
						listElementValue = getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST2, summary.getOrgAccountId(), list, local, resource);
					}
				   row.addDataCell(listElementValue, 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_LIST3)){
					String listElementValue = "";
					if(null!=summary.getList3()){
						String list = summary.getList3();
						listElementValue = getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST3, summary.getOrgAccountId(), list, local, resource);
					}
				   row.addDataCell(listElementValue, 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_LIST4)){
					String listElementValue = "";
					if(null!=summary.getList4()){
						String list = summary.getList4();
						listElementValue = getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST4, summary.getOrgAccountId(), list, local, resource);
					}
				   row.addDataCell(listElementValue, 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_LIST5)){
					String listElementValue = "";
					if(null!=summary.getList5()){
						String list = summary.getList5();
						listElementValue = getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST5, summary.getOrgAccountId(), list, local, resource);
					}
				   row.addDataCell(listElementValue, 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_LIST6)){
					String listElementValue = "";
					if(null!=summary.getList6()){
						String list = summary.getList6();
						listElementValue = getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST6, summary.getOrgAccountId(), list, local, resource);
					}
				   row.addDataCell(listElementValue, 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_LIST7)){
					String listElementValue = "";
					if(null!=summary.getList7()){
						String list = summary.getList7();
						listElementValue = getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST7, summary.getOrgAccountId(), list, local, resource);
					}
				   row.addDataCell(listElementValue, 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_LIST8)){
					String listElementValue = "";
					if(null!=summary.getList8()){
						String list = summary.getList8();
						listElementValue = getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST8, summary.getOrgAccountId(), list, local, resource);
					}
				   row.addDataCell(listElementValue, 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_LIST9)){
					String listElementValue = "";
					if(null!=summary.getList9()){
						String list = summary.getList9();
						listElementValue = getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST9, summary.getOrgAccountId(), list, local, resource);
					}
				   row.addDataCell(listElementValue, 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_LIST10)){
					String listElementValue = "";
					if(null!=summary.getList10()){
						String list = summary.getList10();
						listElementValue = getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST10, summary.getOrgAccountId(), list, local, resource);
					}
				   row.addDataCell(listElementValue, 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_LIST11)){
					String listElementValue = "";
					if(null!=summary.getList11()){
						String list = summary.getList11();
						listElementValue = getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST11, summary.getOrgAccountId(), list, local, resource);
					}
				   row.addDataCell(listElementValue, 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_LIST12)){
					String listElementValue = "";
					if(null!=summary.getList12()){
						String list = summary.getList12();
						listElementValue = getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST12, summary.getOrgAccountId(), list, local, resource);
					}
				   row.addDataCell(listElementValue, 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_LIST13)){
					String listElementValue = "";
					if(null!=summary.getList13()){
						String list = summary.getList13();
						listElementValue = getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST13, summary.getOrgAccountId(), list, local, resource);
					}
				   row.addDataCell(listElementValue, 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_LIST14)){
					String listElementValue = "";
					if(null!=summary.getList14()){
						String list = summary.getList14();
						listElementValue = getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST14, summary.getOrgAccountId(), list, local, resource);
					}
				   row.addDataCell(listElementValue, 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_LIST15)){
					String listElementValue = "";
					if(null!=summary.getList15()){
						String list = summary.getList15();
						listElementValue = getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST15, summary.getOrgAccountId(), list, local, resource);
					}
				   row.addDataCell(listElementValue, 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_LIST16)){
					String listElementValue = "";
					if(null!=summary.getList16()){
						String list = summary.getList16();
						listElementValue = getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST16, summary.getOrgAccountId(), list, local, resource);
					}
				   row.addDataCell(listElementValue, 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_LIST17)){
					String listElementValue = "";
					if(null!=summary.getList17()){
						String list = summary.getList17();
						listElementValue = getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST17, summary.getOrgAccountId(), list, local, resource);
					}
				   row.addDataCell(listElementValue, 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_LIST18)){
					String listElementValue = "";
					if(null!=summary.getList18()){
						String list = summary.getList18();
						listElementValue = getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST18, summary.getOrgAccountId(), list, local, resource);
					}
				   row.addDataCell(listElementValue, 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_LIST19)){
					String listElementValue = "";
					if(null!=summary.getList19()){
						String list = summary.getList19();
						listElementValue = getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST19, summary.getOrgAccountId(), list, local, resource);
					}
				   row.addDataCell(listElementValue, 1);
				}
				else if(label.equals(EdocElementConstants.EDOC_ELEMENT_LIST20)){
					String listElementValue = "";
					if(null!=summary.getList20()){
						String list = summary.getList20();
						listElementValue = getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST20, summary.getOrgAccountId(), list, local, resource);
					}
				   row.addDataCell(listElementValue, 1);
				}
				else if(label.equals(EdocQueryColConstants.ISPIG)){
					row.addDataCell(null!=hasArchive ? String.valueOf(hasArchive) : "", 1);
				}
				else if(label.equals(EdocQueryColConstants.PIGE_PATH)){
					row.addDataCell(null!=summaryModel.getArchiveName()? String.valueOf(summaryModel.getArchiveName()) : "", 1);
				}
//				 else if(label.equals(EdocQueryColConstants.EDOC_START_DATE)){
//                    row.addDataCell(null!=summary.getCreateTime() ? String.valueOf(sDateformat1.format(summary.getCreateTime())) : "", 1);
//                }

		}
	
	}
	public static DataRecord exportQuery(HttpServletRequest request,List<EdocStat> results,String excel_title){

		DataRecord dataRecord = new DataRecord();

		Locale local = LocaleContext.getLocale(request);
		String resource = "com.seeyon.v3x.edoc.resources.i18n.EdocResource";
		String com_resouce = "com.seeyon.v3x.common.resources.i18n.SeeyonCommonResources";
//		导出excel文件的国际化

		String title = ResourceBundleUtil.getString(com_resouce, local, "common.subject.label");//主题
		String wordNo = ResourceBundleUtil.getString(resource, local, "edoc.element.wordno.label");//文号
		String date = ResourceBundleUtil.getString(com_resouce, local, "common.date.sendtime.label");//发文时间
		String signPerson = ResourceBundleUtil.getString(resource, local, "edoc.element.issuer");//签发人
		String mainToDep = ResourceBundleUtil.getString(resource, local, "edoc.element.sendtounit");//主送单位
		String copyToDep = ResourceBundleUtil.getString(resource, local, "edoc.element.copytounit");//抄送单位
		String copies = ResourceBundleUtil.getString(resource, local, "edoc.element.copies");//抄送份数
		String remark = ResourceBundleUtil.getString(resource, local, "edoc.stat.remark.label");//备考
			
		//Pagination.setNeedCount(false);
		if (null != results && results.size() > 0) {
			DataRow[] datarow = new DataRow[results.size()];
			for (int i = 0; i < results.size(); i++) {
				EdocStat stat = results.get(i);
				DataRow row = new DataRow();
				
				row.addDataCell(null!=stat.getSubject() ? String.valueOf(stat.getSubject()) : "", 1);
				row.addDataCell(null!=stat.getDocMark() ? String.valueOf(stat.getDocMark()) : "", 1);
				row.addDataCell(null!=stat.getCreateDate() ? String.valueOf(Datetimes.formatDate(stat.getCreateDate())) : "", 1);			
				row.addDataCell(null!=stat.getIssuer() ? String.valueOf(stat.getIssuer()) : "", 1);
				row.addDataCell(null!=stat.getSendTo() ? String.valueOf(stat.getSendTo()) : "", 1);
				row.addDataCell(null!=stat.getCopyTo() ? String.valueOf(stat.getCopyTo()) : "", 1);
				row.addDataCell(null!=stat.getCopies() ? String.valueOf(stat.getCopies()) : "", 1);
				row.addDataCell(null!=stat.getRemark() ? String.valueOf(stat.getRemark()) : "", 1);
				datarow[i] = row;
			}
			try {
				dataRecord.addDataRow(datarow);
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		
		String[] columnName = { title,wordNo, date,signPerson, mainToDep, copyToDep, copies,remark};
		dataRecord.setColumnName(columnName);
		dataRecord.setTitle(excel_title);
		dataRecord.setSheetName(excel_title);
	
		return dataRecord;
	}
	/**
	 * xgghen
	 * @param request
	 * @param results
	 * @param excel_title
	 * @param edocType
	 * @return
	 */
	public static DataRecord exportQuery(HttpServletRequest request,List<WebEdocStat> results,String excel_title,Integer edocType){

		DataRecord dataRecord = new DataRecord();

		Locale local = LocaleContext.getLocale(request);
		String resource = "com.seeyon.v3x.edoc.resources.i18n.EdocResource";
//		导出excel文件的国际化
		String[] columnName = null ;
		
		if(edocType.intValue() == EdocEnum.edocType.sendEdoc.ordinal()) {
			columnName = new String[9] ;
			columnName[0] = ResourceBundleUtil.getString(resource, local, "edoc.element.doctype");//公文的种类
			columnName[1] = ResourceBundleUtil.getString(resource, local, "edoc.element.subject");//公文的标题
			columnName[2] = ResourceBundleUtil.getString(resource, local, "edoc.element.secretlevel.simple");//公文的密级
			columnName[3] = ResourceBundleUtil.getString(resource, local, "edoc.element.wordno.label");//公文的文号
			columnName[4] = ResourceBundleUtil.getString(resource, local, "edoc.edoctitle.createDate.label");//公文的建文日期
			columnName[5] = ResourceBundleUtil.getString(resource, local, "edoc.element.issuer");//公文的签发人
			columnName[6] = ResourceBundleUtil.getString(resource, local, "edoc.element.sendtounit");//公文主送单位
			columnName[7] = ResourceBundleUtil.getString(resource, local, "edoc.edoctitle.pigeonholePath.label");//归档路径
			columnName[8] = ResourceBundleUtil.getString(resource, local, "edoc.stat.remark.label");//备考
			if(null != results  && results.size() > 0){
				DataRow[] datarow = new DataRow[results.size()];
				for(int i = 0 ; i < results.size() ; i ++ ) {
					WebEdocStat webEdocStat = results.get(i) ;
					DataRow row = new DataRow(); 
					row.addDataCell(null!=webEdocStat.getDocType() ? String.valueOf(webEdocStat.getDocType()) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getSubject() ? String.valueOf(webEdocStat.getSubject()) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getSecretLevel() ? String.valueOf(webEdocStat.getSecretLevel()) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getDocMark() ? String.valueOf(webEdocStat.getDocMark()) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getCreateDate() ? String.valueOf(Datetimes.formatDate(webEdocStat.getCreateDate())) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getIssUser() ? String.valueOf(webEdocStat.getIssUser()) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getSendTo() ? String.valueOf(webEdocStat.getSendTo()) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getArchiveName() ? String.valueOf(webEdocStat.getArchiveName() ) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getRemark() ? String.valueOf(webEdocStat.getRemark()) : "", 1) ;
					datarow[i] = row;
				}
				try {
					dataRecord.addDataRow(datarow);
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);;
				}
			}
		}else if(edocType.intValue() == EdocEnum.edocType.recEdoc.ordinal()){
			columnName = new String[9] ;
			columnName[0] = ResourceBundleUtil.getString(resource, local, "edoc.element.doctype");//公文的种类
			columnName[1] = ResourceBundleUtil.getString(resource, local, "edoc.element.subject");//公文的标题
			columnName[2] = ResourceBundleUtil.getString(resource, local, "edoc.element.secretlevel.simple");//公文的密级
			columnName[3] = ResourceBundleUtil.getString(resource, local, "edoc.element.wordno.label");//公文的文号
			columnName[4] = ResourceBundleUtil.getString(resource, local, "edoc.element.wordinno.label");//内部文号
			columnName[5] = ResourceBundleUtil.getString(resource, local, "edoc.edoctitle.regDate.label");//登记日期
			columnName[6] = ResourceBundleUtil.getString(resource, local, "edoc.edoctitle.fromUnit.label");//公文来文单位
			columnName[7] = ResourceBundleUtil.getString(resource, local, "edoc.edoctitle.pigeonholePath.label");//归档路径
			columnName[8] = ResourceBundleUtil.getString(resource, local, "edoc.stat.remark.label");//备考
			if(null != results  && results.size() > 0){
				DataRow[] datarow = new DataRow[results.size()];
				for(int i = 0 ; i < results.size() ; i ++ ) {
					WebEdocStat webEdocStat = results.get(i) ;
					DataRow row = new DataRow(); 
					row.addDataCell(null!=webEdocStat.getDocType() ? String.valueOf(webEdocStat.getDocType()) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getSubject() ? String.valueOf(webEdocStat.getSubject()) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getSecretLevel() ? String.valueOf(webEdocStat.getSecretLevel()) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getDocMark() ? String.valueOf(webEdocStat.getDocMark()) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getSerialNo() ? String.valueOf(webEdocStat.getSerialNo()) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getRecviverDate() ? String.valueOf(Datetimes.formatDate(webEdocStat.getRecviverDate())) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getAccount() ? String.valueOf(webEdocStat.getAccount()) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getArchiveName() ? String.valueOf(webEdocStat.getArchiveName() ) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getRemark() ? String.valueOf(webEdocStat.getRemark()) : "", 1) ;
					datarow[i] = row;
				}
				try {
					dataRecord.addDataRow(datarow);
				} catch (Exception e) {
								LOGGER.error(e.getMessage(), e);;
				}
			}
		}else if(edocType.intValue() == EdocEnum.edocType.signReport.ordinal()){
			columnName = new String[9] ;
			columnName[0] = ResourceBundleUtil.getString(resource, local, "edoc.element.doctype");//公文的种类
			columnName[1] = ResourceBundleUtil.getString(resource, local, "edoc.element.subject");//公文的标题
			columnName[2] = ResourceBundleUtil.getString(resource, local, "edoc.element.secretlevel.simple");//公文的密级
			columnName[3] = ResourceBundleUtil.getString(resource, local, "edoc.element.wordinno.label");//内部文号
			columnName[4] = ResourceBundleUtil.getString(resource, local, "edoc.edoctitle.createPerson.label");//建文人
			columnName[5] = ResourceBundleUtil.getString(resource, local, "edoc.edoctitle.createDate.label");//建文日期
			columnName[6] = ResourceBundleUtil.getString(resource, local, "edoc.element.sendunit");//建文单位
			columnName[7] = ResourceBundleUtil.getString(resource, local, "edoc.edoctitle.pigeonholePath.label");//归档路径
			columnName[8] = ResourceBundleUtil.getString(resource, local, "edoc.stat.remark.label");//备考
			if(null != results  && results.size() > 0){
				DataRow[] datarow = new DataRow[results.size()];
				for(int i = 0 ; i < results.size() ; i ++ ) {
					WebEdocStat webEdocStat = results.get(i) ;
					DataRow row = new DataRow(); 
					row.addDataCell(null!=webEdocStat.getDocType() ? String.valueOf(webEdocStat.getDocType()) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getSubject() ? String.valueOf(webEdocStat.getSubject()) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getSecretLevel() ? String.valueOf(webEdocStat.getSecretLevel()) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getSerialNo() ? String.valueOf(webEdocStat.getSerialNo()) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getCreateUser() ? String.valueOf(webEdocStat.getCreateUser()) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getCreateDate() ? String.valueOf(Datetimes.formatDate(webEdocStat.getCreateDate())) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getAccount() ? String.valueOf(webEdocStat.getAccount()) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getArchiveName() ? String.valueOf(webEdocStat.getArchiveName() ) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getRemark() ? String.valueOf(webEdocStat.getRemark()) : "", 1) ;
					datarow[i] = row;
				}
				try {
					dataRecord.addDataRow(datarow);
				} catch (Exception e) {
								LOGGER.error(e.getMessage(), e);;
				}
			}
		}else if (edocType == 999) { //查询归档公文
			columnName = new String[10] ;
			columnName[0] = ResourceBundleUtil.getString(resource, local, "edoc.element.doctype");//公文的种类
			columnName[1] = ResourceBundleUtil.getString(resource, local, "edoc.element.subject");//公文的标题
			columnName[2] = ResourceBundleUtil.getString(resource, local, "edoc.element.secretlevel.simple");//公文的密级
			columnName[3] = ResourceBundleUtil.getString(resource, local, "edoc.element.wordno.label");//公文的文号
			columnName[4] = ResourceBundleUtil.getString(resource, local, "edoc.element.wordinno.label");//内部文号
			columnName[5] = ResourceBundleUtil.getString(resource, local, "edoc.edoctitle.createPerson.label") ;//建文人
			columnName[6] = ResourceBundleUtil.getString(resource, local, "edoc.edoctitle.pigeonhole.label");//归档日期
			columnName[7] = ResourceBundleUtil.getString(resource, local, "edoc.form.sort");//类型
			columnName[8] = ResourceBundleUtil.getString(resource, local, "edoc.edoctitle.pigeonholePath.label");//归档路径
			columnName[9] = ResourceBundleUtil.getString(resource, local, "edoc.stat.remark.label");//备考
			if(null != results  && results.size() > 0){
				DataRow[] datarow = new DataRow[results.size()];
				for(int i = 0 ; i < results.size() ; i ++ ) {
					WebEdocStat webEdocStat = results.get(i) ;
					DataRow row = new DataRow(); 
					row.addDataCell(null!=webEdocStat.getDocType() ? String.valueOf(webEdocStat.getDocType()) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getSubject() ? String.valueOf(webEdocStat.getSubject()) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getSecretLevel() ? String.valueOf(webEdocStat.getSecretLevel()) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getDocMark() ? String.valueOf(webEdocStat.getDocMark()) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getSerialNo() ? String.valueOf(webEdocStat.getSerialNo()) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getCreateUser() ? String.valueOf(webEdocStat.getCreateUser()) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getArchivedTime() ? String.valueOf(Datetimes.formatDate(webEdocStat.getArchivedTime())) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getEdocType() ? String.valueOf(webEdocStat.getEdocType()) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getArchiveName() ? String.valueOf(webEdocStat.getArchiveName() ) : "", 1) ;
					row.addDataCell(null!=webEdocStat.getRemark() ? String.valueOf(webEdocStat.getRemark()) : "", 1) ;
					datarow[i] = row;
				}
				try {
					dataRecord.addDataRow(datarow);
				} catch (Exception e) {
								LOGGER.error(e.getMessage(), e);;
				}
			}			
		}		
		dataRecord.setColumnName(columnName);
		dataRecord.setTitle(excel_title);
		dataRecord.setSheetName(excel_title);	
		return dataRecord;
	}
	
	
	public static DataRecord exportEdocElement(HttpServletRequest request,List<EdocElement> elementList,String element_title){

		//MetadataManager metadataManager= (MetadataManager)AppContext.getBean("metadataManager");
		
		DataRecord dataRecord = new DataRecord();

		Locale local = LocaleContext.getLocale(request);
		String resource = "com.seeyon.v3x.edoc.resources.i18n.EdocResource";
		//String com_resouce = "com.seeyon.v3x.common.resources.i18n.SeeyonCommonResources";
//		导出excel文件的国际化

		String elementName = ResourceBundleUtil.getString(resource, local, "edoc.element.elementName");//元素名称
		String elementCode = ResourceBundleUtil.getString(resource, local, "edoc.element.elementfieldName");//元素代码
		String dataType = ResourceBundleUtil.getString(resource, local, "edoc.element.elementType");//数据类型
		String elementType = ResourceBundleUtil.getString(resource, local, "edoc.element.elementIsSystem"); //元素类型
		String state = ResourceBundleUtil.getString(resource, local, "edoc.element.elementStatus"); //元素状态
		String disabled = ResourceBundleUtil.getString(resource, local, "edoc.element.disabled");//元素状态-停用
		String enabled = ResourceBundleUtil.getString(resource, local, "edoc.element.enabled");//元素状态-启用
		
		//Pagination.setNeedCount(false);
		
		if (null != elementList && elementList.size() > 0) {
			DataRow[] datarow = new DataRow[elementList.size()];
			for (int i = 0; i < elementList.size(); i++) {
				EdocElement element = elementList.get(i);
				DataRow row = new DataRow();				
				String name = element.getName();
				String dType = "";
				String dTypeLable = "";
				String eType = ResourceBundleUtil.getString(resource, local, "edoc.element.userType");;
				
				boolean isSystem = element.getIsSystem();
				if(isSystem){
					if(null!=name && !"".equals(name)){
						name = ResourceBundleUtil.getString(resource, local, name);
						eType = ResourceBundleUtil.getString(resource, local, "edoc.element.systemType");
					}
				}
				
				switch(element.getType()){
					
					case EdocElement.C_iElementType_Comment : dTypeLable = "edoc.element.comment";break;
					case EdocElement.C_iElementType_Date : dTypeLable = "edoc.element.date";break;
					case EdocElement.C_iElementType_Decimal : dTypeLable = "edoc.element.decimal";break;
					case EdocElement.C_iElementType_Integer : dTypeLable = "edoc.element.integer";break;
					case EdocElement.C_iElementType_List : dTypeLable = "edoc.element.list";break;
					case EdocElement.C_iElementType_LogoImg : dTypeLable = "edoc.element.img";break;
					case EdocElement.C_iElementType_String : dTypeLable = "edoc.element.string";break;
					case EdocElement.C_iElementType_Text : dTypeLable = "edoc.element.text";break;
				}
		
				dType = ResourceBundleUtil.getString(resource, local, dTypeLable);
				
				row.addDataCell(null!= name ? String.valueOf(name) : "", 1);
				row.addDataCell(null!= element.getFieldName() ? String.valueOf(element.getFieldName()) : "", 1);
				row.addDataCell(null!= dType ? String.valueOf(dType) : "", 1);
				row.addDataCell(null!= eType ? String.valueOf(eType) : "", 1);
				row.addDataCell(element.getStatus() ==1? enabled : disabled, 1);
				
				datarow[i] = row;
			}
			try {
				dataRecord.addDataRow(datarow);
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		
		String[] columnName = { elementName , elementCode , dataType , elementType,state};
		dataRecord.setColumnName(columnName);
		dataRecord.setTitle(element_title);
		dataRecord.setSheetName(element_title);
	
		return dataRecord;
	}
	
	/**
	 * 公文年度编号变更
	 * @return
	 */
	private static String currentEdocMarkYear="";
	private static String edocMarkCat="edoc_mark_year";
	private static String edocMarkCatItem="edoc_mark_year_cuurent";
	private static ConfigManager configMgr=(ConfigManager)AppContext.getBean("configManager");
	public static void checkDocmarkByYear()
	{
		try
		{
			Calendar cal=Calendar.getInstance();		
			int iYear=cal.get(Calendar.YEAR);
		
			ConfigItem cf=null;
			if("".equals(currentEdocMarkYear))
			{//启动后没有进行初始化时候,config中读取
				cf=configMgr.getConfigItem(edocMarkCat,edocMarkCatItem,1L);
				if(cf==null)
				{//数据库中未记录				
					cf=new ConfigItem();
					cf.setIdIfNew();
					cf.setConfigCategory(edocMarkCat);
					cf.setConfigItem(edocMarkCatItem);
					cf.setConfigValue(Integer.toString(iYear));				
					configMgr.addConfigItem(cf);				
				}
				currentEdocMarkYear=cf.getConfigValue();
			}
			if(!currentEdocMarkYear.equals(Integer.toString(iYear)))
			{
				EdocMarkManager edocMarkManager= (EdocMarkManager)AppContext.getBean("edocMarksManager");
				edocMarkManager.turnoverCurrentNoAnnual();
				cf=configMgr.getConfigItem(edocMarkCat,edocMarkCatItem,1L);
				cf.setConfigValue(Integer.toString(iYear));
				configMgr.updateConfigItem(cf);
				currentEdocMarkYear=Integer.toString(iYear);
			}
		}catch(Exception e)
		{			
			LOGGER.error("", e);
		}
	}
	
	/**
	 * 根据公文类型返回该公文单所包含的处理意见列表
	 * @param elementList 公文单的元素列表
	 * @param edocType 公文类型
	 * @return
	 */
	public static List<FormBoundPerm> getProcessOpinionFromEdocForm(List<String> elementList, int edocType,long accountId)throws Exception{
		
		/*
		User user = AppContext.getCurrentUser();
		*/
		String category = "";
		//BUG20120806012093_v1_处理领导拟办无法显示在卡片拟办位置
		if(edocType == EdocEnum.edocType.sendEdoc.ordinal()){
			category = MetadataNameEnum.edoc_send_permission_policy.name();
		}else if(edocType == EdocEnum.edocType.recEdoc.ordinal()){
			category = MetadataNameEnum.edoc_rec_permission_policy.name();
		}else if(edocType == EdocEnum.edocType.signReport.ordinal()){
			category =  MetadataNameEnum.edoc_qianbao_permission_policy.name();
		}
			
		//List<FormBoundPerm> returnList = new ArrayList<FormBoundPerm>();
		List<FormBoundPerm> boundPermList = new ArrayList<FormBoundPerm>();
		
		//BUG20120806012093_v1_处理领导拟办无法显示在卡片拟办位置
		PermissionManager permissionManager= (PermissionManager)AppContext.getBean("permissionManager");
		//MetadataManager metadataManager= (MetadataManager)AppContext.getBean("metadataManager");
		
		EdocElementManager edocElementManager = (EdocElementManager)AppContext.getBean("edocElementManager");
		//得到所有启用的公文意见元素列表
		//List<EdocElement> tempList = edocElementManager.getByStatusAndType(EdocElement.C_iStatus_Active, EdocElement.C_iElementType_Comment);
		//List<String> processList = new ArrayList<String>();
		//for(EdocElement element : tempList){
		//	processList.add(element.getFieldName());
		//}
		
		//根据类别查处所有该类别下的节点权限，再将他们的名字保存到一个LIST集合中 
		//*自定义的权限保存的是国际化资源的KEY值
		//String label = "";
		String value = "";
		String processName = "";
		String processItemName = "";
		FormBoundPerm otherOpinionPerm = null;
		for(String fieldName : elementList){
			EdocElement ele = edocElementManager.getByFieldName(fieldName);
			
			if(null!=ele && ele.getType() != EdocElement.C_iElementType_Comment)continue;
			
			if(null!=ele && null!=ele.getName()) {
				
				if(ele.getIsSystem() == true){
					
					value = ResourceUtil.getString(ele.getName());
					if(fieldName.equals(EdocOpinion.REPORT)){
						processName = ResourceUtil.getString("node.policy.report");
					}else if(fieldName.equals(EdocOpinion.FEED_BACK)){
						processName = "";
					}
					else{
						processName = value;
					}
					
					processItemName = ele.getFieldName();
					
					//BUG20120806012093_v1_处理领导拟办无法显示在卡片拟办位置--start
					//按当前公文元素的名字，以及公文类别（发文、收文、签报）查出对应的节点权限。避免这种情况发生：文单里的公文意见元素和节点权限，不是同一个公文类别的
					//比如，上传一个签报单，里面有收文用的“拟办”意见元素，上传后，会自动绑定收文的节点权限“拟办”，这是不对的，会导致意见显示不到这个意见元素里。
					Permission f=permissionManager.getPermission(category,ele.getFieldName(),accountId);
					if(f==null || !ele.getFieldName().equals(f.getName())){//getPermission 方法可能返回只会节点
						processName = "";
						processItemName = "";
					}
					//BUG20120806012093_v1_处理领导拟办无法显示在卡片拟办位置--end
				}else{
					value = ele.getName();
					processName = "";
					processItemName = "";
				}
				FormBoundPerm formBoundPerm = new FormBoundPerm();
				formBoundPerm.setPermItem(ele.getFieldName());
				formBoundPerm.setPermName(value);
				
				if("otherOpinion".equalsIgnoreCase(fieldName)) {
					otherOpinionPerm = formBoundPerm;
					continue;
				}
				formBoundPerm.setPermItemName(processItemName);
				formBoundPerm.setProcessName(processName);
				formBoundPerm.setProcessItemName(processItemName);
				boundPermList.add(formBoundPerm);
			}
			
		}
		if(null!=otherOpinionPerm){
			boundPermList.add(otherOpinionPerm);//把处理意见加到最后
		}
		
		
		
		return boundPermList;
	}
	public static Long getFlowPermAccountId(Long defaultAccountId, EdocSummary summary, TemplateManager TemplateManager){
		Long flowPermAccountId = defaultAccountId;
    	if(summary != null){
    		if(summary.getTempleteId() != null){
    			CtpTemplate templete;
				try {
					templete = templateManager.getCtpTemplate(summary.getTempleteId());
					if(templete != null){
	    				flowPermAccountId = templete.getOrgAccountId();
	    			}
				} catch (BusinessException e) {
					LOGGER.error("", e);
				}
    		}
    		else{
    			if(summary.getOrgAccountId() != null){
    				flowPermAccountId = summary.getOrgAccountId();
    			}
    		}
    	}
    	return flowPermAccountId;
	}
	public static Long getFlowPermAccountId(EdocSummary summary, Long defaultAccountId, TemplateManager TemplateManager){
		return getFlowPermAccountId(defaultAccountId, summary, TemplateManager);
	}
	public static Long getFlowPermAccountId(Long defaultAccountId, Long templeteId, Long orgAccountId, TemplateManager TemplateManager){
		Long flowPermAccountId = defaultAccountId;
		if(templeteId != null){
			CtpTemplate templete;
			try {
				templete = templateManager.getCtpTemplate(templeteId);
				if(templete != null){
    				flowPermAccountId = templete.getOrgAccountId();
    			}
			} catch (BusinessException e) {
				LOGGER.error("", e);
			}
		}
		else{
			if(orgAccountId != null){
				flowPermAccountId = orgAccountId;
			}
		}
    	return flowPermAccountId;
	}
	public static Long getFlowPermAccountId(Long defaultAccountId, EdocSummary summary, CtpTemplate template){
		Long flowPermAccountId = defaultAccountId;
    	if(summary != null){
    		if(template != null){
    			flowPermAccountId = template.getOrgAccountId();
    		}
    		else{
    			if(summary.getOrgAccountId() != null){
    				flowPermAccountId = summary.getOrgAccountId();
    			}
    		}
    	}
    	return flowPermAccountId;
	}
	
	public static String getCategoryName(int edocType){
		String category = "";
		if(edocType == EdocEnum.edocType.sendEdoc.ordinal()){
			category = MetadataNameEnum.edoc_send_permission_policy.name();
		}else if(edocType == EdocEnum.edocType.recEdoc.ordinal()){
			category = MetadataNameEnum.edoc_rec_permission_policy.name();
		}else if(edocType == EdocEnum.edocType.signReport.ordinal()){
			category =  MetadataNameEnum.edoc_qianbao_permission_policy.name();
		}
		return category;
	}
	/**
	 * 查找节点权限的备选操作，用于初始化节点权限选择池
	 * @param elementList
	 * @param edocType
	 * @return
	 * @throws Exception
	 */
	public static String getProcessOpinionFromEdocFormOperation(List<String> elementList, int edocType)throws Exception{
		
		User user = AppContext.getCurrentUser();
		
		String category = getCategoryName(edocType);
		
			
		StringBuilder returnString = new StringBuilder();
		List<FormBoundPerm> boundPermList = new ArrayList<FormBoundPerm>();
		
		PermissionManager permissionManager= (PermissionManager)AppContext.getBean("permissionManager");
		EnumManager metadataManager= (EnumManager)AppContext.getBean("enumManagerNew");
		
		//根据类别查处所有该类别下的节点权限，再将他们的名字保存到一个LIST集合中 
		//*自定义的权限保存的是国际化资源的KEY值
		String label = "";
		String value = "";
		List<Permission> flowList = permissionManager.getPermissionsByStatus(category, Permission.Node_isActive, user.getLoginAccount());
		for(Permission perm:flowList){
			if(elementList.contains(perm.getName())){
				if(perm.getType().intValue() == Permission.Node_Type_System.intValue()){
					if(edocType==EdocEnum.edocType.sendEdoc.ordinal()){
						label = metadataManager.getEnumItemLabel(EnumNameEnum.edoc_send_permission_policy, perm.getName());
					}else if(edocType==EdocEnum.edocType.recEdoc.ordinal()){
						label = metadataManager.getEnumItemLabel(EnumNameEnum.edoc_rec_permission_policy, perm.getName());
					}else{
						label = metadataManager.getEnumItemLabel(EnumNameEnum.edoc_qianbao_permission_policy, perm.getName());
					}
					
					if(null!=label && !"".equals(label)){
						value = ResourceUtil.getString( label);
					}
				}else{
						value = perm.getName();
				}
				FormBoundPerm formBoundPerm = new FormBoundPerm();
				formBoundPerm.setPermItem(perm.getName());
				formBoundPerm.setPermName(value);
				boundPermList.add(formBoundPerm);
			}
		}
		
		//循环比较处理意见的名称，如果元素列表中包含处理意见，放入返回列表中
		for(FormBoundPerm perm:boundPermList){
			if(elementList.contains(perm.getPermItem())){
			    returnString.append("(")
			                .append(perm.getPermItem())
			                .append(")");
			}
		}
		
		return returnString.toString();
	}
	
	
	public static List<FormBoundPerm> getProcessOpinionByEdocFormId(List<String> elementList, long edocFormId, int edocType,long accountId, boolean isUpload) throws Exception{
		String category = "";
		if(edocType == EdocEnum.edocType.sendEdoc.ordinal()){
			category = MetadataNameEnum.edoc_send_permission_policy.name();
		}else if(edocType == EdocEnum.edocType.recEdoc.ordinal()){
			category = MetadataNameEnum.edoc_rec_permission_policy.name();
		}else if(edocType == EdocEnum.edocType.signReport.ordinal()){
			category =  MetadataNameEnum.edoc_qianbao_permission_policy.name();
		}
		PermissionManager permissionManager= (PermissionManager)AppContext.getBean("permissionManager");
		EdocFormManager edocFormManager= (EdocFormManager)AppContext.getBean("edocFormManager");
		EdocElementManager edocElementManager = (EdocElementManager)AppContext.getBean("edocElementManager");		
		
		List<FormBoundPerm> boundPermList = new ArrayList<FormBoundPerm>();

		String value = "";
		String processName = "";
		String processItemName = "";
		FormBoundPerm otherOpinionPerm = null;
		
		for(String fieldName : elementList){
			
			EdocElement ele = edocElementManager.getByFieldName(fieldName);
			
			if(null!=ele && ele.getType() != EdocElement.C_iElementType_Comment)continue;
			
			if(null!=ele && null!=ele.getName()) {
				if(ele.getIsSystem() == true){
					value = ResourceUtil.getString(ele.getName());
					processName = ele.getFieldName();
					
					if(processName.equals(EdocOpinion.REPORT)){
						processItemName = ResourceUtil.getString("node.policy.report");
					}else{
						processItemName = value;
					}
					
					//if(isUpload) {//编辑时重新上传
						Permission f = permissionManager.getPermission(category,ele.getFieldName(),accountId);
						if(f==null || !ele.getFieldName().equals(f.getName())){//通过getPermission获取到的节点可能是只会节点
							processName = "";
							processItemName = "";
						}
					//}else{
					 //   processName = "";
                     //   processItemName = "";
					//}
				}else{
					value = ele.getName();
					processName = "";
					processItemName = "";
					/*if(isUpload) {//编辑时重新上传
						Permission f = permissionManager.getPermission(category,ele.getFieldName(),accountId);
						if(f!=null && ele.getFieldName().equals(f.getName())){//f 目前为止不可能为null
							processName = ele.getFieldName();
							processItemName = value;
						}
					}*/
				}
				FormBoundPerm formBoundPerm = new FormBoundPerm();
				formBoundPerm.setPermItem(ele.getFieldName());
				formBoundPerm.setPermName(value);
				if("otherOpinion".equalsIgnoreCase(fieldName)){
					otherOpinionPerm = formBoundPerm;
					continue;
				}
				formBoundPerm.setPermItemName(processItemName);
				formBoundPerm.setProcessName(processName);
				formBoundPerm.setProcessItemName(processItemName);
				if(isUpload && ele.getIsSystem()) {//编辑时重新上传，默认为同名权限
					formBoundPerm.setPermItemList(processName);
				}
				boundPermList.add(formBoundPerm);	
			}
		}
		if(null!=otherOpinionPerm){
			List<EdocFormFlowPermBound> list = edocFormManager.findBoundByFormId(edocFormId, otherOpinionPerm.getPermItem());
			for (EdocFormFlowPermBound bound : list) {
				otherOpinionPerm.setSortType(bound.getSortType());
			}
			boundPermList.add(otherOpinionPerm);// 把处理意见加到最后
		}
		
		//if(!isUpload) {
			for(FormBoundPerm perm : boundPermList){
				if (null != perm.getPermItem() && "otherOpinion".equalsIgnoreCase(perm.getPermItem()))
					continue;
				//此处判断当前文单是否属于本单位的文单
				//boolean flag=edocFormManager.getFormAccountEdoc(accountId, edocFormId);
				//if(flag){
				List<EdocFormFlowPermBound> list = edocFormManager.findBoundByFormId(edocFormId, perm.getPermItem(),accountId);
				StringBuilder str_temp = new StringBuilder();
				StringBuilder str_temp_b = new StringBuilder();
				for(EdocFormFlowPermBound bound : list){
				    
				    String tLable = bound.getFlowPermNameLabel();
				    String sFlowname = bound.getFlowPermName();
				    
				    EdocElement element = null;
				    if(Strings.isNotBlank(sFlowname)){
				        element = edocElementManager.getByFieldName(sFlowname);
				    }
					 
					if(null!=element && element.getIsSystem()){//查找元素，如果非空证明是系统预置的fuhe,shenpi....节点权限和系统预置的处理意见
					}else if(element == null){//如果element为空，那么
						element = edocElementManager.getByFieldName(bound.getProcessName());//在用processName查
						if(null!=element){//节点权限为自定义但处理意见元素为系统的, 节点权限为自定义而且处理意见元素也是扩展的
						}else{
	                        sFlowname = "";
	                    }
					}else{
					    sFlowname = "";
					}
					
					if(Strings.isNotBlank(tLable) && Strings.isNotBlank(sFlowname)){
					    if(str_temp.length() > 0){
	                        str_temp.append(",");
	                        str_temp_b.append(",");
	                    }
					    str_temp.append(tLable);
                        str_temp_b.append(sFlowname);
					}
					
					perm.setSortType(bound.getSortType());
				}

                if(Strings.isNotEmpty(list)){
                    perm.setPermItemName(str_temp.toString());
                    perm.setPermItemList(str_temp_b.toString());
                }
				
				//}
			}			
		//}
		return boundPermList;

	}
	
	public static String getLogoURL(){
		
			User user = AppContext.getCurrentUser();
			Long accountId = user.getLoginAccount();
			return getLogoURL(accountId);
			//在修改方法中,首先进行一次替换,如果之前设置的为默认logo, 那么将logo置空
			//String url = MainDataLoader.getInstance().getLogoImagePath(accountId);
			//return "<img src='/seeyon"+url+"' />";
		
	}
	public static String getLogoURL(long accountId){
		//在修改方法中,首先进行一次替换,如果之前设置的为默认logo, 那么将logo置空
		String url="";
		try {
		    url = SystemEnvironment.getContextPath() + getLoginURLSrc(accountId);
		} catch (BusinessException e) {
			LOGGER.error("调用T3获取单位Logo地址错误", e);
		}
		return "<img src='"+url+"' />";
	}
	
	/**
	 * 获取单位Logo, 只返回地址
	 * 
	 * @return
	 * @throws BusinessException 
	 *
	 * @Author      : xuqw
	 * @Date        : 2016年7月27日下午5:35:52
	 *
	 */
	public static String getLoginURLSrc(long accountId) throws BusinessException{
	    return portalApi.getAccountLogo(accountId);
	}
	
	/**
	 * 检查是否有公文单及套红模板文件存，如果不存在复制一份到指定分区
	 * @throws Exception
	 */
	public static void copyEdocFile()throws Exception{
		
		String[] t_FileIds = new String[1];
		
		t_FileIds[0] = "-6001972826857714844"; //套红模板文件压缩包
		
		
		String[] f_FileIds = new String[3];
		// -- 公文单（签报，收文，发文）
		f_FileIds[0] = "-1766191165740134579"; 
		f_FileIds[1] = "-2921628185995099164";
		f_FileIds[2] = "6071519916662539448";		
		
		copyFile(t_FileIds, Constants.EDOC_FILE_TYPE_TEMPLATE);
		copyFile(f_FileIds, Constants.EDOC_FILE_TYPE_EDOCFORM);
		
	}
	
	public static void copyFile(String[] fileIds, int type)throws Exception{
		
		String fileFolder = baseFileFolder;
		if(type == Constants.EDOC_FILE_TYPE_EDOCFORM){
			fileFolder += formFolder;
		}
		else if(type == Constants.EDOC_FILE_TYPE_TEMPLATE){
			fileFolder += templateFolder;
		}
		
		
		
		
		FileManager fileManager = (FileManager)AppContext.getBean("fileManager");
		
		for(String id : fileIds){
			V3XFile v3xFile=fileManager.getV3XFile(Long.valueOf(id));
			if(null != v3xFile){
				File file = fileManager.getFile(v3xFile.getId()); 
					if(null == file){
						File tempFile = new File(fileFolder+"\\/" + id);
						if(null!=tempFile){
							String folder = fileManager.getFolder(v3xFile.getCreateDate(), true);
                            File in = new File(fileFolder + "\\/" + id);
                            File out = new File(folder + "\\/" + id);
                            FileUtils.copyFile(in, out);
						}
					}
			}
		}
		
	}
	
	/**
	 * 为新建单位复制内部公文文号
	 * @param accountId
	 */
	public static void generateEdocInnerMarkByAccountId(long accountId){
		try{
		LOGGER.info("开始为新建单位复制内部公文文号...");
		EdocInnerMarkDefinitionManager edocInnerMarkDefinitionManager = (EdocInnerMarkDefinitionManager)AppContext.getBean("edocInnerMarkDefinitionManager");
		List<EdocInnerMarkDefinition> list = edocInnerMarkDefinitionManager.getEdocInnerMarkDefsList(V3xOrgEntity.VIRTUAL_ACCOUNT_ID);
		if(list == null) return;
		for(EdocInnerMarkDefinition def : list){
			EdocInnerMarkDefinition newDef = new EdocInnerMarkDefinition();
			newDef.setIdIfNew();
			newDef.setCurrentNo(def.getCurrentNo());
			newDef.setDomainId(accountId);
			newDef.setExpression(def.getExpression());
			newDef.setLength(def.getLength());
			newDef.setMaxNo(def.getMaxNo());
			newDef.setMinNo(def.getMinNo());
			newDef.setType(def.getType());
			newDef.setWordNo(def.getWordNo());
			newDef.setYearEnabled(def.getYearEnabled());
			edocInnerMarkDefinitionManager.create(newDef);
		}
		LOGGER.info("内部公文文号复制完毕");
		}catch(Exception e){
			LOGGER.error("!为新建单位复制文号失败",e);
		}
	}
	
	/**
	 * 为新建单位复制公文套红模板
	 * @param accountId
	 * @deprecated
	 */
	public static void generateEdocTemplateForm(long accountId){
		
		User user = AppContext.getCurrentUser();
		try{
			LOGGER.info("开始为新建单位复制公文套红模板...");
			EdocDocTemplateManager edocDocTemplateManager = (EdocDocTemplateManager)AppContext.getBean("edocDocTemplateManager");
			List<EdocDocTemplate> list = edocDocTemplateManager.getAllTemplateByAccountId(V3xOrgEntity.VIRTUAL_ACCOUNT_ID);
			for(EdocDocTemplate template : list){
				EdocDocTemplate newTemplate = new EdocDocTemplate();
				newTemplate.setIdIfNew();
				newTemplate.setCreateTime(new Timestamp(System.currentTimeMillis()));
				newTemplate.setAclEntity(null);
				newTemplate.setCreateUserId(user.getId());
				newTemplate.setDescription(template.getDescription());
				newTemplate.setDomainId(accountId);
				newTemplate.setGrantNames(template.getGrantNames());
				newTemplate.setLastUpdate(new Timestamp(System.currentTimeMillis()));
				newTemplate.setLastUserId(user.getId());
				newTemplate.setStatus(template.getStatus());
				newTemplate.setTemplateAcls(null);
				newTemplate.setTemplateFileId(Long.valueOf(1));
				newTemplate.setTextType(template.getTextType());
				newTemplate.setType(template.getType());
				newTemplate.setFileUrl(null);
				
			}
			LOGGER.info("内部公文文号复制完毕");
			}catch(Exception e){
				LOGGER.error("!为新建单位复制文号失败",e);
			}		
		
	}
	
	/**
	 * 另一套复制方法，直接调用manager中的方法
	 * @param accountId
	 */
	public static void generateEdocTemplateFormOringinal(long accountId){
		
		try{
			LOGGER.info("开始为新建单位复制公文套红模板...");
			EdocDocTemplateManager edocDocTemplateManager = (EdocDocTemplateManager)AppContext.getBean("edocDocTemplateManager");
			edocDocTemplateManager.addEdocTemplate(accountId);
			
			LOGGER.info("内部公文文号复制完毕");
			}catch(Exception e){
				LOGGER.error("!为新建单位复制文号失败",e);
			}		
	}
	
	/**
	 * 判断某个单位的公文相关的预置数据是否初始化，如果没有初始化则初始化
	 * @param accountId
	 * @return
	 */
	public static void compensate(long accountId){
	    
	    boolean toFix = false;
		int count = edocElementManager.countEdocElementsFromDB(accountId);
		if(count != EdocElementEnum.size()){
		    toFix = true;
		}
		
		EdocKeyWordManager edocKeyWordManager = (EdocKeyWordManager)AppContext.getBean("edocKeyWordManager");
		List<EdocKeyWord> list = edocKeyWordManager.getEdocKeyWordByAccountId(accountId);
		
		if(Strings.isEmpty(list)){
		    toFix = true;
		}
		
		/** 不修复公文单了， 以前A83.5系统预置公文单和和最新的都不一样，修复不了，报客户BUG再说 **/
		List<EdocForm> edocforms = edocFormManager.getAllEdocForms(accountId);
		if(Strings.isEmpty(edocforms)/* || edocforms.size() < 4*/){//预置了4条公文单
		    toFix = true;
		}
		//说明没有进行过初始化公文数据的操作，这个地方开始进行初始化，分版的时候有用
		if(toFix){
			generateZipperFleet(accountId);
		}
	}
	/**
	 * 每建一个单位，为新单位生成一套公文单，一套套红模板，一套主题词库，一套内部文号
	 * @param accountId
	 */
	public static void generateZipperFleet(long accountId){
	    
	    
	    /****************************************
	     *                                      
         *   这里面的方法一定要可重复执行， 异常情况修复数据用                    
         *                                       
         ****************************************/
	    
		LOGGER.info("----开始为单位："+accountId+"   复制公文的所有数据--------------------：");
		//先复制公文种类，为复制公文单设置公文种类使用
		generateAccountDefaultCategory(accountId);
		//复制公文单
		generateEdocFormByAccountId(accountId);
		
		//公文元素
		generateEdocElementByAccountId(accountId);
		//主题词
		generateEdocKeyWordByAccountId(accountId);
		//公文开关
		generateEdocSwitchKeyByAccountId(accountId);

	}
	
	
	/**
	 * 复制公文开关数据
	 * @param accountId
	 */
	private static void generateEdocSwitchKeyByAccountId(long accountId) {
		LOGGER.info("开始为新建单位复制公文开关...");
		try{
			ConfigManager configManager = (ConfigManager)AppContext.getBean("configManager");
			configManager.saveInitCmpConfigData(IConfigPublicKey.EDOC_SWITCH_KEY,accountId);
		}catch(Exception e){
			LOGGER.error("新建单位的时候复制公文开关异常",e);
		}
		LOGGER.info("复制系统公文开关结束。");
		
	}

	/**
	 * 新建单位复制公文元素
	 * @param accountId
	 */
	private static void generateEdocElementByAccountId(long accountId) {
		LOGGER.info("开始为新建单位复制系统公文元素transCopyGroupElement2NewAccout...");
		try{
		    
		    int count = edocElementManager.countEdocElementsFromDB(accountId);
		    if(count == 0){
		        edocElementManager.transCopyGroupElement2NewAccout(accountId);
		    }else{
		      
		        //集团的一起修复下
                edocElementManager.fixElements(0l);
                
		        //做数据修复， 修复集团
		        edocElementManager.fixElements(accountId);
		    }
			
		}catch(Exception e){
			LOGGER.error("新建单位的时候复制系统公文元素异常",e);
		}
		LOGGER.info("复制系统公文元素结束。");
	}
	/**
	 * 新建单位复制系统公文主题词库
	 * @param accountId
	 */
	private static void generateEdocKeyWordByAccountId(long accountId) {
	    LOGGER.info("开始为新建单位复制系统公文主题词库...");
	    try{
	        EdocKeyWordManager edocElementManager = (EdocKeyWordManager)AppContext.getBean("edocKeyWordManager");
	        edocElementManager.initCmpKeyWords(accountId);
	    }catch(Exception e){
	        LOGGER.error("新建单位的时候复制系统公文主题词库异常",e);
	    }
	    LOGGER.info("复制系统公文主题词库结束。");
	}
	
	private static void generateAccountDefaultCategory(long accountId) {
		LOGGER.info("开始为新建单位复制公文种类...");
		EdocCategoryManager edocCategoryManager = (EdocCategoryManager)AppContext.getBean("edocCategoryManager");
		edocCategoryManager.generateAccountDefaultCategory(accountId);
		LOGGER.info("复制系统公文种类结束。");
	}

	/**
	 * 为新建单位复制公文单
	 * @param accountId
	 */
	public static void generateEdocFormByAccountId(long accountId){
		
		LOGGER.info("开始为新建单位复制公文单...");
		
		try{
			User user = AppContext.getCurrentUser();
			
			V3xOrgAccount account = orgManager.getRootAccount();
			List<EdocForm> forms = edocFormManager.getEdocFormByAcl(String.valueOf(account.getId()));
			
			
			
			
			//集团公文单
			List<EdocFormExtendInfo> edocFormExtendInfos = new ArrayList<EdocFormExtendInfo>();
			for(EdocForm form : forms){
				if(form.getIsSystem()) {
					continue;
				}
				
				//检查是否已经有公文单授权信息了
				int infoCont = edocFormManager.countExtendInfo(form.getId(), accountId);
				if(infoCont == 0){
				    EdocFormExtendInfo info = new EdocFormExtendInfo();
				    info.setIdIfNew();
				    info.setAccountId(accountId);
				    info.setStatus(Constants.EDOC_USELESS);
				    info.setIsDefault(false);
				    info.setEdocForm(form);
				    info.setOptionFormatSet(FormOpinionConfig.getDefualtConfig());
				    edocFormExtendInfos.add(info);
				}
			}
			DBAgent.saveAll(edocFormExtendInfos);
			
			
			List<EdocCategory> edocCategorylist=edocCategoryManager.getCategoryByAccount(accountId);//取出当前单位的公文种类
			long edocCategoryId=0L;  //类型为系统预置的种类 id
			for(EdocCategory edocCategory:edocCategorylist){
				if(edocCategory.getStoreType()==EdocCategoryStoreTypeEnum.SYSTEM.ordinal()){
					edocCategoryId=edocCategory.getId();
					break;
				}
			}

			List<EdocForm> sysAccountList = edocFormManager.getAllEdocForms(1L); // 查出预置的公文单数据
			for(EdocForm form : sysAccountList){
				//先判断一下文单内容是否为空，如果为空就需要重新预置一下内容
				if(form.getIsSystem() && Strings.isBlank(form.getContent())){
					edocFormManager.updateFormContentToDBOnly();
					sysAccountList= edocFormManager.getAllEdocForms(1L);
					break;
				}
			}
			
			
			List<EdocForm> accountList = null;
			//兼容修复数据
            List<EdocForm> thisForms = edocFormManager.getAllEdocForms(accountId);
            if(Strings.isNotEmpty(thisForms)){
                
                //有系统公文单了就不再进行修复了
                LOGGER.info("存在系统公文单...");
                return;
                
                /*accountList = new ArrayList<EdocForm>();
                for(EdocForm sysForm : sysAccountList){
                    
                    boolean initForm = true;
                    for(EdocForm f : thisForms){
                        if(sysForm.getFileId().equals(f.getFileId())){
                            initForm = false;
                            break;
                        }
                    }
                    if(initForm){
                        accountList.add(sysForm);
                    }
                }*/
                
            }else{
                accountList = sysAccountList;
            }
            
			LOGGER.info("预置公文单长度"+accountList.size());
			List<EdocForm> edocforms = new ArrayList<EdocForm>();
			for(EdocForm form : accountList){
				EdocForm newForm = new EdocForm();
				Set<EdocFormAcl> edocFormAcls = new HashSet<EdocFormAcl>();
				newForm.setIdIfNew();
	
				newForm.setContent(form.getContent());
				newForm.setCreateTime(new Timestamp(System.currentTimeMillis()));
				newForm.setCreateUserId(user.getId());
				newForm.setDescription(form.getDescription());
				newForm.setName(form.getName());
				newForm.setDomainId(accountId);
				newForm.setShowLog(false);
				newForm.setStatus(form.getStatus());
				newForm.setType(form.getType());
				newForm.setLastUpdate(new Timestamp(System.currentTimeMillis()));
				newForm.setEdocFormAcls(edocFormAcls);
				newForm.setIsDefault(true);
				newForm.setIsSystem(true);
				if(form.getType()==0 && edocCategoryId!=0){ //如果是发文单，预置发文单的公文种类
					newForm.setSubType(edocCategoryId);
				}
				
				//复制扩展信息
				Set<EdocFormExtendInfo> infos = new HashSet<EdocFormExtendInfo>();
				EdocFormExtendInfo info = new EdocFormExtendInfo();
				info.setIdIfNew();
				info.setAccountId(newForm.getDomainId());
				info.setEdocForm(newForm);
				info.setStatus(com.seeyon.v3x.edoc.util.Constants.EDOC_USEED);
				//转收文预置单 不是默认的
				if(form.getType()==1 && "收文单（转收文）".equals(form.getName())){
					info.setIsDefault(false);
				}else{
					info.setIsDefault(true);
				}
				
				info.setOptionFormatSet(FormOpinionConfig.getDefualtConfig());
	
				infos.add(info);
				newForm.setEdocFormExtendInfo(infos);
				
				
				//复制授权信息
				Set<EdocFormAcl> acls = new HashSet<EdocFormAcl>();
				EdocFormAcl acl = new EdocFormAcl();
				acl.setIdIfNew();
				acl.setDomainId(newForm.getDomainId());
				acl.setFormId(newForm.getId());
				acl.setEntityType(ORGENT_TYPE_ACCOUNT);
				
				acls.add(acl);
				newForm.setEdocFormAcls(acls);
				
				// -- 复制新公文元素 -- start --
				Set<EdocFormElement> oldFormElements = form.getEdocFormElements();
		    	Set<EdocFormElement> newFormElements = new HashSet<EdocFormElement>();
				for(EdocFormElement oldElement : oldFormElements){
		    		EdocFormElement newElement = new EdocFormElement();
		    		newElement.setIdIfNew();
		    		newElement.setElementId(oldElement.getElementId());
		    		newElement.setFormId(newForm.getId());
		    		newFormElements.add(newElement);
		    	}
				newForm.setEdocFormElements(newFormElements);
				// -- end --
				
				
				// -- 复制新公文节点权限绑定  -- end --
				Set<EdocFormFlowPermBound> oldFormBounds = form.getEdocFormFlowPermBound();
				Set<EdocFormFlowPermBound> newFormBounds = new HashSet<EdocFormFlowPermBound>();
				
				for(EdocFormFlowPermBound oldBound : oldFormBounds){
					EdocFormFlowPermBound newBound = new EdocFormFlowPermBound();
					newBound.setIdIfNew();
					newBound.setEdocFormId(newForm.getId());
					newBound.setFlowPermName(oldBound.getFlowPermName());
					newBound.setFlowPermNameLabel(oldBound.getFlowPermNameLabel());
					newBound.setProcessName(oldBound.getProcessName());
					newBound.setDomainId(accountId);
					newFormBounds.add(newBound);
				}
				newForm.setEdocFormFlowPermBound(newFormBounds);
			
				newForm.setFileId(form.getFileId());

				
				edocforms.add(newForm);
			}
			if(Strings.isNotEmpty(edocforms)){
			    edocFormManager.saveEdocForms(edocforms);
			}
		
		}catch(Exception e){
			LOGGER.error("为新建单位复制公文单失败");
		}
		LOGGER.info("公文单复制完毕");
	}
	
	public static void exportToExcel(HttpServletRequest request,
			HttpServletResponse response,FileToExcelManager fileToExcelManager
			,String title,DataRecord dataRecord) throws Exception{
		try {
			fileToExcelManager.save(response, title, dataRecord);
			//fileToExcelManager.save(request, response,title, "location.href", dataRecord);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);;
		}		
	}
	
	public static boolean hasNodeExist(CtpAffair affair,Map<String,String> conditions){
		if(conditions == null || conditions.size()==0)
			return true;
		try{
			if(affair != null && affair.getSubObjectId() != null){
	            WorkItem workitem = wapi.getWorkItemById("",affair.getSubObjectId());
	            long caseId = workitem.getCaseId();
	            BPMProcess process = wapi.getBPMProcess(affair.getProcessId(), caseId);
	            for(String nodeId:conditions.keySet()){
	            	if(process.getActivityById(nodeId)==null){
	            		return false;
	            	}
	            }
			}
		}catch(Exception e){
			LOGGER.error("校验分支节点发生错误", e);
		}
		return true;
	}
	


	/**
	 * wangwei
	 * 得到节点权限所属单位ID<br>
	 * 原则：1、系统模板，取模板所在单位ID<br>
	 * 		2、自由协同，取协同所在单位ID
	 * @return
	 */
    public static Long getFlowPermAccountId(EdocSummary summary,Long senderAccountId) {
    	Long flowPermAccountId = senderAccountId; 
		if(summary.getTempleteId() != null){
			CtpTemplate templete;
			try {
				 TemplateManager    templateManager =  (TemplateManager)AppContext.getBean("templateManager");
				templete = templateManager.getCtpTemplate(summary.getTempleteId());
				if(templete != null){
					flowPermAccountId = templete.getOrgAccountId();
				}
			} catch (BusinessException e) {
				LOGGER.error("", e);
			}
			
		}
		else{
			if(summary.getOrgAccountId() != null){
				flowPermAccountId = summary.getOrgAccountId();
			}
		}
    	return flowPermAccountId;
    }
    
    public static void createQuartzJobOfSummary(EdocSummary summary, WorkTimeManager workTimeManager){
      	createQuartzJob(ApplicationCategoryEnum.edoc, summary.getId(), summary.getCreateTime(), 
    			summary.getDeadlineDatetime(), summary.getAdvanceRemind(), summary.getOrgAccountId(), workTimeManager);
    }
    
    protected static void createQuartzJob(ApplicationCategoryEnum app,long summaryId, Date createTime, Date deadLine, Long advanceRemind, Long orgAcconutID, WorkTimeManager workTimeManager){
    	try{
			//超期提醒
			if(deadLine != null){

				//Date deadLineRunTime = workTimeManager.getCompleteDate4Nature(createTime, deadLine, orgAcconutID);
				{
					Map<String, String> datamap = new HashMap<String, String>(3);

			    	datamap.put("appType", String.valueOf(app.key()));
			    	datamap.put("isAdvanceRemind", "1");
			    	datamap.put("objectId", String.valueOf(summaryId));

			   		QuartzHolder.newQuartzJob("ColProcessDeadLine" + summaryId, deadLine, "processCycRemindQuartzJob", datamap);
				}

		   		//提前提醒
		        if (advanceRemind != null && advanceRemind > 0) {
		        	Date advanceRemindTime = workTimeManager.getRemindDate(deadLine, advanceRemind);//.getCompleteDate4Nature(createTime, deadLine - advanceRemind, orgAcconutID);

		            Map<String, String> datamap = new HashMap<String, String>(3);
		            datamap.put("appType", String.valueOf(app.key()));
		            datamap.put("isAdvanceRemind", "0");
		            datamap.put("objectId", String.valueOf(summaryId));

		            QuartzHolder.newQuartzJob("ColProcessRemind" + summaryId, advanceRemindTime, "processCycRemindQuartzJob", datamap);
		        }
			}
	    }
		catch (Exception e) {
	        LOGGER.error("获取定时调度器对象失败", e);
	    }
    }
    
    public static void deleteQuartzJobOfSummary(EdocSummary summary){
    	deleteQuartzJob(summary.getId());
    }
    
	public static String getName(String ids,String seperator) throws Exception{
		 String[] sids = ids.split("[,]");
		 StringBuilder name = new StringBuilder();
		 for(String sid : sids){
			 String[] std =  sid.split("[|]");
			 Long acccountId = Long.valueOf(std[1]);
			 if(name.length() > 0){
				 name.append(seperator);
			 }
			 name.append(EdocRoleHelper.getAccountById(acccountId).getName());
		 }
		 return name.toString();
	}

	public static String getI18nSeperator(HttpServletRequest request){
		String seperator = "、";
		try{
			Locale locale =Functions.getLocale(request);
			String	sep   = ResourceBundleUtil.getString("com.seeyon.v3x.common.resources.i18n.SeeyonCommonResources", locale,"common.separator.label");
			if(Strings.isNotBlank(sep)) seperator = sep;
		}catch(Exception e){
			LOGGER.error("处理国际化资源异常：",e);
		}
		return seperator;
	}
	 public static void reLoadAccountName(EdocSummary summary,String seperator){
			try{
				//主送单位s
				if(Strings.isNotBlank(summary.getSendUnit()) && Strings.isNotBlank(summary.getSendUnitId())){
					 String name = getName(summary.getSendUnitId(),seperator);
					 if(Strings.isNotBlank(name))  summary.setSendUnit(name);
				}
				//send_to
				if(Strings.isNotBlank(summary.getSendTo()) && Strings.isNotBlank(summary.getSendToId())){
					 String name = getName(summary.getSendToId(),seperator);
					 if(Strings.isNotBlank(name))  summary.setSendTo(name);
				}
				
				//reportto
				if(Strings.isNotBlank(summary.getReportTo()) && Strings.isNotBlank(summary.getReportToId())){
					 String name = getName(summary.getReportToId(),seperator);
					 if(Strings.isNotBlank(name))  summary.setReportTo(name);
				}
				
				//copyTo
				if(Strings.isNotBlank(summary.getCopyTo()) && Strings.isNotBlank(summary.getCopyToId())){
					 String name = getName(summary.getCopyToId(),seperator);
					 if(Strings.isNotBlank(name))  summary.setCopyTo(name);
				}
				
				if(Strings.isNotBlank(summary.getPrintUnitId()) && Strings.isNotBlank(summary.getPrintUnit())){
					String name = getName(summary.getPrintUnitId(),seperator);
					if(Strings.isNotBlank(name)) summary.setPrintUnit(name);
				}
			}catch(Exception e){
				LOGGER.error("edocHelper.reLoadAccountName异常",e);
			}
		}
	 
	 public static int takeBack(Long caseId,CtpAffair affair) {
		 int result = 0;
/*		 try {
			 ProcessEngine pe = getProcessEngine();
			 BPMCase theCase = pe.getCase(caseId);
			 BPMProcess process= getRunningProcessByCaseId(caseId);
			
		 }catch(Exception e) {
			 LOGGER.error("edocHelper.takeBack异常", e);
		 }
*/		 return result;
	 }
	    public static boolean isInform(BPMAbstractNode activity){
	    	String _nodePolicy = activity.getSeeyonPolicy().getId();
			if("inform".equals(_nodePolicy) || "zhihui".equals(_nodePolicy)){
				return true;
			}
			return false;
	    }
	  public static List<BPMHumenActivity> findDirectHumenChildrenCascade(BPMAbstractNode current_node, boolean isInformRecur) {
	        List<BPMHumenActivity> result = new ArrayList<BPMHumenActivity>();
	        List<BPMTransition> down_links = current_node.getDownTransitions();
	        for (BPMTransition down_link : down_links) {
	            BPMAbstractNode _node = down_link.getTo();
	            if (_node instanceof BPMHumenActivity) {
	                BPMHumenActivity node = (BPMHumenActivity) _node;
	                result.add(node);
	            	if(isInformRecur && isInform(node)){
	            		List<BPMHumenActivity> children = findDirectHumenChildrenCascade(_node,isInformRecur);
	                    result.addAll(children);
	            	}
	            } else if (_node instanceof BPMAndRouter || _node instanceof BPMConRouter) {
	                List<BPMHumenActivity> children = findDirectHumenChildrenCascade(_node,isInformRecur);
	                result.addAll(children);
	            } else if (_node instanceof BPMEnd) {
	                return new ArrayList<BPMHumenActivity>(0);
	            }
	        }
	        return result;
	    }
	    /**
	     * 得到activiey的父节点,如果不是人工节点递归读取;
	     *
	     * @param activity
	     * @return
	     */
	    public static List<BPMHumenActivity> getParentHumens(BPMActivity activity) {
	        List<BPMHumenActivity> humenList = new ArrayList<BPMHumenActivity>();
	        List<BPMTransition> transitions = activity.getUpTransitions();
	        for (BPMTransition tran : transitions) {
	            BPMAbstractNode parent = tran.getFrom();
	            if (parent.getNodeType() == BPMAbstractNode.NodeType.humen) {
	                humenList.add((BPMHumenActivity)parent);
	            }
	            else if (parent.getNodeType() == BPMAbstractNode.NodeType.join || parent.getNodeType() == BPMAbstractNode.NodeType.split) {
	                humenList.addAll(getParentHumens((BPMActivity)parent));
	            }
	        }
	        return humenList;
	    }

	 public static BPMActivity getBPMActivityByAffair(CtpAffair affair) throws Exception {

	        if (affair == null)
	            return null;
	        Long workitemId = affair.getSubObjectId();
	        if (workitemId == null || workitemId == 0) {
	            return null;
	        }

	        WorkItem workitem;
			try {
				workitem = getWorkItemById(workitemId);
				String activityId = workitem != null ? workitem.getActivityId():null;
			    long caseId = workitem != null ? workitem.getCaseId() : null;
			    BPMProcess process = wapi.getBPMProcess(affair.getProcessId(),caseId);
			    BPMActivity activity = process.getActivityById(activityId);
			    return activity;
			} catch (EdocException e) {
				LOGGER.error("", e);
			}
	      return null;
	    }
	 
	    //政务专用，覆盖父类的同名方法进行修改--加签 - 与后一节点并发
	    public static List<String> insertNextParellel(BPMProcess process, Long workitemId, BPMSeeyonPolicy policy, boolean isFormOperationReadonly) throws EdocException {
	    	User user = AppContext.getCurrentUser();
	    	List<String> memAndPolicyName = new ArrayList<String>();
	    	try {
	    		WorkItem workitem = wapi.getWorkItemById("",workitemId);
	    		BPMActivity currentActivity = process.getActivityById(workitem.getActivityId());
	    		List downTransitions = currentActivity.getDownTransitions();
	    		BPMTransition nextTrans = (BPMTransition) downTransitions.get(0);
	    		BPMAndRouter split = null;
	    		BPMAndRouter join = null;
	    		//查看下一节点的节点类型
				BPMAbstractNode childNode = nextTrans.getTo();
				BPMAbstractNode.NodeType nodeType = childNode.getNodeType();
				if(nodeType == BPMAbstractNode.NodeType.end ||
						nodeType == BPMAbstractNode.NodeType.join){
					return null;
				}else if(nodeType == BPMAbstractNode.NodeType.split){
					split = (BPMAndRouter)childNode;
	                join = findJoinOfSplit(process,split);

				}else if(nodeType == BPMAbstractNode.NodeType.humen){
					String splitId = UUIDLong.longUUID()+"";
	    			String joinId = UUIDLong.longUUID()+"";
	    			split = new BPMAndRouter(splitId, "split");
	    			join = new BPMAndRouter(joinId, "join");
	    			split.setStartAnd(true);
	    			join.setStartAnd(false);
	    			String relevancyId = UUIDLong.longUUID() + "";
	    			split.setParallelismNodeId(relevancyId);
	    			join.setParallelismNodeId(relevancyId);
	    			process.addChild(split);
	    			process.addChild(join);
	    			
	    			process.addNodeToSwimlane(currentActivity.getId(), split);
	    			process.addNodeToSwimlane(currentActivity.getId(), join);

	    			BPMTransition nextChildTrans = (BPMTransition)childNode.getDownTransitions().get(0);
	    			BPMAbstractNode nextChildNode = nextChildTrans.getTo();
	    			BPMTransition trans1 = new BPMTransition(currentActivity, split);
	    			BPMTransition trans2 = new BPMTransition(split, childNode);
	    			BPMTransition trans3 = new BPMTransition(childNode, join);
	    			BPMTransition trans4 = new BPMTransition(join, nextChildNode);
	    			copyCondition(nextTrans, trans1);
	    			copyCondition(nextChildTrans, trans4);

	    			process.addLink(trans1);
	    			process.addLink(trans2);
	    			process.addLink(trans3);
	    			process.addLink(trans4);

	    			process.removeLink(nextTrans);
	    			currentActivity.removeDownTransition(nextTrans);
	    			childNode.removeUpTransition(nextTrans);

	    			process.removeLink(nextChildTrans);
	    			childNode.removeDownTransition(nextChildTrans);
	    			nextChildNode.removeUpTransition(nextChildTrans);
				}

	    		//向split、join之间添加新结�点
	    		Date now = new Date(System.currentTimeMillis());
	    		process.setUpdateDate(now);
	    	} catch (BPMException e) {
	    		throw new EdocException("加签并发操作异常", e);
	    	}
	    	return memAndPolicyName;
	    }
	    

	    
	    /**
		 * 找到Split节点对应的join节点。
		 * @param process
		 * @param split
		 * @return
		 */
		private static BPMAndRouter findJoinOfSplit(BPMProcess process, BPMAndRouter split) {

			BPMAndRouter join = null;
	        BPMAbstractNode node = split;
	        // 算法查找
	        // 查找所有直接后续节点都通过的Join节点。
			List<BPMTransition> links = node.getDownTransitions();
			if(links==null) return null;

			Set<BPMAbstractNode> set = new HashSet<BPMAbstractNode>();
			for (BPMTransition link : links) {
				BPMAbstractNode to = link.getTo();
				 Set<BPMAbstractNode>  allNext =getAllNextNodes(to);
				if(set.size()==0)
				{
					set.addAll(allNext);
				}
				else
				{
					set.retainAll(allNext);
				}
			}
	        if(set.size()>0)
	        {
	        	// 找出路径中第一个Join节点
	        	BPMAndRouter firstJoin =null;
	        	for (BPMAbstractNode n : set) {
					if(isJoinNode(n))
					{
						BPMAndRouter join2 = (BPMAndRouter)n;
						if(firstJoin==null)
						{
							firstJoin = join2;
						}
						firstJoin = (passThrough(firstJoin, join2))?firstJoin:join2;
					}
				}
	        	return firstJoin;


	        }
	        // return (BPMAndRouter)n
			// ColHelper中出现了多次查找，使用的方式也不一致，下面是原来的两种判断方式，依据ParallelismNodeId查找，算法查找有效后可删除；如果ParallelismNodeId有效，优先使用按节点下溯查找
	        // 按节点下溯查找
	        boolean foundJoin = false;
	        while (!foundJoin) {
	            BPMTransition trans = (BPMTransition) node.getDownTransitions().get(0);
	            node = trans.getTo();
	            if (node instanceof BPMAndRouter) {
	                BPMAndRouter andNode = (BPMAndRouter) node;
	                if (isJoinNode(node)) {
	                	if(split.getParallelismNodeId().equals(andNode.getParallelismNodeId())){
	                		foundJoin = true;
	                		join = andNode;
	                		return join;
	                	}
	                }
	            }
	        }

			// 遍历所有activity
			List<BPMAbstractNode> activityList = process.getActivitiesList();
			for(int i=0; i<activityList.size(); i++){
				node = activityList.get(i);
				if(node instanceof BPMAndRouter){
					BPMAndRouter andRouter = (BPMAndRouter)node;
					String parallelismSplitId = split.getParallelismNodeId();
					String parallelismJoinId = andRouter.getParallelismNodeId();
					if(parallelismSplitId.equalsIgnoreCase(parallelismJoinId)
							&& !split.getId().equals(andRouter.getId())){
						join = andRouter;
						break;
					}
				}
			}
			return join;
		}
		
		/**
		 * 取得节点的所有后续节点。
		 * @param node
		 * @return
		 */
		private static Set<BPMAbstractNode> getAllNextNodes(BPMAbstractNode node)
		{
			Set<BPMAbstractNode> result = new HashSet<BPMAbstractNode>();
			List<BPMTransition> links = node.getDownTransitions();
			for (BPMTransition link : links) {

				BPMAbstractNode to = link.getTo();
				result.add(to);
				if(!(to instanceof BPMEnd))
				result.addAll(getAllNextNodes(to));
			}
			return result;
		}
		
		private static boolean isJoinNode(BPMAbstractNode node) {
			return (node instanceof BPMAndRouter) && !((BPMAndRouter) node).isStartAnd();
		}
		
	    //政务专用，覆盖父类的同名方法---加签
	    public static List<String> insertPeople(Long caseId, Long workitemId, BPMProcess process, String userId, boolean isFormOperationReadonly) throws EdocException {
	    	return null;
	    }
	    //加签 - 串发
	    public static List<String> insertSerial(BPMProcess process, Long workitemId,  BPMSeeyonPolicy policy, boolean isFormOperationReadonly) throws EdocException {
	        User user = AppContext.getCurrentUser();
	        List<String> memAndPolicyName = new ArrayList<String>();
	        try {
	            WorkItem workitem = wapi.getWorkItemById("",workitemId);
	            BPMActivity currentActivity = process.getActivityById(workitem.getActivityId());
	            List downTransitions = new ArrayList(currentActivity.getDownTransitions());

	            //添加的第一个结�点
	            //添加的最后一个结�点
	            BPMAbstractNode lastNode = null;
	            BPMAbstractNode previousNode = currentActivity;

	            lastNode = previousNode;
	            if (downTransitions != null) {
	                for (int i = 0; i < downTransitions.size(); i++) {
	                    BPMTransition trans = (BPMTransition) downTransitions.get(i);
	                    BPMAbstractNode to = trans.getTo();
	                    BPMTransition userLink1 = new BPMTransition(lastNode, to);
	                    copyCondition(trans,userLink1);
	                    process.addLink(userLink1);
	                    process.removeLink(trans);
	                    currentActivity.removeDownTransition(trans);
	                }
	            }

	            Date now = new Date(System.currentTimeMillis());
	            process.setUpdateDate(now);
	        } catch (BPMException e) {
	        	throw new EdocException("加签并发操作异常", e);
	        }
	        return memAndPolicyName;
	    }

	    public static CtpAffair getAffairByEdocRegisterId(EdocRegister edocRegister) throws BusinessException {
	    	if(edocRegister.getRegisterType() == 1) {
		    	AffairManager affairManager = (AffairManager)AppContext.getBean("affairManager");
		    	Map<String, Object> columns = new HashMap<String, Object>();
		    	columns.put("app", ApplicationCategoryEnum.edoc.getKey());
		    	columns.put("subApp", ApplicationSubCategoryEnum.old_edocRegister.getKey());
		    	columns.put("objectId", edocRegister.getEdocId());
		    	columns.put("subObjectId", edocRegister.getRecieveId());
		    	List<CtpAffair> affairList = affairManager.getByConditions(null, columns);
		    	if(affairList!=null && affairList.size()>0) {
		    		return affairList.get(0);
		    	}
	    	}
	    	return null;
	    }
	    
	    public static CtpAffair getDistributeAffair(Long registerId) throws BusinessException{
	    	AffairManager affairManager = (AffairManager)AppContext.getBean("affairManager");
	    	Map<String, Object> columns = new HashMap<String, Object>();
	    	columns.put("app", ApplicationCategoryEnum.edoc.getKey());
	    	columns.put("subApp", ApplicationSubCategoryEnum.old_edocRecDistribute.getKey());
	    	columns.put("objectId", registerId);
	    	columns.put("subObjectId", -1L);
	    	List<CtpAffair> affairList = affairManager.getByConditions(null, columns);
	    	if(affairList!=null && affairList.size()>0) {
	    		return affairList.get(0);
	    	}
	    	return null;
	    }
		
		/**
		 * 根据用户ID获取OrgEnt_Member
		 * 
		 * @param userId
		 * @return
		 */
		public static V3xOrgMember getMemberById(Long userId){
			V3xOrgMember member = null;
	       try {
	    	   OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
	           member = (V3xOrgMember) orgManager.getMemberById(userId);
	       } catch (BusinessException e) {
	           LOGGER.error("", e);
	       }
	       if(member==null) member=new V3xOrgMember();
	       return member;
		}
		
	    public static List<AgentModel> getEdocAgents(){
	    	List<AgentModel> ids = new ArrayList<AgentModel>();
	    	User u = AppContext.getCurrentUser();
	    	List<AgentModel> agentToList = MemberAgentBean.getInstance().getAgentModelToList(u.getId());
	    	List<AgentModel> agentList = MemberAgentBean.getInstance().getAgentModelList(u.getId());
	    	if(CollectionUtils.isEmpty(agentToList)){
	    		agentToList = agentList;
	    	}
	    	if(agentToList!=null){
	    		Date date = new Date();
	    		for(AgentModel m : agentToList){
	    			if(m.isHasEdoc() && date.after(m.getStartDate()) && date.before(m.getEndDate())){
	    				ids.add(m);
	    			}
	    		}
	    	}
	    	return ids;
	    }
	    
	    public static List<Long> findMyBrothers(CtpAffair affair){
	    	if(affair == null)
	    		return null; 
	    	List<Long> activities = null;
	    	try {
	    		BPMActivity currentActivity = getBPMActivityByAffair(affair);
	    		List<BPMActivity> parents = getParent(currentActivity);
	    		if(parents != null && !parents.isEmpty()) {
	    			List<BPMHumenActivity> allBrothers = EdocHelper.findDirectHumenChildrenCascade(parents.get(0), true);
	    			if(allBrothers != null && !allBrothers.isEmpty()) {
	    				activities = new ArrayList<Long>(allBrothers.size());
	    				for(BPMHumenActivity humen:allBrothers) {
	    					activities.add(Long.parseLong(humen.getId()));
	    				} 
	    			}
	    		}
	    	}catch(Exception e) {
	    		LOGGER.error("edocHelper.findMyBrothers异常", e);
	    	}
	    	return activities;
	    }
	
	/**
	 * 公文正文类型，如果没有office控件，默认显示为html
	 * @param _bodyContentType
	 * @return
	 */
	public static String getEdocBodyContentType(String bodyContentType) {
	    String _bodyContentType = bodyContentType;
		if(null == _bodyContentType){
        	_bodyContentType = com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_OFFICE_WORD;
        	if(com.seeyon.ctp.common.SystemEnvironment.hasPlugin("officeOcx")==false) {
            	_bodyContentType = com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_HTML;
            }
        }
		return _bodyContentType;
	}

	
	public static String getInterceptStringByLength(String value, int length) {
		String _value = value;
	    StringBuilder shows = new StringBuilder ();//用于显示的值  
		if(Strings.isNotBlank(_value)) {
			_value = _value.replaceAll ( "\r",  "" );    
			_value = _value.replaceAll ( "\n",  "\\\\"+System.getProperty("line.separator"));
			if(_value.length() >= length){//长度自己设置
				shows.append(_value.substring(0, length));//开头
				shows.append("....");
			}
			//GOV-4653 公文草稿箱发送一条有没填的必填项，提示框中公文标题变成书名号
			else{
				shows.append(_value);
			}
		}
		return shows.toString();
	}
	
	
	public static EdocSummary copyEdocSummaryFromRegister(EdocSummary summary, EdocRegister register) {
		summary.setSubject(register.getSubject());//标题
		summary.setIdentifier(register.getIdentifier());//标识
		summary.setDocType(register.getDocType());//公文类型
		summary.setSendType(register.getSendType());//行文类型
		summary.setDocMark(register.getDocMark());//来文字号
		summary.setSerialNo(register.getSerialNo());//内部编号
		summary.setUrgentLevel(register.getUrgentLevel());//紧急程度
		summary.setSecretLevel(register.getSecretLevel());//文件密级
		
		if(register.getKeepPeriod() != null && !"null".equals(register.getKeepPeriod())){
		    summary.setKeepPeriod(Strings.isBlank(register.getKeepPeriod())?1:Integer.parseInt(register.getKeepPeriod()));//保密期限
		}
		summary.setIssuer(register.getIssuer());//签发人
		//summary.setSigningDate(register.getIssueDate());//签发日期
		summary.setSigningDate(register.getEdocDate());//签发日期
		summary.setKeywords(register.getKeywords());//主题词
		summary.setCopies(register.getCopies());//拷贝份数
		summary.setFilefz(register.getNoteAppend());//附注
		summary.setFilesm(register.getAttNote());//附件说明
		summary.setSendTo(register.getSendTo());//主送单位
		summary.setSendToId(register.getSendToId());//主送单位id
		summary.setCopyTo(register.getCopyTo());//抄送单位
		summary.setCopyToId(register.getCopyToId());//抄送单位id
		summary.setSendUnit(register.getSendUnit());//来文单位
		
		Long rSendUnitId = register.getSendUnitId();
		if(rSendUnitId != null){
		    summary.setSendUnitId("Account|" + rSendUnitId);//来文单位id
		}
		if(register.getRecTime() != null){//签收日期
		    summary.setReceiptDate(new java.sql.Date(register.getRecTime().getTime()));
		}
		summary.setUnitLevel(register.getUnitLevel());
		/**summary.setDocMark2(register.getDocMark());
		summary.setSendUnit2(register.getSendUnit());//来文单位
		summary.setSendUnitId2("Account|"+register.getSendUnitId());//来文单位id
		 */
		//V5.1-G6--V51-4-6 分发里，登记日期就取G6的登记日期
		if(EdocHelper.isG6Version()){
			summary.setRegistrationDate(register.getRegisterDate());
		}
		return summary;
	}	
	
	public static EdocRegister copyEdocRegisterFromSummary(EdocSummary summary, EdocRegister register) {
		register.setSubject(summary.getSubject());//标题
		register.setIdentifier(summary.getIdentifier());//标识
		register.setDocType(summary.getDocType());//公文类型
		register.setSendType(summary.getSendType());//行文类型
		register.setDocMark(summary.getDocMark());//来文字号
		register.setSerialNo(summary.getSerialNo());//内部编号
		register.setUrgentLevel(Strings.isBlank(summary.getUrgentLevel())?"1":summary.getUrgentLevel());//紧急程度
		register.setSecretLevel(Strings.isBlank(summary.getSecretLevel())?"1":summary.getSecretLevel());//文件密级
		register.setKeepPeriod(String.valueOf(summary.getKeepPeriod()));//保密期限
		register.setIssuer(summary.getIssuer());//签发人
		//register.setIssueDate(summary.getSigningDate());//签发日期
		register.setEdocDate(summary.getSigningDate());//签发日期
		register.setKeywords(summary.getKeywords());//主题词
		register.setCopies(summary.getCopies());//拷贝份数
		register.setNoteAppend(summary.getFilefz());//附注
		register.setAttNote(summary.getFilesm());//附件说明
		register.setSendTo(summary.getSendTo());//主送单位
		register.setSendToId(summary.getSendToId());//主送单位id
		register.setCopyTo(summary.getCopyTo());//抄送单位
		register.setCopyToId(summary.getCopyToId());//抄送单位id
		//register.setSendUnit(summary.getSendUnit());//来文单位
		//register.setSendUnitId(summary.getSendUnitId());//来文单位id
		register.setEdocUnit(summary.getSendUnit());//成文单位
		register.setEdocUnitId(summary.getSendUnitId());//成文单位id
		return register;
	}
	
	public static V3xOrgMember getFirstEdocRole(Long accountId, Long userId, int edocType) throws Exception {
		OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
		if(EdocRoleHelper.isEdocCreateRole(accountId, userId, EdocEnum.edocType.recEdoc.ordinal())) {
			return orgManager.getMemberById(userId);
		} else {
			ConfigManager configManager = (ConfigManager)AppContext.getBean("configManager");
			Long memberId = getDistributeUser(configManager, orgManager, EdocEnum.edocType.recEdoc.ordinal(), IConfigPublicKey.EDOC_CREATE_ITEM_KEY_REC, userId, accountId);
			if(memberId != null) {
				return orgManager.getMemberById(memberId);
			}
		}
		return null;
	}
	
	/*
	 * G6 V1.0 SP1后续功能_签收时自动登记功能
	 * @see com.seeyon.v3x.exchange.manager.EdocExchangeManager#getDistributeUser(ConfigManager, java.lang.Long, java.lang.Long)
	 */
	public static Long getDistributeUser(ConfigManager configManager, OrgManager orgManager, int edocType, String roleType, Long userId, Long accountId) {
		try {
			String edocRecDistributeCreates = "";
			String[] edocRecDistributeCreatesArray = null;
			String edocRecDistributeCreatesFirst = null;
			if(EdocRoleHelper.isEdocCreateRole(accountId, userId, edocType)){
				return userId;
			}
			ConfigItem edocRecDistributeItem = configManager.getConfigItem(IConfigPublicKey.EDOC_CREATE_KEY, roleType, accountId);
			if(null == edocRecDistributeItem || null == edocRecDistributeItem.getExtConfigValue() || "".equals(edocRecDistributeItem.getExtConfigValue())){
				return null;
			}
			edocRecDistributeCreates = edocRecDistributeItem.getExtConfigValue();
			edocRecDistributeCreatesArray = edocRecDistributeCreates.split(",");
			edocRecDistributeCreatesFirst = edocRecDistributeCreatesArray[0];//取得第一个
			String orgType = edocRecDistributeCreatesFirst.split("\\|")[0];//第一个组织的类型（Member|Department|Post|Level）
			Long id = Long.parseLong(edocRecDistributeCreatesFirst.split("\\|")[1]);//第一个组织的ID
			if("Member".equals(orgType)){//收文分发权限的第一个是'人员'，不是岗位或职务，就直接返回第一个人
				return Long.parseLong(edocRecDistributeCreatesFirst.split("\\|")[1]);
			}else {
				for(String str : edocRecDistributeCreatesArray){
					List<V3xOrgMember> list = null;
					orgType = str.split("\\|")[0];
					id = Long.parseLong(str.split("\\|")[1]);
					Long memberId = null;
					if("Department".equals(orgType)){
						list = orgManager.getMembersByDepartment(id, false);
					}else if("Post".equals(orgType)){
						list = orgManager.getMembersByPost(id);
					}else if("Level".equals(orgType)){
						list = orgManager.getMembersByLevel(id);
					}else if("Member".equals(orgType)){
						memberId = id;
					}
					if(list != null && list.size() > 0)
						return list.get(0).getId();
					else if(null != memberId)
						return memberId;
					else
						continue;
				}
			}
		} catch (NumberFormatException e) {
			LOGGER.error("", e);
		} catch (BusinessException e) {
			LOGGER.error("", e);
		}
		return null;
	}
	
    private static OrgManager getOrgManager(){
    	if(orgManager == null){
    		orgManager = (OrgManager)AppContext.getBean("orgManager");
    	}

    	return orgManager;
    }
    
    public static void setActivityIsDelete(BPMProcess process, Map<String, String> condition) throws EdocException{
        try {
            Iterator<String> iter = condition.keySet().iterator();
            while (iter.hasNext()) {
                String nodeId = (String) iter.next();
                String isDelete = condition.get(nodeId);
                BPMHumenActivity activity = (BPMHumenActivity) process.getActivityById(nodeId);
                if(activity==null)
                	continue;
                BPMSeeyonPolicy seeyonPolicy = (BPMSeeyonPolicy) activity.getSeeyonPolicy();
                seeyonPolicy.setIsDelete(isDelete);
            }

        } catch (Exception e) {
        	throw new EdocException("动态设置节点是否进行假删除异常", e);
        }
    }
		/**
		 * 转换分支显示
		 * @param condition  分支条件
		 * @param pattern    显示格式
		 * @param orgManager
		 * @author yuhj
		 * @author wangchw
		 * @return
		 */
		public static String parseCondition(String condition,String pattern,OrgManager orgManager){
            if(condition == null || "".equals(condition)){
                return condition;
            }
            String pn = null;
            StringBuffer sb = null;
            if(pattern == null){
                sb = new StringBuffer("(Account)|(Department)|(Post)|(Level)");
                sb.append("|(standardpost)|(grouplevel)")
                .append("|(include\\([^\\&\\& | \\|\\|]*\\))|(exclude\\([^\\&\\& | \\|\\|]*\\))|(<>)")
                .append("|(isRole\\([^\\&\\& | \\|\\|]*\\))|(isNotRole\\([^\\&\\& | \\|\\|]*\\))|(isPost\\(.*?Post\\))|(isNotPost\\(.*?Post\\))")
                .append("|(isDep\\(.*?Child\\))|(isNotDep\\(.*?Child\\))|(exist\\(.*?\\d\\))")
                .append("|(\\{[^\\}]*\\})")
                .append("|(\\[[^:]*:[^\\[\\]]*\\])");
                pn = sb.toString();
            }else{
                pn = pattern;
            }
            Pattern p = Pattern.compile(pn);
            Matcher m = p.matcher(condition);
            sb = new StringBuffer();
            String group = null;
            String temp = null;
            String[] arr = null;
            int position = -1;
            int lastPosition = -1;
            String separator = ResourceUtil.getString("common.separator.label");
            V3xOrgDepartment dep = null;
            V3xOrgPost post = null;
            while(m.find()) {
                group = m.group();
                if(group.equals(ORGENT_TYPE_DEPARTMENT)){   //部门
                    m.appendReplacement(sb, new StringBuilder("[")
                                                   .append(ResourceUtil.getString("org.department.label"))
                                                   .append("]").toString());
                }else if(group.equals(ORGENT_TYPE_ACCOUNT)){  //单位
                m.appendReplacement(sb, new StringBuilder("[")
                                            .append(ResourceUtil.getString("org.account.label"))
                                            .append("]").toString());
                }else if(group.equals(ORGENT_TYPE_POST) || "standardpost".equals(group)){  //岗位
                    m.appendReplacement(sb, new StringBuilder("[")
                                                    .append(ResourceUtil.getString("org.post.label"))
                                                    .append("]").toString());
                }else if(group.equals(ORGENT_TYPE_LEVEL) || "grouplevel".equals(group)){   //职务级别
                    m.appendReplacement(sb, new StringBuilder("[")
                                                .append(ResourceUtil.getString("org.level.label"))
                                                .append("]").toString());
                }else if(group.startsWith("{") && group.endsWith("}")) {  //表单数据域
                    position = group.indexOf(":");
                    if(position!=-1){
                        m.appendReplacement(sb, new StringBuilder("{")
                                                     .append(group.substring(position+1, group.length()-1))
                                                     .append("}").toString());
                    }else{
                        m.appendReplacement(sb, group);
                    }
                }else if((group.startsWith("include") || group.startsWith("exclude"))&& group.indexOf(",")!=-1){  //组和副岗，现在副岗用isPost表达，兼容旧数据
                    if(group.indexOf("team,")!=-1){    //组，格式：include(team,代码组:'-258834243227926063')
                        try{
                            temp = group.substring(group.indexOf(":")+2, group.length()-2);
                            V3xOrgTeam team = orgManager.getTeamById(Long.parseLong(temp));
                            if(team != null){
                                temp = team.getName();
                            }else{
                                temp = group;
                            }
                        }catch(Exception e){
                            temp = group;
                            LOGGER.error("edocHelper.parseCondition异常",e);
                        }
                        
                        String oldTemp = temp.toString();
                        temp = new StringBuilder("[")
                                 .append(ResourceUtil.getString("org.team.label"))
                                 .append("] ").append(group.startsWith("include") ? " = ":" <> ")
                                 .append(oldTemp).toString();
                        m.appendReplacement(sb, temp);
                    }else if(group.indexOf("secondpost")!=-1){  //副岗，格式：include(secondpost,研发二部-测试工程师:'-6208460242951650128_9099248666769706830')
                        try{
                            temp = group.substring(group.indexOf(":")+2, group.length()-2);
                            dep = orgManager.getDepartmentById(Long.parseLong(temp.substring(0, temp.indexOf("_"))));
                            post = orgManager.getPostById(Long.parseLong(temp.substring(temp.indexOf("_")+1)));
                        }catch(Exception e){
                            temp = group;
                            LOGGER.error("edocHelper.parseCondition异常",e);
                        }
                        if(dep != null && post != null){
                            temp = new StringBuilder("[")
                                     .append(ResourceUtil.getString("org.secondPost.label"))
                                     .append("] ")
                                     .append(group.startsWith("include")?" = ":" <> ")
                                     .append(dep.getName())
                                     .append("-")
                                     .append(post.getName())
                                     .toString();
                        }else{
                            temp = group;
                        }
                        m.appendReplacement(sb, temp);
                    }
                }else if(group.startsWith("[") && group.endsWith("]")){  //数据值
                    if(group.indexOf(ORGENT_TYPE_ACCOUNT)!=-1||group.indexOf(ORGREL_TYPE_CONCURRENT_ACCOUNT)!=-1||
                            group.indexOf(ORGENT_TYPE_LEVEL)!=-1||group.indexOf(ORGREL_TYPE_CONCURRENT_LEVEL)!=-1){
                        if(group.indexOf(ORGENT_TYPE_ACCOUNT)!=-1||group.indexOf(ORGREL_TYPE_CONCURRENT_ACCOUNT)!=-1){
                            if(group.indexOf(ORGENT_TYPE_ACCOUNT)!=-1){
                                group=group.replace(ORGENT_TYPE_ACCOUNT, ResourceUtil.getString("org.mamber_form.belongAcunt.label"));
                            }
                            if(group.indexOf(ORGREL_TYPE_CONCURRENT_ACCOUNT)!=-1){
                                group=group.replace(ORGREL_TYPE_CONCURRENT_ACCOUNT, ResourceUtil.getString("org.mamber_form.concurrentAcunt.label"));
                            }
                            m.appendReplacement(sb, group);
                        }else{
                            if(group.indexOf(ORGENT_TYPE_LEVEL)!=-1){
                                group=group.replace(ORGENT_TYPE_LEVEL, ResourceUtil.getString("org.mamber_form.belongLevl.label"));
                            }
                            if(group.indexOf(ORGREL_TYPE_CONCURRENT_LEVEL)!=-1){
                                group=group.replace(ORGREL_TYPE_CONCURRENT_LEVEL, ResourceUtil.getString("org.mamber_form.concurrentLevl.label"));
                            }
                            m.appendReplacement(sb, group);
                        }
                    }else{
                        temp = group.substring(1, group.length()-1);
                        arr = temp.split(":");
                        if(arr != null && arr.length == 2){
                            //枚举直接取显示值
                            if(arr[1].length() > 10){
                                temp = getEntityName(arr[1],null,arr[0],orgManager);
                            }else{
                                temp = arr[0];
                            }
                        }else{
                            temp = group;
                        }
                        m.appendReplacement(sb, temp);
                    }
                }else if(group.indexOf("isRole") != -1){   //角色，格式：isRole(Role,'部门主管:DepManager')
                    temp = new StringBuilder("[")
                            .append(ResourceUtil.getString("org.role.label"))
                            .append("] = ")
                            .append(ResourceUtil.getString("space.department.manager.label"))
                            .toString();
                    m.appendReplacement(sb, temp);
                }else if(group.indexOf("isNotRole") != -1){
                    temp = new StringBuilder("[")
                            .append(ResourceUtil.getString("org.role.label"))
                            .append("] <> ")
                            .append(ResourceUtil.getString("space.department.manager.label"))
                            .toString();
                    m.appendReplacement(sb, temp);
                }else if(group.indexOf("isPost") != -1 || group.indexOf("isNotPost") != -1){  //岗位，格式：isPost(财务总监:-7239727208021734147,Post,Member_Post,Concurrent_Post)
                    arr = group.substring(0, group.length()-1).split(",");
                    if(arr != null && arr.length > 1){
                        StringBuilder tempBuilder = new StringBuilder("[");
                        for(int i=1;i<arr.length;i++){
                            
                            if(tempBuilder.length() > 1){
                                tempBuilder.append(",");
                            }
                            if(ORGENT_TYPE_POST.equals(arr[i])){
                                tempBuilder.append(ResourceUtil.getString("org.member_form.primaryPost.label"));
                            }else if(ORGREL_TYPE_MEMBER_POST.equals(arr[i])){
                                tempBuilder.append(ResourceUtil.getString("org.member_form.secondPost.label"));
                            }else if(ORGREL_TYPE_CONCURRENT_POST.equals(arr[i])){
                                tempBuilder.append(ResourceUtil.getString("cntPost.body.cntpost.label"));
                            }
                        }
                        tempBuilder.append("]");
                        
                        if(group.indexOf("isPost") != -1){
                            tempBuilder.append(" = ");
                        } else{
                            tempBuilder.append(" <> ");
                        }
                        position = arr[0].indexOf("(");
                        lastPosition = arr[0].indexOf(":");
                        tempBuilder.append(getEntityName(arr[0].substring(lastPosition+1),ORGENT_TYPE_POST,arr[0].substring(position+1, lastPosition),orgManager));
                        temp = tempBuilder.toString();
                    }else{
                        temp = group;
                    }
                    m.appendReplacement(sb, temp);
                }else if(group.indexOf("isDep") != -1 || group.indexOf("isNotDep") != -1){
                    List<String> depts=new ArrayList<String>();//选择的部门条件
                    if(group.indexOf(ORGREL_TYPE_DEPARTMENT)!=-1){
                        depts.add(ORGREL_TYPE_DEPARTMENT);
                        group=group.replace(","+ORGREL_TYPE_DEPARTMENT, "");
                   }
                   if(group.indexOf(ORGREL_TYPE_SECOND_POST_DEPARTMENT)!=-1){
                        depts.add(ORGREL_TYPE_SECOND_POST_DEPARTMENT);
                        group=group.replace(","+ORGREL_TYPE_SECOND_POST_DEPARTMENT, "");
                   }
                   if(group.indexOf(ORGREL_TYPE_CONCURRENT_DEPARTMENT)!=-1){
                        depts.add(ORGREL_TYPE_CONCURRENT_DEPARTMENT);
                        group=group.replace(","+ORGREL_TYPE_CONCURRENT_DEPARTMENT, "");
                   }
                    arr = group.substring(0, group.length()-1).split(",");
                    if(arr != null && arr.length > 1){
                        
                        StringBuilder tempBuilder = new StringBuilder("[");
                        
                        //新增部门条件后处理
                        if(depts!=null&&depts.size()>0){
                            for(int i=0;i<depts.size();i++){
                                if(ORGREL_TYPE_DEPARTMENT.equals(depts.get(i))){
                                    tempBuilder.append(ResourceUtil.getString("org.member_form.departments.label"));
                                }
                                if(ORGREL_TYPE_SECOND_POST_DEPARTMENT.equals(depts.get(i))){
                                    if(tempBuilder.length() > 1){
                                        tempBuilder.append(",");
                                    }
                                    tempBuilder.append(ResourceUtil.getString("org.member_form.secondPostDepartment.label"));
                                }
                                if(ORGREL_TYPE_CONCURRENT_DEPARTMENT.equals(depts.get(i))){
                                    if(tempBuilder.length() > 1){
                                        tempBuilder.append(",");
                                    }
                                    tempBuilder.append(ResourceUtil.getString("org.member_form.concurrentDepartment.label"));
                                }
                            }
                            
                        }else{//对低版本的流程进行处理
                            tempBuilder.append(ResourceUtil.getString("org.department.label"));
                        }
                        tempBuilder.append("]");
                        
                        if(group.indexOf("isDep")!= -1){
                            tempBuilder.append(" = ");
                        } else{
                            tempBuilder.append(" <> ");
                        }
                        for(int i=0;i<arr.length;i++){
                            lastPosition = arr[i].lastIndexOf(":");
                            if(lastPosition!=-1){
                                if(i==0){
                                    position = arr[i].indexOf("(");
                                    tempBuilder.append(getEntityName(arr[i].substring(lastPosition+1),ORGENT_TYPE_DEPARTMENT,arr[0].substring(position+1, lastPosition),orgManager));
                                }else{
                                    tempBuilder.append(getEntityName(arr[i].substring(lastPosition+1),ORGENT_TYPE_DEPARTMENT,arr[0].substring(0, lastPosition),orgManager));
                                }
                                if(i != arr.length-2){
                                    tempBuilder.append(separator);
                                }
                            }
                        }
                        tempBuilder.append(" ");
                        if(group.indexOf(EdocConstant.BranchDepartmentStatus.includeChild.name()) != -1){
                            tempBuilder.append(ResourceUtil.getString("col.branch.includeChildren"));
                        }else{
                            tempBuilder.append(ResourceUtil.getString("col.branch.excludeChildren"));
                        }
                        temp = tempBuilder.toString();
                    }else{
                        temp = group;
                    }
                    m.appendReplacement(sb, temp);
                }else if(group.indexOf("exist")!=-1){
                    arr = group.substring(0, group.length()-1).split(",");
                    if(arr != null && arr.length==3){
                        StringBuilder tempBuilder = new StringBuilder(arr[0].substring(arr[0].indexOf("(")+1));
                        
                        tempBuilder.append(" ").append(arr[1]);
                        position = arr[2].indexOf(":");
                        tempBuilder.append(" ");
                        if(position != -1){
                            tempBuilder.append(getEntityName(arr[2].substring(position+1),null,arr[2].substring(0, position),orgManager));
                        } else{
                            tempBuilder.append(arr[2]);
                        }
                        temp = tempBuilder.toString();
                    }else{
                        temp = group;
                    }
                    m.appendReplacement(sb, temp);
                }
            }
            m.appendTail(sb);
            return sb.toString().replaceAll("&&", "and").replaceAll("\\|\\|", "or").replaceAll("==", "=")
            .replaceAll("form:check", ResourceUtil.getString("col.branch.check")).replaceAll("form:uncheck", ResourceUtil.getString("col.branch.uncheck"));
        }
		
		
		/**
		 * 通过类型和ID串获取相关的名称
		 * @Author      : xuqw
		 * @Date        : 2015年6月17日下午5:22:09
		 * @param typeAndIds Account|12345
		 * @param sq 分隔符
		 * @param orgManager
		 * @return
		 */
		public static String getEntityNames(String typeAndIds, String sq,OrgManager orgManager){
		    
		    StringBuilder ret = new StringBuilder();
		    
		    if(Strings.isNotBlank(typeAndIds)){
		        
		        String[] entitys = typeAndIds.split("[,]");
		        for(String entity : entitys){
		            
		            String[] eInfos = entity.split("[|]");
		            String type = eInfos[0];
		            String id = eInfos[1];
		            String name = getEntityName(id, type, "", orgManager);
		            
		            if(Strings.isNotBlank(name)){
		                if(ret.length() > 0){
		                    ret.append(sq);
		                }
		                ret.append(name);
		            }
		        }
		    }
		    
		    return ret.toString();
		}
		
		
		/**
		 * 获取组织模型的名称
		 * @Author      : xuqw
		 * @Date        : 2015年6月17日下午5:18:54
		 * @param entityId
		 * @param entityType
		 * @param defaultName
		 * @param orgManager
		 * @return
		 */
	    public static String getEntityName(String entityId,String entityType,String defaultName,OrgManager orgManager){
	        
	        String ret = defaultName;
			
	        try{
			    
				Long id = Long.parseLong(entityId);
				V3xOrgEntity entity = null;
				
				String[] typeArray = null;
				int index = 0;
				if(Strings.isNotBlank(entityType)){
				    typeArray = new String[5];
				    typeArray[index++] = entityType;
				}else {
				    typeArray = new String[4];
                }
				typeArray[index++] = ORGENT_TYPE_DEPARTMENT;
				typeArray[index++] = ORGENT_TYPE_POST;
				typeArray[index++] = ORGENT_TYPE_LEVEL;
				typeArray[index++] = ORGENT_TYPE_ACCOUNT;
				
				for(int i = 0; i < typeArray.length && entity == null; i++){
				    
				    entity = orgManager.getEntity(typeArray[i], id);
				}
				
				if(entity != null){
                    ret = entity.getName();
                }
			}catch(Exception e){
				LOGGER.error("edocHelper.getEntityName异常", e);
			}
			return ret;
		}

	    protected static void deleteQuartzJob(Long summaryId){
	    	if(QuartzHolder.hasQuartzJob("ColProcessDeadLine" + summaryId)){
	    		QuartzHolder.deleteQuartzJob("ColProcessDeadLine" + summaryId);
	    		QuartzHolder.deleteQuartzJob("ColProcessRemind" + summaryId);
	    	}
	    	QuartzHolder.deleteQuartzJob("EdocSupervise" + summaryId);
		}
	    
	    public static WorkItem getWorkItemById(Long workitemId) throws EdocException {
	        WorkItem wi = null;
	        try {
	        	wi = wapi.getWorkItemById("", workitemId);
	        } catch (BPMException ex) {
	        	LOGGER.error("workitem["+workitemId+"]不存在了，重新产生一条一模一样的待办数据，但失败了!",ex);
                throw new EdocException("获取工作项管理对外接口异常[ColHelper.getWorkItemById]", ex);
	        }
	        return wi;

	    }

	    public static void copyCondition(BPMTransition target,BPMTransition to){
	    	to.setConditionBase(target.getConditionBase());
	    	to.setConditionId(target.getConditionId());
	    	to.setConditionTitle(target.getConditionTitle());
	    	to.setConditionType(target.getConditionType());
	    	to.setFormCondition(target.getFormCondition());
	    	to.setIsForce(target.getIsForce());
	    }
	    
		/**
		 * 判断节点1后续节点是否包含节点2。
		 * @param split
		 * @param join
		 * @return
		 */
		public static boolean passThrough(BPMAbstractNode split,BPMAbstractNode join)
		{
			List<BPMTransition> links = split.getDownTransitions();
			if(links==null) return false;
			for (BPMTransition link : links) {
				BPMAbstractNode to = link.getTo();
				if(to instanceof BPMEnd) break;
				if(to.equals(join)) return true;
				if(passThrough(to,join)) return true;
			}
			return false;
		}
		
		//加签 - 并发
	    public static List<String> insertParellel(BPMProcess process, Long workitemId, BPMSeeyonPolicy policy, boolean isFormOperationReadonly) throws EdocException {
	    	User user = AppContext.getCurrentUser();
	        List<String> memAndPolicyName = new ArrayList<String>();
	        try {
	            WorkItem workitem = wapi.getWorkItemById("",workitemId);
	            BPMActivity currentActivity = process.getActivityById(workitem.getActivityId());
	            List downTransitions = currentActivity.getDownTransitions();
	            BPMTransition nextTrans = (BPMTransition) downTransitions.get(0);
	            BPMAndRouter split = null;
	            BPMAndRouter join = null;
	            //查看下一结点是否有split类型�jiedian节点
	            for (int i = 0; i < downTransitions.size(); i++) {
	                BPMTransition trans = (BPMTransition) downTransitions.get(i);
	                if (trans.getTo() instanceof BPMAndRouter) {
	                    BPMAndRouter to = (BPMAndRouter) trans.getTo();
	                    if (to.isStartAnd()) {
	                        split = to;
	                    }
	                }
	            }

	            //如果有split结点，遍历找到join结点
	            if (split != null) {
	                boolean foundJoin = false;
	                BPMAbstractNode node = split;
	                while (!foundJoin) {
	                    BPMTransition trans = (BPMTransition) node.getDownTransitions().get(0);
	                    node = trans.getTo();
	                    if (node instanceof BPMAndRouter) {
	                        BPMAndRouter andNode = (BPMAndRouter) node;
	                        if (!andNode.isStartAnd()) {
	                        	if(split.getParallelismNodeId().equals(andNode.getParallelismNodeId())){
	                        		foundJoin = true;
	                        		join = andNode;
	                        	}
	                        }
	                    }
	                }
	            }


	            //如果没有split结点，新建split和join
	            if (split == null) {
	                String splitId = UUIDLong.longUUID()+"";
	                String joinId = UUIDLong.longUUID()+"";
	                split = new BPMAndRouter(splitId, "split");
	                join = new BPMAndRouter(joinId, "join");
	                split.setStartAnd(true);
	                join.setStartAnd(false);
	                String relevancyId = UUIDLong.longUUID() + "";
	                split.setParallelismNodeId(relevancyId);
	                join.setParallelismNodeId(relevancyId);
	                process.addChild(split);
	                process.addChild(join);
	                
	                process.addNodeToSwimlane(currentActivity.getId(), split);
	                process.addNodeToSwimlane(currentActivity.getId(), join);

	                BPMAbstractNode nextNode = (BPMAbstractNode) ((BPMTransition) downTransitions.get(0)).getTo();
	                //如果后面是结束结点或分支结点，split和join之间不设结点，join直接连到结束结点�
	                if (!((nextNode instanceof BPMHumenActivity) || (nextNode instanceof BPMTimeActivity))) {
	                    BPMTransition trans1 = new BPMTransition(currentActivity, split);
	                    BPMTransition trans2 = new BPMTransition(join, nextNode);
	                    //currentActivity.addDownTransition(trans1);  //wonder whether this statement is necessary
	                    copyCondition(nextTrans, trans2);
	                    process.addLink(trans1);
	                    process.addLink(trans2);
	                    process.removeLink(nextTrans);
	                    currentActivity.removeDownTransition(nextTrans);
	                }
	                //如果后面不是结束结点，将下一结点纳入split/join之中，join之后连接nextNode.nextNode
	                else {
	                    BPMTransition trans1 = new BPMTransition(currentActivity, split);
	                    BPMTransition trans2 = new BPMTransition(join, nextNode);
	                    //BPMTransition trans3 = new BPMTransition(split, join);
	                    //BPMTransition trans4 = new BPMTransition(nextNode, join);
	                    copyCondition(nextTrans, trans2);

	                    process.addLink(trans1);
	                    process.addLink(trans2);
	                    //process.addLink(trans3);
	                    //process.addLink(trans4);

	                    process.removeLink(nextTrans);
	                    //process.removeLink(next2Trans);
	                    currentActivity.removeDownTransition(nextTrans);
	                    //nextNode.removeDownTransition(next2Trans);

	                }
	            }else{
	            	String splitId = UUIDLong.longUUID()+"";
	                String joinId = UUIDLong.longUUID()+"";
	                split = new BPMAndRouter(splitId, "split");
	                join = new BPMAndRouter(joinId, "join");
	                split.setStartAnd(true);
	                join.setStartAnd(false);
	                String relevancyId = UUIDLong.longUUID() + "";
	                split.setParallelismNodeId(relevancyId);
	                join.setParallelismNodeId(relevancyId);

	                process.addChild(split);
	                process.addChild(join);

	                process.addNodeToSwimlane(currentActivity.getId(), split);
	                process.addNodeToSwimlane(currentActivity.getId(), join);
	                
	                BPMAbstractNode nextNode = (BPMAbstractNode) ((BPMTransition) downTransitions.get(0)).getTo();
	                BPMTransition trans1 = new BPMTransition(currentActivity, split);
	                BPMTransition trans2 = new BPMTransition(join, nextNode);

	                copyCondition(nextTrans, trans2);

	                process.addLink(trans1);
	                process.addLink(trans2);

	                process.removeLink(nextTrans);
	                currentActivity.removeDownTransition(nextTrans);
	            }

	            Date now = new Date(System.currentTimeMillis());
	            process.setUpdateDate(now);
	        } catch (BPMException e) {
	        	throw new EdocException("加签并发操作异常", e);
	        }
	        return memAndPolicyName;
	    }
		/**
		 * 取当前节点的父人工节点
		 * @param activity   当前人工节点
		 * @return
		 */
		public static List getParent(BPMActivity activity){
			List humenList = new ArrayList();
	        List<BPMTransition> transitions = activity.getUpTransitions();
	        for (BPMTransition trans : transitions) {
	            BPMAbstractNode parent = trans.getFrom();
	            if (parent.getNodeType() == BPMAbstractNode.NodeType.humen) {
	                humenList.add((BPMHumenActivity) parent);
	            } else if(parent.getNodeType() == BPMAbstractNode.NodeType.start){
	            	humenList.add(parent);
	            } else if (parent.getNodeType() == BPMAbstractNode.NodeType.join || parent.getNodeType() == BPMAbstractNode.NodeType.split) {
	                humenList.addAll(getParent((BPMActivity) parent));
	            }
	        }
	        return humenList;
		}
		
		
	    /**
	     * 
	     * @param affairData
	     * @param itemId
	     * @throws BusinessException
	     */
	    public static void workflowFinish(AffairData affairData, Long itemId) throws BusinessException {
	        User user = AppContext.getCurrentUser();
	        WorkflowBpmContext context = new WorkflowBpmContext();
	        context.setDebugMode(false);
	        //context.setAppName(ModuleType.getEnumByKey(content.getModuleType()).name());
	        context.setAppName(ApplicationCategoryEnum.edoc.name());
	        context.getBusinessData().put("subAppName", GovdocWorkflowTypeEnum.oldedoc.name());
			
	        context.setCurrentWorkitemId(itemId);
	        context.setCurrentUserId(String.valueOf(user.getId()));
	        context.setCurrentUserName(user.getName());
	        context.setCurrentAccountId(String.valueOf(user.getAccountId()));
	        context.setCurrentAccountName("");
	        context.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, affairData);
	        @SuppressWarnings("unchecked")
	        Map<String, String> wfdef = ParamUtil.getJsonDomain("workflow_definition");
	        String processXml = wfdef.get("process_xml");
	        if (processXml != null && !"".equals(processXml.trim())) {
	            context.setProcessXml(processXml);
	        }
	        String readyObjectJSON = wfdef.get("readyObjectJSON");
	        if (readyObjectJSON != null && !"".equals(readyObjectJSON.trim())) {
	            context.setReadyObjectJson(readyObjectJSON);
	        }
	        wapi.finishWorkItem(context);

	    }
	    
	    public static SeeyonPolicy getPolicyByAffair(CtpAffair affair,String processId) throws BusinessException {
	        if (affair == null)
	            return new SeeyonPolicy("collaboration","协同");

	        if(Strings.isNotBlank(affair.getNodePolicy())){
	        	return new SeeyonPolicy(affair.getNodePolicy(), BPMSeeyonPolicy.getShowName(affair.getNodePolicy()));
	        }
	        Long workitemId = affair.getSubObjectId();
	        if (workitemId == null || workitemId == 0) {
	            return null;
	        }
	        String[] result= wapi.getNodePolicyIdAndName("edoc", processId, affair.getActivityId().toString());
	        return new SeeyonPolicy(result[0], result[1]);
	    }

	    
	    /**
	     * 根据affair得到错误提示消息，回退，撤销，取回等
	     * @param affair
	     * @return
	     */
	    public static String getErrorMsgByAffair(CtpAffair affair)
	    {
	        String state = "";
	        String msg = "";
	        if(affair != null) {
	            int forwardMemberFlag = 0;
	            String forwardMember = null;
	            if(Strings.isNotBlank(affair.getForwardMember())){
                	forwardMember = affair.getForwardMember();
                    forwardMemberFlag = 1;
	            }

	            if(affair.isDelete()){
	                try{
	                    state = ResourceUtil.getString("collaboration.state.9.delete");
	                }catch(Exception e){
	                    LOGGER.error(e.getMessage(), e);
	                }
	            }
	            else{
	                switch(StateEnum.valueOf(affair.getState())){
	                    case col_done : state = ResourceUtil.getString("collaboration.state.4.done");
	                    break;
	                    case col_cancel : state = ResourceUtil.getString("collaboration.state.5.cancel");
	                    break;
	                    case col_stepBack : state = ResourceUtil.getString("collaboration.state.6.stepback");
	                    break;
	                    case col_takeBack : state = ResourceUtil.getString("collaboration.state.7.takeback");
	                    break;
	                    case col_competeOver : state = ResourceUtil.getString("collaboration.state.8.strife");
	                    break;
	                    case col_stepStop : state = ResourceUtil.getString("collaboration.state.10.stepstop");
	                    break;
	                    case col_waitSend :
	                        switch(SubStateEnum.valueOf(affair.getSubState())){
	                            case col_waitSend_stepBack:state = ResourceUtil.getString("collaboration.state.6.stepback");break;
	                            case col_waitSend_cancel:state = ResourceUtil.getString("collaboration.state.5.cancel");break;
                            default:
                                break;
	                        }
	                    break;
                    default:
                        break;
	                }
	            }
	            String appName=ResourceUtil.getString("application."+affair.getApp()+".label");
	            if(Strings.isNotBlank(state)){
	                msg = ResourceUtil.getString("collaboration.state.invalidation.alert", affair.getSubject(), state,appName, forwardMemberFlag, forwardMember);
	            }
	        }else{
	            state = ResourceUtil.getString("collaboration.state.9.delete");
	            msg = ResourceUtil.getString("collaboration.state.inexistence.alert", state);
	        }
	        return msg;
	    }   
	    
	    /**
	     * 获取affairData
	     */
	    public static AffairData getAffairData(EdocSummary summary ,User user){
	    	AffairData affairData = new AffairData();
	    	Timestamp now = new Timestamp(System.currentTimeMillis());
//          affairData.setForwardMember(summary.getForwardMember());
	    	affairData.setMemberId(user.getId());
	    	affairData.setFormAppId(summary.getFormAppid());
	    	
	    	affairData.setIsSendMessage(false); //是否发消息
//          affairData.setResentTime(summary.getResentTime());//如协同colsummary
	    	affairData.setState(StateEnum.col_pending.key());//事项状态 - 协同业务中3为待办
	    	//affairData.setSubject(summary.getSubject());//如协同colsummary
	    	affairData.setSubject(Strings.isBlank(summary.getDynamicSubject()) ? summary.getSubject() : summary.getDynamicSubject());//如协同colsummary
	    	affairData.setSubState(SubStateEnum.col_pending_unRead.key());//事项子状态 协同业务中11为协同-待办-未读
	    	affairData.setSummaryAccountId(summary.getOrgAccountId());//如协同colsummary.orgAccountId

	        String contentType = "";
	        if(summary.getFirstBody()!=null){
	        	contentType = summary.getFirstBody().getContentType();
	        }else if(summary.getBodyType()!=null){
	        	contentType=MainbodyType.getEnumByKey(GovdocUtil.getContentType(summary.getBodyType())).name();
	        }
	        if(Strings.isBlank(contentType)){
	            contentType = "HTML";
	        }
	        affairData.setContentType(contentType);
	        affairData.setSender(summary.getStartUserId());
	        affairData.setIsHasAttachment(summary.isHasAttachments());
	        affairData.setModuleType(ApplicationCategoryEnum.edoc.key());
	        if(summary.getEdocType() == 0){
	            affairData.setSubModuleType(ApplicationSubCategoryEnum.old_edocSend.key());
	        }else if(summary.getEdocType() == 1){
	            affairData.setSubModuleType(ApplicationSubCategoryEnum.old_edocRec.key());
	        }else if(summary.getEdocType() == 2){
	            affairData.setSubModuleType(ApplicationSubCategoryEnum.old_edocSign.key());
	        }
	        affairData.setModuleId(summary.getId());//保存进affair表中的object_id
	       
	        affairData.setCreateDate(null==summary.getCreateTime() ? now : summary.getCreateTime());
	       
	        if(Strings.isBlank(summary.getAttachments())){
	            affairData.setIsHasAttachment(false);
	        }else{
	            affairData.setIsHasAttachment(true);
	        }
	        if(summary.getImportantLevel()!=null){
	        	affairData.setImportantLevel(summary.getImportantLevel()); //如协同colsummary
	        }else if(Strings.isNotBlank(summary.getUrgentLevel())){
				affairData.setImportantLevel(Integer.parseInt(summary.getUrgentLevel()));
			}
		  
			//公文的流程期限不应该设置到待办事项affair的处理期限中
	//      if (summary.getDeadline() != null && summary.getDeadline().intValue() > 0) {
	//          affairData.setProcessDeadline(summary.getDeadline());
	//      }
	          
	        if(summary.getTempleteId() != null){
	            TemplateManager templeteManager = (TemplateManager)AppContext.getBean("templateManager");
	            CtpTemplate templete = null;
				try {
					templete = templeteManager.getCtpTemplate(summary.getTempleteId());
					affairData.setTemplateId(summary.getTempleteId());
					if(templete != null){
						affairData.setPerssionAccountId(templete.getOrgAccountId());
					}
				} catch (BusinessException e) {
					LOGGER.error("", e);
				}
	        }
	        else{
	        	affairData.setPerssionAccountId(summary.getOrgAccountId());
	        }
	        
	        
	        //转发公文后发送，需要保存原公文的拟文人id
	        if(Strings.isNotBlank(summary.getForwardMember())){
	            affairData.setForwardMember(summary.getForwardMember());
	        }
			//首页栏目的扩展字段设置--公文文号、发文单位等--start
			Map<String, Object> extParam = new HashMap<String, Object>();
	        extParam.put("edoc_send_doc_mark", summary.getDocMark()); //公文文号
	        extParam.put("edoc_send_send_unit", summary.getSendUnit());//发文单位
	        if(extParam != null){
	        	affairData.setBusinessData(extParam);
	        }
			//首页栏目的扩展字段设置--公文文号、发文单位等--end
			
	    	return affairData;
	    }
	    
	    /**
	     * 获得上传的附件在文单中的描述信息
	     * @param attachmentList
	     * @return
	     */
	    public static String getAttachmentListStr(List<Attachment> attachmentList){
	        StringBuilder str = new StringBuilder();
	        for(int i = 0;i<attachmentList.size();i++){
                Attachment att = attachmentList.get(i);
                String filename = att.getFilename();
                if(filename.lastIndexOf(".")>-1){
                    filename = filename.substring(0,filename.lastIndexOf("."));
                }
                str.append(filename+",");
            }
	        return str.toString();
	    }
	    
	    public static String getAttachmentListStr(long referenceId){
	        AttachmentManager attachmentManager = (AttachmentManager)AppContext.getBean("attachmentManager");
	        List<Attachment> attachmentList = attachmentManager.getByReference(referenceId);
	        //OA-31708 wangdn调用"正文套红模板"拟文，插入附件后，处理人处理时插入附件，此时文单中的附件元素显示了发文的附件和处理意见的附件，且没有顺序
	        List<Attachment> attachmentListCopy = new ArrayList<Attachment>();
	        //只要拟文添加和处理修改添加的附件，不要处理时填意见所添加的附件
	        for(Attachment att: attachmentList){
	            //type=0表示附件 (附件元素上不显示关联文档)
	            if(att.getType() == 0 && att.getSubReference().longValue() == referenceId){
	                attachmentListCopy.add(att);
	            }
	        }
	        return getAttachmentListStr(attachmentListCopy);
	    }
	    
	    /**
	     * 给出附件名称数组，拼出附件字段串
	     * @param filenames
	     * @return
	     */
	    public static String getAttachments(String[] filenames) {
	    	StringBuilder attachments = new StringBuilder();
			if(filenames != null) {
		        for (int i=0; i<filenames.length; i++) {
					String filename = filenames[i];
					int lastIndex = filename.length();
					if(filename.lastIndexOf(".") != -1){
						lastIndex = filename.lastIndexOf(".");
					}
					filename = filename.substring(0, lastIndex);
					if(attachments.length() > 0){
					    attachments.append(attachments);
					    attachments.append("&#x0A;");
					}
					attachments.append(i+1);
                    attachments.append(".");
                    attachments.append(filename);
		    	}
			}
			return attachments.toString();
	    }
	    /**
	     * 剥离附件和关联文档
	     * @param filenames
	     * @param fileTypes
	     * @return
	     * add by libing 
	     */
	    public static String getAttachments(String[] filenames, String[] fileTypes) {
	    	StringBuilder attachments = new StringBuilder();
			if(filenames != null) {
				int index = 1; 
		        for (int i=0; i<filenames.length; i++) {
		        	if("2".equals(fileTypes[i])){
		        		continue;
		        	}
					String filename = filenames[i];
					int lastIndex = filename.length();
					if(filename.lastIndexOf(".") != -1){
						lastIndex = filename.lastIndexOf(".");
					}
					filename = filename.substring(0, lastIndex);
					if(attachments.length() > 0){
					    attachments.append("&#x0A;");
					}
					attachments.append((index++));
                    attachments.append(".");
                    attachments.append(filename);
		    	}
			}
			return attachments.toString();
		}
	    
	    
	    /**
	     * 给出附件集合，拼出附件字段串
	     * @param attachmentList
	     * @return
	     */
	    public static String getAttachments(List<Attachment> attachmentList) {
	        StringBuilder attachments = new StringBuilder();
	        for(int i = 0;i<attachmentList.size();i++) {
                Attachment att = attachmentList.get(i);
                if (att.getType() == 2) {
        			continue;
        		}
                String filename = att.getFilename();
				int lastIndex = filename.length();
				if(filename.lastIndexOf(".") != -1) {
					lastIndex = filename.lastIndexOf(".");
				}
				filename = filename.substring(0, lastIndex);
				if(attachments.length() > 0) {
				    attachments.append("&#x0A;");
				}
				attachments.append((i+1)).append(".").append(filename);
            }
	        return attachments.toString();
	    }
	    
	    public static List<Attachment> getOpinionAttachmentsNotRelationDoc(long referenceId,long subReferenceId) {
	    	AttachmentManager attachmentManager = (AttachmentManager)AppContext.getBean("attachmentManager");
	        List<Attachment> attachmentList = attachmentManager.getByReference(referenceId,subReferenceId);
	        //OA-31708 wangdn调用"正文套红模板"拟文，插入附件后，处理人处理时插入附件，此时文单中的附件元素显示了发文的附件和处理意见的附件，且没有顺序
	        List<Attachment> attachmentListCopy = new ArrayList<Attachment>();
	        //只要拟文添加和处理修改添加的附件，不要处理时填意见所添加的附件
	        for(Attachment att: attachmentList){
	            //type=0表示附件 (附件元素上不显示关联文档)
	            if(att.getType() == 0 && att.getSubReference().longValue() == subReferenceId){
	                attachmentListCopy.add(att);
	            }
	        }
	        return attachmentListCopy;
	    }
	    /**
	     * 给出附件引用ID，查询附件，拼出附件字段串
	     * @param attachmentList
	     * @return
	     */
	    public static String getAttachments(long referenceId) {
	    	AttachmentManager attachmentManager = (AttachmentManager)AppContext.getBean("attachmentManager");
	        List<Attachment> attachmentList = attachmentManager.getByReference(referenceId);
	        //OA-31708 wangdn调用"正文套红模板"拟文，插入附件后，处理人处理时插入附件，此时文单中的附件元素显示了发文的附件和处理意见的附件，且没有顺序
	        List<Attachment> attachmentListCopy = new ArrayList<Attachment>();
	        //只要拟文添加和处理修改添加的附件，不要处理时填意见所添加的附件
	        for(Attachment att: attachmentList){
	            //type=0表示附件 (附件元素上不显示关联文档)
	            if(att.getType() == 0 && att.getSubReference().longValue() == referenceId){
	                attachmentListCopy.add(att);
	            }
	        }
	        return getAttachments(attachmentListCopy);
	    }
	    
	    /**
	     * 是否为V5-G6版本
	     * @return
	     */
	    public static boolean isG6Version(){
	        String isG6=SystemProperties.getInstance().getProperty("edoc.isG6");
	        if("true".equals(isG6)){
	        	return true;
	        }
	        //int productId =  SystemProperties.getInstance().getIntegerProperty("system.ProductId");
	        //if(productId==SYSTEM_PRODUCTID_government || productId==SYSTEM_PRODUCTID_governmentgroup){ //政务单组织和多组织
	        //	return true;
	       // }
	        return false;
	    }

        public static void updateEnumItemRef(HttpServletRequest request)throws Exception {
            com.seeyon.ctp.common.ctpenumnew.manager.EnumManager enumManagerNew = 
                    (com.seeyon.ctp.common.ctpenumnew.manager.EnumManager)AppContext.getBean("enumManagerNew");
            String doc_type = request.getParameter("my:doc_type");
            if(Strings.isNotBlank(doc_type)){
                enumManagerNew.updateEnumItemRef(EnumNameEnum.edoc_doc_type.name(), doc_type);
            }
            String send_type = request.getParameter("my:send_type");
            if(Strings.isNotBlank(send_type)){
                enumManagerNew.updateEnumItemRef(EnumNameEnum.edoc_send_type.name(), send_type);
            }
            String secret_level = request.getParameter("my:secret_level");
            if(Strings.isNotBlank(secret_level)){
                enumManagerNew.updateEnumItemRef(EnumNameEnum.edoc_secret_level.name(), secret_level);
            }
            String urgent_level = request.getParameter("my:urgent_level");
            if(Strings.isNotBlank(urgent_level)){
                enumManagerNew.updateEnumItemRef(EnumNameEnum.edoc_urgent_level.name(), urgent_level);
            }
            String keep_period = request.getParameter("my:keep_period");
            if(Strings.isNotBlank(keep_period)){
                enumManagerNew.updateEnumItemRef(EnumNameEnum.edoc_keep_period.name(), keep_period);
            }
            String unit_level = request.getParameter("my:unit_level");
            if(Strings.isNotBlank(unit_level)){
                enumManagerNew.updateEnumItemRef(EnumNameEnum.edoc_unit_level.name(), unit_level);
            }
        }
        
         
        public static CtpAffair getEdocSenderAffair(Long objectId) throws BusinessException {
            DetachedCriteria criteria = DetachedCriteria
                    .forClass(CtpAffair.class)
                    .add(Restrictions.eq("objectId", objectId))
                    .add(Restrictions.eq("delete", false))
                    .add(Restrictions.in("state",
                            new Object[] { StateEnum.col_sent.key(),
                                    StateEnum.col_waitSend.key() }));
            List<CtpAffair> list = DBAgent.findByCriteria(criteria);
            CtpAffair senderAffair = null;
            if (Strings.isNotEmpty(list)) {
                senderAffair = list.get(0);
            }
            return senderAffair;
        }
        
        /**
         * 设置summary附件标识,修改affair附件标识
         * @param summary
         * @throws BusinessException
         */
        public static void updateAttIdentifier(EdocSummary summary, List<ProcessLog> logs, boolean isUpdate, String action) throws Exception {
        	AttachmentManager attachmentManager = (AttachmentManager)AppContext.getBean("attachmentManager");
        	AffairManager affairManager = (AffairManager)AppContext.getBean("affairManager");
        	boolean hasAtt = attachmentManager.hasAttachments(summary.getId(), summary.getId());
            summary.setHasAttachments(hasAtt);
            String oldAttachmentListStr = summary.getAttachments();
            String attachmentListStr = EdocHelper.getAttachments(summary.getId());
            summary.setAttachments(attachmentListStr);
            
            List<CtpAffair> affairList = affairManager.getAffairs(ApplicationCategoryEnum.edoc, EdocUtil.getSubAppCategoryByEdocType(summary.getEdocType()), summary.getId());
            if(Strings.isNotEmpty(affairList)) {
            	for(CtpAffair affair : affairList) {
            		affair.setIdentifier(IdentifierUtil.update(affair.getIdentifier(), EdocSummary.INENTIFIER_INDEX.HAS_ATTACHMENTS.ordinal(), hasAtt ? '1' : '0'));
            		affairManager.updateAffair(affair);
            	}
            }
            if(isUpdate) {
            	EdocManager edocManager = (EdocManager)AppContext.getBean("edocManager");
            	edocManager.update(summary);
            }
            if(Strings.isNotEmpty(logs)) {
            	ProcessLogManager processLogManager = (ProcessLogManager)AppContext.getBean("processLogManager");
            	processLogManager.insertLog(logs);
            }
            boolean isSendMessage = false;
            if("stepBack".equals(action) || "stepStop".equals(action) || "backToDraft".equals(action)) {
            	isSendMessage = false;
            } else {
	            if((Strings.isBlank(oldAttachmentListStr) && Strings.isNotBlank(attachmentListStr)) 
	                    || (Strings.isNotBlank(oldAttachmentListStr) && Strings.isBlank(attachmentListStr))
	                    ||(Strings.isNotBlank(oldAttachmentListStr) && Strings.isNotBlank(attachmentListStr) && !oldAttachmentListStr.equals(attachmentListStr))) {
	            	isSendMessage = true;
	        	} 
            }
            if(isSendMessage) {
            	UserMessageManager userMessageManager = (UserMessageManager)AppContext.getBean("userMessageManager");
            	EdocMessageHelper.updateAttachmentMessage(affairManager, userMessageManager, orgManager, summary);
            }
        }
        
        
        /**
	 	 * 获取归档的archiveId
	 	 * @param templeteId
	 	 * @param manager
	 	 * @return
         * @throws BusinessException 
	 	 */
	 	public static Long getTempletePrePigholePath(Long templeteId, TemplateManager manager) throws BusinessException{
		 if(templeteId == null){
			 return null;
		 }
		 CtpTemplate templete = manager.getCtpTemplate(templeteId);
		 if(templete != null){
			 EdocSummary summary = (EdocSummary) XMLCoder.decoder(templete.getSummary());
			 if(summary != null){
				 Long archiveId = summary.getArchiveId();
				 if(archiveId != null){
					 return archiveId;
				 }
			 }
		 }
		 return null;
	 }
	 	
	 public static String getAuditorName(User user,long memberId){
	     String auditorName = user.getName();
         try{
        	 orgManager = getOrgManager();
        	 V3xOrgMember member= orgManager.getMemberById(memberId);
             if(member != null){
            	 auditorName = member.getName();
             }
         }catch(Exception e){
             LOGGER.error("查找人员错误", e);
         }
         return auditorName;
	 }
	 	
	 public static String getCompositionStr(String issuserStr,String issuerName){
	     String separator = ResourceBundleUtil.getString("com.seeyon.v3x.common.resources.i18n.SeeyonCommonResources", "common.separator.label");
         if(Strings.isNotBlank(issuserStr)){
            String[] arg = issuserStr.split(separator);
            boolean findName = false;
            for(String name:arg) {
                if(name.equals(issuerName)) {
                    findName = true;
                    break; 
                }
            }
            
            if(findName){
                issuerName=issuserStr;
            }else{
                issuerName=issuserStr+separator + issuerName;
            }
	     }
         return issuerName;
	 }
	 	
	 public static boolean isAddAttachmentByOpinion(HttpServletRequest request,long summaryId){
	     String[] filename = request.getParameterValues("attachment_filename");
         String[] extReference = request.getParameterValues("attachment_extReference");
         /**
          * 当修改附件上传了附件时，filename和extReference的长度是一致的
          * 而意见处上传了附件，filename有值而extReference为空字符串
          */
         boolean isHasAtt = false;
         if(filename!=null&&filename.length>0){
             for(String reference : extReference){
                 if(!String.valueOf(summaryId).equals(reference)){
                     isHasAtt = true;
                     break;
                 }
             }
         }
         return isHasAtt;
	 }
	 
	 public static String getOpinionAttStr(){
	     return "（"+ResourceUtil.getString("collaboration.newcoll.dangfu")+"）";
	 }
	 
	 public static boolean isBindEdocMark(String mark){
	     boolean isBindMark = false;
	     if(Strings.isNotBlank(mark)){
	         String[] marks = mark.split("[|]");
             //不是手填的文号，就为模板绑定的文号
             if(marks != null && marks.length == 4 && !"3".equals(marks[3])){
                 isBindMark = true;
             }
	     }
	     return isBindMark;
	 }
	 
	 public static String exeTemplateMarkJs(String docMark,String docMark2,String serialNo){
	     StringBuffer docMarkByTemplateJs = new StringBuffer("var templateMarkJs = {};");
	     if(isBindEdocMark(docMark)){
	         docMarkByTemplateJs.append("templateMarkJs.docMark = 'true';");
	     }else{
	         docMarkByTemplateJs.append("templateMarkJs.docMark = 'false';");
	     }
	     if(isBindEdocMark(docMark2)){
	         docMarkByTemplateJs.append("templateMarkJs.docMark2 = 'true';");
	     }else{
	         docMarkByTemplateJs.append("templateMarkJs.docMark2 = 'false';");
	     }
	     if(isBindEdocMark(serialNo)){
	         docMarkByTemplateJs.append("templateMarkJs.serialNo = 'true';");
	     }else{
	         docMarkByTemplateJs.append("templateMarkJs.serialNo = 'false';");
	     }
	     return docMarkByTemplateJs.toString();
	 }
	 /**
	 * 显示下级单位意见的排序原则是
	 * 1、按照单位第一条意见的上报时间决定单位间意见的排序。
	 * 2、单位内意见的排序，按时间正序排列
	 */ 
	public static List<EdocOpinion> getFeedBackOptions(List<EdocOpinion> opinions) {
		//先将相同单位的 放在一起
		Map<Long,List<EdocOpinion>> opMap = new HashMap<Long,List<EdocOpinion>>();
		for(EdocOpinion op : opinions){
			Long subEdocId = op.getSubEdocId();//不同的单位 收文id也是不一样的，所以就根据下级收文id来区分不同的单位
			List<EdocOpinion> list = opMap.get(subEdocId);
			if(list == null){
				list = new ArrayList<EdocOpinion>();
			}
			list.add(op);
			opMap.put(subEdocId, list);
		}
		//再从每个单位中找出时间最早的，然后根据每个单位最早的就知道单位排序了
		Set<Long> set = opMap.keySet();
		Iterator<Long> it = set.iterator();
		
		Map<Timestamp,List<EdocOpinion>> timeMap = new HashMap<Timestamp,List<EdocOpinion>>();
		List<Timestamp> timeList = new ArrayList<Timestamp>();
		while(it.hasNext()){
			Long key = it.next();
			List<EdocOpinion> list = opMap.get(key);
			Timestamp initialTime = new Timestamp(System.currentTimeMillis());
			for(EdocOpinion op : list){
				
				Timestamp time = op.getCreateTime();
				if(time.before(initialTime)){
					initialTime = time;
				}
			}
			timeList.add(initialTime);
			timeMap.put(initialTime, list);
		}
		
		//将每个单位最早汇报的意见时间List 进行排序
		Collections.sort(timeList,new Comparator<Timestamp>(){
            public int compare(Timestamp arg0, Timestamp arg1) {
                if(arg0.before(arg1)){
                	return -1;
                }else{
                	return 1;
                }
            }
		});
		List<EdocOpinion> newOpinions = new ArrayList<EdocOpinion>();
		for(Timestamp time : timeList){
			newOpinions.addAll(timeMap.get(time));
		}			
		return newOpinions;
	}

	 
	//流程期限
     public static String getDeadLineName(java.util.Date deadlineDatetime){
         if(null == deadlineDatetime){//无
             return ResourceUtil.getString("collaboration.project.nothing.label");
         }else{
         	return Datetimes.formatDatetimeWithoutSecond(deadlineDatetime);
         }
     }
     
     public static Set<Long> getDeptSenderList(long edocStartUserId,long edocOrgAccountId) {
     	//使用Set过滤重复的人员,如果一个人既在主部门充当收发员，又在副岗所在部门充当收发员，首页只显示一条待办事项。
 		Set<Long> deptList=new HashSet<Long>();
 		try{
 			//发起人在主单位和兼职单位获取公文收发员的方式有所不同。
 			OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
 			V3xOrgMember startMember=orgManager.getMemberById(edocStartUserId);
 			//1、查找兼职部门的公文收发员
 			Map<Long, List<MemberPost>> map=orgManager.getConcurentPostsByMemberId(edocOrgAccountId,edocStartUserId);
 			
 			Set<Long> concurentDepartSet=map.keySet();
 			for(Long deptId:concurentDepartSet){
 				V3xOrgDepartment dept = orgManager.getDepartmentById(deptId);
 				if(dept.getOrgAccountId().equals(edocOrgAccountId)){
 					deptList.add(deptId);
 				}
 			}
 			//2.查找发起人副岗所在部门的公文收发员
 			List<MemberPost> list=startMember.getSecond_post();
 			for(MemberPost memberPost:list){
 				if(memberPost.getOrgAccountId().equals(edocOrgAccountId)){
 					deptList.add(memberPost.getDepId());
 				}
 			}
 			//3。主部门的公文收发员。
 			if(edocOrgAccountId==startMember.getOrgAccountId()){
 				deptList.add(startMember.getOrgDepartmentId());
 			}
 		}catch(Exception e){
 			LOGGER.error("公文交换查找部门收发员出错：", e);
 		}
 		return deptList;
 	}
     
     /**
      * 获取用户在所有单位下的有分发角色的部门
      * @Author      : xuqiangwei
      * @Date        : 2015年1月18日下午11:42:48
      * @param edocStartUserId
      * @return
      */
     public static Set<Long> getDeptSenderList(long edocStartUserId) {
         //使用Set过滤重复的人员,如果一个人既在主部门充当收发员，又在副岗所在部门充当收发员，首页只显示一条待办事项。
         Set<Long> deptList=new HashSet<Long>();
         try{
             //发起人在主单位和兼职单位获取公文收发员的方式有所不同。
             OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
             V3xOrgMember startMember=orgManager.getMemberById(edocStartUserId);
             //1、查找兼职部门的公文收发员
             Map<Long, List<MemberPost>> map=orgManager.getConcurentPostsByMemberId(null,edocStartUserId);
             
             Set<Long> concurentDepartSet=map.keySet();
             for(Long deptId:concurentDepartSet){
                 deptList.add(deptId);
             }
             //2.查找发起人副岗所在部门的公文收发员
             List<MemberPost> list = startMember.getSecond_post();
             for(MemberPost memberPost:list){
                 deptList.add(memberPost.getDepId());
             }
             //3。主部门的公文收发员。
             deptList.add(startMember.getOrgDepartmentId());
         }catch(Exception e){
             LOGGER.error("公文交换查找部门收发员出错：", e);
         }
         return deptList;
     }
     
     /**
      * 获取交换部门和单位分发员
      * @Author      : xuqiangwei
      * @Date        : 2015年1月19日下午2:22:35
      * @param summary
      * @param deptUserId 可以为null，如果为null将取summary.getStartUserId()
      * @return
      * @throws BusinessException
      */
    public static Map<String,Object> getOrgExchangeMembersAndDeptSenders(EdocSummary summary) throws BusinessException{
    	Map<String,Object> map = new HashMap<String,Object>();
    	
    	// 公文所属单位的公文收发员
		
		// 封发节点或者是具有【交换类型】节点权限
		List<V3xOrgMember> memberList = EdocRoleHelper.getUsersByRoleName("AccountGovdocSend",summary.getOrgAccountId());
		
		/* xiangfan 添加  修复 GOV-4911 Start */
		Set<Long> deptSenderList = null;
	    deptSenderList = EdocHelper.getDeptSenderList(summary.getStartUserId(), summary.getOrgAccountId());
		
		Iterator<Long> iterator = deptSenderList.iterator();
		Long deptId;
		StringBuilder deptList= new StringBuilder();
		OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
		while(iterator.hasNext()){
			deptId=(Long)iterator.next();
			V3xOrgDepartment dept=orgManager.getDepartmentById(deptId);
			if(dept!=null){
				if(deptList.length() > 0){
				    deptList.append("|");
				}
				deptList.append(dept.getId())
				        .append(",")
				        .append(dept.getName());
			}
		}
		map.put("memberList", memberList);
		map.put("deptSenderList", deptList);
		return map;
    }
    
    
    /**
	 * 获取能够使用的模板，过滤掉停用的。
	 * @param user     ：用户
	 * @param edocType ：类型（正文/文单）
	 * @param bodyType : Officeword:word正文/Wpsword:wps正文

	 * @return
	 * @throws Exception
	 */
	public static List<EdocDocTemplate> getEdocDocTemplate(Long orgAccountId,User user, String edocType, String bodyType)
			throws Exception {

		List<EdocDocTemplate> list = new ArrayList<EdocDocTemplate>();
		orgAccountId = V3xOrgEntity.VIRTUAL_ACCOUNT_ID;
		String bdType = bodyType == null ? null : bodyType.toLowerCase();
		if (null != edocType && ("edoc".equals(edocType) || "govdoc".equals(edocType))) {//G6 6.1 新公文
			list = edocDocTemplateManager.findGrantedListForTaoHong(orgAccountId,user.getId(), Constants.EDOC_DOCTEMPLATE_WORD,bdType);
		} else if (null != edocType && "script".equals(edocType)) {
			list = edocDocTemplateManager.findGrantedListForTaoHong(orgAccountId,user.getId(), Constants.EDOC_DOCTEMPLATE_SCRIPT, bdType);
		} else {
			list = edocDocTemplateManager.findAllTemplate();
		}

		// 过滤掉停用状态的
		Set<Long> ids = new HashSet<Long>();
		List<EdocDocTemplate> list2 = new ArrayList<EdocDocTemplate>();
		for (EdocDocTemplate t : list) {
			if (t.getStatus() == 1 && !ids.contains(t.getId())){
				list2.add(t);
				ids.add(t.getId());
			}
				
		}

		return list2;
	}
	
	public static String getMemberName(Long id){
        return Functions.showMemberName(id);
    }
	
	//当前处理人信息
	public static String parseCurrentNodesInfo(EdocSummary summary) {
	    return parseCurrentNodesInfo(summary.getCompleteTime(),summary.getCurrentNodesInfo(), Collections.<Long, String> emptyMap());
	}
	
	public static String parseCurrentNodesInfo(EdocSummary summary, Map<Long, String> members) {
	    return parseCurrentNodesInfo(summary.getCompleteTime(),summary.getCurrentNodesInfo(), Collections.<Long, String> emptyMap());
	}
	
	public static String parseCurrentNodesInfo(Timestamp completeTime,String nodeInfo, Map<Long, String> members) {
		if(null == completeTime ){//流程未结束
			if(nodeInfo == null){
				return "";
			}
			StringBuilder currentNodsInfo = new StringBuilder();
			
			if(Strings.isNotBlank(nodeInfo) && !"null".equals(nodeInfo.toLowerCase())){
				String[] nodeArr=nodeInfo.split(";"); //人员ID&节点权限;人员ID&节点权限
				
				int num = 0;
				int count=0;
				for(String node:nodeArr){
					if(Strings.isNotBlank(node)&&count<20){//显示20个处理人信息
						String userIdStr=nodeArr[num];
						
						Long memberId = Long.valueOf(userIdStr);
						
						String memberName = members.get(memberId);
						if(memberName == null){
						    memberName = getMemberName(memberId);
						}
						
						if(memberName == null || currentNodsInfo.toString().indexOf(memberName)!=-1){//去重复
							count--;
						}else{
								if(currentNodsInfo.length()==0){
									currentNodsInfo.append(memberName);
								}else{
									currentNodsInfo.append("、"+memberName);
								}
							
						}
					}
					count++;
					num++;
				}
			}else{
				return "";
			}
			if("null".equals(currentNodsInfo.toString().toLowerCase()))return "";
			return currentNodsInfo.toString();
		}
		else{//流程结束
			return ResourceUtil.getString("collaboration.list.finished.label");
		}
	}
	
	public static String parseCurrentNodesInfo(Date completeTime, String currentNodesInfo, Map<Long, String> members) {
		if(completeTime == null) {//流程未结束
			String cninfo = currentNodesInfo;
			StringBuilder currentNodsInfo = new StringBuilder();
			
			if(Strings.isNotBlank(cninfo) && !"null".equals(cninfo.toLowerCase())){
				String[] nodeArr=cninfo.split(";"); //人员ID&节点权限;人员ID&节点权限
				int count=0;
				for(String node:nodeArr){
					if(Strings.isNotBlank(node)&&count<2){//显示两个处理人信息
						//String[] nArr=node.split("&");
						//String userIdStr=nArr[0];
						String userIdStr=nodeArr[count];
						
						Long memberId = Long.valueOf(userIdStr);
						
						String memberName = members.get(memberId);
						if(memberName == null){
						    memberName = getMemberName(memberId);
						}
						
						if(memberName == null || currentNodsInfo.toString().indexOf(memberName)!=-1){//去重复
							count--;
						}else{
							//String policy=nArr[1];//节点权限
							//知会节点不算待办
							//if(Strings.isNotBlank(policy)&&!"zhihui".equals(policy)){
								if(currentNodsInfo.length()==0){
									currentNodsInfo.append(memberName);
								}else{
									currentNodsInfo.append("、"+memberName);
								}
							//}else{
							//	count--;
							//}
						}
					}
					count++;
				}
			}else{
				return "";
			}
			if("null".equals(currentNodsInfo.toString().toLowerCase()))return "";
			return currentNodsInfo.toString();
		}
		else{//流程结束
			return ResourceUtil.getString("collaboration.list.finished.label");
		}
	}
	
	//去掉Affair列表中的当前处理人信息
	public static void deleteAffairsNodeInfo(EdocSummary summary,
			List<CtpAffair> cancelAffairs) {
		String cninfo=summary.getCurrentNodesInfo();
		int index=0;
		String[] nodeArr=cninfo.split(";");
		
		for(CtpAffair a:cancelAffairs){
			String cnStr=a.getMemberId()+"";
			
			for(int i=0;i<nodeArr.length;i++){
				String node = nodeArr[i];
				if(Strings.isNotBlank(node)){
					if(node.equals(cnStr)){
						index = i;
						break;
					}
				}
			}
		}
		StringBuffer str = new StringBuffer();
		for(int i=0;i<nodeArr.length;i++){
			if(i!=index){
				if(i!=nodeArr.length-1){
					str.append(nodeArr[i]+";");
				}else{
					str.append(nodeArr[i]);
				}
				
			}
		}
		summary.setCurrentNodesInfo(str.toString());
	}
	
	
	
	//原来有当前处理人信息，追加
	public static void setCurrentNodesInfo(EdocSummary colSummary, String cnStr) {
		String cninfo=colSummary.getCurrentNodesInfo();
		if(Strings.isNotBlank(cninfo)){//原来有当前处理人信息，追加
			colSummary.setCurrentNodesInfo(cninfo+";"+cnStr);
		}else{
			colSummary.setCurrentNodesInfo(cnStr);
		}
	}
	
	public static void updateCurrentNodesInfo(EdocSummary summary,boolean isUpdateToDb) throws Exception{
		if(summary.getGovdocType() != 0){
  		  	com.seeyon.apps.govdoc.helper.GovdocHelper.updateCurrentNodesInfo(summary, true);
  		  	return;
  	  	}
		Map<String,Object> map = new LinkedHashMap<String,Object>();
		
		map.put("objectId", summary.getId());
		map.put("state", StateEnum.col_pending.key());
		map.put("delete", false);
		map.put("app", ApplicationCategoryEnum.edoc.getKey());
		//防止收文转收文的情况，ObjectId没变
		if(summary.getGovdocType().intValue() == 0){//老公文
			if(summary.getEdocType()==0){//edocSend(19), //发文 edocRec(20),	//收文 edocSign(21),	//签报	
				map.put("subApp", ApplicationSubCategoryEnum.old_edocSend.getKey());
			}else if(summary.getEdocType()==1){//收文
				map.put("subApp", ApplicationSubCategoryEnum.old_edocRec.getKey());
			}else{//签报
				map.put("subApp", ApplicationSubCategoryEnum.old_edocSign.getKey());
			}
		}else{
			if(summary.getEdocType()==0){//edocSend(19), //发文 edocRec(20),	//收文 edocSign(21),	//签报	
				map.put("subApp", ApplicationSubCategoryEnum.edoc_fawen.getKey());
			}else if(summary.getEdocType()==1){//收文
				map.put("subApp", ApplicationSubCategoryEnum.edoc_shouwen.getKey());
			}else{//签报
				map.put("subApp", ApplicationSubCategoryEnum.edoc_qianbao.getKey());
			}
		}
		
		map.put("notInform","notInform");//非知会
		
		FlipInfo fi = new FlipInfo();
        fi.setNeedTotal(false);
        fi.setSize(10);
		
		List<CtpAffair> affairs = affairManager.getByConditions(fi, map);
		
		int count = 0;
        StringBuilder current = new StringBuilder();
        if(Strings.isNotEmpty(affairs)){
        	for(int k = 0;k < affairs.size() && count < 10;k++){
            	CtpAffair cf = affairs.get(k);
            	if("zhihui".equals(cf.getNodePolicy())||"inform".equals(cf.getNodePolicy())
            	        || (Integer.valueOf(SubStateEnum.col_pending_specialBack.getKey())).equals(cf.getSubState())){
            		continue;
            	}
            	count++;
            	current.append(cf.getMemberId());
            	if(k!=affairs.size()-1){
            		current.append(";");
            	}
            }
        }
        summary.setCurrentNodesInfo(current.toString());
        if(isUpdateToDb){
        	edocManager.update(summary);
        }
	}
	
	
	/**
	 * 判断是否有政务收文登记功能，注意：是政务版中定义的登记，在productFeature.xml中定义
	 * @return  true:有；false:无
	 */
	public static boolean hasEdocRegister(){
		return "true".equals(SystemProperties.getInstance().getProperty("edoc.hasEdocRegister"));
	}
	public static boolean hasEdocCategory(){
		return "true".equals(SystemProperties.getInstance().getProperty("edoc.hasEdocCategory"));
	}
	
	/**
	 * 获得我代理的所有人id
	 * @return
	 */
	public static List<Long> getAgentToList(Long userId){
		List<Long> agentToList = new ArrayList<Long>();
		List<AgentModel> agentModelList = MemberAgentBean.getInstance().getAgentModelList(userId);
		List<AgentModel> edocAgent = new ArrayList<AgentModel>();
  		
  		if(agentModelList != null && !agentModelList.isEmpty()){//代理有值
  			java.util.Date now = new java.util.Date();
  			for(AgentModel agentModel : agentModelList){
    			if(agentModel.isHasEdoc()){
    				if(agentModel.getStartDate().before(now) && agentModel.getEndDate().after(now))
    					edocAgent.add(agentModel);
    			}
  	    	}
  		}
  		if(edocAgent!=null&&edocAgent.size()>0){
  			for(AgentModel agent : edocAgent){
  				agentToList.add(agent.getAgentToId());
  			}
  		}
  		return agentToList;
	}
	
    /**
     * 给已有的枚举添加一条枚举项
     * 
     * @Author : xuqiangwei
     * @Date : 2014年10月27日下午8:53:10
     * @param ctpEnum
     *            : 原有枚举
     * @param label
     *            : 新增项显示内容
     * @param value
     *            : 新增想值
     * @param location : 加入位置， 0 - 首位， 1 - 末尾(或者其他不等于0的数字)
     */
    public static void addEnumItem2Enum(
            com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean ctpEnum, String label,
            String value, int location) {
        
        if (ctpEnum == null) {
            ctpEnum = new com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean();
        }
        List<com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem> ctpEnumItems = ctpEnum.getItems();
        
        if(ctpEnumItems == null){
            ctpEnumItems = new ArrayList<com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem>();
        }
        
        com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem ctpEnumItem = 
                new com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem();
        ctpEnumItem.setLabel(label);
        ctpEnumItem.setValue(value);// 新增一条空白下拉选项
        ctpEnumItem.setOutputSwitch(1);//设置新增的这条数据为启用状态
        ctpEnumItem.setState(1);//设置新增的这条数据为启用状态
        if(location == 0){
            ctpEnumItems.add(0, ctpEnumItem);
        }else {
            ctpEnumItems.add(ctpEnumItem);
        }
    }
    
    /**
     * 将Byte转换成16进制字符串
     * @Author      : xuqiangwei
     * @Date        : 2014年11月9日上午1:43:45
     * @param b
     * @return
     */
    public static String byte2hex(byte[] b) // 二进制转字符串
    {
        StringBuffer sb = new StringBuffer();
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1) {
                sb.append("0" + stmp);
            } else {
                sb.append(stmp);
            }

        }
        return sb.toString();
    }

    /**
     * 将16进制字符串转换成byte数组
     * @Author      : xuqiangwei
     * @Date        : 2014年11月9日上午1:44:17
     * @param str
     * @return
     */
    public static byte[] hex2byte(String str) { // 字符串转二进制
        if (str == null)
            return null;
        str = str.trim();
        int len = str.length();
        if (len == 0 || len % 2 == 1)
            return null;
        byte[] b = new byte[len / 2];
        try {
            for (int i = 0; i < str.length(); i += 2) {
                b[i / 2] = (byte) Integer.decode("0X" + str.substring(i, i + 2)).intValue();
            }
            return b;
        } catch (Exception e) {
            return null;
        }
    }
   
    public static String getListElementByElementFiledName(String elementFiledName,Long accountId,String itemValue,Locale local, String resource){
    	EnumManager metadataManager = (EnumManager)AppContext.getBean("enumManagerNew");
    	EdocElementManager edocElementManager = (EdocElementManager)AppContext.getBean("edocElementManager");
    	EdocElement listElement =  edocElementManager.getByFieldName(elementFiledName, accountId);
    	String elementName = "";
		if(null != listElement && listElement.getStatus() != 0 && listElement.getMetadataId()!=null){
			CtpEnumBean listMeta = metadataManager.getEnum(listElement.getMetadataId());
			if(null!=listMeta){
				String listLabel = listMeta.getItemLabel(itemValue) ;
				elementName = ResourceBundleUtil.getString(resource, local, listLabel);
			}
		}
		return elementName;
    }
    
    /**
     * 获取公文文号的单位简称
     * @Author      : xuqiangwei
     * @Date        : 2015年2月7日上午1:58:04
     * @param accountId 不用添加简称的单位ID
     * @param markModel 文号对象
     * @return
     */
    public static String getEdocMarkDispalyName(Long accountId, EdocMarkModel markModel){
        
        String markAccountShortName = "";
        if(markModel != null){
            Long markDomainId = markModel.getDomainId();
            if(!accountId.equals(markDomainId) && markDomainId != null){
                //需要显示外单位文号的单位简称
                OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
                try {
                    V3xOrgAccount markAccount = orgManager.getAccountById(markDomainId);
                    if(markAccount != null){
                        markAccountShortName = "(" + markAccount.getShortName() + ")";
                    }
                } catch (BusinessException e) {
                    LOGGER.error("解析外单位公文文号单位简称报错", e);
                }
            }
        }
        return markAccountShortName;
    }
    
    /**
     * 获取正文类型
     * @Author      : xuqw
     * @Date        : 2015年5月22日上午10:58:28
     * @param bodyType
     * @return
     */
    public static int getBodyTypeKey(String bodyType){
        int key = 0;
        if("HTML".equals(bodyType)){
            key = MainbodyType.HTML.getKey();
        }else if("OfficeWord".equals(bodyType)){
            key = MainbodyType.OfficeWord.getKey();
        }else if("OfficeExcel".equals(bodyType)){
            key = MainbodyType.OfficeExcel.getKey();
        }else if("Pdf".equals(bodyType)) {
            key = MainbodyType.Pdf.getKey();
        }
        //OA-38148 【PC，公文】做正文格式为wps文字的公文模版，调用会报错
        else if("WpsWord".equals(bodyType)){
            key = MainbodyType.WpsWord.getKey();
        }else if("WpsExcel".equals(bodyType)){
            key = MainbodyType.WpsExcel.getKey();
        }
        return key;
    }
    
}
