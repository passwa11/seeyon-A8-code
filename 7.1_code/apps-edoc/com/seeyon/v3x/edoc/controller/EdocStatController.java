/**
 * 
 */
package com.seeyon.v3x.edoc.controller;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.doc.bo.DocResourceBO;
import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.apps.govdoc.helper.GovdocRoleHelper;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.excel.DataRecord;
import com.seeyon.ctp.common.excel.DataRow;
import com.seeyon.ctp.common.excel.FileToExcelManager;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.i18n.LocaleContext;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgPost;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;
import com.seeyon.v3x.common.dao.paginate.Pagination;
import com.seeyon.v3x.common.metadata.MetadataNameEnum;
import com.seeyon.v3x.edoc.domain.EdocStat;
import com.seeyon.v3x.edoc.domain.EdocStatCondition;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.domain.WebEdocStat;
import com.seeyon.v3x.edoc.manager.EdocFormManager;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocManager;
import com.seeyon.v3x.edoc.manager.EdocMarkHistoryManager;
import com.seeyon.v3x.edoc.manager.EdocMarkManager;
import com.seeyon.v3x.edoc.manager.EdocPermissionControlManager;
import com.seeyon.v3x.edoc.manager.EdocStatManager;
import com.seeyon.v3x.edoc.manager.EdocSwitchHelper;
import com.seeyon.v3x.edoc.manager.statistics.ContentData;
import com.seeyon.v3x.edoc.manager.statistics.OrderContent;
import com.seeyon.v3x.edoc.manager.statistics.OrganizationDimension;
import com.seeyon.v3x.edoc.manager.statistics.SendContentImpl;
import com.seeyon.v3x.edoc.manager.statistics.StatConstants;
import com.seeyon.v3x.edoc.manager.statistics.StatContent;
import com.seeyon.v3x.edoc.manager.statistics.StatParamVO;
import com.seeyon.v3x.edoc.manager.statistics.StatisticsContentTypeEnum;
import com.seeyon.v3x.edoc.manager.statistics.StatisticsUtils;
import com.seeyon.v3x.edoc.manager.statistics.StrategyInter;
import com.seeyon.v3x.edoc.manager.statistics.TimeDimension;
import com.seeyon.v3x.edoc.manager.statistics.WorkflowNodeBean;
import com.seeyon.v3x.edoc.manager.statistics.WorkflowNodeImpl;
import com.seeyon.v3x.edoc.util.EdocStatHelper;
import com.seeyon.v3x.edoc.webmodel.EdocStatConditionModel;
import com.seeyon.v3x.exchange.manager.RecieveEdocManager;

/**
 * 类描述：
 * 创建日期：
 *
 * @author kangyutao
 * @version 1.0 
 * @since JDK 5.0
 */
@CheckRoleAccess(roleTypes={Role_NAME.Accountexchange,Role_NAME.Departmentexchange})
public class EdocStatController extends BaseController{
	
	private final String resource_common_baseName = "com.seeyon.v3x.edoc.resources.i18n.EdocResource";
	private EnumManager enumManagerNew;
	private OrgManager orgManager;
	private AttachmentManager attachmentManager;
	private EdocManager edocManager;
	private AffairManager affairManager;
	private SendContentImpl sendContentImpl;
	private WorkflowNodeImpl workflowNodeImpl;
	
	public void setWorkflowNodeImpl(WorkflowNodeImpl workflowNodeImpl) {
		this.workflowNodeImpl = workflowNodeImpl;
	}

	public void setSendContentImpl(SendContentImpl sendContentImpl) {
		this.sendContentImpl = sendContentImpl;
	}

	public void setEnumManagerNew(EnumManager enumManager) {
		this.enumManagerNew = enumManager;
	}



	private EdocPermissionControlManager edocPermissionControlManager;
	private EdocFormManager edocFormManager;
	private TemplateManager templateManager;
	private PermissionManager permissionManager;
	private RecieveEdocManager recieveEdocManager;
	private EdocMarkManager edocMarkManager;
	private EdocMarkHistoryManager edocMarkHistoryManager;
	private EdocStatManager edocStatManager;
	private FileToExcelManager fileToExcelManager;
	private LocalizationContext bundleAttrValue;
	private DocApi docApi;
	
	public void setDocApi(DocApi docApi) {
        this.docApi = docApi;
    }
	
	/**
	 * 先从指定的资源中查找，再查找默认的
	 * 
	 * @param locCtxt
	 * @throws JspTagException
	 */
    public void setBundle(LocalizationContext locCtxt) throws JspTagException {
        this.bundleAttrValue = locCtxt;
    }
	public FileToExcelManager getFileToExcelManager() {
		return fileToExcelManager;
	}

	public void setFileToExcelManager(FileToExcelManager fileToExcelManager) {
		this.fileToExcelManager = fileToExcelManager;
	}

	public EdocMarkHistoryManager getEdocMarkHistoryManager() {
		return edocMarkHistoryManager;
	}

	public void setEdocMarkHistoryManager(
			EdocMarkHistoryManager edocMarkHistoryManager) {
		this.edocMarkHistoryManager = edocMarkHistoryManager;
	}

	public AffairManager getAffairManager() {
		return affairManager;
	}

	public AttachmentManager getAttachmentManager() {
		return attachmentManager;
	}

	public EdocFormManager getEdocFormManager() {
		return edocFormManager;
	}

	public EdocManager getEdocManager() {
		return edocManager;
	}

	public EdocPermissionControlManager getEdocPermissionControlManager() {
		return edocPermissionControlManager;
	}

	public OrgManager getOrgManager() {
		return orgManager;
	}

	public PermissionManager getPermissionManager() {
		return permissionManager;
	}

	public TemplateManager getTemplateManager() {
		return templateManager;
	}

	public EdocStatManager getEdocStatManager() {
		return edocStatManager;
	}

	public void setEdocStatManager(EdocStatManager edocStatManager) {
		this.edocStatManager = edocStatManager;
	}

	public RecieveEdocManager getRecieveEdocManager() {
		return recieveEdocManager;
	}

	public EdocMarkManager getEdocMarkManager() {
		return edocMarkManager;
	}

	public void setEdocMarkManager(EdocMarkManager edocMarkManager) {
		this.edocMarkManager = edocMarkManager;
	}

	public void setRecieveEdocManager(RecieveEdocManager recieveEdocManager) {
		this.recieveEdocManager = recieveEdocManager;
	}

    public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}
	
	public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }
	
	public void setEdocFormManager(EdocFormManager edocFormManager)
	{
		this.edocFormManager=edocFormManager;
	}
	
	public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }


	public void setEdocManager(EdocManager edocManager)
	{
		this.edocManager=edocManager;
	}
	
	public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }
	
    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }
	

    public void setEdocPermissionControlManager(
            EdocPermissionControlManager edocPermissionControlManager) {
        this.edocPermissionControlManager = edocPermissionControlManager;
    }


	@Override
	public ModelAndView index(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		return null;
	}
	
	public ModelAndView statEntry(HttpServletRequest request,
			HttpServletResponse response) throws Exception {	
		ModelAndView mav = new ModelAndView("edoc/docstat/edocQueryIndex");		
		return mav;
	}
	
	public ModelAndView edocQueryTopFrame(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("edoc/docstat/edocQueryTopFrame");
		return mav;		
	}
	/**
	 * 统计查询的结果
	 * @param request
	 * @param isExcelQuery  ：是否是来自Excel导出
	 * @return
	 * @throws Exception
	 */
	private List<EdocStat> query(HttpServletRequest request,boolean isExcelQuery)throws Exception{
		//构造查询参数
		String edocTypeParameter="edocType";
		String beginDateParameter="beginDate";
		String endDateParameter="endDate";
		String flowStateParameter="flowState";
		boolean needPagination=true;
		if(isExcelQuery){
			needPagination=false;
			edocTypeParameter="_oldEdocType";
			beginDateParameter="_oldBeginDate";
			endDateParameter="_oldEndDate";
			flowStateParameter="_oldFlowState";
		}
		
		long domainId = 0;							//如果当前用户担任单位收发员，则赋当前单位id
		List<Long> deptIds = new ArrayList<Long>(); //当前用户担任收发员的部门id
		List<EdocStat> results = new ArrayList<EdocStat>();
		User user=AppContext.getCurrentUser();
		boolean isAccountExchange = GovdocRoleHelper.isAccountExchange();
		
		if (isAccountExchange) {//单位收发员：统计当前登录单位的。
			domainId =	user.getLoginAccount();
		}else {
			//部门收发员：统计当前用户在当前登录单位下的兼职部门中作为公文收发员时，兼职部门内的公文。
			//如果在兼职部门中同时担当两个兼职部门的公文收发员，则统计结果为两个个兼职部门公文之和。
			deptIds.addAll(GovdocRoleHelper.getUserExchangeDepartmentIdsToList());
		}
		
		Integer flag = ServletRequestUtils.getIntParameter(request, "flag", 0);
		
		if (flag == 1) {
			Integer edocType = ServletRequestUtils.getIntParameter(request, edocTypeParameter, 0);
			String strBdate = request.getParameter(beginDateParameter);
			String strEdate = request.getParameter(endDateParameter);
			Date beginDate = null;
			if (strBdate != null && !"".equals(strBdate)) {
				beginDate = Datetimes.getTodayFirstTime(strBdate);
			}
			Date endDate = null;
			if (strEdate != null && !"".equals(strEdate)) {
				endDate = Datetimes.getTodayLastTime(strEdate);
			}	
			
			if (edocType == EdocEnum.edocType.sendEdoc.ordinal()) {//得到的是发文的数据
				Integer flowState = ServletRequestUtils.getIntParameter(request, flowStateParameter, 0);			
				results = edocStatManager.querySentEdocStat(flowState, beginDate, endDate,  deptIds, domainId,needPagination);	
			}
			else if (edocType == EdocEnum.edocType.recEdoc.ordinal()
			        || edocType == EdocEnum.edocType.signReport.ordinal() ) {
				results = edocStatManager.queryEdocStat(edocType, beginDate, endDate, deptIds, domainId,needPagination);
			}
			else if (edocType == 999) { //查询归档公文
				results = edocStatManager.queryArchivedEdocStat(beginDate, endDate, deptIds, domainId,needPagination);
			}
			
		}
		return results;
	}
	
	public ModelAndView listQueryResult(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("edoc/docstat/listQueryResult");
		Integer flag = ServletRequestUtils.getIntParameter(request, "flag", 0);
		if (flag == 1) {
			Integer edocType = ServletRequestUtils.getIntParameter(request, "edocType", 0);
			if (edocType == EdocEnum.edocType.sendEdoc.ordinal()) {//得到的是发文的数据
				mav.addObject("resultType", "sendEdoc") ;
			}
			else if (edocType == EdocEnum.edocType.recEdoc.ordinal() ) {
				mav.addObject("resultType", "recEdoc") ;
			}else if( edocType == EdocEnum.edocType.signReport.ordinal()){
				mav.addObject("resultType", "signReport") ;
			}
			else if (edocType == 999) { //查询归档公文
				mav.addObject("resultType", "acchivedEdocStat") ;
			}
		}
		List<EdocStat> results = query(request,false);
		mav.addObject("edocStats", this.response(results));
		return mav;
	}	
	
	/**
	 *  显示统计条件－－统计入口
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView statCondition(HttpServletRequest request,
			HttpServletResponse response) throws Exception {		
		ModelAndView mav = new ModelAndView("edoc/docstat/edocStatCondition");
		Calendar cal = Calendar.getInstance();
		int curYear = cal.get(Calendar.YEAR);		
		int curMonth = cal.get(Calendar.MONTH)+1;
		int curSeason = 0;
		if (curMonth >= Calendar.JANUARY && curMonth <= Calendar.MARCH) {
			curSeason = 1;
		} 
		else if (curMonth >= Calendar.APRIL && curMonth <= Calendar.JUNE) {
			curSeason = 2;
		}
		else if (curMonth >= Calendar.JULY && curMonth <= Calendar.SEPTEMBER) {
			curSeason = 3;
		}
		else {
			curSeason = 4;
		}
		mav.addObject("curYear", curYear);			
		mav.addObject("curMonth", curMonth);  
		mav.addObject("curSeason", curSeason);
		//当前日期
		mav.addObject("curDay", Datetimes.formatDate(new Date()));
		
		mav.addObject("isG6", EdocHelper.isG6Version());        
		
		return mav;
	}
	
	
	
	
	public ModelAndView openStatContent(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		String url = "";
		//办理情况，流程节点等 大类型id
		String contentTypeId = request.getParameter("contentTypeId");
		int sContentId = Integer.parseInt(contentTypeId);
		//获取单位ID 
		User user = AppContext.getCurrentUser();
    	long loginAccount = user.getLoginAccount();
    	List statList = edocStatManager.getStatContent(sContentId,loginAccount);
    	
    	ModelAndView mav = new ModelAndView();
		StatisticsContentTypeEnum typeEnum = StatisticsContentTypeEnum.valueOf(sContentId);
		
		Map<String,Object> map = new HashMap<String,Object>();
		url = "edoc/docstat/statContent";
		if(statList.size()>0){
			switch(typeEnum){ 
			case workflowNode :
				url = "edoc/docstat/statContent-processNode";
				List sendList = (List)statList.get(0);
				List recList = (List)statList.get(1);
				mav.addObject("sendList", sendList); 
				mav.addObject("recList", recList); 
				
				for(int i=0;i<sendList.size();i++){
					Permission sc = (Permission)sendList.get(i);
					map.put(String.valueOf(sc.getFlowPermId()), sc);
				}
				for(int i=0;i<recList.size();i++){
					Permission sc = (Permission)recList.get(i);
					map.put(String.valueOf(sc.getFlowPermId()), sc);
				}
				break;
			case sentContent :	
			case dealSituation :
				url = "edoc/docstat/statContent";
				mav.addObject("statList", statList); 
				
				for(int i=0;i<statList.size();i++){
					StatContent sc = (StatContent)statList.get(i);
					map.put(sc.getStatContentId(), sc.getStatContentName());
				}
				break;
		}
		}
		
		mav.setViewName(url);
		 
		//某个类型下的一些小类型id  形式如 101-102-103
		String statContentId = request.getParameter("statContentId");
		 
		//大类型的名称
		String statContentName = typeEnum.getStatContentName();
		mav.addObject("statContentName", statContentName); 
		mav.addObject("contentTypeId", contentTypeId);
		    
		StringBuilder scIdValue = new StringBuilder("");
		//构建id_value这种形式，用于页面回显用户刚才勾选的
		if(statContentId != null && statContentId.indexOf("=")>-1){
			String[] scId = statContentId.split("=");
			if(typeEnum == StatisticsContentTypeEnum.dealSituation ||
			   typeEnum == StatisticsContentTypeEnum.sentContent){
				for(String id : scId){
					if(map.get(id)!=null){
						scIdValue.append(id).append("_").append(map.get(id)).append("=");
					}
				}
			}else if(typeEnum == StatisticsContentTypeEnum.workflowNode){
				for(String id : scId){
					if(map.get(id)!=null){
						Permission sc = (Permission)map.get(id);
						scIdValue.append(id).append("_").append(sc.getName()).append("_").append(sc.getLabel()).append("=");
					}
				}
			}
			
		}else if(statContentId != null && !"".equals(statContentId)){
			if(typeEnum == StatisticsContentTypeEnum.dealSituation){
				scIdValue.append(statContentId).append("_").append(map.get(statContentId));
			}else if(typeEnum == StatisticsContentTypeEnum.workflowNode){
				Permission sc = (Permission)map.get(statContentId);
				scIdValue.append(statContentId).append("_").append(sc.getName()).append("_").append(sc.getLabel()).append("=");
			}
		}
		
	    String newScIdValue = scIdValue.toString();
	    if(newScIdValue.endsWith("=")){
	      newScIdValue = newScIdValue.substring(0, scIdValue.length()-1);
        }
        mav.addObject("scIdValue", newScIdValue); 
		
		return mav;
	}
	
	/**
	 * 在更多页面里打开一个公文统计详情
	 */
	public ModelAndView openEdocStat(HttpServletRequest request, 
            HttpServletResponse response) throws Exception {
	    long id = Long.parseLong(request.getParameter("id"));
	    EdocStatCondition stat = edocStatManager.getEdocStatConditionById(id);
	    String url = EdocStatHelper.getEdocStatConditionUrl(stat);
	    return super.redirectModelAndView(url);
	}
	
	/**
	 * 在更多页面删除某一条记录
	 */
	
	public ModelAndView delEdocStat(HttpServletRequest request, 
            HttpServletResponse response) throws Exception {
	    long id = Long.parseLong(request.getParameter("id"));
	    String columnsName = request.getParameter("columnsName");
	    edocStatManager.delEdocStatCondition(id);
	    String s = URLEncoder.encode(columnsName,"UTF-8");
	    return super.redirectModelAndView("edocStat.do?method=getEdocStatConditions&columnsName="+s);
	}
	
	/**
	 * 首页公文统计栏目下点 更多
	 */
	
	public ModelAndView getEdocStatConditions(HttpServletRequest request, 
            HttpServletResponse response) throws Exception {
	    String subject = request.getParameter("subject");
	    String columnsName = ResourceUtil.getString("edoc.stat.label");
	    Map<String,Object> paramMap = new HashMap<String,Object>();
        paramMap.put("subject", subject);
	    ModelAndView mav = new ModelAndView("edoc/docstat/edocStatMoreConditions");
	    User user = AppContext.getCurrentUser();
	    paramMap.put("userId", user.getId());
        List<EdocStatCondition> list = edocStatManager.getEdocStatCondition(user.getLoginAccount(),paramMap);
        List<EdocStatConditionModel> allList = new ArrayList<EdocStatConditionModel>();
        int count=-1;
        for(int i=0;i<list.size();i++){
        	EdocStatConditionModel model = new EdocStatConditionModel();
            model.setStat(list.get(i));
            allList.add(model);
            count++;
        }
	    mav.addObject("list", allList);
	    
	    count = edocStatManager.getEdocStatConditionTotal(user.getLoginAccount(),subject);
	    mav.addObject("count", count);
	    mav.addObject("columnsName", columnsName);
	    return mav;
	}
	
	/**
	 * 插入推送到首页的 公文统计的查询条件
	 */
	public ModelAndView addStatCondition(HttpServletRequest request, 
            HttpServletResponse response) throws Exception {
	    User user = AppContext.getCurrentUser();
	    //维度类型
        String statisticsDimension = request.getParameter("statisticsDimension");
        int dimension = Integer.parseInt(statisticsDimension);
        //组织的编号
        String  organizationIds = request.getParameter("organizationId");
        //时间类型
        int timeType = Integer.parseInt(request.getParameter("timeType"));   
        String sendContentId = request.getParameter("sendContentId");
        String workflowNodeId = request.getParameter("workflowNodeId");
        String processSituationId = request.getParameter("processSituationId");
        String sendNodeCode = request.getParameter("sendNodeCode");
        String recNodeCode = request.getParameter("recNodeCode");
        String organizationName = request.getParameter("organizationName");
        
        String yeartype_startyear = request.getParameter("yeartype-startyear");
        String yeartype_endyear = request.getParameter("yeartype-endyear");
        
        String seasontype_startyear = request.getParameter("seasontype-startyear");
        String seasontype_endyear = request.getParameter("seasontype-endyear");
        String seasontype_startseason = request.getParameter("seasontype-startseason");
        String seasontype_endseason = request.getParameter("seasontype-endseason");
        
        String monthtype_startyear = request.getParameter("monthtype-startyear");
        String monthtype_endyear = request.getParameter("monthtype-endyear");
        String monthtype_startmonth = request.getParameter("monthtype-startmonth");
        String monthtype_endmonth = request.getParameter("monthtype-endmonth");
        
        String daytype_startdate = request.getParameter("daytype-startday");
        String daytype_enddate = request.getParameter("daytype-endday");
        
        EdocStatCondition stat = new EdocStatCondition();
        
        String mainResource = "com.seeyon.v3x.main.resources.i18n.MainResources";
        String commonResource = "com.seeyon.v3x.common.resources.i18n.SeeyonCommonResources";
        
        Locale local = LocaleContext.getLocale(request);
        String year = ResourceBundleUtil.getString(mainResource, local, "menu.tools.calendar.nian");
        String season = ResourceBundleUtil.getString(commonResource, local, "common.quarter.label");
        String month = ResourceBundleUtil.getString(mainResource, local, "menu.tools.calendar.yue");
        String day = ResourceBundleUtil.getString(mainResource, local, "menu.tools.calendar.ri");

        String stat_title = "";
        switch(timeType){
            case 1 :
                if(yeartype_startyear.equals(yeartype_endyear)){
                    stat_title = yeartype_startyear+year;
                }else{
                    stat_title = yeartype_startyear+year+" -- "+yeartype_endyear+year;
                }
                stat.setStarttime(yeartype_startyear);
                stat.setEndtime(yeartype_endyear);
                break;
            case 2 :
                if(seasontype_startyear.equals(seasontype_endyear) && seasontype_startseason.equals(seasontype_endseason)){
                    stat_title = seasontype_startyear+year+seasontype_startseason+season;
                }else{
                    stat_title = seasontype_startyear+year+seasontype_startseason+season+" -- "+
                                    seasontype_endyear+year+seasontype_endseason+season;
                }
                stat.setStarttime(seasontype_startyear+" "+seasontype_startseason);
                stat.setEndtime(seasontype_endyear+" "+seasontype_endseason);
                break;
            case 3 :
                if(monthtype_startyear.equals(monthtype_endyear) && monthtype_startmonth.equals(monthtype_endmonth)){
                    stat_title = monthtype_startyear+year+monthtype_startmonth+month;
                }else{
                    stat_title = monthtype_startyear+year+monthtype_startmonth+month+" -- "+
                                    monthtype_endyear+year+monthtype_endmonth+month;
                }
                stat.setStarttime(monthtype_startyear+" "+monthtype_startmonth);
                stat.setEndtime(monthtype_endyear+" "+monthtype_endmonth);
                break;
            case 4 :
                String[] date = daytype_startdate.split("-");
                String daytype_startyear = date[0];
                String daytype_startmonth = date[1];
                String daytype_startday = date[2]; 

                String[] date2 = daytype_enddate.split("-");
                String daytype_endyear = date2[0];
                String daytype_endmonth = date2[1];
                String daytype_endday = date2[2]; 
                
                if(daytype_startdate.equals(daytype_enddate)){
                    stat_title = daytype_startyear+year+daytype_startmonth+month+daytype_startday+day;
                }else{
                    stat_title = daytype_startyear+year+daytype_startmonth+month+daytype_startday+day + " -- "+
                                    daytype_endyear+year+daytype_endmonth+month+daytype_endday+day;
                }
                stat.setStarttime(daytype_startdate);
                stat.setEndtime(daytype_enddate);
                break;   
        }
        
        
        
        String titleInfo = " " + ResourceUtil.getString("edoc.stat.tables.label");//公文统计表
        if(dimension == 1){
            stat.setTitle(organizationName+titleInfo);
        }else{
            stat.setTitle(stat_title+titleInfo);
        }
        
        
        stat.setIdIfNew();
        stat.setUserId(user.getId());
        stat.setAccountId(user.getLoginAccount());
        stat.setStatisticsDimension(dimension);
        stat.setOrganizationId(organizationIds);
        stat.setTimeType(timeType);
        stat.setSendContentId(sendContentId);
        stat.setWorkflowNodeId(workflowNodeId);
        stat.setProcessSituationId(processSituationId);
        
        Timestamp now = new Timestamp(System.currentTimeMillis());
        stat.setCreateTime(now);
        stat.setSendNodeCode(sendNodeCode);
        stat.setRecNodeCode(recNodeCode);
        edocStatManager.saveEdocStatCondition(stat);
	    return null;
	}
	
	/**
	 * 公文统计，返回统计结果。 
	 */
	public ModelAndView doStat(HttpServletRequest request, 
			HttpServletResponse response) throws Exception {
	    String url = "edoc/docstat/edocStatResult";
		String organizationType = request.getParameter("organizationType");
		//当从统计部门维度的结果中，点击某部门名称时，将要弹出该部门下人员的统计信息
		if("5".equals(organizationType)){
			url = "edoc/docstat/edocStatResult-person";
		}
		ModelAndView mav = new ModelAndView(url);
		Map<Object, List<Object>> map = getStatMap(request,mav);
		if(map == null){
			String jsStr = "alert('"+ ResourceUtil.getString("edoc.error.no.member") +"');window.close();";//该范围下没有人员!
			rendJavaScript(response, jsStr);
			return null;
		}
		mav.addObject("results", map);
		return mav;
	}
	
	private void setOrgDimensionTitle(HttpServletRequest request){
	  //时间类型为 年
        String yeartype_startyear = request.getParameter("yeartype-startyear");
        String yeartype_endyear = request.getParameter("yeartype-endyear");
        
        //时间类型为季度
        String seasontype_startyear = request.getParameter("seasontype-startyear");
        String seasontype_endyear = request.getParameter("seasontype-endyear");
        String seasontype_startseason = request.getParameter("seasontype-startseason");
        String seasontype_endseason = request.getParameter("seasontype-endseason");
         
        //时间类型为月
        String monthtype_startyear = request.getParameter("monthtype-startyear");
        String monthtype_endyear = request.getParameter("monthtype-endyear");
        String monthtype_startmonth = request.getParameter("monthtype-startmonth");
        String monthtype_endmonth = request.getParameter("monthtype-endmonth");
        
        //时间类型为日
        String daytype_startday = request.getParameter("daytype-startday");
        String daytype_endday = request.getParameter("daytype-endday");
        
        //时间类型
        int timeType = Integer.parseInt(request.getParameter("timeType"));  
        request.setAttribute("timeType", timeType);
        request.setAttribute("yeartype_startyear", yeartype_startyear);
        request.setAttribute("yeartype_endyear", yeartype_endyear);
        request.setAttribute("seasontype_startyear", seasontype_startyear);
        request.setAttribute("seasontype_endyear", seasontype_endyear);
        request.setAttribute("seasontype_startseason", seasontype_startseason);
        request.setAttribute("seasontype_endseason", seasontype_endseason);
        request.setAttribute("monthtype_startyear", monthtype_startyear);
        request.setAttribute("monthtype_endyear", monthtype_endyear);
        request.setAttribute("monthtype_startmonth", monthtype_startmonth);
        request.setAttribute("monthtype_endmonth", monthtype_endmonth);
        request.setAttribute("daytype_startdate", daytype_startday);
        request.setAttribute("daytype_enddate", daytype_endday);
	}
	
	private Map<Object, List<Object>> getStatMap(HttpServletRequest request,ModelAndView mav) throws Exception{
	    //维度类型
		String statisticsDimension = request.getParameter("statisticsDimension");
		int dimension = Integer.parseInt(statisticsDimension);
		if(dimension == 2){
		    //当是组织维度时，设置标题
		    setOrgDimensionTitle(request);
		}
		
		
		//组织类型,以后等调用组织页面时再 写
		String organizationType = request.getParameter("organizationType");
		//组织的编号
		String  organizationIds = request.getParameter("organizationId");
		if(organizationIds == null ||"".equals(organizationIds)){
			return null;
		}
		String[] orgIdArr = organizationIds.split(",");
		//组织的名称
		String[] organizations = new String[orgIdArr.length];
		StringBuilder  organizationName = new StringBuilder();
		for(int i=0;i<orgIdArr.length;i++){
			boolean flag = true;
			String orgStr = orgIdArr[i];
			//有可能存在部门岗位
			if(orgStr.startsWith("Department_Post")){
				String[] orgStrArr = orgStr.split("[|]");
				//表示是部门岗位  Department_Post|-7116515853824014787_-3819021029451742224
				if(orgStrArr[1].indexOf("_")>-1){
					flag = false;
					String[] arr = orgStrArr[1].split("[_]");
					V3xOrgPost post = orgManager.getEntityById(V3xOrgPost.class, Long.parseLong(arr[1]));
					if(post != null){
						organizations[i] = post.getName();
					}
				}
			}
			if(flag){
				organizations[i] = orgManager.getEntity(orgStr).getName();
			}
		    
			organizationName.append(organizations[i]);
		    if(i != orgIdArr.length-1){
		    	organizationName.append(StatConstants.LABEL_SIGN);
		    }
		}
		
		//时间类型
		int timeType = Integer.parseInt(request.getParameter("timeType"));   
		
		StatParamVO statParam = new StatParamVO();
		statParam.setUserId(AppContext.getCurrentUser().getId());
		statParam.setAccountId(AppContext.getCurrentUser().getLoginAccount());
		statParam.setDimensionType(dimension);
		statParam.setTimeType(timeType);
		
		//统计内容
		
		String sendContentCode = request.getParameter("sendContentId");
		String[] scArray = sendContentCode.split(StatConstants.SIGN);
		//获得发文类型的名称
		String sendContent = sendContentImpl.getSendContentName(scArray);
		
		//获得流程节点的名称
		String sendNodeCode = request.getParameter("sendNodeCode");
		String recNodeCode = request.getParameter("recNodeCode");

		String[] sendCodes = null;
		String[] recCodes = null;
		if(Strings.isNotBlank(sendNodeCode) && !"null".equals(sendNodeCode)){
			sendCodes = sendNodeCode.split("[=]");
		}
		if(Strings.isNotBlank(recNodeCode) && !"null".equals(recNodeCode)){
			recCodes = recNodeCode.split("[=]");
		}
		
		long accountId = AppContext.getCurrentUser().getLoginAccount();
		List workflowList = workflowNodeImpl.contentDisplay(accountId);
		
		String resource = "com.seeyon.v3x.edoc.resources.i18n.EdocResource"; 
        Locale local = LocaleContext.getLocale(request);
        String workflowNode = "";
        List<Permission> sendList = null;
        List<Permission> recList = null;
		if(workflowList.size()>0){
			sendList = (List<Permission>)workflowList.get(0);
			recList = (List<Permission>)workflowList.get(1);
		}
		//GOV-4999 公文统计-收发查询，统计维度选择组织，统计列表中点击组织名称报红三角，详见附件！
		String sendNodeCodeId = "";
		String recNodeCodeId = "";
		/***代码重构，新建了WorkflowNodeBean类专门来收集统计流程节点的数据信息，较少重复代码***/
		if(Strings.isNotBlank(sendNodeCode)&& !"null".equals(sendNodeCode)){
		    WorkflowNodeBean sendBean = new WorkflowNodeBean();
		    sendBean.execute(sendList, sendCodes, resource, local);
	        sendNodeCodeId = sendBean.getNodeCodeId();
	        sendNodeCode = sendBean.getNodeCode();
	        workflowNode += sendBean.getWorkflowNode();
		}
		
		if(Strings.isNotBlank(recNodeCode)&& !"null".equals(recNodeCode)){
		    WorkflowNodeBean recBean = new WorkflowNodeBean();
		    recBean.execute(recList, recCodes, resource, local);
	        recNodeCodeId = recBean.getNodeCodeId();
	        recNodeCode = recBean.getNodeCode();
	        //OA-17185 统计全部流程节点时候，漏掉了前面发文节点统计名称
	        if(Strings.isNotBlank(workflowNode))workflowNode += StatConstants.LABEL_SIGN;//当需要统计发文节点时，这里需要加上分隔符
	        workflowNode += recBean.getWorkflowNode(); //这里是叠加
		}
		/***代码重构，新建了WorkflowNodeBean类专门来收集统计流程节点的数据信息，较少重复代码***/
		
		String processSituationCode = request.getParameter("processSituationId");
		StringBuilder processSituation = new StringBuilder();
        if(Strings.isNotBlank(processSituationCode)&& !"null".equals(processSituationCode)){
		    String[] processArr = processSituationCode.split("[=]");
	        for(int i=0;i<processArr.length;i++){
	            if(i != processArr.length-1){
	                processSituation.append(StatConstants.DealMap.get(processArr[i])).append(StatConstants.LABEL_SIGN);
	            }else{
	                processSituation.append(StatConstants.DealMap.get(processArr[i]));
	            }
	        }
		}
		
        
		StrategyInter strategy = null;
		
		//Department_Post|-2676763115351880013_5235530654617519331,Department_Post|-2676763115351880013_527668748992990125,Department|-2676763115351880013
		//将上面的组织保存进orgMap中
		Map<Integer,List<Long>> orgMap = StatisticsUtils.getOrgMapByOrgId(organizationIds);
		statParam.setOrgs(orgMap);
		
		Map hasChildrenOrgMap = StatisticsUtils.getHasChirdOrgMapByOrgId(organizationIds);
		statParam.setHasChirdOrgs(hasChildrenOrgMap);
		
		
		request.setAttribute("organizationName", organizationName.toString());
		List<V3xOrgMember> members = new ArrayList<V3xOrgMember>();
		if(dimension == 1){
			strategy = new TimeDimension();
		}else if(dimension == 2){   
			//当组织维度选人时，则不能再往下钻了，故保存组织类型
			Iterator it = orgMap.keySet().iterator();
			int[] orgArr = new int[organizations.length];
			int kk = 0;
			while(it.hasNext()){
				int t = Integer.parseInt(String.valueOf(it.next()));
				
				//选择的组织为部门类型时，部门下面，可能会有子部门，那么这时ll就不只选择的部门个数了，还多了子部门
				List<Long> ll = (List<Long>)orgMap.get(t);
				for(Long l : ll){
					if(kk < organizations.length)
					orgArr[kk++] = t;
				}
			}
			request.setAttribute("orgArr", orgArr);
			
			strategy = new OrganizationDimension();
			//orgMap中 存入人员ID编号
			if("5".equals(organizationType)){
				//只会有一个部门
				List<Long> memberIds = new ArrayList<Long>();
				orgMap.clear();  
				String[] arr = organizationIds.split("[|]");
				//某个单位或部门或职务级别 下取所有人员(包括兼职人员)
				if("Department_Post".equals(arr[0])){
					//表示是部门岗位  Department_Post|-7116515853824014787_-3819021029451742224
					if(arr[1].indexOf("_")>-1){
						String[] idArr = arr[1].split("[_]");
						if(idArr.length == 2){
							members = orgManager.getMembersByDepartmentPost(Long.parseLong(idArr[0]), Long.parseLong(idArr[1]));
						}
					}
				}else{
					members = orgManager.getMembersByType(arr[0], Long.parseLong(arr[1]));
				}
				if(Strings.isEmpty(members)){
					return null;
				}
				
				for(V3xOrgMember m : members){
				    memberIds.add(m.getId());
				}
				orgMap.put(StatConstants.PERSON, memberIds); 
			}
		}
		 
		//构建统计内容
		List<ContentData> statisticsContentList = new ArrayList<ContentData>();  
		
		List statisticsContent = new ArrayList();
		//页面显示统计内容的顺序    
		int order = 1;
		
		//发文情况内容
		if(Strings.isNotBlank(sendContentCode)&& !"null".equals(sendContentCode)){
			ContentData content1 = new ContentData();
			content1.setContentType(StatisticsContentTypeEnum.sentContent.getKey());
			String[] nodes = sendContentCode.split(StatConstants.SIGN);
			String[] sendContentCodeNames = sendContent.split(StatConstants.LABEL_SIGN);
			List<OrderContent> data1 = new ArrayList<OrderContent>();
			for(int i=0;i<nodes.length;i++){
				OrderContent oc = new OrderContent();
				oc.setOrder(order);
				oc.setContentName(nodes[i]);
				data1.add(oc);
				
				OrderContent oc2 = new OrderContent();
				oc2.setOrder(order++);
				oc2.setContentName(sendContentCodeNames[i]);
				statisticsContent.add(oc2);
			}
			content1.setContents(data1);
			statisticsContentList.add(content1);
		}
		
		//收文情况内容
		if(Strings.isNotBlank(processSituationCode)&& !"null".equals(processSituationCode)){
		    
			ContentData content1 = new ContentData();
			content1.setContentType(StatisticsContentTypeEnum.dealSituation.getKey());
			String[] nodes = processSituationCode.split(StatConstants.SIGN);
			String[] processSituationNames = processSituation.toString().split(StatConstants.LABEL_SIGN);
			List<OrderContent> data1 = new ArrayList<OrderContent>();
			for(int i=0;i<nodes.length;i++){
				OrderContent oc = new OrderContent();
				oc.setOrder(order);
				oc.setContentName(nodes[i]);
				data1.add(oc);
				
				OrderContent oc2 = new OrderContent();
				oc2.setOrder(order++);
				oc2.setContentName(processSituationNames[i]);
				statisticsContent.add(oc2);
			}
			content1.setContents(data1);
			statisticsContentList.add(content1);
		}
		//发文流程节点 内容
		if((Strings.isNotBlank(sendNodeCode)&& !"null".equals(sendNodeCode)) || (Strings.isNotBlank(recNodeCode)&& !"null".equals(recNodeCode)) ){
			ContentData content = new ContentData();
			content.setContentType(StatisticsContentTypeEnum.workflowNode.getKey()); 
			
			List<OrderContent> data = new ArrayList<OrderContent>();
			if(Strings.isNotBlank(sendNodeCode)&& !"null".equals(sendNodeCode)){
				String[] sendnodes = sendNodeCode.split(StatConstants.SIGN);
				for(int i=0;i<sendnodes.length;i++){
					OrderContent oc = new OrderContent();
					oc.setOrder(order++);
					oc.setContentName(sendnodes[i]+StatConstants.SEND_NODE_SUFFIX);
					data.add(oc);
				}
			}
			if(Strings.isNotBlank(recNodeCode)&& !"null".equals(recNodeCode)){
				String[] recnodes = recNodeCode.split(StatConstants.SIGN);
				for(int i=0;i<recnodes.length;i++){
					OrderContent oc = new OrderContent();
					oc.setOrder(order++);
					oc.setContentName(recnodes[i]+StatConstants.REC_NODE_SUFFIX);
					data.add(oc);
				}
			}
			String[] workflowNodeNames = workflowNode.split(StatConstants.LABEL_SIGN);
			if(workflowNodeNames!= null && workflowNodeNames.length > 0){
				
				for(int i=0;i<workflowNodeNames.length;i++){
					OrderContent oc2 = new OrderContent(); 
					oc2.setContentName(workflowNodeNames[i]);
					statisticsContent.add(oc2);
				}
			}
			
			content.setContents(data);
			statisticsContentList.add(content);
		}
		
		
		
		Map<Object, List<Object>> map = new LinkedHashMap<Object, List<Object>>();
		if(statParam.getOrgs() != null){   
			
			boolean flag = true;
			Map om = statParam.getOrgs();
			//这里是当统计部门后，点击某部门弹出部门下人员的统计信息
			//当该部门下没有人员时，就不用统计了
			if("5".equals(organizationType)){
				List mlist = (List)om.get(StatConstants.PERSON);
				if(mlist!=null && mlist.size() == 1 && (Long)mlist.get(0) == -1L){
					flag = false;
				}
			}
			if(flag){
				map = strategy.statistics(request, statParam, statisticsContentList);
				List<Long> orderList = StatisticsUtils.getOrderOrgId(organizationIds);
				//当为组织维度时，调整Map中统计顺序为 开始选择的顺序
				if(dimension == 2 && map.size() >= 2 && orderList.size() == map.size()){ 
//				        !"5".equals(organizationType)){
					Map<Object, List<Object>> orderMap = new LinkedHashMap<Object, List<Object>>();
					
					for(Long oid : orderList){
						orderMap.put(oid, map.get(oid));
					}
					map.clear();
					map = orderMap;
				}
			}
		}
		  
		request.setAttribute("statisticsContent", statisticsContent);
		request.setAttribute("organizations", organizations);
		//当在统计结果中，选择部门维度中的某部门时，弹出的页面显示该部门下的人员统计信息
		//设置人员显示列表 为维度
		if("5".equals(organizationType)){
			String[] personNames = new String[members.size()];
			for(int i=0;i<members.size();i++){
				V3xOrgMember member = members.get(i);
				personNames[i] = member.getName();
			}
			request.setAttribute("organizations", personNames);
		}
		
		request.setAttribute("statisticsDimension", statisticsDimension);
		request.setAttribute("timeType", timeType);
		request.setAttribute("organizationType", organizationType);
		if(mav!=null){
				String[] orgIds = organizationIds.split(",");
				mav.addObject("orgIds", orgIds);
			 
			//设置页面传来参数到request作用域中，导出excel功能要用到，因为还要执行一次本统计查询方法
			mav.addObject("statisticsDimension", statisticsDimension);
			mav.addObject("organizationId", organizationIds);
			mav.addObject("organizationName", organizationName);
			mav.addObject("sendContentId", sendContentCode);
			mav.addObject("sendNodeCode", sendNodeCodeId);      
			mav.addObject("recNodeCode", recNodeCodeId);
			mav.addObject("processSituationId", processSituationCode);
			mav.addObject("sendContent", sendContent);
			mav.addObject("workflowNode", workflowNode);
			mav.addObject("processSituation", processSituation); 
		}
		statSum(map);
		
		return map;
	}
	
	private void statSum(Map<Object, List<Object>> map){
		//纵向合计
		int length = 0;
		if(map.size()>0){
			Iterator<Object> it2 = map.keySet().iterator();
			length = map.get(it2.next()).size();
		}
		if(length > 0){
			//GOV-4803 公文统计-收发统计，办结率合计显示错误，详见附件！
			List<Object> sumList = new ArrayList<Object>();
			Object[] sumArr = new Object[length];
			Iterator<Object> it = map.keySet().iterator();
			while(it.hasNext()){ 
				Object key = it.next();
				List<Object> list = map.get(key);
				for(int i=0;i<length;i++){
					Object obj = list.get(i);
					if(obj instanceof Integer){
						int k = (Integer)obj;
						if(sumArr[i] == null){
							sumArr[i] = k;
						}else{
							sumArr[i] = (Integer)sumArr[i] + k;
						}
					}else{
						String str = String.valueOf(obj);
						String[] strArr = str.split("[_]");
						float a = Float.parseFloat(strArr[1]);
						float total = Float.parseFloat(strArr[2]);
						
						if(sumArr[i] == null){
							sumArr[i] = a+"_"+total;
						}else{
							String old = String.valueOf(sumArr[i]);
							String[] oldArr = old.split("[_]");
							float aOld = Float.parseFloat(oldArr[0]);
							float totalOld = Float.parseFloat(oldArr[1]);
							
							float aNew = aOld + a;
							float totalNew = totalOld + total;
							sumArr[i] = aNew+"_"+totalNew;
						}
						//还原办结率或阅读率
						list.set(i, strArr[0]);
					} 
				}
			}
			
			DecimalFormat decimalFormat = new DecimalFormat("######.00");
			for(int i=0;i<sumArr.length;i++){
				Object obj = sumArr[i];
				if(obj instanceof Integer){
					sumList.add(sumArr[i]);
				}
				else{
					String str = String.valueOf(sumArr[i]);
					String[] strArr = str.split("[_]");
					float a = Float.parseFloat(strArr[0]);
					float total = Float.parseFloat(strArr[1]);
					String percent = "";
					if(Float.floatToRawIntBits(a) == 0 || Float.floatToRawIntBits(total) == 0){
						percent = "0%";
					}else{
						percent = decimalFormat.format(a/total*100)+"%"; 
					}
					sumList.add(percent);
				}
			}
			
			map.put("sum",sumList);
		}
	}
	
	
    /**
     * 查询统计记录详细信息。
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView edocDetail(HttpServletRequest request,
                             HttpServletResponse response) throws Exception {
        Long id = Long.valueOf(request.getParameter("id"));
        EdocStat edocStat = edocStatManager.getEdocStat(id);
        Long edocId = edocStat.getEdocId();
        EdocSummary edocSummary = edocManager.getEdocSummaryById(edocId, false);
        User user=AppContext.getCurrentUser();
        

        //changyi moveTo5.0 zhushi
    	//SECURITY 访问安全控制
//        if(!SecurityCheck.isHasAuthorityToStatDetail(request, response, edocSummary, user)){
//        	return null;
//        }
        ModelAndView mav = new ModelAndView("edoc/docstat/edocDetail");
        try{
        mav.addObject("edocStatObj", edocStat);
        mav.addObject("edocSummary", edocSummary);
        String docType = "";//公文种类
        String sendType = "";//行文类型
        String secretLevel = "";//文件密级
        String urgentLevel = "";//紧急程度
        String keepPeriod = "";//保密期限
        if(edocSummary!=null){
	        if (edocSummary.getDocType() != null && !"".equals(edocSummary.getDocType())) {
	        	docType = enumManagerNew.getEnumItemLabel(EnumNameEnum.edoc_doc_type, edocSummary.getDocType());
	        	mav.addObject("docType", docType);
	        }   
	        if (edocSummary.getSendType() != null && !"".equals(edocSummary.getSendType())) {
	        	sendType = enumManagerNew.getEnumItemLabel(EnumNameEnum.edoc_send_type, edocSummary.getSendType());
	        	mav.addObject("sendType", sendType);
	        }
	        if (edocSummary.getSecretLevel() != null && !"".equals(edocSummary.getSecretLevel())) {
	        	secretLevel = enumManagerNew.getEnumItemLabel(EnumNameEnum.edoc_secret_level, edocSummary.getSecretLevel());
	        	mav.addObject("secretLevel", secretLevel);
	        }
	        if (edocSummary.getUrgentLevel() != null && !"".equals(edocSummary.getUrgentLevel())) {
	        	urgentLevel = enumManagerNew.getEnumItemLabel(EnumNameEnum.edoc_urgent_level, edocSummary.getUrgentLevel());
	        	mav.addObject("urgentLevel", urgentLevel);
	        }
	        if (edocSummary.getKeepPeriod() != null) {
	        	keepPeriod = enumManagerNew.getEnumItemLabel(EnumNameEnum.edoc_keep_period, String.valueOf(edocSummary.getKeepPeriod()));
	        	mav.addObject("keepPeriod", keepPeriod);
	        }
        }
	}catch(Exception e){
		    response.setContentType("text/html;charset=UTF-8");
			PrintWriter out = response.getWriter();        	
        	//super.printV3XJS(out);        	
        	out.println("<script>");
        	out.println("alert(\"" + StringEscapeUtils.escapeJavaScript(e.getMessage()) + "\")");
        	out.println("if(window.dialogArguments){"); //弹出
        	out.println("  window.returnValue = \"true\";");
        	out.println("  window.close();");
        	out.println("}else{");
        	out.println("  parent.getA8Top().reFlesh();");
        	out.println("}");
        	out.println("");
        	out.println("</script>");
        	
        	return null;
		}
        return mav;
    }
    
    // 保存公文备考信息
    public ModelAndView saveEdocRemark(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Long id = Long.valueOf(request.getParameter("id"));
    	String remark = request.getParameter("remark");
    	//PrintWriter out = response.getWriter();
    	edocStatManager.saveEdocRemark(id, remark);
    	
    	//GOV-4990 公文统计-收发查询，查询列表中点击某数据，显示的单据下有"提交"按钮，点击无反映,应该有"提交成功"或者"设置备考成功"等提示。
    	response.setContentType("text/html;charset=UTF-8");
    	PrintWriter out = response.getWriter();        	    	
    	out.println("<script>");
    	out.println("alert(parent.edocLang.operateOk)");
    	out.println("if(parent!=null && parent.parent!=null && typeof(parent.parent.searchByself)=='function') {");
    	out.println("	parent.parent.searchByself();");
    	out.println("}");
    	out.println("</script>");
    	return null;
    	
//    	return super.refreshWindow("parent");
    	//return super.refreshWorkspace();
    }
    
    public ModelAndView exportToExcel(HttpServletRequest request,HttpServletResponse response)throws Exception{
    	Map<Object, List<Object>> results = getStatMap(request,null);

    	String organizationType = (String)request.getAttribute("organizationType");
    	String organizationName = (String)request.getAttribute("organizationName");
    	String statisticsDimension = (String)request.getAttribute("statisticsDimension");
    	List<OrderContent> columns = (List<OrderContent>)request.getAttribute("statisticsContent");
    	String[] organizations = (String[])request.getAttribute("organizations");
    	int timeType = (Integer)request.getAttribute("timeType");
    	
    	String[] yearArr = (String[])request.getAttribute("yearArr");
    	String[] monthArr = (String[])request.getAttribute("monthArr");
    	String[] seasonArr = (String[])request.getAttribute("seasonArr");
    	String[] dateArr = (String[])request.getAttribute("dateArr");
    	
    	String yeartype_startyear = (String)request.getAttribute("yeartype_startyear");
    	String seasontype_startyear = (String)request.getAttribute("seasontype_startyear");
    	String seasontype_startseason = (String)request.getAttribute("seasontype_startseason");
    	String monthtype_startyear = (String)request.getAttribute("monthtype_startyear");
    	String monthtype_startmonth = (String)request.getAttribute("monthtype_startmonth");
    	
    	String yeartype_endyear = (String)request.getAttribute("yeartype_endyear");
    	String seasontype_endyear = (String)request.getAttribute("seasontype_endyear");
    	String seasontype_endseason = (String)request.getAttribute("seasontype_endseason");
    	String monthtype_endyear = (String)request.getAttribute("monthtype_endyear");
    	String monthtype_endmonth = (String)request.getAttribute("monthtype_endmonth");
    	
    	String daytype_startdate = (String)request.getAttribute("daytype_startdate");
    	String daytype_enddate = (String)request.getAttribute("daytype_enddate");
    	
    	String daytype_startyear = (String)request.getAttribute("daytype_startyear");
    	String daytype_startmonth = (String)request.getAttribute("daytype_startmonth");
    	String daytype_startday = (String)request.getAttribute("daytype_startday");
    	
    	String daytype_endyear = (String)request.getAttribute("daytype_endyear");
    	String daytype_endmonth = (String)request.getAttribute("daytype_endmonth");
    	String daytype_endday = (String)request.getAttribute("daytype_endday");
    	
    	
    	String mainResource = "com.seeyon.v3x.main.resources.i18n.MainResources";
    	String commonResource = "com.seeyon.v3x.common.resources.i18n.SeeyonCommonResources";
    	String edocResource = "com.seeyon.v3x.edoc.resources.i18n.EdocResource";
    	
    	Locale local = LocaleContext.getLocale(request);
    	String year = ResourceBundleUtil.getString(mainResource, local, "menu.tools.calendar.nian");
    	String season = ResourceBundleUtil.getString(commonResource, local, "common.quarter.label");
    	String month = ResourceBundleUtil.getString(mainResource, local, "menu.tools.calendar.yue");
    	String day = ResourceBundleUtil.getString(mainResource, local, "menu.tools.calendar.ri");
    	String statSum = ResourceBundleUtil.getString(edocResource, local, "edoc.stat.sum.label");
    	String statTable = ResourceBundleUtil.getString(edocResource, local, "edoc.stat.tables.label");
    	 
    	DataRecord dataRecord = new DataRecord();
    	Pagination.setNeedCount(false);
    	if(null != results && results.size() > 0){
    		DataRow[] datarow = new DataRow[results.size()];
    		
    		Iterator it = results.keySet().iterator();
    		int i=0;
    		while(it.hasNext()){
    			Object key = it.next();
    			List<Object> list = results.get(key);
    			DataRow row = new DataRow();
    			//组织类型
    	    	if("2".equals(statisticsDimension)){
    	    		if(i < results.size()-1)
    	    			row.addDataCell(organizations[i],1);
    	    		else
    	    			row.addDataCell(statSum,1);
    	    	}else{
    	    		if(i < results.size()-1){
	    	    		switch(timeType){
	    	    			case 1 :
	    	    				row.addDataCell(yearArr[i]+year,1);
	    	    				break;
	    	    			case 2 :
	    	    				row.addDataCell(yearArr[i]+year+seasonArr[i]+season,1);
	    	    				break;
	    	    			case 3 :
	    	    				row.addDataCell(yearArr[i]+year+monthArr[i]+month,1);
	    	    				break;
	    	    			case 4 :
	    	    				row.addDataCell(dateArr[i],1);
	    	    				break;	
	    	    		}
    	    		}else{
    	    			row.addDataCell(statSum,1);
    	    		}
    	    		
    	    	}
    			
    			for(int j=0;j<list.size();j++){
    				row.addDataCell(String.valueOf(list.get(j)),1);
    			}
    			datarow[i++] = row;
    		}
    		dataRecord.addDataRow(datarow);
    	}
    	
    	String[] columnName = new String[columns.size()+1];
    	if("1".equals(statisticsDimension)){
    		columnName[0] = "  "+ResourceUtil.getString("edoc.stat.time")+" / " + ResourceUtil.getString("edoc.stat.content")+"  ";//  统计时间  / 统计内容  ;
    	}else{
    		columnName[0] = "  "+ResourceUtil.getString("edoc.stat.label.statUnit")+" / " + ResourceUtil.getString("edoc.stat.content")+"  ";//  统计组织 / 统计内容  ;
    	}
    	
    	
    	for(int i=0;i<columns.size();i++){
    		OrderContent oc = columns.get(i);
    		columnName[i+1] = oc.getContentName();
    	}
    	dataRecord.setColumnName(columnName);    
    	
    	short[] columnWidth = new short[columns.size()+1];
    	for(int i=0;i<columnWidth.length;i++){
    		if(i == 0){
    			columnWidth[0] = 30;
    		}else{
    			columnWidth[i] = 20;
    		}
    	}
    	dataRecord.setColumnWith(columnWidth);
    	
		String stat_title = "";
		if("1".equals(statisticsDimension)){
			stat_title = organizationName;
		}else{
			switch(timeType){
			case 1 :
				if(yeartype_startyear.equals(yeartype_endyear)){
					stat_title = yeartype_startyear+year;
				}else{
					stat_title = yeartype_startyear+year+" -- "+yeartype_endyear+year;
				}
				break;
			case 2 :
				if(seasontype_startyear.equals(seasontype_endyear) && seasontype_startseason.equals(seasontype_endseason)){
					stat_title = seasontype_startyear+year+seasontype_startseason+season;
				}else{
					stat_title = seasontype_startyear+year+seasontype_startseason+season+" -- "+
									seasontype_endyear+year+seasontype_endseason+season;
				}
				
				break;
			case 3 :
				if(monthtype_startyear.equals(monthtype_endyear) && monthtype_startmonth.equals(monthtype_endmonth)){
					stat_title = monthtype_startyear+year+monthtype_startmonth+month;
				}else{
					stat_title = monthtype_startyear+year+monthtype_startmonth+month+" -- "+
									monthtype_endyear+year+monthtype_endmonth+month;
				}
				break;
			case 4 :
				if(daytype_startdate.equals(daytype_enddate)){
					stat_title = daytype_startyear+year+daytype_startmonth+month+daytype_startday+day;
				}else{
					stat_title = daytype_startyear+year+daytype_startmonth+month+daytype_startday+day + " -- "+
									daytype_endyear+year+daytype_endmonth+month+daytype_endday+day;
				}
				break;	
			} 
		}
		if("5".equals(organizationType)){
			stat_title += organizationName;
		}
		stat_title += statTable;
		dataRecord.setTitle(stat_title);
		dataRecord.setSheetName(stat_title);
//		OrganizationHelper.exportToExcel(request, response, fileToExcelManager, stat_title, dataRecord);
		fileToExcelManager.save(response, stat_title, dataRecord);
		
    	return null;
    }
    
    /**
     * 导出查询结果的excel表
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView exportQueryToExcel(HttpServletRequest request,HttpServletResponse response)throws Exception{
		
    	// --  用于输出excel的标题 －－
    	// --  start  --
		Locale local = LocaleContext.getLocale(request);
		String resource = "com.seeyon.v3x.edoc.resources.i18n.EdocResource";
		String stat_title = ResourceBundleUtil.getString(resource, local, "edoc.query.tables.label"); //标题
		String send_title = ResourceBundleUtil.getString(resource, local, "edoc.stat.tables.send.label"); //发文
		String sign_title = ResourceBundleUtil.getString(resource, local, "edoc.stat.tables.sign.label"); //签报
		String recieve_title = ResourceBundleUtil.getString(resource, local, "edoc.stat.tables.recieve.label"); //收文
		String archivie_title = ResourceBundleUtil.getString(resource, local, "edoc.stat.tables.archive.label"); //归档
    	// -- end --
		
		Integer flag = ServletRequestUtils.getIntParameter(request, "flag", 0);
		Integer edocType = ServletRequestUtils.getIntParameter(request, "_oldEdocType", 0);
		if (flag == 1) {					
			if (edocType == EdocEnum.edocType.sendEdoc.ordinal()) {
				stat_title = send_title; //标题为发文
			}
			else if (edocType == EdocEnum.edocType.recEdoc.ordinal() || edocType == EdocEnum.edocType.signReport.ordinal()) {
				if(edocType == EdocEnum.edocType.recEdoc.ordinal()){
					stat_title = recieve_title; //标题为收文
				}else{
					stat_title = sign_title; //标题为签报				
				}
			}
			else if (edocType == 999) { //查询归档公文
				stat_title = archivie_title; // 标题为归档
			}
		}
		List<EdocStat> results =query(request,true);
		
    	DataRecord dataRecord = EdocHelper.exportQuery(request, this.response(results),stat_title,edocType);
    	fileToExcelManager.save(response, stat_title, dataRecord);
    	return null;
    }
    
	private String getLabel(String itemValue,CtpEnumBean metadata){
	    CtpEnumItem itms = metadata.getItem(itemValue);
		
		if (itms==null) return null;
		String label = null;
		if(itemValue != null) {
			if(this.bundleAttrValue != null){ //指定语言
				label = ResourceBundleUtil.getString(bundleAttrValue, itms.getLabel());
			}/*此处可删除，因为新框架中ResourceUtil.getString()可以根据key获取到任何地方的国际化转换，屏蔽这一段会引起不在指定目录下的国际化获取不到
			   ，所以列表中有某些国际化是英文标识
			else if(Strings.isNotBlank(metadata.getResourceBundle())){ //在原数据中定义了resourceBundle
				label =ResourceUtil.getString(itms.getLabel());// ResourceBundleUtil.getString(metadata.getResourceBundle(), itms.getLabel());
			}*/else{
				label = ResourceUtil.getString(itms.getLabel());
			}
			
			if(label == null){
				return itms.getLabel();
			}			
		}

		
		return label;
	}
    
    /**
    * 进行封装的方法，封装成发送到页面的数据
    * @param list
    * @return
    */
    private List<WebEdocStat> response(List<EdocStat> list) throws Exception{
    	if(list == null) {
    		return null ;
    	}
    	List<WebEdocStat> webEdocStatList = new ArrayList<WebEdocStat>() ;
    	CtpEnumBean docTypeMetadata = enumManagerNew.getEnum(MetadataNameEnum.edoc_doc_type.name());//得到公文种类的枚举
    	CtpEnumBean  secretLeveleMetadata = enumManagerNew.getEnum(MetadataNameEnum.edoc_secret_level.name());//得到公文密级的枚举
		
		
		List<Long> ids=new ArrayList<Long>();
		for(EdocStat edocStat: list) {
			ids.add(edocStat.getEdocId());
		}		
		Hashtable<Long,EdocSummary> hs=new Hashtable<Long,EdocSummary>();
		if(list.size()>0)
		{
			hs=edocManager.queryBySummaryIds(ids);
		}
		List<Long> archiveIds = new ArrayList<Long>();
		EdocSummary summary= new EdocSummary();
		for(EdocStat edocStat: list) {
			summary = hs.get(edocStat.getEdocId());
		 
    		String createUser =  "" ;
    		if(edocStat.getCreateUserid() != null){ 
    			createUser = orgManager.getMemberById(edocStat.getCreateUserid()).getName() ;
    		}    
    		String accountName = "" ;
	   		 if(Strings.isNotBlank(summary.getSendUnit())){
	   			 accountName = summary.getSendUnit();
	   		 }
    		String docType = this.getLabel(edocStat.getDocType(), docTypeMetadata) ;//公文的种类
    		//EdocSummary edocSummary = this.edocManager.getEdocSummaryById(edocStat.getEdocId(), false) ;
    		String secretLevel="";
    		if(hs.get(edocStat.getEdocId())!=null){
    			secretLevel = this.getLabel(summary.getSecretLevel(), secretLeveleMetadata) ;
    		}else{
    			secretLevel = this.getLabel(null, secretLeveleMetadata) ;
    		}
    		WebEdocStat webEdocStat  = new WebEdocStat() ;
    		if(summary != null){
	    		Long archiveId = summary.getArchiveId() ;
                if (archiveId != null) {
                    archiveIds.add(archiveId);
                    webEdocStat.setArchiveId(archiveId);
                }
    		}
    		webEdocStat.setId(edocStat.getId()) ;    		
    		webEdocStat.setDocType(docType) ;  		
    		webEdocStat.setAccount(accountName) ;
    		webEdocStat.setArchivedTime(edocStat.getArchivedTime()) ;
    		webEdocStat.setCreateDate(edocStat.getCreateDate()) ;
    		webEdocStat.setDocMark(edocStat.getDocMark()) ; 		
    		webEdocStat.setCreateUser(createUser) ;
    		webEdocStat.setIssUser(edocStat.getIssuer()) ;
    		webEdocStat.setSubject(edocStat.getSubject()) ;
    		webEdocStat.setRemark(edocStat.getRemark()) ;
    		webEdocStat.setSendTo(edocStat.getSendTo()) ;
    		webEdocStat.setSerialNo(edocStat.getSerialNo()) ;
    		webEdocStat.setSecretLevel(secretLevel) ;
    		webEdocStat.setRecviverDate(edocStat.getCreateDate()) ;//登记日期
    		String edocType = "" ; //公文的类型
    		if(edocStat.getEdocType() == EdocEnum.edocType.sendEdoc.ordinal()) {
    			edocType = ResourceBundleUtil.getString(resource_common_baseName, "edoc.docmark.inner.send") ;
    		}else if(edocStat.getEdocType() == EdocEnum.edocType.recEdoc.ordinal()) {
    			edocType = ResourceBundleUtil.getString(resource_common_baseName, "edoc.docmark.inner.receive") ;
    		}else if(edocStat.getEdocType() == EdocEnum.edocType.signReport.ordinal()){
    			edocType = ResourceBundleUtil.getString(resource_common_baseName, "edoc.docmark.inner.signandreport") ;
    		}
    		webEdocStat.setEdocType(edocType) ;
    		webEdocStatList.add(webEdocStat) ;
    	}   
        
        //查询DocResouce,获取归档路径。
		//TODO
		
		if (AppContext.hasPlugin("doc")) {
			List<DocResourceBO> docs = docApi.findDocResources(archiveIds);
			for (WebEdocStat webEdocStat : webEdocStatList) {
				for (DocResourceBO doc : docs) {
					if (doc.getId().equals(webEdocStat.getArchiveId())) {

						String frName = doc.getFrName();
						frName = ResourceUtil.getString(frName);

						if (doc.getLogicalPath() != null && doc.getLogicalPath().split("\\.").length > 1) {
							frName = com.seeyon.v3x.edoc.util.Constants.Edoc_PAGE_SHOWPIGEONHOLE_SYMBOL
									+ java.io.File.separator + frName;
						}

						webEdocStat.setArchiveName(frName);
						webEdocStat.setLogicalPath((String) doc.getLogicalPath());
						break;
					}
				}
			}
		}
    	return webEdocStatList ;   
    }
    
    
    /**
     * 进入公文统计页面
     * @Author      : xuqiangwei
     * @Date        : 2014年12月8日下午5:30:51
     * @param request
     * @param response
     * @return
     */
	public ModelAndView mainEntry(HttpServletRequest request,HttpServletResponse response)throws Exception{
	    
	    String defualtTagCode = "rec_send_serarch";//默认选中页签
        String tagCode = request.getParameter("tagCode");
        tagCode = Strings.isBlank(tagCode) ? defualtTagCode : tagCode;
        ModelAndView mav = new ModelAndView("edoc/docstat/statIndex");
        mav.addObject("tagCode", tagCode);
        
        return mav;
	}
	
	/**
	 * 进入收发查询页面
	 * @Author      : xuqiangwei
	 * @Date        : 2014年12月9日上午9:55:08
	 * @param request
	 * @param response
	 * @return
	 */
	public ModelAndView initRecSendSearch(HttpServletRequest request,HttpServletResponse response){
	    
	    ModelAndView mav = new ModelAndView("edoc/docstat/edocStatMain");
        return mav;
	}
	
	/**
	 * 进入公文登记薄的高级查询页面
	 * @Author      : xuqiangwei
	 * @Date        : 2014年12月20日下午11:16:35
	 * @param request
	 * @param response
	 * @return
	 */
	public ModelAndView statCompQuery(HttpServletRequest request,HttpServletResponse response){
	    
	    String listType = request.getParameter("listType");
	    ModelAndView mav = null;
	    if("sendRegister".equals(listType)){
	        mav = new ModelAndView("edoc/docstat/edocSendRegisterCombQuery");
	    }else if("recRegister".equals(listType)){
	        mav = new ModelAndView("edoc/docstat/edocRecRegisterCombQuery");
	    }
	    
	    if(mav != null){
	        
	        String isG6 = SystemProperties.getInstance().getProperty("edoc.isG6");
	        mav.addObject("isG6", isG6);
	        
	        //G6登记开关
	        String registerSwitch = EdocSwitchHelper.isOpenRegister();
	        mav.addObject("registerSwitch", registerSwitch);
	    }

	    return mav;
	}
}