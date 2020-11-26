/**
 * $Author: wuwl  $
 * $Rev: 2221 $
 * $Date:: 2012-08-21 16:42:02#$:
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.common.template.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.project.api.ProjectApi;
import com.seeyon.ctp.cap.api.bean.CAPFormBean;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.ContentConfig;
import com.seeyon.ctp.common.content.ContentViewRet;
import com.seeyon.ctp.common.content.mainbody.CtpContentAllBean;
import com.seeyon.ctp.common.content.mainbody.MainbodyStatus;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.po.template.CtpTemplateCategory;
import com.seeyon.ctp.common.template.manager.CollaborationTemplateManager;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.template.util.CtpTemplateUtil;
import com.seeyon.ctp.common.template.util.TemplateUtil;
import com.seeyon.ctp.common.template.utils.TemplateContentUtil;
import com.seeyon.ctp.common.template.vo.TemplateCategory;
import com.seeyon.ctp.common.template.vo.TemplateCategoryComparator;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
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
import com.seeyon.ctp.util.annotation.CheckRoleAccess;
import com.seeyon.ctp.workflow.engine.enums.BPMSeeyonPolicySetting;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.common.taglibs.functions.Functions;


/**
 * <p>Title: 协同模版</p>
 * <p>Description: Collaboration CRUD!</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: seeyon.com</p>
 */
public class CollaborationTemplateController extends BaseController {

	private static Log log = LogFactory.getLog(CollaborationTemplateController.class);

	private CollaborationTemplateManager collaborationTemplateManager;
	private WorkflowApiManager    wapi ;
	private OrgManager            orgManager;
	private ProjectApi projectApi;
    private AttachmentManager     attachmentManager;
    private TemplateManager templateManager;
	private CAPFormManager capFormManager;
	private EnumManager enumManagerNew;
	private ColManager colManager;

	public void setColManager(ColManager colManager) {
		this.colManager = colManager;
	}

	public CAPFormManager getCapFormManager() {
        return capFormManager;
    }

    public void setCapFormManager(CAPFormManager capFormManager) {
        this.capFormManager = capFormManager;
    }

    private PermissionManager permissionManager;
    
	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}

	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}
	
	public ProjectApi getProjectApi() {
		return projectApi;
	}

	public void setProjectApi(ProjectApi projectApi) {
		this.projectApi = projectApi;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	public void setWapi(WorkflowApiManager wapi) {
		this.wapi = wapi;
	}

	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}
	public void setCollaborationTemplateManager(CollaborationTemplateManager collaborationTemplateManager) {
	    this.collaborationTemplateManager = collaborationTemplateManager;
	}

    public void setEnumManagerNew(EnumManager enumManagerNew) {
        this.enumManagerNew = enumManagerNew;
    }

    /**
	 * 	协同模版首页访问页面
	 */
	@CheckRoleAccess(roleTypes = { Role_NAME.TtempletManager,Role_NAME.AccountAdministrator})
	public ModelAndView indexFrame(HttpServletRequest request,HttpServletResponse response) throws Exception {
	    request.setAttribute("isAccountAdmin", collaborationTemplateManager.isAccountAdmin());
		return new ModelAndView("common/template/indexFrame");
	}

    /**
     *  新建协同模版布局页面
     */
	@CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement,Role_NAME.AccountAdministrator,Role_NAME.TtempletManager,Role_NAME.InfoManager})
    public ModelAndView templateSysMgr(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 模版类型 0 协同模版， 1公文模版
        int categoryType = ReqUtil.getInt(request, "categoryType");
        String categoryId = request.getParameter("categoryId");
        if (StringUtil.checkNull(categoryId)) {
            categoryId = String.valueOf(categoryType);
        }
        ModelAndView mav = new ModelAndView("common/template/templateSysMgr");
        List<CtpTemplateCategory> categories = new ArrayList<CtpTemplateCategory>();
        if (ModuleType.collaboration.getKey() == categoryType) {
            categories = getCollaborationTemplate();
        } else if (ModuleType.edoc.getKey() == categoryType || ModuleType.edocRec.getKey() == categoryType
                || ModuleType.edocSend.getKey() == categoryType || ModuleType.edocSign.getKey() == categoryType) {
    		//权限判断,没有公文的管理员权限直接返回
    		boolean hasAuth=AppContext.hasResourceCode("F07_edocSystem");//管理员权限
    		boolean hasAuth1=AppContext.hasResourceCode("F07_edocSystem1");//管理员账号登录

    		if(!hasAuth && !hasAuth1 && !AppContext.getCurrentUser().isAdministrator()){
    			return null;
    		}
            categories = getEdocTemplate();
        }else if(ModuleType.info.getKey() == categoryType){
        	
        }
        request.setAttribute("isAdmin", collaborationTemplateManager.isAccountAdmin());
        request.setAttribute("categoryType", categoryType);
        request.setAttribute("categoryId", categoryId);
        List<TemplateCategory> templateCategories = new ArrayList<TemplateCategory>(categories.size());
        for (CtpTemplateCategory category : categories) {
            templateCategories.add(new TemplateCategory(category));
        }
        request.setAttribute("fftemplateTree", templateCategories);
        return mav;
    }

    private List<CtpTemplateCategory> getCollaborationTemplate() throws BusinessException {
        long orgAccountId = AppContext.currentAccountId();
        List<CtpTemplateCategory> categories = templateManager.getCategoryByAuth(orgAccountId);
        if(Strings.isNotEmpty(categories)){
        	log.info("分类数量2="+categories.size());
        }
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
            categories.add(root);
        for (CtpTemplateCategory ctpTemplateCategory : categories) {
            if (formType.equals(ctpTemplateCategory.getParentId())) {
                ctpTemplateCategory.setParentId(collaborationType);
            }
        }
        Collections.sort(categories, new TemplateCategoryComparator());
        return categories;
    }
    
    private List<CtpTemplateCategory> getEdocTemplate() throws BusinessException {
        long orgAccountId = AppContext.currentAccountId();
        List<CtpTemplateCategory> categories = new ArrayList<CtpTemplateCategory>();
        List<CtpTemplateCategory> edocRec = this.collaborationTemplateManager.getCategory(orgAccountId,
                ModuleType.edocRec.ordinal());
        List<CtpTemplateCategory> edocSend = this.collaborationTemplateManager.getCategory(orgAccountId,
                ModuleType.edocSend.ordinal());
        List<CtpTemplateCategory> sginReport = this.collaborationTemplateManager.getCategory(orgAccountId,
                ModuleType.edocSign.ordinal());
        categories.addAll(edocRec);
        categories.addAll(edocSend);
        categories.addAll(sginReport);
        // 公文模版类型根结点
        Long edocType = Long.parseLong(String.valueOf(ModuleType.edoc.getKey()));
        CtpTemplateCategory edocNode = new CtpTemplateCategory(edocType,
                ResourceUtil.getString("template.categorytree.edoctemplate.label"), null);
        categories.add(edocNode);
        //
        Long edocSendType = Long.parseLong(String.valueOf(ModuleType.edocSend.getKey()));
        CtpTemplateCategory edocSendNode = new CtpTemplateCategory(edocSendType,
                ResourceUtil.getString("template.categorytree.senddoctemplate.label"), edocType);
        categories.add(edocSendNode);
        // 
        Long edocRecType = Long.parseLong(String.valueOf(ModuleType.edocRec.getKey()));
        CtpTemplateCategory edocRecNode = new CtpTemplateCategory(edocRecType,
                ResourceUtil.getString("template.categorytree.recidoctemplate.label"), edocType);
        categories.add(edocRecNode);
        //
        Long edocSignType = Long.parseLong(String.valueOf(ModuleType.edocSign.getKey()));
        CtpTemplateCategory sginReportNode = new CtpTemplateCategory(edocSignType,
                ResourceUtil.getString("template.categorytree.signdoctemplate.label"), edocType);
        categories.add(sginReportNode);
        return categories;
    }

	/**
	 *	新建、修改协同模版页面，修改包含数据回填
	 */
    @CheckRoleAccess(roleTypes = { Role_NAME.TtempletManager,Role_NAME.AccountAdministrator})	
	@SuppressWarnings("unchecked")
	public ModelAndView systemNewTemplate(HttpServletRequest request,HttpServletResponse response) throws Exception {
		ModelAndView modelAndView = new ModelAndView("common/template/systemNewTemplate");
		String message = "" ;
		String from = request.getParameter("from") ;
		String id = request.getParameter("id") ;
		//设置类型
		request.setAttribute("categoryType", ModuleType.collaboration.ordinal());
		//设置选择当前分类
		String categoryId = request.getParameter("categoryId");
		User user = AppContext.getCurrentUser();
		//设置默认节点
		Map<String,String> defaultNodeMap =this.getColDefaultNode(user.getLoginAccount());
		modelAndView.addObject("defaultNodeName",defaultNodeMap.get("defaultNodeName"));
		modelAndView.addObject("defaultNodeLable",defaultNodeMap.get("defaultNodeLable"));
		modelAndView.addObject("hasWorkflowAdvanced", AppContext.hasPlugin("workflowAdvanced"));

		if(Strings.isNotBlank(categoryId)){
			modelAndView.addObject("categoryId",categoryId);
		}

        List<String[]> deadlines = new ArrayList<String[]>();
        List<CtpEnumItem> collaborationDeadlines = enumManagerNew.getEnumItems(EnumNameEnum.collaboration_deadline,true);
        if (Strings.isNotEmpty(collaborationDeadlines)) {
            for (CtpEnumItem item : collaborationDeadlines) {
                String[] deadline = new String[2];
                deadline[0] = item.getValue();
                deadline[1] = ResourceUtil.getString(item.getLabel());
                deadlines.add(deadline);
            }
        }
        modelAndView.addObject("deadlines", deadlines);
		
		if (Strings.isBlank(id)) {
			modelAndView.addObject("newBusiness","1");
			modelAndView.addObject("id",UUIDLong.longUUID());
			collaborationTemplateManager.newCollaborationTemplate(modelAndView,
					user, from);
		} else {
			modelAndView.addObject("newBusiness","0");
			modelAndView.addObject("id",id);
			message = collaborationTemplateManager.updateCollaborationTemplate(
					user, modelAndView, from, id);
			if (message.startsWith("success")) {
				if(message.endsWith("workflow")){
					TemplateContentUtil.contentNew(ModuleType.collaboration,null);
					//回填工作流需要设置的东西
					setWorkFlowInfo(Long.parseLong(id));
					List<CtpContentAllBean> cobj1 = (List)AppContext.getRequestContext("contentList") ;
					if(null != cobj1){
						String content = cobj1.get(0).getContentHtml();
						if(Strings.isNotBlank(content)){
							if(content.indexOf("contentType:'html'")> -1){
								String replace = content.replace("contentType:'html'", "contentType:'html',toolbarSet:'BasicAdmin'");
								cobj1.get(0).setContentHtml(replace);
							}
						}
					}
					return modelAndView;
				}
				ContentViewRet context = TemplateContentUtil.contentView(
						ModuleType.collaboration, Long.parseLong(id),
						null,CtpContentAllBean.viewState__editable,"1");
				if(!message.endsWith("text")){
					//回填工作流需要设置的东西
					setWorkFlowInfo(Long.parseLong(id));
				}
				List<CtpContentAllBean> cobj2 = (List)AppContext.getRequestContext("contentList") ;
				if(null != cobj2){
					String content = cobj2.get(0).getContentHtml();
					if(Strings.isNotBlank(content)){
						if(content.indexOf("contentType:'html'")> -1){
							String replace = content.replace("contentType:'html'", "contentType:'html',toolbarSet:'BasicAdmin'");
							cobj2.get(0).setContentHtml(replace);
						}
					}
				}
				return modelAndView;
			} else {
				response.getWriter().write(message);
				return modelAndView;
			}
		}
		TemplateContentUtil.contentNew(ModuleType.collaboration,null);
		List<CtpContentAllBean> cobj3 = (List)AppContext.getRequestContext("contentList") ;
		if(null != cobj3){
			String content = cobj3.get(0).getContentHtml();
			if(content.indexOf("contentType:'html'")> -1){
				String replace = content.replace("contentType:'html'", "contentType:'html',toolbarSet:'BasicAdmin'");
				cobj3.get(0).setContentHtml(replace);
				//AppContext.putRequestContext("contentList", cobj);
			}
		}
		return modelAndView;
	}
	
	/**
	 *	新建、修改信息模版页面，修改包含数据回填
	 */
	@SuppressWarnings("unchecked")
	public ModelAndView systemNewInfoTemplate(HttpServletRequest request,HttpServletResponse response) throws Exception {
		ModelAndView modelAndView = new ModelAndView("common/govtemplate/systemNewInfoTemplate");
		String message = "" ;
		String from = request.getParameter("from") ;
		String id = request.getParameter("id") ;
		//设置类型
		request.setAttribute("categoryType", ModuleType.info.ordinal());
		//设置选择当前分类
		String categoryId = request.getParameter("categoryId");
		modelAndView.addObject("categoryId",ModuleType.info.getKey());
		
		User user = AppContext.getCurrentUser();
		modelAndView.addObject("newBusiness","1");
		modelAndView.addObject("id",UUIDLong.longUUID());
		//formmanager.getFormListForOuter();
		return modelAndView;
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
	
	public ModelAndView systemCollaborationTemplateList(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView modelAndView = new ModelAndView("common/template/templateSysMgr");
		Map<String, String> params = new HashMap<String, String>();
		FlipInfo flipInfo = new FlipInfo();

		User user = AppContext.getCurrentUser();
		long orgAccountId = user.getLoginAccount();
		String categoryType = request.getParameter("categoryType");
		String categoryId = request.getParameter("categoryId");

		if(StringUtils.isNotBlank(categoryId)) {
			params.put("categoryType", categoryType);
			params.put("categoryId", categoryId);
			params.put("orgAccountId", String.valueOf(orgAccountId));
			templateManager.getSystemTempletes(flipInfo,params);
			request.setAttribute("ffcollaborationTemplateTable", flipInfo);
		}
		// 鏌ヨ妯＄増鍒嗙被鏍�
		List<CtpTemplateCategory> categories = collaborationTemplateManager.getCategory(orgAccountId);
		List<Long> canManager = collaborationTemplateManager.getCanManager(user.getId(), orgAccountId, categories);
		request.setAttribute("fftemplateTree", categories);

		modelAndView.addObject("categoryType", categoryType);
		return modelAndView;
	}
	
	
	
	/**
	 *	保存协同模版
	 */
	@CheckRoleAccess(roleTypes = { Role_NAME.TtempletManager,Role_NAME.AccountAdministrator})
	public ModelAndView saveCollaborationTemplate(HttpServletRequest request,HttpServletResponse response) throws Exception {
       
		collaborationTemplateManager.saveCollaborationTemplate();
	    
		Map  params = ParamUtil.getJsonDomain("templateMainData");
		String categoryId = params.get("categoryId").toString();
		String categoryType = params.get("categoryType").toString();
		return redirectModelAndView("/collTemplate/collTemplate.do?method=templateSysMgr&categoryId="+categoryId+
				"&categoryType="+categoryType+com.seeyon.ctp.common.taglibs.functions.Functions.csrfSuffix(),"parent");
	}
	
	/**
     *  编辑协同模版分类
     */
	@CheckRoleAccess(roleTypes = {Role_NAME.AccountAdministrator, Role_NAME.TtempletManager})
    public ModelAndView editSystemCategory(HttpServletRequest request,HttpServletResponse response) throws Exception {
        String categoryId = request.getParameter("categoryId");
        if (!StringUtil.checkNull(categoryId)) {
            CtpTemplateCategory category = collaborationTemplateManager.getCategoryById(Long.parseLong(categoryId));
            if(category != null){
                String options = collaborationTemplateManager.getCategory2HTML(AppContext.getCurrentUser()
                        .getLoginAccount(), Long.valueOf(category.getId()));
                request.setAttribute("categoryHTML", options);
                request.setAttribute("parentId", category.getParentId());
                Map<String, Object> resMap = new HashMap<String, Object>();
                resMap.put("id", categoryId);
                resMap.put("name", category.getName());
                resMap.put("sort", category.getSort());
                resMap.put("description", "null".equals(category.getDescription()) ? "" : category.getDescription());
                String[] auths = collaborationTemplateManager.getCategoryAuths(category);
                request.setAttribute("authtxt", auths[1]);
                request.setAttribute("authvalue", auths[0]);
                request.setAttribute("ffcategoryForm", resMap);
                request.setAttribute("categoryType", category.getType());
                //当前登陆人是否是单位管理员
                User user = AppContext.getCurrentUser();
                request.setAttribute("canAdmin", user.isAdministrator());
                request.setAttribute("isAdmin", user.isAdministrator());
            }
        }
        return new ModelAndView("common/template/systemCategory");
    }

    /**
     *  新建协同模版分类页面
     */
    @CheckRoleAccess(roleTypes = { Role_NAME.TtempletManager,Role_NAME.AccountAdministrator})
    public ModelAndView showSystemCategory(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String parentCategoryId = request.getParameter("parentId");
        String canAdmin = request.getParameter("canAdmin");
        if (!StringUtil.checkNull(parentCategoryId)) {
            String options = collaborationTemplateManager.getCategory2HTML(AppContext.getCurrentUser()
                    .getLoginAccount(), null);
            request.setAttribute("categoryHTML", options);
        }
        request.setAttribute("isAdmin", collaborationTemplateManager.isAccountAdmin());
        request.setAttribute("parentId", parentCategoryId);
        request.setAttribute("canAdmin", canAdmin);
        return new ModelAndView("common/template/systemCategory");
    }
    @CheckRoleAccess(roleTypes = { Role_NAME.TtempletManager,Role_NAME.AccountAdministrator})
    public ModelAndView showCategoryTree(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<CtpTemplateCategory> categories = new ArrayList<CtpTemplateCategory>();
        int categoryType = ReqUtil.getInt(request, "categoryType");
        if (ModuleType.collaboration.getKey() == categoryType || ModuleType.form.getKey() == categoryType) {
            categories = getCollaborationTemplate();
        } else if (ModuleType.edoc.getKey() == categoryType || ModuleType.edocRec.getKey() == categoryType
                || ModuleType.edocSend.getKey() == categoryType || ModuleType.edocSign.getKey() == categoryType) {
            categories = getEdocTemplate();
        }
        request.setAttribute("fftemplateTree", categories);
        return new ModelAndView("common/template/templateTree");
    }
    
	/**
	 * **********************************************************************************************************
	 * 此类的工具方法，暂时先注释掉，等确定好表结构后再确定是否还使用
	 * **********************************************************************************************************
	 */
    public ModelAndView templateOperDes(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("common/template/templateOperDes");
        String from = ReqUtil.getString(request, "from", "");
        request.setAttribute("from", from);
        request.setAttribute("total", ReqUtil.getString(request, "total", "0"));
        return modelAndView;
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
        String senderName =  ColUtil.getMemberName(memberId);
        request.setAttribute("senderInfo", senderName + " ( " + cd + " ) ");
        //summary信息
        String summayInfo = template.getSummary();
        ColSummary summary = XMLCoder.decoder(summayInfo,ColSummary.class);
        request.setAttribute("summary", summary);
        //属性页签的几个属性 流程期限 提醒 关联项目 预归档到 转发 改变流程 修改正文 归档
        String deadLine = ColUtil.getDeadLineName(summary.getDeadline());
        request.setAttribute("deadLine", deadLine);
        String remind = ColUtil.getAdvanceRemind(String.valueOf(null != summary.getAdvanceRemind() ? summary
                .getAdvanceRemind().intValue() : 0));
        request.setAttribute("remind", remind);
        if (null != summary.getArchiveId() && Strings.isNotBlank(summary.getArchiveId().toString())) {
        	String archiveAllName = ColUtil.getArchiveAllNameById(summary.getArchiveId());
        	String archiveName = ColUtil.getArchiveNameById(summary.getArchiveId());
            request.setAttribute("archiveName", archiveName);
            request.setAttribute("archiveAllName", archiveAllName);
            
        }
         //附件归档全路径
        if(summary.getAttachmentArchiveId() != null){
        	String attachmentArchiveAllName = ColUtil.getArchiveAllNameById(summary.getAttachmentArchiveId());
        	String attachmentArchiveName = ColUtil.getArchiveNameById(summary.getAttachmentArchiveId());
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
        	log.info("查看模板rightID="+template.getSubject()+"***rightId="+rightId);
        	if(Strings.isNotBlank(rightId) && "-1.-1".equals(rightId)){
        		rightId ="-1";
        	}
        	colManager.addFormRightIdToFormCache(rightId, template.getFormAppId());
        	if(cap4Form){
        		modelAndView.addObject("cap4Form", cap4Form);
        	}else{
        		context = TemplateContentUtil.contentView(ModuleType.collaboration, Long.parseLong(templateId), null,
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
	     boolean canAnyDealMerge = ColUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE,summary);
	     boolean canPreDealMerge = ColUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE,summary);
	     boolean canStartMerge = ColUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.START_MERGE,summary);
	     request.setAttribute("canAnyDealMerge", canAnyDealMerge);
	     request.setAttribute("canPreDealMerge", canPreDealMerge);
	     request.setAttribute("canStartMerge", canStartMerge);
        return modelAndView;
    }
   
    /**
     * 我的模板参看更多
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView moreTemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("common/template/moreTemplate");
        User user = AppContext.getCurrentUser();
        // 收文
        List<CtpTemplate> shouTempleteList = new ArrayList<CtpTemplate>();
        // 发文
        List<CtpTemplate> faTempleteList = new ArrayList<CtpTemplate>();
        // 签报
        List<CtpTemplate> qianTempleteList = new ArrayList<CtpTemplate>();
        //信息报送
        List<CtpTemplate> infoTempleteList = new ArrayList<CtpTemplate>();
        // 个人模板
        List<CtpTemplate> personalTempletes = new ArrayList<CtpTemplate>();
        // 全部种类
       
        String searchValue = ReqUtil.getString(request, "searchValue","");
        String columnsName = ReqUtil.getString(request, "columnsName");
        String category = ReqUtil.getString(request, "category");
        Long orgAccountId  = user.getLoginAccount();
        //当前用户模版所在的所有单位
        Map<Long,String> accounts = new HashMap<Long,String>();
            
        String isShowOuter = "true";
        //保存我的模板个性化查看方式-扁平结构
        collaborationTemplateManager.saveCustomViewType("1");
        String fragmentId = ReqUtil.getString(request, "fragmentId");
        if (Strings.isNotBlank(fragmentId)) {
            category =  "-1";
        	if(AppContext.hasPlugin("collaboration")){
        		category += ",1,2";
        	}
        	if(AppContext.hasPlugin("edoc")){
        		category += ",4,19,20,21";
        	}
        	if(AppContext.hasPlugin("infosend")){
        		category += ",32";
        	}
            String ordinal = ReqUtil.getString(request, "ordinal");
            category = collaborationTemplateManager.getMoreTemplateCategorys(category, fragmentId, ordinal);         
        }
        if (Strings.isBlank(category)) {
            Map params = ParamUtil.getJsonParams();
            searchValue = String.valueOf(params.get("searchValue"));
            category = String.valueOf(params.get("category"));
            String _orgAccountId = String.valueOf(params.get("orgAccountId"));
            
            if ("1".equals(_orgAccountId) || Strings.isBlank(_orgAccountId)) {
            	isShowOuter = "true";
            } else {
                orgAccountId = Long.valueOf(_orgAccountId);
            	isShowOuter = "false";
            }
        }
        V3xOrgAccount logonOrgAccount = orgManager.getAccountById(orgAccountId);
        if (!accounts.containsKey(logonOrgAccount.getId())) {
        	accounts.put(logonOrgAccount.getId(), logonOrgAccount.getName());
        }
        request.setAttribute("searchValue",
                Strings.toHTML(CtpTemplateUtil.unescape(searchValue),false));
       
        
        templateManager.transMergeCtpTemplateConfig(user.getId());

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", user.getId());
    	params.put("accountId", orgAccountId);
        params.put("category", category);
        if (Strings.isNotBlank(searchValue)) {
            params.put("subject", searchValue);
        }
        FlipInfo flipInfo = null;
        List<CtpTemplate> allTempleteConfig = templateManager.getMyConfigCollTemplate(flipInfo, params);
        
        Map<String, List<CtpTemplate>> collTempleteCategory = new HashMap<String, List<CtpTemplate>>();
        boolean isPluginEdoc = SystemEnvironment.hasPlugin("edoc");
        boolean isEdoc = Functions.isEnableEdoc();
        Long categoryId = null;
        CtpTemplateCategory ctpCategory = null;
        List<CtpTemplate> list = null;
        String creatorAlt = null;
        Map<Long, String> templeteCreatorAlt = new HashMap<Long, String>();
        Map<Long, String> templeteIcon = new HashMap<Long, String>();
        
        List<CtpTemplateCategory> categoryList = new ArrayList<CtpTemplateCategory>();
		Map<Long, String> cashName = new HashMap<Long, String>();
        for (CtpTemplate config : allTempleteConfig) {
        	
            // 处理模板名包含特殊字符的情况
        	if(Strings.isBlank(config.getSubject())){//OA-61910 升级前已经停用、删除的模板升级后却出现了，点击提示已经删除；
        		continue;
        	}
            config.setSubject(config.getSubject());
            
            Long cAccountId = config.getOrgAccountId();
            if (!orgAccountId.equals(cAccountId)) {
            	
                if (!accounts.containsKey(cAccountId)) {
                    V3xOrgAccount outOrgAccount = orgManager.getAccountById(cAccountId);
                	accounts.put(outOrgAccount.getId(), outOrgAccount.getName());
                }
        		if (!"true".equals(isShowOuter)) {
        			continue;
        		}
        	}
        	
            int type = config.getModuleType().intValue();
            creatorAlt = showTempleteCreatorAlt(config.getMemberId(), cAccountId);
            
            if (creatorAlt != null) {
            	if(config.getFormAppId() != null){
        	    	if(cashName.get(config.getFormAppId()) == null){
        	    		CAPFormBean fBean = capFormManager.getForm(config.getFormAppId());
        	    		if(fBean != null){
        	    			cashName.put(config.getFormAppId(), fBean.getFormName());
        	    		}
            	    }
					StringBuilder sb = new StringBuilder();
        	    	sb.append(ResourceUtil.getString("template.form.name")).append("：");
        	    	sb.append(Strings.getSafeLimitLengthString(cashName.get(config.getFormAppId()),38,"...")).append("\n");
        	    	sb.append(creatorAlt);
        	    	creatorAlt = sb.toString();
            	}
            }
            
            if (creatorAlt != null) {
                templeteCreatorAlt.put(config.getId(), creatorAlt);
            }
            String icon = "collaboration_16";
            if (type == ModuleType.edocSend.ordinal()
            		|| type == ModuleType.edocRec.ordinal()
            		|| type == ModuleType.edocSign.ordinal() 
                    || type == ModuleType.edoc.ordinal()) {
                icon = "red_text_template_16";
            } else {
                if ("text".equals(config.getType())) {
                    icon = "format_template_16";
                } else if ("template".equals(config.getType()) && String.valueOf(MainbodyType.FORM.getKey()).equals(config.getBodyType())) {
                    icon = "form_temp_16";
                } else if ("workflow".equals(config.getType())) {
                    icon = "flow_template_16";
                }
            }
            templeteIcon.put(config.getId(), icon);
            if ((type == ModuleType.collaboration.ordinal()
                    || type == ModuleType.form.ordinal()) && config.isSystem()) {
                categoryId = config.getCategoryId();
                ctpCategory = collaborationTemplateManager.getCategoryById(categoryId);
                if (ctpCategory != null) {
                    list = collTempleteCategory.get(ctpCategory.getName());
                    if (list == null) {
                        list = new ArrayList<CtpTemplate>();
                    }
                    list.add(config);
                    collTempleteCategory.put(ctpCategory.getName(), list);
                    if (!categoryList.contains(ctpCategory)) {
                        categoryList.add(ctpCategory);
                    }
                }
            }else if(type == ModuleType.info.ordinal() && config.isSystem()){//信息报送
            	infoTempleteList.add(config);
            }else if (type == 1 || !config.isSystem()) {
                personalTempletes.add(config);
            } else if (isEdoc && isPluginEdoc) {
                if (type == ModuleType.edocRec.ordinal()) {
                    shouTempleteList.add(config);
                } else if (type == ModuleType.edocSend.ordinal()) {
                    faTempleteList.add(config);
                } else if (type == ModuleType.edocSign.ordinal()) {
                    qianTempleteList.add(config);
                }
            }
        }
        //排序模版分类
        Collections.sort(categoryList, new TemplateCategoryComparator());
        // 协同和表单
        List<String> categoryNames = new ArrayList<String>();
        for (CtpTemplateCategory ctpTemCategory : categoryList) {
            if (!categoryNames.contains(ctpTemCategory.getName())){
                categoryNames.add(ctpTemCategory.getName());
            }
        }
        
        modelAndView.addObject("category", category);
        modelAndView.addObject("infoTemplete", infoTempleteList);
        modelAndView.addObject("shouTemplete", shouTempleteList);
        modelAndView.addObject("faTemplete", faTempleteList);
        modelAndView.addObject("qianTemplete", qianTempleteList);
        modelAndView.addObject("collTempleteCategory", collTempleteCategory);
        modelAndView.addObject("categoryNames", categoryNames);
        modelAndView.addObject("personalTempletes", personalTempletes);
        modelAndView.addObject("templeteCreatorAlt", templeteCreatorAlt);
        modelAndView.addObject("templeteIcon", templeteIcon);
        modelAndView.addObject("columnsName", columnsName);
        modelAndView.addObject("accounts",accounts);
        modelAndView.addObject("isShowOuter",isShowOuter);
        modelAndView.addObject("orgAccountId", orgAccountId.toString());
        return modelAndView;
    }

	
    
    /***
     * 显示模板的创建者等详细信息
     */
    private String showTempleteCreatorAlt(long memberId, long accountId) {
        try {
            V3xOrgMember member = orgManager.getMemberById(memberId);
            if (member == null) {
                return null;
            }
            
            String s = null;
            if(member.getIsAdmin()){
                s = Functions.showMemberName(member);
            }
            else{
                s = Functions.showMemberAlt(member);
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append(ResourceUtil.getString("common.creater.label")).append(" : ");
            sb.append(s);
            
            return sb.toString();
        }
        catch (BusinessException e) {
            return null;
        }
    }

    /**
     * 我的模板配置模板
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView showTemplateConfig(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("common/template/templeteConfigFrame");
        return modelAndView;
    }
    
    /**
     * 我的模板排序页面
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView showTempleteSort(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("common/template/templeteSort");
        // 全部种类
        String category = "-1,1,2,19,20,21,4,32,401,402,404";
        boolean isPluginEdoc =  SystemEnvironment.hasPlugin("edoc");
        boolean isEdoc = Functions.isEnableEdoc();
        if (!isEdoc || !isPluginEdoc) {
            category = "-1,1,2";
        }
        List<CtpTemplate> templeteList = templateManager.getPersonalTemplete(category, -1, false);
        modelAndView.addObject("templeteList", templeteList);
        return modelAndView;
    }
    
    /**
     * 我的模板模板配置
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView templateConfig(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 全部种类
        String category = "-1,1,2,4,19,20,21,32";
        boolean isPluginEdoc =  SystemEnvironment.hasPlugin("edoc");
        boolean isEdoc = Functions.isEnableEdoc();
        if (!isEdoc || !isPluginEdoc || !AppContext.getCurrentUser().isV5Member()) {
            category = "-1,1,2";
        }
        return redirectModelAndView("/template/template.do?method=templateChoose&templateChoose=true&category=" + category);
    }
    
    /**
     * 部门模板更多
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView moreDepartmentTemplate(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = new ModelAndView("common/template/moreDepartmentTemplate");
        long departmentId = AppContext.getCurrentUser().getDepartmentId();
        String deptIdParam = request.getParameter("departmentId");
        if(Strings.isNotBlank(deptIdParam)){
        	departmentId = Long.valueOf(deptIdParam);
        }
        Long orgAccountId = AppContext.currentAccountId();

        List<CtpTemplateCategory> templeteCategorysColl = collaborationTemplateManager.getCategory(orgAccountId,
                ModuleType.collaboration.ordinal());
        templeteCategorysColl.addAll(collaborationTemplateManager.getCategory(orgAccountId,
                ModuleType.form.ordinal()));
        
        
        List<ModuleType> moduleTypes = new ArrayList<ModuleType>();
        moduleTypes.add(ModuleType.collaboration);
        moduleTypes.add(ModuleType.form);
        List<CtpTemplate> templeteListColl = templateManager.getSystemTempletesByOrgEntity(
                V3xOrgEntity.ORGENT_TYPE_DEPARTMENT, departmentId,moduleTypes);
        Long categoryId = null;
        CtpTemplateCategory ctpCategory = null;
        List<CtpTemplate> list = null;
        String creatorAlt = null;
        Map<String, List<CtpTemplate>> templete = new HashMap<String, List<CtpTemplate>>();
        Map<Long, String> templeteCreatorAlt = new HashMap<Long, String>();
        Map<Long, String> templeteIcon = new HashMap<Long, String>();
		Map<Long, String> cashName = new HashMap<Long, String>();
        for (CtpTemplate config : templeteListColl) {
            creatorAlt = showTempleteCreatorAlt(config.getMemberId(), config.getOrgAccountId());
            
            if (creatorAlt != null) {
            	if(config.getFormAppId() != null){
        	    	if(cashName.get(config.getFormAppId()) == null){
        	    	    CAPFormBean fBean = capFormManager.getForm(config.getFormAppId());
        	    		if(fBean != null){
        	    			cashName.put(config.getFormAppId(), fBean.getFormName());
        	    		}
            	    }
					StringBuilder sb = new StringBuilder();
        	    	sb.append(ResourceUtil.getString("template.form.name")).append("：");
        	    	sb.append(Strings.getSafeLimitLengthString(cashName.get(config.getFormAppId()),38,"...")).append("\n");
        	    	sb.append(creatorAlt);
        	    	creatorAlt = sb.toString();
            	}
            }
            
            if (creatorAlt != null) {
                templeteCreatorAlt.put(config.getId(), creatorAlt);
            }
            String icon = "collaboration_16";
            if (config.getModuleType() == ModuleType.edocSend.ordinal() || config.getModuleType() == ModuleType.edocRec.ordinal()
                    || config.getModuleType() == ModuleType.edocSign.ordinal() || config.getModuleType() == ModuleType.edoc.ordinal()) {
                icon = "red_text_template_16";
            } else {
                if ("text".equals(config.getType())) {
                    icon = "format_template_16";
                } else if ("template".equals(config.getType()) && "20".equals(config.getBodyType())) {
                    icon = "form_temp_16";
                } else if ("workflow".equals(config.getType())) {
                    icon = "flow_template_16";
                }
            }
            templeteIcon.put(config.getId(), icon);
            categoryId = config.getCategoryId();
            ctpCategory = collaborationTemplateManager.getCategoryById(categoryId);
            if (ctpCategory != null) {
                list = templete.get(ctpCategory.getName());
                if (list == null) {
                    list = new ArrayList<CtpTemplate>();
                }
                list.add(config);
                templete.put(ctpCategory.getName(), list);
            }
        }
        Set<String> names = templete.keySet();
        modelAndView.addObject("categoryNames", names);
        modelAndView.addObject("collTempleteCategory", templete);
        if (Functions.isEnableEdoc()) {
        	List<ModuleType> moduleTypes1 = new ArrayList<ModuleType>();
        	moduleTypes1.add(ModuleType.edocRec);
            List<CtpTemplate> templeteListRec = templateManager.getSystemTempletesByOrgEntity(
                    V3xOrgEntity.ORGENT_TYPE_DEPARTMENT, departmentId,moduleTypes1);
            for(int cou  = 0;cou < templeteListRec.size(); cou ++){//发文模板图标
            	CtpTemplate _config = templeteListRec.get(cou);
                String icon = "red_text_template_16";
                templeteIcon.put(_config.getId(), icon);
            }
            modelAndView.addObject("templeteListRec", templeteListRec);
            List<ModuleType> moduleTypes2 = new ArrayList<ModuleType>();
            moduleTypes2.add(ModuleType.edocSend);
            List<CtpTemplate> templeteListSend = templateManager.getSystemTempletesByOrgEntity(
                    V3xOrgEntity.ORGENT_TYPE_DEPARTMENT, departmentId,moduleTypes2);
            for(int cou  = 0;cou < templeteListSend.size(); cou ++){//发文模板图标
            	CtpTemplate _config = templeteListSend.get(cou);
                String icon = "red_text_template_16";
                templeteIcon.put(_config.getId(), icon);
            }
            modelAndView.addObject("templeteListSend", templeteListSend);
            List<ModuleType> moduleTypes3 = new ArrayList<ModuleType>();
            moduleTypes3.add(ModuleType.edocSign);
            List<CtpTemplate> templeteListSginReport = templateManager.getSystemTempletesByOrgEntity(
                    V3xOrgEntity.ORGENT_TYPE_DEPARTMENT, departmentId,moduleTypes3 );
            for(int cou  = 0;cou < templeteListSginReport.size(); cou ++){//发文模板图标
            	CtpTemplate _config = templeteListSginReport.get(cou);
                String icon = "red_text_template_16";
                templeteIcon.put(_config.getId(), icon);
            }
            modelAndView.addObject("templeteListSginReport", templeteListSginReport);
        }
        /**信息报送开始**/
        List<ModuleType> moduleTypes4 = new ArrayList<ModuleType>();
        moduleTypes4.add(ModuleType.info);
        List<CtpTemplate> templeteListInfo = templateManager.getSystemTempletesByOrgEntity(
                V3xOrgEntity.ORGENT_TYPE_DEPARTMENT, departmentId,moduleTypes4);
        for(int cou  = 0;cou < templeteListInfo.size(); cou ++){
        	CtpTemplate _config = templeteListInfo.get(cou);
            String icon = "red_text_template_16";
            templeteIcon.put(_config.getId(), icon);
        }
        modelAndView.addObject("templeteListInfo", templeteListInfo);
        /**信息报送结束**/
        modelAndView.addObject("templeteCreatorAlt", templeteCreatorAlt);
        modelAndView.addObject("templeteIcon", templeteIcon);
        return modelAndView;
    }
    
    //@CheckRoleAccess(roleTypes = { Role_NAME.TtempletManager})   525权限问题
    public ModelAndView selectSourceTemplate(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("common/template/selectSourceTemplate");
        request.setAttribute("categoryId", ReqUtil.getString(request, "categoryId"));
        request.setAttribute("templateId", ReqUtil.getString(request, "templateId"));
        return modelAndView;
    }
    /**
     * 显示打印选择框
     */
    public ModelAndView showPrintSelector(HttpServletRequest request,HttpServletResponse response) throws Exception{
    	//判断当前正文是否是表单模板
    	String isForm = request.getParameter("isForm");
    	ModelAndView mav = new ModelAndView("common/template/printTypeSelector");
    	mav.addObject("isForm", isForm);
    	return mav;

    }
    
    /**
	 * 个人模板 保存
	 * libing
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView saveTemplate(HttpServletRequest request,HttpServletResponse response) throws Exception {
		
		collaborationTemplateManager.saveTemplate();
		return null;
		
	}
	
	/**
	 * 管理员设置协同模板-高级
	 */
    public ModelAndView handleAdvance(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("common/template/advanceHtml");
        return mav;
    }
    
    public ModelAndView attachmentPigeonhole(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("common/template/attachmentPigeonhole");
        return mav;
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
}
