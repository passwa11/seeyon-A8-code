package com.seeyon.apps.govdoc.manager.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.CollectionUtils;

import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.edoc.api.EdocApi;
import com.seeyon.apps.edoc.bo.GovdocTemplateDepAuthBO;
import com.seeyon.apps.edoc.bo.TemplateMarkInfo;
import com.seeyon.apps.govdoc.constant.GovdocAppLogAction;
import com.seeyon.apps.govdoc.constant.GovdocEnum.OperationType;
import com.seeyon.apps.govdoc.dao.GovdocTemplateDao;
import com.seeyon.apps.govdoc.helper.GovdocContentHelper;
import com.seeyon.apps.govdoc.manager.GovdocLogManager;
import com.seeyon.apps.govdoc.manager.GovdocTemplateManager;
import com.seeyon.apps.govdoc.util.GovdocContentUtil;
import com.seeyon.apps.govdoc.vo.GovdocBodyVO;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.ContentSaveOrUpdateRet;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyService;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.po.template.CtpTemplateAuth;
import com.seeyon.ctp.common.po.template.CtpTemplateCategory;
import com.seeyon.ctp.common.po.template.CtpTemplateConfig;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.supervise.vo.SuperviseSetVO;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.template.enums.TemplateEnum;
import com.seeyon.ctp.common.template.enums.TemplateEnum.State;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.template.util.CtpTemplateUtil;
import com.seeyon.ctp.common.template.util.TemplateUtil;
import com.seeyon.ctp.common.template.vo.TemplateBO;
import com.seeyon.ctp.common.template.vo.TemplateCategory;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormFieldBean;
import com.seeyon.ctp.form.bean.FormFieldComBean.FormFieldComEnum;
import com.seeyon.ctp.form.bean.FormViewBean;
import com.seeyon.ctp.form.manager.GovdocTemplateDepAuthManager;
import com.seeyon.ctp.form.po.CtpTemplateRelationAuth;
import com.seeyon.ctp.form.po.GovdocTemplateDepAuth;
import com.seeyon.ctp.form.util.Enums.FormType;
import com.seeyon.ctp.form.util.FormConstant;
import com.seeyon.ctp.form.util.SelectPersonOperation;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.organization.bo.MemberPost;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgRole;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.manager.OrgManagerDirect;
import com.seeyon.ctp.organization.manager.RoleManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.UniqueList;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.engine.enums.BPMSeeyonPolicySetting;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;

import www.seeyon.com.utils.UUIDUtil;

public class GovdocTemplateManagerImpl implements GovdocTemplateManager {

	private static final Log LOGGER = LogFactory.getLog(GovdocTemplateManagerImpl.class);
	
	private OrgManager          		orgManager;
	
	private TemplateManager     		templateManager;
	
	private AttachmentManager   		attachmentManager;
	
	private FormApi4Cap3 				formApi4Cap3;
	
	private MainbodyManager     		ctpMainbodyManager;
	
	private WorkflowApiManager          wapi;
	
    private FileManager                 fileManager;
    
    private PermissionManager           permissionManager;
    
    private SuperviseManager    		superviseManager;
	
	
	private GovdocTemplateDao 		    govdocTemplateDao;
	private GovdocLogManager 			govdocLogManager;
	
	private EdocApi 					edocApi;
	
    private OrgManagerDirect    		orgManagerDirect;
    
    private RoleManager         		roleManager;
    
	private CAPFormManager 				capFormManager;
    
	@Override
	public void setLianheTemplateAttr(List<CtpTemplate> templateList) {
		if(templateList==null||templateList.size()==0){
    		return;
    	}
    	long loginAccountId = AppContext.currentAccountId();
    	GovdocTemplateDepAuthManager govdocTemplateDepAuthManager = (GovdocTemplateDepAuthManager)AppContext.getBean("govdocTemplateDepAuthManager");
    	List<GovdocTemplateDepAuth> list = govdocTemplateDepAuthManager.findByOrgIdAndAccountId4Lianhe(loginAccountId, loginAccountId);
    	if(list!=null && list.size()>0){
    		for(CtpTemplate t:templateList){
    			if(list.get(0).getTemplateId().equals(t.getId())){
    				t.putExtraAttr(GovdocTemplateDepAuth.class.getName(), list.get(0));
    				break;
    			}
    		}
    	}
	}
	
	@Override
	public List<CtpTemplate> findLianHeTemplateList(Map<String, Object> map) {
		return govdocTemplateDao.findTemplatesByAuthAccount(map);
	}

    @SuppressWarnings("unchecked")
    @Override
    public FlipInfo selectTempletesNew(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
        
        params.put("delete", "false");
        if (params.get("subject") != null) {
            params.put("subject", CtpTemplateUtil.unescape(params.get("subject")));
        }
        if (params.get("member") != null) {
            List<V3xOrgMember> members = orgManager.getMemberByIndistinctName(params.get("member"));
            StringBuilder sb = new StringBuilder("-1");
            if (!CollectionUtils.isEmpty(members)) {
                for (V3xOrgMember v3xOrgMember : members) {
                    sb.append(",");
                    sb.append(v3xOrgMember.getId());
                }
            }
            params.put("memberId", sb.toString());
        }
        // 默认不查询表单正文的模板
        String bodyType = "10,30,41,42,43,44,45";
        params.put("bodyType", bodyType);
        // 因为后面需要对模板列表检查是否授权，所以查询数据库时需要查询出所有记录然后进行内存分页
      //  FlipInfo flipInfoTemp = new FlipInfo();
      //  flipInfoTemp.setSize(Integer.MAX_VALUE);
        // 协同模板管理默认显示全部模板
        if("1".equals(params.get("categoryId"))){
            params.remove("categoryId");
        }
        boolean needSearchCategory = true;
        // 公文模板管理默认显示全部模板
        if("4".equals(params.get("categoryType"))){
        	needSearchCategory =false;
            params.put("categoryType", "19,20,21");
            params.put("categoryId", "19,20,21");
            
        }
       if ("4".equals(params.get("app"))) {
    	   needSearchCategory =false;
           params.put("categoryType", "401,402,404");
           params.put("categoryId", null);
       }
       if(!"4".equals(params.get("app"))&&!"4".equals(params.get("categoryType"))){
    	   long rootCategoryId = 0;
    	   try {
    		   rootCategoryId = Long.valueOf(params.get("categoryId"));
    	   } catch (Exception e) {
    	   }
    	   if(rootCategoryId!=0){
    		   StringBuilder categoryIdStrs = new StringBuilder();
    		   categoryIdStrs.append(rootCategoryId);
    	        /*List<Integer> intList = new ArrayList<Integer>();
    	        intList.add(Integer.parseInt(String.valueOf(ModuleType.govdocSend.getKey())));
    	        intList.add(Integer.parseInt(String.valueOf(ModuleType.govdocRec.getKey())));
    	        List<CtpTemplateCategory> templeteCategories = this.templateManager.getCategorys(AppContext.getCurrentUser().getLoginAccount(), intList);
    	        templeteCategories = getChildCategory(rootCategoryId,templeteCategories);
    	        for (CtpTemplateCategory ctpTemplateCategory : templeteCategories) {
    	        	categoryIdStrs.append(","+ctpTemplateCategory.getId());
				}*///注释掉，模板查询父类不带出子分类模板
    	        params.put("app",params.get("categoryType"));
    	        params.put("categoryId", categoryIdStrs.toString());
    	   }
       }
        if("32".equals(params.get("categoryType"))){
        	needSearchCategory = false;
        	params.put("categoryType", "32");
            params.put("categoryId", "32");
        }
        if(needSearchCategory){
        	params = getCtpCategoriesByAuth(params);
        }
        
        flipInfo = govdocTemplateDao.selectAllSystemTempletes(flipInfo, params);
        List<CtpTemplate> result = flipInfo.getData();
        setLianheTemplateAttr(result);
        List<TemplateBO> resultBO = new ArrayList<TemplateBO>();
        if (result != null) {
            TemplateBO bo = null;
            String[] results = null;
            V3xOrgMember member = null;
            V3xOrgAccount  account = orgManager.getAccountById(AppContext.currentAccountId()) ;
            for (CtpTemplate ctpTemplate : result) {
                // 是否有模板所属分类的权限
                if (ctpTemplate.getModuleType() == ModuleType.edocRec.getKey()
                        || ctpTemplate.getModuleType() == ModuleType.edocSend.getKey()
                        || ctpTemplate.getModuleType() == ModuleType.edocSign.getKey()
                        || ctpTemplate.getModuleType() == ModuleType.collaboration.getKey()) {
                    bo = new TemplateBO(ctpTemplate);
                    results = getTemplateAuth(ctpTemplate);
                    bo.setAuth(results[0]);
                    bo.setAuthValue(results[1]);
                    bo.setHasAttsFlag(CtpTemplateUtil.isHasAttachments(ctpTemplate));
                    member = orgManager.getMemberById(ctpTemplate.getMemberId());
                    if (member != null) {
                        if (orgManager.isAdministratorById(member.getId(),account)) {
                            bo.setCreaterName("单位管理员");
                        } else {
                            bo.setCreaterName(member.getName());
                        }
                    }
                    resultBO.add(bo);
                }
                if(ctpTemplate.getModuleType() == ModuleType.info.getKey()){
                	 bo = new TemplateBO(ctpTemplate);
                	 results = getTemplateAuth(ctpTemplate);
                	 bo.setAuth(results[0]);
                     bo.setAuthValue(results[1]);
                     bo.setHasAttsFlag(CtpTemplateUtil.isHasAttachments(ctpTemplate));
                     try{
                    	 bo.setCreateUnit(account.getName());
                     }catch(Exception e){
                    	 
                     }
                	 resultBO.add(bo);
                }
                if (ctpTemplate.getModuleType() == ApplicationCategoryEnum.govdocExchange.getKey()
                		|| ctpTemplate.getModuleType() == ApplicationCategoryEnum.govdocRec.getKey()
                		|| ctpTemplate.getModuleType() == ApplicationCategoryEnum.govdocSend.getKey()
                		|| ctpTemplate.getModuleType() == ApplicationCategoryEnum.govdocSign.getKey()) {
                	bo = new TemplateBO(ctpTemplate);
               	 	results = getTemplateAuth(ctpTemplate);
               	 	bo.setAuth(results[0]);
                    bo.setAuthValue(results[1]);
					String attListJSON = attachmentManager.getAttListJSON(ctpTemplate.getId());
					if("[]".equals(attListJSON)){
						bo.setHasAttsFlag(false);
					}else{
						bo.setHasAttsFlag(true);
					}
                    try{
                   	 bo.setCreateUnit(account.getName());
                    }catch(Exception e){
                   	 
                    }
                    FormBean fb = formApi4Cap3.getFormByFormCode4Govdoc(ctpTemplate);
                    if(fb!=null){
	                    bo.putExtraAttr("formId", fb.getId());
	                    bo.putExtraAttr("formName", fb.getFormName());
	               	 	resultBO.add(bo);
                    }
               	 	if(ctpTemplate.getExtraAttr(GovdocTemplateDepAuth.class.getName())!=null){
               	 		bo.setSubject(ResourceUtil.getString("govdoc.title.lianhe.label") + bo.getSubject());
               	 	}
				}
            }
        }
        
       //DBAgent.memoryPaging(resultBO, flipInfo);
        flipInfo.setData(resultBO);
        return flipInfo;
    }

	@Override
	public List<CtpTemplateCategory> getCategorysByAuth(Long accountId, List<ModuleType> types) throws BusinessException {
    	if(accountId == null || types == null){
    		return new ArrayList<CtpTemplateCategory>();
    	}
    	
    	List<CtpTemplateCategory> templateCategorys = templateManager.getCategorys(accountId,types);
       
    	return checkCategoryAuth(accountId, templateCategorys);
    }

	@Override
	public StringBuffer getCategory2HTMLNew(Long accountId, Long parentCategoryId) throws BusinessException {
        StringBuffer categoryHTML = new StringBuffer();
        List<Long> categoryTypes = new ArrayList<Long>();
        categoryTypes.add(Long.valueOf(4));
        categoryTypes.add(Long.valueOf(401));
        categoryTypes.add(Long.valueOf(402));
        List<CtpTemplateCategory> templeteCategories = new ArrayList<CtpTemplateCategory>();
        List<CtpTemplateCategory> notNeedCategorys = getSubCategorys(accountId, parentCategoryId);
        notNeedCategorys.add(templateManager.getCategorybyId(parentCategoryId));
        
        //
        Long edocSendType = Long.parseLong(String.valueOf(ModuleType.govdocSend.getKey()));
        CtpTemplateCategory govdocSendNode = new CtpTemplateCategory(edocSendType,"发文模板", Long.parseLong(String.valueOf(ModuleType.edoc.getKey())));
        templeteCategories.add(govdocSendNode);
        //客开 项目名称： 作者：fzc 修改日期：2018-3-28 [修改功能：公文模板分类]start
        Long govdocRecType = Long.parseLong(String.valueOf(ModuleType.govdocRec.getKey()));
        CtpTemplateCategory govdocRecNode = new CtpTemplateCategory(govdocRecType,"收文模板", Long.parseLong(String.valueOf(ModuleType.edoc.getKey())));
        templeteCategories.add(govdocRecNode);
        Long govdocSignType = Long.parseLong(String.valueOf(ModuleType.govdocSign.getKey()));
        CtpTemplateCategory govdocSignNode = new CtpTemplateCategory(govdocSignType,"签报模板", Long.parseLong(String.valueOf(ModuleType.edoc.getKey())));
        templeteCategories.add(govdocSignNode);
        //客开 项目名称： 作者：fzc 修改日期：2018-3-28 [修改功能：公文模板分类]end
        //List<CtpTemplateCategory> result = checkCategoryAuth(accountId, templeteCategories);
        StringBuffer sb = new StringBuffer();
        return sb.append(category2HTMLNew(templeteCategories, categoryHTML, categoryTypes, 1));
    }

	@Override
	public String saveGovDocTemplate() throws BusinessException {
		Map para = ParamUtil.getJsonDomain("colMainData");
		User user = AppContext.getCurrentUser();

		String type = (String)para.get("type");
		String subApp = (String) para.get("subApp");
		Long overId = -1L;
		Long formparentid = null;
		Object govdocBodyType = para.get("govdocBodyType");
		String bodytype = (String)para.get("tembodyType");
		if(Strings.isNotBlank((String)para.get("overId"))){//overId即为要覆盖的老模板的ID
			overId = Long.parseLong((String)para.get("overId"));
			templateManager.deleteCtpTemplate(overId);//删除模板
			if(!("0".equals((String)para.get("contentIdUseDelete")))){
			  ctpMainbodyManager.deleteById(Long.valueOf((String)para.get("contentIdUseDelete")));
			}
		}

		Timestamp createDate = new Timestamp(System.currentTimeMillis());

		CtpTemplate template = null;
		//设置模板的一些信息
		template = (CtpTemplate) ParamUtil.mapToBean(para, new CtpTemplate(),false);
		if (Strings.isNotBlank(subApp)) {
			if ("1".equals(subApp)) {
				template.setModuleType(ApplicationCategoryEnum.govdocSend.getKey());
				template.setCategoryId(Long.valueOf(ApplicationCategoryEnum.govdocSend.getKey()));
			}else if ("2".equals(subApp)){
				template.setModuleType(ApplicationCategoryEnum.govdocRec.getKey());
				template.setCategoryId(Long.valueOf(ApplicationCategoryEnum.govdocRec.getKey()));
			}else if ("3".equals(subApp)){
				template.setModuleType(ApplicationCategoryEnum.govdocSign.getKey());
				template.setCategoryId(Long.valueOf(ApplicationCategoryEnum.govdocSign.getKey()));
				
			}
		}
		template.setSubstate(4);//公文模板默认审核通过
		if( null!=para.get("personTid") && Strings.isNotBlank((String)para.get("personTid"))){
			template.setId(Long.valueOf((String)para.get("personTid")));
		}
		if(null != bodytype && Strings.isNotBlank(bodytype)){
			template.setBodyType(bodytype);
		}else{
			template.setBodyType(String.valueOf(MainbodyType.HTML.getKey()));//默认为10HTML类型
		}
		try{
			template.setCanTrackWorkflow(Integer.parseInt((String)para.get("canTrackWorkFlow")));
		}catch(Exception e){
			template.setCanTrackWorkflow(0);
		}
		String temCanSuperviseString = (String)para.get("temCanSupervise");
		template.setCanSupervise(Strings.isBlank(temCanSuperviseString) ? true: Boolean.valueOf(temCanSuperviseString));
		template.setSubject((String)para.get("saveAsTempleteSubject"));
		String invokeTemplateId = (String)para.get("tId");
		CtpTemplate parentTemplate = null;
		if(StringUtils.isNotBlank(invokeTemplateId)){
			 parentTemplate = templateManager.getCtpTemplate(Long.parseLong(invokeTemplateId));
			 //表单个人模版的父id，当调用表单模版时该id为空，调用表单模版另存个人模版后再次另存页面没有刷新该id为空，当调用表单个人模版是该id不为空。
			 String temformParentId = (String)para.get("temformParentId");
			if(Strings.isNotBlank(temformParentId)){
				formparentid = Long.parseLong(temformParentId);
				template.setFormParentid(formparentid);
			}else {
				//如果是表单需要保存parent，或者是系统模板用于督办
				if(String.valueOf(MainbodyType.FORM.getKey()).equalsIgnoreCase(bodytype) || parentTemplate.isSystem())
					template.setFormParentid(Long.parseLong(invokeTemplateId));
			}
		}
		//正文
		ContentSaveOrUpdateRet content = null;
		String summary =  edocApi.getTemplateSummary(para,type);
		String markInfo =  edocApi.getTemplateMarkInfoXmlByParams(user, para);
		/**
		 * 协同模板：将把协同所有的信息作为模板保存，调用者不可修改流程，其他信心可以修改。
		 * 格式模板：将把协同的正文模板保存，调用者只引用正文。
		 * 流程模板：将把协同流程作为模板保存，调用者只能引用流程，不允许修改流程。
		 */
		if (TemplateEnum.Type.template.name().equals(type)) {
			//其他模板修改存为协同模板的时候，这里需要将ID 设置成正文的moduleID。。。。
			 AppContext.putThreadContext("_perTemModuleId",template.getId());
			content= GovdocContentUtil.
					contentSaveOrUpdate(OperationType.personalTemplate, new AffairData(),template.getSubject(),false);
			template.setWorkflowId(Long.parseLong(content.getProcessId()));
			Long _contentSaveId = Long.valueOf((String)para.get("contentSaveId"));
			template.setBody(_contentSaveId);
			
			
			 updatePermissionRef(user);
		     
			//删除表单模板产生的动态数据，该数据存在ctpContentBean.content字段中，不需要在表单动态表中产生数据
			
			 //1.获取从content对象(不能直接使用content.getContent()，无法更新)；
			 Long _contentDataId = Long.valueOf((String)para.get("contentDataId"));
			 List<CtpContentAll> contentAllList = ctpMainbodyManager.getContentListByContentDataIdAndModuleType(ModuleType.edoc.getKey(), _contentDataId);
			 CtpContentAll contentAll = contentAllList.get(0);
			 //使用该变量表示是否需要删除数据：如果新建协同调用模板保存待发后再另存为模板，此时一条formdata数据就对应两条contentAllBean数据了
			 //如果通过ContentDataId查出有多条数据的话，保存模板后不删除数据，否则其他协同打开时也找不到数据了。
			 boolean needDelFormData = true;
			 if(contentAllList.size()>1){
				 needDelFormData = false;
				 for(CtpContentAll c:contentAllList ){
					 if(c.getId().longValue()==_contentSaveId.longValue()){
						 contentAll = c;
					 }
				 }
			 }
			 //2.获取表单数据Json数据；
			 String _content=formApi4Cap3.getSessioMasterDataBean(_contentDataId).getDataJsonString();
			 //3.将JSON对象设置为content的content值；
			 contentAll.setContent(_content);
			 //4. 更新content对象；
			 ctpMainbodyManager.saveOrUpdateContentAll(contentAll);
			 //5.删除对应的表单数据；
			 if(needDelFormData){
				 try {   
					 Long _contentTemplateId = Long.valueOf(Strings.isBlank((String)para.get("contentTemplateId"))? "0" : (String)para.get("contentTemplateId"));
					 formApi4Cap3.deleteFormData(_contentDataId,_contentTemplateId);
					 //deleteFlowAtts(template.getId());
				 } catch (Exception e) {
					 LOGGER.error("删除表单数据异常", e);
					 throw new BusinessException(e);
				 }
			 }
			 
			if(null != govdocBodyType){ //增加個人模板正文保存邏輯
				int bodyType = Integer.valueOf(para.get("tembodyType").toString());
				String contentIDStr = para.get("govdocContent").toString();
				
				 //保存公文正文 G67 start
				CtpContentAll content2 = new CtpContentAll();
				content2.setId(UUIDUtil.getUUIDLong());
				content2.setCreateId(AppContext.currentUserId());
				content2.setCreateDate(new Date());
		       	content2.setContentType(bodyType);
		       	
		        //复制正文  防止前台界面直接使用该正文ID发送内容，导致模版内容被修改
				if(bodyType != MainbodyType.HTML.getKey()){//如果不是标准正文
    				long contentFileId = Long.parseLong(contentIDStr);
    				long newContentFileId = fileManager.copyFileBeforeModify(contentFileId);
    				//PDF正文没有上传正文时newContentFileId为-1，不处理
	    				//同步更新 v3xfile的filename
    				if(newContentFileId!=-1L){
    					V3XFile v3xfile = fileManager.getV3XFile(newContentFileId);
    					v3xfile.setFilename(newContentFileId+"");
    					fileManager.update(v3xfile);
    					content2.setContent(String.valueOf(newContentFileId));
    				}
    			}else{
    				content2.setContent(contentIDStr);
    			}
		       	content2.setModuleTemplateId(0L);
		       	content2.setModuleId(template.getId());
		       	content2.setModuleType(template.getModuleType());
		       	content2.setTitle(para.get("saveAsTempleteSubject").toString());
		       	content2.setSort(2);
		       	content2.setModifyDate(new Date());
		       	content2.setModifyId(AppContext.currentUserId());
		       	MainbodyService.getInstance().saveOrUpdateContentAll(content2);
		       	//更新表单正文的moduleType
		       	CtpContentAll  ctpcontentAll = GovdocContentHelper.getFormContentByModuleId(template.getId());
		       	if(null != ctpcontentAll){
		       		ctpcontentAll.setModuleType(template.getModuleType());
		       		ctpcontentAll.setModifyDate(new Date());
		       		ctpcontentAll.setModuleTemplateId(-1L);
		       		ctpcontentAll.setTitle(para.get("saveAsTempleteSubject").toString());
		       		Long _contentDataId1 = Long.valueOf((String)para.get("contentDataId"));
		       		//ctpcontentAll.setContent(""); //还原到附件BUG上，如果公用同一个domian数据，会导致调用模板发出去后，影响个人模板
		       		ctpcontentAll.setContent(formApi4Cap3.getSessioMasterDataBean(_contentDataId1).getDataJsonString());
		       		MainbodyService.getInstance().saveOrUpdateContentAll(ctpcontentAll);
		       	}
			}
		}

		boolean isSave =false;
		if(overId == -1L) {
			isSave = true;
		}
		if(String.valueOf(MainbodyType.FORM.getKey()).equalsIgnoreCase(bodytype) && overId == -1L){
			isSave = true;
		}
		template.setIdIfNew();
		long templateId = template.getId();
		template.setSummary(summary);
		template.setBindMarkInfo(markInfo);
		template.setCreateDate(createDate);
		template.setModifyDate(createDate);
		template.setMemberId(user.getId());
		template.setSystem(false);
		template.setDelete(false);
		template.setState(0);
		template.setOrgAccountId(user.getLoginAccount());
		if(!isSave && TemplateEnum.Type.template.name().equals(type)){
			// 删除原有附件
			this.attachmentManager.deleteByReference(templateId,templateId);
		}
		if(TemplateEnum.Type.template.name().equals(type)){// 保存附件
			//String attaFlag = this.attachmentManager.create(ApplicationCategoryEnum.collaboration,templateId,templateId);
			String attaFlag = this.saveAttachmentFromDomain(ApplicationCategoryEnum.collaboration,templateId);
			if(com.seeyon.ctp.common.filemanager.Constants.isUploadLocaleFile(attaFlag)){
	        	CtpTemplateUtil.setHasAttachments(template, true);
	        }
		}
        if(template.getFormParentid() !=null){//另存表单个人模版时先判断父模版id是否存在
        	
        	if(parentTemplate != null){
        		  template.setOrgAccountId(parentTemplate.getOrgAccountId());
            	  template.setFormAppId(parentTemplate.getFormAppId());
        	}
        	
        	if("".equals(invokeTemplateId) || "null".equals(invokeTemplateId) || invokeTemplateId == null)
        		parentTemplate = templateManager.getCtpTemplate(template.getFormParentid());
        	if(parentTemplate ==null){
        		return "templete_notsavePersonalSuccess";//该个人模版所引用的模版已被删除,不能进行另存
        	}else
        		template.setOrgAccountId(parentTemplate.getOrgAccountId());
        }
		//if (isSave) { //新建
			templateManager.saveCtpTemplate(template);
			//TODO templeteConfigManager.pushThisTempleteToMain4Member(templete.getMemberId(), templeteId, -1);//将当前模板推送到首页
		//}else { // 修改
			//templateManager.updateCtpTemplate(template);
		//}
		if(TemplateEnum.Type.template.name().equals(type)) {

		    Map superviseMap = ParamUtil.getJsonDomain("colMainData");
	        SuperviseSetVO ssvo = (SuperviseSetVO)ParamUtil.mapToBean(superviseMap, new SuperviseSetVO(), false);
			superviseManager.saveOrUpdateSupervise4Template(template.getId(),ssvo);
		}
		// 将当前模板推送到首页-我的模板
		List<Long> authMemberIdsList = new ArrayList<Long>();
		authMemberIdsList.add(AppContext.currentUserId());
        templateManager.updateTempleteConfig(templateId, authMemberIdsList);
		return "templete_savePersonalSuccess"; //成功保存个人模板
	}
	
	/**
     * 根据module_id删除表单中的附件
     * @param reference
     */
    private void deleteFlowAtts(Long reference){
        AttachmentManager attachmentManager = (AttachmentManager) AppContext.getBean("attachmentManager");
        List<Attachment> atts = attachmentManager.getByReference(reference);
        if(atts!=null){
            for(Attachment a:atts){
                if(a.getCategory()!=null&&a.getCategory().intValue()==ApplicationCategoryEnum.form.getKey()){
                    attachmentManager.deleteById(a.getId());
                }
            }
        }
    }

	/**
	 * 此方法仅供首页模板栏目调用，对首页模板栏目进行特殊操作
	 * 向模板配置表中插入授权数据，删除未授权数据
	 * @param flipInfo
	 * @param param
	 */
	@SuppressWarnings("unchecked")
	public void transMergeCtpTemplateConfig(FlipInfo flipInfo, Map<String, Object> param) throws BusinessException {
		Long userId = (Long)param.get("userId");
		if(userId == null){
			return ;
		}
		//===================================处理向模板配置表中插入授权数据，删除未授权数据========开始===============================
		//所有的模板集合，包含有权限使用的系统模板和个人模板
		List<CtpTemplate> allTemplateList = new ArrayList<CtpTemplate>();
		//查询出所有的有权限使用的模板，(不包含计划模板)用来判断是否需要往配置表中插入数据
		List<Integer> categoryList = new ArrayList<Integer>();
		categoryList.add(-1);
		categoryList.add(1);
		categoryList.add(2);
		categoryList.add(4);
//		categoryList.add(19);
//		categoryList.add(20);
//		categoryList.add(21);
		categoryList.add(32);
		categoryList.add(401);
		categoryList.add(402);
		categoryList.add(404);
		List<CtpTemplate> allSystemTempletes = this.getSysFormTemplatesByOwnerMemberId(userId,categoryList);
		//查询个人模板
		List<CtpTemplate> personalTempletes  = templateManager.getPersonalTemplates(userId);
        //合并系统模板和个人模板
		allTemplateList.addAll(allSystemTempletes);
        allTemplateList.addAll(personalTempletes);

        //查询配置的所有模板，包含删除的
        Map<String, Object> params = new HashMap<String, Object>();

        params.put("userId", userId);
        List<CtpTemplateConfig> templateConfigList = templateManager.getCtpTemplateConfig(null, params);

        //配置表中所有的模板id
        Set<Long> setAll = new HashSet<Long>();
        if(Strings.isNotEmpty(templateConfigList)){
        	for(CtpTemplateConfig templateConfig : templateConfigList){
    			Long templateId = templateConfig.getTempleteId();
    			setAll.add(templateId);
    		}
        }
        //需要往配置表中插入的数据集合
        List<CtpTemplate> insetList = new ArrayList<CtpTemplate>();
        // 需要从配置表中删除一些没有授权的数据
        Set<Long> all = new HashSet<Long>();
        if(Strings.isNotEmpty(allTemplateList)){
        	for(Iterator<CtpTemplate> iter = allTemplateList.iterator();iter.hasNext();){
            	CtpTemplate tem = iter.next();
            	Long temId = tem.getId();
            	all.add(temId);
            	if(!setAll.contains(temId)){
            		insetList.add(tem);
            	}
            }
        }
        //如果配置表中没有，要将CtpTemplate表中的数据添加
        if(Strings.isNotEmpty(insetList)){
        	List<CtpTemplateConfig> ctpTemplateConfig = this.ctpTemplate2CtpTemplateConfig(insetList);
            DBAgent.saveAll(ctpTemplateConfig);
        }
        //删除配置表中没有授权的数据
        List<CtpTemplateConfig> delList = new ArrayList<CtpTemplateConfig>();
        Set<Long>  filter  = new HashSet<Long>();
    	if(Strings.isNotEmpty(templateConfigList)){
        	for(CtpTemplateConfig templateConfig : templateConfigList){
    			Long templateId = templateConfig.getTempleteId();
    			if(!filter.contains(templateId)){
    				filter.add(templateId);
    			}else{
    				delList.add(templateConfig);
    			}

    			if(!all.contains(templateId)){
    				delList.add(templateConfig);
    			}
    		}
        }
        if(delList.size() > 0){
        	DBAgent.deleteAll(delList);
        }
      //===================================处理向模板配置表中插入数据========结束===============================
	}

	@Override
	@AjaxAccess
	public Integer getMaxSortId(String parentIds) throws BusinessException {
		if(StringUtils.isNotBlank(parentIds)){
			Long parentId=Long.valueOf(parentIds);
			int maxSortId=govdocTemplateDao.getCategoryMaxSortId(parentId);
			return maxSortId+1;
		}
		return 1;
	}
	
	private Map<String, String>  getCtpCategoriesByAuth(Map<String, String> params)
			throws BusinessException {
		long orgAccountId = AppContext.getCurrentUser().getAccountId();
        List<ModuleType> _treeModuleTypes = new ArrayList<ModuleType>();
        ModuleType _treeModuleType = ModuleType.collaboration;
        if(Strings.isBlank(params.get("categoryId"))){
        	List<Long> cid = new ArrayList<Long>();
        	if(params.get("categoryType")!=null){
        		String _c = params.get("categoryType");
        		_treeModuleType = ModuleType.getEnumByKey(Integer.valueOf(_c == null ? "1" : _c));
        		_treeModuleTypes.add(_treeModuleType);
        		cid.add((long) _treeModuleType.getKey());
        	}
        	List<CtpTemplateCategory> categorys = new ArrayList<CtpTemplateCategory>();
        	List<CtpTemplateCategory> orgAccountCategorys = getCategorysByAuth(orgAccountId,_treeModuleTypes);
        	categorys.addAll(orgAccountCategorys);
        	List<MemberPost> memberPosts = orgManager.getMemberConcurrentPosts(AppContext.getCurrentUser().getId());
        	if(Strings.isNotEmpty(memberPosts)){
        		for(MemberPost post : memberPosts){
        			long postId = post.getPostId();
        			categorys.addAll(getCategorysByAuth(postId,_treeModuleTypes));
        		}
        	}
        	for(CtpTemplateCategory c : categorys){
        		cid.add(c.getId());
        	}
        	if(Strings.isNotEmpty(cid)){
        		params.put("categoryId", Strings.join(cid, ","));
        	}
        }
        return params;
	}
	
    public String[] getTemplateAuth(CtpTemplate ctpTemplate) throws BusinessException {
        String[] result = new String[2];
        if (ctpTemplate != null) {
            List<CtpTemplateAuth> auths = templateManager.getCtpTemplateAuths(ctpTemplate.getId(), null);

            result[0] = Functions.showOrgEntities(auths, "authId", "authType", null);
            result[1] = Functions.parseElements(auths, "authId", "authType");
        }
        return result;
    }
    
    /**
     * @param accountId
     * @param templateCategorys
     * @return 检查模板类型的是否授权
     * @throws BusinessException
     */
    private List<CtpTemplateCategory> checkCategoryAuth(Long accountId, List<CtpTemplateCategory> templateCategorys)
            throws BusinessException {
        List<CtpTemplateCategory> result = new ArrayList<CtpTemplateCategory>();
        if (templateCategorys != null) {
            CtpTemplateCategory temp = null;
            User user = AppContext.getCurrentUser();
            for (CtpTemplateCategory ctpTemplateCategory : templateCategorys) {
                if (ctpTemplateCategory.isDelete() == null || !ctpTemplateCategory.isDelete()) {
                    // 单位管理员可访问所有
                    if (orgManager.isAdministrator(user.getLoginName(), orgManager.getAccountById(user.getLoginAccount()))
                            || this.isCtpTemplateCategoryCanManager(AppContext.currentUserId(), accountId,
                                    findRootParent(ctpTemplateCategory)))
                        try {
                            // 返回clone对象
                            temp = (CtpTemplateCategory) ctpTemplateCategory.clone();
                            temp.setId(ctpTemplateCategory.getId());
                            result.add(temp);
                        } catch (CloneNotSupportedException e) {
                            LOGGER.error("", e);
                        }
                }
            }
        }
        return result;
    }
    
    public CtpTemplateCategory findRootParent(CtpTemplateCategory ctpTemplateCategory) throws BusinessException{
        if (ctpTemplateCategory == null)
            return null;
        if (ctpTemplateCategory.getParentId() == null
                || (ctpTemplateCategory.getParentId() > 0 && ctpTemplateCategory.getParentId() < 100)) {
            return ctpTemplateCategory;
        }
        return findRootParent(templateManager.getCtpTemplateCategory(ctpTemplateCategory.getParentId()));
    }
    
    @Override
    public boolean isCtpTemplateCategoryCanManager(long memberId, long loginAccountId, CtpTemplateCategory c)
            throws BusinessException {
        if (c == null)
            return false;
        Set<CtpTemplateAuth> a = templateManager.getCategoryAuths(c);
        for (CtpTemplateAuth auth : a) {
            if (auth.getAuthId().longValue() == memberId
                    && (c.getOrgAccountId() != null && c.getOrgAccountId().longValue() == loginAccountId)) {
                return true;
            }
        }
        return false;
    }
    
    public List<CtpTemplateCategory> getSubCategorys(Long accountId, Long id) throws BusinessException {
        List<CtpTemplateCategory> result = new UniqueList<CtpTemplateCategory>();
        List<ModuleType> intList = new ArrayList<ModuleType>();
        intList.add(ModuleType.collaboration);
        intList.add(ModuleType.form);
        List<CtpTemplateCategory> templeteCategories = templateManager.getCategorys(accountId, intList);
        if (templeteCategories != null) {
            for (CtpTemplateCategory ctpTemplateCategory : templeteCategories) {
                if (ctpTemplateCategory.getParentId() != null && ctpTemplateCategory.getParentId().equals(id)) {
                    result.add(ctpTemplateCategory);
                }
            }
        }
        return result;
    }
    
    private StringBuffer category2HTMLNew(List<CtpTemplateCategory> categories, StringBuffer categoryHTML,
            List<Long> currentNode, int level) throws BusinessException {
        for (CtpTemplateCategory category : categories) {
            Long parentId = category.getParentId();
            if (currentNode.contains(parentId)) {
                categoryHTML.append("<option value='" + category.getId() + "'>");
                for (int i = 0; i < level; i++) {
                    categoryHTML.append("&nbsp;&nbsp;&nbsp;&nbsp;");
                }
                categoryHTML.append(Strings.toHTML(category.getName().trim()) + "</option>\n");
                List<Long> categoryTypes = new ArrayList<Long>();
                categoryTypes.add(category.getId());
                category2HTML(categories, categoryHTML, categoryTypes, level + 1);
            }
        }
        return categoryHTML;
    }
    
    private StringBuffer category2HTML(List<CtpTemplateCategory> categories, StringBuffer categoryHTML,
            List<Long> currentNode, int level) throws BusinessException {
        for (CtpTemplateCategory category : categories) {
            Long parentId = category.getParentId();
            if (currentNode.contains(parentId)) {
                if (AppContext.getCurrentUser().isAdministrator()
                        || templateManager.isTemplateCategoryManager(AppContext.currentUserId(),
                        AppContext.currentAccountId(), findRootParent(category))) {
                    categoryHTML.append("<option value='" + category.getId() + "'>");
                    for (int i = 0; i < level; i++) {
                        categoryHTML.append("&nbsp;&nbsp;&nbsp;&nbsp;");
                    }
                    categoryHTML.append(Strings.toHTML(category.getName().trim()) + "</option>\n");
                    List<Long> categoryTypes = new ArrayList<Long>();
                    categoryTypes.add(category.getId());
                    category2HTML(categories, categoryHTML, categoryTypes, level + 1);
                }
            }
        }
        return categoryHTML;
    }
    
	private void updatePermissionRef(User user) throws BPMException, BusinessException {
		Map<String, String> wfdef = ParamUtil.getJsonDomain("workflow_definition");
		 String processXml = wfdef.get("process_xml");
		 List<String> list = wapi.getWorkflowUsedPolicyIds("collaboration",processXml,null,null);
		 for(String strPname:list){
			 permissionManager.updatePermissionRef(EnumNameEnum.col_flow_perm_policy.name(),strPname,user.getLoginAccount());
		 }
	}
	
	/**
     * 获取前台页面的附件
     * @return
     * @throws BusinessException 
     */
    @SuppressWarnings("unchecked")
    @Override
    public String saveAttachmentFromDomain(ApplicationCategoryEnum type,Long module_id) throws BusinessException{
        
        List assDocGroup = ParamUtil.getJsonDomainGroup("assDocDomain");
        int assDocSize = assDocGroup.size();
        Map assDocMap = ParamUtil.getJsonDomain("assDocDomain");
        if (assDocSize == 0 && assDocMap.size() > 0) {
            assDocGroup.add(assDocMap);
        }
        
        List attFileGroup = ParamUtil.getJsonDomainGroup("attFileDomain");
        int attFileSize = attFileGroup.size();
        Map attFileMap = ParamUtil.getJsonDomain("attFileDomain");
        if (attFileSize == 0 && attFileMap.size() > 0) {
            attFileGroup.add(attFileMap);
        }
        
        assDocGroup.addAll(attFileGroup);
        
        List result;
        try {
            result = attachmentManager.getAttachmentsFromAttachList(ApplicationCategoryEnum.collaboration, module_id,module_id, assDocGroup);
        } catch (Exception e) {
            LOGGER.error("", e);
            throw new BusinessException("创建附件出错");
        }
        
        return attachmentManager.create(result);
    }
    
	@AjaxAccess
	public Map saveTemplate2Cache(Map dataMap)throws BusinessException{
		
		Map listMap = new HashMap();
		Map baseInfo = (Map)dataMap.get("baseInfo");
		String defId = (String)baseInfo.get("defId");
		boolean cap4Form =  Strings.isNotBlank((String)baseInfo.get("cap4Flag")) && "1".equals((String)baseInfo.get("cap4Flag"));
			
		listMap = saveTemplate2cacheCap3(dataMap,baseInfo,defId,listMap);
	
       return listMap ;
	}
	
	private Map saveTemplate2cacheCap3(Map dataMap,Map baseInfo,String defId,Map listMap) throws NumberFormatException, BusinessException{

		FormBean fb = capFormManager.getEditingForm();
		
		//基础信息
		//流程信息
		Map processCreate = (Map)dataMap.get("processCreate");
		CtpTemplate c = (CtpTemplate)ParamUtil.mapToBean(baseInfo,new CtpTemplate(),false);
		
		//TODO 保存流程信息
		int moduleType = ModuleType.collaboration.getKey();//应用类型 java.lang.String 例如协同为collaboration，公文为edoc等。
        String processName = (String)baseInfo.get("subject");
        String processXml = processCreate.get("process_xml") == null || Strings.isBlank((String)processCreate.get("process_xml")) ? "" : (String)processCreate.get("process_xml");//流程定义模版内容 java.lang.String 流程模版xml内容
        String subProcessSetting = processCreate.get("process_subsetting") == null || Strings.isBlank((String)processCreate.get("process_subsetting")) ? "" : (String)processCreate.get("process_subsetting");//流程模版绑定的子流程信息 java.lang.String 流程模版绑定的子流程信息
        //String workflowRule = processCreate.get("process_rulecontent") == null || Strings.isBlank((String)processCreate.get("process_rulecontent")) ? "" : (String)processCreate.get("process_rulecontent");//流程规则说明
        String workflowRule = (String)baseInfo.get("siwfRule");
        String processId = processCreate.get("process_id") == null || Strings.isBlank((String)processCreate.get("process_id")) ? "-1" : (String)processCreate.get("process_id");
        String processEventJson = processCreate.get("process_event") == null || Strings.isBlank((String)processCreate.get("process_event")) ? "" : (String)processCreate.get("process_event");
        String fbatchId = (String)baseInfo.get("fbatchId");
		
        String canPraise= (String)baseInfo.get("canPraise");
        c.setCanPraise(( Strings.isBlank(canPraise) || "false".equals(canPraise)) ? false : true);
        
        String canCopy= (String)baseInfo.get("canCopy");
        c.setCanCopy(( Strings.isBlank(canCopy) || "false".equals(canCopy)) ? false : true);
        
        String canAIProcessing= (String)baseInfo.get("canAIProcessing");
        c.setCanAIProcessing(( Strings.isBlank(canAIProcessing) || "false".equals(canAIProcessing)) ? false : true);
        
        //TODO 高级信息
        c.setResponsible((String)baseInfo.get("responsible"));
        c.setAuditor((String)baseInfo.get("auditor"));
        c.setConsultant((String)baseInfo.get("consultant"));
        c.setInform((String)baseInfo.get("inform"));
        c.setCoreUseOrg((String)baseInfo.get("coreUseOrg"));
		
        String belongOrg = (String)baseInfo.get("belongOrg");
        c.setBelongOrg(Strings.isNotBlank(belongOrg)?Long.valueOf(belongOrg):null);
        
        //文号绑定设置
        String markInfo = "";
        String bindMarkValue = (String) baseInfo.get("bindMarkValue");
        if(Strings.isNotBlank(bindMarkValue)) {
        	String[] bindMarkValues = bindMarkValue.split(",");
        	List<TemplateMarkInfo> markList = new ArrayList<TemplateMarkInfo>();
        	for(int i=0; i<bindMarkValues.length; i++) {
        		String[] markValues = bindMarkValues[i].split("[|]");
        		TemplateMarkInfo tMarkObj = new TemplateMarkInfo();
        		tMarkObj.setSelectType(0);
        		tMarkObj.setMarkType(Integer.parseInt(markValues[0]));
				tMarkObj.setMarkDefId(Long.parseLong(markValues[1]));
				tMarkObj.setWordNo(markValues.length<=2 ? "" : markValues[2]);
				markList.add(tMarkObj);
        	}
        	if(Strings.isNotEmpty(markList)) {
        		markInfo = XMLCoder.encoder(markList);
        	}
        }
        c.setBindMarkInfo(markInfo);
        
        String publishTime = (String)baseInfo.get("publishTime");
        if(Strings.isNotBlank(publishTime)){
        	c.setPublishTime(Datetimes.parse(publishTime)); 
        }
        if(null!=fb){
			if(fb.getGovDocFormType()==FormType.govDocSendForm.getKey()){
				moduleType = ApplicationCategoryEnum.govdocSend.getKey();
			}
			if(fb.getGovDocFormType()==FormType.govDocReceiveForm.getKey()){
				moduleType = ApplicationCategoryEnum.govdocRec.getKey();
			}
			if(fb.getGovDocFormType()==FormType.govDocExchangeForm.getKey()){
				moduleType = ApplicationCategoryEnum.govdocExchange.getKey();
			}
			if(fb.getGovDocFormType()==FormType.govDocSignForm.getKey()){
				moduleType = ApplicationCategoryEnum.govdocSign.getKey();
			}
		}
        if(null == c.getId()){//新建
        	c.setIdIfNew();
        	if (fb.getBind().getExtraAttr("batchId") == null) {
        		LOGGER.error("流程模板依据 batchId 值为空！模板保存异常！");
            }
        	long proId = wapi.insertWorkflowTemplate("" + moduleType, processName, processXml, subProcessSetting, workflowRule, "" + AppContext.currentUserId(), Long.valueOf(defId), Long.parseLong(fbatchId), processEventJson);
        	c.setWorkflowId(proId);
        	c.setCreateDate(new Date());
        	c.setModifyDate(new Date());
        	
        	//正文保存放在这里，以免新建时template的id为空
            GovdocBodyVO bodyVo = new GovdocBodyVO();
            GovdocContentHelper.fillSaveTemplateBodyVo(bodyVo, c, baseInfo);
            c.setBindTHTemplateId(bodyVo.getBindTHTemplateId());
            c.putExtraAttr("govdocContentAll", bodyVo.getBodyContent());
            
        	fb.getBind().addFlowTemplate(c);
        }else{//修改
        	CtpTemplate oldTemplate = fb.getBind().getFlowTemplate(c.getId());
        	if(oldTemplate.getExtraAttr("oldProId") != null){
        		c.putExtraAttr("oldProId", (Long)oldTemplate.getExtraAttr("oldProId"));
        	}
        	c.setCreateDate(oldTemplate.getCreateDate());
        	
        	//正文保存放在这里，以免新建时template的id为空
            GovdocBodyVO bodyVo = new GovdocBodyVO();
            GovdocContentHelper.fillSaveTemplateBodyVo(bodyVo, c, baseInfo);
            c.setBindTHTemplateId(bodyVo.getBindTHTemplateId());
            c.putExtraAttr("govdocContentAll", bodyVo.getBodyContent());
            
        	c = fb.getBind().updateCtpTemplate2Cache(c);
        	c.setModifyMember(AppContext.getCurrentUser().getId());
        	
        	Long oldProcessId = c.getWorkflowId();
        	Long proId = null;
        	if(null == oldProcessId){
        		proId = wapi.insertWorkflowTemplate("" + moduleType, processName, processXml, subProcessSetting, workflowRule, "" + AppContext.currentUserId(), Long.valueOf(defId), Long.parseLong(fbatchId), processEventJson);
        	}else{
        		proId = oldProcessId;
        		proId = wapi.updateWorkflowTemplate("" + moduleType, processName, processXml, subProcessSetting, workflowRule, "" + AppContext.currentUserId(), Long.valueOf(defId),
        				Long.parseLong(fbatchId), oldProcessId, processEventJson);
        		if (c.getExtraAttr("oldProId") == null) {
        			c.putExtraAttr("oldProId", oldProcessId);// 用于删除流程模板的
        		}
        	}
            c.setWorkflowId(proId);
            String s = (String)baseInfo.get("updateProcessDesFlag");
            c.putExtraAttr("updateProcessDesFlag", s);
       }
       c.setModifyDate(new Date());
       c.setState(TemplateEnum.State.normal.ordinal());
       
       c.setProcessLevel(1);//公文模板 流程级别默认为1
       c.setOrgAccountId(AppContext.currentAccountId());
       c.setMemberId(AppContext.currentUserId());
       c.setModuleType(moduleType);
       c.setBodyType("" + MainbodyType.FORM.getKey());
       c.setCategoryId(fb.getCategoryId());
       c.setDelete(false);
       c.setFormAppId(Long.valueOf(defId));
       c.setSystem(true);
       c.setType("template");
       
       //归档
       ColSummary summary = new ColSummary();
       summary.setCanEdit(true);//公文模板 能否修改正文 默认为true chenyq
       String archiveId= (String)baseInfo.get("archiveId");
       summary.setArchiveId(Strings.isBlank(archiveId) ? null : Long.parseLong(archiveId));
       if (summary.getArchiveId() == null) {
           String archive_Id= (String)baseInfo.get("archive_Id");
           summary.setArchiveId( Strings.isBlank(archive_Id) ? null : Long.parseLong(archive_Id));
       }
       
       //附件归档
       String attachmentArchiveId = (String)baseInfo.get("attachmentArchiveId");
       summary.setAttachmentArchiveId(Strings.isBlank(attachmentArchiveId) ? null : Long.parseLong(attachmentArchiveId));
       if(baseInfo.get("processTermTypeCheck")==null){
    	   summary.setProcessTermType(null);
		}else{
			String processTermType= (String)baseInfo.get("processTermType");
			summary.setProcessTermType(Integer.valueOf(processTermType));
		}
       if(baseInfo.get("remindIntervalCheck")==null){
    	   summary.setRemindInterval(null);
		}else{
			String remindInterval = (String)baseInfo.get("remindInterval");
			summary.setRemindInterval(Long.valueOf(remindInterval));
		}
       summary.setBodyType("" + MainbodyType.FORM.getKey());
       
       String canArchive= (String)baseInfo.get("canArchive");
       summary.setCanArchive( ( Strings.isBlank(canArchive) || "false".equals(canArchive))? false : true);
       
       String canForward= (String)baseInfo.get("canForward");
       summary.setCanForward( ( Strings.isBlank(canForward) || "false".equals(canForward)) ? false : true);
       
       String canEditAttachment= (String)baseInfo.get("canEditAttachment");
       summary.setCanEditAttachment( ( Strings.isBlank(canEditAttachment) || "false".equals(canEditAttachment)) ? false : true);
       
       String canModify= (String)baseInfo.get("canModify");
       summary.setCanModify( ( Strings.isBlank(canModify) || "false".equals(canModify)) ? false : true);
       
       //保存合并处理策略
       Map<String,String> mergeDealType = new HashMap<String,String>();
       String canStartMerge= (String)baseInfo.get("canStartMerge");
       if((BPMSeeyonPolicySetting.MergeDealType.START_MERGE.getValue()).equals(canStartMerge)){
    	   mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.START_MERGE.name(), canStartMerge);
       }
       String canPreDealMerge= (String)baseInfo.get("canPreDealMerge");
       if((BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE.getValue()).equals(canPreDealMerge)){
       	mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE.name(), canPreDealMerge);
       }
       String canAnyDealMerge= (String)baseInfo.get("canAnyDealMerge");
       if((BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE.getValue()).equals(canAnyDealMerge)){
       	mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE.name(), canAnyDealMerge);
       }
       summary.setMergeDealType(JSONUtil.toJSONString(mergeDealType));
       
       
       String updateSubject= (String)baseInfo.get("updateSubject");
       summary.setUpdateSubject((Strings.isBlank(updateSubject) || "false".equals(updateSubject)) ? false : true);
       
       String deadline= (String)baseInfo.get("deadline");
       summary.setDeadline( Strings.isBlank(deadline) ? null : Long.parseLong(deadline));
       
       String importantLevel= (String)baseInfo.get("importantLevel");
       summary.setImportantLevel( Strings.isBlank(importantLevel) ? null : Integer.parseInt(importantLevel));
       summary.setTempleteId(c.getId());
       summary.setSubject(c.getSubject());
       
       String advanceremind= (String)baseInfo.get("advanceremind");
       summary.setAdvanceRemind( Strings.isBlank(advanceremind) ? null : Long.parseLong(advanceremind));
       //显示明细
       if (summary.getArchiveId() != null) {
           StringBuilder archiverFormid = new StringBuilder("");
           for (FormViewBean fvb : fb.getFormViewList()) {
               String viewId = (String)baseInfo.get("view_" + fvb.getId());
               if (Strings.isNotBlank(viewId)) {
                   if(archiverFormid.length() > 0){
                       archiverFormid.append("|");
                   }
                   archiverFormid.append(viewId + "." + baseInfo.get("auth_" + fvb.getId()));
               }
           }
           if (Strings.isNotBlank(archiverFormid.toString())) {
               summary.putExtraAttr("archiverFormid", archiverFormid.toString());
           }

           //String archiveForm = map.get("archiveForm");//预归档内容 表单
           String archiveText = fb.hasRedTemplete() ? (String)baseInfo.get("archiveText") : "";//预归档内容 正文
           String archiveTextName = fb.hasRedTemplete() ? (String)baseInfo.get("archiveTextName") : "";//归档后正文名称
           String archiveKeyword = (String)baseInfo.get("archiveKeyword");//文档关键字
           summary.putExtraAttr("archiveText", archiveText);
           summary.putExtraAttr("archiveTextName", archiveTextName);
           summary.putExtraAttr("archiveKeyword", archiveKeyword);
           JSONObject jo = new JSONObject();
           try {
               jo.put("archiveText", archiveText);
               jo.put("archiveTextName", archiveTextName);
               jo.put("archiveKeyword", archiveKeyword);
           } catch (JSONException e) {
        	   LOGGER.error("", e);
           }

           String archiveField = (String)baseInfo.get("archiveFieldName");
           String archiveIsCreate = (String)baseInfo.get("archiveIsCreate");
           if (Strings.isNotBlank(archiveField)) {
               summary.putExtraAttr("archiveField", archiveField);
               summary.putExtraAttr("archiveIsCreate", archiveIsCreate);
               try {
                   jo.put("archiveField", archiveField);
                   jo.put("archiveFieldName", fb.getFieldBeanByName(archiveField).getDisplay());
                   jo.put("archiveIsCreate", archiveIsCreate);
               } catch (JSONException e) {
            	   LOGGER.error("", e);
               }
           }
           summary.setAdvancePigeonhole(jo.toString());
       } else {
           summary.putExtraAttr("archiverFormid", "");
           summary.putExtraAttr("archiveText", "");
           summary.putExtraAttr("archiveTextName", "");
           summary.putExtraAttr("archiveKeyword", "");
           summary.putExtraAttr("archiveField", "");
           summary.putExtraAttr("archiveIsCreate", "");
           summary.setAdvancePigeonhole(null);
       }
       
       String cycleState = Strings.isNotBlank((String)baseInfo.get("cycleState")) ? (String)baseInfo.get("cycleState") : "0";
       summary.putExtraAttr("cycleState", cycleState);
       if ("1".equals(cycleState) && Strings.isNotBlank((String)baseInfo.get("cycleSender"))) {
           summary.putExtraAttr("cycleSender", (String)baseInfo.get("cycleSender"));
           summary.putExtraAttr("cycleStartDate", (String)baseInfo.get("cycleStartDate"));
           summary.putExtraAttr("cycleEndDate", (String)baseInfo.get("cycleEndDate"));
           summary.putExtraAttr("cycleType", (String)baseInfo.get("cycleType"));
           summary.putExtraAttr("cycleMonth", (String)baseInfo.get("cycleMonth"));
           summary.putExtraAttr("cycleOrder", (String)baseInfo.get("cycleOrder"));
           summary.putExtraAttr("cycleDay", (String)baseInfo.get("cycleDay"));
           summary.putExtraAttr("cycleWeek", (String)baseInfo.get("cycleWeek"));
           summary.putExtraAttr("cycleHour", (String)baseInfo.get("cycleHour"));
       } else {
           summary.putExtraAttr("cycleSender", "");
           summary.putExtraAttr("cycleStartDate", "");
           summary.putExtraAttr("cycleEndDate", "");
           summary.putExtraAttr("cycleType", "");
           summary.putExtraAttr("cycleMonth", "");
           summary.putExtraAttr("cycleOrder", "");
           summary.putExtraAttr("cycleDay", "");
           summary.putExtraAttr("cycleWeek", "");
           summary.putExtraAttr("cycleHour", "");
           summary.putExtraAttr("cycleState", "0");
       }

       
       Map<String, String> superviseMap = ParamUtil.getJsonDomain("superviseDiv");
       String superviseStr = XMLCoder.encoder(superviseMap);
       c.putExtraAttr("superviseStr", superviseStr);
       
       String replaceSubject = summary.getSubject() == null ? null : summary.getSubject().replaceAll(new String(new char[]{(char)160}), " ");
       summary.setSubject(replaceSubject);
       c.setSubject(replaceSubject);
       
       c.setSummary(XMLCoder.encoder(summary));
       c.putExtraAttr("summary", summary);
        
       if(AppContext.hasPlugin("ai")){//有AI插件
    	   setProcessMonitorInfo(baseInfo, c);
       }
       
       //授权
       String auth = (String)baseInfo.get("auth");
       List<CtpTemplateAuth> authList = new ArrayList<CtpTemplateAuth>();
       if (auth != null && Strings.isNotBlank(auth)) {
           String[] authObj = auth.split(",");
           CtpTemplateAuth templateAuth = null;
           int sort = 1;
           for (String a : authObj) {
               templateAuth = new CtpTemplateAuth();
               String[] authSp = a.split("\\|");
               templateAuth.setId(UUIDLong.longUUID());
               templateAuth.setAuthId(Long.parseLong(authSp[1]));
               templateAuth.setAuthType(authSp[0]);
               templateAuth.setModuleId(c.getId());
               templateAuth.setCreateDate(new Date());
               templateAuth.setModuleType(moduleType);
               templateAuth.setSort(sort++);
               authList.add(templateAuth);
           }
       }
       c.putExtraAttr("authList", authList);
       
		// cx 部门授权
		String depAuthSet = (String) baseInfo.get("dep_auth");
		String depAuthText = (String) baseInfo.get("dep_auth_txt");
		if (Strings.isNotBlank(depAuthSet) && depAuthSet.indexOf(OrgConstants.GROUPID.toString()) > 0) {
			List<CtpTemplate> templates = fb.getBind().getFlowTemplateList();
			for (CtpTemplate ctpTemplate : templates) {
				ctpTemplate.getExtraMap().remove("depAuthList");
			}
			List<GovdocTemplateDepAuthBO> depAuthSetList = new ArrayList<GovdocTemplateDepAuthBO>();
			List<V3xOrgAccount> accounts = orgManager.getAllAccounts();
			for (V3xOrgAccount account : accounts) {
				if (account.getId() == OrgConstants.GROUPID) {
					continue;
				}
				GovdocTemplateDepAuthBO templateDepAuth = new GovdocTemplateDepAuthBO();
				templateDepAuth.setId(UUIDLong.longUUID());
				templateDepAuth.setOrgName(account.getName());
				templateDepAuth.setOrgId(account.getId());
				templateDepAuth.setOrgType("Account");
				templateDepAuth.setTemplateId(c.getId());
				depAuthSetList.add(templateDepAuth);
			}
			c.putExtraAttr("depAuthList", depAuthSetList);
		} else {
			List<GovdocTemplateDepAuthBO> depAuthSetList = new ArrayList<GovdocTemplateDepAuthBO>();
			if (depAuthSet != null && Strings.isNotBlank(depAuthSet)) {
				String[] authObj = depAuthSet.split(",");
				String[] authtxt = depAuthText.split("、");
				GovdocTemplateDepAuthBO templateDepAuth = null;
				for (int i = 0; i < authObj.length; i++) {
					templateDepAuth = new GovdocTemplateDepAuthBO();
					String[] authSp = authObj[i].split("\\|");
					templateDepAuth.setId(UUIDLong.longUUID());
					templateDepAuth.setOrgName(authtxt[i]);
					templateDepAuth.setOrgId(Long.parseLong(authSp[1]));
					templateDepAuth.setOrgType(authSp[0]);
					templateDepAuth.setTemplateId(c.getId());
					depAuthSetList.add(templateDepAuth);
				}
			}
			c.putExtraAttr("depAuthList", depAuthSetList);
			List<CtpTemplate> templates = fb.getBind().getFlowTemplateList();
			for (CtpTemplate ctpTemplate : templates) {
				Object depAuthListObj = ctpTemplate.getExtraAttr("depAuthList");
				if(null!=depAuthListObj && !ctpTemplate.getId().toString().equals(c.getId().toString())){
					List<GovdocTemplateDepAuthBO> depAuthList = (List<GovdocTemplateDepAuthBO>)depAuthListObj;
					for(int i=0;i<depAuthList.size();i++){
						GovdocTemplateDepAuthBO govdocTemplateDepAuth = depAuthList.get(i);
						for(GovdocTemplateDepAuthBO currDepAuth:depAuthSetList){
							if(currDepAuth.getOrgId().longValue() == govdocTemplateDepAuth.getOrgId()){
								depAuthList.remove(govdocTemplateDepAuth);
								i--;
								break;
							}
						}
					}
				}
			} 
		}

       //关联表单授权
       String relationAuth = (String)baseInfo.get("authRelation");
       List<CtpTemplateRelationAuth> relationAuthList = new ArrayList<CtpTemplateRelationAuth>();
       if (relationAuth != null && Strings.isNotBlank(relationAuth)) {
           String[] relationAuthObj = relationAuth.split(",");
           CtpTemplateRelationAuth formRelationAuth = null;
           for (String a : relationAuthObj) {
               formRelationAuth = new CtpTemplateRelationAuth();
               String[] authSp = a.split("\\|");
               formRelationAuth.setIdIfNew();
               formRelationAuth.setTemplateId(c.getId());
               formRelationAuth.setAuthType(SelectPersonOperation.changeType(authSp[0]));
               formRelationAuth.setAuthValue(String.valueOf(authSp[1]));
               formRelationAuth.setCreateDate(new Date());
               relationAuthList.add(formRelationAuth);
           }
       }
       c.putExtraAttr("relationAuthList", relationAuthList);
       //附件
       try {
           attachmentManager.deleteByReference(c.getId(), c.getId());
           attachmentManager.create(ApplicationCategoryEnum.collaboration, c.getId(), c.getId());
           TemplateUtil.setHasAttachments(c, attachmentManager.hasAttachments(c.getId(), c.getId()));
       } catch (Exception e) {
           throw new BusinessException(e);
       }
       
       listMap.put("frombindlist",fb.getBind().getFlowTemplateList());
       //TODO 等大研发代码确定后再迁移
       c.setDelete(false);
       c.setSystem(true);
	   if(null != c.getPublishTime() && c.getPublishTime().after(new Date())){
		   c.setSubstate(3);
		   c.setState(State.invalidation.ordinal());
	   }else{
		   c.setSubstate(4);
	   }
       //c.setSubstate(4);
       c.setVersion(1);    
       
       return listMap;
	
	}
	public List<CtpTemplate> getSysFormTemplatesByOwnerMemberId(long memberId,List<Integer> moduleTypes)throws BusinessException{
		String hql ="select t from CtpTemplate t where t.moduleType in (:moduleType) and  t.bodyType = :bodyType  and  t.memberId in (:memberId) and t.delete = :delete and t.system = :system ";
		Map<String,Object> m = new HashMap<String,Object>();
		m.put("memberId",memberId);
		m.put("delete",Boolean.FALSE);
		m.put("moduleType",moduleTypes);
		m.put("bodyType",String.valueOf(MainbodyType.FORM.getKey()));
		m.put("system", Boolean.TRUE);
		return DBAgent.find(hql,m);
	}
	
	private List<CtpTemplateConfig> ctpTemplate2CtpTemplateConfig(List<CtpTemplate> template){
		List<CtpTemplateConfig> configList = new ArrayList<CtpTemplateConfig>();
		if(Strings.isEmpty(template)){
			return configList;
		}
		for(CtpTemplate tem:template){
			CtpTemplateConfig config = new CtpTemplateConfig();
			config.setIdIfNew();
			config.setMemberId(AppContext.currentUserId());
			config.setSort(tem.getSort());
			config.setTempleteId(tem.getId());
			config.setType(tem.getModuleType());
			config.setDelete(false);
			configList.add(config);
		}
		return configList;
	}
	
    @Override
    @CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.TtempletManager,Role_NAME.EdocManagement})
    public TemplateCategory saveCategory(TemplateCategory category) throws BusinessException {
        category.setCreateDate(new Date());
        category.setModifyDate(new Date());
        category.setNewId();
        category.setDelete(false);
        templateManager.saveCtpTemplateCategory(category.toPO());
        String memberIds = category.getAuth();
        if (!StringUtil.checkNull(memberIds) && !"<点击此处选择人员>".equals(memberIds)) {
            String[] m1 = memberIds.split(",");
            List<CtpTemplateAuth> ctpTemplateAuth = new ArrayList<CtpTemplateAuth>();
            CtpTemplateAuth auth = null;
            for (String s : m1) {
                Long memberId = Long.valueOf(s.split("[|]")[1]);
                auth = new CtpTemplateAuth();
                auth.setNewId();
                auth.setModuleType(-1);
                auth.setModuleId(category.getId());
                auth.setAuthType(V3xOrgEntity.ORGENT_TYPE_MEMBER);
                auth.setAuthId(memberId);
                auth.setSort(1);
                auth.setCreateDate(new Date());
                ctpTemplateAuth.add(auth);
            }
            templateManager.saveCtpTemplateAuths(ctpTemplateAuth);
            updatePrivAuth(memberIds,null);
        }
        return category;
    }
    
    private void updatePrivAuth(String memberIds,List<String> delarrayList) throws BusinessException {
        List<String> authAllList = new ArrayList<String>();
        List<CtpTemplateAuth> ctpTemplateAuths = templateManager.getCtpTemplateAuths(null, -1,AppContext.currentAccountId());
        if(Strings.isNotEmpty(ctpTemplateAuths)){
        	for(int count = 0 ; count <ctpTemplateAuths.size(); count ++){
        		CtpTemplateAuth ctAuth = ctpTemplateAuths.get(count);
        		authAllList.add("Member|"+ctAuth.getAuthId());
        	}
        }
        String delStr = "";
        if(null != delarrayList){
        	for(int a = 0 ; a < delarrayList.size(); a++){
        		if(!authAllList.contains(delarrayList.get(a))){
        			delStr += delarrayList.get(a)+",";
        		}
        	}
        }
        if(delStr.length() > 0){
        	delStr = delStr.substring(0,delStr.length()-1);
        }
        // 模板管理员角色
        V3xOrgRole role = orgManager.getRoleByName(OrgConstants.Role_NAME.TtempletManager.name(),AppContext.currentAccountId());

        // 如果当前单位不存在此角色
        if(role == null){
            role = orgManager.getRoleByName(OrgConstants.Role_NAME.TtempletManager.name(),null);
            role.setOrgAccountId(AppContext.currentAccountId());
            role.setId(UUIDLong.longUUID());
            role.setCode(String.valueOf(role.getId()));
            orgManagerDirect.addRole(role);
        }
        //增加
        if(Strings.isNotBlank(memberIds) && !"<点击此处选择人员>".equals(memberIds)){
        	if(memberIds.length() > 0){
        		String[] memberAdd = memberIds.split(",");
        		for(int a = 0 ; a < memberAdd.length; a ++){
        			if(!orgManager.isRole(Long.valueOf(memberAdd[a].split("[|]")[1]), AppContext.getCurrentUser().getLoginAccount(),
        			    OrgConstants.Role_NAME.TtempletManager.name(),OrgConstants.MemberPostType.Main)){
        				V3xOrgEntity entity = orgManager.getEntity(memberAdd[a]);
        				orgManagerDirect.addRole2Entity(role.getId(),  AppContext.currentAccountId(),entity);
        			}
        		}
        	}
        }
        //删除
        if(delStr.length() > 1){
        	roleManager.delRole2Entity(role.getName(), AppContext.currentAccountId(), delStr);
        }
    }
    @Override
    @CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.TtempletManager,Role_NAME.EdocManagement})
    public TemplateCategory updateCategory(TemplateCategory category) throws BusinessException {
        category.setModifyMember(AppContext.getCurrentUser().getId());
        category.setModifyDate(new Date());
        category.setDelete(false);
        CtpTemplateCategory old = templateManager.getCtpTemplateCategory(category.getId());
        if(old!=null){
            category.setCreateDate(old.getCreateDate());
            category.setCreateMember(old.getCreateMember());
            //OA-159891 修改公文模板分类后，在公文所属应用中未更新 chenyq 20181113
            /*if(null != old.getType()){
            	category.setType(old.getType());
            }*/
        }
        CtpTemplateCategory pocategory = category.toPO();
        templateManager.updateCtpTemplateCategory(pocategory);
        //得到该模板的授权信息
        List<CtpTemplateAuth> ctpTemplateAuths = templateManager.getCtpTemplateAuths(category.getId(), -1);
        // 先删除之前的授权
        templateManager.deleteCtpTemplateAuths(category.getId(), null);
        String memberIds = category.getAuth();
        List<String> oldAuthInfoList = new ArrayList<String>();//以前该模板的授权信息
        List<String> deleteIdsList = new ArrayList<String>();//存放此次该模板删除的

        List<String> curAuthIds = (List<String>)Arrays.asList(memberIds.split(","));

        for(int a = 0 ; a < ctpTemplateAuths.size(); a ++){
        	CtpTemplateAuth ctpTemplateAuth = ctpTemplateAuths.get(a);
        	oldAuthInfoList.add("Member|"+ctpTemplateAuth.getAuthId());
        }
    	for(int n = 0 ; n <oldAuthInfoList.size(); n ++){
    		if(!curAuthIds.contains(oldAuthInfoList.get(n))){
    			deleteIdsList.add(oldAuthInfoList.get(n));
    		}
    	}
        if (!StringUtil.checkNull(memberIds) && !"<点击此处选择人员>".equals(memberIds)) {
            String[] m1 = memberIds.split(",");
            List<CtpTemplateAuth> ctpTemplateAuth = new ArrayList<CtpTemplateAuth>();
            CtpTemplateAuth auth = null;
            for (String s : m1) {
                Long memberId = Long.valueOf(s.split("[|]")[1]);
                auth = new CtpTemplateAuth();
                auth.setNewId();
                auth.setModuleType(-1);
                auth.setModuleId(category.getId());
                auth.setAuthType(V3xOrgEntity.ORGENT_TYPE_MEMBER);
                auth.setAuthId(memberId);
                auth.setSort(1);
                auth.setCreateDate(new Date());
                ctpTemplateAuth.add(auth);
            }
            templateManager.saveCtpTemplateAuths(ctpTemplateAuth);
        }
        updatePrivAuth(memberIds,deleteIdsList);
        return category;
    }
    
	@Override
	public Map<Integer, List<TemplateMarkInfo>> getFormBindMarkList(User user) throws BusinessException {
		FormBean fBean = formApi4Cap3.getEditingForm();
		List<FormFieldBean> docMarkFieldList = fBean.getFieldsByType(FormFieldComEnum.BASE_MARK);
		List<FormFieldBean> serialNoFieldList = fBean.getFieldsByType(FormFieldComEnum.BASE_INNER_MARK);
		List<FormFieldBean> signMarkFieldList = fBean.getFieldsByType(FormFieldComEnum.BASE_SIGN_MARK);
		
		String markType = "";
		if(Strings.isNotEmpty(docMarkFieldList)) {
			markType += "0,";
		}
		if(Strings.isNotEmpty(serialNoFieldList)) {
			markType += "1,";
		} 
		if(Strings.isNotEmpty(signMarkFieldList)) {
			markType += "2,";
		}
		if(Strings.isNotBlank(markType)) {
			markType = markType.substring(0, markType.length() - 1);
		}
		Map<Integer, List<TemplateMarkInfo>> markMap = edocApi.getFormBindMarkList(markType, user.getLoginAccount());
		if(Strings.isNotEmpty(docMarkFieldList)) {
			if(!markMap.containsKey(0)) {
				markMap.put(0, new ArrayList<TemplateMarkInfo>());
			}
		} 
		if(Strings.isNotEmpty(serialNoFieldList)) {
			if(!markMap.containsKey(1)) {
				markMap.put(1, new ArrayList<TemplateMarkInfo>());
			}
		} 
		if(Strings.isNotEmpty(signMarkFieldList)) {
			if(!markMap.containsKey(2)) {
				markMap.put(2, new ArrayList<TemplateMarkInfo>());
			}
		}
		return markMap;
	}
    
	/**
	 * 设置流程监控和自动处理条件
	 * @param baseInfo
	 * @param c
	 */
	private void setProcessMonitorInfo(Map baseInfo, CtpTemplate c) {
		c.putExtraAttr(FormConstant.FLOW_TEMPLATE_EXT_MONITOR_CAN_SEND_MSG, (String) baseInfo.get("processMonitorInput"));
		c.putExtraAttr(FormConstant.FLOW_TEMPLATE_EXT_PROCESS_MONITOR, (String)baseInfo.get("monitorArray"));
        c.putExtraAttr(FormConstant.FLOW_TEMPLATE_EXT_AI_PROCESSING_CONDITION,(String)baseInfo.get("autoDealConditionVal"));
	}
	
	public Map checkTemplateIsDelete(String tid)throws BusinessException{
		HashMap result = new HashMap();
		CtpTemplate templete = templateManager.getCtpTemplate(Long.parseLong(tid));
		if(null == templete){
			result.put("isDel", "0");
		}else{
			if(templete.isDelete()){
				result.put("isDel", "1");
			}else{
				result.put("isDel", "0");
			}
		}
		return  result;
	}
	
    @Override
    @CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement,Role_NAME.TtempletManager})
    public void updateTempleteAuth(Long[] ids, String value, Integer categoryType) throws BusinessException {

        if (ids != null) {
            User user = AppContext.getCurrentUser();
            templateManager.deleteCtpTemplateAuths(ids, null);


            if (!StringUtil.checkNull(value)) {
                String[] m1 = value.split(",");
                List<CtpTemplateAuth> ctpTemplateAuth = new ArrayList<CtpTemplateAuth>();
                CtpTemplateAuth auth = null;
                for (Long id : ids) {
                    CtpTemplate t = templateManager.getCtpTemplate(id);
					List<CtpTemplateAuth> ctpTemplateAuthOne = new ArrayList<CtpTemplateAuth>();
                    for (String s : m1) {
                        String authType = s.split("[|]")[0];
                        Long memberId = Long.valueOf(s.split("[|]")[1]);
                        auth = new CtpTemplateAuth();
                        auth.setNewId();
                        auth.setModuleType(t.getModuleType());
                        auth.setModuleId(id);
                        auth.setAuthType(authType);
                        auth.setAuthId(memberId);
                        auth.setSort(1);
                        auth.setCreateDate(new Date());
                        ctpTemplateAuth.add(auth);
						ctpTemplateAuthOne.add(auth);
                    }
//                    if(ctpTemplateAuthOne.size() == 0 ){
//						ArrayList<Long> delTemplates = new ArrayList<Long>();
//						delTemplates.add(t.getId());
//						templateCacheManager.deleteCacheTemplateAuthByTemplateIds(delTemplates);
//					}else{
//						templateCacheManager.synchronizeTemplateAuthCache(t,ctpTemplateAuthOne);
//					}
					templateManager.synchronizeTemplateAuthCache(t,ctpTemplateAuthOne);

                    
                	 if(AppContext.hasPlugin("edoc") && edocApi.isEdoc(categoryType)){//2: 发文，3：收文,//5:签报
                		 govdocLogManager.insertAppLog(user, GovdocAppLogAction.EDOC_TEMPLETEAUTHORIZE.key(), user.getName(),t.getSubject());
                     }else if(ApplicationCategoryEnum.info.getKey()==categoryType){
                     	/** (应用设置) 应用模板授权*/
                    	 govdocLogManager.insertAppLog(user, GovdocAppLogAction.INFORMATION_TEMPLATE_AUTHORIZE.key(), user.getName(),t.getSubject());
                     }else{
                    	 govdocLogManager.insertAppLog(user, GovdocAppLogAction.COLL_TEMPLETEAUTHORIZE.key(), user.getName(),t.getSubject()) ;
                     }
                }
                templateManager.saveCtpTemplateAuths(ctpTemplateAuth);

            }else{
            	//没有新增权限全部是删除权限，删除完成后删除对应的缓存
				if(ids != null && ids.length > 0){
					ArrayList<Long> delTemplates = new ArrayList<Long>(ids.length);
					for (int i=0; i < ids.length; i++){
						delTemplates.add(ids[i]);
					}
					templateManager.deleteCacheTemplateAuthByTemplateIds(delTemplates);
				}
			}
        }
    
    }
	@AjaxAccess
	@Override
	public List<Long> getPersonTemplateIds(String subject, String type, boolean isEdoc) {
		User user = AppContext.getCurrentUser();
		List list = govdocTemplateDao.getPersonTemplateIds(user.getId(),subject,type,isEdoc);
		return list;
	}
	

	@Override
	public Integer getAccountGovdocSysTemplateCount(Long accountId) throws BusinessException {
		return govdocTemplateDao.getAccountGovdocSysTemplateCount(accountId);
	}
	
	@AjaxAccess
	@Override
	public void updateSubStateByFormId(Long fromId, Integer subState) throws BusinessException {
		if(fromId == null || subState == null){
			return;
		}
		templateManager.updateTemplateSubStateByFormId(fromId, subState.intValue());
	}

	@AjaxAccess
	public String getBodyType(String templateId) throws BusinessException {
		String bodyType = "";
		if(Strings.isNotBlank(templateId)) {
			CtpTemplate template = templateManager.getCtpTemplate(Long.parseLong(templateId));
			if(template != null){
				bodyType = template.getBodyType();
			}
		}
		return bodyType;
	}
	
	/*****************************************  set注入 start  ***********************************************************/
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}
	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}
	public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
        this.formApi4Cap3 = formApi4Cap3;
    }
	public void setCtpMainbodyManager(MainbodyManager ctpMainbodyManager) {
		this.ctpMainbodyManager = ctpMainbodyManager;
	}
	public void setEdocApi(EdocApi edocApi) {
		this.edocApi = edocApi;
	}
	public void setWapi(WorkflowApiManager wapi) {
		this.wapi = wapi;
	}
	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}
	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}
	public void setSuperviseManager(SuperviseManager superviseManager) {
		this.superviseManager = superviseManager;
	}
	public void setGovdocTemplateDao(GovdocTemplateDao govdocTemplateDao) {
		this.govdocTemplateDao = govdocTemplateDao;
	}
	public void setOrgManagerDirect(OrgManagerDirect orgManagerDirect) {
		this.orgManagerDirect = orgManagerDirect;
	}
	public void setRoleManager(RoleManager roleManager) {
		this.roleManager = roleManager;
	}
	public void setCapFormManager(CAPFormManager capFormManager) {
		this.capFormManager = capFormManager;
	}
	public void setGovdocLogManager(GovdocLogManager govdocLogManager) {
		this.govdocLogManager = govdocLogManager;
	}
	/*****************************************  set注入   end  ***********************************************************/
}
