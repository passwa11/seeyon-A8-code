package com.seeyon.apps.govdoc.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

import com.seeyon.apps.doc.constants.DocConstants;
import com.seeyon.apps.edoc.constants.EdocConstant.flowState;
import com.seeyon.apps.govdoc.constant.GovdocEnum.ExchangeDetailStatus;
import com.seeyon.apps.govdoc.constant.GovdocEnum.GovdocSummaryParamEnum;
import com.seeyon.apps.govdoc.po.GovdocExchangeDetail;
import com.seeyon.apps.govdoc.util.GovdocParamUtil;
import com.seeyon.apps.govdoc.vo.GovdocSummaryVO;
import com.seeyon.apps.wpstrans.util.WpsTransConstant;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.permission.bo.NodePolicy;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.plugin.PluginDefinition;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.ctp.form.po.FormPermissionConfig;
import com.seeyon.ctp.form.util.permission.factory.PermissionFatory;
import com.seeyon.ctp.organization.bo.MemberRole;
import com.seeyon.ctp.util.CommonTools;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.enums.EdocOpenFrom;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;

public class GovdocSummaryHelper extends GovdocHelper {

	private static final Log LOGGER = CtpLogFactory.getLog(GovdocSummaryHelper.class);

	/**
	 * 从request中获取参数
	 * @param request
	 * @return
	 */
	public static GovdocSummaryVO fillSummaryVoParam(HttpServletRequest request) throws BusinessException {
		GovdocSummaryVO summaryVO = new GovdocSummaryVO();
		summaryVO.setCurrentUser(AppContext.getCurrentUser());
		// 打开公文入口标识，取自枚举ColOpenFrom
		summaryVO.setOpenFrom(GovdocParamUtil.getString(request, GovdocSummaryParamEnum.openFrom.name()));
		// 打开公文常规参数，affairId待办列表打开，summaryId从统计列表打开
		summaryVO.setAffairId(GovdocParamUtil.getLong(request, GovdocSummaryParamEnum.affairId.name()));
		summaryVO.setSummaryId(GovdocParamUtil.getLong(request, GovdocSummaryParamEnum.summaryId.name()));
		// 当affairId与summaryId为空时，已知流程ID打开公文
		summaryVO.setProcessId(GovdocParamUtil.getString(request, GovdocSummaryParamEnum.processId.name()));
		// 文单节点权限ID
		summaryVO.setOperationId(GovdocParamUtil.getString(request, GovdocSummaryParamEnum.operationId.name()));
		// 文单多视图节点权限ID
		summaryVO.setFormMutilOprationIds(GovdocParamUtil.getString(request, GovdocSummaryParamEnum.formMutilOprationIds.name()));
		// 来自子流程时，多视图节点权限ID赋值给文单节点权限ID
		if (EdocOpenFrom.subFlow.name().equals(summaryVO.getOpenFrom())) {
			summaryVO.setOperationId(summaryVO.getFormMutilOprationIds());
		}
		// 锚点，用于消息打开的时候倒叙去查询谁处理的消息(功能暂未使用)
		summaryVO.setContentAnchor(GovdocParamUtil.getString(request, GovdocSummaryParamEnum.contentAnchor.name()));
		// 入口：联合发文标识
		summaryVO.setJointly("true".equals(GovdocParamUtil.getString(request, GovdocSummaryParamEnum.isJointly.name())));
		// 入口：致信打开协同的时候会传递这个参数ucpc
		summaryVO.setExtFrom(GovdocParamUtil.getString(request, GovdocSummaryParamEnum.extFrom.name()));
		// 入口：代领导批示列表打开，列表标识(功能暂未迁移)
		summaryVO.setLeaderPishiType(GovdocParamUtil.getString(request, GovdocSummaryParamEnum.leaderPishiType.name()));
		// 入口：流程追溯回退列表链接，表示追溯类型
		summaryVO.setTrackTypeRecord(GovdocParamUtil.getString(request, GovdocSummaryParamEnum.trackTypeRecord.name()));
		// 入口：表示来自分库历史表
		summaryVO.setHistoryFlag("1".equals(GovdocParamUtil.getString(request, GovdocSummaryParamEnum.dumpData.name())));
		// 入口：文档中心，配合openFrom使用，固定3位数，分别表示3种权限，值域:1或0
		summaryVO.setLenPotent(GovdocParamUtil.getString(request, GovdocSummaryParamEnum.lenPotent.name()));
		// 归档类型0单位归档(模板设置的归档)，1部门归档（其他方式的归档），可以当作入口，文档中心打开归档公文会带这个参数
		summaryVO.setPigeonholeType(GovdocParamUtil.getInteger(request, GovdocSummaryParamEnum.pigeonholeType.name()));
		//是否从文档中心打开，如果是，值为1
		summaryVO.setIsGovArchive(GovdocParamUtil.getString(request, "isGovArchive"));
		return summaryVO;
	}
	
	/**
	 * 根据相关条件判断当前节点是否不能包含以下节点
	 *
	 * @param summary
	 * @param basicActionList
	 * @param commonActionList
	 * @param advanceActionList
	 */
	public static void checkCanAction(GovdocSummaryVO summaryVO, 
			CtpAffair affair, EdocSummary summary, 
			List<String> basicActionList, List<String> commonActionList,
			List<String> advanceActionList, boolean isTemplete,Permission permission) throws BusinessException {
		// 判断是否有日程事件的菜单，如果没有的话，则不能有转事件
		if (!WFComponentUtil.checkByReourceCode("F02_eventlist")) {
			commonActionList.remove("Transform");
			advanceActionList.remove("Transform");
		}
		// 没有新建协同权限，就不能转发
		if (!WFComponentUtil.checkByReourceCode("F01_newColl")) {
			advanceActionList.remove("Forward");
			commonActionList.remove("Forward");
		}
		// advanceActionList.remove("Terminate");
		// commonActionList.remove("Terminate");
		// 不允许改变流程
		if ((isTemplete || !affair.getMemberId().equals(summary.getStartMemberId())) && summary.getCanModify() != null && !summary.getCanModify()) {
			if (advanceActionList != null) {
				advanceActionList.remove("JointSign");
				advanceActionList.remove("RemoveNode");
				advanceActionList.remove("Infom");
				advanceActionList.remove("AddNode");
				advanceActionList.remove("moreSign");
				advanceActionList.remove("PassRead");
			}
			if (commonActionList != null) {
				commonActionList.remove("AddNode");
				commonActionList.remove("JointSign");
				commonActionList.remove("RemoveNode");
				commonActionList.remove("Infom");
				commonActionList.remove("moreSign");
				commonActionList.remove("PassRead");
			}
		}
		// 转发的表单不能编辑，不管处理节点是不是发起人
		if (Strings.isNotBlank(summary.getForwardMember()) && summary.getCanEdit() != null && !summary.getCanEdit()) {
			if (advanceActionList.contains("Edit")) {
				advanceActionList.remove("Edit");
			}
			if (commonActionList.contains("Edit")) {
				commonActionList.remove("Edit");
			}
		}

		// 580督查督办：非督办人员 或者不包含督办插件 不能看到转督办的节点权限
		if (!GovdocRoleHelper.isSuperviseStaff() || !AppContext.hasPlugin("supervision")) {
			if (advanceActionList.contains("TranstoSupervise")) {
				advanceActionList.remove("TranstoSupervise");
			}
			if (commonActionList.contains("TranstoSupervise")) {
				commonActionList.remove("TranstoSupervise");
			}
		}
		if (summary.isFinished()) {
			basicActionList.remove("Track");
		}
		//新建允许操作是否设置修改正文
        if(summary.get_canEdit()!= null && !summary.get_canEdit()){
        	commonActionList.remove("Edit");
        	advanceActionList.remove("Edit");
        }
        //新建允许操作设置不允许修改附件&& 发起人为处理人时 && 发起人修改附件的开关开启  则可以修改附件
        //OA-161815  已和产品确认 唯一开关未拟文的开关
//        boolean allowUpdateAttachment = govdocOpenManager.isAllowUpdateAttachment();
        if(!summary.getCanEditAttachment()
        		/*&& !(summary.getStartMemberId().longValue() == affair.getMemberId().longValue() && allowUpdateAttachment)*/){
        	commonActionList.remove("allowUpdateAttachment");
        	advanceActionList.remove("allowUpdateAttachment");
        }
        if(summary.getGovdocType()==ApplicationSubCategoryEnum.edoc_jiaohuan.getKey()){
        	commonActionList.remove("PDFSign");
        	advanceActionList.remove("PDFSign");
        	commonActionList.remove("SignChange");
        	advanceActionList.remove("SignChange");
        }
		//G6 6.1sp1升级------------start
		//wps转ofd控制
        if(!WpsTransConstant.WPSTRANS_ENABLE){
        	if (advanceActionList.contains("TransToOfd")) {
        		advanceActionList.remove("TransToOfd");
        	} 
        	if (commonActionList.contains("TransToOfd")) {
        		commonActionList.remove("TransToOfd"); 
        	}
        }
		//转发 不显示
		if (advanceActionList.contains("Forward")) {
			advanceActionList.remove("Forward");
		} 
		if (commonActionList.contains("Forward")) {
			commonActionList.remove("Forward"); 
		}
		
		if(summaryVO.getSwitchVo().getSuperNodestatus() != 0) {
			//基础策略
			/*commonActionList.remove("AddNode");
			commonActionList.remove("AddNode");
			commonActionList.remove("AddNode");
			commonActionList.remove("AddNode");
			commonActionList.remove("AddNode");
			commonActionList.remove("AddNode");
			commonActionList.remove("AddNode");
			commonActionList.remove("AddNode");
			commonActionList.remove("AddNode");
			commonActionList.remove("AddNode");*/
		}
		
		if(!summaryVO.getSwitchVo().isZhuanfawenTactics()) {//若转发文开关未打开，则不显示转发文策略
			commonActionList.remove("Zhuanfawen");
		}
	}
	
	public static void fillSummaryVoBySwitchBefore(GovdocSummaryVO summaryVO) throws BusinessException {
		PluginDefinition definition = SystemEnvironment.getPluginDefinition("govdoc");
		summaryVO.getSwitchVo().setChuantouchakan1("true".equals(definition.getPluginProperty("govdoc.chuantouchakan1")));
		summaryVO.getSwitchVo().setChuantouchakan2("true".equals(definition.getPluginProperty("govdoc.chuantouchakan2")));
		if(summaryVO.getSwitchVo().isChuantouchakan1() || summaryVO.getSwitchVo().isChuantouchakan2()) {
			summaryVO.getSwitchVo().setZhuanfawenTactics(govdocOpenManager.isEdocZhuanfawenTactics());// 启用收文节点转发文策略 开关
			summaryVO.getSwitchVo().setZhuanfawen(govdocOpenManager.checkEdocZhuanfawen());// 转发文默认设置开关
		}
		if(AppContext.hasPlugin("sms")) {
			summaryVO.getSwitchVo().setDuanxintixing(govdocOpenManager.isDuanxintixing());// 短信提醒	
		}
		
		// 获取当前人员的角色权限
		List<MemberRole> memberRoles = orgManager.getMemberRoles(AppContext.getCurrentUser().getId(), null);
		for (MemberRole memberRole : memberRoles) {
			if ("SendEdoc".equals(memberRole.getRole().getCode()) || "EdocQuickSend".equals(memberRole.getRole().getCode())) {
				summaryVO.getSwitchVo().setHasSendEdocRole(true);// 有发文拟文权限，快速发文权限，可以进行转发文操作
			}
		}
		// 只有已发才能有开关修改附件
		if ("listSent".equals(summaryVO.getOpenFrom()) && !summaryVO.isJointly() && summaryVO.getSummary().getState() == flowState.run.ordinal()) {
			summaryVO.getSwitchVo().setCanEditAtt(govdocOpenManager.isAllowUpdateAttachment());
		}
		
		// 允许在文单内编辑意见
		summaryVO.getSwitchVo().setAllowEditInForm("true".equals(definition.getPluginProperty("govdoc.allowEditInForm")));
		if (summaryVO.getSwitchVo().isAllowEditInForm()) {
			summaryVO.getSwitchVo().setAllowCommentInForm(govdocOpenManager.isAllowCommentInForm());// 是否选择在文单中填写 意见
		}
		
		//公文布局开关
		//OA-174371  公文布局设置一屏，公文统计登记薄打开公文查看，显示的经典布局
//		if(EdocOpenFrom.formStatistical.name().equals(summaryVO.getOpenFrom())){//文单统计用经典打开
//			summaryVO.getSwitchVo().setNewGovdocView("0");
//		} else {
			summaryVO.getSwitchVo().setNewGovdocView(govdocOpenManager.getGovdocViewValue(summaryVO.getCurrentUser().getId(), summaryVO.getCurrentUser().getLoginAccount()));	
//		}
		
		//获得超级节点状态
		if (null != summaryVO.getAffair().getActivityId()) {
			int superNodeStatus = govdocWorkflowManager.getSuperNodeStatus(summaryVO.getSummary().getProcessId(), String.valueOf(summaryVO.getAffair().getActivityId()));
			summaryVO.getSwitchVo().setSuperNodestatus(superNodeStatus);
		}
	}

	/**
	 * 根据权限配置控制正文是否显示
	 * @param summaryVO
	 */
	@SuppressWarnings("unchecked")
	public static void fillSummaryVoByShowContentSwitch(GovdocSummaryVO summaryVO) {
		if("listSent".equals(summaryVO.getOpenFrom()) && summaryVO.getNodePolicyObj().getFormDefaultShow()== 1) {
			summaryVO.getSwitchVo().setFormDefaultShow(NodePolicy.FORMDEFAULTSHOW_FORM);
		} else {
			summaryVO.getSwitchVo().setFormDefaultShow(summaryVO.getNodePolicyObj().getFormDefaultShow());//正文(0) 标准文单(2) 全文签批单(1)
		}
		/** 根据权限配置控制正文是否显示 */
		if (GovdocHelper.isCheckShowContentByGovdocNodePropertyConfigOfOpenFrom(summaryVO.getOpenFrom())) {// 登记簿，已发和待发时可以查看正文
			summaryVO.getSwitchVo().setShowContentByGovdocNodePropertyConfig(true);
		} else {
			if (summaryVO.getPermission() != null) {
				// 获取表单中设置的正文显示
				String showContentFlag = "true";// 默认显示正文
				FormPermissionConfig formPermissionConfig = summaryVO.getFormPermConfig();
				if (null != formPermissionConfig) {
					Map<String, String> conentShowMap = (Map<String, String>) JSONUtil.parseJSONString(formPermissionConfig.getShowContentConfig());
					if (null != conentShowMap) {
						for (String key : conentShowMap.keySet()) {
							if (summaryVO.getPermission().getFlowPermId().longValue() == Long.valueOf(key)) {
								showContentFlag = conentShowMap.get(key);
								break;
							}
						}
					}
				}
				summaryVO.getSwitchVo().setShowContentByGovdocNodePropertyConfig("true".equals(showContentFlag));
			}
		}
	}
	
	public static void fillSummaryVoByArchive(GovdocSummaryVO summaryVO) throws BusinessException {
		EdocSummary summary = summaryVO.getSummary();
		CtpAffair affair = summaryVO.getAffair();
		
		//节点权限是否允许归档(发起人只受节点权限控制是否可以归档，其他不限制。)
		boolean isContainOperation = (Boolean) summaryVO.getBasicActionList().contains("Archive");
		//当前人员是发起人或拟文时允许归档
		boolean isSenderOrCanArchive = (summary.getStartUserId().equals(affair.getMemberId()) || (summary.getCanArchive() != null && summary.getCanArchive() == true));
		//当前人员有归档权限
		boolean hasResourceCode = GovdocRoleHelper.hasDocResource(summaryVO.getCurrentUser());
		
		//是否显示归档：1节点权限是否允许归档、2当前人员是发起人或拟文时允许归档、3当前人员有归档权限
		boolean canArchive = isContainOperation && isSenderOrCanArchive && hasResourceCode;
		summaryVO.getSwitchVo().setCanArchive(canArchive);
		AppContext.putSessionContext("canArchive", canArchive);
		
		//是否开启收藏开关
		summaryVO.getSwitchVo().setCollectFlag("true".equals(SystemProperties.getInstance().getProperty("doc.collectFlag")));
		//是否显示收藏：1节点权限是否允许归档、2当前人员是发起人或拟文时允许归档、3当前人员有归档权限、4非A6、5配置文件开启收藏
		boolean canFavorite = isContainOperation && isSenderOrCanArchive && hasResourceCode && !GovdocHelper.isA6() && summaryVO.getSwitchVo().isCollectFlag();
		summaryVO.getSwitchVo().setCanFavorite(canFavorite);
		if (!canFavorite) {
			LOGGER.info("協同ID：<" + summary.getId() + ">,没有收藏权限,isContainOperation:" + isContainOperation + " isSenderOrCanArchive:" + isSenderOrCanArchive
					+ " hasResourceCode：" + hasResourceCode + ",propertyFav:" + summaryVO.getSwitchVo().isCollectFlag() + ",!isA6:" + !GovdocHelper.isA6());
		}
		//公文是否做过收藏
        if(summaryVO.getSwitchVo().isCollectFlag()) {
        	List<Map<String,Long>> collectMap = docApi.findFavorites(AppContext.currentUserId(),CommonTools.newArrayList(affair.getId()));
        	summaryVO.getSwitchVo().setIsCollect(Strings.isNotEmpty(collectMap));
        }
	}
	
	/**
	 * 一屏式布局策略重置
	 * @param summaryVO
	 * @return
	 * @throws BusinessException
	 */
	public static void fillSummaryVoByAction(GovdocSummaryVO summaryVO) throws BusinessException {
		if(!"0".equals(summaryVO.getSwitchVo().getNewGovdocView())) {
			int n = 4;
			if(summaryVO.getCommonActionList().size() >= n) {
				List<String> commonAction = new ArrayList<String>();
				if(summaryVO.getBasicActionList().contains("Print")) {//一屏式要求打印在最左边
					commonAction.add("Print");
				}
				for(String action : summaryVO.getCommonActionList()) {
					if(commonAction.size()<n) {
						commonAction.add(action);
					} else {
						summaryVO.getAdvanceActionList().add(action);
					}
				}
				summaryVO.getCommonActionList().clear();
				summaryVO.getCommonActionList().addAll(commonAction);
			}
		}
	}
	
	/**
	 * 获取保存到本地和打印的权限
	 * lenPotents(三位字符串，1:待定；2:下载;3：打印)
	 * @param summaryVO
	 * @return
	 * @throws BusinessException
	 */
	public static void fillSummaryVoByPrint(GovdocSummaryVO summaryVO) throws BusinessException {
		boolean officecanPrint = false;
		Boolean officecanSaveLocal = false;
		boolean onlySeeContent = false; // 只查看正文
		boolean canPrint = false;// 关联的文档是否可以打印(文单打印)
		try {
			String lenPotents = summaryVO.getLenPotent();//前端传参
			String openForm = summaryVO.getOpenFrom();
			String permissionEnumName = PermissionFatory.getPermBySubApp(summaryVO.getGovdocType()).getCategorty();
			if (!(EnumNameEnum.edoc_new_change_permission_policy.name().equals(permissionEnumName) && "dengji".equals(summaryVO.getNodePolicy()))) {
				Permission permission = summaryVO.getPermission();
				if (permission != null) {
					String baseAction = permission.getBasicOperation();
					// 是否有打印权限，根据v3x_affair表中的state字段来判断
					// 节点对文档中心是否有打印权限
					if (GovdocHelper.isSummaryFromDoc(summaryVO)) {
						officecanPrint = "0".equals(lenPotents.substring(2, 3)) ? false : true;
						officecanSaveLocal = "0".equals(lenPotents.substring(1, 2)) ? false : true;
					} else if (Strings.isNotBlank(baseAction)) {
						int state = summaryVO.getAffair().getState().intValue();
						if (StateEnum.col_waitSend.getKey() == state || StateEnum.col_sent.getKey() == state || EdocOpenFrom.supervise.name().equals(openForm)) {
							officecanPrint = true;
							officecanSaveLocal = true;
						} else {
							if (baseAction.indexOf("PrintContentAcc") >= 0) {
								officecanPrint = true;
							}
							if (baseAction.indexOf("SaveContentAcc") >= 0) {
								officecanSaveLocal = true;
							}
						}
					}
					
					// 判断打印权限
					String[] split = baseAction.split(",");
					List<String> actionList = Arrays.asList(split);
					for (String action : actionList) {
						if ("Print".equals(action.trim())) {
							canPrint = true;
							break;
						}
					}
					
					// 公文档案借阅时，操作权限处不勾选打印，但是借阅出去的公文仍然可以打印
					if ("lenPotent".equals(openForm)) {
						canPrint = officecanPrint;
					}
				}
				
				//系统开关“启用公文关联文档打印权限“,开关开启后，所有穿透都有打印权限，跟节点权限无关;
				//开关处于停用状态时，穿透依然是跟节点权限有关，节点权限有打印，才显示打印按钮
				String openFrom = summaryVO.getOpenFrom();
				if (EdocOpenFrom.glwd.name().equals(openFrom)) {
					ConfigItem relationPrintConf = govdocOpenManager.getEdocSwitch("system_switch", "relation_print", ConfigItem.Default_Account_Id);
					String configValue = relationPrintConf.getConfigValue();
					if ("enable".equals(configValue)) {
						canPrint = true;
					}
				}
				
				String lenPotent = "0";
				if (lenPotents != null && !"".equals(lenPotents)) {
					lenPotent = lenPotents.substring(0, 1);
				}
				if (Byte.toString(DocConstants.LENPOTENT_CONTENT).equals(lenPotent)) {
					onlySeeContent = true;
				}
				
				
				// 如果是签收 需要打印正文
				if ("qianshou".equals(summaryVO.getNodePolicy())) {
					// 如果是标准正文 则不允许打印
					if (!String.valueOf(MainbodyType.HTML.getKey()).equals(summaryVO.getSummary().getBodyType())) {
						canPrint = true;
					}
				}
				
				summaryVO.getSwitchVo().setOfficecanPrint(officecanPrint);
				summaryVO.getSwitchVo().setOfficecanSaveLocal(officecanSaveLocal);
				summaryVO.getSwitchVo().setOnlySeeContent(onlySeeContent);
				summaryVO.getSwitchVo().setCanPrint(canPrint);
			}
			
			//是否允许下载到本地打印
			String printTempPath = govdocDocTemplateManager.getLocalPrintTemplate(summaryVO.getSummary().getFormAppid());
			summaryVO.getSwitchVo().setCanLocalPrint(Strings.isNotBlank(printTempPath));
		} catch (Exception e) {
			LOGGER.error("获取公文处理节点权限异常,affairId:" + summaryVO.getAffair().getId(), e);
			throw new BusinessException("获取公文处理节点权限异常,affairId:" + summaryVO.getAffair().getId());
		}
	}
	
	
	public static void fillSummaryVoBySwitchAfter(GovdocSummaryVO summaryVO) throws BusinessException {
		CtpAffair affair = summaryVO.getAffair();
		
        //1: 撤销的不能进行回复 2：回退导致撤销的不能进行回复
  		if (GovdocHelper.isReadonlyOfOpenFrom(summaryVO.getOpenFrom()) || GovdocHelper.isWaitAffairNotBacked(affair) || summaryVO.isHistoryFlag()) {
  			summaryVO.getSwitchVo().setReadOnly(true);
  		}
  		if (AffairUtil.isFormReadonly(affair)) {
  			AppContext.putRequestContext("deeReadOnly", 1);
  		}
	}

	public static void fillSummaryVoShowButton(GovdocSummaryVO summaryVO) {
		List<String> basicActionList = summaryVO.getBasicActionList();
		EdocSummary summary = summaryVO.getSummary();
		CtpAffair affair = summaryVO.getAffair();
		//签收分办按钮相关
		if (null != summary.getGovdocType() && summary.getGovdocType() == ApplicationSubCategoryEnum.edoc_jiaohuan.getKey()) {
			GovdocExchangeDetail detail = summaryVO.getExchangeDetail();
			if (detail != null) {
				if (basicActionList.contains("ReSign") && basicActionList.contains("Distribute")) {// 如果有签收并分办
					if (detail.getStatus() == ExchangeDetailStatus.waitSign.getKey()) {// 如果为待签收
						summaryVO.getSwitchVo().setShowButton("sign");
					} else if (detail.getStatus() == ExchangeDetailStatus.hasSign.getKey()) {// 如果为已签收
						summaryVO.getSwitchVo().setShowButton("distributeAndFinish");
					} else if (detail.getStatus() == ExchangeDetailStatus.hasFenBan.getKey()) {// 如果已分办
						summaryVO.getSwitchVo().setShowButton("finish");
					}
				} else if (basicActionList.contains("ReSign")) {
					if (detail.getStatus() == ExchangeDetailStatus.waitSign.getKey()) {// 如果为待签收
						summaryVO.getSwitchVo().setShowButton("sign");
					} else if (detail.getStatus() == ExchangeDetailStatus.hasSign.getKey()) {// 如果为已签收
						summaryVO.getSwitchVo().setShowButton("finish");
					} else if (detail.getStatus() == ExchangeDetailStatus.hasFenBan.getKey()) {// 如果已分办
						summaryVO.getSwitchVo().setShowButton("finish");
					}
				} else if (basicActionList.contains("Distribute")) {
					if (detail.getStatus() == ExchangeDetailStatus.waitSign.getKey()) {// 如果为待签收
						summaryVO.getSwitchVo().setShowButton("distributeAndFinish");
					} else if (detail.getStatus() == ExchangeDetailStatus.hasSign.getKey()) {// 如果为已签收
						summaryVO.getSwitchVo().setShowButton("distribute");
					} else if (detail.getStatus() == ExchangeDetailStatus.hasFenBan.getKey()) {// 如果已分办
						summaryVO.getSwitchVo().setShowButton("finish");
					}
				}
			}
		}
		if (affair != null && affair.getApp() == ApplicationCategoryEnum.edoc.getKey()
				&& affair.getSubApp() == ApplicationSubCategoryEnum.edoc_shouwen.getKey() && null != affair.getNodePolicy()
				&& "fenban".equals(affair.getNodePolicy())) {
			summaryVO.getSwitchVo().setShowButton("distribute");
		}
	}
	
	public static void fillSummaryVoByBack(GovdocSummaryVO summaryVO) throws BusinessException {
		CtpAffair affair = summaryVO.getAffair();
		EdocSummary summary = summaryVO.getSummary();
		//公文回退参数
		Long isFlowBack = affair.getBackFromId();
    	if(isFlowBack != null) {
    		FormOpinionConfig displayConfig = summaryVO.getFormOpinionConfig();
    		summaryVO.setOpinionType(displayConfig.getOpinionType());
    		if("3".equals(displayConfig.getOpinionType()) || "4".equals(displayConfig.getOpinionType())) {
    			summaryVO.setAlertStepbackDialog(true);
    		}
    	}
        if (affair.getSubState() != null && SubStateEnum.col_pending_specialBacked.getKey() == affair.getSubState()) {
			summaryVO.setSpecifyFallback(true);
		}
        if (StateEnum.col_pending.getKey() == affair.getState()) {
        	if(!GovdocHelper.isAffairAppointBacked(affair)) {//当时节点非指定回退状态
        		if (summary.getCaseId() != null) {
					//加入指定回退 有些不能操作的代码
					boolean isInSpecialSB = govdocWorkflowManager.isInSpecialStepBackStatus(summary.getCaseId(), summaryVO.isHistoryFlag());
					if (!isInSpecialSB) {// inInSpecialSB为false 就是 处于指定回退状态
						summaryVO.setInSpecialSB(isInSpecialSB);
					}
				}
        	}
		}
	}
	
}
