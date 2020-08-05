package com.seeyon.ctp.common.content.mainbody;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.edoc.api.EdocApi;
import com.seeyon.apps.edoc.bo.EdocSummaryBO;
import com.seeyon.apps.open.api.BusinessOpenApi;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.domain.ReplaceBase64Result;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.po.DataContainer;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.util.Enums.FormType;
import com.seeyon.ctp.form.util.FormConstant;
import com.seeyon.ctp.portal.api.PortalApi;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.HttpSessionUtil;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;

/**
 * 正文组件的控制器
 * @author wangfeng
 *
 */
public class MainbodyController extends BaseController {
    private static final Logger LOGGER        = Logger.getLogger(MainbodyController.class);
    private MainbodyManager ctpMainbodyManager;
    private PortalApi portalApi;
    private EdocApi edocApi;
    private FormApi4Cap3 formApi4Cap3;
	private FileManager fileManager;

    public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
        this.formApi4Cap3 = formApi4Cap3;
    }

    public void setPortalApi(PortalApi portalApi) {
        this.portalApi = portalApi;
    }

	/** 正文组件查看
     * @see com.seeyon.ctp.common.controller.BaseController#index(HttpServletRequest, HttpServletResponse)
     */
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
    	ModelAndView content = null;
        try{
	        Map params = request.getParameterMap();
	        String isNew = ParamUtil.getString(params, "isNew", false);
	        // 必须传（如果是新建，传-1），各模块业务数据id，在moduleId为-1的情况下是新建状态，moduleId不为-1说明是查看状态，在数据库中已经有该正文数据
	        long moduleId = ParamUtil.getLong(params, "moduleId", -1l, true);
	        // 必须传，正文所属模块类型formView:表单样式 collabration:协同
	        int moduleType = ParamUtil.getInt(params, "moduleType", true);
	        String openFrom = ParamUtil.getString(params, "openFrom", false);
			String govdoc = ParamUtil.getString(params, "govdoc");
			String isPrint = ParamUtil.getString(params, "isPrint", false);
			String nodeName = ParamUtil.getString(params, "nodeName", false);
			String subState = ParamUtil.getString(params, "subState");
			AppContext.putThreadContext("subState",subState);
			//当前打开数据是否是子流程数据
			String isSubFlow = ParamUtil.getString(params, "isSubFlow", false);
			AppContext.putThreadContext("isSubFlow",isSubFlow);
			if(StringUtil.checkNull(isPrint)){
				isPrint = "false";
			}
	        String resend = ParamUtil.getString(params, "resend", "");
            String templateId = ParamUtil.getString(params, "templateId", "");
	        AppContext.putThreadContext("openFrom", openFrom);
	        AppContext.putThreadContext("templateId", templateId);
	        String contentDataId = ParamUtil.getString(params, "contentDataId", "");
	        String formRecordid = ParamUtil.getString(params, "formRecordid", "");
	        Long _summaryId = ParamUtil.getLong(params, "summaryId", -1l, false);//流程数据复制需要当前数据的summaryid，也就是moduleid，因为拷贝附件的时候需要用它来当附件表的referenceid
	        AppContext.putThreadContext("contentDataId", contentDataId);
	        AppContext.putThreadContext("_summaryId", _summaryId);

	        /*******************************兼容帆软报表穿透start**************************************/
	        String isFromFrReport = ParamUtil.getString(params, "isFromFrReport", false);
	        if(isFromFrReport!=null){
	        	Map p = new HashMap();
	            p.put("contentDataId", moduleId);
	            List<CtpContentAll> contentList = DBAgent.findByNamedQuery("ctp_common_content_findByContentDataId", p);
            	if(contentList==null||contentList.size()<=0){
            		throw new BusinessException("帆软报表穿透失败，未找到内容，内容ID："+moduleId);
            	}
	            CtpContentAll tempContent =  contentList.get(0);
	            moduleId = tempContent.getModuleId();
	            moduleType = tempContent.getModuleType();
	        }
	        /*******************************兼容帆软报表穿透end**************************************/
	        
	        /*********************************************表单查看模式处理start************************************************/
	        String style = ParamUtil.getString(params, "style");//表单查看样式
	        AppContext.putThreadContext("style", style);//放入线程变量,FormMainBodyHandler.handleContentView
	        /*********************************************表单查看模式处理end************************************************/
	        
	        ModuleType mType = ModuleType.getEnumByKey(moduleType);
	        if (mType == null) {
	            throw new BusinessException("moduleType is not validate!");
	        }	
	        // 权限id，不传默认为0,rightId有三种情况，-1：表单设计态；-2：表单管理员预览态；真实存在的rightId
	        String rightId = ParamUtil.getString(params, "rightId", "", false);
	        String formAppid = request.getParameter("formAppid");
	        String form = request.getParameter("form"); 
	        long myFormId = request.getParameter("myFormId")==null?-1l:Long.parseLong(request.getParameter("myFormId").toString());
	        String govdocForm = request.getParameter("govdocForm");

	        //如果是触发消息来的，则重新根据当前登录人获取权限ID add by chenxb
			String triggerMessage = ParamUtil.getString(params, "triggerMessage", "", false);
			if (Strings.isNotEmpty(triggerMessage)) {
				String formId = ParamUtil.getString(params, "formId", "", false);
				AppContext.putThreadContext("triggerMessage", triggerMessage);
				AppContext.putThreadContext("formId", formId);
			}
			//原来的线程变量，现在统一通过方法参数来传递；需要废弃线程变量
			Map<String,Object> threadContextParams = new HashMap<String,Object>();
			threadContextParams.put("subState",subState);
			// 查看第几个正文，默认是第一个正文
	        Integer indexParam = ParamUtil.getInt(params, "indexParam", 0);
	        
	        //查看模式 1 可编辑状态  2不可编辑状态 3 可编辑但没有JS事件状态 4 表单设计态
	        int viewState = ParamUtil.getInt(params, "viewState", CtpContentAllBean.viewState_readOnly);
	        
	        //复制自哪条正文数据，fromCopy是ctp_content_all中的的id，默认为-1
	        Long fromCopy =  ParamUtil.getLong(params, "fromCopy", -1l, false);
            if (Strings.isNotBlank(formRecordid) && "dataRelation".equals(openFrom)) {
                List<CtpContentAll> contentList = ctpMainbodyManager.getContentListByContentDataIdAndModuleType(ModuleType.collaboration.getKey(), Long.valueOf(formRecordid));
                if (!contentList.isEmpty()) {
                    fromCopy = contentList.get(0).getId();
                }
            }
	        
	        List<CtpContentAllBean> contentList = null;
	        CtpContentAllBean contentAll = null;
	        //如果是查看修改状态
	        if (isNew == null || "false".equals(isNew.trim())) {
	        	if(govdoc  != null && govdoc.equals("1")){
	        		//contentList = ctpMainbodyManager.transGovdocContentView(mType, moduleId,viewState,rightId,indexParam);
	        	}else{
//协同V5 OA-161746外单位交换来的文，在签收单中外单位的部门和人员未显示单位简称区隔 start
	        		if("edoc".equals(mType.name()))
					{
						Long targetSummaryId = moduleId;
						if(targetSummaryId == -1)
						{
							targetSummaryId = _summaryId;
						}
						if(targetSummaryId != -1)
						{
							EdocSummaryBO targetSummary = edocApi.getEdocSummary(targetSummaryId);
							if(targetSummary != null)
							{
								Integer govdocType = targetSummary.getGovdocType();
								if(ApplicationSubCategoryEnum.edoc_jiaohuan.getKey() == govdocType)
								{
									AppContext.putThreadContext("formType",FormType.govDocExchangeForm.name());
								}
							}
						}

					}
					//TODO 线程线变量需要改造 改为调用 这个方法contentList = ctpMainbodyManager.transContentViewResponse(mType, moduleId,viewState,rightId,indexParam,fromCopy,threadContextParams)
					//协同V5 OA-161746外单位交换来的文，在签收单中外单位的部门和人员未显示单位简称区隔 end
		            contentList = ctpMainbodyManager.transContentViewResponse(mType, moduleId,viewState,rightId,indexParam,fromCopy);

	        	}
	            if(contentList==null||contentList.size()==0){
	            	throw new BusinessException("该正文不存在,moduleId="+moduleId);
	            }else{
	            	contentAll = contentList.get(0);
	            }
	        } else {//如果是新建状态
	            int contentType = ParamUtil.getInt(params, "contentType", true);
	            MainbodyType cType = MainbodyType.getEnumByKey(contentType);
	            if (cType == null) {
	                throw new BusinessException("contentType is not validate!");
	            }
	            
	            ContentContext context = new ContentContext();
                context.setTransOfficeId(request.getParameter("transOfficeId"));
                context.setOriginalNeedClone(request.getParameter("originalNeedClone"));
                context.setSessionId(HttpSessionUtil.getSessionId(request));
                
	            contentAll = ctpMainbodyManager.transContentNewResponse(mType, moduleId, cType, rightId,threadContextParams, context);
	            contentList = new ArrayList<CtpContentAllBean>();
	            contentList.add(contentAll);
	        }
			;
			if(Strings.isNotEmpty(nodeName)){
				AppContext.removeSessionArrribute(String.valueOf(contentAll.getContentDataId()));
				AppContext.putSessionContext(String.valueOf(contentAll.getContentDataId()),nodeName);
			}

	        LOGGER.info("isNew="+String.valueOf(isNew)+",moduleId="+String.valueOf(moduleId)+",_summaryId="+String.valueOf(_summaryId)+",contentAll.id="+((null!=contentAll) ? String.valueOf(contentAll.getId()):"空正文"));
	        
            if (!ctpMainbodyManager.checkRight(contentAll, request, response)){
                return null;
            }

	        /*********************************************表单查看模式处理start************************************************/
	        style = (String) AppContext.getThreadContext("style");
	        if("3".equals(style)||"4".equals(style)){//轻量级样式
	        	content = new ModelAndView("common/content/lightContent");
	        }else{//INFOPATH设计器样式
	        	content = new ModelAndView("common/content/content");
	        }
	        if(StringUtil.checkNull(style)){
	        	style="1";
	        }
	        /*********************************************表单查看模式处理end************************************************/
	        if(Strings.isNotBlank(formAppid)){
	        	FormBean formBean = formApi4Cap3.getForm(Long.parseLong(formAppid));
	        	if(formBean!=null && (formBean.getFormType()==FormType.govDocSignForm.getKey()||formBean.getFormType()==FormType.govDocSendForm.getKey()||formBean.getFormType()==FormType.govDocReceiveForm.getKey()||formBean.getFormType()==FormType.govDocExchangeForm.getKey())){
	        		request.setAttribute("isGovdocForm", 1);
	        		content.addObject("isGovdocForm",1);
	        	}
	        }
	        if(Strings.isNotBlank(form)){
        		request.setAttribute("isGovdocForm", 1);
        		content.addObject("isGovdocForm",1);
	        }
	        content.addObject("openFrom", openFrom);
	        content.addObject("viewState", viewState);
	        content.addObject("contentList", contentList);
	        content.addObject("formJson", contentAll.getExtraMap().get("formJson"));
	        content.addObject("indexParam", indexParam);
	        content.addObject("isNew", isNew);
	        content.addObject("style",style);
	        content.addObject("styleName",("4".equals(style)?"phone":"pc"));
	        content.addObject("resend",resend);
			content.addObject("isPrint",isPrint);
	        String accountLogo= portalApi.getAccountLogo(AppContext.currentAccountId());
	        content.addObject("logoIcon",accountLogo);
	        //content.addObject("logoIcon",portalTemplateManager.getAccountLogo(AppContext.currentAccountId()));
	        
        	Map<String,String> map = AppContext.getCurrentUser().getCustomizeJson(FormConstant.FORM_STYLE, Map.class);
        	String rememberStyle = null;
        	if(map!=null){
        		rememberStyle = map.get(String.valueOf(contentList.get(0).getContentTemplateId()));
        	}
        	content.addObject("rememberStyle",(rememberStyle!=null));
        	/*
        	EdocOptionBO optionBO = new EdocOptionBO();
        	optionBO.setOpenFrom(openFrom);
    		optionBO.setFormAppid(formAppid);
    		String affairId = request.getParameter("affairId");
    		optionBO.setAffairId(affairId);
    		optionBO.setFrom(request.getParameter("from"));
    		
    		//公文已经去掉了所以去掉
    		if(edocApi!=null){
    			EdocOptionBO edocOptionBO = edocApi.getMainBodyLogic(optionBO);
    			if(edocOptionBO!=null){
    				content.addObject("policy", edocOptionBO.getPolicy());
    				content.addObject("affairState", edocOptionBO.getAffairState());
    				content.addObject("optionId",edocOptionBO.getOptionId());
    				content.addObject("opinionType", edocOptionBO.getOpinionType());
    				content.addObject("opinionsJs", edocOptionBO.getOpinionsJs());
    				content.addObject("senderOpinion", edocOptionBO.getSenderOpinion());
    				content.addObject("senderOpinionAttStr", edocOptionBO.getSenderOpinionAttStr());
    				content.addObject("ols",edocOptionBO.getOls());
    				content.addObject("allowCommentInForm", edocOptionBO.getAllowCommentInForm());
    				content.addObject("hwjs",edocOptionBO.getHwjs());
    			}
    		}
        	*/
        	
        	//执行对外开放的扩展接口，只执行一个实现
            Map implMap = AppContext.getBeansOfType(BusinessOpenApi.class);
            if(implMap.size() > 0){
            	for(Object obj : implMap.values()){
            		BusinessOpenApi bean=(BusinessOpenApi) obj;
            		content.addObject("printContentJSPath",bean.getPrintContentJSPath());
            		content.addObject("printContentMethodName",bean.getPrintContentMethodName());
            		content.addObject("mainBodyDisplayJSPath",bean.getMainBodyDisplayJSPath());
            		content.addObject("mainBodyOnloadMethodName",bean.getMainBodyOnloadMethodName());
            		break;
    			}
            }
        }catch(BusinessException be){
        	LOGGER.error(be.getMessage(), be);
        	throw be;
        }
        return content;
    }
    
    /**
     * 公文新建的时候 正文调用的方法
     * @param request
     * @param response
     * @return
     * @throws BusinessException
     */
    public ModelAndView invokingForm(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
    	ModelAndView content = null;
    	try{ 
    		
    		Map params = request.getParameterMap();
	        String isNew = ParamUtil.getString(params, "isNew", false);
	        // 必须传（如果是新建，传-1），各模块业务数据id，在moduleId为-1的情况下是新建状态，moduleId不为-1说明是查看状态，在数据库中已经有该正文数据
    		long moduleId = ParamUtil.getLong(params, "moduleId", -1l, false);	  
    		Long formId = ParamUtil.getLong(params, "formId", -1l, false);
	        // 必须传，正文所属模块类型formView:表单样式 collabration:协同
	        int moduleType = ParamUtil.getInt(params, "moduleType", -1,false);	
	        String openFrom = ParamUtil.getString(params, "openFrom", false);
	        AppContext.putThreadContext("openFrom", openFrom);
	        String contentDataId = ParamUtil.getString(params, "contentDataId", "");
	        AppContext.putThreadContext("contentDataId", contentDataId);
	        /*******************************兼容帆软报表穿透start**************************************/
	        String isFromFrReport = ParamUtil.getString(params, "isFromFrReport", false);
	        if(isFromFrReport!=null){
	        	Map p = new HashMap();
	            p.put("contentDataId", moduleId);
	            List<CtpContentAll> contentList = DBAgent.findByNamedQuery("ctp_common_content_findByContentDataId", p);
            	if(contentList==null||contentList.size()<=0){
            		throw new BusinessException("帆软报表穿透失败，未找到内容，内容ID："+moduleId);
            	}
	            CtpContentAll tempContent =  contentList.get(0);
	            moduleId = tempContent.getModuleId();
	            moduleType = tempContent.getModuleType();
	        }
	        /*******************************兼容帆软报表穿透end**************************************/
	        
	        /*********************************************表单查看模式处理start************************************************/
	        String style = ParamUtil.getString(params, "style");//表单查看样式
	        AppContext.putThreadContext("style", style);//放入线程变量,FormMainBodyHandler.handleContentView
	        /*********************************************表单查看模式处理end************************************************/
	        
	        ModuleType mType = null;
	        if (moduleType != -1) {
				mType = ModuleType.getEnumByKey(moduleType);
				if (mType == null) {
					throw new BusinessException("moduleType is not validate!");
				}
			}
			// 权限id，不传默认为0,rightId有三种情况，-1：表单设计态；-2：表单管理员预览态；真实存在的rightId
	        String rightId = ParamUtil.getString(params, "rightId", "", false);
			// 查看第几个正文，默认是第一个正文
	        Integer indexParam = ParamUtil.getInt(params, "indexParam", 0);
	        
	        //查看模式 1 可编辑状态  2不可编辑状态 3 可编辑但没有JS事件状态 4 表单设计态
	        int viewState = ParamUtil.getInt(params, "viewState", CtpContentAllBean.viewState_readOnly);
	        
	        //复制自哪条正文数据，fromCopy是ctp_content_all中的的id，默认为-1
	        Long fromCopy =  ParamUtil.getLong(params, "fromCopy", -1l, false);
	        
	        List<CtpContentAllBean> contentList = null;
	        CtpContentAllBean contentAll = null;
	        //如果是查看修改状态
        	if (isNew == null || "false".equals(isNew.trim())) {
	            contentList = ctpMainbodyManager.transContentViewResponse(mType, moduleId,viewState,rightId,indexParam,fromCopy);
	            if(contentList==null||contentList.size()==0){
	            	throw new BusinessException("该正文不存在,moduleId="+moduleId);
	            }else{
	            	contentAll = contentList.get(0);
	            }
	        } else {//如果是新建状态
    			Map<String, Object> map = new HashMap<String, Object>();
    			String distributeContentDataId = request.getParameter("distributeContentDataId");
    			String distributeContentTemplateId = request.getParameter("distributeContentTemplateId");
    			if (Strings.isNotBlank(distributeContentDataId)) {
    				map.put("distributeContentDataId", distributeContentDataId);
				}
    			if (Strings.isNotBlank(distributeContentTemplateId)) {
    				map.put("distributeContentTemplateId", distributeContentTemplateId);
    			}
    			String forwardAffairId = request.getParameter("forwardAffairId");
    			if (Strings.isNotBlank(forwardAffairId)) {
    				AffairManager affairManager = (AffairManager) AppContext.getBean("affairManager");
    				CtpAffair affair = affairManager.get(Long.valueOf(forwardAffairId));
    				map.put("forwardSubject", affair.getSubject());
    				//2018 - 1- 30  陈祥 解耦  写死的逻辑直接干掉；
    				/*boolean isGzEditon = "true".equals(PropertiesConfiger.getInstance().getProperty("system.gz.edition"));
    		        if(isGzEditon && affair.getSubApp() == ApplicationSubCategoryEnum.edoc_shouwen.getKey()){
//    		        	EdocElementManager edocManager = (EdocManager) AppContext.getBean("edocManager");
//    		        	EdocSummary summary = edocManager.getEdocSummaryById(affair.getObjectId(),false);
//    		        	map.put("serialNo", summary.getSerialNo());
//    		        	map.put("string1", summary.getSendUnit());
//    		        	map.put("text8", summary.getSubject()+"  "+summary.getDocMark());
    		        }*/
				}
    			//会议纪要转发文  chenyq 2017-12-23
//    			String meetingSummaryId = request.getParameter("meetingSummaryId");
//    			if(Strings.isNotBlank(meetingSummaryId)){
//    				MtSummary mtSummary = meetingSummaryDao.get(Long.valueOf(meetingSummaryId));
//        			String mtSubject = mtSummary.getMtName();
//    				map.put("forwardSubject", mtSubject);
//    			}
				map.put("formId", formId);
				map.put("oldSummaryId", request.getParameter("oldSummaryId"));
				map.put("signSummaryId", request.getParameter("signSummaryId"));
				map.put("forwardSummaryId", request.getParameter("forwardSummaryId"));
				//如果是普通发文 待办调用模板 也需要复制数据
				String summaryId = request.getParameter("summaryId");
				if(!Strings.isBlank(summaryId)){
					map.put("oldSummaryId", summaryId);
				}
				String oldElementStr = ParamUtil.getString(params, "oldElements");
				if (Strings.isNotBlank(oldElementStr)) {
					try {
						oldElementStr = oldElementStr.replaceAll("%(?![0-9a-fA-F]{2})", "%25"); //GOVA-2896
						oldElementStr = URLDecoder.decode(oldElementStr, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						oldElementStr = "";
					}
					String oldElementArr[] = oldElementStr.split("@");
					Map<String, String> oldElementMap = new HashMap<String, String>();
					for (String string : oldElementArr) {
						String arr[] = string.split(":");
						oldElementMap.put(arr[0], arr[1]);
					}
					map.put("oldElements", oldElementMap);
				}
				contentList = ctpMainbodyManager.transContentViewResponseForGovdoc(mType, moduleId,viewState,rightId,indexParam,fromCopy,map);
	            if(contentList==null||contentList.size()==0){
	            	throw new BusinessException("该正文不存在,moduleId="+moduleId);
	            }else{
	            	contentAll = contentList.get(0);
	            }
	        }

    		
    		/*********************************************表单查看模式处理start************************************************/
    		style = (String)AppContext.getThreadContext("style");
    		if("3".equals(style)||"4".equals(style)){//轻量级样式
    			content = new ModelAndView("common/content/lightContent");
    		}else{//INFOPATH设计器样式
    			content = new ModelAndView("common/content/content");
    		}
    		if(StringUtil.checkNull(style)){
    			style="1";
    		}
    		contentAll.setModuleType(ModuleType.edoc.getKey());
    		/*********************************************表单查看模式处理end************************************************/
    		
    		content.addObject("openFrom", openFrom);
    		content.addObject("viewState", viewState);
    		content.addObject("contentList", contentList);
    		content.addObject("formJson", contentAll.getExtraMap().get("formJson"));
    		content.addObject("indexParam", indexParam);
    		content.addObject("isNew", isNew);
    		content.addObject("style",style);
    		content.addObject("styleName",("4".equals(style)?"phone":"pc"));
//    		content.addObject("logoIcon",portalTemplateManager.getAccountLogo(AppContext.currentAccountId()));
    		content.addObject("formId",formId);
    		
    		Map<String,String> map =AppContext.getCurrentUser().getCustomizeJson(FormConstant.FORM_STYLE, Map.class);
    		String rememberStyle = null;
    		if(map!=null){
    			rememberStyle = map.get(String.valueOf(contentList.get(0).getContentTemplateId()));
    		}
    		content.addObject("rememberStyle",(rememberStyle!=null));
    		if(moduleType == ModuleType.edoc.getKey()){
    			content.addObject("isGovdocForm",1);
    		}
    		
    	}catch(BusinessException be){
    		LOGGER.error(be.getMessage(), be);
    		throw be;
    	}
    	return content;
    }
    /**新增或者修改正文内容
     * @param request
     * @param response
     * @return ModelAndView
     * @throws SQLException
     * @throws Exception
     */
    public ModelAndView saveOrUpdate(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/html;charset=UTF-8");
        DataContainer dc = new DataContainer();
        CtpContentAllBean contentAll = null;
        PrintWriter out = null;
        try {
            out = response.getWriter();
            //获取当前是第几个正文
            //long currentIndex = ParamUtil.getLong(request.getParameterMap(), "currentIndex");
            Map<String, String> div = ParamUtil.getJsonDomain("_currentDiv");
            String curDiv = div.get("_currentDiv");
            //获取正文参数，组装正文对象, ContentAll
            contentAll = (CtpContentAllBean) ParamUtil.mapToBean(ParamUtil.getJsonDomain("mainbodyDataDiv_"+curDiv),CtpContentAllBean.class, true);

			ReplaceBase64Result result = fileManager.replaceBase64Image(contentAll.getContent());
			contentAll.setContent(result.getHtml());

			String needCheckRule = request.getParameter("needCheckRule");//是否需要校验表单业务规则
            if(needCheckRule!=null){contentAll.putExtraMap("needCheckRule", needCheckRule);}
            
            //如果是预处理，只合并更新缓存对象，并不执行入库操作；
            //这个方法的目的，就是为了预提交是去掉AOP事务，功能跟 #transContentSaveOrUpdate一样
            if(request.getParameter("notSaveDB") != null) {
                ctpMainbodyManager.transContentSaveOrUpdateWithoutDB(contentAll);
            }
            else{
                ctpMainbodyManager.transContentSaveOrUpdate(contentAll);
            }
            //需要返回生成的ContentAll对象json对象和保存状态是否成功，供应用层使用
			String contentId="";
			if(Strings.isNotEmpty(contentAll.getContent())){
				contentId = contentAll.getContent();
			}
            contentAll.setContent("");
            dc.add("success", "true");
            CtpContentAll c = contentAll.toContentAll();
            c.setTitle(Strings.escapeJson(c.getTitle()));
            dc.add("contentAll", c);
            dc.add("sn", (DataContainer)contentAll.getAttr("sn"));
            contentAll.setContent(contentId);
        } catch (Exception e) {
            dc.add("success", "false");
            if(contentAll!=null){
            	contentAll.setContent("");
            	CtpContentAll c = contentAll.toContentAll();
            	c.setTitle(Strings.escapeJson(c.getTitle()));
                dc.add("contentAll", c);
            }
            String errorMsg = "";
            if(e.getMessage()!=null){
            	errorMsg = e.getMessage().toString().trim();
            }
            dc.add("errorMsg",errorMsg);
            LOGGER.error(e.getMessage(), e);
        } finally{
            out.print(dc.getJson());
        }
        out.flush();
        out.close();
        return null;
    }
    
	public MainbodyManager getCtpMainbodyManager() {
		return ctpMainbodyManager;
	}

	public void setCtpMainbodyManager(MainbodyManager ctpMainbodyManager) {
		this.ctpMainbodyManager = ctpMainbodyManager;
	}

	public EdocApi getEdocApi() {
		return edocApi;
	}

	public void setEdocApi(EdocApi edocApi) {
		this.edocApi = edocApi;
	}

	public FileManager getFileManager() {
		return fileManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}
}