package com.seeyon.v3x.edoc.controller;

import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseTemplateRole;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.po.template.CtpTemplateAuth;
import com.seeyon.ctp.common.po.template.CtpTemplateCategory;
import com.seeyon.ctp.common.supervise.enums.SuperviseEnum;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.supervise.vo.SuperviseMessageParam;
import com.seeyon.ctp.common.supervise.vo.SuperviseSetVO;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.template.enums.TemplateEnum;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.template.util.CtpTemplateUtil;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgRole;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.common.web.login.CurrentUser;
import com.seeyon.v3x.edoc.constants.EdocOrgConstants;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocForm;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.event.EdocTemplateSaveEvent;
import com.seeyon.v3x.edoc.manager.EdocFormManager;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocManager;
import com.seeyon.v3x.edoc.manager.EdocMarkDefinitionManager;
import com.seeyon.v3x.edoc.manager.EdocSummaryManager;
import com.seeyon.v3x.edoc.manager.EdocSuperviseManager;
import com.seeyon.v3x.edoc.manager.EdocSwitchHelper;
import com.seeyon.v3x.edoc.util.DataUtil;
import com.seeyon.v3x.edoc.util.EdocSuperviseHelper;
import com.seeyon.v3x.edoc.util.EdocUtil;
import com.seeyon.v3x.edoc.webmodel.EdocFormModel;
import com.seeyon.v3x.edoc.webmodel.EdocMarkModel;

public class EdocTempleteController extends BaseController {
	
	private static Log LOGGER = LogFactory.getLog(EdocTempleteController.class);
	
	private TemplateManager templateManager;
	private WorkflowApiManager wapi;
	private AttachmentManager attachmentManager;
	private MainbodyManager ctpMainbodyManager;
	private CollaborationApi collaborationApi;
	
	private EdocSummaryManager edocSummaryManager;
	private DocApi docApi;

	public void setCtpMainbodyManager(MainbodyManager ctpMainbodyManager) {
		this.ctpMainbodyManager = ctpMainbodyManager;
	}

	public void setWapi(WorkflowApiManager wapi) {
		this.wapi = wapi;
	}

	public void setEdocSummaryManager(EdocSummaryManager edocSummaryManager) {
		this.edocSummaryManager = edocSummaryManager;
	}

	public void setCollaborationApi(CollaborationApi collaborationApi) {
        this.collaborationApi = collaborationApi;
    }
	
	public void setDocApi(DocApi docApi) {
        this.docApi = docApi;
    }
	
	private EdocSuperviseManager edocSuperviseManager;
	public void setEdocSuperviseManager(EdocSuperviseManager edocSuperviseManager) {
		this.edocSuperviseManager = edocSuperviseManager;
	}

	private OrgManager orgManager; 
	
	private EnumManager enumManagerNew;
	
	private EdocFormManager edocFormManager;
	
	private SuperviseManager superviseManager;
	private EdocMarkDefinitionManager edocMarkDefinitionManager;
	
//	private TempleteConfigManager templeteConfigManager;
	private AppLogManager appLogManager;
	
	
	
	public SuperviseManager getSuperviseManager() {
		return superviseManager;
	}


	public void setSuperviseManager(SuperviseManager superviseManager) {
		this.superviseManager = superviseManager;
	}


	public void setEnumManagerNew(EnumManager enumManager) {
        this.enumManagerNew = enumManager;
    }
	
	
	public void setEdocManager(EdocManager edocManager) {
		this.edocManager = edocManager;
	}
	public void setEdocMarkDefinitionManager(
			EdocMarkDefinitionManager edocMarkDefinitionManager) {
		this.edocMarkDefinitionManager = edocMarkDefinitionManager;
	}

	private EdocManager edocManager;
	public AppLogManager getAppLogManager() {
		return appLogManager;
	}
	public void setAppLogManager(AppLogManager appLogManager) {
		this.appLogManager = appLogManager;
	}

	public void setEdocFormManager(EdocFormManager edocFormManager)
	{
		this.edocFormManager=edocFormManager;
	}

	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}

	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}


	@Override
	public ModelAndView index(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		return null;
	}

	/**
	 * 设置是否为联合发文标志
	 *
	 */
	private void _setIsUnit(EdocSummary summary)
	{
		Long edocFormId=summary.getFormId();
		if(edocFormId!=null)
		{
			EdocForm ef=edocFormManager.getEdocForm(edocFormId);
			if(ef!=null)
			{
				summary.setIsunit(ef.getIsunit());
			}
		}
	}
	
    @CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView systemSaveTemplete(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
    	return saveTemplete(request, response);
    }
    
    public ModelAndView saveTemplete(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		User user = AppContext.getCurrentUser();


		String type = request.getParameter("type");
		//String from = request.getParameter("from");
		int isSys = request.getParameter("isSys") == null? 1 : Integer.parseInt(request.getParameter("isSys"));
		
		CtpTemplate templete = new CtpTemplate();
		templete.setDelete(false);
		Long categoryId = -1L;
		//如果调用系统模版，另存个人模版，记录系统模版id
        String originalTemplateId = request.getParameter("templeteId");
		
	    Integer categoryType = Integer.parseInt(request.getParameter("categoryType"));    
	    categoryId = (long)categoryType;
		
		Set<Long> memberSet = new HashSet<Long>();
		Timestamp createDate = new Timestamp(System.currentTimeMillis());

		try{
		    bind(request, templete);
		}catch (Exception e){
		    LOGGER.error("", e);
		}
		
		templete.setSubject(request.getParameter("templatename"));
		// 基准时长
        String referenceTime = request.getParameter("referenceTime");
        
        if(!TemplateEnum.Type.workflow.name().equals(type)) {
        	if (Strings.isBlank(referenceTime)) {
                //OA-26859 调用发文模板后将其存为个人模板，督办设置，基准时长没有保存（如图）
                //在发文页面中基准时长隐藏域name为 standardDuration
                String standardDuration = request.getParameter("standardDuration");
                if(Strings.isNotBlank(standardDuration)){
                    templete.setStandardDuration(Long.parseLong(standardDuration));
                }else{
                    templete.setStandardDuration(0L);
                }
                
            }
            else{
                templete.setStandardDuration(Long.parseLong(referenceTime));
            }
        }
        
        boolean isSave = templete.isNew();
        templete.setIdIfNew();
        templete.setModuleType(categoryType);
        templete.setState(0);
        
		String edocTable = request.getParameter("edoctable");
		EdocForm form = this.edocFormManager.getEdocForm(Long.parseLong(edocTable));
		//发文类型
		if(categoryType==2) {
			
			if(Strings.isNotBlank(edocTable)) {
				if(form != null) {
				    //将文单所属的类型  设置给  模板的子模块分类  ，不知道这里对不对
//					templete.setSubCategoryType(form.getSubType());
				    templete.setSubModuleType(form.getSubType());
				}
			}
		}
		
		String summary = null;
		/**
         * 协同模板：将把协同所有的信息作为模板保存，调用者不可修改流程，其他信心可以修改。
         * 格式模板：将把协同的正文模板保存，调用者只引用正文。
         * 流程模板：将把协同流程作为模板保存，调用者只能引用流程，不允许修改流程。
         */
		Timestamp now = new Timestamp(System.currentTimeMillis());
		String process_xml = request.getParameter("process_xml");
		String process_rulecontent = request.getParameter("process_rulecontent");
		
		long processTemplateId = -1;
		String processTemplateIdStr = request.getParameter("processTemplateId");
		if(Strings.isNotBlank(processTemplateIdStr)){
		    processTemplateId = Long.parseLong(processTemplateIdStr);
		}
		
		String bodyType=request.getParameter("bodyType");
		templete.setBodyType(String.valueOf(EdocHelper.getBodyTypeKey(bodyType)));
		
		// OA-17454  发文、收文、签报中新建公文，操作：1、保存草稿，2、待发中编辑，3、编辑时另存为个人模版，4、再次去调用刚才的个人模版，报错  
		//新建公文，存草稿后，再编辑，存为个人模板(需要之前草稿中的xml生成模板的workflowId)，再次调用该个人模板时才不会有错
		String processId = request.getParameter("processId");
		//processId不为空，且当process_xml为空时，表示从草稿箱中编辑，当用户没有改变选人XML时，process_xml是没有值的，所以调用工作流接口获得原来保存的XML
		if(Strings.isNotBlank(processId)&& Strings.isBlank(process_xml)){
		    process_xml = wapi.selectWrokFlowXml(processId);
		}
		String processEventJson = request.getParameter("process_event");
		// 不是正文格式，才保存流程
		if (!TemplateEnum.Type.text.name().equals(type) && Strings.isNotBlank(process_xml)) {
		    processTemplateId = wapi.saveWorkflowTemplate("edoc", 
                    templete.getSubject(), 
                    processTemplateId, 
                    process_xml, 
                    process_rulecontent, 
                    String.valueOf(user.getId()),
                    processEventJson);
		    
		    int edocType = Integer.parseInt(request.getParameter("edocType"));
		    int moduleType = 0;
		    if(edocType == 0){
		    	moduleType = ApplicationCategoryEnum.edocSend.key();
	        }else if(edocType == 1){
	        	moduleType = ApplicationCategoryEnum.edocRec.key();
	        }else if(edocType == 2){
	        	moduleType = ApplicationCategoryEnum.edocSign.key();
	        }
		    //非格式模板，需要保存节点权限的引用
		    EdocUtil.updatePermissinRef(moduleType, process_xml, "-1", String.valueOf(processTemplateId),user.getLoginAccount());
		}
		
		if(processTemplateId != -1){
		    templete.setWorkflowId(processTemplateId);
		}
		
		//格式模版修改的时候需要把流程删除
        if(TemplateEnum.Type.text.name().equals(type) && templete.getWorkflowId() != null){
            templete.setWorkflowId(null);
        }
		
		//不是流程格式，才保存正文
		if(!TemplateEnum.Type.workflow.name().equals(type)) {
			String body = request.getParameter("content");
			
			ModuleType moduleType = null;
            if(categoryType == ModuleType.edocSend.ordinal()){
                moduleType = ModuleType.edocSend;
            }else if(categoryType == ModuleType.edocRec.ordinal()){
                moduleType = ModuleType.edocRec;
            }else if(categoryType == ModuleType.edocSign.ordinal()){
                moduleType = ModuleType.edocSign;
            }
			
			//修改将以前的文单删除了
			if(!isSave){
				ctpMainbodyManager.deleteContentAllByModuleId(moduleType, templete.getId());
			}
			
			//保存正文内容
			CtpContentAll content = new CtpContentAll();
			content.setIdIfNew();
			if(!"HTML".equals(bodyType)){
			    content.setContentDataId(Long.parseLong(body));
			}else{
			    content.setContent(body);
			}
			content.setCreateDate(now);
			content.setCreateId(user.getId());
			content.setModuleId(templete.getId());
			content.setModuleTemplateId(-1L);
			content.setModuleType(moduleType.getKey());
			
			content.setContentType(EdocHelper.getBodyTypeKey(bodyType));
			
			ctpMainbodyManager.saveOrUpdateContentAll(content);
			templete.setBody(content.getId());
		}
		//保存附件应该在设置summary之前做，应该构建summary需要设置附件信息，保存了附件之后才能获得附件信息
		long templeteId = templete.getId();
		if(!isSave){
            // 删除原有附件
            this.attachmentManager.deleteByReference(templeteId);
        }

        //流程模板不保存正文
        if (!TemplateEnum.Type.workflow.name().equals(type)) {
            // 保存附件
            String attaFlag = this.attachmentManager.create(ApplicationCategoryEnum.edoc, templeteId, templeteId, request);
            if(com.seeyon.ctp.common.filemanager.Constants.isUploadLocaleFile(attaFlag)){
                //OA-19263  基础数据--模版管理中新建模版时插入了附件，保存后在列表中没有看到附件的标志  
                CtpTemplateUtil.setHasAttachments(templete, true);
            }
        }
		
		
		
		if (!TemplateEnum.Type.workflow.name().equals(type)) {
			summary = this.doColSummary(request,templete.getId());
		} else {
			EdocSummary summaryObj = new EdocSummary();
			summaryObj.setEdocType(Integer.parseInt(request.getParameter("edocType")));

			summaryObj.setAdvanceRemind(0L);
        	summaryObj.setDeadline(0L);
        	summaryObj.setCanTrack(1);
        	
			summary = XMLCoder.encoder(summaryObj);
			templete.setBodyType(String.valueOf(MainbodyType.HTML.getKey()));
		}
	
		templete.setSummary(summary);
		
		templete.setCreateDate(createDate);
		templete.setMemberId(user.getId());
		templete.setSystem(isSys == 0 ? false : true);
		templete.setOrgAccountId(user.getLoginAccount());
		
		CtpTemplate originalTemplate = null;
        //编辑模板后，保存
        if(Strings.isNotBlank(originalTemplateId)){
            originalTemplate = this.templateManager.getCtpTemplate(Long.parseLong(originalTemplateId));
            categoryType = originalTemplate.getModuleType();
            categoryId = (long)categoryType;
            templete.setCategoryId(categoryId);
            
            //OA-50749  单位管理员新建的模版，被公文管理员修改后，再次调用该模版或查看我的模版栏目，鼠标悬浮显示的创建人都是最后一次修改模版的人员  
            if(isSys == 1){
                templete.setMemberId(originalTemplate.getMemberId());
            }
        }
        //当编辑后，保存为个人模板时
        if(isSys == 0 && Strings.isNotBlank(originalTemplateId)) {
            if(originalTemplate != null) {
                if(originalTemplate.isSystem()) {    //原始模版是系统模版
                    templete.setFormParentid(originalTemplate.getId());
                }else if(originalTemplate.getFormParentid()!=null) {    //如果原始模版是个人模版，并且原始模版是另存系统模版，当前个人模版保存系统模版id
                    templete.setFormParentid(originalTemplate.getFormParentid());
                }
            }
        }
		
		
		List<CtpTemplateAuth> authList = new ArrayList<CtpTemplateAuth>();
		if(isSys == 1) {
			//授权信息
			String authInfo = request.getParameter("authInfo");
			String[][] authInfos = Strings.getSelectPeopleElements(authInfo);
			if(authInfos != null){
				int i = 0;
				for (String[] strings : authInfos) {
					CtpTemplateAuth auth = new CtpTemplateAuth();
					
					auth.setIdIfNew();
					auth.setAuthType(strings[0]);
					auth.setAuthId(Long.parseLong(strings[1]));
					auth.setSort(i++);
					auth.setModuleId(templeteId);
					
					auth.setCreateDate(now);
//					auth.setModuleType(categoryType);
					//OA-47939 因为模板列表授权时，统一将发文收文签报模板授权类型设为4了，那么在新建和修改页面授权时权限类别也采用4
					auth.setModuleType(ModuleType.edoc.getKey()); 
					
					//关联关系去掉
//					templete.getTempleteAuths().add(auth);
					Set<Long> memberIdsSet = Functions.getAllMembersId(auth.getAuthType(), auth.getAuthId());
					
					if(memberIdsSet != null && !memberIdsSet.isEmpty()){
						memberSet.addAll(memberIdsSet);
					}
					authList.add(auth);
				}
			}
		}
		/**增加流程可追溯 add by libing 3-13**/
		String parameter = request.getParameter("canTrackWorkFlow");
		if(Strings.isBlank(parameter) || !"templete".equals(type)){
			templete.setCanTrackWorkflow(0);
		}else{
			templete.setCanTrackWorkflow(Integer.parseInt(parameter));
		}
		
		templete.setModifyDate(now);
		if (isSave) { //新建
			if(isSys == 1){
				if (!TemplateEnum.Type.workflow.name().equals(type)) {
					this.saveColSuperviseForTemplate(request, response, templete, isSave, false);
				}
			}
			else{
				EdocSuperviseHelper.saveSuperviseForPersonalTemplate(request, templete.getSubject(), templete.getId(), isSave, EdocConstant.superviseState.supervising.ordinal(), superviseManager);
			}
			templateManager.saveCtpTemplate(templete);
			//这里还要保存 tempate权限
			if(authList.size()>0){
			    templateManager.saveCtpTemplateAuths(authList);
			}
	        //记录日志
		
            appLogManager.insertLog(user, AppLogAction.Edoc_Templete_Create,user.getName(),templete.getSubject());
		}
		else { // 修改
			if(isSys == 1)
				this.saveColSuperviseForTemplate(request, response, templete, false, false);
			else{
				EdocSuperviseHelper.saveSuperviseForPersonalTemplate(request, templete.getSubject(), templete.getId(), isSave, EdocConstant.superviseState.supervising.ordinal(), superviseManager);
			}
			templateManager.updateCtpTemplate(templete);
			//修改时，权限如何保存，可考虑先删除，再添加
			templateManager.deleteCtpTemplateAuths(templete.getId(), ModuleType.edoc.getKey());
			if(authList.size()>0){
                templateManager.saveCtpTemplateAuths(authList);
            }
		     //记录日志
        appLogManager.insertLog(user, AppLogAction.Edoc_Templete_Update,user.getName(),templete.getSubject());
		}
		
		//设置文号定义已经使用
		try{
			setMarkDefinitionPublished(request);
		}catch(Exception e){
			LOGGER.error("公文保存模板异常：",e);
		}
		
		EdocTemplateSaveEvent etEvent = new EdocTemplateSaveEvent(this);
		etEvent.setTemplateId(templete.getId());
		EventDispatcher.fireEvent(etEvent);
		
		if(isSys == 1) {
			///*
			//将当前模板推送到首页-我的模板
//	        List<Long> authMemberIdsList = new ArrayList<Long>();
//	        
//	        if(memberSet != null && !memberSet.isEmpty()){
//	        	authMemberIdsList.addAll(memberSet);
//	        }
	        //*/
	        
	        //OA-8813  新建了一个模版，在建的时候流程单击流程框，上次建模版的流程就带出来了  
	        //新建保存模板之后，从顶层加的包含5.0框架的框架开始刷新，这样包含流程图的dialog也就重新生成了
	        response.setContentType("text/html;charset=UTF-8");
	        PrintWriter out = response.getWriter();
            out.println("<script>");
//            out.println("parent.parent.location.href='edocController.do?method=sysCompanyMain&categoryType="+categoryType+"';");
            //OA-33438  新建或修改一个公文模板后，点击公文模板，再按照模板名称和修改时间查询，查询结果为空  
            out.println("parent.parent.location.href='edocController.do?method=sysCompanyMain&categoryType="+ModuleType.edoc.getKey()+"';");
            out.println("</script>");
	        return null;
		} else {
			List<Long> authMemberIdsList = new ArrayList<Long>();
	        authMemberIdsList.add(AppContext.currentUserId());
	        templateManager.updateTempleteConfig(templeteId, authMemberIdsList);
	        
			response.setContentType("text/html;charset=UTF-8");
			PrintWriter out = response.getWriter();
			out.println("<script>");
			out.println("try {parent.getA8Top().endProc();}catch(e) {}");
			out.println("alert(parent._('edocLang.templete_savePersonalSuccess'))");
			out.println("</script>");

			return null;
		}
	}
    
    
	private void setMarkDefinitionPublished(HttpServletRequest req) {
		String docMark = req.getParameter("my:doc_mark");
		String docMark2 =req.getParameter("my:doc_mark2");
		String serialNo =req.getParameter("my:serial_no");
		if(Strings.isNotBlank(docMark)){
			 EdocMarkModel em=EdocMarkModel.parse(docMark);
	         if (em!=null)
	         {
	        	 edocMarkDefinitionManager.setEdocMarkDefinitionUsed(em.getMarkDefinitionId());
	         }
		}
		if(Strings.isNotBlank(docMark2)){
			 EdocMarkModel em=EdocMarkModel.parse(docMark2);
	         if (em!=null)
	         {
	        	 edocMarkDefinitionManager.setEdocMarkDefinitionUsed(em.getMarkDefinitionId());
	         }		
		}
		if(Strings.isNotBlank(serialNo)){
			 EdocMarkModel em=EdocMarkModel.parse(serialNo);
	         if (em!=null)
	         {
	        	 edocMarkDefinitionManager.setEdocMarkDefinitionUsed(em.getMarkDefinitionId());
	         }
		}
	}
	
	/**
	 * 生成协同属性信息
	 * @param request
	 * @return
	 * @throws Exception
	 */
	private String doColSummary(HttpServletRequest request,long templeteId) throws Exception{
		EdocSummary edocSummary = new EdocSummary();		
		edocSummary.setId(null);
		edocSummary.setEdocType(Integer.parseInt(request.getParameter("edocType")));
		long formId=Long.parseLong(request.getParameter("edoctable"));
        DataUtil.requestToSummary(request,edocSummary,formId);
        
        setSummaryDocMarkValue(edocSummary);
        
        //OA-23023  公文模板中插入的附件，在保存后，不能在文单中显示 
        String[] filenames = request.getParameterValues(com.seeyon.ctp.common.filemanager.Constants.FILEUPLOAD_INPUT_NAME_filename);
    	String[] fileTypes = request.getParameterValues(com.seeyon.ctp.common.filemanager.Constants.FILEUPLOAD_INPUT_NAME_type);
    	
    	edocSummary.setAttachments(EdocHelper.getAttachments(filenames,fileTypes));
        //OA-23025 公文应用设置授权给lixsh，lixsh新建模板时，发起者部门自动填充他本人的，将本模板在授权给他人在调用时，发起者部门不会根据发起人变化
        //发文部门就不设置了,拟文调用模板时会自动设置拟文人所在的发文部门
        edocSummary.setSendDepartment(null);
        
        String type = request.getParameter("type");
        //正文模板不保存
        if (TemplateEnum.Type.text.name().equals(type)) {	
            //OA-23046 test01调用公文格式模板，不能编辑流程期限和提前提醒时间
            //不设置流程期限不能设置null,要设置0,因为拟文页面调用模板时，判断流程期限 是否置灰，是根据summaryFromTemplate.deadline!=0时置灰的
        	edocSummary.setAdvanceRemind(0L);
        	edocSummary.setDeadline(0L);
		}else{
		    
		    //OA-23001 新建发文调用系统模版，然后将当前待发送的公文另存为个人模版，再次去调用它，提醒时间没有置灰了，变成可选的了
		    //当调用的模板设置了提醒时间，调用之后在文单上提醒时间是置灰的，直接取advanceRemind是取不到的，针对该情况在拟文页面将提醒时间置灰时多添加了一个advanceRemind2的input
		    String deadline2 = request.getParameter("deadline2");
		    //因为在上面的DataUtil.requestToSummary(request,edocSummary,formId);中就已经设置了 deadline了
	        if(Strings.isNotBlank(deadline2) && edocSummary.getDeadline() <=0){
	            edocSummary.setDeadline(Long.parseLong(deadline2));
	        }
	        String advanceRemind2 = request.getParameter("advanceRemind2");
	        if(Strings.isNotBlank(advanceRemind2) && edocSummary.getAdvanceRemind() <= 0){
	            edocSummary.setAdvanceRemind(Long.parseLong(advanceRemind2));
	        }
		}
//		colSummary.setCanForward(request.getParameterValues("canForward") != null);
//		colSummary.setCanArchive(request.getParameterValues("canArchive") != null);
//		colSummary.setCanDueReminder(request.getParameterValues("canDueReminder") != null);
//		colSummary.setCanModify(request.getParameterValues("canModify") != null);
        if(Strings.isNotBlank(request.getParameter("isTrack")))
        	edocSummary.setCanTrack(Integer.parseInt(request.getParameter("isTrack")));   //存为个人模版，传递的是isTrack
//		colSummary.setCanEdit(request.getParameterValues("canEdit") != null);

		String note = request.getParameter("note");// 发起人附言
		// 附言内容为空，就不记录了
//		if (StringUtils.isNotBlank(note)) {
			EdocOpinion senderOpinion = new EdocOpinion();
			senderOpinion.setContent(note);
			senderOpinion.setOpinionType(EdocOpinion.OpinionType.senderOpinion.ordinal());
			senderOpinion.affairIsTrack = request.getParameterValues("isTrack") != null;
			senderOpinion.setCreateTime(new Timestamp(System.currentTimeMillis()));

			edocSummary.getEdocOpinions().add(senderOpinion);
//		}
		_setIsUnit(edocSummary);
		return XMLCoder.encoder(edocSummary);
	}
	
	private ModuleType getModuleTypeInSystem(int categoryType){
	    ModuleType moduleType = null;
        if(categoryType == ModuleType.edocSend.ordinal()){
            moduleType = ModuleType.edocSend;
        }else if(categoryType == ModuleType.edocRec.ordinal()){
            moduleType = ModuleType.edocRec;
        }else if(categoryType == ModuleType.edocSign.ordinal()){
            moduleType = ModuleType.edocSign;
        }else if(categoryType == ModuleType.govdocSend.getKey()){
            moduleType = ModuleType.govdocSend;
        }else if(categoryType == ModuleType.govdocRec.getKey()){
            moduleType = ModuleType.govdocRec;
        }else if(categoryType == ModuleType.govdocSign.getKey()){
            moduleType = ModuleType.govdocSign;
        }
        return moduleType;
	}
	
	
	/**
	 * 新建/修改
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
    @CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView systemNewTemplete(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView modelAndView = new ModelAndView("edoc/templete/newEdocTemplete");
		User user = AppContext.getCurrentUser();
		//保存调用工作流相关接口所需要的参数
        modelAndView.addObject("currentUserId", user.getId());
        modelAndView.addObject("currentUserName", user.getName());
     //   V3xOrgAccount account = orgManager.getAccountById(user.getAccountId());
        modelAndView.addObject("currentUserAccountName", user.getLoginAccountName());
        modelAndView.addObject("currentUserAccountId", user.getLoginAccount());
		
        String logoURL = EdocHelper.getLogoURL();
        modelAndView.addObject("logoURL", logoURL);
		
		String from = request.getParameter("from");
		Integer categoryType= null;
		String categoryTypeStr = request.getParameter("categoryType");
		if(Strings.isNotBlank(categoryTypeStr)){
		    categoryType = Integer.parseInt(categoryTypeStr);
		}
		
		String templeteId = request.getParameter("templeteId");
		CtpTemplate templete = null;
		if(StringUtils.isNotBlank(templeteId)){ //修改
            templete = this.templateManager.getCtpTemplate(Long.parseLong(templeteId));
            categoryType = templete.getModuleType();
		}
		modelAndView.addObject("categoryType",categoryType);
		String templateType="templete";
		
		Long orgAccountId = user.getLoginAccount();
		
		
		long memberId = user.getId();
		
		int iEdocType = categoryType;
		if(ModuleType.edocSend.getKey() == categoryType){
		    iEdocType = 19;
		}else if(ModuleType.edocRec.getKey() == categoryType){
		    iEdocType = 20;
		}else if(ModuleType.edocSign.getKey() == categoryType){
		    iEdocType = 21;
		}
		modelAndView.addObject("edocType",categoryType==19 ? 0 : (categoryType==20 ? 1 : 2));
		
		CtpEnumBean flowPermPolicyMetadata=null; 
		//默认节点权限属性设置
		String defaultNodeName="";
		String defaultNodeLable = "";
		String defaultCategory = "";
        if(iEdocType==19){
        	defaultCategory = ApplicationCategoryEnum.edocSend.name();
        	iEdocType=EdocEnum.edocType.sendEdoc.ordinal();
        	flowPermPolicyMetadata= enumManagerNew.getEnum(EnumNameEnum.edoc_send_permission_policy.name());
        } else if(iEdocType==20) {
        	defaultCategory = ApplicationCategoryEnum.edocRec.name();
        	iEdocType=EdocEnum.edocType.recEdoc.ordinal();
        	flowPermPolicyMetadata= enumManagerNew.getEnum(EnumNameEnum.edoc_rec_permission_policy.name());
        } else {
        	defaultCategory = ApplicationCategoryEnum.edocSign.name();
        	iEdocType=EdocEnum.edocType.signReport.ordinal();
        	flowPermPolicyMetadata= enumManagerNew.getEnum(EnumNameEnum.edoc_qianbao_permission_policy.name());
        } 
        Map<String,String> defaultNodeMap = edocSummaryManager.getEdocDefaultNode(defaultCategory,orgAccountId);
        if (defaultNodeMap != null) {
        	defaultNodeName = defaultNodeMap.get("defaultNodeName");
        	defaultNodeLable = defaultNodeMap.get("defaultNodeLable");
        }
        modelAndView.addObject("defaultNodeName",defaultNodeName);
        modelAndView.addObject("defaultNodeLable", defaultNodeLable);
        modelAndView.addObject("flowPermPolicyMetadata",flowPermPolicyMetadata);
        String domainIds = orgManager.getUserIDDomain(user.getId(), V3xOrgEntity.VIRTUAL_ACCOUNT_ID, V3xOrgEntity.ORGENT_TYPE_ACCOUNT);
//        List <EdocForm> edocForms=edocFormManager.getEdocForms(user.getLoginAccount(),domainIds,iEdocType);
        //OA-33741 客户bug：建立公文模板时，部分公文单无法选择到
        List <EdocForm> edocForms=edocFormManager.getEdocForms(user.getLoginAccount(),domainIds,iEdocType,-1,false);
        //GOV-4528 【公文管理-模板管理-新建】新建模板时，可以使用已经停用的文单，这是不合理的。
        //去掉停用的
       	for(Iterator<EdocForm> it = edocForms.iterator();it.hasNext();){
       		EdocForm ef = it.next();
       		if(ef.getStatus()!= null && ef.getStatus().intValue() != EdocForm.C_iStatus_Published.intValue())
       			it.remove();
       	}
        if(edocForms==null || edocForms.size()<=0)
        {
            //OA-7856 文管理，公文基础数据设置，模板管理，新建模板，报异常提示
        	response.setContentType("text/html;charset=UTF-8");
        	String szJs="<script>alert(\""+ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource","alert_nofind_edocForm")+"\");self.history.back();</script>";
        	response.getWriter().print(szJs);
        	return null;
        }
        
        modelAndView.addObject("edocForms",edocForms);
        
        EdocForm defaultEdocForm=null;
        Long edocFormId=0L;//默认公文单ID
        
		EdocSummary summary = null;
		EdocBody body = null;
		
		modelAndView.addObject("canDeleteOriginalAtts", true);    //允许删除原附件
		
		if(StringUtils.isNotBlank(templeteId)){ //修改
			templateType=templete.getType();
			
			//OA-48285 升级测试+企业版/企业版环境：修改模板时单击授权按钮，授权单位没回显
			/*升级处理，将原来权限类别为19,20,21的都改为4*/
			List<CtpTemplateAuth> beforeUpdateTempleteAuths =  this.templateManager.getCtpTemplateAuths(Long.parseLong(templeteId), categoryType);
			if(Strings.isNotEmpty(beforeUpdateTempleteAuths)){
			    for(CtpTemplateAuth auth : beforeUpdateTempleteAuths){
			        auth.setModuleType(ModuleType.edoc.getKey());
			        templateManager.updateCtpTemplateAuth(auth);
			    }
			}
			/*升级处理   ----------------------end*/
			
			List<CtpTemplateAuth> templeteAuths = this.templateManager.getCtpTemplateAuths(Long.parseLong(templeteId), ModuleType.edoc.getKey());
			modelAndView.addObject("templeteAuths", templeteAuths);
			
			summary = (EdocSummary) XMLCoder.decoder(templete.getSummary());
			EdocHelper.reLoadAccountName(summary,EdocHelper.getI18nSeperator(request));
			//OA-47059升级测试：调用升级前建好的流程模版，提示数据不存在
			if(!templateType.equals(TemplateEnum.Type.workflow.name())){
				body = getEdocBody(categoryType, templete.getId());
			}
			
			
			if(!TemplateEnum.Type.text.name().equals(templateType)) {		
	            modelAndView.addObject("hasWorkflow", Boolean.TRUE);
	            modelAndView.addObject("process_desc_by", "xml");
	            
	            String workflowNodesInfo = wapi.getWorkflowNodesInfo(
	                        String.valueOf(templete.getWorkflowId()), "edoc", flowPermPolicyMetadata);
	            
	            modelAndView.addObject("workflowNodesInfo",workflowNodesInfo);
	            modelAndView.addObject("processTemplateId", templete.getWorkflowId());
	        }
			//预归档
			Long archiveId = null;
	        String archiveName = "";
	        if(summary.getArchiveId() != null){
	        	archiveId = summary.getArchiveId();
	        	if(AppContext.hasPlugin("doc")){
	        	    archiveName = docApi.getDocResourceName(archiveId);
	        	}
	        }
	        modelAndView.addObject("archiveName", archiveName);
	        
            modelAndView.addObject("attachments", attachmentManager
                    .getByReference(templete.getId(), templete.getId()));

            modelAndView.addObject("note", summary.getSenderOpinion());//发起人附言
            
            modelAndView.addObject("templete", templete);
            
            edocFormId=summary.getFormId();
            //检查模版公文单是否存在
            if(edocFormId!=null)
            {
            	defaultEdocForm=edocFormManager.getEdocForm(edocFormId);
            	//OA-37938应用检查：模板中绑定的文单被停用之后，在模板的修改页面查看调用的公文单与实际显示的不一致 名字还是原来的名字，单其实文单不一样。
            	if(defaultEdocForm!=null && !edocForms.contains(defaultEdocForm)){
            	    modelAndView.addObject("setDefaultForm", "true");
            	    modelAndView.addObject("formName",defaultEdocForm.getName());
            	    defaultEdocForm = null;
            	}
            }
            if(defaultEdocForm==null){defaultEdocForm=edocFormManager.getDefaultEdocForm(user.getLoginAccount(),iEdocType);}
                        //分枝 开始
//            modelAndView.addObject("branchs", this.templateManager.getBranchsByTemplateId(templete.getId(),ApplicationCategoryEnum.edoc.ordinal()));
            //分枝 结束
            
            CtpSuperviseDetail detail = superviseManager.getSupervise(templete.getId());
            
            if(detail != null) {
                
//            	Set<CtpSupervisor> supervisors = detail.getColSupervisors();
                List<CtpSupervisor> supervisors = superviseManager.getSupervisors(detail.getId());
                
            	StringBuilder ids = new StringBuilder();
            	for(CtpSupervisor supervisor:supervisors)
            		ids.append(supervisor.getSupervisorId() + ",");
            	// fix 39573
            	if (ids != null && !"".equals(ids.toString())) {
            		modelAndView.addObject("colSupervisors", ids.substring(0, ids.length()-1));
            	}
            	modelAndView.addObject("colSupervise", detail);
            	
            	List<CtpSuperviseTemplateRole> roleList = superviseManager.findRoleByTemplateId(templete.getId());
            	if(roleList !=null && roleList.size()>0){
            	    StringBuilder superviseRole = new StringBuilder();
                    for(CtpSuperviseTemplateRole role : roleList){
                        if(superviseRole.length() > 0){
                            superviseRole.append(",");
                        }
                        superviseRole.append(role.getRole());
                    }
                    modelAndView.addObject("colSuperviseRole", superviseRole.toString());
            	}
            }
            
		}
		else { //直接新建
            summary = new EdocSummary();
            body = new EdocBody();
            String bodyContentType=Constants.EDITOR_TYPE_OFFICE_WORD;
            // 这里先屏蔽掉判断 是有安装office插件的判断,使模板新建页面中 正文类型选中word格式
//            if(com.seeyon.ctp.common.SystemEnvironment.hasPlugin("officeOcx")==false){bodyContentType=Constants.EDITOR_TYPE_HTML;}
            body.setContentType(bodyContentType);
            defaultEdocForm=edocFormManager.getDefaultEdocForm(user.getLoginAccount(),iEdocType);
            summary.setEdocType(iEdocType);            
            summary.setCanTrack(1);
//          if(user.getLoginAccount() != 0 && (categoryType== 2 || categoryType== 5))
//			{
//			  summary.setSendUnit(EdocRoleHelper.getAccountById(user.getLoginAccount()).getName());
//			  summary.setSendUnitId("Account|"+Long.toString(user.getLoginAccount()));
//			}
			
        }
		
		if(defaultEdocForm==null)
        {
			response.setContentType("text/html;charset=UTF-8");
        	String szJs="<script>alert(\""+ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource","alert_nofind_edocForm")+"\");self.history.back();</script>";
        	response.getWriter().print(szJs);
        	return null;
        }else if(defaultEdocForm.getIsSystem() && Strings.isBlank(defaultEdocForm.getContent())){
        	edocFormManager.updateFormContentToDBOnly();
        	defaultEdocForm=edocFormManager.getEdocForm(defaultEdocForm.getId());
        }
		
		edocFormId=defaultEdocForm.getId();
		modelAndView.addObject("edocFormId",edocFormId);
				
		List<CtpTemplateCategory> templeteCategories = this.templateManager.getCategorys(orgAccountId, ModuleType.getEnumByKey(categoryType));
		StringBuffer categoryHTML = new StringBuffer();		
		
		for (int i = 0; i < templeteCategories.size(); i++) {
		    CtpTemplateCategory category = templeteCategories.get(i);
			
		    Set<CtpTemplateAuth> authSet = templateManager.getCategoryAuths(category);
		    boolean flag = false;
		    for (CtpTemplateAuth auth : authSet) {
	            if(auth.getAuthId().longValue() == memberId 
	                    && (category.getOrgAccountId() != null && category.getOrgAccountId().longValue() == orgAccountId)){
	                flag = true;
	                break;
	            }
	        }
		    
			if(!"SYS".equalsIgnoreCase(from) && category.getParentId() == null && !flag){
				templeteCategories.remove(category);
				i--;
			}
		}
		category2HTML(templeteCategories, categoryHTML, Long.valueOf(categoryType), 1);
		
		modelAndView.addObject("categoryHTML", categoryHTML);
		
        modelAndView.addObject("summary", summary);
        modelAndView.addObject("body", body);
		
        Map<String, CtpEnumBean> colMetadata = enumManagerNew.getEnumsMap(ApplicationCategoryEnum.collaboration);
        CtpEnumBean comImportanceMetadata =  enumManagerNew.getEnum(EnumNameEnum.common_importance.name());

        modelAndView.addObject("colMetadata", colMetadata);
        modelAndView.addObject("comImportanceMetadata", comImportanceMetadata);
        
        CtpEnumBean deadlineMetadata =  enumManagerNew.getEnum(EnumNameEnum.collaboration_deadline.name());
        CtpEnumBean remindMetadata =  enumManagerNew.getEnum(EnumNameEnum.common_remind_time.name());
        
        modelAndView.addObject("remindMetadata", remindMetadata);
        modelAndView.addObject("deadlineMetadata", deadlineMetadata); 
        
        modelAndView.addObject("controller", "edocController.do");
        //modelAndView.addObject("edocType",edocType);
        modelAndView.addObject("wendanId",edocFormId);
        String appName="sendEdoc";
      if(iEdocType==1){appName="recEdoc";}
        else if(iEdocType==2){appName="signReport";}
        modelAndView.addObject("appName",appName);     
        
        EdocFormModel fm=edocFormManager.getEdocFormModel(edocFormId,summary,-1,true,false);
        fm.setDeadline(summary.getDeadline());
        
        //特殊字符替换 OA-83571
        String xslt = fm.getXslt();
        xslt = xslt.replaceAll("&", "&amp;");
        fm.setXslt(xslt);
        
        modelAndView.addObject("formModel",fm);
        modelAndView.addObject("templateType",templateType);        
        
        //分支 开始 
        //request.getSession().setAttribute("SessionObject", edocFormManager.getElementByEdocForm(defaultEdocForm));
       // request.getSession().setAttribute("edocTemplateFenzhi", edocFormManager.getElementByEdocForm(defaultEdocForm.getId()));
        //分支 结束
      //需要获得创建公文的单位的开关_正文套红日期
        Long accountId=summary.getOrgAccountId()==null?user.getLoginAccount():summary.getOrgAccountId();
        boolean taohongriqiSwitch=EdocSwitchHelper.taohongriqiSwitch(accountId);
        modelAndView.addObject("taohongriqiSwitch",taohongriqiSwitch);
		return modelAndView;
	}

	private static StringBuffer category2HTML(List<CtpTemplateCategory> categories, 
			StringBuffer categoryHTML, Long currentNode, int level){
		for (CtpTemplateCategory category : categories) {
			Long parentId = category.getParentId();
			if(parentId.equals(currentNode) || (parentId != null && parentId.equals(currentNode))){
				
				categoryHTML.append("<option value='" + category.getId() + "'>");
				
				for (int i = 0; i < level; i++) {
					categoryHTML.append("&nbsp;&nbsp;&nbsp;&nbsp;");
				}
				
				categoryHTML.append(Strings.toHTML(category.getName()) + "</option>\n");
				
				category2HTML(categories, categoryHTML, category.getId(), level + 1);
			}
		}
		
		return categoryHTML;
	}
	
	public ModelAndView systemDetail(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView modelAndView = new ModelAndView("edoc/templete/systemDetail");
				
		return modelAndView;
	}
	
	
	/**
	 * 点击察看详细内容 - 管理员
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView systemSummary(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView modelAndView = new ModelAndView("edoc/templete/systemSummary");
		Long templeteId = Long.parseLong(request.getParameter("templeteId"));
		
		CtpTemplate templete = this.templateManager.getCtpTemplate(templeteId);
		
		//EdocSummary summary = new EdocSummary();
		//summary.setSubject(templete.getSubject());
		
		EdocSummary summary = (EdocSummary) XMLCoder.decoder(templete.getSummary());
		
		modelAndView.addObject("summary", summary);
		modelAndView.addObject("templete", templete);
		
		try{
		    CtpEnumBean remindMetadata =  enumManagerNew.getEnum(EnumNameEnum.common_remind_time.name());
		    CtpEnumBean  deadlineMetadata=  enumManagerNew.getEnum(EnumNameEnum.collaboration_deadline.name());
		    //由于升级前3.5的提醒时间为无的值是-1,5.0升级为0，因此需要做兼容。
		    Long advanceRemind=summary.getAdvanceRemind();
		    if(advanceRemind==-1L){
		    	advanceRemind=0L;
		    }
	        String remindLabel=remindMetadata.getItemLabel(advanceRemind.toString());
	        String deallineLabel=deadlineMetadata.getItemLabel(summary.getDeadline().toString());
	        String bounder="com.seeyon.v3x.collaboration.resources.i18n.CollaborationResource";        
	        modelAndView.addObject("deallineLabel", ResourceBundleUtil.getString(bounder,deallineLabel));        
	        bounder="com.seeyon.v3x.common.resources.i18n.SeeyonCommonResources";
	        modelAndView.addObject("remindLabel", ResourceBundleUtil.getString(bounder,remindLabel));        
	    }catch(Exception e)
	    {
	        LOGGER.error("", e);
	    }
		
		try {
			V3xOrgMember member = this.orgManager.getEntityById(V3xOrgMember.class, templete.getMemberId());
			modelAndView.addObject("member", member);
			if(member.getIsAdmin()){
				member.setName(Functions.showMemberName(member));
			}
		}
		catch (Exception e) {
			LOGGER.error("点击察看模板抛出异常", e);
		}
		
        modelAndView.addObject("attachments", attachmentManager.getByReference(templeteId));
        modelAndView.addObject("canDeleteOriginalAtts", true);    //允许删除原附件        
        modelAndView.addObject("archiveName", edocManager.getShowArchiveNameByArchiveId(summary.getArchiveId()));
        modelAndView.addObject("fullArchiveName", edocManager.getFullArchiveNameByArchiveId(summary.getArchiveId()));
      
		return modelAndView;
	}
	
	private EdocBody getEdocBody(int mType,long templeteId) throws Exception{
	    EdocBody body = null;
	    ModuleType moduleType = getModuleTypeInSystem(mType);
        List<CtpContentAll> contents = ctpMainbodyManager.getContentList(moduleType, templeteId, "");
        if(contents != null && contents.size()>0){
            CtpContentAll content = contents.get(0);
//            body = (EdocBody) XMLCoder.decoder(content.getContent());
            body = new EdocBody();
            body.setIdIfNew();
            
            if(content.getContentType() == MainbodyType.HTML.getKey()){
                body.setContentType("HTML");
                body.setContent(content.getContent());
            }else if(content.getContentType() == MainbodyType.OfficeWord.getKey()){
                body.setContentType("OfficeWord");
                body.setContent(String.valueOf(content.getContentDataId()));
            }else if(content.getContentType() == MainbodyType.OfficeExcel.getKey()){
                body.setContentType("OfficeExcel");
                body.setContent(String.valueOf(content.getContentDataId()));
            }else if(content.getContentType() == MainbodyType.Pdf.getKey()) {
                body.setContentType(MainbodyType.Pdf.name());
                body.setContent(String.valueOf(content.getContentDataId()));
            }else if(content.getContentType() == MainbodyType.WpsWord.getKey()){
                body.setContentType(MainbodyType.WpsWord.name());
                body.setContent(String.valueOf(content.getContentDataId()));
            }else if(content.getContentType() == MainbodyType.WpsExcel.getKey()){
                body.setContentType(MainbodyType.WpsExcel.name());
                body.setContent(String.valueOf(content.getContentDataId()));
            }
            body.setCreateTime((Timestamp)content.getCreateDate());
        }
        return body;
	}
	
	
	public ModelAndView systemTopic(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		User user = AppContext.getCurrentUser();
		ModelAndView modelAndView = new ModelAndView("edoc/templete/systemTopic");
		
        String logoURL = EdocHelper.getLogoURL();
        modelAndView.addObject("logoURL", logoURL);
		
		Long templeteId = Long.parseLong(request.getParameter("templeteId"));
		
		CtpTemplate templete = this.templateManager.getCtpTemplate(templeteId);
		
		EdocSummary summary = (EdocSummary) XMLCoder.decoder(templete.getSummary());
		EdocHelper.reLoadAccountName(summary,EdocHelper.getI18nSeperator(request));
		if(!TemplateEnum.Type.workflow.name().equals(templete.getType()))
        {
            EdocBody body = getEdocBody(templete.getModuleType(),templete.getId());
            if(body != null){
                summary.getEdocBodies().add(body);
            }
        }
		
		Long formId=summary.getFormId();
		if(formId==null)
		{
			EdocForm defaultEdocForm=edocFormManager.getDefaultEdocForm(user.getLoginAccount(),summary.getEdocType());
			formId=defaultEdocForm.getId();			
		}
		EdocFormModel fm=null;
		if(formId!=null){
		    //fm=edocFormManager.getEdocFormModel(formId,summary,-1);
		    //查看模板时，文号要显示 以模板的权限方式显示文号
		    fm=edocFormManager.getEdocFormModel(formId,summary,-1,true,false);
		}
		if(formId==null || fm==null)
		{
			String szJs="<script>alert(\""+ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource","alert_nofind_edocForm")+"\");</script>";
        	response.getWriter().print(szJs);
			return null;
		}
		if(!TemplateEnum.Type.workflow.name().equals(templete.getType()))
		{
			fm.setEdocBody(summary.getFirstBody());
		}                
        fm.setSenderOpinion(summary.getSenderOpinion());
      //&符号转义-BUG_普通_V5_V5.1sp1_致远客服部_公文单有"<"，在导入的时候会提示“公文单数据出现异常！错误原因：解析XML失败”_20150626010068.在seeyonForm3.js有对应转换。搜bug编号
        String xsl=fm.getXslt();
        xsl = xsl.replaceAll("&", "&amp;");
        fm.setXslt(xsl);
        modelAndView.addObject("formModel",fm);
        //modelAndView.addObject("body", body);
		if(summary != null){
			//modelAndView.addObject("senderOpinion", summary.getSenderOpinion());//发起人附言
		}
		
		return modelAndView;
	}
	
	public ModelAndView systemWorkflow(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView modelAndView = new ModelAndView("edoc/templete/systemWorkflow");
		Long templeteId = Long.parseLong(request.getParameter("templeteId"));
		
		CtpTemplate templete = this.templateManager.getCtpTemplate(templeteId);
		
		Long workflowId = templete.getWorkflowId();
		//模板配置中，查看个人模板的流程处理
		if (!templete.isSystem() && null != templete.getFormParentid()) {
		    //获得个人模板的workflowId（ctp_template表中为null）,是取对应的系统模板的workflowId
		    CtpTemplate ctpTemplateP = templateManager.getCtpTemplate(templete.getFormParentid());
		    if(null != ctpTemplateP && null != ctpTemplateP.getWorkflowId()){
		        workflowId = ctpTemplateP.getWorkflowId();
	        } 
        }
        
		
		
       /* 模板不提供获取流程接口 String caseProcessXML = templete.getWorkflow();
		
        caseProcessXML = ColHelper.trimXMLProcessor(caseProcessXML);
        caseProcessXML = StringEscapeUtils.escapeJavaScript(caseProcessXML);
		
		modelAndView.addObject("hasDiagram", "true");
		modelAndView.addObject("caseProcessXML", caseProcessXML);*/
        
		EdocSummary summary = (EdocSummary) XMLCoder.decoder(templete.getSummary());
		modelAndView.addObject("summary", summary);
		
		if(!TemplateEnum.Type.workflow.name().equals(templete.getType()))
		{
		    EdocBody body = getEdocBody(templete.getModuleType(),templete.getId());
		    if(body != null){
		    	modelAndView.addObject("contentType", body.getContentType());
		    }
		}
		
		CtpEnumBean comMetadata =  enumManagerNew.getEnum(EnumNameEnum.common_remind_time.name());
		modelAndView.addObject("comMetadata", comMetadata);
		modelAndView.addObject("isShowButton", false);
		modelAndView.addObject("appName", EdocEnum.getEdocAppName(summary.getEdocType()));
		modelAndView.addObject("workflowId",workflowId);
		modelAndView.addObject("edocContentType", templete.getType());
		try{
			/*// 模板不提供获取流程接口分支 开始
			User user = AppContext.getCurrentUser();
			List<ColBranch> branchs = this.templateManager.getBranchsByTemplateId(templete.getId(),ApplicationCategoryEnum.edoc.ordinal());
	        modelAndView.addObject("branchs", branchs);
	        if(branchs != null) {
	        	modelAndView.addObject("teams", this.orgManager.getUserDomain(user.getId(), user.getLoginAccount(), V3xOrgEntity.ORGENT_TYPE_TEAM));
	        	V3xOrgMember mem = orgManager.getMemberById(user.getId());
	        	List<MemberPost> secondPosts = mem.getSecond_post();
	        	modelAndView.addObject("secondPosts", secondPosts);
	        }*/
		}catch(Exception e)
		{
			LOGGER.error(e.getMessage(),e);
		}
	    //分支 结束
		return modelAndView;
	}
	
	/**
	 * 模板详情,普通用户,模版调用,预留
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView detail(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView modelAndView = new ModelAndView("edoc/templete/detail");

        String logoURL = EdocHelper.getLogoURL();
        modelAndView.addObject("logoURL", logoURL);
		
		Long id = Long.valueOf(request.getParameter("id"));

		CtpTemplate templete = templateManager.getCtpTemplate(id);
		if (templete == null) {
			return modelAndView;
		}

		modelAndView.addObject("templete", templete);

		String type = templete.getType();

		// 正文格式不显示流程
		if (!TemplateEnum.Type.text.name().equals(type)) {
		    
			CtpEnumBean nodePermissionPolicy = null;//; enumManagerNew.getEnum(EnumNameEnum.col_flow_perm_policy);
			if(templete.getModuleType() ==ModuleType.edocSend.ordinal())
			{
				nodePermissionPolicy= enumManagerNew.getEnum(EnumNameEnum.edoc_send_permission_policy.name());
			}
			else if(templete.getModuleType() ==ModuleType.edocRec.ordinal())
			{
				nodePermissionPolicy= enumManagerNew.getEnum(EnumNameEnum.edoc_rec_permission_policy.name());
			}
			else if(templete.getModuleType() ==ModuleType.edocSign.ordinal())
			{
				nodePermissionPolicy= enumManagerNew.getEnum(EnumNameEnum.edoc_qianbao_permission_policy.name());
			}
			String workflowNodesInfo= "";
			if(templete.getFormParentid() != null){//系统个人模板
			    CtpTemplate pTemplate= templateManager.getCtpTemplate(templete.getFormParentid());
			    //OA-43330 调用个人模版异常
			    //个人模板所对应的 系统模板是格式模板的时候，workflowid为null,需要排除
			    if(pTemplate.getWorkflowId() != null){
			        workflowNodesInfo = wapi.getWorkflowNodesInfo(
	                        String.valueOf(pTemplate.getWorkflowId()), "edoc", nodePermissionPolicy);
	                modelAndView.addObject("processTemplateId", pTemplate.getWorkflowId());
			    }
			    
			}else{
			    workflowNodesInfo = wapi.getWorkflowNodesInfo(
                        String.valueOf(templete.getWorkflowId()), "edoc", nodePermissionPolicy);
			    modelAndView.addObject("processTemplateId", templete.getWorkflowId());
			}
            
            modelAndView.addObject("workflowNodesInfo",workflowNodesInfo);
            
			
//	        modelAndView.addObject("workflowInfo", workflowInfo);
			modelAndView.addObject("nodePermissionPolicy", nodePermissionPolicy);
		}
		
		EdocSummary summary = (EdocSummary) XMLCoder.decoder(templete.getSummary());
		EdocHelper.reLoadAccountName(summary,EdocHelper.getI18nSeperator(request));
		// 流程模板不保存正文
		if (!TemplateEnum.Type.workflow.name().equals(type)) {
		    ModuleType moduleType = getModuleTypeInSystem(templete.getModuleType());
		    if(moduleType != null){
		    	EdocBody body = getEdocBody(moduleType.getKey(),templete.getId());
				summary.getEdocBodies().add(body);
		    }		    
			summary.setTempleteId(id);
			EdocFormModel fm=edocFormManager.getEdocFormModel(summary.getFormId(),summary,-1,false,true);
			fm.setEdocBody(summary.getFirstBody());
			//&符号转义-BUG_普通_V5_V5.1sp1_致远客服部_公文单有"<"，在导入的时候会提示“公文单数据出现异常！错误原因：解析XML失败”_20150626010068.在seeyonForm3.js有对应转换。搜bug编号
            String xsl=fm.getXslt();
            xsl = xsl.replaceAll("&", "&amp;");
            fm.setXslt(xsl);
			modelAndView.addObject("formModel",fm);
		}
		
		//分枝 开始
	//	modelAndView.addObject("branchs", this.templateManager.getBranchsByTemplateId(id,ApplicationCategoryEnum.edoc.ordinal()));		
		//分枝 结束
		
		//督办信息
//		if("templete".equals(type)) {
//			CtpSuperviseDetail detail = colSuperviseManager.getSupervise(Constant.superviseType.template.ordinal(),id);
//			if(detail != null) {
//				User user = AppContext.getCurrentUser();
//				Long terminalDate = detail.getTemplateDateTerminal();
//            	if(null!=terminalDate){
//            		Date superviseDate = Datetimes.addDate(new Date(), terminalDate.intValue());
//            		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
//            		String date = format.format(superviseDate);
//            		detail.setAwakeDate(format.parse(date));
//            		modelAndView.addObject("superviseDate", date);
//            	}
//            	/*Set<CtpSupervisor> supervisors = detail.getColSupervisors();*/
//            	Set<String> sIdSet = new HashSet<String>();
//            	StringBuffer ids = new StringBuffer();
//            	StringBuffer names = new StringBuffer();
//            	
//            /*	for(CtpSupervisor supervisor:supervisors){
//            		sIdSet.add(supervisor.getSupervisorId().toString());
//            	}*/
//            /*	List<SuperviseTemplateRole> roleList = colSuperviseManager.findSuperviseRoleByTemplateId(templete.getId());
//            	if((null!=roleList && !roleList.isEmpty()) || !sIdSet.isEmpty()){
//            		modelAndView.addObject("sVisorsFromTemplate", "true");//公文调用的督办模板是否设置了督办人
//            	}*/
//            	V3xOrgRole orgRole = null;
//            	
//            	/*for(CtpSuperviseTemplateRole role : roleList){
//            		if(null==role.getRole() || "".equals(role.getRole())){
//            			continue;
//            		}
//            		if(role.getRole().toLowerCase().equals(EdocOrgConstants.ORGENT_META_KEY_SEDNER.toLowerCase())){
//            			sIdSet.add(String.valueOf(user.getId()));
//            		}
//            		boolean haveManager = false;
//            		
//
//            		
//            		if(role.getRole().toLowerCase().equals(EdocOrgConstants.ORGENT_META_KEY_DEPMANAGER.toLowerCase())||role.getRole().toLowerCase().equals(EdocOrgConstants.ORGENT_META_KEY_SEDNER.toLowerCase() + EdocOrgConstants.ORGENT_META_KEY_DEPMANAGER.toLowerCase())){
//            			
//            			orgRole = orgManager.getRoleByName(EdocOrgConstants.ORGENT_META_KEY_DEPMANAGER, user.getLoginAccount());
//            			if(null!=orgRole){
//            			List<V3xOrgDepartment> depList = orgManager.getDepartmentsByUser(user.getId());
//            			for(V3xOrgDepartment dep : depList){
//            				List<V3xOrgMember> managerList = orgManager.getMembersByRole(dep.getId(), orgRole.getId());
//            				for(V3xOrgMember mem : managerList){
//            					haveManager = true;
//                				sIdSet.add(mem.getId().toString());
//                			}
//            			}
//            			}
//
//            		}
//            		else
//            		{
//            			modelAndView.addObject("isOnlySender", "true");
//            		}
//            		if(!haveManager){
//            			modelAndView.addObject("noDepManager", "true");
//            		}
//
//            		if(role.getRole().toLowerCase().equals(EdocOrgConstants.ORGENT_META_KEY_SEDNER.toLowerCase() + EdocOrgConstants.ORGENT_META_KEY_SUPERDEPMANAGER.toLowerCase())){
//            			orgRole = orgManager.getRoleByName(EdocOrgConstants.ORGENT_META_KEY_SUPERDEPMANAGER, user.getLoginAccount());
//            			if(null!=orgRole){
//            			List<V3xOrgDepartment> depList = orgManager.getDepartmentsByUser(user.getId());
//            			for(V3xOrgDepartment dep : depList){
//            			List<V3xOrgMember> superManagerList = orgManager.getMembersByRole(dep.getId(), orgRole.getId());
//               				for(V3xOrgMember mem : superManagerList){
//               					sIdSet.add(mem.getId().toString());
//               				}
//            			}
//            			}
//            		}	
//            	}*/
//            	
//            	for(String s : sIdSet){
//            		V3xOrgMember mem = orgManager.getMemberById(Long.valueOf(s));
//            		if(mem!=null){
//            		ids.append(mem.getId());
//            		ids.append(",");
//            		names.append(mem.getName());
//            		names.append(",");
//            		}
//            	}
//            	
//            	if(ids.length()>1 && names.length()>1){
//            		modelAndView.addObject("colSupervisors", ids.substring(0, ids.length()-1));
//            		detail.setSupervisors(names.substring(0, names.length()-1));
//            	}
//            	modelAndView.addObject("colSupervise", detail);
//			}
//		}
		return modelAndView;
	}
	
    private void saveColSuperviseForTemplate(HttpServletRequest request,HttpServletResponse response,CtpTemplate template,boolean isNew,boolean sendMessage) throws BusinessException {
		String supervisorId = request.getParameter("supervisorId");
        String supervisors = request.getParameter("supervisors");
        String awakeDate = request.getParameter("awakeDate");
        String role = request.getParameter("superviseRole");
        //if((supervisorId != null && !"".equals(supervisorId))||(role!=null && !"".equals(role)) && awakeDate != null && !"".equals(awakeDate)) {
    	User user = AppContext.getCurrentUser();
        //boolean canModifyAwake = "on".equals(request.getParameter("canModifyAwake"))?true:false;
        String superviseTitle = request.getParameter("superviseTitle");
        //Date date = Datetimes.parse(awakeDate, Datetimes.dateStyle);
        String[] idsStr = supervisorId.split(",");
        long[] ids = new long[idsStr.length];
        if(!Strings.isBlank(supervisorId)){
	        int i = 0;
	        for(String id:idsStr) {
	        	ids[i] = Long.parseLong(id);
	        	i++;
	        }
        }
        //重要程度
        int importantLevel = 1;
        Long awakeDat = 0L;
        if(Strings.isNotBlank(awakeDate)) awakeDat = Long.valueOf(awakeDate);
        if(isNew){
        	if((supervisorId != null && !"".equals(supervisorId))||(role!=null && !"".equals(role)) && awakeDate != null && !"".equals(awakeDate)) {
        		edocSuperviseManager.saveForTemplate(importantLevel, template.getSubject(),superviseTitle,user.getId(),user.getName(), supervisors, ids,awakeDat, SuperviseEnum.EntityType.template.ordinal(), template.getId(),sendMessage);
        		edocSuperviseManager.saveSuperviseTemplateRole(template.getId(), role);
        	}
        }
        else{
        	edocSuperviseManager.updateForTemplate(importantLevel, template.getSubject(),superviseTitle,user.getId(),user.getName(), supervisors, ids, awakeDat, SuperviseEnum.EntityType.template.ordinal(), template.getId(),sendMessage);
        	edocSuperviseManager.updateSuperviseTemplateRole(template.getId(), role);
        }
	       // }
    }

	/**
	 * @param colSuperviseManager the colSuperviseManager to set
	 */
	public void setColSuperviseManager(SuperviseManager colSuperviseManager) {
		this.superviseManager = colSuperviseManager;
	}

    public ModelAndView showBranchDesc(HttpServletRequest request,HttpServletResponse response) throws Exception{
    	ModelAndView mv = new ModelAndView("collaboration/templete/moreCondition");
    	//String linkId = request.getParameter("linkId");
    	//String templateId = request.getParameter("templateId");
    	return mv;
    }
    public ModelAndView openSupervise(HttpServletRequest request,HttpServletResponse response) throws Exception{
    	ModelAndView mv = new ModelAndView("edoc/templete/openSupervise");
    	return mv;
    }
    /**
     * 督办设置
     */
    /**
	 * 协同督办选择窗口
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView superviseWindowForTemplate(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView(
				"edoc/templete/superviseWindowForTemplate");
		String isFromEdoc = request.getParameter("isFromEdoc");
		if (!Strings.isBlank(isFromEdoc)) {
			mav.addObject("title", "edoc.supervise.label");
		} else {
			mav.addObject("title", "col.supervise.label");
		}
		String summaryId = request.getParameter("summaryId");
		if (summaryId != null && !"".equals(summaryId)) {
		    mav.addObject("submitIt", "1");
		} else {
			mav.addObject("supervisorId", request.getParameter("supervisorId"));
			mav.addObject("supervisors", request.getParameter("supervisors"));
			mav.addObject("superviseTitle", request
					.getParameter("superviseTitle"));
			mav.addObject("awakeDate", request.getParameter("awakeDate"));
			mav.addObject("canModify", request.getParameter("canModify"));

			String role = request.getParameter("role");
			if (!Strings.isBlank(role) && role.length() > 0) {
				String[] strs = role.split(",");
				for (int i = 0; i < strs.length; i++) {
					mav.addObject(strs[i], strs[i]);
				}
			}
		}
		return mav;
	}
	
	public ModelAndView edocSuperviseWindowEntry(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView(
				"edoc/supervise/superviseIframeForEdoc");
		return mav;
	}

	/**
	 * 协同督办选择窗口
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView superviseWindow(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView(
				"edoc/superviseWindow");
		//String isFromEdoc = request.getParameter("isFromEdoc");
		//迁移代码，协同是新做功能，此处目前只提供公文使用，所以屏蔽了验证
		/*if (!Strings.isBlank(isFromEdoc)) {*/
			mav.addObject("title", "edoc.supervise.label");
			mav.addObject("isFromEdoc", "true");
		/*} else {
			mav.addObject("title", "col.supervise.label");
		}*/
		String summaryId = request.getParameter("summaryId");
		String currentPage=request.getParameter("currentPage");//当前页面
		String superviseTitle = "";
	//	if (summaryId != null && !"".equals(summaryId) && !"newEdoc".equals(currentPage)) {
		if (summaryId != null && !"".equals(summaryId) && !"newEdoc".equals(currentPage)) {
			Set<String> idSets = new HashSet<String>();
			StringBuilder supervisorId = new StringBuilder();
			Set<String> tempIdSets = new HashSet<String>();	
			
			CtpSuperviseDetail detail = this.superviseManager.getSupervise(Long.parseLong(summaryId));
			
			ColSummary summary = null;
			if(AppContext.hasPlugin("collaboration")){
			    summary = collaborationApi.getColSummary(Long.parseLong(summaryId));
			}
			//已結束的流程不能设置督办
			if(summary != null && summary.getFinishDate() != null){
			    super.rendJavaScript(response, "alert('"+ ResourceUtil.getString("col.supervise.cannotSet.isFinished") +"');window.close();");
			    return super.refreshWorkspace();
			}
			if (detail != null) {
				if (null != summary && null != summary.getTempleteId()) {
				    
					CtpSuperviseDetail tempDetail = superviseManager.getSupervise(summary.getTempleteId());
				    
					if (null != tempDetail) {
						List<CtpSupervisor> tempVisors= superviseManager.getSupervisors(tempDetail.getId());
						for (CtpSupervisor ts : tempVisors) {
							idSets.add(ts.getSupervisorId().toString());
							tempIdSets.add(ts.getSupervisorId().toString());
						}
						List<CtpSuperviseTemplateRole> roleList = superviseManager.findRoleByTemplateId(summary
								.getTempleteId()); 
						V3xOrgRole orgRole = null;
						User user = CurrentUser.get();

				for (CtpSuperviseTemplateRole role : roleList) {
					if (null == role.getRole() || "".equals(role.getRole())) {
						continue;
					}
					if (role.getRole().toLowerCase().equals(
							EdocOrgConstants.ORGENT_META_KEY_SEDNER
									.toLowerCase())) {
						tempIdSets.add(String.valueOf(user.getId()));
					}
					if (role
							.getRole()
							.toLowerCase()
							.equals(
									EdocOrgConstants.ORGENT_META_KEY_SEDNER
											.toLowerCase()
											+ EdocOrgConstants.ORGENT_META_KEY_DEPMANAGER
													.toLowerCase())) {
						orgRole = orgManager.getRoleByName(
								EdocOrgConstants.ORGENT_META_KEY_DEPMANAGER,
								user.getLoginAccount());
						if (null != orgRole) {
							//List<V3xOrgDepartment> depList = orgManager.getDepartmentsByUser(user.getId());
							//for (V3xOrgDepartment dep : depList) {
								List<V3xOrgMember> managerList = orgManager.getMembersByRole(user.getAccountId(), orgRole.getId());
								for (V3xOrgMember mem : managerList) {
									tempIdSets.add(mem.getId().toString());
								}
							//}
						}
					}
					if (role
							.getRole()
							.toLowerCase()
							.equals(
									EdocOrgConstants.ORGENT_META_KEY_SEDNER
											.toLowerCase()
											+ EdocOrgConstants.ORGENT_META_KEY_SUPERDEPMANAGER
													.toLowerCase())) {
						orgRole = orgManager
								.getRoleByName(
										EdocOrgConstants.ORGENT_META_KEY_SUPERDEPMANAGER,
										user.getLoginAccount());
						if (null != orgRole) {
							//List<V3xOrgDepartment> depList = orgManager.getDepartmentsByUser(user.getId());
							//for (V3xOrgDepartment dep : depList) {
								List<V3xOrgMember> superManagerList = orgManager.getMembersByRole(user.getAccountId(), orgRole.getId());
								for (V3xOrgMember mem : superManagerList) {
									tempIdSets.add(mem.getId().toString());
								}
							//}
						}
					}
				}

				StringBuilder ids = new StringBuilder();
				for (String s : tempIdSets) {
					ids.append(s);
					ids.append(",");
				}

				if (ids.length() > 1) {
					mav.addObject("unCancelledVisor", ids.substring(0, ids
							.length() - 1));
				}
					}
				}
				List<CtpSupervisor> supervisors= superviseManager.getSupervisors(detail.getId());
				for (CtpSupervisor supervisor : supervisors) {
					idSets.add(supervisor.getSupervisorId().toString());
				}
				for (String id : idSets) {
					supervisorId.append(id + ",");
				}
				if (supervisorId.length() > 0) {
					mav.addObject("supervisorId", supervisorId.substring(0,
							supervisorId.length() - 1));
				}
				mav.addObject("superviseId", detail.getId());
				mav.addObject("supervisors", detail.getSupervisors());
				superviseTitle = detail.getTitle();
				mav.addObject("awakeDate", Datetimes.format(detail
						.getAwakeDate(), Datetimes.datetimeWithoutSecondStyle));
				mav.addObject("sVisorsFromTemplate", "true");
				mav.addObject("unCancelledVisor", request
						.getParameter("unCancelledVisor"));
				// mav.addObject("canModify", detail.isCanModify());
			}
			mav.addObject("submitIt", "1");
		} else {
			mav.addObject("supervisorId", request.getParameter("supervisorId"));
			mav.addObject("supervisors", request.getParameter("supervisors"));
			superviseTitle = request.getParameter("superviseTitle");
			
			mav.addObject("awakeDate", request.getParameter("awakeDate"));
			mav.addObject("canModify", request.getParameter("canModify"));
			mav.addObject("unCancelledVisor", request
					.getParameter("unCancelledVisor"));
			mav.addObject("sVisorsFromTemplate", request
					.getParameter("sVisorsFromTemplate"));
			mav.addObject("temformParentId", request
					.getParameter("temformParentId"));
		}
		if(Strings.isNotBlank(superviseTitle)){
			superviseTitle = superviseTitle.replaceAll("<br/>", "\n");
		}
		mav.addObject("superviseTitle", superviseTitle);
		return mav;
	}
	/**
	 * 在已发列表中新建/修改督办
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView saveSupervise(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String superviseId = request.getParameter("superviseId");
		String supervisorNames = request.getParameter("supervisorNames");
		String superviseDate = request.getParameter("superviseDate");
		// String canModify = request.getParameter("canModify");
		String title = request.getParameter("title");
		String summaryId = request.getParameter("summaryId");
		EdocSummary summary = edocManager.getEdocSummaryById(Long.parseLong(summaryId),false);
		if("true".equals(request.getParameter("isDelete"))){
            superviseManager.deleteSuperviseAllAndSendMsgById(Long.parseLong(superviseId),summary.getId(),null);
        } else{
            String supervisorMemberId = request.getParameter("supervisorMemberId");
            SuperviseMessageParam smp = new SuperviseMessageParam();
	        smp.setImportantLevel(summary.getImportantLevel());
	        smp.setSubject(summary.getSubject());
	        smp.setMemberId(null);
	        smp.setSendMessage(false);
	        
	        SuperviseSetVO sVO = new SuperviseSetVO();
            //sVO.setTemplateDateTerminal();
            sVO.setAwakeDate(superviseDate);
            //sVO.setRole(role);
            sVO.setSupervisorIds(supervisorMemberId);
            sVO.setSupervisorNames(supervisorNames);
            sVO.setTitle(title);
            sVO.setUnCancelledVisor(null);
	        
    		if (Strings.isNotBlank(superviseId)){
    		    sVO.setDetailId(Long.parseLong(superviseId));
    			//superviseManager.update(smp, title,user.getId(),user.getName(), supervisorNames, idList, Datetimes.parse(superviseDate, Datetimes.dateStyle), SuperviseEnum.EntityType.edoc.ordinal(), Long.parseLong(summaryId));
            } else{
            	//superviseManager.save(smp, title,user.getId(),user.getName(), supervisorNames, idList, Datetimes.parse(superviseDate, Datetimes.dateStyle), SuperviseEnum.EntityType.edoc.ordinal(), Long.parseLong(summaryId));
            }
    		superviseManager.saveOrUpdateSupervise4Process(sVO, smp, Long.parseLong(summaryId), SuperviseEnum.EntityType.edoc);
    		
        }
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		out.println("<script>");
		out.println("  getA8Top().window.close();");
		out.println("</script>");
		return null;
	}
	
	private void setSummaryDocMarkValue(EdocSummary edocSummary) {
		// 处理公文文号
        // 如果公文文号为空，不做任何处理
        String docMark = edocSummary.getDocMark(); 
        docMark=this.registDocMark(edocSummary.getId(), docMark, 1,edocSummary.getEdocType(),false,EdocEnum.MarkType.edocMark.ordinal());
        if(docMark!=null) {
        	edocSummary.setDocMark(docMark);
        }
        //处理第二个公文文号
        docMark = edocSummary.getDocMark2();
        docMark=this.registDocMark(edocSummary.getId(), docMark, 2,edocSummary.getEdocType(),false,EdocEnum.MarkType.edocMark.ordinal());
        if(docMark!=null) {
        	edocSummary.setDocMark2(docMark);
        }
        //内部文号
        String serialNo = edocSummary.getSerialNo();
        serialNo=this.registDocMark(edocSummary.getId(), serialNo, 3,edocSummary.getEdocType(),false,EdocEnum.MarkType.edocInMark.ordinal());
        if(serialNo!=null) {
        	edocSummary.setSerialNo(serialNo);
        }
	}
	
	/**
     * 登记使用的文号,返回真正的文号串
     * @param markStr:掩码格式文号，详细见EdocMarkModel.parse()方法
     * @param markNum
     */
    private String registDocMark(Long summaryId,String markStr,int markNum,int edocType,boolean checkId,int markType) {
    	if(Strings.isNotBlank(markStr)){
    		markStr = markStr.replaceAll(String.valueOf((char)160), String.valueOf((char)32));
    	}
    	EdocMarkModel em=EdocMarkModel.parse(markStr);
    	if (em!=null) {
        	Integer t = em.getDocMarkCreateMode();//0:未选择文号，1：下拉选择的文号，2：选择的断号，3.手工输入 4.预留文号
        	if(t == 3) {
	        	String _edocMark = em.getMark(); //需要保存到数据库中的公文文号
	        	_edocMark = _edocMark.replaceAll(String.valueOf((char)160), String.valueOf((char)32));
	        	return _edocMark;
        	}
        }    	
    	return markStr;
    }
	
}
