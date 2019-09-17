package com.seeyon.apps.govdoc.helper;

import static java.io.File.separator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import com.seeyon.apps.govdoc.constant.GovdocEnum;
import com.seeyon.apps.govdoc.constant.GovdocEnum.GovdocNewParamEnum;
import com.seeyon.apps.govdoc.constant.GovdocQuickSendWorkFlow;
import com.seeyon.apps.govdoc.mark.helper.GovdocMarkHelper;
import com.seeyon.apps.govdoc.po.GovdocExchangeMain;
import com.seeyon.apps.govdoc.util.GovdocParamUtil;
import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.apps.govdoc.vo.GovdocBaseVO;
import com.seeyon.apps.govdoc.vo.GovdocBodyVO;
import com.seeyon.apps.govdoc.vo.GovdocCommentVO;
import com.seeyon.apps.govdoc.vo.GovdocComponentVO;
import com.seeyon.apps.govdoc.vo.GovdocDealVO;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.apps.govdoc.vo.GovdocRepealVO;
import com.seeyon.apps.govdoc.vo.GovdocSummaryVO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.PropertiesConfiger;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.affair.enums.AffairExtPropEnums;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.config.IConfigPublicKey;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.constants.CustomizeConstants;
import com.seeyon.ctp.common.content.ContentConfig;
import com.seeyon.ctp.common.content.ContentViewRet;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.mainbody.CtpContentAllBean;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.ctpenumnew.Constants;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.encrypt.CoderFactory;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.Util;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.permission.bo.NodePolicy;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.po.template.CtpTemplateCategory;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.template.enums.TemplateEnum;
import com.seeyon.ctp.common.template.util.TemplateUtil;
import com.seeyon.ctp.common.trace.enums.WorkflowTraceEnums;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormDataMasterBean;
import com.seeyon.ctp.form.bean.FormFieldBean;
import com.seeyon.ctp.form.po.GovdocFormDefault;
import com.seeyon.ctp.form.util.Enums.FormStateEnum;
import com.seeyon.ctp.form.util.Enums.FormType;
import com.seeyon.ctp.form.util.Enums.FormUseFlagEnum;
import com.seeyon.ctp.form.util.permission.factory.PermissionFatory;
import com.seeyon.ctp.form.util.permission.util.PermissionUtil;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgLevel;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgPost;
import com.seeyon.ctp.organization.bo.V3xOrgTeam;
import com.seeyon.ctp.util.FileUtil;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.engine.enums.BPMSeeyonPolicySetting;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.ocip.exchange.exceptions.BussinessException;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.util.EdocUtil;

import net.joinwork.bpm.definition.BPMActor;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.definition.BPMTransition;
import www.seeyon.com.utils.UUIDUtil;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class GovdocNewHelper extends GovdocHelper {

	private static final Log LOGGER = CtpLogFactory.getLog(GovdocNewHelper.class);

	/**
	 * 公文新建页面-组装Vo对象
	 * @param request
	 * @param newVo
	 * @throws BusinessException
	 */
	public static void fillNewGovdocVoByParams(HttpServletRequest request, GovdocNewVO newVo) throws BusinessException {
		Long templateId = GovdocParamUtil.getLong(request, GovdocNewParamEnum.templateId.name(), null);// 非空表示调用模板
		String from = GovdocParamUtil.getString(request, GovdocNewParamEnum.from.name());
		String subApp = GovdocParamUtil.getString(request, GovdocNewParamEnum.sub_app.name(), "-99");
		Long affairId = GovdocParamUtil.getLong(request, GovdocNewParamEnum.affairId.name());// 待发事项AffairId
		Long summaryId = GovdocParamUtil.getLong(request, GovdocNewParamEnum.summaryId.name(), UUIDUtil.getUUIDLong());// 本次编辑的公文ID
		// 来自签收流程-分办
		Long signSummaryId = GovdocParamUtil.getLong(request, GovdocNewParamEnum.signSummaryId.name(), null);// 签收流程公文ID
		String distributeType = GovdocParamUtil.getString(request, GovdocNewParamEnum.distributeType.name());// 本次操作为签收并分办
		Long distributeAffairId = GovdocParamUtil.getLong(request, GovdocNewParamEnum.distributeAffairId.name(), null);// 签收流程分办节点AffairId
		Long distributeContentTemplateId = GovdocParamUtil.getLong(request, GovdocNewParamEnum.distributeContentTemplateId.name(), null);
		Long distributeContentDataId = GovdocParamUtil.getLong(request, GovdocNewParamEnum.distributeContentDataId.name(), null);
		// 转发
		String meetingSummaryId = request.getParameter("meetingSummaryId");// 会议纪要转公文
		String forwardAffairId = GovdocParamUtil.getString(request, "forwardAffairId");
		String forwardText = request.getParameter("forwardText");
		// 项目公文
		Long projectID = GovdocParamUtil.getLong(request, "projectId", null);
		// 是否新建拟文
		boolean isNew = Strings.isBlank(GovdocParamUtil.getString(request, GovdocNewParamEnum.summaryId.name()));
		// 是否快速发文
		boolean isQuickSend = "1".equals(subApp) && "true".equals(GovdocParamUtil.getString(request, GovdocNewParamEnum.isQuickSend.name(), "false"));

		ContentConfig _config = ContentConfig.getConfig(ModuleType.edoc);
		newVo.setContentCfg(_config);
		newVo.setTemplateId(templateId);
		newVo.setFrom(from);
		newVo.setSubApp(subApp);
		newVo.setSummaryId(summaryId);
		newVo.setAffairId(affairId);
		newVo.setQuickSend(isQuickSend);
		newVo.setNew(isNew);
		newVo.setNewBusiness(isNew ? "1" : "0");
		newVo.setSignSummaryId(signSummaryId);
		newVo.setDistributeType(distributeType);
		newVo.setContentDataId(distributeContentDataId);
		newVo.setContentTemplateId(distributeContentTemplateId);
		newVo.setDistributeAffairId(distributeAffairId);
		if ("template".equals(from) && templateId != null) {
			newVo.setOriginalContentId(templateId);// 用于调模板，显示绑定的公文单数据，从ctp_content_all中取module_id
		} else if (!isNew && summaryId != null) {
			newVo.setOriginalContentId(summaryId);// 用于编辑，显示对应的公文单数据，从ctp_content_all中取module_id
		}
		newVo.setForwardText(forwardText);
		newVo.setForwardAffairId(forwardAffairId);
		newVo.setProjectId(projectID);
		newVo.setCurrentUser(AppContext.getCurrentUser());
		// 获取用户的岗位，打印用
		newVo.setPostName(Functions.showOrgPostName(AppContext.getCurrentUser().getPostId()));

		// 设置流程期限
		/*
		 * newVo.setDeadLine(GovdocParamUtil.getLong(request, "deadLine",
		 * null)); try { String temp = request.getParameter("deadlineDatetime");
		 * newVo.setDeadLineTime(Strings.isNotBlank(temp) ? DateUtil.parse(temp)
		 * : null); } catch (ParseException e) { LOGGER.error(e.getMessage(),
		 * e); } newVo.setAdvanceRemind(GovdocParamUtil.getLong(request,
		 * "advanceRemind", null));
		 */
	}

	/**
	 * 公文新建页面-组装文单数据
	 * @param newVo
	 * @return
	 * @throws BusinessException
	 */
	public static GovdocNewVO fillFormData(GovdocNewVO newVo) throws BusinessException {
		List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
		CtpTemplateCategory category = null;
		CtpTemplate template = newVo.getTemplate();
		FormBean defaultFormBean = govdocFormManager.getFormByTemplate4Govdoc(template);
		// 如果是调用外单位的模板
		if (template != null && AppContext.currentAccountId() != template.getOrgAccountId() && defaultFormBean != null) {
			FormBean formBean = govdocFormManager.getForm(defaultFormBean.getId());
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", formBean.getId());
			map.put("categoryId", formBean.getCategoryId());
			map.put("name", formBean.getFormName());
			listMap.add(map);
			category = templateManager.getCtpTemplateCategory(formBean.getCategoryId());
		} else {
			FlipInfo fi = new FlipInfo();
			fi.setPage(-1);
			fi.setSize(-1);
			fi.setNeedTotal(false);// 不查总数
			Map<String, Object> params = new HashMap<String, Object>();
			if ("1".equals(newVo.getSubApp())) {
				params.put("formType", FormType.govDocSendForm.getKey());
			} else if ("2".equals(newVo.getSubApp())) {
				params.put("formType", FormType.govDocReceiveForm.getKey());
			} else if ("3".equals(newVo.getSubApp())) {
				params.put("formType", FormType.govDocSignForm.getKey());
			}
			params.put("useFlag", FormUseFlagEnum.enabled.getKey());
			params.put("orgAccountId", AppContext.currentAccountId());
			params.put("state", FormStateEnum.published.getKey());
			listMap = govdocFormManager.selectByFlipInfo(fi, params);
		}
		if (listMap.size() == 0) {
			return null;
		}
		List<ModuleType> intList = new ArrayList<ModuleType>();
		ModuleType govdocModuleType = null;
		String moduleCategoryName = "";
		if ("1".equals(newVo.getSubApp())) {
			govdocModuleType = ModuleType.govdocSend;
			moduleCategoryName = "发文模板";
		} else if ("2".equals(newVo.getSubApp())) {
			govdocModuleType = ModuleType.govdocRec;
			moduleCategoryName = "收文模板";
		} else if ("3".equals(newVo.getSubApp())) {
			govdocModuleType = ModuleType.govdocSign;
			moduleCategoryName = "签报模板";
		}
		intList.add(govdocModuleType);
		List<CtpTemplateCategory> list = new ArrayList<CtpTemplateCategory>();
		long defaultCategoryId = 0;
		Long defaultFormId = null;
		if (newVo.getSummary() != null) {
			defaultFormId = newVo.getSummary().getFormAppid();
		}
		// 如果是调用外单位的模板
		if (template != null && AppContext.currentAccountId() != template.getOrgAccountId() && category != null) {
			list.add(category);
			if (category.getId() != null) {
				defaultCategoryId = category.getId();
			} else {
				defaultCategoryId = 0;
			}
		} else {
			list = templateManager.getCategorys(AppContext.getCurrentUser().getLoginAccount(), intList);
			CtpTemplateCategory govdocSendNode = new CtpTemplateCategory((long) govdocModuleType.getKey(), moduleCategoryName, Long.parseLong(String.valueOf(ModuleType.edoc
					.getKey())));
			list.add(govdocSendNode);
			GovdocFormDefault govdocFormDefault = null;
			if (defaultFormId == null) {
				if (newVo.getIsQuickSend()) {
					govdocFormDefault = govdocFormManager.getGovdocFormDefaultBySubApp(AppContext.currentAccountId(), newVo.getSubApp(), 1);
				} else {
					govdocFormDefault = govdocFormManager.getGovdocFormDefaultBySubApp(AppContext.currentAccountId(), newVo.getSubApp(), 0);
				}
				if (govdocFormDefault != null) {
					defaultFormId = govdocFormDefault.getFormId();
					defaultCategoryId = govdocFormDefault.getCategoryId();
				}
			} else {
				for (Map<String, Object> map : listMap) {
					if (map.get("id").equals(defaultFormId)) {
						for (CtpTemplateCategory ctpTemplateCategory : list) {
							if (map.get("categoryId").equals(ctpTemplateCategory.getId())) {
								defaultCategoryId = ctpTemplateCategory.getId();
								break;
							}
						}
					}
					if (defaultCategoryId != 0) {
						break;
					}
				}
			}
			if (template != null) {// 调用模板
				if(null != template.getFormAppId()){
					FormBean tempFb = govdocFormManager.getForm(template.getFormAppId());
					if (tempFb != null) {
						defaultCategoryId = tempFb.getCategoryId();
					}
				}
			}
			Iterator<CtpTemplateCategory> it = list.iterator();
			while (it.hasNext()) {
				CtpTemplateCategory ec = it.next();
				boolean result = false;
				for (Map<String, Object> map : listMap) {
					Object ecIdObj = map.get("categoryId");
					// 根据类型获取默认表单
					Long moren = govdocFormManager.getDefaultSendFormId(ec.getId());
					if (map.get("id").equals(moren)) {
						map.put("defaultFormByCategory", 1);
					}
					if (null != ecIdObj && Strings.isNotBlank(ecIdObj.toString())) {
						if (ec.getId().toString().equals(ecIdObj.toString())) {// 如果列表中有该类型
							result = true;
						}
					}
				}
				if (!result) {
					it.remove();
				}
			}
		}
		newVo.setEdocCategoryList(list);
		newVo.setFormList(JSONUtil.toJSONString(listMap));
		newVo.setDefaultFormId(defaultFormId);
		newVo.setDefaultCategoryId(defaultCategoryId);
		return newVo;
	}

	public static void fillNewGovdocObj(GovdocNewVO newVo) throws Exception {
		
	}
	
	public static void fillCommonData(GovdocNewVO newVo) throws Exception {
		PermissionVO defaultPermission = new PermissionVO();
		// 设置默认节点权限
		if ("1".equals(newVo.getSubApp())) {
			defaultPermission = permissionManager.getDefaultPermissionByConfigCategory(EnumNameEnum.edoc_new_send_permission_policy.name(), newVo.getCurrentUser().getAccountId());
		} else if ("2".equals(newVo.getSubApp())) {
			defaultPermission = permissionManager.getDefaultPermissionByConfigCategory(EnumNameEnum.edoc_new_rec_permission_policy.name(), newVo.getCurrentUser().getAccountId());
		} else if ("3".equals(newVo.getSubApp())) {
			defaultPermission = permissionManager.getDefaultPermissionByConfigCategory(EnumNameEnum.edoc_new_qianbao_permission_policy.name(), newVo.getCurrentUser()
					.getAccountId());
		}
		newVo.setDefaultPermission(defaultPermission);
		if (newVo.getSummary() == null) {
			EdocSummary summary = new EdocSummary();
			// fb 分办跳转页面流程密级需要转到收文单
			if (newVo.getSignSummaryId() != null) {
				EdocSummary signSummary = govdocSummaryManager.getSummaryById(newVo.getSignSummaryId());
				summary.setSecretLevel(signSummary.getSecretLevel());
			}
			newVo.setSummary(summary);
			summary.setCanForward(true);
			summary.setCanArchive(true);
			summary.setCanDueReminder(true);
			summary.setCanEditAttachment(true);
			summary.setCanModify(true);
			summary.setCanTrack(1);
			summary.setCanEdit(true);
			summary.setAdvanceRemind(newVo.getAdvanceRemind());
			summary.setDeadline(newVo.getDeadLine());
			summary.setDeadlineDatetime(newVo.getDeadLineTime());
			summary.setCreateTime(new Timestamp(System.currentTimeMillis()));
			// xuker 快速发文
			if (newVo.getIsQuickSend()) {
				summary.setProcessId(GovdocQuickSendWorkFlow.processId + "");// 预置的数据
				summary.setGovdocType(Integer.valueOf(newVo.getSubApp()));
				summary.setIsQuickSend(true);
				newVo.setProcessId(GovdocQuickSendWorkFlow.processId + "");
			}
		}
		/**
		 * 1：来自关联项目 2：原始协同模板喊关联项目的置灰
		 */
		if ((newVo.isParentColTemplete() && null != newVo.getTemplate() && null != newVo.getTemplate().getProjectId())) {
			newVo.setDisabledProjectId("1");
		}
		if (newVo.getProjectId() == null) {
			Long projectId = newVo.getSummary().getProjectId();
			newVo.setProjectId(projectId);
		}
		ContentViewRet context = null;
		CtpContentAll ctpContentAll = null;
		// 正文
		// 加一个条件。并且不是转发文操作
		if (Strings.isBlank(newVo.getForwardText()) && newVo.getSignSummaryId() == null
				&& (newVo.isNew() && newVo.getTemplateId() == null || (newVo.getTemplateId() != null && TemplateEnum.Type.workflow.name().equals(newVo.getTemplate().getType())))) {
			context = new ContentViewRet();
			context.setModuleId(null);
			context.setModuleType(ModuleType.edoc.getKey());
			context.setCommentMaxPath("00");
		} else {
			String rightId = null;
			CtpTemplate template = newVo.getTemplate();
			if (template != null && template.getWorkflowId() != null && template.getWorkflowId() != 0) {
				rightId = govdocWorkflowManager.getNodeFormViewAndOperationName(template.getWorkflowId(), null);
			}

			// 设置转发后的表单不能修改正文
			EdocSummary fromToSummary = newVo.getSummary();
			int viewState = CtpContentAllBean.viewState__editable;
			if (fromToSummary.getParentformSummaryid() != null && !fromToSummary.getCanEdit()) {
				viewState = CtpContentAllBean.viewState_readOnly;
			}
			newVo.setContentViewState(viewState);
			newVo.setUuidlong(UUIDLong.longUUID());
			newVo.setZwModuleId(newVo.getOriginalContentId());
			newVo.setZwRightId(rightId);
			newVo.setZwIsnew(false);
			newVo.setZwViewState(viewState);
			
			newVo.getBodyVo().setBodyType(MainbodyType.WpsWord.name());
			
			
			context = new ContentViewRet();
			context.setModuleType(ModuleType.edoc.getKey());
			context.setModuleId(newVo.getOriginalContentId());
			// TODO 这行代码有错,需要修改
			// context.setModuleType(PermissionFatory.getPermBySubApp(newVo.getSubApp()).getModuleType());
			context.setCommentMaxPath("00");
			// 附言
			// TODO 这行代码有错,需要修改
			// ContentUtil.commentView(request, _config,
			// ModuleType.collaboration,originalContentId,null);
			// 取出模板中公文正文数据 start
			// List<CtpContentAll> contentList = null;
			if (newVo.getTemplateId() != null && template != null && template.getId() != null && newVo.getAffairId() == null) {
				// 分办在这里其实没进去，在下面逻辑中获取到分办正文
				if ((!newVo.isNew() && newVo.getDistributeAffairId() != null) || newVo.getSummary().getFromType() == GovdocEnum.GovdocFromTypeEnum.api.ordinal()) {// 如果是分办或者外系统通过rest接口导入的文，调用模板正文还是使用分办的原正文
					ctpContentAll = GovdocContentHelper.getBodyContentByModuleId(newVo.getSummaryId());
				} else {
					ctpContentAll = GovdocContentHelper.getBodyContentByModuleId(newVo.getTemplateId());
					if (null != ctpContentAll && "-1".equals(ctpContentAll.getContent())) {// 如果调用的无正文模板，则保留以前的正文类型和正文内容
						CtpContentAll oldContentAll = GovdocContentHelper.getBodyContentByModuleId(Long.valueOf(newVo.getSummaryId()));
						if (oldContentAll != null) {
							ctpContentAll.setContent(oldContentAll.getContent());
							ctpContentAll.setContentType(oldContentAll.getContentType());
						}
					}
				}
			}
			// 取出模板中公文正文数据 end
			// 非模板 普通公文正文数据 start
			else {
				ctpContentAll = GovdocContentHelper.getBodyContentByModuleId(Long.valueOf(newVo.getSummaryId()));
			}
			if (newVo.getSignSummaryId() != null && newVo.getTemplateId() != null) {// 找到分办的正文，可能是转的PDF正文
				ctpContentAll = GovdocContentHelper.getBodyContentByModuleId(Long.valueOf(newVo.getSignSummaryId()));
			}
			if (Strings.isNotBlank(newVo.getForwardText())) {
				String[] result = newVo.getForwardText().split("\\.");
				if (Strings.isNotBlank(newVo.getForwardAffairId())) {
					CtpAffair affair = affairManager.get(Long.valueOf(newVo.getForwardAffairId()));
					newVo.setForwardSummaryId(affair.getObjectId());
					List<Attachment> atts = new ArrayList<Attachment>();
					List<Attachment> titleAreaAtts = new ArrayList<Attachment>();
					if ("0".equals(result[0])) {// 正文作为新公文正文
						CtpContentAll fromCtpContentAll = GovdocContentHelper.getBodyContentByModuleId(affair.getObjectId());
						// 需要复制一份新的作为转发文的正文
						try {
							ctpContentAll = (CtpContentAll) fromCtpContentAll.clone();
						} catch (CloneNotSupportedException e1) {
							LOGGER.error("正文复制失败", e1);
						}
						if (ctpContentAll != null) {
							ctpContentAll.setCreateDate(new Date());
							if (fromCtpContentAll.getContentType() != MainbodyType.HTML.getKey()) {
								try {
									V3XFile v3xInFile = fileManager.getV3XFile(Long.valueOf(fromCtpContentAll.getContent()));
									V3XFile v3xOutFile = (V3XFile) v3xInFile.clone();
									v3xOutFile.setNewId();
									v3xOutFile.setCreateDate(ctpContentAll.getCreateDate());
									v3xOutFile.setUpdateDate(ctpContentAll.getCreateDate());
									fileManager.save(v3xOutFile);
									// 设置附件文件为新复制的文件id
									ctpContentAll.setContent(v3xOutFile.getId().toString());
									// upload目录
									String uploadFolder = fileManager.getFolder(ctpContentAll.getCreateDate(), true);
									// 原始文件
									File fileIn = fileManager.getFile(v3xInFile.getId(), v3xInFile.getCreateDate());
									// 复制的文件
									File FileOut = new File(uploadFolder + File.separator + v3xOutFile.getId());
									if (fileIn != null && FileOut != null) {
										FileUtil.copyFile(fileIn, FileOut);
									}
								} catch (CloneNotSupportedException e) {
									LOGGER.info("正文复制错误:" + ctpContentAll.getTitle(), e);
								}
							}
						}
					} else if ("1".equals(result[0])) {// 正文作为新公文附件
						newVo.getBodyVo().setBodyType("");
						
						CtpContentAll contentAll = GovdocContentHelper.getBodyContentByModuleId(affair.getObjectId());
						String content = contentAll.getContent();
						Date createDate = contentAll.getCreateDate();
						String subject = affair.getSubject();
						if (contentAll.getContentType() == MainbodyType.HTML.getKey()) {
							V3XFile f = fileManager.save(content == null ? "" : content, ApplicationCategoryEnum.edoc, subject + ".htm", createDate, false);
							Attachment cAtt = new Attachment(f, ApplicationCategoryEnum.edoc, com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FILE);
							atts.add(cAtt);
							titleAreaAtts.add(cAtt);
						} else if (contentAll.getContentType() == MainbodyType.Pdf.getKey() || contentAll.getContentType() == MainbodyType.Ofd.getKey()) {// OFD支持
							String srcPath = fileManager.getFolder(createDate, true) + separator + String.valueOf(content);
							File srcFile = new File(srcPath);
							if (srcFile.exists() && srcFile.isFile()) {
								InputStream in = new FileInputStream(srcFile);
								// GOV-4751
								// 收文转发文时选择将收文的正文作为新发文的附件，结果查看新发文的正文，也显示成收文正文的内容了（正常应该显示为空，原收文的正文以附件形式显示）。
								String extName = ".pdf";
								if (contentAll.getContentType() == MainbodyType.Ofd.getKey()) {
									extName = ".ofd";
								}
								V3XFile f = fileManager.save(in, ApplicationCategoryEnum.edoc, subject + extName, createDate, false);
								Attachment cAtt = new Attachment(f, ApplicationCategoryEnum.edoc, com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FILE);
								atts.add(cAtt);
								titleAreaAtts.add(cAtt);
							} else {
								LOGGER.error("公文正文的文件不存在：" + srcFile);
							}
						} else {
							String srcPath = fileManager.getFolder(createDate, true) + separator + String.valueOf(content);
							// 1.解密文件
							String newPath = CoderFactory.getInstance().decryptFileToTemp(srcPath);
							// 2.转换成标准正文
							String newPathName = SystemEnvironment.getSystemTempFolder() + separator + String.valueOf(UUIDLong.longUUID());
							Util.jinge2StandardOffice(newPath, newPathName);
							// 3.构造输入流
							InputStream in = new FileInputStream(new File(newPathName));
							// GOV-4751
							// 收文转发文时选择将收文的正文作为新发文的附件，结果查看新发文的正文，也显示成收文正文的内容了（正常应该显示为空，原收文的正文以附件形式显示）。
							V3XFile f = fileManager.save(in, ApplicationCategoryEnum.edoc,
									subject + EdocUtil.getOfficeFileExt(MainbodyType.getEnumByKey(contentAll.getContentType()).toString()), createDate, false);
							Attachment cAtt = new Attachment(f, ApplicationCategoryEnum.edoc, com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FILE);
							atts.add(cAtt);
							titleAreaAtts.add(cAtt);
						}
					} else if ("2".equals(result[0])) {// 不带正文和附件//sun
						ctpContentAll = null;
						
						newVo.getBodyVo().setBodyType("");
					}
					// 新发文关联收文 和 收文关联新发文
					if ("true".equals(result[1]) || "true".equals(result[2])) {
						newVo.setGovdocRelation1(result[1]);
					}
					if ("true".equals(result[2])) {
						newVo.setGovdocRelation2(result[2]);
					}
					List<Attachment> attachments = attachmentManager.getByReference(affair.getObjectId());
					for (Attachment attachment : attachments) {
						if (attachment.getType() == com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FILE.ordinal()) {
							atts.add(attachment);
							if (attachment.getCategory() == ApplicationCategoryEnum.edoc.getKey()) {
								titleAreaAtts.add(attachment);
							}
						}
					}
					newVo.setFilesmContentAttsName(attachmentManager.getFileNameExcludeSuffix(titleAreaAtts));
					newVo.setAttListJSON(attachmentManager.getAttListJSON(atts));
				}
				// 收文转发文自动办结//sun
				ConfigItem configItem = govdocOpenManager.getEdocSwitch(IConfigPublicKey.GOVDOC_SWITCH_KEY, "zfwzidongbanjie", AppContext.currentAccountId());
				if (configItem != null) {
					newVo.setZfwZiDongBanJie(configItem.getConfigValue());
				}
			}
		}
		//是否有转发文的信息。
		List<GovdocExchangeMain> turnSendEdocRelations = govdocExchangeManager.findByReferenceIdId(newVo.getSummary().getId(),GovdocExchangeMain.EXCHANGE_TYPE_ZHUANFAWEN);
		if (turnSendEdocRelations != null && turnSendEdocRelations.size() > 0) {
			if (newVo.getSummary().getGovdocType() != null && newVo.getSummary().getGovdocType() == ApplicationSubCategoryEnum.edoc_fawen.getKey()) {
				Long sumID = turnSendEdocRelations.get(0).getSummaryId();
				EdocSummary turnSummary=govdocSummaryManager.getSummaryById(sumID);
				if(turnSummary!=null){
					newVo.setGovdocRelation1("true");
					newVo.setForwardSummaryId(sumID);
				}
			}
		}
		// 非模板 普通公文正文数据 start
		if (ctpContentAll != null && null != ctpContentAll.getContentType() && MainbodyType.FORM.getKey() != ctpContentAll.getContentType()) {
			Integer tempType = 0;
			// 来自待发
			if (newVo.getSummaryId() != null && "waitSend".equals(newVo.getFrom())) {
				CtpContentAll oldContentAll = GovdocContentHelper.getBodyContentByModuleId(Long.valueOf(newVo.getSummaryId()));
				if (oldContentAll != null) {
					tempType = oldContentAll.getContentType();
				}
			} else {
				tempType = ctpContentAll.getContentType();
			}
			newVo.getBodyVo().setContentType(tempType);
			newVo.getBodyVo().setBodyType(GovdocUtil.getBodyType(tempType));
			newVo.getBodyVo().setCreateDate((Date) ctpContentAll.getCreateDate());// 公文正文对象的时间需要单独处理一下
			newVo.getBodyVo().setContent(ctpContentAll.getContent());
			if(newVo.getBodyVo().getContentType()!=null) {
				if(newVo.getBodyVo().getContentType().intValue() != MainbodyType.HTML.getKey()&&Strings.isNotBlank(ctpContentAll.getContent())) {
					newVo.getBodyVo().setFileId(Long.parseLong(ctpContentAll.getContent()));
				}
			}
			newVo.getBodyVo().setBodyContent(ctpContentAll);
			
			if (Strings.isNotBlank(ctpContentAll.getContent()) && !newVo.getBodyVo().getBodyType().equals(MainbodyType.HTML.name())) {
				V3XFile v3xFile = fileManager.getV3XFile(Long.parseLong(ctpContentAll.getContent()));
				if (v3xFile != null) {
					newVo.getBodyVo().setFileName(v3xFile.getFilename());
				}
				//取得复制的正文的filename，作为正文onload是的filename。解决签章验证的问题
				//签章问题不用考虑了 该段代码屏蔽OA-164226
				
//				GovdocExchangeDetail detail = govdocExchangeManager.findDetailBySummaryId(newVo.getSignSummaryId());
//				if(detail!=null){ 
//					GovdocExchangeMain main = govdocExchangeManager.getGovdocExchangeMainById(detail.getMainId());
//					if(main != null && main.getOriginalFileId()!=null){
//						newVo.setExchangeContentIdNewGov(String.valueOf(main.getOriginalFileId()));
//					}
//				}
			}

			// 公文模板是否有正文类型，-1表示没有，0表示有设置类型
			if ((ctpContentAll.getContentType() == 43 || ctpContentAll.getContentType() == 41)
			// 兼容前台界面直接只用content来判断是否有正文，从而造成HTML正文在前台JS报错
					&& "-1".equals(ctpContentAll.getContent())) {
				newVo.getBodyVo().setContentT("-1");
			} else {
				newVo.getBodyVo().setContentT("0");
				if (newVo.getSummaryId() != null && "waitSend".equals(newVo.getFrom()) && newVo.getTemplateId() != null) {
					CtpContentAll contentAll = GovdocContentHelper.getBodyContentByModuleId(Long.valueOf(newVo.getTemplateId()));
					if (contentAll != null && contentAll.getContent() != null && "-1".equals(contentAll.getContent())) {
						newVo.getBodyVo().setContentT("-1");
					} else {
						newVo.getBodyVo().setContentT("0");
					}
				}
			}
		}

		boolean isSpecialSteped = false;
		// 流程相关信息
		if (context != null) {

			context.setWfProcessId(newVo.getSummary().getProcessId());
			context.setWfCaseId(newVo.getCaseId() == null ? -1l : newVo.getCaseId());
			
			String xml = "";
			BPMProcess process = null;
			isSpecialSteped = newVo.getAffair() != null && newVo.getAffair().getSubState() == SubStateEnum.col_pending_specialBacked.key();
			if (newVo.getTemplate() != null && newVo.getTemplate().getWorkflowId() != null && !isSpecialSteped && !"resend".equals(newVo.getFrom())) {
				Long wfProcessId = newVo.getTemplate().getWorkflowId();
				if(wfProcessId.longValue() == 0) {
					if(newVo.getTemplate().getFormParentid() != null) {
						CtpTemplate rootTemplate = templateManager.getCtpTemplate(newVo.getTemplate().getFormParentid());
						if(rootTemplate != null) {
							wfProcessId = rootTemplate.getWorkflowId();
						}
					}
				}
				// 如果存在模板 需要将模板的ID设置到页面的工作流的相关参数上面去 模板的XML信息
				// 模板的workflowNodesInfo信息
				context.setWfProcessId(String.valueOf(wfProcessId));
				process = govdocWorkflowManager.getTemplateProcess(wfProcessId);
				if (!TemplateUtil.isSystemTemplate(newVo.getTemplate().getId())) {
					xml = govdocWorkflowManager.selectWrokFlowTemplateXml(wfProcessId.toString());
				}
			} else if (Strings.isNotBlank(newVo.getSummary().getProcessId())) {
				process = govdocWorkflowManager.getBPMProcessForM1(newVo.getSummary().getProcessId());
				xml = govdocWorkflowManager.selectWrokFlowXml(newVo.getProcessId());
				String rightId = govdocWorkflowManager.getNodeFormViewAndOperationName(process, null);
				String formOperationId = (rightId != null && rightId.split("[.]").length == 2) ? rightId.split("[.]")[1] : "";
				newVo.setFormOperationId(formOperationId);
				newVo.setZwRightId(rightId);
			}
			String workflowNodesInfo = govdocWorkflowManager.getWorkflowNodesInfo(context.getWfProcessId(), ModuleType.edoc.name(),
					PermissionUtil.getCtpEnumBySubApp(newVo.getSubApp()));
			// add 快速发文的流程xml 20160316 start
			if (newVo.getIsQuickSend()) {
				xml = GovdocQuickSendWorkFlow.processXml;
				workflowNodesInfo = GovdocQuickSendWorkFlow.workflowNodesInfo;
			}
			if (!isSpecialSteped && !"resend".equals(newVo.getFrom())) {
				// add 快速发文的流程xml 20160316 end
				if (newVo.getTemplate() != null) {
					context.setProcessTemplateId(String.valueOf(newVo.getTemplate().getWorkflowId()));
					context.setWfProcessId("");
					if(newVo.getTemplate().isSystem()){
						newVo.setNoselfflow("noselfflow");
					}
				}
			}
			context.setWorkflowNodesInfo(workflowNodesInfo);
			if (!isSpecialSteped) {
				newVo.setWfXMLInfo(Strings.escapeJavascript(xml));
			}
			newVo.setContentContext(context);
		}
		// 除正文外都不能修改流程
		newVo.setOnlyViewWF(null != newVo.getTemplate() && !TemplateEnum.Type.text.name().equals(newVo.getTemplate().getType()));
		// 加载页面可能有的专业签章数据
		AppContext.putRequestContext("moduleId", newVo.getSummaryId());
		AppContext.putRequestContext("canDeleteISigntureHtml", true);
		if (newVo.getSummary().getDeadlineDatetime() != null) {
			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			newVo.setDeadLineDateTimeHidden(sf.format(newVo.getSummary().getDeadlineDatetime()));
		}
		if (newVo.getSummary().getOrgAccountId() == null) {
			newVo.getSummary().setOrgAccountId(AppContext.currentAccountId());
		}
		String _trackValue = customizeManager.getCustomizeValue(newVo.getCurrentUser().getId(), CustomizeConstants.TRACK_SEND);
		newVo.setCustomSetTrack(Strings.isBlank(_trackValue));

		// 580督办添加
		// if (AppContext.hasPlugin("supervision") && supervisionManager !=
		// null) {
		// modelAndView.addObject("supervisionFormId",
		// supervisionManager.getSupervisionAFormId());
		// String supervisionType = request.getParameter("supType");
		// if (Strings.isNotBlank(supervisionType)) {
		// // 580督查督办：获取是催办 还是 变更
		// String operType = request.getParameter("operType");
		// modelAndView.addObject("operType", operType);
		// modelAndView.addObject("supType", supervisionType);
		// // 初始化关联文档
		// SupervisionManager supervisionManager = (SupervisionManager)
		// AppContext.getBean("supervisionManager");
		// String masterDataId = request.getParameter("masterDataId");
		// long supervisionDataId = Strings.isNotBlank(masterDataId) ?
		// Long.valueOf(masterDataId) : -1L;
		// String json = supervisionManager.getRelationData(supervisionDataId,
		// operType);
		// modelAndView.addObject("relationJson", json);
		// modelAndView.addObject("relationField",
		// request.getParameter("relationField"));
		// }
		// }

	}

	/**
	 * 公文新建页面-封装续办对象
	 * @param newVo
	 * @throws Exception
	 */
	public static void fillCustomDealWithDate(GovdocNewVO newVo) throws Exception {
		int type = 0;
		boolean isCustomDealWithTemplate = false;// 是否为续办模板
		// add by rz 2017-12-06[获取续办模板] start
		CtpTemplate template = newVo.getTemplate();
		if (newVo.getTemplate() != null) {
			EdocSummary tSummary = (EdocSummary) XMLCoder.decoder(template.getSummary());
			if (tSummary.getIsCustomDealWith() != null && tSummary.getIsCustomDealWith()) {
				isCustomDealWithTemplate = tSummary.getIsCustomDealWith();
				if (isCustomDealWithTemplate) {
					newVo.setCustomDealWithPermission(tSummary.getCustomDealWithNode());
					List<Map<String, Object>> members = new ArrayList<Map<String, Object>>();
					Map<String, Object> memberMap = new HashMap<String, Object>();
					memberMap.put("id", tSummary.getCustomDealWithMemberId());
					memberMap.put("name", tSummary.getCustomDealWithMemberName());
					members.add(memberMap);
					newVo.setMembers(members);
					newVo.setCustomDealWithMemberId(tSummary.getCustomDealWithMemberId());
				}
			}
		}
		newVo.setCustomDealWithTemplate(isCustomDealWithTemplate);
		// add by rz 2017-12-06[获取续办模板] end
		String configItem = "";
		if ("1".equals(newVo.getSubApp())) {
			type = ApplicationSubCategoryEnum.edoc_fawen.getKey();
			configItem = "niwen";
		} else if ("3".equals(newVo.getSubApp())) {
			type = ApplicationSubCategoryEnum.edoc_qianbao.getKey();
			configItem = "niwen";
		} else {
			type = ApplicationSubCategoryEnum.edoc_shouwen.getKey();
			configItem = "dengji";
		}
		String category = PermissionFatory.getPermBySubApp(type).getCategorty();
		Permission permission = permissionManager.getPermission(category, configItem, AppContext.currentAccountId());
		Boolean isSubmitToReturn = false;
		Boolean isCustomCealwith = true;
		if (newVo.getAffair() != null) {
			CtpAffair ctpAffair = affairManager.get(newVo.getAffairId());
			Map<String, Object> map = AffairUtil.getExtProperty(ctpAffair);
			Object proCustomCealwith = map.get(AffairExtPropEnums.pro_custom_dealwith.toString());
			if (!"true".equals(proCustomCealwith)) {
				isCustomCealwith = false;
			}
		}

		if (isCustomDealWithTemplate || (permission != null && !newVo.isFromTemplate() && isCustomCealwith)) {
			NodePolicy nodePolicy = permission.getNodePolicy();
			if (Strings.isNotBlank(nodePolicy.getPermissionRange()) || Strings.isNotBlank(nodePolicy.getPermissionRange1())) {
				// 如果是续办
				newVo.setCustomDealWith(true);// 是否续办
				Map<String, List<Map<String, Object>>> memberMapList = new HashMap<String, List<Map<String, Object>>>();
				List<String> permissionList = new ArrayList<String>();
				List<PermissionVO> permissions = new ArrayList<PermissionVO>();
				String[] permissionIds = StringUtils.split(nodePolicy.getPermissionRange(), ",");
				// 提交返回节点权限长度
				newVo.setReturnPermissionsLength(permissionIds.length);
				if (permissionIds != null && permissionIds.length > 0) {
					isSubmitToReturn = true;
				}
				permissionIds = (String[]) ArrayUtils.addAll(permissionIds, StringUtils.split(nodePolicy.getPermissionRange1(), ","));
				for (String str : permissionIds) {
					if (Strings.isBlank(str))
						continue;
					PermissionVO permissionVO = permissionManager.getPermission(Long.valueOf(str));
					if (permissionVO == null) {
						continue;
					}
					if (permissionVO != null && permissionVO.getIsEnabled() == 1)
						permissions.add(permissionVO);
					Permission temp = permissionManager.getPermission(category, permissionVO.getName(), permissionVO.getOrgAccountId());
					String memberRange = temp.getNodePolicy().getMemberRange();
					// logger.debug("自流程策略设置的人员="+memberRange);
					List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
					if (Strings.isNotBlank(memberRange)) {
						// 获取发起者
						Long startMember = AppContext.currentUserId();
						String[] strs = StringUtils.split(memberRange, ",");
						for (String memberStr : strs) {
							if (Strings.isBlank(memberStr))
								continue;
							String[] ids = StringUtils.split(memberStr, "|");
							if (memberStr.startsWith("Member")) {
								V3xOrgMember member = orgManager.getMemberById(Long.valueOf(ids[ids.length - 1]));
								if (member == null || !member.isValid())
									continue;
								Map<String, Object> memberMap = new HashMap<String, Object>();
								memberMap.put("id", member.getId());
								memberMap.put("name", member.getName());
								memberMap.put("orgAccountId", member.getOrgAccountId());
								if (!list.contains(memberMap)) {
									list.add(memberMap);
								}
							} else if (memberStr.startsWith("Node")) {
								GovdocOrgHelper.parseRoleMember(list, startMember, ids[1]);
							}
						}
					}
					memberMapList.put(permissionVO.getName(), list);
					permissionList.add(permissionVO.getName());
				}
				newVo.setPermissions(permissions);
				if (!permissionList.contains(newVo.getCustomDealWithPermission())) {
					newVo.setMembers(null);
					newVo.setCustomDealWithMemberId(null);
					if (newVo.isCustomDealWithTemplate()) {
						newVo.setCustomDealWithMemberId("-1");
					}
				}
				// 处理存为草稿状态的续办列表值
				ContentViewRet context = newVo.getContentContext();
				String wfNodesInfo = null;
				if (context != null) {
					wfNodesInfo = context.getWorkflowNodesInfo();
				}
				if (!Strings.isBlank(wfNodesInfo) && !isCustomDealWithTemplate) {
					try {
						WorkflowApiManager wapi= (WorkflowApiManager)AppContext.getBean("wapi");
						BPMProcess process = wapi.getBPMProcess(context.getWfProcessId());
						if (process != null) {
							BPMTransition bpmTransition = (BPMTransition) process.getStart().getDownTransitions().get(0);
							String seeyonPolicy = bpmTransition.getTo().getSeeyonPolicy().getId();
							newVo.setCustomDealWithPermission(seeyonPolicy);
							List<BPMActor> actorList = bpmTransition.getTo().getActorList();
							BPMActor bpmActor = actorList.get(0);
							String memberId = bpmActor.getParty().getId();
							newVo.setCustomDealWithMemberId(memberId);
							String type1 = bpmActor.getParty().getType().id;
							boolean isContainsMember = false;
							for (Map<String, Object> entry : memberMapList.get(seeyonPolicy)) {
								if (entry.get("id").toString().equals(memberId)) {
									isContainsMember = true;
									break;
								}
							}
							if (!isContainsMember) {
								Map<String, Object> memberMap = new HashMap<String, Object>();
								if ("user".equals(type1)) {
									V3xOrgMember member = orgManager.getMemberById(Long.parseLong(memberId));
									memberMap.put("id", member.getId());
									memberMap.put("name", member.getName());
									memberMap.put("orgAccountId", member.getOrgAccountId());
								} else if ("Department".equals(type1)) {
									V3xOrgDepartment member = orgManager.getDepartmentById(Long.parseLong(memberId));
									memberMap.put("id", member.getId());
									memberMap.put("name", member.getName());
									memberMap.put("orgAccountId", member.getOrgAccountId());
								} else if ("Team".equals(type1)) {
									V3xOrgTeam member = orgManager.getTeamById(Long.parseLong(memberId));
									memberMap.put("id", member.getId());
									memberMap.put("name", member.getName());
									memberMap.put("orgAccountId", member.getOrgAccountId());
								} else if ("Level".equals(type1)) {
									V3xOrgLevel member = orgManager.getLevelById(Long.parseLong(memberId));
									memberMap.put("id", member.getId());
									memberMap.put("name", member.getName());
									memberMap.put("orgAccountId", member.getOrgAccountId());
								} else if ("Post".equals(type1)) {
									V3xOrgPost member = orgManager.getPostById(Long.parseLong(memberId));
									memberMap.put("id", member.getId());
									memberMap.put("name", member.getName());
									memberMap.put("orgAccountId", member.getOrgAccountId());
								}
								memberMap.put("type", type1.toLowerCase());
								memberMapList.get(seeyonPolicy).add(memberMap);
							}
							newVo.setMembers(memberMapList.get(seeyonPolicy));
						}
					} catch (Exception e) {
						// logger.error(e.getMessage(), e);
					}
				} else {
					if (!newVo.isCustomDealWithTemplate()) {
						newVo.setMembers(memberMapList.get(permissions.get(0).getName()));
					}
				}
				newVo.setMemberJson(JSONUtil.toJSONString(memberMapList));
				if (isSubmitToReturn) {
					boolean hasChengban = true;
					// 判断承办是否被停用
					Permission chengban = permissionManager.getPermission(category, "chengban", AppContext.currentAccountId());
					if (chengban == null) {
						chengban = permissionManager.getPermission(category, "承办", AppContext.currentAccountId());
						if (chengban == null) {
							hasChengban = false;
						}
					}
					if (chengban != null) {
						newVo.setCurrentPolicyId(chengban.getName());
						newVo.setCurrentPolicyName(chengban.getLabel());
					}
					newVo.setNotExistChengban(!hasChengban);
				} else {
					newVo.setNotExistChengban(false);
				}
				newVo.setNextMember(orgManager.getMemberById(AppContext.currentUserId()));
			} else {
				newVo.setCustomDealWith(false);
			}
		}
	}
	
	public static void fillTremDate(GovdocNewVO newVo) throws BussinessException{
		EdocSummary summary = newVo.getSummary();
		if(summary != null){
			newVo.setTemplateHasProcessTermType(summary.getRemindInterval()!=null&&summary.getRemindInterval()>=0);
			boolean processTremType= false;
			boolean remindInterval = false;
			if((summary.getProcessTermType()!=null && summary.getProcessTermType()>=0)){
				processTremType = true;
				newVo.setTemplateHasProcessTermType(true);
			}
			if(summary.getRemindInterval()!=null && summary.getRemindInterval()>=0){
				remindInterval = true;
				newVo.setTemplateHasRemindInterval(true);
			}
			newVo.setProcessTrem(processTremType);
			newVo.setRemindInterval(remindInterval);	
		}
	}
	public static void findSenderCommentLists(GovdocNewVO newVo) throws BusinessException {
		Long moduleId = null;
		if (newVo.getSummaryId() != null) {
			moduleId = newVo.getSummaryId();
		} else if (newVo.getTemplateId() != null) {
			moduleId = newVo.getTemplateId();
		}
		List<Comment> commentSenderList = ctpCommentManager.getCommentAllByCTYPE(ModuleType.edoc, moduleId, Comment.CommentType.sender);
		AppContext.putRequestContext("commentSenderList", commentSenderList);
		newVo.setCommentSenderList(commentSenderList);
	}

	public static void fillQuickSendData(GovdocNewVO newVo) throws Exception {
		if (newVo.getIsQuickSend()) {
			newVo.getSummary().setProcessId(GovdocQuickSendWorkFlow.processId + "");// 预置的数据
			newVo.getSummary().setGovdocType(1);
			newVo.getSummary().setIsQuickSend(true);
			newVo.setProcessId(GovdocQuickSendWorkFlow.processId + "");
			newVo.setXml(GovdocQuickSendWorkFlow.processXml);
			newVo.setWorkflowNodesInfo(GovdocQuickSendWorkFlow.workflowNodesInfo);
		}
	}

	public static void fillSecretLevelList(GovdocNewVO newVo) throws BusinessException {
		// fb 动态获取枚举，流程密级显示用到
		List<CtpEnumItem> secretLevelList1 = enumManagerNew.getEnumItems(EnumNameEnum.edoc_secret_level);
		List<CtpEnumItem> secretLevelList = new ArrayList<CtpEnumItem>();
		if (CollectionUtils.isNotEmpty(secretLevelList1)) {
			for (CtpEnumItem item : secretLevelList1) {// 理论上是修改manager接口的，但 但
														// 改了之后很多报错。。。。。。。。
				if (item.getState() == Constants.METADATAITEM_SWITCH_DISABLE) {
					continue;
				}
				item.setLabel(ResourceUtil.getString(item.getLabel()));
				secretLevelList.add(item);
			}
		}
		newVo.setSecretLevelList(secretLevelList);
	     //合并处理设置
	     boolean canAnyDealMerge = GovdocUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE,newVo.getSummary());
	     boolean canPreDealMerge = GovdocUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE,newVo.getSummary());
	     boolean canStartMerge = GovdocUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.START_MERGE,newVo.getSummary());
	     newVo.setCanAnyDealMerge(canAnyDealMerge);
	     newVo.setCanPreDealMerge(canPreDealMerge);
	     newVo.setCanStartMerge(canStartMerge);
	}

	public static void fillGovdocSwitch(GovdocNewVO newVo) throws BusinessException {
		Long accountId = AppContext.currentAccountId();
		//获取公文布局参数
		newVo.setNewGovdocView(govdocOpenManager.getGovdocViewValue(newVo.getCurrentUser().getId(), newVo.getCurrentUser().getLoginAccount()));
			
		if (newVo.getTemplateId() == null) {
			if (!govdocOpenManager.isEdocSelfFlow(accountId)) {
				newVo.setNoselfflow("noselfflow1");
			}
		}
		
		if (newVo.getIsQuickSend()) {
			// 判断是否有打印控制
			ConfigItem item = govdocOpenManager.getEdocSwitch("printSet", "enabled", accountId);
			if (item != null && "1".equals(item.getConfigValue())) {
				newVo.setPrintSet(true);
				ConfigItem configItem = govdocOpenManager.getEdocSwitch("printSet", "mainNum", accountId);
				String mainNum = configItem.getConfigValue();
				configItem = govdocOpenManager.getEdocSwitch("printSet", "copyNum", accountId);
				String copyNum = configItem.getConfigValue();
				newVo.setMainNum(mainNum);
				newVo.setCopyNum(copyNum);
			}
		}
		
		String isGzFlag = null;
		try {
			isGzFlag = PropertiesConfiger.getInstance().getProperty("system.gz.edition");
		} catch (Exception e) {
			LOGGER.error("配置文件未正确加载");
		}
		if ("true".equals(isGzFlag) && "2".equals(newVo.getSubApp())) {
			newVo.getBodyVo().setBodyType(MainbodyType.Pdf.name());
		}
		// 是否是贵州专版
		newVo.setGzEdition("true".equals(isGzFlag));
		
		// 需要获得创建公文的单位的开关_正文套红日期
		newVo.getBodyVo().setTaohongriqiSwitch(govdocOpenManager.isTaohongriqiSwitch(accountId));
	}

	/**
	 * 发文节点权限信息
	 * 
	 * @param newVo
	 * @throws BusinessException
	 */
	public static void fillNodePolicyInfo(GovdocNewVO newVo) throws BusinessException {
		// 拟文默认显示正文还是文单逻辑
		int type = ApplicationSubCategoryEnum.edoc_fawen.getKey();
		String configItem = "niwen";
		if (Integer.valueOf(newVo.getSubApp()) == ApplicationSubCategoryEnum.edoc_shouwen.getKey()) {
			type = ApplicationSubCategoryEnum.edoc_shouwen.getKey();
			configItem = "dengji";
		} else if (Integer.valueOf(newVo.getSubApp()) == ApplicationSubCategoryEnum.edoc_qianbao.getKey()) {
			type = ApplicationSubCategoryEnum.edoc_qianbao.getKey();
		}
		String category = PermissionFatory.getPermBySubApp(type).getCategorty();
		Permission permission = permissionManager.getPermission(category, configItem, AppContext.currentAccountId());
		if (permission != null) {
			NodePolicy nodePolicy = permission.getNodePolicy();
			newVo.setFormDefaultShow(nodePolicy.getFormDefaultShow());
			// 判断拟文节点是否有打印权限
			if (Strings.isNotBlank(nodePolicy.getBaseAction())) {
				AppContext.putRequestContext("officecanPrint", nodePolicy.getBaseAction().contains("Print"));
			} else {
				AppContext.putRequestContext("officecanPrint", false);
			}
		}
	}

	
	
	public static void fillBackFormId(GovdocSummaryVO summaryVO) throws BusinessException {
		// 连续回退问题时，后续节点获取backFormID错误V5_V5.71_海南省港务公安局_如果连续回退了多个节点时再提交或者前面一个节点取回再提交时绑定了意见的不会--Start
		Long isFlowBack = summaryVO.getAffair().getBackFromId();
		if (isFlowBack == null) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("objectId", summaryVO.getSummary().getId());
			params.put("memberId", summaryVO.getAffair().getMemberId());
			params.put("state", StateEnum.col_cancel.key());
			params.put("nodePolicy", summaryVO.getAffair().getNodePolicy());

			List<CtpAffair> affairs = affairManager.getByConditions(null, params);
			if (affairs != null && affairs.size() > 0) {
				isFlowBack = affairs.get(0).getBackFromId();
			}
		}
		summaryVO.setBackFormId(isFlowBack);
	}

	/**
	 * 保存待发参数获取
	 * 
	 * @param para
	 * @return
	 */
	public static GovdocNewVO fillSaveDrftParam(GovdocNewVO newVo, Map para, HttpServletRequest request) throws BusinessException {
		newVo.setCurTemId(ParamUtil.getLong(para, "curTemId", null));
		newVo.settId(ParamUtil.getLong(para, "tId", null));
		newVo.setNewBusiness(ParamUtil.getString(para, "newBusiness", null));
		newVo.setContentSaveId(ParamUtil.getString(para, "contentSaveId", null));
		
		// 从URL中取参数
		Long distributeAffairId = GovdocParamUtil.getLong(request, "distributeAffairId", null);
		newVo.setDistributeAffairId(distributeAffairId);
		// 非编辑状态的附件说明
		newVo.setCantEditFilesmName(ParamUtil.getString(para, "cantEditFilesmName", null));
		newVo.setFilesmFieldName(ParamUtil.getString(para, "filesmFieldName", null));
		newVo.setQuickSend("true".equals(ParamUtil.getString(para, "isQuickSend", null)));
		// 转发文关联收文参数
		if (para.get("govdocRelation1") != null) {
			newVo.setGovdocRelation1(para.get("govdocRelation1").toString());
		}
		if (para.get("govdocRelation2") != null) {
			newVo.setGovdocRelation2(para.get("govdocRelation2").toString());
		}
		if (para.get("forwardAffairId") != null) {
			newVo.setForwardAffairId(para.get("forwardAffairId").toString());
		}
		if (para.get("customDealWith") != null && "true".equals(para.get("customDealWith"))) {
			newVo.setCustomDealWith(true);
		}

		EdocSummary summary = (EdocSummary) ParamUtil.mapToBean(para, new EdocSummary(), false);
		// 更新修改正文的状态
		if (para.get("canEdit") != null && "1".equals((String) para.get("canEdit"))) {
			summary.setCanEdit(true);
		} else {
			summary.setCanEdit(false);
		}
		// 获取关联项目的ID
		summary.setProjectId(ParamUtil.getLong(para, "selectProjectId", null, false));
		summary.setGovdocType(ParamUtil.getInt(para, "subApp", null, false));
		if (summary.getGovdocType().intValue() == 1) {
			summary.setEdocType(0);
		} else if (summary.getGovdocType().intValue() == 2) {
			summary.setEdocType(1);
		} else if (summary.getGovdocType().intValue() == 3) {
			summary.setEdocType(2);
		} else if (summary.getGovdocType().intValue() == 4) {
			summary.setEdocType(3);
		}
		
		//公文正文相关，用于保存待发
		GovdocBodyVO bodyVo = GovdocContentHelper.fillSendBodyVo(newVo, para);
		summary.setBodyType(String.valueOf(bodyVo.getContentType()));
		
		// 保存开关
		if (para.get("canArchive") != null && "1".equals(para.get("canArchive"))) {
			summary.set_canArchive(true);
		}
		if (para.get("canEditAttachment") != null && "1".equals(para.get("canEditAttachment"))) {
			summary.setCanEditAttachment(true);
		}
		if (para.get("canEdit") != null && "1".equals(para.get("canEdit"))) {
			summary.set_canEdit(true);
		}
		if (para.get("canModify") != null && "1".equals(para.get("canModify"))) {
			summary.set_canModify(true);
		}
		// 关于跟踪的代码
		Object canTrack = para.get("canTrack");
		int track = 0;
		if (null != canTrack) {
			track = 1;// affair的track为1的时候为全部跟踪，0时为不跟踪，2时为跟踪指定人
			if (null != para.get("radiopart")) {
				track = 2;
			}
			summary.setCanTrack(track);
		} else {
			// 如果没勾选跟踪，这里讲值设置为false
			summary.setCanTrack(0);
		}
		newVo.setSummary(summary);
		// 得到跟踪人员的ID
		newVo.setTrackType(track);
		newVo.setTrackMemberId(ParamUtil.getString(para, "zdgzry", null));
        //保存合并处理策略
        Map<String,String> mergeDealType = new HashMap<String,String>();
        String canStartMerge= (String)para.get("canStartMerge");
        if((BPMSeeyonPolicySetting.MergeDealType.START_MERGE.getValue()).equals(canStartMerge)){
     	   mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.START_MERGE.name(), canStartMerge);
        }
        String canPreDealMerge= (String)para.get("canPreDealMerge");
        if((BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE.getValue()).equals(canPreDealMerge)){
        	mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE.name(), canPreDealMerge);
        }
        String canAnyDealMerge= (String)para.get("canAnyDealMerge");
        if((BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE.getValue()).equals(canAnyDealMerge)){
        	mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE.name(), canAnyDealMerge);
        }
        summary.setMergeDealType(JSONUtil.toJSONString(mergeDealType));
		return newVo;
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	public static GovdocNewVO fillSendParam(GovdocNewVO newVo, HttpServletRequest request) throws BusinessException {
		// 构建协同VO对象
		Map para = ParamUtil.getJsonDomain("colMainData");
		// 从前端获取Summary对象
		EdocSummary summary = (EdocSummary) ParamUtil.mapToBean(para, new EdocSummary(), false);
		// 由于_canEdit有默认值
		summary.setCanEdit("1".equals(para.get("canEdit")));
		if (!summary.isNew()) {
			try {
				EdocSummary oldSummary = govdocSummaryManager.getSummaryById(summary.getId(), false);
				if (oldSummary != null) {
					newVo.setOldSummary((EdocSummary) oldSummary.clone());
				}
			} catch (CloneNotSupportedException cse) {
				LOGGER.error("保存待发clone原公文对象出错", cse);
			}
		}
		summary.setGovdocType(ParamUtil.getInt(para, "subApp"));
		summary.setEdocType(GovdocUtil.getEdocTypeByGovdoc(summary.getGovdocType()));

		String secretLevel = (String) para.get("secretLevel");
		if (Strings.isNotBlank(secretLevel)) {
			summary.setSecretLevel(secretLevel);
		} else {
			summary.setSecretLevel(null);
		}
		// 新建发送设置summary新ID
		summary.setSubject(summary.getSubject().trim());
		String dls = (String) para.get("deadLineselect");
		if (Strings.isNotBlank(dls)) {
			summary.setDeadline(Long.valueOf(dls));
		}
		// 流程自动终止
		if (para.get("canAutostopflow") == null) {
			summary.setCanAutostopflow(false);
		}
		//流程超期
		if (para.get("remindIntervalCheckBox") == null || (para.get("deadLineselect") != null && para.get("deadLineselect") == "0")){
			summary.setRemindInterval(-1L);
		}
		if (para.get("processTermTypeCheck") == null || (para.get("deadLineselect") != null && para.get("deadLineselect") == "0")){
			summary.setProcessTermType(-1);
		}
		if (null != para.get("phaseId") && Strings.isNotBlank((String) para.get("phaseId"))) {
			newVo.setPhaseId((String) para.get("phaseId"));
		}
		if (Strings.isNotBlank((String) para.get("tId"))) {
			newVo.settId(new Long((String) para.get("tId")));
		}
		if (Strings.isNotBlank((String) para.get("curTemId"))) {
			newVo.setCurTemId(Long.valueOf((String) para.get("curTemId")));
		}
		if (Strings.isNotBlank((String) para.get("parentSummaryId"))) {
			summary.setParentformSummaryid(Long.valueOf((String) para.get("parentSummaryId")));
		}
        //保存合并处理策略
        Map<String,String> mergeDealType = new HashMap<String,String>();
        String canStartMerge= (String)para.get("canStartMerge");
        if((BPMSeeyonPolicySetting.MergeDealType.START_MERGE.getValue()).equals(canStartMerge)){
     	   mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.START_MERGE.name(), canStartMerge);
        }
        String canPreDealMerge= (String)para.get("canPreDealMerge");
        if((BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE.getValue()).equals(canPreDealMerge)){
        	mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE.name(), canPreDealMerge);
        }
        String canAnyDealMerge= (String)para.get("canAnyDealMerge");
        if((BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE.getValue()).equals(canAnyDealMerge)){
        	mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE.name(), canAnyDealMerge);
        }
        summary.setMergeDealType(JSONUtil.toJSONString(mergeDealType));

		newVo.setNewBusiness((String) para.get("newBusiness"));
		// 是否是快速发文
		String isQuickSendFla = ParamUtil.getString(para, "isQuickSend");
		if ("true".equals(isQuickSendFla)) {
			newVo.setQuickSend(true);
			// 选中了要进入公文库
			String toEdocLibFlag = ParamUtil.getString(para, "toEdocLibFlag");
			if (Strings.isNotBlank(toEdocLibFlag) && "1".equals(toEdocLibFlag)) {
				summary.setToEdocLibFlag(true);
				summary.setToEdocLibType("quickSend");
			}
		}
		// 设置模板id
		summary.setTempleteId(newVo.gettId());
		// 跟踪的相关逻辑代码(根据Id来去值) 并且判断出跟踪的类型指定人还是全部人
		Object canTrack = para.get("canTrack");
		int track = 0;
		if (null != canTrack) {
			track = 1;// affair的track为1的时候为全部跟踪，0时为不跟踪，2时为跟踪指定人
			if (null != para.get("radiopart")) {
				track = 2;
			}
			summary.setCanTrack(1);
		} else {
			// 如果没勾选跟踪，这里讲值设置为false
			summary.setCanTrack(0);
		}
		newVo.setTrackType(track);
		// 得到跟踪人员的ID
		newVo.setTrackMemberId((String) para.get("zdgzry"));

		// 保存修改正文开关
		if (para.get("canEdit") != null && "1".equals(para.get("canEdit"))) {
			summary.set_canEdit(true);
		}

		newVo.setCaseId(ParamUtil.getLong(para, "caseId", null));
		newVo.setCurrentAffairId(ParamUtil.getLong(para, "currentaffairId", null));
		newVo.setCurrentProcessId(ParamUtil.getLong(para, "oldProcessId", null));
		
		//公文正文相关，用于公文发送
		GovdocBodyVO bodyVo = GovdocContentHelper.fillSendBodyVo(newVo, para);
		summary.setBodyType(String.valueOf(bodyVo.getContentType()));
		
		// 分办时，获取的签收流程AffairId
		String distributeAffairId = request.getParameter("distributeAffairId");
		if (Strings.isNotBlank(distributeAffairId)) {
			newVo.setDistributeAffairId(Long.valueOf(distributeAffairId));
			String _affairId = request.getParameter("affairId");
			if (Strings.isBlank(_affairId)) {
				_affairId = distributeAffairId;
			}
		}
		newVo.setSummary(summary);
		newVo.getSummary().setFormId(ParamUtil.getLong(para, "formId", null));
		if (para.get("customDealWith") != null && "true".equals(para.get("customDealWith"))) {
			newVo.setCustomDealWith(true);
		}
		GovdocMarkHelper.fillSaveMarkParameter(newVo, para);
		// 转发文关联收文参数
		if (para.get("govdocRelation1") != null) {
			newVo.setGovdocRelation1(para.get("govdocRelation1").toString());
		}
		if (para.get("govdocRelation2") != null) {
			newVo.setGovdocRelation2(para.get("govdocRelation2").toString());
		}
		if (para.get("forwardAffairId") != null) {
			newVo.setForwardAffairId(para.get("forwardAffairId").toString());
		}
		// 非编辑状态的附件说明
		newVo.setCantEditFilesmName(ParamUtil.getString(para, "cantEditFilesmName", null));
		newVo.setFilesmFieldName(ParamUtil.getString(para, "filesmFieldName", null));
		return newVo;
	}

	

	public static boolean checkHttpParamValid(GovdocBaseVO baseVo, HttpServletRequest request) throws BusinessException {
		Map<String, String> wfdef = ParamUtil.getJsonDomain("workflow_definition");
		String workflow_data_flag = wfdef.get("workflow_data_flag");
		if (Strings.isBlank(workflow_data_flag) || "undefined".equals(workflow_data_flag.trim()) || "null".equals(workflow_data_flag.trim())) {
			baseVo.setErrorMsg("从前端获取数据失败，请重试！");

			Enumeration es = request.getHeaderNames();
			StringBuilder stringBuffer = new StringBuilder();
			if (es != null) {
				while (es.hasMoreElements()) {
					Object name = es.nextElement();
					String header = request.getHeader(name.toString());
					stringBuffer.append(name + ":=" + header + ",");
				}
				LOGGER.warn("request header---" + stringBuffer.toString());
			}
			return false;
		}
		//防护  校验是否有动态表数据
		try {
			Map para = ParamUtil.getJsonDomain("colMainData");
			Long masterId = ParamUtil.getLong(para, "formRecordid");
			Long formId = ParamUtil.getLong(para, "formRecordid");
			FormApi4Cap3 formApi4Cap3 = (FormApi4Cap3) AppContext.getBean("formApi4Cap3");
			if(masterId != null && formId != null){
				FormDataMasterBean dataBean = formApi4Cap3.findDataById(masterId, formId);
				if(dataBean == null){
					baseVo.setErrorMsg("表单数据保存失败");
					return false;
				}
			}
		} catch (SQLException e) {
			baseVo.setErrorMsg("表单数据保存失败");
			return false;
		}
		return true;
	}

	public static GovdocComponentVO fillComponentParam(HttpServletRequest request) {
		GovdocComponentVO compVO = new GovdocComponentVO();
		String openFrom = request.getParameter("openFrom");
		compVO.setAffairId(GovdocParamUtil.getLong(request, "affairId"));
		compVO.setRightId(GovdocParamUtil.getString(request, "rightId"));
		compVO.setOperationId(GovdocParamUtil.getString(request, "operationId"));
		compVO.setReadonly("true".equalsIgnoreCase(GovdocParamUtil.getString(request, "readonly")));
		compVO.setOpenFrom(GovdocParamUtil.getString(request, "openFrom"));
		if (String.valueOf(WorkflowTraceEnums.workflowTrackType.step_back_repeal.getKey()).equals(request.getParameter("trackType")) && "stepBackRecord".equals(openFrom)) {
			compVO.setOpenFrom("repealRecord");
		}
		compVO.setIsRecSendRel(GovdocParamUtil.getString(request, "isRecSendRel"));
		compVO.setHistoryFlag("true".equalsIgnoreCase(GovdocParamUtil.getString(request, "isHistoryFlag")));
		compVO.setIsHasPraise(GovdocParamUtil.getString(request, "isHasPraise"));
		compVO.setUser(AppContext.getCurrentUser());
		compVO.setFormAppid(GovdocParamUtil.getLong(request, "formAppid"));
		//如果是文档中心打开的、模板设置的 预归档公文，需要取显示明细中的rightId
		compVO.setIsGovArchive(GovdocParamUtil.getString(request, "isGovArchive"));
		
		List<Comment> commentList = (List<Comment>) request.getAttribute("commentList");
		compVO.setCommentList(commentList);
		List<CtpContentAllBean> contentList = (List<CtpContentAllBean>) request.getAttribute("contentList");
		compVO.setContentList(contentList);
		compVO.setTrackType(GovdocParamUtil.getInteger(request, "trackType", 0));
		compVO.setLeaderPishiType(GovdocParamUtil.getString(request, "leaderPishiType"));
		compVO.setNewGovdocView(request.getParameter("newGovdocView"));
		return compVO;
	}

	public static GovdocDealVO fillFinishParam(GovdocDealVO dealVo, HttpServletRequest request) {
		dealVo.setAffairId(GovdocParamUtil.getLong(request, "affairId"));
		dealVo.setConvertPdf("true".equalsIgnoreCase(GovdocParamUtil.getString(request, "isConvertPdf")));
		dealVo.setDistributeAffairId(GovdocParamUtil.getLong(request, "distributeAffairId"));
		dealVo.setGovdocContent(request.getParameter("govdocContent"));
		dealVo.setSmsAlert("yes".equalsIgnoreCase(GovdocParamUtil.getString(request, "duanxintixing")));
		dealVo.setCustomDealWithActivitys(GovdocParamUtil.getString(request, "customDealWithActivitys"));
		dealVo.setPigeonholeValue(GovdocParamUtil.getString(request, "pigeonholeValue"));
		dealVo.setLeaderPishiFlag(GovdocParamUtil.getString(request, "leaderPishiType"));
		// 跟踪信息
		Map<String, Object> trackPara = ParamUtil.getJsonDomain("trackDiv_detail");
		dealVo.setTrackPara(trackPara);
		// 联合发文信息
		Map<String, Object> commentDeal = ParamUtil.getJsonDomain("comment_deal");
		dealVo.setJointlyUnit(GovdocParamUtil.getString(commentDeal, "_jointlyIssued_value"));
		Map<String, Object> pishiParams = new HashMap<String, Object>();
		if(commentDeal.get("pishiName") != null && commentDeal.get("pishiNo") != null
			&& commentDeal.get("pishiyear") != null && commentDeal.get("proxyDate") != null){
			pishiParams.put("pishiName", commentDeal.get("pishiName"));
			pishiParams.put("pishiNo", commentDeal.get("pishiNo"));
			pishiParams.put("pishiyear", commentDeal.get("pishiyear"));
			pishiParams.put("proxyDate", commentDeal.get("proxyDate"));
			dealVo.setPishiParams(pishiParams);
		}
		return dealVo;
	}

	public static GovdocDealVO fillZcdbParam(GovdocDealVO dealVo, HttpServletRequest request) {
		dealVo.setAffairId(GovdocParamUtil.getLong(request, "affairId"));
		// 正常暂存待办，取待办待办意见（非签收稍后待办、非签收不分办）
		dealVo.setDistributeType(GovdocParamUtil.getString(request, "distributeType"));

		Map para = ParamUtil.getJsonDomain("colSummaryData");
		dealVo.setConvertPdf("1".equals(ParamUtil.getString(para, "isConvertContent")));
		// 联合发文信息
		Map<String, Object> commentDeal = ParamUtil.getJsonDomain("comment_deal");

		dealVo.setJointlyUnit(GovdocParamUtil.getString(commentDeal, "_jointlyIssued_value"));
		dealVo.setJointlyUnit(GovdocParamUtil.getString(commentDeal, "_jointlyIssued_value"));
		dealVo.setLeaderPishiFlag(GovdocParamUtil.getString(request, "leaderPishiType"));
		Map<String, Object> pishiParams = new HashMap<String, Object>();
		if(commentDeal.get("pishiName") != null && commentDeal.get("pishiNo") != null
			&& commentDeal.get("pishiyear") != null && commentDeal.get("proxyDate") != null){
			pishiParams.put("pishiName", commentDeal.get("pishiName"));
			pishiParams.put("pishiNo", commentDeal.get("pishiNo"));
			pishiParams.put("pishiyear", commentDeal.get("pishiyear"));
			pishiParams.put("proxyDate", commentDeal.get("proxyDate"));
			dealVo.setPishiParams(pishiParams);
		}
		return dealVo;
	}

	public static GovdocRepealVO fillRepealVO(HttpServletRequest request) {
		GovdocRepealVO repealVO = new GovdocRepealVO();
		Map<String, Object> commentDeal = ParamUtil.getJsonDomain("comment_deal");
		repealVO.setSummaryIdStr(request.getParameter("summaryId"));
		repealVO.setAffairIdStr(request.getParameter("affairId"));
		repealVO.setIsWFTrace(request.getParameter("isWFTrace"));
		String repealComment = request.getParameter("repealComment");
		repealComment = repealComment.replaceAll(new String(new char[] { (char) 160 }), " ");
		repealVO.setRepealComment(request.getParameter("repealComment"));
		if (commentDeal != null) {
			repealVO.setExtAtt1(commentDeal.get("extAtt1").toString());
		}
		return repealVO;
	}

	/**
	 * 回退参数设置
	 * 
	 * @param request
	 * @param dealVo
	 * @return
	 * @throws BusinessException
	 */
	public static GovdocDealVO fillStepBackParam(HttpServletRequest request, GovdocDealVO dealVo) throws BusinessException {
		dealVo.setSummaryId(GovdocParamUtil.getLong(request, "summaryId"));
		dealVo.setAffairId(GovdocParamUtil.getLong(request, "affairId"));
		dealVo.setIsWFTrace(GovdocParamUtil.getString(request, "isWFTrace"));

		Comment comment = new Comment();
		ParamUtil.getJsonDomainToBean("comment_deal", comment);
		dealVo.setComment(comment);

		return dealVo;
	}

	/**
	 * 指定回退参数设置
	 * 
	 * @param request
	 * @param dealVo
	 * @return
	 * @throws BusinessException
	 */
	public static GovdocDealVO fillAppointStepBackParam(HttpServletRequest request, GovdocDealVO dealVo) throws BusinessException {
		dealVo.setSummaryId(GovdocParamUtil.getLong(request, "summaryId"));
		dealVo.setAffairId(GovdocParamUtil.getLong(request, "affairId"));
		dealVo.setWorkitemId(GovdocParamUtil.getLong(request, "workitemId"));
		dealVo.setActivityId(GovdocParamUtil.getLong(request, "activityId"));
		dealVo.setCaseId(GovdocParamUtil.getLong(request, "caseId"));
		dealVo.setProcessId(GovdocParamUtil.getString(request, "processId"));
		dealVo.setSelectTargetNodeId(GovdocParamUtil.getString(request, "theStepBackNodeId"));
		dealVo.setSubmitStyle(GovdocParamUtil.getString(request, "submitStyle"));
		dealVo.setIsWFTrace(GovdocParamUtil.getString(request, "isWFTrace"));
		dealVo.setIsCircleBack(GovdocParamUtil.getString(request, "isCircleBack"));
		WorkflowTraceEnums.workflowTrackType trackType = WorkflowTraceEnums.workflowTrackType.special_step_back_repeal;
		if ("1".equals(dealVo.getIsCircleBack())) {
			trackType = WorkflowTraceEnums.workflowTrackType.circle_step_back_repeal;
		}
		dealVo.setTrackWorkflowType(String.valueOf(trackType.getKey()));

		// 跟踪参数
		// Map<String, String> trackPara =
		// ParamUtil.getJsonDomain("trackDiv_detail");
		// dealVo.setIsTrack(GovdocParamUtil.getBoolean(trackPara,
		// "trackParam"));

		// 取出模板信息
		Map<String, Object> templateMap = (Map<String, Object>) ParamUtil.getJsonDomain("colSummaryData");
		dealVo.setTemplateColSubject(GovdocParamUtil.getString(templateMap, "templateColSubject"));
		dealVo.setTemplateWorkflowId(GovdocParamUtil.getString(templateMap, "templateWorkflowId"));

		Comment comment = new Comment();
		ParamUtil.getJsonDomainToBean("comment_deal", comment);
		dealVo.setComment(comment);

		return dealVo;
	}

	/**
	 * 终止参数设置
	 * 
	 * @param request
	 * @param dealVo
	 * @return
	 * @throws BusinessException
	 */
	public static GovdocDealVO fillStepStopParam(GovdocDealVO dealVo, HttpServletRequest request) throws BusinessException {
		dealVo.setAffairId(GovdocParamUtil.getLong(request, "affairId"));
		dealVo.setExtAtt1(GovdocParamUtil.getString(request, "extAtt1"));
		dealVo.setFrom(GovdocParamUtil.getString(request, "from"));

		if ("new".equals(dealVo.getFrom())) {// 转发自动终止
			Comment comment = new Comment();
			// 如果是转发文，处理意见显示转发文原待办收文自动办结//sun
			comment.setExtAtt1("govdoc.dealZhuanfawen.termination");
			comment.setExtAtt3(StringUtils.EMPTY);
			dealVo.setComment(comment);
		}

		return dealVo;
	}

	/**
	 * EdocSummary中扩展字段赋值
	 * 
	 * @param summary
	 * @param fieldBean
	 * @throws BusinessException
	 */
	public static void fillSummaryExtendParam(EdocSummary summary, FormFieldBean fieldBean, FormDataMasterBean masterBean) throws BusinessException {
		String mappingField = fieldBean.getMappingField();
		if (Strings.isNotBlank(mappingField)) {
			String tail = null;
			Method method = null;
			Object value = masterBean.getFieldValue(fieldBean.getName());
			if (value != null) {
				try {
					if (Pattern.compile("string\\d*").matcher(mappingField).find()) {
						tail = mappingField.substring(mappingField.indexOf("string") + 6);
						method = EdocSummary.class.getMethod("setVarchar" + tail, String.class);
					} else if (Pattern.compile("text\\d*").matcher(mappingField).find()) {
						tail = mappingField.substring(mappingField.indexOf("text") + 4);
						method = EdocSummary.class.getMethod("setText" + tail, String.class);
					} else if (Pattern.compile("integer\\d*").matcher(mappingField).find()) {
						tail = mappingField.substring(mappingField.indexOf("integer") + 7);
						method = EdocSummary.class.getMethod("setInteger" + tail, Integer.class);
						value = new Integer(value.toString());
					} else if (Pattern.compile("date\\d*").matcher(mappingField).find()) {
						tail = mappingField.substring(mappingField.indexOf("date") + 4);
						method = EdocSummary.class.getMethod("setDate" + tail, java.sql.Date.class);
						Date date = (Date) value;
						value = new java.sql.Date(date.getTime());
					} else if (Pattern.compile("decimal\\d*").matcher(mappingField).find()) {
						tail = mappingField.substring(mappingField.indexOf("decimal") + 7);
						method = EdocSummary.class.getMethod("setDecimal" + tail, Double.class);
						value = new Double(value.toString());
					} else if (Pattern.compile("list\\d*").matcher(mappingField).find()) {
						tail = mappingField.substring(mappingField.indexOf("list") + 4);
						method = EdocSummary.class.getMethod("setList" + tail, String.class);
					} else {
						return;
					}
					method.invoke(summary, value);
				} catch (Exception e) {
					LOGGER.warn("反射设置EdocSummary扩展字段值时出错:" + method, e);
				}
			}
		}
	}

	/**
	 * 创建关联信息
	 * 
	 * @param reference
	 *            主公文事项Id
	 * @param subject
	 *            关联的标题
	 * @param summaryId
	 *            关联的id
	 * @param summaryId
	 *            公文类型
	 * @param type
	 *            类型 转发文or交换or转收文
	 * @throws BusinessException
	 */
	public static void createGovdocRelationMain(Long reference, String subject, Long summaryId, int type) throws BusinessException {
		GovdocExchangeMain govdocRelation = new GovdocExchangeMain();
		govdocRelation.setIdIfNew();
		govdocRelation.setReferenceId(reference);
		govdocRelation.setSubject(subject);
		govdocRelation.setSummaryId(summaryId);
		govdocRelation.setCreateTime(new Date());
		govdocRelation.setCreatePerson(String.valueOf(AppContext.getCurrentUser().getId()));
		govdocRelation.setType(type);
		// 添加判断，当关联表中存在了这两个公文的关系，则不再添加
		int count = govdocExchangeManager.findRelationBySummaryIdAndReference(summaryId, reference);
		if (count < 1) {
			govdocExchangeManager.saveOrUpdateMain(govdocRelation);
		}

	}

	/**
	 * 
	 * @param summary
	 * @param summaryFromUE
	 * @throws BusinessException
	 */
	public static EdocSummary cloneSummaryByUE(EdocSummary summary, EdocSummary summaryFromUE) throws BusinessException {
		summary.setSubject(summaryFromUE.getSubject());// 标题
		summary.setDeadline(summaryFromUE.getDeadline());
		summary.setDeadlineDatetime(summaryFromUE.getDeadlineDatetime());
		summary.setAdvanceRemind(summaryFromUE.getAdvanceRemind());
		summary.setAwakeDate(summaryFromUE.getAwakeDate());
		summary.setImportantLevel(summaryFromUE.getImportantLevel());
		summary.setArchiveId(summaryFromUE.getArchiveId());
		summary.setAdvancePigeonhole(summaryFromUE.getAdvancePigeonhole());
		summary.setProjectId(summaryFromUE.getProjectId());
		summary.setCanArchive(summaryFromUE.getCanArchive());
		summary.setCanAutostopflow(summaryFromUE.getCanAutostopflow());
		summary.setCanEdit(summaryFromUE.getCanEdit());
		summary.setCanEditAttachment(summaryFromUE.getCanEditAttachment());
		summary.setCanModify(summaryFromUE.getCanModify());
		summary.setCanForward(summaryFromUE.getCanForward());
		summary.setSecretLevel(summaryFromUE.getSecretLevel());
		summary.setJianbanType(summaryFromUE.getJianbanType());
		summary.setCanTrack(summaryFromUE.getCanTrack());
		summary.setBodyType(summaryFromUE.getBodyType());
		return summary;
	}
	/**
	 */
	public static GovdocCommentVO fillEditCommentParam(GovdocCommentVO commentVo, HttpServletRequest request) {
		commentVo.setModifyContent(request.getParameter("modifyContent"));
		commentVo.setPishiYears(request.getParameter("pishiYears"));
		commentVo.setPishinos(request.getParameter("pishinos"));
		commentVo.setProxydates(request.getParameter("proxydates"));
		commentVo.setEditOpinionId(request.getParameter("editOpinionId")==null?0l:Long.parseLong(request.getParameter("editOpinionId").trim()));
		commentVo.setAffairId(Long.parseLong(request.getParameter("affairId")));
		commentVo.setIsEditAttachment(request.getParameter("isEditAttachment"));
		commentVo.setClearAttr(request.getParameter("clearAttr"));
		return commentVo;
	}
}
