package com.seeyon.ctp.portal.section;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import com.google.common.collect.Maps;
import com.seeyon.apps.govdoc.constant.GovdocEnum.GovdocResCodeEnum;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.authenticate.domain.UserHelper;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.login.bo.MenuBO;
import com.seeyon.ctp.portal.section.bo.SectionTreeNode;
import com.seeyon.ctp.portal.section.manager.BaseAbstractSectionSelector;
import com.seeyon.ctp.portal.util.PortletPropertyContants.PropertyName;
import com.seeyon.ctp.report.engine.api.ReportConstants.DesignType;
import com.seeyon.ctp.report.engine.api.manager.ReportApi;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.v3x.common.web.login.CurrentUser;

public class EdocSectionSelector extends BaseAbstractSectionSelector {
    private static String count = "8";
    private ReportApi reportApi;
	
    public void setReportApi(ReportApi reportApi) {
		this.reportApi = reportApi;
	}

    @Override
    public List<SectionTreeNode> selectSectionTreeData(String spaceType, String spaceId) throws BusinessException {
        List<String[]> sections = super.selectAllowedSections(spaceType);
        //由于公文的栏目选择顺序和协同的不一样，所以不能用section中初始设置的sortId，目前的顺序是已发、待办、已办、督办、我的模板、统计、发文登记薄、收文登记薄
        //SectionTreeNode[] l = new SectionTreeNode[8];
        List<SectionTreeNode> ll = new LinkedList<SectionTreeNode>();
        List<String> sectionNames = new ArrayList<String>();
        User user = CurrentUser.get();
        if(sections!=null) {
            for(String[] str : sections){
                if("outer".equals(spaceType) && "pendingSection".equals(str[0]) && !hasEdocMenuCode(user))
                    continue;
                if("outer".equals(spaceType) && "doneSection".equals(str[0]) && !hasEdocMenuCode(user))
                    continue;
                if("outer".equals(spaceType) && "sentSection".equals(str[0]) && !hasEdocMenuCode(user))
                    continue;
                if("outer".equals(spaceType) && "templeteSection".equals(str[0]) && !hasEdocMenuCode(user))
                    continue;
                if("outer".equals(spaceType) && "edocRecRegisterSection".equals(str[0]) && !hasEdocResourceCode(user, GovdocResCodeEnum.F20_sendandreportAuth.name()))
                    continue;
                if("outer".equals(spaceType) && "edocSendRegisterSection".equals(str[0]) && !hasEdocResourceCode(user, GovdocResCodeEnum.F20_recandreportAuth.name()))
                    continue;
                if("outer".equals(spaceType) && "edocStatisticsSection".equals(str[0]) && !hasEdocResourceCode(user, GovdocResCodeEnum.F20_stat.name()))
                    continue;
                sectionNames.add(str[0]);
            }
        }
        if(CollectionUtils.isNotEmpty(sectionNames)){
            if(sectionNames.contains("pendingSection")) {
                ll.add(addEdocPendingSection());
            }
            if(sectionNames.contains("superviseSection")) {
                ll.add(addEdocSuperviseSection());
            }
            if(sectionNames.contains("doneSection")) {
                ll.add(addEdocDoneScetion());
            }
            if(sectionNames.contains("sentSection")) {
                ll.add(addEdocSentSection());
            }
            if(sectionNames.contains("templeteSection")) {
                ll.add(addEdocTemplateSection());
            }
            
            SectionTreeNode queryNode = new SectionTreeNode();
            String queryNodeId = String.valueOf(UUIDLong.longUUID());
            queryNode.setId(queryNodeId);
            queryNode.setSectionName("公文查询");
            ll.add(queryNode);
            
            SectionTreeNode statsNode = new SectionTreeNode();
            String statsNodeId = String.valueOf(UUIDLong.longUUID());
            statsNode.setId(statsNodeId);
            statsNode.setSectionName("公文统计");
            ll.add(statsNode);
            
            Map<String,Object> params = Maps.newHashMap();
    		params.put("category", ApplicationCategoryEnum.edoc.name());
//    		params.put("designType", DesignType.QUERY.name());
            List<Map<String, Object>> designs = reportApi.findDesign(params);
            for(Map<String, Object> map : designs){
            	String nodeId = MapUtils.getString(map, "designId");
            	String designType = MapUtils.getString(map, "designType");
            	SectionTreeNode node = new SectionTreeNode();
            	if(DesignType.QUERY.name().equals(designType)){
            		node.setParentId(queryNodeId);
            		node.setSectionBeanId("vreportQuerySection");
            	}else{
            		node.setParentId(statsNodeId);
            		node.setSectionBeanId("vreportStatsSection");
            	}
	        	node.setId(nodeId);
	        	node.setSectionName(MapUtils.getString(map, "title"));
	        	node.setSingleBoardId(node.getId());
	        	Map<String, String> pp = new HashMap<String, String>();
	            //默认已选栏目名称
	        	pp.put("category", ApplicationCategoryEnum.edoc.name());
	        	node.setProperties(pp);
	        	ll.add(node);
            }
            
            if(sectionNames.contains("edocStatisticsSection")) {
                Long uuid = UUIDLong.longUUID();
                SectionTreeNode node = new SectionTreeNode();
                node.setId(String.valueOf(uuid));
                node.setSectionBeanId("edocStatisticsSection");
                node.setSectionName(ResourceUtil.getString("edoc.static.label"));
                node.setParentId(statsNodeId);
                ll.add(node);
            }
            if(sectionNames.contains("edocSendRegisterSection")) {
                Long uuid = UUIDLong.longUUID();
                SectionTreeNode node = new SectionTreeNode();
                node.setId(String.valueOf(uuid));
                node.setSectionBeanId("edocSendRegisterSection");
                node.setSectionName(ResourceUtil.getString("edoc.sendbook.label"));
                node.setParentId(statsNodeId);
                ll.add(node);
            }
            if(sectionNames.contains("edocRecRegisterSection")) {
                Long uuid = UUIDLong.longUUID();
                SectionTreeNode node = new SectionTreeNode();
                node.setId(String.valueOf(uuid));
                node.setSectionBeanId("edocRecRegisterSection");
                node.setSectionName(ResourceUtil.getString("edoc.recbook.label"));
                node.setParentId(statsNodeId);
                ll.add(node);
            }
            if(sectionNames.contains("edocReturnSection")) {
            	Long uuid = UUIDLong.longUUID();
            	SectionTreeNode node = new SectionTreeNode();
            	node.setId(String.valueOf(uuid));
            	node.setSectionBeanId("edocReturnSection");
            	node.setSectionName(ResourceUtil.getString("edoc.return.title"));
            	ll.add(node);
            }
        }
        return ll;
    }
    /**
     * 公文栏目预置
     * @author xiangfan
     * @return
     */

    //已办公文
    public SectionTreeNode addEdocDoneScetion(){
        Map<String, String> params = new HashMap<String, String>();
        //默认已选栏目名称
        params.put(PropertyName.columnsName.name(), ResourceUtil.getString("edoc.section.done.label"));
        //单列表
        params.put("columnsStyle", "orderList");
        //8行
        params.put("count", count);
        //字段：标题，接受时间，发起人，公文文号，发文单位，类型
        params.put("rowList", "subject,receiveTime,sendUser,edocMark,sendUnit,category");
        //流程来源：流程分类
        params.put("panel", "track_catagory");
        //流程分类：公文
        params.put("track_catagory_value", "catagory_edoc");

        return buildSectionTreeNode("doneSection", ResourceUtil.getString("edoc.section.done.label"), params);
    }

    //待办公文
    public SectionTreeNode addEdocPendingSection(){
        Map<String, String> params = new HashMap<String, String>();
        //默认已选栏目名称
        params.put(PropertyName.columnsName.name(), ResourceUtil.getString("edoc.section.pending.label"));
        //单列表
        params.put("columnStyle", "orderList");
        //8行
        params.put("count", count);
        //字段：标题，接受时间，发起人，公文文号，发文单位，类型
        params.put("rowList", "subject,receiveTime,sendUser,edocMark,sendUnit,category");
        //到期提醒
        params.put("dueToRemind", "1");
        //统计图
        params.put("graphical_value", "handleType,exigency,overdue,handlingState");
//        //流程来源：流程分类
//        params.put("panel", "track_catagory");
        //流程分类：公文
//        params.put("track_catagory_value", "catagory_edoc");
        params.put("panel","sources");
        params.put("sources_name",ResourceUtil.getString("collaboration.pending.panel.combination.sources"));
        params.put("sources_track_catagory","name,value");
        params.put("sources_track_catagory_value","catagory_edoc");
        params.put("sources_track_catagory_name","track_catagory");
        params.put("sources_relation","name,select");
        params.put("sources_relation_select","");
        params.put("sources_relation_name","relation");


        return buildSectionTreeNode("pendingSection", ResourceUtil.getString("edoc.section.pending.label"), params);
    }

    //已发公文
    public SectionTreeNode addEdocSentSection(){
        Map<String, String> params = new HashMap<String, String>();
        //默认已选栏目名称
        params.put(PropertyName.columnsName.name(), ResourceUtil.getString("edoc.section.sent.label"));
        params.put("columnsStyle", "orderList");
        params.put("count", count);
        params.put("rowList", "subject,edocMark,publishDate,type");
        params.put("panel", "track_catagory");
        params.put("track_catagory_value", "catagory_edoc");

        return buildSectionTreeNode("sentSection", ResourceUtil.getString("edoc.section.sent.label"), params);
    }

    //公文督办
    public SectionTreeNode addEdocSuperviseSection(){
        Map<String, String> params = new HashMap<String, String>();
        //默认已选栏目名称
        params.put(PropertyName.columnsName.name(), ResourceUtil.getString("edoc.section.supervise.label"));
        params.put("columnsStyle", "orderList");
        params.put("count", count);
        params.put("rowList", "subject,receiveTime,sendUser,category");
        params.put("panel", "track_catagory");
        params.put("track_catagory_value", "catagory_edoc");

        return buildSectionTreeNode("superviseSection", ResourceUtil.getString("edoc.section.supervise.label"), params);
    }

    //公文模板
    public SectionTreeNode addEdocTemplateSection(){
        Map<String, String> params = new HashMap<String, String>();
        //默认已选栏目名称
        params.put(PropertyName.columnsName.name(), ResourceUtil.getString("edoc.section.template.label"));
        params.put("count", "16");
        params.put("recent", "10");
        params.put("panel", "track_catagory");
        params.put("track_catagory_value", "catagory_edoc");

        return buildSectionTreeNode("templeteSection", ResourceUtil.getString("edoc.section.template.label"), params);
    }

    private static SectionTreeNode buildSectionTreeNode(String beanId, String name, Map<String, String> params){
        SectionTreeNode node = new SectionTreeNode();
        Long uuid = UUIDLong.longUUID();
        node.setId(String.valueOf(uuid));
        node.setSectionBeanId(beanId);
        node.setSectionName(name);
        node.setProperties(params);
        return node;
    }

    public boolean hasEdocResourceCode(User user ,String resCode) {
    	return user.isAdmin() || user.isGroupAdmin() || user.hasResourceCode(resCode);
    }

    public boolean hasEdocMenuCode(User user) {
    	if(user.isAdmin() || user.isGroupAdmin()) {
    		return true;
    	}
    	List<?> menus = UserHelper.getMenus();
    	for(int i = 0; i < menus.size(); i++){
    	    MenuBO menu = (MenuBO) menus.get(i);
    	    if ("OfficialDocument.png".equals(menu.getIcon())){
    	        return true;
    	    }
    	}
        return false;
    }
}
