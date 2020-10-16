package com.seeyon.ctp.portal.portlet;

import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.collaboration.enums.ColQueryCondition;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.PendingManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.portal.portlet.PortletConstants.PortletCategory;
import com.seeyon.ctp.portal.portlet.PortletConstants.PortletSize;
import com.seeyon.ctp.portal.portlet.PortletConstants.UrlType;
import com.seeyon.ctp.portal.section.SectionExtraDataSelectImpl;
import com.seeyon.ctp.portal.section.SectionReferenceImpl;
import com.seeyon.ctp.portal.section.SectionReferenceValueRangeExtraSelectImpl;
import com.seeyon.ctp.portal.section.SectionReferenceValueRangeImpl;
import com.seeyon.ctp.util.FlipInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollaborationPortlet implements BasePortlet {
    
    private static final Log log = LogFactory.getLog(CollaborationPortlet.class);
    
    private SuperviseManager superviseManager;
    private PendingManager pendingManager;
    private CollaborationApi collaborationApi;
    
    public void setCollaborationApi(CollaborationApi collaborationApi) {
		this.collaborationApi = collaborationApi;
	}
    public void setPendingManager(PendingManager pendingManager) {
        this.pendingManager = pendingManager;
    }

    public void setSuperviseManager(SuperviseManager superviseManager) {
        this.superviseManager = superviseManager;
    }

    @Override
	public String getId() {
		return "collaborationPortlet";
	}

	@Override
	public List<ImagePortletLayout> getData() {
		List<ImagePortletLayout> layouts = new ArrayList<ImagePortletLayout>();
		layouts.add(this.getCollaborationTheme());
		layouts.add(this.getListDone());
		layouts.add(this.getListSent());
		layouts.add(this.getWaitSendPortlet());
        layouts.add(this.getPendingPortlet());
        layouts.add(this.getMyAgent());
        layouts.add(this.getPendingWorkPortlet());
        layouts.add(this.getSupervisePortlet());
        layouts.add(this.getTrackPortlet());
        layouts.add(this.getMyTempletePortlet());
        
        layouts.add(this.getColPortlet());
        //layouts.add(this.formTemplate());移动端与我的模板重复
        
        //layouts.add(this.remindImportantPortlet());本期去掉
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
	@Override
	public int getDataCount(String portletId) {
	    int dateCount = -1;
	    Map<String, String> query = new HashMap<String, String>();
        Map<String, Object> query1 = new HashMap<String, Object>();
        //参数控制,只查询数量
        query1.put("onlyCount", true);
	    User user = AppContext.getCurrentUser();
        query.put(ColQueryCondition.currentUser.name(), String.valueOf(user.getId()));
        try {
    	    if ("waitSendPortlet".equals(portletId)) {
    	        query.put(ColQueryCondition.state.name(), String.valueOf(StateEnum.col_waitSend.key()));
    	        dateCount = this.collaborationApi.getColAffairsCountByCondition(query);
    	    } else if ("pendingPortlet".equals(portletId) || "colPortlet".equals(portletId)) {
	            query.put(ColQueryCondition.state.name(), String.valueOf(StateEnum.col_pending.key()));
	            //需要查询代理时，加上该参数
	            query.put("hasNeedAgent", "true");
                dateCount = this.collaborationApi.getColAffairsCountByCondition(query);
    	    } else if ("supervisePortlet".equals(portletId)) {
    	        query1.put("app",ApplicationCategoryEnum.collaboration.getKey());
    	        FlipInfo a = superviseManager.getSuperviseList4App(new FlipInfo(), query1);
    	        dateCount = a.getTotal();
            } else if ("trackPortlet".equals(portletId)) {
                query1.put("ordinal", "2");
                query1.put("isTrack", true);
                FlipInfo a = pendingManager.getMoreList4SectionContion(new FlipInfo(), query1);
                dateCount = a.getTotal();
            } else if ("pendingWorkPortlet".equals(portletId)) {
                query1.put("fragmentId", "");
                query1.put("ordinal", "0");
                query1.put("state", StateEnum.col_pending.key());
                query1.put("isTrack", false);
                FlipInfo a = pendingManager.getMoreList4SectionContion(new FlipInfo(), query1);
                dateCount = a.getTotal();
            }else if("myAgentPortlet".equals(portletId)){
                query1.put("fragmentId","");
                query1.put("ordinal","");
                query1.put("state", StateEnum.col_pending.key());
                query1.put("isTrack", false);
                String rowStr = "subject,receiveTime,sendUser,deadLine,category";
                query1.put("rowStr",rowStr);
                FlipInfo a = pendingManager.getMoreAgentList4SectionContion(new FlipInfo(), query1);
                dateCount = a.getTotal();
            }
        } catch (Exception e) {
            log.error("首页工作桌面",e);
        }
		return dateCount;
	}

    @Override
    public boolean isAllowDataUsed(String portletId) {
        if ("waitSendPortlet".equals(portletId) && AppContext.getCurrentUser().hasResourceCode("F01_listWaitSend")) {
            return true;
        } else if ("pendingPortlet".equals(portletId) && AppContext.getCurrentUser().hasResourceCode("F01_listPending")) {
            return true;
        } else if ("supervisePortlet".equals(portletId) && AppContext.getCurrentUser().hasResourceCode("F01_supervise")) {
            return true;
        } else if ("trackPortlet".equals(portletId)
                && (AppContext.hasPlugin(PortletCategory.collaboration.name()) || AppContext.hasPlugin(PortletCategory.edoc.name()) || AppContext.hasPlugin(PortletCategory.formbizconfigs.name()))) {
            return true;
        } else if ("colPortlet".equals(portletId)) {
            return AppContext.getCurrentUser().hasResourceCode("F01_listPending") || AppContext.getCurrentUser().hasResourceCode("F01_listDone")
                    || AppContext.getCurrentUser().hasResourceCode("F01_listWaitSend") || AppContext.getCurrentUser().hasResourceCode("F01_listSent");
        } else if ("formTemplate".equals(portletId)) {
            return true;
        } else if ("pendingWorkPortlet".equals(portletId)) {
            return true;
        } else if ("myTemplete".equals(portletId)) {
            return true;
        } else if ("verduePortlet".equals(portletId)) {
            return true;
        } else if ("remindImportantPortlet".equals(portletId)) {
            return true;
        }else if("myAgentPortlet".equals(portletId)){
            return true;
        }else if("collaborationthemespace".equals(portletId)&&(AppContext.hasResourceCode("T03_cooperation_work"))){
            return true;
        }else if("doneevent".equals(portletId)&&(AppContext.hasResourceCode("F01_listDone"))){
            return true;
        }else if("sentevent".equals(portletId)&&(AppContext.hasResourceCode("F01_listSent"))){
            return true;
        }
        return false;
    }

	@Override
	public boolean isAllowUsed() {
	    
		return true;
	}

    /**
     * 主题空间
     * @return
     */
	private ImagePortletLayout getCollaborationTheme(){
        ImagePortletLayout layout = new ImagePortletLayout();
        layout.setResourceCode("T03_cooperation_work");
        layout.setPluginId("collaboration");
        layout.setCategory(PortletCategory.collaboration.name());
        layout.setDisplayName("collaboration.portal.colTopSpace");
        layout.setOrder(0);
        layout.setPortletId("collaborationthemespace");
        layout.setPortletName(ResourceUtil.getString("collaboration.portal.colTopSpace"));
        if(AppContext.hasPlugin("workflowAdvanced")) {
            layout.setPortletUrl("/bpm/bpmPortal.do?method=index&menu=T03_cooperation_work&themType=19&_resourceCode=T03_cooperation_work");
            layout.setPortletUrlType(UrlType.link.name());
            layout.setDisplayName("system.menuname.WorkingManagement.portal");
        }else {
            layout.setPortletUrl("/portal/spaceController.do?method=showThemSpace&themType=19");
            layout.setPortletUrlType(UrlType.workspace.name());
        }

        layout.setSize(PortletSize.middle.ordinal());
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("collaboration.portal.colTopSpace");
        image1.setSummary("");
        image1.setImageUrl("d_collaborationthemespace.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
    }

    /**
     * 已办
     * @return
     */
    private ImagePortletLayout getListDone(){
        ImagePortletLayout layout = new ImagePortletLayout();
        layout.setResourceCode("F01_listDone");
        layout.setPluginId("collaboration");
        layout.setCategory(PortletCategory.collaboration.name());
        layout.setDisplayName("system.menuname.DoneEvent");
        layout.setOrder(105);
        layout.setPortletId("doneevent");
        layout.setPortletName(ResourceUtil.getString("collaboration.state.14.done"));
        layout.setSpaceTypes("personal,personal_custom,leader,outer,custom,department,corporation,public_custom,group,public_custom_group,cooperation_work,objective_manage,edoc_manage,meeting_manage,performance_analysis,form_application,related_project_space,vjoinpc,vjoinmobile,m3mobile,weixinmobile");
        layout.setMobileUrl("/seeyon/m3/apps/v5/collaboration/html/colAffairs.html?openFrom=listDone");
        layout.setPortletUrl("/collaboration/collaboration.do?method=listDone");
        layout.setPortletUrlType(UrlType.workspace.name());

        layout.setSize(PortletSize.middle.ordinal());
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("system.menuname.DoneEvent");
        image1.setSummary("");
        image1.setImageUrl("d_doneevent.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
    }

    /**
     * 已发
     * @return
     */
    private ImagePortletLayout getListSent(){
        ImagePortletLayout layout = new ImagePortletLayout();
        layout.setResourceCode("F01_listSent");
        layout.setPluginId("collaboration");
        layout.setCategory(PortletCategory.collaboration.name());
        layout.setDisplayName("system.menuname.SentEvent");
        layout.setOrder(103);
        layout.setPortletId("sentevent");
        layout.setPortletName(ResourceUtil.getString("collaboration.state.12.col_sent"));
        layout.setSpaceTypes("personal,personal_custom,leader,outer,custom,department,corporation,public_custom,group,public_custom_group,cooperation_work,objective_manage,edoc_manage,meeting_manage,performance_analysis,form_application,related_project_space,vjoinpc,vjoinmobile,m3mobile,weixinmobile");
        layout.setMobileUrl("/seeyon/m3/apps/v5/collaboration/html/colAffairs.html?openFrom=listSent");
        layout.setPortletUrl("/collaboration/collaboration.do?method=listSent");
        layout.setPortletUrlType(UrlType.workspace.name());
        

        layout.setSize(PortletSize.middle.ordinal());
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("system.menuname.SentEvent");
        image1.setSummary("");
        image1.setImageUrl("d_sentevent.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
    }
	/**
	 * 待发事项
	 * @return
	 */
    private ImagePortletLayout getWaitSendPortlet() {
	    ImagePortletLayout layout = new ImagePortletLayout();
	    layout.setResourceCode("F01_listWaitSend");
        layout.setPluginId("collaboration");
        layout.setCategory(PortletCategory.collaboration.name());
        layout.setDisplayName("system.menuname.UnsentEvent");
        layout.setOrder(102);
        layout.setPortletId("waitSendPortlet");
        layout.setPortletName(ResourceUtil.getString("coll.toSend.label"));
        layout.setSpaceTypes("personal,personal_custom,leader,outer,custom,department,corporation,public_custom,group,public_custom_group,cooperation_work,objective_manage,edoc_manage,meeting_manage,performance_analysis,form_application,related_project_space,vjoinpc,vjoinmobile,m3mobile,weixinmobile");
        layout.setMobileUrl("/seeyon/m3/apps/v5/collaboration/html/colAffairs.html?openFrom=listWaitSend");
    	layout.setPortletUrl("/collaboration/collaboration.do?method=listWaitSend");
        layout.setPortletUrlType(UrlType.workspace.name());
        
        
        layout.setSize(PortletSize.middle.ordinal());
        layout.setNeedNumber(1);
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("system.menuname.UnsentEvent");
        image1.setSummary("");
        image1.setImageUrl("d_unsentEvent.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
	    return layout;
	}
	
	/**
	 * 待办事项，是协同工作下的不列出所有的的内容
	 * @return
	 */
	private ImagePortletLayout getPendingPortlet() {
	    ImagePortletLayout layout = new ImagePortletLayout();
        layout.setResourceCode("F01_listPending");
        layout.setPluginId("collaboration");
        layout.setCategory(PortletCategory.collaboration.name());
        layout.setDisplayName("system.menuname.TODOEvent");
        layout.setOrder(104);
        layout.setPortletId("pendingPortlet");
        layout.setPortletName(ResourceUtil.getString("coll.pending.label"));
        layout.setSpaceTypes("personal,personal_custom,leader,outer,custom,department,corporation,public_custom,group,public_custom_group,cooperation_work,objective_manage,edoc_manage,meeting_manage,performance_analysis,form_application,related_project_space,vjoinpc,vjoinmobile,m3mobile,weixinmobile");
        layout.setMobileUrl("/seeyon/m3/apps/v5/collaboration/html/colAffairs.html?openFrom=listPending");
        layout.setPortletUrl("/collaboration/collaboration.do?method=listPending");
        layout.setPortletUrlType(UrlType.workspace.name());
        layout.setSize(PortletSize.middle.ordinal());
        layout.setNeedNumber(1);
 //       layout.setCanRepeat(1);
//        SectionReferenceImpl dataSourceReference=new SectionReferenceImpl();
//        dataSourceReference.setSubject("cannel.panel.label");
//        dataSourceReference.setName("panel");
//        dataSourceReference.setSpaceType("defaultPC,m3mobile");
//        dataSourceReference.setValueType(5);
//        dataSourceReference.setDefaultValue("all");
//        List<SectionReferenceValueRangeImpl> valueRangeList=new ArrayList<SectionReferenceValueRangeImpl>();
//        SectionReferenceValueRangeImpl sectionReferenceValueRange1 = new SectionReferenceValueRangeImpl();
//        sectionReferenceValueRange1.setSubject("pending.panel.all.label");
//        sectionReferenceValueRange1.setSpaceType("defaultPC,m3mobile");
//        sectionReferenceValueRange1.setValue("all");
//        valueRangeList.add(sectionReferenceValueRange1);
//        SectionReferenceValueRangeImpl sectionReferenceValueRange2 = new SectionReferenceValueRangeImpl();
//        sectionReferenceValueRange2.setSubject("pending.panel.sender.label");
//        sectionReferenceValueRange2.setSpaceType("defaultPC,m3mobile");
//        sectionReferenceValueRange2.setValue("sender");
//        sectionReferenceValueRange2.setBackUp(true);
//        sectionReferenceValueRange2.setPanelSetUrl("/selectPeople.do?ViewPage=SelectPeople4PortalSender&amp;amp;Panels=Department,Team,Post,Outworker,RelatePeople,BusinessDepartment&amp;amp;SelectType=Account,Department,Team,RelatePeople,Post,Member&amp;amp;id=portalSender&amp;amp;include=true");
//        valueRangeList.add(sectionReferenceValueRange2);
//        SectionReferenceValueRangeImpl sectionReferenceValueRange3 = new SectionReferenceValueRangeImpl();
//        sectionReferenceValueRange3.setSubject("collaboration.pending.panel.combination.sources");
//        sectionReferenceValueRange3.setSpaceType("defaultPC,m3mobile");
//        sectionReferenceValueRange3.setValue("sources");
//        sectionReferenceValueRange3.setBackUp(true);
//        List<SectionReferenceValueRangeExtraSelectImpl> sectionReferenceValueRangeExtraSelectList=new ArrayList<SectionReferenceValueRangeExtraSelectImpl>();
//        SectionReferenceValueRangeExtraSelectImpl sectionReferenceValueRangeExtraSelect1 = new SectionReferenceValueRangeExtraSelectImpl();
//        sectionReferenceValueRangeExtraSelect1.setSubject("template.section.choose.label");
//        sectionReferenceValueRangeExtraSelect1.setSpaceType("defaultPC,m3mobile");
//        sectionReferenceValueRangeExtraSelect1.setValue("track_catagory");
//        sectionReferenceValueRangeExtraSelect1.setPanelSetUrl("/portalAffair/portalAffairController.do?method=showPortalCatagory&amp;amp;openFrom=pendingSection");
//        sectionReferenceValueRangeExtraSelectList.add(sectionReferenceValueRangeExtraSelect1);
//
//        SectionReferenceValueRangeExtraSelectImpl sectionReferenceValueRangeExtraSelect2 = new SectionReferenceValueRangeExtraSelectImpl();
//        sectionReferenceValueRangeExtraSelect2.setSubject("pending.panel.policy.or.other.label");
//        sectionReferenceValueRangeExtraSelect2.setSpaceType("defaultPC,m3mobile");
//        sectionReferenceValueRangeExtraSelect2.setValue("Policy");
//        sectionReferenceValueRangeExtraSelect2.setPanelSetUrl("/portalAffair/portalAffairController.do?method=showPortalCatagory&amp;amp;openFrom=pendingSection");
//        sectionReferenceValueRangeExtraSelectList.add(sectionReferenceValueRangeExtraSelect2);
//
//        SectionReferenceValueRangeExtraSelectImpl sectionReferenceValueRangeExtraSelect3 = new SectionReferenceValueRangeExtraSelectImpl();
//        sectionReferenceValueRangeExtraSelect3.setSubject("collaboration.template.toolbar.category");
//        sectionReferenceValueRangeExtraSelect3.setSpaceType("defaultPC,m3mobile");
//        sectionReferenceValueRangeExtraSelect3.setValue("templete_pending");
//        sectionReferenceValueRangeExtraSelect3.setPanelSetUrl("template/template.do?method=templateChooseMul&amp;amp;type=-1&amp;amp;isPortal=1&amp;amp;isMul=true&amp;amp;scope=MaxScope&amp;amp;isCanSelectCategory=true&amp;amp;isShowTemplateRecentDeal=true&amp;amp;isAlwaysShowTemplateCommon=true&amp;amp;openFrom=pendingSection");
//        sectionReferenceValueRangeExtraSelectList.add(sectionReferenceValueRangeExtraSelect3);
//
//        SectionReferenceValueRangeExtraSelectImpl sectionReferenceValueRangeExtraSelect4 = new SectionReferenceValueRangeExtraSelectImpl();
//        sectionReferenceValueRangeExtraSelect4.setSubject("pending.panel.importlevel.label");
//        sectionReferenceValueRangeExtraSelect4.setSpaceType("defaultPC,m3mobile");
//        sectionReferenceValueRangeExtraSelect4.setValue("importLevel");
//        sectionReferenceValueRangeExtraSelect4.setPanelSetUrl("/portalAffair/portalAffairController.do?method=showPortalImportLevel&amp;amp;openFrom=pendingSection");
//        sectionReferenceValueRangeExtraSelectList.add(sectionReferenceValueRangeExtraSelect4);
//
//        SectionReferenceValueRangeExtraSelectImpl sectionReferenceValueRangeExtraSelect5 = new SectionReferenceValueRangeExtraSelectImpl();
//        sectionReferenceValueRangeExtraSelect5.setSubject("collaboration.pending.panel.overTime.label");
//        sectionReferenceValueRangeExtraSelect5.setSpaceType("defaultPC,m3mobile");
//        sectionReferenceValueRangeExtraSelect5.setValue("overTime");
//        sectionReferenceValueRangeExtraSelectList.add(sectionReferenceValueRangeExtraSelect5);
//
//        SectionReferenceValueRangeExtraSelectImpl sectionReferenceValueRangeExtraSelect6 = new SectionReferenceValueRangeExtraSelectImpl();
//        sectionReferenceValueRangeExtraSelect6.setSubject("collaboration.pending.panel.handlingState.label");
//        sectionReferenceValueRangeExtraSelect6.setSpaceType("defaultPC,m3mobile");
//        sectionReferenceValueRangeExtraSelect6.setValue("handlingState");
//        sectionReferenceValueRangeExtraSelect6.setPanelSetUrl("collaboration/pending.do?method=handlingState");
//        sectionReferenceValueRangeExtraSelectList.add(sectionReferenceValueRangeExtraSelect6);
//
//        SectionReferenceValueRangeExtraSelectImpl sectionReferenceValueRangeExtraSelect7 = new SectionReferenceValueRangeExtraSelectImpl();
//        sectionReferenceValueRangeExtraSelect7.setSubject("collaboration.pending.panel.my.leader.label");
//        sectionReferenceValueRangeExtraSelect7.setSpaceType("defaultPC,m3mobile");
//        sectionReferenceValueRangeExtraSelect7.setValue("myLeader");
//        sectionReferenceValueRangeExtraSelectList.add(sectionReferenceValueRangeExtraSelect7);
//
//        SectionReferenceValueRangeExtraSelectImpl sectionReferenceValueRangeExtraSelect8 = new SectionReferenceValueRangeExtraSelectImpl();
//        sectionReferenceValueRangeExtraSelect8.setSubject("collaboration.pending.panel.my.dept.label");
//        sectionReferenceValueRangeExtraSelect8.setSpaceType("defaultPC,m3mobile");
//        sectionReferenceValueRangeExtraSelect8.setValue("myDept");
//        sectionReferenceValueRangeExtraSelectList.add(sectionReferenceValueRangeExtraSelect8);
//
//        SectionReferenceValueRangeExtraSelectImpl sectionReferenceValueRangeExtraSelect9 = new SectionReferenceValueRangeExtraSelectImpl();
//        sectionReferenceValueRangeExtraSelect9.setSubject("collaboration.pending.panel.add.to.me.label");
//        sectionReferenceValueRangeExtraSelect9.setSpaceType("defaultPC,m3mobile");
//        sectionReferenceValueRangeExtraSelect9.setValue("addToMe");
//        sectionReferenceValueRangeExtraSelectList.add(sectionReferenceValueRangeExtraSelect9);
//
//        SectionReferenceValueRangeExtraSelectImpl sectionReferenceValueRangeExtraSelect10 = new SectionReferenceValueRangeExtraSelectImpl();
//        sectionReferenceValueRangeExtraSelect10.setSubject("collaboration.pending.panel.relation");
//        sectionReferenceValueRangeExtraSelect10.setSpaceType("defaultPC,m3mobile");
//        sectionReferenceValueRangeExtraSelect10.setValue("relation");
//        sectionReferenceValueRangeExtraSelect10.setShowCheckBox(false);
//        ArrayList<SectionExtraDataSelectImpl> sectionExtraDataSelectList = new ArrayList<SectionExtraDataSelectImpl>();
//        SectionExtraDataSelectImpl sectionExtraDataSelect1 = new SectionExtraDataSelectImpl();
//        sectionExtraDataSelect1.setKey("1");
//        sectionExtraDataSelect1.setValue("and");
//        sectionExtraDataSelectList.add(sectionExtraDataSelect1);
//        SectionExtraDataSelectImpl sectionExtraDataSelect2 = new SectionExtraDataSelectImpl();
//        sectionExtraDataSelect1.setKey("0");
//        sectionExtraDataSelect1.setValue("or");
//        sectionExtraDataSelectList.add(sectionExtraDataSelect2);
//        SectionExtraDataSelectImpl[] SectionExtraDataSelectArray=new SectionExtraDataSelectImpl[sectionExtraDataSelectList.size()];
//        sectionReferenceValueRangeExtraSelect10.setSelect(sectionExtraDataSelectList.toArray(SectionExtraDataSelectArray));
//        sectionReferenceValueRangeExtraSelectList.add(sectionReferenceValueRangeExtraSelect10);
//        SectionReferenceValueRangeExtraSelectImpl[] SectionReferenceValueRangeExtraSelectArray=new SectionReferenceValueRangeExtraSelectImpl[sectionReferenceValueRangeExtraSelectList.size()];
//        sectionReferenceValueRange3.setValueRangeExtraSelects(sectionReferenceValueRangeExtraSelectList.toArray(SectionReferenceValueRangeExtraSelectArray));
//        valueRangeList.add(sectionReferenceValueRange3);
//        SectionReferenceValueRangeImpl[] sectionReferenceValueRangeArray=new SectionReferenceValueRangeImpl[valueRangeList.size()];
//        dataSourceReference.setValueRanges(valueRangeList.toArray(sectionReferenceValueRangeArray));
//        layout.setDataSource(dataSourceReference);
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("system.menuname.TODOEvent");
        image1.setSummary("");
        image1.setImageUrl("d_todoevent.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
	}

    /**
     * 代理事项磁贴
     * @return
     */
	private ImagePortletLayout getMyAgent(){
        ImagePortletLayout layout = new ImagePortletLayout();
        //layout.setResourceCode("T3_MyAgent");
        layout.setPluginId("collaboration");
        layout.setCategory(PortletCategory.collaboration.name());
        layout.setDisplayName("common.my.agent.label");
        layout.setOrder(105);
        layout.setPortletId("myAgentPortlet");
        layout.setPortletName(ResourceUtil.getString("collaboration.portal.remind.agent"));
        layout.setSpaceTypes("personal,personal_custom,leader,outer,custom,department,corporation,public_custom,group,public_custom_group,cooperation_work,objective_manage,edoc_manage,meeting_manage,performance_analysis,form_application,related_project_space");
        layout.setMobileUrl("");//TODO 移动端1130发版后提供H5页面
        layout.setPortletUrl("/collaboration/pending.do?method=morePending&from=Agent");
        layout.setPortletUrlType(UrlType.workspace.name());
        layout.setSize(PortletSize.middle.ordinal());
        layout.setNeedNumber(1);
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("common.my.agent.label");
        image1.setSummary("");
        image1.setImageUrl("d_agent.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
    }
	/**
     *  流程督办监控
     * @return
     */
    private ImagePortletLayout getSupervisePortlet() {
        ImagePortletLayout layout = new ImagePortletLayout();
        layout.setResourceCode("F01_supervise");
        layout.setPluginId("collaboration");
        layout.setCategory(PortletCategory.collaboration.name());
        layout.setDisplayName("system.menuname.SupervisoryEvent");
        layout.setOrder(107);
        layout.setPortletId("supervisePortlet");
        layout.setPortletName(ResourceUtil.getString("supervise.process.lable0"));
        layout.setPortletUrl("/supervise/supervise.do?method=superviseFrame&managerType=mySupervise");
    	layout.setPortletUrlType(UrlType.workspace.name());
        layout.setSize(PortletSize.middle.ordinal());
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageUrl("d_supervisoryevent.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
    }
    
    /**
     * 跟踪事项
     * @return
     */
    private ImagePortletLayout getTrackPortlet() {
        ImagePortletLayout layout = new ImagePortletLayout();
        layout.setPluginId("collaboration");
        layout.setCategory(PortletCategory.collaboration.name());
        layout.setDisplayName("collaboration.protal.track.label");
        layout.setOrder(106);
        layout.setPortletId("trackPortlet");
        layout.setPortletName(ResourceUtil.getString("coll.track.label"));
        layout.setPortletUrl("/portalAffair/portalAffairController.do?method=moreTrack&ordinal=2&currentPanel=all&rowStr=subject,receiveTime,sendUser,currentNodesInfo,deadline,category");
        layout.setPortletUrlType(UrlType.workspace.name());
        layout.setSize(PortletSize.middle.ordinal());
        layout.setNeedNumber(1);
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("collaboration.protal.track.label");
        image1.setSummary("");
        image1.setImageUrl("d_trackevent.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
    }
    
    /**
     * 协同
     * @return
     */
    private ImagePortletLayout getColPortlet() {
        ImagePortletLayout layout = new ImagePortletLayout();
        layout.setPluginId("collaboration");
        layout.setResourceCode("F01_listPending,F01_listDone,F01_listSent,F01_listWaitSend");
        layout.setCategory("collaboration,common");
        layout.setOrder(54);
        layout.setmOrder(50);
        layout.setPortletId("colPortlet");
        layout.setPortletName(ResourceUtil.getString("coll.shortLabel"));
        layout.setDisplayName("pending.collaboration.label");
        layout.setSpaceTypes("m3mobile,weixinmobile,mobile_application");
        layout.setPortletUrl("");
        layout.setMobileUrl("/seeyon/m3/apps/v5/collaboration/html/colAffairs.html");
        layout.setPortletUrlType(UrlType.workspace.name());
        layout.setSize(PortletSize.middle.ordinal());
        layout.setNeedNumber(1);
        List<ImageLayout> ims = new ArrayList<ImageLayout>();
        
        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("pending.collaboration.label");
        image1.setSummary("");
        image1.setImageUrl("d_collaborationthemespace.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
    }
    
    /**
	 * 待办工作
	 * @return
	 */
	private ImagePortletLayout getPendingWorkPortlet() {
	    ImagePortletLayout layout = new ImagePortletLayout();
        layout.setPluginId("collaboration");
        layout.setCategory(PortletCategory.common.name());
        layout.setDisplayName("common.my.pending.title");
        layout.setOrder(65);
        layout.setPortletId("pendingWorkPortlet");
        layout.setPortletName(ResourceUtil.getString("common.my.pending.title"));
        layout.setPortletUrl("/collaboration/pending.do?method=morePending&rowStr=subject,receiveTime,sendUser,category");
        layout.setPortletUrlType(UrlType.workspace.name());
        layout.setMobileUrl("/seeyon/m3/apps/v5/portal/html/morePending.html?openFrom=listPending");
        layout.setmOrder(51);
        layout.setSpaceTypes("personal,personal_custom,leader,outer,custom,department,corporation,public_custom,group,public_custom_group,cooperation_work,objective_manage,edoc_manage,meeting_manage,performance_analysis,form_application,related_project_space,m3mobile,weixinmobile");
        layout.setSize(PortletSize.middle.ordinal());
        layout.setNeedNumber(1);
        layout.setCanRepeat(1);
        SectionReferenceImpl dataSourceReference=new SectionReferenceImpl();
        dataSourceReference.setSubject("cannel.panel.label");
        dataSourceReference.setName("panel");
        dataSourceReference.setSpaceType("defaultPC,m3mobile");
        dataSourceReference.setValueType(5);
        dataSourceReference.setDefaultValue("all");
        List<SectionReferenceValueRangeImpl> valueRangeList=new ArrayList<SectionReferenceValueRangeImpl>();
        SectionReferenceValueRangeImpl sectionReferenceValueRange1 = new SectionReferenceValueRangeImpl();
        sectionReferenceValueRange1.setSubject("pending.panel.all.label");
        sectionReferenceValueRange1.setSpaceType("defaultPC,m3mobile");
        sectionReferenceValueRange1.setValue("all");
        valueRangeList.add(sectionReferenceValueRange1);
        SectionReferenceValueRangeImpl sectionReferenceValueRange2 = new SectionReferenceValueRangeImpl();
        sectionReferenceValueRange2.setSubject("pending.panel.sender.label");
        sectionReferenceValueRange2.setSpaceType("defaultPC,m3mobile");
        sectionReferenceValueRange2.setValue("sender");
        sectionReferenceValueRange2.setBackUp(true);
        sectionReferenceValueRange2.setPanelSetUrl("/selectPeople.do?ViewPage=SelectPeople4PortalSender&Panels=Department,Team,Post,Outworker,RelatePeople,BusinessDepartment&SelectType=Account,Department,Team,RelatePeople,Post,Member&id=portalSender&include=true");
        valueRangeList.add(sectionReferenceValueRange2);
        SectionReferenceValueRangeImpl sectionReferenceValueRange3 = new SectionReferenceValueRangeImpl();
        sectionReferenceValueRange3.setSubject("collaboration.pending.panel.combination.sources");
        sectionReferenceValueRange3.setSpaceType("defaultPC,m3mobile");
        sectionReferenceValueRange3.setValue("sources");
        sectionReferenceValueRange3.setBackUp(true);
        List<SectionReferenceValueRangeExtraSelectImpl> sectionReferenceValueRangeExtraSelectList=new ArrayList<SectionReferenceValueRangeExtraSelectImpl>();
        SectionReferenceValueRangeExtraSelectImpl sectionReferenceValueRangeExtraSelect1 = new SectionReferenceValueRangeExtraSelectImpl();
        sectionReferenceValueRangeExtraSelect1.setSubject("template.section.choose.label");
        sectionReferenceValueRangeExtraSelect1.setSpaceType("defaultPC,m3mobile");
        sectionReferenceValueRangeExtraSelect1.setValue("track_catagory");
        sectionReferenceValueRangeExtraSelect1.setPanelSetUrl("/portalAffair/portalAffairController.do?method=showPortalCatagory&openFrom=pendingSection");
        sectionReferenceValueRangeExtraSelectList.add(sectionReferenceValueRangeExtraSelect1);

        SectionReferenceValueRangeExtraSelectImpl sectionReferenceValueRangeExtraSelect2 = new SectionReferenceValueRangeExtraSelectImpl();
        sectionReferenceValueRangeExtraSelect2.setSubject("pending.panel.policy.or.other.label");
        sectionReferenceValueRangeExtraSelect2.setSpaceType("defaultPC,m3mobile");
        sectionReferenceValueRangeExtraSelect2.setValue("Policy");
        sectionReferenceValueRangeExtraSelect2.setPanelSetUrl("/portalAffair/portalAffairController.do?method=showPortalCatagory&openFrom=pendingSection");
        sectionReferenceValueRangeExtraSelectList.add(sectionReferenceValueRangeExtraSelect2);

        SectionReferenceValueRangeExtraSelectImpl sectionReferenceValueRangeExtraSelect3 = new SectionReferenceValueRangeExtraSelectImpl();
        sectionReferenceValueRangeExtraSelect3.setSubject("collaboration.template.toolbar.category");
        sectionReferenceValueRangeExtraSelect3.setSpaceType("defaultPC,m3mobile");
        sectionReferenceValueRangeExtraSelect3.setValue("templete_pending");
        sectionReferenceValueRangeExtraSelect3.setPanelSetUrl("template/template.do?method=templateChooseMul&type=-1&isPortal=1&isMul=true&scope=MaxScope&isCanSelectCategory=true&isShowTemplateRecentDeal=true&isAlwaysShowTemplateCommon=true&openFrom=pendingSection");
        sectionReferenceValueRangeExtraSelectList.add(sectionReferenceValueRangeExtraSelect3);

        SectionReferenceValueRangeExtraSelectImpl sectionReferenceValueRangeExtraSelect4 = new SectionReferenceValueRangeExtraSelectImpl();
        sectionReferenceValueRangeExtraSelect4.setSubject("pending.panel.importlevel.label");
        sectionReferenceValueRangeExtraSelect4.setSpaceType("defaultPC,m3mobile");
        sectionReferenceValueRangeExtraSelect4.setValue("importLevel");
        sectionReferenceValueRangeExtraSelect4.setPanelSetUrl("/portalAffair/portalAffairController.do?method=showPortalImportLevel&openFrom=pendingSection");
        sectionReferenceValueRangeExtraSelectList.add(sectionReferenceValueRangeExtraSelect4);

        SectionReferenceValueRangeExtraSelectImpl sectionReferenceValueRangeExtraSelect5 = new SectionReferenceValueRangeExtraSelectImpl();
        sectionReferenceValueRangeExtraSelect5.setSubject("collaboration.pending.panel.overTime.label");
        sectionReferenceValueRangeExtraSelect5.setSpaceType("defaultPC,m3mobile");
        sectionReferenceValueRangeExtraSelect5.setValue("overTime");
        sectionReferenceValueRangeExtraSelectList.add(sectionReferenceValueRangeExtraSelect5);

        SectionReferenceValueRangeExtraSelectImpl sectionReferenceValueRangeExtraSelect6 = new SectionReferenceValueRangeExtraSelectImpl();
        sectionReferenceValueRangeExtraSelect6.setSubject("collaboration.pending.panel.handlingState.label");
        sectionReferenceValueRangeExtraSelect6.setSpaceType("defaultPC,m3mobile");
        sectionReferenceValueRangeExtraSelect6.setValue("handlingState");
        sectionReferenceValueRangeExtraSelect6.setPanelSetUrl("collaboration/pending.do?method=handlingState");
        sectionReferenceValueRangeExtraSelectList.add(sectionReferenceValueRangeExtraSelect6);

        SectionReferenceValueRangeExtraSelectImpl sectionReferenceValueRangeExtraSelect7 = new SectionReferenceValueRangeExtraSelectImpl();
        sectionReferenceValueRangeExtraSelect7.setSubject("collaboration.pending.panel.my.leader.label");
        sectionReferenceValueRangeExtraSelect7.setSpaceType("defaultPC,m3mobile");
        sectionReferenceValueRangeExtraSelect7.setValue("myLeader");
        sectionReferenceValueRangeExtraSelectList.add(sectionReferenceValueRangeExtraSelect7);

        SectionReferenceValueRangeExtraSelectImpl sectionReferenceValueRangeExtraSelect8 = new SectionReferenceValueRangeExtraSelectImpl();
        sectionReferenceValueRangeExtraSelect8.setSubject("collaboration.pending.panel.my.dept.label");
        sectionReferenceValueRangeExtraSelect8.setSpaceType("defaultPC,m3mobile");
        sectionReferenceValueRangeExtraSelect8.setValue("myDept");
        sectionReferenceValueRangeExtraSelectList.add(sectionReferenceValueRangeExtraSelect8);

        SectionReferenceValueRangeExtraSelectImpl sectionReferenceValueRangeExtraSelect9 = new SectionReferenceValueRangeExtraSelectImpl();
        sectionReferenceValueRangeExtraSelect9.setSubject("collaboration.pending.panel.add.to.me.label");
        sectionReferenceValueRangeExtraSelect9.setSpaceType("defaultPC,m3mobile");
        sectionReferenceValueRangeExtraSelect9.setValue("addToMe");
        sectionReferenceValueRangeExtraSelectList.add(sectionReferenceValueRangeExtraSelect9);

        SectionReferenceValueRangeExtraSelectImpl sectionReferenceValueRangeExtraSelect10 = new SectionReferenceValueRangeExtraSelectImpl();
        sectionReferenceValueRangeExtraSelect10.setSubject("collaboration.pending.panel.relation");
        sectionReferenceValueRangeExtraSelect10.setSpaceType("defaultPC,m3mobile");
        sectionReferenceValueRangeExtraSelect10.setValue("relation");
        sectionReferenceValueRangeExtraSelect10.setShowCheckBox(false);
        ArrayList<SectionExtraDataSelectImpl> sectionExtraDataSelectList = new ArrayList<SectionExtraDataSelectImpl>();
        SectionExtraDataSelectImpl sectionExtraDataSelect1 = new SectionExtraDataSelectImpl();
        sectionExtraDataSelect1.setKey("1");
        sectionExtraDataSelect1.setValue("and");
        sectionExtraDataSelectList.add(sectionExtraDataSelect1);
        SectionExtraDataSelectImpl sectionExtraDataSelect2 = new SectionExtraDataSelectImpl();
        sectionExtraDataSelect2.setKey("0");
        sectionExtraDataSelect2.setValue("or");
        sectionExtraDataSelectList.add(sectionExtraDataSelect2);
        SectionExtraDataSelectImpl[] SectionExtraDataSelectArray=new SectionExtraDataSelectImpl[sectionExtraDataSelectList.size()];
        sectionReferenceValueRangeExtraSelect10.setSelect(sectionExtraDataSelectList.toArray(SectionExtraDataSelectArray));
        sectionReferenceValueRangeExtraSelectList.add(sectionReferenceValueRangeExtraSelect10);
        SectionReferenceValueRangeExtraSelectImpl[] SectionReferenceValueRangeExtraSelectArray=new SectionReferenceValueRangeExtraSelectImpl[sectionReferenceValueRangeExtraSelectList.size()];
        sectionReferenceValueRange3.setValueRangeExtraSelects(sectionReferenceValueRangeExtraSelectList.toArray(SectionReferenceValueRangeExtraSelectArray));
        valueRangeList.add(sectionReferenceValueRange3);
        SectionReferenceValueRangeImpl[] sectionReferenceValueRangeArray=new SectionReferenceValueRangeImpl[valueRangeList.size()];
        dataSourceReference.setValueRanges(valueRangeList.toArray(sectionReferenceValueRangeArray));
        layout.setDataSource(dataSourceReference);
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("common.my.pending.title");
        image1.setSummary("");
        image1.setImageUrl("Pendingwork");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
	}
	
	/**
	 * 我的模板(表单模版 改名)
	 * @return
	 */
	private ImagePortletLayout getMyTempletePortlet() {
		ImagePortletLayout layout = new ImagePortletLayout();
        layout.setPluginId("collaboration");
        layout.setCategory("collaboration,common");
        layout.setDisplayName("common.my.template");
        layout.setOrder(68);
        layout.setPortletId("formTemplate");
        layout.setPortletName(ResourceUtil.getString("template.templatePub.myTemplate"));
        layout.setPortletUrl("/template/template.do?method=moreTreeTemplate&ordinal=0");
        layout.setPortletUrlType(UrlType.workspace.name());
        layout.setMobileUrl("/seeyon/m3/apps/v5/collaboration/html/templateIndex.html");
        layout.setSize(PortletSize.middle.ordinal());
        layout.setSpaceTypes("personal,personal_custom,leader,outer,custom,department,corporation,public_custom,group,public_custom_group,cooperation_work,objective_manage,edoc_manage,meeting_manage,performance_analysis,form_application,related_project_space,m3mobile,weixinmobile,mobile_application");
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageUrl("formtemplate");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
	}
	
    private ImagePortletLayout remindImportantPortlet() {
        ImagePortletLayout layout = new ImagePortletLayout();
        layout.setPluginId("collaboration");
        layout.setCategory(PortletCategory.collaboration.name());
        layout.setDisplayName("collaboration.portal.remind.important");
        layout.setOrder(107);
        layout.setPortletId("remindImportantPortlet");
        layout.setPortletName(ResourceUtil.getString("collaboration.portal.remind.important"));
        layout.setPortletUrl("mobile");
        layout.setMobileUrl("/seeyon/m3/apps/v5/portal/todo/html/todo-list.html");
        layout.setPortletUrlType(UrlType.workspace.name());
        layout.setSize(PortletSize.middle.ordinal());
        layout.setSpaceTypes("m3mobile,weixinmobile");
        layout.setNeedNumber(1);
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageUrl("d_formtemplate.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
    }
}
