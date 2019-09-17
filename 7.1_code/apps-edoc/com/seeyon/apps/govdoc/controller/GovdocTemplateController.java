package com.seeyon.apps.govdoc.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.json.JSONException;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.ai.api.AIApi;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.doc.bo.DocResourceBO;
import com.seeyon.apps.edoc.bo.GovdocTemplateDepAuthBO;
import com.seeyon.apps.edoc.bo.TemplateMarkInfo;
import com.seeyon.apps.edoc.enums.EdocEnum.TempleteType;
import com.seeyon.apps.govdoc.helper.GovdocContentHelper;
import com.seeyon.apps.govdoc.helper.GovdocWorkflowHelper;
import com.seeyon.apps.govdoc.manager.GovdocDocTemplateManager;
import com.seeyon.apps.govdoc.manager.GovdocTemplateManager;
import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.apps.govdoc.vo.GovdocBodyVO;
import com.seeyon.apps.project.api.ProjectApi;
import com.seeyon.apps.project.bo.ProjectBO;
import com.seeyon.cap4.form.api.FormApi4Cap4;
import com.seeyon.cap4.form.bean.FormViewBean;
import com.seeyon.cdp.CDPAgent;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ProductEditionEnum;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.content.ContentConfig;
import com.seeyon.ctp.common.content.ContentViewRet;
import com.seeyon.ctp.common.content.mainbody.CtpContentAllBean;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyStatus;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseTemplateRole;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.po.template.CtpTemplateAuth;
import com.seeyon.ctp.common.po.template.CtpTemplateCategory;
import com.seeyon.ctp.common.shareMap.V3xShareMap;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.template.manager.ProcessInsHandler;
import com.seeyon.ctp.common.template.manager.TemplateCategoryManager;
import com.seeyon.ctp.common.template.manager.TemplateInsManager;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.template.util.TemplateUtil;
import com.seeyon.ctp.common.template.vo.TemplateBO;
import com.seeyon.ctp.common.template.vo.TemplateCategory;
import com.seeyon.ctp.common.template.vo.TemplateCategoryComparator;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormAuthViewBean;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormFieldBean;
import com.seeyon.ctp.form.manager.GovdocTemplateDepAuthManager;
import com.seeyon.ctp.form.po.CtpTemplateRelationAuth;
import com.seeyon.ctp.form.po.GovdocTemplateDepAuth;
import com.seeyon.ctp.form.util.Enums.FormType;
import com.seeyon.ctp.form.util.FormConstant;
import com.seeyon.ctp.form.util.FormUtil;
import com.seeyon.ctp.form.util.SelectPersonOperation;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.ReqUtil;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.engine.enums.BPMSeeyonPolicySetting;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.ctp.workflow.wapi.WorkflowFormDataMapManager;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.util.EdocUtil;

@SuppressWarnings("deprecation")
public class GovdocTemplateController extends BaseController {

	private static Log LOG = CtpLogFactory.getLog(GovdocTemplateController.class);
	
	private GovdocTemplateManager 				govdocTemplateManager;
	private WorkflowApiManager    				wapi ;
    private TemplateManager 					templateManager;
	private PermissionManager 					permissionManager;
	private GovdocDocTemplateManager			govdocDocTemplateManager;
	private OrgManager							orgManager;
	private SuperviseManager					superviseManager;
	private AttachmentManager					attachmentManager;
	private DocApi 								docApi;
	private AIApi 								aiApi;
	private ProjectApi 							projectApi;
	private CAPFormManager 						capFormManager = null;
	private FormApi4Cap3					    formApi4Cap3;
	private FormApi4Cap4                        formApi4Cap4;
	private MainbodyManager						ctpMainbodyManager;
	private TemplateCategoryManager 			templateCategoryManager;
    private AffairManager                		affairManager;
    private TemplateInsManager 					templateInsManager;
    private static EnumManager    				enumManagerNew;
	
    private Map<ApplicationCategoryEnum,ProcessInsHandler> processInsHandlerMap = new HashMap<ApplicationCategoryEnum,ProcessInsHandler>();
    
    public void init(){
    	 Map<String, ProcessInsHandler> handlers = AppContext.getBeansOfType(ProcessInsHandler.class);
         for (String key : handlers.keySet()) {
         	ProcessInsHandler handler = handlers.get(key);
         	try {
         		processInsHandlerMap.put(handler.getAppEnum(), handler);
 			} catch (BusinessException e) {
 				LOG.error("", e);
 			}
         }
    }
    
	/**
     *  新建协同模版布局页面
     */
	public ModelAndView lianheTemplateSet(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	Map<String, Object> map = new HashMap<String,Object>();//categoryType=401, categoryId=null, app=401
    	map.put("orgAccountId", AppContext.currentAccountId());
    	map.put("moduleType",  ApplicationCategoryEnum.govdocSend.getKey());
    	ModelAndView mav = new ModelAndView("govdoc/template/lianheTemplateSet");
    	List<CtpTemplate> templateList = govdocTemplateManager.findLianHeTemplateList(map);
    	govdocTemplateManager.setLianheTemplateAttr(templateList);
    	mav.addObject("lists", templateList);
        return mav;
    }

    /**
     *  新建协同模版布局页面新公文
     */
    public ModelAndView templateSysMgrNew(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 模版类型 0 协同模版， 1公文模版
        int categoryType = ReqUtil.getInt(request, "categoryType");
        String categoryId = request.getParameter("categoryId");
        if (StringUtil.checkNull(categoryId)) {
            categoryId = String.valueOf(categoryType);
        }
        ModelAndView mav = new ModelAndView("govdoc/template/templateSysMgrNew");
        List<CtpTemplateCategory> categories = new ArrayList<CtpTemplateCategory>();
        boolean is_gov_only = (Boolean)(SysFlag.sys_isGovVer.getFlag());
        if(ProductEditionEnum.getCurrentProductEditionEnum().equals(ProductEditionEnum.governmentgroup)){
        	is_gov_only = false;
        }else if (ProductEditionEnum.getCurrentProductEditionEnum().equals(ProductEditionEnum.government)) {
            formApi4Cap3.singleSetShelf(); 
		}
        Long accountId = AppContext.currentAccountId();
        mav.addObject("curAccountId",accountId);
        if(ProductEditionEnum.getCurrentProductEditionEnum().getValue().equals(ProductEditionEnum.enterprise.getValue())
        		|| ProductEditionEnum.getCurrentProductEditionEnum().getValue().equals(ProductEditionEnum.a6.getValue()) ) {
        	is_gov_only = true;
        }
        mav.addObject("govOnly", is_gov_only);
        if (ModuleType.collaboration.getKey() == categoryType) {
            categories = getCollaborationTemplate();
        } else if (ModuleType.edoc.getKey() == categoryType || ModuleType.govdocRec.getKey() == categoryType
                || ModuleType.govdocSend.getKey() == categoryType || ModuleType.govdocSign.getKey() == categoryType) {
    		User user = AppContext.getCurrentUser();
    		//增加一个公文的标识符，前面页签选多维的时候要用
    		mav.addObject("isGovdoc",true);
    		//TODO：待添加角色resourceCode后放开代码验证
        	//权限判断,没有公文的管理员权限直接返回
//    		boolean hasAuth=AppContext.hasResourceCode("F07_edocSystem");//管理员权限
//    		boolean hasAuth1=AppContext.hasResourceCode("F07_edocSystem1");//管理员账号登录
//    		boolean hasAuth2=AppContext.hasResourceCode("F20_manage");//管理员账号登录
//    		if(!hasAuth && !hasAuth1 && !hasAuth2){
//    			return null;
//    		}
            categories = getEdocTemplateNew();
        }else if(ModuleType.info.getKey() == categoryType){
        	
        }
        request.setAttribute("isAdmin", isAccountAdmin());
        request.setAttribute("categoryType", categoryType);
        request.setAttribute("categoryId", categoryId);
        List<TemplateCategory> templateCategories = new ArrayList<TemplateCategory>(categories.size());
        for (CtpTemplateCategory category : categories) {
            templateCategories.add(new TemplateCategory(category));
        }
        request.setAttribute("fftemplateTree", templateCategories);
        return mav;
    }
    private boolean isAccountAdmin() throws BusinessException {
        return AppContext.getCurrentUser().isAdministrator();
    }
    /**
     *  新建协同模版分类页面新公文模板
     */
    @SuppressWarnings("deprecation")
	public ModelAndView showSystemCategoryNew(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String parentCategoryId = request.getParameter("parentId");
        String canAdmin = request.getParameter("canAdmin");
        if (!StringUtil.checkNull(parentCategoryId)) {
            StringBuffer options = govdocTemplateManager.getCategory2HTMLNew(AppContext.getCurrentUser()
                    .getLoginAccount(), null);
            request.setAttribute("categoryHTML", options.toString());
        }
        request.setAttribute("parentId", parentCategoryId);
        request.setAttribute("canAdmin", canAdmin);
        return new ModelAndView("govdoc/template/systemCategoryNew");
    }


    private List<CtpTemplateCategory> getCollaborationTemplate() throws BusinessException {
        long orgAccountId = AppContext.currentAccountId();
        List<CtpTemplateCategory> categories = templateManager.getCategoryByAuth(orgAccountId);
        List<CtpTemplateCategory> formCategories = templateManager.getCategoryByAuth(orgAccountId,ModuleType.form.ordinal());
        Long collaborationType = Long.parseLong(String.valueOf(ModuleType.collaboration.getKey()));
        Long formType = Long.parseLong(String.valueOf(ModuleType.form.getKey()));
        for (CtpTemplateCategory ctpTemplateCategory : formCategories) {
            if(categories.contains(ctpTemplateCategory)){
                continue;
            }
            categories.add(ctpTemplateCategory);
        }
        for(int count = categories.size()-1; count > -1; count --){//双重防护,避免出现两个协同模板
        	CtpTemplateCategory ctpTemplateCategory = categories.get(count);
        	if(ctpTemplateCategory.getId() ==1L){
        		categories.remove(count);
        		break;
        	}
        }
        // 协同模版类型根结点
        CtpTemplateCategory root = new CtpTemplateCategory(collaborationType,
                ResourceUtil.getString("collaboration.template.category.type.0"), null);
        if (root != null) {
            categories.add(root);
        }
        for (CtpTemplateCategory ctpTemplateCategory : categories) {
            if (formType.equals(ctpTemplateCategory.getParentId())) {
                ctpTemplateCategory.setParentId(collaborationType);
            }
        }
        Collections.sort(categories, new TemplateCategoryComparator());
        return categories;
    }

	/**
     *  编辑协同模版分类新公文
     */
    public ModelAndView editSystemCategoryNew(HttpServletRequest request,HttpServletResponse response) throws Exception {
        String categoryId = request.getParameter("categoryId");
        if (!StringUtil.checkNull(categoryId)) {
            CtpTemplateCategory category = templateManager.getCtpTemplateCategory(Long.parseLong(categoryId));
            if(category != null){
                StringBuffer options = govdocTemplateManager.getCategory2HTMLNew(AppContext.getCurrentUser()
                        .getLoginAccount(), Long.valueOf(category.getId()));
                request.setAttribute("categoryHTML", options.toString());
                request.setAttribute("parentId", category.getParentId());
                Map<String, Object> resMap = new HashMap<String, Object>();
                resMap.put("id", categoryId);
                resMap.put("name", category.getName());
                resMap.put("sort", category.getSort());
                resMap.put("description", category.getDescription());
                resMap.put("disabled", "disabled");//修改分类页面 无法修改上级分类
                String[] auths = getCategoryAuths(category);
                request.setAttribute("authtxt", auths[1]);
                request.setAttribute("authvalue", auths[0]);
                request.setAttribute("ffcategoryForm", resMap);
                request.setAttribute("categoryType", category.getType());
                //当前登陆人是否是单位管理员
                User user = AppContext.getCurrentUser();
                request.setAttribute("canAdmin", user.isAdministrator());
            }
        }
        return new ModelAndView("govdoc/template/systemCategoryNew");
    }
    public String[] getCategoryAuths(CtpTemplateCategory c) throws BusinessException{
        String[] result = new String[2];
        Set<CtpTemplateAuth> auths = templateManager.getCategoryAuths(c);
        if(auths != null){
            StringBuilder sbValue = new StringBuilder();
            StringBuilder sbTxt = new StringBuilder();
            int i = 0;
            for (CtpTemplateAuth ctpTemplateAuth : auths) {
                if(i != 0){
                    sbValue.append(",");
                    sbTxt.append(",");
                }
                sbValue.append(ctpTemplateAuth.getAuthType()).append("|").append(ctpTemplateAuth.getAuthId());
                V3xOrgMember member = orgManager.getMemberById(ctpTemplateAuth.getAuthId());
                if(null != c.getOrgAccountId() && !c.getOrgAccountId().equals(member.getOrgAccountId())){
                	sbTxt.append(member.getName()+"("+orgManager.getAccountById(member.getOrgAccountId()).getShortName()+")");
                }else{
                	sbTxt.append(member.getName());
                }
                i++;
            }
            result[0] = sbValue.toString();
            result[1] = sbTxt.toString();
        }
        return result;
    }

    /**
	 * 公文个人模板 保存
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView saveTemplate(HttpServletRequest request,HttpServletResponse response) throws Exception {
		govdocTemplateManager.saveGovDocTemplate();
		return null;
		
	}
	
    private List<CtpTemplateCategory> getEdocTemplateNew() throws BusinessException {
        long orgAccountId = AppContext.currentAccountId();
        List<CtpTemplateCategory> categories = new ArrayList<CtpTemplateCategory>();
        List<CtpTemplateCategory> govdocRec = this.templateManager.getCategory(orgAccountId,Integer.parseInt(String.valueOf(ModuleType.govdocRec.getKey())));
        List<CtpTemplateCategory> govdocSend = this.templateManager.getCategory(orgAccountId,Integer.parseInt(String.valueOf(ModuleType.govdocSend.getKey())));
        List<CtpTemplateCategory> govdocSign = this.templateManager.getCategory(orgAccountId,Integer.parseInt(String.valueOf(ModuleType.govdocSign.getKey())));
        categories.addAll(govdocRec);
        categories.addAll(govdocSend);
        categories.addAll(govdocSign);
        // 公文模版类型根结点
        Long edocType = Long.parseLong(String.valueOf(ModuleType.edoc.getKey()));
        CtpTemplateCategory edocNode = new CtpTemplateCategory(edocType,
                ResourceUtil.getString("template.categorytree.edoctemplate.label"), null);
        edocNode.setSort(0);
        categories.add(edocNode);
        
        //
        Long edocSendType = Long.parseLong(String.valueOf(ModuleType.govdocSend.getKey()));
        CtpTemplateCategory govdocSendNode = new CtpTemplateCategory(edocSendType,"发文模板", edocType);
        govdocSendNode.setSort(0);
        categories.add(govdocSendNode);
        // 
        Long edocRecType = Long.parseLong(String.valueOf(ModuleType.govdocRec.getKey()));
        CtpTemplateCategory govdocRecNode = new CtpTemplateCategory(edocRecType,"收文模板", edocType);
        govdocRecNode.setSort(0);
        categories.add(govdocRecNode);
        
        Long edoSignType = Long.parseLong(String.valueOf(ModuleType.govdocSign.getKey()));
        CtpTemplateCategory govdocSignNode = new CtpTemplateCategory(edoSignType,"签报模板", edocType);
        govdocSignNode.setSort(0);
        categories.add(govdocSignNode);
        
        Collections.sort(categories, new Comparator<CtpTemplateCategory>() {
			@Override
			public int compare(CtpTemplateCategory o1, CtpTemplateCategory o2) {
				return o1.getSort().compareTo(o2.getSort());
			}
		});
        
        return categories;
    }
    
    
	private void setWorkFlowInfo(Long template) throws BPMException, BusinessException{
    	//回填工作流需要设置的东西
		ContentViewRet contextwf =  (ContentViewRet) AppContext.getRequestContext("contentContext");
		if(contextwf != null &&null != templateManager.getCtpTemplate(template).getWorkflowId()){
		    contextwf.setWfProcessId(templateManager.getCtpTemplate(template).getWorkflowId().toString());
		    EnumManager em = (EnumManager) AppContext.getBean("enumManagerNew");
	        Map<String, CtpEnumBean> ems = em.getEnumsMap(ApplicationCategoryEnum.collaboration);
	        CtpEnumBean nodePermissionPolicy = ems.get(EnumNameEnum.col_flow_perm_policy.name());
		    String workflowNodesInfo= wapi.getWorkflowNodesInfo(contextwf.getWfProcessId(), ModuleType.collaboration.name(), nodePermissionPolicy);
		    contextwf.setWorkflowNodesInfo(workflowNodesInfo);
		    AppContext.putRequestContext("contentContext", contextwf);
		}
    }
	
    /**
     * 获取协同的默认节点
     * @return
     * @throws BusinessException
     */
    private Map<String,String> getColDefaultNode(Long orgAccountId) throws BusinessException {
    	Map<String,String> tempMap = new HashMap<String,String>();
    	//默认节点权限
        PermissionVO permission = this.permissionManager.getDefaultPermissionByConfigCategory(EnumNameEnum.col_flow_perm_policy.name(),orgAccountId);
        String defaultNodeName = "";
        String defaultNodeLable = "";
        if (permission != null) {
        	defaultNodeName = permission.getName();
        	defaultNodeLable = permission.getLabel();
        }
        tempMap.put("defaultNodeName", defaultNodeName);
        tempMap.put("defaultNodeLable", defaultNodeLable);
        return tempMap;
    }
	//公文表单
	public ModelAndView saveTemplate2Cache(HttpServletRequest request,
            HttpServletResponse response) throws BusinessException{
		//基础信息
		Map baseInfo = ParamUtil.getJsonDomain("baseInfo");
		//流程信息
		Map processCreate = ParamUtil.getJsonDomain("processCreate");

		Map dataMap = new HashMap();
		dataMap.put("baseInfo", baseInfo);
		dataMap.put("processCreate", processCreate);
//		String isNew = request.getParameter("isNew");
//		String approveType =  request.getParameter("approveType");
//		dataMap.put("isNew",isNew);
//		dataMap.put("approveType",approveType);
		dataMap.put("isNew","");
		Map saveformbind2List = govdocTemplateManager.saveTemplate2Cache(dataMap);
//		sendAffair( dataMap, request, baseInfo, response, processCreate);

		return null;
	}
	
    public ModelAndView editTemplatePage(HttpServletRequest request,HttpServletResponse response) throws BusinessException, JSONException{
    	
    	ModelAndView mav = new ModelAndView("govdoc/form/govdocProcessIns");
    	String defId = request.getParameter("defId");
    	String  editFlag = request.getParameter("editFlag");//是否可以编辑
		if(Strings.isEmpty(editFlag)){
			mav.addObject("editFlag", true);
		}else{
			mav.addObject("editFlag", editFlag);
		}
    	String fbatchId = request.getParameter("fbatchId");
    	String templateId = request.getParameter("templateId");
    	String moduleType = request.getParameter("moduleType");
    	String govdocModuleType = request.getParameter("govdocModuleType");
    	mav.addObject("moduleType", moduleType);
    	mav.addObject("hasAIPlugin", AppContext.hasPlugin("ai"));
    	mav.addObject("hasCDPPlugin", CDPAgent.isEnabled());
    	
    	String templateNameShow = ResourceUtil.getString("template.page.processIns.tn.js");
    	if(Strings.isNotBlank(moduleType) && "1".equals(moduleType)){//协同模板
    		String isNew = request.getParameter("isNew");
    		String _subject = request.getParameter("defaultSubject");
    		mav.addObject("defaultSubject", _subject);
    		if(Strings.isNotBlank(request.getParameter(_subject))){
    			templateNameShow = _subject;
    		}
    		if(Strings.isNotBlank(request.getParameter("categoryName"))){
    			//空格的Ascii 值为160  trim不掉。
    			mav.addObject("categoryName", request.getParameter("categoryName").replaceAll("\\u00A0",""));
    		}
    		mav.addObject("deadlineLabel", request.getParameter("deadlineLabel"));
    		mav.addObject("auth_txt", request.getParameter("auth_txt"));
    		mav.addObject("showSupervisors", request.getParameter("showSupervisors"));
    		mav.addObject("templateId", templateId);
    		if(null != V3xShareMap.getReserved("templateData"+AppContext.currentUserId() + templateId)){
    			CtpTemplate c = (CtpTemplate)V3xShareMap.getReserved("templateData"+AppContext.currentUserId() + templateId);
    			mav= bulidPageData(mav,c);
    			templateNameShow = c.getSubject();
    		}else{//修改模板第一次的时候进来 走该分支
    			CtpTemplate ctpTemplate = templateManager.getCtpTemplate(Long.valueOf(templateId));
    			if(null != ctpTemplate){
    				ctpTemplate = templateManager.addOrgIntoTempalte(ctpTemplate);
    				mav= bulidPageData(mav,ctpTemplate);
    				templateNameShow = ctpTemplate.getSubject();
    			}
    		}
    		mav.addObject("appName", ApplicationCategoryEnum.collaboration.name());
    		mav = commonPermissAttribute(mav,moduleType);
    		mav.addObject("templateNameShow", templateNameShow);
    	}else if(Strings.isNotBlank(moduleType) && 
    			("19".equals(moduleType) ||"20".equals(moduleType) || "21".equals(moduleType))){
    		moduleType = govdocModuleType;
    		String openWinId = request.getParameter("openWinId");
    		
    		String _subject = request.getParameter("defaultSubject");
    		mav.addObject("defaultSubject", _subject);
    		if(Strings.isNotBlank(request.getParameter(_subject))){
    			templateNameShow = _subject;
    		}
    		mav.addObject("deadlineLabel", request.getParameter("deadlineLabel"));
    		mav.addObject("categoryName", ResourceUtil.getString("templete.category.type."+ moduleType));
    		mav.addObject("auth_txt", request.getParameter("auth_txt"));
    		mav.addObject("showSupervisors", request.getParameter("showSupervisors"));
    		mav.addObject("templateId", templateId);
    		mav.addObject("wendanId",request.getParameter("wendanId"));
    		mav.addObject("openWinId",openWinId);
    		if(null != V3xShareMap.getReserved("templateData"+AppContext.currentUserId() + openWinId)){
    			CtpTemplate c = (CtpTemplate)V3xShareMap.getReserved("templateData"+AppContext.currentUserId() + openWinId);
    			mav= bulidPageData(mav,c);
    			templateNameShow = c.getSubject();
    		}else{//修改模板第一次的时候进来 走该分支
    			if(Strings.isNotBlank(templateId)){
    				CtpTemplate ctpTemplate = templateManager.getCtpTemplate(Long.valueOf(templateId));
    				if(null != ctpTemplate){
    					ctpTemplate = templateManager.addOrgIntoTempalte(ctpTemplate);
    					mav= bulidPageData(mav,ctpTemplate);
    					templateNameShow = ctpTemplate.getSubject();
    				}
    			}
    		}
    		mav.addObject("appName", ApplicationCategoryEnum.valueOf(Integer.valueOf(moduleType)).name());
    		mav = commonPermissAttribute(mav,moduleType);
    		mav.addObject("templateNameShow", templateNameShow);
    	}else{
    		mav.addObject("moduleType", "2");
    		boolean cap4Flag = Strings.isNotBlank(request.getParameter("cap4Flag")) && "1".equals(request.getParameter("cap4Flag"));
    		mav.addObject("templateNameShow", templateNameShow);
    		if(cap4Flag){
    			mav.addObject("cap4Flag","1");
    			mav = templatePageCAP4(mav,defId,fbatchId,templateId);
    		}else{
    			mav = templatePageCAP3(mav,defId,fbatchId,templateId);
    		}

    		mav.addObject("appName", ApplicationCategoryEnum.valueOf(Integer.valueOf(govdocModuleType)).name());
    	}
    	
    	//是否存在关联项目 relationProject
    	boolean relationProjectFlag = false;
    	if(AppContext.hasPlugin("project") ){
    		relationProjectFlag = true;
    	}
    	mav.addObject("relationProjectFlag",relationProjectFlag);
    	boolean templateCanUse = false;
    	if(Strings.isNotBlank(templateId)){
    		templateCanUse = templateManager.templateCanUse(Long.valueOf(templateId));
    	}
    	mav.addObject("templateCanUse",templateCanUse);
    	
    	mav = setBelongOrgDefaultValue(mav);
		boolean exchangeTemplateDepOrg = "true".equals(SystemProperties.getInstance().getProperty("govdoc.exchangeTemplateDepOrg")) ? true : false;
		mav.addObject("exchangeTemplateDepOrg", exchangeTemplateDepOrg);
    	return mav;
    }
    
    public ModelAndView templateBeanList(HttpServletRequest request,
            HttpServletResponse response) throws BusinessException {
    	ModelAndView mav = new ModelAndView("govdoc/form/govdocFormBindList");
    	boolean cap4Flag = Strings.isNotBlank(request.getParameter("cap4Flag")) && "1".equals(request.getParameter("cap4Flag"));
    	mav.addObject("cap4Flag",request.getParameter("cap4Flag"));
    	FlipInfo ff = new FlipInfo();
    	int govdocModuleType = ApplicationCategoryEnum.govdocSend.getKey();
    	if(cap4Flag){
    		
    		Long formId = ReqUtil.getLong(request, "formId");
            com.seeyon.cap4.form.bean.FormBean cap4fb = formApi4Cap4.checkAndLoadForm2Session(formId, "");
            if (cap4fb.getBind().getExtraAttr("batchId") == null) {
            	cap4fb.getBind().putExtraAttr("batchId", UUIDLong.longUUID());
            	Map query = new HashMap();
    			query.put("contentTemplateId",String.valueOf(cap4fb.getId()));
    			query.put("delete", false);
    			ff = templateManager.getformBindList(null, query);
    		    mav.addObject("fbatchId",cap4fb.getBind().getExtraAttr("batchId"));
    		    cap4fb.getBind().setFlowTemplateList(ff.getData());
            }else{
            	ff = new FlipInfo();
            	sortWithModifyData(cap4fb.getBind().getFlowTemplateList());
    			ff.setData(cap4fb.getBind().getFlowTemplateList());
    			mav.addObject("fbatchId",cap4fb.getBind().getExtraAttr("batchId"));
            }
            List<TemplateBO> tVO = templateManager.covertTemplatePO2VO(ff.getData());
    		ff.setData(tVO);
    		request.setAttribute("ffformBindList",ff);
            com.seeyon.cap4.form.bean.FormAuthViewBean startAuth = cap4fb.getNewFormAuthViewBean();
            com.seeyon.cap4.form.bean.FormAuthViewBean nomorlAuth = cap4fb.getUpdateAndShowFormAuthViewBeans().get(0);
            
            mav.addObject("startOperation", startAuth.getId());
            mav.addObject("nomorlOperation", nomorlAuth.getId());
            mav.addObject("formBean", cap4fb);
    		mav.addObject("redTemplete",cap4fb.hasRedTemplete());
    	}else{
    		
    		FormBean fb = capFormManager.getEditingForm();
        	
        	if(fb.getBind().getExtraAttr("batchId")==null){
        		fb.getBind().putExtraAttr("batchId", UUIDLong.longUUID());
    			Map query = new HashMap();
    			query.put("contentTemplateId",String.valueOf(fb.getId()));
    			query.put("delete", false);
    			ff = templateManager.getformBindList(null, query);
    			List<CtpTemplate> templateList = ff.getData();
    			if(!templateList.isEmpty()){
				    for (CtpTemplate ctpTemplate : templateList) {
	    				Map<String, Object> params = new HashMap<String, Object>();
	    				params.put("moduleId", ctpTemplate.getId());
	    				List<CtpContentAll> contents = ctpMainbodyManager.getContentList(params);
	    				CtpContentAll govdocContentAll = null;
	    				if(contents!=null&&!contents.isEmpty()){
	    					for(int i = 0;i < contents.size() ; i++){
	    						CtpContentAll c = contents.get(i);
		    					if(c.getContentType() != MainbodyType.FORM.getKey()){
		    						if(i == 0 && contents.size() > 1){
		    							govdocContentAll = contents.get(1);
		    						}
		    						govdocContentAll = c;
		    					}
	    					}
	    				}
	    				//模板绑定的正文
				    	ctpTemplate.putExtraAttr("govdocContentAll",govdocContentAll);
				    	GovdocTemplateDepAuthManager govdocTemplateDepAuthManager = (GovdocTemplateDepAuthManager) AppContext.getBean("govdocTemplateDepAuthManager");
				    	List<GovdocTemplateDepAuth> depAuthList = govdocTemplateDepAuthManager.findByTemplateId(ctpTemplate.getId());
				    	List<GovdocTemplateDepAuthBO> result = new ArrayList<GovdocTemplateDepAuthBO>();
				    	for (GovdocTemplateDepAuth govdocTemplateDepAuth : depAuthList) {
				    		result.add(GovdocUtil.toGovdocTemplateDepAuthBO(govdocTemplateDepAuth));
						}
						ctpTemplate.putExtraAttr("depAuthList", result);
				    }
    			}
    		    mav.addObject("fbatchId",fb.getBind().getExtraAttr("batchId"));
    		    fb.getBind().setFlowTemplateList(templateList);
    		}else{
    			ff = new FlipInfo();
    			sortWithModifyData(fb.getBind().getFlowTemplateList());
    			ff.setData(fb.getBind().getFlowTemplateList());
    			mav.addObject("fbatchId",fb.getBind().getExtraAttr("batchId"));
    		}
    		List<TemplateBO> tVO = templateManager.covertTemplatePO2VO(ff.getData());
    		ff.setData(tVO);
    		request.setAttribute("ffformBindList",ff);
    		FormAuthViewBean startAuth = fb.getNewFormAuthViewBean();
    		FormAuthViewBean nomorlAuth = fb.getUpdateAndShowFormAuthViewBeans().get(0);
    		if (fb.getGovDocFormType() == FormType.govDocSendForm.getKey()) {
    			govdocModuleType = ApplicationCategoryEnum.govdocSend.getKey();
    		} else if (fb.getGovDocFormType() == FormType.govDocReceiveForm.getKey()) {
    			govdocModuleType = ApplicationCategoryEnum.govdocRec.getKey();
    		} else if (fb.getGovDocFormType() == FormType.govDocExchangeForm.getKey()) {
    			govdocModuleType = ApplicationCategoryEnum.govdocExchange.getKey();
    		} else if (fb.getGovDocFormType() == FormType.govDocSignForm.getKey()) {
    			govdocModuleType = ApplicationCategoryEnum.govdocSign.getKey();
    		}
    		mav.addObject("govdocModuleType", govdocModuleType);
    		mav.addObject("startOperation", startAuth.getId());
    		mav.addObject("nomorlOperation", nomorlAuth.getId());
    		mav.addObject("formBean", fb);
    		mav.addObject("redTemplete",fb.hasRedTemplete());
    	}
    	
        PermissionVO vo = permissionManager.getDefaultPermissionByConfigCategory("col_flow_perm_policy", AppContext.currentAccountId());
        mav.addObject("defaultPolicyId", vo.getName());
        mav.addObject("defaultPolicyName", vo.getLabel());
		List<ProjectBO> projectSummaries = new ArrayList<ProjectBO>();
        if(AppContext.hasPlugin("project")){
        	projectSummaries = projectApi.findProjectsByAccountId(AppContext.currentAccountId());
        }
		mav.addObject("project", projectSummaries);
		mav.addObject("productId",SystemProperties.getInstance().getProperty("system.ProductId"));
		boolean exchangeTemplateDepOrg = "true".equals(SystemProperties.getInstance().getProperty("govdocConfig.exchangeTemplateDepOrg")) ? true : false;
		mav.addObject("exchangeTemplateDepOrg", exchangeTemplateDepOrg);
    	return mav;
    }
    
    public ModelAndView bulidPageData(ModelAndView mav, CtpTemplate c) throws BusinessException{
		//高级信息
		mav.addObject("template", c);
		mav = CommonOperation(mav, c);
		if(null != c.getBelongOrg()){
        	V3xOrgAccount accountById = orgManager.getAccountById(c.getBelongOrg());
        	if(null != accountById){
        		mav.addObject("belongOrgFB","Account|"+c.getBelongOrg());
        		mav.addObject("belongOrgShow",accountById.getName());
        	}else{
        		mav.addObject("belongOrgFB","Department|" +  c.getBelongOrg());
        		V3xOrgDepartment departmentById = orgManager.getDepartmentById(c.getBelongOrg());
        		if(null != departmentById){
        			mav.addObject("belongOrgShow",departmentById.getName());
        		}
        	}
        }
		//RACI数据回填
        if(Strings.isNotBlank(c.getResponsible())){
        	String[] raciFillbackMessage = getRACIFillbackMessage(c.getResponsible());
        	mav.addObject("r4show", raciFillbackMessage[0]);
        	mav.addObject("r4db", raciFillbackMessage[1]);
        	mav.addObject("r4fb", raciFillbackMessage[2]);
        }
        if(Strings.isNotBlank(c.getAuditor())){
        	String[] raciFillbackMessage = getRACIFillbackMessage(c.getAuditor());
        	mav.addObject("a4show", raciFillbackMessage[0]);
        	mav.addObject("a4db", raciFillbackMessage[1]);
        	mav.addObject("a4fb", raciFillbackMessage[2]);
        }
        if(Strings.isNotBlank(c.getConsultant())){
        	String[] raciFillbackMessage = getRACIFillbackMessage(c.getConsultant());
        	mav.addObject("c4show", raciFillbackMessage[0]);
        	mav.addObject("c4db", raciFillbackMessage[1]);
        	mav.addObject("c4fb", raciFillbackMessage[2]);
        }
        if(Strings.isNotBlank(c.getInform())){
        	String[] raciFillbackMessage = getRACIFillbackMessage(c.getInform());
        	mav.addObject("i4show", raciFillbackMessage[0]);
        	mav.addObject("i4db", raciFillbackMessage[1]);
        	mav.addObject("i4fb", raciFillbackMessage[2]);
        }
        if(Strings.isNotBlank(c.getCoreUseOrg())){
        	String[] raciFillbackMessage = getRACIFillbackMessage(c.getCoreUseOrg());
        	mav.addObject("coreUseOrg4show", raciFillbackMessage[0]);
        	mav.addObject("coreUseOrg4db", raciFillbackMessage[1]);
        	mav.addObject("coreUseOrg4fb", raciFillbackMessage[2]);
        }
		//流程信息
        if(c.getId() == null){
        	mav.addObject("siwfRule",c.getExtraAttr("wfRule"));
        	//流程信息
        	mav.addObject("processXml", c.getExtraAttr("processXml"));
        	mav.addObject("process_id", c.getExtraAttr("process_id"));
        	mav.addObject("process_event", c.getExtraAttr("process_event"));
        }else{//修改模板的时候第一次进来
        	if(null != c.getWorkflowId()){//排除格式模板
        		mav.addObject("siwfRule", wapi.getWorkflowRuleInfo("",c.getWorkflowId().toString()));
        	}
        }
        if(null != c.getMemberId()){
        	V3xOrgMember bm = orgManager.getMemberById(c.getMemberId());
        	String bmName = bm.getName();
        	if(bm.getIsAdmin()){
				bmName = orgManager.getAccountById(bm.getOrgAccountId()).getName()+"单位管理员";
			}
        	
        	mav.addObject("createMemberName", bmName);
        }
	    return mav;
    }
    
    /**
	 * 公文模板绑定文号界面
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ModelAndView bindMark(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("govdoc/form/bind/bindMark");
		String templateId = ReqUtil.getString(request, "templateId", "");
		String bindMarkValue = ReqUtil.getString(request, "bindMarkValue", "");
		if (Strings.isNotBlank(bindMarkValue)) {
			String[] bindMarkValues = bindMarkValue.split(",");
			for(int i=0; i<bindMarkValues.length; i++) {
				String[] markValues = bindMarkValues[i].split("[|]");
				
				TemplateMarkInfo tMarkObj = new TemplateMarkInfo();
				tMarkObj.setMarkType(Integer.parseInt(markValues[0]));
				tMarkObj.setMarkDefId(Long.parseLong(markValues[1]));
				tMarkObj.setWordNo(markValues.length < 3 ? "" : markValues[2]);
				if("0".equals(markValues[0])) {//绑定公文文号
					mav.addObject("docMarkObj", tMarkObj);
				} else if("1".equals(markValues[0])) {//绑定内部编号
					mav.addObject("serialNoObj", tMarkObj);
				} else if("2".equals(markValues[0])) {//绑定签收编号
					mav.addObject("signMarkObj", tMarkObj);
				}
			}
		} else if (Strings.isNotBlank(templateId) && !"undefined".equals(templateId)) {//公文模板绑定文号
			CtpTemplate template = formApi4Cap3.getEditingForm().getBind().getFlowTemplate(Long.parseLong(templateId));
			List<TemplateMarkInfo> tMarkBindList = (List<TemplateMarkInfo>) XMLCoder.decoder(template.getBindMarkInfo());
			if(Strings.isNotEmpty(tMarkBindList)) {
				for(TemplateMarkInfo tMarkObj : tMarkBindList) {
					if(tMarkObj.getMarkType().intValue() == 0) {
						mav.addObject("docMarkObj", tMarkObj);
					} else if(tMarkObj.getMarkType().intValue() == 1) {
						mav.addObject("serialNoObj", tMarkObj);
					} else if(tMarkObj.getMarkType().intValue() == 2) {
						mav.addObject("signMarkObj", tMarkObj);
					}
				}
			}
		}
		Map<Integer, List<TemplateMarkInfo>> markMap = govdocTemplateManager.getFormBindMarkList(AppContext.getCurrentUser());
		boolean hasDocMark = false;
		boolean hasSerailNo = false;
		boolean hasSignMark = false;
		if(markMap.containsKey(0)) {
			hasDocMark = true;
		}
		if(markMap.containsKey(1)) {
			hasSerailNo = true;
		}
		if(markMap.containsKey(2)) {
			hasSignMark = true;
		}
		mav.addObject("hasDocMark", hasDocMark);
		mav.addObject("hasSerailNo", hasSerailNo);
		mav.addObject("hasSignMark", hasSignMark);
		mav.addObject("docMarkList", markMap.get(0));
		mav.addObject("serialNoList", markMap.get(1));
		mav.addObject("signMarkList", markMap.get(2));
		return mav;
	}
    
    private String[] getRACIFillbackMessage(String str){
    	String[] fb = new String[3];
    	String forShow = "";
    	String forDB = "";
    	String forFb= "";
    	if(Strings.isNotBlank(str)){
    		String[] split = str.split("[|]");
    		for(int a= 0; a < split.length ;a ++){
    			if(a == split.length -1){
    				forShow += split[a].split("_")[1];
    				forFb += split[a].split("_")[2] + "|" +split[a].split("_")[0];
    			}else{
    				forShow += split[a].split("_")[1] + "、";
    				forFb += split[a].split("_")[2] + "|" +split[a].split("_")[0] +",";
    			}
    		}
    	}
    	forDB = str;
    	fb[0] = forShow;
    	fb[1] = forDB;
    	fb[2] = forFb;
    	return fb;
    }
    
    private ModelAndView commonPermissAttribute(ModelAndView mav,String moduleType) throws BusinessException{
    	String defaultName = "";
		 if(moduleType.equals(String.valueOf(ModuleType.edocSend.getKey()))){
			 defaultName = EnumNameEnum.edoc_send_permission_policy.name();
		 }else if(moduleType.equals(String.valueOf(ModuleType.edocRec.getKey()))){
			 defaultName = EnumNameEnum.edoc_rec_permission_policy.name();
		 }else if(moduleType.equals(String.valueOf(ModuleType.edocSign.getKey()))){
			 defaultName = EnumNameEnum.edoc_qianbao_permission_policy.name();
		 }else{
			 defaultName = "col_flow_perm_policy";
		 }
		 PermissionVO vo = permissionManager.getDefaultPermissionByConfigCategory(defaultName, AppContext.currentAccountId());
		 mav.addObject("defaultPolicyId", vo.getName());
		 mav.addObject("defaultPolicyName", vo.getLabel());
		 return mav;
    }
    
    private ModelAndView templatePageCAP4(ModelAndView mav,String defId,String fbatchId,String templateId) 
    		throws BusinessException{
    	
    	com.seeyon.cap4.form.bean.FormBean cap4fb = formApi4Cap4.checkAndLoadForm2Session(Long.valueOf(defId), "");
    	boolean hasAIPlugin = AppContext.hasPlugin("ai");
    	boolean hasCDPPlugin = CDPAgent.isEnabled();
    	if(hasAIPlugin && hasCDPPlugin){//有AI插件才获取表单数据
    		List<com.seeyon.cap4.form.bean.FormFieldBean> fieldBeanList = cap4fb.getAllFieldBeans();
    		List<Map<String,String>> beanList = new ArrayList<Map<String,String>>();
    		for(com.seeyon.cap4.form.bean.FormFieldBean bean:fieldBeanList){
    			if("DECIMAL".equals(bean.getFieldType())){
    				Map<String,String> beanJSON = new HashMap<String, String>();
    				beanJSON.put("tableName", bean.getOwnerTableName());
    				beanJSON.put("formName", cap4fb.getFormName());
    				beanJSON.put("fieldName", bean.getName());
    				beanJSON.put("fieldNameDisplay","（主表）" +  bean.getDisplay());
    				beanList.add(beanJSON);
    			}
    		}
    		mav.addObject("decimalFieldList", JSONUtil.toJSONString(beanList));
    	}
    	mav.addObject("defaultSubject", cap4fb.getFormName());
    	if(Strings.isNotBlank(templateId)){//修改模板
    		CtpTemplate template = cap4fb.getBind().getFlowTemplate(Long.valueOf(templateId));
    		//组装页面信息用于修改回填到页面
    		mav.addObject("defaultSubject", template.getSubject());
    		if(hasAIPlugin){//有AI插件才获取表单数据
    			if(hasCDPPlugin) {
    				String processMonitorJSON = (String)template.getExtraAttr(FormConstant.FLOW_TEMPLATE_EXT_PROCESS_MONITOR);
    				//加载流程监控配置信息
    				if(Strings.isNotBlank(processMonitorJSON)){
    					mav.addObject("monitorArray", processMonitorJSON);
    				}else{
    					processMonitorInfo(mav,templateId);
    				}
    			}
    			setAIDealCondition(mav, template);
    		}
    		//表单授权
    		Object auth = template.getExtraAttr("authList");
            if (auth == null) {
                //修改时读取授权信息
                template.putExtraAttr("authList", templateManager.getCtpTemplateAuths(template.getId(), template.getModuleType()));
                auth = template.getExtraAttr("authList");
            }
    		List<CtpTemplateAuth> authList = (List<CtpTemplateAuth>) auth;
            StringBuilder ids = new StringBuilder("");
            String names = "";
            if (authList.size() > 0) {
                for (CtpTemplateAuth moduleAuth : authList) {
                    ids.append(moduleAuth.getAuthType()).append("|").append(moduleAuth.getAuthId()).append(",");
                }
                ids = new StringBuilder(ids.substring(0, ids.toString().length() - 1));
                PageContext p = null;
                names = Functions.showOrgEntities(ids.toString(), p);
            }
            mav.addObject("auth", ids.toString());
            mav.addObject("auth_txt", names);
            
            if(null != template.getBelongOrg()){
            	V3xOrgAccount accountById = orgManager.getAccountById(template.getBelongOrg());
            	if(null != accountById){
            		mav.addObject("belongOrgFB","Account|"+template.getBelongOrg());
            		mav.addObject("belongOrgShow",accountById.getName());
            	}else{
            		mav.addObject("belongOrgFB","Department|" +  template.getBelongOrg());
            		V3xOrgDepartment departmentById = orgManager.getDepartmentById(template.getBelongOrg());
            		if(null != departmentById){
            			mav.addObject("belongOrgShow",departmentById.getName());
            		}
            	}
            }
    		//RACI数据回填
            template = templateManager.addOrgIntoTempalte(template);
            if(Strings.isNotBlank(template.getResponsible())){
            	String[] raciFillbackMessage = getRACIFillbackMessage(template.getResponsible());
            	mav.addObject("r4show", raciFillbackMessage[0]);
            	mav.addObject("r4db", raciFillbackMessage[1]);
            	mav.addObject("r4fb", raciFillbackMessage[2]);
            }
            if(Strings.isNotBlank(template.getAuditor())){
            	String[] raciFillbackMessage = getRACIFillbackMessage(template.getAuditor());
            	mav.addObject("a4show", raciFillbackMessage[0]);
            	mav.addObject("a4db", raciFillbackMessage[1]);
            	mav.addObject("a4fb", raciFillbackMessage[2]);
            }
            if(Strings.isNotBlank(template.getConsultant())){
            	String[] raciFillbackMessage = getRACIFillbackMessage(template.getConsultant());
            	mav.addObject("c4show", raciFillbackMessage[0]);
            	mav.addObject("c4db", raciFillbackMessage[1]);
            	mav.addObject("c4fb", raciFillbackMessage[2]);
            }
            if(Strings.isNotBlank(template.getInform())){
            	String[] raciFillbackMessage = getRACIFillbackMessage(template.getInform());
            	mav.addObject("i4show", raciFillbackMessage[0]);
            	mav.addObject("i4db", raciFillbackMessage[1]);
            	mav.addObject("i4fb", raciFillbackMessage[2]);
            }
            if(Strings.isNotBlank(template.getCoreUseOrg())){
            	String[] raciFillbackMessage = getRACIFillbackMessage(template.getCoreUseOrg());
            	mav.addObject("coreUseOrg4show", raciFillbackMessage[0]);
            	mav.addObject("coreUseOrg4db", raciFillbackMessage[1]);
            	mav.addObject("coreUseOrg4fb", raciFillbackMessage[2]);
            }
    		//督办信息
            Map<String, Object> map = null;
            String superviseStr = (String) template.getExtraAttr("superviseStr");
            if (superviseStr == null) {
                map = new HashMap<String, Object>();
                //回填督办信息到页面
                CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(template.getId());
                if (null != superviseDetail) {
                    List<CtpSupervisor> supervisors = superviseManager.getSupervisors(superviseDetail.getId());
                    String supids = "";
                    mav.addObject("detailId", getLongString(superviseDetail.getId()));
                    mav.addObject("awakeDate", getLongString(superviseDetail.getTemplateDateTerminal()));
                    mav.addObject("title", superviseDetail.getTitle());
                    mav.addObject("supervisorNames", superviseDetail.getSupervisors());
                    map.put("supervisorNames", superviseDetail.getSupervisors());
                    mav.addObject("templateDateTerminal", getLongString(superviseDetail.getTemplateDateTerminal()));

                    String snames = "";
                    V3xOrgMember member;
                    for (CtpSupervisor ctps : supervisors) {
                    	supids += ctps.getSupervisorId() + ",";
                        member = this.orgManager.getMemberById(ctps.getSupervisorId());
                        if (member != null) {
                            snames += (FormUtil.getOrgEntityName(member, orgManager) + "、");
                        }
                    }
                    if (supids.length() > 0) {
                    	supids = supids.substring(0, supids.length() - 1);
                        mav.addObject("supervisorIds", supids);
                    }
                    if (snames.length() > 0) {
                        snames = snames.substring(0, snames.length() - 1);
                        mav.addObject("supervisorNames2", snames);
                    }
                    //督办角色
                    List<CtpSuperviseTemplateRole> roleList = superviseManager.findRoleByTemplateId(template.getId());
                    StringBuilder roles = new StringBuilder("");
                    for (CtpSuperviseTemplateRole srole : roleList) {
                        roles.append(srole.getRole()).append(",");
                    }
                    if (roles.length() > 0) {
                        roles = new StringBuilder(roles.substring(0, roles.toString().length() - 1));
                        mav.addObject("role", roles.toString());
                        map.put("role", roles.toString());
                    }
                }
            } else {
                map = (Map<String, Object>) XMLCoder.decoder(superviseStr);
                mav.addObject("supervisorIds", map.get("supervisorIds"));
                mav.addObject("detailId", map.get("detailId"));
                mav.addObject("supervisorNames", map.get("supervisorNames"));
                mav.addObject("title", map.get("title"));
                mav.addObject("templateDateTerminal", map.get("templateDateTerminal"));
            }
            String showSupers = "";
            if (map.get("role") != null && Strings.isNotBlank(map.get("role").toString())) {
                String role = map.get("role").toString();
                String[] roles = role.split(",");
                for (String string : roles) {
                    showSupers += (getRoleName(string) + "、");
                }
                showSupers = showSupers.substring(0, showSupers.length() - 1);
                mav.addObject("role", role);
            }
            if (map.get("supervisorNames") != null && Strings.isNotBlank(map.get("supervisorNames").toString())) {
                showSupers = ("".equals(showSupers) ? "" : (showSupers + "、")) + map.get("supervisorNames").toString();
            }
            mav.addObject("showSupervisors", showSupers);
            
            
    		//附件信息
    		String attListJSON = attachmentManager.getAttListJSON(template.getId());
	        mav.addObject("attListJSON",attListJSON);
	        ColSummary temSummary = (ColSummary)XMLCoder.decoder(template.getSummary());
	        //预归档相关数据回填
	        String fullPath = "";
	        if(null != temSummary.getArchiveId() && docApi != null){
	        	
	        	String archiveName = docApi.getDocResourceName(temSummary.getArchiveId());
	            if (Strings.isNotBlank(archiveName)) {
	                mav.addObject("archive_name", archiveName);
	                Object obj = temSummary.getExtraAttr("archiverFormid");
	                if (obj != null) {
	                    String archiverFormid = (String) obj;
	                    if (Strings.isNotBlank(archiverFormid)) {
	                        String[] arc = archiverFormid.split("[|_]");
	                        for (String a : arc) {
	                            if (Strings.isNotBlank(a)) {
	                                String[] viewAuth = a.split("\\.");
	                                mav.addObject("view_" + viewAuth[0], viewAuth[0]);
	                                mav.addObject("auth_" + viewAuth[0], viewAuth[1]);
	                            }
	                        }
	                        mav.addObject("archiverFormid",archiverFormid);
	                    }
	                }

	            } else {
	               mav.addObject("archive_Id", "");
	            }
	        	
                DocResourceBO resourceBO = docApi.getDocResource(temSummary.getArchiveId());
                if(null != resourceBO){
                	fullPath = docApi.getPhysicalPath(resourceBO.getLogicalPath(), "\\", false, 0);
                }
                String archiveField = String.valueOf(temSummary.getExtraAttr("archiveField"));
                String archiveIsCreate = String.valueOf(temSummary.getExtraAttr("archiveIsCreate"));
                String archiveText = cap4fb.hasRedTemplete() ? String.valueOf(temSummary.getExtraAttr("archiveText")) : "";
                String archiveTextName = cap4fb.hasRedTemplete() ? String.valueOf(temSummary.getExtraAttr("archiveTextName")) : "";
                String archiveKeyword = String.valueOf(temSummary.getExtraAttr("archiveKeyword"));
                if (StringUtil.checkNull(archiveKeyword)) {
                	archiveKeyword = "";
                }
                if (!StringUtil.checkNull(archiveField)) {
                    mav.addObject("archiveFieldName", archiveField);
                    com.seeyon.cap4.form.bean.FormFieldBean field = cap4fb.getFieldBeanByName(archiveField);
                    mav.addObject("archiveFieldDisplay", field.getDisplay());
                    fullPath=  fullPath + "\\" + "{" + field.getDisplay() + "}";
                }
                mav.addObject("fullPath", fullPath);
                if (!StringUtil.checkNull(archiveIsCreate)) {
                    mav.addObject("archiveIsCreate", archiveIsCreate);
                }else{
                    mav.addObject("archiveIsCreate", "false");
                }
                    /*if (!StringUtil.checkNull(archiveForm)) {
                        map.put("archiveForm", archiveForm);
                    }*/
                mav.addObject("archiveText", archiveText);
                mav.addObject("archiveTextName", archiveTextName);
                mav.addObject("archiveKeyword", archiveKeyword);
	        }
	        
	        if(null != temSummary.getAttachmentArchiveId()){
	        	mav =  attachmentArchiveInfo(temSummary,mav);
	        }
	        //--自动发起数据回填开始
	        String cycleState = String.valueOf(temSummary.getExtraAttr("cycleState"));
	        cycleState = StringUtil.checkNull(cycleState) ? "0" : cycleState;
	        mav.addObject("cycleState", cycleState);
	        if ("1".equals(cycleState)) {
	            String cycleSender = String.valueOf(temSummary.getExtraAttr("cycleSender"));
	            if (!StringUtil.checkNull(cycleSender)) {
	                PageContext pageContext = null;
	                String cycleSenderName = Functions.showOrgEntities(cycleSender, pageContext);
	                mav.addObject("cycleSender", cycleSender);
	                mav.addObject("cycleSender_txt", cycleSenderName);

	                mav.addObject("cycleStartDate", String.valueOf(temSummary.getExtraAttr("cycleStartDate")));
	                mav.addObject("cycleEndDate", String.valueOf(temSummary.getExtraAttr("cycleEndDate")));
	                mav.addObject("cycleType", String.valueOf(temSummary.getExtraAttr("cycleType")));
	                mav.addObject("cycleMonth", String.valueOf(temSummary.getExtraAttr("cycleMonth")));
	                mav.addObject("cycleOrder", String.valueOf(temSummary.getExtraAttr("cycleOrder")));
	                mav.addObject("cycleDay", String.valueOf(temSummary.getExtraAttr("cycleDay")));
	                mav.addObject("cycleWeek", String.valueOf(temSummary.getExtraAttr("cycleWeek")));
	                mav.addObject("cycleHour", String.valueOf(temSummary.getExtraAttr("cycleHour")));
	            }
	        }
	        //--自动发起数据回填结束
	        
	        mav.addObject("summary",temSummary);
    		mav.addObject("template",template);
    		mav = CommonOperation(mav,template);
    		mav.addObject("updateFlag","1");  
    		V3xOrgMember bm = orgManager.getMemberById(template.getMemberId());
    		String bmName = bm.getName();
    		if(bm.getIsAdmin()){
				bmName = orgManager.getAccountById(bm.getOrgAccountId()).getName()+"单位管理员";
			}
    		mav.addObject("createMemberName", bmName);
    		//流程规则说明
    		EnumManager em = (EnumManager) AppContext.getBean("enumManagerNew");
        	CtpEnumItem cei = em.getEnumItem(EnumNameEnum.collaboration_deadline,String.valueOf(temSummary.getDeadline()));
            String enumLabel = ResourceUtil.getString(cei.getLabel());
            mav.addObject("deadlineLabel", enumLabel);
    		mav.addObject("siwfRule",wapi.getWorkflowRuleInfo("", template.getWorkflowId().toString()));
    		
    	//合并处理设置
   	     boolean canAnyDealMerge = EdocUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE,temSummary);
   	     boolean canPreDealMerge = EdocUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE,temSummary);
   	     boolean canStartMerge = EdocUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.START_MERGE,temSummary);
   	     mav.addObject("canAnyDealMerge", canAnyDealMerge);
   	     mav.addObject("canPreDealMerge", canPreDealMerge);
   	     mav.addObject("canStartMerge", canStartMerge);
   	     mav.addObject("templateNameShow", template.getSubject());
    	}else{
    		mav.addObject("templateNameShow", cap4fb.getFormName());
    	}
    	//关联项目
    	List<ProjectBO> projectSummaries = new ArrayList<ProjectBO>();
        if(AppContext.hasPlugin("project")){
        	projectSummaries = projectApi.findProjectsByAccountId(AppContext.currentAccountId());
        }
		mav.addObject("project", projectSummaries);
		
		
		List<FormViewBean> views = cap4fb.getFormViewList();
		List<FormViewBean> pcViews = new ArrayList<FormViewBean>(views.size());
		List<FormViewBean> mobileViews = new ArrayList<FormViewBean>(views.size());
		for(FormViewBean view : views){
		    if(view.isPc()){
		        pcViews.add(view);
		    }else {
		        mobileViews.add(view);
            }
		}
		mav.addObject("pcViews", pcViews);
		mav.addObject("mobileViews", mobileViews);
		
		
		
		//预归档到 显示明细 需要用到 formbean对象
		mav.addObject("formBean", cap4fb);
		
		
		PermissionVO vo = permissionManager.getDefaultPermissionByConfigCategory("col_flow_perm_policy", AppContext.currentAccountId());
        mav.addObject("defaultPolicyId", vo.getName());
        mav.addObject("defaultPolicyName", vo.getLabel());
        
        WorkflowFormDataMapManager formDataManager = wapi.getWorkflowFormDataMapManager("form");
        String startDefualtRightId = formDataManager.getStartDefualtRightId(cap4fb.getId());
        String normalDefualtRightId = formDataManager.getNormalDefualtRightId(cap4fb.getId());
        
        mav.addObject("startDefualtRightId", startDefualtRightId);
        mav.addObject("normalDefualtRightId", normalDefualtRightId);
        
    	mav.addObject("defId",defId);
    	mav.addObject("fbatchId",fbatchId);
    	mav.addObject("newFlag","1");
    	mav.addObject("categoryName",tempateCategoryName(cap4fb.getCategoryId()));
    	mav.addObject("formTitle", cap4fb.getFormName());
    	return mav;
    }
    
    private ModelAndView templatePageCAP3(ModelAndView mav,String defId,String fbatchId,String templateId) throws BusinessException{
    	FormBean fb = capFormManager.getEditingForm();
    	boolean hasAIPlugin = AppContext.hasPlugin("ai");
    	boolean hasCDPPlugin = CDPAgent.isEnabled();
    	if(hasAIPlugin && hasCDPPlugin){
    		List<Map<String,String>> beanList = new ArrayList<Map<String,String>>();
    		List<FormFieldBean> fieldBeanList = fb.getMasterTableBean().getFields();
    		for(FormFieldBean bean:fieldBeanList){
    			if("DECIMAL".equals(bean.getFieldType())){
    				Map<String,String> beanJSON = new HashMap<String, String>();
    				beanJSON.put("tableName", bean.getOwnerTableName());
    				beanJSON.put("formName", fb.getFormName());
    				beanJSON.put("fieldName", bean.getName());
    				beanJSON.put("fieldNameDisplay", "（主表）" + bean.getDisplay());
    				beanList.add(beanJSON);
    			}
    		}
    		mav.addObject("decimalFieldList", JSONUtil.toJSONString(beanList));
    	}
    	mav.addObject("defaultSubject", fb.getFormName());
    	
    	
    	Boolean isNewTemplate = true;//告诉界面是否是新建的模板
    	GovdocBodyVO bodyVo = new GovdocBodyVO();
    	
    	if(Strings.isNotBlank(templateId)){//修改模板
    		isNewTemplate = false;
    		CtpTemplate template = fb.getBind().getFlowTemplate(Long.valueOf(templateId));
    		bodyVo.setBindTHTemplateId(template.getBindTHTemplateId());
    		//组装页面信息用于修改回填到页面
    		mav.addObject("defaultSubject", template.getSubject());
    		mav.addObject("isNew",false);
    		if(hasAIPlugin){
    			if(hasCDPPlugin) {
    				String processMonitorJSON = (String)template.getExtraAttr(FormConstant.FLOW_TEMPLATE_EXT_PROCESS_MONITOR);
    				String processMonitorInput = (String) template.getExtraAttr(FormConstant.FLOW_TEMPLATE_EXT_MONITOR_CAN_SEND_MSG);
    				//加载流程监控配置信息
    				if(Strings.isNotBlank(processMonitorJSON)){
    					mav.addObject("isSendMsg", "1".equals(processMonitorInput) ? true : false);
    					mav.addObject("monitorArray", processMonitorJSON);
    				}else{
    					processMonitorInfo(mav,templateId);
    				}
    			}
    			setAIDealCondition(mav, template);
    		}
    		//表单授权
    		Object auth = template.getExtraAttr("authList");
            if (auth == null) {
                //修改时读取授权信息
                template.putExtraAttr("authList", templateManager.getCtpTemplateAuths(template.getId(), template.getModuleType()));
                auth = template.getExtraAttr("authList");
            }
    		List<CtpTemplateAuth> authList = (List<CtpTemplateAuth>) auth;
            StringBuilder ids = new StringBuilder("");
            String names = "";
            if (authList.size() > 0) {
                for (CtpTemplateAuth moduleAuth : authList) {
                    ids.append(moduleAuth.getAuthType()).append("|").append(moduleAuth.getAuthId()).append(",");
                }
                ids = new StringBuilder(ids.substring(0, ids.toString().length() - 1));
                PageContext p = null;
                names = Functions.showOrgEntities(ids.toString(), p);
            }
            mav.addObject("auth", ids.toString());
            mav.addObject("auth_txt", names);
          //cx 部门授权
            Object depAuth = template.getExtraAttr("depAuthList");
            List<GovdocTemplateDepAuthBO> depAuthList = (List<GovdocTemplateDepAuthBO>) depAuth;
            StringBuilder idss = new StringBuilder("");
            String namess = "";
            if (CollectionUtils.isNotEmpty(depAuthList)) {
                for (GovdocTemplateDepAuthBO moduleAuth : depAuthList) {
                    idss.append(moduleAuth.getOrgType()).append("|").append(moduleAuth.getOrgId()).append(",");
                }
                idss = new StringBuilder(idss.substring(0, idss.toString().length() - 1));
                PageContext p = null;
                namess = Functions.showOrgEntities(idss.toString(), p);
            }
            mav.addObject("dep_auth", idss.toString());
            mav.addObject("dep_auth_txt", namess);
    		//关联表单授权
    		Object authRelationObj = template.getExtraAttr("relationAuthList");
            if (authRelationObj == null) {
                template.putExtraAttr("relationAuthList", capFormManager.getCtpTemplateRelationAuths(template.getId()));//修改时读取授权信息
                authRelationObj = template.getExtraAttr("relationAuthList");
            }
    		List<CtpTemplateRelationAuth> authRelationList = (List<CtpTemplateRelationAuth>) authRelationObj;
            StringBuilder text = new StringBuilder();
            StringBuilder value = new StringBuilder();
            if (authRelationList.size() > 0) {
                CtpTemplateRelationAuth relationAuth = null;
                for (int i = 0; i < authRelationList.size(); i++) {
                    relationAuth = authRelationList.get(i);
                    String userType = SelectPersonOperation.getTypeByTypeId(relationAuth.getAuthType());
                    String tempName;
                    if (relationAuth.getAuthType() == 7 || relationAuth.getAuthType() == 8) {
                        tempName = FormUtil.getShowMemNameByIds(userType + "|" + relationAuth.getAuthValue(), fb);
                    } else {
                        tempName = SelectPersonOperation.getNameByTypeIdAndUserId(relationAuth.getAuthType().intValue(), Long.parseLong(relationAuth.getAuthValue()));
                    }
                    if (Strings.isNotBlank(tempName)) {
                        text.append(tempName).append("、");
                    }
                    value.append(userType).append("|").append(relationAuth.getAuthValue()).append(",");
                }
            }
            String value4Show = value.toString();
            String text4Show = text.toString();
            if (Strings.isNotBlank(value4Show)) {
                value4Show = value4Show.substring(0, value4Show.length() - 1);
            }
            if (Strings.isNotBlank(text4Show)) {
                text4Show = text4Show.substring(0, text4Show.length() - 1);
            }
            mav.addObject("authRelation", value4Show);
            mav.addObject("authRelation_txt", text4Show);
            
            if(null != template.getBelongOrg()){
            	V3xOrgAccount accountById = orgManager.getAccountById(template.getBelongOrg());
            	if(null != accountById){
            		mav.addObject("belongOrgFB","Account|"+template.getBelongOrg());
            		mav.addObject("belongOrgShow",accountById.getName());
            	}else{
            		mav.addObject("belongOrgFB","Department|" +  template.getBelongOrg());
            		V3xOrgDepartment departmentById = orgManager.getDepartmentById(template.getBelongOrg());
            		if(null != departmentById){
            			mav.addObject("belongOrgShow",departmentById.getName());
            		}
            	}
            }
    		//RACI数据回填
            template = templateManager.addOrgIntoTempalte(template);
            if(Strings.isNotBlank(template.getResponsible())){
            	String[] raciFillbackMessage = getRACIFillbackMessage(template.getResponsible());
            	mav.addObject("r4show", raciFillbackMessage[0]);
            	mav.addObject("r4db", raciFillbackMessage[1]);
            	mav.addObject("r4fb", raciFillbackMessage[2]);
            }
            if(Strings.isNotBlank(template.getAuditor())){
            	String[] raciFillbackMessage = getRACIFillbackMessage(template.getAuditor());
            	mav.addObject("a4show", raciFillbackMessage[0]);
            	mav.addObject("a4db", raciFillbackMessage[1]);
            	mav.addObject("a4fb", raciFillbackMessage[2]);
            }
            if(Strings.isNotBlank(template.getConsultant())){
            	String[] raciFillbackMessage = getRACIFillbackMessage(template.getConsultant());
            	mav.addObject("c4show", raciFillbackMessage[0]);
            	mav.addObject("c4db", raciFillbackMessage[1]);
            	mav.addObject("c4fb", raciFillbackMessage[2]);
            }
            if(Strings.isNotBlank(template.getInform())){
            	String[] raciFillbackMessage = getRACIFillbackMessage(template.getInform());
            	mav.addObject("i4show", raciFillbackMessage[0]);
            	mav.addObject("i4db", raciFillbackMessage[1]);
            	mav.addObject("i4fb", raciFillbackMessage[2]);
            }
            if(Strings.isNotBlank(template.getCoreUseOrg())){
            	String[] raciFillbackMessage = getRACIFillbackMessage(template.getCoreUseOrg());
            	mav.addObject("coreUseOrg4show", raciFillbackMessage[0]);
            	mav.addObject("coreUseOrg4db", raciFillbackMessage[1]);
            	mav.addObject("coreUseOrg4fb", raciFillbackMessage[2]);
            }
            
    		//督办信息
            Map<String, Object> map = null;
            String superviseStr = (String) template.getExtraAttr("superviseStr");
            if (superviseStr == null) {
                map = new HashMap<String, Object>();
                //回填督办信息到页面
                CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(template.getId());
                if (null != superviseDetail) {
                    List<CtpSupervisor> supervisors = superviseManager.getSupervisors(superviseDetail.getId());
                    String supids = "";
                    mav.addObject("detailId", getLongString(superviseDetail.getId()));
                    mav.addObject("awakeDate", getLongString(superviseDetail.getTemplateDateTerminal()));
                    mav.addObject("title", superviseDetail.getTitle());
                    mav.addObject("supervisorNames", superviseDetail.getSupervisors());
                    map.put("supervisorNames", superviseDetail.getSupervisors());
                    mav.addObject("templateDateTerminal", getLongString(superviseDetail.getTemplateDateTerminal()));

                    String snames = "";
                    V3xOrgMember member;
                    for (CtpSupervisor ctps : supervisors) {
                    	supids += ctps.getSupervisorId() + ",";
                        member = this.orgManager.getMemberById(ctps.getSupervisorId());
                        if (member != null) {
                            snames += (FormUtil.getOrgEntityName(member, orgManager) + "、");
                        }
                    }
                    if (supids.length() > 0) {
                    	supids = supids.substring(0, supids.length() - 1);
                        mav.addObject("supervisorIds", supids);
                    }
                    if (snames.length() > 0) {
                        snames = snames.substring(0, snames.length() - 1);
                        mav.addObject("supervisorNames2", snames);
                    }
                    //督办角色
                    List<CtpSuperviseTemplateRole> roleList = superviseManager.findRoleByTemplateId(template.getId());
                    StringBuilder roles = new StringBuilder("");
                    for (CtpSuperviseTemplateRole srole : roleList) {
                        roles.append(srole.getRole()).append(",");
                    }
                    if (roles.length() > 0) {
                        roles = new StringBuilder(roles.substring(0, roles.toString().length() - 1));
                        mav.addObject("role", roles.toString());
                        map.put("role", roles.toString());
                    }
                }
            } else {
                map = (Map<String, Object>) XMLCoder.decoder(superviseStr);
                mav.addObject("supervisorIds", map.get("supervisorIds"));
                mav.addObject("detailId", map.get("detailId"));
                mav.addObject("supervisorNames", map.get("supervisorNames"));
                mav.addObject("title", map.get("title"));
                mav.addObject("templateDateTerminal", map.get("templateDateTerminal"));
            }
            String showSupers = "";
            if (map.get("role") != null && Strings.isNotBlank(map.get("role").toString())) {
                String role = map.get("role").toString();
                String[] roles = role.split(",");
                for (String string : roles) {
                    showSupers += (getRoleName(string) + "、");
                }
                showSupers = showSupers.substring(0, showSupers.length() - 1);
                mav.addObject("role", role);
            }
            if (map.get("supervisorNames") != null && Strings.isNotBlank(map.get("supervisorNames").toString())) {
                showSupers = ("".equals(showSupers) ? "" : (showSupers + "、")) + map.get("supervisorNames").toString();
            }
            mav.addObject("showSupervisors", showSupers);
            
            
    		//附件信息
    		String attListJSON = attachmentManager.getAttListJSON(template.getId());
	        mav.addObject("attListJSON",attListJSON);
	        ColSummary temSummary = null;
	        if(template.getSummary().contains("com.seeyon.v3x.edoc.domain.EdocSummary")){
	        	temSummary = getColSummaryByEdocSummary(template.getSummary());
	        }else{
	        	temSummary = (ColSummary) XMLCoder.decoder(template.getSummary());
	        }
	        //预归档相关数据回填
	        String fullPath = "";
	        if(AppContext.hasPlugin("doc") && null != temSummary.getArchiveId()){
	        	String archiveName = docApi.getDocResourceName(temSummary.getArchiveId());
	            if (Strings.isNotBlank(archiveName)) {
	                mav.addObject("archive_name", archiveName);
	                Object obj = temSummary.getExtraAttr("archiverFormid");
	                if (obj != null) {
	                    String archiverFormid = (String) obj;
	                    if (Strings.isNotBlank(archiverFormid)) {
	                        String[] arc = archiverFormid.split("\\|");
	                        for (String a : arc) {
	                            if (Strings.isNotBlank(a)) {
	                                String[] viewAuth = a.split("\\.");
	                                mav.addObject("view_" + viewAuth[0], viewAuth[0]);
	                                mav.addObject("auth_" + viewAuth[0], viewAuth[1]);
	                            }
	                        }
	                        mav.addObject("archiverFormid",archiverFormid);
	                    }
	                }

	            } else {
	               mav.addObject("archive_Id", "");
	            }
	        	
                DocResourceBO resourceBO = docApi.getDocResource(temSummary.getArchiveId());
                if(null != resourceBO){
                	fullPath = docApi.getPhysicalPath(resourceBO.getLogicalPath(), "\\", false, 0);
                }
                String archiveField = String.valueOf(temSummary.getExtraAttr("archiveField"));
                String archiveIsCreate = String.valueOf(temSummary.getExtraAttr("archiveIsCreate"));
                String archiveText = fb.hasRedTemplete() ? String.valueOf(temSummary.getExtraAttr("archiveText")) : "";
                String archiveTextName = fb.hasRedTemplete() ? String.valueOf(temSummary.getExtraAttr("archiveTextName")) : "";
                String archiveKeyword = String.valueOf(temSummary.getExtraAttr("archiveKeyword"));
                if (StringUtil.checkNull(archiveKeyword)) {
                	archiveKeyword = "";
                }
                if (!StringUtil.checkNull(archiveField)) {
                    mav.addObject("archiveFieldName", archiveField);
                    FormFieldBean field = fb.getFieldBeanByName(archiveField);
                    mav.addObject("archiveFieldDisplay", field.getDisplay());
                    fullPath=  fullPath + "\\" + "{" + field.getDisplay() + "}";
                }
                mav.addObject("fullPath", fullPath);
                if (!StringUtil.checkNull(archiveIsCreate)) {
                    mav.addObject("archiveIsCreate", archiveIsCreate);
                }else{
                    mav.addObject("archiveIsCreate", "false");
                }
                    /*if (!StringUtil.checkNull(archiveForm)) {
                        map.put("archiveForm", archiveForm);
                    }*/
                mav.addObject("archiveText", archiveText);
                mav.addObject("archiveTextName", archiveTextName);
                mav.addObject("archiveKeyword", archiveKeyword);
                
                
	        }
	        
	        if(null != temSummary.getAttachmentArchiveId()){
	        	mav =  attachmentArchiveInfo(temSummary,mav);
	        }
	        
	        //--自动发起数据回填开始
	        String cycleState = String.valueOf(temSummary.getExtraAttr("cycleState"));
	        cycleState = StringUtil.checkNull(cycleState) ? "0" : cycleState;
	        mav.addObject("cycleState", cycleState);
	        if ("1".equals(cycleState)) {
	            String cycleSender = String.valueOf(temSummary.getExtraAttr("cycleSender"));
	            if (!StringUtil.checkNull(cycleSender)) {
	                PageContext pageContext = null;
	                String cycleSenderName = Functions.showOrgEntities(cycleSender, pageContext);
	                mav.addObject("cycleSender", cycleSender);
	                mav.addObject("cycleSender_txt", cycleSenderName);

	                mav.addObject("cycleStartDate", String.valueOf(temSummary.getExtraAttr("cycleStartDate")));
	                mav.addObject("cycleEndDate", String.valueOf(temSummary.getExtraAttr("cycleEndDate")));
	                mav.addObject("cycleType", String.valueOf(temSummary.getExtraAttr("cycleType")));
	                mav.addObject("cycleMonth", String.valueOf(temSummary.getExtraAttr("cycleMonth")));
	                mav.addObject("cycleOrder", String.valueOf(temSummary.getExtraAttr("cycleOrder")));
	                mav.addObject("cycleDay", String.valueOf(temSummary.getExtraAttr("cycleDay")));
	                mav.addObject("cycleWeek", String.valueOf(temSummary.getExtraAttr("cycleWeek")));
	                mav.addObject("cycleHour", String.valueOf(temSummary.getExtraAttr("cycleHour")));
	            }
	        }
	        //--自动发起数据回填结束
	        mav.addObject("summary",temSummary);
    		mav.addObject("template",template);
    		mav = CommonOperation(mav,template);
    		mav.addObject("updateFlag","1");
    		V3xOrgMember bm = orgManager.getMemberById(template.getMemberId());
    		if(bm != null){
    			String bmName = bm.getName();
    			if(bm.getIsAdmin()){
    				bmName = orgManager.getAccountById(bm.getOrgAccountId()).getName()+"单位管理员";
    			}
    			mav.addObject("createMemberName", bmName);
    		}
    		EnumManager em = (EnumManager) AppContext.getBean("enumManagerNew");
        	CtpEnumItem cei = em.getEnumItem(EnumNameEnum.collaboration_deadline,String.valueOf(temSummary.getDeadline()));
            String enumLabel = ResourceUtil.getString(cei.getLabel());
            mav.addObject("deadlineLabel", enumLabel);
    		//流程规则说明
    		mav.addObject("siwfRule",wapi.getWorkflowRuleInfo("", template.getWorkflowId().toString()));
    		
    		//合并处理设置
    	     boolean canAnyDealMerge = EdocUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE,temSummary);
    	     boolean canPreDealMerge = EdocUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE,temSummary);
    	     boolean canStartMerge = EdocUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.START_MERGE,temSummary);
    	     mav.addObject("canAnyDealMerge", canAnyDealMerge);
    	     mav.addObject("canPreDealMerge", canPreDealMerge);
    	     mav.addObject("canStartMerge", canStartMerge);
    	     mav.addObject("templateNameShow", template.getSubject());
    	     
    	     //绑定的正文回填
    	     Object object= template.getExtraAttr("govdocContentAll");
    	     if(null != object) {
    	    	 GovdocContentHelper.fillTemplateBodyVo(bodyVo, (CtpContentAll) object);
    	     }
    	     
    	     //绑定文号回填
    	     String binfMarkInfo = "";
    	     String binfMark = JSONUtil.toJSONString(XMLCoder.decoder(template.getBindMarkInfo()));
    	     if(Strings.isBlank(binfMarkInfo)){
    	    	 binfMarkInfo = binfMark;
    	     }
    	     mav.addObject("markList", binfMarkInfo);
    	     
    	}else{
    		mav.addObject("isNew",true);
    		mav.addObject("templateNameShow", fb.getFormName());
    	}
    	mav.addObject("isNewTemplate", isNewTemplate);
    	//关联项目
    	List<ProjectBO> projectSummaries = new ArrayList<ProjectBO>();
        if(AppContext.hasPlugin("project")){
        	projectSummaries = projectApi.findProjectsByAccountId(AppContext.currentAccountId());
        }
		mav.addObject("project", projectSummaries);
		
		//视图列表
        mav.addObject("pcViews", fb.getFormViewList());
        
		//预归档到 显示明细 需要用到 formbean对象
		mav.addObject("formBean", fb);
		
		String configCategory = "col_flow_perm_policy";
		if (fb.getGovDocFormType() == FormType.govDocSendForm.getKey()) {
			configCategory = EnumNameEnum.edoc_new_send_permission_policy.name();
		} else if (fb.getGovDocFormType() == FormType.govDocReceiveForm.getKey()) {
			configCategory = EnumNameEnum.edoc_new_rec_permission_policy.name();
		} else if (fb.getGovDocFormType() == FormType.govDocSignForm.getKey()) {
			configCategory = EnumNameEnum.edoc_new_qianbao_permission_policy.name();
		} else if (fb.getGovDocFormType() == FormType.govDocExchangeForm.getKey()) {
			configCategory = EnumNameEnum.edoc_new_change_permission_policy.name();
		}
		PermissionVO vo = permissionManager.getDefaultPermissionByConfigCategory(configCategory, AppContext.currentAccountId());
        mav.addObject("defaultPolicyId", vo.getName());
        mav.addObject("defaultPolicyName", vo.getLabel());
     
        WorkflowFormDataMapManager formDataManager = wapi.getWorkflowFormDataMapManager("form");
        String startDefualtRightId = formDataManager.getStartDefualtRightId(fb.getId());
        String normalDefualtRightId = formDataManager.getNormalDefualtRightId(fb.getId());
        
		mav.addObject("startDefualtRightId", startDefualtRightId);
		mav.addObject("normalDefualtRightId", normalDefualtRightId);
        
    	mav.addObject("defId",defId);
    	mav.addObject("fbatchId",fbatchId);
    	mav.addObject("categoryName",tempateCategoryName(fb.getCategoryId()));
    	mav.addObject("formTitle", fb.getFormName());
    	
    	//获取新公文的正文类型
    	bodyVo.setMainbodyTypeListJSONStr(ContentConfig.getConfig(ModuleType.edoc).getMainbodyTypeListJSONStr());
    	//取出所有正文套红模版
    	bodyVo.setTaohongList(govdocDocTemplateManager.findTemplateByType(TempleteType.content.getKey()));
    	mav.addObject("bodyVo", bodyVo);
    	mav.addObject("currentPageName", "formBind");
    	
    	return mav;
    }
    
    private ModelAndView setBelongOrgDefaultValue(ModelAndView mav) throws BusinessException{
    	User cUser = AppContext.getCurrentUser();
    	if(null == mav.getModelMap().get("belongOrgFB")){
    		if(cUser.isAdmin()){
    			Long accountId = cUser.getAccountId();
    			mav.addObject("belongOrgFB","Account|" +  accountId);
    			V3xOrgAccount account = orgManager.getAccountById(accountId);
    			if(null != account){
    				mav.addObject("belongOrgShow",account.getName());
    			}
    		}else{
    			Long departmentId = 0l;
    			if(AppContext.currentAccountId() == cUser.getAccountId().longValue()){
    				departmentId = cUser.getDepartmentId();
    			}else{
    				List<V3xOrgDepartment> departments = orgManager.getDepartmentsByUser(cUser.getId());
    				for(V3xOrgDepartment department : departments){
    					if(department.getOrgAccountId().equals(AppContext.currentAccountId())){
    						departmentId = department.getId();
    						break;
    					}
    				}
    			}
    			mav.addObject("belongOrgFB","Department|" +  departmentId);
    			V3xOrgDepartment departmentById = orgManager.getDepartmentById(departmentId);
    			if(null != departmentById){
    				mav.addObject("belongOrgShow",departmentById.getName());
    			}
    		}
    	}
    	mav.addObject("defaultUser", cUser.getName());
    	return mav;
    }
	
    private ModelAndView CommonOperation(ModelAndView mav,CtpTemplate c){
    	if(null != c.getPublishTime()){
			String publishTime = Datetimes.format(c.getPublishTime(),"yyyy-MM-dd HH:mm");
			mav.addObject("publishTime", publishTime);
		}
		 if(null != c.getProcessLevel()){
			 String enumShowName = templateManager.getEnumShowName("cap_process_leavel", c.getProcessLevel().toString());
			 mav.addObject("processLL", enumShowName);
			 mav.addObject("processLL", enumShowName);
		 }
		 
    	return mav;
    }
    
    private void processMonitorInfo(ModelAndView mav, String templateId) {
			try {
				if(CDPAgent.isEnabled() && AppContext.hasPlugin("ai")){
					List processMonitorList = aiApi.getByTemplateId(Long.valueOf(templateId));
					if(processMonitorList != null && processMonitorList.size() > 0){
						String jsonStr = JSONUtil.toJSONString(processMonitorList.get(0));
						Map<String,String> boMap = JSONUtil.parseJSONString(jsonStr,Map.class);
						mav.addObject("isSendMsg", boMap.get("isSendMsg"));
						mav.addObject("monitorArray", JSONUtil.toJSONString(processMonitorList));
					}
				}
			} catch (BusinessException e) {
				LOG.error("查询流程监控条件异常：",e);
			}
	}
    
	private void setAIDealCondition(ModelAndView mav, CtpTemplate template) {
		String autoDealConditionVal = (String) template.getExtraAttr(FormConstant.FLOW_TEMPLATE_EXT_AI_PROCESSING_CONDITION);
		if(Strings.isNotBlank(autoDealConditionVal)) {
			mav.addObject("autoDealConditionVal", autoDealConditionVal);
		} else {
			try {
				mav.addObject("autoDealConditionVal", aiApi.getProcessingConditionByTemplateId(template.getId()));
			} catch (BusinessException e) {
				LOG.error("查询流程智能处理设置条件异常", e);
			}
		}
	}
	
    private String getLongString(Long value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }
    
    /**
     * 通过督办角色获取名称
     *
     * @return
     */
    private String getRoleName(String role) {
        if ("sender".equals(role)) {
            return ResourceUtil.getString("collaboration.common.common.supervise.initiator");
        } else {
            return ResourceUtil.getString("collaboration.common.common.supervise.initiatorManager");
        }
    }
    
    private ModelAndView attachmentArchiveInfo(ColSummary temSummary,ModelAndView mav) throws BusinessException{
    	mav.addObject("attachmentArchiveId", temSummary.getAttachmentArchiveId());
    	mav.addObject("archiveAttachment",null == temSummary.getAttachmentArchiveId() ? "false" : "true");
    	String attachmentArchiveName = "";
    	if(temSummary.getAttachmentArchiveId() != null && docApi != null){
    		DocResourceBO docResourceBO = docApi.getDocResource(temSummary.getAttachmentArchiveId());
    		if(docResourceBO != null){
    			attachmentArchiveName = docApi.getPhysicalPath(docResourceBO.getLogicalPath(), "\\", false, 0);
    		}
    	}
    	mav.addObject("attachmentArchiveName", attachmentArchiveName);//附件归档的完整目录
    	return mav;
    }
    
    private String tempateCategoryName(Long categoryId){
    	CtpTemplateCategory categorybyId = templateManager.getCategorybyId(categoryId);
    	if(null != categorybyId){
    		return categorybyId.getName();
    	}
    	return "";
    }
    
	public ColSummary getColSummaryByEdocSummary(String summaryStr) {
		ColSummary summary = new ColSummary();

		EdocSummary edocSummary = (EdocSummary) XMLCoder.decoder(summaryStr);
		summary.setId(edocSummary.getId());
		summary.setCanArchive(edocSummary.get_canArchive());
		summary.setCanTrack(edocSummary.getCanTrack()==1?true:false);
		summary.setFormid(edocSummary.getFormId());
		summary.setSubject(edocSummary.getSubject());
		summary.setCanEdit(edocSummary.getCanEdit());
		summary.setCanArchive(edocSummary.getCanArchive());
		summary.setCanModify(edocSummary.getCanModify());
		summary.setCanForward(edocSummary.getCanForward());
		summary.setCanDueReminder(edocSummary.getCanDueReminder());
		summary.setAwakeDate(edocSummary.getAwakeDate());
		summary.setCanAutostopflow(edocSummary.getCanAutostopflow());
		summary.setFormAppid(edocSummary.getFormAppid());
		summary.setFormRecordid(edocSummary.getFormRecordid());
		summary.setBodyType(edocSummary.getBodyType());
		summary.setAdvanceRemind(edocSummary.getAdvanceRemind());
		summary.setAdvancePigeonhole(edocSummary.getAdvancePigeonhole());
		summary.setArchiveId(edocSummary.getArchiveId());
		summary.setImportantLevel(edocSummary.getImportantLevel());
		summary.setCanEditAttachment(edocSummary.getCanEditAttachment());
		summary.setUpdateSubject(edocSummary.getUpdateSubject());
		summary.setCanAnyMerge(edocSummary.getCanAnyMerge());
		summary.setCanMergeDeal(edocSummary.getCanMergeDeal());
		summary.setCoverTime(edocSummary.getCoverTime());
		summary.setDeadline(edocSummary.getDeadline());
		summary.setCanAutostopflow(edocSummary.getCanAutostopflow());
		summary.setTempleteId(edocSummary.getTempleteId());
		summary.setImportantLevel(edocSummary.getImportantLevel());
		summary.setAttachmentArchiveId(edocSummary.getAttachmentArchiveId());
		 /*********** v7.1sp1 新增字段 strat  ************************/
		summary.setMergeDealType(edocSummary.getMergeDealType());
		summary.setProcessTermType(edocSummary.getProcessTermType());
		summary.setRemindInterval(edocSummary.getRemindInterval());
		 /*********** v7.1sp1 新增字段 end  ************************/
		Map extraMap = edocSummary.getExtraMap();
		Iterator it = extraMap.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry m = (Map.Entry)it.next();
			summary.putExtraAttr((String)m.getKey(), (String)m.getValue());
		}
	
		return summary;	
	}
	
    private void sortWithModifyData(List<CtpTemplate> t){
    	Collections.sort(t,new Comparator<CtpTemplate>(){
			@Override
			public int compare(CtpTemplate t1, CtpTemplate t2) {
				//附件列表需倒序
        		Date d1=t1.getModifyDate();
        		Date d2=t2.getModifyDate();
        		int res=0;
        		if(d1!=null&&d2!=null){
        			res=d1.compareTo(d2);
        		}else if(d1 == null&&d2!=null){
        			res=-1;
        		}else if(d1!=null){
        			res=1;
        		}
        		return res==0?0:res>0?-1:1;  
			}
			
		});
    }
    
    public ModelAndView templateDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("common/template/templateDetail");
        String templateId = request.getParameter("templateId");
        String openFrom = request.getParameter("openFrom");
        if (Strings.isNotBlank(openFrom)) {
            request.setAttribute("openFrom", openFrom);
        } else {
            request.setAttribute("openFrom", "");
        }
        
        CtpTemplate template = templateManager.getCtpTemplate(Long.valueOf(templateId));
        boolean hasAttachments = TemplateUtil.isHasAttachments(template);
        if (hasAttachments) {
            modelAndView.addObject("canDeleteOriginalAtts", false);
            modelAndView.addObject("cloneOriginalAtts", true);
            String attListJSON = attachmentManager.getAttListJSON(template.getId());//附件信息  	
            if("[]".equals(attListJSON)){
              hasAttachments =false;
            }
            modelAndView.addObject("attListJSON", attListJSON);
        }
        modelAndView.addObject("hasAttachments", hasAttachments);
        Long memberId = template.getMemberId();
        String cd = Datetimes.formatDatetimeWithoutSecond(template.getCreateDate());
        String senderName =  Functions.showMemberName(memberId);
        request.setAttribute("senderInfo", senderName + " ( " + cd + " ) ");
        //summary信息
        String summayInfo = template.getSummary();
        EdocSummary summary = (EdocSummary) XMLCoder.decoder(summayInfo);
        request.setAttribute("summary", summary);
        //属性页签的几个属性 流程期限 提醒 关联项目 预归档到 转发 改变流程 修改正文 归档
        String deadLine = WFComponentUtil.getDeadLineName(summary.getDeadline());
        request.setAttribute("deadLine", deadLine);
        String remind = WFComponentUtil.getAdvanceRemind(String.valueOf(null != summary.getAdvanceRemind() ? summary
                .getAdvanceRemind().intValue() : 0));
        request.setAttribute("remind", remind);
        if (null != summary.getArchiveId() && Strings.isNotBlank(summary.getArchiveId().toString())) {
        	String archiveAllName = EdocUtil.getArchiveAllNameById(summary.getArchiveId());
        	String archiveName = EdocUtil.getArchiveNameById(summary.getArchiveId());
            request.setAttribute("archiveName", archiveName);
            request.setAttribute("archiveAllName", archiveAllName);
            
        }
         //附件归档全路径
        if(summary.getAttachmentArchiveId() != null){
        	String attachmentArchiveAllName = EdocUtil.getArchiveAllNameById(summary.getAttachmentArchiveId());
        	String attachmentArchiveName = EdocUtil.getArchiveNameById(summary.getAttachmentArchiveId());
        	request.setAttribute("attachmentArchiveName", attachmentArchiveName);
        	request.setAttribute("attachmentArchiveAllName", attachmentArchiveAllName);
        }
        if (AppContext.hasPlugin(ApplicationCategoryEnum.project.name())&& null != summary.getProjectId() && !Long.valueOf(-1).equals(summary.getProjectId()) && Strings.isNotBlank(summary.getProjectId().toString())) {
            request.setAttribute("projectName", projectApi.getProject(summary.getProjectId()).getProjectName());

        }
        //正文
        String rightId = null;
        Long workflowId = template.getWorkflowId();
        //如果父模板id不为空时，查询父模板
        if(template.getFormParentid() != null){
        	CtpTemplate parentTemplate = templateManager.getCtpTemplate(template.getFormParentid());
        	if(parentTemplate != null){
        		workflowId = parentTemplate.getWorkflowId();
        		templateId = parentTemplate.getId().toString();
        	}
        }
        if(null != workflowId && Strings.isNotBlank(workflowId.toString())){
        	rightId = wapi.getNodeFormOperationName(workflowId, null);
        }
        ContentViewRet context = null;
        boolean cap4Form = null != template.getFormAppId() &&  capFormManager.isCAP4Form(template.getFormAppId());
        if(!"workflow".equals(template.getType())){
        	LOG.info("查看模板rightID="+template.getSubject()+"***rightId="+rightId);
        	if(Strings.isNotBlank(rightId) && "-1.-1".equals(rightId)){
        		rightId ="-1";
        	}
        	if(cap4Form){
        		modelAndView.addObject("cap4Form", cap4Form);
        	}else{
        		context = GovdocWorkflowHelper.contentView(ModuleType.getEnumByKey(template.getModuleType()), Long.parseLong(templateId), null,
        				CtpContentAllBean.viewState_readOnly, StringUtil.checkNull(rightId) ? "-1" : rightId);
        	}
        }else{
        	List<CtpContentAllBean> contentList = new ArrayList<CtpContentAllBean>();
    		CtpContentAllBean content_null = new CtpContentAllBean(new CtpContentAll());
    		content_null.setViewState(CtpContentAllBean.viewState_readOnly);
    		content_null.setStatus(MainbodyStatus.STATUS_RESPONSE_VIEW);
    		content_null.setRightId(rightId);
    		content_null.setContentType(10);
    		content_null.setContentHtml("");
            contentList.add(content_null);
        	context = new ContentViewRet();
            context.setModuleId(template.getId());
            context.setModuleType(ModuleType.collaboration.getKey());
            request.setAttribute("contentList", contentList);
            request.setAttribute("contentContext", context);
            ContentConfig contentCfg = ContentConfig.getConfig(ModuleType.collaboration);
            request.setAttribute("contentCfg", contentCfg);
        }
        //    	comment comment = (Comment)request.getAttribute("commentDraft");
        //    	context.setContentSenderId(comment.getCreateId());
        request.setAttribute("template", template);
        request.setAttribute("temTraceType", template.getCanTrackWorkflow());
        request.setAttribute("fromTemplate", Boolean.TRUE);
        request.setAttribute("wfId",workflowId);
        request.setAttribute("appName",ApplicationCategoryEnum.valueOf(template.getModuleType()).name());
        //无
        String isNull = ResourceUtil.getString("collaboration.project.nothing.label");
        if ("workflow".equals(template.getType())) {
            request.setAttribute("archiveName", isNull);
            request.setAttribute("projectName", isNull);
        }
        if ("text".equals(template.getType())) {
            request.setAttribute("deadLine", isNull);
            request.setAttribute("remind", isNull);
            request.setAttribute("archiveName", isNull);
            request.setAttribute("projectName", isNull);
        }
        String[] parms = {"archiveName","projectName","deadLine","remind","attachmentArchiveName"};
        for (String string : parms) {
            if(StringUtil.checkNull(String.valueOf(request.getAttribute(string)))){
                request.setAttribute(string, isNull);
            }
        }
      //合并处理设置
	     boolean canAnyDealMerge = GovdocUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE,summary);
	     boolean canPreDealMerge = GovdocUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE,summary);
	     boolean canStartMerge = GovdocUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.START_MERGE,summary);
	     request.setAttribute("canAnyDealMerge", canAnyDealMerge);
	     request.setAttribute("canPreDealMerge", canPreDealMerge);
	     request.setAttribute("canStartMerge", canStartMerge);
        return modelAndView;
    }
    
    public ModelAndView templateOperDes(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("common/template/templateOperDes");
        String from = ReqUtil.getString(request, "from", "");
        request.setAttribute("from", from);
        request.setAttribute("total", ReqUtil.getString(request, "total", "0"));
        return modelAndView;
    }

	public void setGovdocTemplateManager(GovdocTemplateManager govdocTemplateManager) {
		this.govdocTemplateManager = govdocTemplateManager;
	}

	public void setWapi(WorkflowApiManager wapi) {
		this.wapi = wapi;
	}

	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}

	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}

	public void setGovdocDocTemplateManager(GovdocDocTemplateManager govdocDocTemplateManager) {
		this.govdocDocTemplateManager = govdocDocTemplateManager;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
        this.formApi4Cap3 = formApi4Cap3;
    }
	
	public void setFormApi4Cap4(FormApi4Cap4 formApi4Cap4) {
        this.formApi4Cap4 = formApi4Cap4;
    }

	public void setSuperviseManager(SuperviseManager superviseManager) {
		this.superviseManager = superviseManager;
	}

	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}

	public void setAiApi(AIApi aiApi) {
		this.aiApi = aiApi;
	}

	public void setDocApi(DocApi docApi) {
		this.docApi = docApi;
	}

	public void setProjectApi(ProjectApi projectApi) {
		this.projectApi = projectApi;
	}

	public void setCapFormManager(CAPFormManager capFormManager) {
		this.capFormManager = capFormManager;
	}

	public void setCtpMainbodyManager(MainbodyManager ctpMainbodyManager) {
		this.ctpMainbodyManager = ctpMainbodyManager;
	}

	public void setTemplateCategoryManager(TemplateCategoryManager templateCategoryManager) {
		this.templateCategoryManager = templateCategoryManager;
	}

	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}

	public void setTemplateInsManager(TemplateInsManager templateInsManager) {
		this.templateInsManager = templateInsManager;
	}
	
    public static EnumManager getEnumManagerNew() {
		return enumManagerNew;
	}

	public static void setEnumManagerNew(EnumManager enumManagerNew) {
		GovdocTemplateController.enumManagerNew = enumManagerNew;
	}
	
	
	
	/********************************************* set注入 end ***************************************************************/
}
