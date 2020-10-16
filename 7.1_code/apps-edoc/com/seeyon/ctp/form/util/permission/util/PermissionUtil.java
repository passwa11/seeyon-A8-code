package com.seeyon.ctp.form.util.permission.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;

import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.edoc.bo.EdocSummaryBO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.constants.ProductEditionEnum;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.bo.PermissionOperation;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.po.PagePermission;
import com.seeyon.ctp.form.util.permission.factory.PermissionFatory;
import com.seeyon.ctp.form.util.permission.factory.PermissionLoad;
import com.seeyon.ctp.form.util.permission.vo.PermissionNewVO;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Strings;

public class PermissionUtil {
	public static String replacePerm(String workflowNodesInfo) {
		Pattern p = Pattern.compile("\\(([^\\)]+)");
			Matcher matcher = p.matcher(workflowNodesInfo);
			if (matcher.find() && matcher.groupCount() >= 1){
				String permStr = matcher.group(1);
				if (!ResourceUtil.getString("node.policy."+permStr).equals(("node.policy."+permStr))) {
					workflowNodesInfo = workflowNodesInfo.replace(permStr, ResourceUtil.getString("node.policy."+permStr));
				}
			}
		return workflowNodesInfo;
	}
	
	public static CtpEnumBean getCtpEnumBySubApp(String sub_app) throws BusinessException {
		EnumManager em = (EnumManager) AppContext.getBean("enumManagerNew");
	    PagePermission permission = PermissionFatory.getPermBySubApp(sub_app);
	    Map<String, CtpEnumBean> ems = em.getEnumsMap(permission.getApplicationCategoryEnum());
	    CtpEnumBean nodePermissionPolicy = ems.get(permission.getCategorty());
		return nodePermissionPolicy;
	}
	
	public static void removeSomePermission(String isEdoc, String permissionName,List<PermissionOperation> metadata1,List<PermissionOperation> metadata2) {
		StringBuilder metaStr1 = new StringBuilder(),metaStr2 = new StringBuilder();
		if (EnumNameEnum.edoc_rec_permission_policy.name().equals(isEdoc)) {
			metaStr2.append("EdocExchangeType");
		}else if (EnumNameEnum.edoc_send_permission_policy.name().equals(isEdoc)) {
			metaStr1.append("TurnRecEdoc");
		}else if (EnumNameEnum.edoc_qianbao_permission_policy.name().equals(isEdoc)) {			
			metaStr2.append("EdocExchangeType");
			metaStr1.append("TurnRecEdoc");			
		}else if (EnumNameEnum.edoc_new_change_permission_policy.name().equals(isEdoc)) {
			metaStr1.append("TurnRecEdoc,Cancel,Infom,TanstoPDF,TransToOfd,Forward,JointlyIssued,UpdateForm,EdocTemplate,WordNoChange,SpecifiesReturn");
			metaStr2.append("FaDistribute,Cancel,EdocExchangeType,SpecifiesReturn");
		}else if (EnumNameEnum.edoc_new_send_permission_policy.name().equals(isEdoc)||EnumNameEnum.edoc_new_qianbao_permission_policy.name().equals(isEdoc)) {
			metaStr1.append("TurnRecEdoc,Forward,WordNoChange");
			metaStr2.append("Distribute,ReSign,EdocExchangeType");
			if(EnumNameEnum.edoc_new_qianbao_permission_policy.name().equals(isEdoc)){
				metaStr1.append(",JointlyIssued,TanstoPDF");
				metaStr2.append(",FaDistribute,TanstoPDF");
			}
			if("faxing".equals(permissionName)){
				metaStr1.append(",Cancel");
				metaStr2.append(",Cancel");
			}
			//if (permissionName.equals("qianfa") || permissionName.equals("shenhe")) {
				metaStr1.append(",UpdateForm");
			//}
		}else if (EnumNameEnum.edoc_new_rec_permission_policy.name().equals(isEdoc)) {
			metaStr1.append("Forward,JointlyIssued,WordNoChange,TanstoPDF,TransToOfd");
			metaStr2.append("Distribute,ReSign,FaDistribute,EdocExchangeType");
			//if (permissionName.equals("huiqian")||permissionName.equals("qianfa") || permissionName.equals("shenhe")) {
				metaStr1.append(",UpdateForm");
			//}
		}
		if ("zhihui".equals(permissionName)) {
			metaStr1.append(",TurnRecEdoc");
		}
		boolean is_gov_only = (Boolean)(SysFlag.sys_isGovVer.getFlag());
        if(is_gov_only && ProductEditionEnum.getCurrentProductEditionEnum().equals(ProductEditionEnum.government)){
        	metaStr1.append(",JointlyIssued");
        }
		
		removeItem(metadata1, metaStr1.toString());
		removeItem(metadata2, metaStr2.toString());
	}
    
    public static List<PermissionOperation> removeItem(List<PermissionOperation> itemList,String key){
        String[] keyArr = key.split(",");
        List<String> keyList = Arrays.asList(keyArr);
    	if(CollectionUtils.isNotEmpty(itemList)){
			for(Iterator<PermissionOperation> item = itemList.iterator();item.hasNext();){
				PermissionOperation po = item.next();
				if (CollectionUtils.isNotEmpty(keyList) && keyList.contains(po.getKey())) {
					item.remove();
				}
			}
        }
        return itemList;
    }
	
	public static String getCateGoryName(boolean isTemplete, String categoryName) throws BusinessException {
		String subAppName =AppContext.getRawRequest().getParameter("appName");
		String formType=AppContext.getRawRequest().getParameter("formTypeEnum");
		FormApi4Cap3 formApi4Cap3 = (FormApi4Cap3)AppContext.getBean("formManager");
		FormBean fb = formApi4Cap3.getEditingForm();
		if(Strings.isNotBlank(formType) && !"0".equals(formType)){
			try {
				categoryName = PermissionFatory.getPermByFormType(Integer.valueOf(formType)).getCategorty();	
			}catch (Exception e) { // 督办
				if (ApplicationCategoryEnum.collaboration.name().equals(subAppName)) {
					subAppName = formType;
				}
				categoryName = PermissionFatory.getCategoryNameByAppName(formType);
			}
		}else if (isTemplete && fb != null) {
			categoryName = PermissionFatory.getPermByFormType(fb.getGovDocFormType()).getCategorty();
			AppContext.getRawRequest().setAttribute("govdocFormType", fb.getGovDocFormType());
		}else {
			categoryName = PermissionFatory.getCategoryNameByAppName(subAppName);
		}
		return categoryName;
	}
	
	public static String getParentAppName(String subAppName){
		if (ApplicationCategoryEnum.govdocSend.name().equals(subAppName)||ApplicationCategoryEnum.govdocRec.name().equals(subAppName)||
        		ApplicationCategoryEnum.govdocExchange.name().equals(subAppName)||ApplicationCategoryEnum.govdocSign.name().equals(subAppName)) {
        	return ApplicationCategoryEnum.collaboration.name();
		}
		return subAppName;
	}
	
	public static List<ConfigItem> getNewPermList(Long orgAccountId,ConfigManager configManager) {
		Collection<PermissionNewVO> pList = PermissionLoad.getMapPermVOs().values();
		List<ConfigItem> permList = new ArrayList<ConfigItem>();
		for(PermissionNewVO pVo:pList){
			if((Boolean)SysFlag.sys_isG6S.getFlag() && pVo.getSubApp() == ApplicationSubCategoryEnum.edoc_qianbao){
				continue;
			}
			List<ConfigItem> edocList = configManager.listAllConfigByCategory(pVo.getPagePermission().getCategorty(), orgAccountId);
			Collections.sort(edocList);
			permList.addAll(edocList);
		}
		return permList;
	}
	
	/**
	 * 得到节点权限所属单位ID<br>
	 * 原则：1、系统模板，取模板所在单位ID<br>
	 * 		2、自由协同，取协同所在单位ID
	 * @return
	 * @throws BusinessException 
	 */
    public static Long getFlowPermAccountId(ColSummary colSummary) throws BusinessException {
    	Long senderId = colSummary.getStartMemberId();
    	OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
        V3xOrgMember sender = orgManager.getMemberById(senderId);
    	TemplateManager templateManager = (TemplateManager) AppContext.getBean("templateManager");
    	Long flowPermAccountId = sender.getOrgAccountId(); 
		if(colSummary.getTempleteId() != null){
			CtpTemplate templete;
			templete = templateManager.getCtpTemplate(colSummary.getTempleteId());
			if(templete != null){
				flowPermAccountId = templete.getOrgAccountId();
			}
		}
		else{
			if(colSummary.getOrgAccountId() != null){
				flowPermAccountId = colSummary.getOrgAccountId();
			}
		}
    	return flowPermAccountId;
    }
	/**
	 * 得到节点权限所属单位ID<br>
	 * 原则：1、系统模板，取模板所在单位ID<br>
	 * 		2、自由协同，取协同所在单位ID
	 * @return
	 * @throws BusinessException 
	 */
    public static Long getFlowPermAccountId(EdocSummaryBO summary) throws BusinessException {
    	Long senderId = summary.getStartMemberId();
    	OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
        V3xOrgMember sender = orgManager.getMemberById(senderId);
    	TemplateManager templateManager = (TemplateManager) AppContext.getBean("templateManager");
    	Long flowPermAccountId = sender.getOrgAccountId(); 
		if(summary.getTempleteId() != null){
			CtpTemplate templete;
			templete = templateManager.getCtpTemplate(summary.getTempleteId());
			if(templete != null){
				flowPermAccountId = templete.getOrgAccountId();
			}
		}
		else{
			if(summary.getOrgAccountId() != null){
				flowPermAccountId = summary.getOrgAccountId();
			}
		}
    	return flowPermAccountId;
    }
}
