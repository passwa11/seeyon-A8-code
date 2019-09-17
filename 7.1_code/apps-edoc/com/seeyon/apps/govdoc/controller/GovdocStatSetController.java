package com.seeyon.apps.govdoc.controller;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.govdoc.manager.GovdocStatSetManager;
import com.seeyon.apps.govdoc.po.EdocStatSet;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.util.Strings;

/**
 * 新公文统计设置控制器
 * @author 唐桂林
 *
 */
public class GovdocStatSetController extends BaseController {
	
	//private static final Log LOGGER = CtpLogFactory.getLog(GovdocStatSetController.class);
	
	private GovdocStatSetManager govdocStatSetManager;

	/**
	 * 打开节点权限选择窗口
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView mtechEdocFromFlowPermFrame(HttpServletRequest request,HttpServletResponse response)throws Exception{
		String type = request.getParameter("type");
		if("0".equals(type)){  
			type = "edoc_new_send_permission_policy";
		}else{
			type = "edoc_new_rec_permission_policy";
		}
		ModelAndView mav = new ModelAndView("/common/statistics/mtechEdocFromFlowPermFrame");
		mav.addObject("type", type);
		
		mav.addObject("url", "/seeyon/govdoc/statset.do?method=mtechEdocFromFlowPermList&type="+type+"&node="+URLEncoder.encode(request.getParameter("node"),"UTF-8"));
		return mav;
	}
	
	/**
     * 获取节点权限列表
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
	public ModelAndView mtechEdocFromFlowPermList(HttpServletRequest request,HttpServletResponse response)throws Exception{
		String type = request.getParameter("type");
		ModelAndView mav = new ModelAndView("/common/statistics/mtechEdocFromFlowPermList");
		mav.addObject("type", type);
		mav.addObject("node", request.getParameter("node"));
		return mav;
	}
	
	/**
	 * 公文统计授权
	 */
	public ModelAndView list(HttpServletRequest request,HttpServletResponse response)throws Exception{
		String type="state_type";
		if(Strings.isBlank(type)){
			return null;
		}
		ModelAndView mav = new ModelAndView("govdoc/database/statset/list");
		String edocStatSetTitle="签收统计";
		mav.addObject("type", type);
		mav.addObject("edocStatSetTitle", edocStatSetTitle);
		//添加左侧树
		List<EdocStatSet> resultlist = new ArrayList<EdocStatSet>();
		
		EdocStatSet signRoot = new EdocStatSet();
		signRoot.setId(2L);
		signRoot.setParentId(1L);
		signRoot.setName("交换统计");
		resultlist.add(signRoot);
		EdocStatSet workRoot = new EdocStatSet();
		workRoot.setId(3L);
		workRoot.setParentId(1L);
		workRoot.setName("工作统计");
		resultlist.add(workRoot);
		EdocStatSet reportRoot = new EdocStatSet();
		reportRoot.setId(4L);
		reportRoot.setParentId(1L);
		reportRoot.setName("自定义查询统计");
		resultlist.add(reportRoot);
		request.setAttribute("ffdeptTree", resultlist);
		
		//验证预制数据
		govdocStatSetManager.checkStatInitData();
		
        return mav;
	}

	/**
	 * 公文统计授权
	 */
	public ModelAndView govdocReportSet(HttpServletRequest request,HttpServletResponse response)throws Exception{
		ModelAndView mav = new ModelAndView("govdoc/database/statset/govdocReportSet");
		return mav;
	}
	public void setGovdocStatSetManager(GovdocStatSetManager govdocStatSetManager) {
		this.govdocStatSetManager = govdocStatSetManager;
	}

}
