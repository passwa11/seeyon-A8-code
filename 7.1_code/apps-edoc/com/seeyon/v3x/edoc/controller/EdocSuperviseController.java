package com.seeyon.v3x.edoc.controller;

import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.common.supervise.enums.SuperviseEnum;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.supervise.vo.CtpSuperviseVO;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.v3x.common.metadata.MetadataNameEnum;
import com.seeyon.v3x.common.security.SecurityCheck;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.domain.EdocSummaryRelation;
import com.seeyon.v3x.edoc.manager.EdocManager;
import com.seeyon.v3x.edoc.manager.EdocSummaryManager;
import com.seeyon.v3x.edoc.manager.EdocSummaryRelationManager;
import com.seeyon.v3x.edoc.manager.EdocSuperviseManager;
import com.seeyon.v3x.edoc.util.Constants;
import com.seeyon.v3x.edoc.util.EdocSuperviseHelper;
import com.seeyon.v3x.edoc.webmodel.EdocSuperviseDealModel;
import com.seeyon.v3x.edoc.webmodel.EdocSuperviseModel;

public class EdocSuperviseController extends BaseController{
	
	public static final Log log = LogFactory.getLog(EdocSuperviseController.class);
	SuperviseManager edocSuperviseManagers= (SuperviseManager)AppContext.getBean("superviseManager");
	public EdocSuperviseManager edocSuperviseManager;
	public EdocSummaryManager edocSummaryManager; 
	public EdocManager edocManager;
	public AffairManager affairManager;
	public EnumManager enumManagerNew;
	public PermissionManager permissionManager;
	public OrgManager orgManager;
    private EdocSummaryRelationManager edocSummaryRelationManager = null;


	public void setEdocSummaryRelationManager(
            EdocSummaryRelationManager edocSummaryRelationManager) {
        this.edocSummaryRelationManager = edocSummaryRelationManager;
    }
    
	public EnumManager getEnumManagerNew() {
		return enumManagerNew;
	}

	public void setEnumManagerNew(EnumManager enumManager) {
		this.enumManagerNew = enumManager;
	}

	public EdocSuperviseManager getEdocSuperviseManager() {
		return edocSuperviseManager;
	}

	public void setEdocSuperviseManager(EdocSuperviseManager edocSuperviseManager) {
		this.edocSuperviseManager = edocSuperviseManager;
	}

	public ModelAndView index(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		return null;
	}
	
	public ModelAndView mainEntry(HttpServletRequest request,HttpServletResponse response)throws Exception{
		
		ModelAndView mav = new ModelAndView("edoc/edocFrameEntry");
		mav.addObject("varTempPageController", "edocSupervise.do");
		mav.addObject("entry", "listMain");
		
		return mav;
	}
	
	public ModelAndView listMain(HttpServletRequest request,HttpServletResponse response)throws Exception{
		
		ModelAndView mav = new ModelAndView("edoc/supervise/supervise_list_main");
		return mav;
	}
	/**
	 * 公文督办左侧页面
	 * 
	 * @return 转到superviseLeft.jsp页面
	 * @throws Exception
	 */
	public ModelAndView superviseLeft(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("edoc/supervise/superviseLeft");
		return mav;
	}
	
	
	public ModelAndView edit(HttpServletRequest request,HttpServletResponse response)throws Exception{
		
		return null;
	}
	
	/**
	 * 返回的是公文的流程查看页面
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
    public ModelAndView detail(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    	super.noCache(response);
    	String superviseId = request.getParameter("superviseId");
    	String summaryIdStr = request.getParameter("summaryId");
    	String isOpenFrom = request.getParameter("isOpenFrom");
    	User user=AppContext.getCurrentUser();
    	Long summaryId = null;
    	boolean flag = false;
    	if(Strings.isNotBlank(summaryIdStr)){
    		flag = true;
    	}
    	CtpAffair affair = null;
    	if(null!=superviseId || flag){
    		if(flag){
    			summaryId = Long.valueOf(summaryIdStr);
    		}else{
	    		CtpSuperviseDetail detail = edocSuperviseManager.getSuperviseById(Long.valueOf(superviseId));
	    		if(null!=detail){
	    			summaryId = detail.getEntityId();
	    	    }
	    	}
    		
    		/**
    		 * wangwei注解
    		 * getSenderAffair()和协同公用一个方法，查询结果包含了假删除的数据，不适应迁移公文
    		 * getEdocSenderAffair()公文重写适应迁移功能能，查询结果过滤假删除的数据进行验证
    		 */
    		affair = affairManager.getSenderAffair(summaryId);
    			String m = edocSuperviseManager.checkColSupervisor(summaryId, affair);
    			if(Strings.isNotBlank(m)){
    				response.setContentType("text/html;charset=UTF-8");
    				PrintWriter out = response.getWriter();
    				out.println("<script>");
    				out.println("alert(\"" + StringEscapeUtils.escapeJavaScript(m) + "\")");
    				out.println("if(window.dialogArguments){");
    				out.println("  window.returnValue = \"true\";");
    				out.println("  window.close();");
    				out.println("}else{");
    				out.println(" parent.opener.getA8Top().reFlesh();window.close();");
    				out.println("}");
    				out.println("");
    				out.println("</script>");
    				return null;
    			}
    	}
    	
    	//SECURITY 访问安全检查
    	//wangwei:这块的安全暂时不能放开，报错2013-02-23
    	if(!SecurityCheck.isLicit(request, response, ApplicationCategoryEnum.edoc, user, summaryId, affair, null)){
			return null;
		}
    	
    	String openModal = request.getParameter("openModal");
    	if(!"list".equals(openModal)){
    		openModal = "popup";
    	}
    	ModelAndView mav = new ModelAndView("edoc/edocSuperiseDetail");
    	mav.addObject("summaryId", summaryId);
		mav.addObject("controller", "edocController.do");
		mav.addObject("from", "supervise");
		mav.addObject("openModal", openModal);
		mav.addObject("isOpenFrom",isOpenFrom);
		if(affair!=null){
			mav.addObject("affairId", affair.getId());
		}
		
		//如果有关联发文，前台做展示
		String relSends = request.getParameter("relSends");
        String relRecs = request.getParameter("relRecs");
        
        //关联发文
        if(Strings.isBlank(relSends)){
            relSends = "haveNot";
            
            long relationSummaryId = summaryId;
            List<EdocSummary> newEdocList = null;
            String edocType=request.getParameter("edocType");
            String detailType = request.getParameter("detailType");
            
            /*********puyc 关联发文 * 收文的Id,收文的Type*********/
            if(Strings.isBlank(detailType)){ 
                newEdocList = this.edocSummaryRelationManager.findNewEdoc(relationSummaryId, user.getId(), 1);
            }
            else if(!"listSent".equals(detailType)){ 
                newEdocList = this.edocSummaryRelationManager.findNewEdocByRegisteredOrWaitSent(relationSummaryId, user.getId(), 1,2);
            }
            if(Strings.isEmpty(newEdocList)&&"1".equals(edocType) ){
                newEdocList = this.edocSummaryRelationManager.findNewEdoc(relationSummaryId, user.getId(), 1);
            }
            
            if(Strings.isNotEmpty(newEdocList)){
                relSends = "haveMany";
                mav.addObject("recEdocId",relationSummaryId);
                mav.addObject("recType",1);
                mav.addObject("relSends",relSends);
           }
        }
        
        //关联收文
        if(Strings.isBlank(relRecs)){
            relRecs = "haveNot";
            
            String canNotOpen = request.getParameter("canNotOpen");
            String sendSummaryId = request.getParameter("sendSummaryId"); //从发文关联收文点击进来的,这个是发文summaryId
            
            EdocSummaryRelation edocSummaryRelationR = this.edocSummaryRelationManager.findRecEdoc(summaryId, 0);
            if(edocSummaryRelationR != null){
                relRecs = "haveMany";
                if("isYes".equals(canNotOpen) || Strings.isNotBlank(sendSummaryId)){
                    relRecs = "haveNot";
                }
                mav.addObject("relRecs",relRecs);
            }
        }
        
        
		return mav;
	}
    /**
     * logEntry为superviseLog的入口,在superviseLog外边套一层框架
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView logEntry(HttpServletRequest request,HttpServletResponse response)throws Exception{
    	String superviseId = request.getParameter("superviseId");
    	ModelAndView mav = new ModelAndView("edoc/supervise/superviseLogIframe");
    	return mav.addObject("superviseId", superviseId);
    }
    
    
    /**
     * 更改督办的内容摘要
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView updateContent(HttpServletRequest request,HttpServletResponse response)throws Exception{
    	
    	String content = request.getParameter("content");
    	String superviseId = request.getParameter("superviseId");
    	String processState = request.getParameter("status");

    	if(null!=superviseId && null!=content){
    		CtpSuperviseDetail detail = edocSuperviseManager.getSuperviseById(Long.valueOf(superviseId));

    		detail.setDescription(content);
    		edocSuperviseManager.changeSuperviseDetail(detail);
    		}
    	int proType = Constants.EDOC_SUPERVISE_PROGRESSING;
    	if(!Strings.isBlank(processState) && Integer.valueOf(processState)==Constants.EDOC_SUPERVISE_TERMINAL){
    		proType = Constants.EDOC_SUPERVISE_TERMINAL;
    	}
    	ModelAndView mav = getUpdatedMAV(proType);
    	mav.addObject("status", processState);
		mav.addObject("label", request.getParameter("label"));
    	return mav;
    	
    }
    
    /**
     * 查看督办的内容摘要
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView showDescription(HttpServletRequest request,HttpServletResponse response)throws Exception{
    	String superviseId = request.getParameter("superviseId");
    	//String content = request.getParameter("content");
    	String content = "";
    	String title = "";
    	int status = Constants.EDOC_SUPERVISE_PROGRESSING;
    	if(null!=superviseId && !"".equals(superviseId)){
    		CtpSuperviseDetail detail = edocSuperviseManager.getSuperviseById(Long.valueOf(superviseId));
    		if(null!=detail){
    			content = detail.getDescription();
    			title = detail.getTitle();
    			status = detail.getStatus();
    		}
    	}
    	return new ModelAndView("edoc/supervise/superviseDescription").addObject("content", content).addObject("superviseId", superviseId).addObject("title", title).addObject("status",status);
    }
    

    public ModelAndView changeSupervise(HttpServletRequest request,HttpServletResponse response)throws Exception{
		String superviseId = request.getParameter("superviseId");
		String awakeDate = request.getParameter("awakeDate");
		String description = request.getParameter("description");

		CtpSuperviseDetail detail = this.edocSuperviseManager.getSuperviseById(Long.valueOf(superviseId));

		if(detail!=null){
			if(Strings.isNotBlank(awakeDate)){
				detail.setAwakeDate(Datetimes.parse(awakeDate, Datetimes.datetimeWithoutSecondStyle));
			}
			if(Strings.isNotBlank(description)){
				detail.setDescription(description);
			}
		}
		this.edocSuperviseManager.changeSuperviseDetail(detail);
		return null;
    }
    /**
     * 修改督办的截止时间
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
	public ModelAndView change(HttpServletRequest request,HttpServletResponse response)throws Exception{
		
		String superviseId = request.getParameter("superviseId");
		String endDate = request.getParameter("endDate");
		String subject = request.getParameter("subject");
			if(null!=superviseId && null!=endDate){
			CtpSuperviseDetail detail = edocSuperviseManager.getSuperviseById(Long.valueOf(superviseId));
			//debug:36574 start author:MENG
			//debug:36574 end
			detail.setAwakeDate(DateUtil.parse(endDate, "yyyy-MM-dd HH:mm"));
			
			String summarySubject = "...";
			
			//如果从页面获取的标为空,查summary找到subject
			if(null==subject || "".equals(subject)){
				EdocSummary summary = edocSummaryManager.findById(detail.getEntityId());
				summarySubject = summary.getSubject(); 
			}
			
			//--更改完督办时间后,根据新时间生成定时器,届时自动提醒所有督办人
        	try{
    			String scheduleProp = detail.getScheduleProp();
    			if(null!=scheduleProp){
	    			String[] scheduleProp_Array = scheduleProp.split("\\|");
	    			if(null!=scheduleProp_Array && scheduleProp_Array.length > 0){
	    				QuartzHolder.deleteQuartzJobByGroupAndJobName(scheduleProp_Array[1], scheduleProp_Array[0]);
    				}
    			}
    			
    			Long jobId = UUIDLong.longUUID();
                String jobName = jobId.toString();
                Long groupId = UUIDLong.longUUID();
                String groupName = groupId.toString();
                
                Date eDate = detail.getAwakeDate();
                if(eDate.before(new Date())){
                	eDate = Datetimes.addSecond(new Date(), 5);  //如果设置督办时间比现在时间提前，或者是当天，过5秒后提醒。
                }else{
                	eDate = Datetimes.addHour(eDate, -16);   //设置后前一天8点提醒。
                }
    			
    			//--根据detail获取所有督办人的id,组成字符串传到后台

     			StringBuilder ids = new StringBuilder("");
     			List<CtpSupervisor> s_list = edocSuperviseManager.listSupervies(detail.getId());
    			for(CtpSupervisor s:s_list){
    				ids.append(s.getSupervisorId()).append(",");
    			}
    			if(ids.toString().endsWith(",")){
    				ids.deleteCharAt(ids.length()-1);
    			}
                
                detail.setScheduleProp(jobName + "|" + groupName);
                
                Map<String, String> parameterMap = new HashMap<String, String>();
				parameterMap.put("edocSuperviseId", String.valueOf(detail.getId()));
				parameterMap.put("supervisorMemberId", ids.toString());
				parameterMap.put("subject", null!=subject && !"".equals(subject) ? subject : summarySubject);
                QuartzHolder.newQuartzJob(groupName, jobName, eDate, "terminateEdocSuperviseJob", parameterMap);
        	}catch(Exception e){
        		log.error(e.getMessage(), e);
        	}
        	
			edocSuperviseManager.changeSuperviseDetail(detail);
			//--
		}
		
		//--返回
		
		
		
    	String processState = request.getParameter("status");
    	int proType = Constants.EDOC_SUPERVISE_PROGRESSING;
    	if(!Strings.isBlank(processState) && Integer.valueOf(processState)==Constants.EDOC_SUPERVISE_TERMINAL){
    		proType = Constants.EDOC_SUPERVISE_TERMINAL;
    	}
    	ModelAndView mav = getUpdatedMAV(proType);
    	mav.addObject("status", processState);
		mav.addObject("label", request.getParameter("label"));
		return mav;
	}
	

	/**
	 * 弹出催办的消息页面
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView hasten(HttpServletRequest request,HttpServletResponse response)throws Exception{
		
		ModelAndView mav = new ModelAndView("edoc/supervise/superviseMessage");
        String processId = request.getParameter("processId");
        String activityId = request.getParameter("activityId");
        String superviseId = request.getParameter("superviseId");		

        mav.addObject("processId", processId);
        mav.addObject("activityId", activityId);
        mav.addObject("superviseId", superviseId);
		return mav;
	}
	
	/**
	 * 应comrade jincm的要求，将修改流程方式进行改变
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView showDigarm(HttpServletRequest request,HttpServletResponse response)throws Exception{
		//String superviseId = request.getParameter("superviseId");
		String isSuperviseStr = request.getParameter("isSupervise");
		//String summaryId = request.getParameter("summaryId");
		ModelAndView mav = new ModelAndView("edoc/showDiagram");
		
		Boolean isSupervise = Strings.isBlank(isSuperviseStr) || (Strings.isNotBlank(isSuperviseStr) && Boolean.parseBoolean(isSuperviseStr));
		mav.addObject("isSupervise", isSupervise);
		//Boolean hasWorkflow = false;        //是否还存在流程
		//String process_desc_by = "";        //路程排序
		//boolean hasDiagram = false;    
		int iEdocType = 0;
		

         //   hasWorkflow = Boolean.TRUE;

    	CtpEnumBean remindMetadata =  enumManagerNew.getEnum(EnumNameEnum.common_remind_time.name());
    	CtpEnumBean  deadlineMetadata=  enumManagerNew.getEnum(EnumNameEnum.collaboration_deadline.name());
        
        mav.addObject("remindMetadata", remindMetadata);
        mav.addObject("deadlineMetadata", deadlineMetadata); 
        
        mav.addObject("controller", "edocController.do");
        mav.addObject("appName",EdocEnum.getEdocAppName(iEdocType));
        mav.addObject("templeteCategrory",EdocEnum.getTemplateCategory(iEdocType)); 
        
        CtpEnumBean flowPermPolicyMetadata=null; 
        String defaultPerm="shenpi";
        if(EdocEnum.edocType.recEdoc.ordinal()==iEdocType)
    	{
    		mav.addObject("policy", "dengji");
    		mav.addObject("newEdoclabel", "edoc.new.type.rec");
    		flowPermPolicyMetadata= enumManagerNew.getEnum(EnumNameEnum.edoc_rec_permission_policy.name());
    		defaultPerm="yuedu";
    	}
        else if(EdocEnum.edocType.sendEdoc.ordinal()==iEdocType)
    	{
        	mav.addObject("policy", "niwen");
        	mav.addObject("newEdoclabel", "edoc.new.type.send");
    		flowPermPolicyMetadata= enumManagerNew.getEnum(EnumNameEnum.edoc_send_permission_policy.name());
    	}
    	else
    	{
    		mav.addObject("policy", "niwen");
    		mav.addObject("newEdoclabel", "edoc.new.type.send");
    		flowPermPolicyMetadata= enumManagerNew.getEnum(EnumNameEnum.edoc_qianbao_permission_policy.name());
    	}
        mav.addObject("defaultPermLabel", "node.policy."+defaultPerm);
        mav.addObject("flowPermPolicyMetadata",flowPermPolicyMetadata);
    	
    	//分支 结束    	

    	return mav;
	}
	
	/**
	 * 给当前的节点发送催办的消息
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView sendMessage(HttpServletRequest request,HttpServletResponse response)throws Exception{
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		
		String processId = request.getParameter("processId");
		String superviseId = request.getParameter("superviseId");
		String summaryId = request.getParameter("summaryId");
		String mode = request.getParameter("remindMode");
		String additional_mark = request.getParameter("content");
		String activityId = request.getParameter("activityId");

		String[] people = request.getParameterValues("deletePeople");
        long[] receivers = new long[people.length];
        int i = 0;
        for(String p :people) {
        	receivers[i] = Long.parseLong(p);
        	i++;
        }
        
		edocSuperviseManager.sendMessage(Long.valueOf(superviseId), mode, processId,activityId, additional_mark,receivers,summaryId);

				
		String alertNote = ResourceUtil.getString("edoc.supervise.sendMessage.success");
						
		try{

			out.println("<script>");
			if(Strings.isNotBlank(superviseId)){
				Long id = Long.parseLong(superviseId);
				out.println("parent.setHastenTimesBack('" + this.edocSuperviseManager.getHastenTimes(id)+ "');");
			}
			out.println("alert('"+alertNote+"');");
			out.println("parent.close()");
			out.println("</script>");
			return null;
		}catch(Exception e){
			alertNote = ResourceUtil.getString("edoc.supervise.sendMessage.failure");
			out.println("<script>");
			out.println("alert('"+alertNote+"');");
			out.println("parent.close()");
			out.println("</script>");		
			return null;
		}

	}
	
	public ModelAndView deleteSuperviseDetail(HttpServletRequest request,HttpServletResponse response)throws Exception{
		
		String ids = request.getParameter("id");
		
		if(null!=ids && !"".equals(ids)){
			edocSuperviseManager.deleteSuperviseDetail(ids);
		}
		
		return super.refreshWindow("parent");
	}
	
	/**
	 * 公文督办打开的督办窗口
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView superviseWindowEntry(HttpServletRequest request,HttpServletResponse response)throws Exception{
		
		ModelAndView mav = new ModelAndView("edoc/supervise/supervise_window_iframe");
		
		String str_summaryId = request.getParameter("summaryId");
		
		mav.addObject("summaryId", str_summaryId);
		
		return mav;
	}
	
	/**
	 * 公文督办选择窗口
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView superviseWindow(HttpServletRequest request,HttpServletResponse response)throws Exception{
		
		ModelAndView mav = new ModelAndView("edoc/supervise/supervise_window");
		
		mav.addObject("assignedMemberId", request.getParameter("assignedMemberId"));
		mav.addObject("assignedDate", request.getParameter("assignedDate"));
		
		String str_summaryId = request.getParameter("summaryId");
		if(null!=str_summaryId && !"".equals(str_summaryId)){
			Long summaryId = Long.valueOf(str_summaryId);
	    	
			Object[] object = EdocSuperviseHelper.getSupervisorIdsBySummaryId(summaryId);
	    	if(null!=object && object.length > 0){
	    		String supervisorIds = (String)object[0]; //督办人的ID
	    		Date endDate = (Date)object[1];     //督办时间
	    		mav.addObject("supervisorIds", supervisorIds);
	    		mav.addObject("endDate", endDate);
	    	}
	    	
	    	CtpSuperviseDetail detail = edocSuperviseManagers.getSupervise(summaryId);
	    	
	    	if(null!=detail){
	    		mav.addObject("supervisorNames", detail.getSupervisors());
	    		List<CtpSupervisor> set = edocSuperviseManagers.getSupervisors(detail.getId());
	    		if(null!=set && set.size()>0){
	    			mav.addObject("count", set.size());
	    		}
	    	}
		}
		String iscol = request.getParameter("iscol");
    	mav.addObject("iscol", Strings.isNotBlank(iscol)?Boolean.valueOf(iscol):false);
    	
    	String superviseTitle = request.getParameter("superviseTitle");
    	if(Strings.isNotBlank(superviseTitle)){
    		superviseTitle = superviseTitle.replaceAll("<br/>", "\n");
    		mav.addObject("superviseTitle", superviseTitle);
    	}
    	return mav;
	}
	
    public ModelAndView saveSupervise(HttpServletRequest request,HttpServletResponse response)throws Exception{
    	String superviseId = request.getParameter("superviseId");
    	String supervisorNames = request.getParameter("supervisorNames");
    	String supervisorMemberId = request.getParameter("supervisorMemberId");
    	String superviseDate = request.getParameter("superviseDate");
    	String title = request.getParameter("title");
    	String summaryId = request.getParameter("summaryId");
    	 User user = AppContext.getCurrentUser();
    	if(Strings.isNotBlank(summaryId)){
    	    EdocSummary summary = edocSummaryManager.findById(Long.parseLong(summaryId));
    	    
    	    if("true".equals(request.getParameter("isDelete"))){
    	        edocSuperviseManager.deleteSuperviseDetailAndSupervisors(summary);
    	    } else {

                String[] ids = supervisorMemberId.split(",");
                Long[] supervisorIds = null;
                if (ids != null) {
                    supervisorIds = new Long[ids.length];
                    int i = 0;
                    for (String id : ids) {
                        supervisorIds[i] = Long.parseLong(id);
                        i++;
                    }
                }
                
                long superviseDetailId = -1;
                
                if (Strings.isNotBlank(superviseId)){ //更新原督办设置
                    superviseDetailId = Long.parseLong(superviseId);
                    
                    CtpSuperviseDetail detail = this. edocSuperviseManagers.get(superviseDetailId);
                    CtpSuperviseVO ctpSuperviseVO = new CtpSuperviseVO();
                    this.Po2Vo(detail, ctpSuperviseVO);
                    ctpSuperviseVO.setSupervisorIds(supervisorIds);
                    ctpSuperviseVO.setSupervisors(supervisorNames);
                    ctpSuperviseVO.setEntityType(SuperviseEnum.EntityType.edoc.ordinal());
                    ctpSuperviseVO.setTitle(title);
                    ctpSuperviseVO.setAwakeDate(Datetimes.parse(superviseDate));
                    edocSuperviseManager.update(ctpSuperviseVO,Long.valueOf(summaryId));
                }
                else{ //新设置督办
                    superviseDetailId = UUIDLong.longUUID();
                    CtpSuperviseDetail detail = this.edocSuperviseManagers.getSupervise(Long.parseLong(summaryId));
                    CtpSuperviseVO ctpSuperviseVO = new CtpSuperviseVO();
                    if(detail != null) {
                    	this.Po2Vo(detail, ctpSuperviseVO);
                    	ctpSuperviseVO.setSupervisorIds(supervisorIds);
                        ctpSuperviseVO.setSupervisors(supervisorNames);
                        ctpSuperviseVO.setEntityType(SuperviseEnum.EntityType.edoc.ordinal());
                        ctpSuperviseVO.setTitle(title);
                        ctpSuperviseVO.setAwakeDate(Datetimes.parse(superviseDate));
                        edocSuperviseManager.update(ctpSuperviseVO,Long.valueOf(summaryId));
                    } else {
	                    ctpSuperviseVO.setId(superviseDetailId);
	                    ctpSuperviseVO.setTitle(title);
	                    ctpSuperviseVO.setSupervisors(supervisorNames);
	                    ctpSuperviseVO.setSupervisorIds(supervisorIds);
	                    ctpSuperviseVO.setAwakeDate(Datetimes.parse(superviseDate));
	                    ctpSuperviseVO.setEntityType(SuperviseEnum.EntityType.edoc.ordinal());
	                    ctpSuperviseVO.setEntityId(Long.parseLong(summaryId));
	                    ctpSuperviseVO.setStatus(SuperviseEnum.superviseState.supervising.ordinal());
	                    ctpSuperviseVO.setCreateDate(new Date());
	                    ctpSuperviseVO.setCount(0l);
	                    ctpSuperviseVO.setSenderId(user.getId());  
	                    edocSuperviseManager.save(ctpSuperviseVO);
                    }
                }
            
                
                this.createQuarz4Supervise(Long.parseLong(summaryId), superviseDetailId, Datetimes.parse(superviseDate), summary.getStartUserId(), supervisorMemberId, summary.getSubject());
            }
        }
    	response.setContentType("text/html;charset=UTF-8");
    	PrintWriter out = response.getWriter();
    	out.println("<script>");
    	out.println(" parent._closeWin();");
    	out.println("</script>");
    	return null;
    }
	
    
    private  void createQuarz4Supervise(long summaryId,
            Long detailId,
            Date superviseDate ,
            long senderId,
            String supervisorMemberId,
            String subject){
        String name = "ColSupervise" + summaryId;

        try{
            QuartzHolder.deleteQuartzJob(name);
        }
        catch(Exception e) {
            log.error("",e);
        }

        try {
            Map<String, String> p = new HashMap<String, String>(4);
            p.put("colSuperviseId", String.valueOf(detailId));
            p.put("senderId", String.valueOf(senderId));
            p.put("supervisorMemberId", supervisorMemberId.toString());
            p.put("subject", subject);
            QuartzHolder.newQuartzJob(name, superviseDate, "terminateColSuperviseJob", p);
        }
        catch(Exception e) {
            log.error("",e);
        }
    }
    
    private CtpSuperviseVO Po2Vo(CtpSuperviseDetail detail,CtpSuperviseVO ctpSuperviseVO){
	    //督办事项id
	    ctpSuperviseVO.setId(detail.getId());
        //督办主题
	    ctpSuperviseVO.setTitle(detail.getTitle());
        //实体类型
	    ctpSuperviseVO.setEntityType(detail.getEntityType());
        //实体ID
	    ctpSuperviseVO.setEntityId(detail.getEntityId());
        //发起人ID
	    ctpSuperviseVO.setSenderId(detail.getSenderId());
        //状态
	    ctpSuperviseVO.setStatus(detail.getStatus());
        //督办人
	    ctpSuperviseVO.setSupervisors(detail.getSupervisors());
        //描述
	    ctpSuperviseVO.setDescription(detail.getDescription());
        //催办次数
	    ctpSuperviseVO.setCount(detail.getCount());
        //督办日期
	    ctpSuperviseVO.setAwakeDate(detail.getAwakeDate());
        //提醒模式
	    ctpSuperviseVO.setRemindMode(detail.getRemindMode());
        //超期提醒参数
	    ctpSuperviseVO.setScheduleProp(detail.getScheduleProp());
        //完成时间
	    ctpSuperviseVO.setCreateDate(detail.getCreateDate());
        //模版期限
	    ctpSuperviseVO.setTemplateDateTerminal(detail.getTemplateDateTerminal());
        //督办日期类型
	    ctpSuperviseVO.setSuperviseDateType(detail.getSuperviseDateType());
	    return ctpSuperviseVO;
	}
	
	/**
	 * 用于刷新页面的mav,应该可以使用loaction.reload()代替
	 * @return
	 */
	
	
	
    public ModelAndView showAffairEntry(HttpServletRequest request,HttpServletResponse response)throws Exception{
    	ModelAndView mv = new ModelAndView("edoc/supervise/showAffairEntry");
    	return mv;
    }
    
    public ModelAndView showAffair(HttpServletRequest request,HttpServletResponse response)throws Exception{
    	String summaryId = request.getParameter("summaryId");
    	List<EdocSuperviseDealModel> models = this.edocSuperviseManager.getAffairModel(Long.parseLong(summaryId));
    	ModelAndView mv = new ModelAndView("collaboration/supervise/showAffair");
    	mv.addObject("models", models);
    	return mv;
    }
	
	private ModelAndView getUpdatedMAV(int state){
		ModelAndView mav = new ModelAndView("edoc/supervise/supervise_list_iframe");
		List<EdocSuperviseModel> list = null;
		mav.addObject("list", list);
		Map<String, CtpEnumBean> colMetadata = enumManagerNew.getEnumsMap(ApplicationCategoryEnum.edoc);
		mav.addObject("colMetadata", colMetadata);
		return mav;	
	}
	
	 /**
     * 用于在列表上只显示流程图(没有调用showDigram),去掉了内容和流程处理部分.
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView showDigramOnly(HttpServletRequest request,HttpServletResponse response)throws Exception{
		String superviseId = request.getParameter("superviseId");
		String edocId = request.getParameter("edocId");
		ModelAndView mav = new ModelAndView("edoc/supervise/superviseDiagram");
		
		CtpSuperviseDetail detail = edocSuperviseManager.getSuperviseById(Long.valueOf(superviseId));
		if(null!=detail){
			Boolean hasWorkflow = false;        //是否还存在流程
			String processDescBy = "";        //路程排序
			boolean hasDiagram = false;    
			int iEdocType = 0;
			
			EdocSummary summary = edocSummaryManager.findById(detail.getEntityId());
			if(summary == null){
			    log.error("summary is null,id:"+detail.getEntityId());
			    return mav;
			}
	    	if(summary.getCaseId() != null){
	    		hasDiagram = true;
	    		iEdocType = summary.getEdocType();
	    	}
    		
	    	Map<String, CtpEnumBean> colMetadata = enumManagerNew.getEnumsMap(ApplicationCategoryEnum.edoc);
	    	CtpEnumBean comMetadata = enumManagerNew.getEnum(EnumNameEnum.common_remind_time.name());
	    	CtpEnumBean remindMetadata = enumManagerNew.getEnum(MetadataNameEnum.common_remind_time.name());
	    	CtpEnumBean  deadlineMetadata= enumManagerNew.getEnum(MetadataNameEnum.collaboration_deadline.name());
	        
	        mav.addObject("remindMetadata", remindMetadata);
	        mav.addObject("deadlineMetadata", deadlineMetadata); 
	        
	        mav.addObject("controller", "edocController.do");
	        mav.addObject("appName",EdocEnum.getEdocAppName(iEdocType));
	        mav.addObject("templeteCategrory",EdocEnum.getTemplateCategory(iEdocType)); 
	        
	        CtpEnumBean flowPermPolicyMetadata=null; 
	        String defaultPerm="shenpi";
	        if(EdocEnum.edocType.recEdoc.ordinal()==iEdocType)
	    	{
	    		mav.addObject("policy", "dengji");
	    		mav.addObject("newEdoclabel", "edoc.new.type.rec");
	    		flowPermPolicyMetadata=enumManagerNew.getEnum(MetadataNameEnum.edoc_rec_permission_policy.name());
	    		defaultPerm="yuedu";
	    	}
	        else if(EdocEnum.edocType.sendEdoc.ordinal()==iEdocType)
	    	{
	        	mav.addObject("policy", "niwen");
	        	mav.addObject("newEdoclabel", "edoc.new.type.send");
	    		flowPermPolicyMetadata=enumManagerNew.getEnum(MetadataNameEnum.edoc_send_permission_policy.name());
	    	}
	    	else
	    	{
	    		mav.addObject("policy", "niwen");
	    		mav.addObject("newEdoclabel", "edoc.new.type.send");
	    		flowPermPolicyMetadata=enumManagerNew.getEnum(MetadataNameEnum.edoc_qianbao_permission_policy.name());
	    	}
	        int actorId=EdocEnum.getStartAccessId(iEdocType);
	        mav.addObject("defaultPermLabel", "node.policy."+defaultPerm);
	        mav.addObject("flowPermPolicyMetadata",flowPermPolicyMetadata);
	        boolean templateFlag = false;
            if(summary.getTempleteId() != null) templateFlag = true;
            mav.addObject("templateFlag", templateFlag);
	    	mav.addObject("comMetadata", comMetadata);
	    	mav.addObject("colMetadata", colMetadata);
	    	mav.addObject("summary", summary);
	    	mav.addObject("isShowButton", false);
	    	mav.addObject("hasDiagram", hasDiagram);
	    	mav.addObject("process_desc_by", processDescBy);
	    	mav.addObject("hasWorkflow", hasWorkflow);
	    	mav.addObject("actorId",actorId);
	    	mav.addObject("superviseId", superviseId);
	    	mav.addObject("processId", summary.getProcessId());
	    	mav.addObject("caseId", summary.getCaseId());
	    	mav.addObject("summaryId", edocId);
    	}

    	return mav;
    }

	public EdocSummaryManager getEdocSummaryManager() {
		return edocSummaryManager;
	}

	public void setEdocSummaryManager(EdocSummaryManager edocSummaryManager) {
		this.edocSummaryManager = edocSummaryManager;
	}

	public EdocManager getEdocManager() {
		return edocManager;
	}

	public void setEdocManager(EdocManager edocManager) {
		this.edocManager = edocManager;
	}

	public AffairManager getAffairManager() {
		return affairManager;
	}

	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}

	public PermissionManager getPermissionManager() {
		return permissionManager;
	}

	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}

	public OrgManager getOrgManager() {
		return orgManager;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

}