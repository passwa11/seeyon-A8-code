package com.seeyon.ctp.portal.portlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.govdoc.helper.GovdocHelper;
import com.seeyon.apps.govdoc.manager.GovdocListManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.portal.portlet.PortletConstants.PortletCategory;
import com.seeyon.ctp.portal.portlet.PortletConstants.PortletSize;
import com.seeyon.ctp.portal.portlet.PortletConstants.UrlType;
import com.seeyon.ctp.util.FlipInfo;

public class EdocPortlet implements BasePortlet {
	
	private static final Log LOGGER = LogFactory.getLog(EdocPortlet.class);

	private SuperviseManager superviseManager;
	private GovdocListManager govdocListManager;
	
	@Override
	public String getId() {
		return "edocPortlet";
	}

	@Override
	public List<ImagePortletLayout> getData() {
		List<ImagePortletLayout> layouts = new ArrayList<ImagePortletLayout>();
		//发文管理
		layouts.add(this.getSendEdocManagerPortlet());
		//收文管理
		layouts.add(this.getRecManagerPortlet());
		if(!(Boolean)SysFlag.sys_isG6S.getFlag()){
			//签报管理
			layouts.add(this.getSignReportPortlet());
			//签报拟文
			layouts.add(this.getSignReportNewPortlet());
			//客开 项目名称： 作者：fzc 修改日期：2018-3-28 [修改功能：]start
            //公文查询
            layouts.add(this.getEdocQueryPortlet());
            //公文统计
            layouts.add(this.getEdocStatisticsPortlet());
			//客开 项目名称： 作者：fzc 修改日期：2018-3-28 [修改功能：]end
		}
		//公文交换
		layouts.add(this.getEdocExchangePortlet());
		//公文督办
		layouts.add(this.getEdocSupervisePortlet());
		//收文登记
		layouts.add(this.getEdocRecRegisterPortlet());
		//公文
		layouts.add(this.getEdocPortlet());
		//发文拟文
		layouts.add(this.getEdocSendNewPortletPortlet());
		//待办公文
		layouts.add(this.getPendingEdocPortlet());
		return layouts;
	}
	
	@Override
	public ImagePortletLayout getPortlet(String portletId) {
		List<ImagePortletLayout> layouts = this.getData();
		if(CollectionUtils.isNotEmpty(layouts)){
			for(ImagePortletLayout layout : layouts){
				if(portletId.equals(layout.getPortletId())){
					return layout;
				}
			}
		}
		return null;
	}


	/*@Override
	public int getDataCount(String portletId) {
		try {
			User user = AppContext.getCurrentUser();
			
			//发文、收文、签报待办数
	        if("sendManagerPortlet".equals(portletId) || "recManagerPortlet".equals(portletId) ||"signReportPortlet".equals(portletId)){
	        	int pendingCount=0;
	        	 String listType = "listPendingAll" ;
	 		    int type = EdocNavigationEnum.EdocV5ListTypeEnum.getEnumByKey(listType).getType();
	 		    Map<String, Object> condition = new HashMap<String, Object>();
	 		    int edocType=0;
	 		    if("sendManagerPortlet".equals(portletId)){
	 		    	edocType=0;
	 		    }else if("recManagerPortlet".equals(portletId)){
	 		    	edocType=1;
	 		    }else if("signReportPortlet".equals(portletId)){
	 		    	edocType=2;
	 		    }
	 		    condition.put("edocType", edocType);
	 		    condition.put("state", StateEnum.col_pending.key());
	 	        condition.put("user", user);
	 	        condition.put("listType","listPending");
	 	        condition.put("userId", user.getId()); 
	 			condition.put("accountId", user.getLoginAccount());//不需要进行分页查询，
	 			pendingCount  = edocListManager.findEdocPendingCount(type, condition);
	 			return  pendingCount;
	        }else if("edocExchangePortlet".equals(portletId)){ //待交换数据（待发送和待签收）的和
	        	int count1 = 0;
	        	int count2 = 0;
	        	Map<String, Object> condition = new HashMap<String, Object>();
	        	String listType="listExchangeToSend";
	    		condition.put("listType", "listExchangeToSend");
	    		condition.put("modelType", "toSend");
	    		condition.put("user", user);
	    		int type = EdocNavigationEnum.EdocV5ListTypeEnum.getTypeName(listType);
	    		count1 =  edocExchangeManager.findEdocExchangeRecordCount(type, condition);
	        	listType="listExchangeToRecieve";
	    		condition.put("listType", "listExchangeToRecieve");
	    		condition.put("modelType", "toSend");
	    		condition.put("user", user);
	    		type = EdocNavigationEnum.EdocV5ListTypeEnum.getTypeName(listType);
	    		count2 =  edocExchangeManager.findEdocExchangeRecordCount(type, condition);
	        	return count1+count2;
	        	
	        }else if("edocSupervisePortlet".equals(portletId)){ //公文督办
	        	Map<String, String> query = new HashMap<String, String>();
	        	query.put("app", "4");
	        	int dateCount=0;
	        	
    	        FlipInfo a = superviseManager.getSuperviseList4App(new FlipInfo(), query);
    	        if(a!=null){
    	        	dateCount = a.getTotal();
    	        }
    	        
	        	return dateCount;
	        }
		   
		} catch (BusinessException e) {
			LOGGER.error("首页工作桌面公文查询待办数异常",e);
			return 0;
		}
		
		return -1;
		

	}*/

	/**
	 * 新公文数量统计
	 */
	public int getDataCount(String portletId) {
		try {
			Map<String, String> condition = new HashMap<String, String>();
			condition.put("onlyCount", "true");
			// 发文、收文、签报待办数
			if ("edocPortlet".equals(portletId) || "sendManagerPortlet".equals(portletId) || "recManagerPortlet".equals(portletId) || "signReportPortlet".equals(portletId)) {
				String listType = "listPendingAll";
				String govdocType = null;
				if ("sendManagerPortlet".equals(portletId)) {
					govdocType = "1";
				} else if ("recManagerPortlet".equals(portletId)) {
					govdocType = "2,4";
				} else if ("signReportPortlet".equals(portletId)) {
					govdocType = "3";
				}
				condition.put("govdocType", govdocType);
				condition.put("listType", listType);
				FlipInfo flipInfo = new FlipInfo();
				flipInfo = govdocListManager.findPendingList(flipInfo, condition);
				return flipInfo.getTotal();
			} else if ("edocExchangePortlet".equals(portletId)) { // 待交换数据（待发送和待签收）的和
				condition.put("listType", "listExchangeSendPending");
				FlipInfo flipInfo = new FlipInfo();
				flipInfo = govdocListManager.findPendingList(flipInfo, condition);
				return flipInfo.getTotal();
			} else if ("edocSupervisePortlet".equals(portletId)) { // 公文督办
				Map<String, String> query = new HashMap<String, String>();
				query.put("app", "4");
				int dateCount = 0;

				FlipInfo a = superviseManager.getSuperviseList4App(new FlipInfo(), query);
				if (a != null) {
					dateCount = a.getTotal();
				}

				return dateCount;
			}

		} catch (BusinessException e) {
			LOGGER.error("首页工作桌面公文查询待办数异常", e);
			return 0;
		}

		return -1;

	}
    @Override
    public boolean isAllowDataUsed(String portletId) {
    	//由于G6是没有权限判断的，和杨圆沟通后把权限注释
        //if ("edocPortlet".equals(portletId)) {
        //    return AppContext.hasResourceCode("F07_sendManager") || AppContext.hasResourceCode("F07_recManager") || AppContext.hasResourceCode("F07_signReport");
        //}
        return true;
    }

	@Override
	public boolean isAllowUsed() {
		return true;
	}

	//客开 项目名称： 作者：fzc 修改日期：2018-3-28 [修改功能：]start
    /**
     *公文查询
     * @return
     */
	private ImagePortletLayout getEdocQueryPortlet() {
	    ImagePortletLayout layout = new ImagePortletLayout();
        layout.setResourceCode("F20_search");
        layout.setPluginId("edoc");
        layout.setCategory(PortletCategory.edoc.name());
        layout.setDisplayName("system.menuname.DocSearch");
        layout.setOrder(5);
        layout.setPortletId("edocSearchPortlet");
        layout.setPortletName(ResourceUtil.getString("menu.edoc.query.label"));//公文查询
		layout.setPortletUrl("/govdoc/govdoc.do?method=queryIndex&_resourceCode=F20_search");
        layout.setPortletUrlType(UrlType.workspace.name());
        layout.setSize(PortletSize.middle.ordinal());
        List<ImageLayout> ims = new ArrayList<ImageLayout>();
        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("system.menuname.DocSearch");
        image1.setSummary("");
        image1.setImageUrl("d_docsearch.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
	}

    /**
     * 公文统计
     * @return
     */
	private ImagePortletLayout getEdocStatisticsPortlet() {
	    ImagePortletLayout layout = new ImagePortletLayout();
        layout.setResourceCode("F20_stat");
        layout.setPluginId("edoc");
        layout.setCategory(PortletCategory.edoc.name());
        layout.setDisplayName("system.menuname.DocStatistics");
        layout.setOrder(6);
        layout.setPortletId("edocStatisticsPortlet");
        layout.setPortletName(ResourceUtil.getString("menu.statisticsManager.label"));//公文统计
		layout.setPortletUrl("/govdoc/govdoc.do?method=statIndex&listType=listOldStat&_resourceCode=F20_stat");
        layout.setPortletUrlType(UrlType.workspace.name());
        layout.setSize(PortletSize.middle.ordinal());
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("system.menuname.DocStatistics");
        image1.setSummary("");
        image1.setImageUrl("d_docstatistics.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
	}
	//客开 项目名称： 作者：fzc 修改日期：2018-3-28 [修改功能：]end

	/**
	 * 发文管理
	 * @return
	 */
	private ImagePortletLayout getSendEdocManagerPortlet() {
	    ImagePortletLayout layout = new ImagePortletLayout();
        layout.setResourceCode("F20_govDocSendManage");
        layout.setPluginId("edoc");
        layout.setCategory(PortletCategory.edoc.name());
        layout.setDisplayName("system.menuname.DocDispatch");
        layout.setOrder(1);
        layout.setPortletId("sendManagerPortlet");
        layout.setPortletName(ResourceUtil.getString("menu.sendManager.label"));//发文管理
        //layout.setPortletUrl("/edocController.do?method=entryManager&entry=sendManager");
		layout.setPortletUrl("/govdoc/govdoc.do?method=index&govdocType=1&_resourceCode=F20_govDocSendManage");
        //移动端发文管理磁贴 chenyq 20190305
        layout.setMobileUrl("/seeyon/m3/apps/v5/edoc/html/edocList.html?listType=send");
        layout.setSpaceTypes("mobile_application,m3mobile,weixinmobile");
        //移动端发文管理磁贴 chenyq 20190305
        layout.setPortletUrlType(UrlType.workspace.name());
        layout.setSize(PortletSize.middle.ordinal());
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("system.menuname.DocDispatch");
        image1.setSummary("");
        image1.setImageUrl("d_docdispatch.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
	}
	/**
	 * 发文拟文
	 * @return
	 */
	private ImagePortletLayout getEdocSendNewPortletPortlet() {
		ImagePortletLayout layout = new ImagePortletLayout();
		layout.setPluginId("edoc");
		layout.setCategory(PortletCategory.edoc.name());
		if((Boolean)SysFlag.sys_isG6S.getFlag()){
			layout.setResourceCode("F20_fawenNewQuickSend");
			layout.setDisplayName("system.menuname.quickfawen");
			layout.setPortletUrl("/govdoc/govdoc.do?method=newGovdoc&sub_app=1&isQuickSend=true&_resourceCode=F20_fawenNewQuickSend");
		}else{
			layout.setResourceCode("F20_newSend");
			layout.setDisplayName("system.menuname.edocfawen");
			layout.setPortletUrl("/govdoc/govdoc.do?method=newGovdoc&sub_app=1&_resourceCode=F20_newSend");
		}
		layout.setOrder(3);
		layout.setPortletId("edocSendNewPortlet");
		layout.setPortletName(ResourceUtil.getString("system.menuname.edocfawen"));//发文管理
		layout.setPortletUrlType(UrlType.open.name());
		layout.setSize(PortletSize.middle.ordinal());
		List<ImageLayout> ims = new ArrayList<ImageLayout>();
		ImageLayout image1 = new ImageLayout();
		image1.setImageTitle("system.menuname.edocfawen");
		image1.setSummary("");
		image1.setImageUrl("d_edocfawen.png");
		ims.add(image1);
		layout.setImageLayouts(ims);
		return layout;
	}
	/**
	 * 收文管理
	 * @return
	 */
	private ImagePortletLayout getRecManagerPortlet() {
	    ImagePortletLayout layout = new ImagePortletLayout();
        layout.setResourceCode("F20_receiveManage");
        layout.setPluginId("edoc");
        layout.setCategory(PortletCategory.edoc.name());
        layout.setDisplayName("system.menuname.DocReceiving");
        layout.setOrder(2);
        layout.setPortletId("recManagerPortlet");
        layout.setPortletName(ResourceUtil.getString("menu.receiveManager.label"));//收文管理
        //layout.setPortletUrl("/edocController.do?method=entryManager&entry=recManager");
		layout.setPortletUrl("/govdoc/govdoc.do?method=index&govdocType=2,4&_resourceCode=F20_receiveManage");
        //移动端收文管理磁贴 chenyq 20190305
        layout.setMobileUrl("/seeyon/m3/apps/v5/edoc/html/edocList.html?listType=receive");
        layout.setSpaceTypes("mobile_application,m3mobile,weixinmobile");
        //移动端收文管理磁贴 chenyq 20190305
        layout.setPortletUrlType(UrlType.workspace.name());
        layout.setSize(PortletSize.middle.ordinal());
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("system.menuname.DocReceiving");
        image1.setSummary("");
        image1.setImageUrl("d_docreceiving.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
	}
	/**
	 * 签报拟文
	 * @return
	 */
	private ImagePortletLayout getSignReportNewPortlet() {
	    ImagePortletLayout layout = new ImagePortletLayout();
        layout.setResourceCode("F20_newSign");
        layout.setPluginId("edoc");
        layout.setCategory(PortletCategory.edoc.name());
        layout.setDisplayName("system.menuname.SignReportNew");
        layout.setOrder(3);
        layout.setPortletId("signReportNewPortlet");
        layout.setPortletName(ResourceUtil.getString("system.menuname.SignReportNew"));//签报拟文
        layout.setPortletUrl("/govdoc/govdoc.do?method=newGovdoc&sub_app=3&_resourceCode=F20_newSign");
        layout.setPortletUrlType(UrlType.open.name());
        layout.setSize(PortletSize.middle.ordinal());
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("system.menuname.SignReportNew");
        image1.setSummary("");
        image1.setImageUrl("d_edocqianbao.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
	}
	/**
	 * 签报管理
	 * @return
	 */
	private ImagePortletLayout getSignReportPortlet() {
	    ImagePortletLayout layout = new ImagePortletLayout();
        layout.setResourceCode("F20_signReport");
        layout.setPluginId("edoc");
        layout.setCategory(PortletCategory.edoc.name());
        layout.setDisplayName("system.menuname.SignReceipt");
        layout.setOrder(3);
        layout.setPortletId("signReportPortlet");
        layout.setPortletName(ResourceUtil.getString("menu.signManager.label"));//签报管理
        //layout.setPortletUrl("/edocController.do?method=entryManager&entry=signReport");
        //客开 项目名称： 作者：fzc 修改日期：2018-3-22 [修改功能：首页磁铁]start
        //layout.setPortletUrl("/govDoc/govDocController.do?method=govDocSend&isSignReport=true&_resourceCode=F20_signReport");
        layout.setPortletUrl("/govdoc/govdoc.do?method=index&govdocType=3&_resourceCode=F20_signReport");
		//客开 项目名称： 作者：fzc 修改日期：2018-3-22 [修改功能：首页磁铁]end
        layout.setPortletUrlType(UrlType.workspace.name());
        layout.setSize(PortletSize.middle.ordinal());
        //移动端签报管理磁贴 chenyq 20190305
        layout.setMobileUrl("/seeyon/m3/apps/v5/edoc/html/edocList.html?listType=report");
        layout.setSpaceTypes("mobile_application,m3mobile,weixinmobile");
        //移动端签报管理磁贴 chenyq 20190305
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("system.menuname.SignReceipt");
        image1.setSummary("");
        image1.setImageUrl("d_signreceipt.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
	}
	
	/**
	 * 公文交换 TODO 暂时保存A8
	 * @return
	 */
	private ImagePortletLayout getEdocExchangePortlet() {
	    ImagePortletLayout layout = new ImagePortletLayout();
        layout.setResourceCode("F07_edocExchange");
        layout.setPluginId("edoc");
        layout.setCategory(PortletCategory.edoc.name());
        layout.setDisplayName("system.menuname.DocExchage");
        layout.setOrder(4);
        layout.setPortletId("edocExchangePortlet");
        layout.setPortletName(ResourceUtil.getString("menu.exchangeManager.label"));//公文交换
        layout.setPortletUrl("/exchangeEdoc.do?method=listMainEntry");
        layout.setPortletUrlType(UrlType.workspace.name());
        layout.setSize(PortletSize.middle.ordinal());
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("system.menuname.DocExchage");
        image1.setSummary("");
        image1.setImageUrl("d_docexchage.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
	}
	
	/**
	 * 公文督办
	 * @return
	 */
	private ImagePortletLayout getEdocSupervisePortlet() {
	    ImagePortletLayout layout = new ImagePortletLayout();
        layout.setResourceCode("F20_supervise");
        layout.setPluginId("edoc");
        layout.setCategory(PortletCategory.edoc.name());
        layout.setDisplayName("system.menuname.DocSupervision");
        layout.setOrder(6);
        layout.setPortletId("edocSupervisePortlet");
        layout.setPortletName(ResourceUtil.getString("menu.superviseManager.label"));//公文督办
        //layout.setPortletUrl("/supervise/supervise.do?method=listSupervise&app=4");
		layout.setPortletUrl("/supervise/supervise.do?method=listSupervise&app=4&_resourceCode=F20_supervise");
        layout.setPortletUrlType(UrlType.workspace.name());
        layout.setSize(PortletSize.middle.ordinal());
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("system.menuname.DocSupervision");
        image1.setSummary("");
        image1.setImageUrl("d_docsupervision.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
	}
	
	/**
	 * 收文登记
	 * @return
	 */
	private ImagePortletLayout getEdocRecRegisterPortlet() {
		ImagePortletLayout layout = new ImagePortletLayout();
		if(GovdocHelper.isG6Version()){
			layout.setResourceCode("F20_newDengji");
		}else{
			layout.setResourceCode("F07_recRegister");
		}
        layout.setPluginId("edoc");
        layout.setCategory(PortletCategory.edoc.name());
        layout.setDisplayName("system.menuname.edocdengji");
        layout.setOrder(7);
        layout.setPortletId("edocRecRegisterPortlet");
        layout.setPortletName(ResourceUtil.getString("menu.edocRecNew.label"));//收文登记
        //A8的url
        //String url = "/edocController.do?method=entryManager&entry=recManager&listType=newEdoc&edocType=1&listType=listV5Register";
        //g6的url
        //if(EdocHelper.isG6Version()){
        //	url = "/edocController.do?method=entryManager&entry=recManager&listType=newEdocRegister&comm=create&edocType=1&registerType=2&sendUnitId=-1&registerId=-1&listType=registerPending&recListType=registerPending";
        //}
        //if(EdocSwitchHelper.isOpenRegister()==false){
        //	url="/edocController.do?method=entryManager&entry=recManager&listType=newEdoc&edocType=1";
        //}
		String url="/govdoc/govdoc.do?method=newGovdoc&sub_app=2&_resourceCode=F20_newDengji";//更换为新链接
        layout.setPortletUrl(url);
        layout.setPortletUrlType(UrlType.open.name());
        layout.setSize(PortletSize.middle.ordinal());
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("system.menuname.edocdengji");
        image1.setSummary("");
        image1.setImageUrl("d_edocdengji.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
	}
	
	/**
     * 公文
     * @return
     */
    private ImagePortletLayout getEdocPortlet() {
        ImagePortletLayout layout = new ImagePortletLayout();
        layout.setResourceCode("");
        layout.setPluginId("edoc");
        layout.setCategory("common,edoc");
        layout.setOrder(80);
        layout.setPortletId("edocPortlet");
        layout.setPortletName("公文");
        layout.setDisplayName("pending.edoc.label");
        layout.setNeedNumber(1);
        layout.setPortletUrl("");
        layout.setMobileUrl("/seeyon/m3/apps/v5/edoc/html/edocList.html");
        layout.setPortletUrlType(UrlType.workspace.name());
        layout.setSize(PortletSize.middle.ordinal());
        layout.setSpaceTypes("m3mobile,weixinmobile,mobile_application");
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("pending.edoc.label");
        image1.setSummary("");
        image1.setImageUrl("d_officthemespace.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
    }
    
    /**
     * 待办公文
     * @return
     */
    private ImagePortletLayout getPendingEdocPortlet() {
        ImagePortletLayout layout = new ImagePortletLayout();
        layout.setResourceCode("F20_govDocSendManage,F20_receiveManage,F20_signReport");
        layout.setPluginId("edoc");
        layout.setCategory("edoc");
        layout.setOrder(405);
        layout.setPortletId("pendingEdocPortlet");
        layout.setPortletName("待办公文");
        layout.setDisplayName("edoc.section.pending.label");
        layout.setMobileUrl("/seeyon/m3/apps/v5/edoc/html/edocList.html#allPending_");
        layout.setSize(PortletSize.middle.ordinal());
        layout.setSpaceTypes("m3mobile,weixinmobile,personal");
        layout.setNeedNumber(1);
        List<ImageLayout> ims = new ArrayList<ImageLayout>();
        
        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("edoc.section.pending.label");
        image1.setSummary("");
        image1.setImageUrl("d_officthemespace.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
    }
    
    public void setSuperviseManager(SuperviseManager superviseManager) {
		this.superviseManager = superviseManager;
	}
	public void setGovdocListManager(GovdocListManager govdocListManager) {
		this.govdocListManager = govdocListManager;
	}
	
}
