package com.seeyon.ctp.portal.controller;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.manager.PendingManager;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.track.bo.TrackAjaxTranObj;
import com.seeyon.ctp.common.track.manager.CtpTrackMemberManager;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.ReqUtil;
import com.seeyon.ctp.util.Strings;

public class PortalAffairController extends BaseController {

	private EnumManager enumManagerNew;

    private PendingManager pendingManager;

    private AffairManager affairManager;

    private CtpTrackMemberManager trackManager;

	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	public void setTrackManager(CtpTrackMemberManager trackManager) {
		this.trackManager = trackManager;
	}
	public void setPendingManager(PendingManager pendingManager) {
		this.pendingManager = pendingManager;
	}
	public void setEnumManagerNew(EnumManager enumManager) {
		this.enumManagerNew = enumManager;
	}
	/**
     * 流程分类
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView showPortalCatagory(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("apps/collaboration/showPortalCatagory");

        String category = ReqUtil.getString(request, "category", "");
        if (Strings.isNotBlank((category))) {
            mav = new ModelAndView("apps/collaboration/showPortalCatagory4MyTemplate");
        }
        return mav;
    }
    /**
     * 流程状态
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView showPortalProcessState(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("apps/collaboration/showPortalProcessState");
        String openFrom = request.getParameter("openFrom");
        request.setAttribute("from", request.getParameter("from"));
        request.setAttribute("openFrom", openFrom);
        return mav;
    }
    /**
     * portal显示重要程度的页面
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView showPortalImportLevel(HttpServletRequest request,HttpServletResponse response) throws Exception{
        ModelAndView mav = new ModelAndView("apps/collaboration/showPortalImportLevel");
        List<CtpEnumItem> secretLevelItems =  enumManagerNew.getEnumItems(EnumNameEnum.edoc_urgent_level);
        WFComponentUtil.putImportantI18n2Session();
        mav.addObject("hasEdoc", AppContext.hasPlugin("edoc"));
        mav.addObject("itemCount", secretLevelItems.size());
        return mav;
    }

    /**
     * 已发事项 栏目 【更多】
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView moreSent(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("apps/collaboration/moreSent");
        FlipInfo fi = new FlipInfo();
        String fragmentId = request.getParameter("fragmentId");
        String ordinal = request.getParameter("ordinal");
        String columnsName = ReqUtil.getString(request, "columnsName");
        Map<String,Object> query = new HashMap<String,Object>();
        query.put("fragmentId", fragmentId);
        query.put("ordinal", ordinal);
        query.put("state", StateEnum.col_sent.key());
        query.put("isTrack", false);
        query.put("isFromMore", true);

        WFComponentUtil.putImportantI18n2Session();


        this.pendingManager.getMoreList4SectionContion(fi, query);
        request.setAttribute("ffmoreList", fi);
        modelAndView.addObject("total", fi.getTotal());
        fi.setParams(query);
        modelAndView.addObject("params", query);
        modelAndView.addObject("columnsName", columnsName);
        return modelAndView;
    }
    /**
     * 已办事项 栏目 【更多】
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView moreDone(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("apps/collaboration/moreDone");
        FlipInfo fi = new FlipInfo();
        String fragmentId = request.getParameter("fragmentId");
        String ordinal = request.getParameter("ordinal");
        String rowStr = request.getParameter("rowStr");
        String columnsName = ReqUtil.getString(request, "columnsName");
        String isGroupBy = request.getParameter("isGroupBy");
        if (Strings.isBlank(isGroupBy)) {
            isGroupBy = "false";
        }
        String section = "doneSection";
        Map<String,Object> query = new HashMap<String,Object>();
        query.put("fragmentId", fragmentId);
        query.put("ordinal", ordinal);
        query.put("state", StateEnum.col_done.key());
        query.put("isTrack", false);
        query.put("section", section);
        query.put("isGroupBy", isGroupBy);
        query.put("isFromMore", true);

        WFComponentUtil.putImportantI18n2Session();

        this.pendingManager.getMoreList4SectionContion(fi, query);
        modelAndView.addObject("total", fi.getTotal());
        request.setAttribute("ffmoreList", fi);
        fi.setParams(query);
        modelAndView.addObject("section", section);
        modelAndView.addObject("params", query);
        String sub="sendUser,";

        String p1=rowStr.substring(0,rowStr.indexOf(sub)+sub.length())+"sendUserUnit,"+rowStr.substring(rowStr.indexOf(sub)+sub.length());
        modelAndView.addObject("rowStr", p1);
        modelAndView.addObject("columnsName", columnsName);
        modelAndView.addObject("isGroupBy",isGroupBy);
        return modelAndView;
    }
    /**
     * 待发事项 栏目 【更多】
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView moreWaitSend(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("apps/collaboration/moreWaitSend");
        FlipInfo fi = new FlipInfo();
        String fragmentId = request.getParameter("fragmentId");
        String ordinal = request.getParameter("ordinal");
        String rowStr = request.getParameter("rowStr");
        String columnsName = ReqUtil.getString(request, "columnsName");
        Map<String,Object> query = new HashMap<String,Object>();
        query.put("fragmentId", fragmentId);
        query.put("ordinal", ordinal);
        query.put("state", StateEnum.col_waitSend.key());
        query.put("isTrack", false);
        query.put("isFromMore", true);

        WFComponentUtil.putImportantI18n2Session();


        this.pendingManager.getMoreList4SectionContion(fi, query);
        modelAndView.addObject("total", fi.getTotal());
        modelAndView.addObject("rowStr", rowStr);
        request.setAttribute("ffmoreList", fi);
        fi.setParams(query);
        modelAndView.addObject("params", query);
        modelAndView.addObject("columnsName", columnsName);
        return modelAndView;
    }


    /**
     * 跟踪事项 栏目 【更多】
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView moreTrack(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("apps/collaboration/moreTrack");
        FlipInfo fi = new FlipInfo();
        String rowStr = request.getParameter("rowStr");
        String fragmentId = request.getParameter("fragmentId");
        String ordinal = request.getParameter("ordinal");
        String columnsName = ResourceUtil.getString("collaboration.protal.track.label");
        modelAndView.addObject("columnsName", columnsName);
        Map<String,Object> query = new HashMap<String,Object>();
        query.put("fragmentId", fragmentId);
        query.put("ordinal", ordinal);
        query.put("isTrack", true);
        query.put("isFromMore", true);
        query.put("showCurNodesInfo",true);

        WFComponentUtil.putImportantI18n2Session();


        this.pendingManager.getMoreList4SectionContion(fi, query);
        request.setAttribute("ffmoreList", fi);
        fi.setParams(query);
        modelAndView.addObject("params", query);
        modelAndView.addObject("total", fi.getTotal());
        modelAndView.addObject("rowStr", rowStr);
        return modelAndView;
    }

    /**
     * 取消跟踪事项 栏目 【更多】
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView cancelTrack(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map args = ParamUtil.getJsonParams();
        String affairId = (String) args.get("affairId");
        TrackAjaxTranObj obj = new TrackAjaxTranObj();
        obj.setAffairId(affairId);
        this.transCancelTrack(obj);
        String fragmentId = (String) args.get("fragmentId");
        String ordinal = (String) args.get("ordinal");
        String rowStr = request.getParameter("rowStr");
        String columnsName = request.getParameter("columnsName");
        String s = URLEncoder.encode(columnsName);
        return this.redirectModelAndView("/portalAffair/portalAffairController.do?method=moreTrack&fragmentId="+fragmentId+"&ordinal="+ordinal+"&rowStr="+rowStr+"&columnsName=" + s);
    }

    private void transCancelTrack(TrackAjaxTranObj obj) throws BusinessException {
        Object obj1 = obj.getAffairId();
        if(obj1 != null){
            String[] affairIds = obj1.toString().split("[,]");
            for(int i=0;i<affairIds.length;i++){
                Map<String,Object> columnValue = new HashMap<String,Object>();
                columnValue.put("track", 0);
                //更新affair事项表状态
                Long affairId = Long.parseLong(affairIds[i]);
                this.affairManager.update(affairId, columnValue);
                trackManager.deleteTrackMembers(null, affairId);
            }
        }
    }
}
