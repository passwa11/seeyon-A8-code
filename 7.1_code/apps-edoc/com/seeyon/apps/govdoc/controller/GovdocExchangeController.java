package com.seeyon.apps.govdoc.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.edoc.constants.EdocConstant.flowState;
import com.seeyon.apps.govdoc.constant.GovdocEnum;
import com.seeyon.apps.govdoc.manager.GovdocExchangeManager;
import com.seeyon.apps.govdoc.po.GovdocExchangeMain;
import com.seeyon.apps.govdoc.po.JointlyIssyedVO;
import com.seeyon.apps.govdoc.util.GovdocParamUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.plugin.PluginDefinition;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.MemberRole;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;

public class GovdocExchangeController extends BaseController {
	
	private OrgManager orgManager;
	private GovdocExchangeManager govdocExchangeManager;

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	public void setGovdocExchangeManager(GovdocExchangeManager govdocExchangeManager) {
		this.govdocExchangeManager = govdocExchangeManager;
	}

	public ModelAndView exchangeIndex(HttpServletRequest request, HttpServletResponse response) throws Exception{
		ModelAndView mav = new ModelAndView("govdoc/list/exchange_index");
		
		List<MemberRole> roles = orgManager.getMemberRoles(AppContext.currentUserId(), AppContext.currentAccountId());
		for (MemberRole memberRole : roles) {
			String roleCode = memberRole.getRole().getCode();
			if(OrgConstants.Role_NAME.AccountGovdocRec.name().equals(roleCode)
					|| OrgConstants.Role_NAME.DepartmentGovdocRec.name().equals(roleCode)){
				mav.addObject("isSigner",true);
			}
			if(OrgConstants.Role_NAME.AccountGovdocSend.name().equals(roleCode)
					|| OrgConstants.Role_NAME.DepartmentGovdocSend.name().equals(roleCode)){
				mav.addObject("isSender",true);
			}
			if(OrgConstants.Role_NAME.RecEdocBack.name().equals(roleCode)){
				mav.addObject("isBacker",true);
			}
		}
		return mav;
	}
	
	//交换详情页面
	public ModelAndView showExchangeSendState(HttpServletRequest request, HttpServletResponse response) throws Exception{
		ModelAndView modelAndView = new ModelAndView("govdoc/exchange/showExchangeSendState");
    	String summaryId = request.getParameter("summaryId");
    	String status = request.getParameter("status");
    	Integer type = GovdocParamUtil.getInteger(request, "exchangeType");
		type = type == null ? GovdocExchangeMain.EXCHANGE_TYPE_JIAOHUAN : type;
    	GovdocExchangeMain main = govdocExchangeManager.findBySummaryId(Long.valueOf(summaryId), type);
		Map<String, String> conditionMap = new HashMap<String, String>();
		FlipInfo fi = new FlipInfo();
		if(main != null){
			conditionMap.put("mainId", main.getId().toString());
			if(Strings.isNotBlank(status)){
				conditionMap.put("status", status);
			}
			modelAndView.addObject("govdocExchangeMainId", main.getId());
			fi.setSize(-1);
			fi = govdocExchangeManager.findGovdocExchangeDetail(fi, conditionMap);
		}
		request.setAttribute("ffexchangeDetail", fi);
		PluginDefinition definition = SystemEnvironment.getPluginDefinition("govdoc");
		boolean chuantouchakan1 =  "true".equals(definition.getPluginProperty("govdoc.chuantouchakan1"));
		boolean chuantouchakan2 = "true".equals(definition.getPluginProperty("govdoc.chuantouchakan2"));
		modelAndView.addObject("chuantouchakan", (chuantouchakan1 && type == GovdocExchangeMain.EXCHANGE_TYPE_JIAOHUAN) || (chuantouchakan2 && type == GovdocExchangeMain.EXCHANGE_TYPE_ZHUANSHOUWEN));
		modelAndView.addObject("isZhuanShouWen", type == GovdocExchangeMain.EXCHANGE_TYPE_ZHUANSHOUWEN);
		return modelAndView;
	}
	
	//交换流程日志页面
	public ModelAndView showProcessLog(HttpServletRequest request, HttpServletResponse response) throws Exception{
		ModelAndView modelAndView = new ModelAndView("govdoc/exchange/showProcessLog");
    	String detailId = request.getParameter("detailId");
		Map<String, String> conditionMap = new HashMap<String, String>();
    	conditionMap.put("detailId", detailId);
    	FlipInfo flipInfo = new FlipInfo();
        flipInfo.setParams(conditionMap);
    	flipInfo = govdocExchangeManager.findGovdocExchangeDetailLog(flipInfo, conditionMap);
    	request.setAttribute("fflogDetail", flipInfo);
    	modelAndView.addObject("from",request.getParameter("from"));
    	return modelAndView;
	}
	
	//发文穿透查看收文信息
	public ModelAndView exchangeRecInfo(HttpServletRequest request, HttpServletResponse response) throws Exception{
		ModelAndView modelAndView = new ModelAndView("govdoc/exchange/exchangeRecInfo");
		String mainId = request.getParameter("mainId");
		Map<String, String> conditionMap = new HashMap<String, String>();
		conditionMap.put("mainId", mainId);
		FlipInfo flipInfo = new FlipInfo();
		flipInfo.setParams(conditionMap);
		flipInfo = govdocExchangeManager.findGovdocExchangeDetail(flipInfo, conditionMap);
		request.setAttribute("ffexchangeRecInfo", flipInfo);
		return modelAndView;
	}
	/**
     * 显示催办确认页面
     */
    public ModelAndView showPressDialog(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
         ModelAndView mav = new ModelAndView("govdoc/exchange/repealPressDialog");
         return mav;
    }
	
    /**
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView jointlyIssuedDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("govdoc/jointlyIssued/jointlyIssued_detail");
		long summaryId = Long.parseLong(request.getParameter("summaryId"));
		long affairId = Long.parseLong(request.getParameter("affairId"));
		boolean canOpe = false;
		try {
			AffairManager affairManager = (AffairManager) AppContext.getBean("affairManager");
			String policy = affairManager.get(affairId).getNodePolicy();
			PermissionManager permissionManager = (PermissionManager) AppContext.getBean("permissionManager");
			List<String> rList = permissionManager.getActionList(EnumNameEnum.edoc_new_send_permission_policy.name(), policy, AppContext.currentAccountId());
			for (String s : rList) {
				if ("JointlyIssued".equalsIgnoreCase(s)) {
					canOpe = true;
					break;
				}
			}

		} catch (Exception e) {
		}
		mav.addObject("summaryId",summaryId);
		List<JointlyIssyedVO> list = govdocExchangeManager.findMainBySummaryId4Lianhe(summaryId, true);
		//如果查询出来第一条是协办流程，那么当前打开的就是主办流程
		//反之，查询出来第一条是主办流程，那么当前打开的就是协办流程
		boolean isZhuban= list.get(0).isSendFlow();
		String chexiaoStr = "<span class=\"color_blue\"><a href='javascript:void(0)' onclick='revoke(\"summaryId\",\"processId\",\"affairId\",\"affairApp\")'>" + "【撤销】" + "</a></span>";
		String chongfaStr = "<span class=\"color_blue\"><a href='javascript:void(0)' onclick='reSend(\"detailId\")'>" + "【重发】</a></span>";
		List<JointlyIssyedVO> listResult = new ArrayList<JointlyIssyedVO>();
		for (JointlyIssyedVO vo : list) {
			String action = "";
			// 发送错误，回退 撤销的 都可以重发
			if (vo.getState() == GovdocEnum.ExchangeDetailStatus.waitSend.getKey() || vo.getState() == GovdocEnum.ExchangeDetailStatus.hasBack.getKey()) {
				action = chongfaStr.replace("detailId", String.valueOf(vo.getExchangeDetailId()));
			} else if (vo.getState() == GovdocEnum.ExchangeDetailStatus.hasCancel.getKey()) {
				action = chongfaStr.replace("detailId", String.valueOf(vo.getExchangeDetailId()));
			} else {
				if (vo.getSummaryState() == flowState.finish.ordinal() || vo.getSummaryState() == flowState.terminate.ordinal()) {
				} else if (vo.getSummaryState() == flowState.run.ordinal()) {
					action = chexiaoStr.replace("summaryId", String.valueOf(vo.getSummaryId()));
					action = action.replace("processId", String.valueOf(vo.getProcessId()));
					action = action.replace("affairId", String.valueOf(vo.getAffairId()));
					action = action.replace("affairApp", String.valueOf(vo.getAffairApp()));
				}
			}
			vo.setOper_str("");
			//如果当前公文是主办单位公文
			if (isZhuban) {
				vo.setOper_str(vo.getOper_str() + "    " + action);
			}
			if (!canOpe
					&& vo.getAffairId() != -1
					&& (vo.getState() == GovdocEnum.ExchangeDetailStatus.waitSend.getKey()
							|| vo.getState() == GovdocEnum.ExchangeDetailStatus.hasCancel.getKey() || vo.getState() == GovdocEnum.ExchangeDetailStatus.hasBack
							.getKey())) {
				continue;
			}
			if (vo.getAffairId() == -1
					&& (vo.getState() == GovdocEnum.ExchangeDetailStatus.waitSend.getKey()
							|| vo.getState() == GovdocEnum.ExchangeDetailStatus.hasCancel.getKey() || vo.getState() == GovdocEnum.ExchangeDetailStatus.hasBack
							.getKey())) {
				//continue;
			}
			listResult.add(vo);
		}
		
		mav.addObject("canOpe", canOpe);// 是否能进行操作
		mav.addObject("lists", listResult);
		return mav;
	}
}
