package com.seeyon.apps.xkjt.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.xkjt.manager.XkjtManager;
import com.seeyon.apps.xkjt.po.XkjtLeaderBanJie;
import com.seeyon.apps.xkjt.po.XkjtLeaderDaiYue;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.content.affair.AffairManager;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocManager;

public class XkjtController extends BaseController {
	public static Log log = LogFactory.getLog(XkjtController.class);
	public ModelAndView allRoles(HttpServletRequest request,HttpServletResponse response){
		User user = AppContext.getCurrentUser();
		ModelAndView mav = new ModelAndView("plugin/xkjt/member2role");
		mav.addObject("accountId", user.getAccountId());
		return mav;
	}
	public ModelAndView initList(HttpServletRequest request,HttpServletResponse response){
		XkjtManager xkjtManager = (XkjtManager) AppContext.getBean("xkjtManager");
		User user = AppContext.getCurrentUser();
		OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
		
		/**项目：徐州矿物集团【待阅栏目：因使用的是JDBCAgent，所以将返回值改为Object】 作者：wxt.xiangrui 时间：2019-6-3 start*/
		List<Object> xkjtLeaderDaiYues = new ArrayList<Object>();
		/**项目：徐州矿物集团【待阅栏目：因使用的是JDBCAgent，所以将返回值改为Object】 作者：wxt.xiangrui 时间：2019-6-3 end*/
		try {
			xkjtLeaderDaiYues = xkjtManager.findXkjtLeaderDaiYueByMemberId(user.getId());
		} catch (BusinessException e) {
			log.error("chenq----获取待阅出错", e);
			
		}
		ModelAndView mav = new ModelAndView("plugin/xkjt/dai_yue");
		mav.addObject("xkjtLeaderDaiYues", xkjtLeaderDaiYues);
		return mav;
	}
	public ModelAndView daiYueMore(HttpServletRequest request,HttpServletResponse response){
		ModelAndView mav = new ModelAndView("plugin/xkjt/dai_yue_more");
		XkjtManager xkjtManager = (XkjtManager) AppContext.getBean("xkjtManager");
		FlipInfo fi = new FlipInfo();
		Map params = new HashMap();
		try {
			xkjtManager.findMoreXkjtLeaderDaiYue(fi, params);
			request.setAttribute("ffdaiYueTable", fi);
		} catch (BusinessException e) {
			log.error("chenq--初始化更多待阅数据异常", e);
		}
		return mav;
	}
	public ModelAndView initYiYueList(HttpServletRequest request,HttpServletResponse response){
		XkjtManager xkjtManager = (XkjtManager) AppContext.getBean("xkjtManager");
		User user = AppContext.getCurrentUser();
		OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
		
		List<XkjtLeaderDaiYue> xkjtLeaderYiYues = new ArrayList<XkjtLeaderDaiYue>();
		try {
			xkjtLeaderYiYues = xkjtManager.findXkjtLeaderYiYueByMemberId(user.getId());
		} catch (BusinessException e) {
			log.error("chenq----获取已阅数据失败", e);
			
		}
		ModelAndView mav = new ModelAndView("plugin/xkjt/yi_yue");
		mav.addObject("xkjtLeaderYiYues", xkjtLeaderYiYues);
		return mav;
	}
	public ModelAndView yiYueMore(HttpServletRequest request,HttpServletResponse response){
		ModelAndView mav = new ModelAndView("plugin/xkjt/yi_yue_more");
		XkjtManager xkjtManager = (XkjtManager) AppContext.getBean("xkjtManager");
		FlipInfo fi = new FlipInfo();
		Map params = new HashMap();
		try {
			xkjtManager.findMoreXkjtLeaderYiYue(fi, params);
			request.setAttribute("ffyiYueTable", fi);
		} catch (BusinessException e) {
			log.error("chenq--初始化更多已阅数据失败", e);
		}
		return mav;
	}
	
	/**项目：徐州矿物集团【办结栏目】 作者：wxt.xiangrui 时间：2019-5-29 start*/
	public ModelAndView initBanJieList(HttpServletRequest request,HttpServletResponse response){
		XkjtManager xkjtManager = (XkjtManager) AppContext.getBean("xkjtManager");
		User user = AppContext.getCurrentUser();
		OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
		//徐矿 zelda 办结增加  组合来源 - 模板选择 zelda 2019年12月6日21:23:52 start
		String templetIds = request.getParameter("templetIds");
		//徐矿 zelda 办结增加  组合来源 - 模板选择 zelda 2019年12月6日21:23:52 end
		List<Object> xkjtLeaderBanJies =new ArrayList<Object>();
		try {
			xkjtLeaderBanJies = xkjtManager.findXkjtLeaderBanJieByMemberId(user.getId(), templetIds);
		} catch (BusinessException e) {
			log.error("chenq----获取办结出错", e);
		}
		ModelAndView mav = new ModelAndView("plugin/xkjt/ban_jie");
		mav.addObject("xkjtLeaderBanJies", xkjtLeaderBanJies);
		return mav;
	}
	/**项目：徐州矿物集团【办结栏目】 作者：wxt.xiangrui 时间：2019-5-29 end*/
	
	/**项目：徐州矿物集团【办结栏目更多页】 作者：wxt.xiangrui 时间：2019-5-29 start*/
	public ModelAndView banJieMore(HttpServletRequest request,HttpServletResponse response){
		ModelAndView mav = new ModelAndView("plugin/xkjt/ban_jie_more");
		XkjtManager xkjtManager = (XkjtManager) AppContext.getBean("xkjtManager");
		FlipInfo fi = new FlipInfo();
		Map parameterMap=request.getParameterMap();
		Map params = new HashMap();
		//项目 徐矿 办结栏目更多根据模板id查询   zelda 2019年12月7日11:09:24 start
		String templetIds = String.valueOf(request.getParameter("templetIds"));
		if(Strings.isNotBlank(templetIds) && !"null".equals(templetIds)) {
			params.put("templetIds", templetIds);
			mav.addObject("templetIds", templetIds);
		}else {
			mav.addObject("templetIds", "");
		}
		//项目 徐矿 办结栏目更多根据模板id查询   zelda 2019年12月7日11:09:24 end
		/*
		 * try { xkjtManager.findMoreXkjtLeaderBanJie(fi, params);
		 * request.setAttribute("ffBanJieTable", fi);
		 * 
		 * } catch (BusinessException e) { log.error("chenq--初始化更多已阅数据失败", e); }
		 */
		return mav;
	}
	/**项目：徐州矿物集团【办结栏目更多页】 作者：wxt.xiangrui 时间：2019-5-29 end*/
		public ModelAndView showPdf(HttpServletRequest request,HttpServletResponse response){
		ModelAndView mav = new ModelAndView("plugin/xkjt/webcontent");
		String fileId = request.getParameter("fileId");
		FileManager fileManager = (FileManager) AppContext.getBean("fileManager");
		try {
			if (Strings.isNotBlank(fileId) && fileManager != null)  {
				V3XFile file = fileManager.getV3XFile(Long.valueOf(fileId));
				mav.addObject("xkjtFileId", file.getId());
				mav.addObject("xkjtCreateDate", file.getCreateDate());
			}
		} catch (Exception e) {
			
		}
		return mav;
	}
	

}
