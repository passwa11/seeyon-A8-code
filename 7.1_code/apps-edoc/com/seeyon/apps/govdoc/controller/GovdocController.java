package com.seeyon.apps.govdoc.controller;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Maps;
import com.seeyon.apps.collaboration.enums.ColOpenFrom;
import com.seeyon.apps.collaboration.vo.AttachmentVO;
import com.seeyon.apps.edoc.api.NewGovdocDataHandler;
import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.govdoc.constant.GovdocEnum;
import com.seeyon.apps.govdoc.constant.GovdocEnum.NewGovdocFrom;
import com.seeyon.apps.govdoc.constant.GovdocEnum.OperationType;
import com.seeyon.apps.govdoc.constant.GovdocListEnum.GovdocListTypeEnum;
import com.seeyon.apps.govdoc.helper.GovdocContentHelper;
import com.seeyon.apps.govdoc.helper.GovdocNewHelper;
import com.seeyon.apps.govdoc.helper.GovdocSummaryHelper;
import com.seeyon.apps.govdoc.manager.GovdocCommentManager;
import com.seeyon.apps.govdoc.manager.GovdocContentManager;
import com.seeyon.apps.govdoc.manager.GovdocContinueManager;
import com.seeyon.apps.govdoc.manager.GovdocExchangeManager;
import com.seeyon.apps.govdoc.manager.GovdocLockManager;
import com.seeyon.apps.govdoc.manager.GovdocManager;
import com.seeyon.apps.govdoc.manager.GovdocOpenManager;
import com.seeyon.apps.govdoc.manager.GovdocPishiManager;
import com.seeyon.apps.govdoc.manager.GovdocPubManager;
import com.seeyon.apps.govdoc.manager.GovdocSignetManager;
import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.apps.govdoc.manager.GovdocTrackManager;
import com.seeyon.apps.govdoc.manager.impl.GovdocDocTemplateManagerImpl;
import com.seeyon.apps.govdoc.manager.impl.NewGovdocDataHelper;
import com.seeyon.apps.govdoc.mark.helper.GovdocMarkHelper;
import com.seeyon.apps.govdoc.po.GovdocExchangeDetail;
import com.seeyon.apps.govdoc.po.GovdocExchangeMain;
import com.seeyon.apps.govdoc.util.GovdocParamUtil;
import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.apps.govdoc.vo.GovdocBodyVO;
import com.seeyon.apps.govdoc.vo.GovdocCommentVO;
import com.seeyon.apps.govdoc.vo.GovdocComponentVO;
import com.seeyon.apps.govdoc.vo.GovdocDealVO;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.apps.govdoc.vo.GovdocRepealVO;
import com.seeyon.apps.govdoc.vo.GovdocSummaryVO;
import com.seeyon.apps.govdoc.vo.GovdocTrackVO;
import com.seeyon.apps.ocip.exchange.edoc.GovdocOcipSSOUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.constants.ProductEditionEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentEditHelper;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.common.plugin.PluginDefinition;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.comment.CtpCommentAll;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.shareMap.V3xShareMap;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.report.engine.api.ReportConstants.DesignType;
import com.seeyon.ctp.report.engine.api.manager.ReportApi;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.ReqUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.v3x.edoc.domain.EdocSummary;

/**
 * 新公文控制器
 * 
 * @author 唐桂林
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class GovdocController extends BaseController {

	private static final Log LOGGER = CtpLogFactory.getLog(GovdocController.class);

	private GovdocManager govdocManager;
	private GovdocPubManager govdocPubManager;
	private GovdocSummaryManager govdocSummaryManager;
	private GovdocExchangeManager govdocExchangeManager;
	private GovdocContinueManager govdocContinueManager;
	private GovdocOpenManager govdocOpenManager;
	
	private GovdocContentManager govdocContentManager;
	private GovdocCommentManager govdocCommentManager;
	private GovdocTrackManager govdocTrackManager;
	private GovdocLockManager govdocLockManager;
	private GovdocSignetManager govdocSignetManager;
	private GovdocPishiManager govdocPishiManager;
	private GovdocDocTemplateManagerImpl govdocDocTemplateManager;
	private TemplateManager templateManager;
	private AffairManager affairManager;
	private ConfigManager configManager;
	private ReportApi reportApi;
	private FormApi4Cap3 formApi4Cap3;

	/*************************** 00000 公文导航 start ***************************/
	/**
	 * 公文应用设置
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView databaseIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("govdoc/database/database_index");
		mav.addObject("isAdministrator", AppContext.getCurrentUser().isAdministrator());
		return mav;
	}
	/**
	 * 公文应用设置
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView databaseMain(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("govdoc/database/database_main");
		String listType = ReqUtil.getString(request, "listType");
		if("element".equals(listType)){
			return super.redirectModelAndView("/govdoc/element.do?method=list");
		}else if("exchangeaccount".equals(listType)){
			return super.redirectModelAndView("/govdoc/exchangeaccount.do?method=list");
		}
		return mav;
	}
	/**
	 * 公文列表
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("govdoc/list/list_index");
		User currentUser = AppContext.getCurrentUser();
        if(AppContext.hasPlugin("collaboration")) {
            mav.addObject("hasNewColRole", currentUser.hasResourceCode("F01_newColl"));
        }
		return mav;
	}
	/**
	 * 公文查询
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView queryIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("govdoc/query/query_index");
		
		Map<String,Object> params = Maps.newHashMap();
		params.put("category", ApplicationCategoryEnum.edoc.name());
		params.put("designType", DesignType.QUERY.name());
		params.put("userId", AppContext.currentUserId());
		List<Map<String,Object>> reports = reportApi.findDesign(params);
		
		mav.addObject("hasReport",!reports.isEmpty());
		return mav;
	}
	/**
	 * 公文跳转页面
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView list(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String govdocType = GovdocParamUtil.getString(request, "govdocType", "");
		String listType = GovdocParamUtil.getString(request, "listType", "");
		String configId = GovdocParamUtil.getString(request, "configId", "");
		String controller = GovdocListTypeEnum.getControllerName(listType);
		String method = GovdocListTypeEnum.getMethodName(listType);

		String url = controller + "?method=" + method + "&listType=" + listType;
		if (Strings.isNotBlank(govdocType)) {
			url += "&govdocType=" + govdocType;
		}
		if (Strings.isNotBlank(configId)) {
			url += "&configId=" + configId;
		}
		return super.redirectModelAndView(url.toString());
	}
	/**
	 * 公文统计
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView statIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("govdoc/stat/stat_index");
		Map<String,Object> params = Maps.newHashMap();
		params.put("category", ApplicationCategoryEnum.edoc.name());
		params.put("designType", DesignType.STATISTICS.name());
		params.put("userId", AppContext.currentUserId());
		List<Map<String,Object>> reports = reportApi.findDesign(params);
		
		mav.addObject("hasReport",!reports.isEmpty());
		return mav;
	}
	/**
	 * 代录设置
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView leaderPishiSet(HttpServletRequest request,HttpServletResponse response)throws Exception{
		ModelAndView mav = new ModelAndView("/govdoc/leaderpishiset/leaderPishiSet");
		mav.addObject("type", "LeaderPishi");
		mav.addObject("leaderPishiSet", "代录设置");
        return mav;
	}
	/**
	 * 代领导批示功能
	 * @author rz
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView leaderPishiList(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView modelAndView = new ModelAndView("edoc/listLeaderPishi");
		modelAndView.addObject("type", request.getParameter("leaderPishi"));
		return modelAndView;
	}
	/**
	 * 领导批示编号设置
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView leaderShortNameSet(HttpServletRequest request,HttpServletResponse response)throws Exception{
		return new ModelAndView("/govdoc/leaderpishiset/leaderShortNameSet");
	}
	/*************************** 00000 公文导航   end ***************************/

	
	/*************************** 11111 拟文界面 start ***************************/
	/**
	 * 表单公文新建页面
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView newGovdoc(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("govdoc/new/newGovdoc");
		try {
			GovdocNewVO newVo = new GovdocNewVO();
			
			// 处理参数
			GovdocNewHelper.fillNewGovdocVoByParams(request, newVo);
			// 封装开关
			GovdocNewHelper.fillGovdocSwitch(newVo);
			//节点权限策略判断
			GovdocNewHelper.fillNodePolicyInfo(newVo);
			//拟文入口-调用模板
			if (NewGovdocFrom.template.name().equals(newVo.getFrom())||NewGovdocFrom.bizconfig.name().equals(newVo.getFrom())) {		
				govdocManager.fillNewVoByTemplate(newVo);
				//校验模板
				if(Strings.isNotBlank(newVo.getErrorMsg())) {
					GovdocUtil.newCollAlertAndBack(response, StringEscapeUtils.escapeJavaScript(newVo.getErrorMsg()));
					return null;
				}
				newVo.setNew(true);
			} else if (NewGovdocFrom.resend.name().equals(newVo.getFrom())) {//拟文入口-已发列表的重复发起
				govdocManager.fillNewVoByResend(newVo);
			} else if (NewGovdocFrom.waitSend.name().equals(newVo.getFrom())) {//拟文入口-来自待发
				newVo.setNewBusiness("0");
				try {
					govdocManager.fillNewVoByWaitSend(newVo);
					if(Strings.isNotBlank(newVo.getErrorMsg())) {
						GovdocUtil.newCollAlertAndBack(response, StringEscapeUtils.escapeJavaScript(newVo.getErrorMsg()));
						return null;
					}
				} catch (BPMException e) {
					LOGGER.error(e.getMessage(), e);
					GovdocUtil.newCollAlert(response, StringEscapeUtils.escapeJavaScript("模板已经被删除，或者您已经没有该模板的使用权限"));
					return null;
				}
			} else if (NewGovdocFrom.distribute.name().equals(newVo.getFrom())) {//拟文入口-来自分办
				// 如果是从分办进来的
				govdocContentManager.updateContentByFenban(newVo);
			} else if(NewGovdocFrom.trans.name().equals(newVo.getFrom())) {//拟文入口-转公文
				//来自转公文
				initNewGovdocTranVO(newVo,request);
			}else {//拟文入口-新建
				
			}
			// 处理一些公共的数据
			GovdocNewHelper.fillSecretLevelList(newVo);

			// 封装表单数据
			if (GovdocNewHelper.fillFormData(newVo) == null) {
				mav = new ModelAndView("govdoc/new/newgovdoc_noform");
				return mav;
			}
			GovdocNewHelper.fillCommonData(newVo);
			GovdocContentHelper.fillNewBodyData(newVo);
			// 如果是快速发文 封装部分数据
			GovdocNewHelper.fillQuickSendData(newVo);
			// 封装续办相关数据
			GovdocNewHelper.fillCustomDealWithDate(newVo);
			// 封装流程超期相关数据
			GovdocNewHelper.fillTremDate(newVo);
			if (newVo.isCustomDealWithTemplate() && !newVo.isCustomDealWith()) {
				GovdocUtil.newCollAlert(response, StringEscapeUtils.escapeJavaScript("本模板为续办模板,请开启续办权限!"));
				return null;
			}
			
			// 附言
			if (NewGovdocFrom.waitSend.name().equals(newVo.getFrom()) || NewGovdocFrom.resend.name().equals(newVo.getFrom())) { // 某些情况下不需要进这里面，比如调用模板，应该取模板的附言
				GovdocNewHelper.findSenderCommentLists(newVo);
			}
			newVo.setAction("new");
			GovdocMarkHelper.fillNewMarkParameter(newVo);
						
			mav.addObject("vobj", newVo);
			mav.addObject("contentContext", newVo.getContentContext());
			
			// 用于判断是新建页面打开控件还是处理界面打开
			mav.addObject("currentPageName", "newGov");
			
			//获取下载到本地的打印模版路径
			String printTempPath = govdocDocTemplateManager.getLocalPrintTemplate(newVo.getDefaultFormId());
			mav.addObject("hasPrintTempPath", Strings.isNotBlank(printTempPath));
			
			govdocManager.fillNewVoByExchange(newVo);
		} catch(Exception e) {
			LOGGER.error("进入公文拟文界面出错", e);
		}
		return mav;
	}

    /**
     * 转协同数据处理
     * @Author      : caihl
     * @param vobj
     * @throws BusinessException
     */
    private void initNewGovdocTranVO(GovdocNewVO vobj, HttpServletRequest request) throws BusinessException {
    	String cashId = request.getParameter("cashId");
		Object object = V3xShareMap.get(cashId);
		if(object == null){
			return;
		}
		Map<String, String> map = (Map)object;
		String subject = map.get("subject") == null?"":map.get("subject");
		String manual = map.get("manual") == null?"":map.get("manual");
		String handlerName = map.get("handlerName") == null?"":map.get("handlerName");
		String sourceId = map.get("sourceId") == null?"":map.get("sourceId");
		String extendInfo = map.get("ext") == null?"":map.get("ext");
		String bodyType = map.get("bodyType") == null?"":map.get("bodyType");
		String bodyContent = map.get("bodyContent") == null?"":map.get("bodyContent");
		//String personId = map.get("personId") == null?"":map.get("personId");
		//String from = map.get("from") == null?"":map.get("from");
		String sub_app = map.get("sub_app") == null?"":map.get("sub_app");

        NewGovdocDataHandler handler = NewGovdocDataHelper.getHandler(handlerName);

        vobj.setSubApp(sub_app);
        Map<String, Object> params = null;
        if(handler != null){
            params = handler.getParams(sourceId, extendInfo);
        }
        
        int contentType = MainbodyType.OfficeWord.getKey();
        bodyType = MainbodyType.OfficeWord.name();
        if ("true".equalsIgnoreCase(manual) && handler != null) {// 从后端代码获取参数
            if(Strings.isBlank(subject)){
            	vobj.setSubjectFromTrans(handler.getSubject(params));
            }
            bodyType = handler.getBodyType(params);
            contentType = handler.getContentType(params);
            bodyContent = handler.getBodyContent(params);
        }


        CtpContentAll content = new CtpContentAll();

        content.setContent(bodyContent);
        content.setCreateDate(new Date());
        content.setContentType(contentType);

        vobj.getBodyVo().setBodyContent(content);
        vobj.getBodyVo().setBodyType(bodyType);
        vobj.getBodyVo().setContentType(contentType);
        vobj.getBodyVo().setContent(bodyContent);
        vobj.getBodyVo().setCreateDate(content.getCreateDate());

        if (handler != null) {
            List<Attachment> atts = handler.getAttachments(params);
            // 附件处理
            vobj.setAtts(atts);
            if (Strings.isNotEmpty(atts)) {
            	govdocContentManager.setAttachmentJSON(vobj, atts);
            }
            //OA-164670 盖章的会议纪要转公文后能修改正文
//            vobj.setCloneOriginalAtts(true);
        }

    }
	
	/**
	 * 查看属性设置
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView attributeSettingDialog(HttpServletRequest request, HttpServletResponse response) {
		// 定义跳转目标路径
		ModelAndView mav = new ModelAndView("govdoc/dialog/attributeSettingDialog");
		try {
			String affairId = request.getParameter("affairId");
			String isHistoryFlag = request.getParameter("isHistoryFlag");
			Map args = new HashMap();
			args.put("affairId", affairId);
			args.put("isHistoryFlag", isHistoryFlag);
			Map map = govdocPubManager.getAttributeSettingInfo(args);
			request.setAttribute("ffattribute", map);
			// 督办属性设置 标志位
			mav.addObject("supervise", map.get("supervise"));
			mav.addObject("openFrom", request.getParameter("openFrom"));
			mav.addObject("processTermTypeName", map.get("processTermTypeName"));
			mav.addObject("processTermType",  map.get("processTermType"));
		} catch(Exception e) {
			LOGGER.error("进入公文属性设置查看界面出错", e);
		}
		return mav;
	}
	/**
	 * 表单公文-存为个人模板弹出框
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView templateDialog(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("govdoc/dialog/saveAsTemplateDialog");
		try {
			String _hasWorkflow = request.getParameter("hasWorkflow");
			String _subject = request.getParameter("subject");
			String _tembodyType = request.getParameter("tembodyType");
			String _formtitle = request.getParameter("formtitle");
			//String _defaultValue = request.getParameter("defaultValue");
			String _temType = request.getParameter("temType");
			if ("hasnotTemplate".equals(_temType)) {
				mav.addObject("canSelectType", "all");
			} else if ("template".equals(_temType)) {
				mav.addObject("canSelectType", "template");
			} else if ("workflow".equals(_temType)) {
				mav.addObject("canSelectType", "workflow");
			} else if ("text".equals(_temType)) {
				mav.addObject("canSelectType", "text");
			}
			mav.addObject("hasWorkflow", _hasWorkflow);
			mav.addObject("subject", _subject);
			mav.addObject("tembodyType", _tembodyType);
			mav.addObject("formtitle", _formtitle);
			//mav.addObject("defaultValue", _defaultValue);
		} catch(Exception e) {
			LOGGER.error("进入存为个人模板弹出框界面出错", e);
		}
		return mav;
	}
	/*************************** 11111 拟文界面   end ***************************/
	
	
	/*************************** 22222 公文发送 start ***************************/
	/**
	 * 表单公文-发送
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@CheckRoleAccess(roleTypes = { Role_NAME.SendEdoc, Role_NAME.SignEdoc, Role_NAME.RecEdoc })
	public ModelAndView send(HttpServletRequest request, HttpServletResponse response) {
		try {
			GovdocNewVO newVo = new GovdocNewVO();
			
			// 前段参数完整性校验
			if (!GovdocNewHelper.checkHttpParamValid(newVo, request)) {
				GovdocUtil.newCollAlert(response, StringEscapeUtils.escapeJavaScript(newVo.getErrorMsg()) + "');");
				return null;
			}
			
			newVo.setCurrentDate(DateUtil.currentDate());
			newVo.setCurrentUser(AppContext.getCurrentUser());
			GovdocNewHelper.fillSendParam(newVo, request);
			
			// 陈应强 判断流程发起时，正文是否存在 start
			if(!govdocContentManager.checkContent(newVo)) {
				GovdocUtil.newCollAlert(response, newVo.getErrorMsg());
				return null;
			}
			
			// 发送
			govdocManager.transSend(newVo, EdocConstant.SendType.normal);
			
			// 保存 转发文关联
			zhuanFaWenRelation(request, newVo);
			
			// 如果是分办,需要把被分办的流程给处理掉
			sendDistribute(request, response, newVo);
			
		} catch(Exception e) {
			LOGGER.error("公文发送出错", e);
		}
		
		return null;
	}

	/**
	 * 表单公文-保存待发
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView saveDraft(HttpServletRequest request, HttpServletResponse response) {
		try {
			request.setAttribute("__huanhang", "\r\n");
			
			//从domains中取参数
			Map para = ParamUtil.getJsonDomain("colMainData");
			
			GovdocNewVO newVo = new GovdocNewVO();
			newVo.setCurrentUser(AppContext.getCurrentUser());
			newVo.setCurrentDate(DateUtil.currentDate());
			newVo = GovdocNewHelper.fillSaveDrftParam(newVo, para, request);
			
			govdocManager.transSaveDraft(newVo, para);
			
			//转发文关联
			zhuanFaWenRelation(request,newVo);
			// 分办
			sendDistribute(request, response, newVo);
			
			/** 如果是续办,则保存续办相关信息 */
			newVo.setNew(true);
			govdocContinueManager.setCustomDealWith(newVo);
	
			super.rendJavaScript(response,  "parent.endSaveDraft('" + newVo.getSummary().getId()  + "','" + newVo.getContentSaveId()  + "','" + newVo.getSenderAffair().getId() + "','" + newVo.getTemplateId()  + "',"  + newVo.getIsQuickSend() + ")");
		} catch(Exception e) {
			LOGGER.error("公文保存待发出错", e);
		}
		return null;
	}
	/*************************** 22222 公文发送   end ***************************/
	
	
	/*************************** 33333 公文查看 start ***************************/
	/**
	 * 表单公文-查看-主页
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView summary(HttpServletRequest request, HttpServletResponse response) throws Exception {
		long l1 = System.currentTimeMillis();
		ModelAndView mav = new ModelAndView("govdoc/summary/summary");
		// 用于判断是新建页面打开控件还是处理界面打开
		try {
			ModelAndView modelAndView = GovdocOcipSSOUtil.getGovdocModel(request);
			if(modelAndView == null) {
				modelAndView = GovdocOcipSSOUtil.getOutEdocLink(request);
			}
			if (modelAndView != null) {
				Map<String, Object> model = modelAndView.getModel();
				String error = (String) model.get("error");
				if (error != null) {
					WFComponentUtil.webAlertAndClose(response, error);
					
					return null;
				}
				return modelAndView;
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			WFComponentUtil.webAlertAndClose(response, "无法访问该公文,可能本地代理关闭导致！");
            return null;	
		}
		
		long l2 = System.currentTimeMillis();
		//从request中获取参数
		GovdocSummaryVO summaryVO = GovdocSummaryHelper.fillSummaryVoParam(request);
		long l3 = System.currentTimeMillis();
		LOGGER.info("公文查看summary界面获取参数耗时：" + (l3 - l2));
		
		//设置对象到VO对象
		if(!govdocPubManager.fillSummaryObj(summaryVO)) {
			String openFrom = request.getParameter("openFrom");
			if(openFrom!=null&&"F8Reprot".equals(openFrom)){
				response.setContentType("text/html;charset=UTF-8");
		        PrintWriter out = response.getWriter();
		        out.println("<script>");
		        out.println("alert('"+ org.apache.commons.lang3.StringEscapeUtils.escapeEcmaScript(summaryVO.getErrorMsg())+"');");
		        out.println(" window.parent.close();");
		        out.println("</script>");
		        return null;
			}
			WFComponentUtil.webAlertAndClose(response, summaryVO.getErrorMsg());
			return null;
		}
		
		long l4 = System.currentTimeMillis();
		LOGGER.info("公文查看summary界面设置对象耗时：" + (l4 - l3));
		try {
			summaryVO.setAction("summary");
			govdocManager.transShowSummary(summaryVO);
			
			long l5 = System.currentTimeMillis();
			LOGGER.info("公文查看summary界面transShowSummary耗时：" + (l5 - l4));
			
			mav.addObject("summaryVO", summaryVO);
			// 用于判断是新建页面打开控件还是处理界面打开
			mav.addObject("currentPageName", "summary");
		} catch (Exception e) {
			LOGGER.error("打开新公文详情界面出错", e);
		}
		
		long l6 = System.currentTimeMillis();
		LOGGER.info("公文查看summary方法总耗时：" + (l6 - l1));
		
		return mav;
	}
	
	/**
	 * 表单公文-查看-公文单
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView componentPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		long l1 = System.currentTimeMillis();
		ModelAndView mav = new ModelAndView("govdoc/summary/componentPage");
		
		long l2 = System.currentTimeMillis();
		GovdocComponentVO summaryVO = GovdocNewHelper.fillComponentParam(request);
		long l3 = System.currentTimeMillis();
		LOGGER.info("公文查看componentPage界面获取参数耗时：" + (l3 - l2));
		try {
			//设置对象到VO对象
			if(!govdocPubManager.fillComponentObj(summaryVO)) {
				WFComponentUtil.webAlertAndClose(response, summaryVO.getErrorMsg());
				return null;
			}
			
			long l4 = System.currentTimeMillis();
			LOGGER.info("公文查看componentPage界面设置对象耗时：" + (l4 - l3));
			
			govdocManager.transComponentPage(summaryVO, request);
			
			long l5 = System.currentTimeMillis();
			LOGGER.info("公文查看summary界面transComponentPage耗时：" + (l5 - l4));
		} catch (Exception e) {
			LOGGER.error("打开新公文文单界面出错", e);
			return null;
		}
		mav.addObject("summaryVO", summaryVO);
		
		long l6 = System.currentTimeMillis();
		LOGGER.info("公文查看componentPage方法总耗时：" + (l6 - l1));
		
		return mav;
	}	

	/**
	 * 公文意见单独页面
	 */
	public ModelAndView commentPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		long l1 = System.currentTimeMillis();
		ModelAndView mav = new ModelAndView("govdoc/summary/commentPage");
		GovdocComponentVO summaryVO = GovdocNewHelper.fillComponentParam(request);
		try {
			//设置对象到VO对象
			if(!govdocPubManager.fillComponentObj(summaryVO)) {
				WFComponentUtil.webAlertAndClose(response, summaryVO.getErrorMsg());
				return null;
			}
			govdocManager.transCommentPage(summaryVO, request);
		} catch (Exception e) {
			LOGGER.error("打开新公文文单界面出错", e);
			return null;
		}
		mav.addObject("summaryVO", summaryVO);
		
		long l6 = System.currentTimeMillis();
		LOGGER.info("公文查看commentPage方法总耗时：" + (l6 - l1));
		return mav;
	}
	/**
	 * 表单公文-查看-附件
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView findAttachmentListBuSummaryId(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("apps/collaboration/attachmentList");
		try {
			String summaryId = request.getParameter("summaryId");
			String memberId = request.getParameter("memberId");
			// 附件
			String _openFrom = request.getParameter("openFromList");
			if (!ColOpenFrom.supervise.name().equals(_openFrom) && Strings.isNotBlank(memberId)) {
				AppContext.putThreadContext(Comment.THREAD_CTX_DOCUMENT_AFFAIR_MEMBER_ID, Long.valueOf(memberId));
			}
			List<AttachmentVO> attachmentVOs = govdocContentManager.getAttachmentListBySummaryId(Long.valueOf(summaryId), Long.valueOf(memberId));
			Set<AttachmentVO> atts = new HashSet<AttachmentVO>();
			atts.addAll(attachmentVOs);
			mav.addObject("attachmentVOs", atts);
			mav.addObject("attSize", attachmentVOs.size());
		} catch(Exception e) {
			LOGGER.error("公文查看附件出错", e);
		}
		return mav;
	}
	
	/**
	 * 获取显示HTML正文
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView htmlBody(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mv = new ModelAndView("govdoc/summary/htmlBody");
		try {
			Long s_summaryId = Long.valueOf(request.getParameter("summaryId"));
			GovdocSummaryVO summaryVO = new GovdocSummaryVO();
			summaryVO.setSummaryId(s_summaryId);
			GovdocContentHelper.fillSummaryBodyData(summaryVO);
			mv.addObject("summaryVO", summaryVO);
			mv.addObject("htmlISignCount", govdocSignetManager.getISignCount(s_summaryId));
			User user = AppContext.getCurrentUser();
			mv.addObject("newGovdocView", govdocOpenManager.getGovdocViewValue(user.getId(), user.getLoginAccount()));
			mv.addObject("currentPageName", "htmlBody");
		} catch(Exception e) {
			LOGGER.error("公文显示HTML正文出错", e);
		}
		return mv;
	}

	/**
	 * 获取显示HTML正文
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView transOffice(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("govdoc/summary/transOffice");
		try {
			Long summaryId = Long.parseLong(request.getParameter("summaryId"));
			CtpContentAll transOffice = GovdocContentHelper.getBodyContentByModuleId(summaryId);
			GovdocBodyVO bodyVo = new GovdocBodyVO();
			if (transOffice != null) {
				bodyVo.setCreateDate(transOffice.getCreateDate());
				bodyVo.setContentType(transOffice.getContentType());
				bodyVo.setBodyType(GovdocUtil.getBodyType(transOffice.getContentType()));
				bodyVo.setContent(transOffice.getContent());
				if(bodyVo.getContentType()!=null) {
					if(bodyVo.getContentType().intValue() != MainbodyType.HTML.getKey()) {
						bodyVo.setFileId(Long.parseLong(transOffice.getContent()));
					}
				}
			}
			mav.addObject("bodyVo", bodyVo);
			mav.addObject("currentPageName", "transOffice");
		} catch(Exception e) {
			LOGGER.error("公文显示HTML正文出错", e);
		}
		return mav;
	}
	
	/**
	 * 获取Pdf/Ofd显示的正文
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView transBody(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("govdoc/summary/transBody");
		try {
			Long summaryId = Long.parseLong(request.getParameter("summaryId"));
			Integer contentType = Integer.parseInt(request.getParameter("contentType"));
			CtpContentAll transBody = GovdocContentHelper.getOnlyTransBodyContentByModuleId(summaryId, contentType);
			GovdocBodyVO bodyVo = new GovdocBodyVO();
			if (transBody != null) {
				if(transBody.getContentType().intValue() == 45) {//PDF
					bodyVo.setPdfFileId(transBody == null ? "" : transBody.getContent());
					bodyVo.setPdfContent(transBody);
				} else if(transBody.getContentType().intValue() == 46) {//OFD
					bodyVo.setOfdFileId(transBody == null ? "" : transBody.getContent());
					bodyVo.setOfdContent(transBody);
				}
				bodyVo.setContentType(transBody.getContentType());
				bodyVo.setBodyType(GovdocUtil.getBodyType(transBody.getContentType()));
				bodyVo.setContent(transBody.getContent());
				if(bodyVo.getContentType()!=null) {
					if(bodyVo.getContentType().intValue() != MainbodyType.HTML.getKey()) {
						bodyVo.setFileId(Long.parseLong(transBody.getContent()));
					}
				}
			}
			mav.addObject("bodyVo", bodyVo);
			mav.addObject("currentPageName", "transBody");
		} catch(Exception e) {
			LOGGER.error("公文显示Pdf/Ofd正文出错", e);
		}
		return mav;
	}
	
    /**
     * 根据模板moduleid 查询公文单是否设置了正文
     * @param request
     * @param response
     * @return
     */
    public ModelAndView checkBodyExist(HttpServletRequest request, HttpServletResponse response){
    	PrintWriter out = null;
    	 try {
    		 out = response.getWriter();
    		 String moduleId = request.getParameter("moduleId");
    		 if(Strings.isNotBlank(moduleId)){
    			 CtpContentAll all=  GovdocContentHelper.getBodyContentByModuleId(Long.valueOf(moduleId));
    			 if(null != all && !"-1".equals(all.getContent())) {
    				 out.write("true");
    			 }else{
    				 out.write("false");
    			 }
    		 }
    	 } catch (Exception e) {
			LOGGER.error(e);
		}
    	return null;
    }
    
	/**
	 * 公文正文展现
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView bodyIframe(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("govdoc/summary/bodyIframe");
		try {
			Long moduleId = Long.parseLong(request.getParameter("moduleId"));
			CtpContentAll bodyContent = GovdocContentHelper.getBodyContentByModuleId(moduleId);
			GovdocBodyVO bodyVo = new GovdocBodyVO();
			if (bodyContent != null) {
				bodyVo.setContentType(bodyContent.getContentType());
				bodyVo.setBodyType(GovdocUtil.getBodyType(bodyContent.getContentType()));
				bodyVo.setContent(bodyContent.getContent());
				if(bodyVo.getContentType()!=null) {
					if(bodyVo.getContentType().intValue() != MainbodyType.HTML.getKey()) {
						bodyVo.setFileId(Long.parseLong(bodyContent.getContent()));
					}
				}
				bodyVo.setModuleId(bodyContent.getModuleId());
			}
			mav.addObject("bodyVo", bodyVo);
			mav.addObject("currentPageName", "bodyIframe");
		} catch(Exception e) {
			LOGGER.error("公文显示Pdf/Ofd正文出错", e);
		}
		return mav;
	}
	
	/**
     * 移动PDF正文圈阅设置
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView pdfContentSet(HttpServletRequest request,HttpServletResponse response)throws Exception{
		ModelAndView mav = new ModelAndView("govdoc/pdfContentSet");
		//查询打印设置
		List<ConfigItem> items = configManager.listAllConfigByCategory("pdfContentSet", 1L);
		for (ConfigItem item : items) {
			if("serialNo".equals(item.getConfigItem())){
				mav.addObject("serialNo", item.getConfigValue());
			}else if("authUsers".equals(item.getConfigItem())){
				String authUsers = item.getExtConfigValue();
				mav.addObject("authUsers", authUsers);
				mav.addObject("authUserNames", item.getConfigValue());
			}
		}
		return mav;
	}
	/*************************** 33333 公文查看   end ***************************/


	/*************************** 44444 公文处理 start ***************************/
	/**
	 * 表单公文-处理-提交
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView finishWorkItem(HttpServletRequest request, HttpServletResponse response) throws Exception {
		GovdocDealVO dealVo = new  GovdocDealVO();
		
		// 前段参数完整性校验
		if (!GovdocNewHelper.checkHttpParamValid(dealVo, request)) {
			GovdocUtil.newCollAlert(response, StringEscapeUtils.escapeJavaScript(dealVo.getErrorMsg()) + "');");
			return null;
		}
		
		dealVo.setCurrentDate(DateUtil.currentDate());
		dealVo.setCurrentUser(AppContext.getCurrentUser());
		GovdocNewHelper.fillFinishParam(dealVo, request);
		
		try {
			//公文处理提交时封装对象到vo中
			if(!govdocPubManager.fillFinishObj(dealVo)) {
				WFComponentUtil.webAlertAndClose(response, dealVo.getErrorMsg());
				return null;
			}
			//校验锁
			boolean isLock = govdocLockManager.canGetLock(dealVo.getAffairId());
			if (!isLock) {
				return null;
			}
			//封装文号相关参数
			GovdocMarkHelper.fillSaveMarkParameter(dealVo, null);
			//封装节点权限
			govdocManager.fillFinishPermisssion(dealVo);
			//公文处理提交
			govdocManager.transFinishWorkItem(dealVo);
		} catch(Exception e) {
			LOGGER.error("公文提交出错", e);
		} finally {
			govdocLockManager.unlockAll(dealVo.getSummary(), dealVo.getAffairId(), dealVo.getCurrentUser().getId());
		}
		return null;

	}

	/**
	 * 表单公文-处理-暂存待办
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView doZCDB(HttpServletRequest request, HttpServletResponse response) throws Exception {
		GovdocDealVO dealVo = new  GovdocDealVO();
		dealVo.setCurrentDate(DateUtil.currentDate());
		dealVo.setCurrentUser(AppContext.getCurrentUser());
		GovdocNewHelper.fillZcdbParam(dealVo, request);
		
		try {
			//公文处理提交时封装对象到vo中
			if(!govdocPubManager.fillZcdbObj(dealVo)) {
				WFComponentUtil.webAlertAndClose(response, dealVo.getErrorMsg());
				return null;
			}
			
			if (dealVo.isConvertPdf()) {//如果只是转pdf
				govdocManager.transDoZcdbByPdf(dealVo);
			} else {
				govdocManager.transDoZcdb(dealVo);
			}
			
			//签收稍后分办、签收分办
			if (Strings.isNotBlank(dealVo.getDistributeType())) {
				//签收分办合并时，选择是/直接分办，将签收意见放入session
				if("yes".equals(dealVo.getDistributeType()) || "normal".equals(dealVo.getDistributeType())) {
					Comment comment = govdocCommentManager.getCommnetFromRequest(OperationType.finish, dealVo.getAffair().getMemberId(), dealVo.getAffair().getObjectId());
					//添加 代录判断
					String pishiFlag = govdocPishiManager.checkLeaderPishi(AppContext.currentUserId(), dealVo.getAffair().getMemberId());
					if("pishi".equals(pishiFlag) && "2".equals(dealVo.getLeaderPishiFlag())
							&& comment.getContent()!=null && comment.getContent().contains("代录)")){
						comment.setContent(comment.getContent()+"(由"+AppContext.getCurrentUser().getName()+"代录)");
					}
					AppContext.putSessionContext("comment", comment.toCommentAll());
				}
				
				//公文签收后的交换业务处理
				govdocExchangeManager.exchangeSign(dealVo.getSummary(), dealVo.getAffair(), dealVo.getCurrentUser().getId());
			}
			govdocManager.transJointlyIssued(dealVo);
		} catch(Exception e) {
			LOGGER.error("公文暂存待办出错", e);
		} finally {
			govdocLockManager.unlockAll(dealVo.getSummary(), dealVo.getAffairId(), dealVo.getCurrentUser().getId());
		}

		return null;
	}

	/**
	 * 表单公文-处理-存为草稿
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView doDraft(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			long summaryId = Long.parseLong(request.getParameter("summaryId"));
			long affairId = Long.valueOf(request.getParameter("affairId"));
			// 先保存意见
			govdocCommentManager.saveOpinionDraft(affairId, summaryId);
		} catch(Exception e) {
			LOGGER.error("公文存为草稿出错", e);
		}
		return null;
	}
	/**
     * 移交
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView govdocTansfer(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	ModelAndView modelAndView = new ModelAndView();
    	Map<String,String> params = new HashMap<String,String>();
    	String affairId = request.getParameter("affairId");
    	params.put("affairId", affairId);
    	params.put("transferMemberId", request.getParameter("transferMemberId"));
    	boolean isLock = false;
    	try {
        	isLock = govdocLockManager.canGetLock(Long.valueOf(affairId));
        	if (!isLock) {
        		LOGGER.error(AppContext.currentAccountName()+"不能获取到map缓存锁，不能执行操作finishWorkItem,affairId"+affairId);
        		return null;
        	}
        	String message = this.govdocManager.transfer(params);
        	modelAndView.addObject("message",message);
        	
		} catch(Exception e) {
			LOGGER.error("公文移交出错", e);
		} finally {
			if(isLock) {
				govdocLockManager.unlock(Long.valueOf(affairId));
			}
		}
    	return null; //没有view，暂时返回空，不然404
    }

	
	/**
	 * 表单公文-处理-撤销流程
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView repeal(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/html;charset=UTF-8");
		
		GovdocRepealVO repealVO = GovdocNewHelper.fillRepealVO(request);

		PrintWriter out = null;
		try {
			out = response.getWriter();			
			repealVO.setCurrentUser(AppContext.getCurrentUser());
			govdocManager.transRepal(repealVO);
			out.println("<script>");
			out.println("window.parent.$('#summary').attr('src','');"); // 刷新iframe的父页面
			out.println("window.parent.$('.slideDownBtn').trigger('click');");
			out.println("window.parent.$('#listPending').ajaxgridLoad();");
			out.println("</script>");
		} catch(Exception e) {
			LOGGER.error("公文撤销出错", e);
		} finally {
			govdocLockManager.unlock(Long.valueOf(repealVO.getAffairIdStr()));
			if(out != null) {
				out.close();
			}	
		}		
		return null;
	}

	/**
	 * 表单公文-处理-终止
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView stepStop(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/html;charset=UTF-8");
		
    	GovdocDealVO dealVo = new GovdocDealVO();
        dealVo.setCurrentDate(DateUtil.currentDate());
        dealVo.setCurrentUser(AppContext.getCurrentUser());
        
        PrintWriter out = null;
        boolean isLock = false;
        try {
        	 out = response.getWriter();
        	 
        	 GovdocNewHelper.fillStepStopParam(dealVo, request);
        	 
        	 isLock = govdocLockManager.canGetLock(dealVo.getAffairId());
        	 if (!isLock) {
        		 LOGGER.error(AppContext.currentAccountName() + "不能获取到map缓存锁，不能执行操作stepStop, affairId:" + dealVo.getAffairId());
        		 return null;
        	 }
        	 
        	 if(!govdocPubManager.fillStepStopObj(dealVo)) {
        		 WFComponentUtil.webAlertAndClose(response, dealVo.getErrorMsg());
        		 return null;
        	 }
        	 
        	if (dealVo.getAffair().getSubApp() == ApplicationSubCategoryEnum.edoc_jiaohuan.getKey()) {
				govdocExchangeManager.exchangeSign(dealVo.getSummary(), dealVo.getAffair(), dealVo.getCurrentUser().getId());
			}
        	 
			if(!govdocManager.transStepStop(dealVo)) {
				WFComponentUtil.webAlertAndClose(response, dealVo.getErrorMsg());
				return null;
			}
			out.println("<script>");
		    out.println("window.parent.$('#summary').attr('src','');"); //刷新iframe的父页面
		    out.println("window.parent.$('.slideDownBtn').trigger('click');");
		    out.println("window.parent.$('#listPending').ajaxgridLoad();");
		    out.println("</script>");
        } catch(Exception e) {
        	LOGGER.error("公文终止出错", e);
        } finally{
			if (isLock) {
				govdocLockManager.unlockAll(dealVo.getSummary(), dealVo.getAffairId(), dealVo.getCurrentUser().getId());
			}
			if(out != null){
				out.close();
			}
        }                               
        return null;
	}

	/**
	 * 表单公文-处理-回退
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView stepBack(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = null;
		
		boolean isLock = false;
		GovdocDealVO dealVo = new GovdocDealVO();
		try {
			out = response.getWriter();
	        dealVo.setCurrentUser(AppContext.getCurrentUser());
	        dealVo = GovdocNewHelper.fillStepBackParam(request, dealVo);
	        isLock = govdocLockManager.canGetLock(dealVo.getAffairId());
			if (!isLock) {
				LOGGER.error(AppContext.currentAccountName() + "不能获取到map缓存锁，不能执行操作stepBack,affairId:" + dealVo.getAffairId());
				return null;
			}
			if(!govdocManager.transStepBack(dealVo)) {
				WFComponentUtil.webAlertAndClose(response, dealVo.getErrorMsg());
				return null;
			}
			out.println("<script>");
			out.println("window.parent.$('#summary').attr('src','');"); // 刷新iframe的父页面
			out.println("window.parent.$('.slideDownBtn').trigger('click');");
			out.println("window.parent.$('#listPending').ajaxgridLoad();");
			out.println("</script>");
		} catch(Exception e) {
			LOGGER.error("公文回退出错", e);
		} finally {
			if (isLock) {
				govdocLockManager.unlockAll(dealVo.getSummary(), dealVo.getAffairId(), dealVo.getCurrentUser().getId());
			}
			if(out != null) {
				out.close();
			}
		}						
		return null;
	}

	/**
	 * 表单公文-处理-指定回退
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView appointStepBack(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = null;
		
        boolean isLock = false;
		GovdocDealVO dealVo = new GovdocDealVO();
        try {
        	out = response.getWriter();
	        dealVo.setCurrentUser(AppContext.getCurrentUser());
	        dealVo = GovdocNewHelper.fillAppointStepBackParam(request, dealVo);
	        isLock = govdocLockManager.canGetLock(dealVo.getAffairId());
			if (!isLock) {
				LOGGER.error(AppContext.currentAccountName() + "不能获取到map缓存锁，不能执行操作stepBack,affairId:" + dealVo.getAffairId());
				return null;
			}
			if (!govdocPubManager.fillAppointStepBackObj(dealVo)) {
				return null;
			}
			govdocManager.transAppointStepBack(dealVo);
			out.println("<script>");
	        out.println("window.parent.$('#summary').attr('src','');"); //刷新iframe的父页面
	        out.println("window.parent.$('.slideDownBtn').trigger('click');");
	        out.println("window.parent.$('#listPending').ajaxgridLoad();");
	        out.println("</script>");
        } catch(Exception e) {
        	LOGGER.error("公文指定回退出错", e);
        } finally {
        	if (isLock) {
				govdocLockManager.unlockAll(dealVo.getSummary(), dealVo.getAffairId(), dealVo.getCurrentUser().getId());
			}
        	if(out != null) {
        		out.close();
        	}
        }       
        return null;
	}
	/*************************** 44444 公文处理   end ***************************/
	
	
	/*************************** 66666 公文交换 start ***************************/
	private void sendDistribute(HttpServletRequest request, HttpServletResponse response, GovdocNewVO newVo) {
		if (newVo.getDistributeAffairId() != null) {// 分办,需要处理流程
			try {
				AppContext.getRawRequest().setAttribute("fromDistribute", true);
				
				CtpCommentAll commentAll = AppContext.getSessionContext("comment") == null ? null : (CtpCommentAll) AppContext.getSessionContext("comment");
				CtpAffair signAffair = affairManager.get(newVo.getDistributeAffairId());
				EdocSummary signSummary = govdocSummaryManager.getSummaryById(signAffair.getObjectId());
				
				//把签收流程的分办节点处理掉
				GovdocDealVO dealVo = new GovdocDealVO();
				dealVo.setAffair(signAffair);
				dealVo.setSummary(signSummary);
				dealVo.setCurrentUser(newVo.getCurrentUser());
				if (commentAll != null) {
					dealVo.setComment(new Comment(commentAll));
				}
				govdocManager.transFinishWorkItemByDistrubite(dealVo);
				
				// 同步保存分办的批示编号
				govdocPishiManager.saveLeaderPishiNo(dealVo);
				
				//分办后交换状态变更
				govdocExchangeManager.exchangeDistribute(signSummary, newVo.getSenderAffair(), signAffair, newVo.getCurrentUser().getId(), newVo.getSummary().getId());
			} catch(Exception e) {
				LOGGER.error("签收流程提交分办出错", e);
			} finally {
				AppContext.removeSessionArrribute("comment");
			}
		} else {
			try {
				CtpAffair affair = affairManager.get(newVo.getCurrentAffairId());
				if (affair != null) {
					GovdocExchangeDetail detail = govdocExchangeManager.findDetailByRecSummaryId(affair.getObjectId());
					if (detail != null && detail.getStatus() == GovdocEnum.ExchangeDetailStatus.draftFenBan.getKey()) {
						EdocSummary distributeSummary = govdocSummaryManager.getSummaryById(detail.getSummaryId());
						govdocExchangeManager.exchangeDistribute(distributeSummary, affair, null, newVo.getCurrentUser().getId(), newVo.getSummary().getId());
					} 
				}
			} catch(Exception e) {
				LOGGER.error("签收流程提交分办出错", e);
			} finally {
				AppContext.removeSessionArrribute("comment");
			}
		}
	}
	/*************************** 66666 公文交换   end ***************************/
	
	
	/*************************** 77777 公文其它 start ***************************/    
    /**
     * 收文转收文
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView toTurnRecEdoc(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView modelAndView = new ModelAndView("govdoc/turnRecEdoc");
		modelAndView.addObject("summaryId", request.getParameter("summaryId"));
    	return modelAndView;
    }
    public ModelAndView doTurnRecEdoc(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	try {
	    	Long affairId = Long.valueOf(request.getParameter("affairId"));
	    	String unitId = request.getParameter("unitId");
	    	String opinion = request.getParameter("opinion");
	    	CtpAffair affair = affairManager.get(affairId);
	    	EdocSummary edocSummary = govdocSummaryManager.getSummaryById(affair.getObjectId());
	    	Map<String, Object> extendParam = new HashMap<String, Object>();
	    	extendParam.put("exchangeType", GovdocExchangeMain.EXCHANGE_TYPE_ZHUANSHOUWEN);
	    	extendParam.put("opinion", opinion);
	    	extendParam.put("sendToId", unitId);
	    	govdocExchangeManager.exchangeSend(edocSummary, AppContext.currentUserId(), affairId, extendParam);
    	} catch(Exception e) {
    		LOGGER.error("收文转收文出错", e);
    	}
    	return null;
    }
    
    /**
     * 转发文弹出默认设置页面
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView forwordOption(HttpServletRequest request,HttpServletResponse response) throws Exception {
    	ModelAndView mav = new ModelAndView("govdoc/forwordOption");
	    if(govdocOpenManager.isEdocFawenZidongBanjie()) {
	    	mav.addObject("zfwzidongbanjie","yes");
	    }
    	return mav;
    }
    
    /**
	 * 判断公文在当前收文节点是否有转发文操作
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	public String isZhuanFawen (HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			String category = "";
			String nodepolicy = "";
			String orgAccountId = request.getParameter("orgAccountId");		
			String govdocType = request.getParameter("govdocType");
			String affairId = request.getParameter("affairId");
			CtpAffair affair = affairManager.get(Long.valueOf(affairId));
	        if(!govdocOpenManager.isEdocZhuanfawenTactics()){//是否启用-收文节点转发文策略
	        	response.getWriter().print(JSONUtil.toJSONString(1));
				return null;
	        }
			if("2".equals(govdocType)){
				category = EnumNameEnum.edoc_new_rec_permission_policy.name();
			}
			nodepolicy = affair.getNodePolicy();
			PermissionManager permissionManager=(PermissionManager)AppContext.getBean("permissionManager");
			PermissionVO permissionVo = permissionManager.getPermissionVO(category, nodepolicy, Long.valueOf(orgAccountId));
			String advanced = permissionVo.getAdvancedOperation();//节点已选中的高级操作
			String common = permissionVo.getCommonOperation();//节点已选中的常用操作 
			if(Strings.isNotBlank(advanced)){
				String[] advancedOperation = advanced.split(",");
				for (int i=0;i<advancedOperation.length;i++) {				
					if("Zhuanfawen".equals(advancedOperation[i].trim())){					
						response.getWriter().print(JSONUtil.toJSONString(1));
						return null;
					}
				}
			}
			if(Strings.isNotBlank(common)){
				String[] commonOperation = common.split(",");
				for (int i=0;i<commonOperation.length;i++) {
					if("Zhuanfawen".equals(commonOperation[i].trim())){
						response.getWriter().print(JSONUtil.toJSONString(1));
						return null;
					}
				}
			}
			response.getWriter().print(JSONUtil.toJSONString(0));
		} catch(Exception e) {
			LOGGER.error("判断公文在当前收文节点是否有转发文操作", e);
		}
		return null;
	}
	/**
	 * 保存 转发文关联
	 * @param request
	 * @param info
	 * @throws NumberFormatException
	 * @throws BusinessException
	 */
	private void zhuanFaWenRelation(HttpServletRequest request,GovdocNewVO info) throws NumberFormatException, BusinessException{
		//新发文关联收文  和 收文关联新发文
        String forwardAffairId = info.getForwardAffairId();
        String govdocRelation1 = info.getGovdocRelation1();
        String govdocRelation2 = info.getGovdocRelation2();
        if (Strings.isNotBlank(forwardAffairId) && Strings.isNotBlank(govdocRelation1) || Strings.isNotBlank(govdocRelation2)) {
        	CtpAffair affair = affairManager.get(Long.valueOf(forwardAffairId));
        	//新发文关联收文
        	if ("true".equals(govdocRelation1)) {
        		//主公文是发文id，被关联是收文 TODO 保存信息不正确
        		GovdocNewHelper.createGovdocRelationMain(info.getSummary().getId(), affair.getSubject(), affair.getObjectId(), GovdocExchangeMain.EXCHANGE_TYPE_ZHUANFAWEN);
        	}
        	//收文关联新发文
        	if ("true".equals(govdocRelation2)) {
        		GovdocNewHelper.createGovdocRelationMain(affair.getObjectId(), info.getSummary().getSubject(), info.getSummary().getId(), GovdocExchangeMain.EXCHANGE_TYPE_ZHUANFAWEN);
        	}
		}
	}
	
	/**
	 * 
	* @Title: toTurnSendEdocInfo
	* @Description: 收文转发文所有列表页面
	 */
	public ModelAndView toTurnSendEdocInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	ModelAndView modelAndView = new ModelAndView("govdoc/turnSendEdocInfo");
    	modelAndView.addObject("referenceId", request.getParameter("referenceId"));
    	modelAndView.addObject("type", request.getParameter("type"));
    	return modelAndView;
    }	
    public ModelAndView getTurnRecEdocInfo2(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView modelAndView = new ModelAndView("govdoc/turnRecEdocInfo2");
		String detailId = request.getParameter("detailId");
		GovdocExchangeDetail detail = govdocExchangeManager.getExchangeDetailById(Long.valueOf(detailId));
		GovdocExchangeMain main = govdocExchangeManager.getGovdocExchangeMainById(detail.getMainId());
		modelAndView.addObject("opinion", detail.getOpinion());
		modelAndView.addObject("createDate", DateUtil.getDate(main.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
		modelAndView.addObject("createUnit", Functions.showOrgAccountNameByMemberid(main.getStartUserId()));
		modelAndView.addObject("summaryId", main.getSummaryId());
		boolean chuantouchakan2 = false;
		PluginDefinition definition = SystemEnvironment.getPluginDefinition("govdoc");
		if (definition != null && "true".equals(definition.getPluginProperty("govdoc.chuantouchakan2"))) {
			chuantouchakan2 = true;
		}
		modelAndView.addObject("chuantouchakan2",chuantouchakan2);
		return modelAndView;
	}
    
    /**
     * 公文管理平台
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView govdocManagementPlatform(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	ModelAndView mv = new ModelAndView("common/govdocassets/govdocManagementPlatform");
    	Map<String,Integer> managerCountNumber = new HashMap<String,Integer>();
        Long currAccountId = AppContext.currentAccountId();
        managerCountNumber = govdocManager.getManagermentPlatformCount(currAccountId);
        //单组织预制文单
        if (ProductEditionEnum.getCurrentProductEditionEnum().equals(ProductEditionEnum.government)) {
            formApi4Cap3.singleSetShelf(); 
		}
        mv.addObject("managerCountNumber",managerCountNumber);
        
    	return mv;
    }
    
    /**
     * 缓存数据(用于会议纪要转公文等功能实现)
     * 由于V3xShareMap缓存方式，get后数据就被清空，所以不用判断是否存在，直接存储
     */
    public ModelAndView cashTransData(HttpServletRequest request,HttpServletResponse response) throws Exception {
    	String cashId = String.valueOf(UUIDLong.longUUID());
    	
    	Map<String, String> paramMap = new HashMap<String, String>();
    	
    	Enumeration<String> names = request.getParameterNames();
    	while(names.hasMoreElements()){
    	    String name = names.nextElement();
    	    paramMap.put(name, request.getParameter(name));
    	}
		V3xShareMap.put(cashId, paramMap);
		
		response.getWriter().write(cashId);
    	return null;
    }
	
	 /**
	  * 编辑意见
	  * @param request
	  * @param response
	  * @return
	  * @throws Exception
	  */
	 public ModelAndView editOpinion(HttpServletRequest request, HttpServletResponse response) throws Exception {
			response.setContentType("text/html;charset=UTF-8");
			
			GovdocCommentVO commentVO = new GovdocCommentVO();
			commentVO.setRequest(request);
			commentVO.setCurrentDate(DateUtil.currentDate());
			commentVO.setCurrentUser(AppContext.getCurrentUser());
			commentVO.setAttachmentEditHelper(new AttachmentEditHelper(request));
			GovdocNewHelper.fillEditCommentParam(commentVO, request);
			govdocCommentManager.editComment(commentVO);
			 PrintWriter out = response.getWriter();
		     out.println("<script>");
		     out.println("alert('" + commentVO.getOutInfo() + "')");
		     out.println("</script>");
		     out.flush();
		     return super.refreshWindow("parent");
		 }
	/*************************** 77777 公文其它   end ***************************/
	
	
	/*************************** 88888 打开Dialog start ***************************/
	/**
	 * 列表打开详细界面打开跟踪设置页面
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView openTrackDetail(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("govdoc/track/trackDetail");
		try {
			String objectId = request.getParameter("objectId");
			String affairId = request.getParameter("affairId");

			GovdocTrackVO trackVo = govdocTrackManager.getTrackInfoBySummaryId(Long.valueOf(objectId), Long.valueOf(affairId));
			mav.addObject("objectId", trackVo.getSummaryId());
			mav.addObject("affairId", trackVo.getAffairId());
			mav.addObject("trackType", trackVo.getTrackType());
			mav.addObject("state", trackVo.getState());
			mav.addObject("startMemberId", trackVo.getStartMemberId());
			if (Strings.isNotBlank(trackVo.getZdgzrStr())) {
				mav.addObject("zdgzrStr", trackVo.getZdgzrStr());
			}
		} catch (Exception e) {
			LOGGER.error("列表打开详细界面打开跟踪设置页面出错", e);
		}
		return mav;
	}
	/**
	 * 显示取回确认页面
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
    public ModelAndView takeBackDialog(HttpServletRequest request, HttpServletResponse response) throws Exception {
         return new ModelAndView("govdoc/dialog/takeBackDialog");
    }
	/**
	 * 公文回退dialog打开
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView stepBackDialog(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("govdoc/dialog/stepBackDialog");
		try {
			String _affairId = request.getParameter("affairId");
	    	String _objectId = request.getParameter("objectId");
	    	if(Strings.isNotBlank(_affairId)){
	    		CtpAffair ctpAffair = affairManager.get(Long.valueOf(_affairId));
	    		if(null != ctpAffair && null !=ctpAffair.getTempleteId()){
	    			CtpTemplate ctpTemplate = templateManager.getCtpTemplate(ctpAffair.getTempleteId());
	    			if(ctpTemplate!=null){
	    			   mav.addObject("template", ctpTemplate.getCanTrackWorkflow());
	    			}
	    		}
	    	}
	    	mav.addObject("fromBatch",request.getParameter("fromBatch"));
	    	mav.addObject("affairId",_affairId);
	    	mav.addObject("objectId",_objectId);
	    	mav.addObject("openFrom",request.getParameter("openFrom"));
	    	mav.addObject("nodeattitudeList", govdocCommentManager.getAttitudeList(request.getParameter("permissionId")));
	    	return mav;
		} catch(Exception e) {
			LOGGER.error("公文回退dialog打开出错", e);
		}
		return mav;
	}
	
	/***
	 * 指定回退
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView stepBackToDialog(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("govdoc/dialog/stepBackToDialog");
		try {
			String _affairId = request.getParameter("affairId");
			String _objectId = request.getParameter("objectId");
			if(Strings.isNotBlank(_affairId)) {
				CtpAffair ctpAffair = affairManager.get(Long.valueOf(_affairId));
				if(null != ctpAffair && null !=ctpAffair.getTempleteId()){
					CtpTemplate ctpTemplate = templateManager.getCtpTemplate(ctpAffair.getTempleteId());
					if(ctpTemplate!=null){
						mav.addObject("template", ctpTemplate.getCanTrackWorkflow());
					}
				}
			}
			mav.addObject("affairId",_affairId);
			mav.addObject("objectId",_objectId);
			mav.addObject("nodeattitudeList", govdocCommentManager.getAttitudeList(request.getParameter("permissionId")));
			return mav;
		} catch(Exception e) {
			LOGGER.error("公文指定回退dialog打开出错", e);
		}
		return mav;
	}
	/***
	 * 终止
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView stepStopDialog(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("govdoc/dialog/stepStopDialog");
		try {
			String _affairId = request.getParameter("affairId");
			String _objectId = request.getParameter("objectId");
			if(Strings.isNotBlank(_affairId)) {
				CtpAffair ctpAffair = affairManager.get(Long.valueOf(_affairId));
				if(null != ctpAffair && null !=ctpAffair.getTempleteId()){
					CtpTemplate ctpTemplate = templateManager.getCtpTemplate(ctpAffair.getTempleteId());
					if(ctpTemplate!=null){
						mav.addObject("template", ctpTemplate.getCanTrackWorkflow());
					}
				}
			}
			mav.addObject("affairId",_affairId);
			mav.addObject("objectId",_objectId);
			mav.addObject("nodeattitudeList", govdocCommentManager.getAttitudeList(request.getParameter("permissionId")));
		} catch(Exception e) {
			LOGGER.error("公文终止dialog打开出错", e);
		}
		return mav;
	}
	/**
	 * 处理公文，选择了不同意弹出框
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView disagreeDealDialog(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return new ModelAndView("govdoc/dialog/disagreeDeal");
	}
	/**
	 * 是否保留最后一条意见弹出框
	 * @param request
	 * @param response
	 * @return
	 */
    public ModelAndView opinionSetDialog(HttpServletRequest request, HttpServletResponse response){
    	ModelAndView mav = new ModelAndView("govdoc/dialog/opinion_set_dialog");
    	String summaryId = request.getParameter("summaryId");
    	String affairId = request.getParameter("affairId");
    	String policy = request.getParameter("policy");
    	String opinionType = request.getParameter("opinionType");
    	mav.addObject("summaryId",summaryId);
    	mav.addObject("affairId",affairId);
    	mav.addObject("policy",policy);
    	mav.addObject("opinionType",opinionType);
    	return mav;
    }
    /**
	 * 选择公文节点权限
	 * @param request
	 * @param response
	 * @return
	 */
    public ModelAndView govdocPermission(HttpServletRequest request, HttpServletResponse response){
    	ModelAndView mav = new ModelAndView("govdoc/dialog/select_govdoc_permission");
    	return mav;
    }
    /**
     * <p>Title: 公文报表结果页面</p>
     * <p>Company: seeyon.com</p>
     * <p>author : fucz</p>
     * <p>since V5 7.1 2019</p>
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView govdocReportIndex(HttpServletRequest request,HttpServletResponse response)throws Exception{
		ModelAndView mav = new ModelAndView("/govdoc/govdocReportIndex");
		Map<String,Object> params = Maps.newHashMap();
		params.put("category", ApplicationCategoryEnum.edoc.name());
//		params.put("designType", ReqUtil.getString(request, "designType"));
		
		List<String> designTypes = new ArrayList<String>();
		if("query".equals(ReqUtil.getString(request, "type","query"))){
			designTypes.add(DesignType.QUERY.name());
		}else{
			designTypes.add(DesignType.STATISTICS.name());
			designTypes.add(DesignType.MULTSTATS.name());
			designTypes.add(DesignType.SCHEDULEDSTATS.name());
		}
		params.put("designTypes", designTypes);
		List<Map<String,Object>> reports = reportApi.findDesign(params);
		mav.addObject("reports",reports);
        return mav;
	}
    
    public ModelAndView showEditorDialog(HttpServletRequest request, HttpServletResponse response){
    	ModelAndView view = new ModelAndView("govdoc/summary/showEditorDialog");
    	Long summaryId = Long.valueOf(request.getParameter("summaryId"));
    	CtpContentAll transOffice = GovdocContentHelper.getBodyContentByModuleId(summaryId);
		GovdocBodyVO bodyVo = new GovdocBodyVO();
		if (transOffice != null) {
			bodyVo.setCreateDate(transOffice.getCreateDate());
			bodyVo.setContentType(transOffice.getContentType());
			bodyVo.setBodyType(GovdocUtil.getBodyType(transOffice.getContentType()));
			bodyVo.setContent(transOffice.getContent());
			if(bodyVo.getContentType()!=null) {
				if(bodyVo.getContentType().intValue() != MainbodyType.HTML.getKey()) {
					bodyVo.setFileId(Long.parseLong(transOffice.getContent()));
				}
			}
		}
		view.addObject("bodyVo", bodyVo);
		view.addObject("currentPageName", "showEditorDialog");
    	return view;
    }
    /*************************** 88888 打开Dialog   end ***************************/

    
    /*************************** 99999 Spring注入，请将业务写在上面 start ******************************/
    
	public void setGovdocManager(GovdocManager govdocManager) {
		this.govdocManager = govdocManager;
	}
	public void setConfigManager(ConfigManager configManager) {
		this.configManager = configManager;
	}
	public void setGovdocSummaryManager(GovdocSummaryManager govdocSummaryManager) {
		this.govdocSummaryManager = govdocSummaryManager;
	}
	public void setGovdocCommentManager(GovdocCommentManager govdocCommentManager) {
		this.govdocCommentManager = govdocCommentManager;
	}
	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	public void setGovdocTrackManager(GovdocTrackManager govdocTrackManager) {
		this.govdocTrackManager = govdocTrackManager;
	}
	public void setGovdocLockManager(GovdocLockManager govdocLockManager) {
		this.govdocLockManager = govdocLockManager;
	}
	public void setGovdocPubManager(GovdocPubManager govdocPubManager) {
		this.govdocPubManager = govdocPubManager;
	}
	public void setGovdocSignetManager(GovdocSignetManager govdocSignetManager) {
		this.govdocSignetManager = govdocSignetManager;
	}
	public void setGovdocContentManager(GovdocContentManager govdocContentManager) {
		this.govdocContentManager = govdocContentManager;
	}
	public void setGovdocExchangeManager(GovdocExchangeManager govdocExchangeManager) {
		this.govdocExchangeManager = govdocExchangeManager;
	}
	public void setGovdocOpenManager(GovdocOpenManager govdocOpenManager) {
		this.govdocOpenManager = govdocOpenManager;
	}
	public void setGovdocContinueManager(GovdocContinueManager govdocContinueManager) {
		this.govdocContinueManager = govdocContinueManager;
	}
	public void setGovdocPishiManager(GovdocPishiManager govdocPishiManager) {
		this.govdocPishiManager = govdocPishiManager;
	}
	public void setGovdocDocTemplateManager(GovdocDocTemplateManagerImpl govdocDocTemplateManager) {
		this.govdocDocTemplateManager = govdocDocTemplateManager;
	}
	public void setReportApi(ReportApi reportApi) {
		this.reportApi = reportApi;
	}
	public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
        this.formApi4Cap3 = formApi4Cap3;
    }
	/*************************** 99999 Spring注入，请将业务写在上面   end ******************************/
	
}
