package com.seeyon.ctp.portal.section;

import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.utils.AgentUtil;
import com.seeyon.apps.edoc.api.EdocApi;
import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.bo.AffairCondition;
import com.seeyon.ctp.common.affair.bo.PendingRow;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.manager.PendingManager;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.CustomizeConstants;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.customize.manager.CustomizeManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.template.dao.TemplateDao;
import com.seeyon.ctp.common.template.manager.TemplateCategoryManager;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.portal.api.PortalApi;
import com.seeyon.ctp.portal.po.StatisticalChart;
import com.seeyon.ctp.portal.section.util.SectionUtils;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class PendingController extends BaseController {

	private static final Log log = LogFactory.getLog(PendingController.class);
    private PortalApi portalApi;
    private PendingManager               pendingManager;
    private AffairManager                affairManager;
    private EdocApi                      edocApi;
    private PendingSection pendingSection;
    private TemplateDao					 templateDao;
    private TemplateManager				 templateManager;
    private OrgManager					 orgManager;
    private TemplateCategoryManager		 templateCategoryManager;
    private AgentSection				 agentSection;
    private CustomizeManager customizeManager;

	public void setCustomizeManager(CustomizeManager customizeManager) {
		this.customizeManager = customizeManager;
	}

    public AgentSection getAgentSection() {
		return agentSection;
	}

	public void setAgentSection(AgentSection agentSection) {
		this.agentSection = agentSection;
	}

	public TemplateCategoryManager getTemplateCategoryManager() {
		return templateCategoryManager;
	}

	public void setTemplateCategoryManager(TemplateCategoryManager templateCategoryManager) {
		this.templateCategoryManager = templateCategoryManager;
	}

	public OrgManager getOrgManager() {
		return orgManager;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	public TemplateManager getTemplateManager() {
		return templateManager;
	}

	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}

	public TemplateDao getTemplateDao() {
		return templateDao;
	}

	public void setTemplateDao(TemplateDao templateDao) {
		this.templateDao = templateDao;
	}

	public PendingSection getPendingSection() {
		return pendingSection;
	}

	public void setPendingSection(PendingSection pendingSection) {
		this.pendingSection = pendingSection;
	}

	public void setPortalApi(PortalApi portalApi) {
        this.portalApi = portalApi;
    }

    public void setPendingManager(PendingManager pendingManager) {
        this.pendingManager = pendingManager;
    }

    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }

    public void setEdocApi(EdocApi edocApi) {
        this.edocApi = edocApi;
    }

    @SuppressWarnings("unchecked")
    public ModelAndView transPendingMain(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView modelAndView=new ModelAndView("apps/collaboration/pendingMain");
		User user = AppContext.getCurrentUser();
		Long memberId = user.getId();
		Long fragmentId = Long.parseLong(request.getParameter("fragmentId"));
		String sectionId = request.getParameter("sectionId");
		String sectionName = request.getParameter("sectionName");
		modelAndView.addObject("sectionId", sectionId);
        String ordinal = request.getParameter("ordinal");
        List<String> graphicalList=new ArrayList<String>();
        int pageSize=8;//每页的行数默认8行
        int width=0;
        String widthStr = request.getParameter("width");
        if(Strings.isNotBlank(widthStr)){
        	width= Integer.valueOf(widthStr);
        }
        Map<String, String> preference = portalApi.getPropertys(fragmentId, ordinal);
        //选择的统计图
        String graphical = preference.get("graphical_value");
        if(Strings.isBlank(graphical)){
        	graphical="importantLevel,overdue,handlingState,handleType";
        }
        String[] graphicalArr=graphical.split(",");
        graphicalList= Arrays.asList(graphicalArr);

        //提前几天提醒
        String remindDays=preference.get("dueToRemind");
        Integer dueToRemind=0;
        if(Strings.isNotBlank(remindDays)){
        	dueToRemind= Integer.valueOf(preference.get("dueToRemind"));
        }
        //columnStyle=orderList
        String currentPanel = SectionUtils.getPanel("all", preference);
        //显示列
  		String rowStr = preference.get("rowList");

        String countStr = preference.get("count");
        if (Strings.isNotBlank(countStr)) {
            pageSize = Integer.parseInt(countStr);//设置行数
        }

  		//判断是双列表、单列表或者列表+统计图listAndStatisticalGraph
        String columnsStyle = Strings.escapeNULL(preference.get("columnStyle"), "orderList");


        if("doubleList".equals(columnsStyle)){
        	columnsStyle = "orderList";
        }

  		if(Strings.isBlank(rowStr)){
  			rowStr = "subject,receiveTime,sendUser,category";
  		}
  		List<String> columnHeaderList=new ArrayList<String>();
  		String[] rows = rowStr.split(",");
  		for(String row : rows){
  			columnHeaderList.add(row.trim());
  		}
        List<CtpAffair> affairs = null;
        if ("agentSection".equals(sectionName)) {
            columnsStyle = "orderList";
            currentPanel = "agent";

            AffairCondition condition = new AffairCondition(memberId, StateEnum.col_pending);
            Object[] agentObj = AgentUtil.getUserAgentToMap(memberId);
            boolean agentToFlag = (Boolean) agentObj[0];
            Map<Integer, List<AgentModel>> map = (Map<Integer, List<AgentModel>>) agentObj[1];
            condition.setAgent(agentToFlag, map);

            FlipInfo fi = new FlipInfo();
            fi.setNeedTotal(false);
            fi.setPage(1);
            fi.setSize(pageSize);
            fi.setSortField("receiveTime");
            fi.setSortOrder("desc");

            affairs = condition.getAgentPendingAffair(affairManager, fi);
        } else {
            affairs = this.pendingManager.getPendingList(memberId, fragmentId, ordinal, pageSize);
        }
		//列表+统计图，统计各种统计图相关的数据（如果数据量太大会有性能问题）
  		/*if("listAndStatisticalGraph".equals(columnsStyle)){
  			Map params=new HashMap();
  			params.put("fragmentId", fragmentId);
  			params.put("ordinal", ordinal);
            pendingManager.transInitChartData(params);
        }*/

		//CtpAffair转换成PendingRow
		List<PendingRow> rowList = this.pendingManager.affairList2PendingRowList(affairs,user, currentPanel, true,rowStr, StateEnum.col_pending.key());

        PendingRow pr1 = new PendingRow();
        int count = pageSize - rowList.size();
        for (int j = 0; j < count; j++) {
            rowList.add(pr1);
        }

        modelAndView.addObject("rowList1", rowList);
        modelAndView.addObject("pageSize", pageSize);
        modelAndView.addObject("dueToRemind", dueToRemind);
        modelAndView.addObject("columnsStyle", columnsStyle);
        modelAndView.addObject("graphicalList", graphicalList);
        modelAndView.addObject("columnHeaderList", columnHeaderList);
        modelAndView.addObject("currentPanel",currentPanel);
        modelAndView.addObject("fragmentId",fragmentId);
        modelAndView.addObject("ordinal",ordinal);
        modelAndView.addObject("width", width);
        modelAndView.addObject("aiSortValue",preference.get("aiSortValue"));

        return modelAndView;
	}

	//TODO 统计图文档跳转，任务完成后删除该方法
	public ModelAndView showTestChart(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("apps/report/chart/testChart");
		return mav;
	}

	/**
     * 更多待办事项
     */
	@SuppressWarnings("unchecked")
	public ModelAndView morePending(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	ModelAndView modelAndView = new ModelAndView("apps/collaboration/morePending");
    	FlipInfo fi = new FlipInfo();
        String fragmentId = request.getParameter("fragmentId");
        String proertyFrom=request.getParameter("propertyFrom");
        String propertyId=request.getParameter("propertyId");
        String ordinal = request.getParameter("ordinal");
        String rowStr=request.getParameter("rowStr");
        String columnsName=request.getParameter("columnsName");

        String aiSortValue = "0";
        String actionFrom = "";

        if(Strings.isBlank(fragmentId) && Strings.isBlank(ordinal) &&
        		Strings.isBlank(columnsName) && "subject,receiveTime,sendUser,category".equals(rowStr)) {
        	String aiSortJSON = customizeManager.getCustomizeValue(AppContext.currentUserId(), CustomizeConstants.PENDING_CENTER_AI_SORT);
        	Map<String, String> aiSortMap = JSONUtil.parseJSONString(aiSortJSON, Map.class);
        	if(aiSortMap != null && Strings.isNotBlank(aiSortMap.get("pendingWork"))) {
        		aiSortValue = aiSortMap.get("pendingWork");
			}
        	modelAndView.addObject("aiSortValue", aiSortValue);
        	modelAndView.addObject("openFrom", "pendingWork");
        }
        if(Strings.isBlank(rowStr)){
        	Map<String, String> preference = new HashMap<String, String>();
            String fromM1 = request.getParameter("fromM1");
            if(Strings.isNotBlank(fromM1)) {
                preference = (Map<String, String>)JSONUtil.parseJSONString(fromM1);
            } else if(fragmentId!= null && ordinal != null){
            	String fragmentIdStr=fragmentId.toString();
            	if(Strings.isNotBlank(fragmentIdStr)){
            		Long fragmentIdl = Long.parseLong(fragmentIdStr);
            		preference = portalApi.getPropertys(fragmentIdl, ordinal.toString());
            	}
            }else if(Strings.isNotBlank(proertyFrom)&&Strings.isNotBlank(propertyId)&&Strings.isDigits(propertyId)){
            	preference=portalApi.getPropertyByCategoryAndId(proertyFrom, Long.valueOf(propertyId));
			}
            rowStr = preference.get("rowList");
            if (Strings.isBlank(rowStr)) {
            	if (Strings.equals(request.getParameter("from"), "Agent")) {
            		rowStr = "subject,receiveTime,sendUser,deadLine,category";
            	} else {
            		rowStr="subject,receiveTime,sendUser,category";
            	}
            }
        }
        Map<String, Object> query = new HashMap<String, Object>();
        query.put("fragmentId", fragmentId);
        query.put("ordinal", ordinal);
        query.put("state", StateEnum.col_pending.key());
        query.put("isTrack", false);
        query.put("aiSortValue", aiSortValue);
        query.put("proertyFrom",proertyFrom);
        query.put("propertyId",propertyId);

        String myRemind = request.getParameter("myRemind");
        String from = request.getParameter("from");
        //首页 -代理列表
        if(Strings.equals(from, "Agent")){
        	actionFrom = "Agent";
            query.put("rowStr",rowStr);
            pendingManager.getMoreAgentList4SectionContion(fi, query);
        }
        else{
            if (Strings.isNotBlank(myRemind)) {
                query.put("myRemind", myRemind);
            }
            pendingManager.getMoreList4SectionContion(fi, query);
        }
        //判断是否有待登记的权限
        User user = AppContext.getCurrentUser();
        if(AppContext.hasPlugin("edoc")){
            boolean isCreate = edocApi.isEdocCreateRole(user.getId(),user.getLoginAccount(), EdocEnum.edocType.recEdoc.ordinal());
            modelAndView.addObject("isRegistRole", isCreate);
        }
        request.setAttribute("ffmoreList", fi);
        fi.setParams(query);
        modelAndView.addObject("params", query);
        modelAndView.addObject("columnsName", columnsName);
        modelAndView.addObject("total", fi.getTotal());
        if(Strings.isEmpty(fi.getData())){
        	log.info("数据量："+fi.getTotal()+",data="+fi.getData());
        }else{
        	if(fi.getData().get(0) instanceof PendingRow){
        		log.info("数据量："+fi.getTotal()+",data="+((PendingRow)(fi.getData().get(0))).getSubject());
        	}
        }

        modelAndView.addObject("rowStr",rowStr);
        boolean hasAIPlugin = AppContext.hasPlugin("ai");
        //从首页快捷 超期事项、代理事项 打开的时候，不显示智能排序按钮
        if("overTime".equals(myRemind) || "Agent".equals(from)) {
        	hasAIPlugin = false;
        }
        modelAndView.addObject("hasAIPlugin", hasAIPlugin);
        modelAndView.addObject("actionFrom", actionFrom);

        WFComponentUtil.putImportantI18n2Session();

    	return modelAndView;
    }

	public ModelAndView showLeftList(HttpServletRequest request,
    		HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        ModelAndView modelAndView = new ModelAndView("apps/collaboration/showLeftList");
        String dataName = request.getParameter("dataName");
        String pageSize = request.getParameter("pageSize");
        String columnHeaderStr = request.getParameter("columnHeaderStr");
        String currentPanel = request.getParameter("currentPanel");
        String _fragmentId = request.getParameter("fragmentId");
        Long fragmentId = Long.valueOf(_fragmentId);
        String ordinal = request.getParameter("ordinal");
        String sectionId = request.getParameter("sectionId");
        modelAndView.addObject("sectionId", sectionId);
    	int width=0;
        String widthStr = request.getParameter("width");
        if(Strings.isNotBlank(widthStr)){
        	width= Integer.valueOf(widthStr);
        }
    	Map<String, String> preference = portalApi.getPropertys(fragmentId, ordinal);
    	Long memberId=user.getId();
    	List<String> columnHeaderList=new ArrayList<String>();
    	String[] columnNames=columnHeaderStr.split(",");
    	for(String c:columnNames){
    		columnHeaderList.add(c.trim());
    	}
        int pageCount = 8;
        if (Strings.isNotBlank(pageSize)) {
            pageCount = Integer.valueOf(pageSize);
        }
//    	List<ApplicationCategoryEnum> apps = new ArrayList<ApplicationCategoryEnum>();
//		apps.add(ApplicationCategoryEnum.collaboration);
//		apps.add(ApplicationCategoryEnum.edocSend);//发文 19
//		apps.add(ApplicationCategoryEnum.edocRec);//收文 20
//		apps.add(ApplicationCategoryEnum.edocSign);//签报21
//		apps.add(ApplicationCategoryEnum.exSend);//待发送公文22
//		apps.add(ApplicationCategoryEnum.exSign);//待签收公文 23
//		apps.add(ApplicationCategoryEnum.edocRegister);//待登记公文 24
//		apps.add(ApplicationCategoryEnum.edocRecDistribute);//收文分发34
//		apps.add(ApplicationCategoryEnum.meeting);
//		apps.add(ApplicationCategoryEnum.bulletin);
//		apps.add(ApplicationCategoryEnum.inquiry);
//		apps.add(ApplicationCategoryEnum.news);
//		apps.add(ApplicationCategoryEnum.office);
//		apps.add(ApplicationCategoryEnum.info);

    	FlipInfo fp=new FlipInfo();
    	fp.setSize(pageCount);
    	List<CtpAffair> affairList = new ArrayList<CtpAffair>();

    	List<Integer> colEnums =new ArrayList<Integer>();
		colEnums.add(ApplicationCategoryEnum.collaboration.getKey());//协同
		List<Integer> edocEnums =new ArrayList<Integer>();
		edocEnums.add(ApplicationCategoryEnum.edocSend.getKey());//发文 19
		edocEnums.add(ApplicationCategoryEnum.edocRec.getKey());//收文 20
		edocEnums.add(ApplicationCategoryEnum.edocSign.getKey());//签报21
		edocEnums.add(ApplicationCategoryEnum.exSend.getKey());//待发送公文22
		edocEnums.add(ApplicationCategoryEnum.exSign.getKey());//待签收公文 23
		edocEnums.add(ApplicationCategoryEnum.edocRegister.getKey());//待登记公文 24
		edocEnums.add(ApplicationCategoryEnum.edocRecDistribute.getKey());//收文分发34
		//添加排序
		fp.setSortField("receiveTime");
        fp.setSortOrder("desc");
    	if("pending".equals(dataName)){
    		affairList=pendingManager.getPendingAffairList(fp,memberId,preference);
    	}else if("zcdb".equals(dataName)){
    		affairList=pendingManager.getZcdbAffairList(fp,memberId,preference);
    	}else if("import3".equals(dataName)){
    		affairList=pendingManager.getAffairsByCategoryAndImpLevl(fp,colEnums, 3,memberId,preference);//非常重要
    	}else if("import2".equals(dataName)){
    		affairList=pendingManager.getAffairsByCategoryAndImpLevl(fp,colEnums, 2,memberId,preference);//重要
    	}else if("import1".equals(dataName)){
    		affairList=pendingManager.getAffairsByCategoryAndImpLevl(fp,colEnums, 1,memberId,preference);//普通
    	}else if("overdue".equals(dataName)){
    		affairList=pendingManager.getAffairsIsOverTime(fp,memberId,preference,true);//已超期
    	}else if("noOverdue".equals(dataName)){
    		affairList=pendingManager.getAffairsIsOverTime(fp,memberId,preference,false);//未超期
    	}else if("teTi".equals(dataName)){
            affairList=pendingManager.getAffairsByCategoryAndImpLevl(fp,edocEnums, 5,memberId,preference);//特提
        }else if("urgent".equals(dataName)){
            affairList=pendingManager.getAffairsByCategoryAndImpLevl(fp,edocEnums, 4,memberId,preference);//紧急
    	}else if("expedited".equals(dataName)){
    		affairList=pendingManager.getAffairsByCategoryAndImpLevl(fp,edocEnums, 3,memberId,preference);//加急
    	}else if("pingAnxious".equals(dataName)){
    		affairList=pendingManager.getAffairsByCategoryAndImpLevl(fp,edocEnums, 2,memberId,preference);//平急
    	}else if("commonExigency".equals(dataName)){
    		affairList=pendingManager.getAffairsByCategoryAndImpLevl(fp,edocEnums, 1,memberId,preference);//普通
    	}else if("zyxt".equals(dataName)){
    		affairList=pendingManager.getCollAffairs(fp,memberId,preference,false);//自由协同，
    	}else if("xtbdmb".equals(dataName)){
    		affairList=pendingManager.getCollAffairs(fp,memberId,preference,true);//协同/表单模板，
    	}else if("shouWen".equals(dataName)){
    		List<Integer> appEnums = new ArrayList<Integer>();
    		appEnums.add(ApplicationCategoryEnum.edocRec.key());
    		affairList=pendingManager.getAffairCountByApp(fp,memberId,preference,appEnums);//收文，
    	}else if("faWen".equals(dataName)){
    		List<Integer> appEnums = new ArrayList<Integer>();
    		appEnums.add(ApplicationCategoryEnum.edocSend.key());
    		affairList=pendingManager.getAffairCountByApp(fp,memberId,preference,appEnums);//发文，
    	}else if("daiFaEdoc".equals(dataName)){
    		List<Integer> appEnums = new ArrayList<Integer>();
    		appEnums.add(ApplicationCategoryEnum.exSend.key());
    		affairList=pendingManager.getAffairCountByApp(fp,memberId,preference,appEnums);//待发送，
    	}else if("daiQianShou".equals(dataName)){
    		List<Integer> appEnums = new ArrayList<Integer>();
    		appEnums.add(ApplicationCategoryEnum.exSign.key());
    		affairList=pendingManager.getAffairCountByApp(fp,memberId,preference,appEnums);//待签收，
    	}else if("daiDengJi".equals(dataName)){
    		List<Integer> appEnums = new ArrayList<Integer>();
    		appEnums.add(ApplicationCategoryEnum.edocRegister.key());
    		affairList=pendingManager.getAffairCountByApp(fp,memberId,preference,appEnums);//待登记，
    	}else if("qianBao".equals(dataName)){
    		List<Integer> appEnums = new ArrayList<Integer>();
    		appEnums.add(ApplicationCategoryEnum.edocSign.key());
    		affairList=pendingManager.getAffairCountByApp(fp,memberId,preference,appEnums);//签报，
    	}else if("huiYi".equals(dataName)){
    		List<Integer> appEnums = new ArrayList<Integer>();
    		appEnums.add(ApplicationCategoryEnum.meeting.key());
    		affairList=pendingManager.getAffairCountByApp(fp,memberId,preference,appEnums);//会议，
    	}else if("huiYiShi".equals(dataName)){
    		List<Integer> appEnums = new ArrayList<Integer>();
    		appEnums.add(ApplicationCategoryEnum.meetingroom.key());
    		affairList=pendingManager.getAffairCountByApp(fp,memberId,preference,appEnums);//会议室审批，
    	}else if("daiShenPiGGXX".equals(dataName)){
    		//公告，新闻，调查，讨论
	    	List<Integer> publicInfoEnums = new ArrayList<Integer>();
	    	publicInfoEnums.add(ApplicationCategoryEnum.news.getKey());// 新闻
    		affairList=pendingManager.getAffairCountByApp(fp,memberId,preference,publicInfoEnums);//待审批公共信息 (公告，新闻，调查，讨论)
    	}else if("daiShenPiZHBG".equals(dataName)){
    		List<Integer> appEnums = new ArrayList<Integer>();
    		appEnums.add(ApplicationCategoryEnum.office.key());
    		affairList=pendingManager.getAffairCountByApp(fp,memberId,preference,appEnums);//待审批综合办公审批
    	}else if("diaoCha".equals(dataName)){
    		List<Integer> appEnums = new ArrayList<Integer>();
    		appEnums.add(ApplicationCategoryEnum.inquiry.key());
    		affairList=pendingManager.getAffairCountByApp(fp,memberId,preference,appEnums);//调查
    	}
    	List<PendingRow> rowList = this.pendingManager.affairList2PendingRowList(affairList,user, currentPanel, false,columnHeaderStr,StateEnum.col_pending.key());

    	List<PendingRow> rowList1=rowList;
        PendingRow pr1 = new PendingRow();
        int count = pageCount - rowList.size();
        for (int j = 0; j < count; j++) {
            rowList1.add(pr1);
        }
    	modelAndView.addObject("columnHeaderList", columnHeaderList);
        modelAndView.addObject("leftList", rowList1);
        modelAndView.addObject("width", width);
    	return modelAndView;
    }

	/**
	 * 流程图选择和排序
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
    public ModelAndView showStatisticalChart(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView modelAndView = new ModelAndView("apps/collaboration/statisticalChart_choose");
		List<StatisticalChart> allList=new ArrayList<StatisticalChart>();
		List<StatisticalChart> leftList=new ArrayList<StatisticalChart>();
		//办理类型
		StatisticalChart handleType=new StatisticalChart("handleType","collaboration.statisticalChart.handleType.label");
		allList.add(handleType);
		//重要程度
		StatisticalChart importantLevel=new StatisticalChart("importantLevel","collaboration.statisticalChart.importantLevel.label");
		allList.add(importantLevel);
		//紧急程度（公文）
		if (AppContext.hasPlugin("edoc")) {
			StatisticalChart exigency=new StatisticalChart("exigency","collaboration.statisticalChart.exigency.label");
			allList.add(exigency);
		}
		//是否超期
		StatisticalChart overdue=new StatisticalChart("overdue","collaboration.statisticalChart.overdue.label");
		allList.add(overdue);
		//办理状态
		StatisticalChart handlingState=new StatisticalChart("handlingState","collaboration.statisticalChart.handlingState.label");
		allList.add(handlingState);
		leftList=allList;

		modelAndView.addObject("metadata", leftList);
		return modelAndView;
	}
	/**
	 * 处理状态选择
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView handlingState(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("apps/collaboration/handlingState_choose");
		if("waitSendSection".equals(request.getParameter("comeFrom"))){
		  mav.addObject("waitSendSection","1");
		}
		return mav;
	}
	/**
	 * 待办中心
	 */
	public ModelAndView morePendingCenter(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("apps/collaboration/morePendingCenter");
        Long memberId = AppContext.currentUserId();
		//页签
		String source = request.getParameter("source");
		String section = request.getParameter("section");
		String from = request.getParameter("from");
		String fragmentId = request.getParameter("fragmentId");
		String ordinal = request.getParameter("ordinal");
		String currentPanel = request.getParameter("currentPanel");
		String rowStr = request.getParameter("rowStr");
		String columnsName = request.getParameter("columnsName");
		String portalId = request.getParameter("portalId");
		String spaceId = request.getParameter("spaceId");

		mav.addObject("memberId", memberId);
		mav.addObject("source", source);
		mav.addObject("from", from);
		mav.addObject("fragmentId", fragmentId);
		mav.addObject("ordinal", ordinal);
		mav.addObject("currentOrdinal", ordinal);
		mav.addObject("currentPanel", currentPanel);
		mav.addObject("rowStr", rowStr);
		mav.addObject("currentRowStr", rowStr);
		mav.addObject("columnsName", columnsName);
		mav.addObject("portalId", portalId);
		mav.addObject("spaceId", spaceId);
		mav.addObject("isV5Member", AppContext.getCurrentUser().getExternalType() == 0);

		mav.addObject("hasAIPlugin", AppContext.hasPlugin("ai"));

		return mav;
	}
	/**
     * 待办更多——grid数据加载
     */
	public ModelAndView morePendingAll(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	ModelAndView modelAndView = new ModelAndView("apps/collaboration/morePending");
    	FlipInfo fi = new FlipInfo();
        String fragmentId = request.getParameter("fragmentId");
        String ordinal = request.getParameter("ordinal");
        String rowStr=request.getParameter("rowStr");
        String columnsName=request.getParameter("columnsName");
        String source = request.getParameter("source");
        String section = request.getParameter("section");
        String from = request.getParameter("from");
        String x = (null==request.getParameter("x")) ? "" : request.getParameter("x");
        String y = (null==request.getParameter("y")) ? "" : request.getParameter("y");
        String spaceId = (null==request.getParameter("spaceId")) ? "" : request.getParameter("spaceId");
        String actionFrom = "";

        boolean isRecentTemplate = false;
        if(Strings.isBlank(rowStr)){
        	Map<String, String> preference = new HashMap<String, String>();
            if(fragmentId!= null && ordinal != null){
            	String fragmentIdStr=fragmentId.toString();
            	if(Strings.isNotBlank(fragmentIdStr)){
            		Long fragmentIdl = Long.parseLong(fragmentIdStr);
            		preference = portalApi.getPropertys(fragmentIdl, ordinal.toString());
            	}
            }
            rowStr = preference.get("rowList");
            if (Strings.isBlank(rowStr)) {
            	rowStr="subject,receiveTime,sendUser,category";
            }
        }
        Map<String, Object> query = new HashMap<String, Object>();
        query.put("state", StateEnum.col_pending.key());
        query.put("isTrack", false);
        query.put("fragmentId", fragmentId);
        query.put("ordinal", ordinal);
        query.put("rowStr", rowStr);
        boolean updatePendingCache = false;
        String aiSortValue = "0";

        String aiSortJSON = customizeManager.getCustomizeValue(AppContext.currentUserId(), CustomizeConstants.PENDING_CENTER_AI_SORT);
        Map<String, String> aiSortMap = JSONUtil.parseJSONString(aiSortJSON, Map.class);
       if ("Common".equals(source)) { //来自栏目分类
    	   if ("All".equals(from)) { //全部
    		   query.put("panel", "all");
			   if(aiSortMap != null && Strings.isNotBlank(aiSortMap.get("sectionAll"))) {
				   aiSortValue = aiSortMap.get("sectionAll");
			   }
			   query.put("aiSortValue", aiSortValue);
           } else if ("Agent".equals(from)) {
			   if(aiSortMap != null && Strings.isNotBlank(aiSortMap.get("sectionAgent"))) {
				   aiSortValue = aiSortMap.get("sectionAgent");
			   }
			   query.put("aiSortValue", aiSortValue);
			   actionFrom = "Agent";
           } else  if (Strings.isNotBlank(section)){
        	   updatePendingCache = true;
        	   String[] args = section.split("_");
        	   if (args.length > 1) {
        		   fragmentId = args[0];
        		   ordinal = args[1];
        		   Map<String, String> preference = portalApi.getPropertys(Long.valueOf(fragmentId), ordinal);
        		   if (preference != null) {
        			   rowStr = preference.get("rowList");
        			   if (Strings.isBlank(rowStr)) {
        	            	rowStr="subject,receiveTime,sendUser,category";
        	           }
        			   aiSortValue = preference.get("aiSortValue");
        		   }
        	   }
           }
    	   query.put("rowStr", rowStr);
    	   query.put("fragmentId", fragmentId);
    	   query.put("ordinal", ordinal);
    	   query.put("aiSortValue", aiSortValue);
        } else if ("Template".equals(source)) { //模板分类
        	String categoryId = request.getParameter("categoryId");
            if (Strings.isBlank(categoryId)) {
            	categoryId =  "C_-1";
                if(AppContext.hasPlugin("collaboration")){
                	categoryId += ",C_1,C_2";
                }
                if(AppContext.hasPlugin("edoc")){
                	categoryId += ",C_4,C_19,C_20,C_21";
                }
                if(AppContext.hasPlugin("infosend")){
                	categoryId += ",C_32";
                }
            } else if ("C_0".equals(categoryId)){
            	categoryId =  "C_-1";
                if(AppContext.hasPlugin("collaboration")){
                	categoryId += ",C_1,C_2";
                }
                if(AppContext.hasPlugin("infosend")){
                	categoryId += ",C_32";
                }
            } else if("C_10".equals(categoryId)) {
            	isRecentTemplate = true;
            	query.put("from", "Recent");
            	actionFrom = "Recent";
            }
            query.put("templatePanel", categoryId);

            if(aiSortMap != null && Strings.isNotBlank(aiSortMap.get("template"))) {
        		aiSortValue = aiSortMap.get("template");
        	}

            query.put("aiSortValue", aiSortValue);
        } else if ("Staff".equals(source)) { //组织分类
        	String departmentId = request.getParameter("departmentId");
        	if (!Strings.isBlank(departmentId)) {
        		query.put("senderPanel", departmentId);
        	}

        	if(aiSortMap != null && Strings.isNotBlank(aiSortMap.get("staff"))) {
        		aiSortValue = aiSortMap.get("staff");
        	}

            query.put("aiSortValue", aiSortValue);
        }
       if (rowStr.indexOf("processingProgress") != -1) {
    	   query.put("isNeedReplayCounts", true);//需要获取回复数
       }
	   if (isRecentTemplate) {
		   pendingManager.getMoreRecentList4SectionContion(fi, query);
	   }else if("Agent".equals(from)){//首页 -代理列表
           pendingManager.getMoreAgentList4SectionContion(fi, query);
       } else {
    	   pendingManager.getMoreList4SectionContion(fi, query);
       }

        //判断是否有待登记的权限
        User user = AppContext.getCurrentUser();
        if(AppContext.hasPlugin("edoc")){
            boolean isCreate = edocApi.isEdocCreateRole(user.getId(),user.getLoginAccount(), EdocEnum.edocType.recEdoc.ordinal());
            modelAndView.addObject("isRegistRole", isCreate);
        }
        request.setAttribute("ffmoreList", fi);
        fi.setParams(query);
        if (isRecentTemplate) {
        	query.put("TemplateTypePanel", "recentTemplate");
        } else if ("Template".equals(source)) {
        	query.put("TemplateTypePanel", "template");
        }
        modelAndView.addObject("params", query);
        modelAndView.addObject("columnsName", columnsName);
        modelAndView.addObject("total", fi.getTotal());

        int count = fi.getTotal();
        if (updatePendingCache) {
        	pendingSection.updatePendingCountMaps(section, count);
        }
        modelAndView.addObject("currentCount", count);
        modelAndView.addObject("pendingCountCacheKey", section);
        if(Strings.isEmpty(fi.getData())){
        	log.info("数据量："+fi.getTotal()+",data="+fi.getData());
        }else{
        	if(fi.getData().get(0) instanceof PendingRow){
        		log.info("数据量："+fi.getTotal()+",data="+((PendingRow)(fi.getData().get(0))).getSubject());
        	}
        }
        modelAndView.addObject("x", x);
        modelAndView.addObject("y", y);
        modelAndView.addObject("spaceId", spaceId);
        modelAndView.addObject("fragmentId", fragmentId);
        modelAndView.addObject("ordinal", ordinal);
        modelAndView.addObject("aiSortValue", aiSortValue);
		String sub="sendUser,";
		String p1=rowStr.substring(0,rowStr.indexOf(sub)+sub.length())+"sendUserUnit,"+rowStr.substring(rowStr.indexOf(sub)+sub.length());
        modelAndView.addObject("rowStr",p1);
        modelAndView.addObject("actionFrom",actionFrom);

        WFComponentUtil.putImportantI18n2Session();

        modelAndView.addObject("hasAIPlugin", AppContext.hasPlugin("ai"));

    	return modelAndView;
    }

	/**
	 * 跳转到组合查询页面
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
    public ModelAndView pendingCombinedQuery(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("apps/collaboration/pending_com_query");
        modelAndView.addObject("isV5Member", AppContext.getCurrentUser().getExternalType() == 0);
        modelAndView.addObject("pageFrom",request.getParameter("pagefrom"));
        WFComponentUtil.putImportantI18n2Session();
        return modelAndView;
    }

}
