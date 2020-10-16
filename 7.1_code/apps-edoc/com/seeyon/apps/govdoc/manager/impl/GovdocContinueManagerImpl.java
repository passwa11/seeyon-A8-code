package com.seeyon.apps.govdoc.manager.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.govdoc.helper.GovdocOrgHelper;
import com.seeyon.apps.govdoc.manager.GovdocContinueManager;
import com.seeyon.apps.govdoc.manager.GovdocFormManager;
import com.seeyon.apps.govdoc.manager.GovdocPishiManager;
import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.apps.govdoc.manager.GovdocWorkflowManager;
import com.seeyon.apps.govdoc.vo.GovdocDealVO;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.apps.govdoc.vo.GovdocSummaryVO;
import com.seeyon.apps.govdoc.vo.GovdocXubanVO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.enums.AffairExtPropEnums;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.permission.bo.NodePolicy;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.util.permission.factory.PermissionFatory;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgLevel;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgPost;
import com.seeyon.ctp.organization.bo.V3xOrgTeam;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.edoc.domain.EdocSummary;

import net.joinwork.bpm.definition.BPMActivity;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.engine.wapi.NodeInfo;

/**
 * 新公文续办管理类
 * @author 唐桂林
 *
 */
public class GovdocContinueManagerImpl implements GovdocContinueManager {

	private static final Log LOGGER = LogFactory.getLog(GovdocContinueManagerImpl.class);
	
	private GovdocSummaryManager govdocSummaryManager;
	private GovdocFormManager govdocFormManager;
	private GovdocWorkflowManager govdocWorkflowManager;
	
	private AffairManager affairManager;
	private OrgManager orgManager;
	private PermissionManager permissionManager;
	private GovdocPishiManager govdocPishiManager;
	private CollaborationApi collaborationApi;
	private WorkflowApiManager wapi;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setCustomDealWith(GovdocNewVO info) throws BusinessException{
        if(info.isCustomDealWith()){
        	//拟文时需要填写续办相关信息
        	EdocSummary summary=info.getSummary();
        	BPMProcess process = wapi.getBPMProcess(summary.getProcessId());
        	String customDealWithActivitys = "";
        	if(process != null){
        		for (Object activity : process.getActivitiesList()) {
        			customDealWithActivitys += ","+((BPMActivity)activity).getId();
        		}
        	}
        	List<CtpAffair> affairs = null;
        	if(info.isNew()){
        		affairs = affairManager.getAffairs(info.getSummary().getId(), StateEnum.col_waitSend);
        	}else{
        		affairs = affairManager.getAffairs(info.getSummary().getId(), StateEnum.col_sent);
        	}
        	for (CtpAffair ctpAffair : affairs) {
        		Map map=AffairUtil.getExtProperty(ctpAffair);
        		map.put(AffairExtPropEnums.pro_custom_dealwith.toString(), "true");
        		if(customDealWithActivitys.length() > 0){
        			map.put(AffairExtPropEnums.pro_custom_dealwith_activitys.toString(), customDealWithActivitys.substring(1));
        		}
        		AffairUtil.setExtProperty(ctpAffair, map);
        		affairManager.updateAffair(ctpAffair);
        	}
        }
	}

	/**
	 * 
	 * @param affair
	 * @throws BusinessException
	 */
	public void setCustomAffairExt(GovdocDealVO finishVO) throws BusinessException {
		//处理时更新affair的续办扩展字段
		CtpAffair affair = finishVO.getAffair();
		String customDealWithActivitys = finishVO.getCustomDealWithActivitys();
		if (Strings.isNotBlank(customDealWithActivitys) && !"undefined".equalsIgnoreCase(customDealWithActivitys)) {
			Map<String, Object> map = AffairUtil.getExtProperty(affair);
			map.put(AffairExtPropEnums.pro_custom_dealwith_activitys.toString(), customDealWithActivitys);
			AffairUtil.setExtProperty(affair, map);
			finishVO.setAffair(affair);
		}
		if(AppContext.currentUserId() != affair.getMemberId() && "2".equals(finishVO.getLeaderPishiFlag())){
			String pishiFlag = govdocPishiManager.checkLeaderPishi(AppContext.currentUserId(), affair.getMemberId());
			if("pishi".equals(pishiFlag)){
				AffairUtil.addExtProperty(affair,AffairExtPropEnums.dailu_pishi_mark, pishiFlag+AppContext.currentUserId());
				finishVO.setAffair(affair);
			}
		}
	}
	
	public void fillSummaryVoByXuban(GovdocSummaryVO summaryVO) throws BusinessException {
		if(!summaryVO.getSwitchVo().isCanXuban()) {//当前节点未续办，不做处理
			return;
		}
		if(summaryVO.getAffair().getSubState() == SubStateEnum.col_pending_specialBack.key()) {
			return;
		}
		if(summaryVO.getPermission() == null) {
			return;
		}
		String category = PermissionFatory.getPermBySubApp(summaryVO.getSummary().getGovdocType()).getCategorty();
		Permission permission = summaryVO.getPermission();
		EdocSummary summary = summaryVO.getSummary();
		CtpAffair affair = summaryVO.getAffair();
		Map<String, Object> map = summaryVO.getExtPropertyMap();
		if(map == null) {
			map = AffairUtil.getExtProperty(affair);
		}
		NodePolicy nodePolicy = permission.getNodePolicy();
		
		GovdocXubanVO xubanVo = new GovdocXubanVO();
		summaryVO.getSwitchVo().setCanXuban(true);
		
		xubanVo.setCustomDealWith(map.get(AffairExtPropEnums.pro_custom_dealwith) == null ? "true" : map.get(AffairExtPropEnums.pro_custom_dealwith).toString());// 是否续办
		xubanVo.setCustomDealWithPermission(map.get(AffairExtPropEnums.pro_custom_dealwith_permission));
		xubanVo.setCustomDealWithMemberId(map.get(AffairExtPropEnums.pro_custom_dealwith_memberid));
				
		Boolean isSubmitToReturn = false;
		Map<String, List<Map<String, Object>>> memberMapList = new HashMap<String, List<Map<String, Object>>>();
		List<PermissionVO> permissions = new ArrayList<PermissionVO>();
		String[] permissionIds = StringUtils.split(nodePolicy.getPermissionRange(), ",");
		// 提交返回节点权限长度
		xubanVo.setReturnPermissionsLength(permissionIds.length);
		permissionIds = (String[]) ArrayUtils.addAll(permissionIds, StringUtils.split(nodePolicy.getPermissionRange1(), ","));
		for (String str : permissionIds) {
			if (Strings.isBlank(str)) {
				continue;
			}
			PermissionVO permissionVO = this.permissionManager.getPermission(Long.valueOf(str));
			if (permissionVO == null) {
				continue;
			}
			if (permissionVO != null && permissionVO.getIsEnabled() == 1) {
				permissions.add(permissionVO);
			}
			Permission temp = permissionManager.getPermission(category, permissionVO.getName(), permissionVO.getOrgAccountId());
			String memberRange = temp.getNodePolicy().getMemberRange();
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			if (Strings.isNotBlank(memberRange)) {
				String[] strs = StringUtils.split(memberRange, ",");
				for (String memberStr : strs) {
					if (Strings.isBlank(memberStr)) {
						continue;
					}
					String[] ids = StringUtils.split(memberStr, "|");
					if (memberStr.startsWith("Member")) {
						V3xOrgMember member = this.orgManager.getMemberById(Long.valueOf(ids[ids.length - 1]));
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
						GovdocOrgHelper.parseRoleMember(list, summary.getStartMemberId(), ids[1]);
					}
				}
			}
			memberMapList.put(permissionVO.getName(), list);
		}
		xubanVo.setPermissions(permissions);

		List<Map<String, Object>> members = new ArrayList<Map<String, Object>>();
		if (permissionIds != null && permissionIds.length > 0) {
			PermissionVO permissionVO = this.permissionManager.getPermission(Long.valueOf(permissionIds[0]));
			members = memberMapList.get(permissionVO.getName());
			isSubmitToReturn = true;
		}
		
		if (map.get(AffairExtPropEnums.pro_custom_dealwith_memberid) != null) {
			Long memberId = (Long) map.get(AffairExtPropEnums.pro_custom_dealwith_memberid);
			Map<String, Object> memberMap = new HashMap<String, Object>();
			V3xOrgMember selectMember = this.orgManager.getMemberById(memberId);
			if (selectMember != null) {
				memberMap.put("id", selectMember.getId());
				memberMap.put("name", selectMember.getName());
				memberMap.put("orgAccountId", selectMember.getOrgAccountId());
				members.add(memberMap);
			} else {
				V3xOrgDepartment department = orgManager.getDepartmentById(memberId);
				if (department != null) {
					memberMap.put("id", department.getId());
					memberMap.put("name", department.getName());
					memberMap.put("orgAccountId", department.getOrgAccountId());
					memberMap.put("type", "department");
					members.add(memberMap);
				} else {
					V3xOrgTeam team = orgManager.getTeamById(memberId);
					if (team != null) {
						memberMap.put("id", team.getId());
						memberMap.put("name", team.getName());
						memberMap.put("orgAccountId", team.getOrgAccountId());
						memberMap.put("type", "team");
						members.add(memberMap);
					} else {
						V3xOrgLevel level = orgManager.getLevelById(memberId);
						if (level != null) {
							memberMap.put("id", level.getId());
							memberMap.put("name", level.getName());
							memberMap.put("orgAccountId", level.getOrgAccountId());
							memberMap.put("type", "level");
							members.add(memberMap);
						} else {
							V3xOrgPost post = orgManager.getPostById(memberId);
							if (post != null) {
								memberMap.put("id", post.getId());
								memberMap.put("name", post.getName());
								memberMap.put("orgAccountId", post.getOrgAccountId());
								memberMap.put("type", "level");
								members.add(memberMap);
							}
						}
					}
				}
			}
		}
		xubanVo.setMembers(members);
		xubanVo.setMemberJson(JSONUtil.toJSONString(memberMapList));
		
		V3xOrgMember returnToMember = null;
		if ("sendMember".equals(nodePolicy.getReturnTo())) {
			returnToMember = this.orgManager.getMemberById(summary.getStartMemberId());
		} else if ("currentMember".equals(nodePolicy.getReturnTo())) {
			if("2".equals(summaryVO.getLeaderPishiType())){
				returnToMember = this.orgManager.getMemberById(affair.getMemberId());
			}else{
				returnToMember = this.orgManager.getMemberById(AppContext.currentUserId());
			}
		}
		boolean hasChengban = true;
		if ("sendMember".equals(nodePolicy.getReturnTo())) {
			if (isSubmitToReturn) {
				// 判断承办是否被停用
				Permission chengban = permissionManager.getPermission(category, "chengban", AppContext.currentAccountId());
				if (chengban == null) {
					chengban = permissionManager.getPermission(category, "承办", AppContext.currentAccountId());
					if (chengban == null) {
						hasChengban = false;
					}
				}
				if (chengban != null) {
					xubanVo.setCurrentPolicyId(chengban.getName());
					xubanVo.setCurrentPolicyName(chengban.getLabel());
				}
				xubanVo.setNotExistChengban(!hasChengban);
			} else {
				xubanVo.setNotExistChengban(false);
			}
		} else if ("currentMember".equals(nodePolicy.getReturnTo())) {
			xubanVo.setCurrentPolicyId(permission.getName());
			xubanVo.setCurrentPolicyName(permission.getLabel());
		}
		xubanVo.setNextMember(returnToMember);
		summaryVO.setXubanVo(xubanVo);
	}
	
	public void fillSummaryVoByCustomDealWithForM3(GovdocSummaryVO summaryVO) throws BusinessException {
		if (summaryVO.getSummary() != null && summaryVO.getAffair() != null) {
			EdocSummary summary = summaryVO.getSummary();
			CtpAffair affair = summaryVO.getAffair();
			String category = PermissionFatory.getPermBySubApp(summaryVO.getSummary().getGovdocType()).getCategorty();
			String configItem = collaborationApi.getPolicyByAffair(summaryVO.getAffair()).getId();
			if("newCol".equals(configItem)){
				configItem = "niwen";
			}
			long formId = summary.getFormAppid();
			FormBean formBean = govdocFormManager.getForm(formId);
			Permission permission = permissionManager.getPermission(category, configItem, formBean.getOwnerAccountId());
			if (permission != null) {
				Map<String, Object> map = null;
				// 客开 作者:mly 项目名称：自流程 修改功能：展示自流程操作 start
				NodePolicy nodePolicy = permission.getNodePolicy();
				String bActions = nodePolicy.getBaseAction();
				summaryVO.setCanShowOpinion(bActions.contains("Opinion"));
				summaryVO.setCanShowAttitude(Strings.isNotBlank(nodePolicy.getAttitude()));
				summaryVO.setCanShowCommonPhrase(bActions.contains("CommonPhrase"));
				summaryVO.setCanUploadAttachment(bActions.contains("UploadAttachment"));
				summaryVO.setCanUploadRel(bActions.contains("UploadRelDoc"));
				summaryVO.setIsFaxingNode(bActions.contains("FaDistribute"));
				// chenx 添加默认显示的
				// zengc 已发列表进来打开公文节点权限为niwen，不知道为什么数据库里面没得默认值，导致返回的是qwqp的值。现在将这种情况做适配。
				if("listSent".equals(summaryVO.getOpenFrom())&& nodePolicy.getFormDefaultShow()== 1){
					summaryVO.setFormDefaultShow(nodePolicy.FORMDEFAULTSHOW_FORM);
				}else{
					summaryVO.setFormDefaultShow(nodePolicy.getFormDefaultShow());
				}
				/*
				 * 客开 项目名称：贵州市政府-G6V580省级专版 作者：mtech 修改日期：2017-08-09
				 * [修改功能：节点权限默认显示正文]start mav.addObject("edocDefaultShow",
				 * nodePolicy.getEdocDefaultShow()); 客开 项目名称：贵州市政府-G6V580省级专版
				 * 作者：mtech 修改日期：2017-08-09 [修改功能：节点权限默认显示正文]end
				 */
				/*
				 * 客开 项目名称：贵州市政府-G6V580省级专版 作者：mtech 修改日期：2017-08-10
				 * [修改功能：节点权限-进入公文库选项]start
				 */
				summaryVO.setToEdocLibFlag(nodePolicy.getToEdocLibFlag());
				summaryVO.setToEdocLibSelectFlag(nodePolicy.getToEdocLibSelectFlag());
				/*
				 * 客开 项目名称：贵州市政府-G6V580省级专版 作者：mtech 修改日期：2017-08-10
				 * [修改功能：节点权限-进入公文库选项]end
				 */
				if ((Strings.isNotBlank(nodePolicy.getPermissionRange()) || Strings.isNotBlank(nodePolicy.getPermissionRange1())) && affair != null
						&& affair.getSubState() != SubStateEnum.col_pending_specialBack.key()) {
					summaryVO.setShowCustomDealWith("true");
					if (affair != null) {
						map = AffairUtil.getExtProperty(affair);
						summaryVO.setCustomDealWith(map.get(AffairExtPropEnums.pro_custom_dealwith) == null ? "true" : map.get(
								AffairExtPropEnums.pro_custom_dealwith).toString());// 是否续办
						summaryVO.setCustomDealWithPermission(map.get(AffairExtPropEnums.pro_custom_dealwith_permission));
						summaryVO.setCustomDealWithMemberId(map.get(AffairExtPropEnums.pro_custom_dealwith_memberid));
					}
					Boolean isSubmitToReturn = false;
					Map<String, List<Map<String, Object>>> memberMapList = new HashMap<String, List<Map<String, Object>>>();
					List<PermissionVO> permissions = new ArrayList<PermissionVO>();
					String[] permissionIds = StringUtils.split(nodePolicy.getPermissionRange(), ",");
					// 提交返回节点权限长度
					summaryVO.setReturnPermissionsLength(permissionIds.length);
					permissionIds = (String[]) ArrayUtils.addAll(permissionIds, StringUtils.split(nodePolicy.getPermissionRange1(), ","));
					for (String str : permissionIds) {
						if (Strings.isBlank(str))
							continue;
						PermissionVO permissionVO = this.permissionManager.getPermission(Long.valueOf(str));
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
							Long startMember = govdocSummaryManager.getSummaryById(affair.getObjectId()).getStartUserId();
							String[] strs = StringUtils.split(memberRange, ",");
							for (String memberStr : strs) {
								if (Strings.isBlank(memberStr))
									continue;
								String[] ids = StringUtils.split(memberStr, "|");
								if (memberStr.startsWith("Member")) {
									V3xOrgMember member = this.orgManager.getMemberById(Long.valueOf(ids[ids.length - 1]));
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
					}
					summaryVO.setPermissions(permissions);

					List<Map<String, Object>> members = new ArrayList<Map<String, Object>>();
					if (permissionIds != null && permissionIds.length > 0) {
						PermissionVO permissionVO = this.permissionManager.getPermission(Long.valueOf(permissionIds[0]));
						members = memberMapList.get(permissionVO.getName());
						isSubmitToReturn = true;
					}
					// logger.debug("自流程affair.getCustomDealWithMemberId()="+affair.getCustomDealWithMemberId());
					if (map.get(AffairExtPropEnums.pro_custom_dealwith_memberid) != null) {
						Long memberId = (Long) map.get(AffairExtPropEnums.pro_custom_dealwith_memberid);
						Map<String, Object> memberMap = new HashMap<String, Object>();
						V3xOrgMember selectMember = this.orgManager.getMemberById(memberId);
						if (selectMember != null) {
							memberMap.put("id", selectMember.getId());
							memberMap.put("name", selectMember.getName());
							memberMap.put("orgAccountId", selectMember.getOrgAccountId());
							members.add(memberMap);
						} else {
							V3xOrgDepartment department = orgManager.getDepartmentById(memberId);
							if (department != null) {
								memberMap.put("id", department.getId());
								memberMap.put("name", department.getName());
								memberMap.put("orgAccountId", department.getOrgAccountId());
								memberMap.put("type", "department");
								members.add(memberMap);
							} else {
								V3xOrgTeam team = orgManager.getTeamById(memberId);
								if (team != null) {
									memberMap.put("id", team.getId());
									memberMap.put("name", team.getName());
									memberMap.put("orgAccountId", team.getOrgAccountId());
									memberMap.put("type", "team");
									members.add(memberMap);
								} else {
									V3xOrgLevel level = orgManager.getLevelById(memberId);
									if (level != null) {
										memberMap.put("id", level.getId());
										memberMap.put("name", level.getName());
										memberMap.put("orgAccountId", level.getOrgAccountId());
										memberMap.put("type", "level");
										members.add(memberMap);
									} else {
										V3xOrgPost post = orgManager.getPostById(memberId);
										if (post != null) {
											memberMap.put("id", post.getId());
											memberMap.put("name", post.getName());
											memberMap.put("orgAccountId", post.getOrgAccountId());
											memberMap.put("type", "level");
											members.add(memberMap);
										}
									}
								}
							}
						}
					}
					summaryVO.setMembers(members);
					summaryVO.setMemberJson(JSONUtil.toJSONString(memberMapList));
					V3xOrgMember returnToMember = null;
					if ("sendMember".equals(nodePolicy.getReturnTo())) {
						returnToMember = this.orgManager.getMemberById(summary.getStartMemberId());
					} else if ("currentMember".equals(nodePolicy.getReturnTo())) {
						if("2".equals(summaryVO.getLeaderPishiType())){
							returnToMember = this.orgManager.getMemberById(affair.getMemberId());
						}else{
							returnToMember = this.orgManager.getMemberById(AppContext.currentUserId());
						}
					}
					boolean hasChengban = true;
					if ("sendMember".equals(nodePolicy.getReturnTo())) {
						if (isSubmitToReturn) {
							// 判断承办是否被停用
							Permission chengban = permissionManager.getPermission(category, "chengban", AppContext.currentAccountId());
							if (chengban == null) {
								chengban = permissionManager.getPermission(category, "承办", AppContext.currentAccountId());
								if (chengban == null) {
									hasChengban = false;
								}
							}
							if (chengban != null) {
								summaryVO.setCurrentPolicyId(chengban.getName());
								summaryVO.setCurrentPolicyName(chengban.getLabel());
							}
							summaryVO.setNotExistChengban(!hasChengban);
						} else {
							summaryVO.setNotExistChengban(false);
						}
					} else if ("currentMember".equals(nodePolicy.getReturnTo())) {
						summaryVO.setCurrentPolicyId(permission.getName());
						summaryVO.setCurrentPolicyName(permission.getLabel());
					}
					summaryVO.setNextMember(returnToMember);
				}
				// 客开 作者:mly 项目名称：自流程 修改功能：展示自流程操作 end
			}
		}
	}
	
	@Override
	public void deleteCustomDealWidth(CtpAffair affair) throws BusinessException {
		Map<String, Object> map = AffairUtil.getExtProperty(affair);
		Object customActivitys = map.get(AffairExtPropEnums.pro_custom_dealwith_activitys.toString());
		if (customActivitys != null && !Strings.isBlank(customActivitys.toString())) {
			govdocWorkflowManager.deleteNode(affair, Arrays.asList(StringUtils.split(customActivitys.toString(), ",")));
		}
	}
	
	@Override
	public void deleteAllCustomDealWidth(Long summaryId) throws BusinessException {
		List<CtpAffair> affairs = this.affairManager.getAffairs(summaryId);
		for(CtpAffair temp : affairs){
			Map<String, Object> map = AffairUtil.getExtProperty(temp);
			if(map.get(AffairExtPropEnums.pro_custom_dealwith_activitys.name()) == null) continue;
			List<String> activityIds = new ArrayList<String>();
			for(String activityId : StringUtils.split(map.get(AffairExtPropEnums.pro_custom_dealwith_activitys.name()).toString(),",")){
				activityIds.add(activityId);
			}
			govdocWorkflowManager.deleteNode(temp,activityIds);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void deleteCustomDealWithStepBack(GovdocDealVO dealVo,List<CtpAffair> affairss,String result,String type){
		// 客开 作者:mly 项目名称:自流程 修改功能  续办人员回退 删除续办人员和提交回退人员 start
		//    	List<CtpAffair> affairs = this.affairManager.getAffairsByAppAndObjectId(ApplicationCategoryEnum.valueOf(ctpAffair.getApp()), ctpAffair.getObjectId());
		if("".equals(type)){
			List<CtpAffair> affairs;
//			CtpAffair pendAffair;
			try {
				List<CtpAffair> pendAffairs = this.affairManager.getAffairs(dealVo.getAffair().getObjectId(),StateEnum.col_pending);
//				if(pendAffairs.size() > 0){
//					pendAffair = pendAffairs.get(0);
//				}else{
//					pendAffair = new CtpAffair();
//				}
				for (CtpAffair pendAffair : pendAffairs) {
					affairs = this.affairManager.getAffairsByObjectIdAndNodeId(dealVo.getAffair().getObjectId(),pendAffair.getActivityId());
					for(CtpAffair temp : affairs){
						Map<String, Object> map = AffairUtil.getExtProperty(temp);
						if(map.get(AffairExtPropEnums.pro_custom_dealwith_activitys.name()) == null) continue;
						List<String> activityIds = new ArrayList<String>();
						for(String activityId : StringUtils.split(map.get(AffairExtPropEnums.pro_custom_dealwith_activitys.name()).toString(),",")){
							activityIds.add(activityId);
						}
						//删除后需要清理掉续办内容
						map.remove(AffairExtPropEnums.pro_custom_dealwith_activitys.name());
						Object[][] wheres = new Object[][] { { "id", temp.getId() }};
						Map<String, Object> nameParameters = new HashMap<String, Object>();
						nameParameters.put("extProps", XMLCoder.encoderMap(map));
						affairManager.update(nameParameters, wheres);
						govdocWorkflowManager.deleteNode(temp,activityIds);
					}
				}
				if("1".equals(result)){
	        		List<CtpAffair> list = affairManager.getAffairs(dealVo.getAffair().getObjectId(),StateEnum.col_cancel);
					Long activity=null;
					for (CtpAffair af : list) {
						if(af.getActivityId()!=null && af.getState() == StateEnum.col_cancel.getKey()){
							activity=af.getActivityId();
						}
					}
					for(CtpAffair affairt : affairss){
						Map<String, Object> map = AffairUtil.getExtProperty(affairt);
						if(map.get(AffairExtPropEnums.pro_custom_dealwith_activitys.name()) == null) continue;
						List<String> activityIds = new ArrayList<String>();
						for(String activityId : StringUtils.split(map.get(AffairExtPropEnums.pro_custom_dealwith_activitys.name()).toString(),",")){
							activityIds.add(activityId);
						}
						affairt.setActivityId(activity);
						govdocWorkflowManager.deleteNode(affairt,activityIds);
					}
				}
			} catch (BusinessException e) {
				LOGGER.error("续办回退删除节点失败", e);
				//e.printStackTrace();
			}
	    	// 客开 作者:mly 项目名称:自流程 修改功能  续办人员回退 删除续办人员和提交回退人员 end
		}else{
			//客开 作者:mly 项目名称:自流程 修改功能：指定回退 删除多余的节点 start
			CtpAffair ctpAffair;
			try {
				ctpAffair = this.affairManager.get(dealVo.getAffairId());
				EdocSummary summary = govdocSummaryManager.getSummaryById(ctpAffair.getObjectId());
				BPMProcess process = wapi.getBPMProcess(summary.getProcessId());
				List<NodeInfo> nodeInfos =process.getNextHumenActivities(dealVo.getSelectTargetNodeId(), null, 0);
				List<String> activitys = new ArrayList<String>();
				boolean find = false;
				while(!find){
					for(NodeInfo nodeInfo : nodeInfos){
						if(nodeInfo.getId().equals(ctpAffair.getActivityId().toString())){
							find = true;
						}else{
							activitys.add(nodeInfo.getId());
							nodeInfos =process.getNextHumenActivities(nodeInfo.getId(), null, 0);
						}
					}
				}
				activitys.add(dealVo.getSelectTargetNodeId());
				activitys.remove("start");
				List<String> deleteActivitys = new ArrayList<String>();
				for(String activity: activitys){
					if ("start".equals(activity)) {
						continue;
					}
					List<CtpAffair> affairs =  this.affairManager.getAffairsByObjectIdAndNodeId(ctpAffair.getObjectId(), Long.valueOf(activity));
					for(CtpAffair affair : affairs){
						Map<String, Object> map = AffairUtil.getExtProperty(affair);
						if(map.get(AffairExtPropEnums.pro_custom_dealwith_activitys.name()) == null) continue;
						String[] strs = StringUtils.split(map.get(AffairExtPropEnums.pro_custom_dealwith_activitys.name()).toString(),",");
						for(String str : strs){
							if(deleteActivitys.contains(str)) continue;
							deleteActivitys.add(str);
						}
					}
				}
				govdocWorkflowManager.deleteNode(ctpAffair, deleteActivitys);
			} catch (BusinessException e) {
				LOGGER.error("续办指定回退删除节点失败", e);
				//e.printStackTrace();
			}
		}
		//客开 作者:mly 项目名称:自流程 修改功能：指定回退 删除多余的节点 end
	}
	
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}
	public void setGovdocSummaryManager(GovdocSummaryManager govdocSummaryManager) {
		this.govdocSummaryManager = govdocSummaryManager;
	}
	public void setGovdocFormManager(GovdocFormManager govdocFormManager) {
		this.govdocFormManager = govdocFormManager;
	}
	public void setGovdocWorkflowManager(GovdocWorkflowManager govdocWorkflowManager) {
		this.govdocWorkflowManager = govdocWorkflowManager;
	}
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	public void setGovdocPishiManager(GovdocPishiManager govdocPishiManager) {
		this.govdocPishiManager = govdocPishiManager;
	}
	public void setCollaborationApi(CollaborationApi collaborationApi) {
        this.collaborationApi = collaborationApi;
    }

	public WorkflowApiManager getWapi() {
		return wapi;
	}

	public void setWapi(WorkflowApiManager wapi) {
		this.wapi = wapi;
	}

}
