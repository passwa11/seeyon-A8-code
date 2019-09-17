package com.seeyon.apps.govdoc.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Predicate;
import com.seeyon.apps.govdoc.manager.GovdocPishiManager;
import com.seeyon.apps.govdoc.po.GovdocLeaderSerialShortname;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.UUIDLong;

/**
 * 领导批示编号控制器
 * 
 * @author rz
 * 
 */
public class GovdocLeaderPishiController extends BaseController {
	private static final Log LOGGER = CtpLogFactory.getLog(GovdocLeaderPishiController.class);
	
	private GovdocPishiManager govdocPishiManager;
	
	public void setGovdocPishiManager(GovdocPishiManager govdocPishiManager) {
		this.govdocPishiManager = govdocPishiManager;
	}
	/**
	 * 领导批示编号设置
	 * @author rz
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView addOrEditShortName(HttpServletRequest request,HttpServletResponse response)throws Exception{
		ModelAndView mav = new ModelAndView("govdoc/database/leaderpishi/create");
		String id = request.getParameter("id");
		String flag = request.getParameter("flag");
		mav.addObject("flag",flag);
		GovdocLeaderSerialShortname govdocLSS = null;
		if(id != null && !"".equals(id)){
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", Long.valueOf(id));
			List<GovdocLeaderSerialShortname> list = govdocPishiManager.getGovdocLSSs(null, map);
			if(!list.isEmpty()){
				govdocLSS = list.get(0);
			}
			if(govdocLSS != null ){
				mav.addObject("id",govdocLSS.getId());
				mav.addObject("leaderId",govdocLSS.getLeaderId());
				mav.addObject("leaderName",govdocLSS.getLeaderName());
				mav.addObject("shortName",govdocLSS.getShortName());
				mav.addObject("orgAccountId",govdocLSS.getOrgAccountId());
			}			
		}
        return mav;
	}
	
	/**
	 * 新增/修改批示编号简称
	 * @author rz
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView saveOrEditLeaderShortName(HttpServletRequest request,HttpServletResponse response)throws Exception{
		try {
			User user = AppContext.getCurrentUser();
			// 获取前端参数
			String id =  request.getParameter("id");
			String flag =  request.getParameter("flag");
			String sLeaderId =  request.getParameter("leaderId");
			String leaderName =  request.getParameter("leaderName");
			String shortName =  request.getParameter("shortName");
			String sOrgAccountId =  request.getParameter("orgAccountId");
			if(leaderName == null || "".equals(leaderName)){
				leaderName =  request.getParameter("oldLeaderName");
			}
			if(leaderName != null && !"".equals(leaderName)){
				Long orgAccountId = null;
				Long leaderId = null;
				if(sOrgAccountId != null && !"".equals(sOrgAccountId)){
					orgAccountId = Long.valueOf(sOrgAccountId);
				}
				if(sLeaderId != null && !"".equals(sLeaderId)){
					leaderId = Long.valueOf(sLeaderId);
				}
				// 构建 批示编号简称
				GovdocLeaderSerialShortname govdocLSS = new GovdocLeaderSerialShortname();
				if(id != null && "edit".equals(flag) ){
					govdocLSS.setId(Long.valueOf(id));
				}else{
					govdocLSS.setId(UUIDLong.longUUID());
				}
				if("overlay".equals(flag) && leaderId!=null && !"".equals(leaderId.toString())){
					govdocPishiManager.deleteLSSByLeaderId(leaderId, orgAccountId);
				}
				govdocLSS.setLeaderId(leaderId);
				govdocLSS.setLeaderName(leaderName);
				govdocLSS.setShortName(shortName);
				govdocLSS.setIsUsable(true);
				govdocLSS.setOrgAccountId(orgAccountId);
				govdocPishiManager.saveOrEditLeaderShortName(govdocLSS);
			}
		} catch(Exception e) {
			LOGGER.error("添加保存错误", e);
		}
		return null;
	}
}
