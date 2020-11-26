package com.seeyon.apps.collaboration.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;

import com.seeyon.apps.collaboration.bo.BackgroundDealParamBO;
import com.seeyon.apps.collaboration.bo.BackgroundDealResult;
import com.seeyon.apps.collaboration.bo.DeleteAjaxTranObj;
import com.seeyon.apps.collaboration.bo.QuerySummaryParam;
import com.seeyon.apps.collaboration.constants.ColConstant.SendType;
import com.seeyon.apps.collaboration.dao.ColDao;
import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.apps.collaboration.enums.ColListType;
import com.seeyon.apps.collaboration.enums.ColQueryCondition;
import com.seeyon.apps.collaboration.manager.Col4WFAnalysisManager;
import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.collaboration.manager.ColPubManager;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.quartz.CollProcessBackgroundManager;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.collaboration.vo.ColQuoteVO;
import com.seeyon.apps.collaboration.vo.ColSummaryVO;
import com.seeyon.apps.collaboration.vo.NodePolicyVO;
import com.seeyon.apps.collaboration.vo.SeeyonPolicy;
import com.seeyon.apps.common.isignaturehtml.manager.ISignatureHtmlManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.comment.Comment.CommentType;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.permission.bo.NodePolicy;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.isignaturehtml.CtpIsignatureHtml;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.po.template.CtpTemplateHistory;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.template.utils.TemplateApproveUtil;
import com.seeyon.ctp.handover.api.HandoverManager;
import com.seeyon.ctp.handover.constants.HandoverConstant;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.enums.LockAction;
import com.seeyon.ctp.workflow.wapi.WorkFlowAppExtendAbstractManager;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;

import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;

public class CollaborationApiImpl extends AbstractCollaborationApi implements CollaborationApi{
    private static final Log LOG = CtpLogFactory.getLog(CollaborationApiImpl.class);
    private ColManager    colManager;
    private ColPubManager colPubManager;
    private ColDao        colDao;
    private AffairManager affairManager = null;
    private OrgManager orgManager = null;
    private WorkflowApiManager wapi;
    private WorkFlowAppExtendAbstractManager workFlowAppExtendManager;
    private CollProcessBackgroundManager collProcessBackgroundManager;
    private ISignatureHtmlManager iSignatureHtmlManager;
    private Col4WFAnalysisManager col4WFAnalysisManager;
    private TemplateManager templateManager;
    private PermissionManager permissionManager;
    private HandoverManager handoverManager;
    
    
    
	public void setHandoverManager(HandoverManager handoverManager) {
		this.handoverManager = handoverManager;
	}

	public ISignatureHtmlManager getiSignatureHtmlManager() {
		return iSignatureHtmlManager;
	}

	public void setiSignatureHtmlManager(ISignatureHtmlManager iSignatureHtmlManager) {
		this.iSignatureHtmlManager = iSignatureHtmlManager;
	}
    
	public void setCol4WFAnalysisManager(Col4WFAnalysisManager col4wfAnalysisManager) {
        col4WFAnalysisManager = col4wfAnalysisManager;
    }
    
	public CollProcessBackgroundManager getCollProcessBackgroundManager() {
        return collProcessBackgroundManager;
    }

    public void setCollProcessBackgroundManager(CollProcessBackgroundManager collProcessBackgroundManager) {
        this.collProcessBackgroundManager = collProcessBackgroundManager;
    }

    public void setWorkFlowAppExtendManager(WorkFlowAppExtendAbstractManager workFlowAppExtendManager) {
		this.workFlowAppExtendManager = workFlowAppExtendManager;
	}

	public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }
    
	public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }
    
	public void setColDao(ColDao colDao) {
		this.colDao = colDao;
	}

	public ColPubManager getColPubManager() {
		return colPubManager;
	}

	public void setColPubManager(ColPubManager colPubManager) {
		this.colPubManager = colPubManager;
	}

	public ColManager getColManager() {
		return colManager;
	}

	public ColDao getColDao() {
		return colDao;
	}

	public void setColManager(ColManager colManager) {
		this.colManager = colManager;
	}
	
	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}
	public PermissionManager getPermissionManager() {
		return permissionManager;
	}
	
	@Override
	public ColSummary getColSummary(Long id) throws BusinessException {
		return colManager.getColSummaryById(id);
	}
	
	@Override
	public List<ColSummary> findColSummarys(List<Long> ids) throws BusinessException {

	    List<ColSummary> list = new ArrayList<ColSummary>();
	    
	    if(Strings.isNotEmpty(ids)){
	       List<Long>[] ida =  Strings.splitList(ids, 1000);
	       for(List<Long> id : ida){
	           list.addAll((List<ColSummary>)colManager.findColSummarysByIds(id));
	       }
	    }
	    return list;
	}

	
	@Override
	public ColSummary getColSummaryByFormRecordId(Long formRecordId) throws BusinessException {
		ColSummary summary = colManager.getColSummaryByFormRecordId(formRecordId);
		return summary;
	}
	
	@Override
	public Map<Long, Long> getColSummaryIdByFormRecordIds(List<Long> formRecords) throws BusinessException {
	    
	    return colManager.getColSummaryIdByFormRecordIds(formRecords);
	}

	
	@Override
	public FlipInfo findAffairs4Project(FlipInfo flipInfo, Map<String, String> queryMap) throws BusinessException {
		//设置用户id
		String userId = queryMap.get(ColQueryCondition.currentUser.name());
		if (Strings.isBlank(userId)) {
			User user = AppContext.getCurrentUser();
			userId = String.valueOf(user.getId());
		}
		queryMap.put(ColQueryCondition.currentUser.name(),userId);
		FlipInfo f = colDao.getColSummaryByCondition(flipInfo, queryMap);
		return f;
	}

	@Override
	public List<ColSummaryVO> getTrackList4BizConfig(Long memberId, List<Long> tempIds) throws BusinessException {
		if (memberId == null || tempIds == null) {
			throw new BusinessException("memberId,tempIds " + ResourceUtil.getString("collaboration.error.common.empty"));//不能为空！
		}
		return colManager.getTrackList4BizConfig(memberId, tempIds);
	}
	
	@Override
	public FlipInfo findDoneAffairs(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
		return colManager.getDoneList(flipInfo, params);
	}
	@Override
	public FlipInfo findSentAffairs(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
		return colManager.getSentList(flipInfo, params);
	}
	@Override
	public FlipInfo findPendingAffairs(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
		return colManager.getPendingList(flipInfo, params);
	}
	@Override
	public FlipInfo findWaitSentAffairs(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
		return colManager.getWaitSendList(flipInfo, params);
	}
	
	@Override
	public int getColCount(Long memberId, StateEnum state, List<Long> templeteIds) throws BusinessException {
		if (memberId == null || templeteIds == null) {
			throw new BusinessException("memberId,templeteIds "+ ResourceUtil.getString("collaboration.error.common.empty"));//不能为null！
		}
		int count = colManager.getColCount(memberId,state.key(), templeteIds);
		return count;
	}

	@Override
	public List<ColSummaryVO> findMyPendingColByExpectedProcessTime(Map<String, Object> tempMap)
			throws BusinessException {
		return colManager.getMyCollDeadlineNotEmpty(tempMap);
	}
	@Override
	public ColSummary getColSummaryByProcessId(String processId) throws BusinessException {
		return colManager.getColSummaryByProcessId(Long.valueOf(processId));
	}
	@Override
	public Integer getColSummaryCount(Date beginDate, Date endDate, boolean isForm) throws BusinessException {
		if (beginDate == null || endDate == null) {
			throw new BusinessException("beginDate,endDate "+ ResourceUtil.getString("collaboration.error.common.empty"));//不能为空！
		}
		return colManager.getColSummaryCount(beginDate, endDate, isForm);
	}

	@Override
	public List<Long> findColSummaryIdList(Date starDate, Date endDate, Integer firstRow, Integer pageSize,boolean isForm) throws BusinessException {
		if (starDate == null || endDate == null) {
			throw new BusinessException("starDate,endDate "+ ResourceUtil.getString("collaboration.error.common.empty"));//不能为空！
		}
		return colManager.findIndexResumeIDList(starDate, endDate, firstRow, pageSize,isForm);
	}


	
	@Override
	public void finishWorkItem(CtpAffair affair, ColSummary summary, Comment comment, Map<String, Object> params) throws BusinessException {
		try{
		    
            if(!Integer.valueOf(StateEnum.col_pending.key()).equals(affair.getState())){
                LOG.info(AppContext.currentUserName()+",事项不是待办事项，不能处理。affairId:"+affair.getId()+",summary.id:"+summary.getId());
                return;
            }
			colManager.transFinishWorkItemPublic(affair, summary, comment, ColHandleType.finish, params);
		}finally{
			colManager.colDelLock(summary, affair);
		}
	}

	@Override
    public void doZCDB(CtpAffair affair, ColSummary summary, Comment comment, Map<String, Object> params) throws BusinessException {
	    colManager.transDoZcdbPublic(affair, summary, comment, ColHandleType.wait, params);
    }
	
	@Override
	public void transSendColl(SendType sendType, Long templateId, Long senderId, Long formMasterId,
			Long parentSummaryId, Long newSumamryId) throws BusinessException {
		colPubManager.transSendColl(sendType, templateId, senderId, formMasterId, parentSummaryId, newSumamryId);
	}

    @Override
    public void addSenderComment(Long userId, Long summaryId, String content, String attrs, boolean toSendMsg)
            throws BusinessException {
        
    	if(summaryId != null) {
	        CtpAffair sendAffair = affairManager.getSenderAffair(summaryId);
	        if(sendAffair != null && !sendAffair.getSenderId().equals(userId)){
	            throw new BusinessException(ResourceUtil.getString("collaboration.error.common.unauthorized"));//越权操作
	        } else {
		        Comment c = new Comment();
		        c.setCreateId(userId);
		        c.setCreateDate(new Date());
		        c.setContent(content);
		        c.setPushMessage(toSendMsg);
		        c.setPid(0l);
		        c.setClevel(1);//这个参数没有什么用
		        c.setPath("00");//这个参数没有什么用
		        c.setModuleType(ApplicationCategoryEnum.collaboration.getKey());
		        c.setCtype(Comment.CommentType.sender.getKey());
		        c.setModuleId(summaryId);
		        c.setAffairId(sendAffair == null?null:sendAffair.getId());
		        c.setTitle(sendAffair == null?"":sendAffair.getSubject());
		        /*
		        String relateInfo = "[]";
		        if(Strings.isNotEmpty(attrs)){
		            relateInfo = JSONUtil.toJSONString(attrs);
		        }
		        c.setRelateInfo(relateInfo);
		        */
		        c.setRelateInfo(attrs);
		        colManager.insertComment(c, "");
	        }
    	} else {
    		throw new BusinessException("summmaryId "+ ResourceUtil.getString("collaboration.error.common.empty"));//传递的summmaryId为null
    	}
    }

    @Override
    public void replyComment(Long userId, Long commentId, Comment comment)
            throws BusinessException {
    	this.replyComment1(userId, commentId, comment);
    }
    
    @Override
    public Comment replyComment1(Long userId, Long commentId, Comment comment)
            throws BusinessException {

        if(comment == null){
            throw new BusinessException("comment " + ResourceUtil.getString("collaboration.error.common.empty"));//参数传入
        }
        comment.setCtype(CommentType.reply.getKey());
        comment.setCreateDate(new Date());
        comment.setCreateId(userId);
        comment.setPid(commentId);
        
        return colManager.insertComment(comment, "");
    }

	@Override
	public void deleteAffair(String pageType, long affairId) throws BusinessException {
		colManager.deleteAffair(pageType, affairId);
	}
	
	/**
     *  取回已办事项
     * @param affairId
     * @param isSaveOpinion 是否对愿意见修改
     * @throws BusinessException
     */
    public String takeBack(long affairId,boolean isSaveOpinion) throws BusinessException {
    	Map<String,Object> params = new HashMap<String,Object>();
    	params.put("affairId", affairId);
    	params.put("isSaveOpinion", isSaveOpinion);
    	String message = colManager.transTakeBack(params);
    	return message;
    }

	@SuppressWarnings("unchecked")
	@Override
	public FlipInfo findColQuote(FlipInfo flipInfo, Map<String, String> query) throws BusinessException {
		FlipInfo fi = colManager.getSentlist4Quote(flipInfo, query);
		if (fi.getData() != null) {
			List<ColSummaryVO> result = fi.getData();
			List<ColQuoteVO> colQuoteList = new ArrayList<ColQuoteVO>();
			for (ColSummaryVO colSummary : result) {
				colQuoteList.add(new ColQuoteVO(colSummary));
			}
			fi.setData(colQuoteList);
		}
		return fi;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FlipInfo findHandoverQuote(FlipInfo flipInfo, Map<String, String> query) throws BusinessException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("sendee", String.valueOf(AppContext.currentUserId()));
		params.put("type", HandoverConstant.Type.toMe.name());
		params.put("state", StateEnum.col_sent.key()+","+StateEnum.col_done.key());
		params.put("subject", query.get("subject"));
		params.put("memberName", query.get("memberName"));
		FlipInfo fi = handoverManager.listAffairs(flipInfo, params);
		if(Strings.isNotEmpty(fi.getData())){
			List<Map<String, Object>> affairs = fi.getData();
			List<ColQuoteVO> colQuoteList = new ArrayList<ColQuoteVO>();
			for(Map<String, Object> map : affairs){
				colQuoteList.add(new ColQuoteVO(map));
			}
			fi.setData(colQuoteList);
		}
		return fi;
	}

	@Override
	public Map<String, Integer> getColAffairsCount(Map<String, String> query) throws BusinessException {
		Map<String,Integer> mapTemp = new HashMap<String,Integer>();
		query.put(ColQueryCondition.state.name(), "2,3,4");
		Map<String, Integer> map = colDao.getColQuoteCount(query);
		int sentCount = 0;
		int pendingCount = 0;
		int doneCount = 0;
		if (map != null && map.get("2") != null ) {
			sentCount = map.get("2");
		}
		if (map != null && map.get("3") != null ) {
			pendingCount = map.get("3");
		}
		if (map != null && map.get("4") != null ) {
			doneCount = map.get("4");
		}
		mapTemp.put("listSent", sentCount);
		mapTemp.put("listPending", pendingCount);
		mapTemp.put("listDone", doneCount);
		return mapTemp;
	}
	
	/**
	 * 根据已发、已办、待办查询各事项的总和
	 * 正常:<br>
     *     1、传入已发事项的key，返回已发列表<br>
     *     2、传入待办事项的key，返回待办列表<br>
     *     3、传入已办事项的key，返回已发列表<br>
     * @param query 查询参数 
     *        key - {@link com.seeyon.apps.collaboration.enums.ColQueryCondition}.name() value - string<br>
     *  必传参数<br>
     *  Key(String 列名) - state  Value(Long) - 查询事项状态（StateEnum:col_pending,col_send,col_done的key） <br>   
	 * @return
	 * @throws BusinessException
	 */
	public int getColAffairsCountByCondition(Map<String, String> query) throws BusinessException {
		int count = colManager.countByCondition(query);
		return count;
	}
	
	@Override
	public void transDoForward(User user,Map<String,String> params) throws BusinessException {
		String summaryIds = params.get("summaryId");
		String affairIds = params.get("affairId");
		if(Strings.isNotBlank(summaryIds) && Strings.isNotBlank(affairIds)){
			String[] summaryIdsArr = summaryIds.split("_");
			String[] affairIdsArr = affairIds.split("_");
			for(int i = 0;i<summaryIdsArr.length;i++){
				Long summaryId = Long.valueOf(summaryIdsArr[i]);
				Long affairId = Long.valueOf(affairIdsArr[i]);
				colManager.transDoForward(user, summaryId, affairId, params);
			}
		}
	}

    @Override
    public  List<ColSummaryVO> findColSummarys(QuerySummaryParam param, FlipInfo flip) throws BusinessException {

        List<ColSummary> summarys = colManager.findColSummarys(param, flip);
        
        List<ColSummaryVO> vos = null;
        
        if(Strings.isNotEmpty(summarys)){
            vos = new ArrayList<ColSummaryVO>();
            for(ColSummary summary : summarys){
                
                ColSummaryVO v = new ColSummaryVO();
                
                if(summary != null){
                    v.setSubject(summary.getSubject());
                    V3xOrgMember member = orgManager.getMemberById(summary.getStartMemberId());
                    v.setStartMemberName(member.getName());
                    v.setStartDate(summary.getStartDate());
                    v.setFinishDate(summary.getFinishDate());
                    v.setRunWorktime(summary.getRunWorktime());
                    v.setIsCoverTime(summary.isCoverTime());
                    v.setOverWorkTime(summary.getOverWorktime());
                    vos.add(v);
                }
            }
            
        }else{
            vos = new ArrayList<ColSummaryVO>(0);
        }
        
        if(flip != null){
            flip.setData(vos);
        }
        
        return vos;
    }

	@Override
	public Map<String, Object> transRepeal(Long affairId, Map<String, String> params) throws BusinessException {
		Map<String,Object> ret = null;
		User user = AppContext.getCurrentUser();
//		ret.put("isSucess", true);
		if (affairId == null || params == null) {
			throw new BusinessException("affairId,params "+ ResourceUtil.getString("collaboration.error.common.empty"));//不能为空！
		}
		//撤销附言
		String content = ParamUtil.getString(params, "content");
		if(Strings.isBlank(content)){
			throw new BusinessException("params.content"+ ResourceUtil.getString("collaboration.error.common.empty"));//不能为空！
		}
		//处理是否追溯流程
        String isWFTrace = ParamUtil.getString(params, "isWFTrace", "0");
        CtpAffair affair = null;
        ColSummary summary = null;
        boolean canDeal = true;
        try {
            //获取对象
            affair = affairManager.get(affairId);
            summary = getColSummary(affair.getObjectId());
            //校验流程是否可以撤销
            ret = transCheckCanRepeal(affairId);
            canDeal = (Boolean) ret.get("canRepeal");
            //流程锁校验
            if(canDeal){
            	String[] wfLockCheck = wapi.checkWorkflowLock(summary.getProcessId(), user.getId().toString(), 12);
                if(wfLockCheck == null){
                    canDeal = false;
                    ret.put("errorMsg",ResourceUtil.getString("workflow.wapi.exception.msg002"));
                }else if("false".equals(wfLockCheck[0])){
                    canDeal = false;
                    ret.put("errorMsg",wfLockCheck[1]);
                }
            }
            
            //执行工作流的事件
            if(canDeal){
            	WorkflowBpmContext context = new WorkflowBpmContext();
            	context.setFormData(null!=affair.getFormRecordid()?affair.getFormRecordid().toString():"");
                context.setMastrid(null!=affair.getFormRecordid()?affair.getFormRecordid().toString():"");
                context.setProcessId(affair.getProcessId());
                context.setProcessTemplateId(affair.getProcessId());
                context.setBussinessId(affair.getObjectId().toString());
                context.setAffairId(affair.getId().toString());
                context.setAppName(ApplicationCategoryEnum.valueOf(affair.getApp()).name());
                context.setFormAppId(null!=affair.getFormAppId()?affair.getFormAppId().toString():"");
                context.setFormViewOperation(affair.getMultiViewStr());
                context.setMatchRequestToken(UUID.randomUUID()+"");
                context.setProcessXml("");
            	String executeResult =  wapi.executeWorkflowBeforeEvent("BeforeCancel", context);
            	if(Strings.isNotBlank(executeResult)){
            		 canDeal = false;
                     ret.put("errorMsg",executeResult);
            	}
            }
            //执行撤销操作
            if (canDeal) {
            	Map<String,Object> tempMap=new HashMap<String, Object>();
                tempMap.put("affairId", affair.getId().toString());
                tempMap.put("summaryId", summary.getId().toString());
                tempMap.put("repealComment", content);
                //流程会用到这个参数
                tempMap.put("isWFTrace", isWFTrace);
                String repalRet = colManager.transRepal(tempMap);
                if(Strings.isNotBlank(repalRet)){
                    ret.put("errorMsg", repalRet);
                }
            }
        } finally {
            if( canDeal ){
            	colManager.unlockCollAll(affairId,affair,summary);
            }
        }
        ret.put("isSucess", canDeal);
		return ret;
	}
	
	/**
	 * 校验流程是否可以撤销
	 * @param affairId
	 * @return
	 * @throws BusinessException
	 */
	private Map<String, Object> transCheckCanRepeal(Long affairId) throws BusinessException {
		Map<String,Object> ret = new HashMap<String,Object>();
		User user = AppContext.getCurrentUser();
		ret.put("canRepeal", true);
		if (affairId == null) {
			throw new BusinessException("affairId "+ ResourceUtil.getString("collaboration.error.common.empty"));//不能为空！
		}
        CtpAffair affair = null;
        ColSummary summary = null;
        boolean canDeal = true;
        //获取对象
        affair = affairManager.get(affairId);
        summary = getColSummary(affair.getObjectId());
        
        Map<String, String> checkMap = new HashMap<String, String>();
        checkMap.put("summaryId", summary.getId().toString());
        Map<String, String> checkRetMap = colManager.checkIsCanRepeal(checkMap);
        if(checkRetMap != null && Strings.isNotBlank(checkRetMap.get("msg"))){
          canDeal = false;
          ret.put("errorMsg", checkRetMap.get("msg"));
        }
        //校验Affair状态
        if(canDeal){
        	String affairValid = colManager.checkAffairValid(affairId.toString(),affair.getNodePolicy());
            if(Strings.isNotBlank(affairValid)){
            	 canDeal = false;
                 ret.put("errorMsg", affairValid);
            }
        }
        //流程校验是否可以提交
        if(canDeal){
            String[] wfCheck = wapi.canRepeal(ApplicationCategoryEnum.collaboration.name(), 
                                 toString(summary.getProcessId()), toString(affair.getActivityId()));
            if("false".equals(wfCheck[0])){
                canDeal = false;
                ret.put("errorMsg", wfCheck[1]);
            }
        }
        ret.put("canRepeal", canDeal);
		return ret;
	}
	
	/*
     * 转化成String
     */
    private String toString(Object o){
        String ret = "";
        if(o != null){
            ret = o.toString();
        }
        return ret;
    }
    
    public String hasten(String processId, String activityId, List<Long> personIds, String superviseId, String content, boolean sendPhoneMessage)  throws BusinessException {
        //流程id, 节点id, 接受催办消息的人员, 督办记录id, 催办正文,是否发送消息
        return workFlowAppExtendManager.hasten(processId, activityId, personIds, superviseId, content, sendPhoneMessage);
    }

    @Override
    public List<String> checkForwardPermission(String affairIds) throws BusinessException {
        
       return colManager.checkForwardPermission(affairIds);
    }

	@Override
	public String transStepBack(Long affairId, Comment comment,boolean isWFTrace) throws BusinessException {
		if (affairId == null) {
			throw new BusinessException("affairId "+ ResourceUtil.getString("collaboration.error.common.empty"));//不能为空！
		}
		if(comment==null){
			throw new BusinessException("comment "+ ResourceUtil.getString("collaboration.error.common.empty"));//不能为空！
		}
        CtpAffair affair = affairManager.get(affairId);
        ColSummary summary = getColSummary(affair.getObjectId());
        String stepStopRet = "";
        try{ 
        	String[] wfCheck = wapi.canStepBack(toString(affair.getSubObjectId()), 
                    toString(summary.getCaseId()), toString(summary.getProcessId()), 
                                    toString(affair.getActivityId()), "", "");
            if("false".equals(wfCheck[0])){
                return  wfCheck[1];
            }
        	Map<String,Object> tempMap=new HashMap<String, Object>();
            tempMap.put("affairId", affair.getId().toString());
            tempMap.put("summaryId", affair.getObjectId().toString());
            tempMap.put("targetNodeId", "");
            tempMap.put("comment", comment);
            stepStopRet = colManager.transStepBack(tempMap);
    	}finally{
    		colManager.colDelLock(Long.valueOf(affairId));
    	}
        
		return stepStopRet;
	}

    @Override
    public void deleteCollDatasPhysical(Long formAppId) throws BusinessException {
        colManager.deleteCollDatasPhysically(formAppId);
    }

    @Override
    public BackgroundDealResult transfinishWorkItemInBackground(BackgroundDealParamBO bo) throws BusinessException {
        try {
           return  collProcessBackgroundManager.transFinishWorkItem(bo);
        } catch (Exception e) {
            
           LOG.error("", e);
           
           BackgroundDealResult dealResult = new BackgroundDealResult();
           dealResult.setCan(false);
           dealResult.setMsg(e.getMessage());
           return dealResult;
        }
    }

	@Override
	public boolean hasCtpIsignature(Long summaryId) throws BusinessException {
		List<CtpIsignatureHtml> isignatureList = iSignatureHtmlManager.getISignatureByDocumentId(summaryId);
		if(Strings.isEmpty(isignatureList)){
			return false;
		}else{
			return true;
		}
	}

    @Override
    public String showSubjectOfSummary(ColSummary summary, Boolean isProxy, int length, String proxyName) {
        return ColUtil.showSubjectOfSummary(summary, isProxy, length, proxyName);
    }

    @Override
    public AffairData getAffairData(ColSummary summary) throws BusinessException {
        return ColUtil.getAffairData(summary);
    }

    @Override
    public List<Object[]> transStatList(FlipInfo flipInfo, Map<String, Object> query) {
        return col4WFAnalysisManager.transStatList(flipInfo, query);
    }

    @Override
    public String executeWorkflowBeforeEvent(CtpAffair affair, String eventId, String currentNodeLast) {
        return ColUtil.executeWorkflowBeforeEvent(affair, eventId, currentNodeLast);
    }

    @Override
    public SeeyonPolicy getPolicyByAffair(CtpAffair affair) throws BusinessException {
        return ColUtil.getPolicyByAffair(affair);
    }

    @Override
    public Long getFlowPermAccountId(Long defaultAccountId, ColSummary summary) throws BusinessException {
        return ColUtil.getFlowPermAccountId(defaultAccountId, summary);
    }

    @Override
    public void deepClone(CtpTemplate template, CtpTemplateHistory history) {
        TemplateApproveUtil.deepClone(template, history);
    }

	public WorkflowApiManager getWapi() {
		return wapi;
	}

	public void setWapi(WorkflowApiManager wapi) {
		this.wapi = wapi;
	}

	@Override
	public void deleteAllCollDatasPhysicallyByFormAppId(Long formAppId) throws BusinessException {
		colManager.deleteAllCollDatasPhysically(formAppId,false);
	}

	@Override
	public void deleteAllCollDatasPhysicallyBySummaryId(Long summaryId) throws BusinessException{
		colManager.deleteAllCollDatasPhysically(summaryId,true);
	}

	@Override
	public FlipInfo getSummarysAndTemplates(FlipInfo flipInfo,Map<String, Object> map) throws BusinessException {
		return colManager.getSummarysAndTemplates(flipInfo,map);
	}
	@Override
	public FlipInfo getColSummaryByRelation(FlipInfo flipInfo,Map<String,Object> query) throws BusinessException{
		return colManager.getColSummaryByRelation(flipInfo,query);
	}
	public void batchDeleteAllCollDatasPhysically(Map<String,Object> map) throws BusinessException{
		colManager.batchDeleteAllCollDatasPhysically(map);
	}

	@Override
	public String checkCanStopFlow(CtpAffair affair) throws BusinessException {
		//检查代理，避免不是处理人也能处理了。
		String canDeal = "";
		try {
			canDeal = ColUtil.ajaxCheckAgent(affair.getMemberId(), affair.getSubject(), ModuleType.collaboration);
		} catch (Exception e1) {
			LOG.error("", e1);
		}
		return canDeal;
	}

    @Override
    public CtpTemplate getCtpTemplate(Long id) throws BusinessException {
        return templateManager.getCtpTemplate(id);
    }

	@Override
	public String deleteAffairs(List<Long> affairIds) throws BusinessException {
		if(Strings.isEmpty(affairIds)) {
			return ResourceUtil.getString("collaboration.grid.selectDelete");//请选择要删除的协同
		}
		//批量校验affair是否可以删除
		DeleteAjaxTranObj deleteObj = new DeleteAjaxTranObj();
		deleteObj.setAffairIds(Strings.join(affairIds, ","));
		String canDeleteMsg = colManager.checkCanDelete(deleteObj);
		if(Strings.isNotBlank(canDeleteMsg) && !"success".equals(canDeleteMsg)) {
			return canDeleteMsg; //不能删除的条件
		}
		
		List<CtpAffair> affairs = affairManager.get(affairIds);
		for (CtpAffair ctpAffair : affairs) {
			String pageType = "";
			//指定回退状态不能删除
			if(Integer.valueOf(SubStateEnum.col_pending_specialBacked.getKey()).equals(ctpAffair.getSubState())) {
				return ResourceUtil.getString("collaboration.alert.CantModifyBecauseOfAppointStepBack");
			}
			
			ColSummary summary = getColSummary(ctpAffair.getObjectId());
			if(summary==null) {
				throw new BusinessException("summary is null!");
			}
			
			long flowPermAccountId = ColUtil.getFlowPermAccountId(AppContext.currentAccountId(), summary.getOrgAccountId(), summary.getPermissionAccountId());
            Permission  permisson = permissionManager.getPermission(EnumNameEnum.col_flow_perm_policy.name(), ctpAffair.getNodePolicy(), flowPermAccountId);
            if (permisson != null) {
            	try {
            		
            		//节点权限设置不允许删除
	            	NodePolicyVO nodePolicyVo = new NodePolicyVO(permisson);
	            	boolean canReMove = nodePolicyVo.isReMove(); 
	            	if(!canReMove) {
	            		return ResourceUtil.getString("collaboration.alert.CantReMoveOne")+":"+ctpAffair.getSubject();
	            	}
            		
					if(Integer.valueOf(StateEnum.col_pending.getKey()).equals(ctpAffair.getState())){
						
						//模板不允许删除
						if(ctpAffair.getTempleteId()!=null) {
							return ResourceUtil.getString("collaboration.template.notHandle.notDeleteArchive")+":"+ctpAffair.getSubject();
						}
						
						//意见必填, 不允许直接归档和删除处理
						NodePolicy nodePolicy = permisson.getNodePolicy();
						Integer opinion = nodePolicy.getOpinionPolicy();
						boolean opinionIsNotNull = (opinion != null && opinion.intValue() == 1);
						
						if(opinionIsNotNull) {
							return ResourceUtil.getString("collaboration.template.notDeleteArchive.nullOpinion")+":"+ctpAffair.getSubject();
						}
						
						//校验锁
						String[] lockResult = wapi.lockWorkflowProcess(ctpAffair.getProcessId(), AppContext.currentUserId()+"", LockAction.Submit.getKey());
						if("false".equals(lockResult[0])) {
							return lockResult[1]+":"+ctpAffair.getSubject();//未获取到锁
						}
						
						//校验是否可以删除
						String[] canSubmitResult = wapi.canWorkflowCurrentNodeSubmit(ctpAffair.getSubObjectId()+"");
						if("false".equals(canSubmitResult[0])) {
							return canSubmitResult[1];
						}
						
						pageType = ColListType.pending.name();
						
					}else if(Integer.valueOf(StateEnum.col_sent.getKey()).equals(ctpAffair.getState())){
						pageType = ColListType.sent.name();
					}else if(Integer.valueOf(StateEnum.col_done.getKey()).equals(ctpAffair.getState()) || Integer.valueOf(StateEnum.col_stepStop.getKey()).equals(ctpAffair.getState())) {
						pageType = ColListType.finish.name();
					}else if(Integer.valueOf(StateEnum.col_waitSend.getKey()).equals(ctpAffair.getState())) {
						pageType = ColListType.draft.name();
					}
					
					//执行affair的删除
					colManager.deleteAffair(pageType, ctpAffair.getId());
				} catch (Exception e) {
					LOG.error("", e);
				}finally {
					//解锁
					wapi.releaseWorkFlowProcessLock(ctpAffair.getProcessId(), AppContext.currentUserId()+"", LockAction.Submit.getKey());
				}
            	
            }
		}
		return "";
	}

	 @Override	
	 public String transTakeBack(long affairId,boolean isSaveOpinion) throws BusinessException {
	 	String message = "";	
	 	
	 	try {
			message = canTakeBack(affairId);
			if(Strings.isNotBlank(message)){
				return message;
			}
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("affairId", affairId);
			params.put("isSaveOpinion", isSaveOpinion);
			message = colManager.transTakeBack(params);
		} finally {
			colManager.colDelLock(affairId);
		}
		
		return message;
	}
 
	 /**
	  * 
	  * @Title: canTakeBack   
	  * @Description: 校验是否可以取回
	  * @param affairId
	  * @return
	  * @throws Exception      
	  * @return: String  
	  * @date:   2019年5月20日 下午3:23:42
	  * @author: xusx
	  * @since   V7.1SP1	       
	  * @throws
	  */
	 private String canTakeBack(Long affairId) throws BusinessException {
		 
		 CtpAffair affair = affairManager.get(affairId);
		 
		 if(affair==null) {
			 throw new BusinessException("affair is null");
		 }
		 
		 ColSummary summary = this.getColSummary(affair.getObjectId());
		 
		 if(summary==null) {
			 throw new BusinessException("summary is null");
		 }
		 
		 //检验锁和状态是否正常
		 String checkMsg = colManager.checkAffairValid(affairId.toString(), affair.getNodePolicy());
		 
		 //工作流校验是否可以取回
		 if(Strings.isBlank(checkMsg)){
			 String wfCheck = wapi.canTakeBack(ApplicationCategoryEnum.collaboration.name(), toString(summary.getProcessId()),
					 toString(affair.getActivityId()), toString(affair.getSubObjectId()));
			 Map<String, String> wfCheckMap = JSONUtil.parseJSONString(wfCheck, Map.class);
			 if("false".equals(wfCheckMap.get("canTakeBack"))){
				 checkMsg = ResourceUtil.getString("collaboration.takeBackErr."+wfCheckMap.get("state")+".msg");
			 }
		 }
		 
		 //取回前加锁校验
		 if(Strings.isBlank(checkMsg)){
			 String[] lockResult = wapi.lockWorkflowProcess(affair.getProcessId(), AppContext.currentUserId()+"", LockAction.TackBack.getKey());
			 if("false".equals(lockResult[0])) {
				 checkMsg = lockResult[1];
			 }
		 }
		 return checkMsg;
	 }
}
