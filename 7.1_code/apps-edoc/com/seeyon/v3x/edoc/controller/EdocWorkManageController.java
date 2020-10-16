package com.seeyon.v3x.edoc.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.performancereport.api.PerformanceReportApi;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.excel.DataRecord;
import com.seeyon.ctp.common.excel.DataRow;
import com.seeyon.ctp.common.excel.FileToExcelManager;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.common.metadata.MetadataNameEnum;
import com.seeyon.v3x.common.web.login.CurrentUser;
import com.seeyon.v3x.edoc.manager.EdocWorkManageManager;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryModel;

public class EdocWorkManageController  extends BaseController {

	
	private static final Log log = LogFactory.getLog(EdocWorkManageController.class);
	private static final String edocRes = "com.seeyon.v3x.edoc.resources.i18n.EdocResource";
	
	private EnumManager enumManagerNew;
	private EdocWorkManageManager edocWorkManageManager;
	private FileToExcelManager fileToExcelManager;
	private CollaborationApi collaborationApi;
	private PerformanceReportApi performanceReportApi;
	
	public void setPerformanceReportApi(PerformanceReportApi performanceReportApi) {
		this.performanceReportApi = performanceReportApi;
	}


	public void setCollaborationApi(CollaborationApi collaborationApi) {
        this.collaborationApi = collaborationApi;
    }
	

	public void setEnumManagerNew(EnumManager enumManager) {
		this.enumManagerNew = enumManager;
	}
	
	public void setFileToExcelManager(FileToExcelManager fileToExcelManager) {
        this.fileToExcelManager = fileToExcelManager;
    }

    public void setEdocWorkManageManager(EdocWorkManageManager edocWorkManageManager) {
		this.edocWorkManageManager = edocWorkManageManager;
	}

	 /**
	  * 公文统计转协同, TODO 需要修改成前端调用
	  * @param request
	  * @param response
	  * @return
	  * @throws Exception
	  */
    @Deprecated
	 public ModelAndView edocReportToCol(HttpServletRequest request, HttpServletResponse response) throws Exception{
	     /*String title = request.getParameter("statType");
	     String statContent = request.getParameter("statContent");
	     List<Attachment> attachments = new ArrayList<Attachment>();
	     
	     TransNewColParam tParam = new TransNewColParam();
	     tParam.setSubject(title);
	     tParam.setBodyType(MainbodyType.HTML.getKey());
	     tParam.setContent(statContent);
	     tParam.setAtts(attachments);*/
	   //后台转协同方式已经取消，请使用前端JS接口转协同，如有疑问，请联系许强威
	     return null;//collaborationApi.appToColl(tParam);
	 }
	 
	 public ModelAndView listEdocExport(HttpServletRequest request, HttpServletResponse response) throws Exception{
	     User user = CurrentUser.get();
         Long memberId = null;
         String memberIdStr = request.getParameter("userId");
         String freeCol = request.getParameter("Checkbox1");//自由协同
         String templateCol = request.getParameter("Checkbox2");//模板协同
         String processType = null;
         if(Strings.isNotBlank(freeCol) && Strings.isBlank(templateCol)){
             processType = "freeCol";
         }else if(Strings.isBlank(freeCol) && Strings.isNotBlank(templateCol)){
             processType = "templateCol";
         }
         if(Strings.isNotBlank(memberIdStr)){
             memberId = Long.parseLong(memberIdStr);
         }else {
             memberId = user.getId();
         }
         
         int type = 0;//默认为 当前待办
         String typeStr = request.getParameter("typeStr");
         if(Strings.isNotBlank(typeStr)){   //超期统计(type 会传传多个状态值)
             String[] typeArry = typeStr.split(",");
             List<Integer> list = new ArrayList<Integer>(typeArry.length);
             for(String s : typeArry){
                 list.add(Integer.valueOf(s));
             }
             if(list.size() <2 && list.contains(StateEnum.col_done.key())){
                 type = 8;
             }else if(list.contains(StateEnum.col_done.key()) && list.contains(StateEnum.col_pending.key())){
                 type = 22;//包含已办+当前待办（待办不根据时查询）
             }else if(list.contains(StateEnum.col_sent.key()) && list.contains(StateEnum.col_done.key())){
                 type = 23;
             }else if(list.size() < 2 && list.contains(StateEnum.col_sent.key())){
                 type = 9;
             }
         }else {                            //日程工作统计
             typeStr = request.getParameter("type");
             if(Strings.isNotBlank(typeStr)){
                 type = Integer.parseInt(typeStr);
             }
         }
         Date beginDate = null;
         if(Strings.isNotBlank(request.getParameter("startTime"))){
             beginDate = Datetimes.getTodayFirstTime(request.getParameter("startTime"));
         }
         Date endDate = null;
         if(Strings.isNotBlank(request.getParameter("endTime"))){
             endDate = Datetimes.getTodayLastTime(request.getParameter("endTime"));
         }
         int coverTime = 2; //coverTime  0：未超期；1:超期；全部时不传值
         if(Strings.isNotBlank(request.getParameter("coverTime"))){
             try{
                 coverTime=Integer.parseInt(request.getParameter("coverTime"));
             }catch(Exception e){
                 coverTime=2;
             }
         }
         List<EdocSummaryModel> colList = edocWorkManageManager.queryEdocList(memberId, type, beginDate, endDate,coverTime, processType, false);
         convertExport(colList, request, response);
	     return null;
	 }
	 
	 public void convertExport(List<EdocSummaryModel> lists, HttpServletRequest request, HttpServletResponse response) throws Exception{
	     CtpEnumBean secretLevel = enumManagerNew.getEnum(MetadataNameEnum.edoc_secret_level.name());
	     CtpEnumBean deadline = enumManagerNew.getEnum(MetadataNameEnum.collaboration_deadline.name());
	     DataRecord dr = new DataRecord();
	     String[] colNames = new String[10];
	     colNames[0] = ResourceBundleUtil.getString(edocRes, "edoc.element.secretlevel.simple", new Object[0]);
	     colNames[1] = ResourceBundleUtil.getString(edocRes, "exchange.edoc.title", new Object[0]);
	     colNames[2] = ResourceBundleUtil.getString(edocRes, "edoc.element.wordno.label", new Object[0]);
	     colNames[3] = ResourceBundleUtil.getString(edocRes, "edoc.element.wordinno.label", new Object[0]);
	     colNames[4] = ResourceBundleUtil.getString(edocRes, "edoc.supervise.sender", new Object[0]);
	     colNames[5] = ResourceBundleUtil.getString(edocRes, "edoc.supervise.startdate", new Object[0]);
	     colNames[6] = ResourceBundleUtil.getString(edocRes, "edoc.supervise.managedate", new Object[0]);
	     colNames[7] = ResourceBundleUtil.getString(edocRes, "edoc.node.cycle.label", new Object[0]);
	     colNames[8] = ResourceBundleUtil.getString(edocRes, "edoc.isTrack.label", new Object[0]);
	     colNames[9] = ResourceBundleUtil.getString(edocRes, "hasten.number.label", new Object[0]);
	     dr.setColumnName(colNames);
	     dr.setTitle(request.getParameter("title"));
	     dr.setSheetName(request.getParameter("title"));
	     if (lists != null && lists.size() > 0) {
	         DataRow[] datarow = new DataRow[lists.size()];
	         for (int i = 0; i < lists.size(); i++) { 
	             datarow[i] = new DataRow();
	             datarow[i].addDataCell(Strings.isNotBlank(lists.get(i).getSummary().getSecretLevel())?
	                     ResourceBundleUtil.getString(edocRes,secretLevel.getItemLabel(String.valueOf(lists.get(i).getSummary().getSecretLevel()))):"",1);
	             datarow[i].addDataCell(lists.get(i).getSummary().getSubject(),1);
	             datarow[i].addDataCell(null != lists.get(i).getSummary().getDocMark() ?lists.get(i).getSummary().getDocMark():"",1);
	             datarow[i].addDataCell(null != lists.get(i).getSummary().getSerialNo() ? lists.get(i).getSummary().getSerialNo():"",1);
	             datarow[i].addDataCell(lists.get(i).getSummary().getStartMember().getName(),1);
	             datarow[i].addDataCell(Datetimes.formatDatetimeWithoutSecond(lists.get(i).getSummary().getCreateTime()),1);
	             datarow[i].addDataCell(null != lists.get(i).getDealTime() ? Datetimes.formatDatetimeWithoutSecond(lists.get(i).getDealTime()):"",1);
	             datarow[i].addDataCell(null != lists.get(i).getDeadLine() ? ResourceBundleUtil.getString(edocRes,deadline.getItemLabel(String.valueOf(lists.get(i).getDeadLine()))):"",1);
	             datarow[i].addDataCell(lists.get(i).getTrack() == 0 ? 
	                     ResourceBundleUtil.getString(edocRes, "edoc.form.no", new Object[0])
	                     :ResourceBundleUtil.getString(edocRes, "edoc.form.yes", new Object[0]),1);
	             datarow[i].addDataCell(String.valueOf(lists.get(i).getHastenTimes()),1);
	             
	         }
	         dr.addDataRow(datarow);
	     }
	     this.fileToExcelManager.save(response, ResourceBundleUtil.getString(edocRes, request.getParameter("title"),
	                     new Object[0]), new DataRecord[] { dr });
	 }
	 
	 public ModelAndView showListOfEdoc(HttpServletRequest request, HttpServletResponse response) throws Exception{
	     ModelAndView mav = new ModelAndView("edoc/edocStatList_main_frame");
	     String reportName = request.getParameter("reportName");
	     String reportNameEncode="";
			try {
				reportNameEncode = URLEncoder.encode(reportName,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				log.error("转url码异常!",e);
			}
	     mav.addObject("listMethod", "showListOfEdocIframe");
	     mav.addObject("reportName", reportNameEncode);
	     return mav;
     }
	 
	 public ModelAndView edocCoverTimeStat(HttpServletRequest request, HttpServletResponse response) throws Exception{
	     ModelAndView mav = new ModelAndView("edoc/edocStatList_main_frame");
	     mav.addObject("isReportPerfermance","1");
	     mav.addObject("listMethod", "edocCoverTimeStatIframe");
	     return mav;
	 }
	 
	 /**
	  * 公文超期统计
	  * @param request
	  * @param response
	  * @return
	  * @throws Exception
	  */
	 public ModelAndView edocCoverTimeStatIframe(HttpServletRequest request, HttpServletResponse response) throws Exception{
	     ModelAndView mv = new ModelAndView("edoc/edocStatList");
	     String reportName = request.getParameter("reportName");
	     mv.addObject("isReportPerfermance",request.getParameter("isReportPerfermance"));
	     User user = CurrentUser.get();
	     Long memberId = null;
	     String memberIdStr = request.getParameter("user_id");
	     if(Strings.isNotBlank(memberIdStr)){
	         memberId = Long.parseLong(memberIdStr);
	         if(memberId.equals(user.getId())){
	             mv.addObject("isCurrentUser", true);
	         }else{
	             mv.addObject("isCurrentUser", false);
	         }
	     }
	     else{
	         memberId = CurrentUser.get().getId();
	         mv.addObject("isCurrentUser", true);
	     }
	     Long reportId = Strings.isBlank(request.getParameter("reportId"))?0:Long.parseLong(request.getParameter("reportId"));
	     if(AppContext.hasPlugin("performancereport")){
	         if(!performanceReportApi.checkReport(reportId, CurrentUser.get().getAccountId(), memberId)) {
	             return mv;
	         }
	     }
	     int type = 0;//默认为 当前待办
	     String typeStr = request.getParameter("state");
	     if(Strings.isNotBlank(typeStr)){
	         String[] typeArry = typeStr.split(",");
	         List<Integer> list = new ArrayList<Integer>(typeArry.length);
	         for(String s : typeArry){
	             list.add(Integer.valueOf(s));
	         }
	         if(list.size() <2 && list.contains(StateEnum.col_done.key())){
	             type = 8;
	         }else if(list.contains(StateEnum.col_done.key()) && list.contains(StateEnum.col_pending.key())){
	             type = 22;//包含已办+当前待办（待办不根据时查询）
	         }
	     }
	     Date beginDate = null;
	     if(Strings.isNotBlank(request.getParameter("start_time"))){
	         beginDate = Datetimes.getTodayFirstTime(request.getParameter("start_time"));
	     }
	     Date endDate = null;
	     if(Strings.isNotBlank(request.getParameter("end_time"))){
	         endDate = Datetimes.getTodayLastTime(request.getParameter("end_time"));
	     }
	     int coverTime = 2; //coverTime  0：未超期；1:超期；全部时不传值
	     if(Strings.isNotBlank(request.getParameter("coverTime"))){
	         try{
	             coverTime=Integer.parseInt(request.getParameter("coverTime"));
	         }catch(Exception e){
	             coverTime=2;
	         }
	     }
	     List<EdocSummaryModel> colList = edocWorkManageManager.queryEdocList(memberId, type, beginDate, endDate,coverTime, null, true);
	     mv.addObject("colList", colList);
	      
	     Map<String, CtpEnumBean> colMetadata = enumManagerNew.getEnumsMap(ApplicationCategoryEnum.edoc);
	     CtpEnumBean attitude = enumManagerNew.getEnum(EnumNameEnum.collaboration_attitude.name()); //处理意见 attitude
	     colMetadata.put(MetadataNameEnum.collaboration_attitude.toString(), attitude);
	     CtpEnumBean deadline = enumManagerNew.getEnum(EnumNameEnum.collaboration_deadline.name()); //处理期限 attitude
	     colMetadata.put(MetadataNameEnum.collaboration_deadline.toString(), deadline);    
	     mv.addObject("colMetadata", colMetadata);
	     mv.addObject("type", type);
	     
	     mv.addObject("reportName", reportName);
	     mv.addObject("userId", request.getParameter("memberId"));
	     mv.addObject("typeStr", request.getParameter("state"));
	     mv.addObject("startTime", request.getParameter("start_time"));
	     mv.addObject("endTime", request.getParameter("end_time"));
	     mv.addObject("coverTime", request.getParameter("coverTime"));
	   
	     //标明绩效考核页面
	     mv.addObject("pageType", "edocPerformanceList");
	     
	     return mv;
	 }

	/**
     * 绩效管理 - 显示公文列表
     */
    public ModelAndView showListOfEdocIframe(HttpServletRequest request, HttpServletResponse response) throws Exception{
        ModelAndView mv = new ModelAndView("edoc/edocStatList");
        String reportName = request.getParameter("reportName");
        User user = CurrentUser.get();
        Long memberId = null;
        String memberIdStr = request.getParameter("memberId");
        String freeCol = request.getParameter("Checkbox1");//自由协同
        String templateCol = request.getParameter("Checkbox2");//模板协同
        String processType = null;//默认为  自由协同+模板协同
        if(Strings.isBlank(memberIdStr)){
            memberIdStr = request.getParameter("user_id");
        }
        if(Strings.isNotBlank(memberIdStr)){
            memberId = Long.parseLong(memberIdStr);
            if(memberId.equals(user.getId())){
            	mv.addObject("isCurrentUser", true);
            }else{
            	mv.addObject("isCurrentUser", false);
            }
        }
        else{
            memberId = CurrentUser.get().getId();
            mv.addObject("isCurrentUser", true);
        }
        Long reportId = Strings.isBlank(request.getParameter("reportId"))?0:Long.parseLong(request.getParameter("reportId"));
	    
        if(AppContext.hasPlugin("performancereport")){
            if(!performanceReportApi.checkReport(reportId, CurrentUser.get().getAccountId(), memberId)) {
                return mv;
            }
        }
        int type = 0;
        String typeStr = request.getParameter("type");
        if(Strings.isBlank(typeStr)){
            typeStr = request.getParameter("state");
        }
        if(Strings.isNotBlank(typeStr)){
            //来至流程已发已办统计
            if(Strings.isNotBlank(freeCol) || Strings.isNotBlank(templateCol)){
                if(Strings.isNotBlank(freeCol) && Strings.isBlank(templateCol)){
                    processType = "freeCol";
                }else if(Strings.isBlank(freeCol) && Strings.isNotBlank(templateCol)){
                    processType = "templateCol";
                }
                String[] typeArry = typeStr.split(",");
                List<Integer> list = new ArrayList<Integer>(typeArry.length);
                for(String s : typeArry){
                    list.add(Integer.valueOf(s));
                }
                if(list.contains(StateEnum.col_sent.key()) && list.contains(StateEnum.col_done.key())){
                    typeStr = "23";
                }else if(list.size() < 2 && list.contains(StateEnum.col_sent.key())){
                    typeStr = "9";
                }else if(list.size() < 2 && list.contains(StateEnum.col_done.key())){
                    typeStr = "8";
                }
            }
            type = Integer.parseInt(typeStr);
        }
        Date beginDate = null;
        if(Strings.isNotBlank(request.getParameter("beginDate"))){
        	beginDate = Datetimes.getTodayFirstTime(request.getParameter("beginDate"));
        	mv.addObject("startTime", request.getParameter("beginDate"));
        }else if(Strings.isNotBlank(request.getParameter("start_time"))){
            beginDate = Datetimes.getTodayFirstTime(request.getParameter("start_time"));
            mv.addObject("startTime", request.getParameter("start_time"));
        }
        Date endDate = null;
        if(Strings.isNotBlank(request.getParameter("endDate"))){
        	 endDate = Datetimes.getTodayLastTime(request.getParameter("endDate"));
        	 mv.addObject("endTime", request.getParameter("endDate"));
        }else if(Strings.isNotBlank(request.getParameter("end_time"))){
             endDate = Datetimes.getTodayLastTime(request.getParameter("end_time"));
             mv.addObject("endTime", request.getParameter("end_time"));
        }
        int coverTime = 2; //coverTime  0：未超期；1:超期；全部时不传值
        if(Strings.isNotBlank(request.getParameter("coverTime"))){
        	try{
        		coverTime=Integer.parseInt(request.getParameter("coverTime"));
        	}catch(Exception e){
        		coverTime=2;
        	}
        }
        List<EdocSummaryModel> colList = edocWorkManageManager.queryEdocList(memberId, type, beginDate, endDate,coverTime, processType, true);
        mv.addObject("colList", colList);
        
        Map<String, CtpEnumBean> colMetadata = enumManagerNew.getEnumsMap(ApplicationCategoryEnum.edoc);
        CtpEnumBean attitude = enumManagerNew.getEnum(EnumNameEnum.collaboration_attitude.name()); //处理意见 attitude
        colMetadata.put(MetadataNameEnum.collaboration_attitude.toString(), attitude);
        CtpEnumBean deadline = enumManagerNew.getEnum(EnumNameEnum.collaboration_deadline.name()); //处理期限 attitude
        colMetadata.put(MetadataNameEnum.collaboration_deadline.toString(), deadline);    
        mv.addObject("colMetadata", colMetadata);
        mv.addObject("type", type);

        mv.addObject("reportName", reportName);
        mv.addObject("userId", memberId);
        mv.addObject("coverTime", request.getParameter("coverTime"));
        mv.addObject("Checkbox1", request.getParameter("Checkbox1"));
        mv.addObject("Checkbox2", request.getParameter("Checkbox2"));
        mv.addObject("typeStr", request.getParameter("state"));//流程已发已办统计字段
        
        //标明绩效考核页面
        mv.addObject("pageType", "edocPerformanceList");
        
        return mv;
    }

}
