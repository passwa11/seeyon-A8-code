package com.seeyon.ctp.rest.resources;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.cip.api.pending.CipPendingApi;
import com.seeyon.apps.collaboration.listener.WorkFlowEventListener;
import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.collaboration.util.CollaborationUtils;
import com.seeyon.apps.taskmanage.util.MenuPurviewUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.bo.AffairCondition;
import com.seeyon.ctp.common.affair.bo.PendingRow;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.manager.PendingManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.comment.CommentManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.flag.BrowserEnum;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormAuthViewBean;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.portal.portlet.manager.DeskCollaborationProcessManager;
import com.seeyon.ctp.services.ServiceException;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.annotation.RestInterfaceAnnotation;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ctp.workflow.wapi.PopResult;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.oainterface.impl.exportdata.PendingListExporter;
import com.seeyon.oainterface.impl.exportdata.TrackListExporter;

import net.joinwork.bpm.definition.BPMActivity;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.definition.BPMSeeyonPolicy;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;

@Path("/affair")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class AffairResource extends BaseResource {
    
    private DeskCollaborationProcessManager deskCollaborationProcessManager;
    
    private AffairManager affairManager;
    
    private FormApi4Cap3 formApi4Cap3;
    
    private WorkflowApiManager wapi;
    
    private ColManager colManager;
    
    private CommentManager ctpCommentManager;
    
    private OrgManager orgManager;
    
    private PendingManager pendingManager;
    
    private CipPendingApi cipPendingApi;
    
	private static Log  log = LogFactory.getLog(AffairResource.class);
	
	/**
	 * 获取待办事项(不包含代理事项)
	 * @param ticketId sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
	 * @param firstNum 列表第一条开始的位置号，“0”为第一条。
	 * @param pageSize 获取列表条数。
	 * @return
	 * @throws com.seeyon.v3x.services.ServiceException
	 */
	@GET
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("pending/{ticketId}/{firstNum}/{pageSize}")
	@RestInterfaceAnnotation
	public Response exportPendingList(@PathParam("ticketId")String ticketId, 
			@PathParam("firstNum") int firstNum, @PathParam("pageSize") int pageSize) throws ServiceException {
		PendingListExporter  exporter=new PendingListExporter();
		try {
			return	ok(exporter.getPendingList(ticketId,firstNum,pageSize));
		} catch (ServiceException e) {
			log.error(e.getMessage(), e);
			throw new ServiceException(e.getErrorNumber(),e.getMessage());
		}
		
	}
	
	/**
	 * 获取跟踪事项列表
	 * @param ticketId sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
	 * @param firstNum 列表第一条开始的位置号，“0”为第一条。
	 * @param pageSize 获取列表条数。
	 * @return
	 * @throws com.seeyon.v3x.services.ServiceException
	 */
	@GET
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("track/{ticketId}/{firstNum}/{pageSize}")
	@RestInterfaceAnnotation
	public Response exportTrackList(@PathParam("ticketId")String ticketId, @PathParam("firstNum") int firstNum, 
			@PathParam("pageSize") int pageSize) throws ServiceException {
		TrackListExporter  exporter = new TrackListExporter();
		try {
			return	ok(exporter.getTrackList(ticketId,firstNum,pageSize));
		} catch (ServiceException e) {
			log.error(e.getMessage(), e);
			throw new ServiceException(e.getErrorNumber(),e.getMessage());
		}
	}

	/**
	 * 获取代理待办事项
	 * @param ticketId sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
	 * @param firstNum 列表第一条开始的位置号，“0”为第一条。
	 * @param pageSize 获取列表条数。
	 * @return
	 * @throws com.seeyon.v3x.services.ServiceException
	 */
	@GET
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("agent/{ticketId}/{firstNum}/{pageSize}")
	@RestInterfaceAnnotation
	public Response exportAgentPendingList(@PathParam("ticketId")String ticketId, 
			@PathParam("firstNum") int firstNum, @PathParam("pageSize") int pageSize) throws ServiceException  {
		PendingListExporter  exporter=new PendingListExporter();
		try {
			return	ok(exporter.getAgentPendingList(ticketId,firstNum,pageSize));
		} catch (ServiceException e) {
			log.error(e.getMessage(), e);
			throw new ServiceException(e.getErrorNumber(),e.getMessage());
		}
	}
    /**
     * 处理自由协同
     * @return 是否处理成功
     * @throws com.seeyon.ctp.common.exceptions.BusinessException 相关异常信息
     */
    @POST
    @Path("finishaffair")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @RestInterfaceAnnotation
    public Response finishaffair(Map<String, String> param) throws BusinessException {
        OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");

        User user = new User();
        V3xOrgMember sender = orgManager.getMemberById(Long.valueOf(param.get("memberid").toString()));
        user.setId(sender.getId());
        user.setLoginName(sender.getLoginName());
        user.setName(sender.getName());
        user.setAccountId(sender.getOrgAccountId());
        user.setBrowser(BrowserEnum.IE);
        user.setLoginAccount(sender.getOrgAccountId());
        AppContext.putThreadContext(GlobalNames.SESSION_CONTEXT_USERINFO_KEY,user);
        getDeskCollaborationProcessManager().finishWorkitemQuick(param);
        return ok(true);
    }
    
    @GET
    @Path("{affairId}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @RestInterfaceAnnotation
    public Response getAffairById(@PathParam("affairId")Long affairId){
        CtpAffair affair = new CtpAffair();
        try {
            affair = getAffairManager().get(affairId);
            return ok(affair);
        } catch (BusinessException e) {
            log.error("获取事项发生异常！", e);
        }
        return ok(affair);
    }
    
    @POST
    @Path("pending/count")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @RestInterfaceAnnotation
    public Response countPendingAffairs(Map<String, Object> params) {

        String memberId = ParamUtil.getString(params, "memberId");
        String appKeys = ParamUtil.getString(params, "appKeys");
        String thirdAppCodes = ParamUtil.getString(params, "thirdAppCodes");
        //未传递使用默认值
        if (Strings.isEmpty(appKeys)) {
            appKeys = "1,4,6";
        }
        //默认支持的appkey
        Set<String> defAppKeySet=new HashSet<String>();
        defAppKeySet.add("1");
        defAppKeySet.add("4");
        defAppKeySet.add("6");
        //循环去重和剔除没有待办数目的应用
        String[] tAppKeyList = appKeys.split(",");
        Set<String>  appKeySet=new HashSet<String>();
        for(String tItem:tAppKeyList){
            //支持的应用ID && 查询列表中没有
            if(defAppKeySet.contains(tItem) && !appKeySet.contains(tItem)){
                appKeySet.add(tItem);
            }
        }

        String[] thirdAppCodeList = null;
        if(null!=thirdAppCodes){
        	thirdAppCodeList=thirdAppCodes.split(",");
        }
        Map<String, Integer> map;
        try {
        	Long memberIdL;
        	if (Strings.isBlank(memberId)) {
        		memberIdL = AppContext.getCurrentUser().getId();
        	} else {
        		memberIdL = Long.parseLong(memberId);
        	}
        	if(thirdAppCodeList!=null){
                List<String> registerCodeList = new ArrayList<String>();
                for(String code:thirdAppCodeList){
                    registerCodeList.add(code);
                }
                map = getCipPendingApi().getPendingCount(registerCodeList, memberIdL);
            }else{
                map = new HashMap<String, Integer>();
            }
            map.putAll(getAffairManager().countPendingAffairs(memberIdL, appKeySet.toArray(new String[appKeySet.size()])));
        } catch (Exception e) {
            log.error("获取用户当前待办的数量出错！memberId:" + memberId + ",appKeyList:" + appKeys, e);
            return ok("false");
        }
        return ok(map);
    }
    
    
    @POST
    @Path("form/finish")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response dealweithForm(Map<String, String> param) throws Exception{
        String userId = param.get("userId");
        String subObjectId = param.get("subObjectId");
        String activityId = param.get("activityId");
        String objectId = param.get("objectId");
        String affairId = param.get("affairId");
        //处理状态
        Integer state = Integer.valueOf(param.get("attitude"));
        //处理意见
        String content = param.get("content");
        
        V3xOrgMember member = getOrgManager().getMemberById(Long.valueOf(userId));
        User user = new User();
        user.setId(member.getId());
        user.setName(member.getName());
        user.setLoginName(member.getLoginName());
        user.setAccountId(member.getOrgAccountId());
        user.setLoginAccount(member.getOrgAccountId());
        AppContext.putThreadContext(GlobalNames.SESSION_CONTEXT_USERINFO_KEY,user);
        String accountId = String.valueOf(member.getOrgAccountId());
        ColSummary colSummary= getColManager().getColSummaryById(Long.valueOf(objectId));
        WorkflowBpmContext wfContext = new WorkflowBpmContext();
        wfContext.setCurrentUserId(userId);
        wfContext.setCurrentAccountId(accountId);
        wfContext.setCurrentWorkitemId(Long.valueOf(subObjectId));
        wfContext.setBusinessData(EventDataContext.CTP_FORM_DATA, new HashMap());
        wfContext.setCurrentActivityId(activityId);
        //wfContext.setSysAutoFinishFlag(false);
        wfContext.setBusinessData("operationType",WorkFlowEventListener.COMMONDISPOSAL);
        wfContext.setBusinessData(EventDataContext.CTP_AFFAIR_OPERATION_TYPE,WorkFlowEventListener.COMMONDISPOSAL);
        wfContext.setAppName("collaboration");
        wfContext.setAppObject(colSummary);
        //传递过来的值
        wfContext.setConditionsOfNodes(param.get("conditionsOfNodes"));
        ColSummary cs= colSummary;
        String formAppId= cs.getFormAppid()==null?null:cs.getFormAppid().toString();
        String masterId= cs.getFormRecordid()==null?null:cs.getFormRecordid().toString();
        wfContext.setFormData(formAppId);
        wfContext.setMastrid(masterId);
        wfContext.setVersion("2.0");
        
        Comment comment = new Comment();
        comment.setHidden(false);
        comment.setId(UUIDLong.longUUID());
        
        switch (state){
            case 0:
                comment.setExtAtt1("collaboration.dealAttitude.haveRead");
                comment.setExtAtt4("haveRead");
                break;
            case 1:
                comment.setExtAtt1("collaboration.dealAttitude.agree");
                comment.setExtAtt4("agree");
                break;
            case 2:
                comment.setExtAtt1("collaboration.dealAttitude.disagree");
                comment.setExtAtt4("disagree");
                break;
            default:
                break;
        }
        comment.setPid(0L);
        comment.setPraiseNumber(0);
        comment.setPraiseToComment(false);
        comment.setPraiseToComment(false);
        comment.setModuleType(ModuleType.collaboration.getKey());
        comment.setModuleId(colSummary.getId());
        comment.setCreateDate(new Timestamp(System.currentTimeMillis()));
        comment.setContent(content);
        
        
        AffairData affairData = ColUtil.getAffairData(colSummary);
        CtpAffair affair = getAffairManager().get(Long.valueOf(affairId));
        
        if (affair != null) {
            comment.setAffairId(affair.getId());
            comment.setCreateId(affair.getMemberId());
            wfContext.setBusinessData(WorkFlowEventListener.CURRENT_OPERATE_MEMBER_ID, affair.getMemberId());
            wfContext.setBussinessId(String.valueOf(affair.getObjectId()));
        }
        
        getCtpCommentManager().insertComment(comment);
        wfContext.setBusinessData("comment", comment);
        wfContext.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, affairData);
        // TODO 解耦
//        wfContext.setBusinessData(WorkFlowEventListener.CtpAffairConstant, affair);
        //wfContext.setBusinessData(WorkFlowEventListener.CURRENT_OPERATE_COMMENT_ID, needCloneBaseCommentId);
        getWapi().transFinishWorkItem(wfContext);
        
        return ok(true);
    }
    
    
    @GET
    @Path("form/canEdit")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response canDealWithForm(@QueryParam("formOperationId")Long formOperationId, @QueryParam("formAppId")Long formAppId, @QueryParam("userId") String userId,
            @QueryParam("objectId")String objectId, @QueryParam("subObjectId")String subObjectId, @QueryParam("activityId")String activityId) throws NumberFormatException, BusinessException{
        
        FormBean fb = getFormApi4Cap3().getForm(formAppId);
        FormAuthViewBean fvb = fb.getAuthViewBeanById(formOperationId);
        //判断是否有编辑字段
        //boolean canEdit = fvb.hasCanEditField();
        //判断是否有必填项
        boolean hasNotNullable = fvb.hasNotNullableField();
        if(hasNotNullable){
            return ok("false");
        }
        
        //判断是否是流程中最后一个人
        ColSummary colSummary= getColManager().getColSummaryById(Long.valueOf(objectId));
        boolean isExecuteFinished= getWapi().isExecuteFinished(colSummary.getProcessId(), Long.valueOf(subObjectId));
        if(isExecuteFinished){
            //判断子流程是否已结束
            boolean isPreNewFlowFinish =  getWapi().hasUnFinishedNewflow(colSummary.getProcessId(), activityId);
            if(isPreNewFlowFinish){
                return ok("false");
            }
            
            //当前节点是否有子流程
            BPMProcess bpmProcess= null;
            BPMActivity bpmActivity= null;
            BPMSeeyonPolicy bpmSeeyonPolicy= null;
            bpmProcess= getWapi().getBPMProcess(colSummary.getProcessId());
            if(bpmProcess != null){
                bpmActivity= bpmProcess.getActivityById(activityId);
            }
            if(bpmActivity != null){
                bpmSeeyonPolicy= bpmActivity.getSeeyonPolicy();
            }
            boolean hasNewflow = bpmSeeyonPolicy != null && "1".equals(bpmSeeyonPolicy.getNF());
            if(hasNewflow){
                return ok("false");
            }
            
            //当前跳过的节点后面是否有分支或者需要选人或者人员不可用
            
            String masterId= colSummary.getFormRecordid()==null?null:colSummary.getFormRecordid().toString();
            WorkflowBpmContext wfContext = new WorkflowBpmContext();
            wfContext.setProcessId(colSummary.getProcessId());
            wfContext.setCaseId(colSummary.getCaseId());
            wfContext.setCurrentActivityId(String.valueOf(activityId));
            wfContext.setCurrentWorkitemId(Long.valueOf(subObjectId));
            wfContext.setFormData(String.valueOf(formAppId));
            wfContext.setMastrid(masterId);
            wfContext.setAppName("collaboration");
            wfContext.setStartUserId(String.valueOf(colSummary.getStartMemberId()));
            wfContext.setCurrentUserId(userId);
            wfContext.setBussinessId(String.valueOf(objectId));
            wfContext.setSysAutoFinishFlag(true);
            PopResult pr = getWapi().isPop(wfContext);
            if("false".equals(pr.getPopResult())){
                //可以提交并把result[1]传递过去
                return ok(pr.getConditionsOfNodes());
            } else {
                return ok("false");
            }
        }
        return ok("true");
    }
    
	/**
	 * 终止协同
	 * @param params Map<String, String> | 必填  | 其他参数
     * <pre>
     * affairId     String  |  必填       |  流程ID
     * repealComment      String  |  必填       |  终止回复评论
     * member       String  |  否            |  当前处理人登录名
     * </pre>
	 * @return Map<String, String>
	 * @throws BusinessException
	 */
	@POST
	@Path("stop")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@RestInterfaceAnnotation
	public Response ctptransStepStop(Map<String, String> param) {
		try {
			String affairId = param.get("affairId");
			String repealComment = param.get("repealComment"); 
			String member = param.get("member");
			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("affairId", affairId);
			tempMap.put("repealComment", repealComment);
			tempMap.put("member", member);
			putUser(member);
			getColManager().transStepStop(tempMap);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			log.error(e.getMessage(), e);
			return ok(false);
		}
		return ok(true);
	}

	/**
	 * 撤销协同
	 * @param params Map<String, String> | 必填  | 其他参数
     * <pre>
     * affairId     String  |  必填       |  事项ID
     * summaryId      String  |  必填       |  协同主表id
     * member       String  |  否            |  当前处理人登录名
     * </pre>
	 * @return Map<String, String>
	 * @throws BusinessException
	 */
	@POST
	@Path("cancel")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@RestInterfaceAnnotation
	public Response ctptransRepalBackground(Map<String, String> param){
		try { 
			Long _summaryId=Long.parseLong(param.get("summaryId"));
			Long _affairId=Long.parseLong(param.get("affairId"));
			String member=param.get("member");
			String repealComment="";
			String trackWorkflowType="";
			boolean isBackOperation=false;
			putUser(member);
			getColManager().transRepalBackground(_summaryId, _affairId, repealComment, trackWorkflowType, 10,Boolean.TRUE,null,new HashMap<String,Object>());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			log.error(e.getMessage(), e);
			return ok(false);
		}
		return ok(true);
	}
	
	/**
	 * 获取待办更多数据(数据来源受栏目配置控制)
	 * 
	 * @return 
	 * @throws 
	 */
	@POST
	@Path("getPendingMore")
	public Response getPendingMore(Map<String, String> param) throws BusinessException {
		FlipInfo flipInfo = super.getFlipInfo();
		flipInfo.setPage(Integer.valueOf(param.get("pageNo")));
		flipInfo.setSize(Integer.valueOf(param.get("pageSize")));
		
		String entityId = param.get("entityId");
        String ordinal = param.get("ordinal");
        String propertyFrom=param.get("propertyFrom");
        String propertyId=param.get("propertyId");
        //if (Strings.isBlank(entityId) || Strings.isBlank(ordinal)) {
        //	Map<String,String> retMap = new HashMap<String, String>();
    	//	retMap.put("errorMsg", ResourceUtil.getString("commom.error.errorParams"));
        //	return ok(retMap);
        //}
        
	    Map<String,Object> query = new HashMap<String,Object>();
	    query.put("state", StateEnum.col_pending.key());
	    query.put("isTrack", false);
	    query.put("fragmentId", entityId);
	    query.put("ordinal", ordinal);
	    query.put("proertyFrom",propertyFrom);
	    query.put("propertyId",propertyId);
	    query.put("rowStr", "subject,receiveTime,sendUser,category,processingProgress");
	    query.put("from", "Vjoin");
	    
	    //查询条件
	    String condition = param.get("condition");
	    String conditionValue = param.get("conditionValue");
	    
	    String textField1 = "";
	    String textField2 = "";
	    if (Strings.isNotBlank(condition) && Strings.isNotBlank(conditionValue)) {
	    	if (AffairCondition.SearchCondition.createDate.name().equals(condition)) {
	    		String[] values = conditionValue.split("#");
	    		textField1 = values[0];
	    		textField2 = values[1];
	    	} else {
	    		textField1 = conditionValue;
	    	}
	    	query.put("condition", condition);
	    	query.put("textfield", textField1);
	    	query.put("textfield1", textField2);
	    }
	    
		getPendingManager().getMoreList4SectionContion(flipInfo, query);
		
		this.convertListSimpleVO2Affairs(flipInfo);
		
		return ok(flipInfo);
	}	
	/**
	 * 获取当前用户是否有新建协同等权限
	 * 
	 * @return 
	 * @throws 
	 */
	@POST
	@Path("userPeivMenu")
	public Response userPeivMenu() throws BusinessException {
		Map<String,Object> params = new HashMap<String,Object>();
    	User user = AppContext.getCurrentUser();
		
    	boolean isHaveNewColl = MenuPurviewUtil.isHaveNewColl(user);
    	params.put("isHaveNewColl", isHaveNewColl);
    	
    	return ok(params);
    	
	}	
	
	private void convertListSimpleVO2Affairs(FlipInfo info) throws BusinessException {
		if (info != null) {
            List<PendingRow> list = info.getData();
            if (Strings.isNotEmpty(list)) {
                List<PendingRow> affairs = new ArrayList<PendingRow>();
                for (PendingRow c : list) {
                    try {
						c.setReceiveTime(CollaborationUtils.showDate(DateUtil.parse(c.getReceiveTimeAll())));
					} catch (ParseException e) {
						log.error("", e);
					}
                    Map<String, String> extParam = getPendingManager().getPendingDetailContent(c);
                    c.setExtParam(extParam);
                    affairs.add(c);
                }
                info.setData(affairs);
            }
        }
	}
	

	private void putUser(String member) throws BusinessException {
		V3xOrgMember HandleMember=getOrgManager().getMemberByLoginName(member);
		User user= new User();
		user.setId(HandleMember.getId());
		user.setDepartmentId(HandleMember.getOrgDepartmentId());
		user.setLoginAccount(HandleMember.getOrgAccountId());
		user.setLoginName(HandleMember.getLoginName());
		user.setName(HandleMember.getName());

		AppContext.putThreadContext(GlobalNames.SESSION_CONTEXT_USERINFO_KEY, user);
	}
    
    
    private DeskCollaborationProcessManager getDeskCollaborationProcessManager(){
        if(deskCollaborationProcessManager == null){
            deskCollaborationProcessManager = (DeskCollaborationProcessManager ) AppContext.getBean("deskCollaborationProcessManagerImpl");
        }
        return deskCollaborationProcessManager;
    }
    
    private AffairManager getAffairManager(){
        if(affairManager == null){
            affairManager = (AffairManager) AppContext.getBean("affairManager");
        }
        return affairManager;
    }
    
    public FormApi4Cap3 getFormApi4Cap3() {
        if(formApi4Cap3 == null){
            formApi4Cap3 = (FormApi4Cap3) AppContext.getBean("formApi4Cap3");
        }
        return formApi4Cap3;
    }

    public WorkflowApiManager getWapi() {
        if(wapi == null){
            wapi = (WorkflowApiManager) AppContext.getBean("wapi");
        }
        return wapi;
    }

    public ColManager getColManager() {
        if(colManager == null){
            colManager = (ColManager) AppContext.getBean("colManager");
        }
        return colManager;
    }

    public CommentManager getCtpCommentManager() {
        if(ctpCommentManager == null){
            ctpCommentManager = (CommentManager) AppContext.getBean("ctpCommentManager");
        }
        return ctpCommentManager;
    }

    public OrgManager getOrgManager() {
        if(orgManager == null){
            orgManager = (OrgManager) AppContext.getBean("orgManager");
        }
        return orgManager;
    }
    
    public PendingManager getPendingManager() {
    	if(pendingManager == null){
    		pendingManager = (PendingManager) AppContext.getBean("pendingManager");
    	}
    	return pendingManager;
    }

	public CipPendingApi getCipPendingApi() {
		if(cipPendingApi == null){
			cipPendingApi = (CipPendingApi) AppContext.getBean("cipPendingApi");
    	}
		return cipPendingApi;
	}

}
